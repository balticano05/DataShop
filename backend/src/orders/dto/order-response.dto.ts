import { OrderStatus } from '../../database/enums/order-status.enum.js';
import { OrderItem } from '../../database/entities/order-item.entity.js';
import { Order } from '../../database/entities/order.entity.js';
import { roundCurrency } from '../../cart/cart-calculator.js';

export interface OrderItemResponse {
  id: string;
  productId: string | null;
  title: string;
  unitPrice: number;
  quantity: number;
  lineTotal: number;
}

export class OrderResponseDto {
  id: string;
  status: OrderStatus;
  total: number;
  items: OrderItemResponse[];
  createdAt: Date;

  static from(order: Order): OrderResponseDto {
    const items = (order.items ?? []).map((item: OrderItem) => ({
      id: item.id,
      productId: item.productId,
      title: item.titleSnapshot,
      unitPrice: item.unitPrice,
      quantity: item.quantity,
      lineTotal: roundCurrency(item.unitPrice * item.quantity),
    }));

    return {
      id: order.id,
      status: order.status,
      total: order.total,
      items,
      createdAt: order.createdAt,
    };
  }
}
