package com.aoxiaoyou.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class AdminStoryContentBlockUpsertRequest {

    private String code;

    @NotBlank(message = "blockType is required")
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
    private String publishedAt;
}
