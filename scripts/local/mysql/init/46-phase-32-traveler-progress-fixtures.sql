-- Phase 32 Plan 32-04: traveler progress fixtures and compatibility data.
-- Implements D32-33 and D32-34 on top of the canonical progress/session/audit schema.
-- All text is UTF-8 / utf8mb4. Do not rewrite this file through non-UTF-8 shell literals.

USE `aoxiaoyou`;

SET NAMES utf8mb4;

SET @phase32_user_id := 320041;
SET @phase32_test_user_id := 320042;
SET @phase32_city_id := (SELECT `id` FROM `cities` WHERE `code` = 'macau' LIMIT 1);
SET @phase32_sub_map_id := (SELECT `id` FROM `sub_maps` WHERE `code` = 'macau-peninsula' LIMIT 1);
SET @phase32_storyline_id := (SELECT `id` FROM `storylines` WHERE `code` = 'macau_fire_route' LIMIT 1);
SET @phase32_poi_id := (SELECT `id` FROM `pois` WHERE `code` = 'ama_temple' LIMIT 1);
SET @phase32_reward_archive_id := (SELECT `id` FROM `rewards` WHERE `code` = 'reward_historic_archive' LIMIT 1);
SET @phase32_reward_voice_pack_id := (SELECT `id` FROM `rewards` WHERE `code` = 'reward_war_voice_pack' LIMIT 1);
SET @phase32_chapter_id := (
  SELECT `id`
  FROM `story_chapters`
  WHERE `storyline_id` = @phase32_storyline_id
    AND `chapter_order` = 1
  LIMIT 1
);
SET @phase32_next_chapter_id := (
  SELECT `id`
  FROM `story_chapters`
  WHERE `storyline_id` = @phase32_storyline_id
    AND `chapter_order` = 2
  LIMIT 1
);

UPDATE `exploration_elements`
SET `poi_id` = @phase32_poi_id
WHERE `element_code` IN (
  'ama_poi_arrival',
  'ama_story_ch1_complete',
  'ama_poi_arrival_intro',
  'ama_checkin_tasks_released',
  'ama_side_clues_collected',
  'ama_hidden_dwell_achievement',
  'ama_completion_reward_title',
  'story_ch01_arrival_immersive_media',
  'story_ch01_mainline_overlays',
  'story_ch01_side_pickups',
  'story_ch01_hidden_challenge',
  'story_ch01_reward_titles'
);

INSERT INTO `exploration_elements` (
  `id`,
  `element_code`,
  `element_type`,
  `owner_type`,
  `owner_id`,
  `owner_code`,
  `city_id`,
  `sub_map_id`,
  `poi_id`,
  `storyline_id`,
  `story_chapter_id`,
  `title_zh`,
  `title_en`,
  `title_zht`,
  `title_pt`,
  `weight_level`,
  `weight_value`,
  `include_in_exploration`,
  `metadata_json`,
  `status`,
  `sort_order`,
  `deleted`
) VALUES (
  3200461,
  'phase32_retired_guardian_note',
  'story_chapter_retired_memory',
  'story_chapter',
  @phase32_chapter_id,
  'macau_fire_route_chapter_1',
  @phase32_city_id,
  @phase32_sub_map_id,
  @phase32_poi_id,
  @phase32_storyline_id,
  @phase32_chapter_id,
  '舊版鏡海守望備忘碎片',
  'Retired Mirror Sea watcher note',
  '舊版鏡海守望備忘碎片',
  'Fragmento arquivado de vigia do Mar Espelho',
  'medium',
  3,
  0,
  JSON_OBJECT(
    'schemaVersion', 1,
    'source', 'phase32-fixture',
    'reason', 'retired-comparison-demo'
  ),
  'archived',
  990,
  0
) ON DUPLICATE KEY UPDATE
  `owner_id` = VALUES(`owner_id`),
  `city_id` = VALUES(`city_id`),
  `sub_map_id` = VALUES(`sub_map_id`),
  `poi_id` = VALUES(`poi_id`),
  `storyline_id` = VALUES(`storyline_id`),
  `story_chapter_id` = VALUES(`story_chapter_id`),
  `title_zh` = VALUES(`title_zh`),
  `title_en` = VALUES(`title_en`),
  `title_zht` = VALUES(`title_zht`),
  `title_pt` = VALUES(`title_pt`),
  `weight_level` = VALUES(`weight_level`),
  `weight_value` = VALUES(`weight_value`),
  `include_in_exploration` = VALUES(`include_in_exploration`),
  `metadata_json` = VALUES(`metadata_json`),
  `status` = VALUES(`status`),
  `sort_order` = VALUES(`sort_order`),
  `deleted` = VALUES(`deleted`);

