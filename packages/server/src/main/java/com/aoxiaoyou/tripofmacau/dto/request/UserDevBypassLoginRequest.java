package com.aoxiaoyou.tripofmacau.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Public user local/dev dev-bypass login request")
public class UserDevBypassLoginRequest {

    @NotBlank(message = "devIdentity must not be blank")
    @Schema(description = "Stable local/dev identity", requiredMode = Schema.RequiredMode.REQUIRED)
    private String devIdentity;

    private String nickname;

    private String avatarUrl;

    private String localeCode;

    private String interfaceMode;
}
