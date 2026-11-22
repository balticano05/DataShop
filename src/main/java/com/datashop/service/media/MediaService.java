package com.datashop.service.media;

import com.datashop.util.Constants;
import com.datashop.web.dto.common.ErrorResponse;
import com.datashop.web.dto.media.UploadedAssetView;
import io.javalin.http.UploadedFile;
import io.javalin.openapi.ContentType;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiContentProperty;
import io.javalin.openapi.OpenApiParam;
import io.javalin.openapi.OpenApiRequestBody;
import io.javalin.openapi.OpenApiResponse;
import io.javalin.openapi.OpenApiSecurity;

import java.util.Optional;
import java.util.UUID;

public interface MediaService {

    @OpenApi(
            path = Constants.Routes.API_ADMIN_MEDIA_PREVIEW,
            methods = HttpMethod.POST,
            operationId = "uploadPreview",
            tags = Constants.Swagger.TAG_MEDIA,
            summary = "Загрузить превью изображения в Supabase PostgreSQL",
            security = @OpenApiSecurity(name = Constants.Swagger.BEARER_AUTH),
            requestBody = @OpenApiRequestBody(
                    required = true,
                    content = @OpenApiContent(
                            mimeType = ContentType.FORM_DATA_MULTIPART,
                            properties = @OpenApiContentProperty(
                                    name = Constants.Params.FILE,
                                    type = "string",
                                    format = "binary"
                            )
                    )
            ),
            responses = {
                    @OpenApiResponse(
                            status = "201",
                            description = "Файл сохранён в Supabase PostgreSQL",
                            content = @OpenApiContent(from = UploadedAssetView.class)
                    ),
                    @OpenApiResponse(
                            status = "400",
                            description = "Некорректный файл",
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
    UploadedAssetView uploadPreview(UploadedFile file);

    Optional<MediaImage> loadImage(UUID id);

    @OpenApi(
            path = Constants.Routes.API_ADMIN_MEDIA_DATASET,
            methods = HttpMethod.POST,
            operationId = "uploadDataset",
            tags = Constants.Swagger.TAG_MEDIA,
            summary = "Загрузить файл датасета",
            security = @OpenApiSecurity(name = Constants.Swagger.BEARER_AUTH),
            pathParams = @OpenApiParam(
                    name = Constants.Params.PRODUCT_ID,
                    type = UUID.class,
                    required = true,
                    description = "Идентификатор товара (UUID)"
            ),
            requestBody = @OpenApiRequestBody(
                    required = true,
                    content = @OpenApiContent(
                            mimeType = ContentType.FORM_DATA_MULTIPART,
                            properties = @OpenApiContentProperty(
                                    name = Constants.Params.FILE,
                                    type = "string",
                                    format = "binary"
                            )
                    )
            ),
            responses = {
                    @OpenApiResponse(
                            status = "201",
                            description = "Файл загружен в приватный Supabase Storage",
                            content = @OpenApiContent(from = UploadedAssetView.class)
                    ),
                    @OpenApiResponse(
                            status = "400",
                            description = "Некорректный файл",
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
    UploadedAssetView uploadDataset(String rawProductId, UploadedFile file);
}