INSERT INTO `user_profiles` (
  `id`,
  `open_id`,
  `nickname`,
  `avatar_url`,
  `level`,
  `title_zh`,
  `title_en`,
  `title_zht`,
  `title_pt`,
  `total_stamps`,
  `current_exp`,
  `next_level_exp`,
  `current_city_id`,
  `current_locale_code`,
  `deleted`
) VALUES
  (
    @phase32_user_id,
    'dev-bypass:phase32-progress-traveler',
    '第 32 階段旅客',
    'https://example.test/assets/phase32-traveler.png',
    6,
    '鏡海路線觀察員',
    'Mirror Sea Route Observer',
    '鏡海路線觀察員',
    'Observador da Rota do Mar Espelho',
    18,
    360,
    520,
    @phase32_city_id,
    'zh-Hant',
    0
  ),
  (
    @phase32_test_user_id,
    'phase32-test-traveler',
    '第 32 階段測試旅客',
    'https://example.test/assets/phase32-test-traveler.png',
    2,
    '測試旅客',
    'Test traveler',
    '測試旅客',
    'Viajante de teste',
    4,
    80,
    120,
    @phase32_city_id,
    'zh-Hant',
    0
  )
ON DUPLICATE KEY UPDATE
  `open_id` = VALUES(`open_id`),
  `nickname` = VALUES(`nickname`),
  `avatar_url` = VALUES(`avatar_url`),
  `level` = VALUES(`level`),
  `title_zh` = VALUES(`title_zh`),
  `title_en` = VALUES(`title_en`),
  `title_zht` = VALUES(`title_zht`),
  `title_pt` = VALUES(`title_pt`),
  `total_stamps` = VALUES(`total_stamps`),
  `current_exp` = VALUES(`current_exp`),
  `next_level_exp` = VALUES(`next_level_exp`),
  `current_city_id` = VALUES(`current_city_id`),
  `current_locale_code` = VALUES(`current_locale_code`),
  `deleted` = VALUES(`deleted`);

INSERT INTO `user_preferences` (
  `id`,
  `user_id`,
  `interface_mode`,
  `font_scale`,
  `high_contrast`,
  `voice_guide_enabled`,
  `senior_mode`,
  `locale_code`,
  `emergency_contact_name`,
  `emergency_contact_phone`,
  `runtime_overrides_json`,
  `deleted`
) VALUES
  (
    3200411,
    @phase32_user_id,
    'story',
    1.2,
    1,
    1,
    0,
    'zh-Hant',
    '林小雯',
    '853-6200-3204',
    JSON_OBJECT(
      'timelineDensity', 'comfortable',
      'payloadPreview', 'expanded',
      'phase32Fixture', TRUE
    ),
    0
  ),
  (
    3200421,
    @phase32_test_user_id,
    'standard',
    1.0,
    0,
    0,
    0,
    'zh-Hant',
    '值班測試員',
    '853-6200-3205',
    JSON_OBJECT('phase32Fixture', TRUE),
    0
  )
ON DUPLICATE KEY UPDATE
  `interface_mode` = VALUES(`interface_mode`),
  `font_scale` = VALUES(`font_scale`),
  `high_contrast` = VALUES(`high_contrast`),
  `voice_guide_enabled` = VALUES(`voice_guide_enabled`),
  `senior_mode` = VALUES(`senior_mode`),
  `locale_code` = VALUES(`locale_code`),
  `emergency_contact_name` = VALUES(`emergency_contact_name`),
  `emergency_contact_phone` = VALUES(`emergency_contact_phone`),
  `runtime_overrides_json` = VALUES(`runtime_overrides_json`),
  `deleted` = VALUES(`deleted`);

