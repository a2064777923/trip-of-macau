SET NAMES utf8mb4;
USE `aoxiaoyou`;

INSERT INTO `ai_capabilities` (
  `domain_code`, `capability_code`, `display_name_zht`, `display_name_zh`, `display_name_en`, `display_name_pt`,
  `summary_zht`, `summary_zh`, `summary_en`, `summary_pt`,
  `supports_public_runtime`, `supports_admin_creative`, `supports_text`, `supports_image`, `supports_audio`, `supports_vision`,
  `status`, `sort_order`
) VALUES
  ('admin_creative', 'admin_image_generation', 'AI 圖像生成', 'AI 图像生成', 'AI Image Generation', 'Geracao de Imagem AI', '為城市、地圖、POI 與故事封面生成可編修的候選視覺素材。', '为城市、地图、POI 与故事封面生成可编辑候选视觉素材。', 'Generate editable candidate visuals for maps, POIs, and story covers.', 'Gerar imagens candidatas editaveis para mapas, POIs e capas.', 0, 1, 0, 1, 0, 0, 'enabled', 10),
  ('admin_creative', 'admin_tts_generation', 'AI 語音合成', 'AI 语音合成', 'AI Speech Synthesis', 'Sintese de Voz AI', '為旁白、NPC 講解與活動播報生成可回收的語音候選素材。', '为旁白、NPC 讲解与活动播报生成可回收语音候选素材。', 'Generate reusable candidate voice assets for narration and NPC playback.', 'Gerar voz candidata reutilizavel para narracao e NPC.', 0, 1, 1, 0, 1, 0, 'enabled', 20),
  ('admin_creative', 'admin_prompt_drafting', 'AI 提示詞輔助', 'AI 提示词辅助', 'AI Prompt Drafting', 'Rascunho de Prompt AI', '根據已填寫表單資料自動組裝可再編輯的提示詞草稿。', '根据已填写表单资料自动组装可再编辑提示词草稿。', 'Compose editable prompts from authored form data.', 'Montar prompts editaveis a partir dos dados preenchidos.', 0, 1, 1, 0, 0, 0, 'enabled', 30),
  ('mini_program', 'itinerary_planning', '行程推薦規劃', '行程推荐规划', 'Itinerary Planning', 'Planeamento de Itinerario', '根據時間、偏好、預算與動線限制生成可執行行程。', '根据时间、偏好、预算与动线限制生成可执行行程。', 'Build actionable itineraries from time, preference, budget, and routing constraints.', 'Gerar itinerarios exequiveis conforme tempo, preferencia e orcamento.', 1, 0, 1, 0, 0, 0, 'enabled', 40),
  ('mini_program', 'travel_qa', '旅行問答', '旅行问答', 'Travel Q&A', 'Perguntas de Viagem', '回答景點、美食、交通、玩法與故事內容相關問題。', '回答景点、美食、交通、玩法与故事内容相关问题。', 'Answer questions about sights, food, transport, activities, and stories.', 'Responder perguntas sobre pontos turisticos, comida, transporte e historias.', 1, 0, 1, 0, 0, 0, 'enabled', 50),
  ('mini_program', 'photo_positioning', '拍照識別定位', '拍照识别定位', 'Photo Positioning', 'Localizacao por Foto', '預留給後續室內視覺定位與圖像比對能力。', '预留给后续室内视觉定位与图像比对能力。', 'Reserved for later indoor visual positioning capability.', 'Reservado para futura localizacao visual interior.', 1, 0, 0, 1, 0, 1, 'planned', 60),
  ('mini_program', 'npc_voice_dialogue', 'NPC 語音對話', 'NPC 语音对话', 'NPC Voice Dialogue', 'Dialogo de Voz NPC', '支援景點 NPC 講解、互動對話與語音播報。', '支持景点 NPC 讲解、互动对话与语音播报。', 'Support NPC narration and spoken dialogue.', 'Suportar narracao e dialogo de voz para NPC.', 1, 0, 1, 0, 1, 0, 'enabled', 70),
  ('mini_program', 'navigation_assist', '導航輔助', '导航辅助', 'Navigation Assist', 'Assistencia de Navegacao', '支援室內目標點導航、路徑決策與提醒文案生成。', '支持室内目标点导航、路径决策与提醒文案生成。', 'Help with indoor target navigation and route decisions.', 'Ajudar na navegacao interior e decisao de rotas.', 1, 0, 1, 0, 0, 0, 'enabled', 80)
