package com.aoxiaoyou.admin.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AdminTranslationSettingsResponse {

    private String primaryAuthoringLocale;

    private List<String> enginePriority;

    private Boolean overwriteFilledLocales;

    private Boolean bridgeEnabled;

    private Integer requestTimeoutMs;

    private Integer maxTextLength;

    private String bridgeScriptPath;
}
