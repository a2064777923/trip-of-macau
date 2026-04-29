package com.aoxiaoyou.tripofmacau.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ActivityResponse {

    private Long id;
    private String code;
    private String activityType;
    private String title;
    private String summary;
    private String description;
    private String htmlContent;
    private String venueName;
    private String address;
    private String organizerName;
    private String organizerContact;
    private String organizerWebsite;
    private Integer signupCapacity;
    private BigDecimal signupFeeAmount;
    private LocalDateTime signupStartAt;
    private LocalDateTime signupEndAt;
    private LocalDateTime publishStartAt;
    private LocalDateTime publishEndAt;
    private Integer isPinned;
    private String coverImageUrl;
    private String heroImageUrl;
    private List<CatalogRelationBindingResponse> cityBindings;
    private List<CatalogRelationBindingResponse> subMapBindings;
    private List<CatalogRelationBindingResponse> storylineBindings;
    private List<String> attachmentAssetUrls;
    private Integer participationCount;
    private Integer sortOrder;
}
