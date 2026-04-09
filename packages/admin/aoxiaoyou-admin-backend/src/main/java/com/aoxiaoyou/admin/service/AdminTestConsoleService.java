package com.aoxiaoyou.admin.service;

import com.aoxiaoyou.admin.common.api.PageResponse;
import com.aoxiaoyou.admin.dto.request.TestAccountBatchStampGrantRequest;
import com.aoxiaoyou.admin.dto.request.TestAccountLevelAdjustRequest;
import com.aoxiaoyou.admin.dto.request.TestAccountMockLocationRequest;
import com.aoxiaoyou.admin.dto.request.TestAccountProgressResetRequest;
import com.aoxiaoyou.admin.dto.request.TestAccountStampGrantRequest;
import com.aoxiaoyou.admin.dto.response.AdminOperationLogResponse;
import com.aoxiaoyou.admin.dto.response.AdminTestAccountListItemResponse;
import com.aoxiaoyou.admin.dto.response.AdminTestStampSummaryResponse;

public interface AdminTestConsoleService {

    PageResponse<AdminTestAccountListItemResponse> page(long pageNum, long pageSize, String testGroup);

    AdminTestAccountListItemResponse toggleMockLocation(Long testAccountId, TestAccountMockLocationRequest request, Long operatorId, String operatorName, String ip);

    AdminTestAccountListItemResponse adjustLevel(Long testAccountId, TestAccountLevelAdjustRequest request, Long operatorId, String operatorName, String ip);

    AdminTestAccountListItemResponse grantStamp(Long testAccountId, TestAccountStampGrantRequest request, Long operatorId, String operatorName, String ip);

    AdminTestAccountListItemResponse batchGrantStamp(Long testAccountId, TestAccountBatchStampGrantRequest request, Long operatorId, String operatorName, String ip);

    AdminTestAccountListItemResponse clearStamps(Long testAccountId, Long operatorId, String operatorName, String ip, String reason);

    AdminTestStampSummaryResponse stampSummary(Long testAccountId);

    AdminTestAccountListItemResponse resetProgress(Long testAccountId, TestAccountProgressResetRequest request, Long operatorId, String operatorName, String ip);

    PageResponse<AdminOperationLogResponse> operationLogs(Long testAccountId, long pageNum, long pageSize);
}

