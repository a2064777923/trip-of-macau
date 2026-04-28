package com.aoxiaoyou.tripofmacau.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExperienceEventResponse {
    private boolean accepted;
    private Long eventId;
    private Long userId;
    private Long elementId;
    private String elementCode;
    private String eventType;
    private String storylineSessionId;
}
