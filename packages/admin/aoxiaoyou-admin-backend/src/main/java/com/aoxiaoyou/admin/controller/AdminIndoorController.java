package com.aoxiaoyou.admin.controller;

import com.aoxiaoyou.admin.common.api.ApiResponse;
import com.aoxiaoyou.admin.common.api.PageResponse;
import com.aoxiaoyou.admin.dto.request.AdminBuildingUpsertRequest;
import com.aoxiaoyou.admin.dto.response.BuildingResponse;
import com.aoxiaoyou.admin.service.AdminIndoorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/v1/map/indoor")
@RequiredArgsConstructor
public class AdminIndoorController {

    private final AdminIndoorService indoorService;

    @GetMapping("/buildings")
    public ApiResponse<PageResponse<BuildingResponse>> pageBuildings(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "20") long pageSize,
            @RequestParam(required = false) String cityCode) {
        return ApiResponse.success(indoorService.pageBuildings(pageNum, pageSize, cityCode));
    }

    @PostMapping("/buildings")
    public ApiResponse<BuildingResponse> createBuilding(@RequestBody AdminBuildingUpsertRequest request) {
        return ApiResponse.success(indoorService.createBuilding(request));
    }

    @PutMapping("/buildings/{id}")
    public ApiResponse<BuildingResponse> updateBuilding(@PathVariable Long id, @RequestBody AdminBuildingUpsertRequest request) {
        return ApiResponse.success(indoorService.updateBuilding(id, request));
    }
}
