package com.aoxiaoyou.admin.service.impl;

import com.aoxiaoyou.admin.common.api.PageResponse;
import com.aoxiaoyou.admin.common.enums.ContentStatus;
import com.aoxiaoyou.admin.common.exception.BusinessException;
import com.aoxiaoyou.admin.dto.request.AdminStoryLineLifecycleRequest;
import com.aoxiaoyou.admin.dto.request.AdminStoryLineUpsertRequest;
import com.aoxiaoyou.admin.dto.response.AdminStoryLineDeleteImpactResponse;
import com.aoxiaoyou.admin.dto.response.AdminStoryLineDetailResponse;
import com.aoxiaoyou.admin.dto.response.AdminStoryLineListItemResponse;
import com.aoxiaoyou.admin.entity.City;
import com.aoxiaoyou.admin.entity.StoryChapter;
import com.aoxiaoyou.admin.entity.StoryLine;
import com.aoxiaoyou.admin.mapper.CityMapper;
import com.aoxiaoyou.admin.mapper.StoryChapterMapper;
import com.aoxiaoyou.admin.mapper.StoryLineMapper;
import com.aoxiaoyou.admin.mapper.SubMapMapper;
import com.aoxiaoyou.admin.service.AdminContentRelationService;
import com.aoxiaoyou.admin.service.AdminStoryLineService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AdminStoryLineServiceImpl implements AdminStoryLineService {

    private final StoryLineMapper storyLineMapper;
    private final StoryChapterMapper storyChapterMapper;
    private final CityMapper cityMapper;
    private final SubMapMapper subMapMapper;
    private final AdminContentRelationService adminContentRelationService;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public PageResponse<AdminStoryLineListItemResponse> page(long pageNum, long pageSize, String keyword, String status) {
        Page<StoryLine> page = storyLineMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<StoryLine>()
                        .and(StringUtils.hasText(keyword), q -> q
                                .like(StoryLine::getCode, keyword)
                                .or().like(StoryLine::getNameZh, keyword)
                                .or().like(StoryLine::getNameEn, keyword)
                                .or().like(StoryLine::getNameZht, keyword)
                                .or().like(StoryLine::getNamePt, keyword))
                        .eq(StringUtils.hasText(status), StoryLine::getStatus, status)
                        .orderByAsc(StoryLine::getSortOrder)
                        .orderByAsc(StoryLine::getId));

        Page<AdminStoryLineListItemResponse> responsePage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        responsePage.setRecords(page.getRecords().stream().map(this::toListItem).toList());
        return PageResponse.of(responsePage);
    }

    @Override
    public AdminStoryLineDetailResponse detail(Long storylineId) {
        return toDetail(requireStoryline(storylineId));
    }

    @Override
    public AdminStoryLineDetailResponse create(AdminStoryLineUpsertRequest.Upsert request) {
        StorylineRelationPayload relationPayload = resolveStorylineRelations(request);
        StoryLine storyLine = new StoryLine();
        applyRequest(storyLine, request, relationPayload);
        storyLineMapper.insert(storyLine);
        syncRelations(storyLine.getId(), relationPayload);
        return toDetail(requireStoryline(storyLine.getId()));
    }

    @Override
    public AdminStoryLineDetailResponse update(Long storylineId, AdminStoryLineUpsertRequest.Upsert request) {
        StorylineRelationPayload relationPayload = resolveStorylineRelations(request);
        StoryLine storyLine = requireStoryline(storylineId);
        applyRequest(storyLine, request, relationPayload);
        storyLineMapper.updateById(storyLine);
        syncRelations(storylineId, relationPayload);
        return toDetail(requireStoryline(storylineId));
    }

    @Override
    public AdminStoryLineDeleteImpactResponse deleteImpact(Long storylineId) {
        return buildDeleteImpact(requireStoryline(storylineId));
    }

    @Override
    public AdminStoryLineDetailResponse updateLifecycle(Long storylineId, AdminStoryLineLifecycleRequest request) {
        StoryLine storyLine = requireStoryline(storylineId);
        String action = normalizeAction(request.getAction());
        if ("hard_delete".equals(action)) {
            deleteWithGuard(storyLine, request.getConfirmationText());
            return null;
        }

        if (!"archive".equals(action) && !"unpublish".equals(action)) {
            throw new BusinessException(4011, "Unsupported storyline lifecycle action");
        }
        storyLine.setStatus(ContentStatus.ARCHIVED.getCode());
        storyLine.setPublishedAt(null);
        storyLineMapper.updateById(storyLine);
        return toDetail(requireStoryline(storylineId));
    }

    @Override
    public void delete(Long storylineId) {
        deleteWithGuard(requireStoryline(storylineId), null);
    }

    private StoryLine requireStoryline(Long storylineId) {
        StoryLine storyLine = storyLineMapper.selectById(storylineId);
        if (storyLine == null) {
            throw new BusinessException(4042, "Storyline not found");
        }
        return storyLine;
    }

    private void deleteWithGuard(StoryLine storyLine, String confirmationText) {
        AdminStoryLineDeleteImpactResponse impact = buildDeleteImpact(storyLine);
        if (!Boolean.TRUE.equals(impact.getHardDeleteAllowed())) {
            throw new BusinessException(4060, "Storyline has runtime dependencies; archive it instead of hard deleting");
        }
        if (!Objects.equals(storyLine.getCode(), confirmationText)) {
            throw new BusinessException(4061, "Storyline code confirmation is required for hard delete");
        }
        storyLineMapper.deleteById(storyLine.getId());
    }

    private AdminStoryLineDeleteImpactResponse buildDeleteImpact(StoryLine storyLine) {
        Map<String, Long> counts = buildDependencyCounts(storyLine);
        boolean publicVisible = ContentStatus.PUBLISHED.getCode().equalsIgnoreCase(Objects.toString(storyLine.getStatus(), ""));
        boolean hasDependencies = counts.values().stream().anyMatch(count -> count != null && count > 0);
        boolean hardDeleteAllowed = !publicVisible
                && isDraftLike(storyLine.getStatus())
                && !hasDependencies;
        List<String> blockingReasons = new ArrayList<>();
        List<String> warningReasons = new ArrayList<>();

        if (publicVisible) {
            blockingReasons.add("故事線仍在小程序公開列表中，必須先封存或下線。");
        }
        if (!isDraftLike(storyLine.getStatus())) {
            blockingReasons.add("只有無依賴的草稿故事線允許永久刪除。");
        }
        counts.forEach((key, value) -> {
            if (value != null && value > 0) {
                blockingReasons.add(dependencyLabel(key) + "：" + value);
            }
        });
        if (publicVisible) {
            warningReasons.add("封存後不會刪除章節、素材、用戶故事模式記錄與探索事件。");
        }
        if (counts.getOrDefault("userStorylineSessions", 0L) > 0 || counts.getOrDefault("userExplorationEvents", 0L) > 0) {
            warningReasons.add("已有旅客進度或互動事件，請保留資料供客服與營運追溯。");
        }

        return AdminStoryLineDeleteImpactResponse.builder()
                .storylineId(storyLine.getId())
                .code(storyLine.getCode())
                .name(firstText(storyLine.getNameZht(), storyLine.getNameZh(), storyLine.getNameEn(), storyLine.getCode()))
                .status(storyLine.getStatus())
                .publicVisible(publicVisible)
                .dependencyCounts(counts)
                .hardDeleteAllowed(hardDeleteAllowed)
                .recommendedAction(hardDeleteAllowed ? "hard_delete" : "archive")
                .blockingReasons(hardDeleteAllowed ? Collections.emptyList() : blockingReasons)
                .warningReasons(warningReasons)
                .build();
    }

    private Map<String, Long> buildDependencyCounts(StoryLine storyLine) {
        Long storylineId = storyLine.getId();
        String storylineCode = storyLine.getCode();
        Map<String, Long> counts = new LinkedHashMap<>();
        counts.put("chapters", safeCount("SELECT COUNT(*) FROM story_chapters WHERE storyline_id = ? AND COALESCE(deleted, 0) = 0", storylineId));
        counts.put("chapterBlockLinks", safeCount("""
                SELECT COUNT(*)
                FROM story_chapter_block_links link
                JOIN story_chapters chapter ON chapter.id = link.chapter_id
                WHERE chapter.storyline_id = ?
                  AND COALESCE(chapter.deleted, 0) = 0
                  AND COALESCE(link.deleted, 0) = 0
                """, storylineId));
        counts.put("contentRelationOwnerLinks", safeCount("""
                SELECT COUNT(*)
                FROM content_relation_links
                WHERE owner_type = 'storyline'
                  AND owner_id = ?
                  AND COALESCE(deleted, 0) = 0
                """, storylineId));
        counts.put("contentRelationTargetLinks", safeCount("""
                SELECT COUNT(*)
                FROM content_relation_links
                WHERE target_type = 'storyline'
                  AND (target_id = ? OR target_code = ?)
                  AND COALESCE(deleted, 0) = 0
                """, storylineId, storylineCode));
        counts.put("contentAssetLinks", safeCount("""
                SELECT COUNT(*)
                FROM content_asset_links
                WHERE entity_type = 'storyline'
                  AND entity_id = ?
                  AND COALESCE(deleted, 0) = 0
                """, storylineId));
        counts.put("experienceBindings", safeCount("""
                SELECT COUNT(*)
                FROM experience_bindings
                WHERE owner_type = 'storyline'
                  AND (owner_id = ? OR owner_code = ?)
                  AND COALESCE(deleted, 0) = 0
                """, storylineId, storylineCode));
        counts.put("experienceOverrides", safeCount("""
                SELECT COUNT(*)
                FROM experience_overrides
                WHERE (
                    (owner_type = 'storyline' AND owner_id = ?)
                    OR (target_owner_type = 'storyline' AND target_owner_id = ?)
                  )
                  AND COALESCE(deleted, 0) = 0
                """, storylineId, storylineId));
        counts.put("explorationElements", safeCount("""
                SELECT COUNT(*)
                FROM exploration_elements
                WHERE COALESCE(deleted, 0) = 0
                  AND (
                    storyline_id = ?
                    OR (owner_type = 'storyline' AND (owner_id = ? OR owner_code = ?))
                    OR story_chapter_id IN (
                      SELECT id FROM story_chapters WHERE storyline_id = ? AND COALESCE(deleted, 0) = 0
                    )
                  )
                """, storylineId, storylineId, storylineCode, storylineId));
        counts.put("userExplorationEvents", safeCount("""
                SELECT COUNT(*)
                FROM user_exploration_events event
                WHERE event.element_id IN (
                    SELECT element.id
                    FROM exploration_elements element
                    WHERE COALESCE(element.deleted, 0) = 0
                      AND (
                        element.storyline_id = ?
                        OR (element.owner_type = 'storyline' AND (element.owner_id = ? OR element.owner_code = ?))
                        OR element.story_chapter_id IN (
                          SELECT id FROM story_chapters WHERE storyline_id = ? AND COALESCE(deleted, 0) = 0
                        )
                      )
                  )
                  OR event.element_code IN (
                    SELECT element.element_code
                    FROM exploration_elements element
                    WHERE COALESCE(element.deleted, 0) = 0
                      AND (
                        element.storyline_id = ?
                        OR (element.owner_type = 'storyline' AND (element.owner_id = ? OR element.owner_code = ?))
                        OR element.story_chapter_id IN (
                          SELECT id FROM story_chapters WHERE storyline_id = ? AND COALESCE(deleted, 0) = 0
                        )
                      )
                    )
                """, storylineId, storylineId, storylineCode, storylineId, storylineId, storylineId, storylineCode, storylineId));
        counts.put("userStorylineSessions", safeCount("SELECT COUNT(*) FROM user_storyline_sessions WHERE storyline_id = ?", storylineId));
        counts.put("userProgressRows", safeCount("""
                SELECT COUNT(*)
                FROM user_progress
                WHERE COALESCE(deleted, 0) = 0
                  AND (storyline_id = ? OR active_storyline_id = ?)
                """, storylineId, storylineId));
        counts.put("storyMaterialPackages", safeCount("""
                SELECT COUNT(*)
                FROM story_material_packages
                WHERE storyline_id = ?
                  AND COALESCE(deleted, 0) = 0
                """, storylineId));
        counts.put("storyMaterialPackageItems", safeCount("""
                SELECT COUNT(*)
                FROM story_material_package_items item
                LEFT JOIN story_material_packages pkg ON pkg.id = item.package_id
                WHERE COALESCE(item.deleted, 0) = 0
                  AND (
                    pkg.storyline_id = ?
                    OR (item.target_type = 'storyline' AND (item.target_id = ? OR item.target_code = ?))
                    OR (item.target_type = 'story_chapter' AND item.target_id IN (
                      SELECT id FROM story_chapters WHERE storyline_id = ? AND COALESCE(deleted, 0) = 0
                    ))
                  )
                """, storylineId, storylineId, storylineCode, storylineId));
        counts.put("storyMaterialPackageItemVersions", safeCount("""
                SELECT COUNT(*)
                FROM story_material_package_item_versions version
                JOIN story_material_package_items item ON item.id = version.package_item_id
                LEFT JOIN story_material_packages pkg ON pkg.id = item.package_id
                WHERE COALESCE(version.deleted, 0) = 0
                  AND COALESCE(item.deleted, 0) = 0
                  AND (
                    pkg.storyline_id = ?
                    OR (item.target_type = 'storyline' AND (item.target_id = ? OR item.target_code = ?))
                    OR (item.target_type = 'story_chapter' AND item.target_id IN (
                      SELECT id FROM story_chapters WHERE storyline_id = ? AND COALESCE(deleted, 0) = 0
                    ))
                  )
                """, storylineId, storylineId, storylineCode, storylineId));
        return counts;
    }

    private long safeCount(String sql, Object... args) {
        try {
            Long count = jdbcTemplate.queryForObject(sql, Long.class, args);
            return count == null ? 0L : count;
        } catch (DataAccessException ex) {
            return 0L;
        }
    }

    private boolean isDraftLike(String status) {
        if (!StringUtils.hasText(status)) {
            return true;
        }
        String normalized = status.trim().toLowerCase(Locale.ROOT);
        return ContentStatus.DRAFT.getCode().equals(normalized) || ContentStatus.EDITING.getCode().equals(normalized);
    }

    private String normalizeAction(String action) {
        if (!StringUtils.hasText(action)) {
            throw new BusinessException(4010, "Lifecycle action is required");
        }
        return action.trim().toLowerCase(Locale.ROOT);
    }

    private String dependencyLabel(String key) {
        return switch (key) {
            case "chapters" -> "章節";
            case "chapterBlockLinks" -> "章節內容積木關聯";
            case "contentRelationOwnerLinks" -> "此故事線對外內容關聯";
            case "contentRelationTargetLinks" -> "其他內容綁定此故事線";
            case "contentAssetLinks" -> "內容資產關聯";
            case "experienceBindings" -> "體驗流程綁定";
            case "experienceOverrides" -> "體驗流程覆寫";
            case "explorationElements" -> "探索元素";
            case "userExplorationEvents" -> "旅客探索事件";
            case "userStorylineSessions" -> "旅客故事模式 session";
            case "userProgressRows" -> "旅客進度記錄";
            case "storyMaterialPackages" -> "故事素材包";
            case "storyMaterialPackageItems" -> "故事素材項";
            case "storyMaterialPackageItemVersions" -> "故事素材版本";
            default -> key;
        };
    }

    private String firstText(String... values) {
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                return value.trim();
            }
        }
        return "";
    }

    private void verifyCity(Long cityId) {
        if (cityId != null && cityMapper.selectById(cityId) == null) {
            throw new BusinessException(4043, "City not found");
        }
    }

    private void applyRequest(StoryLine storyLine, AdminStoryLineUpsertRequest.Upsert request, StorylineRelationPayload relationPayload) {
        storyLine.setCityId(relationPayload.primaryCityId());
        storyLine.setCode(request.getCode());
        storyLine.setNameZh(request.getNameZh());
        storyLine.setNameEn(request.getNameEn());
        storyLine.setNameZht(request.getNameZht());
        storyLine.setNamePt(request.getNamePt());
        storyLine.setDescriptionZh(request.getDescriptionZh());
        storyLine.setDescriptionEn(request.getDescriptionEn());
        storyLine.setDescriptionZht(request.getDescriptionZht());
        storyLine.setDescriptionPt(request.getDescriptionPt());
        storyLine.setEstimatedMinutes(request.getEstimatedMinutes() == null ? 0 : request.getEstimatedMinutes());
        storyLine.setDifficulty(StringUtils.hasText(request.getDifficulty()) ? request.getDifficulty() : "easy");
        storyLine.setCoverAssetId(request.getCoverAssetId());
        storyLine.setBannerAssetId(request.getBannerAssetId());
        storyLine.setRewardBadgeZh(request.getRewardBadgeZh());
        storyLine.setRewardBadgeEn(request.getRewardBadgeEn());
        storyLine.setRewardBadgeZht(request.getRewardBadgeZht());
        storyLine.setRewardBadgePt(request.getRewardBadgePt());
        storyLine.setStatus(StringUtils.hasText(request.getStatus()) ? request.getStatus() : "draft");
        storyLine.setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder());
        storyLine.setPublishedAt(parseDateTime(request.getPublishedAt()));
    }

    private AdminStoryLineListItemResponse toListItem(StoryLine storyLine) {
        List<Long> cityBindingIds = getCityBindingIds(storyLine);
        List<Long> subMapBindingIds = adminContentRelationService.listTargetIds("storyline", storyLine.getId(), "sub_map_binding", "sub_map");
        return AdminStoryLineListItemResponse.builder()
                .storylineId(storyLine.getId())
                .cityId(cityBindingIds.isEmpty() ? storyLine.getCityId() : cityBindingIds.get(0))
                .cityName(joinCityNames(cityBindingIds))
                .cityBindings(cityBindingIds)
                .subMapBindings(subMapBindingIds)
                .code(storyLine.getCode())
                .nameZh(storyLine.getNameZh())
                .difficulty(storyLine.getDifficulty())
                .status(storyLine.getStatus())
                .estimatedMinutes(storyLine.getEstimatedMinutes())
                .totalChapters(countChapters(storyLine.getId()))
                .coverAssetId(storyLine.getCoverAssetId())
                .sortOrder(storyLine.getSortOrder())
                .createdAt(storyLine.getCreatedAt())
                .build();
    }

    private AdminStoryLineDetailResponse toDetail(StoryLine storyLine) {
        List<Long> cityBindingIds = getCityBindingIds(storyLine);
        List<Long> subMapBindingIds = adminContentRelationService.listTargetIds("storyline", storyLine.getId(), "sub_map_binding", "sub_map");
        List<Long> attachmentAssetIds = adminContentRelationService.listTargetIds("storyline", storyLine.getId(), "attachment_asset", "asset");
        return AdminStoryLineDetailResponse.builder()
                .storylineId(storyLine.getId())
                .cityId(cityBindingIds.isEmpty() ? storyLine.getCityId() : cityBindingIds.get(0))
                .cityName(joinCityNames(cityBindingIds))
                .cityBindings(cityBindingIds)
                .subMapBindings(subMapBindingIds)
                .attachmentAssetIds(attachmentAssetIds)
                .code(storyLine.getCode())
                .nameZh(storyLine.getNameZh())
                .nameEn(storyLine.getNameEn())
                .nameZht(storyLine.getNameZht())
                .namePt(storyLine.getNamePt())
                .descriptionZh(storyLine.getDescriptionZh())
                .descriptionEn(storyLine.getDescriptionEn())
                .descriptionZht(storyLine.getDescriptionZht())
                .descriptionPt(storyLine.getDescriptionPt())
                .estimatedMinutes(storyLine.getEstimatedMinutes())
                .difficulty(storyLine.getDifficulty())
                .coverAssetId(storyLine.getCoverAssetId())
                .bannerAssetId(storyLine.getBannerAssetId())
                .rewardBadgeZh(storyLine.getRewardBadgeZh())
                .rewardBadgeEn(storyLine.getRewardBadgeEn())
                .rewardBadgeZht(storyLine.getRewardBadgeZht())
                .rewardBadgePt(storyLine.getRewardBadgePt())
                .status(storyLine.getStatus())
                .totalChapters(countChapters(storyLine.getId()))
                .sortOrder(storyLine.getSortOrder())
                .publishedAt(storyLine.getPublishedAt())
                .createdAt(storyLine.getCreatedAt())
                .updatedAt(storyLine.getUpdatedAt())
                .build();
    }

    private Integer countChapters(Long storylineId) {
        return Math.toIntExact(storyChapterMapper.selectCount(new LambdaQueryWrapper<StoryChapter>()
                .eq(StoryChapter::getStorylineId, storylineId)));
    }

    private LocalDateTime parseDateTime(String value) {
        return StringUtils.hasText(value) ? LocalDateTime.parse(value) : null;
    }

    private StorylineRelationPayload resolveStorylineRelations(AdminStoryLineUpsertRequest.Upsert request) {
        List<Long> cityIds = normalizeIds(request.getCityBindings());
        if (cityIds.isEmpty() && request.getCityId() != null) {
            cityIds = List.of(request.getCityId());
        }
        cityIds.forEach(this::verifyCity);

        List<Long> subMapIds = normalizeIds(request.getSubMapBindings());
        if (!subMapIds.isEmpty()) {
            Map<Long, Long> subMapCityIds = subMapIds.stream()
                    .collect(LinkedHashMap::new, (map, id) -> {
                        var subMap = subMapMapper.selectById(id);
                        if (subMap == null) {
                            throw new BusinessException(4048, "Sub-map not found");
                        }
                        map.put(id, subMap.getCityId());
                    }, Map::putAll);
            if (!cityIds.isEmpty()) {
                boolean allCompatible = subMapCityIds.values().stream().allMatch(cityIds::contains);
                if (!allCompatible) {
                    throw new BusinessException(4049, "Sub-map bindings must belong to bound cities");
                }
            } else {
                cityIds = subMapCityIds.values().stream().filter(Objects::nonNull).distinct().toList();
            }
        }

        return new StorylineRelationPayload(
                cityIds.isEmpty() ? null : cityIds.get(0),
                cityIds,
                subMapIds,
                normalizeIds(request.getAttachmentAssetIds()));
    }

    private void syncRelations(Long storylineId, StorylineRelationPayload payload) {
        adminContentRelationService.syncTargetIds("storyline", storylineId, "city_binding", "city", payload.cityBindings());
        adminContentRelationService.syncTargetIds("storyline", storylineId, "sub_map_binding", "sub_map", payload.subMapBindings());
        adminContentRelationService.syncTargetIds("storyline", storylineId, "attachment_asset", "asset", payload.attachmentAssetIds());
    }

    private List<Long> getCityBindingIds(StoryLine storyLine) {
        if (storyLine.getId() == null) {
            return storyLine.getCityId() == null ? Collections.emptyList() : List.of(storyLine.getCityId());
        }
        List<Long> cityBindingIds = adminContentRelationService.listTargetIds("storyline", storyLine.getId(), "city_binding", "city");
        if (cityBindingIds.isEmpty() && storyLine.getCityId() != null) {
            return List.of(storyLine.getCityId());
        }
        return cityBindingIds;
    }

    private List<Long> normalizeIds(List<Long> values) {
        if (values == null || values.isEmpty()) {
            return Collections.emptyList();
        }
        return values.stream().filter(Objects::nonNull).distinct().toList();
    }

    private String joinCityNames(List<Long> cityIds) {
        if (cityIds == null || cityIds.isEmpty()) {
            return null;
        }
        List<String> cityNames = new ArrayList<>();
        for (Long cityId : cityIds) {
            City city = cityMapper.selectById(cityId);
            if (city != null && StringUtils.hasText(city.getNameZh())) {
                cityNames.add(city.getNameZh());
            }
        }
        return cityNames.isEmpty() ? null : String.join(" / ", cityNames);
    }

    private record StorylineRelationPayload(
            Long primaryCityId,
            List<Long> cityBindings,
            List<Long> subMapBindings,
            List<Long> attachmentAssetIds
    ) {
    }
}
