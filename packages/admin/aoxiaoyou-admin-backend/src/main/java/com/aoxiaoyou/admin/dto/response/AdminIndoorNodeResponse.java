package com.aoxiaoyou.admin.dto.response;

import com.aoxiaoyou.admin.dto.request.AdminIndoorNodeBehaviorPayload;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class AdminIndoorNodeResponse {
    private Long id;
    private Long buildingId;
    private Long floorId;
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
    private String iconUrl;
    private Long animationAssetId;
    private String animationUrl;
    private String linkedEntityType;
    private Long linkedEntityId;
    private List<String> tags;
    private String tagsJson;
    private String popupConfigJson;
    private String displayConfigJson;
    private AdminIndoorNodeBehaviorPayload.OverlayGeometry overlayGeometry;
    private String overlayGeometryJson;
    private Boolean inheritLinkedEntityRules;
    private String runtimeSupportLevel;
    private String metadataJson;
    private Long importBatchId;
    private Integer sortOrder;
    private String status;
    private List<AdminIndoorNodeBehaviorPayload> behaviors;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
