package com.aoxiaoyou.tripofmacau.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StoryChapterResponse {

    private Long id;
    private Integer chapterOrder;
    private String title;
    private String summary;
    private String detail;
    private String achievement;
    private String collectible;
    private String locationName;
    private String unlockType;
    private String mediaUrl;
    private Integer sortOrder;
}
