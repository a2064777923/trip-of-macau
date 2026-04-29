package com.aoxiaoyou.admin;

import com.aoxiaoyou.admin.ai.provider.AiOutboundUrlGuard;
import com.aoxiaoyou.admin.common.exception.BusinessException;
import com.aoxiaoyou.admin.entity.AiProviderConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AiOutboundUrlGuardTest {

    private final AiOutboundUrlGuard guard = new AiOutboundUrlGuard();

    @Test
    void acceptsOfficialOpenAiBaseUrl() {
        String normalized = guard.normalizeConfiguredBaseUrl("openai", "https://api.openai.com/v1/");

        assertEquals("https://api.openai.com/v1", normalized);
    }

    @Test
    void rejectsLoopbackBaseUrl() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> guard.normalizeConfiguredBaseUrl("openai", "https://127.0.0.1/v1"));

        assertEquals(4055, ex.getCode());
    }

    @Test
    void rejectsUnofficialHostForBuiltInProvider() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> guard.normalizeConfiguredBaseUrl("openai", "https://example.com/v1"));

        assertEquals(4055, ex.getCode());
    }

    @Test
    void allowsPublicCustomBaseUrl() {
        String normalized = guard.normalizeConfiguredBaseUrl("custom", "https://example.com/v1/");

        assertEquals("https://example.com/v1", normalized);
    }

    @Test
    void rejectsLocalAssetDownloadUrl() {
        AiProviderConfig provider = new AiProviderConfig();
        provider.setPlatformCode("custom");
        provider.setApiBaseUrl("https://example.com/v1");

        BusinessException ex = assertThrows(BusinessException.class,
                () -> guard.validateAssetDownloadUrl(provider, "https://127.0.0.1/file.png"));

        assertEquals(4055, ex.getCode());
    }

    @Test
    void allowsPublicCustomAssetDownloadUrl() {
        AiProviderConfig provider = new AiProviderConfig();
        provider.setPlatformCode("custom");
        provider.setApiBaseUrl("https://example.com/v1");

        String normalized = guard.validateAssetDownloadUrl(provider, "https://8.8.8.8/file.png");

        assertEquals("https://8.8.8.8/file.png", normalized);
    }

    @Test
    void rejectsLoopbackVoiceCloneSourceUrl() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> guard.validatePublicSourceUrl("https://127.0.0.1/sample.mp3", "Voice clone source URL"));

        assertEquals(4055, ex.getCode());
    }

    @Test
    void allowsPublicVoiceCloneSourceUrlWithQueryString() {
        String normalized = guard.validatePublicSourceUrl(
                "https://example.com/sample.mp3?signature=test",
                "Voice clone source URL"
        );

        assertEquals("https://example.com/sample.mp3?signature=test", normalized);
    }
}
