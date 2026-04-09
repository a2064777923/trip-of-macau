package com.aoxiaoyou.admin.service;

import com.aoxiaoyou.admin.common.api.PageResponse;
import com.aoxiaoyou.admin.dto.request.AdminStoryLineUpsertRequest;
import com.aoxiaoyou.admin.dto.response.AdminStoryLineDetailResponse;
import com.aoxiaoyou.admin.dto.response.AdminStoryLineListItemResponse;

public interface AdminStoryLineService {

    PageResponse<AdminStoryLineListItemResponse> page(long pageNum, long pageSize, String keyword, String status);

    AdminStoryLineDetailResponse detail(Long storylineId);

    AdminStoryLineDetailResponse create(AdminStoryLineUpsertRequest.Upsert request);

    AdminStoryLineDetailResponse update(Long storylineId, AdminStoryLineUpsertRequest.Upsert request);

    void delete(Long storylineId);
}
