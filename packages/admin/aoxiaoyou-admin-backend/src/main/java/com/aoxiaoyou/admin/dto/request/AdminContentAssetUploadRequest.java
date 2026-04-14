package com.aoxiaoyou.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class AdminContentAssetUploadRequest {

    @NotNull(message = "file is required")
    private MultipartFile file;

    @NotBlank(message = "assetKind is required")
    private String assetKind;

    private String localeCode;
    private String status;
}
