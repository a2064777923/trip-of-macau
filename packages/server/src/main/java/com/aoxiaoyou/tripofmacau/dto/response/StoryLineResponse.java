package com.aoxiaoyou.tripofmacau.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class StoryLineResponse {

    private Long id;
    private Long cityId;
    private String cityCode;
    private List<CatalogRelationBindingResponse> cityBindings;
    private List<CatalogRelationBindingResponse> subMapBindings;
    private String code;
    private String status;
    private String name;
    private String nameEn;
    private String description;
    private Integer estimatedMinutes;
    private String difficulty;
    private String rewardBadge;
    private String coverImageUrl;
    private String bannerImageUrl;
    private List<StoryMediaAssetResponse> attachmentAssets;
    private Integer totalChapters;
    private Integer sortOrder;
    private List<StoryChapterResponse> chapters;
}
