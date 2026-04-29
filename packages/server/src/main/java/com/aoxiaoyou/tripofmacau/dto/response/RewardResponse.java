package com.aoxiaoyou.tripofmacau.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RewardResponse {

    private Long id;
    private String code;
    private String name;
    private String subtitle;
    private String description;
    private String highlight;
    private Integer stampCost;
    private Integer inventoryTotal;
    private Integer inventoryRedeemed;
    private Integer availableInventory;
    private String coverImageUrl;
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
    private Integer sortOrder;
}
