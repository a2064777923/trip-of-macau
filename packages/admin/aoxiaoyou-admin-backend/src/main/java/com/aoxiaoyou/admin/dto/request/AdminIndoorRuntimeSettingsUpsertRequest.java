package com.aoxiaoyou.admin.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AdminIndoorRuntimeSettingsUpsertRequest {

    private BigDecimal minScaleMeters;
    private BigDecimal maxScaleMeters;
    private Integer referenceViewportPx;
    private Integer defaultTileSizePx;
}
