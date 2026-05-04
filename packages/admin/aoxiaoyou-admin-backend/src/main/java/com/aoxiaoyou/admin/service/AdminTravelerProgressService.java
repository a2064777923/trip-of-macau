package com.aoxiaoyou.admin.service;

import com.aoxiaoyou.admin.common.api.PageResponse;
import com.aoxiaoyou.admin.dto.response.AdminTravelerProgressWorkbenchResponse;
import com.aoxiaoyou.admin.dto.response.AdminTravelerRewardRuleTraceResponse;
import com.aoxiaoyou.admin.dto.response.AdminTravelerRewardStateResponse;
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

    AdminTravelerRewardStateResponse getRewardState(Long userId);

    AdminTravelerRewardRuleTraceResponse getRewardRuleTrace(
            Long userId,
            Long sourceEventId,
            Long ruleId,
            Long rewardId,
            Long gameRewardId);

    record TimelineQuery(
            long pageNum,
            long pageSize,
            List<String> eventTypes,
            Long storylineId,
            Long chapterId,
            Long poiId,
            String mapScopeType,
            Long mapScopeId,
            String status,
            String rewardType,
            LocalDateTime from,
            LocalDateTime to) {
    }
}
