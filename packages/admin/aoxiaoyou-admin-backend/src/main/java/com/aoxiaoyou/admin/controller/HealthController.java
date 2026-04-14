package com.aoxiaoyou.admin.controller;

import com.aoxiaoyou.admin.dto.response.DashboardStatsResponse;
import com.aoxiaoyou.admin.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class HealthController {

    private final DashboardService dashboardService;

    @GetMapping("/health")
    public Map<String, Object> health() {
        DashboardStatsResponse stats = dashboardService.getDashboardStats();
        boolean databaseHealthy = stats.getIntegrationHealth() != null
                && stats.getIntegrationHealth().getDatabase() != null
                && Boolean.TRUE.equals(stats.getIntegrationHealth().getDatabase().getHealthy());
        boolean publicApiHealthy = stats.getIntegrationHealth() != null
                && stats.getIntegrationHealth().getPublicApi() != null
                && Boolean.TRUE.equals(stats.getIntegrationHealth().getPublicApi().getHealthy());

        String status = databaseHealthy && publicApiHealthy ? "UP" : "DEGRADED";

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "success");
        result.put("data", Map.of(
                "service", "aoxiaoyou-admin-backend",
                "version", "0.1.0",
                "status", status,
                "integrationHealth", stats.getIntegrationHealth(),
                "contentSummary", stats.getContentSummary()
        ));
        result.put("timestamp", System.currentTimeMillis());
        return result;
    }
}
