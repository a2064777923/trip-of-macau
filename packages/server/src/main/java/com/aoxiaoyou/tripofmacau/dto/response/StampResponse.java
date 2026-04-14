package com.aoxiaoyou.tripofmacau.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StampResponse {

    private Long id;
    private String code;
    private String name;
    private String description;
    private String stampType;
    private String rarity;
    private String iconImageUrl;
    private Long relatedPoiId;
    private Long relatedStorylineId;
    private Integer sortOrder;
}
