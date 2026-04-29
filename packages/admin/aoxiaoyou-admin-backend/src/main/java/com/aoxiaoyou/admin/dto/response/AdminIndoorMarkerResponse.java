package com.aoxiaoyou.admin.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class AdminIndoorMarkerResponse {
    private Long id;
    private Long buildingId;
    private Long floorId;
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
    private String iconUrl;
    private Long animationAssetId;
    private String animationUrl;
    private String linkedEntityType;
    private Long linkedEntityId;
    private String tagsJson;
    private String popupConfigJson;
    private String displayConfigJson;
    private String metadataJson;
    private Long importBatchId;
    private Integer sortOrder;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
