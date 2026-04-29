package com.aoxiaoyou.admin.service;

import com.aoxiaoyou.admin.common.api.PageResponse;
import com.aoxiaoyou.admin.dto.request.AdminStoryContentBlockUpsertRequest;
import com.aoxiaoyou.admin.dto.response.AdminStoryContentBlockResponse;

public interface AdminStoryContentBlockService {

    PageResponse<AdminStoryContentBlockResponse> page(long pageNum, long pageSize, String keyword, String blockType, String status);

    AdminStoryContentBlockResponse detail(Long blockId);

    AdminStoryContentBlockResponse create(AdminStoryContentBlockUpsertRequest request);

    AdminStoryContentBlockResponse update(Long blockId, AdminStoryContentBlockUpsertRequest request);

    void delete(Long blockId);
}
