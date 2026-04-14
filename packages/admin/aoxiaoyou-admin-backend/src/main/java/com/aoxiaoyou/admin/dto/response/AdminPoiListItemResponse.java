package com.aoxiaoyou.admin.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class AdminPoiListItemResponse {
    private Long poiId;
    private Long cityId;
    private String cityName;
    private Long subMapId;
    private String subMapCode;
    private String subMapName;
    private Long storylineId;
    private String storylineName;
    private String code;
    private String nameZh;
    private String subtitleZh;
    private String categoryCode;
    private String difficulty;
    private String sourceCoordinateSystem;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String status;
    private Integer sortOrder;
    private Long coverAssetId;
    private Long mapIconAssetId;
    private LocalDateTime createdAt;
}
