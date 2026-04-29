package com.aoxiaoyou.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class AdminAiVoiceCloneRequest {

    @NotNull
    private Long providerId;

    @NotBlank
    private String targetModel;

    @NotBlank
    private String voiceName;

    private Long sourceAssetId;

    private String sourceUrl;

    private String previewText;

    private List<String> languageCodes;
}
