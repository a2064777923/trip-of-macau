package com.aoxiaoyou.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminUserProgressRecomputePreviewRequest {

    @Positive(message = "userId must be positive when provided")
    private Long userId;

    @NotBlank(message = "scopeType is required")
    private String scopeType = "global";

    @Positive(message = "scopeId must be positive when provided")
    private Long scopeId;

    @Positive(message = "storylineId must be positive when provided")
    private Long storylineId;

    private LocalDateTime from;

    private LocalDateTime to;

    @NotBlank(message = "reason is required")
    private String reason;
}
