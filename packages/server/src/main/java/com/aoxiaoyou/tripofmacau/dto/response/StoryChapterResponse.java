package com.aoxiaoyou.tripofmacau.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StoryChapterResponse {

    private Long id;
    private Integer chapterOrder;
    private String status;
    private String title;
    private String summary;
    private String detail;
    private String achievement;
    private String collectible;
    private String locationName;
    private String anchorType;
    private Long anchorTargetId;
    private String anchorTargetCode;
    private String unlockType;
    private String mediaUrl;
    private Long experienceFlowId;
    private Object overridePolicy;
    private Object storyModeConfig;
    private String primaryMediaUrl;
    private StoryMediaAssetResponse primaryMediaAsset;
    private StoryChapterUnlockResponse unlock;
    private StoryChapterConditionResponse prerequisite;
    private StoryChapterConditionResponse completion;
    private StoryChapterEffectResponse effect;
    private java.util.List<StoryContentBlockResponse> contentBlocks;
    private String prerequisiteJson;
    private String completionJson;
    private String rewardJson;
    private Integer sortOrder;
}
