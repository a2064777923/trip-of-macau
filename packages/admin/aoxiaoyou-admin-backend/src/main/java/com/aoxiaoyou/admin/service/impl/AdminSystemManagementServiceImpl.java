package com.aoxiaoyou.admin.service.impl;

import com.aoxiaoyou.admin.common.api.PageResponse;
import com.aoxiaoyou.admin.common.exception.BusinessException;
import com.aoxiaoyou.admin.dto.request.AdminCarryoverSettingsUpsertRequest;
import com.aoxiaoyou.admin.dto.request.AdminIndoorRuntimeSettingsUpsertRequest;
import com.aoxiaoyou.admin.dto.request.AdminMediaPolicySettingsUpsertRequest;
import com.aoxiaoyou.admin.dto.request.AdminRewardUpsertRequest;
import com.aoxiaoyou.admin.dto.response.AdminCarryoverSettingsResponse;
import com.aoxiaoyou.admin.dto.response.AdminIndoorRuntimeSettingsResponse;
import com.aoxiaoyou.admin.dto.response.AdminMapTileResponse;
import com.aoxiaoyou.admin.dto.response.AdminMediaPolicySettingsResponse;
import com.aoxiaoyou.admin.dto.response.AdminOperationLogResponse;
import com.aoxiaoyou.admin.dto.response.AdminRewardResponse;
import com.aoxiaoyou.admin.dto.response.AdminSystemConfigResponse;
import com.aoxiaoyou.admin.entity.MapTileConfig;
import com.aoxiaoyou.admin.entity.Reward;
import com.aoxiaoyou.admin.entity.SysConfig;
import com.aoxiaoyou.admin.entity.SysOperationLog;
import com.aoxiaoyou.admin.mapper.MapTileConfigMapper;
import com.aoxiaoyou.admin.mapper.RewardMapper;
import com.aoxiaoyou.admin.mapper.SysConfigMapper;
import com.aoxiaoyou.admin.mapper.SysOperationLogMapper;
import com.aoxiaoyou.admin.media.MediaUploadPolicyService;
import com.aoxiaoyou.admin.service.AdminSystemManagementService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminSystemManagementServiceImpl implements AdminSystemManagementService {

    private static final String CONFIG_TRANSLATION_DEFAULT_LOCALE = "translation.primary_authoring_locale";
    private static final String CONFIG_TRANSLATION_ENGINE_PRIORITY = "translation.engine_priority";
    private static final String CONFIG_MAP_MIN_SCALE = "map.zoom.default-min-scale";
    private static final String CONFIG_MAP_MAX_SCALE = "map.zoom.default-max-scale";
    private static final String CONFIG_INDOOR_MIN_SCALE_METERS = "indoor.zoom.min-scale-meters";
    private static final String CONFIG_INDOOR_MAX_SCALE_METERS = "indoor.zoom.max-scale-meters";
    private static final String CONFIG_INDOOR_VIEWPORT_PX = "indoor.zoom.reference-viewport-px";
    private static final String CONFIG_TILE_SIZE_PX = "indoor.tile.default-size-px";
    private static final TypeReference<List<String>> STRING_LIST_TYPE = new TypeReference<>() {
    };

    private final RewardMapper rewardMapper;
    private final SysOperationLogMapper sysOperationLogMapper;
    private final SysConfigMapper sysConfigMapper;
    private final MapTileConfigMapper mapTileConfigMapper;
    private final MediaUploadPolicyService mediaUploadPolicyService;
    private final ObjectMapper objectMapper;

    @Override
    public PageResponse<AdminRewardResponse> pageRewards(long pageNum, long pageSize, String status) {
        Page<Reward> page = rewardMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<Reward>()
                        .eq(StringUtils.hasText(status), Reward::getStatus, status)
                        .orderByAsc(Reward::getSortOrder)
                        .orderByAsc(Reward::getId));
        Page<AdminRewardResponse> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(page.getRecords().stream().map(this::toRewardResponse).toList());
        return PageResponse.of(result);
    }

    @Override
    public AdminRewardResponse createReward(AdminRewardUpsertRequest.Upsert request) {
        Reward reward = new Reward();
        applyRewardRequest(reward, request);
        rewardMapper.insert(reward);
        return toRewardResponse(requireReward(reward.getId()));
    }

    @Override
    public AdminRewardResponse updateReward(Long rewardId, AdminRewardUpsertRequest.Upsert request) {
        Reward reward = requireReward(rewardId);
        applyRewardRequest(reward, request);
        rewardMapper.updateById(reward);
        return toRewardResponse(requireReward(rewardId));
    }

    @Override
    public void deleteReward(Long rewardId) {
        requireReward(rewardId);
        rewardMapper.deleteById(rewardId);
    }

    @Override
    public PageResponse<AdminOperationLogResponse> pageAuditLogs(long pageNum, long pageSize, String module) {
        Page<SysOperationLog> page = sysOperationLogMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<SysOperationLog>()
                        .eq(StringUtils.hasText(module), SysOperationLog::getModule, module)
                        .orderByDesc(SysOperationLog::getCreatedAt));
        Page<AdminOperationLogResponse> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(page.getRecords().stream().map(log -> AdminOperationLogResponse.builder()
                .id(log.getId())
                .operationType(log.getOperation())
                .operationTypeName(log.getModule() + " / " + log.getOperation())
                .operationDesc(log.getRequestParams())
                .adminName(log.getAdminUsername())
                .ipAddress(log.getIp())
                .createTime(log.getCreatedAt())
                .build()).toList());
        return PageResponse.of(result);
    }

    @Override
    public PageResponse<AdminSystemConfigResponse> pageConfigs(long pageNum, long pageSize, String keyword) {
        Page<SysConfig> page = sysConfigMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<SysConfig>()
                        .and(StringUtils.hasText(keyword), q -> q.like(SysConfig::getConfigKey, keyword).or().like(SysConfig::getDescription, keyword))
                        .orderByAsc(SysConfig::getConfigKey));
        Page<AdminSystemConfigResponse> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(page.getRecords().stream().map(item -> AdminSystemConfigResponse.builder()
                .id(item.getId())
                .configKey(item.getConfigKey())
                .configValue(item.getConfigValue())
                .configType(item.getConfigType())
                .description(item.getDescription())
                .updatedAt(item.getUpdatedAt())
                .build()).toList());
        return PageResponse.of(result);
    }

    @Override
    public AdminCarryoverSettingsResponse getCarryoverSettings() {
        AdminMediaPolicySettingsResponse mediaSettings = mediaUploadPolicyService.getSettings();
        return AdminCarryoverSettingsResponse.builder()
                .translationDefaultLocale(readStringConfig(CONFIG_TRANSLATION_DEFAULT_LOCALE, "zh-Hant"))
                .translationEnginePriority(readStringListConfig(CONFIG_TRANSLATION_ENGINE_PRIORITY, List.of("google", "bing")))
                .mediaUploadDefaultPolicyCode(resolveDefaultMediaPolicyCode(mediaSettings))
                .mapZoomDefaultMinScale(readDecimalConfig(CONFIG_MAP_MIN_SCALE, "8"))
                .mapZoomDefaultMaxScale(readDecimalConfig(CONFIG_MAP_MAX_SCALE, "18"))
                .indoorZoomDefaultMinScale(readDecimalConfig(CONFIG_INDOOR_MIN_SCALE_METERS, "20"))
                .indoorZoomDefaultMaxScale(readDecimalConfig(CONFIG_INDOOR_MAX_SCALE_METERS, "0.5"))
                .build();
    }

    @Override
    public AdminCarryoverSettingsResponse updateCarryoverSettings(AdminCarryoverSettingsUpsertRequest request) {
        AdminCarryoverSettingsResponse current = getCarryoverSettings();
        String translationDefaultLocale = StringUtils.hasText(request == null ? null : request.getTranslationDefaultLocale())
                ? request.getTranslationDefaultLocale().trim()
                : current.getTranslationDefaultLocale();
        List<String> translationEnginePriority = normalizeStringList(request == null ? null : request.getTranslationEnginePriority());
        if (translationEnginePriority.isEmpty()) {
            translationEnginePriority = current.getTranslationEnginePriority();
        }
        String mediaUploadDefaultPolicyCode = StringUtils.hasText(request == null ? null : request.getMediaUploadDefaultPolicyCode())
                ? request.getMediaUploadDefaultPolicyCode().trim()
                : current.getMediaUploadDefaultPolicyCode();
        BigDecimal mapZoomDefaultMinScale = normalizePositiveDecimal(
                request == null ? null : request.getMapZoomDefaultMinScale(),
                current.getMapZoomDefaultMinScale());
        BigDecimal mapZoomDefaultMaxScale = normalizePositiveDecimal(
                request == null ? null : request.getMapZoomDefaultMaxScale(),
                current.getMapZoomDefaultMaxScale());
        if (mapZoomDefaultMinScale.compareTo(mapZoomDefaultMaxScale) >= 0) {
            throw new BusinessException(4001, "map zoom minimum scale must be lower than maximum scale");
        }

        BigDecimal indoorZoomDefaultMinScale = normalizePositiveDecimal(
                request == null ? null : request.getIndoorZoomDefaultMinScale(),
                current.getIndoorZoomDefaultMinScale());
        BigDecimal indoorZoomDefaultMaxScale = normalizePositiveDecimal(
                request == null ? null : request.getIndoorZoomDefaultMaxScale(),
                current.getIndoorZoomDefaultMaxScale());
        if (indoorZoomDefaultMinScale.compareTo(indoorZoomDefaultMaxScale) <= 0) {
            throw new BusinessException(4001, "indoor minimum visible scale must be greater than indoor maximum visible scale");
        }

        upsertConfig(CONFIG_TRANSLATION_DEFAULT_LOCALE, translationDefaultLocale, "string",
                "Primary authoring locale for multilingual content");
        upsertConfig(CONFIG_TRANSLATION_ENGINE_PRIORITY, writeJson(translationEnginePriority), "json",
                "Ordered translation engine priority for carryover system defaults");
        upsertConfig(CONFIG_MAP_MIN_SCALE, mapZoomDefaultMinScale.stripTrailingZeros().toPlainString(), "number",
                "Default minimum map zoom scale");
        upsertConfig(CONFIG_MAP_MAX_SCALE, mapZoomDefaultMaxScale.stripTrailingZeros().toPlainString(), "number",
                "Default maximum map zoom scale");
        upsertConfig(CONFIG_INDOOR_MIN_SCALE_METERS, indoorZoomDefaultMinScale.stripTrailingZeros().toPlainString(), "number",
                "Indoor floor default minimum visible scale in meters");
        upsertConfig(CONFIG_INDOOR_MAX_SCALE_METERS, indoorZoomDefaultMaxScale.stripTrailingZeros().toPlainString(), "number",
                "Indoor floor default maximum visible scale in meters");

        if (StringUtils.hasText(mediaUploadDefaultPolicyCode)) {
            AdminMediaPolicySettingsResponse mediaSettings = mediaUploadPolicyService.getSettings();
            mediaUploadPolicyService.updateSettings(copyMediaSettingsWithDefaultPolicy(mediaSettings, mediaUploadDefaultPolicyCode));
        }

        return getCarryoverSettings();
    }

    @Override
    public AdminMediaPolicySettingsResponse getMediaPolicySettings() {
        return mediaUploadPolicyService.getSettings();
    }

    @Override
    public AdminMediaPolicySettingsResponse updateMediaPolicySettings(AdminMediaPolicySettingsUpsertRequest request) {
        return mediaUploadPolicyService.updateSettings(request);
    }

    @Override
    public AdminIndoorRuntimeSettingsResponse getIndoorRuntimeSettings() {
        return AdminIndoorRuntimeSettingsResponse.builder()
                .minScaleMeters(readDecimalConfig(CONFIG_INDOOR_MIN_SCALE_METERS, "20"))
                .maxScaleMeters(readDecimalConfig(CONFIG_INDOOR_MAX_SCALE_METERS, "0.5"))
                .referenceViewportPx(readIntegerConfig(CONFIG_INDOOR_VIEWPORT_PX, 390))
                .defaultTileSizePx(readIntegerConfig(CONFIG_TILE_SIZE_PX, 512))
                .build();
    }

    @Override
    public AdminIndoorRuntimeSettingsResponse updateIndoorRuntimeSettings(AdminIndoorRuntimeSettingsUpsertRequest request) {
        BigDecimal minScaleMeters = normalizePositiveDecimal(request == null ? null : request.getMinScaleMeters(), new BigDecimal("20"));
        BigDecimal maxScaleMeters = normalizePositiveDecimal(request == null ? null : request.getMaxScaleMeters(), new BigDecimal("0.5"));
        if (minScaleMeters.compareTo(maxScaleMeters) <= 0) {
            throw new BusinessException(4001, "minScaleMeters must be greater than maxScaleMeters");
        }

        Integer referenceViewportPx = normalizePositiveInteger(request == null ? null : request.getReferenceViewportPx(), 390);
        Integer defaultTileSizePx = normalizeTileSize(request == null ? null : request.getDefaultTileSizePx(), 512);

        upsertConfig(CONFIG_INDOOR_MIN_SCALE_METERS, minScaleMeters.stripTrailingZeros().toPlainString(), "number",
                "Indoor floor default minimum visible scale in meters");
        upsertConfig(CONFIG_INDOOR_MAX_SCALE_METERS, maxScaleMeters.stripTrailingZeros().toPlainString(), "number",
                "Indoor floor default maximum visible scale in meters");
        upsertConfig(CONFIG_INDOOR_VIEWPORT_PX, String.valueOf(referenceViewportPx), "number",
                "Indoor floor zoom derivation reference viewport width in pixels");
        upsertConfig(CONFIG_TILE_SIZE_PX, String.valueOf(defaultTileSizePx), "number",
                "Indoor floor default tile size in pixels");

        return getIndoorRuntimeSettings();
    }

    @Override
    public PageResponse<AdminMapTileResponse> pageMapTiles(long pageNum, long pageSize) {
        Page<MapTileConfig> page = mapTileConfigMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<MapTileConfig>().orderByDesc(MapTileConfig::getUpdatedAt).orderByDesc(MapTileConfig::getId));
        Page<AdminMapTileResponse> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(page.getRecords().stream().map(item -> AdminMapTileResponse.builder()
                .id(item.getId())
                .mapId(item.getMapId())
                .style(item.getStyle())
                .cdnBase(item.getCdnBase())
                .controlPointsUrl(item.getControlPointsUrl())
                .poisUrl(item.getPoisUrl())
                .zoomMin(item.getZoomMin())
                .zoomMax(item.getZoomMax())
                .defaultZoom(item.getDefaultZoom())
                .centerLat(item.getCenterLat())
                .centerLng(item.getCenterLng())
                .version(item.getVersion())
                .status(item.getStatus())
                .updatedAt(item.getUpdatedAt())
                .build()).toList());
        return PageResponse.of(result);
    }

    private Reward requireReward(Long rewardId) {
        Reward reward = rewardMapper.selectById(rewardId);
        if (reward == null) {
            throw new BusinessException(4045, "Reward not found");
        }
        return reward;
    }

    private void applyRewardRequest(Reward reward, AdminRewardUpsertRequest.Upsert request) {
        reward.setCode(request.getCode());
        reward.setNameZh(request.getNameZh());
        reward.setNameEn(request.getNameEn());
        reward.setNameZht(request.getNameZht());
        reward.setNamePt(request.getNamePt());
        reward.setSubtitleZh(request.getSubtitleZh());
        reward.setSubtitleEn(request.getSubtitleEn());
        reward.setSubtitleZht(request.getSubtitleZht());
        reward.setSubtitlePt(request.getSubtitlePt());
        reward.setDescriptionZh(request.getDescriptionZh());
        reward.setDescriptionEn(request.getDescriptionEn());
        reward.setDescriptionZht(request.getDescriptionZht());
        reward.setDescriptionPt(request.getDescriptionPt());
        reward.setHighlightZh(request.getHighlightZh());
        reward.setHighlightEn(request.getHighlightEn());
        reward.setHighlightZht(request.getHighlightZht());
        reward.setHighlightPt(request.getHighlightPt());
        reward.setStampCost(request.getStampCost() == null ? 0 : Math.max(request.getStampCost(), 0));
        reward.setInventoryTotal(request.getInventoryTotal() == null ? 0 : Math.max(request.getInventoryTotal(), 0));
        reward.setInventoryRedeemed(request.getInventoryRedeemed() == null ? 0 : Math.max(request.getInventoryRedeemed(), 0));
        reward.setCoverAssetId(request.getCoverAssetId());
        reward.setStatus(StringUtils.hasText(request.getStatus()) ? request.getStatus() : "draft");
        reward.setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder());
        reward.setPublishStartAt(parseDateTime(request.getPublishStartAt()));
        reward.setPublishEndAt(parseDateTime(request.getPublishEndAt()));
    }

    private LocalDateTime parseDateTime(String value) {
        return StringUtils.hasText(value) ? LocalDateTime.parse(value) : null;
    }

    private AdminRewardResponse toRewardResponse(Reward item) {
        int total = item.getInventoryTotal() == null ? 0 : item.getInventoryTotal();
        int redeemed = item.getInventoryRedeemed() == null ? 0 : item.getInventoryRedeemed();
        return AdminRewardResponse.builder()
                .id(item.getId())
                .code(item.getCode())
                .nameZh(item.getNameZh())
                .nameEn(item.getNameEn())
                .nameZht(item.getNameZht())
                .namePt(item.getNamePt())
                .subtitleZh(item.getSubtitleZh())
                .subtitleEn(item.getSubtitleEn())
                .subtitleZht(item.getSubtitleZht())
                .subtitlePt(item.getSubtitlePt())
                .descriptionZh(item.getDescriptionZh())
                .descriptionEn(item.getDescriptionEn())
                .descriptionZht(item.getDescriptionZht())
                .descriptionPt(item.getDescriptionPt())
                .highlightZh(item.getHighlightZh())
                .highlightEn(item.getHighlightEn())
                .highlightZht(item.getHighlightZht())
                .highlightPt(item.getHighlightPt())
                .stampCost(item.getStampCost())
                .inventoryTotal(total)
                .inventoryRedeemed(redeemed)
                .inventoryRemaining(Math.max(total - redeemed, 0))
                .coverAssetId(item.getCoverAssetId())
                .status(item.getStatus())
                .sortOrder(item.getSortOrder())
                .publishStartAt(item.getPublishStartAt())
                .publishEndAt(item.getPublishEndAt())
                .createdAt(item.getCreatedAt())
                .build();
    }

    private BigDecimal readDecimalConfig(String key, String fallback) {
        SysConfig config = sysConfigMapper.selectOne(new LambdaQueryWrapper<SysConfig>()
                .eq(SysConfig::getConfigKey, key)
                .last("LIMIT 1"));
        if (config == null || !StringUtils.hasText(config.getConfigValue())) {
            return new BigDecimal(fallback);
        }
        try {
            return new BigDecimal(config.getConfigValue().trim());
        } catch (NumberFormatException ignore) {
            return new BigDecimal(fallback);
        }
    }

    private Integer readIntegerConfig(String key, Integer fallback) {
        SysConfig config = sysConfigMapper.selectOne(new LambdaQueryWrapper<SysConfig>()
                .eq(SysConfig::getConfigKey, key)
                .last("LIMIT 1"));
        if (config == null || !StringUtils.hasText(config.getConfigValue())) {
            return fallback;
        }
        try {
            return Integer.parseInt(config.getConfigValue().trim());
        } catch (NumberFormatException ignore) {
            return fallback;
        }
    }

    private String readStringConfig(String key, String fallback) {
        SysConfig config = sysConfigMapper.selectOne(new LambdaQueryWrapper<SysConfig>()
                .eq(SysConfig::getConfigKey, key)
                .last("LIMIT 1"));
        if (config == null || !StringUtils.hasText(config.getConfigValue())) {
            return fallback;
        }
        return config.getConfigValue().trim();
    }

    private List<String> readStringListConfig(String key, List<String> fallback) {
        String rawValue = readStringConfig(key, "");
        if (!StringUtils.hasText(rawValue)) {
            return fallback;
        }
        try {
            List<String> values = objectMapper.readValue(rawValue, STRING_LIST_TYPE);
            List<String> normalized = normalizeStringList(values);
            return normalized.isEmpty() ? fallback : normalized;
        } catch (Exception ignore) {
            List<String> normalized = normalizeStringList(List.of(rawValue.split(",")));
            return normalized.isEmpty() ? fallback : normalized;
        }
    }

    private BigDecimal normalizePositiveDecimal(BigDecimal value, BigDecimal fallback) {
        BigDecimal resolved = value == null ? fallback : value;
        if (resolved.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(4001, "indoor runtime scale values must be greater than 0");
        }
        return resolved;
    }

    private List<String> normalizeStringList(List<String> values) {
        if (values == null || values.isEmpty()) {
            return List.of();
        }
        return values.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .distinct()
                .toList();
    }

    private String resolveDefaultMediaPolicyCode(AdminMediaPolicySettingsResponse settings) {
        if (settings == null) {
            return "compressed";
        }
        String value = firstNonBlank(
                settings.getImage() == null ? null : settings.getImage().getPreferredPolicyCode(),
                settings.getVideo() == null ? null : settings.getVideo().getPreferredPolicyCode(),
                settings.getAudio() == null ? null : settings.getAudio().getPreferredPolicyCode(),
                settings.getFile() == null ? null : settings.getFile().getPreferredPolicyCode());
        return StringUtils.hasText(value) ? value : "compressed";
    }

    private AdminMediaPolicySettingsUpsertRequest copyMediaSettingsWithDefaultPolicy(
            AdminMediaPolicySettingsResponse settings,
            String preferredPolicyCode
    ) {
        AdminMediaPolicySettingsUpsertRequest request = new AdminMediaPolicySettingsUpsertRequest();
        request.setMaxBatchCount(settings == null ? null : settings.getMaxBatchCount());
        request.setMaxBatchTotalBytes(settings == null ? null : settings.getMaxBatchTotalBytes());
        request.setImage(copyMediaPolicy(settings == null ? null : settings.getImage(), preferredPolicyCode));
        request.setVideo(copyMediaPolicy(settings == null ? null : settings.getVideo(), preferredPolicyCode));
        request.setAudio(copyMediaPolicy(settings == null ? null : settings.getAudio(), preferredPolicyCode));
        request.setFile(copyMediaPolicy(settings == null ? null : settings.getFile(), preferredPolicyCode));
        return request;
    }

    private AdminMediaPolicySettingsUpsertRequest.MediaKindPolicy copyMediaPolicy(
            AdminMediaPolicySettingsResponse.MediaKindPolicy source,
            String preferredPolicyCode
    ) {
        AdminMediaPolicySettingsUpsertRequest.MediaKindPolicy target = new AdminMediaPolicySettingsUpsertRequest.MediaKindPolicy();
        if (source != null) {
            target.setMaxFileSizeBytes(source.getMaxFileSizeBytes());
            target.setQualityPercent(source.getQualityPercent());
            target.setMaxWidthPx(source.getMaxWidthPx());
            target.setMaxHeightPx(source.getMaxHeightPx());
            target.setPreserveMetadata(source.getPreserveMetadata());
            target.setNote(source.getNote());
        }
        target.setPreferredPolicyCode(preferredPolicyCode);
        return target;
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return "";
        }
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                return value.trim();
            }
        }
        return "";
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception ex) {
            throw new BusinessException(5001, "Failed to serialize carryover settings");
        }
    }

    private Integer normalizePositiveInteger(Integer value, Integer fallback) {
        int resolved = value == null ? fallback : value;
        if (resolved <= 0) {
            throw new BusinessException(4001, "indoor runtime integer settings must be greater than 0");
        }
        return resolved;
    }

    private Integer normalizeTileSize(Integer value, Integer fallback) {
        int resolved = normalizePositiveInteger(value, fallback);
        if (resolved < 128) {
            return 128;
        }
        if (resolved > 1024) {
            return 1024;
        }
        return resolved;
    }

    private void upsertConfig(String key, String value, String type, String description) {
        SysConfig config = sysConfigMapper.selectOne(new LambdaQueryWrapper<SysConfig>()
                .eq(SysConfig::getConfigKey, key)
                .last("LIMIT 1"));
        if (config == null) {
            config = new SysConfig();
            config.setConfigKey(key);
            config.setConfigType(type);
            config.setDescription(description);
            config.setConfigValue(value);
            sysConfigMapper.insert(config);
            return;
        }
        config.setConfigValue(value);
        config.setConfigType(type);
        config.setDescription(description);
        sysConfigMapper.updateById(config);
    }
}