INSERT INTO `test_accounts` (
  `id`,
  `user_id`,
  `test_group`,
  `mock_latitude`,
  `mock_longitude`,
  `mock_enabled`,
  `mock_poi_id`,
  `notes`,
  `deleted`,
  `_openid`
) VALUES (
  3200420,
  @phase32_test_user_id,
  'phase32-progress',
  22.18756000,
  113.53998000,
  1,
  @phase32_poi_id,
  'Phase 32 旅客進度工作台測試帳號',
  0,
  'phase32-test-traveler'
) ON DUPLICATE KEY UPDATE
  `test_group` = VALUES(`test_group`),
  `mock_latitude` = VALUES(`mock_latitude`),
  `mock_longitude` = VALUES(`mock_longitude`),
  `mock_enabled` = VALUES(`mock_enabled`),
  `mock_poi_id` = VALUES(`mock_poi_id`),
  `notes` = VALUES(`notes`),
  `deleted` = VALUES(`deleted`),
  `_openid` = VALUES(`_openid`);

INSERT INTO `user_progress` (
  `id`,
  `user_id`,
  `storyline_id`,
  `active_storyline_id`,
  `completed_storyline`,
  `completed_chapter_ids_json`,
  `collected_stamp_ids_json`,
  `progress_percent`,
  `last_seen_at`,
  `completed_at`,
  `deleted`
) VALUES
  (
    3200410,
    @phase32_user_id,
    NULL,
    @phase32_storyline_id,
    0,
    JSON_ARRAY(@phase32_chapter_id),
    JSON_ARRAY(8801, 8802, 8803),
    35,
    '2026-04-29 09:20:00',
    NULL,
    0
  ),
  (
    3200412,
    @phase32_user_id,
    @phase32_storyline_id,
    @phase32_storyline_id,
    0,
    JSON_ARRAY(@phase32_chapter_id),
    JSON_ARRAY(8801, 8802, 8803),
    40,
    '2026-04-29 09:20:00',
    NULL,
    0
  ),
  (
    3200422,
    @phase32_test_user_id,
    NULL,
    NULL,
    0,
    JSON_ARRAY(),
    JSON_ARRAY(9901),
    12,
    '2026-04-29 09:00:00',
    NULL,
    0
  )
ON DUPLICATE KEY UPDATE
  `storyline_id` = VALUES(`storyline_id`),
  `active_storyline_id` = VALUES(`active_storyline_id`),
  `completed_storyline` = VALUES(`completed_storyline`),
  `completed_chapter_ids_json` = VALUES(`completed_chapter_ids_json`),
  `collected_stamp_ids_json` = VALUES(`collected_stamp_ids_json`),
  `progress_percent` = VALUES(`progress_percent`),
  `last_seen_at` = VALUES(`last_seen_at`),
  `completed_at` = VALUES(`completed_at`),
  `deleted` = VALUES(`deleted`);

INSERT INTO `user_exploration_state` (
  `id`,
  `user_id`,
  `scope_type`,
  `scope_id`,
  `completed_weight`,
  `available_weight`,
  `progress_percent`,
  `computed_at`
) VALUES
  (32004101, @phase32_user_id, 'global', NULL, 72, 93, 77.42, '2026-04-29 09:25:00'),
  (32004102, @phase32_user_id, 'city', @phase32_city_id, 72, 93, 77.42, '2026-04-29 09:25:00'),
  (32004103, @phase32_user_id, 'sub_map', @phase32_sub_map_id, 72, 93, 77.42, '2026-04-29 09:25:00'),
  (32004104, @phase32_user_id, 'poi', @phase32_poi_id, 72, 72, 100.00, '2026-04-29 09:25:00'),
  (32004105, @phase32_user_id, 'storyline', @phase32_storyline_id, 45, 45, 100.00, '2026-04-29 09:25:00'),
  (32004106, @phase32_user_id, 'story_chapter', @phase32_chapter_id, 45, 45, 100.00, '2026-04-29 09:25:00')
ON DUPLICATE KEY UPDATE
  `completed_weight` = VALUES(`completed_weight`),
  `available_weight` = VALUES(`available_weight`),
  `progress_percent` = VALUES(`progress_percent`),
  `computed_at` = VALUES(`computed_at`);

DELETE FROM `user_checkins` WHERE `id` IN (32004101, 32004102, 32004103);
INSERT INTO `user_checkins` (
  `id`,
  `user_id`,
  `poi_id`,
  `trigger_mode`,
  `distance_meters`,
  `gps_accuracy`,
  `latitude`,
  `longitude`,
  `checked_at`
) VALUES
  (32004101, @phase32_user_id, @phase32_poi_id, 'gps', 18.50, 6.30, 22.1869000, 113.5312000, '2026-04-29 08:45:00'),
  (32004102, @phase32_user_id, 10, 'manual', 42.00, 11.20, 22.1923000, 113.5397000, '2026-04-29 09:05:00'),
  (32004103, @phase32_user_id, 12, 'gps', 25.10, 7.00, 22.1970000, 113.5409000, '2026-04-29 09:40:00');

