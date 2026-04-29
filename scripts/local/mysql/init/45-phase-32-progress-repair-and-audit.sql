SET NAMES utf8mb4;

SET @column_exists = (
  SELECT COUNT(*)
  FROM `information_schema`.`columns`
  WHERE `table_schema` = DATABASE()
    AND `table_name` = 'user_exploration_events'
    AND `column_name` = 'duplicate_marked'
);
SET @ddl = IF(
  @column_exists = 0,
  'ALTER TABLE `user_exploration_events` ADD COLUMN `duplicate_marked` TINYINT(1) NOT NULL DEFAULT 0 AFTER `event_payload_json`',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @column_exists = (
  SELECT COUNT(*)
  FROM `information_schema`.`columns`
  WHERE `table_schema` = DATABASE()
    AND `table_name` = 'user_exploration_events'
    AND `column_name` = 'duplicate_of_event_id'
);
SET @ddl = IF(
  @column_exists = 0,
  'ALTER TABLE `user_exploration_events` ADD COLUMN `duplicate_of_event_id` BIGINT NULL AFTER `duplicate_marked`',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @column_exists = (
  SELECT COUNT(*)
  FROM `information_schema`.`columns`
  WHERE `table_schema` = DATABASE()
    AND `table_name` = 'user_exploration_events'
    AND `column_name` = 'repair_note_json'
);
SET @ddl = IF(
  @column_exists = 0,
  'ALTER TABLE `user_exploration_events` ADD COLUMN `repair_note_json` JSON NULL AFTER `duplicate_of_event_id`',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @index_exists = (
  SELECT COUNT(*)
  FROM `information_schema`.`statistics`
  WHERE `table_schema` = DATABASE()
    AND `table_name` = 'user_exploration_events'
    AND `index_name` = 'idx_user_exploration_events_duplicate'
);
SET @ddl = IF(
  @index_exists = 0,
  'ALTER TABLE `user_exploration_events` ADD KEY `idx_user_exploration_events_duplicate` (`user_id`, `duplicate_marked`, `duplicate_of_event_id`)',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

CREATE TABLE IF NOT EXISTS `user_progress_operation_audits` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `operator_id` BIGINT NOT NULL,
  `operator_name` VARCHAR(128) NOT NULL,
  `target_user_id` BIGINT NOT NULL,
  `scope_type` VARCHAR(48) NOT NULL,
  `scope_id` BIGINT NULL,
  `storyline_id` BIGINT NULL,
  `action_type` VARCHAR(64) NOT NULL,
  `preview_token_hash` VARCHAR(128) NOT NULL DEFAULT '',
  `preview_summary_json` JSON NOT NULL,
  `result_summary_json` JSON NULL,
  `reason` VARCHAR(512) NOT NULL DEFAULT '',
  `request_ip` VARCHAR(64) NOT NULL DEFAULT '',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY `idx_user_progress_operation_audits_target` (`target_user_id`, `created_at`),
  KEY `idx_user_progress_operation_audits_action` (`action_type`, `created_at`),
  KEY `idx_user_progress_operation_audits_operator` (`operator_id`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Preview-first recompute and repair audit trail for derived user progress';
