package com.aoxiaoyou.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminUserProgressRepairRequest {

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

    @NotBlank(message = "actionType is required")
    private String actionType;

    @Positive(message = "targetEventId must be positive when provided")
    private Long targetEventId;

    @Positive(message = "replacementElementId must be positive when provided")
    private Long replacementElementId;

    private String replacementElementCode;

    @Positive(message = "duplicateOfEventId must be positive when provided")
    private Long duplicateOfEventId;

    @NotBlank(message = "reason is required")
    private String reason;

    private String previewHash;

    private String confirmationToken;

    @Pattern(regexp = "REPAIR", message = "confirmationText must be REPAIR")
    private String confirmationText;
}