DELETE FROM `trigger_logs` WHERE `id` IN (32004101, 32004102, 32004103);
INSERT INTO `trigger_logs` (
  `id`,
  `user_id`,
  `poi_id`,
  `trigger_type`,
  `distance`,
  `gps_accuracy`,
  `wifi_used`,
  `created_at`,
  `_openid`
) VALUES
  (32004101, @phase32_user_id, @phase32_poi_id, 'auto', 18.50, 6.30, 0, '2026-04-29 08:44:30', 'dev-bypass:phase32-progress-traveler'),
  (32004102, @phase32_user_id, 10, 'manual', 42.00, 11.20, 1, '2026-04-29 09:04:10', 'dev-bypass:phase32-progress-traveler'),
  (32004103, @phase32_user_id, 12, 'auto', 25.10, 7.00, 0, '2026-04-29 09:39:15', 'dev-bypass:phase32-progress-traveler');

DELETE FROM `reward_redemptions` WHERE `id` IN (32004101, 32004102);
INSERT INTO `reward_redemptions` (
  `id`,
  `user_id`,
  `reward_id`,
  `redemption_status`,
  `stamp_cost_snapshot`,
  `qr_code`,
  `redeemed_at`,
  `expires_at`,
  `deleted`
) VALUES
  (
    32004101,
    @phase32_user_id,
    @phase32_reward_archive_id,
    'redeemed',
    88,
    'PHASE32-ARCHIVE-320041',
    '2026-04-29 08:20:00',
    '2026-05-29 08:20:00',
    0
  ),
  (
    32004102,
    @phase32_user_id,
    @phase32_reward_voice_pack_id,
    'created',
    56,
    'PHASE32-VOICE-320041',
    '2026-04-29 09:55:00',
    '2026-05-14 09:55:00',
    0
  );

DELETE FROM `user_storyline_sessions`
WHERE `session_id` IN ('phase32-session-active', 'phase32-session-exited');
INSERT INTO `user_storyline_sessions` (
  `session_id`,
  `user_id`,
  `storyline_id`,
  `current_chapter_id`,
  `status`,
  `started_at`,
  `last_event_at`,
  `exited_at`,
  `event_count`,
  `temporary_step_state_json`,
  `exit_cleared_temporary_state`
) VALUES
  (
    'phase32-session-active',
    @phase32_user_id,
    @phase32_storyline_id,
    @phase32_next_chapter_id,
    'started',
    '2026-04-29 09:30:00',
    '2026-04-29 10:05:00',
    NULL,
    4,
    JSON_OBJECT(
      'currentStepCode', 'story_ch02_route_shift',
      'temporaryCollectibles', JSON_ARRAY('harbor_signal_flag'),
      'storyMode', TRUE
    ),
    0
  ),
  (
    'phase32-session-exited',
    @phase32_user_id,
    @phase32_storyline_id,
    @phase32_chapter_id,
    'exited',
    '2026-04-29 07:40:00',
    '2026-04-29 08:35:00',
    '2026-04-29 08:36:00',
    7,
    JSON_OBJECT(
      'currentStepCode', 'story_ch01_reward_titles',
      'storyMode', TRUE,
      'notes', 'chapter-one-finished'
    ),
    1
  );

DELETE FROM `user_exploration_events`
WHERE `id` BETWEEN 32004101 AND 32004113
   OR (`user_id` = @phase32_user_id AND `client_event_id` LIKE 'phase32-%');
