package com.aoxiaoyou.admin.dto.request;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class AdminIndoorNodeBehaviorPayload {
    private String behaviorCode;
    private String behaviorNameZh;
    private String behaviorNameEn;
    private String behaviorNameZht;
    private String behaviorNamePt;
    private String appearancePresetCode;
    private String triggerTemplateCode;
    private String effectTemplateCode;
    private List<RuleCondition> appearanceRules = new ArrayList<>();
    private List<TriggerStep> triggerRules = new ArrayList<>();
    private List<EffectDefinition> effectRules = new ArrayList<>();
    private List<Long> rewardRuleIds = new ArrayList<>();
    private PathGraph pathGraph;
    private OverlayGeometry overlayGeometry;
    private String inheritMode;
    private String runtimeSupportLevel;
    private Integer sortOrder;
    private String status;

    @Data
    public static class CoordinatePoint {
        private BigDecimal x;
        private BigDecimal y;
        private Integer order;
    }

    @Data
    public static class OverlayGeometry {
        private String geometryType;
        private List<CoordinatePoint> points = new ArrayList<>();
        private JsonNode properties;
    }

    @Data
    public static class RuleCondition {
        private String id;
        private String category;
        private String label;
        private JsonNode config;
    }

    @Data
    public static class TriggerStep {
        private String id;
        private String category;
        private String label;
        private String dependsOnTriggerId;
        private JsonNode config;
    }

    @Data
    public static class EffectDefinition {
        private String id;
        private String category;
        private String label;
        private JsonNode config;
    }

    @Data
    public static class PathGraph {
        private List<CoordinatePoint> points = new ArrayList<>();
        private Integer durationMs;
        private Integer holdMs;
        private Boolean loop;
        private String easing;
    }
}
