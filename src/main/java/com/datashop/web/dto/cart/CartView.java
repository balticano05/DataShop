package com.datashop.web.dto.cart;

import com.datashop.domain.cart.Cart;
import com.datashop.domain.cart.CartLine;
import com.datashop.domain.cart.CartTotals;
import com.datashop.domain.promo.PromoCode;
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
public final class CartView {

    private final String id;
    private final List<CartItemView> items;
    private final int itemCount;
    private final BigDecimal subtotal;
    private final BigDecimal discount;
    private final BigDecimal total;
    private final CartPromoView promoCode;

    public static CartView from(Cart cart) {
        List<CartItemView> items = cart.getItems().stream()
                .filter(item -> Optional.ofNullable(item.getProduct())
                        .filter(product -> product.getDeletedAt() == null)
                        .isPresent())
                .map(CartItemView::from)
                .toList();
        int itemCount = items.stream().mapToInt(CartItemView::getQuantity).sum();
        int discountPercentage = Optional.ofNullable(cart.getPromoCode())
                .map(PromoCode::getDiscountPercentage)
                .orElse(0);
        CartTotals totals = CartCalculator.calculate(
                items.stream().map(item -> new CartLine(item.getPrice(), item.getQuantity())).toList(),
                discountPercentage
        );
        return CartView.builder()
                .id(cart.getId().toString())
                .items(items)
                .itemCount(itemCount)
                .subtotal(totals.subtotal())
                .discount(totals.discount())
                .total(totals.total())
                .promoCode(Optional.ofNullable(cart.getPromoCode())
                        .map(promo -> CartPromoView.builder()
                                .code(promo.getCode())
                                .discountPercentage(promo.getDiscountPercentage())
                                .build())
                        .orElse(null))
                .build();
    }
}
