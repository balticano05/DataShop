import {
  BadRequestException,
  ConflictException,
  Injectable,
  NotFoundException,
} from '@nestjs/common';
import { InjectDataSource } from '@nestjs/typeorm';
import { DataSource, EntityManager } from 'typeorm';
import { calculateCartTotals } from '../cart/cart-calculator.js';
import { CatalogCacheService } from '../cache/catalog-cache.service.js';
import { CartItem } from '../database/entities/cart-item.entity.js';
import { Cart } from '../database/entities/cart.entity.js';
import { OrderItem } from '../database/entities/order-item.entity.js';
import { Order } from '../database/entities/order.entity.js';
import { PromoCode } from '../database/entities/promo-code.entity.js';
import { OrderStatus } from '../database/enums/order-status.enum.js';
import { OrderResponseDto } from './dto/order-response.dto.js';
import { PaymentsService } from './payments.service.js';

@Injectable()
export class OrdersService {
  constructor(
    @InjectDataSource()
    private readonly dataSource: DataSource,
    private readonly paymentsService: PaymentsService,
    private readonly catalogCache: CatalogCacheService,
  ) {}

  async checkout(userId: string): Promise<OrderResponseDto> {
    const order = await this.dataSource.transaction((manager) =>
      this.runCheckout(manager, userId),
    );

    await this.catalogCache.invalidateAll();

    return OrderResponseDto.from(order);
  }

  async findMine(userId: string): Promise<OrderResponseDto[]> {
    const orders = await this.dataSource.getRepository(Order).find({
      where: { userId },
      relations: { items: true },
      order: { createdAt: 'DESC' },
    });
    return orders.map((order) => OrderResponseDto.from(order));
  }

  async findOne(userId: string, orderId: string): Promise<OrderResponseDto> {
    const order = await this.dataSource.getRepository(Order).findOne({
      where: { id: orderId, userId },
      relations: { items: true },
    });
    if (!order) {
      throw new NotFoundException('Order not found');
    }
    return OrderResponseDto.from(order);
  }

  private async runCheckout(
    manager: EntityManager,
    userId: string,
  ): Promise<Order> {
    const cart = await manager.findOne(Cart, { where: { userId } });
    if (!cart) {
      throw new NotFoundException('Cart not found');
    }

    const items = await manager.find(CartItem, {
      where: { cartId: cart.id },
      relations: { product: true },
    });
    if (items.length === 0) {
      throw new BadRequestException('Cart is empty');
    }

    const discountPercentage = await this.resolveDiscount(
      manager,
      cart.promoCodeId,
    );
    const totals = calculateCartTotals(
      items.map((item) => ({
        price: item.product.price,
        quantity: item.quantity,
      })),
      discountPercentage,
    );

    const orderedItems = [...items].sort((a, b) =>
      a.productId.localeCompare(b.productId),
    );
    for (const item of orderedItems) {
      await this.decrementStock(manager, item.productId, item.quantity, item.product.title);
    }

    const payment = await this.paymentsService.charge(totals.total);
    if (!payment.success) {
      throw new BadRequestException('Payment failed');
    }

    const order = await manager.save(
      manager.create(Order, {
        userId,
        status: OrderStatus.Paid,
        total: totals.total,
      }),
    );

    order.items = await manager.save(
      items.map((item) =>
        manager.create(OrderItem, {
          orderId: order.id,
          productId: item.productId,
          quantity: item.quantity,
          unitPrice: item.product.price,
          titleSnapshot: item.product.title,
        }),
      ),
    );

    await manager.delete(CartItem, { cartId: cart.id });
    await manager.update(Cart, cart.id, { promoCodeId: null });

    return order;
  }

  private async resolveDiscount(
    manager: EntityManager,
    promoCodeId: string | null,
  ): Promise<number> {
    if (!promoCodeId) {
      return 0;
    }
    const promo = await manager.findOne(PromoCode, {
      where: { id: promoCodeId },
    });
    return promo?.isActive ? promo.discountPercentage : 0;
  }

  private async decrementStock(
    manager: EntityManager,
    productId: string,
    quantity: number,
    title: string,
  ): Promise<void> {
    const [updated]: [Array<{ id: string }>, number] = await manager.query(
      `UPDATE products
         SET stock_quantity = stock_quantity - $1,
             updated_at = now()
       WHERE id = $2
         AND deleted_at IS NULL
         AND stock_quantity >= $1
       RETURNING id`,
      [quantity, productId],
    );
    if (updated.length === 0) {
      throw new ConflictException(`Insufficient stock for "${title}"`);
    }
  }
}
