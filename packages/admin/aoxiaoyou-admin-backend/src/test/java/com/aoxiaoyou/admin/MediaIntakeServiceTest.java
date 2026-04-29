package com.aoxiaoyou.admin;

import com.aoxiaoyou.admin.dto.request.AdminContentAssetBatchUploadRequest;
import com.aoxiaoyou.admin.dto.request.AdminContentAssetUploadRequest;
import com.aoxiaoyou.admin.entity.ContentAsset;
import com.aoxiaoyou.admin.entity.SysAdmin;
import com.aoxiaoyou.admin.mapper.ContentAssetMapper;
import com.aoxiaoyou.admin.mapper.SysAdminMapper;
import com.aoxiaoyou.admin.media.CosAssetStorageService;
import com.aoxiaoyou.admin.media.MediaIntakeService;
import com.aoxiaoyou.admin.media.MediaUploadPolicyService;
import com.aoxiaoyou.admin.media.ResolvedMediaUploadPolicy;
import com.aoxiaoyou.admin.media.StoredAssetMetadata;
import com.aoxiaoyou.admin.media.StoredAssetPayload;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MediaIntakeServiceTest {

    @Mock
    private ContentAssetMapper contentAssetMapper;

    @Mock
    private SysAdminMapper sysAdminMapper;

    @Mock
    private MediaUploadPolicyService mediaUploadPolicyService;

    @Mock
    private CosAssetStorageService cosAssetStorageService;

    private MediaIntakeService service;

    @BeforeEach
    void setUp() {
        service = new MediaIntakeService(
                contentAssetMapper,
                sysAdminMapper,
                mediaUploadPolicyService,
                cosAssetStorageService,
                new ObjectMapper()
        );
    }

    @Test
    void intakeSinglePersistsUploadAuditMetadataAndEffectivePolicy() throws Exception {
        SysAdmin admin = admin(true);
        when(sysAdminMapper.selectById(7L)).thenReturn(admin);
        when(mediaUploadPolicyService.resolvePolicy(any(), any(), any())).thenReturn(ResolvedMediaUploadPolicy.builder()
                .assetKind("image")
                .policyFamily("image")
                .requestedPolicyCode("compressed")
                .effectivePolicyCode("image-compressed")
                .maxFileSizeBytes(10_485_760L)
                .qualityPercent(82)
                .maxWidthPx(512)
                .maxHeightPx(512)
                .preserveMetadata(false)
                .uploaderAllowsLossless(true)
                .note("policy note")
                .snapshot(java.util.Map.of("effectivePolicyCode", "image-compressed"))
                .build());
        when(cosAssetStorageService.storeAsset(any(StoredAssetPayload.class))).thenReturn(StoredAssetMetadata.builder()
                .bucketName("tripofmacau-1301163924")
                .region("ap-hongkong")
                .objectKey("miniapp/assets/image/2026/04/14/macau-asset.jpg")
                .canonicalUrl("https://example.com/macau-asset.jpg")
                .mimeType("image/jpeg")
                .localeCode("zh-Hant")
                .fileSizeBytes(1024L)
                .widthPx(256)
                .heightPx(256)
                .checksum("checksum")
                .etag("etag")
                .build());

        AtomicLong idSequence = new AtomicLong(100);
        org.mockito.Mockito.doAnswer(invocation -> {
            ContentAsset asset = invocation.getArgument(0);
            asset.setId(idSequence.getAndIncrement());
            return 1;
        }).when(contentAssetMapper).insert(any(ContentAsset.class));

        AdminContentAssetUploadRequest request = new AdminContentAssetUploadRequest();
        request.setFile(new MockMultipartFile("file", "macau.png", "image/png", samplePngBytes()));
        request.setLocaleCode("zh-Hant");
        request.setUploadSource("drag-drop");
        request.setStatus("published");

        ContentAsset asset = service.intakeSingle(request, 7L, "root");

        assertThat(asset.getId()).isEqualTo(100L);
        assertThat(asset.getAssetKind()).isEqualTo("image");
        assertThat(asset.getUploadSource()).isEqualTo("drag-drop");
        assertThat(asset.getUploadedByAdminId()).isEqualTo(7L);
        assertThat(asset.getUploadedByAdminName()).isEqualTo("內容管理員");
        assertThat(asset.getProcessingPolicyCode()).isEqualTo("image-compressed");
        assertThat(asset.getProcessingStatus()).isEqualTo("processed");
        assertThat(asset.getPublishedAt()).isNotNull();
        assertThat(asset.getProcessingProfileJson()).contains("effectivePolicyCode");

        ArgumentCaptor<StoredAssetPayload> payloadCaptor = ArgumentCaptor.forClass(StoredAssetPayload.class);
        verify(cosAssetStorageService).storeAsset(payloadCaptor.capture());
        assertThat(payloadCaptor.getValue().getAssetKind()).isEqualTo("image");
        assertThat(payloadCaptor.getValue().getContentType()).isEqualTo("image/jpeg");
    }

    @Test
    void intakeBatchCarriesFolderRelativePathsIntoPersistedAssets() {
        SysAdmin admin = admin(false);
        when(sysAdminMapper.selectById(9L)).thenReturn(admin);
        doNothing().when(mediaUploadPolicyService).validateBatch(eq(2), eq(11L));
        when(mediaUploadPolicyService.resolvePolicy(any(), any(), any())).thenReturn(ResolvedMediaUploadPolicy.builder()
                .assetKind("other")
                .policyFamily("file")
                .requestedPolicyCode("passthrough")
                .effectivePolicyCode("passthrough")
                .maxFileSizeBytes(1024L)
                .qualityPercent(100)
                .preserveMetadata(true)
                .uploaderAllowsLossless(false)
                .note("keep original")
                .snapshot(java.util.Map.of("effectivePolicyCode", "passthrough"))
                .build());
        when(cosAssetStorageService.storeAsset(any(StoredAssetPayload.class))).thenReturn(StoredAssetMetadata.builder()
                .bucketName("bucket")
                .region("ap-hongkong")
                .objectKey("miniapp/assets/other/asset.txt")
                .canonicalUrl("https://example.com/asset.txt")
                .mimeType("text/plain")
                .localeCode("")
                .fileSizeBytes(5L)
                .checksum("checksum")
                .etag("etag")
                .build());

        AtomicLong idSequence = new AtomicLong(200);
        org.mockito.Mockito.doAnswer(invocation -> {
            ContentAsset asset = invocation.getArgument(0);
            asset.setId(idSequence.getAndIncrement());
            return 1;
        }).when(contentAssetMapper).insert(any(ContentAsset.class));

        AdminContentAssetBatchUploadRequest request = new AdminContentAssetBatchUploadRequest();
        request.setFiles(new MockMultipartFile[]{
                new MockMultipartFile("files", "a.txt", "text/plain", "hello".getBytes()),
                new MockMultipartFile("files", "b.txt", "text/plain", "world!".getBytes())
        });
        request.setUploadSource("folder");
        request.setClientRelativePaths(new String[]{"covers/a.txt", "attachments/b.txt"});

        List<ContentAsset> assets = service.intakeBatch(request, 9L, "admin");

        assertThat(assets).hasSize(2);
        assertThat(assets.get(0).getClientRelativePath()).isEqualTo("covers/a.txt");
        assertThat(assets.get(1).getClientRelativePath()).isEqualTo("attachments/b.txt");
        assertThat(assets.get(0).getUploadSource()).isEqualTo("folder");
        assertThat(assets.get(0).getProcessingPolicyCode()).isEqualTo("passthrough");
        verify(mediaUploadPolicyService, times(1)).validateBatch(2, 11L);
    }

    private SysAdmin admin(boolean allowLosslessUpload) {
        SysAdmin admin = new SysAdmin();
        admin.setId(7L);
        admin.setNickname("內容管理員");
        admin.setAllowLosslessUpload(allowLosslessUpload);
        return admin;
    }

    private byte[] samplePngBytes() throws Exception {
        BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "png", outputStream);
        return outputStream.toByteArray();
    }
}
