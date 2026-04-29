package com.aoxiaoyou.admin.controller;

import com.aoxiaoyou.admin.common.api.ApiResponse;
import com.aoxiaoyou.admin.dto.request.AdminPoiExperienceRequest;
import com.aoxiaoyou.admin.dto.response.AdminExperienceResponse;
import com.aoxiaoyou.admin.dto.response.AdminPoiExperienceResponse;
import com.aoxiaoyou.admin.service.AdminPoiExperienceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "後台 POI 地點體驗")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/v1/pois/{poiId}/experience")
public class AdminPoiExperienceController {

    private final AdminPoiExperienceService adminPoiExperienceService;

    @Operation(summary = "讀取 POI 預設地點體驗")
    @GetMapping("/default")
    public ApiResponse<AdminPoiExperienceResponse.Snapshot> getDefault(@PathVariable Long poiId) {
        return ApiResponse.success(adminPoiExperienceService.getDefaultExperience(poiId));
    }

    @Operation(summary = "建立或更新 POI 預設體驗流程")
    @PutMapping("/default-flow")
    public ApiResponse<AdminPoiExperienceResponse.Snapshot> upsertDefaultFlow(
            @PathVariable Long poiId,
            @Valid @RequestBody AdminPoiExperienceRequest.FlowUpsert request) {
        return ApiResponse.success(adminPoiExperienceService.upsertDefaultFlow(poiId, request));
    }

    @Operation(summary = "建立 POI 體驗流程步驟")
    @PostMapping("/steps")
    public ApiResponse<AdminPoiExperienceResponse.Step> createStep(
            @PathVariable Long poiId,
            @Valid @RequestBody AdminPoiExperienceRequest.StepStructuredUpsert request) {
        return ApiResponse.success(adminPoiExperienceService.createStep(poiId, request));
    }

    @Operation(summary = "更新 POI 體驗流程步驟")
    @PutMapping("/steps/{stepId}")
    public ApiResponse<AdminPoiExperienceResponse.Step> updateStep(
            @PathVariable Long poiId,
            @PathVariable Long stepId,
            @Valid @RequestBody AdminPoiExperienceRequest.StepStructuredUpsert request) {
        return ApiResponse.success(adminPoiExperienceService.updateStep(poiId, stepId, request));
    }

    @Operation(summary = "刪除 POI 體驗流程步驟")
    @DeleteMapping("/steps/{stepId}")
    public ApiResponse<Boolean> deleteStep(@PathVariable Long poiId, @PathVariable Long stepId) {
        adminPoiExperienceService.deleteStep(poiId, stepId);
        return ApiResponse.success(Boolean.TRUE);
    }

    @Operation(summary = "把 POI 步驟保存為可重用模板")
    @PostMapping("/steps/{stepId}/save-template")
    public ApiResponse<AdminExperienceResponse.Template> saveStepAsTemplate(
            @PathVariable Long poiId,
            @PathVariable Long stepId,
            @Valid @RequestBody AdminPoiExperienceRequest.SaveTemplateRequest request) {
        return ApiResponse.success(adminPoiExperienceService.saveStepAsTemplate(poiId, stepId, request));
    }
}
