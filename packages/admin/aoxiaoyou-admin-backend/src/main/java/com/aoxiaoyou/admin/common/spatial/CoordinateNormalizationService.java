package com.aoxiaoyou.admin.common.spatial;

import com.aoxiaoyou.admin.common.enums.CoordinateSystem;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class CoordinateNormalizationService {

    private static final double PI = 3.1415926535897932384626;
    private static final double AXIS = 6378245.0;
    private static final double EE = 0.00669342162296594323;
    private static final double X_PI = PI * 3000.0 / 180.0;

    public CoordinateNormalizationResult normalizeToGcj02(String sourceCoordinateSystem, BigDecimal latitude, BigDecimal longitude) {
        CoordinateSystem coordinateSystem = CoordinateSystem.fromCode(sourceCoordinateSystem);
        if (latitude == null || longitude == null) {
            return CoordinateNormalizationResult.builder()
                    .sourceCoordinateSystem(coordinateSystem)
                    .sourceLatitude(latitude)
                    .sourceLongitude(longitude)
                    .normalizedLatitude(latitude)
                    .normalizedLongitude(longitude)
                    .normalizationStatus("missing-input")
                    .note("Latitude or longitude is empty.")
                    .build();
        }

        BigDecimal normalizedLatitude = latitude;
        BigDecimal normalizedLongitude = longitude;
        String status = "passthrough";
        String note = "Coordinates already use GCJ-02.";

        if (coordinateSystem == CoordinateSystem.WGS84) {
            double[] converted = wgs84ToGcj02(latitude.doubleValue(), longitude.doubleValue());
            normalizedLatitude = scale(converted[0]);
            normalizedLongitude = scale(converted[1]);
            status = "converted";
            note = "Converted from WGS84 to GCJ-02.";
        } else if (coordinateSystem == CoordinateSystem.BD09) {
            double[] converted = bd09ToGcj02(latitude.doubleValue(), longitude.doubleValue());
            normalizedLatitude = scale(converted[0]);
            normalizedLongitude = scale(converted[1]);
            status = "converted";
            note = "Converted from BD-09 to GCJ-02.";
        } else if (coordinateSystem == CoordinateSystem.UNKNOWN) {
            status = "unknown-source";
            note = "Unsupported source system, preserving original coordinates.";
        }

        return CoordinateNormalizationResult.builder()
                .sourceCoordinateSystem(coordinateSystem)
                .sourceLatitude(scale(latitude.doubleValue()))
                .sourceLongitude(scale(longitude.doubleValue()))
                .normalizedLatitude(normalizedLatitude)
                .normalizedLongitude(normalizedLongitude)
                .normalizationStatus(status)
                .note(note)
                .build();
    }

    private double[] wgs84ToGcj02(double latitude, double longitude) {
        if (outOfChina(latitude, longitude)) {
            return new double[] {latitude, longitude};
        }
        double dLat = transformLatitude(longitude - 105.0, latitude - 35.0);
        double dLon = transformLongitude(longitude - 105.0, latitude - 35.0);
        double radLat = latitude / 180.0 * PI;
        double magic = Math.sin(radLat);
        magic = 1 - EE * magic * magic;
        double sqrtMagic = Math.sqrt(magic);
        dLat = (dLat * 180.0) / ((AXIS * (1 - EE)) / (magic * sqrtMagic) * PI);
        dLon = (dLon * 180.0) / (AXIS / sqrtMagic * Math.cos(radLat) * PI);
        return new double[] {latitude + dLat, longitude + dLon};
    }

    private double[] bd09ToGcj02(double latitude, double longitude) {
        double x = longitude - 0.0065;
        double y = latitude - 0.006;
        double z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * X_PI);
        double theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * X_PI);
        double gcjLongitude = z * Math.cos(theta);
        double gcjLatitude = z * Math.sin(theta);
        return new double[] {gcjLatitude, gcjLongitude};
    }

    private boolean outOfChina(double latitude, double longitude) {
        return longitude < 72.004 || longitude > 137.8347 || latitude < 0.8293 || latitude > 55.8271;
    }

    private double transformLatitude(double longitude, double latitude) {
        double result = -100.0 + 2.0 * longitude + 3.0 * latitude + 0.2 * latitude * latitude
                + 0.1 * longitude * latitude + 0.2 * Math.sqrt(Math.abs(longitude));
        result += (20.0 * Math.sin(6.0 * longitude * PI) + 20.0 * Math.sin(2.0 * longitude * PI)) * 2.0 / 3.0;
        result += (20.0 * Math.sin(latitude * PI) + 40.0 * Math.sin(latitude / 3.0 * PI)) * 2.0 / 3.0;
        result += (160.0 * Math.sin(latitude / 12.0 * PI) + 320 * Math.sin(latitude * PI / 30.0)) * 2.0 / 3.0;
        return result;
    }

    private double transformLongitude(double longitude, double latitude) {
        double result = 300.0 + longitude + 2.0 * latitude + 0.1 * longitude * longitude
                + 0.1 * longitude * latitude + 0.1 * Math.sqrt(Math.abs(longitude));
        result += (20.0 * Math.sin(6.0 * longitude * PI) + 20.0 * Math.sin(2.0 * longitude * PI)) * 2.0 / 3.0;
        result += (20.0 * Math.sin(longitude * PI) + 40.0 * Math.sin(longitude / 3.0 * PI)) * 2.0 / 3.0;
        result += (150.0 * Math.sin(longitude / 12.0 * PI) + 300.0 * Math.sin(longitude / 30.0 * PI)) * 2.0 / 3.0;
        return result;
    }

    private BigDecimal scale(double value) {
        return BigDecimal.valueOf(value).setScale(7, RoundingMode.HALF_UP);
    }
}
