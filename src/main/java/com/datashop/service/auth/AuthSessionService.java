package com.datashop.service.auth;

import com.datashop.util.Constants;
import com.datashop.web.dto.common.ErrorResponse;
import io.javalin.http.Context;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiResponse;
import io.javalin.openapi.OpenApiSecurity;

public interface AuthSessionService {

    void start(Context context, String token);

    @OpenApi(
            path = Constants.Routes.API_AUTH_LOGOUT,
            methods = HttpMethod.POST,
            operationId = "logout",
            tags = Constants.Swagger.TAG_AUTH,
            summary = "Выход из веб-сессии",
            security = @OpenApiSecurity(name = Constants.Swagger.BEARER_AUTH),
            responses = {
                    @OpenApiResponse(status = "204", description = "Cookie сессии очищена"),
                    @OpenApiResponse(
                            status = "401",
                            description = "Требуется аутентификация",
                            content = @OpenApiContent(from = ErrorResponse.class)
                    )
            }
    )
    void logout(Context context);
}
