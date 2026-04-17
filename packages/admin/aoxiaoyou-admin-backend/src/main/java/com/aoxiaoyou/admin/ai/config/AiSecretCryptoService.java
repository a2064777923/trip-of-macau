package com.aoxiaoyou.admin.ai.config;

import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class AiSecretCryptoService {

    private final TextEncryptor encryptor;

    public AiSecretCryptoService(AiCapabilityProperties properties) {
        String password = requireConfiguredSecret(properties.getSecretEncryptionPassword(), "APP_AI_SECRET_ENCRYPTION_PASSWORD");
        String salt = requireConfiguredSecret(properties.getSecretEncryptionSalt(), "APP_AI_SECRET_ENCRYPTION_SALT");
        this.encryptor = Encryptors.delux(
                password,
                salt
        );
    }

    public String encrypt(String plainText) {
        if (!StringUtils.hasText(plainText)) {
            return null;
        }
        return encryptor.encrypt(plainText.trim());
    }

    public String decrypt(String cipherText) {
        if (!StringUtils.hasText(cipherText)) {
            return null;
        }
        return encryptor.decrypt(cipherText);
    }

    public String mask(String secret) {
        if (!StringUtils.hasText(secret)) {
            return null;
        }
        String trimmed = secret.trim();
        if (trimmed.length() <= 6) {
            return "***";
        }
        String prefix = trimmed.substring(0, Math.min(4, trimmed.length()));
        String suffix = trimmed.substring(trimmed.length() - Math.min(4, trimmed.length()));
        return prefix + "********" + suffix;
    }

    private String requireConfiguredSecret(String value, String envKey) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalStateException("Missing required AI secret configuration: " + envKey);
        }
        return value.trim();
    }
}
