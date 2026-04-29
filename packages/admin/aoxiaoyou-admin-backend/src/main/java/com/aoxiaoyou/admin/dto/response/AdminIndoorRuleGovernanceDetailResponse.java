package com.aoxiaoyou.admin.dto.response;

import com.aoxiaoyou.admin.dto.request.AdminIndoorNodeBehaviorPayload;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AdminIndoorRuleGovernanceDetailResponse {
    private Long nodeId;
    private Long behaviorId;
    private String behaviorCode;
    private String behaviorNameZh;
    private String behaviorNameZht;
    private String behaviorNameEn;
    private String behaviorNamePt;
    private String markerCode;
    private String presentationMode;
    private String overlayType;
    private Long buildingId;
    private String buildingNameZht;
    private Long floorId;
    private String floorCode;
    private String linkedEntityType;
    private Long linkedEntityId;
    private String runtimeSupportLevel;
    private String status;
    private Integer appearanceRuleCount;
    private Integer triggerRuleCount;
    private Integer effectRuleCount;
    private Boolean hasPathGraph;
    private Integer conflictCount;
    private List<AdminIndoorNodeBehaviorPayload.RuleCondition> appearanceRules;
    private List<AdminIndoorNodeBehaviorPayload.TriggerStep> triggerRules;
    private List<AdminIndoorNodeBehaviorPayload.EffectDefinition> effectRules;
    private AdminIndoorNodeBehaviorPayload.PathGraph pathGraph;
    private List<Long> linkedRewardRuleIds;
    private List<AdminRewardRuleLinkResponse> linkedRewardRules;
    private List<AdminRewardLinkedEntityResponse> linkedRewards;
    private List<AdminIndoorRuleConflictResponse> conflicts;
    private ParentNodeSummary parentNode;

    @Data
    @Builder
    public static class ParentNodeSummary {
        private Long nodeId;
        private String markerCode;
        private String nodeNameZht;
        private String nodeStatus;
        private String presentationMode;
        private String overlayType;
        private Long buildingId;
        private String buildingNameZht;
        private Long floorId;
        private String floorCode;
        private Long relatedPoiId;
    }
}
