package com.aoxiaoyou.admin.service;

import com.aoxiaoyou.admin.common.api.PageResponse;
import com.aoxiaoyou.admin.dto.request.AdminSubMapUpsertRequest;
import com.aoxiaoyou.admin.dto.response.AdminSubMapResponse;

public interface AdminSubMapService {
    PageResponse<AdminSubMapResponse> pageSubMaps(long pageNum, long pageSize, Long cityId, String keyword, String status);
    AdminSubMapResponse getSubMapDetail(Long id);
    AdminSubMapResponse createSubMap(AdminSubMapUpsertRequest request);
    AdminSubMapResponse updateSubMap(Long id, AdminSubMapUpsertRequest request);
    AdminSubMapResponse publishSubMap(Long id);
    AdminSubMapResponse updateSubMapStatus(Long id, String status);
}
