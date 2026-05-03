package com.aoxiaoyou.admin.dto.request;

import lombok.Data;

public class AdminStoryMaterialQaRequest {

    @Data
    public static class QaItemQuery {
        private String keyword;
        private String assetKind;
        private String chapterCode;
        private String usageTarget;
        private String itemStatus;
        private String healthState;
        private String runtimeExposure;
        private String providerName;
        private String modelCode;
        private Long pageNum;
        private Long pageSize;
    }

    @Data
    public static class QaActionRequest {
        private Long versionId;
        private String targetStatus;
        private String note;
        private Boolean confirmedImpact;
    }

    @Data
    public static class ReplaceRequest {
        private Long replacementAssetId;
        private Long versionId;
        private String note;
        private String targetStatus;
        private Boolean confirmedImpact;
    }

    @Data
    public static class ConsistencyCheckRequest {
        private Boolean includeCosHead;
        private Boolean includeLocalFileCheck;
        private Integer maxCosChecks;
        private Boolean runtimeOnly;
    }
}
