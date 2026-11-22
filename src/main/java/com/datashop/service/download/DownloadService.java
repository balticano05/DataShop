package com.datashop.service.download;

import com.datashop.util.Constants;
import com.datashop.web.dto.common.ErrorResponse;
import com.datashop.web.dto.download.DownloadUrlView;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiParam;
import io.javalin.openapi.OpenApiResponse;
import io.javalin.openapi.OpenApiSecurity;

import java.util.UUID;

public interface DownloadService {

    @OpenApi(
            path = Constants.Routes.API_DOWNLOAD_BY_PRODUCT_ID,
            methods = HttpMethod.GET,
            operationId = "downloadDataset",
            tags = Constants.Swagger.TAG_DOWNLOADS,
            summary = "Получить приватную ссылку Supabase Storage на купленный датасет",
            security = @OpenApiSecurity(name = Constants.Swagger.BEARER_AUTH),
            pathParams = @OpenApiParam(
                    name = Constants.Params.PRODUCT_ID,
                    type = UUID.class,
                    required = true,
                    description = "Идентификатор товара (UUID)"
            ),
            responses = {
                    @OpenApiResponse(
                            status = "200",
                            description = "Приватная ссылка Supabase Storage на 60 секунд",
                            content = @OpenApiContent(from = DownloadUrlView.class)
                    ),
                    @OpenApiResponse(
                            status = "401",
                            description = "Требуется аутентификация",
                            content = @OpenApiContent(from = ErrorResponse.class)
                    ),
                    @OpenApiResponse(
                            status = "403",
                            description = "Датасет не куплен",
                            content = @OpenApiContent(from = ErrorResponse.class)
                    ),
                    @OpenApiResponse(
                            status = "404",
                            description = "Датасет недоступен",
                            content = @OpenApiContent(from = ErrorResponse.class)
                    )
            }
    )
    DownloadUrlView issue(UUID userId, String rawProductId);
}
