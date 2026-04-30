package com.aoxiaoyou.admin.common.content;

import com.aoxiaoyou.admin.common.enums.ContentStatus;
import com.aoxiaoyou.admin.common.exception.BusinessException;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;

public final class ContentLifecycleStatusSupport {

    private static final Set<ContentStatus> MANUALLY_OPERABLE_STATUSES =
            EnumSet.of(ContentStatus.PUBLISHED, ContentStatus.UNPUBLISHED, ContentStatus.ARCHIVED, ContentStatus.DELETED);

    private static final Set<ContentStatus> MUTABLE_STATUSES =
            EnumSet.of(ContentStatus.EDITING, ContentStatus.REVIEWING, ContentStatus.DRAFT,
                    ContentStatus.PUBLISHED, ContentStatus.UNPUBLISHED, ContentStatus.ARCHIVED);

    private ContentLifecycleStatusSupport() {
    }

    public static ContentStatus parseManuallyOperableStatus(String status) {
        ContentStatus contentStatus = parseStatus(status);
        if (!MANUALLY_OPERABLE_STATUSES.contains(contentStatus)) {
            throw new BusinessException(4008, "Status is not manually operable");
        }
        return contentStatus;
    }

    public static ContentStatus parseStatus(String status) {
        if (!StringUtils.hasText(status)) {
            throw new BusinessException(4007, "Status is required");
        }
        try {
            return ContentStatus.fromCode(status.trim());
        } catch (IllegalArgumentException ex) {
            try {
                return ContentStatus.fromCanonicalCode(status.trim());
            } catch (IllegalArgumentException nested) {
                throw new BusinessException(4009, "Unsupported content status");
            }
        }
    }

    public static ContentStatus resolveActionTargetStatus(String action) {
        if (!StringUtils.hasText(action)) {
            throw new BusinessException(4010, "Lifecycle action is required");
        }
        return switch (action.trim().toLowerCase(Locale.ROOT)) {
            case "publish" -> ContentStatus.PUBLISHED;
            case "unpublish" -> ContentStatus.UNPUBLISHED;
            case "remove" -> ContentStatus.DELETED;
            default -> throw new BusinessException(4011, "Unsupported lifecycle action");
        };
    }

    public static void assertTransitionAllowed(String currentStatus, String action) {
        ContentStatus current = StringUtils.hasText(currentStatus) ? parseStatus(currentStatus) : ContentStatus.EDITING;
        ContentStatus target = resolveActionTargetStatus(action);
        if (current.isTerminalDeleted()) {
            throw new BusinessException(4012, "Deleted content cannot be changed by lifecycle action");
        }
        if (current.canonicalCode().equals(target.canonicalCode())) {
            throw new BusinessException(4013, "Lifecycle action does not change current status");
        }
        if (target == ContentStatus.PUBLISHED && !MUTABLE_STATUSES.contains(current)) {
            throw new BusinessException(4014, "Current status cannot be published");
        }
    }

    public static LocalDateTime resolvePublishedAt(ContentStatus status, LocalDateTime currentPublishedAt) {
        return status != null && status.isTravelerVisible()
                ? (currentPublishedAt != null ? currentPublishedAt : LocalDateTime.now())
                : null;
    }
}
