package com.aoxiaoyou.tripofmacau.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class GameRewardResponse {
    private Long id;
    private String code;
    private String rewardType;
    private String rarity;
    private Integer stackable;
    private Integer maxOwned;
    private Integer canEquip;
    private Integer canConsume;
    private String name;
    private String subtitle;
    private String description;
    private String highlight;
    private String coverImageUrl;
    private String iconUrl;
    private String animationUrl;
    private String rewardConfigJson;
    private Long presentationId;
    private RewardPresentationResponse presentation;
    private List<RewardRuleSummaryResponse> ruleSummaries;
    private List<CatalogRelationBindingResponse> relatedStorylines;
    private List<CatalogRelationBindingResponse> relatedCities;
    private List<CatalogRelationBindingResponse> relatedSubMaps;
    private List<CatalogRelationBindingResponse> relatedIndoorBuildings;
    private List<CatalogRelationBindingResponse> relatedIndoorFloors;
    private List<String> attachmentAssetUrls;
    private Integer sortOrder;
}
