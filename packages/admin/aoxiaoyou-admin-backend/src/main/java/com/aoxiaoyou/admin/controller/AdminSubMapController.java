package com.aoxiaoyou.admin.controller;

import com.aoxiaoyou.admin.common.api.ApiResponse;
import com.aoxiaoyou.admin.common.api.PageResponse;
import com.aoxiaoyou.admin.dto.request.AdminStatusUpdateRequest;
import com.aoxiaoyou.admin.dto.request.AdminSubMapUpsertRequest;
import com.aoxiaoyou.admin.dto.response.AdminSubMapResponse;
import com.aoxiaoyou.admin.service.AdminSubMapService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/v1/map/sub-maps")
public class AdminSubMapController {

    private final AdminSubMapService adminSubMapService;

    @GetMapping
    public ApiResponse<PageResponse<AdminSubMapResponse>> page(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "20") long pageSize,
            @RequestParam(required = false) Long cityId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {
        return ApiResponse.success(adminSubMapService.pageSubMaps(pageNum, pageSize, cityId, keyword, status));
    }

    @GetMapping("/{id}")
    public ApiResponse<AdminSubMapResponse> detail(@PathVariable Long id) {
        return ApiResponse.success(adminSubMapService.getSubMapDetail(id));
    }

    @PostMapping
    public ApiResponse<AdminSubMapResponse> create(@RequestBody AdminSubMapUpsertRequest request) {
        return ApiResponse.success(adminSubMapService.createSubMap(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<AdminSubMapResponse> update(@PathVariable Long id, @RequestBody AdminSubMapUpsertRequest request) {
        return ApiResponse.success(adminSubMapService.updateSubMap(id, request));
    }

    @PutMapping("/{id}/publish")
    public ApiResponse<AdminSubMapResponse> publish(@PathVariable Long id) {
        return ApiResponse.success(adminSubMapService.publishSubMap(id));
    }

    @PutMapping("/{id}/status")
    public ApiResponse<AdminSubMapResponse> updateStatus(@PathVariable Long id,
                                                         @RequestBody AdminStatusUpdateRequest request) {
        return ApiResponse.success(adminSubMapService.updateSubMapStatus(id, request == null ? null : request.getStatus()));
    }
}
