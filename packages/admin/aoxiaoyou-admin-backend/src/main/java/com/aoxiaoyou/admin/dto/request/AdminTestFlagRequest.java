package com.aoxiaoyou.admin.dto.request;

import lombok.Data;

@Data
public class AdminTestFlagRequest {

    private Boolean isTestAccount;

    private String reason;
}
