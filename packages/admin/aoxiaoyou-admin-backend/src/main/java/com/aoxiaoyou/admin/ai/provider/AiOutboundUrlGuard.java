package com.aoxiaoyou.admin.ai.provider;

import com.aoxiaoyou.admin.common.exception.BusinessException;
import com.aoxiaoyou.admin.entity.AiProviderConfig;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.net.IDN;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Component
public class AiOutboundUrlGuard {

    private static final Map<String, HostPolicy> PLATFORM_POLICIES = Map.of(
            "openai", new HostPolicy(
                    Set.of("api.openai.com"),
                    Set.of("api.openai.com", "openai.com", "blob.core.windows.net")
            ),
            "bailian", new HostPolicy(
                    Set.of("dashscope.aliyuncs.com"),
                    Set.of("dashscope.aliyuncs.com", "aliyuncs.com", "aliyun.com")
            ),
            "hunyuan", new HostPolicy(
                    Set.of("api.hunyuan.cloud.tencent.com", "hunyuan.cloud.tencent.com"),
                    Set.of("hunyuan.cloud.tencent.com", "cloud.tencent.com", "myqcloud.com")
            ),
            "minimax", new HostPolicy(
                    Set.of("api.minimax.chat", "minimax.chat"),
                    Set.of("minimax.chat")
            ),
            "volcengine", new HostPolicy(
                    Set.of("ark.cn-beijing.volces.com", "volces.com"),
                    Set.of("volces.com", "volccdn.com")
            )
    );

    public String normalizeConfiguredBaseUrl(String platformCode, String rawUrl) {
        URI uri = parseUri(rawUrl, "AI 供應商 Base URL");
        validateBaseUrlShape(uri);
        validateHttpsScheme(uri, "AI 供應商 Base URL");
        String host = normalizeHost(uri, "AI 供應商 Base URL");
        validateSpecialUseHostname(host, "AI 供應商 Base URL");
        validateLiteralAddressIfPresent(host, "AI 供應商 Base URL");
        validatePlatformHostPolicy(platformCode, host, false, null, "AI 供應商 Base URL");
        return stripTrailingSlash(uri.normalize().toString());
    }

    public String validateApiRequestUrl(AiProviderConfig provider, String rawUrl) {
        URI uri = parseUri(rawUrl, "AI 供應商請求地址");
        validateHttpsScheme(uri, "AI 供應商請求地址");
        String host = normalizeHost(uri, "AI 供應商請求地址");
        validateSpecialUseHostname(host, "AI 供應商請求地址");
        validatePlatformHostPolicy(provider == null ? null : provider.getPlatformCode(), host, false, provider, "AI 供應商請求地址");
        validateResolvedAddresses(host, "AI 供應商請求地址");
        return uri.toString();
    }

    public String validateAssetDownloadUrl(AiProviderConfig provider, String rawUrl) {
        URI uri = parseUri(rawUrl, "AI 資源下載地址");
        validateHttpsScheme(uri, "AI 資源下載地址");
        String host = normalizeHost(uri, "AI 資源下載地址");
        validateSpecialUseHostname(host, "AI 資源下載地址");
        validatePlatformHostPolicy(provider == null ? null : provider.getPlatformCode(), host, true, provider, "AI 資源下載地址");
        validateResolvedAddresses(host, "AI 資源下載地址");
        return uri.toString();
    }

    private URI parseUri(String rawUrl, String label) {
        if (!StringUtils.hasText(rawUrl)) {
            throw new BusinessException(4055, label + "不能為空");
        }
        try {
            URI uri = URI.create(rawUrl.trim()).normalize();
            if (!uri.isAbsolute()) {
                throw new BusinessException(4055, label + "必須是完整網址");
            }
            return uri;
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(4055, label + "格式不正確");
        }
    }

    private void validateBaseUrlShape(URI uri) {
        if (StringUtils.hasText(uri.getRawQuery()) || StringUtils.hasText(uri.getRawFragment()) || StringUtils.hasText(uri.getRawUserInfo())) {
            throw new BusinessException(4055, "AI 供應商 Base URL 不能包含查詢參數、片段或帳密資訊");
        }
    }

    private void validateHttpsScheme(URI uri, String label) {
        if (!"https".equalsIgnoreCase(uri.getScheme())) {
            throw new BusinessException(4055, label + "只允許 HTTPS");
        }
    }

    private String normalizeHost(URI uri, String label) {
        String host = uri.getHost();
        if (!StringUtils.hasText(host)) {
            throw new BusinessException(4055, label + "缺少主機名");
        }
        String normalized = host.trim();
        if (normalized.startsWith("[") && normalized.endsWith("]")) {
            normalized = normalized.substring(1, normalized.length() - 1);
        }
        try {
            return IDN.toASCII(normalized).toLowerCase(Locale.ROOT);
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(4055, label + "主機名格式不正確");
        }
    }

