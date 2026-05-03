package com.aoxiaoyou.admin.service.impl;

import com.aoxiaoyou.admin.common.api.PageResponse;
import com.aoxiaoyou.admin.common.exception.BusinessException;
import com.aoxiaoyou.admin.dto.request.AdminStoryMaterialQaRequest;
import com.aoxiaoyou.admin.dto.response.AdminStoryMaterialProductionResponse;
import com.aoxiaoyou.admin.dto.response.AdminStoryMaterialQaResponse;
import com.aoxiaoyou.admin.entity.ContentAsset;
import com.aoxiaoyou.admin.entity.StoryMaterialPackage;
import com.aoxiaoyou.admin.entity.StoryMaterialPackageItem;
import com.aoxiaoyou.admin.entity.StoryMaterialPackageItemVersion;
import com.aoxiaoyou.admin.mapper.ContentAssetMapper;
import com.aoxiaoyou.admin.mapper.StoryMaterialPackageItemMapper;
import com.aoxiaoyou.admin.mapper.StoryMaterialPackageItemVersionMapper;
import com.aoxiaoyou.admin.mapper.StoryMaterialPackageMapper;
import com.aoxiaoyou.admin.service.AdminStoryMaterialQaService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminStoryMaterialQaServiceImpl implements AdminStoryMaterialQaService {

    private static final String STATUS_UPLOADED = "uploaded";
    private static final String STATUS_APPROVED = "approved";
    private static final String STATUS_PUBLISHED = "published";
    private static final String STATUS_REJECTED = "rejected";
    private static final String STATUS_RETRY_REQUIRED = "retry_required";
    private static final String SOURCE_QA_REPLACE = "qa_replace";

    private static final String HEALTH_USABLE = "usable";
    private static final String HEALTH_PLANNED_SLOT = "planned_slot";
    private static final String HEALTH_MISSING_ASSET = "missing_asset";
    private static final String HEALTH_NO_PUBLIC_URL = "no_public_url";
    private static final String HEALTH_UNPUBLISHED = "unpublished";
    private static final String HEALTH_STALE_VERSION = "stale_version";
    private static final String HEALTH_COS_UNAVAILABLE = "cos_unavailable";
    private static final String HEALTH_WRONG_KIND = "wrong_kind";
    private static final String HEALTH_OVERSIZED = "oversized";
    private static final String HEALTH_PREVIEW_FAILED = "preview_failed";
    private static final String HEALTH_NEEDS_REGENERATION = "needs_regeneration";
    private static final String HEALTH_REJECTED = "rejected";

    private static final String MISSING_CURRENT_ASSET = "MISSING_CURRENT_ASSET";
    private static final String MISSING_PUBLISHED_ASSET = "MISSING_PUBLISHED_ASSET";
    private static final String NO_PUBLIC_URL = "NO_PUBLIC_URL";
    private static final String WRONG_ASSET_KIND = "WRONG_ASSET_KIND";
    private static final String COS_HEAD_FAILED = "COS_HEAD_FAILED";
    private static final String STALE_VERSION_POINTER = "STALE_VERSION_POINTER";
    private static final String OVERSIZED_ASSET = "OVERSIZED_ASSET";
    private static final String EXTERNAL_CAPTION_METADATA = "EXTERNAL_CAPTION_METADATA";
    private static final String PLANNED_DEMAND_SLOT = "PLANNED_DEMAND_SLOT";
    private static final String REJECTED_VERSION_SELECTED = "REJECTED_VERSION_SELECTED";

    private final StoryMaterialPackageMapper packageMapper;
    private final StoryMaterialPackageItemMapper packageItemMapper;
    private final StoryMaterialPackageItemVersionMapper versionMapper;
    private final ContentAssetMapper contentAssetMapper;
    private final ObjectMapper objectMapper;

    @Override
    public AdminStoryMaterialQaResponse.QaOverview overview(
            Long packageId,
            AdminStoryMaterialQaRequest.QaItemQuery query
    ) {
        List<StoryMaterialPackageItem> sourceItems = itemsForPackage(packageId);
        List<AdminStoryMaterialQaResponse.QaItem> items = filteredItems(sourceItems, query, CheckOptions.none());
        Set<Long> selectedItemIds = items.stream().map(AdminStoryMaterialQaResponse.QaItem::getId).collect(Collectors.toSet());
        List<AdminStoryMaterialQaResponse.QaFinding> findings = sourceItems.stream()
                .filter(item -> selectedItemIds.contains(item.getId()))
                .flatMap(item -> findingsForItem(item, versionsForItem(item.getId()), CheckOptions.none()).stream())
                .toList();
        return AdminStoryMaterialQaResponse.QaOverview.builder()
                .packageId(packageId)
                .totalItems(items.size())
                .blockingCount(countSeverity(findings, "blocking"))
                .warningCount(countSeverity(findings, "warning"))
                .infoCount(countSeverity(findings, "info"))
                .healthStateCounters(countListValues(items, AdminStoryMaterialQaResponse.QaItem::getHealthStates))
                .assetKindCounters(countSingleValues(items, AdminStoryMaterialQaResponse.QaItem::getAssetKind))
                .statusCounters(countSingleValues(items, AdminStoryMaterialQaResponse.QaItem::getItemStatus))
                .chapterCodeCounters(countSingleValues(items, AdminStoryMaterialQaResponse.QaItem::getChapterCode))
                .runtimeExposureCounters(countSingleValues(items, AdminStoryMaterialQaResponse.QaItem::getRuntimeExposure))
                .build();
    }

    @Override
    public PageResponse<AdminStoryMaterialQaResponse.QaItem> pageItems(
            Long packageId,
            AdminStoryMaterialQaRequest.QaItemQuery query
    ) {
        List<AdminStoryMaterialQaResponse.QaItem> filtered = filteredItems(packageId, query, CheckOptions.none());
        long pageNum = query != null && query.getPageNum() != null && query.getPageNum() > 0 ? query.getPageNum() : 1;
        long pageSize = query != null && query.getPageSize() != null && query.getPageSize() > 0 ? query.getPageSize() : 20;
        int from = (int) Math.min((pageNum - 1) * pageSize, filtered.size());
        int to = (int) Math.min(from + pageSize, filtered.size());
        return PageResponse.<AdminStoryMaterialQaResponse.QaItem>builder()
                .pageNum(pageNum)
                .pageSize(pageSize)
                .total(filtered.size())
                .totalPages((filtered.size() + pageSize - 1) / pageSize)
                .list(filtered.subList(from, to))
                .build();
    }

    @Override
    public AdminStoryMaterialQaResponse.QaDetail detail(Long packageId, Long itemId) {
        StoryMaterialPackageItem item = requireItem(packageId, itemId);
        List<StoryMaterialPackageItemVersion> versions = versionsForItem(itemId);
        StoryMaterialPackageItemVersion current = currentVersion(item, versions);
        StoryMaterialPackageItemVersion published = publishedVersion(versions);
        ContentAsset currentAsset = assetForVersionOrItem(current, item);
        ContentAsset publishedAsset = published == null ? null : assetForVersionOrItem(published, item);
        List<AdminStoryMaterialQaResponse.QaFinding> findings = findingsForItem(item, versions, CheckOptions.none());
        return AdminStoryMaterialQaResponse.QaDetail.builder()
                .item(toQaItem(item, versions, findings))
                .versions(versions.stream().map(this::toVersionResponse).toList())
                .currentContentAsset(toAssetSummary(currentAsset))
                .publishedContentAsset(toAssetSummary(publishedAsset))
                .findings(findings)
                .availableActions(availableActions(item, current, published, findings))
                .build();
    }

    @Override
    @Transactional
    public AdminStoryMaterialQaResponse.QaActionResult reject(
            Long packageId,
            Long itemId,
            AdminStoryMaterialQaRequest.QaActionRequest request,
            Long adminId,
            String adminName,
            List<String> roles
    ) {
        if (request == null) {
            throw new BusinessException(4002, "request is required");
        }
        StoryMaterialPackageItem item = requireItem(packageId, itemId);
        StoryMaterialPackageItemVersion version = requireVersionForItem(
                itemId,
                request.getVersionId() == null ? item.getCurrentVersionId() : request.getVersionId()
        );
        if (isRuntimePublished(item, version)) {
            requireConfirmedSuperAdmin(request.getConfirmedImpact(), roles, "Rejecting a published material version requires super admin confirmation");
        }
        version.setVersionStatus(STATUS_REJECTED);
        version.setPromotionStatus(STATUS_REJECTED);
        version.setVerifiedByAdminId(adminId);
        version.setVerifiedByAdminName(defaultText(adminName));
        version.setVerifiedAt(LocalDateTime.now());
        version.setProvenanceJson(appendQaNote(version.getProvenanceJson(), "reject", request.getNote(), adminId, adminName));
        versionMapper.updateById(version);

        if (Objects.equals(item.getCurrentVersionId(), version.getId())) {
            StoryMaterialPackageItemVersion fallback = previousUsableVersion(itemId, version.getId());
            if (fallback == null) {
                item.setStatus(STATUS_RETRY_REQUIRED);
                packageItemMapper.updateById(item);
            } else {
                applyVersionToItem(item, fallback, assetForVersionOrItem(fallback, item), defaultText(fallback.getPromotionStatus(), STATUS_UPLOADED));
            }
        }
        StoryMaterialPackageItem refreshed = requireItem(packageId, itemId);
        return AdminStoryMaterialQaResponse.QaActionResult.builder()
                .itemId(itemId)
                .currentVersionId(refreshed.getCurrentVersionId())
                .currentStatus(refreshed.getStatus())
                .targetStatus(STATUS_REJECTED)
                .messageZht("已標記為拒絕，版本歷史與資產記錄已保留。")
                .build();
    }

    @Override
    @Transactional
    public AdminStoryMaterialQaResponse.QaActionResult approve(
            Long packageId,
            Long itemId,
            AdminStoryMaterialQaRequest.QaActionRequest request,
            Long adminId,
            String adminName,
            List<String> roles
    ) {
        AdminStoryMaterialQaRequest.QaActionRequest effective = request == null
                ? new AdminStoryMaterialQaRequest.QaActionRequest()
                : request;
        StoryMaterialPackageItem item = requireItem(packageId, itemId);
        StoryMaterialPackageItemVersion version = requireVersionForItem(
                itemId,
                effective.getVersionId() == null ? item.getCurrentVersionId() : effective.getVersionId()
        );
        String targetStatus = normalizeTargetStatus(defaultText(effective.getTargetStatus(), STATUS_APPROVED));
        if (STATUS_PUBLISHED.equals(targetStatus)) {
            requireConfirmedSuperAdmin(effective.getConfirmedImpact(), roles, "Publishing material through QA requires super admin confirmation");
        }
        version.setVersionStatus(targetStatus);
        version.setPromotionStatus(targetStatus);
        version.setVerifiedByAdminId(adminId);
        version.setVerifiedByAdminName(defaultText(adminName));
        version.setVerifiedAt(LocalDateTime.now());
        version.setProvenanceJson(appendQaNote(version.getProvenanceJson(), "approve", effective.getNote(), adminId, adminName));
        versionMapper.updateById(version);
        applyVersionToItem(item, version, assetForVersionOrItem(version, item), targetStatus);
        return AdminStoryMaterialQaResponse.QaActionResult.builder()
                .itemId(itemId)
                .currentVersionId(version.getId())
                .currentStatus(targetStatus)
                .targetStatus(targetStatus)
                .messageZht(STATUS_PUBLISHED.equals(targetStatus) ? "已發布此素材版本。" : "已批准此素材版本。")
                .build();
    }

    @Override
    @Transactional
    public AdminStoryMaterialQaResponse.QaActionResult replace(
            Long packageId,
            Long itemId,
            AdminStoryMaterialQaRequest.ReplaceRequest request,
            Long adminId,
            String adminName,
            List<String> roles
    ) {
        if (request == null || request.getReplacementAssetId() == null) {
            throw new BusinessException(4002, "replacementAssetId is required");
        }
        StoryMaterialPackageItem item = requireItem(packageId, itemId);
        StoryMaterialPackageItemVersion parent = request.getVersionId() == null || request.getVersionId() <= 0
                ? currentVersion(item, versionsForItem(itemId))
                : requireVersionForItem(itemId, request.getVersionId());
        String targetStatus = normalizeTargetStatus(defaultText(request.getTargetStatus(), STATUS_UPLOADED));
        if (STATUS_PUBLISHED.equals(normalizeOptionalCode(item.getStatus())) || STATUS_PUBLISHED.equals(targetStatus)) {
            requireConfirmedImpact(request.getConfirmedImpact(), "Replacing a runtime-visible material requires impact confirmation");
        }
        if (STATUS_PUBLISHED.equals(targetStatus)) {
            requireConfirmedSuperAdmin(request.getConfirmedImpact(), roles, "Publishing a replacement material requires super admin confirmation");
        }
        ContentAsset asset = contentAssetMapper.selectById(request.getReplacementAssetId());
        if (asset == null) {
            throw new BusinessException(4046, "Content asset not found");
        }
        StoryMaterialPackageItemVersion version = newVersion(item, SOURCE_QA_REPLACE, adminId, adminName);
        version.setContentAssetId(asset.getId());
        version.setParentVersionId(parent == null ? item.getCurrentVersionId() : parent.getId());
        version.setLocalPath(defaultText(asset.getClientRelativePath(), item.getLocalPath()));
        version.setCosObjectKey(defaultText(asset.getObjectKey()));
        version.setCanonicalUrl(defaultText(asset.getCanonicalUrl()));
        version.setAssetKind(defaultText(asset.getAssetKind(), item.getAssetKind()));
        version.setPromptText(item.getPromptText());
        version.setScriptText(item.getScriptText());
        version.setProvenanceJson(appendQaNote(null, "replace", request.getNote(), adminId, adminName));
        version.setVersionStatus(targetStatus);
        version.setPromotionStatus(targetStatus);
        if (Set.of(STATUS_APPROVED, STATUS_PUBLISHED).contains(targetStatus)) {
            version.setVerifiedByAdminId(adminId);
            version.setVerifiedByAdminName(defaultText(adminName));
            version.setVerifiedAt(LocalDateTime.now());
        }
        versionMapper.insert(version);
        applyVersionToItem(item, version, asset, targetStatus);
        return AdminStoryMaterialQaResponse.QaActionResult.builder()
                .itemId(itemId)
                .currentVersionId(version.getId())
                .currentStatus(targetStatus)
                .targetStatus(targetStatus)
                .messageZht("已建立替換版本，原版本與資產仍保留在歷史中。")
                .build();
    }

    @Override
    public AdminStoryMaterialQaResponse.ConsistencyReport runConsistencyCheck(
            Long packageId,
            AdminStoryMaterialQaRequest.ConsistencyCheckRequest request
    ) {
        requirePackage(packageId);
        CheckOptions options = CheckOptions.from(request);
        List<AdminStoryMaterialQaResponse.QaFinding> findings = new ArrayList<>();
        List<StoryMaterialPackageItem> items = itemsForPackage(packageId);
        CosCheckBudget budget = new CosCheckBudget(options.maxCosChecks());
        for (StoryMaterialPackageItem item : items) {
            if (options.runtimeOnly() && !"runtime".equals(runtimeExposure(item))) {
                continue;
            }
            findings.addAll(findingsForItem(item, versionsForItem(item.getId()), options.withBudget(budget)));
        }
        return AdminStoryMaterialQaResponse.ConsistencyReport.builder()
                .packageId(packageId)
                .checkedAt(LocalDateTime.now())
                .blockingCount(countSeverity(findings, "blocking"))
                .warningCount(countSeverity(findings, "warning"))
                .infoCount(countSeverity(findings, "info"))
                .findings(findings)
                .build();
    }

    private List<AdminStoryMaterialQaResponse.QaItem> filteredItems(
            Long packageId,
            AdminStoryMaterialQaRequest.QaItemQuery query,
            CheckOptions options
    ) {
        requirePackage(packageId);
        return filteredItems(itemsForPackage(packageId), query, options);
    }

    private List<AdminStoryMaterialQaResponse.QaItem> filteredItems(
            List<StoryMaterialPackageItem> sourceItems,
            AdminStoryMaterialQaRequest.QaItemQuery query,
            CheckOptions options
    ) {
        String keyword = normalizeKeyword(query == null ? null : query.getKeyword());
        String assetKind = normalizeOptionalCode(query == null ? null : query.getAssetKind());
        String chapterCode = normalizeOptionalCode(query == null ? null : query.getChapterCode());
        String usageTarget = normalizeKeyword(query == null ? null : query.getUsageTarget());
        String itemStatus = normalizeOptionalCode(query == null ? null : query.getItemStatus());
        String healthState = normalizeOptionalCode(query == null ? null : query.getHealthState());
        String runtimeExposure = normalizeOptionalCode(query == null ? null : query.getRuntimeExposure());
        String providerName = normalizeKeyword(query == null ? null : query.getProviderName());
        String modelCode = normalizeKeyword(query == null ? null : query.getModelCode());
        return sourceItems.stream()
                .map(item -> {
                    List<StoryMaterialPackageItemVersion> versions = versionsForItem(item.getId());
                    List<AdminStoryMaterialQaResponse.QaFinding> findings = findingsForItem(item, versions, options);
                    return toQaItem(item, versions, findings);
                })
                .filter(item -> !StringUtils.hasText(keyword) || itemContains(item, keyword))
                .filter(item -> !StringUtils.hasText(assetKind) || assetKind.equals(normalizeOptionalCode(item.getAssetKind())))
                .filter(item -> !StringUtils.hasText(chapterCode) || chapterCode.equals(normalizeOptionalCode(item.getChapterCode())))
                .filter(item -> !StringUtils.hasText(usageTarget) || defaultText(item.getUsageTarget()).toLowerCase(Locale.ROOT).contains(usageTarget))
                .filter(item -> !StringUtils.hasText(itemStatus) || itemStatus.equals(normalizeOptionalCode(item.getItemStatus())))
                .filter(item -> !StringUtils.hasText(healthState) || item.getHealthStates().stream().map(this::normalizeOptionalCode).anyMatch(healthState::equals))
                .filter(item -> !StringUtils.hasText(runtimeExposure) || runtimeExposure.equals(normalizeOptionalCode(item.getRuntimeExposure())))
                .filter(item -> !StringUtils.hasText(providerName) || defaultText(item.getProviderName()).toLowerCase(Locale.ROOT).contains(providerName))
                .filter(item -> !StringUtils.hasText(modelCode) || defaultText(item.getModelCode()).toLowerCase(Locale.ROOT).contains(modelCode))
                .toList();
    }

    private AdminStoryMaterialQaResponse.QaItem toQaItem(
            StoryMaterialPackageItem item,
            List<StoryMaterialPackageItemVersion> versions,
            List<AdminStoryMaterialQaResponse.QaFinding> findings
    ) {
        StoryMaterialPackageItemVersion current = currentVersion(item, versions);
        StoryMaterialPackageItemVersion published = publishedVersion(versions);
        ContentAsset asset = assetForVersionOrItem(current, item);
        List<String> healthStates = healthStates(item, current, published, asset, findings, versions);
        Set<String> usageTargets = new LinkedHashSet<>();
        if (StringUtils.hasText(item.getUsageTarget())) {
            usageTargets.add(item.getUsageTarget());
        }
        if (StringUtils.hasText(item.getTargetType())) {
            usageTargets.add(item.getTargetType());
        }
        return AdminStoryMaterialQaResponse.QaItem.builder()
                .id(item.getId())
                .packageId(item.getPackageId())
                .itemKey(item.getItemKey())
                .itemType(item.getItemType())
                .assetKind(defaultText(item.getAssetKind(), current == null ? null : current.getAssetKind()))
                .targetType(item.getTargetType())
                .targetId(item.getTargetId())
                .targetCode(item.getTargetCode())
                .assetId(item.getAssetId())
                .currentVersionId(current == null ? item.getCurrentVersionId() : current.getId())
                .currentVersionNo(current == null ? item.getCurrentVersionNo() : current.getVersionNo())
                .publishedVersionId(published == null ? null : published.getId())
                .publishedVersionNo(published == null ? null : published.getVersionNo())
                .currentVersion(toVersionResponse(current))
                .publishedVersion(toVersionResponse(published))
                .previewUrl(previewUrl(item, current, asset))
                .canonicalUrl(defaultText(current == null ? null : current.getCanonicalUrl(), item.getCanonicalUrl()))
                .localPath(defaultText(current == null ? null : current.getLocalPath(), item.getLocalPath()))
                .cosObjectKey(defaultText(current == null ? null : current.getCosObjectKey(), item.getCosObjectKey()))
                .healthStates(healthStates)
                .findingsCount(findings.size())
                .runtimeExposure(runtimeExposure(item))
                .usageTarget(item.getUsageTarget())
                .chapterCode(item.getChapterCode())
                .itemStatus(item.getStatus())
                .providerName(current == null ? null : current.getProviderName())
                .modelCode(current == null ? null : current.getModelCode())
                .usageTargets(new ArrayList<>(usageTargets))
                .lastProducedAt(item.getLastProducedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }

    private List<AdminStoryMaterialQaResponse.QaFinding> findingsForItem(
            StoryMaterialPackageItem item,
            List<StoryMaterialPackageItemVersion> versions,
            CheckOptions options
    ) {
        List<AdminStoryMaterialQaResponse.QaFinding> findings = new ArrayList<>();
        StoryMaterialPackageItemVersion current = currentVersion(item, versions);
        StoryMaterialPackageItemVersion published = publishedVersion(versions);
        ContentAsset currentAsset = assetForVersionOrItem(current, item);
        String sourceId = item.getId() == null ? defaultText(item.getItemKey()) : String.valueOf(item.getId());

        if (current == null || current.getContentAssetId() == null) {
            if (isPlannedSlot(item)) {
                findings.add(finding("info", PLANNED_DEMAND_SLOT, "此項仍是待生產素材需求位。", "package_item", sourceId, "content asset", "empty", "安排生產、導入或綁定既有資產。"));
            } else {
                findings.add(finding("blocking", MISSING_CURRENT_ASSET, "目前版本缺少可用資產。", "package_item", sourceId, "content asset", "empty", "替換或重新導入素材。"));
            }
        }
        if (STATUS_PUBLISHED.equals(normalizeOptionalCode(item.getStatus())) && (published == null || published.getContentAssetId() == null)) {
            findings.add(finding("blocking", MISSING_PUBLISHED_ASSET, "已發布項目缺少已發布版本資產。", "package_item", sourceId, "published content asset", "empty", "發布一個可用版本或回滾到既有版本。"));
        }
        if ("runtime".equals(runtimeExposure(item)) && !StringUtils.hasText(previewUrl(item, current, currentAsset))) {
            findings.add(finding("blocking", NO_PUBLIC_URL, "運行時素材缺少公開連結。", "package_item", sourceId, "canonical url", "empty", "補齊 COS 公開連結或替換資產。"));
        }
        if (currentAsset != null && StringUtils.hasText(item.getAssetKind()) && StringUtils.hasText(currentAsset.getAssetKind())
                && !normalizeOptionalCode(item.getAssetKind()).equals(normalizeOptionalCode(currentAsset.getAssetKind()))) {
            findings.add(finding("blocking", WRONG_ASSET_KIND, "素材類型與需求不一致。", "content_asset", String.valueOf(currentAsset.getId()), item.getAssetKind(), currentAsset.getAssetKind(), "替換為正確類型的資產。"));
        }
        if (current != null && StringUtils.hasText(item.getAssetKind()) && StringUtils.hasText(current.getAssetKind())
                && !normalizeOptionalCode(item.getAssetKind()).equals(normalizeOptionalCode(current.getAssetKind()))) {
            findings.add(finding("blocking", WRONG_ASSET_KIND, "版本類型與需求不一致。", "package_item_version", String.valueOf(current.getId()), item.getAssetKind(), current.getAssetKind(), "修正版本或重新替換。"));
        }
        if (current != null && isRejected(current)) {
            findings.add(finding("warning", REJECTED_VERSION_SELECTED, "目前指向的版本已被拒絕。", "package_item_version", String.valueOf(current.getId()), "usable version", STATUS_REJECTED, "回滾或替換為可用版本。"));
        }
        latestVersion(versions).ifPresent(latest -> {
            if (current != null && latest.getVersionNo() != null && current.getVersionNo() != null
                    && latest.getVersionNo() > current.getVersionNo()) {
                findings.add(finding("warning", STALE_VERSION_POINTER, "目前指向的版本不是最新版本。", "package_item", sourceId, "latest version " + latest.getVersionNo(), "current version " + current.getVersionNo(), "檢查是否需要批准、發布或回滾。"));
            }
        });
        Long size = currentAsset == null ? null : currentAsset.getFileSizeBytes();
        Long threshold = sizeThresholdBytes(defaultText(item.getAssetKind(), currentAsset == null ? null : currentAsset.getAssetKind()));
        if (size != null && threshold != null && size > threshold) {
            findings.add(finding("warning", OVERSIZED_ASSET, "素材檔案偏大，可能影響小程序載入體驗。", "content_asset", String.valueOf(currentAsset.getId()), String.valueOf(threshold), String.valueOf(size), "壓縮或替換為較小版本。"));
        }
        if (current != null && StringUtils.hasText(current.getSubtitleMetadataJson())) {
            findings.add(finding("info", EXTERNAL_CAPTION_METADATA, "此視頻使用外掛字幕元資料，並非燒錄字幕。", "package_item_version", String.valueOf(current.getId()), "caption metadata", "external", "在前端播放時確認字幕載入策略。"));
        }
        if (currentAsset != null && isPreviewFailed(currentAsset)) {
            findings.add(finding("warning", "PREVIEW_FAILED", "資產處理狀態顯示預覽可能失敗。", "content_asset", String.valueOf(currentAsset.getId()), "previewable", currentAsset.getProcessingStatus(), "重新處理或替換資產。"));
        }
        if (Boolean.TRUE.equals(options.includeLocalFileCheck())) {
            addLocalFileFinding(item, findings);
        }
        if (Boolean.TRUE.equals(options.includeCosHead()) && currentAsset != null && options.budget().tryConsume()) {
            if (!isCosHeadAvailable(currentAsset)) {
                findings.add(finding("blocking", COS_HEAD_FAILED, "COS 公開資產無法通過 HEAD 檢查。", "content_asset", String.valueOf(currentAsset.getId()), "2xx/3xx HEAD", defaultText(currentAsset.getCanonicalUrl()), "檢查 COS 權限、物件 key 或重新上傳。"));
            }
        }
        return findings;
    }

    private void addLocalFileFinding(StoryMaterialPackageItem item, List<AdminStoryMaterialQaResponse.QaFinding> findings) {
        if (!StringUtils.hasText(item.getLocalPath())) {
            return;
        }
        StoryMaterialPackage materialPackage = packageMapper.selectById(item.getPackageId());
        if (materialPackage == null || !StringUtils.hasText(materialPackage.getLocalRoot())) {
            return;
        }
        Path root = Path.of(materialPackage.getLocalRoot()).toAbsolutePath().normalize();
        Path target = root.resolve(item.getLocalPath()).normalize();
        if (!target.startsWith(root) || !Files.isRegularFile(target)) {
            findings.add(finding("warning", "LOCAL_FILE_MISSING", "本地素材檔案不存在或超出素材包根目錄。", "package_item", String.valueOf(item.getId()), item.getLocalPath(), target.toString(), "重新同步本地素材或只依賴已上傳 COS 資產。"));
        }
    }

    private List<String> healthStates(
            StoryMaterialPackageItem item,
            StoryMaterialPackageItemVersion current,
            StoryMaterialPackageItemVersion published,
            ContentAsset asset,
            List<AdminStoryMaterialQaResponse.QaFinding> findings,
            List<StoryMaterialPackageItemVersion> versions
    ) {
        Set<String> states = new LinkedHashSet<>();
        Set<String> codes = findings.stream().map(AdminStoryMaterialQaResponse.QaFinding::getFindingCode).collect(Collectors.toSet());
        if (codes.contains(PLANNED_DEMAND_SLOT)) {
            states.add(HEALTH_PLANNED_SLOT);
        }
        if (codes.contains(MISSING_CURRENT_ASSET) || codes.contains(MISSING_PUBLISHED_ASSET)) {
            states.add(HEALTH_MISSING_ASSET);
        }
        if (codes.contains(NO_PUBLIC_URL)) {
            states.add(HEALTH_NO_PUBLIC_URL);
        }
        if ("runtime".equals(runtimeExposure(item)) && current != null && !STATUS_PUBLISHED.equals(normalizeOptionalCode(current.getPromotionStatus()))) {
            states.add(HEALTH_UNPUBLISHED);
        }
        if (codes.contains(STALE_VERSION_POINTER)) {
            states.add(HEALTH_STALE_VERSION);
        }
        if (codes.contains(COS_HEAD_FAILED)) {
            states.add(HEALTH_COS_UNAVAILABLE);
        }
        if (codes.contains(WRONG_ASSET_KIND)) {
            states.add(HEALTH_WRONG_KIND);
        }
        if (codes.contains(OVERSIZED_ASSET)) {
            states.add(HEALTH_OVERSIZED);
        }
        if (codes.contains("PREVIEW_FAILED")) {
            states.add(HEALTH_PREVIEW_FAILED);
        }
        if (Set.of("manual_import_required", STATUS_RETRY_REQUIRED, "needs_regeneration").contains(normalizeOptionalCode(item.getStatus()))) {
            states.add(HEALTH_NEEDS_REGENERATION);
        }
        if (STATUS_REJECTED.equals(normalizeOptionalCode(item.getStatus())) || isRejected(current)) {
            states.add(HEALTH_REJECTED);
        }
        boolean hasBlockingOrWarning = findings.stream().anyMatch(finding -> Set.of("blocking", "warning").contains(normalizeOptionalCode(finding.getSeverity())));
        if (states.isEmpty() || (!hasBlockingOrWarning && current != null && asset != null)) {
            states.add(HEALTH_USABLE);
        }
        return new ArrayList<>(states);
    }

    public boolean isCosHeadAvailable(ContentAsset asset) {
        if (asset == null || !StringUtils.hasText(asset.getCanonicalUrl()) || !isSafeCosUrl(asset.getCanonicalUrl())) {
            return false;
        }
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(asset.getCanonicalUrl()))
                    .method("HEAD", HttpRequest.BodyPublishers.noBody())
                    .timeout(Duration.ofSeconds(3))
                    .build();
            int status = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(2))
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .build()
                    .send(request, HttpResponse.BodyHandlers.discarding())
                    .statusCode();
            return status >= 200 && status < 400;
        } catch (IOException | InterruptedException | IllegalArgumentException ex) {
            if (ex instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            return false;
        }
    }

    private boolean isSafeCosUrl(String value) {
        try {
            URI uri = URI.create(value);
            String host = uri.getHost();
            return "https".equalsIgnoreCase(uri.getScheme())
                    && StringUtils.hasText(host)
                    && isAllowedCosHost(host);
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    public boolean isAllowedCosHost(String host) {
        return host.endsWith(".myqcloud.com") || host.startsWith("cos.") || host.contains(".cos.");
    }

    private StoryMaterialPackage requirePackage(Long packageId) {
        StoryMaterialPackage materialPackage = packageMapper.selectOne(activePackageQuery().eq(StoryMaterialPackage::getId, packageId));
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

    private StoryMaterialPackageItemVersion requireVersionForItem(Long itemId, Long versionId) {
        if (versionId == null) {
            throw new BusinessException(4002, "versionId is required");
        }
        StoryMaterialPackageItemVersion version = versionMapper.selectOne(activeVersionQuery()
                .eq(StoryMaterialPackageItemVersion::getPackageItemId, itemId)
                .eq(StoryMaterialPackageItemVersion::getId, versionId));
        if (version == null) {
            throw new BusinessException(4049, "Story material package item version not found");
        }
        return version;
    }

    private List<StoryMaterialPackageItem> itemsForPackage(Long packageId) {
        return packageItemMapper.selectList(activeItemQuery()
                .eq(StoryMaterialPackageItem::getPackageId, packageId)
                .orderByAsc(StoryMaterialPackageItem::getSortOrder)
                .orderByAsc(StoryMaterialPackageItem::getId));
    }

    private List<StoryMaterialPackageItemVersion> versionsForItem(Long itemId) {
        return versionMapper.selectList(activeVersionQuery()
                .eq(StoryMaterialPackageItemVersion::getPackageItemId, itemId)
                .orderByDesc(StoryMaterialPackageItemVersion::getVersionNo)
                .orderByDesc(StoryMaterialPackageItemVersion::getId))
                .stream()
                .filter(version -> Objects.equals(version.getPackageItemId(), itemId))
                .toList();
    }

    private StoryMaterialPackageItemVersion currentVersion(StoryMaterialPackageItem item, List<StoryMaterialPackageItemVersion> versions) {
        if (item == null || versions == null || versions.isEmpty()) {
            return null;
        }
        if (item.getCurrentVersionId() != null) {
            return versions.stream()
                    .filter(version -> Objects.equals(version.getId(), item.getCurrentVersionId()))
                    .findFirst()
                    .orElse(null);
        }
        return versions.stream().findFirst().orElse(null);
    }

    private StoryMaterialPackageItemVersion publishedVersion(List<StoryMaterialPackageItemVersion> versions) {
        return versions == null ? null : versions.stream()
                .filter(version -> STATUS_PUBLISHED.equals(normalizeOptionalCode(version.getPromotionStatus())))
                .max(Comparator.comparing(StoryMaterialPackageItemVersion::getVersionNo, Comparator.nullsFirst(Comparator.naturalOrder())))
                .orElse(null);
    }

    private java.util.Optional<StoryMaterialPackageItemVersion> latestVersion(List<StoryMaterialPackageItemVersion> versions) {
        return versions == null ? java.util.Optional.empty() : versions.stream()
                .max(Comparator.comparing(StoryMaterialPackageItemVersion::getVersionNo, Comparator.nullsFirst(Comparator.naturalOrder())));
    }

    private StoryMaterialPackageItemVersion previousUsableVersion(Long itemId, Long rejectedVersionId) {
        return versionsForItem(itemId).stream()
                .filter(version -> !Objects.equals(version.getId(), rejectedVersionId))
                .filter(version -> !isRejected(version))
                .filter(version -> version.getContentAssetId() != null)
                .findFirst()
                .orElse(null);
    }

    private ContentAsset assetForVersionOrItem(StoryMaterialPackageItemVersion version, StoryMaterialPackageItem item) {
        Long assetId = version != null && version.getContentAssetId() != null ? version.getContentAssetId() : item.getAssetId();
        return assetId == null ? null : contentAssetMapper.selectById(assetId);
    }

    private void applyVersionToItem(StoryMaterialPackageItem item,
                                    StoryMaterialPackageItemVersion version,
                                    ContentAsset asset,
                                    String itemStatus) {
        item.setAssetId(version.getContentAssetId());
        item.setCurrentVersionId(version.getId());
        item.setCurrentVersionNo(version.getVersionNo());
        item.setLastProducedAt(LocalDateTime.now());
        item.setLocalPath(defaultText(version.getLocalPath()));
        item.setCosObjectKey(defaultText(version.getCosObjectKey()));
        item.setCanonicalUrl(defaultText(version.getCanonicalUrl(), asset == null ? null : asset.getCanonicalUrl()));
        item.setAssetKind(defaultText(version.getAssetKind(), item.getAssetKind()));
        item.setPromptText(defaultText(version.getPromptText(), item.getPromptText()));
        item.setScriptText(defaultText(version.getScriptText(), item.getScriptText()));
        item.setFallbackItemKey(defaultText(version.getPosterFallbackItemKey(), item.getFallbackItemKey()));
        item.setStatus(itemStatus);
        packageItemMapper.updateById(item);
    }

    private StoryMaterialPackageItemVersion newVersion(StoryMaterialPackageItem item, String sourceType, Long adminUserId, String adminUsername) {
        StoryMaterialPackageItemVersion version = new StoryMaterialPackageItemVersion();
        version.setPackageItemId(item.getId());
        version.setVersionNo(nextVersionNo(item.getId()));
        version.setVersionStatus(STATUS_UPLOADED);
        version.setPromotionStatus(STATUS_UPLOADED);
        version.setSourceType(sourceType);
        version.setProviderName("");
        version.setModelCode("");
        version.setParentItemKey("");
        version.setLocalPath("");
        version.setCosObjectKey("");
        version.setCanonicalUrl("");
        version.setAssetKind(defaultText(item.getAssetKind()));
        version.setPosterFallbackItemKey("");
        version.setCurrencyCode("CNY");
        version.setCreatedByAdminId(adminUserId);
        version.setCreatedByAdminName(defaultText(adminUsername));
        version.setDeleted(0);
        return version;
    }

    private Integer nextVersionNo(Long itemId) {
        return versionsForItem(itemId).stream()
                .map(StoryMaterialPackageItemVersion::getVersionNo)
                .filter(Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(0) + 1;
    }

    private AdminStoryMaterialProductionResponse.PackageItemVersionResponse toVersionResponse(StoryMaterialPackageItemVersion version) {
        if (version == null) {
            return null;
        }
        return AdminStoryMaterialProductionResponse.PackageItemVersionResponse.builder()
                .id(version.getId())
                .packageItemId(version.getPackageItemId())
                .versionNo(version.getVersionNo())
                .versionStatus(version.getVersionStatus())
                .promotionStatus(version.getPromotionStatus())
                .contentAssetId(version.getContentAssetId())
                .aiJobId(version.getAiJobId())
                .aiCandidateId(version.getAiCandidateId())
                .sourceType(version.getSourceType())
                .providerName(version.getProviderName())
                .modelCode(version.getModelCode())
                .parentVersionId(version.getParentVersionId())
                .parentItemKey(version.getParentItemKey())
                .localPath(version.getLocalPath())
                .cosObjectKey(version.getCosObjectKey())
                .canonicalUrl(version.getCanonicalUrl())
                .assetKind(version.getAssetKind())
                .posterFallbackItemKey(version.getPosterFallbackItemKey())
                .promptText(version.getPromptText())
                .scriptText(version.getScriptText())
                .provenanceJson(version.getProvenanceJson())
                .cropMetadataJson(version.getCropMetadataJson())
                .subtitleMetadataJson(version.getSubtitleMetadataJson())
                .estimatedCost(version.getEstimatedCost())
                .actualCost(version.getActualCost())
                .currencyCode(version.getCurrencyCode())
                .verifiedByAdminId(version.getVerifiedByAdminId())
                .verifiedByAdminName(version.getVerifiedByAdminName())
                .verifiedAt(version.getVerifiedAt())
                .rollbackOfVersionId(version.getRollbackOfVersionId())
                .createdByAdminId(version.getCreatedByAdminId())
                .createdByAdminName(version.getCreatedByAdminName())
                .createdAt(version.getCreatedAt())
                .updatedAt(version.getUpdatedAt())
                .build();
    }

    private AdminStoryMaterialQaResponse.ContentAssetSummary toAssetSummary(ContentAsset asset) {
        if (asset == null) {
            return null;
        }
        return AdminStoryMaterialQaResponse.ContentAssetSummary.builder()
                .id(asset.getId())
                .assetKind(asset.getAssetKind())
                .canonicalUrl(asset.getCanonicalUrl())
                .objectKey(asset.getObjectKey())
                .bucketName(asset.getBucketName())
                .region(asset.getRegion())
                .mimeType(asset.getMimeType())
                .originalFilename(asset.getOriginalFilename())
                .fileSizeBytes(asset.getFileSizeBytes())
                .widthPx(asset.getWidthPx())
                .heightPx(asset.getHeightPx())
                .processingStatus(asset.getProcessingStatus())
                .status(asset.getStatus())
                .build();
    }

    private AdminStoryMaterialQaResponse.QaFinding finding(
            String severity,
            String findingCode,
            String messageZht,
            String sourceType,
            String sourceId,
            String expectedValue,
            String actualValue,
            String actionHintZht
    ) {
        return AdminStoryMaterialQaResponse.QaFinding.builder()
                .severity(severity)
                .findingCode(findingCode)
                .messageZht(messageZht)
                .sourceType(sourceType)
                .sourceId(sourceId)
                .expectedValue(expectedValue)
                .actualValue(actualValue)
                .actionHintZht(actionHintZht)
                .build();
    }

    private String appendQaNote(String existingJson, String action, String note, Long adminId, String adminName) {
        try {
            ObjectNode root;
            if (StringUtils.hasText(existingJson)) {
                JsonNode parsed = objectMapper.readTree(existingJson);
                root = parsed.isObject() ? (ObjectNode) parsed : objectMapper.createObjectNode().put("previousProvenanceRaw", existingJson);
            } else {
                root = objectMapper.createObjectNode().put("schemaVersion", 1);
            }
            ObjectNode qa = root.putObject("latestQaAction");
            qa.put("action", action);
            qa.put("note", boundedNote(note));
            qa.put("adminId", adminId == null ? 0 : adminId);
            qa.put("adminName", defaultText(adminName));
            qa.put("actedAt", LocalDateTime.now().toString());
            return objectMapper.writeValueAsString(root);
        } catch (Exception ex) {
            return "{\"schemaVersion\":1,\"latestQaAction\":{\"action\":\"" + action + "\",\"note\":\"" + boundedNote(note) + "\"}}";
        }
    }

    private String previewUrl(StoryMaterialPackageItem item, StoryMaterialPackageItemVersion version, ContentAsset asset) {
        return defaultText(
                version == null ? null : version.getCanonicalUrl(),
                defaultText(item.getCanonicalUrl(), asset == null ? null : asset.getCanonicalUrl())
        );
    }

    private List<String> availableActions(
            StoryMaterialPackageItem item,
            StoryMaterialPackageItemVersion current,
            StoryMaterialPackageItemVersion published,
            List<AdminStoryMaterialQaResponse.QaFinding> findings
    ) {
        Set<String> actions = new LinkedHashSet<>();
        if (current != null && !isRejected(current)) {
            actions.add("approve");
            actions.add("reject");
        }
        actions.add("replace");
        if (published != null && !Objects.equals(item.getCurrentVersionId(), published.getId())) {
            actions.add("rollback_to_published");
        }
        if (findings.stream().anyMatch(finding -> Set.of(MISSING_CURRENT_ASSET, NO_PUBLIC_URL, WRONG_ASSET_KIND).contains(finding.getFindingCode()))) {
            actions.add("request_regeneration");
        }
        return new ArrayList<>(actions);
    }

    private boolean itemContains(AdminStoryMaterialQaResponse.QaItem item, String keyword) {
        return List.of(
                        item.getItemKey(),
                        item.getItemType(),
                        item.getTargetType(),
                        item.getTargetCode(),
                        item.getUsageTarget(),
                        item.getChapterCode(),
                        item.getCanonicalUrl(),
                        item.getCosObjectKey(),
                        item.getLocalPath()
                ).stream()
                .filter(StringUtils::hasText)
                .map(value -> value.toLowerCase(Locale.ROOT))
                .anyMatch(value -> value.contains(keyword));
    }

    private boolean isPlannedSlot(StoryMaterialPackageItem item) {
        return Set.of("planned", "draft", "manual_import_required", STATUS_RETRY_REQUIRED).contains(normalizeOptionalCode(item.getStatus()));
    }

    private boolean isRejected(StoryMaterialPackageItemVersion version) {
        return version != null
                && (STATUS_REJECTED.equals(normalizeOptionalCode(version.getVersionStatus()))
                || STATUS_REJECTED.equals(normalizeOptionalCode(version.getPromotionStatus())));
    }

    private boolean isRuntimePublished(StoryMaterialPackageItem item, StoryMaterialPackageItemVersion version) {
        return STATUS_PUBLISHED.equals(normalizeOptionalCode(item.getStatus()))
                || (version != null && STATUS_PUBLISHED.equals(normalizeOptionalCode(version.getPromotionStatus())));
    }

    private String runtimeExposure(StoryMaterialPackageItem item) {
        String status = normalizeOptionalCode(item.getStatus());
        String usageTarget = defaultText(item.getUsageTarget()).toLowerCase(Locale.ROOT);
        String targetType = defaultText(item.getTargetType()).toLowerCase(Locale.ROOT);
        if (STATUS_PUBLISHED.equals(status)
                || usageTarget.startsWith("storyline")
                || usageTarget.startsWith("chapter")
                || usageTarget.startsWith("pickup")
                || usageTarget.startsWith("poi")
                || usageTarget.startsWith("overlay")
                || usageTarget.startsWith("runtime")
                || targetType.startsWith("story")
                || targetType.startsWith("poi")) {
            return "runtime";
        }
        return "admin_only";
    }

    private boolean isPreviewFailed(ContentAsset asset) {
        String status = normalizeOptionalCode(asset.getProcessingStatus());
        return status.contains("fail") || status.contains("error") || status.contains("broken");
    }

    private Long sizeThresholdBytes(String assetKind) {
        return switch (normalizeOptionalCode(assetKind)) {
            case "image", "icon" -> 10L * 1024 * 1024;
            case "audio" -> 30L * 1024 * 1024;
            case "video" -> 300L * 1024 * 1024;
            case "lottie", "json" -> 2L * 1024 * 1024;
            default -> 50L * 1024 * 1024;
        };
    }

    private void requireConfirmedImpact(Boolean confirmedImpact, String message) {
        if (!Boolean.TRUE.equals(confirmedImpact)) {
            throw new BusinessException(4036, message);
        }
    }

    private void requireConfirmedSuperAdmin(Boolean confirmedImpact, List<String> roles, String message) {
        requireConfirmedImpact(confirmedImpact, message);
        if (!isSuperAdmin(roles)) {
            throw new BusinessException(4036, message);
        }
    }

    private boolean isSuperAdmin(List<String> roles) {
        if (roles == null) {
            return false;
        }
        return roles.stream()
                .filter(StringUtils::hasText)
                .map(role -> role.trim().toUpperCase(Locale.ROOT))
                .anyMatch(role -> role.equals("SUPER_ADMIN") || role.equals("ROLE_SUPER_ADMIN"));
    }

    private String normalizeTargetStatus(String targetStatus) {
        String normalized = normalizeOptionalCode(targetStatus);
        if (!Set.of(STATUS_UPLOADED, STATUS_APPROVED, STATUS_PUBLISHED).contains(normalized)) {
            throw new BusinessException(4002, "targetStatus must be uploaded, approved or published");
        }
        return normalized;
    }

    private Integer countSeverity(List<AdminStoryMaterialQaResponse.QaFinding> findings, String severity) {
        return (int) findings.stream().filter(finding -> severity.equals(normalizeOptionalCode(finding.getSeverity()))).count();
    }

    private Map<String, Long> countSingleValues(List<AdminStoryMaterialQaResponse.QaItem> items, Function<AdminStoryMaterialQaResponse.QaItem, String> extractor) {
        return items.stream()
                .map(extractor)
                .filter(StringUtils::hasText)
                .map(this::normalizeOptionalCode)
                .collect(Collectors.groupingBy(Function.identity(), LinkedHashMap::new, Collectors.counting()));
    }

    private Map<String, Long> countListValues(List<AdminStoryMaterialQaResponse.QaItem> items, Function<AdminStoryMaterialQaResponse.QaItem, List<String>> extractor) {
        return items.stream()
                .flatMap(item -> extractor.apply(item).stream())
                .filter(StringUtils::hasText)
                .map(this::normalizeOptionalCode)
                .collect(Collectors.groupingBy(Function.identity(), LinkedHashMap::new, Collectors.counting()));
    }

    private LambdaQueryWrapper<StoryMaterialPackage> activePackageQuery() {
        return new LambdaQueryWrapper<StoryMaterialPackage>().eq(StoryMaterialPackage::getDeleted, 0);
    }

    private LambdaQueryWrapper<StoryMaterialPackageItem> activeItemQuery() {
        return new LambdaQueryWrapper<StoryMaterialPackageItem>().eq(StoryMaterialPackageItem::getDeleted, 0);
    }

    private LambdaQueryWrapper<StoryMaterialPackageItemVersion> activeVersionQuery() {
        return new LambdaQueryWrapper<StoryMaterialPackageItemVersion>().eq(StoryMaterialPackageItemVersion::getDeleted, 0);
    }

    private String normalizeKeyword(String value) {
        return StringUtils.hasText(value) ? value.trim() : "";
    }

    private String normalizeOptionalCode(String value) {
        return StringUtils.hasText(value) ? value.trim().toLowerCase(Locale.ROOT) : "";
    }

    private String defaultText(String value) {
        return StringUtils.hasText(value) ? value.trim().toLowerCase(Locale.ROOT) : "";
    }

    private String defaultText(String value, String fallback) {
        String normalized = StringUtils.hasText(value) ? value.trim() : "";
        return StringUtils.hasText(normalized) ? normalized : (StringUtils.hasText(fallback) ? fallback.trim() : "");
    }

    private String boundedNote(String note) {
        if (!StringUtils.hasText(note)) {
            return "";
        }
        String trimmed = note.trim();
        return trimmed.length() <= 1000 ? trimmed : trimmed.substring(0, 1000);
    }

    private record CheckOptions(Boolean includeCosHead,
                                Boolean includeLocalFileCheck,
                                Integer maxCosChecks,
                                Boolean runtimeOnly,
                                CosCheckBudget budget) {
        static CheckOptions none() {
            return new CheckOptions(false, false, 0, false, new CosCheckBudget(0));
        }

        static CheckOptions from(AdminStoryMaterialQaRequest.ConsistencyCheckRequest request) {
            int maxCosChecks = request == null || request.getMaxCosChecks() == null ? 20 : Math.max(0, Math.min(request.getMaxCosChecks(), 100));
            return new CheckOptions(
                    request != null && Boolean.TRUE.equals(request.getIncludeCosHead()),
                    request != null && Boolean.TRUE.equals(request.getIncludeLocalFileCheck()),
                    maxCosChecks,
                    request != null && Boolean.TRUE.equals(request.getRuntimeOnly()),
                    new CosCheckBudget(maxCosChecks)
            );
        }

        CheckOptions withBudget(CosCheckBudget sharedBudget) {
            return new CheckOptions(includeCosHead, includeLocalFileCheck, maxCosChecks, runtimeOnly, sharedBudget);
        }
    }

    private static class CosCheckBudget {
        private int remaining;
        private final Set<Long> checkedAssetIds = new HashSet<>();

        CosCheckBudget(int remaining) {
            this.remaining = Math.max(0, remaining);
        }

        boolean tryConsume() {
            if (remaining <= 0) {
                return false;
            }
            remaining -= 1;
            return true;
        }
    }
}