ON DUPLICATE KEY UPDATE
  `display_name_zht` = VALUES(`display_name_zht`),
  `display_name_zh` = VALUES(`display_name_zh`),
  `display_name_en` = VALUES(`display_name_en`),
  `display_name_pt` = VALUES(`display_name_pt`),
  `summary_zht` = VALUES(`summary_zht`),
  `summary_zh` = VALUES(`summary_zh`),
  `summary_en` = VALUES(`summary_en`),
  `summary_pt` = VALUES(`summary_pt`),
  `supports_public_runtime` = VALUES(`supports_public_runtime`),
  `supports_admin_creative` = VALUES(`supports_admin_creative`),
  `supports_text` = VALUES(`supports_text`),
  `supports_image` = VALUES(`supports_image`),
  `supports_audio` = VALUES(`supports_audio`),
  `supports_vision` = VALUES(`supports_vision`),
  `status` = VALUES(`status`),
  `sort_order` = VALUES(`sort_order`);

INSERT INTO `ai_provider_configs` (
  `provider_name`, `display_name`, `provider_type`, `endpoint_style`, `api_base_url`,
  `api_key_encrypted`, `api_key_masked`, `api_secret_encrypted`, `api_secret_masked`,
  `model_name`, `capabilities`, `feature_flags_json`, `request_timeout_ms`, `max_retries`, `quota_daily`,
  `cost_per_1k_tokens`, `status`, `health_status`, `health_message`
) VALUES
  ('dashscope-chat', '阿里百鍊對話', 'dashscope', 'openai_compatible', 'https://dashscope.aliyuncs.com/compatible-mode/v1', NULL, NULL, NULL, NULL, 'qwen-plus', JSON_ARRAY('admin_prompt_drafting', 'itinerary_planning', 'travel_qa', 'npc_voice_dialogue', 'navigation_assist'), JSON_OBJECT('supportsStructuredOutput', true, 'supportsFallback', true, 'supportsPublicRuntime', true), 30000, 2, 5000, 0.010000, 1, 'unknown', '尚未完成連通測試'),
  ('dashscope-image', '阿里百鍊文生圖', 'dashscope', 'dashscope_image', 'https://dashscope.aliyuncs.com/api/v1/services/aigc/image-generation/generation', NULL, NULL, NULL, NULL, 'wan2.6-image', JSON_ARRAY('admin_image_generation'), JSON_OBJECT('supportsTransparentBackground', true, 'supportsCandidateHistory', true), 60000, 1, 2000, 0.080000, 1, 'unknown', '尚未完成連通測試'),
  ('dashscope-tts', '阿里百鍊語音合成', 'dashscope', 'dashscope_tts', 'https://dashscope.aliyuncs.com/api/v1/services/audio/tts/SpeechSynthesizer', NULL, NULL, NULL, NULL, 'cosyvoice-v3-flash', JSON_ARRAY('admin_tts_generation', 'npc_voice_dialogue'), JSON_OBJECT('supportsVoiceSelection', true, 'supportsHistoryTrim', true), 60000, 1, 2000, 0.030000, 1, 'unknown', '尚未完成連通測試')
ON DUPLICATE KEY UPDATE
  `display_name` = VALUES(`display_name`),
  `provider_type` = VALUES(`provider_type`),
  `endpoint_style` = VALUES(`endpoint_style`),
  `api_base_url` = VALUES(`api_base_url`),
  `model_name` = VALUES(`model_name`),
  `capabilities` = VALUES(`capabilities`),
  `feature_flags_json` = VALUES(`feature_flags_json`),
  `request_timeout_ms` = VALUES(`request_timeout_ms`),
  `max_retries` = VALUES(`max_retries`),
  `quota_daily` = VALUES(`quota_daily`),
  `cost_per_1k_tokens` = VALUES(`cost_per_1k_tokens`),
  `status` = VALUES(`status`),
  `health_status` = VALUES(`health_status`),
  `health_message` = VALUES(`health_message`);

DELETE FROM `ai_policy_provider_bindings` WHERE `policy_id` IN (SELECT `id` FROM `ai_capability_policies` WHERE `policy_code` IN ('admin-image-default', 'admin-tts-default', 'itinerary-default', 'travel-qa-default', 'npc-voice-default', 'navigation-default'));
DELETE FROM `ai_capability_policies` WHERE `policy_code` IN ('admin-image-default', 'admin-tts-default', 'itinerary-default', 'travel-qa-default', 'npc-voice-default', 'navigation-default');
DELETE FROM `ai_prompt_templates` WHERE `template_code` IN ('city-cover-image', 'poi-overlay-icon', 'narration-voice');

