package com.aoxiaoyou.admin.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminSpatialAssetLinkResponse {
    private Long id;
    private String entityType;
    private Long entityId;
    private String usageType;
    private Long assetId;
    private String titleZh;
    private String titleEn;
    private String titleZht;
    private String titlePt;
    private String descriptionZh;
    private String descriptionEn;
    private String descriptionZht;
    private String descriptionPt;
    private String displayConfigJson;
    private Integer sortOrder;
    private String status;
}
