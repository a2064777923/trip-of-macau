package com.aoxiaoyou.admin;

import com.aoxiaoyou.admin.dto.request.AdminTranslateRequest;
import com.aoxiaoyou.admin.dto.response.AdminTranslateResponse;
import com.aoxiaoyou.admin.dto.response.AdminTranslationSettingsResponse;
import com.aoxiaoyou.admin.entity.SysConfig;
import com.aoxiaoyou.admin.mapper.SysConfigMapper;
import com.aoxiaoyou.admin.service.impl.AdminTranslationServiceImpl;
import com.aoxiaoyou.admin.translation.TranslationAttemptResult;
import com.aoxiaoyou.admin.translation.TranslationEngineAdapter;
import com.aoxiaoyou.admin.translation.TranslationProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminTranslationServiceImplTest {

    @Mock
    private SysConfigMapper sysConfigMapper;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private TranslationEngineAdapter translationEngineAdapter;

    private AdminTranslationServiceImpl service;

    @BeforeEach
    void setUp() {
        TranslationProperties translationProperties = new TranslationProperties();
        translationProperties.setBridgeEnabled(true);
        translationProperties.setRequestTimeoutMs(9000);
        translationProperties.setMaxTextLength(5000);
        translationProperties.setDefaultEnginePriority("bing,google");

        service = new AdminTranslationServiceImpl(
                sysConfigMapper,
                jdbcTemplate,
                new ObjectMapper(),
                translationEngineAdapter,
                translationProperties
        );
    }

    @Test
    void getSettingsParsesStoredPortuguesePrimaryLocaleAndPriority() {
        when(sysConfigMapper.selectList(any())).thenReturn(List.of(
                sysConfig("translation.primary_authoring_locale", "pt"),
                sysConfig("translation.engine_priority", "[\"google\",\"bing\"]"),
                sysConfig("translation.overwrite_filled_locales", "true")
        ));

        AdminTranslationSettingsResponse settings = service.getSettings();

        assertThat(settings.getPrimaryAuthoringLocale()).isEqualTo("pt");
        assertThat(settings.getEnginePriority()).containsExactly("google", "bing");
        assertThat(settings.getOverwriteFilledLocales()).isTrue();
        assertThat(settings.getBridgeEnabled()).isTrue();
        assertThat(settings.getRequestTimeoutMs()).isEqualTo(9000);
    }

    @Test
    void translateFallsBackToNextEngineWhenTheFirstOneFails() {
        when(sysConfigMapper.selectList(any())).thenReturn(List.of(
                sysConfig("translation.primary_authoring_locale", "zh-Hant"),
                sysConfig("translation.engine_priority", "[\"bing\",\"google\"]"),
                sysConfig("translation.overwrite_filled_locales", "false")
        ));
        when(translationEngineAdapter.translate(any()))
                .thenReturn(TranslationAttemptResult.builder()
                        .success(false)
                        .engine("bing")
                        .message("timeout")
                        .build())
                .thenReturn(TranslationAttemptResult.builder()
                        .success(true)
                        .engine("google")
                        .translatedText("Hello Macau")
                        .message("ok")
                        .build());

        AdminTranslateRequest request = new AdminTranslateRequest();
        request.setSourceLocale("zh-Hant");
        request.setTargetLocales(List.of("en"));
        request.setText("你好澳門");

        AdminTranslateResponse response = service.translate(request);

        assertThat(response.getResults()).hasSize(1);
        assertThat(response.getResults().get(0).getStatus()).isEqualTo("success");
        assertThat(response.getResults().get(0).getTranslatedText()).isEqualTo("Hello Macau");
        assertThat(response.getResults().get(0).getEngine()).isEqualTo("google");
        assertThat(response.getResults().get(0).getAttemptedEngines()).containsExactly("bing", "google");
        verify(translationEngineAdapter, times(2)).translate(any());
    }

    @Test
    void translateSkipsAlreadyFilledLocalesWhenOverwriteDisabled() {
        when(sysConfigMapper.selectList(any())).thenReturn(List.of(
                sysConfig("translation.primary_authoring_locale", "zh-Hant"),
                sysConfig("translation.engine_priority", "[\"bing\",\"google\"]"),
                sysConfig("translation.overwrite_filled_locales", "false")
        ));

        AdminTranslateRequest request = new AdminTranslateRequest();
        request.setSourceLocale("zh-Hant");
        request.setTargetLocales(List.of("pt"));
        request.setText("你好澳門");
        request.setExistingTranslations(Map.of("pt", "Conteudo manual"));

        AdminTranslateResponse response = service.translate(request);

        assertThat(response.getResults()).hasSize(1);
        assertThat(response.getResults().get(0).getStatus()).isEqualTo("skipped");
        assertThat(response.getResults().get(0).getTranslatedText()).isEqualTo("Conteudo manual");
        verify(translationEngineAdapter, times(0)).translate(any());
    }

    private SysConfig sysConfig(String key, String value) {
        SysConfig config = new SysConfig();
        config.setConfigKey(key);
        config.setConfigValue(value);
        return config;
    }
}
