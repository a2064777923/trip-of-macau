package com.aoxiaoyou.admin.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AdminIndoorRuleStatusUpdateResponse {
    private Long behaviorId;
    private String status;
    private String parentNodeStatus;
    private List<String> warnings;
}
