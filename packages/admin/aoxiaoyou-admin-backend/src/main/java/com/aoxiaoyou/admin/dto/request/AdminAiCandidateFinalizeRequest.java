package com.aoxiaoyou.admin.dto.request;

import lombok.Data;

@Data
public class AdminAiCandidateFinalizeRequest {

    private String assetKind;

    private String localeCode;

    private String status;
}
