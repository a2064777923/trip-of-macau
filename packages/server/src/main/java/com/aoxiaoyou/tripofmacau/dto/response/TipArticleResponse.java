package com.aoxiaoyou.tripofmacau.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class TipArticleResponse {

    private Long id;
    private Long cityId;
    private String cityCode;
    private String code;
    private String categoryCode;
    private String title;
    private String summary;
    private List<String> contentParagraphs;
    private String authorDisplayName;
    private String locationName;
    private List<String> tags;
    private String coverImageUrl;
    private String sourceType;
    private Integer sortOrder;
    private LocalDateTime publishedAt;
}