INSERT INTO `ai_capability_policies` (`capability_id`, `policy_code`, `policy_name`, `policy_type`, `execution_mode`, `response_mode`, `default_model`, `system_prompt`, `prompt_template`, `multimodal_enabled`, `voice_enabled`, `structured_output_enabled`, `temperature`, `max_tokens`, `status`, `sort_order`, `notes`)
SELECT `id`, 'admin-image-default', 'AI 圖像生成基礎策略', 'creative', 'auto', 'asset', 'wan2.6-image', '你是澳門文旅內容創作助理，請生成可直接用於封面、橫幅與地圖疊加圖示的候選視覺素材。', '請根據以下資料生成 {{assetSlotName}}：{{subjectSummary}}。視覺風格：{{styleHint}}。圖片比例：{{aspectRatio}}。畫面需精美、具有文旅宣傳感、不可出現水印與多餘文字。', 1, 0, 0, 0.700, 0, 'enabled', 10, '預設用於城市封面、橫幅與 POI 疊加圖示。'
FROM `ai_capabilities` WHERE `capability_code` = 'admin_image_generation';

INSERT INTO `ai_capability_policies` (`capability_id`, `policy_code`, `policy_name`, `policy_type`, `execution_mode`, `response_mode`, `default_model`, `system_prompt`, `prompt_template`, `multimodal_enabled`, `voice_enabled`, `structured_output_enabled`, `temperature`, `max_tokens`, `status`, `sort_order`, `notes`)
SELECT `id`, 'admin-tts-default', 'AI 語音合成基礎策略', 'creative', 'auto', 'asset', 'cosyvoice-v3-flash', '你是澳門文旅語音創作助理，請把文案轉為自然、清晰、適合景點講解與活動播報的語音。', '請把以下文案轉為 {{voiceStyle}} 風格的語音：{{scriptText}}', 0, 1, 0, 0.500, 0, 'enabled', 20, '預設用於旁白、NPC 對話與活動播報。'
FROM `ai_capabilities` WHERE `capability_code` = 'admin_tts_generation';

INSERT INTO `ai_capability_policies` (`capability_id`, `policy_code`, `policy_name`, `policy_type`, `execution_mode`, `response_mode`, `default_model`, `system_prompt`, `prompt_template`, `response_schema_json`, `multimodal_enabled`, `voice_enabled`, `structured_output_enabled`, `temperature`, `max_tokens`, `status`, `sort_order`, `notes`)
SELECT `id`, 'itinerary-default', '行程規劃預設策略', 'public_runtime', 'auto', 'structured', 'qwen-plus', '你是澳門旅行行程規劃助理，只能根據已配置內容提供真實、可執行的建議。', '請根據使用者時間、偏好、預算與地理動線要求，生成可執行的日程安排，並附上備選方案與提醒。', JSON_OBJECT('type','object','required',JSON_ARRAY('summary','days'),'properties',JSON_OBJECT('summary',JSON_OBJECT('type','string'),'days',JSON_OBJECT('type','array'))), 0, 0, 1, 0.400, 1800, 'enabled', 30, '首批面向小程序旅客的核心能力之一。'
FROM `ai_capabilities` WHERE `capability_code` = 'itinerary_planning';

INSERT INTO `ai_capability_policies` (`capability_id`, `policy_code`, `policy_name`, `policy_type`, `execution_mode`, `response_mode`, `default_model`, `system_prompt`, `prompt_template`, `response_schema_json`, `multimodal_enabled`, `voice_enabled`, `structured_output_enabled`, `temperature`, `max_tokens`, `status`, `sort_order`, `notes`)
SELECT `id`, 'travel-qa-default', '旅行問答預設策略', 'public_runtime', 'auto', 'structured', 'qwen-plus', '你是澳門旅行問答助理，只能回答已配置景點、故事與交通內容，未知時需明確說明。', '請根據旅客問題提供準確、可信、可執行的回答，必要時加入不確定性提示。', JSON_OBJECT('type','object','required',JSON_ARRAY('answer'),'properties',JSON_OBJECT('answer',JSON_OBJECT('type','string'),'confidence',JSON_OBJECT('type','string'),'warnings',JSON_OBJECT('type','array'))), 0, 0, 1, 0.200, 1200, 'enabled', 40, '首批面向小程序旅客的核心能力之一。'
FROM `ai_capabilities` WHERE `capability_code` = 'travel_qa';

