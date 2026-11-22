package com.datashop.web.dto.cart;

import com.datashop.domain.cart.CartItem;
import com.datashop.util.cart.CartCalculator;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Getter
@Builder
@ToString
public final class CartItemView {

    private final String id;
    private final String productId;
    private final String title;
    private final BigDecimal price;
    private final int quantity;
    private final BigDecimal lineTotal;
    private final String image;
    private final int stockQuantity;

    public static CartItemView from(CartItem item) {
        BigDecimal price = item.getProduct().getPrice();
        List<String> images = item.getProduct().getImages();
        return CartItemView.builder()
                .id(item.getId().toString())
                .productId(item.getProduct().getId().toString())
                .title(item.getProduct().getTitle())
                .price(price)
                .quantity(item.getQuantity())
                .lineTotal(CartCalculator.roundCurrency(price.multiply(BigDecimal.valueOf(item.getQuantity()))))
                .image(Optional.ofNullable(images)
                        .flatMap(values -> values.stream().findFirst())
                        .orElse(null))
                .stockQuantity(item.getProduct().getStockQuantity())
                .build();
    }
}
