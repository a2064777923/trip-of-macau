package com.aoxiaoyou.tripofmacau.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "用户登录请求")
public class UserLoginRequest {

    @NotBlank(message = "openId 不能为空")
    @Schema(description = "微信 openId", requiredMode = Schema.RequiredMode.REQUIRED)
    private String openId;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "头像 URL")
    private String avatarUrl;

    @Schema(description = "语言偏好", example = "zh_CN")
    private String languagePreference;

    @Schema(description = "界面模式", example = "standard")
    private String interfaceMode;
}
