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

CALL `add_column_if_missing`(
  'indoor_node_behaviors',
  'overlay_geometry_json',
  '`overlay_geometry_json` JSON NULL AFTER `path_graph_json`'
);

DROP PROCEDURE IF EXISTS `add_column_if_missing`;
