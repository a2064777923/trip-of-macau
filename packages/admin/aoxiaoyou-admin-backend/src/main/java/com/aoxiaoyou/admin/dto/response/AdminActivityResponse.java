package com.aoxiaoyou.admin.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AdminActivityResponse {

    private Long id;
    private String code;
    private String title;
    private String description;
    private String coverUrl;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private Integer participationCount;
    private LocalDateTime createdAt;
}
