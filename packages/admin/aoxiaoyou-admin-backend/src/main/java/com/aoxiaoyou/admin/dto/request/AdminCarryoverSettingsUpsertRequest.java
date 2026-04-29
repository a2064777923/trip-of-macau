package com.aoxiaoyou.admin.dto.request;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class AdminCarryoverSettingsUpsertRequest {

    private String translationDefaultLocale;

    private List<String> translationEnginePriority;

    private String mediaUploadDefaultPolicyCode;

    private BigDecimal mapZoomDefaultMinScale;

    private BigDecimal mapZoomDefaultMaxScale;

    private BigDecimal indoorZoomDefaultMinScale;

    private BigDecimal indoorZoomDefaultMaxScale;
}
