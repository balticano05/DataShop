package com.datashop.domain.cart;

import java.math.BigDecimal;

public record CartTotals(BigDecimal subtotal, BigDecimal discount, BigDecimal total) {
}
