package com.aoxiaoyou.tripofmacau.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StoryMediaAssetResponse {
    private Long id;
    private String assetKind;
    private String url;
    private String mimeType;
    private String originalFilename;
    private Integer widthPx;
    private Integer heightPx;
    private String animationSubtype;
    private Boolean defaultLoop;
    private Boolean defaultAutoplay;
    private Long posterAssetId;
    private String posterUrl;
    private Long fallbackAssetId;
    private String fallbackUrl;
    private String availability;
    private String unavailableReason;
    private boolean fallbackUsed;
    private String runtimeKind;
    private Long fileSizeBytes;
    private Integer durationMs;
    private UsageHint usageHint;

    @Data
    @Builder
    public static class UsageHint {
        private String materialItemKey;
        private String usageTarget;
        private String chapterCode;
        private String targetType;
        private String targetCode;
        private String displayRole;
        private String sourceScope;
    }
}
