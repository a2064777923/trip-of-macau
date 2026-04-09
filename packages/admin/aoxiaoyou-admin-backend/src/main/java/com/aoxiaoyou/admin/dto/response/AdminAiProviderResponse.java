package com.aoxiaoyou.admin.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class AdminAiProviderResponse {
    private Long id;
    private String providerName;
    private String displayName;
    private String apiBaseUrl;
    private String modelName;
    private String capabilities;
    private Integer requestTimeoutMs;
    private Integer maxRetries;
    private Integer quotaDaily;
    private BigDecimal costPer1kTokens;
    private Integer status;
}
