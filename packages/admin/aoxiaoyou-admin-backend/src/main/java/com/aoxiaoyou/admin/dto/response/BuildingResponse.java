package com.aoxiaoyou.admin.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class BuildingResponse {
    private Long id;
    private String buildingCode;
    private String bindingMode;
    private Long cityId;
    private String nameZh;
    private String nameEn;
    private String nameZht;
    private String namePt;
    private String addressZh;
    private String addressEn;
    private String addressZht;
    private String addressPt;
    private String cityCode;
    private String cityName;
    private Long subMapId;
    private String subMapCode;
    private String subMapName;
    private Long poiId;
    private String poiName;
    private String sourceCoordinateSystem;
    private BigDecimal sourceLatitude;
    private BigDecimal sourceLongitude;
    private BigDecimal lat;
    private BigDecimal lng;
    private Integer totalFloors;
    private Integer basementFloors;
    private Integer floorCount;
    private Long coverAssetId;
    private String coverImageUrl;
    private String status;
    private Integer sortOrder;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
