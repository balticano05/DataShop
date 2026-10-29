package com.datashop.cart;

import java.math.BigDecimal;

public record CartLine(BigDecimal price, int quantity) {
}
