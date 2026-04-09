package com.aoxiaoyou.admin.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AdminStoryLineListItemResponse {

    private Long storylineId;
    private String code;
    private String name;
    private String description;
    private String coverImageUrl;
    private String category;
    private String difficulty;
    private String status;
    private Integer poiCount;
    private Integer participationCount;
    private Integer completionCount;
    private LocalDateTime createdAt;
}
