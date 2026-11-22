package com.datashop.service.order;

import com.datashop.util.Constants;
import com.datashop.web.dto.common.ErrorResponse;
import com.datashop.web.dto.order.OrderView;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiParam;
import io.javalin.openapi.OpenApiResponse;
import io.javalin.openapi.OpenApiSecurity;

import java.util.List;
import java.util.UUID;

public interface OrderService {

    @OpenApi(
            path = Constants.Routes.API_CHECKOUT,
            methods = HttpMethod.POST,
            operationId = "checkout",
            tags = Constants.Swagger.TAG_ORDERS,
            summary = "Оформить заказ из корзины",
            security = @OpenApiSecurity(name = Constants.Swagger.BEARER_AUTH),
            responses = {
                    @OpenApiResponse(
                            status = "201",
                            description = "Заказ создан",
                            content = @OpenApiContent(from = OrderView.class)
                    ),
                    @OpenApiResponse(
                            status = "400",
                            description = "Корзина пуста",
                            content = @OpenApiContent(from = ErrorResponse.class)
                    ),
                    @OpenApiResponse(
                            status = "401",
                            description = "Требуется аутентификация",
                            content = @OpenApiContent(from = ErrorResponse.class)
                    ),
                    @OpenApiResponse(
                            status = "409",
                            description = "Недостаточно товара на складе",
                            content = @OpenApiContent(from = ErrorResponse.class)
                    ),
                    @OpenApiResponse(
                            status = "429",
                            description = "Слишком много запросов",
                            content = @OpenApiContent(from = ErrorResponse.class)
                    )
            }
    )
    OrderView checkout(UUID userId);

    @OpenApi(
            path = Constants.Routes.API_ORDERS,
            methods = HttpMethod.GET,
            operationId = "listOrders",
            tags = Constants.Swagger.TAG_ORDERS,
            summary = "История заказов текущего пользователя",
            security = @OpenApiSecurity(name = Constants.Swagger.BEARER_AUTH),
            responses = {
                    @OpenApiResponse(
                            status = "200",
                            description = "Заказы",
                            content = @OpenApiContent(from = OrderView[].class)
                    ),
                    @OpenApiResponse(
                            status = "401",
                            description = "Требуется аутентификация",
                            content = @OpenApiContent(from = ErrorResponse.class)
                    )
            }
    )
    List<OrderView> list(UUID userId);

    @OpenApi(
            path = Constants.Routes.API_ORDER_BY_ID,
            methods = HttpMethod.GET,
            operationId = "getOrder",
            tags = Constants.Swagger.TAG_ORDERS,
            summary = "Заказ по id",
            security = @OpenApiSecurity(name = Constants.Swagger.BEARER_AUTH),
            pathParams = @OpenApiParam(
                    name = Constants.Params.ID,
                    type = UUID.class,
                    required = true,
                    description = "Идентификатор заказа (UUID)"
            ),
            responses = {
                    @OpenApiResponse(
                            status = "200",
                            description = "Заказ",
                            content = @OpenApiContent(from = OrderView.class)
                    ),
                    @OpenApiResponse(
                            status = "401",
                            description = "Требуется аутентификация",
                            content = @OpenApiContent(from = ErrorResponse.class)
                    ),
                    @OpenApiResponse(
                            status = "404",
                            description = "Заказ не найден",
                            content = @OpenApiContent(from = ErrorResponse.class)
                    )
            }
    )
    OrderView get(UUID userId, String rawOrderId);

    boolean hasPurchased(UUID userId, UUID productId);
}
