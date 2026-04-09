package com.aoxiaoyou.admin.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AdminRewardResponse {

    private Long id;
    private String name;
    private String description;
    private Integer stampsRequired;
    private Integer totalQuantity;
    private Integer redeemedCount;
    private Integer remainingQuantity;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private LocalDateTime createdAt;
}
