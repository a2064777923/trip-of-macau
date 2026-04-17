package com.aoxiaoyou.admin.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminAiCapabilityResponse {

    private Long id;
    private String domainCode;
    private String capabilityCode;
    private String displayNameZht;
    private String summaryZht;
    private Integer supportsPublicRuntime;
    private Integer supportsAdminCreative;
    private Integer supportsText;
    private Integer supportsImage;
    private Integer supportsAudio;
    private Integer supportsVision;
    private String status;
    private Integer sortOrder;
    private Integer policyCount;
    private Long requestCount24h;
    private Long failedCount24h;
    private Long fallbackCount24h;
}
