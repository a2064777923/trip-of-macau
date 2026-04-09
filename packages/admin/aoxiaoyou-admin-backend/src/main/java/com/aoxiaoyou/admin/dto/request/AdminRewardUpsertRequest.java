package com.aoxiaoyou.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

public class AdminRewardUpsertRequest {

    @Data
    public static class Upsert {
        @NotBlank(message = "奖励名称不能为空")
        private String name;

        private String description;

        private Integer stampsRequired;

        private Integer totalQuantity;

        private Integer redeemedCount;

        private String startTime;

        private String endTime;

        private String status;
    }
}
