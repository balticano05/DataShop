package com.datashop.util.cart;

import com.datashop.domain.cart.CartLine;
import com.datashop.domain.cart.CartTotals;
import com.datashop.util.Constants;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@UtilityClass
public class CartCalculator {

    private final BigDecimal HUNDRED = BigDecimal.valueOf(Constants.Cart.PERCENT_BASE);

    public BigDecimal roundCurrency(BigDecimal value) {
        return value.setScale(Constants.Cart.CURRENCY_SCALE, RoundingMode.HALF_UP);
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

        int normalized = Math.max(
                Constants.Cart.MIN_DISCOUNT_PERCENT,
                Math.min(Constants.Cart.MAX_DISCOUNT_PERCENT, discountPercentage));
        BigDecimal discount = roundCurrency(
                subtotal.multiply(BigDecimal.valueOf(normalized))
                        .divide(HUNDRED, Constants.Cart.DIVISION_SCALE, RoundingMode.HALF_UP)
        );
        BigDecimal total = roundCurrency(subtotal.subtract(discount));
        return new CartTotals(subtotal, discount, total);
    }
}
