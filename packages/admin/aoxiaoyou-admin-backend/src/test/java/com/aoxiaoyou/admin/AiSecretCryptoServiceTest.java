package com.aoxiaoyou.admin;

import com.aoxiaoyou.admin.ai.config.AiCapabilityProperties;
import com.aoxiaoyou.admin.ai.config.AiSecretCryptoService;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AiSecretCryptoServiceTest {

    @Test
    void encryptAndDecryptShouldRoundTrip() {
        AiCapabilityProperties properties = new AiCapabilityProperties();
        properties.setSecretEncryptionPassword("phase18-password");
        properties.setSecretEncryptionSalt("a1b2c3d4e5f60718");
        AiSecretCryptoService service = new AiSecretCryptoService(properties);

        String encrypted = service.encrypt("sk-test-phase18");

        assertThat(encrypted).isNotBlank().isNotEqualTo("sk-test-phase18");
        assertThat(service.decrypt(encrypted)).isEqualTo("sk-test-phase18");
    }

    @Test
    void maskShouldKeepHeadAndTailOnly() {
        AiCapabilityProperties properties = new AiCapabilityProperties();
        properties.setSecretEncryptionPassword("phase18-password");
        properties.setSecretEncryptionSalt("a1b2c3d4e5f60718");
        AiSecretCryptoService service = new AiSecretCryptoService(properties);

        assertThat(service.mask("sk-test-phase18")).isEqualTo("sk-t********se18");
        assertThat(service.mask("short")).isEqualTo("***");
        assertThat(service.mask(null)).isNull();
    }

    @Test
    void shouldFailFastWhenSecretsAreNotConfigured() {
        AiCapabilityProperties properties = new AiCapabilityProperties();

        assertThatThrownBy(() -> new AiSecretCryptoService(properties))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("APP_AI_SECRET_ENCRYPTION_PASSWORD");
    }
}
