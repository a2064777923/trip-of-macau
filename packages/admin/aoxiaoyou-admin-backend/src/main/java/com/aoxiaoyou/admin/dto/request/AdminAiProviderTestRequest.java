package com.aoxiaoyou.admin.dto.request;

import lombok.Data;

@Data
public class AdminAiProviderTestRequest {

    private String capabilityCode;

    private String prompt;

    private String modelOverride;
}
