package com.aoxiaoyou.admin.service;

import com.aoxiaoyou.admin.entity.ContentRelationLink;

import java.util.Collection;
import java.util.List;

public interface AdminContentRelationService {

    List<Long> listTargetIds(String ownerType, Long ownerId, String relationType, String targetType);

    List<ContentRelationLink> listLinks(String ownerType, Collection<Long> ownerIds, String relationType);

    void syncTargetIds(String ownerType, Long ownerId, String relationType, String targetType, List<Long> targetIds);
}
