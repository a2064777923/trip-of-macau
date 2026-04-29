USE `aoxiaoyou`;

SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;
SET FOREIGN_KEY_CHECKS = 0;

CREATE TABLE IF NOT EXISTS `reward_presentations` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `code` VARCHAR(64) NOT NULL,
  `name_zh` VARCHAR(128) NOT NULL,
  `name_zht` VARCHAR(128) NOT NULL DEFAULT '',
  `presentation_type` VARCHAR(32) NOT NULL DEFAULT 'popup_card',
  `first_time_only` TINYINT NOT NULL DEFAULT 1,
  `skippable` TINYINT NOT NULL DEFAULT 1,
  `minimum_display_ms` INT NOT NULL DEFAULT 1200,
  `interrupt_policy` VARCHAR(32) NOT NULL DEFAULT 'queue_after_current',
  `queue_policy` VARCHAR(32) NOT NULL DEFAULT 'enqueue',
  `priority_weight` INT NOT NULL DEFAULT 0,
  `cover_asset_id` BIGINT NULL,
  `voice_over_asset_id` BIGINT NULL,
  `sfx_asset_id` BIGINT NULL,
  `summary_text` VARCHAR(255) NOT NULL DEFAULT '',
  `config_json` JSON NULL,
  `status` VARCHAR(16) NOT NULL DEFAULT 'draft',
  `deleted` TINYINT NOT NULL DEFAULT 0,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY `uk_reward_presentations_code` (`code`),
  KEY `idx_reward_presentations_type_status` (`presentation_type`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `reward_presentation_steps` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `presentation_id` BIGINT NOT NULL,
  `step_type` VARCHAR(32) NOT NULL DEFAULT 'popup_card',
  `step_code` VARCHAR(64) NOT NULL DEFAULT '',
  `title_text` VARCHAR(255) NOT NULL DEFAULT '',
  `asset_id` BIGINT NULL,
  `duration_ms` INT NOT NULL DEFAULT 1200,
  `skippable_override` TINYINT NULL,
  `trigger_sfx_asset_id` BIGINT NULL,
  `voice_over_asset_id` BIGINT NULL,
  `overlay_config_json` JSON NULL,
  `sort_order` INT NOT NULL DEFAULT 0,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY `idx_reward_presentation_steps_parent` (`presentation_id`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `reward_rules` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `code` VARCHAR(64) NOT NULL,
  `rule_type` VARCHAR(32) NOT NULL DEFAULT 'grant_rule',
  `status` VARCHAR(16) NOT NULL DEFAULT 'draft',
  `name_zh` VARCHAR(128) NOT NULL,
  `name_zht` VARCHAR(128) NOT NULL DEFAULT '',
  `summary_text` VARCHAR(255) NOT NULL DEFAULT '',
  `root_condition_group_id` BIGINT NULL,
  `advanced_config_json` JSON NULL,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY `uk_reward_rules_code` (`code`),
  KEY `idx_reward_rules_type_status` (`rule_type`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `reward_condition_groups` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `rule_id` BIGINT NOT NULL,
  `parent_group_id` BIGINT NULL,
  `group_code` VARCHAR(64) NOT NULL DEFAULT '',
  `operator_type` VARCHAR(32) NOT NULL DEFAULT 'all',
  `minimum_match_count` INT NULL,
  `summary_text` VARCHAR(255) NOT NULL DEFAULT '',
  `advanced_config_json` JSON NULL,
  `sort_order` INT NOT NULL DEFAULT 0,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY `idx_reward_condition_groups_rule` (`rule_id`, `parent_group_id`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `reward_conditions` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `group_id` BIGINT NOT NULL,
  `condition_type` VARCHAR(32) NOT NULL DEFAULT 'numeric_progress',
  `metric_type` VARCHAR(64) NOT NULL DEFAULT '',
  `operator_type` VARCHAR(32) NOT NULL DEFAULT 'gte',
  `comparator_value` VARCHAR(128) NOT NULL DEFAULT '',
  `comparator_unit` VARCHAR(32) NOT NULL DEFAULT '',
  `summary_text` VARCHAR(255) NOT NULL DEFAULT '',
  `config_json` JSON NULL,
  `sort_order` INT NOT NULL DEFAULT 0,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY `idx_reward_conditions_group` (`group_id`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `redeemable_prizes` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `code` VARCHAR(64) NOT NULL,
  `legacy_source_type` VARCHAR(32) NOT NULL DEFAULT '',
  `legacy_source_id` BIGINT NULL,
  `prize_type` VARCHAR(32) NOT NULL DEFAULT 'virtual_item_pack',
  `fulfillment_mode` VARCHAR(32) NOT NULL DEFAULT 'virtual_issue',
  `name_zh` VARCHAR(128) NOT NULL,
  `name_en` VARCHAR(128) NOT NULL DEFAULT '',
  `name_zht` VARCHAR(128) NOT NULL DEFAULT '',
  `name_pt` VARCHAR(128) NOT NULL DEFAULT '',
  `subtitle_zh` VARCHAR(255) NOT NULL DEFAULT '',
  `subtitle_en` VARCHAR(255) NOT NULL DEFAULT '',
  `subtitle_zht` VARCHAR(255) NOT NULL DEFAULT '',
  `subtitle_pt` VARCHAR(255) NOT NULL DEFAULT '',
  `description_zh` TEXT NULL,
  `description_en` TEXT NULL,
  `description_zht` TEXT NULL,
  `description_pt` TEXT NULL,
  `highlight_zh` VARCHAR(255) NOT NULL DEFAULT '',
  `highlight_en` VARCHAR(255) NOT NULL DEFAULT '',
  `highlight_zht` VARCHAR(255) NOT NULL DEFAULT '',
  `highlight_pt` VARCHAR(255) NOT NULL DEFAULT '',
  `cover_asset_id` BIGINT NULL,
  `stamp_cost` INT NOT NULL DEFAULT 0,
  `inventory_total` INT NOT NULL DEFAULT 0,
  `inventory_redeemed` INT NOT NULL DEFAULT 0,
  `stock_policy_json` JSON NULL,
  `fulfillment_config_json` JSON NULL,
  `operator_notes` TEXT NULL,
  `presentation_id` BIGINT NULL,
  `status` VARCHAR(16) NOT NULL DEFAULT 'draft',
  `sort_order` INT NOT NULL DEFAULT 0,
  `publish_start_at` DATETIME NULL,
  `publish_end_at` DATETIME NULL,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY `uk_redeemable_prizes_code` (`code`),
  UNIQUE KEY `uk_redeemable_prizes_legacy` (`legacy_source_type`, `legacy_source_id`),
  KEY `idx_redeemable_prizes_status_sort` (`status`, `sort_order`),
  KEY `idx_redeemable_prizes_family` (`prize_type`, `fulfillment_mode`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `game_rewards` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `code` VARCHAR(64) NOT NULL,
  `legacy_source_type` VARCHAR(32) NOT NULL DEFAULT '',
  `legacy_source_id` BIGINT NULL,
  `reward_type` VARCHAR(32) NOT NULL DEFAULT 'badge',
  `rarity` VARCHAR(32) NOT NULL DEFAULT 'common',
  `stackable` TINYINT NOT NULL DEFAULT 0,
  `max_owned` INT NULL,
  `can_equip` TINYINT NOT NULL DEFAULT 0,
  `can_consume` TINYINT NOT NULL DEFAULT 0,
  `name_zh` VARCHAR(128) NOT NULL,
  `name_en` VARCHAR(128) NOT NULL DEFAULT '',
  `name_zht` VARCHAR(128) NOT NULL DEFAULT '',
  `name_pt` VARCHAR(128) NOT NULL DEFAULT '',
  `subtitle_zh` VARCHAR(255) NOT NULL DEFAULT '',
  `subtitle_en` VARCHAR(255) NOT NULL DEFAULT '',
  `subtitle_zht` VARCHAR(255) NOT NULL DEFAULT '',
  `subtitle_pt` VARCHAR(255) NOT NULL DEFAULT '',
  `description_zh` TEXT NULL,
  `description_en` TEXT NULL,
  `description_zht` TEXT NULL,
  `description_pt` TEXT NULL,
  `highlight_zh` VARCHAR(255) NOT NULL DEFAULT '',
  `highlight_en` VARCHAR(255) NOT NULL DEFAULT '',
  `highlight_zht` VARCHAR(255) NOT NULL DEFAULT '',
  `highlight_pt` VARCHAR(255) NOT NULL DEFAULT '',
  `cover_asset_id` BIGINT NULL,
  `icon_asset_id` BIGINT NULL,
  `animation_asset_id` BIGINT NULL,
  `reward_config_json` JSON NULL,
  `presentation_id` BIGINT NULL,
  `status` VARCHAR(16) NOT NULL DEFAULT 'draft',
  `sort_order` INT NOT NULL DEFAULT 0,
  `publish_start_at` DATETIME NULL,
  `publish_end_at` DATETIME NULL,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY `uk_game_rewards_code` (`code`),
  UNIQUE KEY `uk_game_rewards_legacy` (`legacy_source_type`, `legacy_source_id`),
  KEY `idx_game_rewards_type_status` (`reward_type`, `status`),
  KEY `idx_game_rewards_status_sort` (`status`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `reward_rule_bindings` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `rule_id` BIGINT NOT NULL,
  `owner_domain` VARCHAR(32) NOT NULL,
  `owner_id` BIGINT NOT NULL,
  `owner_code` VARCHAR(128) NOT NULL DEFAULT '',
  `binding_role` VARCHAR(32) NOT NULL DEFAULT 'attached',
  `sort_order` INT NOT NULL DEFAULT 0,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY `uk_reward_rule_bindings_owner` (`rule_id`, `owner_domain`, `owner_id`, `binding_role`),
  KEY `idx_reward_rule_bindings_owner` (`owner_domain`, `owner_id`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `reward_presentations` (
  `code`,
  `name_zh`,
  `name_zht`,
  `presentation_type`,
  `first_time_only`,
  `skippable`,
  `minimum_display_ms`,
  `interrupt_policy`,
  `queue_policy`,
  `priority_weight`,
  `cover_asset_id`,
  `summary_text`,
  `config_json`,
  `status`
)
SELECT
  CONCAT('legacy-reward-', `r`.`id`),
  `r`.`name_zh`,
  COALESCE(NULLIF(`r`.`name_zht`, ''), `r`.`name_zh`),
  CASE
    WHEN LOWER(COALESCE(`r`.`popup_preset_code`, '')) = 'achievement-toast' THEN 'toast'
    WHEN LOWER(COALESCE(`r`.`popup_preset_code`, '')) = 'map-bubble' THEN 'popup_card'
    ELSE 'popup_card'
  END,
  1,
  1,
  1200,
  'queue_after_current',
  'enqueue',
  40,
  `r`.`cover_asset_id`,
  CONCAT('從 legacy rewards 遷移：', `r`.`code`),
  JSON_OBJECT(
    'popupPresetCode', `r`.`popup_preset_code`,
    'popupConfigJson', `r`.`popup_config_json`,
    'displayPresetCode', `r`.`display_preset_code`,
    'displayConfigJson', `r`.`display_config_json`,
    'triggerPresetCode', `r`.`trigger_preset_code`,
    'triggerConfigJson', `r`.`trigger_config_json`,
    'exampleContentZh', `r`.`example_content_zh`,
    'exampleContentEn', `r`.`example_content_en`,
    'exampleContentZht', `r`.`example_content_zht`,
    'exampleContentPt', `r`.`example_content_pt`
  ),
  CASE
    WHEN LOWER(COALESCE(`r`.`status`, '')) IN ('published', 'active', '1') THEN 'published'
    WHEN LOWER(COALESCE(`r`.`status`, '')) IN ('archived', 'inactive', '0') THEN 'archived'
    ELSE 'draft'
  END
FROM `rewards` `r`
WHERE NOT EXISTS (
  SELECT 1
  FROM `reward_presentations` `p`
  WHERE `p`.`code` = CONCAT('legacy-reward-', `r`.`id`)
);

INSERT INTO `reward_presentations` (
  `code`,
  `name_zh`,
  `name_zht`,
  `presentation_type`,
  `first_time_only`,
  `skippable`,
  `minimum_display_ms`,
  `interrupt_policy`,
  `queue_policy`,
  `priority_weight`,
  `cover_asset_id`,
  `summary_text`,
  `config_json`,
  `status`
)
SELECT
  CONCAT('legacy-badge-', `b`.`id`),
  `b`.`name_zh`,
  COALESCE(NULLIF(`b`.`name_zht`, ''), `b`.`name_zh`),
  CASE
    WHEN LOWER(COALESCE(`b`.`popup_preset_code`, '')) = 'achievement-toast' THEN 'toast'
    ELSE 'popup_card'
  END,
  1,
  1,
  1000,
  'queue_after_current',
  'enqueue',
  60,
  COALESCE(`b`.`cover_asset_id`, `b`.`icon_asset_id`),
  CONCAT('從 legacy badges 遷移：', `b`.`badge_code`),
  JSON_OBJECT(
    'popupPresetCode', `b`.`popup_preset_code`,
    'popupConfigJson', `b`.`popup_config_json`,
    'displayPresetCode', `b`.`display_preset_code`,
    'displayConfigJson', `b`.`display_config_json`,
    'triggerPresetCode', `b`.`trigger_preset_code`,
    'triggerConfigJson', `b`.`trigger_config_json`,
    'exampleContentZh', `b`.`example_content_zh`,
    'exampleContentEn', `b`.`example_content_en`,
    'exampleContentZht', `b`.`example_content_zht`,
    'exampleContentPt', `b`.`example_content_pt`
  ),
  CASE
    WHEN LOWER(COALESCE(`b`.`status`, '')) IN ('published', 'active', '1') THEN 'published'
    WHEN LOWER(COALESCE(`b`.`status`, '')) IN ('archived', 'inactive', '0') THEN 'archived'
    ELSE 'draft'
  END
FROM `badges` `b`
WHERE NOT EXISTS (
  SELECT 1
  FROM `reward_presentations` `p`
  WHERE `p`.`code` = CONCAT('legacy-badge-', `b`.`id`)
);

INSERT INTO `reward_presentation_steps` (
  `presentation_id`,
  `step_type`,
  `step_code`,
  `title_text`,
  `asset_id`,
  `duration_ms`,
  `skippable_override`,
  `overlay_config_json`,
  `sort_order`
)
SELECT
  `p`.`id`,
  `p`.`presentation_type`,
  CONCAT(`p`.`code`, '-primary'),
  COALESCE(NULLIF(`p`.`name_zht`, ''), `p`.`name_zh`),
  `p`.`cover_asset_id`,
  `p`.`minimum_display_ms`,
  `p`.`skippable`,
  JSON_OBJECT('source', 'legacy-migration'),
  0
FROM `reward_presentations` `p`
WHERE `p`.`code` LIKE 'legacy-%'
  AND NOT EXISTS (
    SELECT 1
    FROM `reward_presentation_steps` `s`
    WHERE `s`.`presentation_id` = `p`.`id`
  );

INSERT INTO `redeemable_prizes` (
  `code`,
  `legacy_source_type`,
  `legacy_source_id`,
  `prize_type`,
  `fulfillment_mode`,
  `name_zh`,
  `name_en`,
  `name_zht`,
  `name_pt`,
  `subtitle_zh`,
  `subtitle_en`,
  `subtitle_zht`,
  `subtitle_pt`,
  `description_zh`,
  `description_en`,
  `description_zht`,
  `description_pt`,
  `highlight_zh`,
  `highlight_en`,
  `highlight_zht`,
  `highlight_pt`,
  `cover_asset_id`,
  `stamp_cost`,
  `inventory_total`,
  `inventory_redeemed`,
  `stock_policy_json`,
  `fulfillment_config_json`,
  `operator_notes`,
  `presentation_id`,
  `status`,
  `sort_order`,
  `publish_start_at`,
  `publish_end_at`,
  `created_at`,
  `updated_at`
)
SELECT
  `r`.`code`,
  'reward',
  `r`.`id`,
  CASE
    WHEN LOWER(`r`.`code`) LIKE '%voucher%' OR LOWER(`r`.`code`) LIKE '%coupon%' THEN 'coupon'
    WHEN LOWER(`r`.`code`) LIKE '%ticket%' THEN 'ticket'
    WHEN LOWER(`r`.`code`) LIKE '%postcard%' THEN 'postcard'
    WHEN LOWER(`r`.`code`) LIKE '%code%' THEN 'code'
    ELSE 'virtual_item_pack'
  END,
  CASE
    WHEN LOWER(`r`.`code`) LIKE '%voucher%' OR LOWER(`r`.`code`) LIKE '%coupon%' OR LOWER(`r`.`code`) LIKE '%code%' THEN 'voucher_code'
    ELSE 'virtual_issue'
  END,
  `r`.`name_zh`,
  `r`.`name_en`,
  `r`.`name_zht`,
  `r`.`name_pt`,
  `r`.`subtitle_zh`,
  `r`.`subtitle_en`,
  `r`.`subtitle_zht`,
  `r`.`subtitle_pt`,
  `r`.`description_zh`,
  `r`.`description_en`,
  `r`.`description_zht`,
  `r`.`description_pt`,
  `r`.`highlight_zh`,
  `r`.`highlight_en`,
  `r`.`highlight_zht`,
  `r`.`highlight_pt`,
  `r`.`cover_asset_id`,
  COALESCE(`r`.`stamp_cost`, 0),
  COALESCE(`r`.`inventory_total`, 0),
  COALESCE(`r`.`inventory_redeemed`, 0),
  JSON_OBJECT(
    'inventoryTotal', COALESCE(`r`.`inventory_total`, 0),
    'inventoryRedeemed', COALESCE(`r`.`inventory_redeemed`, 0),
    'legacyTable', 'rewards'
  ),
  JSON_OBJECT(
    'popupPresetCode', `r`.`popup_preset_code`,
    'displayPresetCode', `r`.`display_preset_code`,
    'triggerPresetCode', `r`.`trigger_preset_code`
  ),
  'Migrated from legacy rewards table during Phase 23 domain split.',
  (SELECT `p`.`id` FROM `reward_presentations` `p` WHERE `p`.`code` = CONCAT('legacy-reward-', `r`.`id`) LIMIT 1),
  CASE
    WHEN LOWER(COALESCE(`r`.`status`, '')) IN ('published', 'active', '1') THEN 'published'
    WHEN LOWER(COALESCE(`r`.`status`, '')) IN ('archived', 'inactive', '0') THEN 'archived'
    ELSE 'draft'
  END,
  COALESCE(`r`.`sort_order`, 0),
  `r`.`publish_start_at`,
  `r`.`publish_end_at`,
  COALESCE(`r`.`created_at`, CURRENT_TIMESTAMP),
  COALESCE(`r`.`updated_at`, CURRENT_TIMESTAMP)
FROM `rewards` `r`
WHERE NOT EXISTS (
  SELECT 1
  FROM `redeemable_prizes` `rp`
  WHERE `rp`.`legacy_source_type` = 'reward'
    AND `rp`.`legacy_source_id` = `r`.`id`
);

INSERT INTO `game_rewards` (
  `code`,
  `legacy_source_type`,
  `legacy_source_id`,
  `reward_type`,
  `rarity`,
  `stackable`,
  `max_owned`,
  `can_equip`,
  `can_consume`,
  `name_zh`,
  `name_en`,
  `name_zht`,
  `name_pt`,
  `description_zh`,
  `description_en`,
  `description_zht`,
  `description_pt`,
  `cover_asset_id`,
  `icon_asset_id`,
  `animation_asset_id`,
  `reward_config_json`,
  `presentation_id`,
  `status`,
  `sort_order`,
  `publish_start_at`,
  `publish_end_at`,
  `created_at`,
  `updated_at`
)
SELECT
  `b`.`badge_code`,
  'badge',
  `b`.`id`,
  'badge',
  COALESCE(NULLIF(`b`.`rarity`, ''), 'common'),
  0,
  1,
  0,
  0,
  `b`.`name_zh`,
  `b`.`name_en`,
  `b`.`name_zht`,
  `b`.`name_pt`,
  `b`.`description_zh`,
  `b`.`description_en`,
  `b`.`description_zht`,
  `b`.`description_pt`,
  COALESCE(`b`.`cover_asset_id`, `b`.`icon_asset_id`),
  `b`.`icon_asset_id`,
  `b`.`animation_asset_id`,
  JSON_OBJECT(
    'badgeType', `b`.`badge_type`,
    'isHidden', COALESCE(`b`.`is_hidden`, 0),
    'isLimitedTime', COALESCE(`b`.`is_limited_time`, 0),
    'limitedStart', IFNULL(DATE_FORMAT(`b`.`limited_start`, '%Y-%m-%d %H:%i:%s'), NULL),
    'limitedEnd', IFNULL(DATE_FORMAT(`b`.`limited_end`, '%Y-%m-%d %H:%i:%s'), NULL),
    'legacyIconUrl', `b`.`icon_url`,
    'legacyImageUrl', `b`.`image_url`,
    'legacyAnimationUnlock', `b`.`animation_unlock`
  ),
  (SELECT `p`.`id` FROM `reward_presentations` `p` WHERE `p`.`code` = CONCAT('legacy-badge-', `b`.`id`) LIMIT 1),
  CASE
    WHEN LOWER(COALESCE(`b`.`status`, '')) IN ('published', 'active', '1') THEN 'published'
    WHEN LOWER(COALESCE(`b`.`status`, '')) IN ('archived', 'inactive', '0') THEN 'archived'
    ELSE 'draft'
  END,
  0,
  `b`.`limited_start`,
  `b`.`limited_end`,
  COALESCE(`b`.`created_at`, CURRENT_TIMESTAMP),
  COALESCE(`b`.`updated_at`, CURRENT_TIMESTAMP)
FROM `badges` `b`
WHERE NOT EXISTS (
  SELECT 1
  FROM `game_rewards` `gr`
  WHERE `gr`.`legacy_source_type` = 'badge'
    AND `gr`.`legacy_source_id` = `b`.`id`
);

INSERT INTO `content_relation_links` (
  `owner_type`,
  `owner_id`,
  `relation_type`,
  `target_type`,
  `target_id`,
  `target_code`,
  `metadata_json`,
  `sort_order`
)
SELECT
  'redeemable_prize',
  `rp`.`id`,
  `links`.`relation_type`,
  `links`.`target_type`,
  `links`.`target_id`,
  `links`.`target_code`,
  `links`.`metadata_json`,
  `links`.`sort_order`
FROM `content_relation_links` `links`
INNER JOIN `redeemable_prizes` `rp`
  ON `rp`.`legacy_source_type` = 'reward'
 AND `rp`.`legacy_source_id` = `links`.`owner_id`
WHERE `links`.`owner_type` = 'reward'
  AND `links`.`deleted` = 0
ON DUPLICATE KEY UPDATE
  `metadata_json` = VALUES(`metadata_json`),
  `sort_order` = VALUES(`sort_order`),
  `deleted` = 0;

INSERT INTO `content_relation_links` (
  `owner_type`,
  `owner_id`,
  `relation_type`,
  `target_type`,
  `target_id`,
  `target_code`,
  `metadata_json`,
  `sort_order`
)
SELECT
  'game_reward',
  `gr`.`id`,
  `links`.`relation_type`,
  `links`.`target_type`,
  `links`.`target_id`,
  `links`.`target_code`,
  `links`.`metadata_json`,
  `links`.`sort_order`
FROM `content_relation_links` `links`
INNER JOIN `game_rewards` `gr`
  ON `gr`.`legacy_source_type` = 'badge'
 AND `gr`.`legacy_source_id` = `links`.`owner_id`
WHERE `links`.`owner_type` = 'badge'
  AND `links`.`deleted` = 0
ON DUPLICATE KEY UPDATE
  `metadata_json` = VALUES(`metadata_json`),
  `sort_order` = VALUES(`sort_order`),
  `deleted` = 0;

INSERT INTO `seed_runs` (`seed_key`, `description`, `status`, `executed_at`, `notes`)
VALUES (
  'phase23-reward-domain-migration',
  'Phase 23 canonical reward domain migration',
  'completed',
  NOW(),
  'Creates split reward-domain tables, migrates legacy rewards/badges into redeemable_prizes/game_rewards, and clones canonical content bindings.'
)
ON DUPLICATE KEY UPDATE
  `description` = VALUES(`description`),
  `status` = VALUES(`status`),
  `executed_at` = VALUES(`executed_at`),
  `notes` = VALUES(`notes`);

SET FOREIGN_KEY_CHECKS = 1;
