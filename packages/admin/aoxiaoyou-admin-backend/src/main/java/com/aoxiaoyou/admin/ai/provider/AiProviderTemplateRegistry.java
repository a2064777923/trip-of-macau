package com.aoxiaoyou.admin.ai.provider;

import lombok.Builder;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Component
public class AiProviderTemplateRegistry {

    private final List<AiProviderTemplateDefinition> templates = List.of(
            AiProviderTemplateDefinition.builder()
                    .platformCode("openai")
                    .platformLabel("OpenAI")
                    .description("適合文字、多模態與語音工作流，支援 OpenAI 相容模型清單同步。")
                    .providerType("openai")
                    .endpointStyle("openai_compatible")
                    .defaultBaseUrl("https://api.openai.com/v1")
                    .docsUrl("https://platform.openai.com/docs/api-reference/models/list")
                    .authScheme("bearer_key")
                    .syncStrategy("list_api")
                    .inventorySemantics("model_list")
                    .defaultModelName("gpt-4o-mini")
                    .supportedModalities(List.of("text", "vision", "audio", "image"))
                    .credentialFields(defaultCredentialFields())
                    .inventorySeeds(List.of(
                            inventorySeed("gpt-4o-mini", "gpt-4o-mini", "GPT-4o mini", "model", List.of("text", "vision"), List.of("itinerary_planning", "travel_qa", "navigation_assist"), new BigDecimal("0.15"), new BigDecimal("0.60"), null, null, 128000, 10, 1),
                            inventorySeed("gpt-image-1", "gpt-image-1", "GPT Image 1", "model", List.of("image"), List.of("admin_image_generation"), null, null, new BigDecimal("0.040000"), null, null, 20, 0),
                            inventorySeed("gpt-4o-mini-tts", "gpt-4o-mini-tts", "GPT-4o mini TTS", "model", List.of("audio"), List.of("admin_tts_generation", "npc_voice_dialogue"), null, null, null, new BigDecimal("0.015000"), null, 30, 0)
                    ))
                    .build(),
            AiProviderTemplateDefinition.builder()
                    .platformCode("bailian")
                    .platformLabel("阿里百鍊")
                    .description("同時覆蓋 OpenAI 相容對話、文生圖與語音合成；模型庫會先嘗試 API 同步，再回退至官方目錄。")
                    .providerType("bailian")
                    .endpointStyle("openai_compatible")
                    .defaultBaseUrl("https://dashscope.aliyuncs.com/compatible-mode/v1")
                    .docsUrl("https://bailian.console.aliyun.com")
                    .authScheme("bearer_key")
                    .syncStrategy("hybrid_list_or_catalog")
                    .inventorySemantics("hybrid_catalog")
                    .defaultModelName("qwen-plus")
                    .supportedModalities(List.of("text", "vision", "image", "audio"))
                    .credentialFields(defaultCredentialFields())
                    .inventorySeeds(List.of(
                            inventorySeed("qwen-plus", "qwen-plus", "Qwen Plus", "model", List.of("text", "vision"), List.of("admin_prompt_drafting", "itinerary_planning", "travel_qa", "npc_voice_dialogue", "navigation_assist"), new BigDecimal("0.010000"), new BigDecimal("0.030000"), null, null, 128000, 10, 1),
                            inventorySeed("wan2.6-image", "wan2.6-image", "Wan 2.6 Image", "model", List.of("image"), List.of("admin_image_generation"), null, null, new BigDecimal("0.080000"), null, null, 20, 0),
                            inventorySeed("cosyvoice-v3-flash", "cosyvoice-v3-flash", "CosyVoice V3 Flash", "model", List.of("audio"), List.of("admin_tts_generation", "npc_voice_dialogue"), null, null, null, new BigDecimal("0.030000"), null, 30, 0)
                    ))
                    .build(),
            AiProviderTemplateDefinition.builder()
                    .platformCode("hunyuan")
                    .platformLabel("騰訊混元")
                    .description("以 OpenAI 相容接口接入，模型庫依官方可用型錄維護。")
                    .providerType("hunyuan")
                    .endpointStyle("openai_compatible")
                    .defaultBaseUrl("https://api.hunyuan.cloud.tencent.com/v1")
                    .docsUrl("https://cloud.tencent.com/document/product/1729/111007")
                    .authScheme("bearer_key")
                    .syncStrategy("documented_catalog")
                    .inventorySemantics("catalog")
                    .defaultModelName("hunyuan-lite")
                    .supportedModalities(List.of("text", "vision"))
                    .credentialFields(defaultCredentialFields())
                    .inventorySeeds(List.of(
                            inventorySeed("hunyuan-lite", "hunyuan-lite", "混元 Lite", "model", List.of("text"), List.of("travel_qa", "navigation_assist"), new BigDecimal("0.100000"), new BigDecimal("0.300000"), null, null, 32000, 10, 1),
                            inventorySeed("hunyuan-standard", "hunyuan-standard", "混元 Standard", "model", List.of("text", "vision"), List.of("itinerary_planning", "travel_qa", "photo_positioning"), new BigDecimal("0.200000"), new BigDecimal("0.600000"), null, null, 128000, 20, 0)
                    ))
                    .build(),
            AiProviderTemplateDefinition.builder()
                    .platformCode("minimax")
                    .platformLabel("MiniMax")
                    .description("多模態平台，模型同步以官方目錄為主，適合語音與創作型工作流。")
                    .providerType("minimax")
                    .endpointStyle("openai_compatible")
                    .defaultBaseUrl("https://api.minimax.chat/v1")
                    .docsUrl("https://platform.minimaxi.com/docs/api-reference/api-overview")
                    .authScheme("bearer_key")
                    .syncStrategy("documented_catalog")
                    .inventorySemantics("catalog")
                    .defaultModelName("MiniMax-Text-01")
                    .supportedModalities(List.of("text", "audio", "image"))
                    .credentialFields(defaultCredentialFields())
                    .inventorySeeds(List.of(
                            inventorySeed("MiniMax-Text-01", "MiniMax-Text-01", "MiniMax Text", "model", List.of("text"), List.of("admin_prompt_drafting", "travel_qa"), new BigDecimal("0.120000"), new BigDecimal("0.400000"), null, null, 128000, 10, 1),
                            inventorySeed("MiniMax-Voice-01", "MiniMax-Voice-01", "MiniMax Voice", "model", List.of("audio"), List.of("admin_tts_generation", "npc_voice_dialogue"), null, null, null, new BigDecimal("0.020000"), null, 20, 0)
                    ))
                    .build(),
            AiProviderTemplateDefinition.builder()
                    .platformCode("volcengine")
                    .platformLabel("火山引擎")
                    .description("以 Endpoint 為核心的模型供應平台，庫存同步會保留 endpoint 資訊。")
                    .providerType("volcengine")
                    .endpointStyle("openai_compatible")
                    .defaultBaseUrl("https://ark.cn-beijing.volces.com/api/v3")
                    .docsUrl("https://www.volcengine.com/docs/82379/1261492")
                    .authScheme("bearer_key")
                    .syncStrategy("endpoint_discovery")
                    .inventorySemantics("endpoint_inventory")
                    .defaultModelName("doubao-seed-1-6")
                    .supportedModalities(List.of("text", "vision", "image"))
                    .credentialFields(defaultCredentialFields())
                    .inventorySeeds(List.of(
                            inventorySeed("doubao-seed-1-6", "doubao-seed-1-6", "Doubao Seed 1.6", "endpoint", List.of("text", "vision"), List.of("itinerary_planning", "travel_qa"), new BigDecimal("0.090000"), new BigDecimal("0.320000"), null, null, 32000, 10, 1),
                            inventorySeed("doubao-image", "doubao-image", "Doubao Image Endpoint", "endpoint", List.of("image"), List.of("admin_image_generation"), null, null, new BigDecimal("0.060000"), null, null, 20, 0)
                    ))
                    .build(),
            AiProviderTemplateDefinition.builder()
                    .platformCode("custom")
                    .platformLabel("自定義接入")
                    .description("自定義 Base URL、授權方式與手動維護模型庫，適合私有代理或特殊平台。")
                    .providerType("custom")
                    .endpointStyle("openai_compatible")
                    .defaultBaseUrl("")
                    .docsUrl("")
                    .authScheme("bearer_key")
                    .syncStrategy("manual")
                    .inventorySemantics("manual")
                    .defaultModelName("")
                    .supportedModalities(List.of("text", "vision", "image", "audio"))
                    .credentialFields(defaultCredentialFields())
                    .inventorySeeds(List.of())
                    .build()
    );

