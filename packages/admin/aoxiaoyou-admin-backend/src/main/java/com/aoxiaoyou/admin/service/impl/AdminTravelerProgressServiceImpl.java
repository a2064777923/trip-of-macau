package com.aoxiaoyou.admin.service.impl;

import com.aoxiaoyou.admin.common.api.PageResponse;
import com.aoxiaoyou.admin.dto.response.AdminTravelerProgressWorkbenchResponse;
import com.aoxiaoyou.admin.dto.response.AdminTravelerTimelineEntryResponse;
import com.aoxiaoyou.admin.dto.response.AdminUserProgressBreakdownResponse;
import com.aoxiaoyou.admin.mapper.AdminTravelerProgressReadMapper;
import com.aoxiaoyou.admin.mapper.CityMapper;
import com.aoxiaoyou.admin.mapper.TestAccountMapper;
import com.aoxiaoyou.admin.mapper.TravelerProfileMapper;
import com.aoxiaoyou.admin.service.AdminTravelerProgressService;
import com.aoxiaoyou.admin.service.AdminUserProgressCalculatorService;
import com.aoxiaoyou.admin.service.support.RouteTraceSourceAdapter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminTravelerProgressServiceImpl implements AdminTravelerProgressService {

    private final TravelerProfileMapper travelerProfileMapper;
    private final TestAccountMapper testAccountMapper;
    private final CityMapper cityMapper;
    private final AdminTravelerProgressReadMapper readMapper;
    private final AdminUserProgressCalculatorService calculatorService;
    private final RouteTraceSourceAdapter routeTraceSourceAdapter;

    @Override
    public AdminTravelerProgressWorkbenchResponse getProgressWorkbench(Long userId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public AdminUserProgressBreakdownResponse getProgressBreakdown(
            Long userId,
            String scopeType,
            Long scopeId,
            boolean includeInactiveElements) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public PageResponse<AdminTravelerTimelineEntryResponse> getTimeline(Long userId, TimelineQuery query) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
