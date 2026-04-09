package com.aoxiaoyou.admin.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AdminBuildingUpsertRequest {
    private String buildingCode;
    private String nameZh;
    private String addressZh;
    private String cityCode;
    private BigDecimal lat;
    private BigDecimal lng;
    private Integer totalFloors;
    private Integer basementFloors;
    private String coverImageUrl;
    private String descriptionZh;
    private Long poiId;
    private String status;
}
