package com.aoxiaoyou.tripofmacau.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class BadgeResponse {

    private Long id;
    private String code;
    private String name;
    private String description;
    private String badgeType;
    private String rarity;
    private Boolean hidden;
    private String coverImageUrl;
    private String iconImageUrl;
    private String animationUrl;
    private String popupPresetCode;
    private String popupConfigJson;
    private String displayPresetCode;
    private String displayConfigJson;
    private String triggerPresetCode;
    private String triggerConfigJson;
    private String exampleContent;
    private List<CatalogRelationBindingResponse> relatedStorylines;
    private List<CatalogRelationBindingResponse> relatedCities;
    private List<CatalogRelationBindingResponse> relatedSubMaps;
    private List<CatalogRelationBindingResponse> relatedIndoorBuildings;
    private List<CatalogRelationBindingResponse> relatedIndoorFloors;
    private List<String> attachmentAssetUrls;
}
