package com.aoxiaoyou.tripofmacau.service.impl;

import com.aoxiaoyou.tripofmacau.dto.response.StoryMediaAssetResponse;
import com.aoxiaoyou.tripofmacau.entity.ContentAsset;
import com.aoxiaoyou.tripofmacau.entity.StoryMaterialPackageItem;
import com.aoxiaoyou.tripofmacau.entity.StoryMaterialPackageItemVersion;
import com.aoxiaoyou.tripofmacau.mapper.StoryMaterialPackageItemMapper;
import com.aoxiaoyou.tripofmacau.mapper.StoryMaterialPackageItemVersionMapper;
import com.aoxiaoyou.tripofmacau.service.PublicRuntimeAssetService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class PublicRuntimeAssetServiceImpl implements PublicRuntimeAssetService {

    public static final String AVAILABILITY_AVAILABLE = "available";
    public static final String AVAILABILITY_FALLBACK = "fallback";
    public static final String AVAILABILITY_UNSUPPORTED = "unsupported";
    public static final String STATUS_PUBLISHED = "published";
    public static final String UNSUPPORTED_REASON = "此媒體暫時未能播放，請稍後再試。";
    private static final String STATUS_APPROVED = "approved";
    private static final String SOURCE_SCOPE_STORY_MATERIAL_PACKAGE = "story_material_package";

    private final StoryMaterialPackageItemMapper storyMaterialPackageItemMapper;
    private final StoryMaterialPackageItemVersionMapper storyMaterialPackageItemVersionMapper;

    @Override
    public StoryMediaAssetResponse toPublicAsset(Long assetId, Map<Long, ContentAsset> publishedAssets) {
        if (assetId == null) {
            return null;
        }
        Map<Long, ContentAsset> safeAssets = publishedAssets == null ? Collections.emptyMap() : publishedAssets;
        ContentAsset asset = safeAssets.get(assetId);
        if (asset == null) {
            return unsupported(assetId, null, safeAssets);
        }
        return toPublicAsset(asset, safeAssets);
    }

    @Override
    public StoryMediaAssetResponse toPublicAsset(ContentAsset asset, Map<Long, ContentAsset> publishedAssets) {
        if (asset == null) {
            return null;
        }
        Map<Long, ContentAsset> safeAssets = publishedAssets == null ? Collections.emptyMap() : publishedAssets;
        StoryMediaAssetResponse.UsageHint usageHint = loadUsageHints(List.of(asset.getId())).get(asset.getId());
        if (isRuntimeAvailable(asset)) {
            return buildResponse(asset, asset, safeAssets, AVAILABILITY_AVAILABLE, false, null, usageHint);
        }

        ContentAsset fallback = resolveDeliveryAsset(asset.getFallbackAssetId(), safeAssets);
        if (fallback == null) {
            fallback = resolveDeliveryAsset(asset.getPosterAssetId(), safeAssets);
        }
        if (fallback != null) {
            return buildResponse(asset, fallback, safeAssets, AVAILABILITY_FALLBACK, true, null, usageHint);
        }
        return unsupported(asset.getId(), asset, safeAssets);
    }

    @Override
    public Map<Long, StoryMediaAssetResponse.UsageHint> loadUsageHints(Collection<Long> assetIds) {
        List<Long> normalizedAssetIds = assetIds == null
                ? Collections.emptyList()
                : assetIds.stream().filter(Objects::nonNull).distinct().toList();
        if (normalizedAssetIds.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<Long, StoryMaterialPackageItem> hintsByAssetId = new LinkedHashMap<>();
        List<StoryMaterialPackageItem> directItems = storyMaterialPackageItemMapper.selectList(
                new LambdaQueryWrapper<StoryMaterialPackageItem>()
                        .in(StoryMaterialPackageItem::getAssetId, normalizedAssetIds)
                        .eq(StoryMaterialPackageItem::getDeleted, 0));
        directItems.stream()
                .filter(this::isPublicItem)
                .sorted(this::compareItems)
                .forEach(item -> hintsByAssetId.putIfAbsent(item.getAssetId(), item));

        List<StoryMaterialPackageItemVersion> versions = storyMaterialPackageItemVersionMapper.selectList(
                new LambdaQueryWrapper<StoryMaterialPackageItemVersion>()
                        .in(StoryMaterialPackageItemVersion::getContentAssetId, normalizedAssetIds)
                        .eq(StoryMaterialPackageItemVersion::getDeleted, 0)
                        .eq(StoryMaterialPackageItemVersion::getPromotionStatus, STATUS_PUBLISHED));
        Set<Long> itemIds = versions.stream()
                .map(StoryMaterialPackageItemVersion::getPackageItemId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Map<Long, StoryMaterialPackageItem> itemsById = itemIds.isEmpty()
                ? Collections.emptyMap()
                : storyMaterialPackageItemMapper.selectList(new LambdaQueryWrapper<StoryMaterialPackageItem>()
                        .in(StoryMaterialPackageItem::getId, itemIds)
                        .eq(StoryMaterialPackageItem::getDeleted, 0))
                .stream()
                .filter(this::isPublicItem)
                .collect(Collectors.toMap(StoryMaterialPackageItem::getId, Function.identity(), (left, right) -> left, LinkedHashMap::new));

        versions.stream()
                .sorted((left, right) -> Integer.compare(safeInt(right.getVersionNo()), safeInt(left.getVersionNo())))
                .forEach(version -> {
                    StoryMaterialPackageItem item = itemsById.get(version.getPackageItemId());
                    if (item != null) {
                        hintsByAssetId.putIfAbsent(version.getContentAssetId(), item);
                    }
                });

        return hintsByAssetId.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> toUsageHint(entry.getValue()),
                        (left, right) -> left,
                        LinkedHashMap::new));
    }

    private StoryMediaAssetResponse buildResponse(
            ContentAsset original,
            ContentAsset delivery,
            Map<Long, ContentAsset> assets,
            String availability,
            boolean fallbackUsed,
            String unavailableReason,
            StoryMediaAssetResponse.UsageHint usageHint) {
        return StoryMediaAssetResponse.builder()
                .id(original.getId())
                .assetKind(firstNonBlank(original.getAssetKind(), delivery.getAssetKind(), "other"))
                .url(firstNonBlank(delivery.getCanonicalUrl(), ""))
                .mimeType(delivery.getMimeType())
                .originalFilename(delivery.getOriginalFilename())
                .widthPx(delivery.getWidthPx())
                .heightPx(delivery.getHeightPx())
                .animationSubtype(delivery.getAnimationSubtype())
                .defaultLoop(delivery.getDefaultLoop())
                .defaultAutoplay(delivery.getDefaultAutoplay())
                .posterAssetId(original.getPosterAssetId())
                .posterUrl(resolveUrl(assets, original.getPosterAssetId()))
                .fallbackAssetId(original.getFallbackAssetId())
                .fallbackUrl(resolveUrl(assets, original.getFallbackAssetId()))
                .availability(availability)
                .unavailableReason(unavailableReason)
                .fallbackUsed(fallbackUsed)
                .runtimeKind(normalizeRuntimeKind(delivery.getAssetKind()))
                .fileSizeBytes(delivery.getFileSizeBytes())
                .durationMs(null)
                .usageHint(usageHint)
                .build();
    }

    private StoryMediaAssetResponse unsupported(Long assetId, ContentAsset original, Map<Long, ContentAsset> assets) {
        StoryMediaAssetResponse.UsageHint usageHint = assetId == null
                ? null
                : loadUsageHints(List.of(assetId)).get(assetId);
        return StoryMediaAssetResponse.builder()
                .id(assetId)
                .assetKind(original == null ? "other" : firstNonBlank(original.getAssetKind(), "other"))
                .url("")
                .mimeType(original == null ? null : original.getMimeType())
                .originalFilename(original == null ? null : original.getOriginalFilename())
                .widthPx(original == null ? null : original.getWidthPx())
                .heightPx(original == null ? null : original.getHeightPx())
                .animationSubtype(original == null ? null : original.getAnimationSubtype())
                .defaultLoop(original == null ? null : original.getDefaultLoop())
                .defaultAutoplay(original == null ? null : original.getDefaultAutoplay())
                .posterAssetId(original == null ? null : original.getPosterAssetId())
                .posterUrl(original == null ? null : resolveUrl(assets, original.getPosterAssetId()))
                .fallbackAssetId(original == null ? null : original.getFallbackAssetId())
                .fallbackUrl(original == null ? null : resolveUrl(assets, original.getFallbackAssetId()))
                .availability(AVAILABILITY_UNSUPPORTED)
                .unavailableReason(UNSUPPORTED_REASON)
                .fallbackUsed(false)
                .runtimeKind(normalizeRuntimeKind(original == null ? null : original.getAssetKind()))
                .fileSizeBytes(original == null ? null : original.getFileSizeBytes())
                .durationMs(null)
                .usageHint(usageHint)
                .build();
    }

    private ContentAsset resolveDeliveryAsset(Long assetId, Map<Long, ContentAsset> assets) {
        if (assetId == null || assets == null) {
            return null;
        }
        ContentAsset asset = assets.get(assetId);
        return isRuntimeAvailable(asset) ? asset : null;
    }

    private boolean isRuntimeAvailable(ContentAsset asset) {
        return asset != null
                && STATUS_PUBLISHED.equalsIgnoreCase(firstNonBlank(asset.getStatus(), ""))
                && StringUtils.hasText(asset.getCanonicalUrl());
    }

    private String resolveUrl(Map<Long, ContentAsset> assets, Long assetId) {
        ContentAsset asset = resolveDeliveryAsset(assetId, assets);
        return asset == null ? null : asset.getCanonicalUrl();
    }

    private StoryMediaAssetResponse.UsageHint toUsageHint(StoryMaterialPackageItem item) {
        return StoryMediaAssetResponse.UsageHint.builder()
                .materialItemKey(item.getItemKey())
                .usageTarget(item.getUsageTarget())
                .chapterCode(item.getChapterCode())
                .targetType(item.getTargetType())
                .targetCode(item.getTargetCode())
                .displayRole(firstNonBlank(item.getUsageTarget(), item.getItemType(), item.getAssetKind(), "media"))
                .sourceScope(SOURCE_SCOPE_STORY_MATERIAL_PACKAGE)
                .build();
    }

    private boolean isPublicItem(StoryMaterialPackageItem item) {
        if (item == null || item.getAssetId() == null && item.getId() == null) {
            return false;
        }
        String status = firstNonBlank(item.getStatus(), "");
        return STATUS_PUBLISHED.equalsIgnoreCase(status) || STATUS_APPROVED.equalsIgnoreCase(status);
    }

    private int compareItems(StoryMaterialPackageItem left, StoryMaterialPackageItem right) {
        int sortCompare = Integer.compare(safeInt(left.getSortOrder()), safeInt(right.getSortOrder()));
        if (sortCompare != 0) {
            return sortCompare;
        }
        return Long.compare(left.getId() == null ? Long.MAX_VALUE : left.getId(), right.getId() == null ? Long.MAX_VALUE : right.getId());
    }

    private String normalizeRuntimeKind(String assetKind) {
        String normalized = firstNonBlank(assetKind, "").trim().toLowerCase();
        if (List.of("image", "icon").contains(normalized)) {
            return "image";
        }
        if (List.of("video", "audio", "lottie", "json", "map_tile").contains(normalized)) {
            return normalized;
        }
        return "other";
    }

    private String firstNonBlank(String... values) {
        return Stream.of(values)
                .filter(StringUtils::hasText)
                .findFirst()
                .orElse("");
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }
}
