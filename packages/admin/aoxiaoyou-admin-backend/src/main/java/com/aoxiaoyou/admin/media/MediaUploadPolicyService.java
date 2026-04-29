package com.aoxiaoyou.admin.media;

import com.aoxiaoyou.admin.common.exception.BusinessException;
import com.aoxiaoyou.admin.dto.request.AdminMediaPolicySettingsUpsertRequest;
import com.aoxiaoyou.admin.dto.response.AdminMediaPolicySettingsResponse;
import com.aoxiaoyou.admin.entity.SysConfig;
import com.aoxiaoyou.admin.entity.SysAdmin;
import com.aoxiaoyou.admin.mapper.SysConfigMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MediaUploadPolicyService {

    private static final String MEDIA_POLICY_KEY = "media.upload.policy";
    private static final TypeReference<AdminMediaPolicySettingsResponse> SETTINGS_TYPE = new TypeReference<>() {
    };

    private final SysConfigMapper sysConfigMapper;
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public AdminMediaPolicySettingsResponse getSettings() {
        SysConfig config = sysConfigMapper.selectOne(new LambdaQueryWrapper<SysConfig>()
                .eq(SysConfig::getConfigKey, MEDIA_POLICY_KEY)
                .last("limit 1"));
        if (config == null || !StringUtils.hasText(config.getConfigValue())) {
            return defaultSettings();
        }
        try {
            AdminMediaPolicySettingsResponse parsed = objectMapper.readValue(config.getConfigValue(), SETTINGS_TYPE);
            return normalizeResponse(parsed);
        } catch (Exception ex) {
            return defaultSettings();
        }
    }

    public AdminMediaPolicySettingsResponse updateSettings(AdminMediaPolicySettingsUpsertRequest request) {
        if (request == null) {
            throw new BusinessException(4001, "Media policy settings request is required");
        }
        AdminMediaPolicySettingsResponse normalized = normalizeResponse(toResponse(request));
        upsertConfig(MEDIA_POLICY_KEY, writeJson(normalized), "json", "Media upload policy defaults and processing limits");
        return normalized;
    }

    public ResolvedMediaUploadPolicy resolvePolicy(String assetKind, MultipartFile file, SysAdmin admin) {
        AdminMediaPolicySettingsResponse settings = getSettings();
        String normalizedAssetKind = normalizeAssetKind(assetKind, file);
        AdminMediaPolicySettingsResponse.MediaKindPolicy kindPolicy = policyForKind(settings, normalizedAssetKind);
        long fileSizeBytes = file == null ? 0L : file.getSize();
        long maxFileSizeBytes = kindPolicy.getMaxFileSizeBytes() == null ? 0L : kindPolicy.getMaxFileSizeBytes();
        if (maxFileSizeBytes > 0 && fileSizeBytes > maxFileSizeBytes) {
            throw new BusinessException(4006, "Uploaded file exceeds the configured size limit");
        }

        boolean allowLossless = admin != null && Boolean.TRUE.equals(admin.getAllowLosslessUpload());
        String requestedPolicyCode = normalizePolicyCode(kindPolicy.getPreferredPolicyCode());
        String effectivePolicyCode = requestedPolicyCode;
        String note = StringUtils.hasText(kindPolicy.getNote()) ? kindPolicy.getNote().trim() : "";
        if ("lossless".equals(requestedPolicyCode) && !allowLossless) {
            effectivePolicyCode = "image".equals(policyFamily(normalizedAssetKind)) ? "image-compressed" : "passthrough";
            note = "Uploader is not allowed to use lossless upload; policy downgraded on the server";
        } else if ("compressed".equals(requestedPolicyCode)) {
            effectivePolicyCode = "image".equals(policyFamily(normalizedAssetKind)) ? "image-compressed" : "passthrough";
        } else if (!"lossless".equals(requestedPolicyCode) && !"passthrough".equals(requestedPolicyCode)) {
            effectivePolicyCode = "image".equals(policyFamily(normalizedAssetKind)) ? "image-compressed" : "passthrough";
        }

        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("assetKind", normalizedAssetKind);
        snapshot.put("policyFamily", policyFamily(normalizedAssetKind));
        snapshot.put("requestedPolicyCode", requestedPolicyCode);
        snapshot.put("effectivePolicyCode", effectivePolicyCode);
        snapshot.put("maxFileSizeBytes", kindPolicy.getMaxFileSizeBytes());
        snapshot.put("qualityPercent", kindPolicy.getQualityPercent());
        snapshot.put("maxWidthPx", kindPolicy.getMaxWidthPx());
        snapshot.put("maxHeightPx", kindPolicy.getMaxHeightPx());
        snapshot.put("preserveMetadata", Boolean.TRUE.equals(kindPolicy.getPreserveMetadata()));
        snapshot.put("uploaderAllowsLossless", allowLossless);

        return ResolvedMediaUploadPolicy.builder()
                .assetKind(normalizedAssetKind)
                .policyFamily(policyFamily(normalizedAssetKind))
                .requestedPolicyCode(requestedPolicyCode)
                .effectivePolicyCode(effectivePolicyCode)
                .maxFileSizeBytes(kindPolicy.getMaxFileSizeBytes())
                .qualityPercent(kindPolicy.getQualityPercent())
                .maxWidthPx(kindPolicy.getMaxWidthPx())
                .maxHeightPx(kindPolicy.getMaxHeightPx())
                .preserveMetadata(kindPolicy.getPreserveMetadata())
                .uploaderAllowsLossless(allowLossless)
                .note(note)
                .snapshot(snapshot)
                .build();
    }

    public void validateBatch(int fileCount, long totalBytes) {
        AdminMediaPolicySettingsResponse settings = getSettings();
        if (settings.getMaxBatchCount() != null && settings.getMaxBatchCount() > 0 && fileCount > settings.getMaxBatchCount()) {
            throw new BusinessException(4007, "Batch upload exceeds the configured file count limit");
        }
        if (settings.getMaxBatchTotalBytes() != null && settings.getMaxBatchTotalBytes() > 0 && totalBytes > settings.getMaxBatchTotalBytes()) {
            throw new BusinessException(4008, "Batch upload exceeds the configured total size limit");
        }
    }

    public String normalizeAssetKind(String assetKind, MultipartFile file) {
        if (StringUtils.hasText(assetKind)) {
            String normalized = assetKind.trim().toLowerCase(Locale.ROOT);
            if ("gif".equals(normalized)) {
                return "image";
            }
            return normalized;
        }
        String contentType = file == null ? "" : file.getContentType();
        if (StringUtils.hasText(contentType)) {
            String normalized = contentType.trim().toLowerCase(Locale.ROOT);
            if (normalized.startsWith("image/")) {
                return "image";
            }
            if (normalized.startsWith("video/")) {
                return "video";
            }
            if (normalized.startsWith("audio/")) {
                return "audio";
            }
            if (normalized.contains("json")) {
                return "json";
            }
        }
        String originalFilename = file == null ? null : file.getOriginalFilename();
        String lowerFilename = originalFilename == null ? "" : originalFilename.trim().toLowerCase(Locale.ROOT);
        if (lowerFilename.endsWith(".json")) {
            return "json";
        }
        return "other";
    }

    private AdminMediaPolicySettingsResponse.MediaKindPolicy policyForKind(AdminMediaPolicySettingsResponse settings, String assetKind) {
        return switch (policyFamily(assetKind)) {
            case "image" -> settings.getImage();
            case "video" -> settings.getVideo();
            case "audio" -> settings.getAudio();
            default -> settings.getFile();
        };
    }

    private String policyFamily(String assetKind) {
        String normalized = assetKind == null ? "" : assetKind.trim().toLowerCase(Locale.ROOT);
        if (List.of("image", "icon", "map_tile").contains(normalized)) {
            return "image";
        }
        if ("video".equals(normalized)) {
            return "video";
        }
        if ("audio".equals(normalized)) {
            return "audio";
        }
        return "file";
    }

    private String normalizePolicyCode(String value) {
        if (!StringUtils.hasText(value)) {
            return "compressed";
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        if ("image-compressed".equals(normalized)) {
            return "compressed";
        }
        return normalized;
    }

    private AdminMediaPolicySettingsResponse defaultSettings() {
        return AdminMediaPolicySettingsResponse.builder()
                .maxBatchCount(50)
                .maxBatchTotalBytes(209_715_200L)
                .image(defaultPolicy(10_485_760L, "compressed", 86, 2560, 2560, Boolean.FALSE, "Image uploads scale down when lossless upload is not allowed"))
                .video(defaultPolicy(157_286_400L, "passthrough", 100, null, null, Boolean.TRUE, "Video uploads keep the original file in this phase"))
                .audio(defaultPolicy(52_428_800L, "passthrough", 100, null, null, Boolean.TRUE, "Audio uploads keep the original file in this phase"))
                .file(defaultPolicy(20_971_520L, "passthrough", 100, null, null, Boolean.TRUE, "Other files keep the original payload"))
                .build();
    }

    private AdminMediaPolicySettingsResponse.MediaKindPolicy defaultPolicy(
            Long maxFileSizeBytes,
            String preferredPolicyCode,
            Integer qualityPercent,
            Integer maxWidthPx,
            Integer maxHeightPx,
            Boolean preserveMetadata,
            String note) {
        return AdminMediaPolicySettingsResponse.MediaKindPolicy.builder()
                .maxFileSizeBytes(maxFileSizeBytes)
                .preferredPolicyCode(preferredPolicyCode)
                .qualityPercent(qualityPercent)
                .maxWidthPx(maxWidthPx)
                .maxHeightPx(maxHeightPx)
                .preserveMetadata(preserveMetadata)
                .note(note)
                .build();
    }

    private AdminMediaPolicySettingsResponse normalizeResponse(AdminMediaPolicySettingsResponse response) {
        AdminMediaPolicySettingsResponse defaults = defaultSettings();
        return AdminMediaPolicySettingsResponse.builder()
                .maxBatchCount(response == null || response.getMaxBatchCount() == null ? defaults.getMaxBatchCount() : response.getMaxBatchCount())
                .maxBatchTotalBytes(response == null || response.getMaxBatchTotalBytes() == null ? defaults.getMaxBatchTotalBytes() : response.getMaxBatchTotalBytes())
                .image(normalizePolicy(response == null ? null : response.getImage(), defaults.getImage()))
                .video(normalizePolicy(response == null ? null : response.getVideo(), defaults.getVideo()))
                .audio(normalizePolicy(response == null ? null : response.getAudio(), defaults.getAudio()))
                .file(normalizePolicy(response == null ? null : response.getFile(), defaults.getFile()))
                .build();
    }

    private AdminMediaPolicySettingsResponse.MediaKindPolicy normalizePolicy(
            AdminMediaPolicySettingsResponse.MediaKindPolicy candidate,
            AdminMediaPolicySettingsResponse.MediaKindPolicy fallback) {
        return AdminMediaPolicySettingsResponse.MediaKindPolicy.builder()
                .maxFileSizeBytes(candidate == null || candidate.getMaxFileSizeBytes() == null ? fallback.getMaxFileSizeBytes() : candidate.getMaxFileSizeBytes())
                .preferredPolicyCode(candidate == null || !StringUtils.hasText(candidate.getPreferredPolicyCode()) ? fallback.getPreferredPolicyCode() : candidate.getPreferredPolicyCode())
                .qualityPercent(candidate == null || candidate.getQualityPercent() == null ? fallback.getQualityPercent() : candidate.getQualityPercent())
                .maxWidthPx(candidate == null || candidate.getMaxWidthPx() == null ? fallback.getMaxWidthPx() : candidate.getMaxWidthPx())
                .maxHeightPx(candidate == null || candidate.getMaxHeightPx() == null ? fallback.getMaxHeightPx() : candidate.getMaxHeightPx())
                .preserveMetadata(candidate == null || candidate.getPreserveMetadata() == null ? fallback.getPreserveMetadata() : candidate.getPreserveMetadata())
                .note(candidate == null || !StringUtils.hasText(candidate.getNote()) ? fallback.getNote() : candidate.getNote())
                .build();
    }

    private AdminMediaPolicySettingsResponse toResponse(AdminMediaPolicySettingsUpsertRequest request) {
        return AdminMediaPolicySettingsResponse.builder()
                .maxBatchCount(request.getMaxBatchCount())
                .maxBatchTotalBytes(request.getMaxBatchTotalBytes())
                .image(toResponsePolicy(request.getImage()))
                .video(toResponsePolicy(request.getVideo()))
                .audio(toResponsePolicy(request.getAudio()))
                .file(toResponsePolicy(request.getFile()))
                .build();
    }

    private AdminMediaPolicySettingsResponse.MediaKindPolicy toResponsePolicy(AdminMediaPolicySettingsUpsertRequest.MediaKindPolicy source) {
        if (source == null) {
            return null;
        }
        return AdminMediaPolicySettingsResponse.MediaKindPolicy.builder()
                .maxFileSizeBytes(source.getMaxFileSizeBytes())
                .preferredPolicyCode(source.getPreferredPolicyCode())
                .qualityPercent(source.getQualityPercent())
                .maxWidthPx(source.getMaxWidthPx())
                .maxHeightPx(source.getMaxHeightPx())
                .preserveMetadata(source.getPreserveMetadata())
                .note(source.getNote())
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

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception ex) {
            throw new BusinessException(5002, "Failed to serialize media policy settings");
        }
    }
}
