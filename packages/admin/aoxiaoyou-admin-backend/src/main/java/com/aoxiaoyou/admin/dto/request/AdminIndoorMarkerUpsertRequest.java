package com.aoxiaoyou.admin.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AdminIndoorMarkerUpsertRequest {
    private String markerCode;
    private String nodeType;
    private String nodeNameZh;
    private String nodeNameEn;
    private String nodeNameZht;
    private String nodeNamePt;
    private String descriptionZh;
    private String descriptionEn;
    private String descriptionZht;
    private String descriptionPt;
    private BigDecimal relativeX;
    private BigDecimal relativeY;
    private Long relatedPoiId;
    private Long iconAssetId;
    private Long animationAssetId;
    private String linkedEntityType;
    private Long linkedEntityId;
    private String tagsJson;
    private String popupConfigJson;
    private String displayConfigJson;
    private String metadataJson;
    private Integer sortOrder;
    private String status;
}
