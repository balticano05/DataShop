package com.datashop.web.api.health;

import com.datashop.service.health.HealthService;
import com.datashop.util.Constants;
import com.datashop.web.dto.health.ReadyView;
import io.javalin.Javalin;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class HealthApiController {

    private final HealthService healthService;

    public void register(Javalin app) {
        app.get(Constants.Routes.HEALTH, context -> context.json(healthService.health()));

        app.get(Constants.Routes.HEALTH_READY, context -> {
            ReadyView ready = healthService.ready();
            context.status(Constants.Health.READY.equals(ready.status())
                            ? Constants.Http.Status.OK
                            : Constants.Http.Status.SERVICE_UNAVAILABLE)
                    .json(ready);
        });
    }
}
