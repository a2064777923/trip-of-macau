package com.aoxiaoyou.admin.controller;

import com.aoxiaoyou.admin.common.api.ApiResponse;
import com.aoxiaoyou.admin.common.api.PageResponse;
import com.aoxiaoyou.admin.dto.request.AdminRewardRuleUpsertRequest;
import com.aoxiaoyou.admin.dto.response.AdminRewardRuleResponse;
import com.aoxiaoyou.admin.service.AdminRewardDomainService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/v1/reward-rules")
@RequiredArgsConstructor
public class AdminRewardRuleController {

    private final AdminRewardDomainService rewardDomainService;

    @GetMapping
    public ApiResponse<PageResponse<AdminRewardRuleResponse>> page(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "20") long pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String ruleType
    ) {
        return ApiResponse.success(rewardDomainService.pageRewardRules(pageNum, pageSize, keyword, status, ruleType));
    }

    @GetMapping("/{ruleId}")
    public ApiResponse<AdminRewardRuleResponse> get(@PathVariable Long ruleId) {
        return ApiResponse.success(rewardDomainService.getRewardRule(ruleId));
    }

    @PostMapping
    public ApiResponse<AdminRewardRuleResponse> create(@Valid @RequestBody AdminRewardRuleUpsertRequest request) {
        return ApiResponse.success(rewardDomainService.createRewardRule(request));
    }

    @PutMapping("/{ruleId}")
    public ApiResponse<AdminRewardRuleResponse> update(
            @PathVariable Long ruleId,
            @Valid @RequestBody AdminRewardRuleUpsertRequest request
    ) {
        return ApiResponse.success(rewardDomainService.updateRewardRule(ruleId, request));
    }

    @DeleteMapping("/{ruleId}")
    public ApiResponse<Boolean> delete(@PathVariable Long ruleId) {
        rewardDomainService.deleteRewardRule(ruleId);
        return ApiResponse.success(Boolean.TRUE);
    }
}
