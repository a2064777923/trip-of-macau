package com.aoxiaoyou.tripofmacau.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
public class UserPreferencesResponse {

    private String interfaceMode;
    private BigDecimal fontScale;
    private Boolean highContrast;
    private Boolean voiceGuideEnabled;
    private Boolean seniorMode;
    private String localeCode;
    private String emergencyContactName;
    private String emergencyContactPhone;
    private Map<String, Object> runtimeOverrides;
}
