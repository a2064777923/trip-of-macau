package com.aoxiaoyou.admin.common.encoding;

import org.springframework.beans.BeanUtils;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public final class SuspiciousTextGuard {

    private SuspiciousTextGuard() {
    }

    public static Optional<SuspiciousTextIssue> findFirstIssue(Object payload) {
        return inspectValue(payload, "$", new IdentityHashMap<>());
    }

    private static Optional<SuspiciousTextIssue> inspectValue(
            Object value,
            String path,
            IdentityHashMap<Object, Boolean> visited
    ) {
        if (value == null) {
            return Optional.empty();
        }
        if (value instanceof String text) {
            return inspectString(text, path);
        }
        Class<?> valueType = value.getClass();
        if (BeanUtils.isSimpleValueType(valueType) || value instanceof Enum<?>) {
            return Optional.empty();
        }
        if (visited.put(value, Boolean.TRUE) != null) {
            return Optional.empty();
        }
        if (value instanceof Map<?, ?> map) {
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                String childPath = appendPath(path, String.valueOf(entry.getKey()));
                Optional<SuspiciousTextIssue> issue = inspectValue(entry.getValue(), childPath, visited);
                if (issue.isPresent()) {
                    return issue;
                }
            }
            return Optional.empty();
        }
        if (value instanceof Collection<?> collection) {
            int index = 0;
            for (Object item : collection) {
                Optional<SuspiciousTextIssue> issue = inspectValue(item, path + "[" + index + "]", visited);
                if (issue.isPresent()) {
                    return issue;
                }
                index += 1;
            }
            return Optional.empty();
        }
        if (valueType.isArray()) {
            int length = Array.getLength(value);
            for (int index = 0; index < length; index += 1) {
                Optional<SuspiciousTextIssue> issue = inspectValue(Array.get(value, index), path + "[" + index + "]", visited);
                if (issue.isPresent()) {
                    return issue;
                }
            }
            return Optional.empty();
        }
        for (Field field : getAllFields(valueType)) {
            if (Modifier.isStatic(field.getModifiers()) || field.isSynthetic()) {
                continue;
            }
            field.setAccessible(true);
            try {
                Object nestedValue = field.get(value);
                Optional<SuspiciousTextIssue> issue = inspectValue(nestedValue, appendPath(path, field.getName()), visited);
                if (issue.isPresent()) {
                    return issue;
                }
            } catch (IllegalAccessException ignored) {
                // ignore inaccessible fields
            }
        }
        return Optional.empty();
    }

    private static Optional<SuspiciousTextIssue> inspectString(String value, String path) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        if (containsReplacementCharacter(value)) {
            return Optional.of(new SuspiciousTextIssue(path, "包含替换字符", preview(value)));
        }
        if (containsSuspiciousControlCharacters(value)) {
            return Optional.of(new SuspiciousTextIssue(path, "包含异常控制字符", preview(value)));
        }
        if (!shouldSkipQuestionMarkHeuristic(path) && looksLikeQuestionMarkCorruption(value)) {
            return Optional.of(new SuspiciousTextIssue(path, "包含连续问号或仅由问号组成", preview(value)));
        }
        if (looksLikeUtf8Mojibake(value)) {
            return Optional.of(new SuspiciousTextIssue(path, "疑似 UTF-8 被错误按 ANSI/Latin-1 解码", preview(value)));
        }
        return Optional.empty();
    }

    private static boolean containsReplacementCharacter(String value) {
        return value.indexOf('\uFFFD') >= 0;
    }

    private static boolean containsSuspiciousControlCharacters(String value) {
        for (int index = 0; index < value.length(); index += 1) {
            char current = value.charAt(index);
            if ((current >= 0x00 && current <= 0x08)
                    || current == 0x0B
                    || current == 0x0C
                    || (current >= 0x0E && current <= 0x1F)
                    || (current >= 0x7F && current <= 0x9F)) {
                return true;
            }
        }
        return false;
    }

    private static boolean looksLikeQuestionMarkCorruption(String value) {
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return false;
        }
        if (trimmed.chars().allMatch(character -> character == '?')) {
            return true;
        }
        return trimmed.contains("???");
    }

    private static boolean looksLikeUtf8Mojibake(String value) {
        if (containsCjk(value) || !fitsLatin1(value) || !containsPotentialMojibakeLead(value)) {
            return false;
        }
        String repaired = new String(value.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
        return !repaired.equals(value) && containsCjk(repaired);
    }

    private static boolean fitsLatin1(String value) {
        for (int index = 0; index < value.length(); index += 1) {
            if (value.charAt(index) > 0xFF) {
                return false;
            }
        }
        return true;
    }

    private static boolean containsPotentialMojibakeLead(String value) {
        String leads = "ÃÂÅÆÇÐÑÕÖØàáâãäåçèéêëìíîïðñòóôõöøùúûüýþÿæ";
        for (int index = 0; index < value.length(); index += 1) {
            if (leads.indexOf(value.charAt(index)) >= 0) {
                return true;
            }
        }
        return false;
    }

    private static boolean containsCjk(String value) {
        return value.codePoints().anyMatch(codePoint ->
                (codePoint >= 0x3400 && codePoint <= 0x4DBF)
                        || (codePoint >= 0x4E00 && codePoint <= 0x9FFF)
                        || (codePoint >= 0xF900 && codePoint <= 0xFAFF)
        );
    }

    private static boolean shouldSkipQuestionMarkHeuristic(String path) {
        String lower = path.toLowerCase(Locale.ROOT);
        return lower.endsWith("json")
                || lower.endsWith("html")
                || lower.endsWith("url")
                || lower.endsWith("uri")
                || lower.endsWith("path")
                || lower.endsWith("token")
                || lower.endsWith("secret")
                || lower.endsWith("password")
                || lower.endsWith("key");
    }

    private static String preview(String value) {
        String normalized = value.replace('\n', ' ').replace('\r', ' ').trim();
        if (normalized.length() <= 24) {
            return normalized;
        }
        return normalized.substring(0, 24) + "...";
    }

    private static String appendPath(String path, String segment) {
        if ("$".equals(path)) {
            return segment;
        }
        return path + "." + segment;
    }

    private static Field[] getAllFields(Class<?> valueType) {
        Field[] declaredFields = valueType.getDeclaredFields();
        Class<?> superclass = valueType.getSuperclass();
        if (superclass == null || Object.class.equals(superclass)) {
            return declaredFields;
        }
        Field[] parentFields = getAllFields(superclass);
        Field[] merged = new Field[declaredFields.length + parentFields.length];
        System.arraycopy(declaredFields, 0, merged, 0, declaredFields.length);
        System.arraycopy(parentFields, 0, merged, declaredFields.length, parentFields.length);
        return merged;
    }
}
