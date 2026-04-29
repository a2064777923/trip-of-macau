-- Phase 32 Plan 32-02: durable story-session persistence foundation.
-- Implements D32-17, D32-18, D32-19, D32-20, and D32-34.
-- All text is UTF-8 / utf8mb4. Do not rewrite this file through non-UTF-8 shell literals.
-- Session status contract: started | exited

USE `aoxiaoyou`;

SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `user_storyline_sessions` (
  `session_id` VARCHAR(96) NOT NULL,
  `user_id` BIGINT NOT NULL,
  `storyline_id` BIGINT NOT NULL,
  `current_chapter_id` BIGINT NULL,
  `status` VARCHAR(32) NOT NULL DEFAULT 'started',
  `started_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `last_event_at` DATETIME NULL,
  `exited_at` DATETIME NULL,
  `event_count` INT NOT NULL DEFAULT 0,
  `temporary_step_state_json` JSON NULL,
  `exit_cleared_temporary_state` TINYINT(1) NOT NULL DEFAULT 0,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`session_id`),
  KEY `idx_user_storyline_sessions_user_status_started` (`user_id`, `status`, `started_at`),
  KEY `idx_user_storyline_sessions_storyline_status` (`storyline_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Durable traveler story-mode session state kept separate from permanent exploration facts';
