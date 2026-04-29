package com.aoxiaoyou.tripofmacau.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RedeemablePrizeResponse {
    private Long id;
    private String code;
    private String prizeType;
    private String fulfillmentMode;
    private String name;
    private String subtitle;
    private String description;
    private String highlight;
    private String coverImageUrl;
    private Integer stampCost;
    private Integer inventoryTotal;
    private Integer inventoryRedeemed;
    private Integer availableInventory;
    private String stockPolicyJson;
    private String fulfillmentConfigJson;
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
