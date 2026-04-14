package com.aoxiaoyou.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminTipArticleUpsertRequest {

    private Long cityId;

    @NotBlank(message = "code is required")
    private String code;

    private String categoryCode;
    private String titleZh;
    private String titleEn;
    private String titleZht;
    private String titlePt;
    private String summaryZh;
    private String summaryEn;
    private String summaryZht;
    private String summaryPt;
    private String contentZh;
    private String contentEn;
    private String contentZht;
    private String contentPt;
    private String authorDisplayName;
    private String locationNameZh;
    private String locationNameEn;
    private String locationNameZht;
    private String locationNamePt;
    private String tagsJson;
    private Long coverAssetId;
    private String sourceType;
    private String status;
    private Integer sortOrder;
    private String publishedAt;
}
