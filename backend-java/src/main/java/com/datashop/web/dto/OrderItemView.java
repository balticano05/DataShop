package com.datashop.web.dto;

import com.datashop.cart.CartCalculator;
import com.datashop.domain.OrderItem;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class OrderItemView {

    String id;
    String productId;
    String title;
    BigDecimal unitPrice;
    int quantity;
    BigDecimal lineTotal;

    public static OrderItemView from(OrderItem item) {
        return OrderItemView.builder()
                .id(item.getId().toString())
                .productId(item.getProduct() == null ? null : item.getProduct().getId().toString())
                .title(item.getTitleSnapshot())
                .unitPrice(item.getUnitPrice())
                .quantity(item.getQuantity())
                .lineTotal(CartCalculator.roundCurrency(
                        item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()))))
                .build();
    }
}
