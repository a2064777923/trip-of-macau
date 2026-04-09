package com.aoxiaoyou.admin.service;

import com.aoxiaoyou.admin.common.api.PageResponse;
import com.aoxiaoyou.admin.dto.request.AdminPoiUpsertRequest;
import com.aoxiaoyou.admin.dto.response.AdminPoiDetailResponse;
import com.aoxiaoyou.admin.dto.response.AdminPoiListItemResponse;

public interface AdminPoiService {

    PageResponse<AdminPoiListItemResponse> pagePois(long pageNum, long pageSize, String keyword, Long storylineId);

    AdminPoiDetailResponse getDetail(Long poiId);

    AdminPoiDetailResponse create(AdminPoiUpsertRequest request);

    AdminPoiDetailResponse update(Long poiId, AdminPoiUpsertRequest request);

    void delete(Long poiId);
}
