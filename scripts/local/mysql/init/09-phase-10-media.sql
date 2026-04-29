-- Phase 10 media asset pipeline and central library alignment

SET @col_original_filename_exists = (
  SELECT COUNT(1)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'content_assets'
    AND column_name = 'original_filename'
);
SET @sql_add_original_filename = IF(
  @col_original_filename_exists = 0,
  'ALTER TABLE content_assets ADD COLUMN `original_filename` VARCHAR(255) NOT NULL DEFAULT '''' AFTER `locale_code`',
  'SELECT 1'
);
PREPARE stmt_add_original_filename FROM @sql_add_original_filename;
EXECUTE stmt_add_original_filename;
DEALLOCATE PREPARE stmt_add_original_filename;

SET @col_file_extension_exists = (
  SELECT COUNT(1)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'content_assets'
    AND column_name = 'file_extension'
);
SET @sql_add_file_extension = IF(
  @col_file_extension_exists = 0,
  'ALTER TABLE content_assets ADD COLUMN `file_extension` VARCHAR(32) NOT NULL DEFAULT '''' AFTER `original_filename`',
  'SELECT 1'
);
PREPARE stmt_add_file_extension FROM @sql_add_file_extension;
EXECUTE stmt_add_file_extension;
DEALLOCATE PREPARE stmt_add_file_extension;

SET @col_upload_source_exists = (
  SELECT COUNT(1)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'content_assets'
    AND column_name = 'upload_source'
);
SET @sql_add_upload_source = IF(
  @col_upload_source_exists = 0,
  'ALTER TABLE content_assets ADD COLUMN `upload_source` VARCHAR(32) NOT NULL DEFAULT ''picker'' AFTER `file_extension`',
  'SELECT 1'
);
PREPARE stmt_add_upload_source FROM @sql_add_upload_source;
EXECUTE stmt_add_upload_source;
DEALLOCATE PREPARE stmt_add_upload_source;

SET @col_client_relative_path_exists = (
  SELECT COUNT(1)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'content_assets'
    AND column_name = 'client_relative_path'
);
SET @sql_add_client_relative_path = IF(
  @col_client_relative_path_exists = 0,
  'ALTER TABLE content_assets ADD COLUMN `client_relative_path` VARCHAR(1024) NULL AFTER `upload_source`',
  'SELECT 1'
);
PREPARE stmt_add_client_relative_path FROM @sql_add_client_relative_path;
EXECUTE stmt_add_client_relative_path;
DEALLOCATE PREPARE stmt_add_client_relative_path;

SET @col_uploaded_by_admin_id_exists = (
  SELECT COUNT(1)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'content_assets'
    AND column_name = 'uploaded_by_admin_id'
);
SET @sql_add_uploaded_by_admin_id = IF(
  @col_uploaded_by_admin_id_exists = 0,
  'ALTER TABLE content_assets ADD COLUMN `uploaded_by_admin_id` BIGINT NULL AFTER `client_relative_path`',
  'SELECT 1'
);
PREPARE stmt_add_uploaded_by_admin_id FROM @sql_add_uploaded_by_admin_id;
EXECUTE stmt_add_uploaded_by_admin_id;
DEALLOCATE PREPARE stmt_add_uploaded_by_admin_id;

