package com.aoxiaoyou.tripofmacau.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "签到/触发日志写入请求")
public class TriggerLogCreateRequest {

    @NotNull(message = "userId 不能为空")
    private Long userId;

    @NotNull(message = "poiId 不能为空")
    private Long poiId;

    @NotBlank(message = "triggerType 不能为空")
    @Schema(description = "触发类型", example = "auto")
    private String triggerType;

    @DecimalMin(value = "0.0", message = "distance 不能小于 0")
    private BigDecimal distance;

    @DecimalMin(value = "0.0", message = "gpsAccuracy 不能小于 0")
    private BigDecimal gpsAccuracy;

    @Schema(description = "是否使用 Wi-Fi 辅助")
    private Boolean wifiUsed;
}
