package com.aoxiaoyou.admin.controller;

import com.aoxiaoyou.admin.common.api.ApiResponse;
import com.aoxiaoyou.admin.dto.response.DashboardStatsResponse;
import com.aoxiaoyou.admin.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "后台仪表盘")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/v1/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "获取仪表盘统计")
    @GetMapping("/stats")
    public ApiResponse<DashboardStatsResponse> stats() {
        return ApiResponse.success(dashboardService.getDashboardStats());
    }
}
