-- =============================================
-- 澳小遊 Trip of Macau - 数据库初始化脚本
-- 版本: 1.0
-- 日期: 2026-04-06
-- CloudBase MySQL 连接信息
-- 主机: 172.17.16.11:3306
-- 数据库: macau-trip-2gn2zm5jefa4a987
-- =============================================

USE `macau-trip-2gn2zm5jefa4a987`;

-- =============================================
-- 小程序业务表
-- =============================================

-- 用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    open_id VARCHAR(64) UNIQUE NOT NULL COMMENT '微信OpenID',
    nickname VARCHAR(64) COMMENT '昵称',
    avatar_url VARCHAR(512) COMMENT '头像URL',
    language_preference VARCHAR(10) DEFAULT 'zh_CN' COMMENT '语言偏好',
    level INT DEFAULT 1 COMMENT '等级',
    title VARCHAR(32) DEFAULT '探索新手' COMMENT '头衔',
    total_stamps INT DEFAULT 0 COMMENT '总印章数',
    interface_mode ENUM('standard', 'elderly') DEFAULT 'standard' COMMENT '界面模式',
    font_scale DECIMAL(2,1) DEFAULT 1.0 COMMENT '字体缩放',
    high_contrast BOOLEAN DEFAULT FALSE COMMENT '高对比度',
    voice_guide_enabled BOOLEAN DEFAULT FALSE COMMENT '语音导览',
    simplified_mode BOOLEAN DEFAULT FALSE COMMENT '简化模式',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_open_id (open_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- POI景点表
CREATE TABLE IF NOT EXISTS pois (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    name_zh VARCHAR(128) NOT NULL COMMENT '中文名称',
    name_en VARCHAR(128) COMMENT '英文名称',
    name_zht VARCHAR(128) COMMENT '繁体名称',
    latitude DECIMAL(10,8) NOT NULL COMMENT '纬度',
    longitude DECIMAL(11,8) NOT NULL COMMENT '经度',
    address VARCHAR(256) COMMENT '地址',
    category_id BIGINT COMMENT '分类ID',
    trigger_radius INT DEFAULT 30 COMMENT '触发半径(米)',
    importance ENUM('normal', 'important', 'very_important') DEFAULT 'normal' COMMENT '重要性',
    story_line_id BIGINT COMMENT '关联故事线ID',
    stamp_type VARCHAR(32) COMMENT '印章类型',
    description TEXT COMMENT '描述',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_location (latitude, longitude),
    INDEX idx_category (category_id),
    INDEX idx_story_line (story_line_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='POI景点表';

-- POI分类表
CREATE TABLE IF NOT EXISTS poi_categories (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(64) NOT NULL COMMENT '分类名称',
    icon VARCHAR(128) COMMENT '图标',
    sort_order INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 印章记录表
CREATE TABLE IF NOT EXISTS user_stamps (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    stamp_type ENUM('location', 'story', 'mission', 'secret', 'team') NOT NULL COMMENT '印章类型',
    source_id BIGINT NOT NULL COMMENT '来源ID',
    checkin_type ENUM('gps', 'wifi', 'manual') DEFAULT 'gps' COMMENT '签到方式',
    latitude DECIMAL(10,8) COMMENT '签到纬度',
    longitude DECIMAL(11,8) COMMENT '签到经度',
    collected_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user (user_id),
    INDEX idx_source (stamp_type, source_id),
    FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 触发日志表
CREATE TABLE IF NOT EXISTS trigger_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    poi_id BIGINT NOT NULL,
    trigger_type ENUM('auto', 'manual') NOT NULL,
    distance DECIMAL(10,2) COMMENT '触发时距离',
    gps_accuracy DECIMAL(10,2) COMMENT 'GPS精度',
    wifi_used BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_poi (user_id, poi_id),
    INDEX idx_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 故事线表
CREATE TABLE IF NOT EXISTS story_lines (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(32) UNIQUE NOT NULL COMMENT '故事线编码',
    name_zh VARCHAR(64) NOT NULL COMMENT '中文名称',
    name_en VARCHAR(64) COMMENT '英文名称',
    description TEXT COMMENT '描述',
    cover_url VARCHAR(512) COMMENT '封面URL',
    total_chapters INT DEFAULT 0 COMMENT '总章节数',
    status ENUM('draft', 'published', 'archived') DEFAULT 'draft',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_code (code),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 章节表
CREATE TABLE IF NOT EXISTS story_chapters (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    story_line_id BIGINT NOT NULL,
    chapter_order INT NOT NULL COMMENT '章节顺序',
    title_zh VARCHAR(128) NOT NULL,
    media_type VARCHAR(32) NOT NULL COMMENT '视频/音频/图文',
    media_url VARCHAR(256) NOT NULL,
    script_zh TEXT COMMENT '中文剧本',
    script_en TEXT,
    script_zht TEXT,
    unlock_type VARCHAR(32) NOT NULL COMMENT '解锁条件类型',
    unlock_param VARCHAR(128) COMMENT '解锁条件参数',
    duration INT COMMENT '时长(秒)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_story_line (story_line_id),
    FOREIGN KEY (story_line_id) REFERENCES story_lines(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 用户故事进度表
CREATE TABLE IF NOT EXISTS user_story_progress (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    story_line_id BIGINT NOT NULL,
    current_chapter INT DEFAULT 1,
    completed_chapters JSON COMMENT '已完成章节ID列表',
    completed_at TIMESTAMP,
    UNIQUE KEY uk_user_story (user_id, story_line_id),
    INDEX idx_user (user_id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (story_line_id) REFERENCES story_lines(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 奖励表
CREATE TABLE IF NOT EXISTS rewards (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name_zh VARCHAR(64) NOT NULL,
    description TEXT,
    stamps_required INT COMMENT '所需印章数',
    total_quantity INT COMMENT '总数量',
    redeemed_count INT DEFAULT 0 COMMENT '已兑换数量',
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    status ENUM('active', 'inactive') DEFAULT 'active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 用户奖励领取表
CREATE TABLE IF NOT EXISTS user_rewards (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    reward_id BIGINT NOT NULL,
    qr_code VARCHAR(256) COMMENT '二维码',
    redeemed BOOLEAN DEFAULT FALSE,
    redeemed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user (user_id),
    INDEX idx_reward (reward_id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (reward_id) REFERENCES rewards(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 地图瓦片配置表
CREATE TABLE IF NOT EXISTS map_tile_configs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    map_id VARCHAR(32) UNIQUE NOT NULL,
    style VARCHAR(32) DEFAULT 'cartoon',
    cdn_base VARCHAR(256) NOT NULL,
    control_points_url VARCHAR(256) COMMENT '控制点JSON URL',
    pois_url VARCHAR(256) COMMENT 'POI数据URL',
    zoom_levels VARCHAR(128) DEFAULT '[1,2,3,4]',
    status ENUM('active', 'inactive') DEFAULT 'active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_map_id (map_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================
-- 后台管理系统表结构
-- =============================================

-- 管理员表
CREATE TABLE IF NOT EXISTS sys_admin (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(64) UNIQUE NOT NULL,
    password VARCHAR(256) NOT NULL,
    nickname VARCHAR(64),
    email VARCHAR(128),
    phone VARCHAR(32),
    avatar_url VARCHAR(512),
    status ENUM('active', 'disabled') DEFAULT 'active',
    last_login_at TIMESTAMP,
    last_login_ip VARCHAR(64),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_username (username),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 角色表
CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_code VARCHAR(32) UNIQUE NOT NULL,
    role_name VARCHAR(64) NOT NULL,
    description VARCHAR(256),
    status ENUM('active', 'disabled') DEFAULT 'active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 权限表
CREATE TABLE IF NOT EXISTS sys_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    parent_id BIGINT DEFAULT 0,
    name VARCHAR(64) NOT NULL,
    permission_key VARCHAR(128) UNIQUE NOT NULL,
    permission_type ENUM('menu', 'button', 'api') DEFAULT 'menu',
    path VARCHAR(128),
    icon VARCHAR(64),
    sort_order INT DEFAULT 0,
    status ENUM('active', 'disabled') DEFAULT 'active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_permission_key (permission_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 管理员-角色关联表
CREATE TABLE IF NOT EXISTS sys_admin_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    admin_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    UNIQUE KEY uk_admin_role (admin_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 角色-权限关联表
CREATE TABLE IF NOT EXISTS sys_role_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    UNIQUE KEY uk_role_permission (role_id, permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 登录日志表
CREATE TABLE IF NOT EXISTS sys_login_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    admin_id BIGINT,
    username VARCHAR(64),
    ip VARCHAR(64),
    user_agent VARCHAR(256),
    login_status ENUM('success', 'failed') DEFAULT 'success',
    fail_reason VARCHAR(256),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_admin (admin_id),
    INDEX idx_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 操作日志表
CREATE TABLE IF NOT EXISTS sys_operation_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    admin_id BIGINT,
    admin_username VARCHAR(64),
    module VARCHAR(64),
    operation VARCHAR(128),
    request_method VARCHAR(16),
    request_url VARCHAR(256),
    request_params TEXT,
    response_data TEXT,
    ip VARCHAR(64),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_admin (admin_id),
    INDEX idx_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 测试账号标记表
CREATE TABLE IF NOT EXISTS test_accounts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL UNIQUE COMMENT '关联小程序用户ID',
    test_group VARCHAR(32) COMMENT '测试分组',
    mock_latitude DECIMAL(10,8) COMMENT '模拟纬度',
    mock_longitude DECIMAL(11,8) COMMENT '模拟经度',
    mock_enabled BOOLEAN DEFAULT FALSE COMMENT '是否启用模拟定位',
    mock_poi_id BIGINT COMMENT '模拟触发的POI',
    notes TEXT COMMENT '备注',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 测试操作日志表
CREATE TABLE IF NOT EXISTS test_operation_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    test_account_id BIGINT NOT NULL,
    operator_id BIGINT NOT NULL COMMENT '操作人管理员ID',
    operation_type VARCHAR(64) NOT NULL COMMENT '操作类型',
    before_data JSON COMMENT '操作前数据',
    after_data JSON COMMENT '操作后数据',
    request_params JSON COMMENT '请求参数',
    ip VARCHAR(64),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_test_account (test_account_id),
    INDEX idx_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 系统配置表
CREATE TABLE IF NOT EXISTS sys_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    config_key VARCHAR(128) UNIQUE NOT NULL,
    config_value TEXT,
    config_type VARCHAR(32) DEFAULT 'string',
    description VARCHAR(256),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_config_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 活动表
CREATE TABLE IF NOT EXISTS activities (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(32) UNIQUE NOT NULL,
    title VARCHAR(128) NOT NULL,
    description TEXT,
    cover_url VARCHAR(512),
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    status ENUM('draft', 'published', 'ended', 'cancelled') DEFAULT 'draft',
    participation_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_code (code),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================
-- 初始数据
-- =============================================

-- 插入默认超级管理员 (密码: admin123, BCrypt加密)
INSERT INTO sys_admin (username, password, nickname, email, status) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '超级管理员', 'admin@tripofmacau.com', 'active');

-- 插入角色
INSERT INTO sys_role (role_code, role_name, description) VALUES
('super_admin', '超级管理员', '拥有所有权限'),
('content_admin', '内容运营', '内容管理、故事线管理'),
('operation_admin', '运营人员', '活动管理、奖励配置'),
('tester', '测试人员', '测试工具、数据调整'),
('data_analyst', '数据分析', '数据查看、导出');

-- 故事线初始数据
INSERT INTO story_lines (code, name_zh, name_en, description, status) VALUES
('silk_road', '海上丝路', 'Maritime Silk Road', '探索澳门在海上丝绸之路的历史角色', 'published'),
('east_west', '东西方战事', 'East-West Encounter', '澳门四百年的东西方交流史', 'published');

-- 插入默认地图配置
INSERT INTO map_tile_configs (map_id, style, cdn_base, status) VALUES
('macau_peninsula', 'cartoon', 'https://cdn.tripofmacau.com/maps/macau', 'active');

-- 系统配置初始数据
INSERT INTO sys_config (config_key, config_value, config_type, description) VALUES
('trigger.outdoor.radius.high', '30', 'int', 'GPS精度<10米时触发半径(米)'),
('trigger.outdoor.radius.medium', '50', 'int', 'GPS精度10-20米时触发半径(米)'),
('trigger.outdoor.radius.low', '80', 'int', 'GPS精度>20米时触发半径(米)'),
('trigger.cooldown.time', '1800000', 'int', '触发冷却时间(毫秒),默认30分钟'),
('trigger.debounce.delay', '2000', 'int', '触发防抖延迟(毫秒)'),
('gps.interval', '2000', 'int', 'GPS采样间隔(毫秒)'),
('manual.checkin.radius', '200', 'int', '手动补签有效半径(米)');
