package com.aoxiaoyou.admin.dto.request;

import lombok.Data;

@Data
public class AdminMediaPolicySettingsUpsertRequest {

    private Integer maxBatchCount;
    private Long maxBatchTotalBytes;
    private MediaKindPolicy image;
    private MediaKindPolicy video;
    private MediaKindPolicy audio;
    private MediaKindPolicy file;

    @Data
    public static class MediaKindPolicy {
        private Long maxFileSizeBytes;
        private String preferredPolicyCode;
        private Integer qualityPercent;
        private Integer maxWidthPx;
        private Integer maxHeightPx;
        private Boolean preserveMetadata;
        private String note;
    }
}
