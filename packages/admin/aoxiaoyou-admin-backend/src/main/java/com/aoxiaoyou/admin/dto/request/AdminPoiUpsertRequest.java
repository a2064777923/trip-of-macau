package com.aoxiaoyou.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AdminPoiUpsertRequest {

    @NotNull(message = "cityId is required")
    private Long cityId;

    private Long subMapId;

    private Long storylineId;

    @NotBlank(message = "code is required")
    private String code;

    @NotBlank(message = "nameZh is required")
    private String nameZh;

    private String nameEn;
    private String nameZht;
    private String namePt;
    private String subtitleZh;
    private String subtitleEn;
    private String subtitleZht;
    private String subtitlePt;

    private String sourceCoordinateSystem = "GCJ02";

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
    private java.util.List<AdminSpatialAssetLinkUpsertRequest> attachments = new java.util.ArrayList<>();
    private String status;
    private Integer sortOrder;
    private String publishedAt;
}
