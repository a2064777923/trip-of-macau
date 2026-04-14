package com.aoxiaoyou.admin.translation;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "app.translation")
public class TranslationProperties {

    private boolean bridgeEnabled = false;

    private String pythonCommand = "python";

    private String bridgeScriptPath = "";

    private int requestTimeoutMs = 8000;

    private int maxTextLength = 5000;

    private String defaultEnginePriority = "google,bing,deepl,yandex,baidu,alibaba,sogou,iciba,tencent";

    public List<String> resolveDefaultEnginePriority() {
        if (!StringUtils.hasText(defaultEnginePriority)) {
            return List.of("google", "bing", "deepl", "yandex", "baidu", "alibaba", "sogou", "iciba", "tencent");
        }
        return Arrays.stream(defaultEnginePriority.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .map(String::toLowerCase)
                .distinct()
                .toList();
    }
}
