/*
 Navicat Premium Data Transfer

 Source Server         : tripofmacau
 Source Server Type    : MySQL
 Source Server Version : 80030 (8.0.30-cynos-3.1.16.003)
 Source Host           : sh-cynosdbmysql-grp-m5x7mh7e.sql.tencentcdb.com:25101
 Source Schema         : macau-trip-2gn2zm5jefa4a987

 Target Server Type    : MySQL
 Target Server Version : 80030 (8.0.30-cynos-3.1.16.003)
 File Encoding         : 65001

 Date: 09/04/2026 07:32:43
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for activities
-- ----------------------------
DROP TABLE IF EXISTS `activities`;
CREATE TABLE `activities`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `title` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `cover_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `start_time` datetime NULL DEFAULT NULL,
  `end_time` datetime NULL DEFAULT NULL,
  `status` enum('draft','published','ended','cancelled') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'draft',
  `participation_count` int NULL DEFAULT 0,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NULL DEFAULT 0,
  `_openid` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用于权限管理，请不要删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `code`(`code` ASC) USING BTREE,
  INDEX `idx_code`(`code` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of activities
-- ----------------------------
INSERT INTO `activities` VALUES (1, 'mfm-heritage-walk', '澳门世界遗产探索月', '结合海上丝路故事线的城市步行探索活动，联动大三巴、议事亭前地与妈阁庙等核心点位。', 'https://cdn.tripofmacau.com/activities/mfm-heritage.jpg', '2026-04-01 09:00:00', '2026-06-30 23:59:59', 'published', 268, '2026-04-08 02:35:20', '2026-04-08 02:35:20', 0, '');
INSERT INTO `activities` VALUES (2, 'night-food-hunt', '澳门夜游美食寻章', '围绕夜间美食与街区故事打造的印章活动，鼓励用户在晚间完成探索与兑换。', 'https://cdn.tripofmacau.com/activities/night-food.jpg', '2026-04-10 18:00:00', '2026-07-31 23:00:00', 'published', 156, '2026-04-08 02:35:20', '2026-04-08 02:35:20', 0, '');
INSERT INTO `activities` VALUES (3, 'elderly-friendly-week', '长者友好语音导览周', '主打长者模式、语音播报和低门槛打卡体验的适老化主题活动。', 'https://cdn.tripofmacau.com/activities/elderly-week.jpg', '2026-05-01 08:00:00', '2026-05-14 20:00:00', 'published', 89, '2026-04-08 02:35:20', '2026-04-08 02:35:20', 0, '');

-- ----------------------------
-- Table structure for admin_roles
-- ----------------------------
DROP TABLE IF EXISTS `admin_roles`;
CREATE TABLE `admin_roles`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `admin_user_id` bigint NOT NULL COMMENT '管理员',
  `role_id` bigint NOT NULL COMMENT '角色',
  `granted_by` bigint NULL DEFAULT NULL COMMENT '授权人',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `_openid` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_admin_role`(`admin_user_id` ASC, `role_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '管理员-角色关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of admin_roles
-- ----------------------------

-- ----------------------------
-- Table structure for ai_navigation_policies
-- ----------------------------
DROP TABLE IF EXISTS `ai_navigation_policies`;
CREATE TABLE `ai_navigation_policies`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `policy_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '策略名',
  `scenario_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '场景编码 itinerary_planning/travel_qa/indoor_vision_positioning/npc_voice_dialogue',
  `policy_type` enum('floor_detection','orientation_detection','target_navigation','scene_recognition') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '类型',
  `scenario_group` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '能力分组 planning/qa/vision/dialogue/navigation',
  `provider_id` bigint NOT NULL COMMENT 'AI供应商',
  `prompt_template` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Prompt模板',
  `system_prompt` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '系统Prompt',
  `model_override` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '模型覆盖',
  `multimodal_enabled` tinyint NULL DEFAULT 0 COMMENT '是否启用多模态',
  `voice_enabled` tinyint NULL DEFAULT 0 COMMENT '是否启用语音能力',
  `temperature` decimal(3, 2) NULL DEFAULT 0.30 COMMENT '温度',
  `max_tokens` int NULL DEFAULT 1024 COMMENT '最大token',
  `response_schema` json NULL COMMENT '期望响应格式',
  `post_process_rules` json NULL COMMENT '后处理规则',
  `fallback_policy_id` bigint NULL DEFAULT NULL COMMENT '备用策略',
  `status` tinyint NULL DEFAULT 1,
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `_openid` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `policy_name`(`policy_name` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'AI导航策略表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of ai_navigation_policies
-- ----------------------------
INSERT INTO `ai_navigation_policies` VALUES (1, '澳门智能行程规划', 'itinerary_planning', 'target_navigation', 'planning', 1, '根据用户停留天数、出发位置、偏好、体力、预算与是否亲子出行，生成多天多时段澳门游玩路线。', '你是澳小遊的行程规划助手，必须给出结构化、可执行、符合真实地理动线的旅行路线。', 'hunyuan-2.0-instruct-20251111', 0, 0, 0.40, 2048, '{\"format\": \"day_plan\"}', '{\"needPoiValidation\": true}', NULL, 1, '2026-04-08 22:18:39', '2026-04-08 22:18:39', '');
INSERT INTO `ai_navigation_policies` VALUES (2, '澳门旅行问答', 'travel_qa', 'scene_recognition', 'qa', 1, '回答用户关于澳门景点、美食、交通、故事线和玩法的问题。', '你是澳小遊的澳门旅行问答助手，应优先基于后台内容库输出简洁准确答案。', 'hunyuan-2.0-instruct-20251111', 0, 0, 0.20, 1024, '{\"format\": \"answer_card\"}', '{\"needCitation\": true}', NULL, 1, '2026-04-08 22:18:39', '2026-04-08 22:18:39', '');
INSERT INTO `ai_navigation_policies` VALUES (3, '室内拍照识别定位', 'indoor_vision_positioning', 'floor_detection', 'vision', 1, '根据用户上传的室内照片、视觉锚点、楼层锚点和参考物，判断所处建筑、楼层、朝向与最近目标点。', '你是澳小遊室内定位识别模型，需输出建筑、楼层、朝向、锚点置信度和附近POI。', 'hunyuan-2.0-instruct-20251111', 1, 0, 0.10, 1536, '{\"format\": \"indoor_location\"}', '{\"needAnchorConfidence\": true}', NULL, 1, '2026-04-08 22:18:39', '2026-04-08 22:18:39', '');
INSERT INTO `ai_navigation_policies` VALUES (4, 'NPC语音讲解对话', 'npc_voice_dialogue', 'orientation_detection', 'dialogue', 1, '根据NPC角色设定、景点背景和用户提问，生成适合语音播报的对话内容。', '你是澳小遊景点NPC，口吻亲切、有故事感、适合TTS播报。', 'hunyuan-2.0-instruct-20251111', 0, 1, 0.70, 1536, '{\"format\": \"npc_dialogue\"}', '{\"ttsReady\": true}', NULL, 1, '2026-04-08 22:18:39', '2026-04-08 22:18:39', '');

-- ----------------------------
-- Table structure for ai_provider_configs
-- ----------------------------
DROP TABLE IF EXISTS `ai_provider_configs`;
CREATE TABLE `ai_provider_configs`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `provider_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '供应商名',
  `display_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '显示名',
  `api_base_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'API Base URL',
  `api_key_encrypted` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '加密存储Key',
  `api_secret_encrypted` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '加密Secret',
  `model_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '默认模型',
  `capabilities` json NULL COMMENT '能力开关',
  `request_timeout_ms` int NULL DEFAULT 30000 COMMENT '超时毫秒',
  `max_retries` int NULL DEFAULT 3 COMMENT '重试次数',
  `quota_daily` int NULL DEFAULT 0 COMMENT '日配额',
  `cost_per_1k_tokens` decimal(10, 4) NULL DEFAULT NULL COMMENT '千token成本',
  `status` tinyint NULL DEFAULT 1,
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `_openid` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `provider_name`(`provider_name` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'AI供应商配置' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of ai_provider_configs
-- ----------------------------
INSERT INTO `ai_provider_configs` VALUES (1, 'hunyuan', '腾讯混元', 'https://api.hunyuan.cloud.tencent.com', NULL, NULL, 'hunyuan-2.0-instruct-20251111', '{\"text\": true, \"voice\": true, \"speech\": true, \"vision\": true, \"multimodal\": true}', 30000, 3, 100000, 0.0200, 1, '2026-04-08 22:18:39', '2026-04-08 22:18:39', '');

-- ----------------------------
-- Table structure for ai_request_logs
-- ----------------------------
DROP TABLE IF EXISTS `ai_request_logs`;
CREATE TABLE `ai_request_logs`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `provider_id` bigint NULL DEFAULT NULL COMMENT '供应商ID',
  `policy_id` bigint NULL DEFAULT NULL COMMENT '策略ID',
  `scenario_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '场景编码',
  `user_openid` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '用户openid',
  `request_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '请求类型',
  `input_data_hash` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '输入摘要哈希',
  `output_summary` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '输出摘要',
  `latency_ms` int NULL DEFAULT NULL COMMENT '耗时ms',
  `tokens_used` int NULL DEFAULT NULL COMMENT 'tokens用量',
  `cost_usd` decimal(10, 6) NULL DEFAULT NULL COMMENT '成本USD',
  `success` tinyint NULL DEFAULT 1 COMMENT '是否成功',
  `error_message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '错误信息',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `_openid` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_provider`(`provider_id` ASC) USING BTREE,
  INDEX `idx_policy`(`policy_id` ASC) USING BTREE,
  INDEX `idx_scenario`(`scenario_code` ASC) USING BTREE,
  INDEX `idx_created`(`created_at` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'AI请求日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of ai_request_logs
-- ----------------------------

-- ----------------------------
-- Table structure for badge_rules
-- ----------------------------
DROP TABLE IF EXISTS `badge_rules`;
CREATE TABLE `badge_rules`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `badge_id` bigint NOT NULL COMMENT '徽章',
  `rule_type` enum('complete_storyline','city_progress','collect_set','complete_activity','complete_task','admin_grant','combo') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '规则类型',
  `rule_config` json NOT NULL COMMENT '规则参数',
  `auto_grant` tinyint NULL DEFAULT 1 COMMENT '自动发放',
  `priority` int NULL DEFAULT NULL COMMENT '优先级',
  `status` tinyint NULL DEFAULT 1,
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `_openid` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '徽章规则表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of badge_rules
-- ----------------------------

-- ----------------------------
-- Table structure for badges
-- ----------------------------
DROP TABLE IF EXISTS `badges`;
CREATE TABLE `badges`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `badge_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '编码',
  `name_zh` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '名称',
  `description_zh` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '描述',
  `icon_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '图标',
  `badge_type` enum('storyline','city_exploration','collection','activity','special','hidden') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '类型',
  `rarity` enum('common','rare','epic','legendary','hidden') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '稀有度',
  `is_hidden` tinyint NULL DEFAULT 0 COMMENT '隐藏徽章',
  `is_limited_time` tinyint NULL DEFAULT 0 COMMENT '限时徽章',
  `limited_start` datetime NULL DEFAULT NULL,
  `limited_end` datetime NULL DEFAULT NULL,
  `image_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '图片',
  `animation_unlock` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '解锁动画',
  `status` tinyint NULL DEFAULT 1,
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `_openid` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `badge_code`(`badge_code` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '徽章表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of badges
-- ----------------------------

-- ----------------------------
-- Table structure for buildings
-- ----------------------------
DROP TABLE IF EXISTS `buildings`;
CREATE TABLE `buildings`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `city_code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '所在城市',
  `building_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '建筑编码',
  `name_zh` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '名称',
  `address_zh` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '地址',
  `lat` decimal(10, 6) NULL DEFAULT NULL COMMENT '入口纬度',
  `lng` decimal(11, 6) NULL DEFAULT NULL COMMENT '入口经度',
  `total_floors` int NULL DEFAULT 1 COMMENT '总楼层',
  `basement_floors` int NULL DEFAULT 0 COMMENT '地下楼层',
  `cover_image_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '外观图',
  `description_zh` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '描述',
  `poi_id` bigint NULL DEFAULT NULL COMMENT '关联入口POI',
  `status` tinyint NULL DEFAULT 1,
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `_openid` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `building_code`(`building_code` ASC) USING BTREE,
  INDEX `idx_city`(`city_code` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '建筑物表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of buildings
-- ----------------------------

-- ----------------------------
-- Table structure for campaigns
-- ----------------------------
DROP TABLE IF EXISTS `campaigns`;
CREATE TABLE `campaigns`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `campaign_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '活动编码',
  `title_zh` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '标题',
  `description_zh` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '描述',
  `cover_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '封面',
  `campaign_type` enum('collection','storyline','event','limited') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '类型',
  `start_time` datetime NULL DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime NULL DEFAULT NULL COMMENT '结束时间',
  `rules_config` json NULL COMMENT '规则配置',
  `reward_binding` json NULL COMMENT '奖励绑定',
  `status` tinyint NULL DEFAULT 0 COMMENT '0草稿1进行中2已结束',
  `participation_count` bigint NULL DEFAULT 0,
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `_openid` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `campaign_code`(`campaign_code` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '推广活动表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of campaigns
-- ----------------------------

-- ----------------------------
-- Table structure for chapter_branches
-- ----------------------------
DROP TABLE IF EXISTS `chapter_branches`;
CREATE TABLE `chapter_branches`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `parent_chapter_id` bigint NOT NULL COMMENT '父章节',
  `branch_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '分支名',
  `branch_condition` json NOT NULL COMMENT '进入条件',
  `target_chapter_id` bigint NOT NULL COMMENT '目标章节',
  `priority` int NULL DEFAULT 0 COMMENT '优先级',
  `exclusive` tinyint NULL DEFAULT 0 COMMENT '是否互斥',
  `status` tinyint NULL DEFAULT 1,
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `_openid` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '章节分支定义表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of chapter_branches
-- ----------------------------

-- ----------------------------
-- Table structure for chapter_tasks
-- ----------------------------
DROP TABLE IF EXISTS `chapter_tasks`;
CREATE TABLE `chapter_tasks`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `chapter_id` bigint NOT NULL COMMENT '所属章节',
  `task_type` enum('checkin','collect','quiz','photo','social','visit') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '任务类型',
  `task_title` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '任务标题',
  `task_description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '任务描述',
  `target_ref_id` bigint NULL DEFAULT NULL COMMENT '目标对象ID',
  `target_ref_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '目标类型',
  `required_count` int NULL DEFAULT 1 COMMENT '需要数量',
  `reward_config` json NULL COMMENT '奖励',
  `time_limit_seconds` int NULL DEFAULT NULL COMMENT '时限',
  `sort_order` int NULL DEFAULT NULL COMMENT '排序',
  `status` tinyint NULL DEFAULT 1,
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `_openid` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_chapter`(`chapter_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '章节任务表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of chapter_tasks
-- ----------------------------

-- ----------------------------
-- Table structure for cities
-- ----------------------------
DROP TABLE IF EXISTS `cities`;
CREATE TABLE `cities`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '城市代码',
  `name_zh` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '中文名',
  `name_en` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '英文名',
  `name_zht` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '繁中名',
  `country_code` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'MO' COMMENT '国家/地区码',
  `center_lat` decimal(10, 6) NULL DEFAULT NULL COMMENT '中心纬度',
  `center_lng` decimal(11, 6) NULL DEFAULT NULL COMMENT '中心经度',
  `bounds_geojson` json NULL COMMENT '地理围栏',
  `default_zoom` int NULL DEFAULT 14 COMMENT '默认缩放',
  `unlock_type` enum('auto','manual','condition') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'auto' COMMENT '解锁方式',
  `unlock_condition` json NULL COMMENT '解锁条件',
  `cover_image_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '封面图',
  `banner_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '横幅图',
  `description_zh` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '描述',
  `sort_order` int NULL DEFAULT 0 COMMENT '排序',
  `status` tinyint NULL DEFAULT 0 COMMENT '0草稿1发布2下线',
  `published_at` datetime NULL DEFAULT NULL COMMENT '发布时间',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `_openid` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_code`(`code` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_sort`(`sort_order` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '城市管理表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of cities
-- ----------------------------
INSERT INTO `cities` VALUES (1, 'macau', '澳门', 'Macau', '澳門', 'MO', 22.198000, 113.556000, NULL, 14, 'auto', NULL, NULL, NULL, '澳门特别行政区，世界文化遗产与娱乐之都。探索历史街巷、品味葡式美食、体验东西方文化交融的独特魅力。', 1, 1, NULL, '2026-04-08 16:09:55', '2026-04-08 16:09:55', '');
INSERT INTO `cities` VALUES (2, 'hongkong', '香港', 'Hong Kong', '香港', 'HK', 22.319300, 114.169400, NULL, 13, 'condition', NULL, NULL, NULL, '国际大都市，繁华购物天堂与维港夜景。', 2, 0, NULL, '2026-04-08 16:09:55', '2026-04-08 16:09:55', '');
INSERT INTO `cities` VALUES (3, 'zhuhai', '珠海', 'Zhuhai', '珠海', 'CN', 22.270900, 113.568800, NULL, 13, 'auto', NULL, NULL, NULL, '海滨花园城市，毗邻澳门的浪漫之城。', 3, 0, NULL, '2026-04-08 16:09:55', '2026-04-08 16:09:55', '');

-- ----------------------------
-- Table structure for collectible_series
-- ----------------------------
DROP TABLE IF EXISTS `collectible_series`;
CREATE TABLE `collectible_series`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `series_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '系列编码',
  `name_zh` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '名称',
  `description_zh` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '描述',
  `cover_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '封面',
  `total_items` int NULL DEFAULT 0 COMMENT '总物品数',
  `bonus_reward_id` bigint NULL DEFAULT NULL COMMENT '集齐奖励',
  `status` tinyint NULL DEFAULT 1,
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `_openid` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `series_code`(`series_code` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '收集物系列表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of collectible_series
-- ----------------------------

-- ----------------------------
-- Table structure for collectibles
-- ----------------------------
DROP TABLE IF EXISTS `collectibles`;
CREATE TABLE `collectibles`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `collectible_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '编码',
  `name_zh` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '名称',
  `name_en` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '英文名',
  `description_zh` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '描述',
  `collectible_type` enum('item','stamp_card','fragment','costume') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'item' COMMENT '类型',
  `rarity` enum('common','uncommon','rare','epic','legendary') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'common' COMMENT '稀有度',
  `image_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '图片',
  `animation_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '动画',
  `series_id` bigint NULL DEFAULT NULL COMMENT '所属系列',
  `acquisition_source` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '获取来源',
  `bind_condition` json NULL COMMENT '绑定条件',
  `display_rule` json NULL COMMENT '显示规则',
  `is_repeatable` tinyint NULL DEFAULT 0 COMMENT '可重复获取',
  `is_limited` tinyint NULL DEFAULT 0 COMMENT '限时',
  `limited_start` datetime NULL DEFAULT NULL COMMENT '限时开始',
  `limited_end` datetime NULL DEFAULT NULL COMMENT '限时结束',
  `cross_city` tinyint NULL DEFAULT 0 COMMENT '跨城市',
  `max_ownership` int NULL DEFAULT 1 COMMENT '最大持有数',
  `status` tinyint NULL DEFAULT 1,
  `sort_order` int NULL DEFAULT NULL,
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted_at` datetime NULL DEFAULT NULL,
  `_openid` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `collectible_code`(`collectible_code` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '收集物表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of collectibles
-- ----------------------------

-- ----------------------------
-- Table structure for indoor_floors
-- ----------------------------
DROP TABLE IF EXISTS `indoor_floors`;
CREATE TABLE `indoor_floors`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `building_id` bigint NOT NULL COMMENT '关联建筑',
  `indoor_map_id` bigint NOT NULL COMMENT '关联室内地图',
  `floor_number` int NOT NULL COMMENT '楼层号(负数=地下)',
  `floor_name_zh` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '楼层名称',
  `floor_plan_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '楼层平面图',
  `altitude_meters` decimal(8, 2) NULL DEFAULT NULL COMMENT '海拔高度',
  `sort_order` int NULL DEFAULT NULL COMMENT '排序',
  `status` tinyint NULL DEFAULT 1,
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `_openid` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_building`(`building_id` ASC) USING BTREE,
  INDEX `idx_map`(`indoor_map_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '室内楼层表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of indoor_floors
-- ----------------------------

-- ----------------------------
-- Table structure for indoor_maps
-- ----------------------------
DROP TABLE IF EXISTS `indoor_maps`;
CREATE TABLE `indoor_maps`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `building_id` bigint NOT NULL COMMENT '关联建筑',
  `map_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '地图名',
  `floor_plan_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '平面图URL',
  `width_px` int NULL DEFAULT NULL COMMENT '图像宽度',
  `height_px` int NULL DEFAULT NULL COMMENT '图像高度',
  `dpi` int NULL DEFAULT 96 COMMENT '分辨率',
  `scale_ratio` decimal(10, 4) NULL DEFAULT NULL COMMENT '像素/米比例',
  `origin_lat` decimal(10, 6) NULL DEFAULT NULL COMMENT '原点纬度',
  `origin_lng` decimal(11, 6) NULL DEFAULT NULL COMMENT '原点经度',
  `version` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '版本号',
  `status` tinyint NULL DEFAULT 1,
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `_openid` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_building`(`building_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '室内地图表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of indoor_maps
-- ----------------------------

-- ----------------------------
-- Table structure for indoor_nodes
-- ----------------------------
DROP TABLE IF EXISTS `indoor_nodes`;
CREATE TABLE `indoor_nodes`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `building_id` bigint NOT NULL COMMENT '建筑',
  `floor_id` bigint NOT NULL COMMENT '楼层',
  `node_type` enum('poi','shop','service','landmark','elevator','stairs','restroom','entrance','exit','custom') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '节点类型',
  `node_name_zh` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '名称',
  `position_x` decimal(10, 2) NULL DEFAULT NULL COMMENT '平面图X坐标',
  `position_y` decimal(10, 2) NULL DEFAULT NULL COMMENT '平面图Y坐标',
  `related_poi_id` bigint NULL DEFAULT NULL COMMENT '关联室外POI',
  `icon` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '图标',
  `tags` json NULL COMMENT '标签',
  `description_zh` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '描述',
  `status` tinyint NULL DEFAULT 1,
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `_openid` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_building`(`building_id` ASC) USING BTREE,
  INDEX `idx_floor`(`floor_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '室内节点表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of indoor_nodes
-- ----------------------------

-- ----------------------------
-- Table structure for map_tile_configs
-- ----------------------------
DROP TABLE IF EXISTS `map_tile_configs`;
CREATE TABLE `map_tile_configs`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `map_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '地图标识',
  `style` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'standard' COMMENT '地图样式: standard/satellite/custom',
  `cdn_base` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '瓦片CDN基础URL',
  `control_points_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '控制点数据URL',
  `pois_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'POI数据URL',
  `zoom_min` int NULL DEFAULT 10 COMMENT '最小缩放层级',
  `zoom_max` int NULL DEFAULT 18 COMMENT '最大缩放级别',
  `center_lat` decimal(10, 6) NULL DEFAULT 22.198000 COMMENT '中心纬度',
  `center_lng` decimal(11, 6) NULL DEFAULT 113.556000 COMMENT '中心经度',
  `default_zoom` int NULL DEFAULT 14 COMMENT '默认缩放级别',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态: 0禁用 1启用',
  `version` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'v1' COMMENT '地图版本号',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `_openid` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_map_id`(`map_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '地图瓦片配置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of map_tile_configs
-- ----------------------------
INSERT INTO `map_tile_configs` VALUES (1, 'macau_main', 'standard', 'https://tiles.example.com/macau/{z}/{x}/{y}.png', NULL, NULL, 10, 18, 22.198000, 113.556000, 14, 1, 'v1', '2026-04-08 16:02:51', '2026-04-08 16:02:51', '');
INSERT INTO `map_tile_configs` VALUES (2, 'macau_satellite', 'satellite', 'https://satellite-tiles.example.com/macau/{z}/{x}/{y}.jpg', NULL, NULL, 12, 18, 22.198000, 113.556000, 14, 1, 'v1', '2026-04-08 16:02:51', '2026-04-08 16:02:51', '');

-- ----------------------------
-- Table structure for permissions
-- ----------------------------
DROP TABLE IF EXISTS `permissions`;
CREATE TABLE `permissions`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `perm_code` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '权限编码',
  `perm_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '权限名',
  `module` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '所属模块',
  `perm_type` enum('menu','button','api','data_scope') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '类型',
  `parent_id` bigint NULL DEFAULT 0 COMMENT '父权限',
  `path` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '路径/URL',
  `method` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'HTTP方法',
  `description` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '说明',
  `sort_order` int NULL DEFAULT NULL COMMENT '排序',
  `status` tinyint NULL DEFAULT 1,
  `_openid` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `perm_code`(`perm_code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 21 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '权限点表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of permissions
-- ----------------------------
INSERT INTO `permissions` VALUES (1, 'dashboard:view', '仪表盘', 'dashboard', 'menu', 0, '/admin/dashboard', 'GET', '仪表盘总览', 1, 1, '');
INSERT INTO `permissions` VALUES (2, 'map:city:view', '城市管理-查看', 'map-space', 'menu', 0, '/admin/map/cities', 'GET', '城市列表查看', 10, 1, '');
INSERT INTO `permissions` VALUES (3, 'map:city:edit', '城市管理-编辑', 'map-space', 'button', 0, NULL, NULL, '城市新增/编辑', 11, 1, '');
INSERT INTO `permissions` VALUES (4, 'map:poi:view', 'POI管理-查看', 'map-space', 'menu', 0, '/admin/map/pois', 'GET', 'POI列表查看', 20, 1, '');
INSERT INTO `permissions` VALUES (5, 'map:poi:edit', 'POI管理-编辑', 'map-space', 'button', 0, NULL, NULL, 'POI新增/编辑/删除', 21, 1, '');
INSERT INTO `permissions` VALUES (6, 'map:indoor:building:view', '室内地图-建筑查看', 'map-space', 'menu', 0, '/admin/map/buildings', 'GET', '建筑物列表', 30, 1, '');
INSERT INTO `permissions` VALUES (7, 'map:ai:config', 'AI导航配置', 'map-space', 'menu', 0, '/admin/map/ai-config', 'GET', 'AI供应商与策略配置', 40, 1, '');
INSERT INTO `permissions` VALUES (8, 'story:storyline:view', '故事线-查看', 'content', 'menu', 0, '/admin/content/storylines', 'GET', '故事线列表', 50, 1, '');
INSERT INTO `permissions` VALUES (9, 'story:storyline:edit', '故事线-编辑', 'content', 'button', 0, NULL, NULL, '故事线增删改', 51, 1, '');
INSERT INTO `permissions` VALUES (10, 'story:chapter:edit', '章节-编辑', 'content', 'button', 0, NULL, NULL, '章节编排与管理', 52, 1, '');
INSERT INTO `permissions` VALUES (11, 'collect:item:view', '收集物-查看', 'collectible', 'menu', 0, '/admin/collectibles/items', 'GET', '收集物列表', 60, 1, '');
INSERT INTO `permissions` VALUES (12, 'collect:badge:view', '徽章-查看', 'collectible', 'menu', 0, '/admin/collectibles/badges', 'GET', '徽章列表', 70, 1, '');
INSERT INTO `permissions` VALUES (13, 'collect:reward:manage', '奖励管理', 'collectible', 'menu', 0, '/admin/collectibles/rewards', 'GET', '奖励配置管理', 80, 1, '');
INSERT INTO `permissions` VALUES (14, 'user:progress:view', '用户进度-查看', 'user', 'menu', 0, '/admin/users/progress', 'GET', '用户探索进度', 90, 1, '');
INSERT INTO `permissions` VALUES (15, 'ops:activity:manage', '活动运营', 'operation', 'menu', 0, '/admin/operations/activities', 'GET', '推广活动管理', 100, 1, '');
INSERT INTO `permissions` VALUES (16, 'test:console', '测试控制台', 'test', 'menu', 0, '/admin/test/console', 'GET', '测试工具面板', 110, 1, '');
INSERT INTO `permissions` VALUES (17, 'sys:admin:user', '管理员帐号', 'system', 'menu', 0, '/admin/system/admin-users', 'GET', '管理员帐号管理', 120, 1, '');
INSERT INTO `permissions` VALUES (18, 'sys:role:manage', '角色权限', 'system', 'menu', 0, '/admin/system/roles', 'GET', '角色与权限矩阵', 130, 1, '');
INSERT INTO `permissions` VALUES (19, 'sys:config:manage', '系统配置', 'system', 'menu', 0, '/admin/system/configs', 'GET', '系统参数配置', 140, 1, '');
INSERT INTO `permissions` VALUES (20, 'sys:audit:log', '审计日志', 'system', 'menu', 0, '/admin/system/audit-logs', 'GET', '操作审计日志', 150, 1, '');

-- ----------------------------
-- Table structure for poi_categories
-- ----------------------------
DROP TABLE IF EXISTS `poi_categories`;
CREATE TABLE `poi_categories`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `icon` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `sort_order` int NULL DEFAULT 0,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `deleted` tinyint NULL DEFAULT 0,
  `_openid` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用于权限管理，请不要删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of poi_categories
-- ----------------------------

-- ----------------------------
-- Table structure for poi_types
-- ----------------------------
DROP TABLE IF EXISTS `poi_types`;
CREATE TABLE `poi_types`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `type_code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '类型编码',
  `name_zh` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '中文名',
  `icon` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '图标',
  `color` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '颜色标识',
  `description` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '描述',
  `sort_order` int NULL DEFAULT NULL COMMENT '排序',
  `status` tinyint NULL DEFAULT 1,
  `_openid` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `type_code`(`type_code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'POI类型字典' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of poi_types
-- ----------------------------
INSERT INTO `poi_types` VALUES (1, 'scenic', '景点 POI', 'EnvironmentOutlined', '#1677ff', '可游览的景点、名胜、建筑等', 1, 1, '');
INSERT INTO `poi_types` VALUES (2, 'trigger', '触发物 POI', 'BulbOutlined', '#faad14', '可触发的交互点、隐藏物品等', 2, 1, '');
INSERT INTO `poi_types` VALUES (3, 'control', '控制点 POI', 'AimOutlined', '#52c41a', '地图校准控制点、路径节点', 3, 1, '');
INSERT INTO `poi_types` VALUES (4, 'mark', '标记点 POI', 'InfoCircleOutlined', '#999999', '纯标记用途的点', 4, 1, '');

-- ----------------------------
-- Table structure for pois
-- ----------------------------
DROP TABLE IF EXISTS `pois`;
CREATE TABLE `pois`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name_zh` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `name_en` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `name_zht` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `subtitle` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `region_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `poi_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `city_code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'macau' COMMENT '所属城市',
  `latitude` decimal(10, 8) NOT NULL,
  `longitude` decimal(11, 8) NOT NULL,
  `map_tile_x` int NULL DEFAULT NULL COMMENT '地图坐标X',
  `address` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `category_id` bigint NULL DEFAULT NULL,
  `parent_poi_id` bigint NULL DEFAULT NULL COMMENT '父POI(层级结构)',
  `trigger_radius` int NULL DEFAULT 30,
  `check_in_method` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `importance` enum('normal','important','very_important') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'normal',
  `story_line_id` bigint NULL DEFAULT NULL,
  `stamp_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `cover_image_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `image_urls` json NULL,
  `audio_guide_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `video_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `ar_content_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `indoor_building_id` bigint NULL DEFAULT NULL COMMENT '所属建筑物',
  `tags` json NULL,
  `difficulty` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `open_time` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `suggested_visit_minutes` int NULL DEFAULT NULL,
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'published',
  `check_in_count` bigint NULL DEFAULT 0,
  `favorite_count` bigint NULL DEFAULT 0,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NULL DEFAULT 0,
  `_openid` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用于权限管理，请不要删除',
  `map_tile_y` int NULL DEFAULT NULL COMMENT '地图坐标Y',
  `map_tile_zoom` int NULL DEFAULT NULL COMMENT '地图坐标Z',
  `indoor_floor_id` bigint NULL DEFAULT NULL COMMENT '所属楼层',
  `indoor_node_id` bigint NULL DEFAULT NULL COMMENT '关联室内节点',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_location`(`latitude` ASC, `longitude` ASC) USING BTREE,
  INDEX `idx_category`(`category_id` ASC) USING BTREE,
  INDEX `idx_story_line`(`story_line_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'POI景点表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of pois
-- ----------------------------
INSERT INTO `pois` VALUES (1, '妈阁庙', 'A-Ma Temple', '媽閣廟', '澳门信俗与出海记忆的起点', 'macau_central', 'story_point', 'macau', 22.18690000, 113.53100000, NULL, '澳门妈阁上街', 1, NULL, 50, 'gps_only', 'very_important', 1, 'story', '澳门最具代表性的海上丝路起点之一，适合搭配语音导览与历史叙事。', 'https://cdn.tripofmacau.com/pois/ama-cover.jpg', '[\"https://cdn.tripofmacau.com/pois/ama-1.jpg\", \"https://cdn.tripofmacau.com/pois/ama-2.jpg\"]', 'https://cdn.tripofmacau.com/audio/ama.mp3', '', '', NULL, '[\"世遗\", \"寺庙\", \"海上丝路\"]', 'easy', '07:00-18:00', 40, 'published', 1280, 642, '2026-04-08 06:36:42', '2026-04-08 06:36:42', 0, '', NULL, NULL, NULL, NULL);
INSERT INTO `pois` VALUES (2, '大三巴牌坊', 'Ruins of St. Paul', '大三巴牌坊', '中西文化交汇的城市地标', 'macau_central', 'landmark', 'macau', 22.19760000, 113.54080000, NULL, '澳门大三巴街附近', 2, NULL, 60, 'gps_only', 'very_important', 1, 'location', '最具辨识度的澳门地标，适合作为新用户首站和活动主视觉。', 'https://cdn.tripofmacau.com/pois/stpaul-cover.jpg', '[\"https://cdn.tripofmacau.com/pois/stpaul-1.jpg\"]', 'https://cdn.tripofmacau.com/audio/stpaul.mp3', '', '', NULL, '[\"地标\", \"首访必到\", \"拍照\"]', 'easy', '全天', 30, 'published', 2450, 1218, '2026-04-08 06:36:42', '2026-04-08 06:36:42', 0, '', NULL, NULL, NULL, NULL);
INSERT INTO `pois` VALUES (3, '大炮台', 'Monte Fort', '大炮台', '俯瞰澳门半岛的防卫据点', 'macau_central', 'story_point', 'macau', 22.19890000, 113.54170000, NULL, '澳门炮台斜巷', 3, NULL, 55, 'gps_only', 'important', 2, 'story', '适合作为东西方战事线的重要章节节点，配合战事解说与观景体验。', 'https://cdn.tripofmacau.com/pois/monte-cover.jpg', '[\"https://cdn.tripofmacau.com/pois/monte-1.jpg\"]', 'https://cdn.tripofmacau.com/audio/monte.mp3', '', '', NULL, '[\"炮台\", \"战争史\", \"观景\"]', 'medium', '07:00-19:00', 45, 'published', 1134, 502, '2026-04-08 06:36:42', '2026-04-08 06:36:42', 0, '', NULL, NULL, NULL, NULL);
INSERT INTO `pois` VALUES (4, '郑家大屋', 'Mandarin House', '鄭家大屋', '海上商贸家族与澳门近代转型的见证', 'macau_central', 'museum', 'macau', 22.19040000, 113.53810000, NULL, '澳门龙头左巷', 4, NULL, 45, 'gps_only', 'important', 1, 'story', '通过家族故事串联海上贸易、文化交流与澳门城市生活。', 'https://cdn.tripofmacau.com/pois/mandarin-cover.jpg', '[\"https://cdn.tripofmacau.com/pois/mandarin-1.jpg\"]', 'https://cdn.tripofmacau.com/audio/mandarin.mp3', '', '', NULL, '[\"家族故事\", \"历史建筑\"]', 'easy', '10:00-18:00', 35, 'published', 684, 298, '2026-04-08 06:36:42', '2026-04-08 06:36:42', 0, '', NULL, NULL, NULL, NULL);

-- ----------------------------
-- Table structure for rewards
-- ----------------------------
DROP TABLE IF EXISTS `rewards`;
CREATE TABLE `rewards`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name_zh` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
  `description` text CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL,
  `stamps_required` int NULL DEFAULT NULL,
  `total_quantity` int NULL DEFAULT NULL,
  `redeemed_count` int NULL DEFAULT 0,
  `start_time` timestamp NULL DEFAULT NULL,
  `end_time` timestamp NULL DEFAULT NULL,
  `status` enum('active','inactive') CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT 'active',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NULL DEFAULT 0,
  `_openid` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of rewards
-- ----------------------------
INSERT INTO `rewards` VALUES (1, '海上丝路纪念徽章', '完成海上丝路 6 章后可领取的限定纪念徽章。', 6, 500, 37, '2026-04-01 00:00:00', '2026-12-31 23:59:59', 'active', '2026-04-08 02:35:46', '2026-04-08 02:35:46', 0, '');
INSERT INTO `rewards` VALUES (2, '澳门夜游餐饮优惠券', '夜游美食寻章活动完成后可兑换的合作商户优惠券。', 12, 1200, 184, '2026-04-10 00:00:00', '2026-08-31 23:59:59', 'active', '2026-04-08 02:35:46', '2026-04-08 02:35:46', 0, '');
INSERT INTO `rewards` VALUES (3, '长者模式语音导览礼包', '为长者模式体验用户准备的语音导览与纪念礼遇。', 4, 300, 28, '2026-05-01 00:00:00', '2026-07-31 23:59:59', 'active', '2026-04-08 02:35:46', '2026-04-08 02:35:46', 0, '');

-- ----------------------------
-- Table structure for role_permissions
-- ----------------------------
DROP TABLE IF EXISTS `role_permissions`;
CREATE TABLE `role_permissions`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `role_id` bigint NOT NULL COMMENT '角色',
  `permission_id` bigint NOT NULL COMMENT '权限',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `_openid` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_role_perm`(`role_id` ASC, `permission_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '角色-权限关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of role_permissions
-- ----------------------------

-- ----------------------------
-- Table structure for roles
-- ----------------------------
DROP TABLE IF EXISTS `roles`;
CREATE TABLE `roles`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `role_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色编码',
  `role_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色名',
  `description` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '描述',
  `is_system` tinyint NULL DEFAULT 0 COMMENT '系统内置',
  `sort_order` int NULL DEFAULT NULL COMMENT '排序',
  `status` tinyint NULL DEFAULT 1,
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `_openid` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `role_code`(`role_code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '角色表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of roles
-- ----------------------------
INSERT INTO `roles` VALUES (1, 'super_admin', '超级管理员', '拥有所有权限的系统管理员', 1, 1, 1, '2026-04-08 16:10:07', '2026-04-08 16:10:07', '');
INSERT INTO `roles` VALUES (2, 'content_editor', '内容编辑', '负责内容创建与编辑', 0, 2, 1, '2026-04-08 16:10:07', '2026-04-08 16:10:07', '');
INSERT INTO `roles` VALUES (3, 'operator', '运营人员', '日常运营操作', 0, 3, 1, '2026-04-08 16:10:07', '2026-04-08 16:10:07', '');
INSERT INTO `roles` VALUES (4, 'viewer', '只读查看', '仅可查看数据', 0, 4, 1, '2026-04-08 16:10:07', '2026-04-08 16:10:07', '');
INSERT INTO `roles` VALUES (5, 'tester', '测试人员', '测试与调试专用', 0, 5, 1, '2026-04-08 16:10:07', '2026-04-08 16:10:07', '');

-- ----------------------------
-- Table structure for story_chapters
-- ----------------------------
DROP TABLE IF EXISTS `story_chapters`;
CREATE TABLE `story_chapters`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `story_line_id` bigint NOT NULL,
  `chapter_order` int NOT NULL,
  `title_zh` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `media_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `media_url` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `script_zh` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `script_en` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `script_zht` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `unlock_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `unlock_param` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `duration` int NULL DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `_openid` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用于权限管理，请不要删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_story_line`(`story_line_id` ASC) USING BTREE,
  CONSTRAINT `story_chapters_ibfk_1` FOREIGN KEY (`story_line_id`) REFERENCES `story_lines` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of story_chapters
-- ----------------------------

-- ----------------------------
-- Table structure for story_lines
-- ----------------------------
DROP TABLE IF EXISTS `story_lines`;
CREATE TABLE `story_lines`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `name_zh` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `name_en` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `cover_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `banner_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `category` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `difficulty` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `estimated_duration_minutes` int NULL DEFAULT NULL,
  `tags` json NULL,
  `publish_at` timestamp NULL DEFAULT NULL,
  `start_at` timestamp NULL DEFAULT NULL,
  `end_at` timestamp NULL DEFAULT NULL,
  `participation_count` int NULL DEFAULT 0,
  `completion_count` int NULL DEFAULT 0,
  `average_completion_time` int NULL DEFAULT 0,
  `total_chapters` int NULL DEFAULT 0,
  `status` enum('draft','published','archived') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'draft',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NULL DEFAULT 0,
  `_openid` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用于权限管理，请不要删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `code`(`code` ASC) USING BTREE,
  INDEX `idx_code`(`code` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of story_lines
-- ----------------------------
INSERT INTO `story_lines` VALUES (1, 'silk_road', '海上丝路', 'Maritime Silk Road', '以澳门开埠和海上商贸交流为主线，串联妈阁庙、郑家大屋、岗顶剧院与议事亭前地。', 'https://cdn.tripofmacau.com/storylines/silk-road-cover.jpg', 'https://cdn.tripofmacau.com/storylines/silk-road-banner.jpg', 'historical', 'easy', 150, '[\"海上丝路\", \"世界遗产\", \"文化探索\"]', '2026-04-01 09:00:00', '2026-04-01 09:00:00', '2026-12-31 23:59:59', 1240, 632, 7200, 6, 'published', '2026-04-07 22:24:31', '2026-04-08 06:36:42', 0, '');
INSERT INTO `story_lines` VALUES (2, 'east_west', '东西方战事', 'East-West Encounter', '聚焦葡澳防卫史与城市变迁，串联大炮台、东望洋炮台、白鸽巢前地等防御与交流节点。', 'https://cdn.tripofmacau.com/storylines/east-west-cover.jpg', 'https://cdn.tripofmacau.com/storylines/east-west-banner.jpg', 'historical', 'medium', 180, '[\"战事\", \"城市防御\", \"深度游\"]', '2026-04-05 10:00:00', '2026-04-05 10:00:00', '2026-12-31 23:59:59', 980, 417, 8600, 8, 'published', '2026-04-07 22:24:31', '2026-04-08 06:36:42', 0, '');

-- ----------------------------
-- Table structure for storyline_anchor_bindings
-- ----------------------------
DROP TABLE IF EXISTS `storyline_anchor_bindings`;
CREATE TABLE `storyline_anchor_bindings`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `chapter_id` bigint NOT NULL COMMENT '章节',
  `anchor_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '锚点类型',
  `anchor_ref_id` bigint NOT NULL COMMENT '锚点ID',
  `anchor_ref_table` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '锚点表名',
  `binding_role` enum('primary','secondary','optional') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'primary' COMMENT '绑定角色',
  `arrival_trigger` json NULL COMMENT '到达触发',
  `departure_trigger` json NULL COMMENT '离开触发',
  `sort_order` int NULL DEFAULT NULL COMMENT '排序',
  `status` tinyint NULL DEFAULT 1,
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `_openid` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_chapter`(`chapter_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '章节锚点绑定表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of storyline_anchor_bindings
-- ----------------------------

-- ----------------------------
-- Table structure for storyline_city_relations
-- ----------------------------
DROP TABLE IF EXISTS `storyline_city_relations`;
CREATE TABLE `storyline_city_relations`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `storyline_id` bigint NOT NULL COMMENT '故事线',
  `city_code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '城市',
  `primary_city` tinyint NULL DEFAULT 0 COMMENT '是否主城市',
  `chapter_offset` int NULL DEFAULT 0 COMMENT '章节偏移',
  `local_config` json NULL COMMENT '本地化配置',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `_openid` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_storyline`(`storyline_id` ASC) USING BTREE,
  INDEX `idx_city`(`city_code` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '故事线-城市关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of storyline_city_relations
-- ----------------------------

-- ----------------------------
-- Table structure for sys_admin
-- ----------------------------
DROP TABLE IF EXISTS `sys_admin`;
CREATE TABLE `sys_admin`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `password` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `nickname` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `email` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `phone` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `avatar_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `status` enum('active','disabled') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'active',
  `last_login_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `last_login_ip` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NULL DEFAULT 0,
  `_openid` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用于权限管理，请不要删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username`(`username` ASC) USING BTREE,
  INDEX `idx_username`(`username` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_admin
-- ----------------------------
INSERT INTO `sys_admin` VALUES (1, 'admin', '$2a$12$jnKJgAeyWeaCkoINH9IBKePpqi5ydKJgCMn5g6DC2TJaCSKPVW/K2', '超级管理员', 'admin@tripofmacau.com', NULL, NULL, 'active', '2026-04-08 17:13:22', '182.93.5.2', '2026-04-07 22:24:28', '2026-04-08 17:13:21', 0, '');

-- ----------------------------
-- Table structure for sys_admin_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_admin_role`;
CREATE TABLE `sys_admin_role`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `admin_id` bigint NOT NULL,
  `role_id` bigint NOT NULL,
  `_openid` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用于权限管理，请不要删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_admin_role`(`admin_id` ASC, `role_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_admin_role
-- ----------------------------

-- ----------------------------
-- Table structure for sys_config
-- ----------------------------
DROP TABLE IF EXISTS `sys_config`;
CREATE TABLE `sys_config`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `config_key` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `config_value` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `config_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'string',
  `description` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NULL DEFAULT 0,
  `_openid` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用于权限管理，请不要删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `config_key`(`config_key` ASC) USING BTREE,
  INDEX `idx_config_key`(`config_key` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_config
-- ----------------------------
INSERT INTO `sys_config` VALUES (1, 'trigger.outdoor.radius.high', '30', 'int', 'GPS精度<10米时触发半径(米)', '2026-04-07 22:24:33', '2026-04-07 22:24:33', 0, '');
INSERT INTO `sys_config` VALUES (2, 'trigger.outdoor.radius.medium', '50', 'int', 'GPS精度10-20米时触发半径(米)', '2026-04-07 22:24:33', '2026-04-07 22:24:33', 0, '');
INSERT INTO `sys_config` VALUES (3, 'trigger.outdoor.radius.low', '80', 'int', 'GPS精度>20米时触发半径(米)', '2026-04-07 22:24:33', '2026-04-07 22:24:33', 0, '');
INSERT INTO `sys_config` VALUES (4, 'trigger.cooldown.time', '1800000', 'int', '触发冷却时间(毫秒)', '2026-04-07 22:24:33', '2026-04-07 22:24:33', 0, '');
INSERT INTO `sys_config` VALUES (5, 'trigger.debounce.delay', '2000', 'int', '触发防抖延迟(毫秒)', '2026-04-07 22:24:33', '2026-04-07 22:24:33', 0, '');
INSERT INTO `sys_config` VALUES (6, 'gps.interval', '2000', 'int', 'GPS采样间隔(毫秒)', '2026-04-07 22:24:33', '2026-04-07 22:24:33', 0, '');
INSERT INTO `sys_config` VALUES (7, 'manual.checkin.radius', '200', 'int', '手动补签有效半径(米)', '2026-04-07 22:24:33', '2026-04-07 22:24:33', 0, '');
INSERT INTO `sys_config` VALUES (8, 'operation.mvp.storylines', '海上丝路,东西方战事', 'string', '当前 MVP 主推故事线', '2026-04-08 02:35:20', '2026-04-08 02:35:20', 0, '');
INSERT INTO `sys_config` VALUES (9, 'operation.elderly.defaultVoice', 'true', 'boolean', '长者模式默认开启语音播报', '2026-04-08 02:35:20', '2026-04-08 02:35:20', 0, '');
INSERT INTO `sys_config` VALUES (10, 'map.style.default', 'cartoon', 'string', '默认地图风格', '2026-04-08 02:35:20', '2026-04-08 02:35:20', 0, '');

-- ----------------------------
-- Table structure for sys_login_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_login_log`;
CREATE TABLE `sys_login_log`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `admin_id` bigint NULL DEFAULT NULL,
  `username` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `ip` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `user_agent` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `login_status` enum('success','failed') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'success',
  `fail_reason` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `_openid` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用于权限管理，请不要删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_admin`(`admin_id` ASC) USING BTREE,
  INDEX `idx_created`(`created_at` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_login_log
-- ----------------------------
INSERT INTO `sys_login_log` VALUES (1, 1, 'admin', '60.246.231.180', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36', 'success', NULL, '2026-04-08 01:57:07', '');
INSERT INTO `sys_login_log` VALUES (2, 1, 'admin', '60.246.231.180', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36', 'success', NULL, '2026-04-08 06:27:00', '');
INSERT INTO `sys_login_log` VALUES (3, 1, 'admin', '182.93.5.2', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36', 'success', NULL, '2026-04-08 14:17:30', '');
INSERT INTO `sys_login_log` VALUES (4, 1, 'admin', '182.93.5.2', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36', 'success', NULL, '2026-04-08 14:55:38', '');
INSERT INTO `sys_login_log` VALUES (5, 1, 'admin', '60.246.231.180', 'Mozilla/5.0 (Windows NT; Windows NT 10.0; zh-TW) WindowsPowerShell/5.1.26100.7920', 'success', NULL, '2026-04-08 15:32:33', '');
INSERT INTO `sys_login_log` VALUES (6, 1, 'admin', '182.93.5.2', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36', 'success', NULL, '2026-04-08 17:13:22', '');

-- ----------------------------
-- Table structure for sys_operation_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_operation_log`;
CREATE TABLE `sys_operation_log`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `admin_id` bigint NULL DEFAULT NULL,
  `admin_username` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `module` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `operation` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `request_method` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `request_url` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `request_params` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `response_data` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `ip` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `_openid` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用于权限管理，请不要删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_admin`(`admin_id` ASC) USING BTREE,
  INDEX `idx_created`(`created_at` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_operation_log
-- ----------------------------

-- ----------------------------
-- Table structure for sys_permission
-- ----------------------------
DROP TABLE IF EXISTS `sys_permission`;
CREATE TABLE `sys_permission`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `parent_id` bigint NULL DEFAULT 0,
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `permission_key` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `permission_type` enum('menu','button','api') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'menu',
  `path` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `icon` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `sort_order` int NULL DEFAULT 0,
  `status` enum('active','disabled') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'active',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `deleted` tinyint NULL DEFAULT 0,
  `_openid` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用于权限管理，请不要删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `permission_key`(`permission_key` ASC) USING BTREE,
  INDEX `idx_permission_key`(`permission_key` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_permission
-- ----------------------------

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `role_code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `role_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `status` enum('active','disabled') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'active',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NULL DEFAULT 0,
  `_openid` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用于权限管理，请不要删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `role_code`(`role_code` ASC) USING BTREE,
  INDEX `idx_role_code`(`role_code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_role
-- ----------------------------
INSERT INTO `sys_role` VALUES (1, 'super_admin', '超级管理员', '拥有所有权限', 'active', '2026-04-07 22:24:30', '2026-04-07 22:24:30', 0, '');
INSERT INTO `sys_role` VALUES (2, 'content_admin', '内容运营', '内容管理、故事线管理', 'active', '2026-04-07 22:24:30', '2026-04-07 22:24:30', 0, '');
INSERT INTO `sys_role` VALUES (3, 'operation_admin', '运营人员', '活动管理、奖励配置', 'active', '2026-04-07 22:24:30', '2026-04-07 22:24:30', 0, '');
INSERT INTO `sys_role` VALUES (4, 'tester', '测试人员', '测试工具、数据调整', 'active', '2026-04-07 22:24:30', '2026-04-07 22:24:30', 0, '');
INSERT INTO `sys_role` VALUES (5, 'data_analyst', '数据分析', '数据查看、导出', 'active', '2026-04-07 22:24:30', '2026-04-07 22:24:30', 0, '');

-- ----------------------------
-- Table structure for sys_role_permission
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_permission`;
CREATE TABLE `sys_role_permission`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `role_id` bigint NOT NULL,
  `permission_id` bigint NOT NULL,
  `_openid` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用于权限管理，请不要删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_role_permission`(`role_id` ASC, `permission_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_role_permission
-- ----------------------------

-- ----------------------------
-- Table structure for test_accounts
-- ----------------------------
DROP TABLE IF EXISTS `test_accounts`;
CREATE TABLE `test_accounts`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `test_group` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `mock_latitude` decimal(10, 8) NULL DEFAULT NULL,
  `mock_longitude` decimal(11, 8) NULL DEFAULT NULL,
  `mock_enabled` tinyint(1) NULL DEFAULT 0,
  `mock_poi_id` bigint NULL DEFAULT NULL,
  `notes` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NULL DEFAULT 0,
  `_openid` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用于权限管理，请不要删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_user`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of test_accounts
-- ----------------------------

-- ----------------------------
-- Table structure for test_operation_logs
-- ----------------------------
DROP TABLE IF EXISTS `test_operation_logs`;
CREATE TABLE `test_operation_logs`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `test_account_id` bigint NOT NULL,
  `operator_id` bigint NOT NULL,
  `operation_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `before_data` json NULL,
  `after_data` json NULL,
  `request_params` json NULL,
  `ip` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `_openid` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用于权限管理，请不要删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_test_account`(`test_account_id` ASC) USING BTREE,
  INDEX `idx_created`(`created_at` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of test_operation_logs
-- ----------------------------

-- ----------------------------
-- Table structure for trigger_logs
-- ----------------------------
DROP TABLE IF EXISTS `trigger_logs`;
CREATE TABLE `trigger_logs`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `poi_id` bigint NOT NULL,
  `trigger_type` enum('auto','manual') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `distance` decimal(10, 2) NULL DEFAULT NULL,
  `gps_accuracy` decimal(10, 2) NULL DEFAULT NULL,
  `wifi_used` tinyint(1) NULL DEFAULT 0,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `_openid` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用于权限管理，请不要删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_poi`(`user_id` ASC, `poi_id` ASC) USING BTREE,
  INDEX `idx_created`(`created_at` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of trigger_logs
-- ----------------------------

-- ----------------------------
-- Table structure for user_badges
-- ----------------------------
DROP TABLE IF EXISTS `user_badges`;
CREATE TABLE `user_badges`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `openid` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户',
  `badge_id` bigint NOT NULL COMMENT '徽章',
  `granted_at` datetime NULL DEFAULT NULL COMMENT '获得时间',
  `grant_type` enum('auto','admin','event') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '发放方式',
  `grant_reason` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '原因',
  `revoked` tinyint NULL DEFAULT 0 COMMENT '是否回收',
  `revoked_at` datetime NULL DEFAULT NULL COMMENT '回收时间',
  `revoked_reason` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '回收原因',
  `_openid` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_badge`(`openid` ASC, `badge_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户徽章记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_badges
-- ----------------------------

-- ----------------------------
-- Table structure for user_city_progress
-- ----------------------------
DROP TABLE IF EXISTS `user_city_progress`;
CREATE TABLE `user_city_progress`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `openid` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户',
  `city_code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '城市',
  `unlocked` tinyint NULL DEFAULT 0 COMMENT '已解锁',
  `unlocked_at` datetime NULL DEFAULT NULL COMMENT '解锁时间',
  `total_pois` int NULL DEFAULT 0 COMMENT '城市总POI数',
  `checked_pois` int NULL DEFAULT 0 COMMENT '已打卡POI',
  `progress_pct` decimal(5, 2) NULL DEFAULT 0.00 COMMENT '进度百分比',
  `total_stamps` int NULL DEFAULT 0 COMMENT '总印章',
  `current_streak_days` int NULL DEFAULT 0 COMMENT '连续天数',
  `last_active_at` datetime NULL DEFAULT NULL COMMENT '最后活跃',
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `_openid` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_city`(`openid` ASC, `city_code` ASC) USING BTREE,
  INDEX `idx_openid`(`openid` ASC) USING BTREE,
  INDEX `idx_city`(`city_code` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户城市探索进度' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_city_progress
-- ----------------------------

-- ----------------------------
-- Table structure for user_collectible_progress
-- ----------------------------
DROP TABLE IF EXISTS `user_collectible_progress`;
CREATE TABLE `user_collectible_progress`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `openid` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户',
  `collectible_id` bigint NOT NULL COMMENT '收集物',
  `owned_count` int NULL DEFAULT 0 COMMENT '拥有数量',
  `first_acquired_at` datetime NULL DEFAULT NULL COMMENT '首次获得',
  `last_acquired_at` datetime NULL DEFAULT NULL COMMENT '最近获得',
  `acquisition_source` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '来源',
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `_openid` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_collect`(`openid` ASC, `collectible_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户收集物进度' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_collectible_progress
-- ----------------------------

-- ----------------------------
-- Table structure for user_stamps
-- ----------------------------
DROP TABLE IF EXISTS `user_stamps`;
CREATE TABLE `user_stamps`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `stamp_type` enum('location','story','mission','secret','team') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `source_id` bigint NOT NULL,
  `checkin_type` enum('gps','wifi','manual') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'gps',
  `latitude` decimal(10, 8) NULL DEFAULT NULL,
  `longitude` decimal(11, 8) NULL DEFAULT NULL,
  `collected_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `_openid` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用于权限管理，请不要删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user`(`user_id` ASC) USING BTREE,
  INDEX `idx_source`(`stamp_type` ASC, `source_id` ASC) USING BTREE,
  CONSTRAINT `user_stamps_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_stamps
-- ----------------------------

-- ----------------------------
-- Table structure for user_story_progress
-- ----------------------------
DROP TABLE IF EXISTS `user_story_progress`;
CREATE TABLE `user_story_progress`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `openid` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户',
  `storyline_id` bigint NOT NULL COMMENT '故事线',
  `status` enum('not_started','in_progress','completed','abandoned') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'not_started' COMMENT '状态',
  `current_chapter_id` bigint NULL DEFAULT NULL COMMENT '当前章节',
  `completed_chapter_ids` json NULL COMMENT '已完成章节ID列表',
  `started_at` datetime NULL DEFAULT NULL COMMENT '开始时间',
  `completed_at` datetime NULL DEFAULT NULL COMMENT '完成时间',
  `total_spent_minutes` int NULL DEFAULT 0 COMMENT '总用时(分)',
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `_openid` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_story`(`openid` ASC, `storyline_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户故事线进度' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_story_progress
-- ----------------------------

-- ----------------------------
-- Table structure for user_trigger_logs
-- ----------------------------
DROP TABLE IF EXISTS `user_trigger_logs`;
CREATE TABLE `user_trigger_logs`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `openid` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户',
  `trigger_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '触发类型',
  `ref_id` bigint NULL DEFAULT NULL COMMENT '关联对象ID',
  `ref_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '关联类型',
  `city_code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '城市',
  `lat` decimal(10, 6) NULL DEFAULT NULL COMMENT '触发位置纬度',
  `lng` decimal(11, 6) NULL DEFAULT NULL COMMENT '触发位置经度',
  `payload` json NULL COMMENT '详细数据',
  `success` tinyint NULL DEFAULT 1 COMMENT '是否成功',
  `error_message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '错误信息',
  `client_version` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '客户端版本',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `_openid` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_openid`(`openid` ASC) USING BTREE,
  INDEX `idx_type`(`ref_type` ASC) USING BTREE,
  INDEX `idx_created`(`created_at` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户触发日志' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_trigger_logs
-- ----------------------------

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `open_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `nickname` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `avatar_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `language_preference` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'zh_CN',
  `level` int NULL DEFAULT 1,
  `title` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '探索新手',
  `total_stamps` int NULL DEFAULT 0,
  `interface_mode` enum('standard','elderly') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'standard',
  `font_scale` decimal(2, 1) NULL DEFAULT 1.0,
  `high_contrast` tinyint(1) NULL DEFAULT 0,
  `voice_guide_enabled` tinyint(1) NULL DEFAULT 0,
  `simplified_mode` tinyint(1) NULL DEFAULT 0,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NULL DEFAULT 0,
  `_openid` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用于权限管理，请不要删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `open_id`(`open_id` ASC) USING BTREE,
  INDEX `idx_open_id`(`open_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of users
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;
