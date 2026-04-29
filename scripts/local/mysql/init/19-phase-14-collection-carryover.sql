USE `aoxiaoyou`;

SET NAMES utf8mb4;

DROP PROCEDURE IF EXISTS `add_column_if_missing`;
DELIMITER $$
CREATE PROCEDURE `add_column_if_missing`(
  IN in_table_name VARCHAR(64),
  IN in_column_name VARCHAR(64),
  IN in_definition TEXT
)
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = in_table_name
      AND COLUMN_NAME = in_column_name
  ) THEN
    SET @ddl = CONCAT('ALTER TABLE `', in_table_name, '` ADD COLUMN ', in_definition);
    PREPARE stmt FROM @ddl;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
  END IF;
END$$
DELIMITER ;

CALL `add_column_if_missing`('collectibles', 'popup_preset_code', '`popup_preset_code` VARCHAR(64) NULL AFTER `display_rule`');
CALL `add_column_if_missing`('collectibles', 'popup_config_json', '`popup_config_json` JSON NULL AFTER `popup_preset_code`');
CALL `add_column_if_missing`('collectibles', 'display_preset_code', '`display_preset_code` VARCHAR(64) NULL AFTER `popup_config_json`');
CALL `add_column_if_missing`('collectibles', 'display_config_json', '`display_config_json` JSON NULL AFTER `display_preset_code`');
CALL `add_column_if_missing`('collectibles', 'trigger_preset_code', '`trigger_preset_code` VARCHAR(64) NULL AFTER `display_config_json`');
CALL `add_column_if_missing`('collectibles', 'trigger_config_json', '`trigger_config_json` JSON NULL AFTER `trigger_preset_code`');
CALL `add_column_if_missing`('collectibles', 'example_content_zh', '`example_content_zh` TEXT NULL AFTER `trigger_config_json`');
CALL `add_column_if_missing`('collectibles', 'example_content_en', '`example_content_en` TEXT NULL AFTER `example_content_zh`');
CALL `add_column_if_missing`('collectibles', 'example_content_zht', '`example_content_zht` TEXT NULL AFTER `example_content_en`');
CALL `add_column_if_missing`('collectibles', 'example_content_pt', '`example_content_pt` TEXT NULL AFTER `example_content_zht`');

CALL `add_column_if_missing`('badges', 'popup_preset_code', '`popup_preset_code` VARCHAR(64) NULL AFTER `animation_unlock`');
CALL `add_column_if_missing`('badges', 'popup_config_json', '`popup_config_json` JSON NULL AFTER `popup_preset_code`');
CALL `add_column_if_missing`('badges', 'display_preset_code', '`display_preset_code` VARCHAR(64) NULL AFTER `popup_config_json`');
CALL `add_column_if_missing`('badges', 'display_config_json', '`display_config_json` JSON NULL AFTER `display_preset_code`');
CALL `add_column_if_missing`('badges', 'trigger_preset_code', '`trigger_preset_code` VARCHAR(64) NULL AFTER `display_config_json`');
CALL `add_column_if_missing`('badges', 'trigger_config_json', '`trigger_config_json` JSON NULL AFTER `trigger_preset_code`');
CALL `add_column_if_missing`('badges', 'example_content_zh', '`example_content_zh` TEXT NULL AFTER `trigger_config_json`');
CALL `add_column_if_missing`('badges', 'example_content_en', '`example_content_en` TEXT NULL AFTER `example_content_zh`');
CALL `add_column_if_missing`('badges', 'example_content_zht', '`example_content_zht` TEXT NULL AFTER `example_content_en`');
CALL `add_column_if_missing`('badges', 'example_content_pt', '`example_content_pt` TEXT NULL AFTER `example_content_zht`');

