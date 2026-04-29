package com.aoxiaoyou.admin.service;

import com.aoxiaoyou.admin.common.api.PageResponse;
import com.aoxiaoyou.admin.dto.response.AdminTravelerProgressWorkbenchResponse;
import com.aoxiaoyou.admin.dto.response.AdminTravelerTimelineEntryResponse;
import com.aoxiaoyou.admin.dto.response.AdminUserProgressBreakdownResponse;

import java.time.LocalDateTime;
import java.util.List;

public interface AdminTravelerProgressService {

    AdminTravelerProgressWorkbenchResponse getProgressWorkbench(Long userId);

    AdminUserProgressBreakdownResponse getProgressBreakdown(
            Long userId,
            String scopeType,
            Long scopeId,
            boolean includeInactiveElements);

    PageResponse<AdminTravelerTimelineEntryResponse> getTimeline(Long userId, TimelineQuery query);

    record TimelineQuery(
            long pageNum,
            long pageSize,
            List<String> eventTypes,
            Long storylineId,
            LocalDateTime from,
            LocalDateTime to) {
    }
}
