package com.aoxiaoyou.admin.common.enums;

import java.util.Arrays;

public enum ContentStatus {
    EDITING("editing"),
    REVIEWING("reviewing"),
    DRAFT("draft"),
    PUBLISHED("published"),
    UNPUBLISHED("unpublished"),
    ARCHIVED("archived"),
    DELETED("deleted");

    private final String code;

    ContentStatus(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public String canonicalCode() {
        return switch (this) {
            case DRAFT -> EDITING.code;
            case ARCHIVED -> UNPUBLISHED.code;
            default -> code;
        };
    }

    public String labelZht() {
        return switch (canonicalCode()) {
            case "editing" -> "編輯中";
            case "reviewing" -> "審批中";
            case "published" -> "已發布";
            case "unpublished" -> "未發布";
            case "deleted" -> "已刪除";
            default -> canonicalCode();
        };
    }

    public boolean isTravelerVisible() {
        return this == PUBLISHED;
    }

    public boolean isTerminalDeleted() {
        return this == DELETED;
    }

    public static ContentStatus fromCode(String code) {
        return Arrays.stream(values())
                .filter(value -> value.code.equalsIgnoreCase(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown content status code: " + code));
    }

    public static ContentStatus fromCanonicalCode(String code) {
        return Arrays.stream(values())
                .filter(value -> value.canonicalCode().equalsIgnoreCase(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown content status code: " + code));
    }
}
