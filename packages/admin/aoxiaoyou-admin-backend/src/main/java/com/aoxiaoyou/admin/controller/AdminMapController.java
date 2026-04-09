package com.aoxiaoyou.admin.controller;

import com.aoxiaoyou.admin.common.api.ApiResponse;
import com.aoxiaoyou.admin.common.api.PageResponse;
import com.aoxiaoyou.admin.dto.request.AdminCityUpsertRequest;
import com.aoxiaoyou.admin.dto.response.AdminCityResponse;
import com.aoxiaoyou.admin.service.AdminCityService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/v1/map")
@RequiredArgsConstructor
public class AdminMapController {

    private final AdminCityService cityService;

    @GetMapping("/cities")
    public ApiResponse<PageResponse<AdminCityResponse>> pageCities(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "20") long pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {
        return ApiResponse.success(cityService.pageCities(pageNum, pageSize, keyword, status));
    }

    @GetMapping("/cities/{id}")
    public ApiResponse<AdminCityResponse> getCity(@PathVariable Long id) {
        return ApiResponse.success(cityService.getCityDetail(id));
    }

    @PostMapping("/cities")
    public ApiResponse<AdminCityResponse> createCity(@RequestBody AdminCityUpsertRequest request) {
        return ApiResponse.success(cityService.createCity(request));
    }

    @PutMapping("/cities/{id}")
    public ApiResponse<AdminCityResponse> updateCity(@PathVariable Long id, @RequestBody AdminCityUpsertRequest request) {
        return ApiResponse.success(cityService.updateCity(id, request));
    }

    @PutMapping("/cities/{id}/publish")
    public ApiResponse<AdminCityResponse> publishCity(@PathVariable Long id) {
        return ApiResponse.success(cityService.publishCity(id));
    }
}
