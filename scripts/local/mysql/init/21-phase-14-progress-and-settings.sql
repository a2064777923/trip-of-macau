USE `aoxiaoyou`;

SET NAMES utf8mb4;

INSERT INTO `sys_config` (`config_key`, `config_value`, `config_type`, `description`, `deleted`, `_openid`)
VALUES
  ('translation.primary_authoring_locale', 'zh-Hant', 'string', 'Primary authoring locale for multilingual content', 0, 'system'),
  ('translation.engine_priority', '["google","bing","tencent"]', 'json', 'Ordered translation engine priority for carryover settings', 0, 'system'),
  ('translation.overwrite_filled_locales', 'false', 'boolean', 'Whether one-click translation overwrites existing locale fields', 0, 'system'),
  ('map.zoom.default-min-scale', '8', 'number', 'Default minimum map zoom scale for carryover system settings', 0, 'system'),
  ('map.zoom.default-max-scale', '18', 'number', 'Default maximum map zoom scale for carryover system settings', 0, 'system'),
  ('indoor.zoom.min-scale-meters', '20', 'number', 'Indoor floor default minimum visible scale in meters', 0, 'system'),
  ('indoor.zoom.max-scale-meters', '0.5', 'number', 'Indoor floor default maximum visible scale in meters', 0, 'system'),
  ('indoor.zoom.reference-viewport-px', '390', 'number', 'Indoor floor zoom derivation reference viewport width in pixels', 0, 'system'),
  ('indoor.tile.default-size-px', '512', 'number', 'Indoor floor default tile size in pixels', 0, 'system')
ON DUPLICATE KEY UPDATE
  `config_value` = VALUES(`config_value`),
  `config_type` = VALUES(`config_type`),
  `description` = VALUES(`description`),
  `deleted` = VALUES(`deleted`),
  `_openid` = VALUES(`_openid`);

INSERT INTO `seed_runs` (`seed_key`, `description`, `status`, `executed_at`, `notes`)
VALUES (
  'phase14-progress-and-settings',
  'Phase 14 carryover defaults for translation, upload policy, and map or indoor zoom ownership',
  'completed',
  NOW(),
  'Seeds the persisted config keys used by Phase 14 system settings and traveler progress inspection.'
)
ON DUPLICATE KEY UPDATE
  `description` = VALUES(`description`),
  `status` = VALUES(`status`),
  `executed_at` = VALUES(`executed_at`),
  `notes` = VALUES(`notes`);
