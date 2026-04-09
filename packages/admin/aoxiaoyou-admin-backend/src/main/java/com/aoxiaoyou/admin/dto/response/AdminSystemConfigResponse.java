package com.aoxiaoyou.admin.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AdminSystemConfigResponse {

    private Long id;
    private String configKey;
    private String configValue;
    private String configType;
    private String description;
    private LocalDateTime updatedAt;
}
