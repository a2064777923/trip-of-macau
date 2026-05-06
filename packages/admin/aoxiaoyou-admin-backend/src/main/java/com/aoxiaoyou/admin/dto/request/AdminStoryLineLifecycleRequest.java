package com.aoxiaoyou.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminStoryLineLifecycleRequest {

    @NotBlank(message = "action is required")
    private String action;

    private String confirmationText;
}
