package com.aoxiaoyou.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class AdminLifecycleOperationResponse {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Preview {
        private String targetType;
        private String targetTypeLabel;
        private Long targetId;
        private String targetCode;
        private String targetName;
        private String action;
        private String actionLabel;
        private String currentStatus;
        private String currentStatusLabel;
        private String targetStatus;
        private String targetStatusLabel;
        private Boolean allowedTransition;
        private Boolean hasBlockingImpacts;
        private String previewHash;
        private Map<String, Integer> impactCounters;
        private List<AdminLifecycleImpactResponse> impacts;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Summary {
        private Long id;
        private String operationCode;
        private String targetType;
        private String targetTypeLabel;
        private Long targetId;
        private String targetCode;
        private String targetName;
        private String action;
        private String actionLabel;
        private String fromStatus;
        private String fromStatusLabel;
        private String toStatus;
        private String toStatusLabel;
        private String operationStatus;
        private String operationStatusLabel;
        private String previewHash;
        private Integer impactCount;
        private Integer blockingImpactCount;
        private Long requestedBy;
        private String requestedByName;
        private String reason;
        private LocalDateTime scheduledAt;
        private LocalDateTime appliedAt;
        private LocalDateTime cancelledAt;
        private LocalDateTime failedAt;
        private String errorMessage;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Detail {
        private Summary summary;
        private List<AdminLifecycleImpactResponse> impacts;
        private Map<String, Object> preview;
        private Map<String, Object> request;
        private Map<String, Object> result;
    }
}
