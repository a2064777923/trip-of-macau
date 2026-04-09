package com.aoxiaoyou.admin.dto.request;

import lombok.Data;

@Data
public class TestAccountProgressResetRequest {

    private String resetType;

    private String reason;
}
