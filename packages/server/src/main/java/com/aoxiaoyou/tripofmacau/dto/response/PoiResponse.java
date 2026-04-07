package com.aoxiaoyou.tripofmacau.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PoiResponse {

    private Long id;
    private String nameZh;
    private String nameEn;
    private String nameZht;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String address;
    private Long categoryId;
    private Integer triggerRadius;
    private String importance;
    private Long storyLineId;
    private String stampType;
    private String description;
}