SET @col_uploaded_by_admin_name_exists = (
  SELECT COUNT(1)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'content_assets'
    AND column_name = 'uploaded_by_admin_name'
);
SET @sql_add_uploaded_by_admin_name = IF(
  @col_uploaded_by_admin_name_exists = 0,
  'ALTER TABLE content_assets ADD COLUMN `uploaded_by_admin_name` VARCHAR(128) NOT NULL DEFAULT '''' AFTER `uploaded_by_admin_id`',
  'SELECT 1'
);
PREPARE stmt_add_uploaded_by_admin_name FROM @sql_add_uploaded_by_admin_name;
EXECUTE stmt_add_uploaded_by_admin_name;
DEALLOCATE PREPARE stmt_add_uploaded_by_admin_name;

SET @col_processing_policy_code_exists = (
  SELECT COUNT(1)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'content_assets'
    AND column_name = 'processing_policy_code'
);
SET @sql_add_processing_policy_code = IF(
  @col_processing_policy_code_exists = 0,
  'ALTER TABLE content_assets ADD COLUMN `processing_policy_code` VARCHAR(64) NOT NULL DEFAULT ''passthrough'' AFTER `etag`',
  'SELECT 1'
);
PREPARE stmt_add_processing_policy_code FROM @sql_add_processing_policy_code;
EXECUTE stmt_add_processing_policy_code;
DEALLOCATE PREPARE stmt_add_processing_policy_code;

SET @col_processing_profile_json_exists = (
  SELECT COUNT(1)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'content_assets'
    AND column_name = 'processing_profile_json'
);
SET @sql_add_processing_profile_json = IF(
  @col_processing_profile_json_exists = 0,
  'ALTER TABLE content_assets ADD COLUMN `processing_profile_json` JSON NULL AFTER `processing_policy_code`',
  'SELECT 1'
);
PREPARE stmt_add_processing_profile_json FROM @sql_add_processing_profile_json;
EXECUTE stmt_add_processing_profile_json;
DEALLOCATE PREPARE stmt_add_processing_profile_json;

SET @col_processing_status_exists = (
  SELECT COUNT(1)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'content_assets'
    AND column_name = 'processing_status'
);
SET @sql_add_processing_status = IF(
  @col_processing_status_exists = 0,
  'ALTER TABLE content_assets ADD COLUMN `processing_status` VARCHAR(32) NOT NULL DEFAULT ''stored'' AFTER `processing_profile_json`',
  'SELECT 1'
);
PREPARE stmt_add_processing_status FROM @sql_add_processing_status;
EXECUTE stmt_add_processing_status;
DEALLOCATE PREPARE stmt_add_processing_status;

SET @col_processing_note_exists = (
  SELECT COUNT(1)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'content_assets'
    AND column_name = 'processing_note'
);
SET @sql_add_processing_note = IF(
  @col_processing_note_exists = 0,
  'ALTER TABLE content_assets ADD COLUMN `processing_note` TEXT NULL AFTER `processing_status`',
  'SELECT 1'
);
PREPARE stmt_add_processing_note FROM @sql_add_processing_note;
EXECUTE stmt_add_processing_note;
DEALLOCATE PREPARE stmt_add_processing_note;

SET @col_published_at_exists = (
  SELECT COUNT(1)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'content_assets'
    AND column_name = 'published_at'
);
SET @sql_add_published_at = IF(
  @col_published_at_exists = 0,
  'ALTER TABLE content_assets ADD COLUMN `published_at` DATETIME NULL AFTER `status`',
  'SELECT 1'
);
PREPARE stmt_add_published_at FROM @sql_add_published_at;
EXECUTE stmt_add_published_at;
DEALLOCATE PREPARE stmt_add_published_at;

SET @idx_upload_source_exists = (
  SELECT COUNT(1)
  FROM information_schema.statistics
  WHERE table_schema = DATABASE()
    AND table_name = 'content_assets'
    AND index_name = 'idx_content_assets_upload_source'
);
SET @sql_idx_upload_source = IF(
  @idx_upload_source_exists = 0,
  'ALTER TABLE content_assets ADD KEY `idx_content_assets_upload_source` (`upload_source`)',
  'SELECT 1'
);
PREPARE stmt_idx_upload_source FROM @sql_idx_upload_source;
EXECUTE stmt_idx_upload_source;
DEALLOCATE PREPARE stmt_idx_upload_source;

SET @idx_processing_policy_exists = (
  SELECT COUNT(1)
  FROM information_schema.statistics
  WHERE table_schema = DATABASE()
    AND table_name = 'content_assets'
    AND index_name = 'idx_content_assets_processing_policy'
);
SET @sql_idx_processing_policy = IF(
  @idx_processing_policy_exists = 0,
  'ALTER TABLE content_assets ADD KEY `idx_content_assets_processing_policy` (`processing_policy_code`)',
  'SELECT 1'
);
PREPARE stmt_idx_processing_policy FROM @sql_idx_processing_policy;
EXECUTE stmt_idx_processing_policy;
DEALLOCATE PREPARE stmt_idx_processing_policy;

SET @idx_uploaded_by_admin_exists = (
  SELECT COUNT(1)
  FROM information_schema.statistics
  WHERE table_schema = DATABASE()
    AND table_name = 'content_assets'
    AND index_name = 'idx_content_assets_uploaded_by_admin_id'
);
SET @sql_idx_uploaded_by_admin = IF(
  @idx_uploaded_by_admin_exists = 0,
  'ALTER TABLE content_assets ADD KEY `idx_content_assets_uploaded_by_admin_id` (`uploaded_by_admin_id`)',
  'SELECT 1'
);
PREPARE stmt_idx_uploaded_by_admin FROM @sql_idx_uploaded_by_admin;
EXECUTE stmt_idx_uploaded_by_admin;
DEALLOCATE PREPARE stmt_idx_uploaded_by_admin;

UPDATE content_assets
SET
  `original_filename` = COALESCE(NULLIF(`original_filename`, ''), SUBSTRING_INDEX(`object_key`, '/', -1)),
  `file_extension` = COALESCE(NULLIF(`file_extension`, ''), LOWER(SUBSTRING_INDEX(SUBSTRING_INDEX(`object_key`, '/', -1), '.', -1))),
  `upload_source` = COALESCE(NULLIF(`upload_source`, ''), 'picker'),
  `uploaded_by_admin_name` = COALESCE(NULLIF(`uploaded_by_admin_name`, ''), '系統上傳'),
  `processing_policy_code` = COALESCE(NULLIF(`processing_policy_code`, ''), 'passthrough'),
  `processing_status` = COALESCE(NULLIF(`processing_status`, ''), 'stored'),
  `published_at` = CASE
    WHEN `status` = 'published' AND `published_at` IS NULL THEN NOW()
    ELSE `published_at`
  END;

SET @col_allow_lossless_upload_exists = (
  SELECT COUNT(1)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'sys_admin'
    AND column_name = 'allow_lossless_upload'
);
SET @sql_add_allow_lossless_upload = IF(
  @col_allow_lossless_upload_exists = 0,
  'ALTER TABLE sys_admin ADD COLUMN `allow_lossless_upload` TINYINT NOT NULL DEFAULT 0 AFTER `avatar_url`',
  'SELECT 1'
);
PREPARE stmt_add_allow_lossless_upload FROM @sql_add_allow_lossless_upload;
EXECUTE stmt_add_allow_lossless_upload;
DEALLOCATE PREPARE stmt_add_allow_lossless_upload;

UPDATE sys_admin
SET `allow_lossless_upload` = CASE
  WHEN `username` = 'admin' THEN 1
  ELSE COALESCE(`allow_lossless_upload`, 0)
END;

INSERT INTO sys_config (`config_key`, `config_value`, `config_type`, `description`, `deleted`, `_openid`)
VALUES (
  'media.upload.policy',
  JSON_OBJECT(
    'maxBatchCount', 50,
    'maxBatchTotalBytes', 209715200,
    'image', JSON_OBJECT('maxFileSizeBytes', 10485760, 'preferredPolicyCode', 'compressed', 'qualityPercent', 86, 'maxWidthPx', 2560, 'maxHeightPx', 2560, 'preserveMetadata', false, 'note', 'Image uploads scale down when lossless upload is not allowed'),
    'video', JSON_OBJECT('maxFileSizeBytes', 157286400, 'preferredPolicyCode', 'passthrough', 'qualityPercent', 100, 'maxWidthPx', NULL, 'maxHeightPx', NULL, 'preserveMetadata', true, 'note', 'Video uploads keep the original file in this phase'),
    'audio', JSON_OBJECT('maxFileSizeBytes', 52428800, 'preferredPolicyCode', 'passthrough', 'qualityPercent', 100, 'maxWidthPx', NULL, 'maxHeightPx', NULL, 'preserveMetadata', true, 'note', 'Audio uploads keep the original file in this phase'),
    'file', JSON_OBJECT('maxFileSizeBytes', 20971520, 'preferredPolicyCode', 'passthrough', 'qualityPercent', 100, 'maxWidthPx', NULL, 'maxHeightPx', NULL, 'preserveMetadata', true, 'note', 'Other files keep the original payload')
  ),
  'json',
  'Media upload policy defaults and processing limits',
  0,
  ''
)
ON DUPLICATE KEY UPDATE
  `config_value` = VALUES(`config_value`),
  `config_type` = VALUES(`config_type`),
  `description` = VALUES(`description`),
  `deleted` = 0,
  `_openid` = '';
