package com.aoxiaoyou.admin.controller;

import com.aoxiaoyou.admin.common.api.ApiResponse;
import com.aoxiaoyou.admin.common.api.PageResponse;
import com.aoxiaoyou.admin.dto.request.AdminTestFlagRequest;
import com.aoxiaoyou.admin.dto.response.AdminUserDetailResponse;
import com.aoxiaoyou.admin.dto.response.AdminUserListItemResponse;
import com.aoxiaoyou.admin.service.AdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "后台用户管理")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/v1/users")
public class AdminUserController {

    private final AdminUserService adminUserService;

    @Operation(summary = "后台分页查询用户")
    @GetMapping
    public ApiResponse<PageResponse<AdminUserListItemResponse>> page(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean isTestAccount) {
        return ApiResponse.success(adminUserService.pageUsers(pageNum, pageSize, keyword, isTestAccount));
    }

    @Operation(summary = "后台查看用户详情")
    @GetMapping("/{userId}")
    public ApiResponse<AdminUserDetailResponse> detail(@PathVariable Long userId) {
        return ApiResponse.success(adminUserService.getUserDetail(userId));
    }

    @Operation(summary = "标记或取消测试账号")
    @PostMapping("/{userId}/test-flag")
    public ApiResponse<AdminUserListItemResponse> updateTestFlag(
            @PathVariable Long userId,
            @RequestBody AdminTestFlagRequest request,
            HttpServletRequest httpServletRequest) {
        Long operatorId = (Long) httpServletRequest.getAttribute("adminUserId");
        String operatorName = (String) httpServletRequest.getAttribute("adminUsername");
        return ApiResponse.success(adminUserService.updateTestFlag(userId, request, operatorId, operatorName, httpServletRequest.getRemoteAddr()));
    }
}
