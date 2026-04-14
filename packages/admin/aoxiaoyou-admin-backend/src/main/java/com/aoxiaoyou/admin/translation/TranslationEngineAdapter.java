package com.aoxiaoyou.admin.translation;

public interface TranslationEngineAdapter {

    TranslationAttemptResult translate(TranslationCommand command);
}
