package com.aoxiaoyou.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminLifecycleImpactResponse {
    private Long id;
    private String impactType;
    private String impactTypeLabel;
    private String severity;
    private String severityLabel;
    private String sourceType;
    private Long sourceId;
    private String sourceCode;
    private String sourceName;
    private String relationType;
    private String targetType;
    private Long targetId;
    private String targetCode;
    private String targetName;
    private String impactSummary;
    private Map<String, Object> metadata;
    private Integer sortOrder;
}
