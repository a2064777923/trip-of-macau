package com.aoxiaoyou.tripofmacau;

import com.aoxiaoyou.tripofmacau.common.enums.LocaleCode;
import com.aoxiaoyou.tripofmacau.common.util.LocalizedContentSupport;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LocalizedContentSupportTest {

    private final LocalizedContentSupport localizedContentSupport = new LocalizedContentSupport(new ObjectMapper());

    @Test
    void resolvesPortugueseBeforeOtherFallbackLocales() {
        String resolved = localizedContentSupport.resolveText(
                "pt",
                "简体内容",
                "English content",
                "繁體內容",
                "Conteudo portugues"
        );

        assertThat(resolved).isEqualTo("Conteudo portugues");
    }

    @Test
    void fallsBackFromPortugueseToEnglishThenChineseDeterministically() {
        String resolved = localizedContentSupport.resolveText(
                "pt",
                "简体内容",
                "English content",
                "",
                ""
        );

        assertThat(resolved).isEqualTo("English content");
        assertThat(localizedContentSupport.localeFallbackOrder(LocaleCode.PT))
                .containsExactly(LocaleCode.PT, LocaleCode.EN, LocaleCode.ZH_HANT, LocaleCode.ZH_HANS);
    }
}
