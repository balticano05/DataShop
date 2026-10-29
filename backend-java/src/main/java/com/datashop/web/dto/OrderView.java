package com.datashop.web.dto;

import com.datashop.domain.Order;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Value
@Builder
public class OrderView {

    String id;
    String status;
    BigDecimal total;
    List<OrderItemView> items;
    Instant createdAt;

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
