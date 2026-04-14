package com.aoxiaoyou.tripofmacau.common.auth;

import com.aoxiaoyou.tripofmacau.common.config.WechatAuthProperties;
import com.aoxiaoyou.tripofmacau.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Locale;

@Slf4j
@Service
public class WechatAuthService {

    private final WechatAuthProperties properties;
    private final Environment environment;
    private final RestClient restClient;

    public WechatAuthService(WechatAuthProperties properties, Environment environment) {
        this.properties = properties;
        this.environment = environment;
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(properties.getAuthTimeoutMs());
        requestFactory.setReadTimeout(properties.getAuthTimeoutMs());
        this.restClient = RestClient.builder()
                .requestFactory(requestFactory)
                .build();
    }

    public WechatCode2SessionResponse exchangeCode(String code) {
        if (!StringUtils.hasText(properties.getMiniAppId()) || !StringUtils.hasText(properties.getMiniAppSecret())) {
            throw new BusinessException(5002, "WeChat mini-program auth is not configured");
        }

        String url = UriComponentsBuilder.fromHttpUrl(properties.getCode2sessionUrl())
                .queryParam("appid", properties.getMiniAppId())
                .queryParam("secret", properties.getMiniAppSecret())
                .queryParam("js_code", code)
                .queryParam("grant_type", "authorization_code")
                .toUriString();

        WechatCode2SessionResponse response;
        try {
            response = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(WechatCode2SessionResponse.class);
        } catch (RestClientException ex) {
            log.warn("WeChat code2session request failed", ex);
            throw new BusinessException(5003, "Failed to reach WeChat auth service");
        }

        if (response == null) {
            throw new BusinessException(5004, "WeChat auth service returned an empty response");
        }
        if (response.hasError()) {
            log.info("WeChat code2session rejected login code: errcode={}, errmsg={}", response.getErrcode(), response.getErrmsg());
            throw new BusinessException(4006, "Invalid or expired WeChat login code");
        }
        if (!response.hasOpenId()) {
            throw new BusinessException(5004, "WeChat auth service returned no openId");
        }
        return response;
    }

    public void assertDevBypassEnabled() {
        if (!properties.isDevBypassEnabled() || !environment.acceptsProfiles(Profiles.of("local", "dev"))) {
            throw new BusinessException(4031, "Dev bypass login is not enabled");
        }
    }

    public String buildDevBypassOpenId(String devIdentity) {
        assertDevBypassEnabled();
        String normalized = devIdentity == null ? "" : devIdentity.trim().toLowerCase(Locale.ROOT);
        if (!StringUtils.hasText(normalized)) {
            throw new BusinessException(4007, "devIdentity must not be blank");
        }
        return "dev-bypass:" + normalized;
    }
}
