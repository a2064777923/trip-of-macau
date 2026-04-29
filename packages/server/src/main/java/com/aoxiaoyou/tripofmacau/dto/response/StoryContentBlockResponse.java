package com.aoxiaoyou.tripofmacau.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class StoryContentBlockResponse {
    private Long id;
    private String code;
    private String blockType;
    private String title;
    private String summary;
    private String body;
    private String stylePreset;
    private String displayMode;
    private String visibilityJson;
    private String displayConditionJson;
    private String configJson;
    private Integer sortOrder;
    private StoryMediaAssetResponse primaryAsset;
    private List<StoryMediaAssetResponse> attachmentAssets;
}
