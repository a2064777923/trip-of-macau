package com.aoxiaoyou.tripofmacau.common.enums;

import java.util.Arrays;

public enum LocaleCode {
    ZH_HANS("zh-Hans"),
    ZH_HANT("zh-Hant"),
    EN("en"),
    PT("pt");

    private final String code;

    LocaleCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static LocaleCode fromCode(String code) {
        return Arrays.stream(values())
                .filter(value -> value.code.equalsIgnoreCase(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown locale code: " + code));
    }
}
