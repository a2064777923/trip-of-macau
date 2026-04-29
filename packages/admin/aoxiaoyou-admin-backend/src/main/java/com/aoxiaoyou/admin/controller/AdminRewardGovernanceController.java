package com.aoxiaoyou.admin.controller;

import com.aoxiaoyou.admin.common.api.ApiResponse;
import com.aoxiaoyou.admin.dto.response.AdminRewardGovernanceResponse;
import com.aoxiaoyou.admin.service.AdminRewardDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/v1/reward-governance")
@RequiredArgsConstructor
public class AdminRewardGovernanceController {

    private final AdminRewardDomainService rewardDomainService;

    @GetMapping
    public ApiResponse<AdminRewardGovernanceResponse> getOverview() {
        return ApiResponse.success(rewardDomainService.getRewardGovernanceOverview());
    }
}
