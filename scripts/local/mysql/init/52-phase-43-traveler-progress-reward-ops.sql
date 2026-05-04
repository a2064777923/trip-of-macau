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
  `reward_config_json`,
  `status`,
  `sort_order`,
  `publish_start_at`,
  `publish_end_at`
)
VALUES (
  'phase43_smoke_game_reward',
  '',
  NULL,
  'badge',
  'rare',
  0,
  1,
  0,
  0,
  'Phase 43 煙測徽章',
  'Phase 43 Smoke Badge',
  'Phase 43 煙測徽章',
  'Emblema de teste Phase 43',
  '支援操作煙測專用',
  'Support-operation smoke fixture',
  '支援操作煙測專用',
  'Fixture para teste de suporte',
  '用於驗證補發獎勵會同步出現在管理端與小程序公開讀取。',
  'Verifies support-granted rewards appear in both admin and public reads.',
  '用於驗證補發獎勵會同步出現在管理端與小程序公開讀取。',
  'Verifica leitura pública e administrativa de recompensas concedidas por suporte.',
  '只應用於本地 Phase 43 煙測。',
  'Local Phase 43 smoke only.',
  '只應用於本地 Phase 43 煙測。',
  'Apenas para teste local Phase 43.',
  JSON_OBJECT('source', 'phase43-smoke', 'supportOnly', TRUE),
  'published',
  4300,
  '2026-01-01 00:00:00',
  '2027-12-31 23:59:59'
)
ON DUPLICATE KEY UPDATE
  `reward_type` = VALUES(`reward_type`),
  `rarity` = VALUES(`rarity`),
  `name_zh` = VALUES(`name_zh`),
  `name_en` = VALUES(`name_en`),
  `name_zht` = VALUES(`name_zht`),
  `name_pt` = VALUES(`name_pt`),
  `description_zh` = VALUES(`description_zh`),
  `description_en` = VALUES(`description_en`),
  `description_zht` = VALUES(`description_zht`),
  `description_pt` = VALUES(`description_pt`),
  `reward_config_json` = VALUES(`reward_config_json`),
  `status` = VALUES(`status`),
  `sort_order` = VALUES(`sort_order`),
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `reward_rules` (
  `code`,
  `rule_type`,
  `status`,
  `name_zh`,
  `name_zht`,
  `summary_text`,
  `advanced_config_json`
)
VALUES (
  'phase43_smoke_resend_rule',
  'support_resend',
  'active',
  'Phase 43 煙測補發規則',
  'Phase 43 煙測補發規則',
  '本地煙測用補發規則，用於驗證支援操作、規則追蹤與公私端獎勵一致性。',
  JSON_OBJECT('source', 'phase43-smoke', 'supportOnly', TRUE)
)
ON DUPLICATE KEY UPDATE
  `rule_type` = VALUES(`rule_type`),
  `status` = VALUES(`status`),
  `name_zh` = VALUES(`name_zh`),
  `name_zht` = VALUES(`name_zht`),
  `summary_text` = VALUES(`summary_text`),
  `advanced_config_json` = VALUES(`advanced_config_json`),
  `updated_at` = CURRENT_TIMESTAMP;
