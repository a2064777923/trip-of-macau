package com.aoxiaoyou.admin.dto.request;

import lombok.Data;

@Data
public class AdminUserUpdateRequest {

    private String displayName;
    private String email;
    private String phone;
    private String status;
    private Boolean allowLosslessUpload;
}
