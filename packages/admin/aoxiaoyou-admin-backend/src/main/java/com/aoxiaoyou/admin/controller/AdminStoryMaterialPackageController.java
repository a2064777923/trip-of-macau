package com.aoxiaoyou.admin.controller;

import com.aoxiaoyou.admin.common.api.ApiResponse;
import com.aoxiaoyou.admin.common.api.PageResponse;
import com.aoxiaoyou.admin.dto.request.AdminStoryMaterialPackageRequest;
import com.aoxiaoyou.admin.dto.request.AdminStoryMaterialProductionRequest;
import com.aoxiaoyou.admin.dto.request.AdminStoryMaterialQaRequest;
import com.aoxiaoyou.admin.dto.response.AdminStoryMaterialPackageResponse;
import com.aoxiaoyou.admin.dto.response.AdminStoryMaterialProductionResponse;
import com.aoxiaoyou.admin.dto.response.AdminStoryMaterialQaResponse;
import com.aoxiaoyou.admin.service.AdminStoryMaterialPackageService;
import com.aoxiaoyou.admin.service.AdminStoryMaterialProductionService;
import com.aoxiaoyou.admin.service.AdminStoryMaterialQaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@Tag(name = "後台故事素材包管理")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/v1/content/material-packages")
public class AdminStoryMaterialPackageController {

    private final AdminStoryMaterialPackageService storyMaterialPackageService;
    private final AdminStoryMaterialProductionService storyMaterialProductionService;
    private final AdminStoryMaterialQaService storyMaterialQaService;

    @Operation(summary = "分頁查詢故事素材包")
    @GetMapping
    public ApiResponse<PageResponse<AdminStoryMaterialPackageResponse.PackageSummary>> page(
            @ModelAttribute AdminStoryMaterialPackageRequest.PackageQuery query) {
        return ApiResponse.success(storyMaterialPackageService.page(query));
    }

    @Operation(summary = "建立故事素材包")
    @PostMapping
    public ApiResponse<AdminStoryMaterialPackageResponse.PackageDetail> create(
            @Valid @RequestBody AdminStoryMaterialPackageRequest.PackageUpsert request,
            HttpServletRequest httpRequest) {
        return ApiResponse.success(storyMaterialPackageService.create(
                request,
                (Long) httpRequest.getAttribute("adminUserId"),
                (String) httpRequest.getAttribute("adminUsername")
        ));
    }

    @Operation(summary = "查詢故事素材包詳情")
    @GetMapping("/{packageId}")
    public ApiResponse<AdminStoryMaterialPackageResponse.PackageDetail> detail(@PathVariable Long packageId) {
        return ApiResponse.success(storyMaterialPackageService.detail(packageId));
    }

    @Operation(summary = "更新故事素材包")
    @PutMapping("/{packageId}")
    public ApiResponse<AdminStoryMaterialPackageResponse.PackageDetail> update(
            @PathVariable Long packageId,
            @Valid @RequestBody AdminStoryMaterialPackageRequest.PackageUpsert request) {
        return ApiResponse.success(storyMaterialPackageService.update(packageId, request));
    }

    @Operation(summary = "刪除故事素材包")
    @DeleteMapping("/{packageId}")
    public ApiResponse<Boolean> delete(@PathVariable Long packageId) {
        storyMaterialPackageService.delete(packageId);
        return ApiResponse.success(Boolean.TRUE);
    }

    @Operation(summary = "新增故事素材包項目")
    @PostMapping("/{packageId}/items")
    public ApiResponse<AdminStoryMaterialPackageResponse.PackageItem> addItem(
            @PathVariable Long packageId,
            @Valid @RequestBody AdminStoryMaterialPackageRequest.ItemUpsert request) {
        return ApiResponse.success(storyMaterialPackageService.addItem(packageId, request));
    }

    @Operation(summary = "更新故事素材包項目")
    @PutMapping("/{packageId}/items/{itemId}")
    public ApiResponse<AdminStoryMaterialPackageResponse.PackageItem> updateItem(
            @PathVariable Long packageId,
            @PathVariable Long itemId,
            @Valid @RequestBody AdminStoryMaterialPackageRequest.ItemUpsert request) {
        return ApiResponse.success(storyMaterialPackageService.updateItem(packageId, itemId, request));
    }

    @Operation(summary = "刪除故事素材包項目")
    @DeleteMapping("/{packageId}/items/{itemId}")
    public ApiResponse<Boolean> deleteItem(
            @PathVariable Long packageId,
            @PathVariable Long itemId) {
        storyMaterialPackageService.deleteItem(packageId, itemId);
        return ApiResponse.success(Boolean.TRUE);
    }

