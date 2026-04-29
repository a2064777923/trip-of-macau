package com.aoxiaoyou.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminIndoorRuntimeSettingsResponse {

    private BigDecimal minScaleMeters;
    private BigDecimal maxScaleMeters;
    private Integer referenceViewportPx;
    private Integer defaultTileSizePx;
}
