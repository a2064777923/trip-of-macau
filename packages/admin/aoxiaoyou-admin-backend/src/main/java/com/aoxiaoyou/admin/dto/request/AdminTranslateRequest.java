package com.aoxiaoyou.admin.dto.request;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class AdminTranslateRequest {

    private String sourceLocale;

    private List<String> targetLocales;

    private String text;

    private List<String> enginePriority;

    private Boolean overwriteFilledLocales;

    private Map<String, String> existingTranslations;
}
