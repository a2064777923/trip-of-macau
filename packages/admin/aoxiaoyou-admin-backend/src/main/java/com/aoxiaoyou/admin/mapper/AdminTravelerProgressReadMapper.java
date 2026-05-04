package com.aoxiaoyou.admin.mapper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface AdminTravelerProgressReadMapper {

    @Select({
            "SELECT",
            "  p.user_id AS userId,",
            "  p.interface_mode AS interfaceMode,",
            "  p.font_scale AS fontScale,",
            "  p.high_contrast AS highContrast,",
            "  p.voice_guide_enabled AS voiceGuideEnabled,",
            "  p.senior_mode AS seniorMode,",
            "  p.locale_code AS localeCode,",
            "  p.emergency_contact_name AS emergencyContactName,",
            "  p.emergency_contact_phone AS emergencyContactPhone,",
            "  CAST(p.runtime_overrides_json AS CHAR) AS runtimeOverridesJson",
            "FROM user_preferences p",
            "WHERE p.user_id = #{userId}",
            "  AND p.deleted = 0",
            "LIMIT 1"
    })
    UserPreferenceRow selectUserPreference(@Param("userId") Long userId);

    @Select({
            "SELECT scope_type AS scopeType, scope_id AS scopeId, scope_name AS scopeName, relation_label AS relationLabel, source AS source",
            "FROM (",
            "  SELECT 'city' AS scope_type, c.id AS scope_id, COALESCE(NULLIF(c.name_zht, ''), c.name_zh, c.name_en, c.code) AS scope_name,",
            "         'current_city' AS relation_label, 'user_profiles.current_city_id' AS source",
            "  FROM user_profiles u",
            "  JOIN cities c ON c.id = u.current_city_id",
            "  WHERE u.id = #{userId} AND u.deleted = 0",
            "  UNION DISTINCT",
            "  SELECT 'sub_map' AS scope_type, sm.id AS scope_id, COALESCE(NULLIF(sm.name_zht, ''), sm.name_zh, sm.name_en, sm.code) AS scope_name,",
            "         'recent_checkin' AS relation_label, 'user_checkins' AS source",
            "  FROM user_checkins uc",
            "  JOIN pois p ON p.id = uc.poi_id",
            "  JOIN sub_maps sm ON sm.id = p.sub_map_id",
            "  WHERE uc.user_id = #{userId}",
            "  UNION DISTINCT",
            "  SELECT 'storyline' AS scope_type, s.id AS scope_id, COALESCE(NULLIF(s.name_zht, ''), s.name_zh, s.name_en, s.code) AS scope_name,",
            "         'legacy_progress' AS relation_label, 'user_progress' AS source",
            "  FROM user_progress up",
            "  JOIN storylines s ON s.id = up.storyline_id",
            "  WHERE up.user_id = #{userId} AND up.deleted = 0 AND up.storyline_id IS NOT NULL",
            ") scoped",
            "ORDER BY FIELD(scope_type, 'city', 'sub_map', 'storyline'), scope_id"
    })
    List<LinkedScopeRow> selectLinkedScopes(@Param("userId") Long userId);

    @Select({
            "/* legacy traveler_progress compatibility snapshot sourced from user_progress */",
            "SELECT",
            "  up.user_id AS userId,",
            "  up.storyline_id AS scopeId,",
            "  CASE WHEN up.storyline_id IS NULL THEN 'global' ELSE 'storyline' END AS scopeType,",
            "  COALESCE(NULLIF(s.name_zht, ''), s.name_zh, s.name_en, s.code) AS scopeName,",
            "  up.progress_percent AS progressPercent,",
            "  up.active_storyline_id AS activeStorylineId,",
            "  up.completed_storyline AS completedStoryline,",
            "  up.last_seen_at AS lastSeenAt,",
            "  up.updated_at AS updatedAt",
            "FROM user_progress up",
            "LEFT JOIN storylines s ON s.id = up.storyline_id",
            "WHERE up.user_id = #{userId}",
            "  AND up.deleted = 0",
            "ORDER BY up.updated_at DESC, up.id DESC"
    })
    List<LegacyProgressRow> selectLegacyProgressRows(@Param("userId") Long userId);

    @Select({
            "SELECT",
            "  us.session_id AS sessionId,",
            "  us.user_id AS userId,",
            "  us.storyline_id AS storylineId,",
            "  COALESCE(NULLIF(s.name_zht, ''), s.name_zh, s.name_en, s.code) AS storylineName,",
            "  us.current_chapter_id AS currentChapterId,",
            "  us.status AS status,",
            "  us.started_at AS startedAt,",
            "  us.last_event_at AS lastEventAt,",
            "  us.exited_at AS exitedAt,",
            "  us.event_count AS eventCount,",
            "  us.exit_cleared_temporary_state AS exitClearedTemporaryState,",
            "  CAST(us.temporary_step_state_json AS CHAR) AS temporaryStepStateJson",
            "FROM user_storyline_sessions us",
            "LEFT JOIN storylines s ON s.id = us.storyline_id",
            "WHERE us.user_id = #{userId}",
            "ORDER BY us.started_at DESC",
            "LIMIT #{limit}"
    })
    List<StorylineSessionRow> selectStorylineSessions(
            @Param("userId") Long userId,
            @Param("limit") int limit);

    @Select({
            "SELECT",
            "  rr.id AS redemptionId,",
            "  rr.user_id AS userId,",
            "  rr.reward_id AS rewardId,",
            "  COALESCE(NULLIF(r.name_zht, ''), r.name_zh, r.name_en, r.code) AS rewardName,",
            "  rr.redemption_status AS redemptionStatus,",
            "  rr.stamp_cost_snapshot AS stampCostSnapshot,",
            "  rr.redeemed_at AS redeemedAt,",
            "  rr.expires_at AS expiresAt",
            "FROM reward_redemptions rr",
            "LEFT JOIN rewards r ON r.id = rr.reward_id",
            "WHERE rr.user_id = #{userId}",
            "  AND rr.deleted = 0",
            "ORDER BY COALESCE(rr.redeemed_at, rr.created_at) DESC, rr.id DESC",
            "LIMIT #{limit}"
    })
    List<RewardRedemptionRow> selectRewardRedemptions(
            @Param("userId") Long userId,
            @Param("limit") int limit);

    @Select({
            "SELECT",
            "  (SELECT COUNT(*) FROM user_checkins c WHERE c.user_id = #{userId}",
            "     AND (#{from} IS NULL OR c.checked_at >= #{from})) AS recentCheckinCount,",
            "  (SELECT COUNT(*) FROM user_exploration_events e WHERE e.user_id = #{userId}",
            "     AND (#{from} IS NULL OR e.occurred_at >= #{from})) AS recentExplorationEventCount,",
            "  (SELECT COUNT(*) FROM trigger_logs t WHERE t.user_id = #{userId}",
            "     AND (#{from} IS NULL OR t.created_at >= #{from})) AS recentTriggerCount"
    })
    RecentContextCountsRow selectRecentContextCounts(
            @Param("userId") Long userId,
            @Param("from") LocalDateTime from);

    @Select({
            "SELECT",
            "  CONCAT('checkin:', c.id) AS entryId,",
            "  'checkin' AS entryType,",
            "  'user_checkins' AS sourceTable,",
            "  c.id AS sourceRecordId,",
            "  c.user_id AS userId,",
            "  p.storyline_id AS storylineId,",
            "  COALESCE(NULLIF(s.name_zht, ''), s.name_zh, s.name_en, s.code) AS storylineName,",
            "  NULL AS chapterId,",
            "  NULL AS chapterName,",
            "  c.poi_id AS poiId,",
            "  COALESCE(NULLIF(p.name_zht, ''), p.name_zh, p.name_en, CONCAT('POI#', c.poi_id)) AS poiName,",
            "  p.city_id AS cityId,",
            "  p.sub_map_id AS subMapId,",
            "  'checked' AS status,",
            "  NULL AS rewardType,",
            "  NULL AS rewardId,",
            "  NULL AS gameRewardId,",
            "  'Check-in' AS title,",
            "  CONCAT('triggerMode=', c.trigger_mode) AS summary,",
            "  c.trigger_mode AS payloadPreview,",
            "  NULL AS rawPayload,",
            "  c.checked_at AS occurredAt",
            "FROM user_checkins c",
            "LEFT JOIN pois p ON p.id = c.poi_id",
            "LEFT JOIN storylines s ON s.id = p.storyline_id",
            "WHERE c.user_id = #{userId}",
            "ORDER BY c.checked_at DESC, c.id DESC"
    })
    List<TimelineSourceRow> selectCheckinTimelineRows(@Param("userId") Long userId);

    @Select({
            "SELECT",
            "  CONCAT('trigger:', t.id) AS entryId,",
            "  'trigger_log' AS entryType,",
            "  'trigger_logs' AS sourceTable,",
            "  t.id AS sourceRecordId,",
            "  t.user_id AS userId,",
            "  p.storyline_id AS storylineId,",
            "  COALESCE(NULLIF(s.name_zht, ''), s.name_zh, s.name_en, s.code) AS storylineName,",
            "  NULL AS chapterId,",
            "  NULL AS chapterName,",
            "  t.poi_id AS poiId,",
            "  COALESCE(NULLIF(p.name_zht, ''), p.name_zh, p.name_en, CONCAT('POI#', t.poi_id)) AS poiName,",
            "  p.city_id AS cityId,",
            "  p.sub_map_id AS subMapId,",
            "  t.trigger_type AS status,",
            "  NULL AS rewardType,",
            "  NULL AS rewardId,",
            "  NULL AS gameRewardId,",
            "  'Trigger' AS title,",
            "  CONCAT('triggerType=', t.trigger_type) AS summary,",
            "  CONCAT('triggerType=', t.trigger_type) AS payloadPreview,",
            "  NULL AS rawPayload,",
            "  t.created_at AS occurredAt",
            "FROM trigger_logs t",
            "LEFT JOIN pois p ON p.id = t.poi_id",
            "LEFT JOIN storylines s ON s.id = p.storyline_id",
            "WHERE t.user_id = #{userId}",
            "ORDER BY t.created_at DESC, t.id DESC"
    })
    List<TimelineSourceRow> selectTriggerTimelineRows(@Param("userId") Long userId);

    @Select({
            "SELECT",
            "  CONCAT('event:', e.id) AS entryId,",
            "  'exploration_event' AS entryType,",
            "  'user_exploration_events' AS sourceTable,",
            "  e.id AS sourceRecordId,",
            "  e.user_id AS userId,",
            "  el.storyline_id AS storylineId,",
            "  COALESCE(NULLIF(s.name_zht, ''), s.name_zh, s.name_en, s.code) AS storylineName,",
            "  el.story_chapter_id AS chapterId,",
            "  COALESCE(NULLIF(sc.title_zht, ''), sc.title_zh, sc.title_en, CONCAT('Chapter#', el.story_chapter_id)) AS chapterName,",
            "  el.poi_id AS poiId,",
            "  COALESCE(NULLIF(p.name_zht, ''), p.name_zh, p.name_en, CONCAT('POI#', el.poi_id)) AS poiName,",
            "  el.city_id AS cityId,",
            "  el.sub_map_id AS subMapId,",
            "  e.event_type AS status,",
            "  NULL AS rewardType,",
            "  NULL AS rewardId,",
            "  NULL AS gameRewardId,",
            "  'Exploration Event' AS title,",
            "  e.event_type AS summary,",
            "  LEFT(CAST(e.event_payload_json AS CHAR), 160) AS payloadPreview,",
            "  CAST(e.event_payload_json AS CHAR) AS rawPayload,",
            "  e.occurred_at AS occurredAt",
            "FROM user_exploration_events e",
            "LEFT JOIN exploration_elements el ON el.id = e.element_id",
            "LEFT JOIN story_chapters sc ON sc.id = el.story_chapter_id",
            "LEFT JOIN pois p ON p.id = el.poi_id",
            "LEFT JOIN storylines s ON s.id = el.storyline_id",
            "WHERE e.user_id = #{userId}",
            "ORDER BY e.occurred_at DESC, e.id DESC"
    })
    List<TimelineSourceRow> selectExplorationTimelineRows(@Param("userId") Long userId);

    @Select({
            "SELECT",
            "  CONCAT('session:', us.session_id) AS entryId,",
            "  'storyline_session' AS entryType,",
            "  'user_storyline_sessions' AS sourceTable,",
            "  0 AS sourceRecordId,",
            "  us.user_id AS userId,",
            "  us.storyline_id AS storylineId,",
            "  COALESCE(NULLIF(s.name_zht, ''), s.name_zh, s.name_en, s.code) AS storylineName,",
            "  us.current_chapter_id AS chapterId,",
            "  COALESCE(NULLIF(sc.title_zht, ''), sc.title_zh, sc.title_en, CONCAT('Chapter#', us.current_chapter_id)) AS chapterName,",
            "  NULL AS poiId,",
            "  NULL AS poiName,",
            "  s.city_id AS cityId,",
            "  NULL AS subMapId,",
            "  us.status AS status,",
            "  NULL AS rewardType,",
            "  NULL AS rewardId,",
            "  NULL AS gameRewardId,",
            "  'Story Session' AS title,",
            "  us.status AS summary,",
            "  us.status AS payloadPreview,",
            "  CAST(us.temporary_step_state_json AS CHAR) AS rawPayload,",
            "  COALESCE(us.last_event_at, us.started_at) AS occurredAt",
            "FROM user_storyline_sessions us",
            "LEFT JOIN storylines s ON s.id = us.storyline_id",
            "LEFT JOIN story_chapters sc ON sc.id = us.current_chapter_id",
            "WHERE us.user_id = #{userId}",
            "ORDER BY COALESCE(us.last_event_at, us.started_at) DESC, us.session_id DESC"
    })
    List<TimelineSourceRow> selectStorySessionTimelineRows(@Param("userId") Long userId);

    @Select({
            "SELECT",
            "  CONCAT('reward:', rr.id) AS entryId,",
            "  'reward_redemption' AS entryType,",
            "  'reward_redemptions' AS sourceTable,",
            "  rr.id AS sourceRecordId,",
            "  rr.user_id AS userId,",
            "  NULL AS storylineId,",
            "  NULL AS storylineName,",
            "  NULL AS chapterId,",
            "  NULL AS chapterName,",
            "  NULL AS poiId,",
            "  NULL AS poiName,",
            "  NULL AS cityId,",
            "  NULL AS subMapId,",
            "  rr.redemption_status AS status,",
            "  'redeemable_reward' AS rewardType,",
            "  rr.reward_id AS rewardId,",
            "  NULL AS gameRewardId,",
            "  'Reward Redemption' AS title,",
            "  rr.redemption_status AS summary,",
            "  rr.redemption_status AS payloadPreview,",
            "  CONCAT('{\"rewardId\":', rr.reward_id, ',\"status\":\"', rr.redemption_status, '\"}') AS rawPayload,",
            "  COALESCE(rr.redeemed_at, rr.created_at) AS occurredAt",
            "FROM reward_redemptions rr",
            "WHERE rr.user_id = #{userId}",
            "  AND rr.deleted = 0",
            "ORDER BY COALESCE(rr.redeemed_at, rr.created_at) DESC, rr.id DESC"
    })
    List<TimelineSourceRow> selectRewardTimelineRows(@Param("userId") Long userId);

    @Select({
            "SELECT",
            "  CONCAT('audit:', a.id) AS entryId,",
            "  'progress_audit' AS entryType,",
            "  'user_progress_operation_audits' AS sourceTable,",
            "  a.id AS sourceRecordId,",
            "  a.target_user_id AS userId,",
            "  a.storyline_id AS storylineId,",
            "  NULL AS storylineName,",
            "  NULL AS chapterId,",
            "  NULL AS chapterName,",
            "  NULL AS poiId,",
            "  NULL AS poiName,",
            "  NULL AS cityId,",
            "  NULL AS subMapId,",
            "  a.action_type AS status,",
            "  NULL AS rewardType,",
            "  NULL AS rewardId,",
            "  NULL AS gameRewardId,",
            "  a.action_type AS title,",
            "  a.reason AS summary,",
            "  a.reason AS payloadPreview,",
            "  CAST(a.result_summary_json AS CHAR) AS rawPayload,",
            "  a.created_at AS occurredAt",
            "FROM user_progress_operation_audits a",
            "WHERE a.target_user_id = #{userId}",
            "ORDER BY a.created_at DESC, a.id DESC"
    })
    List<TimelineSourceRow> selectRepairAuditTimelineRows(@Param("userId") Long userId);

    @Select({
            "SELECT",
            "  CONCAT('game_reward:', ugr.id) AS entryId,",
            "  'game_reward_grant' AS entryType,",
            "  'user_game_reward_grants' AS sourceTable,",
            "  ugr.id AS sourceRecordId,",
            "  ugr.user_id AS userId,",
            "  el.storyline_id AS storylineId,",
            "  COALESCE(NULLIF(s.name_zht, ''), s.name_zh, s.name_en, s.code) AS storylineName,",
            "  el.story_chapter_id AS chapterId,",
            "  COALESCE(NULLIF(sc.title_zht, ''), sc.title_zh, sc.title_en, CONCAT('Chapter#', el.story_chapter_id)) AS chapterName,",
            "  el.poi_id AS poiId,",
            "  COALESCE(NULLIF(p.name_zht, ''), p.name_zh, p.name_en, CONCAT('POI#', el.poi_id)) AS poiName,",
            "  el.city_id AS cityId,",
            "  el.sub_map_id AS subMapId,",
            "  ugr.grant_status AS status,",
            "  gr.reward_type AS rewardType,",
            "  NULL AS rewardId,",
            "  ugr.game_reward_id AS gameRewardId,",
            "  'Game Reward Grant' AS title,",
            "  gr.reward_type AS summary,",
            "  CONCAT('遊戲內獎勵已獲得：', COALESCE(NULLIF(gr.name_zht, ''), gr.name_zh, gr.name_en, gr.code)) AS payloadPreview,",
            "  CONCAT('{\"gameRewardId\":', ugr.game_reward_id, ',\"status\":\"', ugr.grant_status, '\"}') AS rawPayload,",
            "  ugr.granted_at AS occurredAt",
            "FROM user_game_reward_grants ugr",
            "LEFT JOIN game_rewards gr ON gr.id = ugr.game_reward_id",
            "LEFT JOIN user_exploration_events e ON e.id = ugr.source_event_id",
            "LEFT JOIN exploration_elements el ON el.id = e.element_id",
            "LEFT JOIN story_chapters sc ON sc.id = el.story_chapter_id",
            "LEFT JOIN pois p ON p.id = el.poi_id",
            "LEFT JOIN storylines s ON s.id = el.storyline_id",
            "WHERE ugr.user_id = #{userId}",
            "ORDER BY ugr.granted_at DESC, ugr.id DESC"
    })
    List<TimelineSourceRow> selectGameRewardGrantTimelineRows(@Param("userId") Long userId);

    @Select({
            "SELECT",
            "  ugr.id AS grantId,",
            "  ugr.user_id AS userId,",
            "  ugr.game_reward_id AS gameRewardId,",
            "  ugr.rule_id AS sourceRuleId,",
            "  ugr.source_event_id AS sourceEventId,",
            "  ugr.source_session_id AS sourceSessionId,",
            "  ugr.grant_status AS grantStatus,",
            "  ugr.granted_at AS grantedAt,",
            "  ugr.idempotency_key AS idempotencyKey,",
            "  gr.code AS rewardCode,",
            "  gr.reward_type AS rewardType,",
            "  gr.rarity AS rarity,",
            "  COALESCE(NULLIF(gr.name_zht, ''), gr.name_zh, gr.name_en, gr.code) AS rewardName,",
            "  COALESCE(NULLIF(gr.description_zht, ''), gr.description_zh, gr.description_en, '') AS rewardDescription,",
            "  gr.icon_asset_id AS iconAssetId,",
            "  gr.cover_asset_id AS coverAssetId",
            "FROM user_game_reward_grants ugr",
            "LEFT JOIN game_rewards gr ON gr.id = ugr.game_reward_id",
            "WHERE ugr.user_id = #{userId}",
            "ORDER BY ugr.granted_at DESC, ugr.id DESC"
    })
    List<GameRewardGrantStateRow> selectGameRewardGrants(@Param("userId") Long userId);

    @Select({
            "SELECT",
            "  ugr.id AS grantId,",
            "  ugr.user_id AS userId,",
            "  ugr.game_reward_id AS gameRewardId,",
            "  ugr.rule_id AS sourceRuleId,",
            "  ugr.source_event_id AS sourceEventId,",
            "  ugr.grant_status AS grantStatus,",
            "  ugr.granted_at AS grantedAt",
            "FROM user_game_reward_grants ugr",
            "WHERE ugr.user_id = #{userId}",
            "  AND ugr.game_reward_id = #{gameRewardId}",
            "  AND (#{ruleId} IS NULL OR ugr.rule_id = #{ruleId})",
            "  AND (#{sourceEventId} IS NULL OR ugr.source_event_id = #{sourceEventId})",
            "ORDER BY ugr.granted_at DESC, ugr.id DESC",
            "LIMIT 1"
    })
    GameRewardGrantTraceRow selectGameRewardGrantTrace(
            @Param("userId") Long userId,
            @Param("gameRewardId") Long gameRewardId,
            @Param("ruleId") Long ruleId,
            @Param("sourceEventId") Long sourceEventId);

    @Select({
            "SELECT",
            "  rr.id AS redemptionId,",
            "  rr.user_id AS userId,",
            "  rr.reward_id AS rewardId,",
            "  COALESCE(NULLIF(r.name_zht, ''), r.name_zh, r.name_en, r.code) AS rewardName,",
            "  rr.redemption_status AS redemptionStatus,",
            "  rr.stamp_cost_snapshot AS stampCostSnapshot,",
            "  rr.redeemed_at AS redeemedAt,",
            "  rr.expires_at AS expiresAt",
            "FROM reward_redemptions rr",
            "LEFT JOIN rewards r ON r.id = rr.reward_id",
            "WHERE rr.user_id = #{userId}",
            "  AND rr.deleted = 0",
            "ORDER BY COALESCE(rr.redeemed_at, rr.created_at) DESC, rr.id DESC"
    })
    List<RewardRedemptionRow> selectAllRewardRedemptions(@Param("userId") Long userId);

    @Select({
            "SELECT",
            "  e.id AS eventId,",
            "  e.user_id AS userId,",
            "  e.element_id AS elementId,",
            "  e.element_code AS elementCode,",
            "  e.event_type AS eventType,",
            "  e.event_source AS eventSource,",
            "  e.storyline_session_id AS storylineSessionId,",
            "  LEFT(CAST(e.event_payload_json AS CHAR), 160) AS payloadPreview,",
            "  CAST(e.event_payload_json AS CHAR) AS rawPayload,",
            "  e.occurred_at AS occurredAt,",
            "  el.element_type AS elementType,",
            "  el.owner_type AS ownerType,",
            "  el.owner_id AS ownerId,",
            "  el.owner_code AS ownerCode,",
            "  el.city_id AS cityId,",
            "  el.sub_map_id AS subMapId,",
            "  el.poi_id AS poiId,",
            "  el.storyline_id AS storylineId,",
            "  el.story_chapter_id AS chapterId,",
            "  COALESCE(NULLIF(el.title_zht, ''), el.title_zh, el.title_en, el.element_code) AS elementTitle,",
            "  el.status AS elementStatus",
            "FROM user_exploration_events e",
            "LEFT JOIN exploration_elements el ON el.id = e.element_id",
            "WHERE e.user_id = #{userId}",
            "  AND e.id = #{sourceEventId}",
            "LIMIT 1"
    })
    TraceEventElementRow selectTraceEventElement(
            @Param("userId") Long userId,
            @Param("sourceEventId") Long sourceEventId);

    @Select({
            "SELECT",
            "  rb.id AS bindingId,",
            "  rb.rule_id AS ruleId,",
            "  rb.owner_domain AS ownerDomain,",
            "  rb.owner_id AS ownerId,",
            "  rb.owner_code AS ownerCode,",
            "  rb.binding_role AS bindingRole,",
            "  rr.code AS ruleCode,",
            "  rr.rule_type AS ruleType,",
            "  rr.status AS ruleStatus,",
            "  COALESCE(NULLIF(rr.name_zht, ''), rr.name_zh, rr.code) AS ruleName,",
            "  rr.summary_text AS summaryText,",
            "  rr.root_condition_group_id AS rootConditionGroupId",
            "FROM reward_rule_bindings rb",
            "JOIN reward_rules rr ON rr.id = rb.rule_id AND rr.deleted = 0",
            "WHERE rb.deleted = 0",
            "  AND (#{ruleId} IS NULL OR rb.rule_id = #{ruleId})",
            "  AND (",
            "    (#{gameRewardId} IS NOT NULL AND rb.owner_domain = 'game_reward' AND rb.owner_id = #{gameRewardId})",
            "    OR (#{rewardId} IS NOT NULL AND rb.owner_domain IN ('redeemable_prize', 'reward') AND rb.owner_id = #{rewardId})",
            "    OR (#{elementId} IS NOT NULL AND rb.owner_domain IN ('exploration_element', 'element') AND rb.owner_id = #{elementId})",
            "    OR (#{ownerType} <> '' AND rb.owner_domain = #{ownerType} AND rb.owner_id = #{ownerId})",
            "    OR (#{storylineId} IS NOT NULL AND rb.owner_domain = 'storyline' AND rb.owner_id = #{storylineId})",
            "    OR (#{chapterId} IS NOT NULL AND rb.owner_domain IN ('story_chapter', 'chapter') AND rb.owner_id = #{chapterId})",
            "    OR (#{poiId} IS NOT NULL AND rb.owner_domain = 'poi' AND rb.owner_id = #{poiId})",
            "  )",
            "ORDER BY rb.sort_order ASC, rb.id ASC"
    })
    List<RuleBindingTraceRow> selectTraceRuleBindings(
            @Param("ruleId") Long ruleId,
            @Param("rewardId") Long rewardId,
            @Param("gameRewardId") Long gameRewardId,
            @Param("elementId") Long elementId,
            @Param("ownerType") String ownerType,
            @Param("ownerId") Long ownerId,
            @Param("storylineId") Long storylineId,
            @Param("chapterId") Long chapterId,
            @Param("poiId") Long poiId);

    @Select({
            "SELECT",
            "  g.id AS groupId,",
            "  g.rule_id AS ruleId,",
            "  g.parent_group_id AS parentGroupId,",
            "  g.group_code AS groupCode,",
            "  g.operator_type AS operatorType,",
            "  g.minimum_match_count AS minimumMatchCount,",
            "  g.summary_text AS summaryText",
            "FROM reward_condition_groups g",
            "WHERE g.rule_id = #{ruleId}",
            "  AND g.deleted = 0",
            "ORDER BY g.parent_group_id ASC, g.sort_order ASC, g.id ASC"
    })
    List<ConditionGroupTraceRow> selectConditionGroups(@Param("ruleId") Long ruleId);

    @Select({
            "SELECT",
            "  c.id AS conditionId,",
            "  c.group_id AS groupId,",
            "  c.condition_type AS conditionType,",
            "  c.metric_type AS metricType,",
            "  c.operator_type AS operatorType,",
            "  c.comparator_value AS comparatorValue,",
            "  c.comparator_unit AS comparatorUnit,",
            "  c.summary_text AS summaryText",
            "FROM reward_conditions c",
            "WHERE c.group_id IN (",
            "  SELECT g.id FROM reward_condition_groups g WHERE g.rule_id = #{ruleId} AND g.deleted = 0",
            ")",
            "  AND c.deleted = 0",
            "ORDER BY c.group_id ASC, c.sort_order ASC, c.id ASC"
    })
    List<ConditionTraceRow> selectConditions(@Param("ruleId") Long ruleId);

    @Select({
            "SELECT COUNT(*)",
            "FROM reward_redemptions rr",
            "WHERE rr.user_id = #{userId}",
            "  AND rr.deleted = 0",
            "  AND (#{rewardId} IS NULL OR rr.reward_id = #{rewardId})"
    })
    int countRewardRedemptions(
            @Param("userId") Long userId,
            @Param("rewardId") Long rewardId);

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    class UserPreferenceRow {
        private Long userId;
        private String interfaceMode;
        private BigDecimal fontScale;
        private Boolean highContrast;
        private Boolean voiceGuideEnabled;
        private Boolean seniorMode;
        private String localeCode;
        private String emergencyContactName;
        private String emergencyContactPhone;
        private String runtimeOverridesJson;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    class LinkedScopeRow {
        private String scopeType;
        private Long scopeId;
        private String scopeName;
        private String relationLabel;
        private String source;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    class LegacyProgressRow {
        private Long userId;
        private Long scopeId;
        private String scopeType;
        private String scopeName;
        private Integer progressPercent;
        private Long activeStorylineId;
        private Boolean completedStoryline;
        private LocalDateTime lastSeenAt;
        private LocalDateTime updatedAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    class StorylineSessionRow {
        private String sessionId;
        private Long userId;
        private Long storylineId;
        private String storylineName;
        private Long currentChapterId;
        private String status;
        private LocalDateTime startedAt;
        private LocalDateTime lastEventAt;
        private LocalDateTime exitedAt;
        private Integer eventCount;
        private Boolean exitClearedTemporaryState;
        private String temporaryStepStateJson;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    class RewardRedemptionRow {
        private Long redemptionId;
        private Long userId;
        private Long rewardId;
        private String rewardName;
        private String redemptionStatus;
        private Integer stampCostSnapshot;
        private LocalDateTime redeemedAt;
        private LocalDateTime expiresAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    class RecentContextCountsRow {
        private Integer recentCheckinCount;
        private Integer recentExplorationEventCount;
        private Integer recentTriggerCount;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    class TimelineSourceRow {
        private String entryId;
        private String entryType;
        private String sourceTable;
        private Long sourceRecordId;
        private Long userId;
        private Long storylineId;
        private String storylineName;
        private Long chapterId;
        private String chapterName;
        private Long poiId;
        private String poiName;
        private Long cityId;
        private Long subMapId;
        private String status;
        private String rewardType;
        private Long rewardId;
        private Long gameRewardId;
        private String title;
        private String summary;
        private String payloadPreview;
        private String rawPayload;
        private LocalDateTime occurredAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    class TraceEventElementRow {
        private Long eventId;
        private Long userId;
        private Long elementId;
        private String elementCode;
        private String eventType;
        private String eventSource;
        private String storylineSessionId;
        private String payloadPreview;
        private String rawPayload;
        private LocalDateTime occurredAt;
        private String elementType;
        private String ownerType;
        private Long ownerId;
        private String ownerCode;
        private Long cityId;
        private Long subMapId;
        private Long poiId;
        private Long storylineId;
        private Long chapterId;
        private String elementTitle;
        private String elementStatus;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    class RuleBindingTraceRow {
        private Long bindingId;
        private Long ruleId;
        private String ownerDomain;
        private Long ownerId;
        private String ownerCode;
        private String bindingRole;
        private String ruleCode;
        private String ruleType;
        private String ruleStatus;
        private String ruleName;
        private String summaryText;
        private Long rootConditionGroupId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    class ConditionGroupTraceRow {
        private Long groupId;
        private Long ruleId;
        private Long parentGroupId;
        private String groupCode;
        private String operatorType;
        private Integer minimumMatchCount;
        private String summaryText;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    class ConditionTraceRow {
        private Long conditionId;
        private Long groupId;
        private String conditionType;
        private String metricType;
        private String operatorType;
        private String comparatorValue;
        private String comparatorUnit;
        private String summaryText;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    class GameRewardGrantStateRow {
        private Long grantId;
        private Long userId;
        private Long gameRewardId;
        private Long sourceRuleId;
        private Long sourceEventId;
        private Long sourceSessionId;
        private String grantStatus;
        private LocalDateTime grantedAt;
        private String idempotencyKey;
        private String rewardCode;
        private String rewardType;
        private String rarity;
        private String rewardName;
        private String rewardDescription;
        private Long iconAssetId;
        private Long coverAssetId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    class GameRewardGrantTraceRow {
        private Long grantId;
        private Long userId;
        private Long gameRewardId;
        private Long sourceRuleId;
        private Long sourceEventId;
        private String grantStatus;
        private LocalDateTime grantedAt;
    }
}
