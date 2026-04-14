USE `aoxiaoyou`;

SET NAMES utf8mb4;

INSERT INTO `seed_runs` (`seed_key`, `description`, `status`, `executed_at`, `notes`)
VALUES (
  'phase3-public-read-seed',
  'Phase 3 public-read seed for runtime, story chapters, tips, stamps, and notifications',
  'completed',
  NOW(),
  'ASCII-safe seed content to support live public read cutover.'
)
ON DUPLICATE KEY UPDATE
  `description` = VALUES(`description`),
  `status` = VALUES(`status`),
  `executed_at` = VALUES(`executed_at`),
  `notes` = VALUES(`notes`);

INSERT INTO `app_runtime_settings` (
  `setting_group`,
  `setting_key`,
  `locale_code`,
  `title_zh`,
  `title_en`,
  `title_zht`,
  `value_json`,
  `value_text`,
  `status`,
  `sort_order`,
  `published_at`
)
VALUES
  (
    'home',
    'hero_cards',
    '',
    'home hero cards',
    'home hero cards',
    'home hero cards',
    JSON_ARRAY(JSON_OBJECT('cityCode', 'macau', 'priority', 1)),
    NULL,
    'published',
    10,
    NOW()
  ),
  (
    'discover',
    'featured_cards',
    '',
    'discover featured cards',
    'discover featured cards',
    'discover featured cards',
    JSON_ARRAY(
      JSON_OBJECT('cardType', 'activity', 'priority', 1),
      JSON_OBJECT('cardType', 'merchant', 'priority', 2),
      JSON_OBJECT('cardType', 'checkin', 'priority', 3)
    ),
    NULL,
    'published',
    10,
    NOW()
  ),
  (
    'map',
    'checkin_rules',
    '',
    'map checkin rules',
    'map checkin rules',
    'map checkin rules',
    JSON_OBJECT('cooldownSeconds', 1800, 'debounceMillis', 2000, 'manualRadius', 200),
    NULL,
    'published',
    10,
    NOW()
  ),
  (
    'tips',
    'feed_defaults',
    '',
    'tips feed defaults',
    'tips feed defaults',
    'tips feed defaults',
    JSON_OBJECT('pageSize', 10, 'allowUserPublish', true),
    NULL,
    'published',
    10,
    NOW()
  ),
  (
    'profile',
    'badge_panel',
    '',
    'profile badge panel',
    'profile badge panel',
    'profile badge panel',
    JSON_OBJECT('showRecentBadges', true, 'maxItems', 6),
    NULL,
    'published',
    10,
    NOW()
  ),
  (
    'settings',
    'accessibility_defaults',
    '',
    'settings defaults',
    'settings defaults',
    'settings defaults',
    JSON_OBJECT('interfaceMode', 'standard', 'fontScale', 1.0, 'voiceGuideEnabled', true),
    NULL,
    'published',
    10,
    NOW()
  )
ON DUPLICATE KEY UPDATE
  `title_zh` = VALUES(`title_zh`),
  `title_en` = VALUES(`title_en`),
  `title_zht` = VALUES(`title_zht`),
  `value_json` = VALUES(`value_json`),
  `value_text` = VALUES(`value_text`),
  `status` = VALUES(`status`),
  `sort_order` = VALUES(`sort_order`),
  `published_at` = VALUES(`published_at`);

