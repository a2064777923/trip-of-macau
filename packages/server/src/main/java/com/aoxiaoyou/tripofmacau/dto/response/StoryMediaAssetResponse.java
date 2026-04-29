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
}
