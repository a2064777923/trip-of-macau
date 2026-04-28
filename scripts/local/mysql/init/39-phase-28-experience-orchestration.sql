-- Phase 28 replacement: v3.0 experience orchestration foundation.
-- All text is UTF-8 / utf8mb4. Do not rewrite this file through non-UTF-8 shell literals.
-- Canonical binding role for anchor defaults: default_experience_flow
-- Canonical override modes: inherit | disable | replace | append
-- Canonical exploration weight levels: tiny | small | medium | large | core

CREATE TABLE IF NOT EXISTS `experience_templates` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `code` VARCHAR(96) NOT NULL,
  `template_type` VARCHAR(48) NOT NULL,
  `category` VARCHAR(64) NOT NULL DEFAULT '',
  `name_zh` VARCHAR(255) NOT NULL DEFAULT '',
  `name_en` VARCHAR(255) NOT NULL DEFAULT '',
  `name_zht` VARCHAR(255) NOT NULL DEFAULT '',
  `name_pt` VARCHAR(255) NOT NULL DEFAULT '',
  `summary_zh` TEXT NULL,
  `summary_en` TEXT NULL,
  `summary_zht` TEXT NULL,
  `summary_pt` TEXT NULL,
  `config_json` JSON NULL,
  `schema_json` JSON NULL,
  `risk_level` VARCHAR(32) NOT NULL DEFAULT 'normal',
  `status` VARCHAR(32) NOT NULL DEFAULT 'draft',
  `sort_order` INT NOT NULL DEFAULT 0,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY `uk_experience_templates_code` (`code`, `deleted`),
  KEY `idx_experience_templates_type_status` (`template_type`, `status`),
  KEY `idx_experience_templates_category` (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Reusable presentation, trigger, effect, task and game templates';

CREATE TABLE IF NOT EXISTS `experience_flows` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `code` VARCHAR(96) NOT NULL,
  `flow_type` VARCHAR(48) NOT NULL DEFAULT 'default_poi',
  `mode` VARCHAR(48) NOT NULL DEFAULT 'walk_in',
  `name_zh` VARCHAR(255) NOT NULL DEFAULT '',
  `name_en` VARCHAR(255) NOT NULL DEFAULT '',
  `name_zht` VARCHAR(255) NOT NULL DEFAULT '',
  `name_pt` VARCHAR(255) NOT NULL DEFAULT '',
  `description_zh` TEXT NULL,
  `description_en` TEXT NULL,
  `description_zht` TEXT NULL,
  `description_pt` TEXT NULL,
  `map_policy_json` JSON NULL,
  `advanced_config_json` JSON NULL,
  `status` VARCHAR(32) NOT NULL DEFAULT 'draft',
  `sort_order` INT NOT NULL DEFAULT 0,
  `published_at` DATETIME NULL,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY `uk_experience_flows_code` (`code`, `deleted`),
  KEY `idx_experience_flows_type_status` (`flow_type`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Authorable experience flows for POI, story chapter and runtime modes';

CREATE TABLE IF NOT EXISTS `experience_flow_steps` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `flow_id` BIGINT NOT NULL,
  `step_code` VARCHAR(96) NOT NULL,
  `step_type` VARCHAR(48) NOT NULL,
  `template_id` BIGINT NULL,
  `step_name_zh` VARCHAR(255) NOT NULL DEFAULT '',
  `step_name_en` VARCHAR(255) NOT NULL DEFAULT '',
  `step_name_zht` VARCHAR(255) NOT NULL DEFAULT '',
  `step_name_pt` VARCHAR(255) NOT NULL DEFAULT '',
  `description_zh` TEXT NULL,
  `description_en` TEXT NULL,
  `description_zht` TEXT NULL,
  `description_pt` TEXT NULL,
  `trigger_type` VARCHAR(64) NOT NULL DEFAULT 'manual',
  `trigger_config_json` JSON NULL,
  `condition_config_json` JSON NULL,
  `effect_config_json` JSON NULL,
  `media_asset_id` BIGINT NULL,
  `reward_rule_ids_json` JSON NULL,
  `exploration_weight_level` VARCHAR(32) NOT NULL DEFAULT 'small',
  `required_for_completion` TINYINT NOT NULL DEFAULT 0,
  `inherit_key` VARCHAR(128) NOT NULL DEFAULT '',
  `status` VARCHAR(32) NOT NULL DEFAULT 'draft',
  `sort_order` INT NOT NULL DEFAULT 0,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY `uk_experience_flow_steps_code` (`flow_id`, `step_code`, `deleted`),
  KEY `idx_experience_flow_steps_flow` (`flow_id`, `sort_order`),
  KEY `idx_experience_flow_steps_template` (`template_id`),
  CONSTRAINT `fk_experience_flow_steps_flow` FOREIGN KEY (`flow_id`) REFERENCES `experience_flows` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_experience_flow_steps_template` FOREIGN KEY (`template_id`) REFERENCES `experience_templates` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Ordered steps inside an experience flow';

CREATE TABLE IF NOT EXISTS `experience_bindings` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `owner_type` VARCHAR(48) NOT NULL,
  `owner_id` BIGINT NULL,
  `owner_code` VARCHAR(128) NOT NULL DEFAULT '',
  `binding_role` VARCHAR(48) NOT NULL DEFAULT 'default_experience_flow',
  `flow_id` BIGINT NOT NULL,
  `priority` INT NOT NULL DEFAULT 0,
  `inherit_policy` VARCHAR(32) NOT NULL DEFAULT 'inherit',
  `status` VARCHAR(32) NOT NULL DEFAULT 'draft',
  `sort_order` INT NOT NULL DEFAULT 0,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY `uk_experience_bindings_owner_flow` (`owner_type`, `owner_id`, `owner_code`, `binding_role`, `flow_id`, `deleted`),
  KEY `idx_experience_bindings_owner` (`owner_type`, `owner_id`, `owner_code`, `binding_role`),
  KEY `idx_experience_bindings_flow` (`flow_id`),
  CONSTRAINT `fk_experience_bindings_flow` FOREIGN KEY (`flow_id`) REFERENCES `experience_flows` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Binds default_experience_flow or story_override_flow to POI, indoor entities, story chapters or manual owners';

CREATE TABLE IF NOT EXISTS `experience_overrides` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `owner_type` VARCHAR(48) NOT NULL,
  `owner_id` BIGINT NOT NULL,
  `target_owner_type` VARCHAR(48) NOT NULL DEFAULT '',
  `target_owner_id` BIGINT NULL,
  `target_step_code` VARCHAR(96) NOT NULL DEFAULT '',
  `override_mode` VARCHAR(32) NOT NULL DEFAULT 'inherit',
  `replacement_step_id` BIGINT NULL,
  `override_config_json` JSON NULL,
  `status` VARCHAR(32) NOT NULL DEFAULT 'draft',
  `sort_order` INT NOT NULL DEFAULT 0,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY `idx_experience_overrides_owner` (`owner_type`, `owner_id`, `status`),
  KEY `idx_experience_overrides_target` (`target_owner_type`, `target_owner_id`, `target_step_code`),
  KEY `idx_experience_overrides_replacement` (`replacement_step_id`),
  CONSTRAINT `fk_experience_overrides_replacement` FOREIGN KEY (`replacement_step_id`) REFERENCES `experience_flow_steps` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Story chapter or owner-level overrides for inherited experience steps with inherit|disable|replace|append semantics';

CREATE TABLE IF NOT EXISTS `exploration_elements` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `element_code` VARCHAR(128) NOT NULL,
  `element_type` VARCHAR(48) NOT NULL,
  `owner_type` VARCHAR(48) NOT NULL,
  `owner_id` BIGINT NULL,
  `owner_code` VARCHAR(128) NOT NULL DEFAULT '',
  `city_id` BIGINT NULL,
  `sub_map_id` BIGINT NULL,
  `storyline_id` BIGINT NULL,
  `story_chapter_id` BIGINT NULL,
  `title_zh` VARCHAR(255) NOT NULL DEFAULT '',
  `title_en` VARCHAR(255) NOT NULL DEFAULT '',
  `title_zht` VARCHAR(255) NOT NULL DEFAULT '',
  `title_pt` VARCHAR(255) NOT NULL DEFAULT '',
  `weight_level` VARCHAR(32) NOT NULL DEFAULT 'small',
  `weight_value` INT NOT NULL DEFAULT 2,
  `include_in_exploration` TINYINT NOT NULL DEFAULT 1,
  `metadata_json` JSON NULL,
  `status` VARCHAR(32) NOT NULL DEFAULT 'draft',
  `sort_order` INT NOT NULL DEFAULT 0,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY `uk_exploration_elements_code` (`element_code`, `deleted`),
  KEY `idx_exploration_elements_scope` (`city_id`, `sub_map_id`, `storyline_id`, `story_chapter_id`, `status`),
  KEY `idx_exploration_elements_owner` (`owner_type`, `owner_id`, `owner_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Dynamic exploration denominator registry using tiny/small/medium/large/core semantic weights';

CREATE TABLE IF NOT EXISTS `user_exploration_events` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `element_id` BIGINT NULL,
  `element_code` VARCHAR(128) NOT NULL DEFAULT '',
  `event_type` VARCHAR(64) NOT NULL,
  `event_source` VARCHAR(64) NOT NULL DEFAULT 'mini_program',
  `storyline_session_id` VARCHAR(96) NOT NULL DEFAULT '',
  `client_event_id` VARCHAR(128) NOT NULL DEFAULT '',
  `event_payload_json` JSON NULL,
  `occurred_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY `uk_user_exploration_events_client` (`user_id`, `client_event_id`),
  KEY `idx_user_exploration_events_user_element` (`user_id`, `element_id`, `occurred_at`),
  KEY `idx_user_exploration_events_code` (`user_id`, `element_code`, `occurred_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Immutable user exploration event log';

CREATE TABLE IF NOT EXISTS `user_exploration_state` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `scope_type` VARCHAR(48) NOT NULL,
  `scope_id` BIGINT NULL,
  `completed_weight` INT NOT NULL DEFAULT 0,
  `available_weight` INT NOT NULL DEFAULT 0,
  `progress_percent` DECIMAL(6,2) NOT NULL DEFAULT 0,
  `computed_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY `uk_user_exploration_state_scope` (`user_id`, `scope_type`, `scope_id`),
  KEY `idx_user_exploration_state_user` (`user_id`, `computed_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Cached exploration percentages derived from exploration elements';

SET @column_exists = (
  SELECT COUNT(*)
  FROM `information_schema`.`columns`
  WHERE `table_schema` = DATABASE()
    AND `table_name` = 'story_chapters'
    AND `column_name` = 'experience_flow_id'
);
SET @ddl = IF(
  @column_exists = 0,
  'ALTER TABLE `story_chapters` ADD COLUMN `experience_flow_id` BIGINT NULL AFTER `media_asset_id`',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @column_exists = (
  SELECT COUNT(*)
  FROM `information_schema`.`columns`
  WHERE `table_schema` = DATABASE()
    AND `table_name` = 'story_chapters'
    AND `column_name` = 'override_policy_json'
);
SET @ddl = IF(
  @column_exists = 0,
  'ALTER TABLE `story_chapters` ADD COLUMN `override_policy_json` JSON NULL AFTER `experience_flow_id`',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @column_exists = (
  SELECT COUNT(*)
  FROM `information_schema`.`columns`
  WHERE `table_schema` = DATABASE()
    AND `table_name` = 'story_chapters'
    AND `column_name` = 'story_mode_config_json'
);
SET @ddl = IF(
  @column_exists = 0,
  'ALTER TABLE `story_chapters` ADD COLUMN `story_mode_config_json` JSON NULL AFTER `override_policy_json`',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @seed_openid = 'phase28-experience-seed';
SET @poi_ama_id = (SELECT `id` FROM `pois` WHERE `code` = 'ama_temple' LIMIT 1);
SET @poi_lilau_id = (SELECT `id` FROM `pois` WHERE `code` = 'lilau_square' LIMIT 1);
SET @poi_monte_id = (SELECT `id` FROM `pois` WHERE `code` = 'monte_fort' LIMIT 1);
SET @storyline_macau_fire_id = (SELECT `id` FROM `storylines` WHERE `code` = 'macau_fire_route' LIMIT 1);
SET @chapter_ama_id = (SELECT `id` FROM `story_chapters` WHERE `storyline_id` = @storyline_macau_fire_id AND `chapter_order` = 1 LIMIT 1);
SET @city_macau_id = (SELECT `id` FROM `cities` WHERE `code` = 'macau' LIMIT 1);
SET @sub_map_peninsula_id = (SELECT `id` FROM `sub_maps` WHERE `code` = 'macau-peninsula' LIMIT 1);

INSERT INTO `experience_templates` (
  `code`, `template_type`, `category`,
  `name_zh`, `name_en`, `name_zht`, `name_pt`,
  `summary_zh`, `summary_en`, `summary_zht`, `summary_pt`,
  `config_json`, `schema_json`, `risk_level`, `status`, `sort_order`
) VALUES
  ('tpl_poi_intro_modal', 'presentation', 'modal',
   '地點圖文介紹彈窗', 'POI intro modal', '地點圖文介紹彈窗', 'Janela de introducao do local',
   '點擊地點後顯示圖文介紹與前往探索按鈕。', 'Shows intro content and a start exploration button after tapping a POI.', '點擊地點後顯示圖文介紹與前往探索按鈕。', 'Mostra introducao e botao para explorar.',
   JSON_OBJECT('schemaVersion', 1, 'presentation', 'rich_modal', 'primaryAction', '前往探索該地'),
   JSON_OBJECT('schemaVersion', 1, 'required', JSON_ARRAY('presentation')), 'normal', 'published', 10),
  ('tpl_route_guidance_cards', 'effect', 'navigation',
   '目的地導航與推薦卡', 'Destination guidance cards', '目的地導航與推薦卡', 'Cartoes de navegacao',
   '規劃路線後展示交通、故事線與附近地點推薦。', 'After routing, shows traffic, storyline, and nearby recommendations.', '規劃路線後展示交通、故事線與附近地點推薦。', 'Mostra transporte, historias e locais proximos.',
   JSON_OBJECT('schemaVersion', 1, 'effect', 'route_guidance', 'cards', JSON_ARRAY('transport', 'storyline', 'nearby_poi')),
   JSON_OBJECT('schemaVersion', 1), 'normal', 'published', 20),
  ('tpl_proximity_fullscreen_media', 'trigger_effect', 'proximity_media',
   '靠近範圍播放全屏媒體', 'Proximity fullscreen media', '靠近範圍播放全屏媒體', 'Media em tela cheia por proximidade',
   '到達指定半徑後播放全屏動畫、影片或 Lottie。', 'Plays fullscreen animation, video, or Lottie when entering a radius.', '到達指定半徑後播放全屏動畫、影片或 Lottie。', 'Reproduz media em tela cheia ao entrar no raio.',
   JSON_OBJECT('schemaVersion', 1, 'trigger', 'proximity', 'effect', 'fullscreen_media'),
   JSON_OBJECT('schemaVersion', 1), 'high', 'published', 30),
  ('tpl_click_collect_overlay', 'trigger_effect', 'collectible',
   '點擊疊加物拾取', 'Tap overlay pickup', '點擊疊加物拾取', 'Recolha por toque',
   '點擊地圖疊加物後寫入收集事件與探索元素。', 'Tapping a map overlay records pickup and exploration progress.', '點擊地圖疊加物後寫入收集事件與探索元素。', 'Tocar no elemento regista coleta e exploracao.',
   JSON_OBJECT('schemaVersion', 1, 'trigger', 'tap', 'effect', 'grant_collectible'),
   JSON_OBJECT('schemaVersion', 1), 'normal', 'published', 40),
  ('tpl_hidden_quiz_challenge', 'gameplay', 'quiz',
   '全收集問答挑戰', 'All-collection quiz challenge', '全收集問答挑戰', 'Desafio de perguntas',
   '完成指定拾取物與停留條件後解鎖問答挑戰。', 'Unlocks a quiz challenge after pickups and dwell conditions are met.', '完成指定拾取物與停留條件後解鎖問答挑戰。', 'Desbloqueia desafio apos coleta e permanencia.',
   JSON_OBJECT('schemaVersion', 1, 'gameplay', 'quiz', 'questionCount', 3, 'passMode', 'all_correct'),
   JSON_OBJECT('schemaVersion', 1), 'normal', 'published', 50),
  ('tpl_reward_presentation', 'effect', 'reward',
   '獎勵與稱號演出', 'Reward presentation', '獎勵與稱號演出', 'Apresentacao de recompensa',
   '發放徽章、稱號、金币或物品時可播放全屏動畫與音效。', 'Shows animation and sound when granting badges, titles, coins or items.', '發放徽章、稱號、金币或物品時可播放全屏動畫與音效。', 'Mostra animacao e som ao conceder recompensas.',
   JSON_OBJECT('schemaVersion', 1, 'effect', 'reward_grant', 'presentation', 'queue'),
   JSON_OBJECT('schemaVersion', 1), 'high', 'published', 60)
ON DUPLICATE KEY UPDATE
  `template_type` = VALUES(`template_type`),
  `category` = VALUES(`category`),
  `name_zh` = VALUES(`name_zh`),
  `name_en` = VALUES(`name_en`),
  `name_zht` = VALUES(`name_zht`),
  `name_pt` = VALUES(`name_pt`),
  `summary_zh` = VALUES(`summary_zh`),
  `summary_zht` = VALUES(`summary_zht`),
  `config_json` = VALUES(`config_json`),
  `schema_json` = VALUES(`schema_json`),
  `risk_level` = VALUES(`risk_level`),
  `status` = VALUES(`status`),
  `sort_order` = VALUES(`sort_order`),
  `deleted` = 0;

INSERT INTO `experience_flows` (
  `code`, `flow_type`, `mode`,
  `name_zh`, `name_en`, `name_zht`, `name_pt`,
  `description_zh`, `description_en`, `description_zht`, `description_pt`,
  `map_policy_json`, `advanced_config_json`, `status`, `sort_order`, `published_at`
) VALUES
  ('poi_ama_default_walk_in', 'default_poi', 'walk_in',
   '媽閣廟預設地點體驗', 'A-Ma Temple default POI experience', '媽閣廟預設地點體驗', 'Experiencia padrao do Templo A-Ma',
   '自然走近或點擊媽閣廟時使用的基礎互動流程，可被故事線章節繼承與覆寫。', 'Default flow for walk-in or tap interactions at A-Ma Temple.', '自然走近或點擊媽閣廟時使用的基礎互動流程，可被故事線章節繼承與覆寫。', 'Fluxo padrao do Templo A-Ma.',
   JSON_OBJECT('schemaVersion', 1, 'hideUnrelated', FALSE, 'routeStyle', 'poi_default'),
   JSON_OBJECT('schemaVersion', 1, 'source', 'phase28-replacement'), 'published', 10, NOW()),
  ('story_macau_fire_chapter_1_override', 'story_chapter_override', 'story_mode',
   '鏡海初戰章節覆寫流程', 'Mirror Sea Clash chapter override', '鏡海初戰章節覆寫流程', 'Substituicao do primeiro capitulo',
   '繼承媽閣廟預設體驗，但關閉一般到達動畫，改用故事線主線短片、三個主線疊加物與隱藏挑戰。', 'Inherits A-Ma default flow and replaces generic arrival media with storyline-specific effects.', '繼承媽閣廟預設體驗，但關閉一般到達動畫，改用故事線主線短片、三個主線疊加物與隱藏挑戰。', 'Substitui media generico por efeitos da historia.',
   JSON_OBJECT('schemaVersion', 1, 'hideUnrelated', TRUE, 'currentChapterHighlight', TRUE, 'exitResetsSession', TRUE),
   JSON_OBJECT('schemaVersion', 1, 'source', 'phase28-replacement'), 'published', 20, NOW())
ON DUPLICATE KEY UPDATE
  `flow_type` = VALUES(`flow_type`),
  `mode` = VALUES(`mode`),
  `name_zh` = VALUES(`name_zh`),
  `name_zht` = VALUES(`name_zht`),
  `description_zh` = VALUES(`description_zh`),
  `description_zht` = VALUES(`description_zht`),
  `map_policy_json` = VALUES(`map_policy_json`),
  `advanced_config_json` = VALUES(`advanced_config_json`),
  `status` = VALUES(`status`),
  `sort_order` = VALUES(`sort_order`),
  `published_at` = VALUES(`published_at`),
  `deleted` = 0;

SET @tpl_intro_id = (SELECT `id` FROM `experience_templates` WHERE `code` = 'tpl_poi_intro_modal' LIMIT 1);
SET @tpl_route_id = (SELECT `id` FROM `experience_templates` WHERE `code` = 'tpl_route_guidance_cards' LIMIT 1);
SET @tpl_media_id = (SELECT `id` FROM `experience_templates` WHERE `code` = 'tpl_proximity_fullscreen_media' LIMIT 1);
SET @tpl_collect_id = (SELECT `id` FROM `experience_templates` WHERE `code` = 'tpl_click_collect_overlay' LIMIT 1);
SET @tpl_quiz_id = (SELECT `id` FROM `experience_templates` WHERE `code` = 'tpl_hidden_quiz_challenge' LIMIT 1);
SET @tpl_reward_id = (SELECT `id` FROM `experience_templates` WHERE `code` = 'tpl_reward_presentation' LIMIT 1);
SET @flow_ama_default_id = (SELECT `id` FROM `experience_flows` WHERE `code` = 'poi_ama_default_walk_in' LIMIT 1);
SET @flow_chapter_1_id = (SELECT `id` FROM `experience_flows` WHERE `code` = 'story_macau_fire_chapter_1_override' LIMIT 1);

INSERT INTO `experience_flow_steps` (
  `flow_id`, `step_code`, `step_type`, `template_id`,
  `step_name_zh`, `step_name_en`, `step_name_zht`, `step_name_pt`,
  `description_zh`, `description_en`, `description_zht`, `description_pt`,
  `trigger_type`, `trigger_config_json`, `condition_config_json`, `effect_config_json`,
  `media_asset_id`, `reward_rule_ids_json`, `exploration_weight_level`, `required_for_completion`, `inherit_key`, `status`, `sort_order`
) VALUES
  (@flow_ama_default_id, 'tap_intro', 'presentation', @tpl_intro_id,
   '點擊地點介紹', 'Tap POI intro', '點擊地點介紹', 'Introducao por toque',
   '用圖文彈窗展示媽閣廟簡介，並提供前往探索按鈕。', 'Shows A-Ma intro and a start exploration button.', '用圖文彈窗展示媽閣廟簡介，並提供前往探索按鈕。', 'Mostra introducao do Templo A-Ma.',
   'tap', JSON_OBJECT('schemaVersion', 1, 'target', 'poi'), JSON_OBJECT('schemaVersion', 1, 'visibility', 'always'), JSON_OBJECT('schemaVersion', 1, 'effect', 'show_modal', 'primaryAction', '前往探索該地'),
   NULL, NULL, 'tiny', 0, 'poi_intro', 'published', 10),
  (@flow_ama_default_id, 'route_guidance', 'navigation', @tpl_route_id,
   '前往探索導航', 'Explore destination routing', '前往探索導航', 'Navegacao para explorar',
   '規劃路線後展示交通方式、故事線推薦與附近地點。', 'Shows route, transport options, storylines and nearby POIs.', '規劃路線後展示交通方式、故事線推薦與附近地點。', 'Mostra rota e recomendacoes.',
   'tap_action', JSON_OBJECT('schemaVersion', 1, 'action', 'start_explore'), JSON_OBJECT('schemaVersion', 1), JSON_OBJECT('schemaVersion', 1, 'effect', 'route_guidance', 'cards', JSON_ARRAY('transport', 'storyline', 'nearby_poi')),
   NULL, NULL, 'small', 0, 'poi_route_guidance', 'published', 20),
  (@flow_ama_default_id, 'arrival_intro_media', 'fullscreen_media', @tpl_media_id,
   '到達播放介紹動畫', 'Arrival intro animation', '到達播放介紹動畫', 'Animacao ao chegar',
   '進入媽閣廟 50 米範圍後播放地點介紹動畫與背景音。', 'Plays intro animation and ambient audio within 50 meters.', '進入媽閣廟 50 米範圍後播放地點介紹動畫與背景音。', 'Reproduz animacao ao chegar.',
   'proximity', JSON_OBJECT('schemaVersion', 1, 'radiusMeters', 50), JSON_OBJECT('schemaVersion', 1, 'cooldown', 'once_per_user'), JSON_OBJECT('schemaVersion', 1, 'effect', 'fullscreen_media', 'assetKind', 'lottie_or_video'),
   328001, NULL, 'medium', 0, 'poi_arrival_media', 'published', 30),
  (@flow_ama_default_id, 'checkin_task_release', 'task_bundle', NULL,
   '打卡後派發任務', 'Release check-in tasks', '打卡後派發任務', 'Lancamento de tarefas',
   '介紹動畫結束後派發大門拍照與賽博點香兩個任務。', 'After intro media, releases photo and cyber incense tasks.', '介紹動畫結束後派發大門拍照與賽博點香兩個任務。', 'Liberta tarefas apos a introducao.',
   'media_finished', JSON_OBJECT('schemaVersion', 1, 'sourceStepCode', 'arrival_intro_media'), JSON_OBJECT('schemaVersion', 1), JSON_OBJECT('schemaVersion', 1, 'effect', 'release_tasks', 'taskCodes', JSON_ARRAY('ama_gate_photo', 'ama_cyber_incense')),
   NULL, NULL, 'medium', 1, 'poi_checkin_tasks', 'published', 40),
  (@flow_ama_default_id, 'hidden_dwell_title', 'achievement', @tpl_reward_id,
   '停留隱藏成就', 'Hidden dwell achievement', '停留隱藏成就', 'Conquista oculta',
   '在媽閣廟 30 米範圍內累計停留 30 分鐘後解鎖隱藏稱號。', 'Unlocks a hidden title after 30 minutes within 30 meters.', '在媽閣廟 30 米範圍內累計停留 30 分鐘後解鎖隱藏稱號。', 'Desbloqueia titulo oculto apos permanencia.',
   'dwell', JSON_OBJECT('schemaVersion', 1, 'radiusMeters', 30, 'dwellSeconds', 1800), JSON_OBJECT('schemaVersion', 1), JSON_OBJECT('schemaVersion', 1, 'effect', 'grant_title', 'title', '媽祖最愛心誠的孩子'),
   NULL, NULL, 'large', 0, 'poi_hidden_dwell_title', 'published', 50),
  (@flow_chapter_1_id, 'disable_default_arrival_media', 'override', NULL,
   '關閉地點預設到達動畫', 'Disable default arrival media', '關閉地點預設到達動畫', 'Desativar media padrao',
   '故事線模式下關閉媽閣廟一般到達動畫，改由主線劇情短片接管。', 'In story mode, disables the generic POI arrival media.', '故事線模式下關閉媽閣廟一般到達動畫，改由主線劇情短片接管。', 'No modo historia, desativa media padrao.',
   'story_mode_enter', JSON_OBJECT('schemaVersion', 1), JSON_OBJECT('schemaVersion', 1), JSON_OBJECT('schemaVersion', 1, 'overrideMode', 'disable', 'targetStepCode', 'arrival_intro_media'),
   NULL, NULL, 'tiny', 0, 'poi_arrival_media', 'published', 10),
  (@flow_chapter_1_id, 'chapter_core_media', 'fullscreen_media', @tpl_media_id,
   '核心歷史劇情短片', 'Core historical story clip', '核心歷史劇情短片', 'Video historico principal',
   '抵達 50 米範圍後播放中葡首次海防對峙主線劇情。', 'Plays the main historical story after entering 50 meters.', '抵達 50 米範圍後播放中葡首次海防對峙主線劇情。', 'Reproduz narrativa historica principal.',
   'proximity', JSON_OBJECT('schemaVersion', 1, 'radiusMeters', 50), JSON_OBJECT('schemaVersion', 1, 'requiresStoryMode', TRUE), JSON_OBJECT('schemaVersion', 1, 'effect', 'fullscreen_media', 'narration', '鏡海初戰'),
   328003, NULL, 'core', 1, 'story_arrival_media', 'published', 20),
  (@flow_chapter_1_id, 'main_overlay_collect_3', 'overlay_collection', @tpl_collect_id,
   '三個主線疊加物收集', 'Collect three main overlays', '三個主線疊加物收集', 'Coletar tres sobreposicoes',
   '依次點擊明朝水師戰船、媽閣漁民防線與葡國武裝商船。', 'Tap Ming ships, fisher defense line, and Portuguese armed ship overlays.', '依次點擊明朝水師戰船、媽閣漁民防線與葡國武裝商船。', 'Tocar tres sobreposicoes principais.',
   'tap_sequence', JSON_OBJECT('schemaVersion', 1, 'sequence', JSON_ARRAY('ming_warship', 'fisher_defense', 'portuguese_ship')), JSON_OBJECT('schemaVersion', 1, 'afterStepCode', 'chapter_core_media'), JSON_OBJECT('schemaVersion', 1, 'effect', 'complete_chapter_objective'),
   NULL, NULL, 'core', 1, 'story_main_overlays', 'published', 30),
  (@flow_chapter_1_id, 'side_pickups', 'side_pickup_bundle', @tpl_collect_id,
   '支線探索拾取', 'Side exploration pickups', '支線探索拾取', 'Coletas secundarias',
   '明朝海防銅令牌、濠江漁民禦敵漁網殘片、葡人通商納稅契約殘頁。', 'Three optional pickups around A-Ma Temple.', '明朝海防銅令牌、濠江漁民禦敵漁網殘片、葡人通商納稅契約殘頁。', 'Tres objetos opcionais.',
   'mixed', JSON_OBJECT('schemaVersion', 1, 'items', JSON_ARRAY('ming_coastal_token', 'fisher_net_fragment', 'tax_contract_page')), JSON_OBJECT('schemaVersion', 1, 'afterStepCode', 'chapter_core_media'), JSON_OBJECT('schemaVersion', 1, 'effect', 'grant_collectibles'),
   NULL, NULL, 'large', 0, 'story_side_pickups', 'published', 40),
  (@flow_chapter_1_id, 'hidden_guardian_quiz', 'hidden_challenge', @tpl_quiz_id,
   '鏡海守護者隱藏挑戰', 'Mirror Sea Guardian hidden challenge', '鏡海守護者隱藏挑戰', 'Desafio Guardiao do Mar',
   '三個拾取物全部收集且停留超過五分鐘後，開啟三題歷史問答。', 'Unlocks a three-question quiz after full pickups and five minutes dwell.', '三個拾取物全部收集且停留超過五分鐘後，開啟三題歷史問答。', 'Desbloqueia quiz apos coleta total.',
   'compound', JSON_OBJECT('schemaVersion', 1, 'requiresAllPickups', TRUE, 'dwellSeconds', 300), JSON_OBJECT('schemaVersion', 1), JSON_OBJECT('schemaVersion', 1, 'effect', 'unlock_quiz', 'rewardCode', 'ama_battle_mirror_fragment'),
   NULL, NULL, 'core', 0, 'story_hidden_quiz', 'published', 50)
ON DUPLICATE KEY UPDATE
  `step_type` = VALUES(`step_type`),
  `template_id` = VALUES(`template_id`),
  `step_name_zh` = VALUES(`step_name_zh`),
  `step_name_zht` = VALUES(`step_name_zht`),
  `description_zh` = VALUES(`description_zh`),
  `description_zht` = VALUES(`description_zht`),
  `trigger_type` = VALUES(`trigger_type`),
  `trigger_config_json` = VALUES(`trigger_config_json`),
  `condition_config_json` = VALUES(`condition_config_json`),
  `effect_config_json` = VALUES(`effect_config_json`),
  `media_asset_id` = VALUES(`media_asset_id`),
  `exploration_weight_level` = VALUES(`exploration_weight_level`),
  `required_for_completion` = VALUES(`required_for_completion`),
  `inherit_key` = VALUES(`inherit_key`),
  `status` = VALUES(`status`),
  `sort_order` = VALUES(`sort_order`),
  `deleted` = 0;

INSERT INTO `experience_bindings` (
  `owner_type`, `owner_id`, `owner_code`, `binding_role`, `flow_id`, `priority`, `inherit_policy`, `status`, `sort_order`
) VALUES
  ('poi', @poi_ama_id, 'ama_temple', 'default_experience_flow', @flow_ama_default_id, 10, 'inherit', 'published', 10),
  ('story_chapter', @chapter_ama_id, 'macau_fire_route_chapter_1', 'story_override_flow', @flow_chapter_1_id, 20, 'override', 'published', 20)
ON DUPLICATE KEY UPDATE
  `priority` = VALUES(`priority`),
  `inherit_policy` = VALUES(`inherit_policy`),
  `status` = VALUES(`status`),
  `sort_order` = VALUES(`sort_order`),
  `deleted` = 0;

SET @disable_step_id = (SELECT `id` FROM `experience_flow_steps` WHERE `flow_id` = @flow_chapter_1_id AND `step_code` = 'disable_default_arrival_media' LIMIT 1);
INSERT INTO `experience_overrides` (
  `owner_type`, `owner_id`, `target_owner_type`, `target_owner_id`, `target_step_code`,
  `override_mode`, `replacement_step_id`, `override_config_json`, `status`, `sort_order`
) VALUES
  ('story_chapter', @chapter_ama_id, 'poi', @poi_ama_id, 'arrival_intro_media',
   'disable', @disable_step_id, JSON_OBJECT('schemaVersion', 1, 'reason', '故事線主線短片接管一般到達動畫'), 'published', 10)
ON DUPLICATE KEY UPDATE
  `override_mode` = VALUES(`override_mode`),
  `replacement_step_id` = VALUES(`replacement_step_id`),
  `override_config_json` = VALUES(`override_config_json`),
  `status` = VALUES(`status`),
  `sort_order` = VALUES(`sort_order`),
  `deleted` = 0;

INSERT INTO `exploration_elements` (
  `element_code`, `element_type`, `owner_type`, `owner_id`, `owner_code`,
  `city_id`, `sub_map_id`, `storyline_id`, `story_chapter_id`,
  `title_zh`, `title_en`, `title_zht`, `title_pt`,
  `weight_level`, `weight_value`, `include_in_exploration`, `metadata_json`, `status`, `sort_order`
) VALUES
  ('ama_poi_arrival', 'poi_arrival', 'poi', @poi_ama_id, 'ama_temple', @city_macau_id, @sub_map_peninsula_id, NULL, NULL,
   '到達媽閣廟', 'Arrive at A-Ma Temple', '到達媽閣廟', 'Chegar ao Templo A-Ma',
   'small', 2, 1, JSON_OBJECT('schemaVersion', 1, 'source', 'poi_default'), 'published', 10),
  ('ama_story_ch1_complete', 'story_chapter_complete', 'story_chapter', @chapter_ama_id, 'macau_fire_route_chapter_1', @city_macau_id, @sub_map_peninsula_id, @storyline_macau_fire_id, @chapter_ama_id,
   '完成鏡海初戰主線', 'Complete Mirror Sea Clash', '完成鏡海初戰主線', 'Completar primeiro capitulo',
   'core', 8, 1, JSON_OBJECT('schemaVersion', 1, 'source', 'storyline'), 'published', 20),
  ('ama_pickup_ming_coastal_token', 'collectible_pickup', 'story_chapter', @chapter_ama_id, 'ming_coastal_token', @city_macau_id, @sub_map_peninsula_id, @storyline_macau_fire_id, @chapter_ama_id,
   '拾取明朝海防銅令牌', 'Pick up Ming coastal token', '拾取明朝海防銅令牌', 'Recolher ficha costeira Ming',
   'medium', 3, 1, JSON_OBJECT('schemaVersion', 1, 'rarity', 'common'), 'published', 30),
  ('ama_pickup_fisher_net_fragment', 'collectible_pickup', 'story_chapter', @chapter_ama_id, 'fisher_net_fragment', @city_macau_id, @sub_map_peninsula_id, @storyline_macau_fire_id, @chapter_ama_id,
   '拾取濠江漁民禦敵漁網殘片', 'Pick up fisher defense net fragment', '拾取濠江漁民禦敵漁網殘片', 'Recolher fragmento de rede',
   'large', 5, 1, JSON_OBJECT('schemaVersion', 1, 'rarity', 'rare'), 'published', 40),
  ('ama_pickup_tax_contract_page', 'collectible_pickup', 'story_chapter', @chapter_ama_id, 'tax_contract_page', @city_macau_id, @sub_map_peninsula_id, @storyline_macau_fire_id, @chapter_ama_id,
   '拾取葡人通商納稅契約殘頁', 'Pick up trade tax contract page', '拾取葡人通商納稅契約殘頁', 'Recolher pagina do contrato',
   'large', 5, 1, JSON_OBJECT('schemaVersion', 1, 'rarity', 'rare'), 'published', 50),
  ('ama_hidden_guardian_quiz', 'hidden_challenge', 'story_chapter', @chapter_ama_id, 'hidden_guardian_quiz', @city_macau_id, @sub_map_peninsula_id, @storyline_macau_fire_id, @chapter_ama_id,
   '完成鏡海守護者隱藏挑戰', 'Complete Mirror Sea Guardian challenge', '完成鏡海守護者隱藏挑戰', 'Completar desafio oculto',
   'core', 8, 1, JSON_OBJECT('schemaVersion', 1, 'challengeType', 'quiz'), 'published', 60)
ON DUPLICATE KEY UPDATE
  `element_type` = VALUES(`element_type`),
  `owner_type` = VALUES(`owner_type`),
  `owner_id` = VALUES(`owner_id`),
  `owner_code` = VALUES(`owner_code`),
  `city_id` = VALUES(`city_id`),
  `sub_map_id` = VALUES(`sub_map_id`),
  `storyline_id` = VALUES(`storyline_id`),
  `story_chapter_id` = VALUES(`story_chapter_id`),
  `title_zh` = VALUES(`title_zh`),
  `title_zht` = VALUES(`title_zht`),
  `weight_level` = VALUES(`weight_level`),
  `weight_value` = VALUES(`weight_value`),
  `include_in_exploration` = VALUES(`include_in_exploration`),
  `metadata_json` = VALUES(`metadata_json`),
  `status` = VALUES(`status`),
  `sort_order` = VALUES(`sort_order`),
  `deleted` = 0;

UPDATE `story_chapters`
SET
  `experience_flow_id` = @flow_chapter_1_id,
  `override_policy_json` = JSON_OBJECT(
    'schemaVersion', 1,
    'inheritFrom', JSON_OBJECT('ownerType', 'poi', 'ownerId', @poi_ama_id, 'flowCode', 'poi_ama_default_walk_in'),
    'supportedModes', JSON_ARRAY('inherit', 'disable', 'replace', 'append'),
    'stepOverrides', JSON_ARRAY(
      JSON_OBJECT('targetStepCode', 'arrival_intro_media', 'mode', 'disable'),
      JSON_OBJECT('targetStepCode', 'chapter_core_media', 'mode', 'append')
    )
  ),
  `story_mode_config_json` = JSON_OBJECT(
    'schemaVersion', 1,
    'hideUnrelatedContent', TRUE,
    'nearbyRevealMeters', 80,
    'currentRouteStyle', 'highlighted',
    'inactiveRouteStyle', 'muted',
    'exitResetsSessionProgress', TRUE
  )
WHERE `id` = @chapter_ama_id;

INSERT INTO `content_relation_links` (
  `owner_type`, `owner_id`, `relation_type`, `target_type`, `target_id`, `target_code`, `metadata_json`, `sort_order`
) VALUES
  ('poi', @poi_ama_id, 'default_experience_flow', 'experience_flow', @flow_ama_default_id, 'poi_ama_default_walk_in', JSON_OBJECT('schemaVersion', 1), 10),
  ('story_chapter', @chapter_ama_id, 'story_chapter_anchor', 'poi', @poi_ama_id, 'ama_temple', JSON_OBJECT('schemaVersion', 1), 10),
  ('story_chapter', @chapter_ama_id, 'story_override_target', 'experience_flow', @flow_ama_default_id, 'poi_ama_default_walk_in', JSON_OBJECT('schemaVersion', 1, 'overrideFlowId', @flow_chapter_1_id), 20),
  ('story_chapter', @chapter_ama_id, 'exploration_element_binding', 'exploration_element', (SELECT `id` FROM `exploration_elements` WHERE `element_code` = 'ama_story_ch1_complete' LIMIT 1), 'ama_story_ch1_complete', JSON_OBJECT('schemaVersion', 1), 30)
ON DUPLICATE KEY UPDATE
  `target_id` = VALUES(`target_id`),
  `target_code` = VALUES(`target_code`),
  `metadata_json` = VALUES(`metadata_json`),
  `sort_order` = VALUES(`sort_order`),
  `deleted` = 0;

INSERT INTO `seed_runs` (`seed_key`, `description`, `status`, `executed_at`, `notes`)
VALUES (
  'phase28-experience-orchestration',
  'v3.0 experience orchestration foundation',
  'completed',
  NOW(),
  'Adds reusable experience templates, POI default flow, story chapter override flow, and dynamic exploration elements for A-Ma chapter.'
)
ON DUPLICATE KEY UPDATE
  `description` = VALUES(`description`),
  `status` = VALUES(`status`),
  `executed_at` = VALUES(`executed_at`),
  `notes` = VALUES(`notes`);
