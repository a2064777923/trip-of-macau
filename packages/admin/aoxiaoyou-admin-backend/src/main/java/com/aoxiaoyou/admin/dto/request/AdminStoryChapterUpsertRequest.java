package com.aoxiaoyou.admin.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

public class AdminStoryChapterUpsertRequest {

    @Data
    public static class Upsert {
        @NotNull(message = "故事线 ID 不能为空")
        private Long storyLineId;

        @NotNull(message = "章节序号不能为空")
        @Min(value = 1, message = "章节序号必须大于 0")
        private Integer chapterOrder;

        @NotBlank(message = "章节标题不能为空")
        private String titleZh;

        private String titleEn;

        private String titleZht;

        private String mediaType;

        private String mediaUrl;

        private String scriptZh;

        private String scriptEn;

        private String scriptZht;

        private String unlockType;

        private String unlockParam;

        private Integer duration;
    }
}