INSERT INTO `user_exploration_events` (
  `id`,
  `user_id`,
  `element_id`,
  `element_code`,
  `event_type`,
  `event_source`,
  `storyline_session_id`,
  `client_event_id`,
  `event_payload_json`,
  `occurred_at`,
  `duplicate_marked`,
  `duplicate_of_event_id`,
  `repair_note_json`
) VALUES
  (
    32004101,
    @phase32_user_id,
    (SELECT `id` FROM `exploration_elements` WHERE `element_code` = 'ama_poi_arrival' LIMIT 1),
    'ama_poi_arrival',
    'poi_arrival',
    'phase32_fixture',
    '',
    'phase32-ama-poi-arrival',
    JSON_OBJECT('poiCode', 'ama_temple', 'source', 'phase32-fixture'),
    '2026-04-29 08:42:00',
    0,
    NULL,
    NULL
  ),
  (
    32004102,
    @phase32_user_id,
    (SELECT `id` FROM `exploration_elements` WHERE `element_code` = 'ama_poi_arrival_intro' LIMIT 1),
    'ama_poi_arrival_intro',
    'poi_proximity_media_complete',
    'phase32_fixture',
    '',
    'phase32-ama-poi-arrival-intro',
    JSON_OBJECT('stepCode', 'arrival_intro_media', 'source', 'phase32-fixture'),
    '2026-04-29 08:43:00',
    0,
    NULL,
    NULL
  ),
  (
    32004103,
    @phase32_user_id,
    (SELECT `id` FROM `exploration_elements` WHERE `element_code` = 'ama_checkin_tasks_released' LIMIT 1),
    'ama_checkin_tasks_released',
    'task_bundle_released',
    'phase32_fixture',
    '',
    'phase32-ama-checkin-tasks',
    JSON_OBJECT('taskCodes', JSON_ARRAY('ama_gate_photo', 'ama_cyber_incense')),
    '2026-04-29 08:46:00',
    0,
    NULL,
    NULL
  ),
  (
    32004104,
    @phase32_user_id,
    (SELECT `id` FROM `exploration_elements` WHERE `element_code` = 'ama_side_clues_collected' LIMIT 1),
    'ama_side_clues_collected',
    'collectible_bundle_complete',
    'phase32_fixture',
    '',
    'phase32-ama-side-clues',
    JSON_OBJECT('pickupCodes', JSON_ARRAY('ming_coastal_token', 'fisher_net_fragment', 'tax_contract_page')),
    '2026-04-29 08:52:00',
    0,
    NULL,
    NULL
  ),
  (
    32004105,
    @phase32_user_id,
    (SELECT `id` FROM `exploration_elements` WHERE `element_code` = 'ama_hidden_dwell_achievement' LIMIT 1),
    'ama_hidden_dwell_achievement',
    'hidden_achievement_complete',
    'phase32_fixture',
    '',
    'phase32-ama-hidden-dwell',
    JSON_OBJECT('dwellSeconds', 1980, 'radiusMeters', 30),
    '2026-04-29 08:58:00',
    0,
    NULL,
    NULL
  ),
  (
    32004106,
    @phase32_user_id,
    (SELECT `id` FROM `exploration_elements` WHERE `element_code` = 'ama_completion_reward_title' LIMIT 1),
    'ama_completion_reward_title',
    'poi_completion_reward',
    'phase32_fixture',
    '',
    'phase32-ama-completion-reward',
    JSON_OBJECT('rewardSummary', '完成媽閣廟地點體驗'),
    '2026-04-29 09:00:00',
    0,
    NULL,
    NULL
  ),
  (
    32004107,
    @phase32_user_id,
    (SELECT `id` FROM `exploration_elements` WHERE `element_code` = 'story_ch01_arrival_immersive_media' LIMIT 1),
    'story_ch01_arrival_immersive_media',
    'storyline_media_complete',
    'phase32_fixture',
    'phase32-session-exited',
    'phase32-story-ch01-arrival-media',
    JSON_OBJECT('storylineCode', 'macau_fire_route', 'stepCode', 'story_ch01_arrival_immersive_media'),
    '2026-04-29 08:05:00',
    0,
    NULL,
    NULL
  ),
  (
    32004108,
    @phase32_user_id,
    (SELECT `id` FROM `exploration_elements` WHERE `element_code` = 'story_ch01_mainline_overlays' LIMIT 1),
    'story_ch01_mainline_overlays',
    'storyline_overlay_complete',
    'phase32_fixture',
    'phase32-session-exited',
    'phase32-story-ch01-mainline-overlays',
    JSON_OBJECT('overlayCodes', JSON_ARRAY('ming_warship', 'fisher_defense', 'portuguese_ship')),
    '2026-04-29 08:12:00',
    0,
    NULL,
    NULL
  ),
  (
    32004109,
    @phase32_user_id,
    (SELECT `id` FROM `exploration_elements` WHERE `element_code` = 'story_ch01_side_pickups' LIMIT 1),
    'story_ch01_side_pickups',
    'storyline_pickup_complete',
    'phase32_fixture',
    'phase32-session-exited',
    'phase32-story-ch01-side-pickups',
    JSON_OBJECT('pickupCount', 3),
    '2026-04-29 08:18:00',
    0,
    NULL,
    NULL
  ),
  (
    32004110,
    @phase32_user_id,
    (SELECT `id` FROM `exploration_elements` WHERE `element_code` = 'story_ch01_hidden_challenge' LIMIT 1),
    'story_ch01_hidden_challenge',
    'storyline_hidden_challenge_complete',
    'phase32_fixture',
    'phase32-session-exited',
    'phase32-story-ch01-hidden-challenge',
    JSON_OBJECT('quizScore', 3, 'dwellSeconds', 360),
    '2026-04-29 08:25:00',
    0,
    NULL,
    NULL
  ),
  (
    32004111,
    @phase32_user_id,
    (SELECT `id` FROM `exploration_elements` WHERE `element_code` = 'story_ch01_reward_titles' LIMIT 1),
    'story_ch01_reward_titles',
    'storyline_reward_complete',
    'phase32_fixture',
    'phase32-session-exited',
    'phase32-story-ch01-reward-titles',
    JSON_OBJECT('rewardBadges', JSON_ARRAY('初見濠江勳章', '戰火見證者')),
    '2026-04-29 08:30:00',
    0,
    NULL,
    NULL
  ),
  (
    32004112,
    @phase32_user_id,
    (SELECT `id` FROM `exploration_elements` WHERE `element_code` = 'ama_story_ch1_complete' LIMIT 1),
    'ama_story_ch1_complete',
    'story_chapter_complete',
    'phase32_fixture',
    'phase32-session-exited',
    'phase32-story-ch01-complete',
    JSON_OBJECT('chapterOrder', 1, 'anchorPoiCode', 'ama_temple'),
    '2026-04-29 08:35:00',
    0,
    NULL,
    NULL
  ),
  (
    32004113,
    @phase32_user_id,
    3200461,
    'phase32_retired_guardian_note',
    'story_retired_memory_complete',
    'phase32_fixture',
    'phase32-session-exited',
    'phase32-retired-guardian-note',
    JSON_OBJECT('retiredBecause', 'content_refresh', 'visibleForComparisonOnly', TRUE),
    '2026-04-20 18:10:00',
    0,
    NULL,
    JSON_OBJECT('source', 'phase32-fixture', 'note', 'retired-comparison-demo')
  );

