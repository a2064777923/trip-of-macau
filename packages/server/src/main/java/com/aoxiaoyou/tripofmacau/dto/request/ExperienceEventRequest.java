package com.aoxiaoyou.tripofmacau.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ExperienceEventRequest {
    private Long elementId;
    private String elementCode;
    @NotBlank(message = "eventType is required")
    private String eventType;
    private String eventSource;
    private String storylineSessionId;
    private String clientEventId;
    private String payloadJson;
    private String occurredAt;
}
