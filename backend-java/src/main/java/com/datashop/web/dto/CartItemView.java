package com.datashop.web.dto;

import com.datashop.cart.CartCalculator;
import com.datashop.domain.CartItem;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class CartItemView {

    String id;
    String productId;
    String title;
    BigDecimal price;
    int quantity;
    BigDecimal lineTotal;
    String image;
    int stockQuantity;

    public static CartItemView from(CartItem item) {
        BigDecimal price = item.getProduct().getPrice();
        return CartItemView.builder()
                .id(item.getId().toString())
                .productId(item.getProduct().getId().toString())
                .title(item.getProduct().getTitle())
                .price(price)
                .quantity(item.getQuantity())
                .lineTotal(CartCalculator.roundCurrency(price.multiply(BigDecimal.valueOf(item.getQuantity()))))
                .image(item.getProduct().getImages().isEmpty() ? null : item.getProduct().getImages().getFirst())
                .stockQuantity(item.getProduct().getStockQuantity())
                .build();
    }
}
