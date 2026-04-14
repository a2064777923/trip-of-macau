package com.aoxiaoyou.admin.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class AdminTranslationSettingsUpsertRequest {

    private String primaryAuthoringLocale;

    private List<String> enginePriority;

    private Boolean overwriteFilledLocales;
}