INSERT INTO `story_chapters` (
  `id`, `storyline_id`, `chapter_order`,
  `title_zh`, `title_en`, `title_zht`,
  `summary_zh`, `summary_en`, `summary_zht`,
  `detail_zh`, `detail_en`, `detail_zht`,
  `achievement_zh`, `achievement_en`, `achievement_zht`,
  `collectible_zh`, `collectible_en`, `collectible_zht`,
  `location_name_zh`, `location_name_en`, `location_name_zht`,
  `media_asset_id`, `unlock_type`, `unlock_param_json`,
  `status`, `sort_order`, `published_at`
)
VALUES
  (1011, 1, 1, 'Prologue: Tides and Faith', 'Prologue: Tides and Faith', 'Prologue: Tides and Faith', 'Start from A-Ma Temple to understand Macau origins.', 'Start from A-Ma Temple to understand Macau origins.', 'Start from A-Ma Temple to understand Macau origins.', 'Incense, tides, and docking stories make this the first key to understanding Macau.', 'Incense, tides, and docking stories make this the first key to understanding Macau.', 'Incense, tides, and docking stories make this the first key to understanding Macau.', 'Unlock the Silk Road prologue stamp', 'Unlock the Silk Road prologue stamp', 'Unlock the Silk Road prologue stamp', 'Tide postcard', 'Tide postcard', 'Tide postcard', 'A-Ma Temple', 'A-Ma Temple', 'A-Ma Temple', NULL, 'sequence', NULL, 'published', 1, NOW()),
  (1012, 1, 2, 'Chapter 1: Letters from the Sea Breeze', 'Chapter 1: Letters from the Sea Breeze', 'Chapter 1: Letters from the Sea Breeze', 'Follow the harbor to understand the old maritime route.', 'Follow the harbor to understand the old maritime route.', 'Follow the harbor to understand the old maritime route.', 'Walk along the waterfront to find traces of the old ships.', 'Walk along the waterfront to find traces of the old ships.', 'Walk along the waterfront to find traces of the old ships.', 'Unlock harbor dialogue', 'Unlock harbor dialogue', 'Unlock harbor dialogue', 'Fragment of a sailing journal', 'Fragment of a sailing journal', 'Fragment of a sailing journal', 'A-Ma waterfront', 'A-Ma waterfront', 'A-Ma waterfront', NULL, 'sequence', NULL, 'published', 2, NOW()),
  (1013, 1, 3, 'Chapter 2: Harbor Echoes', 'Chapter 2: Harbor Echoes', 'Chapter 2: Harbor Echoes', 'More ship silhouettes and voices are waiting ahead.', 'More ship silhouettes and voices are waiting ahead.', 'More ship silhouettes and voices are waiting ahead.', 'After the first two chapters, the harbor memory becomes clearer and new clues appear.', 'After the first two chapters, the harbor memory becomes clearer and new clues appear.', 'After the first two chapters, the harbor memory becomes clearer and new clues appear.', 'Unlock sea breeze emblem', 'Unlock sea breeze emblem', 'Unlock sea breeze emblem', 'Harbor badge', 'Harbor badge', 'Harbor badge', 'Harbor promenade', 'Harbor promenade', 'Harbor promenade', NULL, 'sequence', NULL, 'published', 3, NOW()),
  (1014, 1, 4, 'Finale: Night Voyage', 'Finale: Night Voyage', 'Finale: Night Voyage', 'Night sea breezes carry the story farther away.', 'Night sea breezes carry the story farther away.', 'Night sea breezes carry the story farther away.', 'Finish the route to earn a title suited for night exploration.', 'Finish the route to earn a title suited for night exploration.', 'Finish the route to earn a title suited for night exploration.', 'Title: Voyage recorder', 'Title: Voyage recorder', 'Title: Voyage recorder', 'Night voyage star map', 'Night voyage star map', 'Night voyage star map', 'Night harbor', 'Night harbor', 'Night harbor', NULL, 'sequence', NULL, 'published', 4, NOW()),
  (1021, 2, 1, 'Prologue: Beyond the City Wall', 'Prologue: Beyond the City Wall', 'Prologue: Beyond the City Wall', 'Stand before the Ruins and read the scars of the city.', 'Stand before the Ruins and read the scars of the city.', 'Stand before the Ruins and read the scars of the city.', 'The wind across the old wall brings the city past back to the present.', 'The wind across the old wall brings the city past back to the present.', 'The wind across the old wall brings the city past back to the present.', 'Unlock the Ruins footprint stamp', 'Unlock the Ruins footprint stamp', 'Unlock the Ruins footprint stamp', 'Wall rubbing', 'Wall rubbing', 'Wall rubbing', 'Ruins of St. Paul', 'Ruins of St. Paul', 'Ruins of St. Paul', NULL, 'sequence', NULL, 'published', 1, NOW()),
  (1022, 2, 2, 'Chapter 1: Cannon Echoes', 'Chapter 1: Cannon Echoes', 'Chapter 1: Cannon Echoes', 'Follow the ruins to find echoes left by past conflicts.', 'Follow the ruins to find echoes left by past conflicts.', 'Follow the ruins to find echoes left by past conflicts.', 'Climb the stone steps and the story starts to come together.', 'Climb the stone steps and the story starts to come together.', 'Climb the stone steps and the story starts to come together.', 'Unlock conflict narration', 'Unlock conflict narration', 'Unlock conflict narration', 'Fortress seal', 'Fortress seal', 'Fortress seal', 'Fortress hill', 'Fortress hill', 'Fortress hill', NULL, 'sequence', NULL, 'published', 2, NOW()),
  (1023, 2, 3, 'Chapter 2: Square and Church', 'Chapter 2: Square and Church', 'Chapter 2: Square and Church', 'The square, the crowds, and the bells create a different rhythm.', 'The square, the crowds, and the bells create a different rhythm.', 'The square, the crowds, and the bells create a different rhythm.', 'Between the square and the church, the city moves from conflict toward coexistence.', 'Between the square and the church, the city moves from conflict toward coexistence.', 'Between the square and the church, the city moves from conflict toward coexistence.', 'Unlock peace clue', 'Unlock peace clue', 'Unlock peace clue', 'Rose window fragment', 'Rose window fragment', 'Rose window fragment', 'Senado Square', 'Senado Square', 'Senado Square', NULL, 'sequence', NULL, 'published', 3, NOW()),
  (1024, 2, 4, 'Finale: Gate of Peace', 'Finale: Gate of Peace', 'Finale: Gate of Peace', 'All clues lead to a new understanding.', 'All clues lead to a new understanding.', 'All clues lead to a new understanding.', 'Finish the full route to gain a title and collectible that represent the journey.', 'Finish the full route to gain a title and collectible that represent the journey.', 'Finish the full route to gain a title and collectible that represent the journey.', 'Title: Witness of the old city', 'Title: Witness of the old city', 'Title: Witness of the old city', 'Peace commemorative cover', 'Peace commemorative cover', 'Peace commemorative cover', 'St. Dominic Church', 'St. Dominic Church', 'St. Dominic Church', NULL, 'sequence', NULL, 'published', 4, NOW())