DELETE FROM `user_progress_operation_audits` WHERE `id` = 32004101;
INSERT INTO `user_progress_operation_audits` (
  `id`,
  `operator_id`,
  `operator_name`,
  `target_user_id`,
  `scope_type`,
  `scope_id`,
  `storyline_id`,
  `action_type`,
  `preview_token_hash`,
  `preview_summary_json`,
  `result_summary_json`,
  `reason`,
  `request_ip`,
  `created_at`
) VALUES (
  32004101,
  1,
  '超級管理員',
  @phase32_user_id,
  'poi',
  @phase32_poi_id,
  @phase32_storyline_id,
  'LINK_ORPHAN_EVENT',
  'phase32-fixture-preview-token',
  JSON_OBJECT(
    'matchingEventCount', 1,
    'targetElementCode', 'phase32_retired_guardian_note',
    'scopeLabel', '媽閣廟'
  ),
  JSON_OBJECT(
    'mutatedEventRows', 1,
    'writtenStateRows', 2,
    'note', '預置修復審計紀錄'
  ),
  'Phase 32 預置修復示例',
  '127.0.0.1',
  '2026-04-29 09:10:00'
) ON DUPLICATE KEY UPDATE
  `operator_id` = VALUES(`operator_id`),
  `operator_name` = VALUES(`operator_name`),
  `target_user_id` = VALUES(`target_user_id`),
  `scope_type` = VALUES(`scope_type`),
  `scope_id` = VALUES(`scope_id`),
  `storyline_id` = VALUES(`storyline_id`),
  `action_type` = VALUES(`action_type`),
  `preview_token_hash` = VALUES(`preview_token_hash`),
  `preview_summary_json` = VALUES(`preview_summary_json`),
  `result_summary_json` = VALUES(`result_summary_json`),
  `reason` = VALUES(`reason`),
  `request_ip` = VALUES(`request_ip`),
  `created_at` = VALUES(`created_at`);
