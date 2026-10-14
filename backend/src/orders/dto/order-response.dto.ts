import { ApiProperty } from '@nestjs/swagger';
import { OrderStatus } from '../../database/enums/order-status.enum.js';
import { OrderItem } from '../../database/entities/order-item.entity.js';
import { Order } from '../../database/entities/order.entity.js';
import { roundCurrency } from '../../cart/cart-calculator.js';

export class OrderItemResponse {
  @ApiProperty({ example: 'i1a2b3c4-d5e6-7f80-9a1b-2c3d4e5f6a7b' })
  id: string;

  @ApiProperty({
    example: '9b2c1d3e-4f5a-6b7c-8d9e-0f1a2b3c4d5e',
    nullable: true,
    description: 'Null when the product was deleted after purchase',
  })
  productId: string | null;

  @ApiProperty({ example: 'Акции Tesla 2019–2024' })
  title: string;

  @ApiProperty({ example: 1499.0 })
  unitPrice: number;

  @ApiProperty({ example: 2 })
  quantity: number;

  @ApiProperty({ example: 2998.0 })
  lineTotal: number;
}

export class OrderResponseDto {
  @ApiProperty({ example: 'o1a2b3c4-d5e6-7f80-9a1b-2c3d4e5f6a7b' })
  id: string;

  @ApiProperty({ enum: OrderStatus, example: OrderStatus.Paid })
  status: OrderStatus;

  @ApiProperty({ example: 2698.2 })
  total: number;

  @ApiProperty({ type: [OrderItemResponse] })
  items: OrderItemResponse[];

  @ApiProperty({ example: '2026-10-12T19:55:48.284Z', format: 'date-time' })
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
