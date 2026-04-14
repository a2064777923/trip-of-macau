package com.aoxiaoyou.admin.service.impl;

import com.aoxiaoyou.admin.common.enums.SpatialAssetUsageType;
import com.aoxiaoyou.admin.common.enums.SpatialEntityType;
import com.aoxiaoyou.admin.common.exception.BusinessException;
import com.aoxiaoyou.admin.dto.request.AdminSpatialAssetLinkUpsertRequest;
import com.aoxiaoyou.admin.dto.response.AdminSpatialAssetLinkResponse;
import com.aoxiaoyou.admin.entity.ContentAsset;
import com.aoxiaoyou.admin.entity.ContentAssetLink;
import com.aoxiaoyou.admin.mapper.ContentAssetLinkMapper;
import com.aoxiaoyou.admin.mapper.ContentAssetMapper;
import com.aoxiaoyou.admin.service.AdminSpatialAssetLinkService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminSpatialAssetLinkServiceImpl implements AdminSpatialAssetLinkService {

    private final ContentAssetLinkMapper contentAssetLinkMapper;
    private final ContentAssetMapper contentAssetMapper;

    @Override
    public List<AdminSpatialAssetLinkResponse> listLinks(String entityType, Long entityId) {
        if (!StringUtils.hasText(entityType) || entityId == null) {
            return Collections.emptyList();
        }
        return contentAssetLinkMapper.selectList(new LambdaQueryWrapper<ContentAssetLink>()
                        .eq(ContentAssetLink::getEntityType, entityType)
                        .eq(ContentAssetLink::getEntityId, entityId)
                        .orderByAsc(ContentAssetLink::getSortOrder)
                        .orderByAsc(ContentAssetLink::getId))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public void syncLinks(String entityType, Long entityId, List<AdminSpatialAssetLinkUpsertRequest> requests) {
        if (entityId == null) {
            return;
        }
        validateEntityType(entityType);
        List<AdminSpatialAssetLinkUpsertRequest> normalizedRequests = requests == null ? Collections.emptyList() : requests;
        List<ContentAssetLink> existingLinks = contentAssetLinkMapper.selectList(new LambdaQueryWrapper<ContentAssetLink>()
                .eq(ContentAssetLink::getEntityType, entityType)
                .eq(ContentAssetLink::getEntityId, entityId));
        Map<Long, ContentAssetLink> existingById = existingLinks.stream()
                .filter(link -> link.getId() != null)
                .collect(Collectors.toMap(ContentAssetLink::getId, link -> link, (left, right) -> left, LinkedHashMap::new));

        Set<Long> retainedIds = normalizedRequests.stream()
                .map(AdminSpatialAssetLinkUpsertRequest::getId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());

        for (ContentAssetLink existing : existingLinks) {
            if (existing.getId() != null && !retainedIds.contains(existing.getId())) {
                contentAssetLinkMapper.deleteById(existing.getId());
            }
        }

        for (int index = 0; index < normalizedRequests.size(); index++) {
            AdminSpatialAssetLinkUpsertRequest request = normalizedRequests.get(index);
            validateUsageType(request.getUsageType());
            requireAttachableAsset(request.getAssetId());

            ContentAssetLink link = request.getId() == null ? new ContentAssetLink() : existingById.get(request.getId());
            if (link == null) {
                throw new BusinessException(4058, "Spatial asset link not found");
            }
            link.setEntityType(entityType);
            link.setEntityId(entityId);
            link.setUsageType(request.getUsageType());
            link.setAssetId(request.getAssetId());
            link.setTitleZh(request.getTitleZh());
            link.setTitleEn(request.getTitleEn());
            link.setTitleZht(request.getTitleZht());
            link.setTitlePt(request.getTitlePt());
            link.setDescriptionZh(request.getDescriptionZh());
            link.setDescriptionEn(request.getDescriptionEn());
            link.setDescriptionZht(request.getDescriptionZht());
            link.setDescriptionPt(request.getDescriptionPt());
            link.setDisplayConfigJson(request.getDisplayConfigJson());
            link.setSortOrder(request.getSortOrder() == null ? index : request.getSortOrder());
            link.setStatus(StringUtils.hasText(request.getStatus()) ? request.getStatus() : "draft");

            if (link.getId() == null) {
                contentAssetLinkMapper.insert(link);
            } else {
                contentAssetLinkMapper.updateById(link);
            }
        }
    }

    private void validateEntityType(String entityType) {
        if (!SpatialEntityType.supportedCodes().contains(entityType)) {
            throw new BusinessException(4055, "Unsupported spatial entity type");
        }
    }

    private void validateUsageType(String usageType) {
        if (!SpatialAssetUsageType.supportedCodes().contains(usageType)) {
            throw new BusinessException(4056, "Unsupported spatial asset usage type");
        }
    }

    private ContentAsset requireAttachableAsset(Long assetId) {
        if (assetId == null) {
            throw new BusinessException(4057, "Spatial asset id is required");
        }
        ContentAsset asset = contentAssetMapper.selectById(assetId);
        if (asset == null) {
            throw new BusinessException(4051, "Content asset not found");
        }
        if (!"draft".equalsIgnoreCase(asset.getStatus()) && !"published".equalsIgnoreCase(asset.getStatus())) {
            throw new BusinessException(4059, "Spatial links only accept draft or published assets");
        }
        return asset;
    }

    private AdminSpatialAssetLinkResponse toResponse(ContentAssetLink link) {
        return AdminSpatialAssetLinkResponse.builder()
                .id(link.getId())
                .entityType(link.getEntityType())
                .entityId(link.getEntityId())
                .usageType(link.getUsageType())
                .assetId(link.getAssetId())
                .titleZh(link.getTitleZh())
                .titleEn(link.getTitleEn())
                .titleZht(link.getTitleZht())
                .titlePt(link.getTitlePt())
                .descriptionZh(link.getDescriptionZh())
                .descriptionEn(link.getDescriptionEn())
                .descriptionZht(link.getDescriptionZht())
                .descriptionPt(link.getDescriptionPt())
                .displayConfigJson(link.getDisplayConfigJson())
                .sortOrder(link.getSortOrder())
                .status(link.getStatus())
                .build();
    }
}
