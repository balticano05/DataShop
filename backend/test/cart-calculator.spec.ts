import { describe, expect, it } from 'vitest';
import { calculateCartTotals } from '../src/cart/cart-calculator';

describe('calculateCartTotals', () => {
  it('sums line totals into subtotal without discount', () => {
    const totals = calculateCartTotals([
      { price: 100, quantity: 2 },
      { price: 50, quantity: 1 },
    ]);
    expect(totals).toEqual({ subtotal: 250, discount: 0, total: 250 });
  });

  it('applies a discount of exactly 10% (task 88)', () => {
    const totals = calculateCartTotals([{ price: 1000, quantity: 1 }], 10);
    expect(totals.discount).toBe(100);
    expect(totals.total).toBe(900);
  });

  it('keeps totals consistent across many lines', () => {
    const totals = calculateCartTotals(
      [
        { price: 1499, quantity: 3 },
        { price: 899, quantity: 2 },
      ],
      10,
    );
    expect(totals.subtotal).toBe(6295);
    expect(totals.discount).toBe(629.5);
    expect(totals.total).toBe(5665.5);
  });

  it('returns an empty result for an empty cart', () => {
    expect(calculateCartTotals([], 10)).toEqual({
      subtotal: 0,
      discount: 0,
      total: 0,
    });
  });

  it('clamps discount percentage to the 0..100 range', () => {
    expect(calculateCartTotals([{ price: 100, quantity: 1 }], 150).discount).toBe(
      100,
    );
    expect(calculateCartTotals([{ price: 100, quantity: 1 }], -5).discount).toBe(
      0,
    );
  });
});
