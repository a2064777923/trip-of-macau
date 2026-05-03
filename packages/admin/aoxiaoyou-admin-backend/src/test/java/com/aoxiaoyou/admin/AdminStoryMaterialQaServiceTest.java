package com.aoxiaoyou.admin;

import com.aoxiaoyou.admin.dto.request.AdminStoryMaterialQaRequest;
import com.aoxiaoyou.admin.dto.response.AdminStoryMaterialQaResponse;
import com.aoxiaoyou.admin.entity.ContentAsset;
import com.aoxiaoyou.admin.entity.StoryMaterialPackage;
import com.aoxiaoyou.admin.entity.StoryMaterialPackageItem;
import com.aoxiaoyou.admin.entity.StoryMaterialPackageItemVersion;
import com.aoxiaoyou.admin.mapper.ContentAssetMapper;
import com.aoxiaoyou.admin.mapper.StoryMaterialPackageItemMapper;
import com.aoxiaoyou.admin.mapper.StoryMaterialPackageItemVersionMapper;
import com.aoxiaoyou.admin.mapper.StoryMaterialPackageMapper;
import com.aoxiaoyou.admin.service.impl.AdminStoryMaterialQaServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AdminStoryMaterialQaServiceTest {

    @Mock
    private StoryMaterialPackageMapper packageMapper;

    @Mock
    private StoryMaterialPackageItemMapper packageItemMapper;

    @Mock
    private StoryMaterialPackageItemVersionMapper versionMapper;

    @Mock
    private ContentAssetMapper contentAssetMapper;

    private final List<StoryMaterialPackageItem> items = new ArrayList<>();
    private final List<StoryMaterialPackageItemVersion> versions = new ArrayList<>();
    private final List<ContentAsset> assets = new ArrayList<>();
    private final AtomicLong versionIds = new AtomicLong(1000);
    private StoryMaterialPackage materialPackage;
    private TestableQaService service;

    @BeforeEach
    void setUp() {
        materialPackage = new StoryMaterialPackage();
        materialPackage.setId(37L);
        materialPackage.setCode("east_west_package");
        materialPackage.setLocalRoot("D:/Archive/trip-of-macau/docs/content-packages/east-west-war-and-coexistence");
        materialPackage.setCosPrefix("miniapp/assets/phase36/east-west");
        materialPackage.setDeleted(0);

        service = new TestableQaService(
                packageMapper,
                packageItemMapper,
                versionMapper,
                contentAssetMapper,
                new ObjectMapper()
        );

        when(packageMapper.selectOne(any())).thenReturn(materialPackage);
        when(packageMapper.selectById(37L)).thenReturn(materialPackage);
        when(packageItemMapper.selectList(any())).thenAnswer(invocation -> new ArrayList<>(items));
        when(packageItemMapper.selectOne(any())).thenAnswer(invocation -> items.stream().findFirst().orElse(null));
        when(versionMapper.selectList(any())).thenAnswer(invocation -> new ArrayList<>(versions));
        when(versionMapper.selectOne(any())).thenAnswer(invocation -> versions.stream()
                .filter(version -> version.getId() != null)
                .findFirst()
                .orElse(null));
        when(contentAssetMapper.selectById(any())).thenAnswer(invocation -> {
            Long id = invocation.getArgument(0);
            return assets.stream().filter(asset -> asset.getId().equals(id)).findFirst().orElse(null);
        });
        doAnswer(invocation -> {
            StoryMaterialPackageItemVersion version = invocation.getArgument(0);
            version.setId(versionIds.getAndIncrement());
            versions.add(version);
            return 1;
        }).when(versionMapper).insert(any(StoryMaterialPackageItemVersion.class));
    }

    @Test
    void overviewClassifiesUsableAndProblemItems() {
        addItem(101L, "cover", "image", "published", 201L, "storyline.cover");
        versions.add(version(301L, 101L, 1, 201L, "image", "published", "https://cdn.example.myqcloud.com/cover.png"));
        assets.add(asset(201L, "image", "https://cdn.example.myqcloud.com/cover.png", 1234L));
        addItem(102L, "missing_audio", "audio", "planned", null, "chapter.narration");

        AdminStoryMaterialQaResponse.QaOverview overview = service.overview(37L, new AdminStoryMaterialQaRequest.QaItemQuery());

        assertThat(overview.getTotalItems()).isEqualTo(2);
        assertThat(overview.getHealthStateCounters()).containsEntry("usable", 1L);
        assertThat(overview.getHealthStateCounters()).containsEntry("planned_slot", 1L);
        assertThat(overview.getAssetKindCounters()).containsEntry("image", 1L);
        assertThat(overview.getAssetKindCounters()).containsEntry("audio", 1L);
    }

    @Test
    void consistencyCheckReportsMissingAndWrongKindAssets() {
        addItem(101L, "wrong_kind", "image", "published", 201L, "storyline.cover");
        versions.add(version(301L, 101L, 1, 201L, "audio", "published", "https://cdn.example.myqcloud.com/audio.mp3"));
        assets.add(asset(201L, "audio", "https://cdn.example.myqcloud.com/audio.mp3", 1234L));
        addItem(102L, "missing", "video", "published", null, "chapter.video");

        AdminStoryMaterialQaResponse.ConsistencyReport report = service.runConsistencyCheck(37L, new AdminStoryMaterialQaRequest.ConsistencyCheckRequest());

        assertThat(report.getFindings()).extracting(AdminStoryMaterialQaResponse.QaFinding::getFindingCode)
                .contains("WRONG_ASSET_KIND", "MISSING_CURRENT_ASSET", "MISSING_PUBLISHED_ASSET");
        assertThat(report.getBlockingCount()).isGreaterThanOrEqualTo(3);
    }

    @Test
    void rejectKeepsContentAssetAndVersionHistory() {
        StoryMaterialPackageItem item = addItem(101L, "cover", "image", "uploaded", 201L, "storyline.cover");
        item.setCurrentVersionId(301L);
        item.setCurrentVersionNo(1);
        StoryMaterialPackageItemVersion current = version(301L, 101L, 1, 201L, "image", "uploaded", "https://cdn.example.myqcloud.com/cover.png");
        versions.add(current);
        assets.add(asset(201L, "image", "https://cdn.example.myqcloud.com/cover.png", 1234L));
        when(versionMapper.selectOne(any())).thenReturn(current);

        AdminStoryMaterialQaRequest.QaActionRequest request = new AdminStoryMaterialQaRequest.QaActionRequest();
        request.setVersionId(301L);
        request.setNote("bad crop");
        AdminStoryMaterialQaResponse.QaActionResult result = service.reject(37L, 101L, request, 7L, "qa", List.of("OPERATOR"));

        assertThat(result.getCurrentStatus()).isEqualTo("retry_required");
        assertThat(current.getVersionStatus()).isEqualTo("rejected");
        assertThat(current.getPromotionStatus()).isEqualTo("rejected");
        assertThat(current.getContentAssetId()).isEqualTo(201L);
        assertThat(versions).hasSize(1);
        verify(contentAssetMapper, never()).deleteById(201L);
    }

    @Test
    void replacePreservesVersionHistory() {
        StoryMaterialPackageItem item = addItem(101L, "cover", "image", "uploaded", 201L, "storyline.cover");
        item.setCurrentVersionId(301L);
        item.setCurrentVersionNo(1);
        StoryMaterialPackageItemVersion current = version(301L, 101L, 1, 201L, "image", "uploaded", "https://cdn.example.myqcloud.com/old.png");
        versions.add(current);
        assets.add(asset(201L, "image", "https://cdn.example.myqcloud.com/old.png", 1234L));
        assets.add(asset(202L, "image", "https://cdn.example.myqcloud.com/new.png", 4567L));
        when(versionMapper.selectOne(any())).thenReturn(current);

        AdminStoryMaterialQaRequest.ReplaceRequest request = new AdminStoryMaterialQaRequest.ReplaceRequest();
        request.setReplacementAssetId(202L);
        request.setVersionId(301L);
        request.setTargetStatus("approved");
        request.setNote("new art board crop");
        AdminStoryMaterialQaResponse.QaActionResult result = service.replace(37L, 101L, request, 7L, "qa", List.of("OPERATOR"));

        assertThat(result.getCurrentVersionId()).isNotEqualTo(301L);
        assertThat(item.getAssetId()).isEqualTo(202L);
        assertThat(item.getCurrentVersionNo()).isEqualTo(2);
        assertThat(versions).hasSize(2);
        assertThat(versions.get(1).getParentVersionId()).isEqualTo(301L);
        assertThat(versions.get(1).getContentAssetId()).isEqualTo(202L);
        assertThat(current.getContentAssetId()).isEqualTo(201L);
    }

    @Test
    void approveUsesExistingPromotionPath() {
        StoryMaterialPackageItem item = addItem(101L, "cover", "image", "uploaded", 201L, "storyline.cover");
        item.setCurrentVersionId(301L);
        StoryMaterialPackageItemVersion current = version(301L, 101L, 1, 201L, "image", "uploaded", "https://cdn.example.myqcloud.com/cover.png");
        versions.add(current);
        assets.add(asset(201L, "image", "https://cdn.example.myqcloud.com/cover.png", 1234L));
        when(versionMapper.selectOne(any())).thenReturn(current);

        AdminStoryMaterialQaRequest.QaActionRequest request = new AdminStoryMaterialQaRequest.QaActionRequest();
        request.setVersionId(301L);
        request.setTargetStatus("approved");
        AdminStoryMaterialQaResponse.QaActionResult result = service.approve(37L, 101L, request, 7L, "qa", List.of("OPERATOR"));

        assertThat(result.getCurrentStatus()).isEqualTo("approved");
        assertThat(current.getVersionStatus()).isEqualTo("approved");
        assertThat(current.getPromotionStatus()).isEqualTo("approved");
        assertThat(item.getCurrentVersionId()).isEqualTo(301L);
        assertThat(item.getStatus()).isEqualTo("approved");
    }

    @Test
    void cosHeadCheckIsBoundedByMaxCosChecks() {
        for (int i = 0; i < 3; i++) {
            long itemId = 101L + i;
            long assetId = 201L + i;
            StoryMaterialPackageItem item = addItem(itemId, "asset_" + i, "image", "published", assetId, "storyline.cover");
            item.setCurrentVersionId(301L + i);
            versions.add(version(301L + i, itemId, 1, assetId, "image", "published", "https://cdn.example.myqcloud.com/" + i + ".png"));
            assets.add(asset(assetId, "image", "https://cdn.example.myqcloud.com/" + i + ".png", 1234L));
        }
        AdminStoryMaterialQaRequest.ConsistencyCheckRequest request = new AdminStoryMaterialQaRequest.ConsistencyCheckRequest();
        request.setIncludeCosHead(true);
        request.setMaxCosChecks(2);

        service.runConsistencyCheck(37L, request);

        assertThat(service.cosHeadCalls.get()).isEqualTo(2);
    }

    @Test
    void externalCaptionMetadataIsInfoFinding() {
        StoryMaterialPackageItem item = addItem(101L, "chapter_video", "video", "published", 201L, "chapter.video");
        item.setCurrentVersionId(301L);
        StoryMaterialPackageItemVersion current = version(301L, 101L, 1, 201L, "video", "published", "https://cdn.example.myqcloud.com/ch01.mp4");
        current.setSubtitleMetadataJson("{\"schemaVersion\":1,\"mode\":\"external\"}");
        versions.add(current);
        assets.add(asset(201L, "video", "https://cdn.example.myqcloud.com/ch01.mp4", 1024L));

        AdminStoryMaterialQaResponse.ConsistencyReport report = service.runConsistencyCheck(37L, new AdminStoryMaterialQaRequest.ConsistencyCheckRequest());

        assertThat(report.getFindings()).anySatisfy(finding -> {
            assertThat(finding.getFindingCode()).isEqualTo("EXTERNAL_CAPTION_METADATA");
            assertThat(finding.getSeverity()).isEqualTo("info");
        });
    }

    private StoryMaterialPackageItem addItem(Long id, String itemKey, String assetKind, String status, Long assetId, String usageTarget) {
        StoryMaterialPackageItem item = new StoryMaterialPackageItem();
        item.setId(id);
        item.setPackageId(37L);
        item.setItemKey(itemKey);
        item.setItemType("asset");
        item.setAssetKind(assetKind);
        item.setAssetId(assetId);
        item.setUsageTarget(usageTarget);
        item.setChapterCode("ch01");
        item.setStatus(status);
        item.setCurrentVersionNo(assetId == null ? 0 : 1);
        item.setDeleted(0);
        items.add(item);
        return item;
    }

    private StoryMaterialPackageItemVersion version(Long id, Long itemId, Integer versionNo, Long assetId, String assetKind, String status, String url) {
        StoryMaterialPackageItemVersion version = new StoryMaterialPackageItemVersion();
        version.setId(id);
        version.setPackageItemId(itemId);
        version.setVersionNo(versionNo);
        version.setVersionStatus(status);
        version.setPromotionStatus(status);
        version.setContentAssetId(assetId);
        version.setSourceType("test");
        version.setAssetKind(assetKind);
        version.setLocalPath("local/" + id);
        version.setCosObjectKey("miniapp/assets/" + id);
        version.setCanonicalUrl(url);
        version.setCurrencyCode("CNY");
        version.setDeleted(0);
        return version;
    }

    private ContentAsset asset(Long id, String assetKind, String url, Long fileSizeBytes) {
        ContentAsset asset = new ContentAsset();
        asset.setId(id);
        asset.setAssetKind(assetKind);
        asset.setCanonicalUrl(url);
        asset.setObjectKey("miniapp/assets/" + id);
        asset.setClientRelativePath("local/" + id);
        asset.setFileSizeBytes(fileSizeBytes);
        asset.setProcessingStatus("stored");
        asset.setStatus("uploaded");
        return asset;
    }

    private static class TestableQaService extends AdminStoryMaterialQaServiceImpl {
        private final AtomicInteger cosHeadCalls = new AtomicInteger();

        TestableQaService(StoryMaterialPackageMapper packageMapper,
                          StoryMaterialPackageItemMapper packageItemMapper,
                          StoryMaterialPackageItemVersionMapper versionMapper,
                          ContentAssetMapper contentAssetMapper,
                          ObjectMapper objectMapper) {
            super(packageMapper, packageItemMapper, versionMapper, contentAssetMapper, objectMapper);
        }

        @Override
        public boolean isCosHeadAvailable(ContentAsset asset) {
            cosHeadCalls.incrementAndGet();
            return false;
        }

        @Override
        public boolean isAllowedCosHost(String host) {
            return true;
        }
    }
}
