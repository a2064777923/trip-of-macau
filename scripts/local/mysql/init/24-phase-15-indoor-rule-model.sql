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

CALL `add_column_if_missing`('indoor_nodes', 'presentation_mode', '`presentation_mode` VARCHAR(32) NOT NULL DEFAULT ''marker'' AFTER `node_type`');
CALL `add_column_if_missing`('indoor_nodes', 'overlay_type', '`overlay_type` VARCHAR(32) NULL AFTER `presentation_mode`');
CALL `add_column_if_missing`('indoor_nodes', 'overlay_geometry_json', '`overlay_geometry_json` JSON NULL AFTER `display_config_json`');
CALL `add_column_if_missing`('indoor_nodes', 'inherit_linked_entity_rules', '`inherit_linked_entity_rules` TINYINT(1) NOT NULL DEFAULT 0 AFTER `overlay_geometry_json`');
CALL `add_column_if_missing`('indoor_nodes', 'runtime_support_level', '`runtime_support_level` VARCHAR(32) NOT NULL DEFAULT ''phase15_storage_only'' AFTER `inherit_linked_entity_rules`');
DROP PROCEDURE IF EXISTS `add_column_if_missing`;

UPDATE `indoor_nodes`
SET
  `presentation_mode` = CASE
    WHEN `presentation_mode` IS NULL OR `presentation_mode` = '' THEN 'marker'
    ELSE `presentation_mode`
  END,
  `inherit_linked_entity_rules` = COALESCE(`inherit_linked_entity_rules`, 0),
  `runtime_support_level` = CASE
    WHEN `runtime_support_level` IS NULL OR `runtime_support_level` = '' THEN 'phase15_storage_only'
    ELSE `runtime_support_level`
  END;

CREATE TABLE IF NOT EXISTS `indoor_node_behaviors` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `node_id` BIGINT NOT NULL,
  `behavior_code` VARCHAR(64) NOT NULL,
  `behavior_name_zh` VARCHAR(255) NULL,
  `behavior_name_en` VARCHAR(255) NULL,
  `behavior_name_zht` VARCHAR(255) NULL,
  `behavior_name_pt` VARCHAR(255) NULL,
  `appearance_preset_code` VARCHAR(64) NULL,
  `trigger_template_code` VARCHAR(64) NULL,
  `effect_template_code` VARCHAR(64) NULL,
  `appearance_rules_json` JSON NULL,
  `trigger_rules_json` JSON NULL,
  `effect_rules_json` JSON NULL,
  `path_graph_json` JSON NULL,
  `inherit_mode` VARCHAR(32) NOT NULL DEFAULT 'override',
  `runtime_support_level` VARCHAR(32) NOT NULL DEFAULT 'phase15_storage_only',
  `sort_order` INT NOT NULL DEFAULT 0,
  `status` VARCHAR(32) NOT NULL DEFAULT 'draft',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_indoor_node_behaviors_node_behavior_code` (`node_id`, `behavior_code`),
  KEY `idx_indoor_node_behaviors_node_id` (`node_id`),
  CONSTRAINT `fk_indoor_node_behaviors_node_id`
    FOREIGN KEY (`node_id`) REFERENCES `indoor_nodes` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Structured indoor node behaviors and authored rule graphs';
