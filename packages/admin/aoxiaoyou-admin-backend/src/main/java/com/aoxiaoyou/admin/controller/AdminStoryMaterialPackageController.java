package com.aoxiaoyou.admin.controller;

import com.aoxiaoyou.admin.common.api.ApiResponse;
import com.aoxiaoyou.admin.common.api.PageResponse;
import com.aoxiaoyou.admin.dto.request.AdminStoryMaterialPackageRequest;
import com.aoxiaoyou.admin.dto.response.AdminStoryMaterialPackageResponse;
import com.aoxiaoyou.admin.service.AdminStoryMaterialPackageService;
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

@Tag(name = "後台故事素材包管理")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/v1/content/material-packages")
public class AdminStoryMaterialPackageController {

    private final AdminStoryMaterialPackageService storyMaterialPackageService;

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
}
