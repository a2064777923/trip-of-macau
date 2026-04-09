package com.aoxiaoyou.admin.service;

import com.aoxiaoyou.admin.common.api.PageResponse;
import com.aoxiaoyou.admin.dto.response.AdminAiLogResponse;
import com.aoxiaoyou.admin.dto.response.AdminAiPolicyResponse;
import com.aoxiaoyou.admin.dto.response.AdminAiProviderResponse;

import java.util.List;

public interface AdminAiService {
    List<AdminAiProviderResponse> listProviders();
    List<AdminAiPolicyResponse> listPolicies(String scenarioGroup);
    PageResponse<AdminAiLogResponse> pageLogs(long pageNum, long pageSize, String scenarioGroup, Integer success, Long providerId);
}
