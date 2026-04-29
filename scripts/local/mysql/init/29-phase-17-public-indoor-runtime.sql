USE `aoxiaoyou`;

SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `indoor_runtime_logs` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `floor_id` BIGINT NOT NULL,
  `node_id` BIGINT NOT NULL,
  `behavior_id` BIGINT NOT NULL,
  `trigger_id` VARCHAR(128) NULL,
  `matched_trigger_id` VARCHAR(128) NULL,
  `event_type` VARCHAR(64) NOT NULL,
  `event_timestamp` DATETIME NULL,
  `relative_x` DECIMAL(10,6) NULL,
  `relative_y` DECIMAL(10,6) NULL,
  `dwell_ms` BIGINT NULL,
  `user_id` BIGINT NULL,
  `client_session_id` VARCHAR(128) NULL,
  `interaction_accepted` TINYINT(1) NOT NULL DEFAULT 0,
  `requires_auth` TINYINT(1) NOT NULL DEFAULT 0,
  `blocked_reason` VARCHAR(64) NULL,
  `effect_categories_json` JSON NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_indoor_runtime_logs_floor_behavior` (`floor_id`, `behavior_id`, `created_at`),
  KEY `idx_indoor_runtime_logs_user` (`user_id`, `created_at`),
  KEY `idx_indoor_runtime_logs_client_session` (`client_session_id`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Authoritative indoor runtime interaction audit logs';

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

CALL `add_column_if_missing`('indoor_runtime_logs', 'matched_trigger_id', '`matched_trigger_id` VARCHAR(128) NULL AFTER `trigger_id`');
CALL `add_column_if_missing`('indoor_runtime_logs', 'event_timestamp', '`event_timestamp` DATETIME NULL AFTER `event_type`');
CALL `add_column_if_missing`('indoor_runtime_logs', 'relative_x', '`relative_x` DECIMAL(10,6) NULL AFTER `event_timestamp`');
CALL `add_column_if_missing`('indoor_runtime_logs', 'relative_y', '`relative_y` DECIMAL(10,6) NULL AFTER `relative_x`');
CALL `add_column_if_missing`('indoor_runtime_logs', 'dwell_ms', '`dwell_ms` BIGINT NULL AFTER `relative_y`');
CALL `add_column_if_missing`('indoor_runtime_logs', 'interaction_accepted', '`interaction_accepted` TINYINT(1) NOT NULL DEFAULT 0 AFTER `client_session_id`');
CALL `add_column_if_missing`('indoor_runtime_logs', 'requires_auth', '`requires_auth` TINYINT(1) NOT NULL DEFAULT 0 AFTER `interaction_accepted`');

DROP PROCEDURE IF EXISTS `add_column_if_missing`;
