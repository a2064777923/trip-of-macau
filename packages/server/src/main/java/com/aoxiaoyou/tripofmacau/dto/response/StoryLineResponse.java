package com.aoxiaoyou.tripofmacau.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StoryLineResponse {

    private Long id;
    private String code;
    private String nameZh;
    private String nameEn;
    private String description;
    private String coverUrl;
    private Integer totalChapters;
    private String status;
}
