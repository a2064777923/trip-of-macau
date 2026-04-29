package com.aoxiaoyou.admin.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class AdminStoryContentBlockResponse {
    private Long id;
    private String code;
    private String blockType;
    private String titleZh;
    private String titleEn;
    private String titleZht;
    private String titlePt;
    private String summaryZh;
    private String summaryEn;
    private String summaryZht;
    private String summaryPt;
    private String bodyZh;
    private String bodyEn;
    private String bodyZht;
    private String bodyPt;
    private Long primaryAssetId;
    private List<Long> attachmentAssetIds;
    private String stylePreset;
    private String displayMode;
    private String visibilityJson;
    private String configJson;
    private String status;
    private Integer sortOrder;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
