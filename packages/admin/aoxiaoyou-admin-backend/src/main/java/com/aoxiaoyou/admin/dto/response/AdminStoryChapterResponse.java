package com.aoxiaoyou.admin.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AdminStoryChapterResponse {
    private Long id;
    private Long storylineId;
    private Integer chapterOrder;
    private String titleZh;
    private String titleEn;
    private String titleZht;
    private String titlePt;
    private String summaryZh;
    private String summaryEn;
    private String summaryZht;
    private String summaryPt;
    private String detailZh;
    private String detailEn;
    private String detailZht;
    private String detailPt;
    private String achievementZh;
    private String achievementEn;
    private String achievementZht;
    private String achievementPt;
    private String collectibleZh;
    private String collectibleEn;
    private String collectibleZht;
    private String collectiblePt;
    private String locationNameZh;
    private String locationNameEn;
    private String locationNameZht;
    private String locationNamePt;
    private Long mediaAssetId;
    private String unlockType;
    private String unlockParamJson;
    private String status;
    private Integer sortOrder;
    private LocalDateTime publishedAt;
    private String createdAt;
    private String updatedAt;
}
