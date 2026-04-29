package com.aoxiaoyou.admin.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class AdminIndoorBuildingDetailResponse {
    private Long id;
    private String buildingCode;
    private String bindingMode;
    private Long cityId;
    private String cityCode;
    private String cityName;
    private Long subMapId;
    private String subMapCode;
    private String subMapName;
    private Long poiId;
    private String poiName;
    private String nameZh;
    private String nameEn;
    private String nameZht;
    private String namePt;
    private String addressZh;
    private String addressEn;
    private String addressZht;
    private String addressPt;
    private String sourceCoordinateSystem;
    private BigDecimal sourceLatitude;
    private BigDecimal sourceLongitude;
    private BigDecimal lat;
    private BigDecimal lng;
    private Integer totalFloors;
    private Integer basementFloors;
    private Long coverAssetId;
    private String coverImageUrl;
    private String descriptionZh;
    private String descriptionEn;
    private String descriptionZht;
    private String descriptionPt;
    private String popupConfigJson;
    private String displayConfigJson;
    private List<AdminSpatialAssetLinkResponse> attachments;
    private List<Long> attachmentAssetIds;
    private List<AdminIndoorFloorResponse> floors;
    private Integer sortOrder;
    private String status;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
