package com.aoxiaoyou.admin.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminIndoorRuleConflictResponse {
    private Long behaviorId;
    private Long nodeId;
    private Long buildingId;
    private Long floorId;
    private Long relatedBehaviorId;
    private Long relatedNodeId;
    private String conflictCode;
    private String severity;
    private String message;
}
