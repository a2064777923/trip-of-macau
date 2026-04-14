package com.aoxiaoyou.admin.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AdminTipArticleResponse {
    private Long id;
    private Long cityId;
    private String cityName;
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
    private LocalDateTime publishedAt;
    private LocalDateTime updatedAt;
}
