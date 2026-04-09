package com.aoxiaoyou.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TestAccountStampGrantRequest {

    @NotBlank(message = "印章类型不能为空")
    private String stampType;

    @NotNull(message = "来源 ID 不能为空")
    private Long sourceId;

    private String reason;
}
