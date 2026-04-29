package com.aoxiaoyou.admin.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class AdminContentAssetBatchUploadRequest {

    @NotNull(message = "files are required")
    private MultipartFile[] files;

    private String assetKind;
    private String localeCode;
    private String status;
    private String uploadSource;
    private String[] clientRelativePaths;
}
