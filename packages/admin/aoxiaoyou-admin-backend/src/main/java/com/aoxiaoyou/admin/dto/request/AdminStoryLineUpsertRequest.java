package com.aoxiaoyou.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

public class AdminStoryLineUpsertRequest {

    @Data
    public static class Upsert {
        @NotBlank(message = "故事线编码不能为空")
        private String code;

        @NotBlank(message = "中文名称不能为空")
        private String nameZh;

        private String nameEn;

        private String description;

        private String coverUrl;

        private String bannerUrl;

        private Integer totalChapters;

        private String category;

        private String difficulty;

        private Integer estimatedDurationMinutes;

        private String tags;

        private String status;

        private String publishAt;

        private String startAt;

        private String endAt;
    }
}

