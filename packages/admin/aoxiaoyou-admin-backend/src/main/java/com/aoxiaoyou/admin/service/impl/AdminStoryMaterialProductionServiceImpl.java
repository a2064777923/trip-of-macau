package com.aoxiaoyou.admin.service.impl;

import com.aoxiaoyou.admin.ai.provider.AiOutboundUrlGuard;
import com.aoxiaoyou.admin.common.exception.BusinessException;
import com.aoxiaoyou.admin.common.material.MaterialProductionPathGuard;
import com.aoxiaoyou.admin.dto.request.AdminStoryMaterialProductionRequest;
import com.aoxiaoyou.admin.dto.response.AdminStoryMaterialProductionResponse;
import com.aoxiaoyou.admin.entity.AiGenerationCandidate;
import com.aoxiaoyou.admin.entity.AiGenerationJob;
import com.aoxiaoyou.admin.entity.ContentAsset;
import com.aoxiaoyou.admin.entity.StoryMaterialPackage;
import com.aoxiaoyou.admin.entity.StoryMaterialPackageItem;
import com.aoxiaoyou.admin.entity.StoryMaterialPackageItemVersion;
import com.aoxiaoyou.admin.mapper.AiGenerationCandidateMapper;
import com.aoxiaoyou.admin.mapper.AiGenerationJobMapper;
import com.aoxiaoyou.admin.mapper.ContentAssetMapper;
import com.aoxiaoyou.admin.mapper.StoryMaterialPackageItemMapper;
import com.aoxiaoyou.admin.mapper.StoryMaterialPackageItemVersionMapper;
import com.aoxiaoyou.admin.mapper.StoryMaterialPackageMapper;
import com.aoxiaoyou.admin.media.CosAssetStorageService;
import com.aoxiaoyou.admin.media.StoredAssetMetadata;
import com.aoxiaoyou.admin.media.StoredAssetPayload;
import com.aoxiaoyou.admin.service.AdminStoryMaterialProductionService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AdminStoryMaterialProductionServiceImpl implements AdminStoryMaterialProductionService {

    private static final String STATUS_UPLOADED = "uploaded";
    private static final String STATUS_APPROVED = "approved";
    private static final String STATUS_PUBLISHED = "published";
    private static final String STATUS_MANUAL_IMPORT_REQUIRED = "manual_import_required";
    private static final String SOURCE_LOCAL_IMPORT = "local_import";
    private static final String SOURCE_AI_CANDIDATE = "ai_candidate";

    private final StoryMaterialPackageMapper packageMapper;
    private final StoryMaterialPackageItemMapper packageItemMapper;
    private final StoryMaterialPackageItemVersionMapper versionMapper;
    private final ContentAssetMapper contentAssetMapper;
    private final AiGenerationCandidateMapper aiGenerationCandidateMapper;
    private final AiGenerationJobMapper aiGenerationJobMapper;
    private final CosAssetStorageService cosAssetStorageService;
    private final MaterialProductionPathGuard pathGuard;
    private final AiOutboundUrlGuard aiOutboundUrlGuard;
    private final ObjectMapper objectMapper;

    @Override
    public AdminStoryMaterialProductionResponse.PreflightResponse preflightPackage(
            Long packageId,
            AdminStoryMaterialProductionRequest.PreflightRequest request,
            Long adminUserId,
            String adminUsername,
            List<String> roles
    ) {
        requirePackage(packageId);
        AdminStoryMaterialProductionRequest.PreflightRequest effective = request == null
                ? new AdminStoryMaterialProductionRequest.PreflightRequest()
                : request;
        List<StoryMaterialPackageItem> items = packageItemMapper.selectList(activeItemQuery()
                .eq(StoryMaterialPackageItem::getPackageId, packageId)
                .in(effective.getItemKeys() != null && !effective.getItemKeys().isEmpty(), StoryMaterialPackageItem::getItemKey, effective.getItemKeys())
                .orderByAsc(StoryMaterialPackageItem::getSortOrder)
                .orderByAsc(StoryMaterialPackageItem::getId));
        Set<String> targetAssetKinds = new LinkedHashSet<>();
        if (effective.getTargetAssetKinds() != null) {
            effective.getTargetAssetKinds().stream().filter(StringUtils::hasText).map(this::normalizeCode).forEach(targetAssetKinds::add);
        }
        if (targetAssetKinds.isEmpty()) {
            items.stream().map(StoryMaterialPackageItem::getAssetKind).filter(StringUtils::hasText).map(this::normalizeCode).forEach(targetAssetKinds::add);
        }
        BigDecimal estimatedTotalCost = effective.getEstimatedTotalCost() != null ? effective.getEstimatedTotalCost() : BigDecimal.ZERO;
        boolean exceedsBatch = effective.getBatchCostCeiling() != null && estimatedTotalCost.compareTo(effective.getBatchCostCeiling()) > 0;
        boolean exceedsDaily = effective.getDailyCostCeiling() != null && estimatedTotalCost.compareTo(effective.getDailyCostCeiling()) > 0;
        boolean requiresSuperAdminConfirmation = exceedsBatch || exceedsDaily;
        if (requiresSuperAdminConfirmation && Boolean.TRUE.equals(effective.getConfirmCostOverride()) && !isSuperAdmin(roles)) {
            throw new BusinessException(4036, "Super admin confirmation is required for this material production cost override");
        }
        List<AdminStoryMaterialProductionResponse.BatchRiskItem> risks = new ArrayList<>();
        if (exceedsBatch) {
            risks.add(AdminStoryMaterialProductionResponse.BatchRiskItem.builder()
                    .riskCode("batch_cost_ceiling_exceeded")
                    .message("Estimated production cost exceeds the batch ceiling")
                    .estimatedCost(estimatedTotalCost)
                    .requiresSuperAdminConfirmation(true)
                    .build());
        }
        if (exceedsDaily) {
            risks.add(AdminStoryMaterialProductionResponse.BatchRiskItem.builder()
                    .riskCode("daily_cost_ceiling_exceeded")
                    .message("Estimated production cost exceeds the daily ceiling")
                    .estimatedCost(estimatedTotalCost)
                    .requiresSuperAdminConfirmation(true)
                    .build());
        }
        return AdminStoryMaterialProductionResponse.PreflightResponse.builder()
                .packageId(packageId)
                .itemCount(items.size())
                .targetAssetKinds(new ArrayList<>(targetAssetKinds))
                .estimatedTotalCost(estimatedTotalCost)
                .dailyCostCeiling(effective.getDailyCostCeiling())
                .batchCostCeiling(effective.getBatchCostCeiling())
                .requiresSuperAdminConfirmation(requiresSuperAdminConfirmation)
                .verificationNote(effective.getVerificationNote())
                .risks(risks)
                .build();
    }

    @Override
    @Transactional
    public AdminStoryMaterialProductionResponse.PackageItemVersionResponse importLocalAsset(
            Long packageId,
            Long itemId,
            AdminStoryMaterialProductionRequest.LocalImportRequest request,
            Long adminUserId,
            String adminUsername,
            List<String> roles
    ) {
        if (request == null) {
            throw new BusinessException(4002, "request is required");
        }
        StoryMaterialPackage materialPackage = requirePackage(packageId);
        StoryMaterialPackageItem item = requireItem(packageId, itemId);
        Path localPath = pathGuard.resolveLocalPath(materialPackage.getLocalRoot(), request.getRelativeLocalPath());
        String objectKey = StringUtils.hasText(request.getForcedCosObjectKey())
                ? pathGuard.validateObjectKey(request.getForcedCosObjectKey(), materialPackage.getCosPrefix())
                : pathGuard.defaultObjectKey(materialPackage.getCosPrefix(), request.getRelativeLocalPath());
        if (!Files.isRegularFile(localPath)) {
            throw new BusinessException(4055, "Local material file does not exist");
        }
        byte[] bytes;
        try {
            bytes = Files.readAllBytes(localPath);
        } catch (IOException ex) {
            throw new BusinessException(4055, "Local material file could not be read");
        }
        String assetKind = StringUtils.hasText(request.getAssetKind()) ? normalizeCode(request.getAssetKind()) : defaultText(item.getAssetKind(), "other");
        String mimeType = probeMimeType(localPath);
        StoredAssetMetadata stored = cosAssetStorageService.storeAsset(StoredAssetPayload.builder()
                .bytes(bytes)
                .originalFilename(localPath.getFileName().toString())
                .contentType(mimeType)
                .assetKind(assetKind)
                .localeCode(request.getLocaleCode())
                .build(), objectKey);

        ContentAsset asset = new ContentAsset();
        asset.setAssetKind(assetKind);
        asset.setBucketName(stored.getBucketName());
        asset.setRegion(stored.getRegion());
        asset.setObjectKey(stored.getObjectKey());
        asset.setCanonicalUrl(stored.getCanonicalUrl());
        asset.setMimeType(stored.getMimeType());
        asset.setLocaleCode(defaultText(stored.getLocaleCode()));
        asset.setOriginalFilename(localPath.getFileName().toString());
        asset.setFileExtension(pathGuard.safeExtension(localPath.getFileName().toString()));
        asset.setUploadSource("material-package");
        asset.setClientRelativePath(request.getRelativeLocalPath());
        asset.setUploadedByAdminId(adminUserId);
        asset.setUploadedByAdminName(defaultText(adminUsername));
        asset.setFileSizeBytes(stored.getFileSizeBytes());
        asset.setWidthPx(stored.getWidthPx());
        asset.setHeightPx(stored.getHeightPx());
        asset.setChecksum(stored.getChecksum());
        asset.setEtag(stored.getEtag());
        asset.setProcessingPolicyCode("material-production");
        asset.setProcessingStatus("stored");
        asset.setProcessingNote(request.getVerificationNote());
        asset.setStatus(STATUS_UPLOADED);
        contentAssetMapper.insert(asset);

        StoryMaterialPackageItemVersion version = newVersion(item, SOURCE_LOCAL_IMPORT, adminUserId, adminUsername);
        version.setContentAssetId(asset.getId());
        version.setProviderName(defaultText(request.getProviderName()));
        version.setModelCode(defaultText(request.getModelCode()));
        version.setParentVersionId(request.getParentVersionId());
        version.setParentItemKey(defaultText(request.getPosterFallbackItemKey()));
        version.setLocalPath(request.getRelativeLocalPath());
        version.setCosObjectKey(stored.getObjectKey());
        version.setCanonicalUrl(stored.getCanonicalUrl());
        version.setAssetKind(assetKind);
        version.setPosterFallbackItemKey(defaultText(request.getPosterFallbackItemKey()));
        version.setPromptText(trimToNull(request.getPromptText()));
        version.setScriptText(trimToNull(request.getScriptText()));
        version.setProvenanceJson(provenanceJson("local_import", request.getVerificationNote(), request.getSubtitlePath()));
        version.setCropMetadataJson(toJson(request.getCropRect(), "cropRect"));
        version.setSubtitleMetadataJson(normalizeJson(request.getSubtitleMetadataJson(), "subtitleMetadataJson"));
        version.setEstimatedCost(request.getEstimatedCost());
        version.setActualCost(request.getActualCost());
        version.setCurrencyCode(defaultText(request.getCurrencyCode(), "CNY"));
        insertVersionAndUpdateItem(materialPackage, item, version, asset, STATUS_UPLOADED);
        return toVersionResponse(version);
    }

    @Override
    @Transactional
    public AdminStoryMaterialProductionResponse.PackageItemVersionResponse bindFinalizedCandidate(
            Long packageId,
            Long itemId,
            AdminStoryMaterialProductionRequest.AiCandidateBindRequest request,
            Long adminUserId,
            String adminUsername,
            List<String> roles
    ) {
        if (request == null || request.getAiCandidateId() == null) {
            throw new BusinessException(4002, "aiCandidateId is required");
        }
        StoryMaterialPackage materialPackage = requirePackage(packageId);
        StoryMaterialPackageItem item = requireItem(packageId, itemId);
        AiGenerationCandidate candidate = aiGenerationCandidateMapper.selectById(request.getAiCandidateId());
        if (candidate == null) {
            throw new BusinessException(4047, "AI generation candidate not found");
        }
        AiGenerationJob job = candidate.getJobId() == null ? null : aiGenerationJobMapper.selectById(candidate.getJobId());
        validateCandidateBindable(item, candidate);
        if (StringUtils.hasText(candidate.getProviderAssetUrl())) {
            aiOutboundUrlGuard.validatePublicSourceUrl(candidate.getProviderAssetUrl(), "AI candidate provider asset URL");
        }
        ContentAsset asset = contentAssetMapper.selectById(candidate.getFinalizedAssetId());
        if (asset == null) {
            throw new BusinessException(4046, "Finalized content asset not found");
        }

        StoryMaterialPackageItemVersion version = newVersion(item, SOURCE_AI_CANDIDATE, adminUserId, adminUsername);
        version.setContentAssetId(asset.getId());
        version.setAiJobId(candidate.getJobId());
        version.setAiCandidateId(candidate.getId());
        version.setProviderName(defaultText(request.getProviderName()));
        version.setModelCode(defaultText(request.getModelCode()));
        version.setParentVersionId(request.getParentVersionId());
        version.setLocalPath(defaultText(asset.getClientRelativePath()));
        version.setCosObjectKey(defaultText(asset.getObjectKey(), candidate.getStorageObjectKey()));
        version.setCanonicalUrl(defaultText(asset.getCanonicalUrl(), candidate.getStorageUrl()));
        version.setAssetKind(defaultText(request.getAssetKind(), asset.getAssetKind()));
        version.setPosterFallbackItemKey(defaultText(request.getPosterFallbackItemKey()));
        version.setPromptText(trimToNull(defaultText(request.getPromptText(), job == null ? null : job.getPromptText())));
        version.setScriptText(trimToNull(request.getScriptText()));
        version.setProvenanceJson(candidateProvenanceJson(candidate, job, request.getVerificationNote()));
        version.setCropMetadataJson(toJson(request.getCropRect(), "cropRect"));
        version.setSubtitleMetadataJson(normalizeJson(request.getSubtitleMetadataJson(), "subtitleMetadataJson"));
        version.setEstimatedCost(request.getEstimatedCost());
        version.setActualCost(request.getActualCost());
        version.setCurrencyCode(defaultText(request.getCurrencyCode(), "CNY"));
        insertVersionAndUpdateItem(materialPackage, item, version, asset, STATUS_UPLOADED);
        return toVersionResponse(version);
    }

    @Override
    @Transactional
    public AdminStoryMaterialProductionResponse.PromotionResult promoteItemVersion(
            Long packageId,
            Long itemId,
            AdminStoryMaterialProductionRequest.PromoteRequest request,
            Long adminUserId,
            String adminUsername,
            List<String> roles
    ) {
        if (!isSuperAdmin(roles) && !Boolean.TRUE.equals(request == null ? null : request.getSuperAdminConfirmation())) {
            throw new BusinessException(4036, "Super admin confirmation is required for material publish actions");
        }
        StoryMaterialPackageItem item = requireItem(packageId, itemId);
        StoryMaterialPackageItemVersion version = requireVersionForItem(item.getId(),
                request != null && request.getVersionId() != null ? request.getVersionId() : item.getCurrentVersionId());
        String targetStatus = request != null && StringUtils.hasText(request.getTargetStatus())
                ? normalizeCode(request.getTargetStatus())
                : STATUS_PUBLISHED;
        if (!Set.of(STATUS_UPLOADED, STATUS_APPROVED, STATUS_PUBLISHED).contains(targetStatus)) {
            throw new BusinessException(4002, "targetStatus must be uploaded, approved or published");
        }
        version.setPromotionStatus(targetStatus);
        version.setVersionStatus(targetStatus);
        version.setVerifiedByAdminId(adminUserId);
        version.setVerifiedByAdminName(defaultText(adminUsername));
        version.setVerifiedAt(LocalDateTime.now());
        versionMapper.updateById(version);
        ContentAsset asset = version.getContentAssetId() == null ? null : contentAssetMapper.selectById(version.getContentAssetId());
        applyVersionToItem(item, version, asset, targetStatus);
        return AdminStoryMaterialProductionResponse.PromotionResult.builder()
                .packageId(packageId)
                .itemId(itemId)
                .versionId(version.getId())
                .versionNo(version.getVersionNo())
                .targetStatus(targetStatus)
                .itemStatus(item.getStatus())
                .requiresSuperAdminConfirmation(true)
                .version(toVersionResponse(version))
                .build();
    }

    @Override
    @Transactional
    public AdminStoryMaterialProductionResponse.RollbackResult rollbackItemVersion(
            Long packageId,
            Long itemId,
            AdminStoryMaterialProductionRequest.RollbackRequest request,
            Long adminUserId,
            String adminUsername,
            List<String> roles
    ) {
        if (!isSuperAdmin(roles) && !Boolean.TRUE.equals(request == null ? null : request.getSuperAdminConfirmation())) {
            throw new BusinessException(4036, "Super admin confirmation is required for material rollback actions");
        }
        if (request == null || request.getRollbackVersionId() == null) {
            throw new BusinessException(4002, "rollbackVersionId is required");
        }
        StoryMaterialPackageItem item = requireItem(packageId, itemId);
        Long previousVersionId = item.getCurrentVersionId();
        StoryMaterialPackageItemVersion target = requireVersionForItem(item.getId(), request.getRollbackVersionId());
        StoryMaterialPackageItemVersion rollbackMarker = newVersion(item, "rollback", adminUserId, adminUsername);
        rollbackMarker.setContentAssetId(target.getContentAssetId());
        rollbackMarker.setParentVersionId(previousVersionId);
        rollbackMarker.setRollbackOfVersionId(target.getId());
        rollbackMarker.setVersionStatus(target.getVersionStatus());
        rollbackMarker.setPromotionStatus(target.getPromotionStatus());
        rollbackMarker.setProviderName(target.getProviderName());
        rollbackMarker.setModelCode(target.getModelCode());
        rollbackMarker.setLocalPath(target.getLocalPath());
        rollbackMarker.setCosObjectKey(target.getCosObjectKey());
        rollbackMarker.setCanonicalUrl(target.getCanonicalUrl());
        rollbackMarker.setAssetKind(target.getAssetKind());
        rollbackMarker.setPosterFallbackItemKey(target.getPosterFallbackItemKey());
        rollbackMarker.setPromptText(target.getPromptText());
        rollbackMarker.setScriptText(target.getScriptText());
        rollbackMarker.setProvenanceJson(provenanceJson("rollback", request.getVerificationNote(), null));
        rollbackMarker.setEstimatedCost(BigDecimal.ZERO);
        rollbackMarker.setActualCost(BigDecimal.ZERO);
        rollbackMarker.setCurrencyCode(defaultText(target.getCurrencyCode(), "CNY"));
        rollbackMarker.setVerifiedByAdminId(adminUserId);
        rollbackMarker.setVerifiedByAdminName(defaultText(adminUsername));
        rollbackMarker.setVerifiedAt(LocalDateTime.now());
        versionMapper.insert(rollbackMarker);

        ContentAsset asset = target.getContentAssetId() == null ? null : contentAssetMapper.selectById(target.getContentAssetId());
        applyVersionToItem(item, target, asset, defaultText(target.getPromotionStatus(), STATUS_UPLOADED));
        return AdminStoryMaterialProductionResponse.RollbackResult.builder()
                .packageId(packageId)
                .itemId(itemId)
                .previousVersionId(previousVersionId)
                .currentVersionId(target.getId())
                .currentVersionNo(target.getVersionNo())
                .itemStatus(item.getStatus())
                .requiresSuperAdminConfirmation(true)
                .version(toVersionResponse(target))
                .build();
    }

    @Override
    public List<AdminStoryMaterialProductionResponse.PackageItemVersionResponse> listVersions(
            Long packageId,
            Long itemId,
            AdminStoryMaterialProductionRequest.VersionHistoryQuery query
    ) {
        StoryMaterialPackageItem item = requireItem(packageId, itemId);
        String promotionStatus = query == null ? null : trimToNull(query.getPromotionStatus());
        return versionMapper.selectList(activeVersionQuery()
                        .eq(StoryMaterialPackageItemVersion::getPackageItemId, item.getId())
                        .eq(StringUtils.hasText(promotionStatus), StoryMaterialPackageItemVersion::getPromotionStatus, normalizeCode(promotionStatus))
                        .orderByDesc(StoryMaterialPackageItemVersion::getVersionNo)
                        .orderByDesc(StoryMaterialPackageItemVersion::getId))
                .stream()
                .map(this::toVersionResponse)
                .toList();
    }

    private void insertVersionAndUpdateItem(StoryMaterialPackage materialPackage,
                                            StoryMaterialPackageItem item,
                                            StoryMaterialPackageItemVersion version,
                                            ContentAsset asset,
                                            String itemStatus) {
        versionMapper.insert(version);
        applyVersionToItem(item, version, asset, itemStatus);
    }

    private StoryMaterialPackageItemVersion newVersion(StoryMaterialPackageItem item, String sourceType, Long adminUserId, String adminUsername) {
        Integer nextVersionNo = nextVersionNo(item.getId());
        StoryMaterialPackageItemVersion version = new StoryMaterialPackageItemVersion();
        version.setPackageItemId(item.getId());
        version.setVersionNo(nextVersionNo);
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
        StoryMaterialPackageItemVersion latest = versionMapper.selectList(activeVersionQuery()
                        .eq(StoryMaterialPackageItemVersion::getPackageItemId, itemId)
                        .orderByDesc(StoryMaterialPackageItemVersion::getVersionNo)
                        .last("LIMIT 1"))
                .stream()
                .findFirst()
                .orElse(null);
        return latest == null || latest.getVersionNo() == null ? 1 : latest.getVersionNo() + 1;
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

    private void validateCandidateBindable(StoryMaterialPackageItem item, AiGenerationCandidate candidate) {
        boolean finalized = Objects.equals(candidate.getIsFinalized(), 1) && candidate.getFinalizedAssetId() != null;
        boolean binaryAvailable = StringUtils.hasText(candidate.getStorageObjectKey()) || StringUtils.hasText(candidate.getStorageUrl());
        if (!finalized || !binaryAvailable) {
            if (isAudioLike(item, candidate)) {
                item.setStatus(STATUS_MANUAL_IMPORT_REQUIRED);
                packageItemMapper.updateById(item);
            }
            throw new BusinessException(4096, "Finalized AI candidate binary is required before binding");
        }
    }

    private boolean isAudioLike(StoryMaterialPackageItem item, AiGenerationCandidate candidate) {
        String itemKey = defaultText(item.getItemKey()).toLowerCase(Locale.ROOT);
        String assetKind = defaultText(item.getAssetKind()).toLowerCase(Locale.ROOT);
        String candidateType = defaultText(candidate.getCandidateType()).toLowerCase(Locale.ROOT);
        return "audio".equals(assetKind)
                || "audio".equals(candidateType)
                || itemKey.contains("narration")
                || itemKey.contains("sfx_reward_unlock");
    }

    private StoryMaterialPackage requirePackage(Long packageId) {
        StoryMaterialPackage materialPackage = packageMapper.selectOne(new LambdaQueryWrapper<StoryMaterialPackage>()
                .eq(StoryMaterialPackage::getDeleted, 0)
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

    private LambdaQueryWrapper<StoryMaterialPackageItem> activeItemQuery() {
        return new LambdaQueryWrapper<StoryMaterialPackageItem>().eq(StoryMaterialPackageItem::getDeleted, 0);
    }

    private LambdaQueryWrapper<StoryMaterialPackageItemVersion> activeVersionQuery() {
        return new LambdaQueryWrapper<StoryMaterialPackageItemVersion>().eq(StoryMaterialPackageItemVersion::getDeleted, 0);
    }

    public AdminStoryMaterialProductionResponse.PackageItemVersionResponse toVersionResponse(StoryMaterialPackageItemVersion version) {
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

    private String candidateProvenanceJson(AiGenerationCandidate candidate, AiGenerationJob job, String note) {
        try {
            return objectMapper.writeValueAsString(new CandidateProvenance(
                    1,
                    SOURCE_AI_CANDIDATE,
                    note,
                    candidate.getProviderAssetUrl(),
                    candidate.getMetadataJson(),
                    job == null ? null : job.getProviderRequestId()
            ));
        } catch (JsonProcessingException ex) {
            throw new BusinessException(4002, "candidate provenance could not be serialized");
        }
    }

    private String provenanceJson(String sourceType, String note, String subtitlePath) {
        try {
            return objectMapper.writeValueAsString(new ProductionProvenance(1, sourceType, note, subtitlePath));
        } catch (JsonProcessingException ex) {
            throw new BusinessException(4002, "production provenance could not be serialized");
        }
    }

    private String toJson(Object value, String fieldName) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new BusinessException(4002, fieldName + " could not be serialized");
        }
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

    private String probeMimeType(Path path) {
        try {
            String mimeType = Files.probeContentType(path);
            return StringUtils.hasText(mimeType) ? mimeType : "application/octet-stream";
        } catch (IOException ex) {
            return "application/octet-stream";
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

    private record ProductionProvenance(Integer schemaVersion, String sourceType, String note, String subtitlePath) {
    }

    private record CandidateProvenance(Integer schemaVersion,
                                       String sourceType,
                                       String note,
                                       String providerAssetUrl,
                                       String candidateMetadataJson,
                                       String providerRequestId) {
    }
}
