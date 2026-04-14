ALTER TABLE cities
  ADD COLUMN IF NOT EXISTS `custom_country_name` VARCHAR(128) NULL AFTER `country_code`;
