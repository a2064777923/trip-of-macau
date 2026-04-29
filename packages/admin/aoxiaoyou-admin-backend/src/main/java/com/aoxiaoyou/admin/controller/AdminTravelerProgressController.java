package com.aoxiaoyou.admin.controller;

import com.aoxiaoyou.admin.common.api.ApiResponse;
import com.aoxiaoyou.admin.common.api.PageResponse;
import com.aoxiaoyou.admin.dto.response.AdminTravelerProgressWorkbenchResponse;
import com.aoxiaoyou.admin.dto.response.AdminTravelerTimelineEntryResponse;
import com.aoxiaoyou.admin.dto.response.AdminUserProgressBreakdownResponse;
import com.aoxiaoyou.admin.service.AdminTravelerProgressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "Traveler Progress Workbench")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/v1/users")
public class AdminTravelerProgressController {

    private final AdminTravelerProgressService adminTravelerProgressService;

    @Operation(summary = "Load traveler progress workbench")
    @GetMapping("/{userId}/progress-workbench")
    public ApiResponse<AdminTravelerProgressWorkbenchResponse> getProgressWorkbench(@PathVariable Long userId) {
        return ApiResponse.success(adminTravelerProgressService.getProgressWorkbench(userId));
    }

    @Operation(summary = "Load traveler progress breakdown")
    @GetMapping("/{userId}/progress-breakdown")
    public ApiResponse<AdminUserProgressBreakdownResponse> getProgressBreakdown(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "global") String scopeType,
            @RequestParam(required = false) Long scopeId,
            @RequestParam(defaultValue = "false") boolean includeInactiveElements) {
        return ApiResponse.success(
                adminTravelerProgressService.getProgressBreakdown(userId, scopeType, scopeId, includeInactiveElements)
        );
    }

    @Operation(summary = "Load traveler timeline")
    @GetMapping("/{userId}/timeline")
    public ApiResponse<PageResponse<AdminTravelerTimelineEntryResponse>> getTimeline(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "20") long pageSize,
            @RequestParam(required = false) List<String> eventTypes,
            @RequestParam(required = false) Long storylineId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return ApiResponse.success(
                adminTravelerProgressService.getTimeline(
                        userId,
                        new AdminTravelerProgressService.TimelineQuery(pageNum, pageSize, eventTypes, storylineId, from, to)
                )
        );
    }
}
