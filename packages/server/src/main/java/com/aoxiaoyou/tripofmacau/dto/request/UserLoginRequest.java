package com.aoxiaoyou.tripofmacau.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Public user login request")
public class UserLoginRequest {

    @NotBlank(message = "openId must not be blank")
    @Schema(description = "Stable mini-program user openId or devtools fallback id", requiredMode = Schema.RequiredMode.REQUIRED)
    private String openId;

    private String nickname;

    private String avatarUrl;

    private String localeCode;

    private String interfaceMode;

    private UserBootstrapStateRequest bootstrapState;
}
