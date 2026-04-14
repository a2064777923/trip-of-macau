package com.aoxiaoyou.tripofmacau.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Schema(description = "Public user preferences update request")
public class UserPreferencesUpdateRequest {

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
