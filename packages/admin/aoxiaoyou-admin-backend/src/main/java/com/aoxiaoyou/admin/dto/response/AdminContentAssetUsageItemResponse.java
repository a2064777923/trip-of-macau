package com.aoxiaoyou.admin.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminContentAssetUsageItemResponse {
    private String relationType;
    private String entityType;
    private Long entityId;
    private String entityCode;
    private String entityName;
    private String usageType;
    private String fieldName;
    private String status;
    private String title;
}
