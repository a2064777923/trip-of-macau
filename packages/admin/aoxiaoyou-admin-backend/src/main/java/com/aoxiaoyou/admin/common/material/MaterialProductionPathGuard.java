package com.aoxiaoyou.admin.common.material;

import com.aoxiaoyou.admin.common.exception.BusinessException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

@Component
public class MaterialProductionPathGuard {

    public Path resolveLocalPath(String packageLocalRoot, String relativeLocalPath) {
        if (!StringUtils.hasText(packageLocalRoot)) {
            throw new BusinessException(4055, "Material package local root is required");
        }
        if (!StringUtils.hasText(relativeLocalPath)) {
            throw new BusinessException(4055, "relativeLocalPath is required");
        }
        String cleaned = relativeLocalPath.trim().replace('\\', '/');
        if (cleaned.contains("\u0000") || cleaned.contains("..") || cleaned.startsWith("/") || cleaned.startsWith("~")) {
            throw new BusinessException(4055, "relativeLocalPath contains an unsafe segment");
        }
        if (cleaned.matches("(?i)^[a-z]:.*") || cleaned.contains("://")) {
            throw new BusinessException(4055, "relativeLocalPath must stay inside the package root");
        }
        Path relative = Paths.get(cleaned);
        if (relative.isAbsolute()) {
            throw new BusinessException(4055, "relativeLocalPath must not be absolute");
        }
        Path root = Paths.get(packageLocalRoot).toAbsolutePath().normalize();
        Path resolved = root.resolve(relative).normalize();
        if (!resolved.startsWith(root)) {
            throw new BusinessException(4055, "relativeLocalPath escapes the package root");
        }
        return resolved;
    }

    public String validateObjectKey(String forcedCosObjectKey, String allowedPrefix) {
        if (!StringUtils.hasText(forcedCosObjectKey)) {
            return null;
        }
        String key = forcedCosObjectKey.trim();
        String normalized = key.replace('\\', '/');
        if (!key.equals(normalized) || normalized.contains("\u0000") || normalized.startsWith("/") || normalized.startsWith("~")) {
            throw new BusinessException(4055, "COS object key contains an unsafe segment");
        }
        if (normalized.contains("..") || normalized.contains("//") || normalized.matches("(?i)^[a-z]:.*") || normalized.contains("://")) {
            throw new BusinessException(4055, "COS object key must be a safe relative key");
        }
        String prefix = StringUtils.hasText(allowedPrefix)
                ? allowedPrefix.trim().replace('\\', '/').replaceAll("^/+", "").replaceAll("/+$", "")
                : "";
        if (StringUtils.hasText(prefix) && !normalized.equals(prefix) && !normalized.startsWith(prefix + "/")) {
            throw new BusinessException(4055, "COS object key is outside the material package prefix");
        }
        return normalized;
    }

    public String defaultObjectKey(String packageCosPrefix, String relativeLocalPath) {
        String relative = StringUtils.hasText(relativeLocalPath)
                ? relativeLocalPath.trim().replace('\\', '/').replaceAll("^/+", "")
                : "asset";
        if (relative.contains("..") || relative.contains("//")) {
            throw new BusinessException(4055, "relativeLocalPath contains an unsafe segment");
        }
        String prefix = StringUtils.hasText(packageCosPrefix)
                ? packageCosPrefix.trim().replace('\\', '/').replaceAll("^/+", "").replaceAll("/+$", "")
                : "";
        String key = StringUtils.hasText(prefix) ? prefix + "/" + relative : relative;
        return validateObjectKey(key, prefix);
    }

    public String safeExtension(String filename) {
        if (!StringUtils.hasText(filename)) {
            return "";
        }
        int dot = filename.lastIndexOf('.');
        if (dot < 0 || dot == filename.length() - 1) {
            return "";
        }
        return filename.substring(dot + 1).toLowerCase(Locale.ROOT);
    }
}
