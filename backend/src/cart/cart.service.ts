import {
  ConflictException,
  Injectable,
  NotFoundException,
} from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { CartItem } from '../database/entities/cart-item.entity.js';
import { Cart } from '../database/entities/cart.entity.js';
import { Product } from '../database/entities/product.entity.js';
import { PromoService } from '../promo/promo.service.js';
import { AddCartItemDto } from './dto/add-cart-item.dto.js';
import { ApplyPromoDto } from './dto/apply-promo.dto.js';
import { CartResponseDto } from './dto/cart-response.dto.js';
import { UpdateCartItemDto } from './dto/update-cart-item.dto.js';

@Injectable()
export class CartService {
  constructor(
    @InjectRepository(Cart)
    private readonly cartsRepository: Repository<Cart>,
    @InjectRepository(CartItem)
    private readonly cartItemsRepository: Repository<CartItem>,
    @InjectRepository(Product)
    private readonly productsRepository: Repository<Product>,
    private readonly promoService: PromoService,
  ) {}

  async getCart(userId: string): Promise<CartResponseDto> {
    const cart = await this.loadCart(userId);
    return CartResponseDto.from(cart);
  }

  async addItem(userId: string, dto: AddCartItemDto): Promise<CartResponseDto> {
    const cart = await this.getOrCreateCart(userId);
    const product = await this.findProduct(dto.productId);

    const existing = await this.cartItemsRepository.findOne({
      where: { cartId: cart.id, productId: product.id },
    });
    const nextQuantity = (existing?.quantity ?? 0) + dto.quantity;
    this.assertStock(nextQuantity, product);

    if (existing) {
      existing.quantity = nextQuantity;
      await this.cartItemsRepository.save(existing);
    } else {
      await this.cartItemsRepository.save(
        this.cartItemsRepository.create({
          cartId: cart.id,
          productId: product.id,
          quantity: dto.quantity,
        }),
      );
    }

    return this.getCart(userId);
  }

  async updateItem(
    userId: string,
    itemId: string,
    dto: UpdateCartItemDto,
  ): Promise<CartResponseDto> {
    const { item } = await this.findOwnedItem(userId, itemId);
    const product = await this.findProduct(item.productId);
    this.assertStock(dto.quantity, product);

    item.quantity = dto.quantity;
    await this.cartItemsRepository.save(item);

    return this.getCart(userId);
  }

  async removeItem(userId: string, itemId: string): Promise<CartResponseDto> {
    const { item } = await this.findOwnedItem(userId, itemId);
    await this.cartItemsRepository.remove(item);
    return this.getCart(userId);
  }

  async applyPromo(
    userId: string,
    dto: ApplyPromoDto,
  ): Promise<CartResponseDto> {
    const cart = await this.getOrCreateCart(userId);
    const promo = await this.promoService.findValid(dto.code);
    cart.promoCodeId = promo.id;
    await this.cartsRepository.save(cart);
    return this.getCart(userId);
  }

  async removePromo(userId: string): Promise<CartResponseDto> {
    const cart = await this.getOrCreateCart(userId);
    cart.promoCodeId = null;
    await this.cartsRepository.save(cart);
    return this.getCart(userId);
  }

  private async getOrCreateCart(userId: string): Promise<Cart> {
    const existing = await this.cartsRepository.findOne({ where: { userId } });
    if (existing) {
      return existing;
    }
    return this.cartsRepository.save(this.cartsRepository.create({ userId }));
  }

  private async loadCart(userId: string): Promise<Cart> {
    await this.getOrCreateCart(userId);
    const cart = await this.cartsRepository.findOne({
      where: { userId },
      relations: { items: { product: true }, promoCode: true },
    });
    if (!cart) {
      throw new NotFoundException('Cart not found');
    }
    return cart;
  }

  private async findProduct(productId: string): Promise<Product> {
    const product = await this.productsRepository.findOne({
      where: { id: productId },
    });
    if (!product) {
      throw new NotFoundException('Product not found');
    }
    return product;
  }

  private async findOwnedItem(
    userId: string,
    itemId: string,
  ): Promise<{ item: CartItem }> {
    const item = await this.cartItemsRepository.findOne({
      where: { id: itemId },
      relations: { cart: true },
    });
    if (!item || item.cart.userId !== userId) {
      throw new NotFoundException('Cart item not found');
    }
    return { item };
  }

  private assertStock(quantity: number, product: Product): void {
    if (quantity > product.stockQuantity) {
      throw new ConflictException(
        `Insufficient stock for "${product.title}": available ${product.stockQuantity}`,
      );
    }
  }
}
