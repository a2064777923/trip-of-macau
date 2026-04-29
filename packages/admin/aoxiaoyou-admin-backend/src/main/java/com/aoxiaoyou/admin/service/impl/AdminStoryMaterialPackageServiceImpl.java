package com.aoxiaoyou.admin.service.impl;

import com.aoxiaoyou.admin.common.api.PageResponse;
import com.aoxiaoyou.admin.common.exception.BusinessException;
import com.aoxiaoyou.admin.dto.request.AdminStoryMaterialPackageRequest;
import com.aoxiaoyou.admin.dto.response.AdminStoryMaterialPackageResponse;
import com.aoxiaoyou.admin.entity.ContentAsset;
import com.aoxiaoyou.admin.entity.StoryMaterialPackage;
import com.aoxiaoyou.admin.entity.StoryMaterialPackageItem;
import com.aoxiaoyou.admin.mapper.ContentAssetMapper;
import com.aoxiaoyou.admin.mapper.StoryMaterialPackageItemMapper;
import com.aoxiaoyou.admin.mapper.StoryMaterialPackageMapper;
import com.aoxiaoyou.admin.service.AdminStoryMaterialPackageService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AdminStoryMaterialPackageServiceImpl implements AdminStoryMaterialPackageService {

    private static final String STATUS_DRAFT = "draft";

    private final StoryMaterialPackageMapper packageMapper;
    private final StoryMaterialPackageItemMapper packageItemMapper;
    private final ContentAssetMapper contentAssetMapper;
    private final ObjectMapper objectMapper;

    @Override
    public PageResponse<AdminStoryMaterialPackageResponse.PackageSummary> page(AdminStoryMaterialPackageRequest.PackageQuery query) {
        long pageNum = query != null && query.getPageNum() != null ? query.getPageNum() : 1;
        long pageSize = query != null && query.getPageSize() != null ? query.getPageSize() : 20;
        String keyword = query == null ? null : trimToNull(query.getKeyword());
        String packageStatus = query == null ? null : normalizeCode(query.getPackageStatus());
        Long storylineId = query == null ? null : query.getStorylineId();
        Page<StoryMaterialPackage> page = packageMapper.selectPage(new Page<>(pageNum, pageSize),
                activePackageQuery()
                        .eq(StringUtils.hasText(packageStatus), StoryMaterialPackage::getPackageStatus, packageStatus)
                        .eq(storylineId != null, StoryMaterialPackage::getStorylineId, storylineId)
                        .and(StringUtils.hasText(keyword), q -> q
                                .like(StoryMaterialPackage::getCode, keyword)
                                .or().like(StoryMaterialPackage::getTitleZh, keyword)
                                .or().like(StoryMaterialPackage::getTitleZht, keyword)
                                .or().like(StoryMaterialPackage::getTitleEn, keyword)
                                .or().like(StoryMaterialPackage::getTitlePt, keyword))
                        .orderByDesc(StoryMaterialPackage::getUpdatedAt)
                        .orderByDesc(StoryMaterialPackage::getId));
        Page<AdminStoryMaterialPackageResponse.PackageSummary> responsePage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        responsePage.setRecords(page.getRecords().stream().map(this::toSummary).toList());
        return PageResponse.of(responsePage);
    }

    @Override
    public AdminStoryMaterialPackageResponse.PackageDetail detail(Long packageId) {
        StoryMaterialPackage materialPackage = requirePackage(packageId);
        return toDetail(materialPackage);
    }

    @Override
    @Transactional
    public AdminStoryMaterialPackageResponse.PackageDetail create(AdminStoryMaterialPackageRequest.PackageUpsert request, Long adminUserId, String adminUsername) {
        if (request == null) {
            throw new BusinessException(4002, "request is required");
        }
        requireUniquePackageCode(request.getCode(), null);
        StoryMaterialPackage materialPackage = new StoryMaterialPackage();
        applyPackageRequest(materialPackage, request);
        materialPackage.setCreatedByAdminId(adminUserId);
        materialPackage.setCreatedByAdminName(defaultText(adminUsername));
        materialPackage.setMaterialCount(0);
        materialPackage.setAssetCount(0);
        materialPackage.setStoryObjectCount(0);
        materialPackage.setDeleted(0);
        packageMapper.insert(materialPackage);
        return toDetail(requirePackage(materialPackage.getId()));
    }

    @Override
    @Transactional
    public AdminStoryMaterialPackageResponse.PackageDetail update(Long packageId, AdminStoryMaterialPackageRequest.PackageUpsert request) {
        if (request == null) {
            throw new BusinessException(4002, "request is required");
        }
        StoryMaterialPackage materialPackage = requirePackage(packageId);
        requireUniquePackageCode(request.getCode(), packageId);
        applyPackageRequest(materialPackage, request);
        packageMapper.updateById(materialPackage);
        recomputeCounters(packageId);
        return toDetail(requirePackage(packageId));
    }

    @Override
    @Transactional
    public AdminStoryMaterialPackageResponse.PackageItem addItem(Long packageId, AdminStoryMaterialPackageRequest.ItemUpsert request) {
        requirePackage(packageId);
        requireUniqueItemKey(packageId, request.getItemKey(), null);
        validateAsset(request.getAssetId());
        StoryMaterialPackageItem item = new StoryMaterialPackageItem();
        item.setPackageId(packageId);
        applyItemRequest(item, request);
        item.setDeleted(0);
        packageItemMapper.insert(item);
        recomputeCounters(packageId);
        return toItem(requireItem(packageId, item.getId()));
    }

    @Override
    @Transactional
    public AdminStoryMaterialPackageResponse.PackageItem updateItem(Long packageId, Long itemId, AdminStoryMaterialPackageRequest.ItemUpsert request) {
        requirePackage(packageId);
        StoryMaterialPackageItem item = requireItem(packageId, itemId);
        requireUniqueItemKey(packageId, request.getItemKey(), itemId);
        validateAsset(request.getAssetId());
        applyItemRequest(item, request);
        packageItemMapper.updateById(item);
        recomputeCounters(packageId);
        return toItem(requireItem(packageId, itemId));
    }

    @Override
    @Transactional
    public void deleteItem(Long packageId, Long itemId) {
        requirePackage(packageId);
        StoryMaterialPackageItem item = requireItem(packageId, itemId);
        item.setDeleted(1);
        packageItemMapper.updateById(item);
        recomputeCounters(packageId);
    }

    @Override
    @Transactional
    public void delete(Long packageId) {
        StoryMaterialPackage materialPackage = requirePackage(packageId);
        List<StoryMaterialPackageItem> items = packageItemMapper.selectList(activeItemQuery()
                .eq(StoryMaterialPackageItem::getPackageId, packageId));
        for (StoryMaterialPackageItem item : items) {
            item.setDeleted(1);
            packageItemMapper.updateById(item);
        }
        materialPackage.setDeleted(1);
        packageMapper.updateById(materialPackage);
    }

    private void applyPackageRequest(StoryMaterialPackage materialPackage, AdminStoryMaterialPackageRequest.PackageUpsert request) {
        if (request == null) {
            throw new BusinessException(4002, "request is required");
        }
        materialPackage.setCode(requireText(request.getCode(), "code is required"));
        materialPackage.setStorylineId(request.getStorylineId());
        materialPackage.setTitleZh(defaultText(request.getTitleZh()));
        materialPackage.setTitleZht(defaultText(request.getTitleZht(), request.getTitleZh()));
        materialPackage.setTitleEn(defaultText(request.getTitleEn()));
        materialPackage.setTitlePt(defaultText(request.getTitlePt()));
        materialPackage.setSummaryZh(trimToNull(request.getSummaryZh()));
        materialPackage.setSummaryZht(trimToNull(request.getSummaryZht()));
        materialPackage.setHistoricalBasisZh(trimToNull(request.getHistoricalBasisZh()));
        materialPackage.setHistoricalBasisZht(trimToNull(request.getHistoricalBasisZht()));
        materialPackage.setLiteraryDramatizationZh(trimToNull(request.getLiteraryDramatizationZh()));
        materialPackage.setLiteraryDramatizationZht(trimToNull(request.getLiteraryDramatizationZht()));
        materialPackage.setLocalRoot(defaultText(request.getLocalRoot()));
        materialPackage.setCosPrefix(defaultText(request.getCosPrefix()));
        materialPackage.setManifestPath(defaultText(request.getManifestPath()));
        materialPackage.setManifestJson(normalizeJson(request.getManifestJson(), "manifestJson"));
        materialPackage.setPackageStatus(StringUtils.hasText(request.getPackageStatus()) ? normalizeCode(request.getPackageStatus()) : STATUS_DRAFT);
    }

    private void applyItemRequest(StoryMaterialPackageItem item, AdminStoryMaterialPackageRequest.ItemUpsert request) {
        if (request == null) {
            throw new BusinessException(4002, "request is required");
        }
        item.setItemKey(requireText(request.getItemKey(), "itemKey is required"));
        item.setItemType(requireText(request.getItemType(), "itemType is required"));
        item.setAssetKind(defaultText(request.getAssetKind()));
        item.setTargetType(defaultText(request.getTargetType()));
        item.setTargetId(request.getTargetId());
        item.setTargetCode(defaultText(request.getTargetCode()));
        item.setAssetId(request.getAssetId());
        item.setLocalPath(defaultText(request.getLocalPath()));
        item.setCosObjectKey(defaultText(request.getCosObjectKey()));
        item.setCanonicalUrl(defaultText(request.getCanonicalUrl()));
        item.setUsageTarget(defaultText(request.getUsageTarget()));
        item.setChapterCode(defaultText(request.getChapterCode()));
        item.setProvenanceType(StringUtils.hasText(request.getProvenanceType()) ? normalizeCode(request.getProvenanceType()) : "manual");
        item.setPromptText(trimToNull(request.getPromptText()));
        item.setScriptText(trimToNull(request.getScriptText()));
        item.setHistoricalBasisZh(trimToNull(request.getHistoricalBasisZh()));
        item.setHistoricalBasisZht(trimToNull(request.getHistoricalBasisZht()));
        item.setLiteraryDramatizationZh(trimToNull(request.getLiteraryDramatizationZh()));
        item.setLiteraryDramatizationZht(trimToNull(request.getLiteraryDramatizationZht()));
        item.setFallbackItemKey(defaultText(request.getFallbackItemKey()));
        item.setStatus(StringUtils.hasText(request.getStatus()) ? normalizeCode(request.getStatus()) : STATUS_DRAFT);
        item.setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder());
    }

    private void recomputeCounters(Long packageId) {
        List<StoryMaterialPackageItem> items = packageItemMapper.selectList(activeItemQuery()
                .eq(StoryMaterialPackageItem::getPackageId, packageId));
        StoryMaterialPackage materialPackage = requirePackage(packageId);
        materialPackage.setMaterialCount(items.size());
        materialPackage.setAssetCount((int) items.stream().filter(item -> item.getAssetId() != null).count());
        materialPackage.setStoryObjectCount((int) items.stream().filter(item -> StringUtils.hasText(item.getTargetType())).count());
        packageMapper.updateById(materialPackage);
    }

    private StoryMaterialPackage requirePackage(Long packageId) {
        StoryMaterialPackage materialPackage = packageMapper.selectOne(activePackageQuery()
                .eq(StoryMaterialPackage::getId, packageId));
        if (materialPackage == null) {
            throw new BusinessException(4048, "Story material package not found");
        }
        return materialPackage;
    }

    private StoryMaterialPackageItem requireItem(Long packageId, Long itemId) {
        StoryMaterialPackageItem item = packageItemMapper.selectOne(activeItemQuery()
                .eq(StoryMaterialPackageItem::getPackageId, packageId)
                .eq(StoryMaterialPackageItem::getId, itemId));
        if (item == null) {
            throw new BusinessException(4049, "Story material package item not found");
        }
        return item;
    }

    private void requireUniquePackageCode(String code, Long currentId) {
        String normalizedCode = requireText(code, "code is required");
        Long count = packageMapper.selectCount(activePackageQuery()
                .eq(StoryMaterialPackage::getCode, normalizedCode)
                .ne(currentId != null, StoryMaterialPackage::getId, currentId));
        if (count != null && count > 0) {
            throw new BusinessException(4091, "Story material package code already exists");
        }
    }

    private void requireUniqueItemKey(Long packageId, String itemKey, Long currentItemId) {
        String normalizedKey = requireText(itemKey, "itemKey is required");
        Long count = packageItemMapper.selectCount(activeItemQuery()
                .eq(StoryMaterialPackageItem::getPackageId, packageId)
                .eq(StoryMaterialPackageItem::getItemKey, normalizedKey)
                .ne(currentItemId != null, StoryMaterialPackageItem::getId, currentItemId));
        if (count != null && count > 0) {
            throw new BusinessException(4092, "Story material package item key already exists");
        }
    }

    private void validateAsset(Long assetId) {
        if (assetId == null) {
            return;
        }
        ContentAsset asset = contentAssetMapper.selectById(assetId);
        if (asset == null) {
            throw new BusinessException(4046, "Content asset not found");
        }
    }

    private LambdaQueryWrapper<StoryMaterialPackage> activePackageQuery() {
        return new LambdaQueryWrapper<StoryMaterialPackage>().eq(StoryMaterialPackage::getDeleted, 0);
    }

    private LambdaQueryWrapper<StoryMaterialPackageItem> activeItemQuery() {
        return new LambdaQueryWrapper<StoryMaterialPackageItem>().eq(StoryMaterialPackageItem::getDeleted, 0);
    }

    private AdminStoryMaterialPackageResponse.PackageDetail toDetail(StoryMaterialPackage materialPackage) {
        List<AdminStoryMaterialPackageResponse.PackageItem> items = packageItemMapper.selectList(activeItemQuery()
                        .eq(StoryMaterialPackageItem::getPackageId, materialPackage.getId())
                        .orderByAsc(StoryMaterialPackageItem::getSortOrder)
                        .orderByAsc(StoryMaterialPackageItem::getId))
                .stream()
                .map(this::toItem)
                .toList();
        return AdminStoryMaterialPackageResponse.PackageDetail.builder()
                .id(materialPackage.getId())
                .code(materialPackage.getCode())
                .storylineId(materialPackage.getStorylineId())
                .titleZh(materialPackage.getTitleZh())
                .titleZht(materialPackage.getTitleZht())
                .titleEn(materialPackage.getTitleEn())
                .titlePt(materialPackage.getTitlePt())
                .summaryZh(materialPackage.getSummaryZh())
                .summaryZht(materialPackage.getSummaryZht())
                .historicalBasisZh(materialPackage.getHistoricalBasisZh())
                .historicalBasisZht(materialPackage.getHistoricalBasisZht())
                .literaryDramatizationZh(materialPackage.getLiteraryDramatizationZh())
                .literaryDramatizationZht(materialPackage.getLiteraryDramatizationZht())
                .localRoot(materialPackage.getLocalRoot())
                .cosPrefix(materialPackage.getCosPrefix())
                .manifestPath(materialPackage.getManifestPath())
                .manifestJson(materialPackage.getManifestJson())
                .packageStatus(materialPackage.getPackageStatus())
                .counters(toCounters(materialPackage))
                .createdByAdminId(materialPackage.getCreatedByAdminId())
                .createdByAdminName(materialPackage.getCreatedByAdminName())
                .publishedAt(materialPackage.getPublishedAt())
                .createdAt(materialPackage.getCreatedAt())
                .updatedAt(materialPackage.getUpdatedAt())
                .items(items)
                .build();
    }

    private AdminStoryMaterialPackageResponse.PackageSummary toSummary(StoryMaterialPackage materialPackage) {
        return AdminStoryMaterialPackageResponse.PackageSummary.builder()
                .id(materialPackage.getId())
                .code(materialPackage.getCode())
                .storylineId(materialPackage.getStorylineId())
                .titleZh(materialPackage.getTitleZh())
                .titleZht(materialPackage.getTitleZht())
                .titleEn(materialPackage.getTitleEn())
                .titlePt(materialPackage.getTitlePt())
                .summaryZh(materialPackage.getSummaryZh())
                .summaryZht(materialPackage.getSummaryZht())
                .packageStatus(materialPackage.getPackageStatus())
                .counters(toCounters(materialPackage))
                .localRoot(materialPackage.getLocalRoot())
                .cosPrefix(materialPackage.getCosPrefix())
                .manifestPath(materialPackage.getManifestPath())
                .createdByAdminId(materialPackage.getCreatedByAdminId())
                .createdByAdminName(materialPackage.getCreatedByAdminName())
                .publishedAt(materialPackage.getPublishedAt())
                .createdAt(materialPackage.getCreatedAt())
                .updatedAt(materialPackage.getUpdatedAt())
                .build();
    }

    private AdminStoryMaterialPackageResponse.PackageCounters toCounters(StoryMaterialPackage materialPackage) {
        return AdminStoryMaterialPackageResponse.PackageCounters.builder()
                .materialCount(materialPackage.getMaterialCount() == null ? 0 : materialPackage.getMaterialCount())
                .assetCount(materialPackage.getAssetCount() == null ? 0 : materialPackage.getAssetCount())
                .storyObjectCount(materialPackage.getStoryObjectCount() == null ? 0 : materialPackage.getStoryObjectCount())
                .build();
    }

    private AdminStoryMaterialPackageResponse.PackageItem toItem(StoryMaterialPackageItem item) {
        return AdminStoryMaterialPackageResponse.PackageItem.builder()
                .id(item.getId())
                .packageId(item.getPackageId())
                .itemKey(item.getItemKey())
                .itemType(item.getItemType())
                .assetKind(item.getAssetKind())
                .targetType(item.getTargetType())
                .targetId(item.getTargetId())
                .targetCode(item.getTargetCode())
                .assetId(item.getAssetId())
                .localPath(item.getLocalPath())
                .cosObjectKey(item.getCosObjectKey())
                .canonicalUrl(item.getCanonicalUrl())
                .usageTarget(item.getUsageTarget())
                .chapterCode(item.getChapterCode())
                .provenanceType(item.getProvenanceType())
                .promptText(item.getPromptText())
                .scriptText(item.getScriptText())
                .historicalBasisZh(item.getHistoricalBasisZh())
                .historicalBasisZht(item.getHistoricalBasisZht())
                .literaryDramatizationZh(item.getLiteraryDramatizationZh())
                .literaryDramatizationZht(item.getLiteraryDramatizationZht())
                .fallbackItemKey(item.getFallbackItemKey())
                .status(item.getStatus())
                .sortOrder(item.getSortOrder())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }

    private String normalizeJson(String value, String fieldName) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String trimmed = value.trim();
        try {
            objectMapper.readTree(trimmed);
            return trimmed;
        } catch (Exception ex) {
            throw new BusinessException(4002, fieldName + " must be valid JSON");
        }
    }

    private String requireText(String value, String message) {
        if (!StringUtils.hasText(value)) {
            throw new BusinessException(4002, message);
        }
        return value.trim();
    }

    private String normalizeCode(String value) {
        return StringUtils.hasText(value) ? value.trim().toLowerCase(Locale.ROOT) : "";
    }

    private String defaultText(String value) {
        return StringUtils.hasText(value) ? value.trim() : "";
    }

    private String defaultText(String value, String fallback) {
        String normalized = defaultText(value);
        return StringUtils.hasText(normalized) ? normalized : defaultText(fallback);
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
