package com.aoxiaoyou.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AdminPoiUpsertRequest {

    @NotBlank(message = "中文名称不能为空")
    private String nameZh;

    private String nameEn;

    private String nameZht;

    private String subtitle;

    private String regionCode;

    private String poiType;

    @NotNull(message = "纬度不能为空")
    private BigDecimal latitude;

    @NotNull(message = "经度不能为空")
    private BigDecimal longitude;

    private String address;

    private Long categoryId;

    private Integer triggerRadius;

    private String checkInMethod;

    private String importance;

    private Long storyLineId;

    private String stampType;

    private String description;

    private String coverImageUrl;

    private String imageUrls;

    private String audioGuideUrl;

    private String videoUrl;

    private String arContentUrl;

    private String tags;

    private String difficulty;

    private String openTime;

    private Integer suggestedVisitMinutes;

    private String status;
}

