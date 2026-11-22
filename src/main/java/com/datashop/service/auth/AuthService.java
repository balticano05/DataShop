package com.datashop.service.auth;

import com.datashop.domain.user.User;
import com.datashop.util.Constants;
import com.datashop.web.dto.auth.AuthResponse;
import com.datashop.web.dto.auth.LoginRequest;
import com.datashop.web.dto.auth.RegisterRequest;
import com.datashop.web.dto.auth.UserView;
import com.datashop.web.dto.common.ErrorResponse;
import io.javalin.openapi.ContentType;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiContentProperty;
import io.javalin.openapi.OpenApiRequestBody;
import io.javalin.openapi.OpenApiResponse;
import io.javalin.openapi.OpenApiSecurity;

import java.util.UUID;

public interface AuthService {

    @OpenApi(
            path = Constants.Routes.API_AUTH_REGISTER,
            methods = HttpMethod.POST,
            operationId = "register",
            tags = Constants.Swagger.TAG_AUTH,
            summary = "Регистрация покупателя",
            requestBody = @OpenApiRequestBody(
                    required = true,
                    content = @OpenApiContent(from = RegisterRequest.class)
            ),
            responses = {
                    @OpenApiResponse(
                            status = "201",
                            description = "Пользователь создан",
                            content = @OpenApiContent(from = AuthResponse.class)
                    ),
                    @OpenApiResponse(
                            status = "400",
                            description = "Некорректный запрос",
                            content = @OpenApiContent(from = ErrorResponse.class)
                    ),
                    @OpenApiResponse(
                            status = "409",
                            description = "Email уже зарегистрирован",
                            content = @OpenApiContent(from = ErrorResponse.class)
                    ),
                    @OpenApiResponse(
                            status = "429",
                            description = "Слишком много запросов",
                            content = @OpenApiContent(from = ErrorResponse.class)
                    )
            }
    )
    User register(String rawEmail, String rawPassword);

    @OpenApi(
            path = Constants.Routes.API_AUTH_LOGIN,
            methods = HttpMethod.POST,
            operationId = "login",
            tags = Constants.Swagger.TAG_AUTH,
            summary = "Вход по email и паролю",
            requestBody = @OpenApiRequestBody(
                    required = true,
                    content = @OpenApiContent(from = LoginRequest.class)
            ),
            responses = {
                    @OpenApiResponse(
                            status = "200",
                            description = "JWT выдан",
                            content = @OpenApiContent(from = AuthResponse.class)
                    ),
                    @OpenApiResponse(
                            status = "400",
                            description = "Некорректный запрос",
                            content = @OpenApiContent(from = ErrorResponse.class)
                    ),
                    @OpenApiResponse(
                            status = "401",
                            description = "Неверный email или пароль",
                            content = @OpenApiContent(from = ErrorResponse.class)
                    ),
                    @OpenApiResponse(
                            status = "429",
                            description = "Слишком много запросов",
                            content = @OpenApiContent(from = ErrorResponse.class)
                    )
            }
    )
    User login(String rawEmail, String rawPassword);

    @OpenApi(
            path = Constants.Routes.API_AUTH_ME,
            methods = HttpMethod.GET,
            operationId = "currentUser",
            tags = Constants.Swagger.TAG_AUTH,
            summary = "Текущий пользователь",
            security = @OpenApiSecurity(name = Constants.Swagger.BEARER_AUTH),
            responses = {
                    @OpenApiResponse(
                            status = "200",
                            description = "Профиль",
                            content = @OpenApiContent(from = UserView.class)
                    ),
                    @OpenApiResponse(
                            status = "401",
                            description = "Требуется аутентификация",
                            content = @OpenApiContent(from = ErrorResponse.class)
                    )
            }
    )
    User require(UUID userId);

    @OpenApi(
            path = Constants.Routes.API_AUTH_ADMIN_CHECK,
            methods = HttpMethod.GET,
            operationId = "adminCheck",
            tags = Constants.Swagger.TAG_AUTH,
            summary = "Проверка роли администратора",
            security = @OpenApiSecurity(name = Constants.Swagger.BEARER_AUTH),
            responses = {
                    @OpenApiResponse(
                            status = "200",
                            description = "Доступ разрешён",
                            content = @OpenApiContent(
                                    mimeType = ContentType.JSON,
                                    type = "object",
                                    properties = {
                                            @OpenApiContentProperty(name = "message", type = "string"),
                                            @OpenApiContentProperty(name = "user", from = UserView.class)
                                    }
                            )
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
    User requireAdmin(UUID userId);
}