    @Operation(summary = "素材包生產前檢查")
    @PostMapping("/{packageId}/production/preflight")
    public ApiResponse<AdminStoryMaterialProductionResponse.PreflightResponse> preflightProduction(
            @PathVariable Long packageId,
            @RequestBody(required = false) AdminStoryMaterialProductionRequest.PreflightRequest request,
            HttpServletRequest httpRequest) {
        return ApiResponse.success(storyMaterialProductionService.preflightPackage(
                packageId,
                request == null ? new AdminStoryMaterialProductionRequest.PreflightRequest() : request,
                (Long) httpRequest.getAttribute("adminUserId"),
                (String) httpRequest.getAttribute("adminUsername"),
                readRoles(httpRequest)
        ));
    }

    @Operation(summary = "從本地素材包導入並上傳素材")
    @PostMapping("/{packageId}/items/{itemId}/production/import")
    public ApiResponse<AdminStoryMaterialProductionResponse.PackageItemVersionResponse> importLocalAsset(
            @PathVariable Long packageId,
            @PathVariable Long itemId,
            @Valid @RequestBody AdminStoryMaterialProductionRequest.LocalImportRequest request,
            HttpServletRequest httpRequest) {
        return ApiResponse.success(storyMaterialProductionService.importLocalAsset(
                packageId,
                itemId,
                request,
                (Long) httpRequest.getAttribute("adminUserId"),
                (String) httpRequest.getAttribute("adminUsername"),
                readRoles(httpRequest)
        ));
    }

    @Operation(summary = "綁定已定稿 AI 候選素材")
    @PostMapping("/{packageId}/items/{itemId}/production/bind-candidate")
    public ApiResponse<AdminStoryMaterialProductionResponse.PackageItemVersionResponse> bindFinalizedCandidate(
            @PathVariable Long packageId,
            @PathVariable Long itemId,
            @Valid @RequestBody AdminStoryMaterialProductionRequest.AiCandidateBindRequest request,
            HttpServletRequest httpRequest) {
        return ApiResponse.success(storyMaterialProductionService.bindFinalizedCandidate(
                packageId,
                itemId,
                request,
                (Long) httpRequest.getAttribute("adminUserId"),
                (String) httpRequest.getAttribute("adminUsername"),
                readRoles(httpRequest)
        ));
    }

    @Operation(summary = "審批或發布素材版本")
    @PostMapping("/{packageId}/items/{itemId}/production/promote")
    public ApiResponse<AdminStoryMaterialProductionResponse.PromotionResult> promoteItemVersion(
            @PathVariable Long packageId,
            @PathVariable Long itemId,
            @RequestBody(required = false) AdminStoryMaterialProductionRequest.PromoteRequest request,
            HttpServletRequest httpRequest) {
        return ApiResponse.success(storyMaterialProductionService.promoteItemVersion(
                packageId,
                itemId,
                request == null ? new AdminStoryMaterialProductionRequest.PromoteRequest() : request,
                (Long) httpRequest.getAttribute("adminUserId"),
                (String) httpRequest.getAttribute("adminUsername"),
                readRoles(httpRequest)
        ));
    }

    @Operation(summary = "回滾素材項目到歷史版本")
    @PostMapping("/{packageId}/items/{itemId}/production/rollback")
    public ApiResponse<AdminStoryMaterialProductionResponse.RollbackResult> rollbackItemVersion(
            @PathVariable Long packageId,
            @PathVariable Long itemId,
            @Valid @RequestBody AdminStoryMaterialProductionRequest.RollbackRequest request,
            HttpServletRequest httpRequest) {
        return ApiResponse.success(storyMaterialProductionService.rollbackItemVersion(
                packageId,
                itemId,
                request,
                (Long) httpRequest.getAttribute("adminUserId"),
                (String) httpRequest.getAttribute("adminUsername"),
                readRoles(httpRequest)
        ));
    }

    @Operation(summary = "查詢素材項目版本歷史")
    @GetMapping("/{packageId}/items/{itemId}/versions")
    public ApiResponse<List<AdminStoryMaterialProductionResponse.PackageItemVersionResponse>> listVersions(
            @PathVariable Long packageId,
            @PathVariable Long itemId,
            @ModelAttribute AdminStoryMaterialProductionRequest.VersionHistoryQuery query) {
        return ApiResponse.success(storyMaterialProductionService.listVersions(packageId, itemId, query));
    }

