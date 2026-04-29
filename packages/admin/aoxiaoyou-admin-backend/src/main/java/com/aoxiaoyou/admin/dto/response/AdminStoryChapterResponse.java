package com.aoxiaoyou.admin.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

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
    private Long experienceFlowId;
    private String overridePolicyJson;
    private String storyModeConfigJson;
    private String anchorType;
    private Long anchorTargetId;
    private String anchorTargetCode;
    private String anchorTargetLabel;
    private String unlockType;
    private String unlockParamJson;
    private String prerequisiteJson;
    private String completionJson;
    private String rewardJson;
    private String status;
    private Integer sortOrder;
    private LocalDateTime publishedAt;
    private String createdAt;
    private String updatedAt;
    private List<AdminStoryChapterContentBlockLinkResponse> contentBlocks;
}