INSERT INTO `ai_capability_policies` (`capability_id`, `policy_code`, `policy_name`, `policy_type`, `execution_mode`, `response_mode`, `default_model`, `system_prompt`, `prompt_template`, `multimodal_enabled`, `voice_enabled`, `structured_output_enabled`, `temperature`, `max_tokens`, `status`, `sort_order`, `notes`)
SELECT `id`, 'npc-voice-default', 'NPC 語音對話預設策略', 'public_runtime', 'auto', 'text', 'qwen-plus', '你是景點 NPC 對話助理，要輸出具角色感、可信且可朗讀的講解文案。', '請根據景點設定與角色背景生成 NPC 講解詞與互動對話，保持親切且可朗讀。', 0, 1, 0, 0.600, 1500, 'enabled', 50, '後續可接入 NPC 語音播放與語音包管理。'
FROM `ai_capabilities` WHERE `capability_code` = 'npc_voice_dialogue';

INSERT INTO `ai_capability_policies` (`capability_id`, `policy_code`, `policy_name`, `policy_type`, `execution_mode`, `response_mode`, `default_model`, `system_prompt`, `prompt_template`, `multimodal_enabled`, `voice_enabled`, `structured_output_enabled`, `temperature`, `max_tokens`, `status`, `sort_order`, `notes`)
SELECT `id`, 'navigation-default', '導航輔助預設策略', 'public_runtime', 'auto', 'structured', 'qwen-plus', '你是室內導航輔助助理，請輸出清楚、短句、低認知負擔的導航建議。', '請根據起點、終點、樓層與環境限制，輸出清晰的導航步驟與注意事項。', 0, 0, 1, 0.200, 1000, 'enabled', 60, '後續可接入室內導航與路徑提示。'
FROM `ai_capabilities` WHERE `capability_code` = 'navigation_assist';

INSERT INTO `ai_policy_provider_bindings` (`policy_id`, `provider_id`, `binding_role`, `sort_order`, `enabled`, `model_override`, `notes`)
SELECT p.id, pr.id, 'primary', 10, 1, pr.model_name, 'Phase 18 預設主供應商'
FROM `ai_capability_policies` p
JOIN `ai_provider_configs` pr
  ON (
    (p.policy_code = 'admin-image-default' AND pr.provider_name = 'dashscope-image')
    OR (p.policy_code = 'admin-tts-default' AND pr.provider_name = 'dashscope-tts')
    OR (p.policy_code IN ('itinerary-default', 'travel-qa-default', 'npc-voice-default', 'navigation-default') AND pr.provider_name = 'dashscope-chat')
  );

INSERT INTO `ai_quota_rules` (`capability_id`, `scope_type`, `window_type`, `window_size`, `request_limit`, `token_limit`, `suspicious_concurrency_threshold`, `action_mode`, `status`, `notes`)
SELECT `id`, 'global', 'minute', 1,
  CASE
    WHEN `capability_code` = 'admin_image_generation' THEN 6
    WHEN `capability_code` = 'admin_tts_generation' THEN 10
    WHEN `capability_code` = 'itinerary_planning' THEN 20
    WHEN `capability_code` = 'travel_qa' THEN 40
    ELSE 12
  END,
  CASE
    WHEN `capability_code` IN ('itinerary_planning', 'travel_qa', 'npc_voice_dialogue', 'navigation_assist', 'admin_prompt_drafting') THEN 120000
    ELSE NULL
  END,
  CASE
    WHEN `capability_code` IN ('itinerary_planning', 'travel_qa') THEN 4
    ELSE 2
  END,
  'throttle',
  'enabled',
  'Phase 18 預設治理規則'
FROM `ai_capabilities`
WHERE `capability_code` IN ('admin_image_generation', 'admin_tts_generation', 'admin_prompt_drafting', 'itinerary_planning', 'travel_qa', 'npc_voice_dialogue', 'navigation_assist')
  AND NOT EXISTS (
    SELECT 1 FROM `ai_quota_rules` q WHERE q.capability_id = `ai_capabilities`.id AND q.scope_type = 'global'
  );

