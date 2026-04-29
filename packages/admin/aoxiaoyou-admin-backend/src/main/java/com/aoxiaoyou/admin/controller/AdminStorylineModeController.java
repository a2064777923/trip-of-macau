package com.aoxiaoyou.admin.controller;

import com.aoxiaoyou.admin.common.api.ApiResponse;
import com.aoxiaoyou.admin.dto.request.AdminStorylineModeRequest;
import com.aoxiaoyou.admin.dto.response.AdminStorylineModeResponse;
import com.aoxiaoyou.admin.service.AdminStorylineModeService;
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

@Tag(name = "後台故事路線與章節覆寫工作台")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/v1/storylines/{storylineId}/mode-workbench")
public class AdminStorylineModeController {

    private final AdminStorylineModeService adminStorylineModeService;

    @Operation(summary = "讀取故事模式工作台快照")
    @GetMapping
    public ApiResponse<AdminStorylineModeResponse.Snapshot> getSnapshot(@PathVariable Long storylineId) {
        return ApiResponse.success(adminStorylineModeService.getSnapshot(storylineId));
    }

    @Operation(summary = "更新故事模式地圖與支線策略")
    @PutMapping("/mode-config")
    public ApiResponse<AdminStorylineModeResponse.Snapshot> updateModeConfig(
            @PathVariable Long storylineId,
            @Valid @RequestBody AdminStorylineModeRequest.StoryModeConfigUpsert request) {
        return ApiResponse.success(adminStorylineModeService.updateModeConfig(storylineId, request));
    }

    @Operation(summary = "更新章節錨點")
    @PutMapping("/chapters/{chapterId}/anchor")
    public ApiResponse<AdminStorylineModeResponse.Snapshot> updateChapterAnchor(
            @PathVariable Long storylineId,
            @PathVariable Long chapterId,
            @Valid @RequestBody AdminStorylineModeRequest.ChapterAnchorUpsert request) {
        return ApiResponse.success(adminStorylineModeService.updateChapterAnchor(storylineId, chapterId, request));
    }

    @Operation(summary = "更新章節覆寫策略")
    @PutMapping("/chapters/{chapterId}/overrides")
    public ApiResponse<AdminStorylineModeResponse.Snapshot> updateChapterOverridePolicy(
            @PathVariable Long storylineId,
            @PathVariable Long chapterId,
            @Valid @RequestBody AdminStorylineModeRequest.ChapterOverridePolicyUpsert request) {
        return ApiResponse.success(adminStorylineModeService.updateChapterOverridePolicy(storylineId, chapterId, request));
    }

    @Operation(summary = "新增章節覆寫步驟")
    @PostMapping("/chapters/{chapterId}/override-steps")
    public ApiResponse<AdminStorylineModeResponse.OverrideRule> createOverrideStep(
            @PathVariable Long storylineId,
            @PathVariable Long chapterId,
            @Valid @RequestBody AdminStorylineModeRequest.OverrideStepUpsert request) {
        return ApiResponse.success(adminStorylineModeService.createOverrideStep(storylineId, chapterId, request));
    }

    @Operation(summary = "更新章節覆寫步驟")
    @PutMapping("/chapters/{chapterId}/override-steps/{overrideId}")
    public ApiResponse<AdminStorylineModeResponse.OverrideRule> updateOverrideStep(
            @PathVariable Long storylineId,
            @PathVariable Long chapterId,
            @PathVariable Long overrideId,
            @Valid @RequestBody AdminStorylineModeRequest.OverrideStepUpsert request) {
        return ApiResponse.success(adminStorylineModeService.updateOverrideStep(storylineId, chapterId, overrideId, request));
    }

    @Operation(summary = "刪除章節覆寫步驟")
    @DeleteMapping("/chapters/{chapterId}/override-steps/{overrideId}")
    public ApiResponse<Boolean> deleteOverrideStep(
            @PathVariable Long storylineId,
            @PathVariable Long chapterId,
            @PathVariable Long overrideId) {
        adminStorylineModeService.deleteOverrideStep(storylineId, chapterId, overrideId);
        return ApiResponse.success(Boolean.TRUE);
    }

    @Operation(summary = "預覽公開故事 runtime")
    @GetMapping("/runtime-preview")
    public ApiResponse<AdminStorylineModeResponse.RuntimePreview> runtimePreview(@PathVariable Long storylineId) {
        return ApiResponse.success(adminStorylineModeService.runtimePreview(storylineId));
    }
}
