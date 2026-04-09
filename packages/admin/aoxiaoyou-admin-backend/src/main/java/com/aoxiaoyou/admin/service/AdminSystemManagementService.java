package com.aoxiaoyou.admin.service;

import com.aoxiaoyou.admin.common.api.PageResponse;
import com.aoxiaoyou.admin.dto.request.AdminRewardUpsertRequest;
import com.aoxiaoyou.admin.dto.response.AdminMapTileResponse;
import com.aoxiaoyou.admin.dto.response.AdminRewardResponse;
import com.aoxiaoyou.admin.dto.response.AdminSystemConfigResponse;
import com.aoxiaoyou.admin.dto.response.AdminOperationLogResponse;

public interface AdminSystemManagementService {

    PageResponse<AdminRewardResponse> pageRewards(long pageNum, long pageSize, String status);

    AdminRewardResponse createReward(AdminRewardUpsertRequest.Upsert request);

    AdminRewardResponse updateReward(Long rewardId, AdminRewardUpsertRequest.Upsert request);

    void deleteReward(Long rewardId);

    PageResponse<AdminOperationLogResponse> pageAuditLogs(long pageNum, long pageSize, String module);

    PageResponse<AdminSystemConfigResponse> pageConfigs(long pageNum, long pageSize, String keyword);

    PageResponse<AdminMapTileResponse> pageMapTiles(long pageNum, long pageSize);
}

