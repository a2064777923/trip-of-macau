package com.aoxiaoyou.admin.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AdminAiProviderSyncJobResponse {

    private Long id;
    private Long providerId;
    private String providerName;
    private String platformCode;
    private String syncStrategy;
    private String jobStatus;
    private String message;
    private String errorDetail;
    private Integer discoveredCount;
    private Integer createdCount;
    private Integer updatedCount;
    private Integer staleCount;
    private String rawPayloadJson;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
