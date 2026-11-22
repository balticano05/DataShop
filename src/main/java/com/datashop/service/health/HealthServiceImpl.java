package com.datashop.service.health;

import com.datashop.db.Jpa;
import com.datashop.util.Constants;
import com.datashop.web.dto.health.HealthView;
import com.datashop.web.dto.health.ReadyView;
import lombok.RequiredArgsConstructor;

import java.lang.management.ManagementFactory;
import java.time.Instant;

@RequiredArgsConstructor
public class HealthServiceImpl implements HealthService {

    private final Jpa jpa;

    @Override
    public HealthView health() {
        return new HealthView(
                Constants.Health.OK,
                Instant.now().toString(),
                ManagementFactory.getRuntimeMXBean().getUptime() / Constants.Health.MILLIS_PER_SECOND
        );
    }

    @Override
    public ReadyView ready() {
        boolean ready = jpa.healthCheck();
        return new ReadyView(
                ready ? Constants.Health.READY : Constants.Health.UNAVAILABLE,
                ready ? Constants.Health.UP : Constants.Health.DOWN,
                Instant.now().toString()
        );
    }
}
