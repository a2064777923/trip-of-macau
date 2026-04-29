package com.aoxiaoyou.admin.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

public class AdminStoryChapterUpsertRequest {

    @Data
    public static class Upsert {
        private Long storylineId;

        @NotNull(message = "chapterOrder is required")
        @Min(value = 1, message = "chapterOrder must be greater than 0")
        private Integer chapterOrder;

        @NotBlank(message = "titleZh is required")
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
        private String unlockType;
        private String unlockParamJson;
        private String prerequisiteJson;
        private String completionJson;
        private String rewardJson;
        private String status;
        private Integer sortOrder;
        private String publishedAt;
        private List<ContentBlockLinkUpsert> contentBlocks;
    }

    @Data
    public static class ContentBlockLinkUpsert {
        private Long id;
        private Long blockId;
        private String overrideTitleJson;
        private String overrideSummaryJson;
        private String overrideBodyJson;
        private String displayConditionJson;
        private String overrideConfigJson;
        private String status;
        private Integer sortOrder;
    }
}