INSERT INTO `ai_prompt_templates` (`capability_id`, `template_code`, `template_name`, `template_type`, `asset_slot_code`, `system_prompt`, `prompt_template`, `variable_schema_json`, `output_constraints_json`, `default_provider_id`, `default_policy_id`, `status`, `sort_order`)
SELECT c.id, 'city-cover-image', '城市封面圖模板', 'image', 'city_cover', '你是澳門文旅視覺創作助理，要生成具宣傳感與沉浸感的城市主視覺。', '生成 {{cityName}} 的寫實風遊戲 CG 宣傳封面，需美觀大氣且引人入勝。城市介紹：{{citySummary}}。視覺重點：{{visualFocus}}。圖片比例：{{aspectRatio}}。', JSON_OBJECT('cityName','string','citySummary','string','visualFocus','string','aspectRatio','string'), JSON_OBJECT('background','clean','textOverlay','forbidden'), (SELECT id FROM ai_provider_configs WHERE provider_name='dashscope-image' LIMIT 1), (SELECT id FROM ai_capability_policies WHERE policy_code='admin-image-default' LIMIT 1), 'enabled', 10
FROM `ai_capabilities` c WHERE c.capability_code = 'admin_image_generation';

INSERT INTO `ai_prompt_templates` (`capability_id`, `template_code`, `template_name`, `template_type`, `asset_slot_code`, `system_prompt`, `prompt_template`, `variable_schema_json`, `output_constraints_json`, `default_provider_id`, `default_policy_id`, `status`, `sort_order`)
SELECT c.id, 'poi-overlay-icon', 'POI 疊加圖示模板', 'image', 'poi_overlay', '你是文旅地圖視覺設計助理，要生成透明背景、可疊加於手機地圖的 POI 圖示。', '文旅 Q 版手繪 2.5D 立體模型，{{poiName}}，核心元素：{{poiElements}}，極簡造型，透明背景，45 度俯視，8K 高清，無文字無水印，手機地圖 POI 疊加專用。', JSON_OBJECT('poiName','string','poiElements','string'), JSON_OBJECT('background','transparent','textOverlay','forbidden'), (SELECT id FROM ai_provider_configs WHERE provider_name='dashscope-image' LIMIT 1), (SELECT id FROM ai_capability_policies WHERE policy_code='admin-image-default' LIMIT 1), 'enabled', 20
FROM `ai_capabilities` c WHERE c.capability_code = 'admin_image_generation';

INSERT INTO `ai_prompt_templates` (`capability_id`, `template_code`, `template_name`, `template_type`, `asset_slot_code`, `system_prompt`, `prompt_template`, `variable_schema_json`, `output_constraints_json`, `default_provider_id`, `default_policy_id`, `status`, `sort_order`)
SELECT c.id, 'narration-voice', '景點講解語音模板', 'tts', 'poi_narration_audio', '你是澳門文旅語音導覽助理，要生成適合景點導覽播放的自然語音。', '請把以下文案轉為 {{voiceStyle}} 風格的講解語音：{{scriptText}}', JSON_OBJECT('voiceStyle','string','scriptText','string'), JSON_OBJECT('format','mp3'), (SELECT id FROM ai_provider_configs WHERE provider_name='dashscope-tts' LIMIT 1), (SELECT id FROM ai_capability_policies WHERE policy_code='admin-tts-default' LIMIT 1), 'enabled', 30
FROM `ai_capabilities` c WHERE c.capability_code = 'admin_tts_generation';

INSERT INTO `ai_request_logs` (`provider_id`, `policy_id`, `capability_code`, `admin_owner_id`, `admin_owner_name`, `request_type`, `input_data_hash`, `output_summary`, `latency_ms`, `tokens_used`, `cost_usd`, `success`, `fallback_triggered`, `trace_id`, `created_at`)
SELECT pr.id, p.id, c.capability_code, 1, 'admin', 'bootstrap', CONCAT('phase18-', c.capability_code), CONCAT('Phase 18 已建立能力：', c.display_name_zht), 320, 120, 0.001200, 1, 0, CONCAT('seed-', c.capability_code), NOW()
FROM `ai_capabilities` c
LEFT JOIN `ai_capability_policies` p ON p.capability_id = c.id
LEFT JOIN `ai_policy_provider_bindings` b ON b.policy_id = p.id AND b.binding_role = 'primary'
LEFT JOIN `ai_provider_configs` pr ON pr.id = b.provider_id
WHERE c.capability_code IN ('itinerary_planning', 'travel_qa', 'admin_image_generation')
  AND NOT EXISTS (
    SELECT 1
    FROM `ai_request_logs` l
    WHERE l.input_data_hash = (CONCAT('phase18-', c.capability_code) COLLATE utf8mb4_unicode_ci)
  );
