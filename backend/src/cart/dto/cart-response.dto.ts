import { Cart } from '../../database/entities/cart.entity.js';
import {
  calculateCartTotals,
  roundCurrency,
} from '../cart-calculator.js';

export interface CartItemResponse {
  id: string;
  productId: string;
  title: string;
  price: number;
  quantity: number;
  lineTotal: number;
  image: string | null;
  stockQuantity: number;
}

export interface CartPromoResponse {
  code: string;
  discountPercentage: number;
}

export class CartResponseDto {
  id: string;
  items: CartItemResponse[];
  itemCount: number;
  subtotal: number;
  discount: number;
  total: number;
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
