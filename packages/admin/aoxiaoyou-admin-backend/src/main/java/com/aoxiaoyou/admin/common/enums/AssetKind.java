package com.aoxiaoyou.admin.common.enums;

import java.util.Arrays;

public enum AssetKind {
    IMAGE("image"),
    ICON("icon"),
    MAP_TILE("map_tile"),
    AUDIO("audio"),
    JSON("json"),
    OTHER("other");

    private final String code;

    AssetKind(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static AssetKind fromCode(String code) {
        return Arrays.stream(values())
                .filter(value -> value.code.equalsIgnoreCase(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown asset kind code: " + code));
    }
}
