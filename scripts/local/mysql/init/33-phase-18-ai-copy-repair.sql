SET NAMES utf8mb4;
USE `aoxiaoyou`;

UPDATE `ai_capabilities`
SET
  `display_name_zht` = CASE `capability_code`
    WHEN 'admin_image_generation' THEN 'AI 圖像生成'
    WHEN 'admin_tts_generation' THEN 'AI 語音合成'
    WHEN 'admin_prompt_drafting' THEN 'AI 提示詞輔助'
    WHEN 'itinerary_planning' THEN '行程推薦規劃'
    WHEN 'travel_qa' THEN '旅行問答'
    WHEN 'photo_positioning' THEN '拍照識別定位'
    WHEN 'npc_voice_dialogue' THEN 'NPC 語音對話'
    WHEN 'navigation_assist' THEN '導航輔助'
    ELSE `display_name_zht`
  END,
  `summary_zht` = CASE `capability_code`
    WHEN 'admin_image_generation' THEN '為城市、地圖、POI 與故事封面生成可編修的候選視覺素材。'
    WHEN 'admin_tts_generation' THEN '為旁白、NPC 講解與活動播報生成可回收的語音候選素材。'
    WHEN 'admin_prompt_drafting' THEN '根據已填寫表單資料自動組裝可再編輯的提示詞草稿。'
    WHEN 'itinerary_planning' THEN '根據時間、偏好、預算與動線限制生成可執行行程。'
    WHEN 'travel_qa' THEN '回答景點、美食、交通、玩法與故事內容相關問題。'
    WHEN 'photo_positioning' THEN '預留給後續室內視覺定位與圖像比對能力。'
    WHEN 'npc_voice_dialogue' THEN '支援景點 NPC 講解、互動對話與語音播報。'
    WHEN 'navigation_assist' THEN '支援室內目標點導航、路徑決策與提醒文案生成。'
    ELSE `summary_zht`
  END
WHERE `capability_code` IN (
  'admin_image_generation',
  'admin_tts_generation',
  'admin_prompt_drafting',
  'itinerary_planning',
  'travel_qa',
  'photo_positioning',
  'npc_voice_dialogue',
  'navigation_assist'
);

UPDATE `ai_provider_configs`
SET
  `display_name` = CASE `provider_name`
    WHEN 'dashscope-chat' THEN '阿里百鍊對話'
    WHEN 'dashscope-image' THEN '阿里百鍊文生圖'
    WHEN 'dashscope-tts' THEN '阿里百鍊語音合成'
    ELSE `display_name`
  END,
  `health_message` = '尚未完成連通測試'
WHERE `provider_name` IN ('dashscope-chat', 'dashscope-image', 'dashscope-tts');

