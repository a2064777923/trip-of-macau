package com.aoxiaoyou.admin.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class AdminPoiDetailResponse {
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
    private String nameEn;
    private String nameZht;
    private String namePt;
    private String subtitleZh;
    private String subtitleEn;
    private String subtitleZht;
    private String subtitlePt;
    private String sourceCoordinateSystem;
    private BigDecimal sourceLatitude;
    private BigDecimal sourceLongitude;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String addressZh;
    private String addressEn;
    private String addressZht;
    private String addressPt;
    private Integer triggerRadius;
    private Integer manualCheckinRadius;
    private Integer staySeconds;
    private String categoryCode;
    private String difficulty;
    private String districtZh;
    private String districtEn;
    private String districtZht;
    private String districtPt;
    private Long coverAssetId;
    private Long mapIconAssetId;
    private Long audioAssetId;
    private String descriptionZh;
    private String descriptionEn;
    private String descriptionZht;
    private String descriptionPt;
    private String introTitleZh;
    private String introTitleEn;
    private String introTitleZht;
    private String introTitlePt;
    private String introSummaryZh;
    private String introSummaryEn;
    private String introSummaryZht;
    private String introSummaryPt;
    private String popupConfigJson;
    private String displayConfigJson;
    private List<AdminSpatialAssetLinkResponse> attachments;
    private String status;
    private Integer sortOrder;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
