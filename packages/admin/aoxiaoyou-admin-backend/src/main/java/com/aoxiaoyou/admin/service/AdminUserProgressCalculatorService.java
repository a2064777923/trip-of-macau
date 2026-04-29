package com.aoxiaoyou.admin.service;

import com.aoxiaoyou.admin.dto.response.AdminUserProgressBreakdownResponse;
import com.aoxiaoyou.admin.dto.response.AdminUserProgressSummaryResponse;

public interface AdminUserProgressCalculatorService {
    AdminUserProgressSummaryResponse calculateSummary(Long userId, String scopeType, Long scopeId, boolean includeInactiveElements);

    AdminUserProgressBreakdownResponse calculateBreakdown(Long userId, String scopeType, Long scopeId, boolean includeInactiveElements);
}
