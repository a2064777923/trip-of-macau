package com.aoxiaoyou.admin.ai.provider;

import com.aoxiaoyou.admin.ai.config.AiCapabilityProperties;
import com.aoxiaoyou.admin.common.exception.BusinessException;
import com.aoxiaoyou.admin.entity.AiProviderConfig;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class DashScopeProviderGateway {

    private final ObjectMapper objectMapper;
    private final AiCapabilityProperties properties;
    private final AiOutboundUrlGuard outboundUrlGuard;

    public ChatResult testChatProvider(AiProviderConfig provider, String apiKey, String prompt, String modelOverride, Integer timeoutMs) {
        return generateText(
                provider,
                apiKey,
                "You are a connectivity probe for Trip of Macau admin.",
                StringUtils.hasText(prompt) ? prompt : "Ping from Trip of Macau admin.",
                modelOverride,
                timeoutMs
        );
    }

    public ChatResult generateText(AiProviderConfig provider, String apiKey, String systemPrompt, String prompt, String modelOverride, Integer timeoutMs) {
        String resolvedModel = StringUtils.hasText(modelOverride) ? modelOverride : provider.getModelName();
        Map<String, Object> requestPayload = new java.util.LinkedHashMap<>();
        requestPayload.put("model", resolvedModel);
        requestPayload.put("messages", List.of(
                Map.of("role", "system", "content", StringUtils.hasText(systemPrompt) ? systemPrompt : "You are an AI assistant."),
                Map.of("role", "user", "content", StringUtils.hasText(prompt) ? prompt : "Ping from Trip of Macau admin.")
        ));
        requestPayload.put("temperature", 0);
        requestPayload.put("max_tokens", 64);
        if (shouldDisableThinkingByDefault(resolvedModel)) {
            requestPayload.put("enable_thinking", false);
        }
        String body = writeJson(requestPayload);

        long startedAt = System.currentTimeMillis();
        Map<String, Object> response = postJson(provider, resolveCompatibleEndpoint(provider, "chat/completions"), apiKey, body, timeoutMs);
        return new ChatResult(
                resolvedModel,
                extractTextFromChatResponse(response),
                System.currentTimeMillis() - startedAt
        );
    }

    public ImageTaskResult submitImageJob(AiProviderConfig provider, String apiKey, String prompt, String modelOverride, String requestPayloadJson) {
        String resolvedModel = StringUtils.hasText(modelOverride) ? modelOverride : provider.getModelName();
        JsonNode payload = readTree(requestPayloadJson);
        String size = payload.path("size").asText("1024*1024");
        String style = payload.path("style").asText(null);

        Map<String, Object> parameters = new java.util.LinkedHashMap<>();
        parameters.put("size", size);
        parameters.put("n", 1);
        if (StringUtils.hasText(style)) {
            parameters.put("style", style);
        }

        String body = writeJson(Map.of(
                "model", resolvedModel,
                "input", Map.of("prompt", prompt),
                "parameters", parameters
        ));

        Map<String, Object> response = postJson(provider, provider.getApiBaseUrl(), apiKey, body, provider.getRequestTimeoutMs());
        Map<String, Object> output = asMap(response.get("output"));
        return new ImageTaskResult(
                resolvedModel,
                output == null ? null : asString(output.get("task_id")),
                output == null ? null : asString(output.get("task_status"))
        );
    }

    public ImagePollResult pollImageTask(AiProviderConfig provider, String apiKey, String taskId) {
        Map<String, Object> response = getJson(provider, buildTaskUrl(taskId), apiKey, provider.getRequestTimeoutMs());
        Map<String, Object> output = asMap(response.get("output"));
        String status = output == null ? null : asString(output.get("task_status"));
        return new ImagePollResult(
                status,
                extractUrls(response),
                writeJsonSafely(output)
        );
    }

    public List<ModelDescriptor> listModels(AiProviderConfig provider, String apiKey, Integer timeoutMs) {
        Map<String, Object> response = getJson(provider, resolveCompatibleEndpoint(provider, "models"), apiKey, timeoutMs);
        Object data = response.get("data");
        if (!(data instanceof List<?> items)) {
            return List.of();
        }
        List<ModelDescriptor> models = new ArrayList<>();
        for (Object item : items) {
            if (item instanceof Map<?, ?> rawModel) {
                String id = asString(rawModel.get("id"));
                if (!StringUtils.hasText(id)) {
                    continue;
                }
                String ownedBy = asString(rawModel.get("owned_by"));
                String object = asString(rawModel.get("object"));
                models.add(new ModelDescriptor(
                        id,
                        id,
                        StringUtils.hasText(object) ? object : "model",
                        ownedBy,
                        writeJsonSafely(rawModel)
                ));
            }
        }
        return models;
    }

    public TtsResult synthesizeSpeech(AiProviderConfig provider, String apiKey, String scriptText, String modelOverride, String requestPayloadJson) {
        JsonNode payload = readTree(requestPayloadJson);
        String resolvedModel = StringUtils.hasText(modelOverride) ? modelOverride : provider.getModelName();
        String voice = payload.path("voice").asText("longanyang");
        String format = payload.path("format").asText("mp3");
        int sampleRate = payload.path("sampleRate").asInt(24000);

        String body = writeJson(Map.of(
                "model", resolvedModel,
                "input", Map.of(
                        "text", scriptText,
                        "voice", voice,
                        "format", format,
                        "sample_rate", sampleRate
                )
        ));

        long startedAt = System.currentTimeMillis();
        Map<String, Object> response = postJson(provider, provider.getApiBaseUrl(), apiKey, body, provider.getRequestTimeoutMs());
        List<String> urls = extractUrls(response);
        return new TtsResult(
                resolvedModel,
                urls.isEmpty() ? null : urls.get(0),
                System.currentTimeMillis() - startedAt,
                writeJsonSafely(response.get("output"))
        );
    }

    public byte[] downloadBinary(AiProviderConfig provider, String url, Integer timeoutMs) {
        try {
            String safeUrl = outboundUrlGuard.validateAssetDownloadUrl(provider, url);
            long maxBytes = properties.getMaxDownloadBytes() == null ? 67108864L : properties.getMaxDownloadBytes();
            return buildClient(timeoutMs == null ? properties.getRequestTimeoutMs() : timeoutMs)
                    .get()
                    .uri(safeUrl)
                    .exchange((request, response) -> {
                        if (response.getStatusCode().is3xxRedirection()) {
                            throw new BusinessException(5058, "AI asset download redirect is not allowed");
                        }
                        if (response.getStatusCode().isError()) {
                            String errorBody = StreamUtils.copyToString(response.getBody(), StandardCharsets.UTF_8);
                            throw new BusinessException(5058, "AI asset download failed: HTTP "
                                    + response.getStatusCode().value() + " " + trimResponseBody(errorBody));
                        }
                        long contentLength = response.getHeaders().getContentLength();
                        if (contentLength > maxBytes) {
                            throw new BusinessException(5058, "AI asset download exceeded size limit");
                        }
                        return readLimitedBytes(response.getBody(), maxBytes);
                    });
        } catch (RestClientException ex) {
            throw new BusinessException(5058, "AI asset download failed: " + ex.getMessage());
        }
    }

    private Map<String, Object> postJson(AiProviderConfig provider, String url, String apiKey, String body, Integer timeoutMs) {
        try {
            String safeUrl = outboundUrlGuard.validateApiRequestUrl(provider, url);
            byte[] rawBytes = buildClient(timeoutMs)
                    .post()
                    .uri(safeUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON, MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .body(body)
                    .exchange((request, response) -> {
                        if (response.getStatusCode().is3xxRedirection()) {
                            throw new BusinessException(5058, "AI provider redirect is not allowed");
                        }
                        if (response.getStatusCode().isError()) {
                            String errorBody = StreamUtils.copyToString(response.getBody(), StandardCharsets.UTF_8);
                            throw new BusinessException(5058, "AI provider call failed: HTTP "
                                    + response.getStatusCode().value() + " " + trimResponseBody(errorBody));
                        }
                        return StreamUtils.copyToByteArray(response.getBody());
                    });
            String raw = rawBytes == null ? "" : new String(rawBytes, StandardCharsets.UTF_8);
            return objectMapper.readValue(raw, new TypeReference<>() {});
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException(5058, "AI provider call failed: " + ex.getMessage());
        }
    }

    private Map<String, Object> getJson(AiProviderConfig provider, String url, String apiKey, Integer timeoutMs) {
        try {
            String safeUrl = outboundUrlGuard.validateApiRequestUrl(provider, url);
            byte[] rawBytes = buildClient(timeoutMs)
                    .get()
                    .uri(safeUrl)
                    .accept(MediaType.APPLICATION_JSON, MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .exchange((request, response) -> {
                        if (response.getStatusCode().is3xxRedirection()) {
                            throw new BusinessException(5058, "AI provider redirect is not allowed");
                        }
                        if (response.getStatusCode().isError()) {
                            String errorBody = StreamUtils.copyToString(response.getBody(), StandardCharsets.UTF_8);
                            throw new BusinessException(5058, "AI provider poll failed: HTTP "
                                    + response.getStatusCode().value() + " " + trimResponseBody(errorBody));
                        }
                        return StreamUtils.copyToByteArray(response.getBody());
                    });
            String raw = rawBytes == null ? "" : new String(rawBytes, StandardCharsets.UTF_8);
            return objectMapper.readValue(raw, new TypeReference<>() {});
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException(5058, "AI provider poll failed: " + ex.getMessage());
        }
    }

    private RestClient buildClient(Integer timeoutMs) {
        int effectiveTimeout = timeoutMs == null ? properties.getRequestTimeoutMs() : timeoutMs;
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory() {
            @Override
            protected void prepareConnection(java.net.HttpURLConnection connection, String httpMethod) throws IOException {
                super.prepareConnection(connection, httpMethod);
                connection.setInstanceFollowRedirects(false);
            }
        };
        requestFactory.setConnectTimeout(effectiveTimeout);
        requestFactory.setReadTimeout(effectiveTimeout);
        return RestClient.builder().requestFactory(requestFactory).build();
    }

    private String buildTaskUrl(String taskId) {
        return properties.getDashscopeTaskStatusBaseUrl().replaceAll("/+$", "") + "/" + taskId;
    }

    private String resolveCompatibleEndpoint(AiProviderConfig provider, String suffix) {
        String baseUrl = provider.getApiBaseUrl();
        if (!StringUtils.hasText(baseUrl)) {
            throw new BusinessException(4055, "AI provider base URL is missing");
        }
        String normalizedBase = baseUrl.trim().replaceAll("/+$", "");
        if (normalizedBase.endsWith("/" + suffix)) {
            return normalizedBase;
        }
        if (normalizedBase.endsWith("/v1") || normalizedBase.endsWith("/v3")) {
            return normalizedBase + "/" + suffix;
        }
        if (normalizedBase.contains("/chat/completions") && Objects.equals(suffix, "models")) {
            return normalizedBase.replace("/chat/completions", "/models");
        }
        if (normalizedBase.contains("/models") && Objects.equals(suffix, "chat/completions")) {
            return normalizedBase.replace("/models", "/chat/completions");
        }
        return normalizedBase + "/" + suffix;
    }

    private JsonNode readTree(String json) {
        if (!StringUtils.hasText(json)) {
            return objectMapper.createObjectNode();
        }
        try {
            return objectMapper.readTree(json);
        } catch (Exception ex) {
            throw new BusinessException(4055, "AI request payload JSON is invalid");
        }
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception ex) {
            throw new BusinessException(5003, "AI request payload could not be serialized");
        }
    }

    private String writeJsonSafely(Object value) {
        try {
            return value == null ? null : objectMapper.writeValueAsString(value);
        } catch (Exception ex) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> asMap(Object value) {
        return value instanceof Map<?, ?> map ? (Map<String, Object>) map : null;
    }

    private String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private String extractTextFromChatResponse(Map<String, Object> response) {
        String topLevelText = extractTextFromChoices(response.get("choices"));
        if (StringUtils.hasText(topLevelText)) {
            return topLevelText;
        }
        Map<String, Object> output = asMap(response.get("output"));
        if (output != null) {
            String outputText = extractTextFromChoices(output.get("choices"));
            if (StringUtils.hasText(outputText)) {
                return outputText;
            }
        }
        return writeJsonSafely(response);
    }

    private String extractTextFromChoices(Object choices) {
        if (!(choices instanceof List<?> choiceList)) {
            return null;
        }
        for (Object choice : choiceList) {
            String extracted = extractTextFromChoice(choice);
            if (StringUtils.hasText(extracted)) {
                return extracted;
            }
        }
        return null;
    }

    private String extractTextFromChoice(Object choice) {
        if (!(choice instanceof Map<?, ?> choiceMap)) {
            return null;
        }
        Object message = choiceMap.get("message");
        if (message instanceof Map<?, ?> messageMap) {
            String messageText = extractTextFromMessage(messageMap);
            if (StringUtils.hasText(messageText)) {
                return messageText;
            }
        }
        String directText = normalizeExtractedText(choiceMap.get("text"));
        if (StringUtils.hasText(directText)) {
            return directText;
        }
        Object delta = choiceMap.get("delta");
        if (delta instanceof Map<?, ?> deltaMap) {
            return extractTextFromMessage(deltaMap);
        }
        return null;
    }

    private String extractTextFromMessage(Map<?, ?> messageMap) {
        String contentText = normalizeExtractedText(messageMap.get("content"));
        if (StringUtils.hasText(contentText)) {
            return contentText;
        }
        return normalizeExtractedText(messageMap.get("text"));
    }

    private String normalizeExtractedText(Object content) {
        if (content instanceof String text) {
            return StringUtils.hasText(text) ? text.trim() : null;
        }
        if (content instanceof List<?> contentList) {
            StringBuilder builder = new StringBuilder();
            for (Object item : contentList) {
                String piece = normalizeContentPart(item);
                if (!StringUtils.hasText(piece)) {
                    continue;
                }
                if (builder.length() > 0) {
                    builder.append('\n');
                }
                builder.append(piece.trim());
            }
            return builder.length() == 0 ? null : builder.toString();
        }
        if (content instanceof Map<?, ?> contentMap) {
            String textValue = firstText(contentMap, "text", "content", "output_text", "value");
            return StringUtils.hasText(textValue) ? textValue.trim() : null;
        }
        return null;
    }

    private String normalizeContentPart(Object part) {
        if (part instanceof String text) {
            return text;
        }
        if (!(part instanceof Map<?, ?> partMap)) {
            return null;
        }
        String type = asString(partMap.get("type"));
        if ("reasoning".equalsIgnoreCase(type) || "reasoning_content".equalsIgnoreCase(type)) {
            return null;
        }
        String textValue = firstText(partMap, "text", "content", "output_text");
        if (StringUtils.hasText(textValue)) {
            return textValue;
        }
        Object nestedText = partMap.get("text");
        if (nestedText instanceof Map<?, ?> nestedMap) {
            return firstText(nestedMap, "value", "content", "text");
        }
        return null;
    }

    private String firstText(Map<?, ?> source, String... keys) {
        for (String key : keys) {
            Object value = source.get(key);
            String normalized = normalizeExtractedText(value);
            if (StringUtils.hasText(normalized)) {
                return normalized;
            }
            if (value != null) {
                String fallback = asString(value);
                if (StringUtils.hasText(fallback)) {
                    return fallback.trim();
                }
            }
        }
        return null;
    }

    private String trimResponseBody(String body) {
        if (!StringUtils.hasText(body)) {
            return "";
        }
        String normalized = body.trim();
        return normalized.length() <= 240 ? normalized : normalized.substring(0, 240);
    }

    private byte[] readLimitedBytes(InputStream inputStream, long maxBytes) throws IOException {
        if (inputStream == null) {
            return new byte[0];
        }
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        long total = 0;
        int read;
        while ((read = inputStream.read(buffer)) != -1) {
            total += read;
            if (total > maxBytes) {
                throw new BusinessException(5058, "AI asset download exceeded size limit");
            }
            outputStream.write(buffer, 0, read);
        }
        return outputStream.toByteArray();
    }

    private boolean shouldDisableThinkingByDefault(String modelCode) {
        if (!StringUtils.hasText(modelCode)) {
            return false;
        }
        String normalized = modelCode.trim().toLowerCase(Locale.ROOT);
        return normalized.startsWith("qwen3.5-") || normalized.startsWith("qwen3.6-");
    }

    private List<String> extractUrls(Object root) {
        List<String> urls = new ArrayList<>();
        collectUrls(root, urls);
        return urls;
    }

    @SuppressWarnings("unchecked")
    private void collectUrls(Object current, List<String> urls) {
        if (current == null) {
            return;
        }
        if (current instanceof Map<?, ?> map) {
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) map).entrySet()) {
                Object value = entry.getValue();
                if (value instanceof String text) {
                    String lower = text.toLowerCase(Locale.ROOT);
                    if ((lower.startsWith("http://") || lower.startsWith("https://")) && !urls.contains(text)) {
                        urls.add(text);
                    }
                } else {
                    collectUrls(value, urls);
                }
            }
            return;
        }
        if (current instanceof List<?> list) {
            for (Object item : list) {
                collectUrls(item, urls);
            }
        }
    }

    public record ChatResult(String resolvedModel, String previewText, long latencyMs) {
    }

    public record ImageTaskResult(String resolvedModel, String taskId, String taskStatus) {
    }

    public record ImagePollResult(String taskStatus, List<String> urls, String metadataJson) {
    }

    public record TtsResult(String resolvedModel, String assetUrl, long latencyMs, String metadataJson) {
    }

    public record ModelDescriptor(String id, String displayName, String inventoryType, String owner, String rawPayloadJson) {
    }
}