UPDATE `ai_capability_policies`
SET
  `policy_name` = CASE `policy_code`
    WHEN 'admin-image-default' THEN 'AI 圖像生成基礎策略'
    WHEN 'admin-tts-default' THEN 'AI 語音合成基礎策略'
    WHEN 'itinerary-default' THEN '行程規劃預設策略'
    WHEN 'travel-qa-default' THEN '旅行問答預設策略'
    WHEN 'npc-voice-default' THEN 'NPC 語音對話預設策略'
    WHEN 'navigation-default' THEN '導航輔助預設策略'
    ELSE `policy_name`
  END,
  `system_prompt` = CASE `policy_code`
    WHEN 'admin-image-default' THEN '你是澳門文旅內容創作助理，請生成可直接用於封面、橫幅與地圖疊加圖示的候選視覺素材。'
    WHEN 'admin-tts-default' THEN '你是澳門文旅語音創作助理，請把文案轉為自然、清晰、適合景點講解與活動播報的語音。'
    WHEN 'itinerary-default' THEN '你是澳門旅行行程規劃助理，只能根據已配置內容提供真實、可執行的建議。'
    WHEN 'travel-qa-default' THEN '你是澳門旅行問答助理，只能回答已配置景點、故事與交通內容，未知時需明確說明。'
    WHEN 'npc-voice-default' THEN '你是景點 NPC 對話助理，要輸出具角色感、可信且可朗讀的講解文案。'
    WHEN 'navigation-default' THEN '你是室內導航輔助助理，請輸出清楚、短句、低認知負擔的導航建議。'
    ELSE `system_prompt`
  END,
  `prompt_template` = CASE `policy_code`
    WHEN 'admin-image-default' THEN '請根據以下資料生成 {{assetSlotName}}：{{subjectSummary}}。視覺風格：{{styleHint}}。圖片比例：{{aspectRatio}}。畫面需精美、具有文旅宣傳感、不可出現水印與多餘文字。'
    WHEN 'admin-tts-default' THEN '請把以下文案轉為 {{voiceStyle}} 風格的語音：{{scriptText}}'
    WHEN 'itinerary-default' THEN '請根據使用者時間、偏好、預算與地理動線要求，生成可執行的日程安排，並附上備選方案與提醒。'
    WHEN 'travel-qa-default' THEN '請根據旅客問題提供準確、可信、可執行的回答，必要時加入不確定性提示。'
    WHEN 'npc-voice-default' THEN '請根據景點設定與角色背景生成 NPC 講解詞與互動對話，保持親切且可朗讀。'
    WHEN 'navigation-default' THEN '請根據起點、終點、樓層與環境限制，輸出清晰的導航步驟與注意事項。'
    ELSE `prompt_template`
  END,
  `notes` = CASE `policy_code`
    WHEN 'admin-image-default' THEN '預設用於城市封面、橫幅與 POI 疊加圖示。'
    WHEN 'admin-tts-default' THEN '預設用於旁白、NPC 對話與活動播報。'
    WHEN 'itinerary-default' THEN '首批面向小程序旅客的核心能力之一。'
    WHEN 'travel-qa-default' THEN '首批面向小程序旅客的核心能力之一。'
    WHEN 'npc-voice-default' THEN '後續可接入 NPC 語音播放與語音包管理。'
    WHEN 'navigation-default' THEN '後續可接入室內導航與路徑提示。'
    ELSE `notes`
  END
WHERE `policy_code` IN (
  'admin-image-default',
  'admin-tts-default',
  'itinerary-default',
  'travel-qa-default',
  'npc-voice-default',
  'navigation-default'
);

UPDATE `ai_prompt_templates`
SET
  `template_name` = CASE `template_code`
    WHEN 'city-cover-image' THEN '城市封面圖模板'
    WHEN 'poi-overlay-icon' THEN 'POI 疊加圖示模板'
    WHEN 'narration-voice' THEN '景點講解語音模板'
    ELSE `template_name`
  END,
  `system_prompt` = CASE `template_code`
    WHEN 'city-cover-image' THEN '你是澳門文旅視覺創作助理，要生成具宣傳感與沉浸感的城市主視覺。'
    WHEN 'poi-overlay-icon' THEN '你是文旅地圖視覺設計助理，要生成透明背景、可疊加於手機地圖的 POI 圖示。'
    WHEN 'narration-voice' THEN '你是澳門文旅語音導覽助理，要生成適合景點導覽播放的自然語音。'
    ELSE `system_prompt`
  END,
  `prompt_template` = CASE `template_code`
    WHEN 'city-cover-image' THEN '生成 {{cityName}} 的寫實風遊戲 CG 宣傳封面，需美觀大氣且引人入勝。城市介紹：{{citySummary}}。視覺重點：{{visualFocus}}。圖片比例：{{aspectRatio}}。'
    WHEN 'poi-overlay-icon' THEN '文旅 Q 版手繪 2.5D 立體模型，{{poiName}}，核心元素：{{poiElements}}，極簡造型，透明背景，45 度俯視，8K 高清，無文字無水印，手機地圖 POI 疊加專用。'
    WHEN 'narration-voice' THEN '請把以下文案轉為 {{voiceStyle}} 風格的講解語音：{{scriptText}}'
    ELSE `prompt_template`
  END
WHERE `template_code` IN ('city-cover-image', 'poi-overlay-icon', 'narration-voice');
