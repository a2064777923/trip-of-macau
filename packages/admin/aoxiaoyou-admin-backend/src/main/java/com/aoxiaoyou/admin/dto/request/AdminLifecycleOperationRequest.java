package com.aoxiaoyou.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

public class AdminLifecycleOperationRequest {

    @Data
    public static class TargetQuery {
        private long pageNum = 1;
        private long pageSize = 20;
        private String keyword;
        private String targetType;
        private String status;
        private String operationStatus;
        private Long cityId;
        private Long subMapId;
        private Long storylineId;
        private Boolean publishedOnly;
        private Boolean withDependenciesOnly;
    }

    @Data
    public static class Preview {
        @NotBlank(message = "targetType is required")
        private String targetType;

        private Long targetId;
        private String targetCode;

        @NotBlank(message = "action is required")
        private String action;

        private String executionMode;
        private Boolean cascade;
        private Map<String, Object> metadata;
    }

    @Data
    public static class CreateOperation {
        @NotBlank(message = "targetType is required")
        private String targetType;

        private Long targetId;
        private String targetCode;

        @NotBlank(message = "action is required")
        private String action;

        @NotBlank(message = "executionMode is required")
        private String executionMode;

        private String scheduledAt;
        private String reason;
        private String previewHash;

        @NotNull(message = "confirmedImpact is required")
        private Boolean confirmedImpact;

        private Boolean cascade;
        private Map<String, Object> metadata;
    }

    @Data
    public static class ApplyOperation {
        private String reason;
        private String previewHash;
        private Boolean confirmedImpact;
        private Map<String, Object> metadata;
    }

    @Data
    public static class CancelOperation {
        private String reason;
    }
}