    public List<AiProviderTemplateDefinition> list() {
        return templates;
    }

    public Optional<AiProviderTemplateDefinition> find(String platformCode) {
        if (platformCode == null) {
            return Optional.empty();
        }
        return templates.stream()
                .filter(item -> item.getPlatformCode().equalsIgnoreCase(platformCode.trim()))
                .findFirst();
    }

    private static List<Map<String, Object>> defaultCredentialFields() {
        return List.of(
                Map.of("field", "apiKey", "label", "API Key", "required", true, "type", "password"),
                Map.of("field", "apiSecret", "label", "API Secret", "required", false, "type", "password")
        );
    }

    private static AiInventorySeed inventorySeed(String code,
                                                 String externalId,
                                                 String displayName,
                                                 String inventoryType,
                                                 List<String> modalities,
                                                 List<String> capabilities,
                                                 BigDecimal inputPrice,
                                                 BigDecimal outputPrice,
                                                 BigDecimal imagePrice,
                                                 BigDecimal audioPrice,
                                                 Integer contextWindow,
                                                 Integer sortOrder,
                                                 Integer isDefault) {
        return AiInventorySeed.builder()
                .inventoryCode(code.toLowerCase(Locale.ROOT).replace(' ', '-'))
                .externalId(externalId)
                .displayName(displayName)
                .inventoryType(inventoryType)
                .modalityCodes(modalities)
                .capabilityCodes(capabilities)
                .inputPricePer1k(inputPrice)
                .outputPricePer1k(outputPrice)
                .imagePricePerCall(imagePrice)
                .audioPricePerMinute(audioPrice)
                .contextWindowTokens(contextWindow)
                .sourceType("documentation")
                .availabilityStatus("available")
                .sortOrder(sortOrder)
                .isDefault(isDefault)
                .build();
    }

    @Data
    @Builder
    public static class AiProviderTemplateDefinition {
        private String platformCode;
        private String platformLabel;
        private String description;
        private String providerType;
        private String endpointStyle;
        private String defaultBaseUrl;
        private String docsUrl;
        private String authScheme;
        private String syncStrategy;
        private String inventorySemantics;
        private String defaultModelName;
        private List<String> supportedModalities;
        private List<Map<String, Object>> credentialFields;
        private List<AiInventorySeed> inventorySeeds;
    }

    @Data
    @Builder
    public static class AiInventorySeed {
        private String inventoryCode;
        private String externalId;
        private String displayName;
        private String inventoryType;
        private List<String> modalityCodes;
        private List<String> capabilityCodes;
        private String sourceType;
        private String availabilityStatus;
        private BigDecimal inputPricePer1k;
        private BigDecimal outputPricePer1k;
        private BigDecimal imagePricePerCall;
        private BigDecimal audioPricePerMinute;
        private Integer contextWindowTokens;
        private Integer sortOrder;
        private Integer isDefault;
    }
}
