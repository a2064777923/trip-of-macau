package com.aoxiaoyou.tripofmacau.dto.response;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndoorRuntimeFloorResponse {
    private Long floorId;
    private String floorCode;
    private Integer floorNumber;
    private Long buildingId;
    private String buildingCode;
    private String name;
    private String description;
    private String coverImageUrl;
    private String floorPlanUrl;
    private String tileSourceType;
    private String tilePreviewImageUrl;
    private String tileRootUrl;
    private String tileManifestJson;
    private String tileZoomDerivationJson;
    private Integer imageWidthPx;
    private Integer imageHeightPx;
    private Integer tileSizePx;
    private Integer gridCols;
    private Integer gridRows;
    private Integer tileLevelCount;
    private Integer tileEntryCount;
    private String importStatus;
    private String importNote;
    private BigDecimal altitudeMeters;
    private BigDecimal areaSqm;
    private BigDecimal zoomMin;
    private BigDecimal zoomMax;
    private BigDecimal defaultZoom;
    private String popupConfigJson;
    private String displayConfigJson;
    private String runtimeVersion;
    private List<Node> nodes;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Node {
        private Long nodeId;
        private String markerCode;
        private String nodeType;
        private String presentationMode;
        private String overlayType;
        private String name;
        private String description;
        private BigDecimal relativeX;
        private BigDecimal relativeY;
        private Long relatedPoiId;
        private String iconUrl;
        private String animationUrl;
        private String linkedEntityType;
        private Long linkedEntityId;
        private String popupConfigJson;
        private String displayConfigJson;
        private Integer sortOrder;
        private String status;
        private String runtimeSupportLevel;
        private OverlayGeometry overlayGeometry;
        private List<Behavior> behaviors;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Behavior {
        private Long behaviorId;
        private String behaviorCode;
        private String name;
        private String status;
        private Integer sortOrder;
        private String runtimeSupportLevel;
        private Boolean supported;
        private Boolean requiresAuth;
        private String blockedReason;
        private List<RuleCondition> appearanceRules;
        private List<TriggerRule> triggerRules;
        private List<EffectRule> effectRules;
        private PathGraph pathGraph;
        private OverlayGeometry overlayGeometry;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RuleCondition {
        private String id;
        private String category;
        private String label;
        private JsonNode config;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TriggerRule {
        private String id;
        private String category;
        private String label;
        private String dependsOnTriggerId;
        private JsonNode config;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EffectRule {
        private String id;
        private String category;
        private String label;
        private JsonNode config;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CoordinatePoint {
        private BigDecimal x;
        private BigDecimal y;
        private Integer order;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PathGraph {
        private List<CoordinatePoint> points;
        private Integer durationMs;
        private Integer holdMs;
        private Boolean loop;
        private String easing;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OverlayGeometry {
        private String geometryType;
        private List<CoordinatePoint> points;
        private JsonNode properties;
    }
}
