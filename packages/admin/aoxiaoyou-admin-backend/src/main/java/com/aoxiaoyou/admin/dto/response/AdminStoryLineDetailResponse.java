package com.aoxiaoyou.admin.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class AdminStoryLineDetailResponse {

    private Long storylineId;
    private String code;
    private String name;
    private String description;
    private String coverImageUrl;
    private String bannerImageUrl;
    private String category;
    private String difficulty;
    private Integer estimatedDurationMinutes;
    private List<String> tags;
    private String status;
    private Integer totalChapters;
    private Integer participationCount;
    private Integer completionCount;
    private Integer averageCompletionTime;
    private LocalDateTime publishAt;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
