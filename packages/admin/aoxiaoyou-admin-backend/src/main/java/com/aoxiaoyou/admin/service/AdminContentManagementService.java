package com.aoxiaoyou.admin.service;

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

public interface AdminContentManagementService {

    PageResponse<AdminRuntimeSettingResponse> pageRuntimeSettings(long pageNum, long pageSize, String settingGroup, String status, String keyword);

    AdminRuntimeSettingResponse createRuntimeSetting(AdminRuntimeSettingUpsertRequest request);

    AdminRuntimeSettingResponse updateRuntimeSetting(Long id, AdminRuntimeSettingUpsertRequest request);

    void deleteRuntimeSetting(Long id);

    PageResponse<AdminContentAssetResponse> pageAssets(
            long pageNum,
            long pageSize,
            String assetKind,
            String status,
            String uploadSource,
            String processingPolicyCode,
            String processingStatus,
            String keyword);

    AdminContentAssetResponse uploadAsset(AdminContentAssetUploadRequest request, Long adminUserId, String adminUsername);

    AdminContentAssetBatchUploadResponse batchUploadAssets(AdminContentAssetBatchUploadRequest request, Long adminUserId, String adminUsername);

    AdminContentAssetResponse createAsset(AdminContentAssetUpsertRequest request);

    AdminContentAssetResponse updateAsset(Long id, AdminContentAssetUpsertRequest request);

    AdminContentAssetUsageSummaryResponse getAssetUsages(Long id);

    void deleteAsset(Long id);

    PageResponse<AdminTipArticleResponse> pageTips(long pageNum, long pageSize, Long cityId, String status, String keyword);

    AdminTipArticleResponse createTip(AdminTipArticleUpsertRequest request);

    AdminTipArticleResponse updateTip(Long id, AdminTipArticleUpsertRequest request);

    void deleteTip(Long id);

    PageResponse<AdminNotificationResponse> pageNotifications(long pageNum, long pageSize, String status, String keyword);

    AdminNotificationResponse createNotification(AdminNotificationUpsertRequest request);

    AdminNotificationResponse updateNotification(Long id, AdminNotificationUpsertRequest request);

    void deleteNotification(Long id);

    PageResponse<AdminStampResponse> pageStamps(long pageNum, long pageSize, String status, String keyword);

    AdminStampResponse createStamp(AdminStampUpsertRequest request);

    AdminStampResponse updateStamp(Long id, AdminStampUpsertRequest request);

    void deleteStamp(Long id);
}
