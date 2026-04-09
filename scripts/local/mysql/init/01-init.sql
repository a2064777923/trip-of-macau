-- 本地开发 MySQL 初始化脚本（与当前 admin-backend 实体/查询对齐）
-- 数据库名：aoxiaoyou
-- 默认账号密码：root / root

CREATE DATABASE IF NOT EXISTS `aoxiaoyou` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `aoxiaoyou`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE TABLE IF NOT EXISTS `users` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `open_id` VARCHAR(64) NOT NULL,
  `nickname` VARCHAR(64),
  `avatar_url` VARCHAR(512),
  `language_preference` VARCHAR(16) DEFAULT 'zh_CN',
  `level` INT DEFAULT 1,
  `title` VARCHAR(64) DEFAULT '探索新手',
  `total_stamps` INT DEFAULT 0,
  `interface_mode` VARCHAR(32) DEFAULT 'standard',
  `font_scale` DECIMAL(3,1) DEFAULT 1.0,
  `high_contrast` TINYINT(1) DEFAULT 0,
  `voice_guide_enabled` TINYINT(1) DEFAULT 0,
  `simplified_mode` TINYINT(1) DEFAULT 0,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT DEFAULT 0,
  UNIQUE KEY `uk_users_open_id` (`open_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `cities` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `code` VARCHAR(32) NOT NULL,
  `name_zh` VARCHAR(128) NOT NULL,
  `name_en` VARCHAR(128),
  `name_zht` VARCHAR(128),
  `country_code` VARCHAR(16),
  `center_lat` DECIMAL(10,7),
  `center_lng` DECIMAL(10,7),
  `bounds_geojson` LONGTEXT,
  `default_zoom` INT,
  `unlock_type` VARCHAR(32),
  `unlock_condition` TEXT,
  `cover_image_url` VARCHAR(512),
  `banner_url` VARCHAR(512),
  `description_zh` TEXT,
  `sort_order` INT DEFAULT 0,
  `status` VARCHAR(16) DEFAULT '0',
  `published_at` DATETIME,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT DEFAULT 0,
  UNIQUE KEY `uk_cities_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `pois` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `name_zh` VARCHAR(128) NOT NULL,
  `name_en` VARCHAR(128),
  `name_zht` VARCHAR(128),
  `subtitle` VARCHAR(255),
  `region_code` VARCHAR(64),
  `poi_type` VARCHAR(64),
  `latitude` DECIMAL(10,7),
  `longitude` DECIMAL(10,7),
  `address` VARCHAR(255),
  `category_id` BIGINT,
  `trigger_radius` INT DEFAULT 30,
  `check_in_method` VARCHAR(32),
  `importance` VARCHAR(32),
  `story_line_id` BIGINT,
  `stamp_type` VARCHAR(32),
  `description` TEXT,
  `cover_image_url` VARCHAR(512),
  `image_urls` JSON,
  `audio_guide_url` VARCHAR(512),
  `video_url` VARCHAR(512),
  `ar_content_url` VARCHAR(512),
  `tags` JSON,
  `difficulty` VARCHAR(32),
  `open_time` VARCHAR(128),
  `suggested_visit_minutes` INT,
  `status` VARCHAR(16) DEFAULT 'draft',
  `check_in_count` BIGINT DEFAULT 0,
  `favorite_count` BIGINT DEFAULT 0,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `story_lines` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `code` VARCHAR(64) NOT NULL,
  `name_zh` VARCHAR(128) NOT NULL,
  `name_en` VARCHAR(128),
  `description` TEXT,
  `cover_url` VARCHAR(512),
  `banner_url` VARCHAR(512),
  `category` VARCHAR(64),
  `difficulty` VARCHAR(32),
  `estimated_duration_minutes` INT,
  `tags` JSON,
  `total_chapters` INT DEFAULT 0,
  `status` VARCHAR(16) DEFAULT 'draft',
  `publish_at` DATETIME,
  `start_at` DATETIME,
  `end_at` DATETIME,
  `participation_count` INT DEFAULT 0,
  `completion_count` INT DEFAULT 0,
  `average_completion_time` INT,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT DEFAULT 0,
  UNIQUE KEY `uk_story_lines_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `story_chapters` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `story_line_id` BIGINT NOT NULL,
  `chapter_order` INT NOT NULL,
  `title_zh` VARCHAR(128) NOT NULL,
  `title_en` VARCHAR(128),
  `title_zht` VARCHAR(128),
  `media_type` VARCHAR(32),
  `media_url` VARCHAR(512),
  `script_zh` LONGTEXT,
  `script_en` LONGTEXT,
  `script_zht` LONGTEXT,
  `unlock_type` VARCHAR(32),
  `unlock_param` TEXT,
  `duration` INT,
  `_openid` VARCHAR(64) DEFAULT '',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT DEFAULT 0,
  KEY `idx_story_chapters_story_line_id` (`story_line_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `trigger_logs` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `user_id` BIGINT,
  `poi_id` BIGINT,
  `trigger_type` VARCHAR(32),
  `distance` DECIMAL(10,2),
  `gps_accuracy` DECIMAL(10,2),
  `wifi_used` TINYINT(1) DEFAULT 0,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `rewards` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `name_zh` VARCHAR(128) NOT NULL,
  `description` TEXT,
  `stamps_required` INT DEFAULT 0,
  `total_quantity` INT DEFAULT 0,
  `redeemed_count` INT DEFAULT 0,
  `start_time` DATETIME,
  `end_time` DATETIME,
  `status` VARCHAR(16) DEFAULT 'inactive',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `activities` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `code` VARCHAR(64) NOT NULL,
  `title` VARCHAR(128) NOT NULL,
  `description` TEXT,
  `cover_url` VARCHAR(512),
  `start_time` DATETIME,
  `end_time` DATETIME,
  `status` VARCHAR(16) DEFAULT 'draft',
  `participation_count` INT DEFAULT 0,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT DEFAULT 0,
  UNIQUE KEY `uk_activities_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `map_tile_configs` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `map_id` VARCHAR(64) NOT NULL,
  `style` VARCHAR(32) DEFAULT 'cartoon',
  `cdn_base` VARCHAR(512),
  `control_points_url` VARCHAR(512),
  `pois_url` VARCHAR(512),
  `zoom_min` INT,
  `zoom_max` INT,
  `center_lat` DECIMAL(10,7),
  `center_lng` DECIMAL(10,7),
  `default_zoom` INT,
  `status` VARCHAR(16) DEFAULT 'active',
  `version` VARCHAR(64),
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT DEFAULT 0,
  UNIQUE KEY `uk_map_tile_configs_map_id` (`map_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `sys_admin` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `username` VARCHAR(64) NOT NULL,
  `password` VARCHAR(255) NOT NULL,
  `nickname` VARCHAR(64),
  `email` VARCHAR(128),
  `phone` VARCHAR(32),
  `avatar_url` VARCHAR(512),
  `status` VARCHAR(16) DEFAULT 'active',
  `last_login_at` DATETIME,
  `last_login_ip` VARCHAR(64),
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT DEFAULT 0,
  UNIQUE KEY `uk_sys_admin_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `admin_users` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `username` VARCHAR(64) NOT NULL,
  `password_hash` VARCHAR(255) NOT NULL,
  `display_name` VARCHAR(64),
  `email` VARCHAR(128),
  `phone` VARCHAR(32),
  `avatar_url` VARCHAR(512),
  `department` VARCHAR(64),
  `is_superuser` TINYINT DEFAULT 0,
  `status` VARCHAR(16) DEFAULT 'active',
  `last_login_at` DATETIME,
  `last_login_ip` VARCHAR(64),
  `password_changed_at` DATETIME,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT DEFAULT 0,
  UNIQUE KEY `uk_admin_users_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `sys_login_log` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `admin_id` BIGINT,
  `username` VARCHAR(64),
  `ip` VARCHAR(64),
  `user_agent` VARCHAR(512),
  `login_status` VARCHAR(16),
  `fail_reason` VARCHAR(255),
  `_openid` VARCHAR(64) DEFAULT '',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `sys_operation_log` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `admin_id` BIGINT,
  `admin_username` VARCHAR(64),
  `module` VARCHAR(64),
  `operation` VARCHAR(128),
  `request_method` VARCHAR(16),
  `request_url` VARCHAR(512),
  `request_params` LONGTEXT,
  `response_data` LONGTEXT,
  `ip` VARCHAR(64),
  `_openid` VARCHAR(64) DEFAULT '',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `sys_config` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `config_key` VARCHAR(128) NOT NULL,
  `config_value` TEXT,
  `config_type` VARCHAR(32) DEFAULT 'string',
  `description` VARCHAR(255),
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT DEFAULT 0,
  UNIQUE KEY `uk_sys_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `permissions` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `perm_code` VARCHAR(128) NOT NULL,
  `perm_name` VARCHAR(128) NOT NULL,
  `module` VARCHAR(64),
  `perm_type` VARCHAR(32),
  `parent_id` BIGINT DEFAULT 0,
  `path` VARCHAR(255),
  `method` VARCHAR(16),
  `description` VARCHAR(255),
  `sort_order` INT DEFAULT 0,
  `status` VARCHAR(16) DEFAULT '1',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT DEFAULT 0,
  UNIQUE KEY `uk_permissions_perm_code` (`perm_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `roles` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `role_code` VARCHAR(64) NOT NULL,
  `role_name` VARCHAR(128) NOT NULL,
  `description` VARCHAR(255),
  `is_system` TINYINT DEFAULT 0,
  `sort_order` INT DEFAULT 99,
  `status` VARCHAR(16) DEFAULT '1',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT DEFAULT 0,
  UNIQUE KEY `uk_roles_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `role_permissions` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `role_id` BIGINT NOT NULL,
  `permission_id` BIGINT NOT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `_openid` VARCHAR(64) DEFAULT '',
  UNIQUE KEY `uk_role_permissions` (`role_id`, `permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `test_accounts` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `test_group` VARCHAR(64),
  `mock_latitude` DOUBLE,
  `mock_longitude` DOUBLE,
  `mock_enabled` TINYINT(1) DEFAULT 0,
  `mock_poi_id` BIGINT,
  `notes` TEXT,
  `_openid` VARCHAR(64) DEFAULT '',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT DEFAULT 0,
  UNIQUE KEY `uk_test_accounts_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `buildings` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `city_code` VARCHAR(32),
  `building_code` VARCHAR(64) NOT NULL,
  `name_zh` VARCHAR(128) NOT NULL,
  `address_zh` VARCHAR(255),
  `lat` DECIMAL(10,7),
  `lng` DECIMAL(10,7),
  `total_floors` INT DEFAULT 1,
  `basement_floors` INT DEFAULT 0,
  `cover_image_url` VARCHAR(512),
  `description_zh` TEXT,
  `poi_id` BIGINT,
  `status` VARCHAR(16) DEFAULT '1',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT DEFAULT 0,
  UNIQUE KEY `uk_buildings_building_code` (`building_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `collectibles` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `collectible_code` VARCHAR(64) NOT NULL,
  `name_zh` VARCHAR(128) NOT NULL,
  `name_en` VARCHAR(128),
  `description_zh` TEXT,
  `collectible_type` VARCHAR(32),
  `rarity` VARCHAR(32),
  `image_url` VARCHAR(512),
  `animation_url` VARCHAR(512),
  `series_id` BIGINT,
  `acquisition_source` VARCHAR(64),
  `bind_condition` TEXT,
  `display_rule` TEXT,
  `is_repeatable` TINYINT DEFAULT 0,
  `is_limited` TINYINT DEFAULT 0,
  `limited_start` DATETIME,
  `limited_end` DATETIME,
  `cross_city` TINYINT DEFAULT 0,
  `max_ownership` INT DEFAULT 1,
  `status` VARCHAR(16) DEFAULT '1',
  `sort_order` INT DEFAULT 0,
  `deleted_at` DATETIME,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT DEFAULT 0,
  UNIQUE KEY `uk_collectibles_code` (`collectible_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `badges` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `badge_code` VARCHAR(64) NOT NULL,
  `name_zh` VARCHAR(128) NOT NULL,
  `description_zh` TEXT,
  `icon_url` VARCHAR(512),
  `badge_type` VARCHAR(32),
  `rarity` VARCHAR(32),
  `is_hidden` TINYINT DEFAULT 0,
  `is_limited_time` TINYINT DEFAULT 0,
  `limited_start` DATETIME,
  `limited_end` DATETIME,
  `image_url` VARCHAR(512),
  `animation_unlock` VARCHAR(512),
  `status` VARCHAR(16) DEFAULT '1',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT DEFAULT 0,
  UNIQUE KEY `uk_badges_code` (`badge_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `ai_provider_configs` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `provider_name` VARCHAR(64) NOT NULL,
  `display_name` VARCHAR(128) NOT NULL,
  `api_base_url` VARCHAR(512),
  `api_key_encrypted` VARCHAR(512),
  `api_secret_encrypted` VARCHAR(512),
  `model_name` VARCHAR(128),
  `capabilities` JSON,
  `request_timeout_ms` INT DEFAULT 30000,
  `max_retries` INT DEFAULT 1,
  `quota_daily` INT DEFAULT 1000,
  `cost_per_1k_tokens` DECIMAL(10,6) DEFAULT 0,
  `status` TINYINT DEFAULT 1,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT DEFAULT 0,
  UNIQUE KEY `uk_ai_provider_name` (`provider_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `ai_navigation_policies` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `policy_name` VARCHAR(128) NOT NULL,
  `scenario_code` VARCHAR(128) NOT NULL,
  `policy_type` VARCHAR(64),
  `scenario_group` VARCHAR(64),
  `provider_id` BIGINT,
  `prompt_template` LONGTEXT,
  `system_prompt` LONGTEXT,
  `model_override` VARCHAR(128),
  `multimodal_enabled` TINYINT DEFAULT 0,
  `voice_enabled` TINYINT DEFAULT 0,
  `temperature` DECIMAL(4,2),
  `max_tokens` INT,
  `response_schema` LONGTEXT,
  `post_process_rules` LONGTEXT,
  `fallback_policy_id` BIGINT,
  `status` TINYINT DEFAULT 1,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `ai_request_logs` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `provider_id` BIGINT,
  `policy_id` BIGINT,
  `user_openid` VARCHAR(64),
  `request_type` VARCHAR(32),
  `input_data_hash` VARCHAR(64),
  `output_summary` LONGTEXT,
  `latency_ms` INT,
  `tokens_used` INT,
  `cost_usd` DECIMAL(10,6),
  `success` TINYINT DEFAULT 1,
  `error_message` LONGTEXT,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `sys_admin` (`username`, `password`, `nickname`, `email`, `status`)
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '超级管理员', 'admin@tripofmacau.local', 'active')
ON DUPLICATE KEY UPDATE `nickname` = VALUES(`nickname`), `status` = VALUES(`status`);

INSERT INTO `roles` (`role_code`, `role_name`, `description`, `is_system`, `sort_order`, `status`)
VALUES
  ('super_admin', '超级管理员', '拥有所有权限', 1, 1, '1'),
  ('content_admin', '内容运营', '内容管理、故事线管理', 0, 10, '1'),
  ('operation_admin', '运营人员', '活动管理、奖励配置', 0, 20, '1'),
  ('tester', '测试人员', '测试工具、数据调整', 0, 30, '1')
ON DUPLICATE KEY UPDATE `role_name` = VALUES(`role_name`), `description` = VALUES(`description`), `status` = VALUES(`status`);

INSERT INTO `permissions` (`perm_code`, `perm_name`, `module`, `perm_type`, `parent_id`, `path`, `method`, `description`, `sort_order`, `status`)
VALUES
  ('dashboard:view', '查看仪表盘', 'dashboard', 'menu', 0, '/dashboard', 'GET', '查看后台仪表盘', 1, '1'),
  ('poi:view', '查看 POI', 'poi', 'api', 0, '/api/admin/v1/pois', 'GET', '查看景点列表', 10, '1'),
  ('poi:create', '创建 POI', 'poi', 'api', 0, '/api/admin/v1/pois', 'POST', '创建景点', 11, '1'),
  ('poi:update', '更新 POI', 'poi', 'api', 0, '/api/admin/v1/pois/{id}', 'PUT', '更新景点', 12, '1'),
  ('poi:delete', '删除 POI', 'poi', 'api', 0, '/api/admin/v1/pois/{id}', 'DELETE', '删除景点', 13, '1'),
  ('storyline:view', '查看故事线', 'storyline', 'api', 0, '/api/admin/v1/storylines', 'GET', '查看故事线', 20, '1'),
  ('storyline:create', '创建故事线', 'storyline', 'api', 0, '/api/admin/v1/storylines', 'POST', '创建故事线', 21, '1'),
  ('storyline:update', '更新故事线', 'storyline', 'api', 0, '/api/admin/v1/storylines/{id}', 'PUT', '更新故事线', 22, '1'),
  ('user:view', '查看用户', 'user', 'api', 0, '/api/admin/v1/users', 'GET', '查看小程序用户', 30, '1'),
  ('user:test-flag', '标记测试账号', 'user', 'api', 0, '/api/admin/v1/users/{id}/test-flag', 'POST', '设置测试账号标记', 31, '1'),
  ('test-console:view', '查看测试台', 'test-console', 'api', 0, '/api/admin/v1/test-console/accounts', 'GET', '查看测试台账号', 40, '1'),
  ('test-console:operate', '操作测试台', 'test-console', 'api', 0, '/api/admin/v1/test-console/accounts/{id}', 'POST', '执行测试操作', 41, '1')
ON DUPLICATE KEY UPDATE `perm_name` = VALUES(`perm_name`), `description` = VALUES(`description`), `status` = VALUES(`status`);

INSERT INTO `role_permissions` (`role_id`, `permission_id`, `created_at`, `_openid`)
SELECT r.id, p.id, NOW(), ''
FROM `roles` r
CROSS JOIN `permissions` p
WHERE r.role_code = 'super_admin'
  AND NOT EXISTS (
    SELECT 1 FROM `role_permissions` rp WHERE rp.role_id = r.id AND rp.permission_id = p.id
  );

INSERT INTO `cities` (`code`, `name_zh`, `name_en`, `name_zht`, `country_code`, `center_lat`, `center_lng`, `default_zoom`, `unlock_type`, `cover_image_url`, `banner_url`, `description_zh`, `sort_order`, `status`, `published_at`)
VALUES ('macau', '澳门', 'Macau', '澳門', 'MO', 22.198745, 113.543873, 13, 'default', 'https://example.com/macau-cover.png', 'https://example.com/macau-banner.png', '本地开发默认城市数据', 1, '1', NOW())
ON DUPLICATE KEY UPDATE `name_zh` = VALUES(`name_zh`), `status` = VALUES(`status`);

INSERT INTO `map_tile_configs` (`map_id`, `style`, `cdn_base`, `control_points_url`, `pois_url`, `zoom_min`, `zoom_max`, `center_lat`, `center_lng`, `default_zoom`, `status`, `version`)
VALUES ('macau_peninsula', 'cartoon', 'http://127.0.0.1:9000/maps/macau', 'http://127.0.0.1:9000/maps/macau/control-points.json', 'http://127.0.0.1:9000/maps/macau/pois.json', 11, 18, 22.198745, 113.543873, 13, 'active', 'local-dev-v1')
ON DUPLICATE KEY UPDATE `cdn_base` = VALUES(`cdn_base`), `version` = VALUES(`version`), `status` = VALUES(`status`);

INSERT INTO `story_lines` (`code`, `name_zh`, `name_en`, `description`, `cover_url`, `banner_url`, `category`, `difficulty`, `estimated_duration_minutes`, `tags`, `total_chapters`, `status`, `publish_at`, `participation_count`, `completion_count`, `average_completion_time`)
VALUES
  ('silk_road', '海上丝路', 'Maritime Silk Road', '探索澳门在海上丝绸之路中的历史角色', 'https://example.com/story/silk-road-cover.png', 'https://example.com/story/silk-road-banner.png', 'history', 'medium', 90, JSON_ARRAY('历史', '港口', '贸易'), 2, 'published', NOW(), 0, 0, 0),
  ('east_west', '东西方战事', 'East-West Encounter', '体验澳门四百年东西方文化交流', 'https://example.com/story/east-west-cover.png', 'https://example.com/story/east-west-banner.png', 'culture', 'easy', 60, JSON_ARRAY('文化', '探索'), 1, 'published', NOW(), 0, 0, 0)
ON DUPLICATE KEY UPDATE `name_zh` = VALUES(`name_zh`), `status` = VALUES(`status`);

INSERT INTO `pois` (`name_zh`, `name_en`, `name_zht`, `subtitle`, `region_code`, `poi_type`, `latitude`, `longitude`, `address`, `trigger_radius`, `check_in_method`, `importance`, `stamp_type`, `description`, `cover_image_url`, `image_urls`, `tags`, `difficulty`, `open_time`, `suggested_visit_minutes`, `status`, `check_in_count`, `favorite_count`)
VALUES
  ('大三巴牌坊', 'Ruins of St. Paul''s', '大三巴牌坊', '澳门地标', 'macau-peninsula', 'landmark', 22.197589, 113.540512, '澳门大三巴街', 50, 'gps', 'very_important', 'location', '本地开发默认 POI 数据', 'https://example.com/poi/stpaul-cover.png', JSON_ARRAY('https://example.com/poi/stpaul-1.png'), JSON_ARRAY('地标', '历史'), 'easy', '10:00-18:00', 40, 'published', 0, 0)
ON DUPLICATE KEY UPDATE `name_en` = VALUES(`name_en`), `status` = VALUES(`status`);

INSERT INTO `buildings` (`city_code`, `building_code`, `name_zh`, `address_zh`, `lat`, `lng`, `total_floors`, `basement_floors`, `cover_image_url`, `description_zh`, `status`)
VALUES ('macau', 'bldg_sjm_museum', '新马路室内馆', '澳门新马路示例地址', 22.193100, 113.541900, 4, 1, 'https://example.com/building/cover.png', '本地开发用示例建筑', '1')
ON DUPLICATE KEY UPDATE `name_zh` = VALUES(`name_zh`), `status` = VALUES(`status`);

INSERT INTO `collectibles` (`collectible_code`, `name_zh`, `name_en`, `description_zh`, `collectible_type`, `rarity`, `image_url`, `acquisition_source`, `is_repeatable`, `is_limited`, `cross_city`, `max_ownership`, `status`, `sort_order`)
VALUES ('item_macau_postcard', '澳门明信片', 'Macau Postcard', '完成指定探索任务后获得', 'postcard', 'rare', 'https://example.com/collectibles/postcard.png', 'storyline', 0, 0, 0, 1, '1', 1)
ON DUPLICATE KEY UPDATE `name_zh` = VALUES(`name_zh`), `status` = VALUES(`status`);

INSERT INTO `badges` (`badge_code`, `name_zh`, `description_zh`, `icon_url`, `badge_type`, `rarity`, `is_hidden`, `is_limited_time`, `image_url`, `status`)
VALUES ('badge_macau_explorer', '澳门探索者', '完成澳门核心地标探索', 'https://example.com/badges/explorer-icon.png', 'exploration', 'epic', 0, 0, 'https://example.com/badges/explorer.png', '1')
ON DUPLICATE KEY UPDATE `name_zh` = VALUES(`name_zh`), `status` = VALUES(`status`);

INSERT INTO `ai_provider_configs` (`provider_name`, `display_name`, `api_base_url`, `api_key_encrypted`, `api_secret_encrypted`, `model_name`, `capabilities`, `request_timeout_ms`, `max_retries`, `quota_daily`, `cost_per_1k_tokens`, `status`)
VALUES ('hunyuan', '腾讯混元', 'https://api.hunyuan.local', 'local-placeholder-key', 'local-placeholder-secret', 'hunyuan-2.0-instruct-20251111', JSON_OBJECT('text', true, 'vision', true, 'multimodal', true, 'speech', true, 'voice', true), 30000, 2, 5000, 0.020000, 1)
ON DUPLICATE KEY UPDATE `display_name` = VALUES(`display_name`), `model_name` = VALUES(`model_name`), `status` = VALUES(`status`);

INSERT INTO `ai_navigation_policies` (`policy_name`, `scenario_code`, `policy_type`, `scenario_group`, `provider_id`, `prompt_template`, `system_prompt`, `model_override`, `multimodal_enabled`, `voice_enabled`, `temperature`, `max_tokens`, `response_schema`, `post_process_rules`, `fallback_policy_id`, `status`)
SELECT '澳门智能行程规划', 'itinerary_planning', 'generation', 'planning', p.id, '根据用户需求输出 2-3 套行程', '你是澳门旅行规划助手。', NULL, 0, 0, 0.70, 2048, NULL, NULL, NULL, 1
FROM `ai_provider_configs` p WHERE p.provider_name = 'hunyuan'
AND NOT EXISTS (SELECT 1 FROM `ai_navigation_policies` ap WHERE ap.scenario_code = 'itinerary_planning');

INSERT INTO `ai_navigation_policies` (`policy_name`, `scenario_code`, `policy_type`, `scenario_group`, `provider_id`, `prompt_template`, `system_prompt`, `model_override`, `multimodal_enabled`, `voice_enabled`, `temperature`, `max_tokens`, `response_schema`, `post_process_rules`, `fallback_policy_id`, `status`)
SELECT '澳门旅行问答', 'travel_qa', 'qa', 'qa', p.id, '结合景点与故事内容回答用户问题', '你是澳门旅行问答助手。', NULL, 0, 0, 0.40, 1024, NULL, NULL, NULL, 1
FROM `ai_provider_configs` p WHERE p.provider_name = 'hunyuan'
AND NOT EXISTS (SELECT 1 FROM `ai_navigation_policies` ap WHERE ap.scenario_code = 'travel_qa');

INSERT INTO `ai_navigation_policies` (`policy_name`, `scenario_code`, `policy_type`, `scenario_group`, `provider_id`, `prompt_template`, `system_prompt`, `model_override`, `multimodal_enabled`, `voice_enabled`, `temperature`, `max_tokens`, `response_schema`, `post_process_rules`, `fallback_policy_id`, `status`)
SELECT '室内拍照识别定位', 'indoor_vision_positioning', 'vision', 'vision', p.id, '根据视觉锚点和楼层信息推断位置', '你是室内视觉定位助手。', NULL, 1, 0, 0.20, 1024, NULL, NULL, NULL, 1
FROM `ai_provider_configs` p WHERE p.provider_name = 'hunyuan'
AND NOT EXISTS (SELECT 1 FROM `ai_navigation_policies` ap WHERE ap.scenario_code = 'indoor_vision_positioning');

INSERT INTO `ai_navigation_policies` (`policy_name`, `scenario_code`, `policy_type`, `scenario_group`, `provider_id`, `prompt_template`, `system_prompt`, `model_override`, `multimodal_enabled`, `voice_enabled`, `temperature`, `max_tokens`, `response_schema`, `post_process_rules`, `fallback_policy_id`, `status`)
SELECT 'NPC语音讲解对话', 'npc_voice_dialogue', 'dialogue', 'dialogue', p.id, '输出适合语音播报的讲解词与互动问答', '你是澳门 NPC 语音导览助手。', NULL, 0, 1, 0.85, 1536, NULL, NULL, NULL, 1
FROM `ai_provider_configs` p WHERE p.provider_name = 'hunyuan'
AND NOT EXISTS (SELECT 1 FROM `ai_navigation_policies` ap WHERE ap.scenario_code = 'npc_voice_dialogue');

INSERT INTO `ai_navigation_policies` (`policy_name`, `scenario_code`, `policy_type`, `scenario_group`, `provider_id`, `prompt_template`, `system_prompt`, `model_override`, `multimodal_enabled`, `voice_enabled`, `temperature`, `max_tokens`, `response_schema`, `post_process_rules`, `fallback_policy_id`, `status`)
SELECT '室内导航辅助', 'indoor_navigation_assist', 'navigation', 'navigation', p.id, '结合地图节点为用户提供导航建议', '你是澳门室内导航助手。', NULL, 0, 0, 0.30, 1024, NULL, NULL, NULL, 1
FROM `ai_provider_configs` p WHERE p.provider_name = 'hunyuan'
AND NOT EXISTS (SELECT 1 FROM `ai_navigation_policies` ap WHERE ap.scenario_code = 'indoor_navigation_assist');

INSERT INTO `ai_request_logs` (`provider_id`, `policy_id`, `user_openid`, `request_type`, `input_data_hash`, `output_summary`, `latency_ms`, `tokens_used`, `cost_usd`, `success`, `error_message`, `created_at`)
SELECT p.id, ap.id, 'local-demo-user', 'admin-test', 'hash_local_demo_001', '本地示例：生成 2 日澳门亲子游路线。', 1280, 860, 0.017200, 1, NULL, NOW()
FROM `ai_provider_configs` p
JOIN `ai_navigation_policies` ap ON ap.provider_id = p.id AND ap.scenario_group = 'planning'
WHERE p.provider_name = 'hunyuan'
AND NOT EXISTS (SELECT 1 FROM `ai_request_logs` l WHERE l.input_data_hash = 'hash_local_demo_001');

INSERT INTO `sys_config` (`config_key`, `config_value`, `config_type`, `description`)
VALUES
  ('trigger.outdoor.radius.high', '30', 'int', 'GPS精度<10米时触发半径(米)'),
  ('trigger.outdoor.radius.medium', '50', 'int', 'GPS精度10-20米时触发半径(米)'),
  ('trigger.outdoor.radius.low', '80', 'int', 'GPS精度>20米时触发半径(米)'),
  ('trigger.cooldown.time', '1800000', 'int', '触发冷却时间(毫秒)'),
  ('gps.interval', '2000', 'int', 'GPS采样间隔(毫秒)')
ON DUPLICATE KEY UPDATE `config_value` = VALUES(`config_value`), `description` = VALUES(`description`);

SET FOREIGN_KEY_CHECKS = 1;
