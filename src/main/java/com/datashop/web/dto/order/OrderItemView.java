package com.datashop.web.dto.order;

import com.datashop.domain.order.OrderItem;
import com.datashop.util.cart.CartCalculator;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.Optional;

@Getter
@Builder
@ToString
public final class OrderItemView {

    private final String id;
    private final String productId;
    private final String title;
    private final BigDecimal unitPrice;
    private final int quantity;
    private final BigDecimal lineTotal;

    public static OrderItemView from(OrderItem item) {
        return OrderItemView.builder()
                .id(item.getId().toString())
                .productId(Optional.ofNullable(item.getProduct())
                        .map(product -> product.getId().toString())
                        .orElse(null))
                .title(item.getTitleSnapshot())
                .unitPrice(item.getUnitPrice())
                .quantity(item.getQuantity())
                .lineTotal(CartCalculator.roundCurrency(
                        item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()))))
                .build();
    }
}
