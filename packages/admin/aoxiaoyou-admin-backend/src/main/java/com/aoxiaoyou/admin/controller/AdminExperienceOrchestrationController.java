package com.aoxiaoyou.admin.controller;

import com.aoxiaoyou.admin.common.api.ApiResponse;
import com.aoxiaoyou.admin.common.api.PageResponse;
import com.aoxiaoyou.admin.dto.request.AdminExperienceRequest;
import com.aoxiaoyou.admin.dto.response.AdminExperienceResponse;
import com.aoxiaoyou.admin.service.AdminExperienceOrchestrationService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "後台體驗編排系統")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/v1/experience")
public class AdminExperienceOrchestrationController {

    private final AdminExperienceOrchestrationService experienceOrchestrationService;

    @Operation(summary = "分頁查詢互動與任務模板")
    @GetMapping("/templates")
    public ApiResponse<PageResponse<AdminExperienceResponse.Template>> pageTemplates(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "20") long pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String templateType,
            @RequestParam(required = false) String status) {
        return ApiResponse.success(experienceOrchestrationService.pageTemplates(pageNum, pageSize, keyword, templateType, status));
    }

    @Operation(summary = "建立互動與任務模板")
    @PostMapping("/templates")
    public ApiResponse<AdminExperienceResponse.Template> createTemplate(
            @Valid @RequestBody AdminExperienceRequest.TemplateUpsert request) {
        return ApiResponse.success(experienceOrchestrationService.createTemplate(request));
    }

    @Operation(summary = "更新互動與任務模板")
    @PutMapping("/templates/{templateId}")
    public ApiResponse<AdminExperienceResponse.Template> updateTemplate(
            @PathVariable Long templateId,
            @Valid @RequestBody AdminExperienceRequest.TemplateUpsert request) {
        return ApiResponse.success(experienceOrchestrationService.updateTemplate(templateId, request));
    }

    @Operation(summary = "刪除互動與任務模板")
    @DeleteMapping("/templates/{templateId}")
    public ApiResponse<Boolean> deleteTemplate(@PathVariable Long templateId) {
        experienceOrchestrationService.deleteTemplate(templateId);
        return ApiResponse.success(Boolean.TRUE);
    }

    @Operation(summary = "分頁查詢體驗流程")
    @GetMapping("/flows")
    public ApiResponse<PageResponse<AdminExperienceResponse.Flow>> pageFlows(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "20") long pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String flowType,
            @RequestParam(required = false) String status) {
        return ApiResponse.success(experienceOrchestrationService.pageFlows(pageNum, pageSize, keyword, flowType, status));
    }

    @Operation(summary = "查詢體驗流程詳情")
    @GetMapping("/flows/{flowId}")
    public ApiResponse<AdminExperienceResponse.Flow> getFlow(@PathVariable Long flowId) {
        return ApiResponse.success(experienceOrchestrationService.getFlow(flowId));
    }

    @Operation(summary = "建立體驗流程")
    @PostMapping("/flows")
    public ApiResponse<AdminExperienceResponse.Flow> createFlow(
            @Valid @RequestBody AdminExperienceRequest.FlowUpsert request) {
        return ApiResponse.success(experienceOrchestrationService.createFlow(request));
    }

    @Operation(summary = "更新體驗流程")
    @PutMapping("/flows/{flowId}")
    public ApiResponse<AdminExperienceResponse.Flow> updateFlow(
            @PathVariable Long flowId,
            @Valid @RequestBody AdminExperienceRequest.FlowUpsert request) {
        return ApiResponse.success(experienceOrchestrationService.updateFlow(flowId, request));
    }

    @Operation(summary = "刪除體驗流程")
    @DeleteMapping("/flows/{flowId}")
    public ApiResponse<Boolean> deleteFlow(@PathVariable Long flowId) {
        experienceOrchestrationService.deleteFlow(flowId);
        return ApiResponse.success(Boolean.TRUE);
    }

    @Operation(summary = "建立流程步驟")
    @PostMapping("/flows/{flowId}/steps")
    public ApiResponse<AdminExperienceResponse.Step> createStep(
            @PathVariable Long flowId,
            @Valid @RequestBody AdminExperienceRequest.StepUpsert request) {
        return ApiResponse.success(experienceOrchestrationService.createStep(flowId, request));
    }

    @Operation(summary = "更新流程步驟")
    @PutMapping("/flows/{flowId}/steps/{stepId}")
    public ApiResponse<AdminExperienceResponse.Step> updateStep(
            @PathVariable Long flowId,
            @PathVariable Long stepId,
            @Valid @RequestBody AdminExperienceRequest.StepUpsert request) {
        return ApiResponse.success(experienceOrchestrationService.updateStep(flowId, stepId, request));
    }

    @Operation(summary = "刪除流程步驟")
    @DeleteMapping("/flows/{flowId}/steps/{stepId}")
    public ApiResponse<Boolean> deleteStep(@PathVariable Long flowId, @PathVariable Long stepId) {
        experienceOrchestrationService.deleteStep(flowId, stepId);
        return ApiResponse.success(Boolean.TRUE);
    }

    @Operation(summary = "查詢體驗流程綁定")
    @GetMapping("/bindings")
    public ApiResponse<PageResponse<AdminExperienceResponse.Binding>> pageBindings(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "20") long pageSize,
            @RequestParam(required = false) String ownerType,
            @RequestParam(required = false) Long ownerId,
            @RequestParam(required = false) String ownerCode) {
        return ApiResponse.success(experienceOrchestrationService.pageBindings(pageNum, pageSize, ownerType, ownerId, ownerCode));
    }

    @Operation(summary = "建立體驗流程綁定")
    @PostMapping("/bindings")
    public ApiResponse<AdminExperienceResponse.Binding> createBinding(
            @Valid @RequestBody AdminExperienceRequest.BindingUpsert request) {
        return ApiResponse.success(experienceOrchestrationService.createBinding(request));
    }

    @Operation(summary = "更新體驗流程綁定")
    @PutMapping("/bindings/{bindingId}")
    public ApiResponse<AdminExperienceResponse.Binding> updateBinding(
            @PathVariable Long bindingId,
            @Valid @RequestBody AdminExperienceRequest.BindingUpsert request) {
        return ApiResponse.success(experienceOrchestrationService.updateBinding(bindingId, request));
    }

    @Operation(summary = "刪除體驗流程綁定")
    @DeleteMapping("/bindings/{bindingId}")
    public ApiResponse<Boolean> deleteBinding(@PathVariable Long bindingId) {
        experienceOrchestrationService.deleteBinding(bindingId);
        return ApiResponse.success(Boolean.TRUE);
    }

    @Operation(summary = "查詢故事或地點覆寫規則")
    @GetMapping("/overrides")
    public ApiResponse<PageResponse<AdminExperienceResponse.OverrideRule>> pageOverrides(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "20") long pageSize,
            @RequestParam(required = false) String ownerType,
            @RequestParam(required = false) Long ownerId) {
        return ApiResponse.success(experienceOrchestrationService.pageOverrides(pageNum, pageSize, ownerType, ownerId));
    }

    @Operation(summary = "建立故事或地點覆寫規則")
    @PostMapping("/overrides")
    public ApiResponse<AdminExperienceResponse.OverrideRule> createOverride(
            @Valid @RequestBody AdminExperienceRequest.OverrideUpsert request) {
        return ApiResponse.success(experienceOrchestrationService.createOverride(request));
    }

    @Operation(summary = "更新故事或地點覆寫規則")
    @PutMapping("/overrides/{overrideId}")
    public ApiResponse<AdminExperienceResponse.OverrideRule> updateOverride(
            @PathVariable Long overrideId,
            @Valid @RequestBody AdminExperienceRequest.OverrideUpsert request) {
        return ApiResponse.success(experienceOrchestrationService.updateOverride(overrideId, request));
    }

    @Operation(summary = "刪除故事或地點覆寫規則")
    @DeleteMapping("/overrides/{overrideId}")
    public ApiResponse<Boolean> deleteOverride(@PathVariable Long overrideId) {
        experienceOrchestrationService.deleteOverride(overrideId);
        return ApiResponse.success(Boolean.TRUE);
    }

    @Operation(summary = "分頁查詢探索元素")
    @GetMapping("/exploration-elements")
    public ApiResponse<PageResponse<AdminExperienceResponse.ExplorationElement>> pageExplorationElements(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "20") long pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String ownerType,
            @RequestParam(required = false) Long ownerId,
            @RequestParam(required = false) Long cityId,
            @RequestParam(required = false) Long subMapId,
            @RequestParam(required = false) Long storylineId,
            @RequestParam(required = false) String status) {
        return ApiResponse.success(experienceOrchestrationService.pageExplorationElements(
                pageNum, pageSize, keyword, ownerType, ownerId, cityId, subMapId, storylineId, status));
    }

    @Operation(summary = "建立探索元素")
    @PostMapping("/exploration-elements")
    public ApiResponse<AdminExperienceResponse.ExplorationElement> createExplorationElement(
            @Valid @RequestBody AdminExperienceRequest.ExplorationElementUpsert request) {
        return ApiResponse.success(experienceOrchestrationService.createExplorationElement(request));
    }

    @Operation(summary = "更新探索元素")
    @PutMapping("/exploration-elements/{elementId}")
    public ApiResponse<AdminExperienceResponse.ExplorationElement> updateExplorationElement(
            @PathVariable Long elementId,
            @Valid @RequestBody AdminExperienceRequest.ExplorationElementUpsert request) {
        return ApiResponse.success(experienceOrchestrationService.updateExplorationElement(elementId, request));
    }

    @Operation(summary = "刪除探索元素")
    @DeleteMapping("/exploration-elements/{elementId}")
    public ApiResponse<Boolean> deleteExplorationElement(@PathVariable Long elementId) {
        experienceOrchestrationService.deleteExplorationElement(elementId);
        return ApiResponse.success(Boolean.TRUE);
    }

    @Operation(summary = "體驗規則治理總覽")
    @GetMapping("/governance/overview")
    public ApiResponse<AdminExperienceResponse.GovernanceOverview> getGovernanceOverview() {
        return ApiResponse.success(experienceOrchestrationService.getGovernanceOverview());
    }
}
