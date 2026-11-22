package com.datashop.util.cart;

import com.datashop.domain.cart.CartLine;
import com.datashop.domain.cart.CartTotals;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CartCalculatorTest {

    @Test
    void calculatesSubtotalDiscountAndTotal() {
        List<CartLine> lines = List.of(
                new CartLine(new BigDecimal("1000.00"), 2),
                new CartLine(new BigDecimal("500.50"), 1)
        );

        CartTotals totals = CartCalculator.calculate(lines, 10);

        assertThat(totals.subtotal()).isEqualByComparingTo("2500.50");
        assertThat(totals.discount()).isEqualByComparingTo("250.05");
        assertThat(totals.total()).isEqualByComparingTo("2250.45");
    }

    @Test
    void ignoresInvalidLines() {
        List<CartLine> lines = List.of(
                new CartLine(new BigDecimal("100.00"), 0),
                new CartLine(null, 2),
                new CartLine(new BigDecimal("200.00"), 1)
        );

        CartTotals totals = CartCalculator.calculate(lines, 0);

        assertThat(totals.subtotal()).isEqualByComparingTo("200.00");
        assertThat(totals.total()).isEqualByComparingTo("200.00");
    }

    @Test
    void clampsDiscountToRange() {
        CartTotals totals = CartCalculator.calculate(
                List.of(new CartLine(new BigDecimal("100.00"), 1)), 150);

        assertThat(totals.discount()).isEqualByComparingTo("100.00");
        assertThat(totals.total()).isEqualByComparingTo("0.00");
    }

    @Test
    void emptyCartIsZero() {
        CartTotals totals = CartCalculator.calculate(List.of(), 10);

        assertThat(totals.subtotal()).isEqualByComparingTo("0.00");
        assertThat(totals.discount()).isEqualByComparingTo("0.00");
        assertThat(totals.total()).isEqualByComparingTo("0.00");
    }
}
