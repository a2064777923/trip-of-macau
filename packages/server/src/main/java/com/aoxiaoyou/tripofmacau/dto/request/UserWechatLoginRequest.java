package com.aoxiaoyou.tripofmacau.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Public user WeChat login request")
public class UserWechatLoginRequest {

    @NotBlank(message = "code must not be blank")
    @Schema(description = "WeChat mini-program login code", requiredMode = Schema.RequiredMode.REQUIRED)
    private String code;

    private String nickname;

    private String avatarUrl;

    private String localeCode;

    private String interfaceMode;
}
