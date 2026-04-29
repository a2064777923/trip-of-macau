package com.aoxiaoyou.admin.service;

import com.aoxiaoyou.admin.common.api.PageResponse;
import com.aoxiaoyou.admin.dto.request.AdminActivityUpsertRequest;
import com.aoxiaoyou.admin.dto.response.AdminActivityResponse;

public interface AdminOperationsService {

    PageResponse<AdminActivityResponse> pageActivities(long pageNum, long pageSize, String keyword, String status, String activityType);

    AdminActivityResponse getActivity(Long activityId);

    AdminActivityResponse createActivity(AdminActivityUpsertRequest.Upsert request);

    AdminActivityResponse updateActivity(Long activityId, AdminActivityUpsertRequest.Upsert request);

    void deleteActivity(Long activityId);
}
