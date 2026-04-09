package com.aoxiaoyou.admin.controller;

import com.aoxiaoyou.admin.common.api.ApiResponse;
import com.aoxiaoyou.admin.common.api.PageResponse;
import com.aoxiaoyou.admin.dto.response.AdminActivityResponse;
import com.aoxiaoyou.admin.service.AdminOperationsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "后台运营管理")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/v1/operations")
public class AdminOperationsController {

    private final AdminOperationsService adminOperationsService;

    @Operation(summary = "分页查询活动")
    @GetMapping("/activities")
    public ApiResponse<PageResponse<AdminActivityResponse>> pageActivities(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {
        return ApiResponse.success(adminOperationsService.pageActivities(pageNum, pageSize, keyword, status));
    }
}
