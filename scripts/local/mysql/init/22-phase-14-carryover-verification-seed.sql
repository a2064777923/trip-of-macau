USE `aoxiaoyou`;

SET NAMES utf8mb4;

SET @fixture_open_id = CONVERT('phase14_progress_user' USING utf8mb4) COLLATE utf8mb4_unicode_ci;
SET @fixture_nickname = 'Phase 14 Carryover Explorer';
SET @city_id = (SELECT `id` FROM `cities` WHERE `code` = 'macau' LIMIT 1);
SET @storyline_id = (SELECT `id` FROM `storylines` WHERE `code` = 'macau_fire_route' LIMIT 1);
SET @poi_a = (SELECT `id` FROM `pois` WHERE `code` = 'ama_temple' LIMIT 1);
SET @poi_b = (SELECT `id` FROM `pois` WHERE `code` = 'lilau_square' LIMIT 1);
SET @poi_c = (SELECT `id` FROM `pois` WHERE `code` = 'monte_fort' LIMIT 1);

INSERT INTO `user_profiles` (
  `open_id`, `nickname`, `avatar_url`, `level`,
  `title_zh`, `title_en`, `title_zht`, `title_pt`,
  `total_stamps`, `current_exp`, `next_level_exp`, `current_city_id`, `current_locale_code`, `deleted`
)
VALUES (
  @fixture_open_id,
  @fixture_nickname,
  'https://tripofmacau-1301163924.cos.ap-hongkong.myqcloud.com/phase14/carryover-user.png',
  4,
  '濠江探索者',
  'Macau Explorer',
  '濠江探索者',
  'Explorador de Macau',
  4,
  180,
  300,
  @city_id,
  'zh-Hant',
  0
)
ON DUPLICATE KEY UPDATE
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
  `deleted` = 0;

SET @user_id = (
  SELECT `id`
  FROM `user_profiles`
  WHERE `open_id` COLLATE utf8mb4_unicode_ci = @fixture_open_id
  LIMIT 1
);

DELETE FROM `user_progress` WHERE `user_id` = @user_id;
DELETE FROM `user_checkins` WHERE `user_id` = @user_id;
DELETE FROM `trigger_logs` WHERE `user_id` = @user_id;

INSERT INTO `user_progress` (
  `user_id`, `storyline_id`, `active_storyline_id`, `completed_storyline`,
  `completed_chapter_ids_json`, `collected_stamp_ids_json`, `progress_percent`,
  `last_seen_at`, `completed_at`, `deleted`
)
VALUES
  (
    @user_id, NULL, @storyline_id, 0,
    JSON_ARRAY(1011, 1012, 1021),
    JSON_ARRAY(101, 102, 103, 104),
    58,
    NOW(),
    NULL,
    0
  ),
  (
    @user_id, @storyline_id, @storyline_id, 1,
    JSON_ARRAY(1011, 1012, 1021, 1022),
    JSON_ARRAY(101, 102, 103, 104),
    100,
    NOW(),
    NOW(),
    0
  );

INSERT INTO `user_checkins` (
  `user_id`, `poi_id`, `trigger_mode`, `distance_meters`, `gps_accuracy`, `latitude`, `longitude`, `checked_at`
)
VALUES
  (@user_id, @poi_a, 'gps', 18.50, 8.00, 22.1869, 113.5312, DATE_SUB(NOW(), INTERVAL 3 DAY)),
  (@user_id, @poi_b, 'manual', 24.00, 12.00, 22.1904, 113.5351, DATE_SUB(NOW(), INTERVAL 2 DAY)),
  (@user_id, @poi_c, 'gps', 11.20, 6.00, 22.1975, 113.5407, DATE_SUB(NOW(), INTERVAL 1 DAY));

INSERT INTO `trigger_logs` (
  `user_id`, `poi_id`, `trigger_type`, `distance`, `gps_accuracy`, `wifi_used`, `_openid`
)
VALUES
  (@user_id, @poi_a, 'auto', 18.50, 8.00, 0, 'phase14-seed'),
  (@user_id, @poi_b, 'manual', 24.00, 12.00, 1, 'phase14-seed'),
  (@user_id, @poi_c, 'auto', 11.20, 6.00, 0, 'phase14-seed');

INSERT INTO `seed_runs` (`seed_key`, `description`, `status`, `executed_at`, `notes`)
VALUES (
  'phase14-carryover-verification',
  'Phase 14 deterministic traveler-progress and trigger-log fixture',
  'completed',
  NOW(),
  'Seeds the Phase 14 traveler progress fixture, recent check-ins, and trigger logs used by smoke verification.'
)
ON DUPLICATE KEY UPDATE
  `description` = VALUES(`description`),
  `status` = VALUES(`status`),
  `executed_at` = VALUES(`executed_at`),
  `notes` = VALUES(`notes`);
