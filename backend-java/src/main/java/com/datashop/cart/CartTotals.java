package com.datashop.cart;

import lombok.Value;

import java.math.BigDecimal;

@Value
public class CartTotals {

    BigDecimal subtotal;
    BigDecimal discount;
    BigDecimal total;

    public static CartTotals empty() {
        return new CartTotals(BigDecimal.ZERO.setScale(2), BigDecimal.ZERO.setScale(2), BigDecimal.ZERO.setScale(2));
    }
}
