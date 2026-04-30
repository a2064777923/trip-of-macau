package com.aoxiaoyou.admin.service.impl;

import com.aoxiaoyou.admin.common.api.PageResponse;
import com.aoxiaoyou.admin.common.content.AdminLifecycleDependencyPreviewService;
import com.aoxiaoyou.admin.common.content.AdminLifecycleTargetRegistry;
import com.aoxiaoyou.admin.common.content.ContentLifecycleStatusSupport;
import com.aoxiaoyou.admin.common.enums.ContentStatus;
import com.aoxiaoyou.admin.common.exception.BusinessException;
import com.aoxiaoyou.admin.dto.request.AdminLifecycleOperationRequest;
import com.aoxiaoyou.admin.dto.response.AdminLifecycleImpactResponse;
import com.aoxiaoyou.admin.dto.response.AdminLifecycleOperationResponse;
import com.aoxiaoyou.admin.dto.response.AdminLifecycleTargetResponse;
import com.aoxiaoyou.admin.entity.ContentLifecycleOperation;
import com.aoxiaoyou.admin.entity.ContentLifecycleOperationImpact;
import com.aoxiaoyou.admin.mapper.ContentLifecycleOperationImpactMapper;
import com.aoxiaoyou.admin.mapper.ContentLifecycleOperationMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminLifecycleOperationServiceImpl implements com.aoxiaoyou.admin.service.AdminLifecycleOperationService {

    private static final String STATUS_SCHEDULED = "scheduled";
    private static final String STATUS_APPLIED = "applied";
    private static final String STATUS_FAILED = "failed";
    private static final String STATUS_CANCELLED = "cancelled";
    private static final String STATUS_APPLYING = "applying";

    private final AdminLifecycleTargetRegistry targetRegistry;
    private final AdminLifecycleDependencyPreviewService dependencyPreviewService;
    private final ContentLifecycleOperationMapper operationMapper;
    private final ContentLifecycleOperationImpactMapper impactMapper;
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public List<AdminLifecycleTargetResponse.TargetType> listTargetTypes() {
        return targetRegistry.listDescriptors().stream()
                .sorted(Comparator.comparing(AdminLifecycleTargetRegistry.TargetDescriptor::targetType))
                .map(descriptor -> AdminLifecycleTargetResponse.TargetType.builder()
                        .targetType(descriptor.targetType())
                        .label(descriptor.label())
                        .tableName(descriptor.tableName())
                        .statusMutable(descriptor.statusMutable())
                        .publicRuntimeRelevant(descriptor.publicRuntimeRelevant())
                        .supportedActions(descriptor.supportedActions())
                        .childTargetTypes(descriptor.childTargetTypes())
                        .build())
                .toList();
    }

    @Override
    public List<AdminLifecycleTargetResponse.StatusOption> listStatuses() {
        return List.of(ContentStatus.EDITING, ContentStatus.REVIEWING, ContentStatus.PUBLISHED,
                        ContentStatus.UNPUBLISHED, ContentStatus.DELETED)
                .stream()
                .map(status -> AdminLifecycleTargetResponse.StatusOption.builder()
                        .status(status.getCode())
                        .canonicalStatus(status.canonicalCode())
                        .label(status.labelZht())
                        .travelerVisible(status.isTravelerVisible())
                        .terminalDeleted(status.isTerminalDeleted())
                        .build())
                .toList();
    }

    @Override
    public PageResponse<AdminLifecycleTargetResponse.TargetSummary> pageTargets(AdminLifecycleOperationRequest.TargetQuery query) {
        long pageNum = Math.max(query.getPageNum(), 1);
        long pageSize = Math.min(Math.max(query.getPageSize(), 1), 100);
        List<AdminLifecycleTargetResponse.TargetSummary> all = new ArrayList<>();
        List<AdminLifecycleTargetRegistry.TargetDescriptor> descriptors = targetRegistry.listDescriptors().stream()
                .filter(descriptor -> !StringUtils.hasText(query.getTargetType()) || descriptor.targetType().equals(query.getTargetType()))
                .toList();
        for (AdminLifecycleTargetRegistry.TargetDescriptor descriptor : descriptors) {
            all.addAll(loadTargetSummaries(descriptor, query));
        }
        all.sort(Comparator
                .comparing(AdminLifecycleTargetResponse.TargetSummary::getUpdatedAt, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(AdminLifecycleTargetResponse.TargetSummary::getTargetType)
                .thenComparing(AdminLifecycleTargetResponse.TargetSummary::getTargetId, Comparator.nullsLast(Long::compareTo)));
        int from = (int) Math.min((pageNum - 1) * pageSize, all.size());
        int to = (int) Math.min(from + pageSize, all.size());
        Page<AdminLifecycleTargetResponse.TargetSummary> page = new Page<>(pageNum, pageSize, all.size());
        page.setRecords(all.subList(from, to));
        return PageResponse.of(page);
    }

    @Override
    public AdminLifecycleOperationResponse.Preview preview(AdminLifecycleOperationRequest.Preview request) {
        AdminLifecycleTargetRegistry.TargetDescriptor descriptor = targetRegistry.require(request.getTargetType());
        TargetRecord target = requireTarget(descriptor, request.getTargetId(), request.getTargetCode());
        String action = normalizeAction(request.getAction());
        ContentStatus targetStatus = ContentLifecycleStatusSupport.resolveActionTargetStatus(action);
        boolean allowedTransition = true;
        List<AdminLifecycleImpactResponse> impacts = new ArrayList<>();
        try {
            ContentLifecycleStatusSupport.assertTransitionAllowed(target.status(), action);
        } catch (BusinessException ex) {
            allowedTransition = false;
            impacts.add(AdminLifecycleImpactResponse.builder()
                    .impactType("status_transition")
                    .impactTypeLabel("狀態轉換")
                    .severity("blocking")
                    .severityLabel("阻擋")
                    .sourceType(descriptor.targetType())
                    .sourceId(target.id())
                    .sourceCode(target.code())
                    .sourceName(target.name())
                    .impactSummary(ex.getMessage())
                    .metadata(Map.of("currentStatus", safe(target.status()), "action", action))
                    .sortOrder(0)
                    .build());
        }
        impacts.addAll(dependencyPreviewService.previewImpacts(
                descriptor,
                target.id(),
                target.code(),
                target.name(),
                action,
                Boolean.TRUE.equals(request.getCascade())));
        String previewHash = buildPreviewHash(descriptor.targetType(), target.id(), action, target.status(), impacts);
        Map<String, Integer> counters = impacts.stream()
                .collect(Collectors.toMap(
                        item -> item.getImpactType() + ":" + item.getSeverity(),
                        item -> 1,
                        Integer::sum,
                        LinkedHashMap::new));
        boolean hasBlocking = impacts.stream().anyMatch(item -> "blocking".equals(item.getSeverity()));
        return AdminLifecycleOperationResponse.Preview.builder()
                .targetType(descriptor.targetType())
                .targetTypeLabel(descriptor.label())
                .targetId(target.id())
                .targetCode(target.code())
                .targetName(target.name())
                .action(action)
                .actionLabel(actionLabel(action))
                .currentStatus(canonicalStatus(target.status()))
                .currentStatusLabel(statusLabel(target.status()))
                .targetStatus(targetStatus.canonicalCode())
                .targetStatusLabel(targetStatus.labelZht())
                .allowedTransition(allowedTransition)
                .hasBlockingImpacts(hasBlocking)
                .previewHash(previewHash)
                .impactCounters(counters)
                .impacts(impacts)
                .build();
    }

    @Override
    @Transactional
    public AdminLifecycleOperationResponse.Summary createOperation(AdminLifecycleOperationRequest.CreateOperation request, Long adminUserId, String adminUsername) {
        if (!Boolean.TRUE.equals(request.getConfirmedImpact())) {
            throw new BusinessException(4020, "confirmedImpact is required");
        }
        AdminLifecycleOperationRequest.Preview previewRequest = new AdminLifecycleOperationRequest.Preview();
        previewRequest.setTargetType(request.getTargetType());
        previewRequest.setTargetId(request.getTargetId());
        previewRequest.setTargetCode(request.getTargetCode());
        previewRequest.setAction(request.getAction());
        previewRequest.setExecutionMode(request.getExecutionMode());
        previewRequest.setCascade(request.getCascade());
        previewRequest.setMetadata(request.getMetadata());
        AdminLifecycleOperationResponse.Preview preview = preview(previewRequest);
        if (StringUtils.hasText(request.getPreviewHash()) && !Objects.equals(request.getPreviewHash(), preview.getPreviewHash())) {
            throw new BusinessException(4021, "Preview has changed, please preview again");
        }
        if (Boolean.TRUE.equals(preview.getHasBlockingImpacts()) && !Boolean.TRUE.equals(request.getCascade())) {
            throw new BusinessException(4022, "Blocking impacts must be resolved before operation");
        }
        if (("remove".equals(preview.getAction()) || "unpublish".equals(preview.getAction())) && !StringUtils.hasText(request.getReason())) {
            throw new BusinessException(4023, "Operation reason is required");
        }

        ContentLifecycleOperation operation = new ContentLifecycleOperation();
        operation.setOperationCode("lifecycle-" + UUID.randomUUID());
        operation.setTargetType(preview.getTargetType());
        operation.setTargetId(preview.getTargetId());
        operation.setTargetCode(preview.getTargetCode());
        operation.setTargetName(preview.getTargetName());
        operation.setAction(preview.getAction());
        operation.setFromStatus(preview.getCurrentStatus());
        operation.setToStatus(preview.getTargetStatus());
        operation.setOperationStatus(resolveInitialOperationStatus(request.getExecutionMode()));
        operation.setScheduledAt(parseDateTime(request.getScheduledAt()));
        operation.setRequestedBy(adminUserId);
        operation.setRequestedByName(adminUsername);
        operation.setReason(trimToNull(request.getReason()));
        operation.setPreviewHash(preview.getPreviewHash());
        operation.setPreviewJson(writeJson(preview));
        operation.setRequestJson(writeJson(request));
        operation.setDeleted(0);
        operationMapper.insert(operation);
        persistImpacts(operation.getId(), preview.getImpacts());

        if ("immediate".equals(normalizeExecutionMode(request.getExecutionMode()))) {
            return applyOperation(operation.getId(), new AdminLifecycleOperationRequest.ApplyOperation(), adminUserId, adminUsername);
        }
        return toSummary(requireOperation(operation.getId()));
    }

    @Override
    @Transactional
    public AdminLifecycleOperationResponse.Summary applyOperation(Long operationId, AdminLifecycleOperationRequest.ApplyOperation request, Long adminUserId, String adminUsername) {
        ContentLifecycleOperation operation = requireOperation(operationId);
        if (STATUS_APPLIED.equals(operation.getOperationStatus())) {
            return toSummary(operation);
        }
        if (STATUS_CANCELLED.equals(operation.getOperationStatus())) {
            throw new BusinessException(4024, "Cancelled lifecycle operation cannot be applied");
        }
        operation.setOperationStatus(STATUS_APPLYING);
        operationMapper.updateById(operation);
        try {
            AdminLifecycleTargetRegistry.TargetDescriptor descriptor = targetRegistry.require(operation.getTargetType());
            ContentStatus targetStatus = ContentLifecycleStatusSupport.parseStatus(operation.getToStatus());
            applyTargetStatus(descriptor, operation.getTargetId(), targetStatus);
            operation.setOperationStatus(STATUS_APPLIED);
            operation.setAppliedAt(LocalDateTime.now());
            operation.setResultJson(writeJson(Map.of(
                    "schemaVersion", 1,
                    "appliedBy", adminUserId == null ? 0 : adminUserId,
                    "appliedByName", safe(adminUsername),
                    "status", STATUS_APPLIED)));
            operation.setErrorMessage(null);
            operationMapper.updateById(operation);
        } catch (Exception ex) {
            operation.setOperationStatus(STATUS_FAILED);
            operation.setFailedAt(LocalDateTime.now());
            operation.setErrorMessage(ex.getMessage());
            operationMapper.updateById(operation);
            throw ex instanceof BusinessException businessException
                    ? businessException
                    : new BusinessException(4025, "Lifecycle operation failed: " + ex.getMessage());
        }
        return toSummary(requireOperation(operationId));
    }

    @Override
    @Transactional
    public AdminLifecycleOperationResponse.Summary cancelOperation(Long operationId, AdminLifecycleOperationRequest.CancelOperation request, Long adminUserId, String adminUsername) {
        ContentLifecycleOperation operation = requireOperation(operationId);
        if (!STATUS_SCHEDULED.equals(operation.getOperationStatus())) {
            throw new BusinessException(4026, "Only scheduled operations can be cancelled");
        }
        operation.setOperationStatus(STATUS_CANCELLED);
        operation.setCancelledAt(LocalDateTime.now());
        operation.setResultJson(writeJson(Map.of(
                "schemaVersion", 1,
                "cancelledBy", adminUserId == null ? 0 : adminUserId,
                "cancelledByName", safe(adminUsername),
                "reason", safe(request == null ? null : request.getReason()))));
        operationMapper.updateById(operation);
        return toSummary(operation);
    }

    @Override
    @Transactional
    public List<AdminLifecycleOperationResponse.Summary> runDueOperations(Long adminUserId, String adminUsername) {
        List<ContentLifecycleOperation> due = operationMapper.selectList(new LambdaQueryWrapper<ContentLifecycleOperation>()
                .eq(ContentLifecycleOperation::getOperationStatus, STATUS_SCHEDULED)
                .le(ContentLifecycleOperation::getScheduledAt, LocalDateTime.now())
                .eq(ContentLifecycleOperation::getDeleted, 0)
                .orderByAsc(ContentLifecycleOperation::getScheduledAt)
                .last("LIMIT 20"));
        List<AdminLifecycleOperationResponse.Summary> summaries = new ArrayList<>();
        for (ContentLifecycleOperation operation : due) {
            summaries.add(applyOperation(operation.getId(), new AdminLifecycleOperationRequest.ApplyOperation(), adminUserId, adminUsername));
        }
        return summaries;
    }

    @Override
    public PageResponse<AdminLifecycleOperationResponse.Summary> pageOperations(long pageNum, long pageSize, String targetType, String action, String operationStatus, String keyword) {
        Page<ContentLifecycleOperation> page = operationMapper.selectPage(new Page<>(Math.max(pageNum, 1), Math.min(Math.max(pageSize, 1), 100)),
                new LambdaQueryWrapper<ContentLifecycleOperation>()
                        .eq(StringUtils.hasText(targetType), ContentLifecycleOperation::getTargetType, targetType)
                        .eq(StringUtils.hasText(action), ContentLifecycleOperation::getAction, action)
                        .eq(StringUtils.hasText(operationStatus), ContentLifecycleOperation::getOperationStatus, operationStatus)
                        .and(StringUtils.hasText(keyword), wrapper -> wrapper
                                .like(ContentLifecycleOperation::getTargetName, keyword)
                                .or().like(ContentLifecycleOperation::getTargetCode, keyword)
                                .or().like(ContentLifecycleOperation::getOperationCode, keyword))
                        .eq(ContentLifecycleOperation::getDeleted, 0)
                        .orderByDesc(ContentLifecycleOperation::getCreatedAt));
        Page<AdminLifecycleOperationResponse.Summary> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(page.getRecords().stream().map(this::toSummary).toList());
        return PageResponse.of(result);
    }

    @Override
    public AdminLifecycleOperationResponse.Detail getOperation(Long operationId) {
        ContentLifecycleOperation operation = requireOperation(operationId);
        List<AdminLifecycleImpactResponse> impacts = impactMapper.selectList(new LambdaQueryWrapper<ContentLifecycleOperationImpact>()
                        .eq(ContentLifecycleOperationImpact::getOperationId, operationId)
                        .eq(ContentLifecycleOperationImpact::getDeleted, 0)
                        .orderByAsc(ContentLifecycleOperationImpact::getSortOrder))
                .stream()
                .map(this::toImpactResponse)
                .toList();
        return AdminLifecycleOperationResponse.Detail.builder()
                .summary(toSummary(operation))
                .impacts(impacts)
                .preview(readJson(operation.getPreviewJson()))
                .request(readJson(operation.getRequestJson()))
                .result(readJson(operation.getResultJson()))
                .build();
    }

    private List<AdminLifecycleTargetResponse.TargetSummary> loadTargetSummaries(AdminLifecycleTargetRegistry.TargetDescriptor descriptor,
                                                                                 AdminLifecycleOperationRequest.TargetQuery query) {
        String keyword = trimToNull(query.getKeyword());
        String status = trimToNull(query.getStatus());
        List<Object> args = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT id, ")
                .append(descriptor.codeColumn()).append(" AS code, ")
                .append(coalesceColumns(descriptor.nameColumns())).append(" AS name, ")
                .append(descriptor.statusColumn()).append(" AS status, created_at, updated_at");
        if (descriptor.publishedAtColumn() != null) {
            sql.append(", ").append(descriptor.publishedAtColumn()).append(" AS published_at");
        } else {
            sql.append(", NULL AS published_at");
        }
        sql.append(" FROM ").append(descriptor.tableName()).append(" WHERE 1=1");
        if (descriptor.deletedColumn() != null) {
            sql.append(" AND COALESCE(").append(descriptor.deletedColumn()).append(", 0) = 0");
        }
        if (StringUtils.hasText(keyword)) {
            sql.append(" AND (").append(descriptor.codeColumn()).append(" LIKE ?");
            args.add("%" + keyword + "%");
            for (String column : descriptor.nameColumns()) {
                sql.append(" OR ").append(column).append(" LIKE ?");
                args.add("%" + keyword + "%");
            }
            sql.append(")");
        }
        if (StringUtils.hasText(status)) {
            sql.append(" AND ").append(descriptor.statusColumn()).append(" IN (?, ?)");
            ContentStatus parsed = ContentLifecycleStatusSupport.parseStatus(status);
            args.add(parsed.getCode());
            args.add(parsed.canonicalCode());
        }
        if (Boolean.TRUE.equals(query.getPublishedOnly())) {
            sql.append(" AND ").append(descriptor.statusColumn()).append(" = 'published'");
        }
        sql.append(" ORDER BY updated_at DESC LIMIT 200");
        try {
            return jdbcTemplate.queryForList(sql.toString(), args.toArray()).stream()
                    .map(row -> toTargetSummary(descriptor, row))
                    .filter(summary -> !Boolean.TRUE.equals(query.getWithDependenciesOnly()) || summary.getDependencyCount() > 0)
                    .toList();
        } catch (DataAccessException ex) {
            return List.of();
        }
    }

    private AdminLifecycleTargetResponse.TargetSummary toTargetSummary(AdminLifecycleTargetRegistry.TargetDescriptor descriptor, Map<String, Object> row) {
        Long id = asLong(row.get("id"));
        String status = asString(row.get("status"));
        return AdminLifecycleTargetResponse.TargetSummary.builder()
                .targetType(descriptor.targetType())
                .targetTypeLabel(descriptor.label())
                .targetId(id)
                .targetCode(asString(row.get("code")))
                .targetName(firstText(asString(row.get("name")), asString(row.get("code")), "#" + id))
                .status(status)
                .canonicalStatus(canonicalStatus(status))
                .statusLabel(statusLabel(status))
                .travelerVisible("published".equals(canonicalStatus(status)))
                .statusMutable(descriptor.statusMutable())
                .publicRuntimeRelevant(descriptor.publicRuntimeRelevant())
                .dependencyCount(countRelations(descriptor.targetType(), id))
                .supportedActions(descriptor.supportedActions())
                .createdAt(asDate(row.get("created_at")))
                .updatedAt(asDate(row.get("updated_at")))
                .publishedAt(asDate(row.get("published_at")))
                .build();
    }

    private TargetRecord requireTarget(AdminLifecycleTargetRegistry.TargetDescriptor descriptor, Long targetId, String targetCode) {
        List<Object> args = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT id, ")
                .append(descriptor.codeColumn()).append(" AS code, ")
                .append(coalesceColumns(descriptor.nameColumns())).append(" AS name, ")
                .append(descriptor.statusColumn()).append(" AS status");
        if (descriptor.publishedAtColumn() != null) {
            sql.append(", ").append(descriptor.publishedAtColumn()).append(" AS published_at");
        } else {
            sql.append(", NULL AS published_at");
        }
        sql.append(" FROM ").append(descriptor.tableName()).append(" WHERE ");
        if (targetId != null) {
            sql.append("id = ?");
            args.add(targetId);
        } else if (StringUtils.hasText(targetCode)) {
            sql.append(descriptor.codeColumn()).append(" = ?");
            args.add(targetCode);
        } else {
            throw new BusinessException(4042, "Lifecycle target id or code is required");
        }
        if (descriptor.deletedColumn() != null) {
            sql.append(" AND COALESCE(").append(descriptor.deletedColumn()).append(", 0) = 0");
        }
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql.toString(), args.toArray());
        if (rows.isEmpty()) {
            throw new BusinessException(4043, "Lifecycle target not found");
        }
        Map<String, Object> row = rows.get(0);
        return new TargetRecord(asLong(row.get("id")), asString(row.get("code")),
                firstText(asString(row.get("name")), asString(row.get("code")), "#" + asLong(row.get("id"))),
                asString(row.get("status")), asDate(row.get("published_at")));
    }

    private void applyTargetStatus(AdminLifecycleTargetRegistry.TargetDescriptor descriptor, Long targetId, ContentStatus targetStatus) {
        if (!descriptor.statusMutable()) {
            throw new BusinessException(4027, "Lifecycle target is not status mutable");
        }
        List<Object> args = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE ").append(descriptor.tableName()).append(" SET ")
                .append(descriptor.statusColumn()).append(" = ?");
        args.add(targetStatus.canonicalCode());
        if (descriptor.publishedAtColumn() != null) {
            sql.append(", ").append(descriptor.publishedAtColumn()).append(" = ?");
            args.add(ContentLifecycleStatusSupport.resolvePublishedAt(targetStatus, null));
        }
        sql.append(", updated_at = NOW() WHERE id = ?");
        args.add(targetId);
        int updated = jdbcTemplate.update(sql.toString(), args.toArray());
        if (updated == 0) {
            throw new BusinessException(4043, "Lifecycle target not found");
        }
    }

    private void persistImpacts(Long operationId, List<AdminLifecycleImpactResponse> impacts) {
        for (AdminLifecycleImpactResponse impact : impacts) {
            ContentLifecycleOperationImpact entity = new ContentLifecycleOperationImpact();
            entity.setOperationId(operationId);
            entity.setImpactType(impact.getImpactType());
            entity.setSeverity(impact.getSeverity());
            entity.setSourceType(impact.getSourceType());
            entity.setSourceId(impact.getSourceId());
            entity.setSourceCode(impact.getSourceCode());
            entity.setSourceName(impact.getSourceName());
            entity.setRelationType(impact.getRelationType());
            entity.setTargetType(impact.getTargetType());
            entity.setTargetId(impact.getTargetId());
            entity.setTargetCode(impact.getTargetCode());
            entity.setTargetName(impact.getTargetName());
            entity.setImpactSummary(impact.getImpactSummary());
            entity.setMetadataJson(writeJson(impact.getMetadata()));
            entity.setSortOrder(impact.getSortOrder());
            entity.setDeleted(0);
            impactMapper.insert(entity);
        }
    }

    private ContentLifecycleOperation requireOperation(Long operationId) {
        ContentLifecycleOperation operation = operationMapper.selectById(operationId);
        if (operation == null || Integer.valueOf(1).equals(operation.getDeleted())) {
            throw new BusinessException(4044, "Lifecycle operation not found");
        }
        return operation;
    }

    private AdminLifecycleOperationResponse.Summary toSummary(ContentLifecycleOperation operation) {
        AdminLifecycleTargetRegistry.TargetDescriptor descriptor = targetRegistry.find(operation.getTargetType()).orElse(null);
        int impactCount = Math.toIntExact(impactMapper.selectCount(new LambdaQueryWrapper<ContentLifecycleOperationImpact>()
                .eq(ContentLifecycleOperationImpact::getOperationId, operation.getId())
                .eq(ContentLifecycleOperationImpact::getDeleted, 0)));
        int blockingCount = Math.toIntExact(impactMapper.selectCount(new LambdaQueryWrapper<ContentLifecycleOperationImpact>()
                .eq(ContentLifecycleOperationImpact::getOperationId, operation.getId())
                .eq(ContentLifecycleOperationImpact::getSeverity, "blocking")
                .eq(ContentLifecycleOperationImpact::getDeleted, 0)));
        return AdminLifecycleOperationResponse.Summary.builder()
                .id(operation.getId())
                .operationCode(operation.getOperationCode())
                .targetType(operation.getTargetType())
                .targetTypeLabel(descriptor == null ? operation.getTargetType() : descriptor.label())
                .targetId(operation.getTargetId())
                .targetCode(operation.getTargetCode())
                .targetName(operation.getTargetName())
                .action(operation.getAction())
                .actionLabel(actionLabel(operation.getAction()))
                .fromStatus(operation.getFromStatus())
                .fromStatusLabel(statusLabel(operation.getFromStatus()))
                .toStatus(operation.getToStatus())
                .toStatusLabel(statusLabel(operation.getToStatus()))
                .operationStatus(operation.getOperationStatus())
                .operationStatusLabel(operationStatusLabel(operation.getOperationStatus()))
                .previewHash(operation.getPreviewHash())
                .impactCount(impactCount)
                .blockingImpactCount(blockingCount)
                .requestedBy(operation.getRequestedBy())
                .requestedByName(operation.getRequestedByName())
                .reason(operation.getReason())
                .scheduledAt(operation.getScheduledAt())
                .appliedAt(operation.getAppliedAt())
                .cancelledAt(operation.getCancelledAt())
                .failedAt(operation.getFailedAt())
                .errorMessage(operation.getErrorMessage())
                .createdAt(operation.getCreatedAt())
                .updatedAt(operation.getUpdatedAt())
                .build();
    }

    private AdminLifecycleImpactResponse toImpactResponse(ContentLifecycleOperationImpact impact) {
        return AdminLifecycleImpactResponse.builder()
                .id(impact.getId())
                .impactType(impact.getImpactType())
                .impactTypeLabel(impactTypeLabel(impact.getImpactType()))
                .severity(impact.getSeverity())
                .severityLabel(severityLabel(impact.getSeverity()))
                .sourceType(impact.getSourceType())
                .sourceId(impact.getSourceId())
                .sourceCode(impact.getSourceCode())
                .sourceName(impact.getSourceName())
                .relationType(impact.getRelationType())
                .targetType(impact.getTargetType())
                .targetId(impact.getTargetId())
                .targetCode(impact.getTargetCode())
                .targetName(impact.getTargetName())
                .impactSummary(impact.getImpactSummary())
                .metadata(readJson(impact.getMetadataJson()))
                .sortOrder(impact.getSortOrder())
                .build();
    }

    private String resolveInitialOperationStatus(String executionMode) {
        return "immediate".equals(normalizeExecutionMode(executionMode)) ? STATUS_APPLYING : STATUS_SCHEDULED;
    }

    private String normalizeExecutionMode(String executionMode) {
        return "scheduled".equalsIgnoreCase(safe(executionMode)) ? "scheduled" : "immediate";
    }

    private String normalizeAction(String action) {
        String normalized = safe(action).trim().toLowerCase(Locale.ROOT);
        ContentLifecycleStatusSupport.resolveActionTargetStatus(normalized);
        return normalized;
    }

    private String buildPreviewHash(String targetType, Long targetId, String action, String status, List<AdminLifecycleImpactResponse> impacts) {
        String payload = targetType + "|" + targetId + "|" + action + "|" + canonicalStatus(status) + "|" +
                impacts.stream()
                        .map(item -> item.getImpactType() + ":" + item.getSeverity() + ":" + safe(item.getTargetType()) + ":" + safe(item.getTargetId()))
                        .sorted()
                        .collect(Collectors.joining(","));
        return DigestUtils.md5DigestAsHex(payload.getBytes(StandardCharsets.UTF_8));
    }

    private int countRelations(String targetType, Long targetId) {
        try {
            Integer count = jdbcTemplate.queryForObject("""
                    SELECT COUNT(*)
                    FROM content_relation_links
                    WHERE deleted = 0
                      AND ((target_type = ? AND target_id = ?) OR (owner_type = ? AND owner_id = ?))
                    """, Integer.class, targetType, targetId, targetType, targetId);
            return count == null ? 0 : count;
        } catch (DataAccessException ex) {
            return 0;
        }
    }

    private String coalesceColumns(List<String> columns) {
        return "COALESCE(" + columns.stream().map(column -> "NULLIF(" + column + ", '')").collect(Collectors.joining(", ")) + ")";
    }

    private String canonicalStatus(String status) {
        if (!StringUtils.hasText(status)) {
            return ContentStatus.EDITING.canonicalCode();
        }
        return ContentLifecycleStatusSupport.parseStatus(status).canonicalCode();
    }

    private String statusLabel(String status) {
        if (!StringUtils.hasText(status)) {
            return "編輯中";
        }
        return ContentLifecycleStatusSupport.parseStatus(status).labelZht();
    }

    private String actionLabel(String action) {
        return switch (safe(action)) {
            case "publish" -> "發布";
            case "unpublish" -> "下線";
            case "remove" -> "移除";
            default -> safe(action);
        };
    }

    private String operationStatusLabel(String status) {
        return switch (safe(status)) {
            case STATUS_SCHEDULED -> "已排程";
            case STATUS_APPLYING -> "執行中";
            case STATUS_APPLIED -> "已套用";
            case STATUS_FAILED -> "失敗";
            case STATUS_CANCELLED -> "已取消";
            default -> safe(status);
        };
    }

    private String severityLabel(String severity) {
        return switch (safe(severity)) {
            case "blocking" -> "阻擋";
            case "warning" -> "警告";
            case "info" -> "提示";
            default -> safe(severity);
        };
    }

    private String impactTypeLabel(String impactType) {
        return switch (safe(impactType)) {
            case "inbound_relation" -> "被其他內容綁定";
            case "outbound_relation" -> "此內容綁定其他內容";
            case "downstream_child" -> "下游子內容";
            case "public_runtime" -> "小程序公開內容";
            case "exploration_progress" -> "探索度與用戶進度";
            case "status_transition" -> "狀態轉換";
            default -> safe(impactType);
        };
    }

    private LocalDateTime parseDateTime(String value) {
        return StringUtils.hasText(value) ? LocalDateTime.parse(value) : LocalDateTime.now();
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String firstText(String... values) {
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                return value.trim();
            }
        }
        return null;
    }

    private String writeJson(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception ex) {
            throw new BusinessException(4028, "Unable to serialize lifecycle JSON");
        }
    }

    private Map<String, Object> readJson(String value) {
        if (!StringUtils.hasText(value)) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(value, new TypeReference<>() {
            });
        } catch (Exception ex) {
            return Map.of("raw", value);
        }
    }

    private String safe(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private Long asLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        return value == null ? null : Long.valueOf(String.valueOf(value));
    }

    private LocalDateTime asDate(Object value) {
        return value instanceof LocalDateTime localDateTime ? localDateTime : null;
    }

    private record TargetRecord(Long id, String code, String name, String status, LocalDateTime publishedAt) {
    }
}
