package com.aoxiaoyou.tripofmacau.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class UserProfileResponse {

    private Long id;
    private String openId;
    private String nickname;
    private String avatarUrl;
    private String languagePreference;
    private Integer level;
    private String title;
    private Integer totalStamps;
    private String interfaceMode;
    private BigDecimal fontScale;
    private Boolean highContrast;
    private Boolean voiceGuideEnabled;
    private Boolean simplifiedMode;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
