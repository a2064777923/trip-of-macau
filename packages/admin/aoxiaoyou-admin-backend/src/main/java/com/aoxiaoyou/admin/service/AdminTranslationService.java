package com.aoxiaoyou.admin.service;

import com.aoxiaoyou.admin.dto.request.AdminTranslateRequest;
import com.aoxiaoyou.admin.dto.request.AdminTranslationSettingsUpsertRequest;
import com.aoxiaoyou.admin.dto.response.AdminTranslateResponse;
import com.aoxiaoyou.admin.dto.response.AdminTranslationSettingsResponse;

public interface AdminTranslationService {

    AdminTranslationSettingsResponse getSettings();

    AdminTranslationSettingsResponse updateSettings(AdminTranslationSettingsUpsertRequest request);

    AdminTranslateResponse translate(AdminTranslateRequest request);
}
