package com.aoxiaoyou.admin;

import com.aoxiaoyou.admin.ai.config.AiCapabilityProperties;
import com.aoxiaoyou.admin.ai.config.AiSecretCryptoService;
import com.aoxiaoyou.admin.ai.provider.AiOutboundUrlGuard;
import com.aoxiaoyou.admin.ai.provider.AiProviderTemplateRegistry;
import com.aoxiaoyou.admin.ai.provider.DashScopeProviderGateway;
import com.aoxiaoyou.admin.ai.routing.AiGovernanceService;
import com.aoxiaoyou.admin.common.exception.BusinessException;
import com.aoxiaoyou.admin.dto.request.AdminAiCandidateFinalizeRequest;
import com.aoxiaoyou.admin.dto.response.AdminAiGenerationJobResponse;
import com.aoxiaoyou.admin.entity.AiCapability;
import com.aoxiaoyou.admin.entity.AiGenerationCandidate;
import com.aoxiaoyou.admin.entity.AiGenerationJob;
import com.aoxiaoyou.admin.entity.AiProviderConfig;
import com.aoxiaoyou.admin.entity.AiProviderInventory;
import com.aoxiaoyou.admin.entity.ContentAsset;
import com.aoxiaoyou.admin.mapper.AiCapabilityMapper;
import com.aoxiaoyou.admin.mapper.AiCapabilityPolicyMapper;
import com.aoxiaoyou.admin.mapper.AiGenerationCandidateMapper;
import com.aoxiaoyou.admin.mapper.AiGenerationJobMapper;
import com.aoxiaoyou.admin.mapper.AiPolicyProviderBindingMapper;
import com.aoxiaoyou.admin.mapper.AiPromptTemplateMapper;
import com.aoxiaoyou.admin.mapper.AiProviderConfigMapper;
import com.aoxiaoyou.admin.mapper.AiProviderInventoryMapper;
import com.aoxiaoyou.admin.mapper.AiProviderSyncJobMapper;
import com.aoxiaoyou.admin.mapper.AiQuotaRuleMapper;
import com.aoxiaoyou.admin.mapper.AiRequestLogMapper;
import com.aoxiaoyou.admin.mapper.ContentAssetMapper;
import com.aoxiaoyou.admin.mapper.SysConfigMapper;
import com.aoxiaoyou.admin.media.CosAssetStorageService;
import com.aoxiaoyou.admin.service.impl.AdminAiServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminAiServiceImplTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private AiCapabilityProperties aiCapabilityProperties;

    @Mock
    private AiSecretCryptoService aiSecretCryptoService;

    @Mock
    private AiOutboundUrlGuard aiOutboundUrlGuard;

    @Mock
    private AiProviderTemplateRegistry aiProviderTemplateRegistry;

    @Mock
    private DashScopeProviderGateway dashScopeProviderGateway;

    @Mock
    private AiGovernanceService aiGovernanceService;

    @Mock
    private CosAssetStorageService cosAssetStorageService;

    @Mock
    private AiCapabilityMapper aiCapabilityMapper;

    @Mock
    private AiCapabilityPolicyMapper aiCapabilityPolicyMapper;

    @Mock
    private AiPolicyProviderBindingMapper aiPolicyProviderBindingMapper;

    @Mock
    private AiProviderConfigMapper aiProviderConfigMapper;

    @Mock
    private AiProviderInventoryMapper aiProviderInventoryMapper;

    @Mock
    private AiProviderSyncJobMapper aiProviderSyncJobMapper;

    @Mock
    private AiQuotaRuleMapper aiQuotaRuleMapper;

    @Mock
    private AiPromptTemplateMapper aiPromptTemplateMapper;

    @Mock
    private AiGenerationJobMapper aiGenerationJobMapper;

    @Mock
    private AiGenerationCandidateMapper aiGenerationCandidateMapper;

    @Mock
    private AiRequestLogMapper aiRequestLogMapper;

    @Mock
    private ContentAssetMapper contentAssetMapper;

    @Mock
    private SysConfigMapper sysConfigMapper;

    @InjectMocks
    private AdminAiServiceImpl service;

    @Test
    void resolveProviderUsesWitnessDefaultsForTravelQa() {
        AiCapability capability = new AiCapability();
        capability.setCapabilityCode("travel_qa");

        AiProviderConfig provider = new AiProviderConfig();
        provider.setId(101L);
        provider.setProviderName("dashscope-chat");
        provider.setDisplayName("DashScope Chat");
        provider.setStatus(1);

        AiProviderInventory inventory = new AiProviderInventory();
        inventory.setId(201L);
        inventory.setProviderId(101L);
        inventory.setInventoryCode("qwen3.5-flash");
        inventory.setExternalId("qwen3.5-flash");
        inventory.setAvailabilityStatus("available");

        when(aiProviderConfigMapper.selectOne(any())).thenReturn(provider);
        when(aiProviderInventoryMapper.selectOne(any())).thenReturn(inventory);

        Object resolved = ReflectionTestUtils.invokeMethod(
                service,
                "resolveProvider",
                capability,
                null,
                null,
                null
        );

        AiProviderConfig resolvedProvider = ReflectionTestUtils.invokeMethod(resolved, "provider");
        AiProviderInventory resolvedInventory = ReflectionTestUtils.invokeMethod(resolved, "inventory");

        assertThat(resolvedProvider.getProviderName()).isEqualTo("dashscope-chat");
        assertThat(resolvedInventory.getInventoryCode()).isEqualTo("qwen3.5-flash");
    }

    @Test
    void resolveProviderFailsDeterministicallyWhenWitnessInventoryMissing() {
        AiCapability capability = new AiCapability();
        capability.setCapabilityCode("admin_image_generation");

        AiProviderConfig provider = new AiProviderConfig();
        provider.setId(102L);
        provider.setProviderName("dashscope-image");
        provider.setStatus(1);

        when(aiProviderConfigMapper.selectOne(any())).thenReturn(provider);
        when(aiProviderInventoryMapper.selectOne(any())).thenReturn(null);

        assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(
                service,
                "resolveProvider",
                capability,
                null,
                null,
                null
        ))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("wan2.6-image");
    }

    @Test
    void resolveProviderHonorsExplicitInventorySelection() {
        AiCapability capability = new AiCapability();
        capability.setCapabilityCode("admin_image_generation");

        AiProviderConfig provider = new AiProviderConfig();
        provider.setId(103L);
        provider.setProviderName("dashscope-image");
        provider.setStatus(1);

        AiProviderInventory inventory = new AiProviderInventory();
        inventory.setId(301L);
        inventory.setProviderId(103L);
        inventory.setInventoryCode("custom-image-model");
        inventory.setExternalId("custom-image-model");
        inventory.setCapabilityCodesJson("[\"admin_image_generation\"]");

        when(aiProviderInventoryMapper.selectById(301L)).thenReturn(inventory);
        when(aiProviderConfigMapper.selectById(103L)).thenReturn(provider);

        Object resolved = ReflectionTestUtils.invokeMethod(
                service,
                "resolveProvider",
                capability,
                null,
                null,
                null,
                301L
        );

        AiProviderConfig resolvedProvider = ReflectionTestUtils.invokeMethod(resolved, "provider");
        AiProviderInventory resolvedInventory = ReflectionTestUtils.invokeMethod(resolved, "inventory");

        assertThat(resolvedProvider.getId()).isEqualTo(103L);
        assertThat(resolvedInventory.getId()).isEqualTo(301L);
        assertThat((String) ReflectionTestUtils.invokeMethod(resolved, "modelOverride")).isEqualTo("custom-image-model");
    }

    @Test
    void finalizeCandidateInsertsCanonicalAssetAndMarksCandidateFinalized() {
        AiGenerationCandidate candidate = new AiGenerationCandidate();
        candidate.setId(11L);
        candidate.setJobId(21L);
        candidate.setCandidateType("image");
        candidate.setStorageBucketName("tripofmacau-1301163924");
        candidate.setStorageRegion("ap-hongkong");
        candidate.setStorageObjectKey("ai/poi/cover.png");
        candidate.setStorageUrl("https://cos.example.com/ai/poi/cover.png");
        candidate.setMimeType("image/png");
        candidate.setFileSizeBytes(2048L);
        candidate.setWidthPx(1024);
        candidate.setHeightPx(768);
        candidate.setMetadataJson("{\"engine\":\"dashscope\"}");
        candidate.setPreviewText("候選封面");
        candidate.setIsSelected(1);
        candidate.setIsFinalized(0);

        AiGenerationJob job = new AiGenerationJob();
        job.setId(21L);
        job.setOwnerAdminId(7L);
        job.setOwnerAdminName("editor");
        job.setGenerationType("image");
        job.setJobStatus("completed");

        when(aiGenerationCandidateMapper.selectById(11L)).thenReturn(candidate);
        when(aiGenerationJobMapper.selectById(21L)).thenReturn(job);
        when(aiGenerationCandidateMapper.selectList(any())).thenReturn(List.of(candidate));
        when(aiCapabilityMapper.selectList(any())).thenReturn(Collections.emptyList());
        when(aiCapabilityPolicyMapper.selectList(any())).thenReturn(Collections.emptyList());
        when(aiPromptTemplateMapper.selectList(any())).thenReturn(Collections.emptyList());
        when(aiProviderConfigMapper.selectList(any())).thenReturn(Collections.emptyList());
        when(aiProviderInventoryMapper.selectList(any())).thenReturn(Collections.emptyList());

        doAnswer(invocation -> {
            ContentAsset asset = invocation.getArgument(0);
            asset.setId(88L);
            return 1;
        }).when(contentAssetMapper).insert(any(ContentAsset.class));

        AdminAiCandidateFinalizeRequest request = new AdminAiCandidateFinalizeRequest();
        request.setAssetKind("image");
        request.setLocaleCode("zh-Hant");
        request.setStatus("published");

        AdminAiGenerationJobResponse response = service.finalizeCandidate(
                11L,
                request,
                7L,
                "editor",
                List.of("OPERATOR")
        );

        ArgumentCaptor<ContentAsset> assetCaptor = ArgumentCaptor.forClass(ContentAsset.class);
        verify(contentAssetMapper).insert(assetCaptor.capture());

        assertThat(assetCaptor.getValue().getCanonicalUrl()).isEqualTo("https://cos.example.com/ai/poi/cover.png");
        assertThat(assetCaptor.getValue().getProcessingPolicyCode()).isEqualTo("ai-generated");
        assertThat(assetCaptor.getValue().getStatus()).isEqualTo("published");
        assertThat(candidate.getFinalizedAssetId()).isEqualTo(88L);
        assertThat(candidate.getIsFinalized()).isEqualTo(1);
        assertThat(response.getFinalizedCandidateId()).isEqualTo(11L);
        assertThat(response.getCandidates()).hasSize(1);
        assertThat(response.getCandidates().get(0).getFinalizedAssetId()).isEqualTo(88L);
    }

    @Test
    void restoreCandidateRejectsCrossOwnerForOperator() {
        AiGenerationCandidate candidate = new AiGenerationCandidate();
        candidate.setId(12L);
        candidate.setJobId(22L);

        AiGenerationJob job = new AiGenerationJob();
        job.setId(22L);
        job.setOwnerAdminId(7L);

        when(aiGenerationCandidateMapper.selectById(12L)).thenReturn(candidate);
        when(aiGenerationJobMapper.selectById(22L)).thenReturn(job);

        assertThatThrownBy(() -> service.restoreCandidate(12L, 8L, List.of("OPERATOR")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("沒有切換 AI 候選版本的權限");
    }

    @Test
    void getGenerationJobAllowsSuperAdminToReadAnotherOwnersHistory() {
        AiGenerationJob job = new AiGenerationJob();
        job.setId(31L);
        job.setOwnerAdminId(7L);
        job.setOwnerAdminName("owner");
        job.setJobStatus("completed");
        job.setGenerationType("image");
        job.setLatestCandidateId(41L);

        AiGenerationCandidate candidate = new AiGenerationCandidate();
        candidate.setId(41L);
        candidate.setJobId(31L);
        candidate.setCandidateType("image");
        candidate.setIsSelected(1);

        when(aiGenerationJobMapper.selectById(31L)).thenReturn(job);
        when(aiGenerationCandidateMapper.selectList(any())).thenReturn(List.of(candidate));
        when(aiCapabilityMapper.selectList(any())).thenReturn(Collections.emptyList());
        when(aiCapabilityPolicyMapper.selectList(any())).thenReturn(Collections.emptyList());
        when(aiPromptTemplateMapper.selectList(any())).thenReturn(Collections.emptyList());
        when(aiProviderConfigMapper.selectList(any())).thenReturn(Collections.emptyList());
        when(aiProviderInventoryMapper.selectList(any())).thenReturn(Collections.emptyList());

        AdminAiGenerationJobResponse response = service.getGenerationJob(31L, 99L, List.of("SUPER_ADMIN"));

        assertThat(response.getId()).isEqualTo(31L);
        assertThat(response.getCandidates()).hasSize(1);
        assertThat(response.getCandidates().get(0).getId()).isEqualTo(41L);
    }

    @Test
    void resolveVoiceSourceUrlValidatesManualSourceUrl() {
        when(aiOutboundUrlGuard.validatePublicSourceUrl("https://media.example.com/sample.mp3", "Voice clone source URL"))
                .thenReturn("https://media.example.com/sample.mp3");

        String resolved = ReflectionTestUtils.invokeMethod(
                service,
                "resolveVoiceSourceUrl",
                null,
                "https://media.example.com/sample.mp3"
        );

        assertThat(resolved).isEqualTo("https://media.example.com/sample.mp3");
        verify(aiOutboundUrlGuard).validatePublicSourceUrl("https://media.example.com/sample.mp3", "Voice clone source URL");
    }

    @Test
    void resolveVoiceSourceUrlValidatesAssetCanonicalUrl() {
        ContentAsset asset = new ContentAsset();
        asset.setId(88L);
        asset.setCanonicalUrl("https://cos.example.com/audio/demo.mp3?sign=test");

        when(contentAssetMapper.selectById(88L)).thenReturn(asset);
        when(aiOutboundUrlGuard.validatePublicSourceUrl(asset.getCanonicalUrl(), "Voice clone asset URL"))
                .thenReturn(asset.getCanonicalUrl());

        String resolved = ReflectionTestUtils.invokeMethod(
                service,
                "resolveVoiceSourceUrl",
                88L,
                null
        );

        assertThat(resolved).isEqualTo(asset.getCanonicalUrl());
        verify(aiOutboundUrlGuard).validatePublicSourceUrl(asset.getCanonicalUrl(), "Voice clone asset URL");
    }
}
