package com.aoxiaoyou.admin.translation;

import com.aoxiaoyou.admin.common.enums.LocaleCode;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TranslationCommand {

    private String engine;

    private LocaleCode sourceLocale;

    private LocaleCode targetLocale;

    private String text;

    private int timeoutMs;
}
