package com.aoxiaoyou.admin.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Data
@Component
@ConfigurationProperties(prefix = "app.integration")
public class IntegrationProperties {

    private String publicBaseUrl = "http://127.0.0.1:8080";
    private String publicHealthPath = "/api/v1/health";
    private int timeoutMs = 5000;
    private String phase6SeedKey = "phase6-mock-dataset-migration";

    public String resolvePublicHealthUrl() {
        String baseUrl = trimTrailingSlash(publicBaseUrl);
        String path = StringUtils.hasText(publicHealthPath) ? publicHealthPath.trim() : "/api/v1/health";
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        return baseUrl + path;
    }

    private String trimTrailingSlash(String value) {
        String normalized = StringUtils.hasText(value) ? value.trim() : "http://127.0.0.1:8080";
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }
}
