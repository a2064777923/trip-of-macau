package com.aoxiaoyou.tripofmacau.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserStampProgressResponse {

    private Long stampId;
    private LocalDateTime collectedAt;
}
