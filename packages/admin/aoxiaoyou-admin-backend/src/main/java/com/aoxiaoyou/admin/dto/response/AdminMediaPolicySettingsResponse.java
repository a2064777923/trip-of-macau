package com.aoxiaoyou.admin.dto.response;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminMediaPolicySettingsResponse {

    private Integer maxBatchCount;
    private Long maxBatchTotalBytes;
    private MediaKindPolicy image;
    private MediaKindPolicy video;
    private MediaKindPolicy audio;
    private MediaKindPolicy file;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
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
