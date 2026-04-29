CREATE TABLE IF NOT EXISTS activities (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(64) NOT NULL,
    title VARCHAR(255),
    description TEXT,
    cover_url VARCHAR(1024),
    start_time DATETIME NULL,
    end_time DATETIME NULL,
    status VARCHAR(32) DEFAULT 'draft',
    participation_count INT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_activities_code (code),
    KEY idx_activities_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

ALTER TABLE activities
    ADD COLUMN activity_type VARCHAR(32) NOT NULL DEFAULT 'official_event' AFTER code,
    ADD COLUMN title_zh VARCHAR(255) NULL AFTER activity_type,
    ADD COLUMN title_en VARCHAR(255) NULL AFTER title_zh,
    ADD COLUMN title_zht VARCHAR(255) NULL AFTER title_en,
    ADD COLUMN title_pt VARCHAR(255) NULL AFTER title_zht,
    ADD COLUMN summary_zh TEXT NULL AFTER title_pt,
    ADD COLUMN summary_en TEXT NULL AFTER summary_zh,
    ADD COLUMN summary_zht TEXT NULL AFTER summary_en,
    ADD COLUMN summary_pt TEXT NULL AFTER summary_zht,
    ADD COLUMN description_zh LONGTEXT NULL AFTER summary_pt,
    ADD COLUMN description_en LONGTEXT NULL AFTER description_zh,
    ADD COLUMN description_zht LONGTEXT NULL AFTER description_en,
    ADD COLUMN description_pt LONGTEXT NULL AFTER description_zht,
    ADD COLUMN html_zh LONGTEXT NULL AFTER description_pt,
    ADD COLUMN html_en LONGTEXT NULL AFTER html_zh,
    ADD COLUMN html_zht LONGTEXT NULL AFTER html_en,
    ADD COLUMN html_pt LONGTEXT NULL AFTER html_zht,
    ADD COLUMN venue_name_zh VARCHAR(255) NULL AFTER html_pt,
    ADD COLUMN venue_name_en VARCHAR(255) NULL AFTER venue_name_zh,
    ADD COLUMN venue_name_zht VARCHAR(255) NULL AFTER venue_name_en,
    ADD COLUMN venue_name_pt VARCHAR(255) NULL AFTER venue_name_zht,
    ADD COLUMN address_zh VARCHAR(255) NULL AFTER venue_name_pt,
    ADD COLUMN address_en VARCHAR(255) NULL AFTER address_zh,
    ADD COLUMN address_zht VARCHAR(255) NULL AFTER address_en,
    ADD COLUMN address_pt VARCHAR(255) NULL AFTER address_zht,
    ADD COLUMN organizer_name VARCHAR(255) NULL AFTER address_pt,
    ADD COLUMN organizer_contact VARCHAR(255) NULL AFTER organizer_name,
    ADD COLUMN organizer_website VARCHAR(512) NULL AFTER organizer_contact,
    ADD COLUMN signup_capacity INT NULL AFTER organizer_website,
    ADD COLUMN signup_fee_amount DECIMAL(10,2) NULL AFTER signup_capacity,
    ADD COLUMN signup_start_at DATETIME NULL AFTER signup_fee_amount,
    ADD COLUMN signup_end_at DATETIME NULL AFTER signup_start_at,
    ADD COLUMN publish_start_at DATETIME NULL AFTER signup_end_at,
    ADD COLUMN publish_end_at DATETIME NULL AFTER publish_start_at,
    ADD COLUMN is_pinned TINYINT(1) NOT NULL DEFAULT 0 AFTER publish_end_at,
    ADD COLUMN cover_asset_id BIGINT NULL AFTER is_pinned,
    ADD COLUMN hero_asset_id BIGINT NULL AFTER cover_asset_id,
    ADD COLUMN sort_order INT NOT NULL DEFAULT 0 AFTER hero_asset_id;

UPDATE activities
SET title_zh = COALESCE(NULLIF(title_zh, ''), title),
    title_zht = COALESCE(NULLIF(title_zht, ''), title_zh, title),
    summary_zh = COALESCE(NULLIF(summary_zh, ''), description),
    summary_zht = COALESCE(NULLIF(summary_zht, ''), summary_zh, description),
    description_zh = COALESCE(NULLIF(description_zh, ''), description),
    description_zht = COALESCE(NULLIF(description_zht, ''), description_zh, description),
    publish_start_at = COALESCE(publish_start_at, start_time),
    publish_end_at = COALESCE(publish_end_at, end_time)
WHERE activity_type = 'official_event'
   OR activity_type IS NOT NULL;

CREATE INDEX idx_activities_type_status ON activities (activity_type, status);
CREATE INDEX idx_activities_publish_window ON activities (publish_start_at, publish_end_at);
CREATE INDEX idx_activities_pinned_sort ON activities (is_pinned, sort_order, id);
