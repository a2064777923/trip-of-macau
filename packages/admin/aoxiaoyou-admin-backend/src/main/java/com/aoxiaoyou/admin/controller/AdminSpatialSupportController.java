package com.aoxiaoyou.admin.controller;

import com.aoxiaoyou.admin.common.api.ApiResponse;
import com.aoxiaoyou.admin.dto.request.AdminCoordinatePreviewRequest;
import com.aoxiaoyou.admin.dto.request.AdminSpatialMetadataSuggestionRequest;
import com.aoxiaoyou.admin.dto.response.AdminCoordinatePreviewResponse;
import com.aoxiaoyou.admin.dto.response.AdminSpatialMetadataSuggestionResponse;
import com.aoxiaoyou.admin.service.AdminSpatialMetadataService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/v1/map/spatial")
public class AdminSpatialSupportController {

    private final AdminSpatialMetadataService adminSpatialMetadataService;

    @PostMapping("/coordinate-preview")
    public ApiResponse<AdminCoordinatePreviewResponse> previewCoordinate(@RequestBody AdminCoordinatePreviewRequest request) {
        return ApiResponse.success(adminSpatialMetadataService.previewCoordinate(request));
    }

    @PostMapping("/metadata/suggest")
    public ApiResponse<AdminSpatialMetadataSuggestionResponse> suggestMetadata(@RequestBody AdminSpatialMetadataSuggestionRequest request) {
        return ApiResponse.success(adminSpatialMetadataService.suggestMetadata(request));
    }
}
