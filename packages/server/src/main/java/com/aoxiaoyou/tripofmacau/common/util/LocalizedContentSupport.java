package com.aoxiaoyou.tripofmacau.common.util;

import com.aoxiaoyou.tripofmacau.common.enums.LocaleCode;
import com.aoxiaoyou.tripofmacau.entity.ContentAsset;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class LocalizedContentSupport {

    private static final TypeReference<List<Map<String, Object>>> LIST_OF_MAPS = new TypeReference<>() {
    };

    private final ObjectMapper objectMapper;

    public LocaleCode resolveLocale(String localeHint) {
        if (!StringUtils.hasText(localeHint)) {
            return LocaleCode.ZH_HANS;
        }
        try {
            return LocaleCode.fromCode(localeHint);
        } catch (IllegalArgumentException ignored) {
            return LocaleCode.ZH_HANS;
        }
    }

    public String resolveText(String localeHint, String zhHans, String en, String zhHant) {
        return resolveText(localeHint, zhHans, en, zhHant, null);
    }

    public String resolveText(String localeHint, String zhHans, String en, String zhHant, String pt) {
        LocaleCode localeCode = resolveLocale(localeHint);
        List<String> candidates = new ArrayList<>(4);
        for (LocaleCode fallbackLocale : localeFallbackOrder(localeCode)) {
            switch (fallbackLocale) {
                case ZH_HANS -> candidates.add(zhHans);
                case ZH_HANT -> candidates.add(zhHant);
                case EN -> candidates.add(en);
                case PT -> candidates.add(pt);
            }
        }
        return candidates.stream()
                .filter(StringUtils::hasText)
                .findFirst()
                .orElse("");
    }

    public List<LocaleCode> localeFallbackOrder(String localeHint) {
        return localeFallbackOrder(resolveLocale(localeHint));
    }

    public List<LocaleCode> localeFallbackOrder(LocaleCode requestedLocale) {
        List<LocaleCode> ordered = new ArrayList<>(4);
        ordered.add(requestedLocale);
        switch (requestedLocale) {
            case ZH_HANT -> {
                ordered.add(LocaleCode.ZH_HANS);
                ordered.add(LocaleCode.EN);
                ordered.add(LocaleCode.PT);
            }
            case ZH_HANS -> {
                ordered.add(LocaleCode.ZH_HANT);
                ordered.add(LocaleCode.EN);
                ordered.add(LocaleCode.PT);
            }
            case EN -> {
                ordered.add(LocaleCode.PT);
                ordered.add(LocaleCode.ZH_HANT);
                ordered.add(LocaleCode.ZH_HANS);
            }
            case PT -> {
                ordered.add(LocaleCode.EN);
                ordered.add(LocaleCode.ZH_HANT);
                ordered.add(LocaleCode.ZH_HANS);
            }
        }
        return orderedDistinctLocales(ordered);
    }

    public int localeRank(String localeHint, String candidateLocaleCode) {
        if (!StringUtils.hasText(candidateLocaleCode)) {
            return localeFallbackOrder(localeHint).size();
        }
        LocaleCode candidate;
        try {
            candidate = LocaleCode.fromCode(candidateLocaleCode);
        } catch (IllegalArgumentException ignored) {
            return localeFallbackOrder(localeHint).size() + 1;
        }
        return localeRank(resolveLocale(localeHint), candidate);
    }

    public int localeRank(LocaleCode requestedLocale, LocaleCode candidateLocale) {
        List<LocaleCode> fallbackOrder = localeFallbackOrder(requestedLocale);
        int index = fallbackOrder.indexOf(candidateLocale);
        return index >= 0 ? index : fallbackOrder.size();
    }

    public Object parseJsonValue(String json, String valueText) {
        if (StringUtils.hasText(json)) {
            try {
                return objectMapper.readValue(json, Object.class);
            } catch (Exception ignored) {
                return json;
            }
        }
        return StringUtils.hasText(valueText) ? valueText : null;
    }

    public List<String> parseStringList(String json) {
        if (!StringUtils.hasText(json)) {
            return Collections.emptyList();
        }
        try {
            List<String> items = objectMapper.readValue(json, new TypeReference<List<String>>() {
            });
            return items == null ? Collections.emptyList() : items.stream().filter(StringUtils::hasText).toList();
        } catch (Exception ignored) {
            return Collections.emptyList();
        }
    }

    public List<Map<String, Object>> parseListOfMaps(Object value) {
        if (value == null) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.convertValue(value, LIST_OF_MAPS);
        } catch (IllegalArgumentException ignored) {
            return Collections.emptyList();
        }
    }

    public List<String> splitParagraphs(String content) {
        if (!StringUtils.hasText(content)) {
            return Collections.emptyList();
        }
        List<String> paragraphs = Arrays.stream(content.split("\\r?\\n+"))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .toList();
        return paragraphs.isEmpty() ? List.of(content.trim()) : paragraphs;
    }

    public String resolveAssetUrl(Map<Long, ContentAsset> assets, Long assetId) {
        if (assetId == null) {
            return "";
        }
        ContentAsset asset = assets.get(assetId);
        if (asset == null || !StringUtils.hasText(asset.getCanonicalUrl())) {
            return "";
        }
        return asset.getCanonicalUrl();
    }

    public String firstNonBlank(String... values) {
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                return value;
            }
        }
        return "";
    }

    public List<String> orderedDistinct(List<String> values) {
        Set<String> seen = new LinkedHashSet<>();
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                seen.add(value);
            }
        }
        return new ArrayList<>(seen);
    }

    private List<LocaleCode> orderedDistinctLocales(List<LocaleCode> values) {
        Set<LocaleCode> seen = new LinkedHashSet<>();
        for (LocaleCode value : values) {
            if (value != null) {
                seen.add(value);
            }
        }
        return new ArrayList<>(seen);
    }
}
