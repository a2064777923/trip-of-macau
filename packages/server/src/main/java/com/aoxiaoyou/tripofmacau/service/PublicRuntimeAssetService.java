package com.aoxiaoyou.tripofmacau.service;

import com.aoxiaoyou.tripofmacau.dto.response.StoryMediaAssetResponse;
import com.aoxiaoyou.tripofmacau.entity.ContentAsset;

import java.util.Collection;
import java.util.Map;

public interface PublicRuntimeAssetService {

    StoryMediaAssetResponse toPublicAsset(Long assetId, Map<Long, ContentAsset> publishedAssets);

    StoryMediaAssetResponse toPublicAsset(ContentAsset asset, Map<Long, ContentAsset> publishedAssets);

    Map<Long, StoryMediaAssetResponse.UsageHint> loadUsageHints(Collection<Long> assetIds);
}
