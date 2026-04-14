package com.aoxiaoyou.admin.service.impl;

import com.aoxiaoyou.admin.common.enums.LocaleCode;
import com.aoxiaoyou.admin.common.exception.BusinessException;
import com.aoxiaoyou.admin.dto.request.AdminTranslateRequest;
import com.aoxiaoyou.admin.dto.request.AdminTranslationSettingsUpsertRequest;
import com.aoxiaoyou.admin.dto.response.AdminTranslateResponse;
import com.aoxiaoyou.admin.dto.response.AdminTranslationSettingsResponse;
import com.aoxiaoyou.admin.entity.SysConfig;
import com.aoxiaoyou.admin.mapper.SysConfigMapper;
import com.aoxiaoyou.admin.service.AdminTranslationService;
import com.aoxiaoyou.admin.translation.TranslationAttemptResult;
import com.aoxiaoyou.admin.translation.TranslationCommand;
import com.aoxiaoyou.admin.translation.TranslationEngineAdapter;
import com.aoxiaoyou.admin.translation.TranslationProperties;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminTranslationServiceImpl implements AdminTranslationService {

    private static final String PRIMARY_AUTHORING_LOCALE_KEY = "translation.primary_authoring_locale";
    private static final String ENGINE_PRIORITY_KEY = "translation.engine_priority";
    private static final String OVERWRITE_FILLED_LOCALES_KEY = "translation.overwrite_filled_locales";
    private static final TypeReference<List<String>> STRING_LIST_TYPE = new TypeReference<>() {
    };

    private final SysConfigMapper sysConfigMapper;
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;
    private final TranslationEngineAdapter translationEngineAdapter;
    private final TranslationProperties translationProperties;

    @Override
    public AdminTranslationSettingsResponse getSettings() {
        TranslationSettings settings = loadSettings();
        return toSettingsResponse(settings);
    }

    @Override
    public AdminTranslationSettingsResponse updateSettings(AdminTranslationSettingsUpsertRequest request) {
        if (request == null) {
            throw new BusinessException(4001, "Translation settings request is required");
        }
        TranslationSettings current = loadSettings();
        String primaryLocale = normalizeLocaleOrDefault(request.getPrimaryAuthoringLocale(), current.primaryAuthoringLocale);
        List<String> enginePriority = normalizeEngines(request.getEnginePriority());
        if (enginePriority.isEmpty()) {
            enginePriority = current.enginePriority;
        }
        boolean overwriteFilled = request.getOverwriteFilledLocales() != null
                ? request.getOverwriteFilledLocales()
                : current.overwriteFilledLocales;

        upsertConfig(PRIMARY_AUTHORING_LOCALE_KEY, primaryLocale, "string", "Primary authoring locale for multilingual content");
        upsertConfig(ENGINE_PRIORITY_KEY, writeJson(enginePriority), "json", "Ordered translation engine priority for operator-triggered machine translation");
        upsertConfig(OVERWRITE_FILLED_LOCALES_KEY, Boolean.toString(overwriteFilled), "boolean", "Whether one-click translation overwrites already filled locale fields by default");

        return getSettings();
    }

    @Override
    public AdminTranslateResponse translate(AdminTranslateRequest request) {
        if (request == null || !StringUtils.hasText(request.getText())) {
            throw new BusinessException(4001, "Translation text is required");
        }
        String text = request.getText().trim();
        if (text.length() > translationProperties.getMaxTextLength()) {
            throw new BusinessException(4002, "Translation text exceeds maxTextLength");
        }

        TranslationSettings settings = loadSettings();
        LocaleCode sourceLocale = LocaleCode.fromCode(normalizeLocaleOrDefault(request.getSourceLocale(), settings.primaryAuthoringLocale));
        List<LocaleCode> targetLocales = normalizeTargetLocales(request.getTargetLocales(), sourceLocale);
        if (targetLocales.isEmpty()) {
            throw new BusinessException(4003, "At least one target locale is required");
        }

        List<String> enginePriority = normalizeEngines(request.getEnginePriority());
        if (enginePriority.isEmpty()) {
            enginePriority = settings.enginePriority;
        }
        if (enginePriority.isEmpty()) {
            throw new BusinessException(4004, "No translation engine is configured");
        }

        boolean overwriteFilled = request.getOverwriteFilledLocales() != null
                ? request.getOverwriteFilledLocales()
                : settings.overwriteFilledLocales;
        Map<String, String> existingTranslations = request.getExistingTranslations() == null
                ? Map.of()
                : request.getExistingTranslations();

        List<AdminTranslateResponse.LocaleResult> results = new ArrayList<>();
        for (LocaleCode targetLocale : targetLocales) {
            String targetCode = targetLocale.getCode();
            String existingText = existingTranslations.get(targetCode);
            if (!overwriteFilled && StringUtils.hasText(existingText)) {
                results.add(AdminTranslateResponse.LocaleResult.builder()
                        .targetLocale(targetCode)
                        .status("skipped")
                        .translatedText(existingText)
                        .attemptedEngines(List.of())
                        .message("Existing translation kept")
                        .build());
                continue;
            }

            List<String> attemptedEngines = new ArrayList<>();
            TranslationAttemptResult success = null;
            String lastMessage = "";
            for (String engine : enginePriority) {
                attemptedEngines.add(engine);
                TranslationAttemptResult attempt = translationEngineAdapter.translate(TranslationCommand.builder()
                        .engine(engine)
                        .sourceLocale(sourceLocale)
                        .targetLocale(targetLocale)
                        .text(text)
                        .timeoutMs(translationProperties.getRequestTimeoutMs())
                        .build());
                if (attempt.isSuccess() && StringUtils.hasText(attempt.getTranslatedText())) {
                    success = attempt;
                    break;
                }
                lastMessage = attempt.getMessage();
            }

            if (success != null) {
                results.add(AdminTranslateResponse.LocaleResult.builder()
                        .targetLocale(targetCode)
                        .status("success")
                        .translatedText(success.getTranslatedText())
                        .engine(success.getEngine())
                        .attemptedEngines(attemptedEngines)
                        .message(success.getMessage())
                        .build());
            } else {
                results.add(AdminTranslateResponse.LocaleResult.builder()
                        .targetLocale(targetCode)
                        .status("failed")
                        .translatedText("")
                        .attemptedEngines(attemptedEngines)
                        .message(StringUtils.hasText(lastMessage) ? lastMessage : "Translation failed")
                        .build());
            }
        }

        return AdminTranslateResponse.builder()
                .sourceLocale(sourceLocale.getCode())
                .targetLocales(targetLocales.stream().map(LocaleCode::getCode).toList())
                .enginePriority(enginePriority)
                .overwriteFilledLocales(overwriteFilled)
                .results(results)
                .build();
    }

    private TranslationSettings loadSettings() {
        Map<String, SysConfig> configByKey = sysConfigMapper.selectList(new LambdaQueryWrapper<SysConfig>()
                        .in(SysConfig::getConfigKey, List.of(PRIMARY_AUTHORING_LOCALE_KEY, ENGINE_PRIORITY_KEY, OVERWRITE_FILLED_LOCALES_KEY)))
                .stream()
                .collect(LinkedHashMap::new, (map, item) -> map.put(item.getConfigKey(), item), LinkedHashMap::putAll);

        String primaryLocale = normalizeLocaleOrDefault(value(configByKey.get(PRIMARY_AUTHORING_LOCALE_KEY)), "zh-Hant");
        List<String> enginePriority = readEnginePriority(value(configByKey.get(ENGINE_PRIORITY_KEY)));
        if (enginePriority.isEmpty()) {
            enginePriority = translationProperties.resolveDefaultEnginePriority();
        }
        boolean overwriteFilled = Boolean.parseBoolean(value(configByKey.get(OVERWRITE_FILLED_LOCALES_KEY)));
        return new TranslationSettings(primaryLocale, enginePriority, overwriteFilled);
    }

    private AdminTranslationSettingsResponse toSettingsResponse(TranslationSettings settings) {
        return AdminTranslationSettingsResponse.builder()
                .primaryAuthoringLocale(settings.primaryAuthoringLocale)
                .enginePriority(settings.enginePriority)
                .overwriteFilledLocales(settings.overwriteFilledLocales)
                .bridgeEnabled(translationProperties.isBridgeEnabled())
                .requestTimeoutMs(translationProperties.getRequestTimeoutMs())
                .maxTextLength(translationProperties.getMaxTextLength())
                .bridgeScriptPath(translationProperties.getBridgeScriptPath())
                .build();
    }

    private void upsertConfig(String key, String value, String type, String description) {
        jdbcTemplate.update("""
                INSERT INTO sys_config (config_key, config_value, config_type, description, deleted, _openid)
                VALUES (?, ?, ?, ?, 0, '')
                ON DUPLICATE KEY UPDATE
                  config_value = VALUES(config_value),
                  config_type = VALUES(config_type),
                  description = VALUES(description),
                  deleted = 0,
                  _openid = ''
                """, key, value, type, description);
    }

    private String value(SysConfig config) {
        return config == null || config.getConfigValue() == null ? "" : config.getConfigValue().trim();
    }

    private String normalizeLocaleOrDefault(String value, String fallback) {
        String candidate = StringUtils.hasText(value) ? value.trim() : fallback;
        try {
            return LocaleCode.fromCode(candidate).getCode();
        } catch (IllegalArgumentException ex) {
            return LocaleCode.fromCode(fallback).getCode();
        }
    }

    private List<LocaleCode> normalizeTargetLocales(List<String> targetLocales, LocaleCode sourceLocale) {
        if (targetLocales == null || targetLocales.isEmpty()) {
            return List.of();
        }
        LinkedHashSet<LocaleCode> deduped = new LinkedHashSet<>();
        for (String targetLocale : targetLocales) {
            if (!StringUtils.hasText(targetLocale)) {
                continue;
            }
            LocaleCode localeCode = LocaleCode.fromCode(targetLocale.trim());
            if (localeCode != sourceLocale) {
                deduped.add(localeCode);
            }
        }
        return new ArrayList<>(deduped);
    }

    private List<String> normalizeEngines(List<String> engines) {
        if (engines == null || engines.isEmpty()) {
            return List.of();
        }
        return engines.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .map(String::toLowerCase)
                .distinct()
                .toList();
    }

    private List<String> readEnginePriority(String rawValue) {
        if (!StringUtils.hasText(rawValue)) {
            return List.of();
        }
        try {
            List<String> values = objectMapper.readValue(rawValue, STRING_LIST_TYPE);
            return normalizeEngines(values);
        } catch (Exception ignored) {
            return normalizeEngines(List.of(rawValue.split(",")));
        }
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception ex) {
            throw new BusinessException(5001, "Failed to serialize translation settings");
        }
    }

    private record TranslationSettings(
            String primaryAuthoringLocale,
            List<String> enginePriority,
            boolean overwriteFilledLocales
    ) {
    }
}
