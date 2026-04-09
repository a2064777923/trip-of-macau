package com.aoxiaoyou.admin.controller;

import com.aoxiaoyou.admin.common.api.ApiResponse;
import com.aoxiaoyou.admin.common.api.PageResponse;
import com.aoxiaoyou.admin.dto.response.AdminAiLogResponse;
import com.aoxiaoyou.admin.dto.response.AdminAiPolicyResponse;
import com.aoxiaoyou.admin.dto.response.AdminAiProviderResponse;
import com.aoxiaoyou.admin.service.AdminAiService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/v1/ai")
public class AdminAiController {

    private final AdminAiService adminAiService;

    @GetMapping("/providers")
    public ApiResponse<List<AdminAiProviderResponse>> listProviders() {
        return ApiResponse.success(adminAiService.listProviders());
    }

    @GetMapping("/policies")
    public ApiResponse<List<AdminAiPolicyResponse>> listPolicies(@RequestParam(required = false) String scenarioGroup) {
        return ApiResponse.success(adminAiService.listPolicies(scenarioGroup));
    }

    @GetMapping("/logs")
    public ApiResponse<PageResponse<AdminAiLogResponse>> pageLogs(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) String scenarioGroup,
            @RequestParam(required = false) Integer success,
            @RequestParam(required = false) Long providerId) {
        return ApiResponse.success(adminAiService.pageLogs(pageNum, pageSize, scenarioGroup, success, providerId));
    }
}
