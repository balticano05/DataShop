import { ApiProperty } from '@nestjs/swagger';
import { Cart } from '../../database/entities/cart.entity.js';
import {
  calculateCartTotals,
  roundCurrency,
} from '../cart-calculator.js';

export class CartItemResponse {
  @ApiProperty({ example: 'c1a2b3c4-d5e6-7f80-9a1b-2c3d4e5f6a7b' })
  id: string;

  @ApiProperty({ example: '9b2c1d3e-4f5a-6b7c-8d9e-0f1a2b3c4d5e' })
  productId: string;

  @ApiProperty({ example: 'Акции Tesla 2019–2024' })
  title: string;

  @ApiProperty({ example: 1499.0 })
  price: number;

  @ApiProperty({ example: 2 })
  quantity: number;

  @ApiProperty({ example: 2998.0 })
  lineTotal: number;

  @ApiProperty({
    example: 'https://placehold.co/800x600/1e293b/ffffff/png?text=TSLA&font=montserrat',
    nullable: true,
  })
  image: string | null;

  @ApiProperty({ example: 25 })
  stockQuantity: number;
}

export class CartPromoResponse {
  @ApiProperty({ example: 'SAVE10' })
  code: string;

  @ApiProperty({ example: 10 })
  discountPercentage: number;
}

export class CartResponseDto {
  @ApiProperty({ example: 'cart-0000-1111-2222-333344445555' })
  id: string;

  @ApiProperty({ type: [CartItemResponse] })
  items: CartItemResponse[];

  @ApiProperty({ example: 2, description: 'Total quantity of items' })
  itemCount: number;

  @ApiProperty({ example: 2998.0 })
  subtotal: number;

  @ApiProperty({ example: 299.8 })
  discount: number;

  @ApiProperty({ example: 2698.2 })
  total: number;

  @ApiProperty({ type: CartPromoResponse, nullable: true })
  promoCode: CartPromoResponse | null;

  static from(cart: Cart): CartResponseDto {
    const items = cart.items.map((item) => ({
      id: item.id,
      productId: item.productId,
      title: item.product.title,
      price: item.product.price,
      quantity: item.quantity,
      lineTotal: roundCurrency(item.product.price * item.quantity),
      image: item.product.images?.[0] ?? null,
      stockQuantity: item.product.stockQuantity,
    }));

    const discountPercentage = cart.promoCode?.discountPercentage ?? 0;
    const totals = calculateCartTotals(
      items.map((item) => ({ price: item.price, quantity: item.quantity })),
      discountPercentage,
    );

    return {
      id: cart.id,
      items,
      itemCount: items.reduce((count, item) => count + item.quantity, 0),
      subtotal: totals.subtotal,
      discount: totals.discount,
      total: totals.total,
      promoCode: cart.promoCode
        ? {
            code: cart.promoCode.code,
            discountPercentage: cart.promoCode.discountPercentage,
          }
        : null,
    };
  }
}
