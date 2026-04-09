package com.aoxiaoyou.admin.common.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.List;

public class RuntimeProfileEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    private static final List<String> CLOUDBASE_MARKERS = List.of(
            "CLOUDBASE_ENV_ID",
            "TCB_ENV_ID",
            "TENCENTCLOUD_RUNENV",
            "K_SERVICE"
    );

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (environment.getActiveProfiles().length > 0 || environment.containsProperty("spring.profiles.active")) {
            return;
        }

        if (isCloudBaseRuntime(environment)) {
            environment.addActiveProfile("cloudbase");
            return;
        }

        environment.addActiveProfile("local");
    }

    private boolean isCloudBaseRuntime(ConfigurableEnvironment environment) {
        return CLOUDBASE_MARKERS.stream()
                .map(environment::getProperty)
                .anyMatch(this::hasText);
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
