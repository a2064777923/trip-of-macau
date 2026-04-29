package com.aoxiaoyou.admin.translation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class TranslatorsPythonBridgeAdapter implements TranslationEngineAdapter {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    private final TranslationProperties translationProperties;
    private final ObjectMapper objectMapper;

    @Override
    public TranslationAttemptResult translate(TranslationCommand command) {
        if (!translationProperties.isBridgeEnabled()) {
            return failed(command.getEngine(), "translation bridge is disabled");
        }
        Path scriptPath = resolveScriptPath(translationProperties.getBridgeScriptPath());
        if (scriptPath == null) {
            return failed(command.getEngine(), "translation bridge script was not found");
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("engine", command.getEngine());
        payload.put("sourceLocale", command.getSourceLocale().getCode());
        payload.put("targetLocale", command.getTargetLocale().getCode());
        payload.put("text", command.getText());
        payload.put("timeoutMs", command.getTimeoutMs());

        ProcessBuilder processBuilder = new ProcessBuilder(
                translationProperties.getPythonCommand(),
                scriptPath.toString()
        );
        processBuilder.environment().put("PYTHONIOENCODING", StandardCharsets.UTF_8.name());
        processBuilder.environment().put("PYTHONUTF8", "1");

        try {
            Process process = processBuilder.start();
            try (OutputStreamWriter writer = new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8)) {
                writer.write(objectMapper.writeValueAsString(payload));
                writer.flush();
            }

            boolean finished = process.waitFor(command.getTimeoutMs(), TimeUnit.MILLISECONDS);
            if (!finished) {
                process.destroyForcibly();
                return failed(command.getEngine(), "translation bridge timed out");
            }

            String stdout = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            String stderr = new String(process.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
            Map<String, Object> response = parseResponse(stdout);
            boolean ok = Boolean.TRUE.equals(response.get("ok"));
            String text = valueOf(response.get("text"));
            String error = valueOf(response.get("error"));

            if (ok && StringUtils.hasText(text)) {
                return TranslationAttemptResult.builder()
                        .success(true)
                        .translatedText(text)
                        .engine(command.getEngine())
                        .message(valueOf(response.get("message")))
                        .build();
            }

            String message = StringUtils.hasText(error)
                    ? error
                    : StringUtils.hasText(stderr) ? stderr.trim() : "translation bridge returned no result";
            return failed(command.getEngine(), message);
        } catch (IOException ex) {
            return failed(command.getEngine(), "failed to start translation bridge: " + ex.getMessage());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return failed(command.getEngine(), "translation bridge interrupted");
        }
    }

    private Path resolveScriptPath(String configuredPath) {
        if (!StringUtils.hasText(configuredPath)) {
            return null;
        }
        Path path = Paths.get(configuredPath.trim());
        if (path.isAbsolute() && Files.exists(path)) {
            return path.normalize();
        }

        Path current = Paths.get("").toAbsolutePath();
        for (int depth = 0; depth < 6 && current != null; depth++) {
            Path candidate = current.resolve(path).normalize();
            if (Files.exists(candidate)) {
                return candidate;
            }
            current = current.getParent();
        }
        return null;
    }

    private Map<String, Object> parseResponse(String stdout) {
        if (!StringUtils.hasText(stdout)) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(stdout, MAP_TYPE);
        } catch (IOException ignored) {
            return Map.of("ok", false, "error", stdout.trim());
        }
    }

    private String valueOf(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }

    private TranslationAttemptResult failed(String engine, String message) {
        return TranslationAttemptResult.builder()
                .success(false)
                .engine(engine)
                .message(StringUtils.hasText(message) ? message.trim() : "translation failed")
                .build();
    }
}
