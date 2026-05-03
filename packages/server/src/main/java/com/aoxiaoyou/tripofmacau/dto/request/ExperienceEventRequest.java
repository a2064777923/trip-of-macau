package com.aoxiaoyou.tripofmacau.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ExperienceEventRequest {
    private Long elementId;
    private String elementCode;
    /**
     * Allowed values: story_opened, chapter_started, content_viewed, media_completed,
     * pickup_interacted, task_completed, reward_acquired, unsupported_viewed,
     * story_session_exit, and backward-compatible chapter_open.
     */
    @NotBlank(message = "eventType is required")
    private String eventType;
    private String eventSource;
    private String storylineSessionId;
    private String clientEventId;
    private String payloadJson;
    private String occurredAt;
}
