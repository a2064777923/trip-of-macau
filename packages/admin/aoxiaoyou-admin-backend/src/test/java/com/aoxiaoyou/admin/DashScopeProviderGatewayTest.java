package com.aoxiaoyou.admin;

import com.aoxiaoyou.admin.ai.config.AiCapabilityProperties;
import com.aoxiaoyou.admin.ai.provider.AiOutboundUrlGuard;
import com.aoxiaoyou.admin.ai.provider.DashScopeProviderGateway;
import com.aoxiaoyou.admin.common.exception.BusinessException;
import com.aoxiaoyou.admin.entity.AiProviderConfig;
import com.aoxiaoyou.admin.entity.AiProviderInventory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DashScopeProviderGatewayTest {

    private final DashScopeProviderGateway gateway =
            new DashScopeProviderGateway(new ObjectMapper(), new AiCapabilityProperties(), new AiOutboundUrlGuard());

    @Test
    void extractsCompatibleModeTopLevelChoicesText() {
        Map<String, Object> response = Map.of(
                "choices", List.of(
                        Map.of(
                                "message", Map.of(
                                        "role", "assistant",
                                        "reasoning_content", "internal reasoning",
                                        "content", "澳門適合故事化探索，因為步行尺度緊湊而且文化層次密集。"
                                )
                        )
                )
        );

        String text = ReflectionTestUtils.invokeMethod(gateway, "extractTextFromChatResponse", response);

        assertEquals("澳門適合故事化探索，因為步行尺度緊湊而且文化層次密集。", text);
    }

    @Test
    void extractsStructuredContentAndSkipsReasoningBlocks() {
        Map<String, Object> response = Map.of(
                "choices", List.of(
                        Map.of(
                                "message", Map.of(
                                        "content", List.of(
                                                Map.of("type", "reasoning_content", "text", "should be ignored"),
                                                Map.of("type", "text", "text", "第一句"),
                                                Map.of("type", "output_text", "text", "第二句")
                                        )
                                )
                        )
                )
        );

        String text = ReflectionTestUtils.invokeMethod(gateway, "extractTextFromChatResponse", response);

        assertEquals("第一句\n第二句", text);
    }

    @Test
    void fallsBackToLegacyOutputChoicesShape() {
        Map<String, Object> response = Map.of(
                "output", Map.of(
                        "choices", List.of(
                                Map.of(
                                        "message", Map.of(
                                                "content", "這是 output.choices 形態的回應。"
                                        )
                                )
                        )
                )
        );

        String text = ReflectionTestUtils.invokeMethod(gateway, "extractTextFromChatResponse", response);

        assertEquals("這是 output.choices 形態的回應。", text);
    }

    @Test
    void rejectsLocalAssetDownloadBeforeNetworkCall() {
        AiProviderConfig provider = new AiProviderConfig();
        provider.setPlatformCode("custom");
        provider.setApiBaseUrl("https://example.com/v1");

        BusinessException ex = assertThrows(BusinessException.class,
                () -> gateway.downloadBinary(provider, "https://127.0.0.1/demo.png", 1000));

        assertEquals(4055, ex.getCode());
    }

    @Test
    @SuppressWarnings("unchecked")
    void parsesVoiceCatalogRowsFromOfficialLikeHtml() {
        String html = """
                <h3><b>CosyVoice-v3-Flash 大模型</b></h3>
                <table><tbody>
                <tr><td><b>适用场景</b></td><td><b>名称</b></td><td><b>音色名称（voice 参考值）</b></td><td><b>年龄</b></td><td><b>音色特质</b></td><td><b>合成文案</b></td><td><b>试听</b></td><td><b>语言</b></td><td><b>备注</b></td></tr>
                <tr>
                  <td>标杆音色</td>
                  <td>龙安洋</td>
                  <td>longanyang</td>
                  <td>20-30 岁</td>
                  <td><b>阳光大男孩</b></td>
                  <td>欢迎来到澳门漫游。</td>
                  <td><audio src="https://example.com/longanyang.mp3"></audio></td>
                  <td>中英双语</td>
                  <td></td>
                </tr>
                </tbody></table>
                """;

        List<DashScopeProviderGateway.VoiceDescriptor> result =
                (List<DashScopeProviderGateway.VoiceDescriptor>) ReflectionTestUtils.invokeMethod(
                        gateway,
                        "parseVoiceCatalog",
                        html,
                        "cosyvoice-v3-flash"
                );

        assertEquals(1, result.size());
        assertEquals("cosyvoice-v3-flash", result.get(0).modelCode());
        assertEquals("longanyang", result.get(0).voiceCode());
        assertEquals(List.of("zh", "en"), result.get(0).languageCodes());
    }

    @Test
    @SuppressWarnings("unchecked")
    void normalizesCantoneseLanguageHintToZh() throws Exception {
        JsonNode payload = new ObjectMapper().readTree("""
                {"languageCode":"yue","voice":"longanyang"}
                """);

        List<String> hints = (List<String>) ReflectionTestUtils.invokeMethod(gateway, "resolveLanguageHints", payload);
        boolean cantoneseSelection = (boolean) ReflectionTestUtils.invokeMethod(gateway, "isCantoneseSelection", payload);

        assertEquals(List.of("zh"), hints);
        assertEquals(true, cantoneseSelection);
    }

    @Test
    void buildImageRequestContextEnablesInterleaveForTextOnlyPromptAndIgnoresMetadataRoots() throws Exception {
        AiProviderConfig provider = new AiProviderConfig();
        provider.setModelName("wan2.6-image");
        provider.setProviderSettingsJson("""
                {"catalogMode":"image","preferredStyle":"tourism_game_cg"}
                """);

        AiProviderInventory inventory = new AiProviderInventory();
        inventory.setRawPayloadJson("""
                {"templateInventoryCode":"wan2.6-image","platformCode":"bailian"}
                """);

        Object context = ReflectionTestUtils.invokeMethod(
                gateway,
                "buildImageRequestContext",
                provider,
                inventory,
                "Generate a Macau landmark icon.",
                "wan2.6-image",
                null
        );

        String body = (String) ReflectionTestUtils.invokeMethod(context, "body");
        @SuppressWarnings("unchecked")
        Map<String, String> headers = (Map<String, String>) ReflectionTestUtils.invokeMethod(context, "headers");
        JsonNode requestBody = new ObjectMapper().readTree(body);

        assertEquals("enable", headers.get("X-DashScope-Async"));
        assertEquals("wan2.6-image", requestBody.path("model").asText());
        assertEquals("1024*1024", requestBody.path("parameters").path("size").asText());
        assertEquals(1, requestBody.path("parameters").path("n").asInt());
        assertTrue(requestBody.path("parameters").path("enable_interleave").asBoolean());
        assertNull(requestBody.get("templateInventoryCode"));
        assertNull(requestBody.get("platformCode"));
    }

    @Test
    void buildImageRequestContextMergesProviderInventoryAndRequestOverridesWithRequestPrecedence() throws Exception {
        AiProviderConfig provider = new AiProviderConfig();
        provider.setModelName("wan2.6-image");
        provider.setProviderSettingsJson("""
                {
                  "imageGeneration": {
                    "parameters": {
                      "size": "960*960",
                      "watermark": false
                    },
                    "headers": {
                      "X-Provider": "provider"
                    }
                  }
                }
                """);

        AiProviderInventory inventory = new AiProviderInventory();
        inventory.setRawPayloadJson("""
                {
                  "imageGeneration": {
                    "parameters": {
                      "n": 2,
                      "enableInterleave": false
                    }
                  }
                }
                """);

        Object context = ReflectionTestUtils.invokeMethod(
                gateway,
                "buildImageRequestContext",
                provider,
                inventory,
                "Generate a Macau landmark icon.",
                "wan2.6-image",
                """
                {
                  "size": "1536*1024",
                  "parameters": {
                    "enable_interleave": true,
                    "prompt_extend": true
                  },
                  "headers": {
                    "X-Request": "request"
                  }
                }
                """
        );

        String body = (String) ReflectionTestUtils.invokeMethod(context, "body");
        @SuppressWarnings("unchecked")
        Map<String, String> headers = (Map<String, String>) ReflectionTestUtils.invokeMethod(context, "headers");
        JsonNode requestBody = new ObjectMapper().readTree(body);

        assertEquals("provider", headers.get("X-Provider"));
        assertEquals("request", headers.get("X-Request"));
        assertEquals("enable", headers.get("X-DashScope-Async"));
        assertEquals("1536*1024", requestBody.path("parameters").path("size").asText());
        assertEquals(2, requestBody.path("parameters").path("n").asInt());
        assertTrue(requestBody.path("parameters").path("enable_interleave").asBoolean());
        assertTrue(requestBody.path("parameters").path("prompt_extend").asBoolean());
        assertEquals(false, requestBody.path("parameters").path("watermark").asBoolean(true));
    }
}
