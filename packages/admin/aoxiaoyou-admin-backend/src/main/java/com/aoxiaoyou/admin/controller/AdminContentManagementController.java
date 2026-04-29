package com.aoxiaoyou.admin.controller;

import com.aoxiaoyou.admin.common.api.ApiResponse;
import com.aoxiaoyou.admin.common.api.PageResponse;
import com.aoxiaoyou.admin.dto.request.AdminContentAssetBatchUploadRequest;
import com.aoxiaoyou.admin.dto.request.AdminContentAssetUploadRequest;
import com.aoxiaoyou.admin.dto.request.AdminContentAssetUpsertRequest;
import com.aoxiaoyou.admin.dto.request.AdminNotificationUpsertRequest;
import com.aoxiaoyou.admin.dto.request.AdminRuntimeSettingUpsertRequest;
import com.aoxiaoyou.admin.dto.request.AdminStampUpsertRequest;
import com.aoxiaoyou.admin.dto.request.AdminTipArticleUpsertRequest;
import com.aoxiaoyou.admin.dto.response.AdminContentAssetBatchUploadResponse;
import com.aoxiaoyou.admin.dto.response.AdminContentAssetResponse;
import com.aoxiaoyou.admin.dto.response.AdminContentAssetUsageSummaryResponse;
import com.aoxiaoyou.admin.dto.response.AdminNotificationResponse;
import com.aoxiaoyou.admin.dto.response.AdminRuntimeSettingResponse;
import com.aoxiaoyou.admin.dto.response.AdminStampResponse;
import com.aoxiaoyou.admin.dto.response.AdminTipArticleResponse;
import com.aoxiaoyou.admin.service.AdminContentManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/v1/content")
public class AdminContentManagementController {

    private final AdminContentManagementService adminContentManagementService;

