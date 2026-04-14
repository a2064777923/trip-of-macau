package com.aoxiaoyou.admin.dto.request;

import lombok.Data;

@Data
public class AdminSpatialAssetLinkUpsertRequest {
    private Long id;
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
    private Integer sortOrder = 0;
    private String status = "draft";
}
