package com.aoxiaoyou.admin.common.enums;

import java.util.Arrays;

public enum CoordinateSystem {
    GCJ02("GCJ02"),
    WGS84("WGS84"),
    BD09("BD09"),
    UNKNOWN("UNKNOWN");

    private final String code;

    CoordinateSystem(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static CoordinateSystem fromCode(String code) {
        if (code == null || code.isBlank()) {
            return GCJ02;
        }
        return Arrays.stream(values())
                .filter(value -> value.code.equalsIgnoreCase(code))
                .findFirst()
                .orElse(UNKNOWN);
    }
}
