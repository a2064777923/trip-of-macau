package com.aoxiaoyou.admin.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class AdminCoordinatePreviewResponse {
    private String sourceCoordinateSystem;
    private BigDecimal sourceLatitude;
    private BigDecimal sourceLongitude;
    private BigDecimal normalizedLatitude;
    private BigDecimal normalizedLongitude;
    private String normalizationStatus;
    private String note;
}
