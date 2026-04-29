package com.aoxiaoyou.admin.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminRewardRuleLinkResponse {
    private Long id;
    private String code;
    private String nameZh;
    private String nameZht;
    private String summaryText;
    private String status;
}
