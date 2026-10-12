export interface CartLine {
  price: number;
  quantity: number;
}

export interface CartTotals {
  subtotal: number;
  discount: number;
  total: number;
}

export function roundCurrency(value: number): number {
  return Math.round((value + Number.EPSILON) * 100) / 100;
}

export function calculateCartTotals(
  lines: CartLine[],
  discountPercentage = 0,
): CartTotals {
  const subtotal = roundCurrency(
    lines.reduce((sum, line) => sum + line.price * line.quantity, 0),
  );
  const normalizedPercentage = Math.min(Math.max(discountPercentage, 0), 100);
  const discount = roundCurrency((subtotal * normalizedPercentage) / 100);
  const total = roundCurrency(subtotal - discount);
  return { subtotal, discount, total };
}