ON DUPLICATE KEY UPDATE
  `title_zh` = VALUES(`title_zh`),
  `title_en` = VALUES(`title_en`),
  `title_zht` = VALUES(`title_zht`),
  `summary_zh` = VALUES(`summary_zh`),
  `summary_en` = VALUES(`summary_en`),
  `summary_zht` = VALUES(`summary_zht`),
  `detail_zh` = VALUES(`detail_zh`),
  `detail_en` = VALUES(`detail_en`),
  `detail_zht` = VALUES(`detail_zht`),
  `achievement_zh` = VALUES(`achievement_zh`),
  `achievement_en` = VALUES(`achievement_en`),
  `achievement_zht` = VALUES(`achievement_zht`),
  `collectible_zh` = VALUES(`collectible_zh`),
  `collectible_en` = VALUES(`collectible_en`),
  `collectible_zht` = VALUES(`collectible_zht`),
  `location_name_zh` = VALUES(`location_name_zh`),
  `location_name_en` = VALUES(`location_name_en`),
  `location_name_zht` = VALUES(`location_name_zht`),
  `status` = VALUES(`status`),
  `sort_order` = VALUES(`sort_order`),
  `published_at` = VALUES(`published_at`);

INSERT INTO `tip_articles` (
  `id`, `city_id`, `code`, `category_code`,
  `title_zh`, `title_en`, `title_zht`,
  `summary_zh`, `summary_en`, `summary_zht`,
  `content_zh`, `content_en`, `content_zht`,
  `author_display_name`,
  `location_name_zh`, `location_name_en`, `location_name_zht`,
  `tags_json`, `cover_asset_id`, `source_type`,
  `status`, `sort_order`, `published_at`
)
VALUES
  (301, 1, 'tip_first_trip', 'newbie', 'How to enjoy Macau on your first trip?', 'How to enjoy Macau on your first trip?', 'How to enjoy Macau on your first trip?', 'Use two storylines to connect the historic center without doubling back.', 'Use two storylines to connect the historic center without doubling back.', 'Use two storylines to connect the historic center without doubling back.', 'Start near the Ruins, then walk slowly toward Senado Square to connect the story beats.\nThis keeps the route compact while still unlocking core story content.', 'Start near the Ruins, then walk slowly toward Senado Square to connect the story beats.\nThis keeps the route compact while still unlocking core story content.', 'Start near the Ruins, then walk slowly toward Senado Square to connect the story beats.\nThis keeps the route compact while still unlocking core story content.', 'Trip of Macau Editorial', 'Macau Peninsula', 'Macau Peninsula', 'Macau Peninsula', JSON_ARRAY('route', 'newbie', 'checkin'), NULL, 'editorial', 'published', 1, NOW()),
  (302, 1, 'tip_senior_walk', 'slow-travel', 'A senior friendly half day route', 'A senior friendly half day route', 'A senior friendly half day route', 'Stay around A-Ma and Senado Square for a shorter and gentler walking rhythm.', 'Stay around A-Ma and Senado Square for a shorter and gentler walking rhythm.', 'Stay around A-Ma and Senado Square for a shorter and gentler walking rhythm.', 'Choose stops with places to sit and turn on voice guidance plus larger text for a lighter experience.\nThis route keeps the pace calm while still giving enough story context.', 'Choose stops with places to sit and turn on voice guidance plus larger text for a lighter experience.\nThis route keeps the pace calm while still giving enough story context.', 'Choose stops with places to sit and turn on voice guidance plus larger text for a lighter experience.\nThis route keeps the pace calm while still giving enough story context.', 'City Guide', 'A-Ma Temple', 'A-Ma Temple', 'A-Ma Temple', JSON_ARRAY('senior-mode', 'slow-travel', 'voice'), NULL, 'editorial', 'published', 2, NOW()),
  (303, 1, 'tip_photo_spots', 'photo', 'Five photo spots that shine in Macau', 'Five photo spots that shine in Macau', 'Five photo spots that shine in Macau', 'From the Ruins frontage to church side streets, this guide highlights timing and framing tips.', 'From the Ruins frontage to church side streets, this guide highlights timing and framing tips.', 'From the Ruins frontage to church side streets, this guide highlights timing and framing tips.', 'Morning works best for facades, while dusk is better for layered street scenes.\nKeep a church tower or stone stair at the edge of frame to retain city depth.', 'Morning works best for facades, while dusk is better for layered street scenes.\nKeep a church tower or stone stair at the edge of frame to retain city depth.', 'Morning works best for facades, while dusk is better for layered street scenes.\nKeep a church tower or stone stair at the edge of frame to retain city depth.', 'Photo Walker', 'Ruins of St. Paul', 'Ruins of St. Paul', 'Ruins of St. Paul', JSON_ARRAY('photo', 'guide', 'popular'), NULL, 'editorial', 'published', 3, NOW())
