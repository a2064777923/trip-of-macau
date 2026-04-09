package com.aoxiaoyou.admin.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TestAccountBatchStampGrantRequest {

    @NotNull(message = "批量发章数量不能为空")
    @Min(value = 1, message = "批量发章数量至少为 1")
    private Integer count;

    private String stampType;

    private String reason;
}
