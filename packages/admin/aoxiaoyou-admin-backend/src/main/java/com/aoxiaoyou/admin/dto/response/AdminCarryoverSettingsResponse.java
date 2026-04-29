package com.aoxiaoyou.admin.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class AdminCarryoverSettingsResponse {

    private String translationDefaultLocale;

    private List<String> translationEnginePriority;

    private String mediaUploadDefaultPolicyCode;

    private BigDecimal mapZoomDefaultMinScale;

    private BigDecimal mapZoomDefaultMaxScale;

    private BigDecimal indoorZoomDefaultMinScale;

    private BigDecimal indoorZoomDefaultMaxScale;
}
