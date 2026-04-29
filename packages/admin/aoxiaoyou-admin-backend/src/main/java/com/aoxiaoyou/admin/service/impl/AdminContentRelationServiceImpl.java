package com.aoxiaoyou.admin.service.impl;

import com.aoxiaoyou.admin.entity.ContentRelationLink;
import com.aoxiaoyou.admin.mapper.ContentRelationLinkMapper;
import com.aoxiaoyou.admin.service.AdminContentRelationService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminContentRelationServiceImpl implements AdminContentRelationService {

    private final ContentRelationLinkMapper contentRelationLinkMapper;

    @Override
    public List<Long> listTargetIds(String ownerType, Long ownerId, String relationType, String targetType) {
        if (!StringUtils.hasText(ownerType) || ownerId == null || !StringUtils.hasText(relationType) || !StringUtils.hasText(targetType)) {
            return Collections.emptyList();
        }
        return contentRelationLinkMapper.selectList(new LambdaQueryWrapper<ContentRelationLink>()
                        .eq(ContentRelationLink::getOwnerType, ownerType)
                        .eq(ContentRelationLink::getOwnerId, ownerId)
                        .eq(ContentRelationLink::getRelationType, relationType)
                        .eq(ContentRelationLink::getTargetType, targetType)
                        .orderByAsc(ContentRelationLink::getSortOrder)
                        .orderByAsc(ContentRelationLink::getId))
                .stream()
                .map(ContentRelationLink::getTargetId)
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public List<ContentRelationLink> listLinks(String ownerType, Collection<Long> ownerIds, String relationType) {
        if (!StringUtils.hasText(ownerType) || ownerIds == null || ownerIds.isEmpty() || !StringUtils.hasText(relationType)) {
            return Collections.emptyList();
        }
        return contentRelationLinkMapper.selectList(new LambdaQueryWrapper<ContentRelationLink>()
                .eq(ContentRelationLink::getOwnerType, ownerType)
                .in(ContentRelationLink::getOwnerId, ownerIds)
                .eq(ContentRelationLink::getRelationType, relationType)
                .orderByAsc(ContentRelationLink::getOwnerId)
                .orderByAsc(ContentRelationLink::getSortOrder)
                .orderByAsc(ContentRelationLink::getId));
    }

    @Override
    public void syncTargetIds(String ownerType, Long ownerId, String relationType, String targetType, List<Long> targetIds) {
        if (!StringUtils.hasText(ownerType) || ownerId == null || !StringUtils.hasText(relationType) || !StringUtils.hasText(targetType)) {
            return;
        }

        List<Long> normalizedTargetIds = (targetIds == null ? Collections.<Long>emptyList() : targetIds).stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        List<ContentRelationLink> existingLinks = contentRelationLinkMapper.selectList(new LambdaQueryWrapper<ContentRelationLink>()
                .eq(ContentRelationLink::getOwnerType, ownerType)
                .eq(ContentRelationLink::getOwnerId, ownerId)
                .eq(ContentRelationLink::getRelationType, relationType)
                .eq(ContentRelationLink::getTargetType, targetType));

        Map<Long, ContentRelationLink> existingByTargetId = existingLinks.stream()
                .filter(link -> link.getTargetId() != null)
                .collect(Collectors.toMap(ContentRelationLink::getTargetId, link -> link, (left, right) -> left, LinkedHashMap::new));

        Set<Long> retainedIds = normalizedTargetIds.stream().collect(Collectors.toSet());
        for (ContentRelationLink existing : existingLinks) {
            if (existing.getTargetId() != null && !retainedIds.contains(existing.getTargetId())) {
                contentRelationLinkMapper.deleteById(existing.getId());
            }
        }

        for (int index = 0; index < normalizedTargetIds.size(); index++) {
            Long targetId = normalizedTargetIds.get(index);
            ContentRelationLink link = existingByTargetId.get(targetId);
            if (link == null) {
                link = new ContentRelationLink();
                link.setOwnerType(ownerType);
                link.setOwnerId(ownerId);
                link.setRelationType(relationType);
                link.setTargetType(targetType);
                link.setTargetId(targetId);
                link.setSortOrder(index);
                contentRelationLinkMapper.insert(link);
                continue;
            }
            link.setSortOrder(index);
            contentRelationLinkMapper.updateById(link);
        }
    }
}