ON DUPLICATE KEY UPDATE
  `title_zh` = VALUES(`title_zh`),
  `title_en` = VALUES(`title_en`),
  `title_zht` = VALUES(`title_zht`),
  `summary_zh` = VALUES(`summary_zh`),
  `summary_en` = VALUES(`summary_en`),
  `summary_zht` = VALUES(`summary_zht`),
  `content_zh` = VALUES(`content_zh`),
  `content_en` = VALUES(`content_en`),
  `content_zht` = VALUES(`content_zht`),
  `author_display_name` = VALUES(`author_display_name`),
  `location_name_zh` = VALUES(`location_name_zh`),
  `location_name_en` = VALUES(`location_name_en`),
  `location_name_zht` = VALUES(`location_name_zht`),
  `tags_json` = VALUES(`tags_json`),
  `status` = VALUES(`status`),
  `sort_order` = VALUES(`sort_order`),
  `published_at` = VALUES(`published_at`);

INSERT INTO `stamps` (
  `id`, `code`, `name_zh`, `name_en`, `name_zht`,
  `description_zh`, `description_en`, `description_zht`,
  `stamp_type`, `rarity`, `icon_asset_id`,
  `related_poi_id`, `related_storyline_id`,
  `status`, `sort_order`, `published_at`
)
VALUES
  (101, 'stamp_ruins_start', 'Ruins footprint stamp', 'Ruins footprint stamp', 'Ruins footprint stamp', 'Reach the Ruins node', 'Reach the Ruins node', 'Reach the Ruins node', 'location', 'common', NULL, 2, NULL, 'published', 1, NOW()),
  (102, 'stamp_square_echo', 'Square rhythm stamp', 'Square rhythm stamp', 'Square rhythm stamp', 'Reach the city square node', 'Reach the city square node', 'Reach the city square node', 'location', 'common', NULL, NULL, NULL, 'published', 2, NOW()),
  (103, 'stamp_silk_prologue', 'Silk Road prologue stamp', 'Silk Road prologue stamp', 'Silk Road prologue stamp', 'Finish the Maritime Silk Road prologue', 'Finish the Maritime Silk Road prologue', 'Finish the Maritime Silk Road prologue', 'story', 'rare', NULL, NULL, 1, 'published', 3, NOW()),
  (104, 'stamp_first_mission', 'First Macau exploration', 'First Macau exploration', 'First Macau exploration', 'Collect three footprint stamps', 'Collect three footprint stamps', 'Collect three footprint stamps', 'mission', 'common', NULL, NULL, NULL, 'published', 4, NOW()),
  (105, 'stamp_rose_echo', 'Rose church echo', 'Rose church echo', 'Rose church echo', 'Trigger one story in night mode', 'Trigger one story in night mode', 'Trigger one story in night mode', 'secret', 'epic', NULL, NULL, 2, 'published', 5, NOW()),
  (106, 'stamp_slow_walk', 'Slow walk stamp', 'Slow walk stamp', 'Slow walk stamp', 'Finish one slow travel recommendation', 'Finish one slow travel recommendation', 'Finish one slow travel recommendation', 'location', 'rare', NULL, NULL, NULL, 'published', 6, NOW()),
  (107, 'stamp_sea_watch', 'Sea breeze watch stamp', 'Sea breeze watch stamp', 'Sea breeze watch stamp', 'Complete one seaside story node', 'Complete one seaside story node', 'Complete one seaside story node', 'location', 'rare', NULL, NULL, 1, 'published', 7, NOW())
