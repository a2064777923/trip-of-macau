package com.aoxiaoyou.admin.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class AdminStoryLineListItemResponse {
    private Long storylineId;
    private Long cityId;
    private String cityName;
    private List<Long> cityBindings;
    private List<Long> subMapBindings;
    private String code;
    private String nameZh;
    private String difficulty;
    private String status;
    private Integer estimatedMinutes;
    private Integer totalChapters;
    private Long coverAssetId;
    private Integer sortOrder;
    private LocalDateTime createdAt;
}
