package com.aoxiaoyou.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

public class AdminLifecycleTargetResponse {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TargetType {
        private String targetType;
        private String label;
        private String tableName;
        private Boolean statusMutable;
        private Boolean publicRuntimeRelevant;
        private List<String> supportedActions;
        private List<String> childTargetTypes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatusOption {
        private String status;
        private String canonicalStatus;
        private String label;
        private Boolean travelerVisible;
        private Boolean terminalDeleted;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TargetSummary {
        private String targetType;
        private String targetTypeLabel;
        private Long targetId;
        private String targetCode;
        private String targetName;
        private String status;
        private String canonicalStatus;
        private String statusLabel;
        private Boolean travelerVisible;
        private Boolean statusMutable;
        private Boolean publicRuntimeRelevant;
        private Integer dependencyCount;
        private List<String> supportedActions;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private LocalDateTime publishedAt;
    }
}
