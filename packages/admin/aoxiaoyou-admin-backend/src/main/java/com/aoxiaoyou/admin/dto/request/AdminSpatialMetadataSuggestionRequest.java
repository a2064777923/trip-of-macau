package com.aoxiaoyou.admin.dto.request;

import lombok.Data;

@Data
public class AdminSpatialMetadataSuggestionRequest {
    private String entityType;
    private String code;
    private String nameZh;
    private String nameEn;
    private String nameZht;
}
