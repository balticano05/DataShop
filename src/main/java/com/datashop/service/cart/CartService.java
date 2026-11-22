package com.datashop.service.cart;

import com.datashop.util.Constants;
import com.datashop.web.dto.cart.AddCartItemRequest;
import com.datashop.web.dto.cart.ApplyPromoRequest;
import com.datashop.web.dto.cart.CartView;
import com.datashop.web.dto.cart.UpdateCartItemRequest;
import com.datashop.web.dto.common.ErrorResponse;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiParam;
import io.javalin.openapi.OpenApiRequestBody;
import io.javalin.openapi.OpenApiResponse;
import io.javalin.openapi.OpenApiSecurity;

import java.util.UUID;

public interface CartService {

    @OpenApi(
            path = Constants.Routes.API_CART,
            methods = HttpMethod.GET,
            operationId = "getCart",
            tags = Constants.Swagger.TAG_CART,
            summary = "Корзина текущего пользователя",
            security = @OpenApiSecurity(name = Constants.Swagger.BEARER_AUTH),
            responses = {
                    @OpenApiResponse(
                            status = "200",
                            description = "Корзина",
                            content = @OpenApiContent(from = CartView.class)
                    ),
                    @OpenApiResponse(
                            status = "401",
                            description = "Требуется аутентификация",
                            content = @OpenApiContent(from = ErrorResponse.class)
                    )
            }
    )
    CartView get(UUID userId);

    @OpenApi(
            path = Constants.Routes.API_CART_ITEMS,
            methods = HttpMethod.POST,
            operationId = "addCartItem",
            tags = Constants.Swagger.TAG_CART,
            summary = "Добавить товар в корзину",
            security = @OpenApiSecurity(name = Constants.Swagger.BEARER_AUTH),
            requestBody = @OpenApiRequestBody(
                    required = true,
                    content = @OpenApiContent(from = AddCartItemRequest.class)
            ),
            responses = {
                    @OpenApiResponse(
                            status = "201",
                            description = "Корзина после добавления",
                            content = @OpenApiContent(from = CartView.class)
                    ),
                    @OpenApiResponse(
                            status = "400",
                            description = "Некорректный запрос",
                            content = @OpenApiContent(from = ErrorResponse.class)
                    ),
                    @OpenApiResponse(
                            status = "401",
                            description = "Требуется аутентификация",
                            content = @OpenApiContent(from = ErrorResponse.class)
                    ),
                    @OpenApiResponse(
                            status = "404",
                            description = "Товар не найден",
                            content = @OpenApiContent(from = ErrorResponse.class)
                    ),
                    @OpenApiResponse(
                            status = "409",
                            description = "Недостаточно товара на складе",
                            content = @OpenApiContent(from = ErrorResponse.class)
                    )
            }
    )
    CartView addItem(UUID userId, String rawProductId, Integer quantity);

    @OpenApi(
            path = Constants.Routes.API_CART_ITEM_BY_ID,
            methods = HttpMethod.PATCH,
            operationId = "updateCartItem",
            tags = Constants.Swagger.TAG_CART,
            summary = "Изменить количество",
            security = @OpenApiSecurity(name = Constants.Swagger.BEARER_AUTH),
            pathParams = @OpenApiParam(
                    name = Constants.Params.ID,
                    type = UUID.class,
                    required = true,
                    description = "Идентификатор позиции корзины (UUID)"
            ),
            requestBody = @OpenApiRequestBody(
                    required = true,
                    content = @OpenApiContent(from = UpdateCartItemRequest.class)
            ),
            responses = {
                    @OpenApiResponse(
                            status = "200",
                            description = "Корзина после изменения",
                            content = @OpenApiContent(from = CartView.class)
                    ),
                    @OpenApiResponse(
                            status = "400",
                            description = "Некорректное количество",
                            content = @OpenApiContent(from = ErrorResponse.class)
                    ),
                    @OpenApiResponse(
                            status = "401",
                            description = "Требуется аутентификация",
                            content = @OpenApiContent(from = ErrorResponse.class)
                    ),
                    @OpenApiResponse(
                            status = "404",
                            description = "Позиция не найдена",
                            content = @OpenApiContent(from = ErrorResponse.class)
                    ),
                    @OpenApiResponse(
                            status = "409",
                            description = "Недостаточно товара на складе",
                            content = @OpenApiContent(from = ErrorResponse.class)
                    )
            }
    )
    CartView updateItem(UUID userId, String rawItemId, Integer quantity);

    @OpenApi(
            path = Constants.Routes.API_CART_ITEM_BY_ID,
            methods = HttpMethod.DELETE,
            operationId = "removeCartItem",
            tags = Constants.Swagger.TAG_CART,
            summary = "Удалить позицию",
            security = @OpenApiSecurity(name = Constants.Swagger.BEARER_AUTH),
            pathParams = @OpenApiParam(
                    name = Constants.Params.ID,
                    type = UUID.class,
                    required = true,
                    description = "Идентификатор позиции корзины (UUID)"
            ),
            responses = {
                    @OpenApiResponse(
                            status = "200",
                            description = "Корзина после удаления",
                            content = @OpenApiContent(from = CartView.class)
                    ),
                    @OpenApiResponse(
                            status = "401",
                            description = "Требуется аутентификация",
                            content = @OpenApiContent(from = ErrorResponse.class)
                    ),
                    @OpenApiResponse(
                            status = "404",
                            description = "Позиция не найдена",
                            content = @OpenApiContent(from = ErrorResponse.class)
                    )
            }
    )
    CartView removeItem(UUID userId, String rawItemId);

    @OpenApi(
            path = Constants.Routes.API_CART_PROMO,
            methods = HttpMethod.POST,
            operationId = "applyPromo",
            tags = Constants.Swagger.TAG_CART,
            summary = "Применить промокод",
            security = @OpenApiSecurity(name = Constants.Swagger.BEARER_AUTH),
            requestBody = @OpenApiRequestBody(
                    required = true,
                    content = @OpenApiContent(from = ApplyPromoRequest.class)
            ),
            responses = {
                    @OpenApiResponse(
                            status = "201",
                            description = "Промокод применён",
                            content = @OpenApiContent(from = CartView.class)
                    ),
                    @OpenApiResponse(
                            status = "400",
                            description = "Некорректный промокод",
                            content = @OpenApiContent(from = ErrorResponse.class)
                    ),
                    @OpenApiResponse(
                            status = "401",
                            description = "Требуется аутентификация",
                            content = @OpenApiContent(from = ErrorResponse.class)
                    ),
                    @OpenApiResponse(
                            status = "404",
                            description = "Промокод не найден",
                            content = @OpenApiContent(from = ErrorResponse.class)
                    )
            }
    )
    CartView applyPromo(UUID userId, String rawCode);

    @OpenApi(
            path = Constants.Routes.API_CART_PROMO,
            methods = HttpMethod.DELETE,
            operationId = "removePromo",
            tags = Constants.Swagger.TAG_CART,
            summary = "Сбросить промокод",
            security = @OpenApiSecurity(name = Constants.Swagger.BEARER_AUTH),
            responses = {
                    @OpenApiResponse(
                            status = "200",
                            description = "Промокод сброшен",
                            content = @OpenApiContent(from = CartView.class)
                    ),
                    @OpenApiResponse(
                            status = "401",
                            description = "Требуется аутентификация",
                            content = @OpenApiContent(from = ErrorResponse.class)
                    )
            }
    )
    CartView removePromo(UUID userId);
}
