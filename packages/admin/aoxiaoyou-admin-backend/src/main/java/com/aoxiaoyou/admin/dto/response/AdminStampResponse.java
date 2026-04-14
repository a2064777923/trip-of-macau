package com.aoxiaoyou.admin.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AdminStampResponse {
    private Long id;
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
    private String relatedPoiName;
    private Long relatedStorylineId;
    private String relatedStorylineName;
    private String status;
    private Integer sortOrder;
    private LocalDateTime publishedAt;
    private LocalDateTime updatedAt;
}
