package com.datashop.service.catalog;

import com.datashop.domain.product.Product;
import com.datashop.domain.product.ProductQuery;
import com.datashop.util.Constants;
import com.datashop.web.dto.common.ErrorResponse;
import com.datashop.web.dto.product.CreateProductRequest;
import com.datashop.web.dto.product.ProductView;
import com.datashop.web.dto.product.ProductsResponse;
import com.datashop.web.dto.product.UpdateProductRequest;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiParam;
import io.javalin.openapi.OpenApiRequestBody;
import io.javalin.openapi.OpenApiResponse;
import io.javalin.openapi.OpenApiSecurity;

import java.util.UUID;

public interface ProductService {

    @OpenApi(
            path = Constants.Routes.API_PRODUCTS,
            methods = HttpMethod.GET,
            operationId = "listProducts",
            tags = Constants.Swagger.TAG_PRODUCTS,
            summary = "Каталог с фильтрами, сортировкой и пагинацией",
            queryParams = {
                    @OpenApiParam(
                            name = Constants.Params.PAGE,
                            type = Integer.class,
                            description = "Номер страницы, начиная с 1 (по умолчанию 1)"
                    ),
                    @OpenApiParam(
                            name = Constants.Params.LIMIT,
                            type = Integer.class,
                            description = "Размер страницы, от 1 до 100 (по умолчанию 12)"
                    ),
                    @OpenApiParam(
                            name = Constants.Params.SEARCH,
                            type = String.class,
                            description = "Поиск по названию и описанию, до 100 символов"
                    ),
                    @OpenApiParam(
                            name = Constants.Params.MIN_PRICE,
                            type = Double.class,
                            description = "Минимальная цена, от 0"
                    ),
                    @OpenApiParam(
                            name = Constants.Params.MAX_PRICE,
                            type = Double.class,
                            description = "Максимальная цена, от 0, не меньше min_price"
                    ),
                    @OpenApiParam(
                            name = Constants.Params.MIN_RATING,
                            type = Double.class,
                            description = "Минимальный рейтинг, от 0 до 5"
                    ),
                    @OpenApiParam(
                            name = Constants.Params.SORT_BY,
                            type = String.class,
                            description = "Поле сортировки: price, title, rating или created_at (по умолчанию created_at)"
                    ),
                    @OpenApiParam(
                            name = Constants.Params.ORDER,
                            type = String.class,
                            description = "Направление сортировки: asc или desc (по умолчанию desc)"
                    )
            },
            responses = {
                    @OpenApiResponse(
                            status = "200",
                            description = "Страница каталога; заголовок X-Cache: HIT или MISS",
                            headers = @OpenApiParam(
                                    name = Constants.Http.CACHE_HEADER,
                                    type = String.class,
                                    description = "Признак попадания в кэш"
                            ),
                            content = @OpenApiContent(from = ProductsResponse.class)
                    ),
                    @OpenApiResponse(
                            status = "400",
                            description = "Некорректные параметры запроса",
                            content = @OpenApiContent(from = ErrorResponse.class)
                    )
            }
    )
    CatalogResult list(ProductQuery query);

    @OpenApi(
            path = Constants.Routes.API_PRODUCT_BY_ID,
            methods = HttpMethod.GET,
            operationId = "getProduct",
            tags = Constants.Swagger.TAG_PRODUCTS,
            summary = "Товар по id",
            pathParams = @OpenApiParam(
                    name = Constants.Params.ID,
                    type = UUID.class,
                    required = true,
                    description = "Идентификатор товара (UUID)"
            ),
            responses = {
                    @OpenApiResponse(
                            status = "200",
                            description = "Товар",
                            content = @OpenApiContent(from = ProductView.class)
                    ),
                    @OpenApiResponse(
                            status = "404",
                            description = "Товар не найден",
                            content = @OpenApiContent(from = ErrorResponse.class)
                    )
            }
    )
    ProductView get(String rawId);

    @OpenApi(
            path = Constants.Routes.API_PRODUCTS,
            methods = HttpMethod.POST,
            operationId = "createProduct",
            tags = Constants.Swagger.TAG_PRODUCTS,
            summary = "Создать товар",
            security = @OpenApiSecurity(name = Constants.Swagger.BEARER_AUTH),
            requestBody = @OpenApiRequestBody(
                    required = true,
                    content = @OpenApiContent(from = CreateProductRequest.class)
            ),
            responses = {
                    @OpenApiResponse(
                            status = "201",
                            description = "Товар создан",
                            content = @OpenApiContent(from = ProductView.class)
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
                            status = "403",
                            description = "Недостаточно прав",
                            content = @OpenApiContent(from = ErrorResponse.class)
                    )
            }
    )
    ProductView create(CreateProductRequest request);

    @OpenApi(
            path = Constants.Routes.API_PRODUCT_BY_ID,
            methods = HttpMethod.PATCH,
            operationId = "updateProduct",
            tags = Constants.Swagger.TAG_PRODUCTS,
            summary = "Изменить товар",
            security = @OpenApiSecurity(name = Constants.Swagger.BEARER_AUTH),
            pathParams = @OpenApiParam(
                    name = Constants.Params.ID,
                    type = UUID.class,
                    required = true,
                    description = "Идентификатор товара (UUID)"
            ),
            requestBody = @OpenApiRequestBody(
                    required = true,
                    content = @OpenApiContent(from = UpdateProductRequest.class)
            ),
            responses = {
                    @OpenApiResponse(
                            status = "200",
                            description = "Обновлённый товар",
                            content = @OpenApiContent(from = ProductView.class)
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
                            status = "403",
                            description = "Недостаточно прав",
                            content = @OpenApiContent(from = ErrorResponse.class)
                    ),
                    @OpenApiResponse(
                            status = "404",
                            description = "Товар не найден",
                            content = @OpenApiContent(from = ErrorResponse.class)
                    )
            }
    )
    ProductView update(String rawId, UpdateProductRequest request);

    @OpenApi(
            path = Constants.Routes.API_PRODUCT_BY_ID,
            methods = HttpMethod.DELETE,
            operationId = "deleteProduct",
            tags = Constants.Swagger.TAG_PRODUCTS,
            summary = "Мягкое удаление товара",
            security = @OpenApiSecurity(name = Constants.Swagger.BEARER_AUTH),
            pathParams = @OpenApiParam(
                    name = Constants.Params.ID,
                    type = UUID.class,
                    required = true,
                    description = "Идентификатор товара (UUID)"
            ),
            responses = {
                    @OpenApiResponse(
                            status = "204",
                            description = "Товар удалён, кэш каталога сброшен"
                    ),
                    @OpenApiResponse(
                            status = "401",
                            description = "Требуется аутентификация",
                            content = @OpenApiContent(from = ErrorResponse.class)
                    ),
                    @OpenApiResponse(
                            status = "403",
                            description = "Недостаточно прав",
                            content = @OpenApiContent(from = ErrorResponse.class)
                    ),
                    @OpenApiResponse(
                            status = "404",
                            description = "Товар не найден",
                            content = @OpenApiContent(from = ErrorResponse.class)
                    )
            }
    )
    void delete(String rawId);

    void setFilePublicId(UUID productId, String filePublicId);

    Product requireEntity(UUID id);

    record CatalogResult(ProductsResponse response, boolean cacheHit) {
    }
}