    @GetMapping("/runtime-settings")
    public ApiResponse<PageResponse<AdminRuntimeSettingResponse>> pageRuntimeSettings(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "20") long pageSize,
            @RequestParam(required = false) String settingGroup,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        return ApiResponse.success(adminContentManagementService.pageRuntimeSettings(pageNum, pageSize, settingGroup, status, keyword));
    }

    @PostMapping("/runtime-settings")
    public ApiResponse<AdminRuntimeSettingResponse> createRuntimeSetting(@Valid @RequestBody AdminRuntimeSettingUpsertRequest request) {
        return ApiResponse.success(adminContentManagementService.createRuntimeSetting(request));
    }

    @PutMapping("/runtime-settings/{id}")
    public ApiResponse<AdminRuntimeSettingResponse> updateRuntimeSetting(@PathVariable Long id, @Valid @RequestBody AdminRuntimeSettingUpsertRequest request) {
        return ApiResponse.success(adminContentManagementService.updateRuntimeSetting(id, request));
    }

    @DeleteMapping("/runtime-settings/{id}")
    public ApiResponse<Boolean> deleteRuntimeSetting(@PathVariable Long id) {
        adminContentManagementService.deleteRuntimeSetting(id);
        return ApiResponse.success(Boolean.TRUE);
    }

    @GetMapping("/assets")
    public ApiResponse<PageResponse<AdminContentAssetResponse>> pageAssets(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "20") long pageSize,
            @RequestParam(required = false) String assetKind,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String uploadSource,
            @RequestParam(required = false) String processingPolicyCode,
            @RequestParam(required = false) String processingStatus,
            @RequestParam(required = false) String keyword) {
        return ApiResponse.success(adminContentManagementService.pageAssets(
                pageNum,
                pageSize,
                assetKind,
                status,
                uploadSource,
                processingPolicyCode,
                processingStatus,
                keyword
        ));
    }

    @PostMapping(value = "/assets/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<AdminContentAssetResponse> uploadAsset(@Valid @ModelAttribute AdminContentAssetUploadRequest request,
                                                              jakarta.servlet.http.HttpServletRequest httpRequest) {
        return ApiResponse.success(adminContentManagementService.uploadAsset(
                request,
                (Long) httpRequest.getAttribute("adminUserId"),
                (String) httpRequest.getAttribute("adminUsername")
        ));
    }

    @PostMapping(value = "/assets/batch-upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<AdminContentAssetBatchUploadResponse> batchUploadAssets(@Valid @ModelAttribute AdminContentAssetBatchUploadRequest request,
                                                                               jakarta.servlet.http.HttpServletRequest httpRequest) {
        return ApiResponse.success(adminContentManagementService.batchUploadAssets(
                request,
                (Long) httpRequest.getAttribute("adminUserId"),
                (String) httpRequest.getAttribute("adminUsername")
        ));
    }

    @PostMapping("/assets")
    public ApiResponse<AdminContentAssetResponse> createAsset(@Valid @RequestBody AdminContentAssetUpsertRequest request) {
        return ApiResponse.success(adminContentManagementService.createAsset(request));
    }

    @PutMapping("/assets/{id}")
    public ApiResponse<AdminContentAssetResponse> updateAsset(@PathVariable Long id, @Valid @RequestBody AdminContentAssetUpsertRequest request) {
        return ApiResponse.success(adminContentManagementService.updateAsset(id, request));
    }

    @GetMapping("/assets/{id}/usages")
    public ApiResponse<AdminContentAssetUsageSummaryResponse> getAssetUsages(@PathVariable Long id) {
        return ApiResponse.success(adminContentManagementService.getAssetUsages(id));
    }

    @DeleteMapping("/assets/{id}")
    public ApiResponse<Boolean> deleteAsset(@PathVariable Long id) {
        adminContentManagementService.deleteAsset(id);
        return ApiResponse.success(Boolean.TRUE);
    }

    @GetMapping("/tips")
    public ApiResponse<PageResponse<AdminTipArticleResponse>> pageTips(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "20") long pageSize,
            @RequestParam(required = false) Long cityId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        return ApiResponse.success(adminContentManagementService.pageTips(pageNum, pageSize, cityId, status, keyword));
    }

    @PostMapping("/tips")
    public ApiResponse<AdminTipArticleResponse> createTip(@Valid @RequestBody AdminTipArticleUpsertRequest request) {
        return ApiResponse.success(adminContentManagementService.createTip(request));
    }

    @PutMapping("/tips/{id}")
    public ApiResponse<AdminTipArticleResponse> updateTip(@PathVariable Long id, @Valid @RequestBody AdminTipArticleUpsertRequest request) {
        return ApiResponse.success(adminContentManagementService.updateTip(id, request));
    }

    @DeleteMapping("/tips/{id}")
    public ApiResponse<Boolean> deleteTip(@PathVariable Long id) {
        adminContentManagementService.deleteTip(id);
        return ApiResponse.success(Boolean.TRUE);
    }

    @GetMapping("/notifications")
    public ApiResponse<PageResponse<AdminNotificationResponse>> pageNotifications(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "20") long pageSize,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        return ApiResponse.success(adminContentManagementService.pageNotifications(pageNum, pageSize, status, keyword));
    }

    @PostMapping("/notifications")
    public ApiResponse<AdminNotificationResponse> createNotification(@Valid @RequestBody AdminNotificationUpsertRequest request) {
        return ApiResponse.success(adminContentManagementService.createNotification(request));
    }

    @PutMapping("/notifications/{id}")
    public ApiResponse<AdminNotificationResponse> updateNotification(@PathVariable Long id, @Valid @RequestBody AdminNotificationUpsertRequest request) {
        return ApiResponse.success(adminContentManagementService.updateNotification(id, request));
    }

    @DeleteMapping("/notifications/{id}")
    public ApiResponse<Boolean> deleteNotification(@PathVariable Long id) {
        adminContentManagementService.deleteNotification(id);
        return ApiResponse.success(Boolean.TRUE);
    }

    @GetMapping("/stamps")
    public ApiResponse<PageResponse<AdminStampResponse>> pageStamps(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "20") long pageSize,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        return ApiResponse.success(adminContentManagementService.pageStamps(pageNum, pageSize, status, keyword));
    }

    @PostMapping("/stamps")
    public ApiResponse<AdminStampResponse> createStamp(@Valid @RequestBody AdminStampUpsertRequest request) {
        return ApiResponse.success(adminContentManagementService.createStamp(request));
    }

    @PutMapping("/stamps/{id}")
    public ApiResponse<AdminStampResponse> updateStamp(@PathVariable Long id, @Valid @RequestBody AdminStampUpsertRequest request) {
        return ApiResponse.success(adminContentManagementService.updateStamp(id, request));
    }

    @DeleteMapping("/stamps/{id}")
    public ApiResponse<Boolean> deleteStamp(@PathVariable Long id) {
        adminContentManagementService.deleteStamp(id);
        return ApiResponse.success(Boolean.TRUE);
    }
}
