package com.aoxiaoyou.tripofmacau.controller;

import com.aoxiaoyou.tripofmacau.common.api.ApiResponse;
import com.aoxiaoyou.tripofmacau.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "统计数据")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stats")
public class StatsController {
    
    private final DashboardService dashboardService;
    
    @Operation(summary = "获取仪表盘统计数据")
    @GetMapping("/dashboard")
    public ApiResponse<Map<String, Object>> getDashboardStats() {
        return ApiResponse.success(dashboardService.getStats());
    }
    
    @Operation(summary = "获取用户增长趋势")
    @GetMapping("/user-growth")
    public ApiResponse<Map<String, Object>> getUserGrowthTrend(
            @RequestParam(defaultValue = "30") int days) {
        // TODO: 实现用户增长趋势查询
        return ApiResponse.success(Map.of(
                "labels", new String[]{"周一", "周二", "周三", "周四", "周五", "周六", "周日"},
                "data", new int[]{120, 150, 180, 170, 200, 220, 250}
        ));
    }
    
    @Operation(summary = "获取印章领取统计")
    @GetMapping("/stamps")
    public ApiResponse<Map<String, Object>> getStampStats(
            @RequestParam(defaultValue = "30") int days) {
        // TODO: 实现印章统计查询
        return ApiResponse.success(Map.of(
                "total", 8924,
                "today", 128,
                "thisWeek", 680
        ));
    }
    
    @Operation(summary = "获取 POI 访问热力图")
    @GetMapping("/poi-heatmap")
    public ApiResponse<Map<String, Object>> getPOIHeatmap() {
        // TODO: 实现 POI 热力图查询
        return ApiResponse.success(Map.of(
                "points", new Object[]{}
        ));
    }
}
