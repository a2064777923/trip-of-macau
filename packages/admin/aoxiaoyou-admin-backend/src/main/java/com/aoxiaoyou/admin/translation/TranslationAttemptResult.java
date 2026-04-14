package com.aoxiaoyou.admin.translation;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TranslationAttemptResult {

    private boolean success;

    private String translatedText;

    private String engine;

    private String message;
}
