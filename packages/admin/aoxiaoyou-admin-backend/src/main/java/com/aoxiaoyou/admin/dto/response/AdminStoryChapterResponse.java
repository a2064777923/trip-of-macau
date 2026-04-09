package com.aoxiaoyou.admin.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminStoryChapterResponse {

    private Long id;
    private Long storyLineId;
    private Integer chapterOrder;
    private String titleZh;
    private String titleEn;
    private String titleZht;
    private String mediaType;
    private String mediaUrl;
    private String scriptZh;
    private String scriptEn;
    private String scriptZht;
    private String unlockType;
    private String unlockParam;
    private Integer duration;
    private String createdAt;
    private String updatedAt;
}
