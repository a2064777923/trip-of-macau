package com.aoxiaoyou.admin.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AdminContentAssetUsageSummaryResponse {
    private Long assetId;
    private Integer usageCount;
    private List<AdminContentAssetUsageItemResponse> usages;
}
