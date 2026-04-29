SET NAMES utf8mb4;
USE `aoxiaoyou`;

DROP PROCEDURE IF EXISTS `ensure_column`;
DELIMITER $$
CREATE PROCEDURE `ensure_column`(
  IN p_table_name VARCHAR(64),
  IN p_column_name VARCHAR(64),
  IN p_alter_sql LONGTEXT
)
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = p_table_name
      AND COLUMN_NAME = p_column_name
  ) THEN
    SET @ddl = p_alter_sql;
    PREPARE stmt FROM @ddl;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
  END IF;
END$$
DELIMITER ;

CALL ensure_column('ai_provider_inventory', 'provider_voice_code', 'ALTER TABLE `ai_provider_inventory` ADD COLUMN `provider_voice_code` VARCHAR(255) NULL AFTER `external_id`');
CALL ensure_column('ai_provider_inventory', 'parent_inventory_code', 'ALTER TABLE `ai_provider_inventory` ADD COLUMN `parent_inventory_code` VARCHAR(128) NULL AFTER `provider_voice_code`');
CALL ensure_column('ai_provider_inventory', 'language_codes_json', 'ALTER TABLE `ai_provider_inventory` ADD COLUMN `language_codes_json` LONGTEXT NULL AFTER `capability_codes_json`');
CALL ensure_column('ai_provider_inventory', 'preview_url', 'ALTER TABLE `ai_provider_inventory` ADD COLUMN `preview_url` VARCHAR(1024) NULL AFTER `audio_price_per_minute`');
CALL ensure_column('ai_provider_inventory', 'preview_text', 'ALTER TABLE `ai_provider_inventory` ADD COLUMN `preview_text` VARCHAR(1024) NULL AFTER `preview_url`');
CALL ensure_column('ai_provider_inventory', 'owner_admin_id', 'ALTER TABLE `ai_provider_inventory` ADD COLUMN `owner_admin_id` BIGINT NULL AFTER `preview_text`');
CALL ensure_column('ai_provider_inventory', 'owner_admin_name', 'ALTER TABLE `ai_provider_inventory` ADD COLUMN `owner_admin_name` VARCHAR(128) NULL AFTER `owner_admin_id`');
CALL ensure_column('ai_provider_inventory', 'source_asset_id', 'ALTER TABLE `ai_provider_inventory` ADD COLUMN `source_asset_id` BIGINT NULL AFTER `owner_admin_name`');
CALL ensure_column('ai_provider_inventory', 'clone_status', 'ALTER TABLE `ai_provider_inventory` ADD COLUMN `clone_status` VARCHAR(32) NULL AFTER `source_asset_id`');
CALL ensure_column('ai_provider_inventory', 'last_verified_at', 'ALTER TABLE `ai_provider_inventory` ADD COLUMN `last_verified_at` DATETIME NULL AFTER `clone_status`');

DROP PROCEDURE IF EXISTS `ensure_column`;
