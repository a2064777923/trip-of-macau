package com.aoxiaoyou.admin.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class AdminPoiListItemResponse {

    private Long poiId;
    private String name;
    private String subtitle;
    private String regionCode;
    private String regionName;
    private String poiType;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Long categoryId;
    private String categoryName;
    private String importance;
    private Integer geofenceRadius;
    private String status;
    private Long storylineId;
    private String storylineName;
    private Long checkInCount;
    private LocalDateTime createdAt;
}
