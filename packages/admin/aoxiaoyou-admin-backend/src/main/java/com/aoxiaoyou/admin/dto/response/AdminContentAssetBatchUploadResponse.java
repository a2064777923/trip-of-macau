package com.aoxiaoyou.admin.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AdminContentAssetBatchUploadResponse {

    private Integer uploadedCount;
    private Integer failedCount;
    private List<AdminContentAssetResponse> items;
    private List<BatchFailure> failures;

    @Data
    @Builder
    public static class BatchFailure {
        private String originalFilename;
        private String clientRelativePath;
        private String message;
    }
}
