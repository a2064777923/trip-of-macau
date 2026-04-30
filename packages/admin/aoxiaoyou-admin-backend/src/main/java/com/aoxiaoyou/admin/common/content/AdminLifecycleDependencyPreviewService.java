package com.aoxiaoyou.admin.common.content;

import com.aoxiaoyou.admin.dto.response.AdminLifecycleImpactResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminLifecycleDependencyPreviewService {

    private final JdbcTemplate jdbcTemplate;

    public List<AdminLifecycleImpactResponse> previewImpacts(
            AdminLifecycleTargetRegistry.TargetDescriptor descriptor,
            Long targetId,
            String targetCode,
            String targetName,
            String action,
            boolean cascade) {
        List<AdminLifecycleImpactResponse> impacts = new ArrayList<>();
        addInboundRelationImpacts(impacts, descriptor, targetId);
        addOutboundRelationImpacts(impacts, descriptor, targetId);
        addDownstreamChildImpacts(impacts, descriptor, targetId, action, cascade);
        addExplorationProgressImpacts(impacts, descriptor, targetId, targetCode);
        addPublicRuntimeImpact(impacts, descriptor, targetId, targetCode, targetName, action);
        for (int i = 0; i < impacts.size(); i++) {
            impacts.get(i).setSortOrder((i + 1) * 10);
        }
        return impacts;
    }

    private void addInboundRelationImpacts(List<AdminLifecycleImpactResponse> impacts,
                                           AdminLifecycleTargetRegistry.TargetDescriptor descriptor,
                                           Long targetId) {
        query("""
                SELECT owner_type, owner_id, relation_type, target_type, target_id, target_code
                FROM content_relation_links
                WHERE deleted = 0 AND target_type = ? AND target_id = ?
                ORDER BY id DESC
                LIMIT 20
                """, descriptor.targetType(), targetId).forEach(row -> impacts.add(AdminLifecycleImpactResponse.builder()
                .impactType("inbound_relation")
                .impactTypeLabel("被其他內容綁定")
                .severity("warning")
                .severityLabel("警告")
                .sourceType(asString(row.get("owner_type")))
                .sourceId(asLong(row.get("owner_id")))
                .relationType(asString(row.get("relation_type")))
                .targetType(descriptor.targetType())
                .targetId(targetId)
                .targetCode(asString(row.get("target_code")))
                .impactSummary("此內容仍被其他內容綁定，操作後可能影響編排或公開展示。")
                .metadata(Map.of("impactSource", "content_relation_links"))
                .build()));
    }

    private void addOutboundRelationImpacts(List<AdminLifecycleImpactResponse> impacts,
                                            AdminLifecycleTargetRegistry.TargetDescriptor descriptor,
                                            Long targetId) {
        query("""
                SELECT owner_type, owner_id, relation_type, target_type, target_id, target_code
                FROM content_relation_links
                WHERE deleted = 0 AND owner_type = ? AND owner_id = ?
                ORDER BY id DESC
                LIMIT 20
                """, descriptor.targetType(), targetId).forEach(row -> impacts.add(AdminLifecycleImpactResponse.builder()
                .impactType("outbound_relation")
                .impactTypeLabel("此內容綁定其他內容")
                .severity("info")
                .severityLabel("提示")
                .sourceType(descriptor.targetType())
                .sourceId(targetId)
                .relationType(asString(row.get("relation_type")))
                .targetType(asString(row.get("target_type")))
                .targetId(asLong(row.get("target_id")))
                .targetCode(asString(row.get("target_code")))
                .impactSummary("此內容包含外部綁定，生命週期變更後相關入口可能不再公開。")
                .metadata(Map.of("impactSource", "content_relation_links"))
                .build()));
    }

    private void addDownstreamChildImpacts(List<AdminLifecycleImpactResponse> impacts,
                                           AdminLifecycleTargetRegistry.TargetDescriptor descriptor,
                                           Long targetId,
                                           String action,
                                           boolean cascade) {
        List<ChildRule> rules = childRules(descriptor.targetType());
        for (ChildRule rule : rules) {
            Integer count = count("SELECT COUNT(*) FROM " + rule.tableName + " WHERE " + rule.parentColumn + " = ? AND COALESCE(deleted, 0) = 0", targetId);
            if (count == null || count == 0) {
                continue;
            }
            String severity = "remove".equals(action) && !cascade ? "blocking" : "warning";
            impacts.add(AdminLifecycleImpactResponse.builder()
                    .impactType("downstream_child")
                    .impactTypeLabel("下游子內容")
                    .severity(severity)
                    .severityLabel("blocking".equals(severity) ? "阻擋" : "警告")
                    .sourceType(descriptor.targetType())
                    .sourceId(targetId)
                    .targetType(rule.targetType)
                    .impactSummary("此內容下方仍有 " + count + " 個「" + rule.label + "」。")
                    .metadata(Map.of("count", count, "childTable", rule.tableName))
                    .build());
        }
    }

    private void addExplorationProgressImpacts(List<AdminLifecycleImpactResponse> impacts,
                                               AdminLifecycleTargetRegistry.TargetDescriptor descriptor,
                                               Long targetId,
                                               String targetCode) {
        Integer count = count("""
                SELECT COUNT(*)
                FROM exploration_elements
                WHERE COALESCE(deleted, 0) = 0
                  AND (
                    (owner_type = ? AND owner_id = ?)
                    OR (? = 'city' AND city_id = ?)
                    OR (? = 'sub_map' AND sub_map_id = ?)
                    OR (? = 'poi' AND poi_id = ?)
                    OR (? = 'indoor_building' AND indoor_building_id = ?)
                    OR (? = 'indoor_floor' AND indoor_floor_id = ?)
                    OR (? = 'storyline' AND storyline_id = ?)
                    OR (? = 'story_chapter' AND story_chapter_id = ?)
                    OR owner_code = ?
                  )
                """,
                descriptor.targetType(), targetId,
                descriptor.targetType(), targetId,
                descriptor.targetType(), targetId,
                descriptor.targetType(), targetId,
                descriptor.targetType(), targetId,
                descriptor.targetType(), targetId,
                descriptor.targetType(), targetId,
                descriptor.targetType(), targetId,
                targetCode);
        if (count != null && count > 0) {
            impacts.add(AdminLifecycleImpactResponse.builder()
                    .impactType("exploration_progress")
                    .impactTypeLabel("探索度與用戶進度")
                    .severity("warning")
                    .severityLabel("警告")
                    .sourceType(descriptor.targetType())
                    .sourceId(targetId)
                    .impactSummary("此操作會影響 " + count + " 個探索元素的公開可用性或分母計算。")
                    .metadata(Map.of("count", count))
                    .build());
        }
    }

    private void addPublicRuntimeImpact(List<AdminLifecycleImpactResponse> impacts,
                                        AdminLifecycleTargetRegistry.TargetDescriptor descriptor,
                                        Long targetId,
                                        String targetCode,
                                        String targetName,
                                        String action) {
        if (!descriptor.publicRuntimeRelevant()) {
            return;
        }
        String summary = switch (action) {
            case "publish" -> "發布後，小程序公開 runtime 可在刷新後讀取此內容。";
            case "unpublish" -> "下線後，小程序公開 runtime 將不再展示此內容。";
            case "remove" -> "移除後，小程序公開 runtime 將排除此內容，相關入口需要依賴預覽確認。";
            default -> "此操作會影響小程序公開內容可見性。";
        };
        impacts.add(AdminLifecycleImpactResponse.builder()
                .impactType("public_runtime")
                .impactTypeLabel("小程序公開內容")
                .severity("remove".equals(action) ? "warning" : "info")
                .severityLabel("remove".equals(action) ? "警告" : "提示")
                .sourceType(descriptor.targetType())
                .sourceId(targetId)
                .sourceCode(targetCode)
                .sourceName(targetName)
                .impactSummary(summary)
                .metadata(Map.of("runtimeFilter", "published-only"))
                .build());
    }

    private List<ChildRule> childRules(String targetType) {
        return switch (targetType) {
            case "city" -> List.of(
                    new ChildRule("sub_map", "子地圖", "sub_maps", "city_id"),
                    new ChildRule("poi", "POI", "pois", "city_id"),
                    new ChildRule("indoor_building", "室內建築", "buildings", "city_id"));
            case "sub_map" -> List.of(
                    new ChildRule("poi", "POI", "pois", "sub_map_id"),
                    new ChildRule("indoor_building", "室內建築", "buildings", "sub_map_id"));
            case "poi" -> List.of(new ChildRule("indoor_building", "室內建築", "buildings", "poi_id"));
            case "indoor_building" -> List.of(
                    new ChildRule("indoor_floor", "室內樓層", "indoor_floors", "building_id"),
                    new ChildRule("indoor_node", "室內標記 / 疊加物", "indoor_nodes", "building_id"));
            case "indoor_floor" -> List.of(new ChildRule("indoor_node", "室內標記 / 疊加物", "indoor_nodes", "floor_id"));
            case "storyline" -> List.of(new ChildRule("story_chapter", "故事章節", "story_chapters", "storyline_id"));
            case "experience_flow" -> List.of(new ChildRule("experience_flow_step", "體驗流程步驟", "experience_flow_steps", "flow_id"));
            default -> List.of();
        };
    }

    private List<Map<String, Object>> query(String sql, Object... args) {
        try {
            return jdbcTemplate.queryForList(sql, args);
        } catch (DataAccessException ex) {
            return List.of();
        }
    }

    private Integer count(String sql, Object... args) {
        try {
            return jdbcTemplate.queryForObject(sql, Integer.class, args);
        } catch (DataAccessException ex) {
            return 0;
        }
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

    private record ChildRule(String targetType, String label, String tableName, String parentColumn) {
    }
}
