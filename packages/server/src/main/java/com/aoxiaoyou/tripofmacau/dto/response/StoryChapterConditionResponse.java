package com.aoxiaoyou.tripofmacau.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class StoryChapterConditionResponse {
    private String type;
    private Map<String, Object> config;
    private String rawJson;
}
