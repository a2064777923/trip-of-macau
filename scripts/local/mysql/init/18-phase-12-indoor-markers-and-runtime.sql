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

CALL `add_column_if_missing`('indoor_nodes', 'marker_code', '`marker_code` VARCHAR(64) NULL AFTER `floor_id`');
CALL `add_column_if_missing`('indoor_nodes', 'node_name_en', '`node_name_en` VARCHAR(256) NULL AFTER `node_name_zh`');
CALL `add_column_if_missing`('indoor_nodes', 'node_name_zht', '`node_name_zht` VARCHAR(256) NULL AFTER `node_name_en`');
CALL `add_column_if_missing`('indoor_nodes', 'node_name_pt', '`node_name_pt` VARCHAR(256) NULL AFTER `node_name_zht`');
CALL `add_column_if_missing`('indoor_nodes', 'description_en', '`description_en` TEXT NULL AFTER `description_zh`');
CALL `add_column_if_missing`('indoor_nodes', 'description_zht', '`description_zht` TEXT NULL AFTER `description_en`');
CALL `add_column_if_missing`('indoor_nodes', 'description_pt', '`description_pt` TEXT NULL AFTER `description_zht`');
CALL `add_column_if_missing`('indoor_nodes', 'relative_x', '`relative_x` DECIMAL(8,6) NULL AFTER `position_y`');
CALL `add_column_if_missing`('indoor_nodes', 'relative_y', '`relative_y` DECIMAL(8,6) NULL AFTER `relative_x`');
CALL `add_column_if_missing`('indoor_nodes', 'icon_asset_id', '`icon_asset_id` BIGINT NULL AFTER `related_poi_id`');
CALL `add_column_if_missing`('indoor_nodes', 'animation_asset_id', '`animation_asset_id` BIGINT NULL AFTER `icon_asset_id`');
CALL `add_column_if_missing`('indoor_nodes', 'linked_entity_type', '`linked_entity_type` VARCHAR(64) NULL AFTER `animation_asset_id`');
CALL `add_column_if_missing`('indoor_nodes', 'linked_entity_id', '`linked_entity_id` BIGINT NULL AFTER `linked_entity_type`');
CALL `add_column_if_missing`('indoor_nodes', 'popup_config_json', '`popup_config_json` JSON NULL AFTER `tags`');
CALL `add_column_if_missing`('indoor_nodes', 'display_config_json', '`display_config_json` JSON NULL AFTER `popup_config_json`');
CALL `add_column_if_missing`('indoor_nodes', 'metadata_json', '`metadata_json` JSON NULL AFTER `display_config_json`');
CALL `add_column_if_missing`('indoor_nodes', 'import_batch_id', '`import_batch_id` BIGINT NULL AFTER `metadata_json`');
CALL `add_column_if_missing`('indoor_nodes', 'sort_order', '`sort_order` INT NOT NULL DEFAULT 0 AFTER `import_batch_id`');
DROP PROCEDURE IF EXISTS `add_column_if_missing`;

ALTER TABLE `indoor_nodes`
  MODIFY COLUMN `status` VARCHAR(32) NOT NULL DEFAULT 'draft';

UPDATE `indoor_nodes`
SET
  `marker_code` = COALESCE(NULLIF(`marker_code`, ''), CONCAT('indoor-node-', `id`)),
  `node_name_en` = COALESCE(NULLIF(`node_name_en`, ''), `node_name_zh`),
  `node_name_zht` = COALESCE(NULLIF(`node_name_zht`, ''), `node_name_zh`),
  `node_name_pt` = COALESCE(NULLIF(`node_name_pt`, ''), `node_name_zh`),
  `description_en` = COALESCE(NULLIF(`description_en`, ''), `description_zh`),
  `description_zht` = COALESCE(NULLIF(`description_zht`, ''), `description_zh`),
  `description_pt` = COALESCE(NULLIF(`description_pt`, ''), `description_zh`),
  `relative_x` = COALESCE(`relative_x`, CASE WHEN `position_x` BETWEEN 0 AND 1 THEN `position_x` ELSE NULL END),
  `relative_y` = COALESCE(`relative_y`, CASE WHEN `position_y` BETWEEN 0 AND 1 THEN `position_y` ELSE NULL END),
  `sort_order` = COALESCE(`sort_order`, `id`),
  `status` = CASE
    WHEN `status` IN ('1', 'active', 'published') THEN 'published'
    WHEN `status` IN ('0', 'disabled', 'archived') THEN 'archived'
    ELSE 'draft'
  END;

CREATE TABLE IF NOT EXISTS `indoor_node_import_batches` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `floor_id` BIGINT NOT NULL,
  `source_filename` VARCHAR(255) NULL,
  `total_rows` INT NOT NULL DEFAULT 0,
  `valid_rows` INT NOT NULL DEFAULT 0,
  `invalid_rows` INT NOT NULL DEFAULT 0,
  `preview_payload_json` JSON NULL,
  `created_by_admin_id` BIGINT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_indoor_node_import_batches_floor_id` (`floor_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Indoor marker CSV import batches';
