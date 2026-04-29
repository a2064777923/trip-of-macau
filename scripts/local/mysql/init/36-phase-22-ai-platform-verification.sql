SET NAMES utf8mb4;
USE `aoxiaoyou`;

UPDATE `ai_provider_configs`
SET `api_base_url` = CASE `provider_name`
      WHEN 'dashscope-image' THEN 'https://dashscope.aliyuncs.com/api/v1/services/aigc/image-generation/generation'
      ELSE `api_base_url`
    END,
    `model_name` = CASE `provider_name`
      WHEN 'dashscope-chat' THEN 'qwen3.5-flash'
      WHEN 'dashscope-image' THEN 'wan2.6-image'
      WHEN 'dashscope-tts' THEN 'cosyvoice-v3-flash'
      ELSE `model_name`
    END
WHERE `provider_name` IN ('dashscope-chat', 'dashscope-image', 'dashscope-tts');

UPDATE `ai_provider_inventory` inv
JOIN `ai_provider_configs` pr ON pr.`id` = inv.`provider_id`
SET inv.`inventory_code` = 'wan2.6-image',
    inv.`external_id` = 'wan2.6-image',
    inv.`display_name` = 'Wan 2.6 Image',
    inv.`last_seen_at` = NOW(),
    inv.`synced_at` = NOW()
WHERE pr.`provider_name` = 'dashscope-image'
  AND inv.`inventory_code` = 'wan2.6-t2i-turbo';

UPDATE `ai_provider_inventory` inv
JOIN `ai_provider_configs` pr ON pr.`id` = inv.`provider_id`
SET inv.`is_default` = CASE
  WHEN pr.`provider_name` = 'dashscope-chat'
       AND inv.`inventory_code` = 'qwen3.5-flash'
       AND inv.`availability_status` = 'available' THEN 1
  WHEN pr.`provider_name` = 'dashscope-image'
       AND inv.`inventory_code` = 'wan2.6-image'
       AND inv.`availability_status` = 'available' THEN 1
  WHEN pr.`provider_name` = 'dashscope-tts'
       AND inv.`inventory_code` = 'cosyvoice-v3-flash'
       AND inv.`availability_status` = 'available' THEN 1
  ELSE 0
END
WHERE pr.`provider_name` IN ('dashscope-chat', 'dashscope-image', 'dashscope-tts');

UPDATE `ai_capability_policies` p
JOIN `ai_capabilities` c ON c.`id` = p.`capability_id`
SET p.`default_model` = CASE c.`capability_code`
  WHEN 'travel_qa' THEN 'qwen3.5-flash'
  WHEN 'admin_image_generation' THEN 'wan2.6-image'
  WHEN 'admin_tts_generation' THEN 'cosyvoice-v3-flash'
  ELSE p.`default_model`
END
WHERE c.`capability_code` IN ('travel_qa', 'admin_image_generation', 'admin_tts_generation');

UPDATE `ai_policy_provider_bindings` b
JOIN `ai_capability_policies` p ON p.`id` = b.`policy_id`
JOIN `ai_capabilities` c ON c.`id` = p.`capability_id`
JOIN `ai_provider_configs` pr
  ON pr.`provider_name` = CASE c.`capability_code`
    WHEN 'travel_qa' THEN 'dashscope-chat'
    WHEN 'admin_image_generation' THEN 'dashscope-image'
    WHEN 'admin_tts_generation' THEN 'dashscope-tts'
    ELSE pr.`provider_name`
  END
JOIN `ai_provider_inventory` inv
  ON inv.`provider_id` = pr.`id`
 AND inv.`inventory_code` = CASE c.`capability_code`
    WHEN 'travel_qa' THEN 'qwen3.5-flash'
    WHEN 'admin_image_generation' THEN 'wan2.6-image'
    WHEN 'admin_tts_generation' THEN 'cosyvoice-v3-flash'
    ELSE inv.`inventory_code`
  END
 AND inv.`availability_status` = 'available'
SET b.`provider_id` = pr.`id`,
    b.`inventory_id` = inv.`id`,
    b.`model_override` = inv.`inventory_code`,
    b.`binding_role` = COALESCE(NULLIF(b.`binding_role`, ''), 'primary'),
    b.`route_mode` = COALESCE(NULLIF(b.`route_mode`, ''), 'primary'),
    b.`enabled` = 1,
    b.`notes` = 'Phase 22 witness default closure'
WHERE c.`capability_code` IN ('travel_qa', 'admin_image_generation', 'admin_tts_generation');

INSERT INTO `ai_policy_provider_bindings` (
  `policy_id`,
  `provider_id`,
  `inventory_id`,
  `binding_role`,
  `route_mode`,
  `sort_order`,
  `enabled`,
  `model_override`,
  `weight_percent`,
  `notes`
)
SELECT
  p.`id`,
  pr.`id`,
  inv.`id`,
  'primary',
  'primary',
  0,
  1,
  inv.`inventory_code`,
  100,
  'Phase 22 witness default closure'
FROM `ai_capability_policies` p
JOIN `ai_capabilities` c ON c.`id` = p.`capability_id`
JOIN `ai_provider_configs` pr
  ON pr.`provider_name` = CASE c.`capability_code`
    WHEN 'travel_qa' THEN 'dashscope-chat'
    WHEN 'admin_image_generation' THEN 'dashscope-image'
    WHEN 'admin_tts_generation' THEN 'dashscope-tts'
  END
JOIN `ai_provider_inventory` inv
  ON inv.`provider_id` = pr.`id`
 AND inv.`inventory_code` = CASE c.`capability_code`
    WHEN 'travel_qa' THEN 'qwen3.5-flash'
    WHEN 'admin_image_generation' THEN 'wan2.6-image'
    WHEN 'admin_tts_generation' THEN 'cosyvoice-v3-flash'
  END
 AND inv.`availability_status` = 'available'
WHERE p.`status` = 'enabled'
  AND c.`capability_code` IN ('travel_qa', 'admin_image_generation', 'admin_tts_generation')
  AND NOT EXISTS (
    SELECT 1
    FROM `ai_policy_provider_bindings` existing
    WHERE existing.`policy_id` = p.`id`
      AND existing.`enabled` = 1
  );
