package com.aoxiaoyou.tripofmacau;

import com.aoxiaoyou.tripofmacau.dto.response.StoryMediaAssetResponse;
import com.aoxiaoyou.tripofmacau.entity.ContentAsset;
import com.aoxiaoyou.tripofmacau.entity.StoryMaterialPackageItem;
import com.aoxiaoyou.tripofmacau.entity.StoryMaterialPackageItemVersion;
import com.aoxiaoyou.tripofmacau.mapper.StoryMaterialPackageItemMapper;
import com.aoxiaoyou.tripofmacau.mapper.StoryMaterialPackageItemVersionMapper;
import com.aoxiaoyou.tripofmacau.service.impl.PublicRuntimeAssetServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PublicRuntimeAssetServiceTest {

    private static final List<String> BANNED_STRINGS = List.of(
            "promptText",
            "scriptText",
            "localPath",
            "providerName",
            "apiKey",
            "secret",
            "estimatedCost",
            "actualCost",
            "qaNote"
    );

    @Mock
    private StoryMaterialPackageItemMapper itemMapper;
    @Mock
    private StoryMaterialPackageItemVersionMapper versionMapper;

    private PublicRuntimeAssetServiceImpl service;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        service = new PublicRuntimeAssetServiceImpl(itemMapper, versionMapper);
        objectMapper = new ObjectMapper();
        when(itemMapper.selectList(any())).thenReturn(List.of());
        when(versionMapper.selectList(any())).thenReturn(List.of());
    }

    @Test
    void publishedAssetReturnsAvailableMetadata() throws Exception {
        ContentAsset asset = asset(100L, "image", "published", "https://cdn.example.com/cover.png");
        asset.setMimeType("image/png");
        asset.setOriginalFilename("cover.png");
        asset.setWidthPx(1080);
        asset.setHeightPx(1440);
        asset.setFileSizeBytes(34567L);

        StoryMediaAssetResponse response = service.toPublicAsset(asset.getId(), Map.of(asset.getId(), asset));

        assertThat(response.getAvailability()).isEqualTo("available");
        assertThat(response.isFallbackUsed()).isFalse();
        assertThat(response.getRuntimeKind()).isEqualTo("image");
        assertThat(response.getUrl()).isEqualTo("https://cdn.example.com/cover.png");
        assertThat(response.getFileSizeBytes()).isEqualTo(34567L);
        assertNoBannedFields(response);
    }

    @Test
    void unpublishedAssetFallsBackWithoutLeakingProvenance() throws Exception {
        ContentAsset original = asset(101L, "video", "draft", "");
        original.setFallbackAssetId(102L);
        ContentAsset fallback = asset(102L, "image", "published", "https://cdn.example.com/fallback.png");

        StoryMediaAssetResponse response = service.toPublicAsset(original, Map.of(
                original.getId(), original,
                fallback.getId(), fallback
        ));

        assertThat(response.getId()).isEqualTo(original.getId());
        assertThat(response.getAvailability()).isEqualTo("fallback");
        assertThat(response.isFallbackUsed()).isTrue();
        assertThat(response.getUrl()).isEqualTo("https://cdn.example.com/fallback.png");
        assertThat(response.getRuntimeKind()).isEqualTo("image");
        assertNoBannedFields(response);
    }

    @Test
    void missingAssetReturnsUnsupportedPlaceholder() throws Exception {
        StoryMediaAssetResponse response = service.toPublicAsset(999L, Map.of());

        assertThat(response.getId()).isEqualTo(999L);
        assertThat(response.getAvailability()).isEqualTo("unsupported");
        assertThat(response.getUrl()).isEmpty();
        assertThat(response.getUnavailableReason()).contains("此媒體暫時未能播放");
        assertThat(response.isFallbackUsed()).isFalse();
        assertNoBannedFields(response);
    }

    @Test
    void lottieMetadataIncludesLoopAutoplayPosterAndFallback() throws Exception {
        ContentAsset lottie = asset(110L, "lottie", "published", "https://cdn.example.com/anim.json");
        lottie.setMimeType("application/json");
        lottie.setAnimationSubtype("lottie_json");
        lottie.setDefaultLoop(true);
        lottie.setDefaultAutoplay(false);
        lottie.setPosterAssetId(111L);
        lottie.setFallbackAssetId(112L);
        ContentAsset poster = asset(111L, "image", "published", "https://cdn.example.com/poster.png");
        ContentAsset fallback = asset(112L, "image", "published", "https://cdn.example.com/fallback.png");

        StoryMediaAssetResponse response = service.toPublicAsset(lottie, Map.of(
                lottie.getId(), lottie,
                poster.getId(), poster,
                fallback.getId(), fallback
        ));

        assertThat(response.getRuntimeKind()).isEqualTo("lottie");
        assertThat(response.getAnimationSubtype()).isEqualTo("lottie_json");
        assertThat(response.getDefaultLoop()).isTrue();
        assertThat(response.getDefaultAutoplay()).isFalse();
        assertThat(response.getPosterUrl()).isEqualTo("https://cdn.example.com/poster.png");
        assertThat(response.getFallbackUrl()).isEqualTo("https://cdn.example.com/fallback.png");
        assertNoBannedFields(response);
    }

    @Test
    void usageHintIncludesOnlySafeMaterialFields() throws Exception {
        StoryMaterialPackageItem item = materialItem(120L, 130L, "published");
        StoryMaterialPackageItemVersion version = version(220L, item.getId(), 130L, "published");
        when(itemMapper.selectList(any())).thenReturn(List.of(item), List.of(item));
        when(versionMapper.selectList(any())).thenReturn(List.of(version));

        Map<Long, StoryMediaAssetResponse.UsageHint> hints = service.loadUsageHints(List.of(130L));
        StoryMediaAssetResponse.UsageHint hint = hints.get(130L);

        assertThat(hint).isNotNull();
        assertThat(hint.getMaterialItemKey()).isEqualTo("ama-ch1-cover");
        assertThat(hint.getUsageTarget()).isEqualTo("chapter_cover");
        assertThat(hint.getChapterCode()).isEqualTo("chapter_1");
        assertThat(hint.getTargetType()).isEqualTo("story_chapter");
        assertThat(hint.getTargetCode()).isEqualTo("ama_temple");
        assertThat(hint.getDisplayRole()).isEqualTo("chapter_cover");
        assertThat(hint.getSourceScope()).isEqualTo("story_material_package");
        assertNoBannedFields(hint);
    }

    @Test
    void rejectedMaterialItemDoesNotExposeUsageHint() {
        StoryMaterialPackageItem item = materialItem(121L, 131L, "rejected");
        StoryMaterialPackageItemVersion version = version(221L, item.getId(), 131L, "published");
        when(itemMapper.selectList(any())).thenReturn(List.of(item), List.of(item));
        when(versionMapper.selectList(any())).thenReturn(List.of(version));

        Map<Long, StoryMediaAssetResponse.UsageHint> hints = service.loadUsageHints(List.of(131L));

        assertThat(hints).doesNotContainKey(131L);
    }

    private ContentAsset asset(Long id, String kind, String status, String canonicalUrl) {
        ContentAsset asset = new ContentAsset();
        asset.setId(id);
        asset.setAssetKind(kind);
        asset.setStatus(status);
        asset.setCanonicalUrl(canonicalUrl);
        return asset;
    }

    private StoryMaterialPackageItem materialItem(Long id, Long assetId, String status) {
        StoryMaterialPackageItem item = new StoryMaterialPackageItem();
        item.setId(id);
        item.setAssetId(assetId);
        item.setItemKey("ama-ch1-cover");
        item.setItemType("image");
        item.setAssetKind("image");
        item.setUsageTarget("chapter_cover");
        item.setChapterCode("chapter_1");
        item.setTargetType("story_chapter");
        item.setTargetCode("ama_temple");
        item.setStatus(status);
        item.setSortOrder(1);
        item.setDeleted(0);
        return item;
    }

    private StoryMaterialPackageItemVersion version(Long id, Long itemId, Long assetId, String status) {
        StoryMaterialPackageItemVersion version = new StoryMaterialPackageItemVersion();
        version.setId(id);
        version.setPackageItemId(itemId);
        version.setContentAssetId(assetId);
        version.setVersionNo(1);
        version.setPromotionStatus(status);
        version.setDeleted(0);
        return version;
    }

    private void assertNoBannedFields(Object value) throws Exception {
        String json = objectMapper.writeValueAsString(value);
        for (String banned : BANNED_STRINGS) {
            assertThat(json).doesNotContain(banned);
        }
    }
}
