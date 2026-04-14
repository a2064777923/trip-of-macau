package com.aoxiaoyou.admin.common.spatial;

import com.aoxiaoyou.admin.common.enums.CoordinateSystem;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class CoordinateNormalizationResult {
    private CoordinateSystem sourceCoordinateSystem;
    private BigDecimal sourceLatitude;
    private BigDecimal sourceLongitude;
    private BigDecimal normalizedLatitude;
    private BigDecimal normalizedLongitude;
    private String normalizationStatus;
    private String note;
}
