package com.aoxiaoyou.tripofmacau.dto.response;

import lombok.Builder;
import lombok.Data;

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
    private Integer sortOrder;
}
