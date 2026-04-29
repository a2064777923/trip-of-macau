package com.aoxiaoyou.admin.common.content;

import com.aoxiaoyou.admin.common.enums.ContentStatus;
import com.aoxiaoyou.admin.common.exception.BusinessException;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.Set;

public final class ContentLifecycleStatusSupport {

    private static final Set<ContentStatus> MANUALLY_OPERABLE_STATUSES =
            EnumSet.of(ContentStatus.PUBLISHED, ContentStatus.ARCHIVED);

    private ContentLifecycleStatusSupport() {
    }

    public static ContentStatus parseManuallyOperableStatus(String status) {
        if (!StringUtils.hasText(status)) {
            throw new BusinessException(4007, "Status is required");
        }
        try {
            ContentStatus contentStatus = ContentStatus.fromCode(status.trim());
            if (!MANUALLY_OPERABLE_STATUSES.contains(contentStatus)) {
                throw new BusinessException(4008, "Status is not manually operable");
            }
            return contentStatus;
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(4009, "Unsupported content status");
        }
    }

    public static LocalDateTime resolvePublishedAt(ContentStatus status, LocalDateTime currentPublishedAt) {
        return status == ContentStatus.PUBLISHED
                ? (currentPublishedAt != null ? currentPublishedAt : LocalDateTime.now())
                : null;
    }
}
