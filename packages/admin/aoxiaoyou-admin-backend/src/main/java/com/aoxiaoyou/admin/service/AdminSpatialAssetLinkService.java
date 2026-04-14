package com.aoxiaoyou.admin.service;

import com.aoxiaoyou.admin.dto.request.AdminSpatialAssetLinkUpsertRequest;
import com.aoxiaoyou.admin.dto.response.AdminSpatialAssetLinkResponse;

import java.util.List;

public interface AdminSpatialAssetLinkService {
    List<AdminSpatialAssetLinkResponse> listLinks(String entityType, Long entityId);
    void syncLinks(String entityType, Long entityId, List<AdminSpatialAssetLinkUpsertRequest> requests);
}
