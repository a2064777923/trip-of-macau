package com.aoxiaoyou.admin.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminAiProviderTestResponse {

    private Long providerId;
    private String providerName;
    private String endpointStyle;
    private Integer success;
    private Long latencyMs;
    private String resolvedModel;
    private String message;
    private String preview;
    private String taskId;
}
