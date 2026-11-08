package com.datashop.web.dto;

import com.datashop.cart.CartCalculator;
import com.datashop.cart.CartLine;
import com.datashop.cart.CartTotals;
import com.datashop.domain.Cart;
import com.datashop.domain.CartItem;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.util.List;

@Value
@Builder
public class CartView {

    String id;
    List<CartItemView> items;
    int itemCount;
    BigDecimal subtotal;
    BigDecimal discount;
    BigDecimal total;
    CartPromoView promoCode;

    public static CartView from(Cart cart) {
        List<CartItemView> items = cart.getItems().stream()
                .filter(item -> item.getProduct() != null && item.getProduct().getDeletedAt() == null)
                .map(CartItemView::from)
                .toList();
        int itemCount = items.stream().mapToInt(CartItemView::getQuantity).sum();
        int discountPercentage = cart.getPromoCode() == null ? 0 : cart.getPromoCode().getDiscountPercentage();
        CartTotals totals = CartCalculator.calculate(
                items.stream().map(item -> new CartLine(item.getPrice(), item.getQuantity())).toList(),
                discountPercentage
        );
        return CartView.builder()
                .id(cart.getId().toString())
                .items(items)
                .itemCount(itemCount)
                .subtotal(totals.getSubtotal())
                .discount(totals.getDiscount())
                .total(totals.getTotal())
                .promoCode(cart.getPromoCode() == null
                        ? null
                        : CartPromoView.builder()
                        .code(cart.getPromoCode().getCode())
                        .discountPercentage(cart.getPromoCode().getDiscountPercentage())
                        .build())
                .build();
    }

    public static CartView empty() {
        CartTotals totals = CartTotals.empty();
        return CartView.builder()
                .items(List.of())
                .itemCount(0)
                .subtotal(totals.getSubtotal())
                .discount(totals.getDiscount())
                .total(totals.getTotal())
                .build();
    }
}
