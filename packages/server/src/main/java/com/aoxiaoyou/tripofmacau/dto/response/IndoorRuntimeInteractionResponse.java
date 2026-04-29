package com.aoxiaoyou.tripofmacau.dto.response;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndoorRuntimeInteractionResponse {
    private Boolean interactionAccepted;
    private Boolean visible;
    private String matchedTriggerId;
    private String blockedReason;
    private Boolean requiresAuth;
    private List<TriggeredEffect> effects;
    private Long interactionLogId;
    private String cooldownUntil;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TriggeredEffect {
        private String effectId;
        private String category;
        private String label;
        private JsonNode config;
        private IndoorRuntimeFloorResponse.PathGraph pathGraph;
        private IndoorRuntimeFloorResponse.OverlayGeometry overlayGeometry;
    }
}
