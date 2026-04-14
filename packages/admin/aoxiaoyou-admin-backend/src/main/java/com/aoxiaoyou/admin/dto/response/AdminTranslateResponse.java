package com.aoxiaoyou.admin.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AdminTranslateResponse {

    private String sourceLocale;

    private List<String> targetLocales;

    private List<String> enginePriority;

    private Boolean overwriteFilledLocales;

    private List<LocaleResult> results;

    @Data
    @Builder
    public static class LocaleResult {
        private String targetLocale;
        private String status;
        private String translatedText;
        private String engine;
        private List<String> attemptedEngines;
        private String message;
    }
}
