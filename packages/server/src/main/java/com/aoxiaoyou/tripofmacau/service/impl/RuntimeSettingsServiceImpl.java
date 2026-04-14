package com.aoxiaoyou.tripofmacau.service.impl;

import com.aoxiaoyou.tripofmacau.common.enums.ContentStatus;
import com.aoxiaoyou.tripofmacau.common.enums.LocaleCode;
import com.aoxiaoyou.tripofmacau.common.util.LocalizedContentSupport;
import com.aoxiaoyou.tripofmacau.dto.response.RuntimeGroupResponse;
import com.aoxiaoyou.tripofmacau.dto.response.RuntimeSettingItemResponse;
import com.aoxiaoyou.tripofmacau.entity.AppRuntimeSetting;
import com.aoxiaoyou.tripofmacau.entity.ContentAsset;
import com.aoxiaoyou.tripofmacau.mapper.AppRuntimeSettingMapper;
import com.aoxiaoyou.tripofmacau.service.CatalogFoundationService;
import com.aoxiaoyou.tripofmacau.service.RuntimeSettingsService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RuntimeSettingsServiceImpl implements RuntimeSettingsService {

    private final AppRuntimeSettingMapper appRuntimeSettingMapper;
    private final CatalogFoundationService catalogFoundationService;
    private final LocalizedContentSupport localizedContentSupport;

    @Override
    public List<AppRuntimeSetting> listPublishedSettingsByGroup(String group) {
        return appRuntimeSettingMapper.selectList(new LambdaQueryWrapper<AppRuntimeSetting>()
                .eq(AppRuntimeSetting::getStatusCode, ContentStatus.PUBLISHED.getCode())
                .eq(AppRuntimeSetting::getSettingGroup, group)
                .orderByAsc(AppRuntimeSetting::getSortOrder)
                .orderByAsc(AppRuntimeSetting::getId));
    }

    @Override
    public RuntimeGroupResponse getRuntimeSettingsByGroup(String group, String localeHint) {
        List<AppRuntimeSetting> records = listPublishedSettingsByGroup(group);
        if (records.isEmpty()) {
            return RuntimeGroupResponse.builder()
                    .group(group)
                    .localeCode(localizedContentSupport.resolveLocale(localeHint).getCode())
                    .settings(Collections.emptyMap())
                    .items(Collections.emptyList())
                    .build();
        }

        Map<String, AppRuntimeSetting> selectedByKey = records.stream()
                .collect(Collectors.groupingBy(AppRuntimeSetting::getSettingKey, LinkedHashMap::new, Collectors.toList()))
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> selectBestLocaleRecord(entry.getValue(), localeHint),
                        (left, right) -> left,
                        LinkedHashMap::new));

        Map<Long, ContentAsset> assets = catalogFoundationService.getPublishedAssetsByIds(selectedByKey.values().stream()
                .map(AppRuntimeSetting::getAssetId)
                .filter(Objects::nonNull)
                .toList());

        Map<String, Object> settings = new LinkedHashMap<>();
        List<RuntimeSettingItemResponse> items = selectedByKey.values().stream()
                .sorted(Comparator.comparing(AppRuntimeSetting::getSortOrder, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(AppRuntimeSetting::getId))
                .map(setting -> {
                    Object value = localizedContentSupport.parseJsonValue(setting.getValueJson(), setting.getValueText());
                    settings.put(setting.getSettingKey(), value);
                    return RuntimeSettingItemResponse.builder()
                            .id(setting.getId())
                            .settingKey(setting.getSettingKey())
                            .title(localizedContentSupport.resolveText(localeHint, setting.getTitleZh(), setting.getTitleEn(), setting.getTitleZht(), setting.getTitlePt()))
                            .description(localizedContentSupport.resolveText(localeHint, setting.getDescriptionZh(), setting.getDescriptionEn(), setting.getDescriptionZht(), setting.getDescriptionPt()))
                            .value(value)
                            .assetUrl(localizedContentSupport.resolveAssetUrl(assets, setting.getAssetId()))
                            .sortOrder(setting.getSortOrder())
                            .build();
                })
                .toList();

        return RuntimeGroupResponse.builder()
                .group(group)
                .localeCode(localizedContentSupport.resolveLocale(localeHint).getCode())
                .settings(settings)
                .items(items)
                .build();
    }

    private AppRuntimeSetting selectBestLocaleRecord(Collection<AppRuntimeSetting> items, String localeHint) {
        LocaleCode requestedLocale = localizedContentSupport.resolveLocale(localeHint);
        return items.stream()
                .min(Comparator.<AppRuntimeSetting>comparingInt(item -> localeRank(item, requestedLocale))
                        .thenComparing(AppRuntimeSetting::getSortOrder, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(AppRuntimeSetting::getId))
                .orElseThrow();
    }

    private int localeRank(AppRuntimeSetting item, LocaleCode requestedLocale) {
        LocaleCode candidateLocale = item.getLocaleCode();
        if (candidateLocale == null) {
            return localizedContentSupport.localeFallbackOrder(requestedLocale).size();
        }
        return localizedContentSupport.localeRank(requestedLocale, candidateLocale);
    }
}
