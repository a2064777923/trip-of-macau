package com.aoxiaoyou.admin.dto.request;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class AdminIndoorNodeUpsertRequest {
    private String markerCode;
    private String nodeType;
    private String presentationMode;
    private String overlayType;
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
    private List<String> tags = new ArrayList<>();
    private String tagsJson;
    private String popupConfigJson;
    private String displayConfigJson;
    private AdminIndoorNodeBehaviorPayload.OverlayGeometry overlayGeometry;
    private Boolean inheritLinkedEntityRules;
    private String runtimeSupportLevel;
    private String metadataJson;
    private List<AdminIndoorNodeBehaviorPayload> behaviors = new ArrayList<>();
    private Integer sortOrder;
    private String status;
}
