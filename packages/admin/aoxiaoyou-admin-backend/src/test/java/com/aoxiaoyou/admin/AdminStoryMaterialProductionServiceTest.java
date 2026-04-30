package com.aoxiaoyou.admin;

import com.aoxiaoyou.admin.ai.provider.AiOutboundUrlGuard;
import com.aoxiaoyou.admin.common.exception.BusinessException;
import com.aoxiaoyou.admin.common.material.MaterialProductionPathGuard;
import com.aoxiaoyou.admin.dto.request.AdminStoryMaterialProductionRequest;
import com.aoxiaoyou.admin.dto.response.AdminStoryMaterialProductionResponse;
import com.aoxiaoyou.admin.entity.AiGenerationCandidate;
import com.aoxiaoyou.admin.entity.AiGenerationJob;
import com.aoxiaoyou.admin.entity.ContentAsset;
import com.aoxiaoyou.admin.entity.StoryMaterialPackage;
import com.aoxiaoyou.admin.entity.StoryMaterialPackageItem;
import com.aoxiaoyou.admin.entity.StoryMaterialPackageItemVersion;
import com.aoxiaoyou.admin.mapper.AiGenerationCandidateMapper;
import com.aoxiaoyou.admin.mapper.AiGenerationJobMapper;
import com.aoxiaoyou.admin.mapper.ContentAssetMapper;
import com.aoxiaoyou.admin.mapper.StoryMaterialPackageItemMapper;
import com.aoxiaoyou.admin.mapper.StoryMaterialPackageItemVersionMapper;
import com.aoxiaoyou.admin.mapper.StoryMaterialPackageMapper;
import com.aoxiaoyou.admin.media.CosAssetStorageService;
import com.aoxiaoyou.admin.media.StoredAssetMetadata;
import com.aoxiaoyou.admin.media.StoredAssetPayload;
import com.aoxiaoyou.admin.service.impl.AdminStoryMaterialProductionServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AdminStoryMaterialProductionServiceTest {

    @TempDir
    private Path tempDir;

    @Mock
    private StoryMaterialPackageMapper packageMapper;

    @Mock
    private StoryMaterialPackageItemMapper packageItemMapper;

    @Mock
    private StoryMaterialPackageItemVersionMapper versionMapper;

    @Mock
    private ContentAssetMapper contentAssetMapper;

    @Mock
    private AiGenerationCandidateMapper aiGenerationCandidateMapper;

    @Mock
    private AiGenerationJobMapper aiGenerationJobMapper;

    @Mock
    private CosAssetStorageService cosAssetStorageService;

    @Mock
    private AiOutboundUrlGuard aiOutboundUrlGuard;

    private final List<StoryMaterialPackageItemVersion> versions = new ArrayList<>();
    private final AtomicLong versionIds = new AtomicLong(900);
    private final AtomicLong assetIds = new AtomicLong(800);
    private StoryMaterialPackage materialPackage;
    private StoryMaterialPackageItem item;
    private AdminStoryMaterialProductionServiceImpl service;

    @BeforeEach
    void setUp() {
        materialPackage = new StoryMaterialPackage();
        materialPackage.setId(36L);
        materialPackage.setCode("east_west_package");
        materialPackage.setLocalRoot(tempDir.toString());
        materialPackage.setCosPrefix("miniapp/assets/phase36/east-west");
        materialPackage.setDeleted(0);

        item = new StoryMaterialPackageItem();
        item.setId(101L);
        item.setPackageId(36L);
        item.setItemKey("hero_ch01_ama_coast");
        item.setAssetKind("image");
        item.setStatus("planned");
        item.setCurrentVersionNo(0);
        item.setDeleted(0);

        service = new AdminStoryMaterialProductionServiceImpl(
                packageMapper,
                packageItemMapper,
                versionMapper,
                contentAssetMapper,
                aiGenerationCandidateMapper,
                aiGenerationJobMapper,
                cosAssetStorageService,
                new MaterialProductionPathGuard(),
                aiOutboundUrlGuard,
                new ObjectMapper()
        );

        when(packageMapper.selectOne(any())).thenReturn(materialPackage);
        when(packageItemMapper.selectOne(any())).thenReturn(item);
        when(versionMapper.selectList(any())).thenAnswer(invocation -> new ArrayList<>(versions));
        doAnswer(invocation -> {
            StoryMaterialPackageItemVersion version = invocation.getArgument(0);
            version.setId(versionIds.getAndIncrement());
            versions.add(version);
            return 1;
        }).when(versionMapper).insert(any(StoryMaterialPackageItemVersion.class));
        doAnswer(invocation -> {
            ContentAsset asset = invocation.getArgument(0);
            asset.setId(assetIds.getAndIncrement());
            return 1;
        }).when(contentAssetMapper).insert(any(ContentAsset.class));
    }

    @Test
    void plannedToUploadedLocalImportCreatesImmutableVersionAndUpdatesCurrentVersionId() throws Exception {
        Files.createDirectories(tempDir.resolve("images/heroes"));
        Files.write(tempDir.resolve("images/heroes/ch01.png"), new byte[]{1, 2, 3});
        when(cosAssetStorageService.storeAsset(any(StoredAssetPayload.class), eq("miniapp/assets/phase36/east-west/images/heroes/ch01.png")))
                .thenReturn(StoredAssetMetadata.builder()
                        .bucketName("tripofmacau")
                        .region("ap-hongkong")
                        .objectKey("miniapp/assets/phase36/east-west/images/heroes/ch01.png")
                        .canonicalUrl("https://cos.example.com/images/heroes/ch01.png")
                        .mimeType("image/png")
                        .localeCode("zh-Hant")
                        .fileSizeBytes(3L)
                        .checksum("checksum")
                        .etag("etag")
                        .build());

        AdminStoryMaterialProductionRequest.LocalImportRequest request = new AdminStoryMaterialProductionRequest.LocalImportRequest();
        request.setRelativeLocalPath("images/heroes/ch01.png");
        request.setAssetKind("image");
        request.setProviderName("image-2");
        request.setModelCode("gpt-image-2");
        request.setPromptText("hero prompt");
        request.setScriptText("script");
        request.setEstimatedCost(new BigDecimal("1.25"));
        request.setSubtitleMetadataJson("{\"schemaVersion\":1}");
        request.setPosterFallbackItemKey("poster_video_fallback");
        request.setLocaleCode("zh-Hant");

        AdminStoryMaterialProductionResponse.PackageItemVersionResponse response = service.importLocalAsset(
                36L,
                101L,
                request,
                7L,
                "editor",
                List.of("OPERATOR")
        );

        assertThat(response.getId()).isEqualTo(item.getCurrentVersionId());
        assertThat(response.getVersionNo()).isEqualTo(1);
        assertThat(response.getPromotionStatus()).isEqualTo("uploaded");
        assertThat(response.getProviderName()).isEqualTo("image-2");
        assertThat(response.getModelCode()).isEqualTo("gpt-image-2");
        assertThat(response.getEstimatedCost()).isEqualByComparingTo("1.25");
        assertThat(response.getSubtitleMetadataJson()).contains("schemaVersion");
        assertThat(response.getPosterFallbackItemKey()).isEqualTo("poster_video_fallback");
        assertThat(item.getStatus()).isEqualTo("uploaded");
        assertThat(item.getCurrentVersionNo()).isEqualTo(1);
        assertThat(versions).hasSize(1);
    }

    @Test
    void rollbackFlipsPointersWithoutDeletingLaterVersions() {
        StoryMaterialPackageItemVersion first = version(901L, 1, 801L, "old.png", "https://cos.example.com/old.png", "published");
        StoryMaterialPackageItemVersion second = version(902L, 2, 802L, "new.png", "https://cos.example.com/new.png", "published");
        versions.add(first);
        versions.add(second);
        item.setAssetId(802L);
        item.setCurrentVersionId(902L);
        item.setCurrentVersionNo(2);
        item.setLocalPath("new.png");
        item.setCosObjectKey("miniapp/assets/new.png");
        item.setCanonicalUrl("https://cos.example.com/new.png");
        when(versionMapper.selectOne(any())).thenReturn(first);
        when(contentAssetMapper.selectById(801L)).thenReturn(asset(801L, "miniapp/assets/old.png", "https://cos.example.com/old.png"));

        AdminStoryMaterialProductionRequest.RollbackRequest request = new AdminStoryMaterialProductionRequest.RollbackRequest();
        request.setRollbackVersionId(901L);

        AdminStoryMaterialProductionResponse.RollbackResult result = service.rollbackItemVersion(
                36L,
                101L,
                request,
                1L,
                "root",
                List.of("SUPER_ADMIN")
        );

        assertThat(result.getPreviousVersionId()).isEqualTo(902L);
        assertThat(result.getCurrentVersionId()).isEqualTo(901L);
        assertThat(item.getAssetId()).isEqualTo(801L);
        assertThat(item.getCurrentVersionId()).isEqualTo(901L);
        assertThat(item.getCurrentVersionNo()).isEqualTo(1);
        assertThat(item.getCanonicalUrl()).isEqualTo("https://cos.example.com/old.png");
        assertThat(versions).hasSize(3);
        assertThat(versions.get(2).getRollbackOfVersionId()).isEqualTo(901L);
    }

    @Test
    void requiresSuperAdminConfirmationForCostOverrideAndPublish() {
        AdminStoryMaterialProductionRequest.PreflightRequest request = new AdminStoryMaterialProductionRequest.PreflightRequest();
        request.setEstimatedTotalCost(new BigDecimal("50.00"));
        request.setBatchCostCeiling(new BigDecimal("10.00"));
        request.setConfirmCostOverride(true);

        assertThatThrownBy(() -> service.preflightPackage(36L, request, 7L, "editor", List.of("OPERATOR")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Super admin confirmation");

        StoryMaterialPackageItemVersion current = version(901L, 1, 801L, "asset.png", "https://cos.example.com/asset.png", "uploaded");
        item.setCurrentVersionId(901L);
        versions.add(current);
        when(versionMapper.selectOne(any())).thenReturn(current);

        assertThatThrownBy(() -> service.promoteItemVersion(
                36L,
                101L,
                new AdminStoryMaterialProductionRequest.PromoteRequest(),
                7L,
                "editor",
                List.of("OPERATOR")
        ))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Super admin confirmation");
    }

    @Test
    void guardedImportRejectsUnsafeRelativeLocalPathAndForcedCosObjectKey() {
        AdminStoryMaterialProductionRequest.LocalImportRequest unsafePath = new AdminStoryMaterialProductionRequest.LocalImportRequest();
        unsafePath.setRelativeLocalPath("../outside.png");

        assertThatThrownBy(() -> service.importLocalAsset(36L, 101L, unsafePath, 7L, "editor", List.of("SUPER_ADMIN")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("relativeLocalPath");

        AdminStoryMaterialProductionRequest.LocalImportRequest unsafeKey = new AdminStoryMaterialProductionRequest.LocalImportRequest();
        unsafeKey.setRelativeLocalPath("safe.png");
        unsafeKey.setForcedCosObjectKey("/escape/safe.png");

        assertThatThrownBy(() -> service.importLocalAsset(36L, 101L, unsafeKey, 7L, "editor", List.of("SUPER_ADMIN")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("COS object key");

        verify(cosAssetStorageService, never()).storeAsset(any(StoredAssetPayload.class), any());
    }

    @Test
    void finalizedCandidateBindingUsesAiOutboundUrlGuardAndStoresLineage() {
        item.setItemKey("audio_ch01_narration");
        item.setAssetKind("audio");
        AiGenerationCandidate candidate = finalizedCandidate(601L, 701L, 801L);
        candidate.setProviderAssetUrl("https://dashscope.aliyuncs.com/assets/audio.mp3");
        when(aiGenerationCandidateMapper.selectById(601L)).thenReturn(candidate);
        AiGenerationJob job = new AiGenerationJob();
        job.setId(701L);
        job.setPromptText("narration prompt");
        job.setProviderRequestId("req-1");
        when(aiGenerationJobMapper.selectById(701L)).thenReturn(job);
        when(contentAssetMapper.selectById(801L)).thenReturn(asset(801L, "miniapp/assets/audio/ch01.mp3", "https://cos.example.com/ch01.mp3"));
        when(aiOutboundUrlGuard.validatePublicSourceUrl(candidate.getProviderAssetUrl(), "AI candidate provider asset URL"))
                .thenReturn(candidate.getProviderAssetUrl());

        AdminStoryMaterialProductionRequest.AiCandidateBindRequest request = new AdminStoryMaterialProductionRequest.AiCandidateBindRequest();
        request.setAiCandidateId(601L);
        request.setProviderName("bailian");
        request.setModelCode("cosyvoice-v2");
        request.setAssetKind("audio");

        AdminStoryMaterialProductionResponse.PackageItemVersionResponse response = service.bindFinalizedCandidate(
                36L,
                101L,
                request,
                7L,
                "editor",
                List.of("OPERATOR")
        );

        assertThat(response.getAiJobId()).isEqualTo(701L);
        assertThat(response.getAiCandidateId()).isEqualTo(601L);
        assertThat(response.getProviderName()).isEqualTo("bailian");
        assertThat(response.getModelCode()).isEqualTo("cosyvoice-v2");
        assertThat(response.getPromptText()).isEqualTo("narration prompt");
        assertThat(item.getCurrentVersionId()).isEqualTo(response.getId());
        verify(aiOutboundUrlGuard).validatePublicSourceUrl(candidate.getProviderAssetUrl(), "AI candidate provider asset URL");
    }

    @Test
    void cosyVoiceFailureLeavesUnpublishedAndDoesNotCreateVersion() {
        item.setItemKey("sfx_reward_unlock");
        item.setAssetKind("audio");
        item.setCurrentVersionId(500L);
        item.setStatus("uploaded");
        AiGenerationCandidate failedCandidate = new AiGenerationCandidate();
        failedCandidate.setId(602L);
        failedCandidate.setJobId(702L);
        failedCandidate.setCandidateType("audio");
        failedCandidate.setIsFinalized(0);
        failedCandidate.setFinalizedAssetId(null);
        when(aiGenerationCandidateMapper.selectById(602L)).thenReturn(failedCandidate);
        when(aiGenerationJobMapper.selectById(702L)).thenReturn(new AiGenerationJob());

        AdminStoryMaterialProductionRequest.AiCandidateBindRequest request = new AdminStoryMaterialProductionRequest.AiCandidateBindRequest();
        request.setAiCandidateId(602L);
        request.setProviderName("bailian");
        request.setModelCode("cosyvoice-v2");

        assertThatThrownBy(() -> service.bindFinalizedCandidate(36L, 101L, request, 7L, "editor", List.of("OPERATOR")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Finalized AI candidate binary");

        assertThat(item.getCurrentVersionId()).isEqualTo(500L);
        assertThat(item.getStatus()).isEqualTo("manual_import_required");
        assertThat(versions).isEmpty();
        verify(versionMapper, never()).insert(any());
    }

    @Test
    void promotionMovesUploadedToPublishedAndCurrentVersionRemainsInspectable() {
        StoryMaterialPackageItemVersion current = version(901L, 1, 801L, "asset.png", "https://cos.example.com/asset.png", "uploaded");
        versions.add(current);
        item.setCurrentVersionId(901L);
        when(versionMapper.selectOne(any())).thenReturn(current);
        when(contentAssetMapper.selectById(801L)).thenReturn(asset(801L, "miniapp/assets/asset.png", "https://cos.example.com/asset.png"));
        AdminStoryMaterialProductionRequest.PromoteRequest request = new AdminStoryMaterialProductionRequest.PromoteRequest();
        request.setTargetStatus("published");

        AdminStoryMaterialProductionResponse.PromotionResult result = service.promoteItemVersion(
                36L,
                101L,
                request,
                1L,
                "root",
                List.of("SUPER_ADMIN")
        );

        assertThat(result.getItemStatus()).isEqualTo("published");
        assertThat(current.getPromotionStatus()).isEqualTo("published");
        assertThat(current.getVerifiedByAdminName()).isEqualTo("root");
        assertThat(item.getCurrentVersionId()).isEqualTo(901L);
        assertThat(item.getStatus()).isEqualTo("published");
    }

    private StoryMaterialPackageItemVersion version(Long id, Integer versionNo, Long assetId, String localPath, String canonicalUrl, String status) {
        StoryMaterialPackageItemVersion version = new StoryMaterialPackageItemVersion();
        version.setId(id);
        version.setPackageItemId(101L);
        version.setVersionNo(versionNo);
        version.setVersionStatus(status);
        version.setPromotionStatus(status);
        version.setContentAssetId(assetId);
        version.setLocalPath(localPath);
        version.setCosObjectKey("miniapp/assets/" + localPath);
        version.setCanonicalUrl(canonicalUrl);
        version.setAssetKind("image");
        version.setCurrencyCode("CNY");
        version.setDeleted(0);
        return version;
    }

    private ContentAsset asset(Long id, String objectKey, String canonicalUrl) {
        ContentAsset asset = new ContentAsset();
        asset.setId(id);
        asset.setAssetKind(objectKey.endsWith(".mp3") ? "audio" : "image");
        asset.setObjectKey(objectKey);
        asset.setCanonicalUrl(canonicalUrl);
        asset.setClientRelativePath(objectKey);
        return asset;
    }

    private AiGenerationCandidate finalizedCandidate(Long candidateId, Long jobId, Long assetId) {
        AiGenerationCandidate candidate = new AiGenerationCandidate();
        candidate.setId(candidateId);
        candidate.setJobId(jobId);
        candidate.setCandidateType("audio");
        candidate.setStorageObjectKey("ai/audio/ch01.mp3");
        candidate.setStorageUrl("https://cos.example.com/ai/audio/ch01.mp3");
        candidate.setIsFinalized(1);
        candidate.setFinalizedAssetId(assetId);
        return candidate;
    }
}
