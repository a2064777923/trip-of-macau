package com.aoxiaoyou.admin.common.enums;

import java.util.Arrays;
import java.util.Set;

public enum SpatialEntityType {
    CITY("city"),
    SUB_MAP("sub_map"),
    POI("poi"),
    INDOOR_BUILDING("indoor_building"),
    INDOOR_FLOOR("indoor_floor");

    private final String code;

    SpatialEntityType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static SpatialEntityType fromCode(String code) {
        return Arrays.stream(values())
                .filter(value -> value.code.equalsIgnoreCase(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown spatial entity type: " + code));
    }

    public static Set<String> supportedCodes() {
        return Set.of(CITY.code, SUB_MAP.code, POI.code, INDOOR_BUILDING.code, INDOOR_FLOOR.code);
    }
}
