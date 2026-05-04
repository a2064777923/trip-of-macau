SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `user_game_reward_grants` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `game_reward_id` BIGINT NOT NULL,
  `rule_id` BIGINT NULL,
  `source_event_id` BIGINT NULL,
  `source_session_id` BIGINT NULL,
  `grant_status` VARCHAR(32) NOT NULL DEFAULT 'granted',
  `grant_reason` VARCHAR(512) NOT NULL DEFAULT '',
  `granted_by` BIGINT NULL,
  `granted_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `idempotency_key` VARCHAR(128) NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY `uk_user_game_reward_grant_idempotency` (`idempotency_key`),
  KEY `idx_user_game_reward_grants_user` (`user_id`, `granted_at`),
  KEY `idx_user_game_reward_grants_reward` (`game_reward_id`, `grant_status`),
  KEY `idx_user_game_reward_grants_source` (`source_event_id`, `rule_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SET @column_exists = (
  SELECT COUNT(*)
  FROM `information_schema`.`columns`
  WHERE `table_schema` = DATABASE()
    AND `table_name` = 'reward_redemptions'
    AND `column_name` = 'source_event_id'
);
SET @ddl = IF(
  @column_exists = 0,
  'ALTER TABLE `reward_redemptions` ADD COLUMN `source_event_id` BIGINT NULL AFTER `expires_at`',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @column_exists = (
  SELECT COUNT(*)
  FROM `information_schema`.`columns`
  WHERE `table_schema` = DATABASE()
    AND `table_name` = 'reward_redemptions'
    AND `column_name` = 'source_rule_id'
);
SET @ddl = IF(
  @column_exists = 0,
  'ALTER TABLE `reward_redemptions` ADD COLUMN `source_rule_id` BIGINT NULL AFTER `source_event_id`',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @column_exists = (
  SELECT COUNT(*)
  FROM `information_schema`.`columns`
  WHERE `table_schema` = DATABASE()
    AND `table_name` = 'reward_redemptions'
    AND `column_name` = 'source_session_id'
);
SET @ddl = IF(
  @column_exists = 0,
  'ALTER TABLE `reward_redemptions` ADD COLUMN `source_session_id` BIGINT NULL AFTER `source_rule_id`',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @column_exists = (
  SELECT COUNT(*)
  FROM `information_schema`.`columns`
  WHERE `table_schema` = DATABASE()
    AND `table_name` = 'reward_redemptions'
    AND `column_name` = 'idempotency_key'
);
SET @ddl = IF(
  @column_exists = 0,
  'ALTER TABLE `reward_redemptions` ADD COLUMN `idempotency_key` VARCHAR(128) NULL AFTER `source_session_id`',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @index_exists = (
  SELECT COUNT(*)
  FROM `information_schema`.`statistics`
  WHERE `table_schema` = DATABASE()
    AND `table_name` = 'reward_redemptions'
    AND `index_name` = 'uk_reward_redemptions_idempotency'
);
SET @ddl = IF(
  @index_exists = 0,
  'ALTER TABLE `reward_redemptions` ADD UNIQUE KEY `uk_reward_redemptions_idempotency` (`idempotency_key`)',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @index_exists = (
  SELECT COUNT(*)
  FROM `information_schema`.`statistics`
  WHERE `table_schema` = DATABASE()
    AND `table_name` = 'user_game_reward_grants'
    AND `index_name` = 'uk_user_game_reward_grant_idempotency'
);
SET @ddl = IF(
  @index_exists = 0,
  'ALTER TABLE `user_game_reward_grants` ADD UNIQUE KEY `uk_user_game_reward_grant_idempotency` (`idempotency_key`)',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
