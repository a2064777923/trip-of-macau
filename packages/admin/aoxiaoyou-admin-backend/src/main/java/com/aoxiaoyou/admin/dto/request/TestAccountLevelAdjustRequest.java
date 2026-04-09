package com.aoxiaoyou.admin.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TestAccountLevelAdjustRequest {

    @NotNull(message = "目标等级不能为空")
    private Integer targetLevel;

    @NotNull(message = "目标经验不能为空")
    private Integer targetExp;

    private String reason;
}
