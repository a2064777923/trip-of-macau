package com.aoxiaoyou.admin.common.content;

import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class AdminLifecycleTargetRegistry {

    private final Map<String, TargetDescriptor> descriptors = List.of(
            descriptor("city", "城市", "cities", "code", List.of("name_zht", "name_zh", "name_en"), "published_at", true, List.of("sub_map", "poi")),
            descriptor("sub_map", "子地圖", "sub_maps", "code", List.of("name_zht", "name_zh", "name_en"), "published_at", true, List.of("poi")),
            descriptor("poi", "POI", "pois", "code", List.of("name_zht", "name_zh", "name_en"), "published_at", true, List.of("indoor_building")),
            descriptor("indoor_building", "室內建築", "buildings", "building_code", List.of("name_zht", "name_zh", "name_en"), "published_at", true, List.of("indoor_floor", "indoor_node")),
            descriptor("indoor_floor", "室內樓層", "indoor_floors", "floor_code", List.of("floor_name_zht", "floor_name_zh", "floor_name_en"), "published_at", true, List.of("indoor_node")),
            descriptor("indoor_node", "室內標記 / 疊加物", "indoor_nodes", "marker_code", List.of("node_name_zht", "node_name_zh", "node_name_en"), null, true, List.of()),
            descriptor("storyline", "故事線", "storylines", "code", List.of("name_zht", "name_zh", "name_en"), "published_at", true, List.of("story_chapter")),
            descriptor("story_chapter", "故事章節", "story_chapters", "anchor_target_code", List.of("title_zht", "title_zh", "title_en"), "published_at", true, List.of()),
            descriptor("content_block", "內容積木", "story_content_blocks", "code", List.of("title_zht", "title_zh", "title_en"), "published_at", true, List.of()),
            descriptor("content_asset", "媒體資源", "content_assets", "object_key", List.of("original_filename", "object_key"), "published_at", true, List.of()),
            descriptor("experience_flow", "體驗流程", "experience_flows", "code", List.of("name_zht", "name_zh", "name_en"), "published_at", true, List.of("experience_flow_step")),
            descriptor("experience_flow_step", "體驗流程步驟", "experience_flow_steps", "step_code", List.of("step_name_zht", "step_name_zh", "step_name_en"), null, true, List.of()),
            descriptor("experience_binding", "體驗綁定", "experience_bindings", "owner_code", List.of("owner_code", "binding_role"), null, true, List.of()),
            descriptor("experience_override", "體驗覆寫", "experience_overrides", "target_step_code", List.of("target_step_code", "override_mode"), null, true, List.of()),
            descriptor("collectible", "收集物", "collectibles", "collectible_code", List.of("name_zht", "name_zh", "name_en"), null, true, List.of()),
            descriptor("reward", "兌換獎勵", "rewards", "code", List.of("name_zht", "name_zh", "name_en"), null, true, List.of()),
            descriptor("game_reward", "遊戲內獎勵", "game_rewards", "code", List.of("name_zht", "name_zh", "name_en"), null, true, List.of()),
            descriptor("redeemable_prize", "兌換獎勵物品", "redeemable_prizes", "code", List.of("name_zht", "name_zh", "name_en"), null, true, List.of()),
            descriptor("honor", "榮譽與稱號", "badges", "badge_code", List.of("name_zht", "name_zh", "name_en"), null, true, List.of()),
            descriptor("activity", "營運活動", "activities", "code", List.of("title_zht", "title_zh", "title_en", "title"), null, true, List.of())
    ).stream().collect(java.util.stream.Collectors.toUnmodifiableMap(TargetDescriptor::targetType, item -> item));

    private static TargetDescriptor descriptor(
            String targetType,
            String label,
            String tableName,
            String codeColumn,
            List<String> nameColumns,
            String publishedAtColumn,
            boolean publicRuntimeRelevant,
            List<String> childTargetTypes) {
        return new TargetDescriptor(
                targetType,
                label,
                tableName,
                "id",
                codeColumn,
                nameColumns,
                "status",
                publishedAtColumn,
                "deleted",
                true,
                publicRuntimeRelevant,
                List.of("publish", "unpublish", "remove"),
                childTargetTypes);
    }

    public Collection<TargetDescriptor> listDescriptors() {
        return descriptors.values();
    }

    public Optional<TargetDescriptor> find(String targetType) {
        return Optional.ofNullable(descriptors.get(targetType));
    }

    public TargetDescriptor require(String targetType) {
        return find(targetType)
                .orElseThrow(() -> new com.aoxiaoyou.admin.common.exception.BusinessException(4041, "Unsupported lifecycle target type"));
    }

    public record TargetDescriptor(
            String targetType,
            String label,
            String tableName,
            String idColumn,
            String codeColumn,
            List<String> nameColumns,
            String statusColumn,
            String publishedAtColumn,
            String deletedColumn,
            boolean statusMutable,
            boolean publicRuntimeRelevant,
            List<String> supportedActions,
            List<String> childTargetTypes) {
    }
}
