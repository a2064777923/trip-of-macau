package com.aoxiaoyou.tripofmacau.controller;

import com.aoxiaoyou.tripofmacau.common.api.ApiResponse;
import com.aoxiaoyou.tripofmacau.common.api.PageResponse;
import com.aoxiaoyou.tripofmacau.entity.TestAccount;
import com.aoxiaoyou.tripofmacau.service.TestAccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "测试账号")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/test-accounts")
public class TestAccountController {
    
    private final TestAccountService testAccountService;
    
    @Operation(summary = "分页获取测试账号列表")
    @GetMapping
    public ApiResponse<PageResponse<TestAccount>> page(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") long page,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") long pageSize,
            @Parameter(description = "测试分组") @RequestParam(required = false) String testGroup) {
        return ApiResponse.success(testAccountService.pageTestAccounts(page, pageSize, testGroup));
    }
    
    @Operation(summary = "获取测试账号详情")
    @GetMapping("/{id}")
    public ApiResponse<TestAccount> detail(@PathVariable Long id) {
        return ApiResponse.success(testAccountService.getById(id));
    }
    
    @Operation(summary = "创建测试账号")
    @PostMapping
    public ApiResponse<TestAccount> create(@RequestBody TestAccount testAccount) {
        return ApiResponse.success(testAccountService.create(testAccount));
    }
    
    @Operation(summary = "更新测试账号")
    @PutMapping("/{id}")
    public ApiResponse<TestAccount> update(@PathVariable Long id, @RequestBody TestAccount testAccount) {
        return ApiResponse.success(testAccountService.update(id, testAccount));
    }
    
    @Operation(summary = "删除测试账号")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        testAccountService.delete(id);
        return ApiResponse.success(null);
    }
    
    @Operation(summary = "切换模拟定位状态")
    @PutMapping("/{id}/mock")
    public ApiResponse<TestAccount> toggleMock(
            @PathVariable Long id,
            @RequestBody Map<String, Object> params) {
        Boolean enabled = (Boolean) params.get("enabled");
        Double latitude = params.get("latitude") != null ? ((Number) params.get("latitude")).doubleValue() : null;
        Double longitude = params.get("longitude") != null ? ((Number) params.get("longitude")).doubleValue() : null;
        Long poiId = params.get("poiId") != null ? ((Number) params.get("poiId")).longValue() : null;
        
        return ApiResponse.success(testAccountService.toggleMockLocation(id, enabled, latitude, longitude, poiId));
    }
    
    @Operation(summary = "调整用户经验值")
    @PostMapping("/{userId}/adjust-exp")
    public ApiResponse<Void> adjustExp(
            @PathVariable Long userId,
            @RequestBody Map<String, Object> params) {
        int expDelta = ((Number) params.get("expDelta")).intValue();
        String reason = (String) params.get("reason");
        testAccountService.adjustUserExp(userId, expDelta, reason);
        return ApiResponse.success(null);
    }
    
    @Operation(summary = "重置用户印章")
    @PostMapping("/{userId}/reset-stamps")
    public ApiResponse<Void> resetStamps(@PathVariable Long userId) {
        testAccountService.resetUserStamps(userId);
        return ApiResponse.success(null);
    }
    
    @Operation(summary = "添加印章")
    @PostMapping("/{userId}/add-stamp")
    public ApiResponse<Void> addStamp(
            @PathVariable Long userId,
            @RequestBody Map<String, Object> params) {
        String stampType = (String) params.get("stampType");
        Long sourceId = params.get("sourceId") != null ? ((Number) params.get("sourceId")).longValue() : null;
        testAccountService.addUserStamp(userId, stampType, sourceId);
        return ApiResponse.success(null);
    }
}
