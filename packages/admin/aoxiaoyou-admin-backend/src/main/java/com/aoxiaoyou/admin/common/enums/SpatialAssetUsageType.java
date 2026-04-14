package com.aoxiaoyou.admin.common.enums;

import java.util.Arrays;
import java.util.Set;

public enum SpatialAssetUsageType {
    COVER("cover"),
    GALLERY("gallery"),
    POPUP("popup"),
    AUDIO("audio"),
    VIDEO("video"),
    MAP_ICON("map-icon");

    private final String code;

    SpatialAssetUsageType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static SpatialAssetUsageType fromCode(String code) {
        return Arrays.stream(values())
                .filter(value -> value.code.equalsIgnoreCase(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown spatial asset usage type: " + code));
    }

    public static Set<String> supportedCodes() {
        return Set.of(
                COVER.code,
                GALLERY.code,
                POPUP.code,
                AUDIO.code,
                VIDEO.code,
                MAP_ICON.code
        );
    }
}