    @Operation(summary = "查詢故事素材包 QA 總覽")
    @GetMapping("/{packageId}/qa/overview")
    public ApiResponse<AdminStoryMaterialQaResponse.QaOverview> qaOverview(
            @PathVariable Long packageId,
            @ModelAttribute AdminStoryMaterialQaRequest.QaItemQuery query) {
        return ApiResponse.success(storyMaterialQaService.overview(packageId, query));
    }

    @Operation(summary = "分頁查詢故事素材 QA 項目")
    @GetMapping("/{packageId}/qa/items")
    public ApiResponse<PageResponse<AdminStoryMaterialQaResponse.QaItem>> qaItems(
            @PathVariable Long packageId,
            @ModelAttribute AdminStoryMaterialQaRequest.QaItemQuery query) {
        return ApiResponse.success(storyMaterialQaService.pageItems(packageId, query));
    }

    @Operation(summary = "查詢故事素材 QA 項目詳情")
    @GetMapping("/{packageId}/qa/items/{itemId}")
    public ApiResponse<AdminStoryMaterialQaResponse.QaDetail> qaItemDetail(
            @PathVariable Long packageId,
            @PathVariable Long itemId) {
        return ApiResponse.success(storyMaterialQaService.detail(packageId, itemId));
    }

    @Operation(summary = "拒絕故事素材版本")
    @PostMapping("/{packageId}/qa/items/{itemId}/reject")
    public ApiResponse<AdminStoryMaterialQaResponse.QaActionResult> rejectQaItem(
            @PathVariable Long packageId,
            @PathVariable Long itemId,
            @Valid @RequestBody AdminStoryMaterialQaRequest.QaActionRequest request,
            HttpServletRequest httpRequest) {
        return ApiResponse.success(storyMaterialQaService.reject(
                packageId,
                itemId,
                request,
                (Long) httpRequest.getAttribute("adminUserId"),
                (String) httpRequest.getAttribute("adminUsername"),
                readRoles(httpRequest)
        ));
    }

    @Operation(summary = "批准故事素材版本")
    @PostMapping("/{packageId}/qa/items/{itemId}/approve")
    public ApiResponse<AdminStoryMaterialQaResponse.QaActionResult> approveQaItem(
            @PathVariable Long packageId,
            @PathVariable Long itemId,
            @RequestBody(required = false) AdminStoryMaterialQaRequest.QaActionRequest request,
            HttpServletRequest httpRequest) {
        return ApiResponse.success(storyMaterialQaService.approve(
                packageId,
                itemId,
                request == null ? new AdminStoryMaterialQaRequest.QaActionRequest() : request,
                (Long) httpRequest.getAttribute("adminUserId"),
                (String) httpRequest.getAttribute("adminUsername"),
                readRoles(httpRequest)
        ));
    }

    @Operation(summary = "替換故事素材版本")
    @PostMapping("/{packageId}/qa/items/{itemId}/replace")
    public ApiResponse<AdminStoryMaterialQaResponse.QaActionResult> replaceQaItem(
            @PathVariable Long packageId,
            @PathVariable Long itemId,
            @Valid @RequestBody AdminStoryMaterialQaRequest.ReplaceRequest request,
            HttpServletRequest httpRequest) {
        return ApiResponse.success(storyMaterialQaService.replace(
                packageId,
                itemId,
                request,
                (Long) httpRequest.getAttribute("adminUserId"),
                (String) httpRequest.getAttribute("adminUsername"),
                readRoles(httpRequest)
        ));
    }

    @Operation(summary = "執行故事素材包一致性檢查")
    @PostMapping("/{packageId}/qa/consistency-check")
    public ApiResponse<AdminStoryMaterialQaResponse.ConsistencyReport> runQaConsistencyCheck(
            @PathVariable Long packageId,
            @RequestBody(required = false) AdminStoryMaterialQaRequest.ConsistencyCheckRequest request) {
        return ApiResponse.success(storyMaterialQaService.runConsistencyCheck(
                packageId,
                request == null ? new AdminStoryMaterialQaRequest.ConsistencyCheckRequest() : request
        ));
    }

    @SuppressWarnings("unchecked")
    private List<String> readRoles(HttpServletRequest request) {
        Object roles = request.getAttribute("adminRoles");
        return roles instanceof List<?> list ? (List<String>) list : Collections.emptyList();
    }
}
