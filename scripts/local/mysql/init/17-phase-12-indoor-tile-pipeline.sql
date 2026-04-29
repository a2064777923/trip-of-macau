USE `aoxiaoyou`;

SET NAMES utf8mb4;

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

CALL `add_column_if_missing`('indoor_floors', 'tile_source_type', '`tile_source_type` VARCHAR(32) NULL AFTER `floor_plan_asset_id`');
CALL `add_column_if_missing`('indoor_floors', 'tile_source_asset_id', '`tile_source_asset_id` BIGINT NULL AFTER `tile_source_type`');
CALL `add_column_if_missing`('indoor_floors', 'tile_source_filename', '`tile_source_filename` VARCHAR(255) NULL AFTER `tile_source_asset_id`');
CALL `add_column_if_missing`('indoor_floors', 'tile_preview_image_url', '`tile_preview_image_url` VARCHAR(512) NULL AFTER `tile_source_filename`');
CALL `add_column_if_missing`('indoor_floors', 'tile_root_url', '`tile_root_url` VARCHAR(512) NULL AFTER `tile_preview_image_url`');
CALL `add_column_if_missing`('indoor_floors', 'tile_manifest_json', '`tile_manifest_json` JSON NULL AFTER `tile_root_url`');
CALL `add_column_if_missing`('indoor_floors', 'tile_zoom_derivation_json', '`tile_zoom_derivation_json` JSON NULL AFTER `tile_manifest_json`');
CALL `add_column_if_missing`('indoor_floors', 'image_width_px', '`image_width_px` INT NULL AFTER `tile_zoom_derivation_json`');
CALL `add_column_if_missing`('indoor_floors', 'image_height_px', '`image_height_px` INT NULL AFTER `image_width_px`');
CALL `add_column_if_missing`('indoor_floors', 'tile_size_px', '`tile_size_px` INT NULL AFTER `image_height_px`');
CALL `add_column_if_missing`('indoor_floors', 'grid_cols', '`grid_cols` INT NULL AFTER `tile_size_px`');
CALL `add_column_if_missing`('indoor_floors', 'grid_rows', '`grid_rows` INT NULL AFTER `grid_cols`');
CALL `add_column_if_missing`('indoor_floors', 'tile_level_count', '`tile_level_count` INT NULL AFTER `grid_rows`');
CALL `add_column_if_missing`('indoor_floors', 'tile_entry_count', '`tile_entry_count` INT NULL AFTER `tile_level_count`');
CALL `add_column_if_missing`('indoor_floors', 'import_status', '`import_status` VARCHAR(32) NOT NULL DEFAULT ''pending'' AFTER `tile_entry_count`');
CALL `add_column_if_missing`('indoor_floors', 'import_note', '`import_note` TEXT NULL AFTER `import_status`');
CALL `add_column_if_missing`('indoor_floors', 'imported_at', '`imported_at` DATETIME NULL AFTER `import_note`');
DROP PROCEDURE IF EXISTS `add_column_if_missing`;

UPDATE `indoor_floors`
SET
  `tile_source_type` = COALESCE(
    NULLIF(`tile_source_type`, ''),
    CASE
      WHEN `floor_plan_asset_id` IS NOT NULL THEN 'asset-image'
      WHEN `floor_plan_url` IS NOT NULL AND `floor_plan_url` <> '' THEN 'legacy-floor-plan'
      ELSE 'pending'
    END
  ),
  `tile_preview_image_url` = COALESCE(NULLIF(`tile_preview_image_url`, ''), `floor_plan_url`),
  `image_width_px` = COALESCE(`image_width_px`, NULL),
  `image_height_px` = COALESCE(`image_height_px`, NULL),
  `tile_size_px` = COALESCE(`tile_size_px`, 512),
  `grid_cols` = COALESCE(`grid_cols`, NULL),
  `grid_rows` = COALESCE(`grid_rows`, NULL),
  `tile_level_count` = COALESCE(`tile_level_count`, NULL),
  `tile_entry_count` = COALESCE(`tile_entry_count`, NULL),
  `import_status` = CASE
    WHEN `tile_manifest_json` IS NOT NULL OR (`floor_plan_url` IS NOT NULL AND `floor_plan_url` <> '') THEN 'ready'
    WHEN `import_status` IN ('processing', 'failed') THEN `import_status`
    ELSE 'pending'
  END;

INSERT INTO `sys_config` (`config_key`, `config_value`, `config_type`, `description`, `deleted`, `_openid`)
VALUES
  ('indoor.zoom.min-scale-meters', '20', 'number', 'Indoor floor default minimum visible scale in meters', 0, 'system'),
  ('indoor.zoom.max-scale-meters', '0.5', 'number', 'Indoor floor default maximum visible scale in meters', 0, 'system'),
  ('indoor.zoom.reference-viewport-px', '390', 'number', 'Indoor floor zoom derivation reference viewport width in pixels', 0, 'system'),
  ('indoor.tile.default-size-px', '512', 'number', 'Indoor floor default tile size in pixels', 0, 'system')
ON DUPLICATE KEY UPDATE
  `description` = VALUES(`description`);
