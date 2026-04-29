package com.aoxiaoyou.admin.dto.request;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class AdminBuildingUpsertRequest {
    private String buildingCode;
    private String nameZh;
    private String nameEn;
    private String nameZht;
    private String namePt;
    private String addressZh;
    private String addressEn;
    private String addressZht;
    private String addressPt;
    private Long cityId;
    private Long subMapId;
    private String bindingMode;
    private String sourceCoordinateSystem = "GCJ02";
    private BigDecimal sourceLatitude;
    private BigDecimal sourceLongitude;
    private BigDecimal lat;
    private BigDecimal lng;
    private Integer totalFloors;
    private Integer basementFloors;
    private String coverImageUrl;
    private Long coverAssetId;
    private String descriptionZh;
    private String descriptionEn;
    private String descriptionZht;
    private String descriptionPt;
    private String popupConfigJson;
    private String displayConfigJson;
    private Long poiId;
    private List<AdminSpatialAssetLinkUpsertRequest> attachments = new ArrayList<>();
    private List<Long> attachmentAssetIds = new ArrayList<>();
    private String status;
    private Integer sortOrder;
    private String publishedAt;
}
