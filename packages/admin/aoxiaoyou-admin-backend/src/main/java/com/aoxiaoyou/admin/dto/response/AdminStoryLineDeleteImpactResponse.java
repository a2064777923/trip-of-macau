package com.aoxiaoyou.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminStoryLineDeleteImpactResponse {
    private Long storylineId;
    private String code;
    private String name;
    private String status;
    private Boolean publicVisible;
    private Map<String, Long> dependencyCounts;
    private Boolean hardDeleteAllowed;
    private String recommendedAction;
    private List<String> blockingReasons;
    private List<String> warningReasons;
}
