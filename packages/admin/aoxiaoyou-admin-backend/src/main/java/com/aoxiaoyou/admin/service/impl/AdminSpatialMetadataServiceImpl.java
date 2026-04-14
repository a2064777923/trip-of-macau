package com.aoxiaoyou.admin.service.impl;

import com.aoxiaoyou.admin.common.spatial.CoordinateNormalizationResult;
import com.aoxiaoyou.admin.common.spatial.CoordinateNormalizationService;
import com.aoxiaoyou.admin.dto.request.AdminCoordinatePreviewRequest;
import com.aoxiaoyou.admin.dto.request.AdminSpatialMetadataSuggestionRequest;
import com.aoxiaoyou.admin.dto.response.AdminCoordinatePreviewResponse;
import com.aoxiaoyou.admin.dto.response.AdminSpatialMetadataSuggestionResponse;
import com.aoxiaoyou.admin.service.AdminSpatialMetadataService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminSpatialMetadataServiceImpl implements AdminSpatialMetadataService {

    private static final Map<String, AdminSpatialMetadataSuggestionResponse> PRESETS = Map.of(
            "macau", suggestion("city", "macau", "MO", 22.1987000, 113.5439000, 13, "Matched built-in Macau preset."),
            "hengqin", suggestion("city", "hengqin", "CN", 22.1150000, 113.5500000, 13, "Matched built-in Hengqin preset."),
            "hong-kong", suggestion("city", "hong-kong", "HK", 22.3193000, 114.1694000, 12, "Matched built-in Hong Kong preset."),
            "ecnu", suggestion("city", "ecnu", "CN", 31.2281200, 121.4062700, 15, "Matched built-in ECNU preset."),
            "macau-peninsula", suggestion("sub_map", "macau-peninsula", "MO", 22.1987000, 113.5439000, 14, "Matched built-in Macau Peninsula preset."),
            "taipa", suggestion("sub_map", "taipa", "MO", 22.1563000, 113.5606000, 14, "Matched built-in Taipa preset."),
            "coloane", suggestion("sub_map", "coloane", "MO", 22.1197000, 113.5695000, 13, "Matched built-in Coloane preset.")
    );

    private final CoordinateNormalizationService coordinateNormalizationService;

    @Override
    public AdminCoordinatePreviewResponse previewCoordinate(AdminCoordinatePreviewRequest request) {
        CoordinateNormalizationResult normalized = coordinateNormalizationService.normalizeToGcj02(
                request.getSourceCoordinateSystem(),
                request.getLatitude(),
                request.getLongitude()
        );
        return AdminCoordinatePreviewResponse.builder()
                .sourceCoordinateSystem(normalized.getSourceCoordinateSystem().getCode())
                .sourceLatitude(normalized.getSourceLatitude())
                .sourceLongitude(normalized.getSourceLongitude())
                .normalizedLatitude(normalized.getNormalizedLatitude())
                .normalizedLongitude(normalized.getNormalizedLongitude())
                .normalizationStatus(normalized.getNormalizationStatus())
                .note(normalized.getNote())
                .build();
    }

    @Override
    public AdminSpatialMetadataSuggestionResponse suggestMetadata(AdminSpatialMetadataSuggestionRequest request) {
        String lookupKey = firstNonBlank(
                normalizeCode(request.getCode()),
                normalizeCode(request.getNameEn()),
                normalizeCode(request.getNameZh()),
                normalizeCode(request.getNameZht())
        );
        AdminSpatialMetadataSuggestionResponse preset = lookupKey == null ? null : PRESETS.get(lookupKey);
        if (preset != null) {
            return preset.toBuilder()
                    .entityType(StringUtils.hasText(request.getEntityType()) ? request.getEntityType() : preset.getEntityType())
                    .build();
        }
        return AdminSpatialMetadataSuggestionResponse.builder()
                .entityType(StringUtils.hasText(request.getEntityType()) ? request.getEntityType() : "city")
                .code(normalizeCode(request.getCode()))
                .countryCode("MO")
                .sourceCoordinateSystem("GCJ02")
                .defaultZoom(13)
                .note("No curated preset matched. Remote AMap suggestion is not configured in this runtime.")
                .amapAssisted(false)
                .build();
    }

    private static AdminSpatialMetadataSuggestionResponse suggestion(
            String entityType,
            String code,
            String countryCode,
            double latitude,
            double longitude,
            int defaultZoom,
            String note
    ) {
        return AdminSpatialMetadataSuggestionResponse.builder()
                .entityType(entityType)
                .code(code)
                .countryCode(countryCode)
                .sourceCoordinateSystem("GCJ02")
                .suggestedCenterLat(BigDecimal.valueOf(latitude))
                .suggestedCenterLng(BigDecimal.valueOf(longitude))
                .defaultZoom(defaultZoom)
                .note(note)
                .amapAssisted(false)
                .build();
    }

    private String normalizeCode(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim().toLowerCase().replace(' ', '-');
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                return value;
            }
        }
        return null;
    }
}
