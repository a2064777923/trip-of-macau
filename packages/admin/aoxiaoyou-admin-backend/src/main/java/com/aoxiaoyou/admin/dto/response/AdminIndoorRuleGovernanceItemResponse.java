package com.aoxiaoyou.admin.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminIndoorRuleGovernanceItemResponse {
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
    private Integer linkedRewardRuleCount;
}
