package com.aoxiaoyou.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminStampUpsertRequest {

    @NotBlank(message = "code is required")
    private String code;

    private String nameZh;
    private String nameEn;
    private String nameZht;
    private String namePt;
    private String descriptionZh;
    private String descriptionEn;
    private String descriptionZht;
    private String descriptionPt;
    private String stampType;
    private String rarity;
    private Long iconAssetId;
    private Long relatedPoiId;
    private Long relatedStorylineId;
    private String status;
    private Integer sortOrder;
    private String publishedAt;
}
