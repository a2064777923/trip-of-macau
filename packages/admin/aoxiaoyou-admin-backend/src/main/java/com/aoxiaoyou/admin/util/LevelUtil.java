package com.aoxiaoyou.admin.util;

public final class LevelUtil {

    private LevelUtil() {
    }

    public static int normalizeLevel(Integer totalStamps) {
        int value = totalStamps == null ? 0 : totalStamps;
        if (value >= 12) return 5;
        if (value >= 9) return 4;
        if (value >= 6) return 3;
        if (value >= 3) return 2;
        return 1;
    }

    public static String levelName(Integer level) {
        if (level == null) return "探索新手";
        return switch (level) {
            case 1 -> "探索新手";
            case 2 -> "澳门新手";
            case 3 -> "澳门探索者";
            case 4 -> "澳门达人";
            default -> "澳门通";
        };
    }

    public static int nextLevelExp(Integer level) {
        if (level == null || level <= 1) return 3;
        if (level == 2) return 6;
        if (level == 3) return 9;
        if (level == 4) return 12;
        return 12;
    }
}
