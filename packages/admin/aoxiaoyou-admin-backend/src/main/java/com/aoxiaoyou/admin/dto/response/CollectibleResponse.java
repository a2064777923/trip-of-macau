package com.aoxiaoyou.admin.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CollectibleResponse {
    private Long id;
    private String collectibleCode;
    private String nameZh;
    private String collectibleType;
    private String rarity;
    private String imageUrl;
    private Long seriesId;
    private String acquisitionSource;
    private Integer isRepeatable;
    private Integer isLimited;
    private Integer maxOwnership;
    private String status;
}
