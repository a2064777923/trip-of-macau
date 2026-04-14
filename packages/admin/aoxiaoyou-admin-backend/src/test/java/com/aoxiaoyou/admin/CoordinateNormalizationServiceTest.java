package com.aoxiaoyou.admin;

import com.aoxiaoyou.admin.common.spatial.CoordinateNormalizationResult;
import com.aoxiaoyou.admin.common.spatial.CoordinateNormalizationService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class CoordinateNormalizationServiceTest {

    private final CoordinateNormalizationService service = new CoordinateNormalizationService();

    @Test
    void normalizesWgs84ToGcj02AndPreservesSourceCoordinates() {
        CoordinateNormalizationResult result = service.normalizeToGcj02(
                "WGS84",
                BigDecimal.valueOf(39.9088230),
                BigDecimal.valueOf(116.3974700)
        );

        assertThat(result.getSourceCoordinateSystem().getCode()).isEqualTo("WGS84");
        assertThat(result.getSourceLatitude()).isEqualByComparingTo("39.9088230");
        assertThat(result.getSourceLongitude()).isEqualByComparingTo("116.3974700");
        assertThat(result.getNormalizationStatus()).isEqualTo("converted");
        assertThat(result.getNormalizedLatitude()).isNotEqualByComparingTo(result.getSourceLatitude());
        assertThat(result.getNormalizedLongitude()).isNotEqualByComparingTo(result.getSourceLongitude());
    }

    @Test
    void normalizesBd09ToGcj02() {
        CoordinateNormalizationResult result = service.normalizeToGcj02(
                "BD09",
                BigDecimal.valueOf(39.9150000),
                BigDecimal.valueOf(116.4040000)
        );

        assertThat(result.getSourceCoordinateSystem().getCode()).isEqualTo("BD09");
        assertThat(result.getNormalizationStatus()).isEqualTo("converted");
        assertThat(result.getNormalizedLatitude()).isNotEqualByComparingTo(result.getSourceLatitude());
        assertThat(result.getNormalizedLongitude()).isNotEqualByComparingTo(result.getSourceLongitude());
    }
}