    private void validatePlatformHostPolicy(String platformCode,
                                            String host,
                                            boolean assetDownload,
                                            AiProviderConfig provider,
                                            String label) {
        String normalizedPlatform = normalizePlatformCode(platformCode);
        if ("custom".equals(normalizedPlatform)) {
            return;
        }
        HostPolicy policy = PLATFORM_POLICIES.get(normalizedPlatform);
        if (policy == null) {
            return;
        }
        Set<String> allowedHosts = new LinkedHashSet<>(assetDownload ? policy.assetHosts() : policy.apiHosts());
        if (provider != null && StringUtils.hasText(provider.getApiBaseUrl())) {
            try {
                allowedHosts.add(normalizeHost(parseUri(provider.getApiBaseUrl(), "AI 供應商 Base URL"), "AI 供應商 Base URL"));
            } catch (BusinessException ignored) {
                // Existing invalid values are handled by runtime validation.
            }
        }
        if (!matchesAllowedHost(host, allowedHosts)) {
            throw new BusinessException(4055, label + "不在允許的供應商域名範圍內");
        }
    }

    private boolean matchesAllowedHost(String host, Set<String> allowedHosts) {
        for (String item : allowedHosts) {
            String normalized = item.toLowerCase(Locale.ROOT);
            if (host.equals(normalized) || host.endsWith("." + normalized)) {
                return true;
            }
        }
        return false;
    }

    private void validateSpecialUseHostname(String host, String label) {
        if ("localhost".equals(host) || host.endsWith(".localhost") || host.endsWith(".local")
                || host.endsWith(".localdomain") || host.endsWith(".internal") || host.endsWith(".home.arpa")) {
            throw new BusinessException(4055, label + "不能使用本機或內網主機名");
        }
    }

    private void validateLiteralAddressIfPresent(String host, String label) {
        if (!looksLikeIpLiteral(host)) {
            return;
        }
        try {
            InetAddress address = InetAddress.getByName(host);
            if (isBlockedAddress(address)) {
                throw new BusinessException(4055, label + "不能指向本機、私網或保留地址");
            }
        } catch (UnknownHostException ex) {
            throw new BusinessException(4055, label + "主機名無法解析");
        }
    }

    private void validateResolvedAddresses(String host, String label) {
        try {
            InetAddress[] addresses = InetAddress.getAllByName(host);
            if (addresses.length == 0) {
                throw new BusinessException(4055, label + "主機名無法解析");
            }
            for (InetAddress address : addresses) {
                if (isBlockedAddress(address)) {
                    throw new BusinessException(4055, label + "不能指向本機、私網或保留地址");
                }
            }
        } catch (UnknownHostException ex) {
            throw new BusinessException(4055, label + "主機名無法解析");
        }
    }

    private boolean looksLikeIpLiteral(String host) {
        return host.contains(":") || host.matches("^\\d{1,3}(\\.\\d{1,3}){3}$");
    }

    private boolean isBlockedAddress(InetAddress address) {
        if (address == null) {
            return true;
        }
        if (address.isAnyLocalAddress() || address.isLoopbackAddress() || address.isLinkLocalAddress()
                || address.isSiteLocalAddress() || address.isMulticastAddress()) {
            return true;
        }
        byte[] bytes = address.getAddress();
        if (address instanceof Inet4Address) {
            int first = Byte.toUnsignedInt(bytes[0]);
            int second = Byte.toUnsignedInt(bytes[1]);
            int third = Byte.toUnsignedInt(bytes[2]);
            return first == 0
                    || first == 10
                    || (first == 100 && second >= 64 && second <= 127)
                    || first == 127
                    || (first == 169 && second == 254)
                    || (first == 172 && second >= 16 && second <= 31)
                    || (first == 192 && second == 0 && third == 0)
                    || (first == 192 && second == 0 && third == 2)
                    || (first == 192 && second == 168)
                    || (first == 198 && (second == 18 || second == 19))
                    || (first == 198 && second == 51 && third == 100)
                    || (first == 203 && second == 0 && third == 113)
                    || first >= 224;
        }
        if (address instanceof Inet6Address) {
            int first = Byte.toUnsignedInt(bytes[0]);
            int second = Byte.toUnsignedInt(bytes[1]);
            return first == 0
                    || (first & 0xfe) == 0xfc
                    || (first == 0xfe && (second & 0xc0) == 0x80)
                    || (first == 0x20 && second == 0x01 && Byte.toUnsignedInt(bytes[2]) == 0x0d && Byte.toUnsignedInt(bytes[3]) == 0xb8);
        }
        return false;
    }

    private String stripTrailingSlash(String value) {
        return value.replaceAll("/+$", "");
    }

    private String normalizePlatformCode(String platformCode) {
        return StringUtils.hasText(platformCode) ? platformCode.trim().toLowerCase(Locale.ROOT) : "custom";
    }

    private record HostPolicy(Set<String> apiHosts, Set<String> assetHosts) {
    }
}
