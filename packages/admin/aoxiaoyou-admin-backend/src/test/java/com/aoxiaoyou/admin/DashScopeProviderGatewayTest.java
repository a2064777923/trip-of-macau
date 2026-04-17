package com.aoxiaoyou.admin;

import com.aoxiaoyou.admin.ai.config.AiCapabilityProperties;
import com.aoxiaoyou.admin.ai.provider.AiOutboundUrlGuard;
import com.aoxiaoyou.admin.ai.provider.DashScopeProviderGateway;
import com.aoxiaoyou.admin.common.exception.BusinessException;
import com.aoxiaoyou.admin.entity.AiProviderConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
}
