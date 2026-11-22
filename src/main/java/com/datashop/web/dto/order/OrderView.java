package com.datashop.web.dto.order;

import com.datashop.domain.order.Order;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Getter
@Builder
@ToString
public final class OrderView {

    private final String id;
    private final String status;
    private final BigDecimal total;
    private final List<OrderItemView> items;
    private final Instant createdAt;

    public static OrderView from(Order order) {
        return OrderView.builder()
                .id(order.getId().toString())
                .status(order.getStatus().value())
                .total(order.getTotal())
                .items(order.getItems().stream().map(OrderItemView::from).toList())
                .createdAt(order.getCreatedAt())
                .build();
    }
}
