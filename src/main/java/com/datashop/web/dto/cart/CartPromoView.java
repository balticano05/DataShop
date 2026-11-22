package com.datashop.web.dto.cart;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public final class CartPromoView {

    private final String code;
    private final int discountPercentage;
}
