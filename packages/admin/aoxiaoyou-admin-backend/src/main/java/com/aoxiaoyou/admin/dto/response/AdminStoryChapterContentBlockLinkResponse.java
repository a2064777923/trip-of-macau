package com.aoxiaoyou.admin.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminStoryChapterContentBlockLinkResponse {
    private Long id;
    private Long chapterId;
    private Long blockId;
    private String overrideTitleJson;
    private String overrideSummaryJson;
    private String overrideBodyJson;
    private String displayConditionJson;
    private String overrideConfigJson;
    private String status;
    private Integer sortOrder;
    private AdminStoryContentBlockResponse block;
}
