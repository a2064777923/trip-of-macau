package com.aoxiaoyou.admin.service;

import com.aoxiaoyou.admin.common.api.PageResponse;
import com.aoxiaoyou.admin.dto.response.AdminActivityResponse;

public interface AdminOperationsService {

    PageResponse<AdminActivityResponse> pageActivities(long pageNum, long pageSize, String keyword, String status);
}
