package com.aoxiaoyou.admin.ai.provider;

import com.aoxiaoyou.admin.ai.config.AiCapabilityProperties;
import com.aoxiaoyou.admin.common.exception.BusinessException;
import com.aoxiaoyou.admin.entity.AiProviderConfig;
import com.aoxiaoyou.admin.entity.AiProviderInventory;
import com.github.houbb.opencc4j.util.ZhConverterUtil;
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
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class DashScopeProviderGateway {

    private static final String DASHSCOPE_TTS_CUSTOMIZATION_URL =
            "https://dashscope.aliyuncs.com/api/v1/services/audio/tts/customization";
    private static final String BAILIAN_VOICE_LIST_URL =
            "https://help.aliyun.com/zh/model-studio/multimodal-timbre-list";
    private static final Pattern ROW_PATTERN = Pattern.compile(
            "<tr[^>]*>(.*?)</tr>",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );
    private static final Pattern CELL_PATTERN = Pattern.compile(
            "<td[^>]*>(.*?)</td>",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );
    private static final Pattern AUDIO_SRC_PATTERN = Pattern.compile(
            "<audio[^>]+src\\s*=\\s*[\"']([^\"']+)[\"']",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );
    private static final Pattern HTML_TAG_PATTERN = Pattern.compile("<[^>]+>");
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");

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

    public ImageTaskResult submitImageJob(AiProviderConfig provider,
                                          AiProviderInventory inventory,
                                          String apiKey,
                                          String prompt,
                                          String modelOverride,
                                          String requestPayloadJson) {
        String resolvedModel = StringUtils.hasText(modelOverride) ? modelOverride : provider.getModelName();
        ImageRequestContext requestContext = buildImageRequestContext(provider, inventory, prompt, resolvedModel, requestPayloadJson);

        Map<String, Object> response = postJson(
                provider,
                provider.getApiBaseUrl(),
                apiKey,
                requestContext.body(),
                provider.getRequestTimeoutMs(),
                requestContext.headers()
        );
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

    private ImageRequestContext buildImageRequestContext(AiProviderConfig provider,
                                                         AiProviderInventory inventory,
                                                         String prompt,
                                                         String resolvedModel,
                                                         String requestPayloadJson) {
        Map<String, Object> requestBody = buildDefaultImageRequestBody(resolvedModel, prompt);
        Map<String, String> headers = new LinkedHashMap<>();

        applyImageRequestConfig(requestBody, headers, readTree(provider == null ? null : provider.getProviderSettingsJson()));
        applyImageRequestConfig(requestBody, headers, readTree(provider == null ? null : provider.getFeatureFlagsJson()));
        applyImageRequestConfig(requestBody, headers, readTree(inventory == null ? null : inventory.getRawPayloadJson()));
        applyImageRequestConfig(requestBody, headers, readTree(inventory == null ? null : inventory.getFeatureFlagsJson()));
        applyImageRequestConfig(requestBody, headers, readTree(requestPayloadJson));

        ensureImageRequestDefaults(requestBody, resolvedModel, prompt);
        headers.putIfAbsent("X-DashScope-Async", "enable");
        return new ImageRequestContext(writeJson(requestBody), headers);
    }

    private Map<String, Object> buildDefaultImageRequestBody(String resolvedModel, String prompt) {
        Map<String, Object> textContent = new LinkedHashMap<>();
        textContent.put("text", StringUtils.hasText(prompt) ? prompt : "Generate an image.");

        List<Object> messageContent = new ArrayList<>();
        messageContent.add(textContent);

        Map<String, Object> message = new LinkedHashMap<>();
        message.put("role", "user");
        message.put("content", messageContent);

        List<Object> messages = new ArrayList<>();
        messages.add(message);

        Map<String, Object> input = new LinkedHashMap<>();
        input.put("messages", messages);

        Map<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("size", "1024*1024");
        parameters.put("n", 1);

        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("model", resolvedModel);
        requestBody.put("input", input);
        requestBody.put("parameters", parameters);
        return requestBody;
    }

    private void applyImageRequestConfig(Map<String, Object> requestBody, Map<String, String> headers, JsonNode source) {
        if (source == null || source.isMissingNode() || source.isNull() || !source.isObject()) {
            return;
        }
        for (JsonNode candidate : extractImageRequestConfigCandidates(source)) {
            mergeImageRequestCandidate(requestBody, headers, candidate);
        }
    }

    private List<JsonNode> extractImageRequestConfigCandidates(JsonNode source) {
        List<JsonNode> candidates = new ArrayList<>();
        if (looksLikeImageRequestConfig(source)) {
            candidates.add(source);
        }
        for (String fieldName : List.of(
                "imageGeneration",
                "image_generation",
                "imageRequest",
                "image_request",
                "dashscopeImage",
                "dashscope_image",
                "request"
        )) {
            JsonNode nested = source.path(fieldName);
            if (nested.isObject() && looksLikeImageRequestConfig(nested)) {
                candidates.add(nested);
            }
        }
        return candidates;
    }

    private boolean looksLikeImageRequestConfig(JsonNode node) {
        if (node == null || !node.isObject()) {
            return false;
        }
        for (String fieldName : List.of(
                "requestBody",
                "body",
                "headers",
                "parameters",
                "input",
                "messages",
                "model",
                "size",
                "style",
                "n",
                "stream",
                "seed",
                "watermark",
                "negative_prompt",
                "negativePrompt",
                "max_images",
                "maxImages",
                "enable_interleave",
                "enableInterleave",
                "prompt_extend",
                "promptExtend"
        )) {
            if (node.has(fieldName)) {
                return true;
            }
        }
        return false;
    }

    private void mergeImageRequestCandidate(Map<String, Object> requestBody, Map<String, String> headers, JsonNode candidate) {
        JsonNode explicitBody = candidate.has("requestBody") ? candidate.get("requestBody") : candidate.get("body");
        if (explicitBody != null && explicitBody.isObject()) {
            mergeNestedMap(requestBody, toObjectMap(explicitBody));
        }

        if (candidate.has("model")) {
            requestBody.put("model", convertJsonNode(candidate.get("model")));
        }
        if (candidate.has("input") && candidate.get("input").isObject()) {
            mergeNestedMap(ensureNestedMap(requestBody, "input"), toObjectMap(candidate.get("input")));
        }
        if (candidate.has("messages") && candidate.get("messages").isArray()) {
            ensureNestedMap(requestBody, "input").put("messages", convertJsonNode(candidate.get("messages")));
        }
        if (candidate.has("parameters") && candidate.get("parameters").isObject()) {
            mergeNestedMap(ensureNestedMap(requestBody, "parameters"), toObjectMap(candidate.get("parameters")));
        }
        applyLegacyImageParameterOverrides(candidate, ensureNestedMap(requestBody, "parameters"));
        if (candidate.has("headers") && candidate.get("headers").isObject()) {
            Map<String, Object> headerOverrides = toObjectMap(candidate.get("headers"));
            for (Map.Entry<String, Object> entry : headerOverrides.entrySet()) {
                if (StringUtils.hasText(entry.getKey()) && entry.getValue() != null) {
                    headers.put(entry.getKey(), String.valueOf(entry.getValue()));
                }
            }
        }
    }

    private void applyLegacyImageParameterOverrides(JsonNode candidate, Map<String, Object> parameters) {
        copyRootField(candidate, parameters, "size", "size");
        copyRootField(candidate, parameters, "style", "style");
        copyRootField(candidate, parameters, "n", "n");
        copyRootField(candidate, parameters, "stream", "stream");
        copyRootField(candidate, parameters, "seed", "seed");
        copyRootField(candidate, parameters, "watermark", "watermark");
        copyRootField(candidate, parameters, "negative_prompt", "negative_prompt");
        copyRootField(candidate, parameters, "negativePrompt", "negative_prompt");
        copyRootField(candidate, parameters, "max_images", "max_images");
        copyRootField(candidate, parameters, "maxImages", "max_images");
        copyRootField(candidate, parameters, "enable_interleave", "enable_interleave");
        copyRootField(candidate, parameters, "enableInterleave", "enable_interleave");
        copyRootField(candidate, parameters, "prompt_extend", "prompt_extend");
        copyRootField(candidate, parameters, "promptExtend", "prompt_extend");
    }

    private void copyRootField(JsonNode source, Map<String, Object> target, String sourceField, String targetField) {
        if (source.has(sourceField)) {
            target.put(targetField, convertJsonNode(source.get(sourceField)));
        }
    }

    private void ensureImageRequestDefaults(Map<String, Object> requestBody, String resolvedModel, String prompt) {
        requestBody.putIfAbsent("model", resolvedModel);

        Map<String, Object> input = ensureNestedMap(requestBody, "input");
        Object messages = input.get("messages");
        if (!(messages instanceof List<?> list) || list.isEmpty()) {
            input.put("messages", buildDefaultImageRequestBody(resolvedModel, prompt).get("input") instanceof Map<?, ?> defaultInput
                    ? defaultInput.get("messages")
                    : null);
        }

        Map<String, Object> parameters = ensureNestedMap(requestBody, "parameters");
        normalizeImageParameterAliases(parameters);
        parameters.putIfAbsent("size", "1024*1024");
        parameters.putIfAbsent("n", 1);
        if (!parameters.containsKey("enable_interleave") && isTextOnlyMessageRequest(input.get("messages"))) {
            parameters.put("enable_interleave", true);
        }
    }

    private void normalizeImageParameterAliases(Map<String, Object> parameters) {
        normalizeParameterAlias(parameters, "enableInterleave", "enable_interleave");
        normalizeParameterAlias(parameters, "maxImages", "max_images");
        normalizeParameterAlias(parameters, "negativePrompt", "negative_prompt");
        normalizeParameterAlias(parameters, "promptExtend", "prompt_extend");
    }

    private void normalizeParameterAlias(Map<String, Object> parameters, String alias, String canonical) {
        if (parameters.containsKey(alias) && !parameters.containsKey(canonical)) {
            parameters.put(canonical, parameters.remove(alias));
            return;
        }
        parameters.remove(alias);
    }

    private boolean isTextOnlyMessageRequest(Object messages) {
        if (!(messages instanceof List<?> messageList) || messageList.isEmpty()) {
            return false;
        }
        for (Object message : messageList) {
            if (!(message instanceof Map<?, ?> messageMap)) {
                continue;
            }
            Object content = messageMap.get("content");
            if (content instanceof List<?> contentList) {
                for (Object part : contentList) {
                    if (!(part instanceof Map<?, ?> partMap)) {
                        continue;
                    }
                    if (partMap.get("image") != null || partMap.get("image_url") != null) {
                        return false;
                    }
                    String type = asString(partMap.get("type"));
                    if ("image".equalsIgnoreCase(type) || "image_url".equalsIgnoreCase(type)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> ensureNestedMap(Map<String, Object> target, String fieldName) {
        Object current = target.get(fieldName);
        if (current instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        Map<String, Object> created = new LinkedHashMap<>();
        target.put(fieldName, created);
        return created;
    }

    private void mergeNestedMap(Map<String, Object> target, Map<String, Object> overrides) {
        for (Map.Entry<String, Object> entry : overrides.entrySet()) {
            Object current = target.get(entry.getKey());
            Object override = copyJsonCompatibleValue(entry.getValue());
            if (current instanceof Map<?, ?> currentMap && override instanceof Map<?, ?> overrideMap) {
                @SuppressWarnings("unchecked")
                Map<String, Object> mutableCurrent = (Map<String, Object>) currentMap;
                @SuppressWarnings("unchecked")
                Map<String, Object> mutableOverride = (Map<String, Object>) overrideMap;
                mergeNestedMap(mutableCurrent, mutableOverride);
            } else {
                target.put(entry.getKey(), override);
            }
        }
    }

    private Map<String, Object> toObjectMap(JsonNode node) {
        return objectMapper.convertValue(node, new TypeReference<>() {});
    }

    private Object convertJsonNode(JsonNode node) {
        return objectMapper.convertValue(node, Object.class);
    }

    private Object copyJsonCompatibleValue(Object value) {
        return objectMapper.convertValue(value, Object.class);
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

    public List<VoiceDescriptor> listSystemVoices(String targetModel, Integer timeoutMs) {
        String html = fetchHtml(BAILIAN_VOICE_LIST_URL, timeoutMs);
        return parseVoiceCatalog(html, targetModel);
    }

    public VoiceCloneResult createVoiceClone(AiProviderConfig provider,
                                             String apiKey,
                                             String targetModel,
                                             String prefix,
                                             String sourceUrl) {
        Map<String, Object> requestPayload = new LinkedHashMap<>();
        requestPayload.put("model", "voice-enrollment");
        requestPayload.put("input", Map.of(
                "action", "create_voice",
                "target_model", targetModel,
                "prefix", prefix,
                "url", sourceUrl
        ));
        Map<String, Object> response = postJson(
                provider,
                DASHSCOPE_TTS_CUSTOMIZATION_URL,
                apiKey,
                writeJson(requestPayload),
                provider.getRequestTimeoutMs()
        );
        return extractVoiceCloneResult(response);
    }

    public VoiceCloneResult queryVoiceClone(AiProviderConfig provider, String apiKey, String voiceId) {
        Map<String, Object> requestPayload = new LinkedHashMap<>();
        requestPayload.put("model", "voice-enrollment");
        requestPayload.put("input", Map.of(
                "action", "query_voice",
                "voice_id", voiceId
        ));
        Map<String, Object> response = postJson(
                provider,
                DASHSCOPE_TTS_CUSTOMIZATION_URL,
                apiKey,
                writeJson(requestPayload),
                provider.getRequestTimeoutMs()
        );
        return extractVoiceCloneResult(response);
    }

    public void deleteVoiceClone(AiProviderConfig provider, String apiKey, String voiceId) {
        Map<String, Object> requestPayload = new LinkedHashMap<>();
        requestPayload.put("model", "voice-enrollment");
        requestPayload.put("input", Map.of(
                "action", "delete_voice",
                "voice_id", voiceId
        ));
        postJson(
                provider,
                DASHSCOPE_TTS_CUSTOMIZATION_URL,
                apiKey,
                writeJson(requestPayload),
                provider.getRequestTimeoutMs()
        );
    }

    public TtsResult synthesizeSpeech(AiProviderConfig provider, String apiKey, String scriptText, String modelOverride, String requestPayloadJson) {
        JsonNode payload = readTree(requestPayloadJson);
        String resolvedModel = StringUtils.hasText(modelOverride) ? modelOverride : provider.getModelName();
        String voice = payload.path("voice").asText(null);
        if (!StringUtils.hasText(voice)) {
            voice = "longanyang";
        }
        String format = payload.path("format").asText(null);
        if (!StringUtils.hasText(format)) {
            format = "mp3";
        }
        int sampleRate = payload.path("sampleRate").isInt() ? payload.path("sampleRate").asInt() : 24000;
        if (sampleRate <= 0) {
            sampleRate = 24000;
        }
        boolean cantoneseSelection = isCantoneseSelection(payload);
        List<String> languageHints = resolveLanguageHints(payload);
        String instruction = normalizeTtsInstruction(payload.path("instruction").asText(null), cantoneseSelection);
        Float rate = readFloat(payload, "rate", "speechRate");
        Float pitch = readFloat(payload, "pitch");
        Integer volume = readInt(payload, "volume");
        String normalizedScriptText = normalizeTtsScript(scriptText, cantoneseSelection);

        Map<String, Object> input = new LinkedHashMap<>();
        input.put("text", normalizedScriptText);
        input.put("voice", voice);
        input.put("format", format);
        input.put("sample_rate", sampleRate);
        if (!languageHints.isEmpty()) {
            input.put("language_hints", List.of(languageHints.get(0)));
        }
        if (StringUtils.hasText(instruction) && supportsInstructionControl(resolvedModel)) {
            input.put("instruction", instruction);
        }
        if (rate != null) {
            input.put("rate", rate);
        }
        if (pitch != null) {
            input.put("pitch", pitch);
        }
        if (volume != null) {
            input.put("volume", volume);
        }

        String body = writeJson(Map.of(
                "model", resolvedModel,
                "input", input
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
            String requestUrl = StringUtils.hasText(url) ? url.trim() : url;
            String safeUrl;
            if (isDashScopeHttpAsset(provider, requestUrl)) {
                outboundUrlGuard.validateAssetDownloadUrl(provider, normalizeDashScopeAssetUrl(provider, requestUrl));
                safeUrl = requestUrl;
            } else {
                safeUrl = outboundUrlGuard.validateAssetDownloadUrl(provider, requestUrl);
            }
            long maxBytes = properties.getMaxDownloadBytes() == null ? 67108864L : properties.getMaxDownloadBytes();
            return buildClient(timeoutMs == null ? properties.getRequestTimeoutMs() : timeoutMs)
                    .get()
                    .uri(URI.create(safeUrl))
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

    private boolean isDashScopeHttpAsset(AiProviderConfig provider, String rawUrl) {
        if (!StringUtils.hasText(rawUrl)) {
            return false;
        }
        try {
            URI uri = URI.create(rawUrl.trim()).normalize();
            if (!"http".equalsIgnoreCase(uri.getScheme())) {
                return false;
            }
            String platformCode = provider == null || !StringUtils.hasText(provider.getPlatformCode())
                    ? ""
                    : provider.getPlatformCode().trim().toLowerCase(Locale.ROOT);
            String host = uri.getHost() == null ? "" : uri.getHost().toLowerCase(Locale.ROOT);
            return "bailian".equals(platformCode) && host.endsWith(".aliyuncs.com");
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    private String normalizeDashScopeAssetUrl(AiProviderConfig provider, String rawUrl) {
        if (!StringUtils.hasText(rawUrl)) {
            return rawUrl;
        }
        try {
            URI uri = URI.create(rawUrl.trim()).normalize();
            if (!"http".equalsIgnoreCase(uri.getScheme())) {
                return uri.toString();
            }
            String platformCode = provider == null || !StringUtils.hasText(provider.getPlatformCode())
                    ? ""
                    : provider.getPlatformCode().trim().toLowerCase(Locale.ROOT);
            String host = uri.getHost() == null ? "" : uri.getHost().toLowerCase(Locale.ROOT);
            if (!"bailian".equals(platformCode) || !host.endsWith(".aliyuncs.com")) {
                return uri.toString();
            }
            int port = uri.getPort();
            if (port != -1 && port != 80) {
                return uri.toString();
            }
            return new URI(
                    "https",
                    uri.getRawUserInfo(),
                    uri.getHost(),
                    -1,
                    uri.getRawPath(),
                    uri.getRawQuery(),
                    uri.getRawFragment()
            ).normalize().toString();
        } catch (IllegalArgumentException | URISyntaxException ex) {
            return rawUrl;
        }
    }

    private Map<String, Object> postJson(AiProviderConfig provider, String url, String apiKey, String body, Integer timeoutMs) {
        return postJson(provider, url, apiKey, body, timeoutMs, Map.of());
    }

    private Map<String, Object> postJson(AiProviderConfig provider,
                                         String url,
                                         String apiKey,
                                         String body,
                                         Integer timeoutMs,
                                         Map<String, String> extraHeaders) {
        try {
            String safeUrl = outboundUrlGuard.validateApiRequestUrl(provider, url);
            byte[] rawBytes = buildClient(timeoutMs)
                    .post()
                    .uri(safeUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON, MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .headers(headers -> extraHeaders.forEach(headers::set))
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

    private String fetchHtml(String url, Integer timeoutMs) {
        try {
            return buildClient(timeoutMs)
                    .get()
                    .uri(url)
                    .accept(MediaType.TEXT_HTML)
                    .retrieve()
                    .body(String.class);
        } catch (Exception ex) {
            throw new BusinessException(5058, "AI voice catalog fetch failed: " + ex.getMessage());
        }
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

    private String defaultString(String value, String fallback) {
        return StringUtils.hasText(value) ? value.trim() : fallback;
    }

    private Integer readInt(JsonNode payload, String... fieldNames) {
        for (String fieldName : fieldNames) {
            JsonNode node = payload.path(fieldName);
            if (node.isInt() || node.isLong()) {
                return node.asInt();
            }
            if (node.isTextual() && StringUtils.hasText(node.asText())) {
                try {
                    return Integer.parseInt(node.asText().trim());
                } catch (NumberFormatException ignored) {
                    return null;
                }
            }
        }
        return null;
    }

    private Float readFloat(JsonNode payload, String... fieldNames) {
        for (String fieldName : fieldNames) {
            JsonNode node = payload.path(fieldName);
            if (node.isFloat() || node.isDouble() || node.isInt() || node.isLong()) {
                return (float) node.asDouble();
            }
            if (node.isTextual() && StringUtils.hasText(node.asText())) {
                try {
                    return Float.parseFloat(node.asText().trim());
                } catch (NumberFormatException ignored) {
                    return null;
                }
            }
        }
        return null;
    }

    private boolean isCantoneseSelection(JsonNode payload) {
        String languageCode = payload.path("languageCode").asText(payload.path("language").asText(""));
        return "yue".equalsIgnoreCase(languageCode)
                || "cantonese".equalsIgnoreCase(languageCode)
                || "zh-yue".equalsIgnoreCase(languageCode);
    }

    private String normalizeTtsScript(String scriptText, boolean cantoneseSelection) {
        String normalized = StringUtils.hasText(scriptText) ? scriptText.trim() : "";
        if (!cantoneseSelection || !StringUtils.hasText(normalized)) {
            return normalized;
        }
        return convertTraditionalToSimplified(normalized);
    }

    private String normalizeTtsInstruction(String instruction, boolean cantoneseSelection) {
        if (!StringUtils.hasText(instruction)) {
            return instruction;
        }
        String normalized = instruction.trim();
        if (!cantoneseSelection) {
            return normalized;
        }
        return convertTraditionalToSimplified(normalized);
    }

    private String convertTraditionalToSimplified(String value) {
        if (!StringUtils.hasText(value)) {
            return value;
        }
        try {
            return ZhConverterUtil.toSimple(value.trim());
        } catch (RuntimeException ex) {
            return value.trim();
        }
    }

    private boolean supportsInstructionControl(String modelCode) {
        if (!StringUtils.hasText(modelCode)) {
            return false;
        }
        String normalized = modelCode.trim().toLowerCase(Locale.ROOT);
        return normalized.contains("cosyvoice-v1") || normalized.contains("cosyvoice-v2");
    }

    private List<String> resolveLanguageHints(JsonNode payload) {
        JsonNode hintsNode = payload.path("languageHints");
        if (hintsNode.isArray()) {
            List<String> hints = new ArrayList<>();
            hintsNode.forEach(item -> {
                if (item != null && item.isTextual() && StringUtils.hasText(item.asText())) {
                    hints.add(normalizeLanguageHint(item.asText()));
                }
            });
            return hints.stream().filter(StringUtils::hasText).toList();
        }
        String languageCode = payload.path("languageCode").asText(payload.path("language").asText(null));
        if (!StringUtils.hasText(languageCode)) {
            return Collections.emptyList();
        }
        String normalized = normalizeLanguageHint(languageCode);
        return StringUtils.hasText(normalized) ? List.of(normalized) : Collections.emptyList();
    }

    private String normalizeLanguageHint(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "mandarin", "putonghua", "zh", "zh-cn", "zh-hans", "zh-hant" -> "zh";
            case "cantonese", "yue", "zh-yue", "yue-hk" -> "zh";
            case "english", "en", "en-us", "en-gb" -> "en";
            case "portuguese", "pt", "pt-pt", "pt-br" -> "pt";
            case "french", "fr" -> "fr";
            case "german", "de" -> "de";
            case "japanese", "ja" -> "ja";
            case "korean", "ko" -> "ko";
            case "russian", "ru" -> "ru";
            case "thai", "th" -> "th";
            case "indonesian", "id" -> "id";
            case "vietnamese", "vi" -> "vi";
            default -> normalized.length() == 2 ? normalized : null;
        };
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

    private List<VoiceDescriptor> parseVoiceCatalog(String html, String targetModel) {
        if (!StringUtils.hasText(html)) {
            return List.of();
        }
        String source = defaultString(html, "");
        String lowerSource = source.toLowerCase(Locale.ROOT);
        List<VoiceDescriptor> discovered = new ArrayList<>();
        int searchFrom = 0;
        while (searchFrom >= 0 && searchFrom < lowerSource.length()) {
            int h3Start = lowerSource.indexOf("<h3", searchFrom);
            if (h3Start < 0) {
                break;
            }
            int h3End = lowerSource.indexOf("</h3>", h3Start);
            if (h3End < 0) {
                break;
            }
            String heading = normalizeText(source.substring(h3Start, Math.min(h3End + 5, source.length())));
            String modelCode = extractModelCode(heading);
            int tableStart = lowerSource.indexOf("<table", h3End);
            if (tableStart < 0) {
                break;
            }
            int nextH3 = lowerSource.indexOf("<h3", h3End + 5);
            if (nextH3 >= 0 && tableStart > nextH3) {
                searchFrom = nextH3;
                continue;
            }
            int tableEnd = lowerSource.indexOf("</table>", tableStart);
            if (tableEnd < 0) {
                break;
            }
            int tbodyStart = lowerSource.indexOf("<tbody", tableStart);
            if (tbodyStart < 0 || tbodyStart > tableEnd) {
                searchFrom = tableEnd + "</table>".length();
                continue;
            }
            int tbodyContentStart = lowerSource.indexOf(">", tbodyStart);
            int tbodyEnd = lowerSource.indexOf("</tbody>", tbodyContentStart);
            if (tbodyContentStart < 0 || tbodyEnd < 0 || tbodyEnd > tableEnd) {
                searchFrom = tableEnd + "</table>".length();
                continue;
            }
            if (!StringUtils.hasText(modelCode)) {
                searchFrom = tableEnd + "</table>".length();
                continue;
            }
            if (StringUtils.hasText(targetModel) && !modelCode.equalsIgnoreCase(targetModel.trim())) {
                searchFrom = tableEnd + "</table>".length();
                continue;
            }
            String tbodyHtml = source.substring(tbodyContentStart + 1, tbodyEnd);
            discovered.addAll(parseVoiceRows(modelCode, tbodyHtml));
            searchFrom = tableEnd + "</table>".length();
        }
        if (discovered.isEmpty()) {
            return List.of();
        }
        Map<String, VoiceDescriptor> deduped = new LinkedHashMap<>();
        for (VoiceDescriptor voice : discovered) {
            deduped.putIfAbsent(voice.modelCode() + "::" + voice.voiceCode(), voice);
        }
        return new ArrayList<>(deduped.values());
    }

    private List<VoiceDescriptor> parseVoiceRows(String modelCode, String tbodyHtml) {
        List<VoiceDescriptor> voices = new ArrayList<>();
        Matcher rowMatcher = ROW_PATTERN.matcher(defaultString(tbodyHtml, ""));
        String currentScene = null;
        while (rowMatcher.find()) {
            String rowHtml = rowMatcher.group(1);
            List<String> cellHtmlList = new ArrayList<>();
            Matcher cellMatcher = CELL_PATTERN.matcher(rowHtml);
            while (cellMatcher.find()) {
                cellHtmlList.add(cellMatcher.group(1));
            }
            if (cellHtmlList.size() < 8) {
                continue;
            }
            int offset = 0;
            if (cellHtmlList.size() >= 9) {
                currentScene = normalizeText(cellHtmlList.get(0));
                offset = 1;
            }
            String displayName = normalizeText(cellHtmlList.get(offset));
            String voiceCode = normalizeText(cellHtmlList.get(offset + 1));
            if (!StringUtils.hasText(voiceCode)
                    || "名称".equals(voiceCode)
                    || "适用场景".equals(displayName)
                    || voiceCode.contains("voice 参考值")
                    || voiceCode.contains("音色名称")
                    || "名称".equals(displayName)) {
                continue;
            }
            String age = normalizeText(cellHtmlList.get(offset + 2));
            String persona = normalizeText(cellHtmlList.get(offset + 3));
            String previewText = normalizeText(cellHtmlList.get(offset + 4));
            String previewUrl = extractAudioSrc(cellHtmlList.get(offset + 5));
            String languageText = normalizeText(cellHtmlList.get(offset + 6));
            String remark = cellHtmlList.size() > offset + 7 ? normalizeText(cellHtmlList.get(offset + 7)) : null;
            List<String> languageCodes = parseLanguageCodes(languageText);
            Map<String, Object> metadata = new LinkedHashMap<>();
            metadata.put("scene", currentScene);
            metadata.put("age", age);
            metadata.put("persona", persona);
            metadata.put("languageText", languageText);
            metadata.put("remark", remark);
            voices.add(new VoiceDescriptor(
                    modelCode,
                    displayName,
                    voiceCode,
                    previewUrl,
                    previewText,
                    languageCodes,
                    writeJsonSafely(metadata)
            ));
        }
        return voices;
    }

    private String extractModelCode(String heading) {
        String normalized = defaultString(heading, "").toLowerCase(Locale.ROOT);
        if (normalized.contains("cosyvoice-v3.5-flash")) {
            return "cosyvoice-v3.5-flash";
        }
        if (normalized.contains("cosyvoice-v3.5-plus")) {
            return "cosyvoice-v3.5-plus";
        }
        if (normalized.contains("cosyvoice-v3-flash")) {
            return "cosyvoice-v3-flash";
        }
        if (normalized.contains("cosyvoice-v3-plus")) {
            return "cosyvoice-v3-plus";
        }
        if (normalized.contains("cosyvoice-v2")) {
            return "cosyvoice-v2";
        }
        if (normalized.contains("cosyvoice-v1")) {
            return "cosyvoice-v1";
        }
        return null;
    }

    private String extractAudioSrc(String html) {
        Matcher matcher = AUDIO_SRC_PATTERN.matcher(defaultString(html, ""));
        return matcher.find() ? matcher.group(1) : null;
    }

    private List<String> parseLanguageCodes(String text) {
        if (!StringUtils.hasText(text)) {
            return List.of();
        }
        String normalized = text.trim();
        List<String> codes = new ArrayList<>();
        if (normalized.contains("中") || normalized.contains("普通话") || normalized.contains("中文")) {
            codes.add("zh");
        }
        if (normalized.contains("广东话") || normalized.contains("粤语")) {
            codes.add("yue");
        }
        if (normalized.contains("英")) {
            codes.add("en");
        }
        if (normalized.contains("法")) {
            codes.add("fr");
        }
        if (normalized.contains("德")) {
            codes.add("de");
        }
        if (normalized.contains("日")) {
            codes.add("ja");
        }
        if (normalized.contains("韩")) {
            codes.add("ko");
        }
        if (normalized.contains("俄")) {
            codes.add("ru");
        }
        if (normalized.contains("葡萄牙")) {
            codes.add("pt");
        }
        if (normalized.contains("泰")) {
            codes.add("th");
        }
        if (normalized.contains("印尼")) {
            codes.add("id");
        }
        if (normalized.contains("越南")) {
            codes.add("vi");
        }
        return codes.stream().distinct().toList();
    }

    private String normalizeText(String html) {
        if (!StringUtils.hasText(html)) {
            return null;
        }
        String withSpaces = html.replace("<span class=\"help-letter-space\"></span>", " ");
        String noTags = HTML_TAG_PATTERN.matcher(withSpaces).replaceAll(" ");
        String decoded = noTags
                .replace("&nbsp;", " ")
                .replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&quot;", "\"")
                .replace("&#39;", "'");
        String compact = WHITESPACE_PATTERN.matcher(decoded).replaceAll(" ").trim();
        return StringUtils.hasText(compact) ? compact : null;
    }

    private VoiceCloneResult extractVoiceCloneResult(Map<String, Object> response) {
        String voiceId = findFirstString(response, "voice_id", "voiceId");
        String status = findFirstString(response, "status", "voice_status", "voiceStatus");
        return new VoiceCloneResult(
                voiceId,
                status,
                writeJsonSafely(response)
        );
    }

    private String findFirstString(Object current, String... keys) {
        if (current == null) {
            return null;
        }
        if (current instanceof Map<?, ?> map) {
            for (String key : keys) {
                Object value = map.get(key);
                if (value != null) {
                    String text = asString(value);
                    if (StringUtils.hasText(text)) {
                        return text;
                    }
                }
            }
            for (Object value : map.values()) {
                String nested = findFirstString(value, keys);
                if (StringUtils.hasText(nested)) {
                    return nested;
                }
            }
            return null;
        }
        if (current instanceof List<?> list) {
            for (Object item : list) {
                String nested = findFirstString(item, keys);
                if (StringUtils.hasText(nested)) {
                    return nested;
                }
            }
        }
        return null;
    }

    public record ChatResult(String resolvedModel, String previewText, long latencyMs) {
    }

    public record ImageTaskResult(String resolvedModel, String taskId, String taskStatus) {
    }

    public record ImagePollResult(String taskStatus, List<String> urls, String metadataJson) {
    }

    public record TtsResult(String resolvedModel, String assetUrl, long latencyMs, String metadataJson) {
    }

    private record ImageRequestContext(String body, Map<String, String> headers) {
    }

    public record ModelDescriptor(String id, String displayName, String inventoryType, String owner, String rawPayloadJson) {
    }

    public record VoiceDescriptor(String modelCode,
                                  String displayName,
                                  String voiceCode,
                                  String previewUrl,
                                  String previewText,
                                  List<String> languageCodes,
                                  String rawPayloadJson) {
    }

    public record VoiceCloneResult(String voiceId, String status, String rawPayloadJson) {
    }
}
