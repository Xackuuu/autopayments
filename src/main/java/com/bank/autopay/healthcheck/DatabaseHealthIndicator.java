package com.bank.autopay.healthcheck;

import com.bank.autopay.repository.AutoPayRuleRepository;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
public class DatabaseHealthIndicator implements HealthIndicator {

    @Autowired
    private AutoPayRuleRepository repository;

    @Override
    public @Nullable Health health() {
        try {
            repository.count();
            return Health.up()
                    .withDetail("database", "PostgreSQL")
                    .withDetail("status", "available")
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withDetails(new HashMap<>() {{
                        put("error", e.getMessage());
                    }})
                    .build();
        }
    }
}
