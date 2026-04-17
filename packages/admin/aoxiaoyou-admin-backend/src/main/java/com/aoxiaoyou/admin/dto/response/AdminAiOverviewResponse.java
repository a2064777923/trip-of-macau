package com.aoxiaoyou.admin.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AdminAiOverviewResponse {

    private Summary summary;
    private List<AdminAiCapabilityResponse> capabilities;
    private List<ProviderHealth> providers;
    private List<Alert> alerts;
    private List<AdminAiGenerationJobResponse> recentJobs;
    private List<AdminAiLogResponse> recentLogs;

    @Data
    @Builder
    public static class Summary {
        private Integer totalCapabilities;
        private Integer enabledCapabilities;
        private Integer enabledProviders;
        private Integer healthyProviders;
        private Integer inventoryRecords;
        private Integer staleProviders;
        private Long requests24h;
        private Long failures24h;
        private Long fallbacks24h;
        private java.math.BigDecimal estimatedCost24h;
        private Long activeJobs;
    }

    @Data
    @Builder
    public static class ProviderHealth {
        private Long providerId;
        private String providerName;
        private String displayName;
        private String healthStatus;
        private String healthMessage;
        private String endpointStyle;
        private String providerType;
        private Integer status;
        private String lastInventorySyncStatus;
        private java.time.LocalDateTime lastInventorySyncedAt;
        private Integer inventoryRecordCount;
        private Long requestCount24h;
        private Long failureCount24h;
        private Long averageLatencyMs;
    }

    @Data
    @Builder
    public static class Alert {
        private String level;
        private String title;
        private String message;
    }
}
