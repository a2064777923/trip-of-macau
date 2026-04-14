package com.aoxiaoyou.admin.media;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Data
@Component
@ConfigurationProperties(prefix = "app.cos")
public class CosProperties {

    private boolean enabled = false;
    private String secretId;
    private String secretKey;
    private String bucketName;
    private String region;
    private String publicBaseUrl;
    private String basePath = "miniapp/assets";
    private int connectionTimeoutMs = 10_000;
    private int socketTimeoutMs = 30_000;

    public String normalizedBasePath() {
        if (!StringUtils.hasText(basePath)) {
            return "";
        }
        return trimSlashes(basePath);
    }

    public String resolvePublicBaseUrl() {
        if (StringUtils.hasText(publicBaseUrl)) {
            return trimTrailingSlash(publicBaseUrl.trim());
        }
        return "https://" + bucketName + ".cos." + region + ".myqcloud.com";
    }

    private String trimSlashes(String value) {
        String normalized = value.trim();
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    private String trimTrailingSlash(String value) {
        String normalized = value;
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }
}
