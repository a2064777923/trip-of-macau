package com.aoxiaoyou.admin.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class AdminPoiDetailResponse {

    private Long poiId;
    private String name;
    private String subtitle;
    private String description;
    private String regionCode;
    private String regionName;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private BigDecimal gcj02Latitude;
    private BigDecimal gcj02Longitude;
    private String address;
    private Integer geofenceRadius;
    private String checkInMethod;
    private String coverImageUrl;
    private List<String> imageUrls;
    private String audioGuideUrl;
    private String videoUrl;
    private String arContentUrl;
    private String poiType;
    private List<String> tags;
    private String difficulty;
    private String openTime;
    private Integer suggestedVisitMinutes;
    private String status;
    private Long checkInCount;
    private Long favoriteCount;
    private Long categoryId;
    private String categoryName;
    private Long storylineId;
    private String storylineName;
    private String stampType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
