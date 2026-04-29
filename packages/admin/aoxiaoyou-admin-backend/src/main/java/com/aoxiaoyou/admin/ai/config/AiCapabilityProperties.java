package com.aoxiaoyou.admin.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app.ai")
public class AiCapabilityProperties {

    private String secretEncryptionPassword;

    private String secretEncryptionSalt;

    private Integer providerTestTimeoutMs = 8000;

    private Integer requestTimeoutMs = 30000;

    private Integer defaultPollIntervalMs = 3000;

    private Long maxDownloadBytes = 67108864L;

    private String dashscopeCompatibleBaseUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1";

    private String dashscopeImageBaseUrl = "https://dashscope.aliyuncs.com/api/v1/services/aigc/image-generation/generation";

    private String dashscopeTaskStatusBaseUrl = "https://dashscope.aliyuncs.com/api/v1/tasks";

    private String dashscopeTtsBaseUrl = "https://dashscope.aliyuncs.com/api/v1/services/audio/tts/SpeechSynthesizer";
}
