package com.datashop.cart;

import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@UtilityClass
public class CartCalculator {

    private final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    public BigDecimal roundCurrency(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    public CartTotals calculate(List<CartLine> lines, int discountPercentage) {
        BigDecimal subtotal = BigDecimal.ZERO;
        for (CartLine line : lines) {
            if (line == null || line.price() == null || line.quantity() <= 0) {
                continue;
            }
            subtotal = subtotal.add(line.price().multiply(BigDecimal.valueOf(line.quantity())));
        }
        subtotal = roundCurrency(subtotal);

        int normalized = Math.max(0, Math.min(100, discountPercentage));
        BigDecimal discount = roundCurrency(
                subtotal.multiply(BigDecimal.valueOf(normalized)).divide(HUNDRED, 4, RoundingMode.HALF_UP)
        );
        BigDecimal total = roundCurrency(subtotal.subtract(discount));
        return new CartTotals(subtotal, discount, total);
    }
}
