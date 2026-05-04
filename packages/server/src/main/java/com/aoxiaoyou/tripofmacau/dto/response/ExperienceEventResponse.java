package com.aoxiaoyou.tripofmacau.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

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
    private boolean duplicate;
    private String acceptedAt;
    private Long currentChapterId;
    private String message;
    private String eventStatus;
    private String outcomeType;
    private String feedbackTitle;
    private String feedbackMessage;
    private List<String> outcomeLabels;
    private UserExplorationResponse explorationSummary;
}
