package com.aoxiaoyou.admin;

import com.aoxiaoyou.admin.common.enums.LocaleCode;
import com.aoxiaoyou.admin.translation.TranslationAttemptResult;
import com.aoxiaoyou.admin.translation.TranslationCommand;
import com.aoxiaoyou.admin.translation.TranslationProperties;
import com.aoxiaoyou.admin.translation.TranslatorsPythonBridgeAdapter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class TranslatorsPythonBridgeAdapterTest {

    private static final String TRADITIONAL_TEXT = "\u7e41\u9ad4\u6b04\u4f4d\u5167\u5bb9";
    private static final String SIMPLIFIED_TEXT = "\u7b80\u4f53\u5b57\u6bb5\u5185\u5bb9";

    @TempDir
    Path tempDir;

    @Test
    void translateUsesUtf8ForPythonBridgeIo() throws IOException {
        Path scriptPath = tempDir.resolve("fake_bridge.py");
        Files.writeString(scriptPath, """
                import json
                import sys

                payload = json.loads(sys.stdin.read() or "{}")
                expected = "\\u7e41\\u9ad4\\u6b04\\u4f4d\\u5167\\u5bb9"
                if payload.get("text") != expected:
                    print(json.dumps({"ok": False, "error": payload.get("text", "")}, ensure_ascii=False))
                else:
                    print(json.dumps({"ok": True, "text": "\\u7b80\\u4f53\\u5b57\\u6bb5\\u5185\\u5bb9"}, ensure_ascii=False))
                """, StandardCharsets.UTF_8);

        TranslationProperties properties = new TranslationProperties();
        properties.setBridgeEnabled(true);
        properties.setPythonCommand("python");
        properties.setBridgeScriptPath(scriptPath.toString());
        properties.setRequestTimeoutMs(5000);

        TranslatorsPythonBridgeAdapter adapter = new TranslatorsPythonBridgeAdapter(properties, new ObjectMapper());
        TranslationAttemptResult result = adapter.translate(TranslationCommand.builder()
                .engine("google")
                .sourceLocale(LocaleCode.ZH_HANT)
                .targetLocale(LocaleCode.ZH_HANS)
                .text(TRADITIONAL_TEXT)
                .timeoutMs(5000)
                .build());

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getEngine()).isEqualTo("google");
        assertThat(result.getTranslatedText()).isEqualTo(SIMPLIFIED_TEXT);
    }
}