ON DUPLICATE KEY UPDATE
  `name_zh` = VALUES(`name_zh`),
  `name_en` = VALUES(`name_en`),
  `name_zht` = VALUES(`name_zht`),
  `description_zh` = VALUES(`description_zh`),
  `description_en` = VALUES(`description_en`),
  `description_zht` = VALUES(`description_zht`),
  `stamp_type` = VALUES(`stamp_type`),
  `rarity` = VALUES(`rarity`),
  `related_poi_id` = VALUES(`related_poi_id`),
  `related_storyline_id` = VALUES(`related_storyline_id`),
  `status` = VALUES(`status`),
  `sort_order` = VALUES(`sort_order`),
  `published_at` = VALUES(`published_at`);

INSERT INTO `notifications` (
  `id`, `code`,
  `title_zh`, `title_en`, `title_zht`,
  `content_zh`, `content_en`, `content_zht`,
  `notification_type`, `target_scope`, `cover_asset_id`, `action_url`,
  `status`, `sort_order`, `publish_start_at`, `publish_end_at`
)
VALUES
  (1, 'notif_night_walk', 'Night walk reminder', 'Night walk reminder', 'Night walk reminder', 'A night walk slot is still open today. Leave a little earlier to stay on schedule.', 'A night walk slot is still open today. Leave a little earlier to stay on schedule.', 'A night walk slot is still open today. Leave a little earlier to stay on schedule.', 'activity', 'all', NULL, '', 'published', 1, NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY)),
  (2, 'notif_tip_saved', 'Your draft got attention', 'Your draft got attention', 'Your draft got attention', 'A travel photo fan liked the route notes you just organized.', 'A travel photo fan liked the route notes you just organized.', 'A travel photo fan liked the route notes you just organized.', 'ugc', 'all', NULL, '', 'published', 2, NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY)),
  (3, 'notif_city_unlock', 'A new city path can unlock', 'A new city path can unlock', 'A new city path can unlock', 'Move closer to Taipa or Coloane and the next city journey will light up automatically.', 'Move closer to Taipa or Coloane and the next city journey will light up automatically.', 'Move closer to Taipa or Coloane and the next city journey will light up automatically.', 'system', 'all', NULL, '', 'published', 3, NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY))
ON DUPLICATE KEY UPDATE
  `title_zh` = VALUES(`title_zh`),
  `title_en` = VALUES(`title_en`),
  `title_zht` = VALUES(`title_zht`),
  `content_zh` = VALUES(`content_zh`),
  `content_en` = VALUES(`content_en`),
  `content_zht` = VALUES(`content_zht`),
  `notification_type` = VALUES(`notification_type`),
  `target_scope` = VALUES(`target_scope`),
  `status` = VALUES(`status`),
  `sort_order` = VALUES(`sort_order`),
  `publish_start_at` = VALUES(`publish_start_at`),
  `publish_end_at` = VALUES(`publish_end_at`);
