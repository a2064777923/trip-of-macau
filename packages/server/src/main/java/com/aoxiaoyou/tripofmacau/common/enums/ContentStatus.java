package com.aoxiaoyou.tripofmacau.common.enums;

import java.util.Arrays;

public enum ContentStatus {
    DRAFT("draft"),
    PUBLISHED("published"),
    ARCHIVED("archived");

    private final String code;

    ContentStatus(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static ContentStatus fromCode(String code) {
        return Arrays.stream(values())
                .filter(value -> value.code.equalsIgnoreCase(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown content status code: " + code));
    }
}

