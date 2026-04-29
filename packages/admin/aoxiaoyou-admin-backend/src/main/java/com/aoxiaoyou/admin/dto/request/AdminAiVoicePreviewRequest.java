package com.aoxiaoyou.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdminAiVoicePreviewRequest {

    @NotNull
    private Long providerId;

    @NotBlank
    private String modelCode;

    @NotBlank
    private String voiceCode;

    private String scriptText;

    private String languageCode;

    private String instruction;

    private String format;

    private Integer sampleRate;

    private Float rate;

    private Float pitch;

    private Integer volume;
}
