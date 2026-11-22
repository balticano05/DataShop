package com.datashop.domain.cart;

import java.math.BigDecimal;

public record CartLine(BigDecimal price, int quantity) {
}
