package com.datashop.service.health;

import com.datashop.util.Constants;
import com.datashop.web.dto.health.HealthView;
import com.datashop.web.dto.health.ReadyView;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiResponse;

public interface HealthService {

    @OpenApi(
            path = Constants.Routes.HEALTH,
            methods = HttpMethod.GET,
            operationId = "health",
            tags = Constants.Swagger.TAG_HEALTH,
            summary = "Проверка живости",
            responses = @OpenApiResponse(
                    status = "200",
                    description = "Сервис запущен",
                    content = @OpenApiContent(from = HealthView.class)
            )
    )
    HealthView health();

    @OpenApi(
            path = Constants.Routes.HEALTH_READY,
            methods = HttpMethod.GET,
            operationId = "ready",
            tags = Constants.Swagger.TAG_HEALTH,
            summary = "Проверка готовности",
            responses = {
                    @OpenApiResponse(
                            status = "200",
                            description = "База данных доступна",
                            content = @OpenApiContent(from = ReadyView.class)
                    ),
                    @OpenApiResponse(
                            status = "503",
                            description = "База данных недоступна",
                            content = @OpenApiContent(from = ReadyView.class)
                    )
            }
    )
    ReadyView ready();
}