CALL `add_column_if_missing`('rewards', 'popup_preset_code', '`popup_preset_code` VARCHAR(64) NULL AFTER `cover_asset_id`');
CALL `add_column_if_missing`('rewards', 'popup_config_json', '`popup_config_json` JSON NULL AFTER `popup_preset_code`');
CALL `add_column_if_missing`('rewards', 'display_preset_code', '`display_preset_code` VARCHAR(64) NULL AFTER `popup_config_json`');
CALL `add_column_if_missing`('rewards', 'display_config_json', '`display_config_json` JSON NULL AFTER `display_preset_code`');
CALL `add_column_if_missing`('rewards', 'trigger_preset_code', '`trigger_preset_code` VARCHAR(64) NULL AFTER `display_config_json`');
CALL `add_column_if_missing`('rewards', 'trigger_config_json', '`trigger_config_json` JSON NULL AFTER `trigger_preset_code`');
CALL `add_column_if_missing`('rewards', 'example_content_zh', '`example_content_zh` TEXT NULL AFTER `trigger_config_json`');
CALL `add_column_if_missing`('rewards', 'example_content_en', '`example_content_en` TEXT NULL AFTER `example_content_zh`');
CALL `add_column_if_missing`('rewards', 'example_content_zht', '`example_content_zht` TEXT NULL AFTER `example_content_en`');
CALL `add_column_if_missing`('rewards', 'example_content_pt', '`example_content_pt` TEXT NULL AFTER `example_content_zht`');

DROP PROCEDURE IF EXISTS `add_column_if_missing`;

UPDATE `collectibles`
SET
  `popup_preset_code` = COALESCE(NULLIF(`popup_preset_code`, ''), 'story-modal'),
  `display_preset_code` = COALESCE(NULLIF(`display_preset_code`, ''), 'map-keepsake'),
  `trigger_preset_code` = COALESCE(NULLIF(`trigger_preset_code`, ''), 'story-completion'),
  `example_content_zht` = COALESCE(NULLIF(`example_content_zht`, ''), `description_zht`, `description_zh`),
  `example_content_zh` = COALESCE(NULLIF(`example_content_zh`, ''), `description_zh`, `description_zht`),
  `example_content_en` = COALESCE(NULLIF(`example_content_en`, ''), `description_en`, `description_zh`),
  `example_content_pt` = COALESCE(NULLIF(`example_content_pt`, ''), `description_pt`, `description_en`, `description_zht`);

UPDATE `badges`
SET
  `popup_preset_code` = COALESCE(NULLIF(`popup_preset_code`, ''), 'achievement-toast'),
  `display_preset_code` = COALESCE(NULLIF(`display_preset_code`, ''), 'badge-ribbon'),
  `trigger_preset_code` = COALESCE(NULLIF(`trigger_preset_code`, ''), 'chapter-completion'),
  `example_content_zht` = COALESCE(NULLIF(`example_content_zht`, ''), `description_zht`, `description_zh`),
  `example_content_zh` = COALESCE(NULLIF(`example_content_zh`, ''), `description_zh`, `description_zht`),
  `example_content_en` = COALESCE(NULLIF(`example_content_en`, ''), `description_en`, `description_zh`),
  `example_content_pt` = COALESCE(NULLIF(`example_content_pt`, ''), `description_pt`, `description_en`, `description_zht`);

UPDATE `rewards`
SET
  `popup_preset_code` = COALESCE(NULLIF(`popup_preset_code`, ''), 'reward-modal'),
  `display_preset_code` = COALESCE(NULLIF(`display_preset_code`, ''), 'inventory-card'),
  `trigger_preset_code` = COALESCE(NULLIF(`trigger_preset_code`, ''), 'reward-redemption'),
  `example_content_zht` = COALESCE(NULLIF(`example_content_zht`, ''), `description_zht`, `description_zh`),
  `example_content_zh` = COALESCE(NULLIF(`example_content_zh`, ''), `description_zh`, `description_zht`),
  `example_content_en` = COALESCE(NULLIF(`example_content_en`, ''), `description_en`, `description_zh`),
  `example_content_pt` = COALESCE(NULLIF(`example_content_pt`, ''), `description_pt`, `description_en`, `description_zht`);

INSERT INTO `seed_runs` (`seed_key`, `description`, `status`, `executed_at`, `notes`)
VALUES (
  'phase14-collection-carryover',
  'Phase 14 carryover schema for collectible, badge, and reward preset-driven authoring with indoor bindings',
  'completed',
  NOW(),
  'Adds preset/config/example-content fields for collectibles, badges, and rewards while confirming indoor building/floor relation support through content_relation_links.'
)
ON DUPLICATE KEY UPDATE
  `description` = VALUES(`description`),
  `status` = VALUES(`status`),
  `executed_at` = VALUES(`executed_at`),
  `notes` = VALUES(`notes`);
