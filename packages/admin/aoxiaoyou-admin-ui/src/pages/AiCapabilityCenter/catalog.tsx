export type AiDomainCode = 'admin_creative' | 'mini_program';

export interface AiWorkspaceNavItem {
  key: string;
  path: string;
  label: string;
  hint: string;
}

export interface AiCapabilityCatalogItem {
  capabilityCode: string;
  domainCode: AiDomainCode;
  label: string;
  shortLabel: string;
  summary: string;
  defaultGenerationType?: 'text' | 'image' | 'tts';
  operatorFocus: string[];
  samplePrompt?: string;
}

export const aiWorkspaceNavItems: AiWorkspaceNavItem[] = [
  { key: 'overview', path: '/ai', label: '總覽', hint: '平台健康、提醒與快捷入口' },
  { key: 'providers', path: '/ai/providers', label: '供應商接入', hint: '模板、連通測試與模型同步' },
  { key: 'models', path: '/ai/models', label: '模型與端點庫', hint: '模型清單、端點庫存與成本配置' },
  { key: 'voices', path: '/ai/voices', label: '音色與聲音工坊', hint: '系統音色、試聽、語言控制與聲音復刻' },
  { key: 'capabilities', path: '/ai/capabilities', label: '能力路由', hint: '主模型、後備模型、策略與模板' },
  { key: 'creative-studio', path: '/ai/creative-studio', label: '創作工作台', hint: '提示詞組裝、候選歷史與回填' },
  { key: 'observability', path: '/ai/observability', label: '監控與成本', hint: '健康、用量、延遲與估算成本' },
  { key: 'settings', path: '/ai/settings', label: '治理設定', hint: '新鮮度、警戒值與平台總開關' },
];

export const aiDomainLabels: Record<AiDomainCode, string> = {
  admin_creative: '編創輔助域',
  mini_program: '用戶服務域',
};

export const aiCapabilityCatalog: Record<string, AiCapabilityCatalogItem> = {
  admin_image_generation: {
    capabilityCode: 'admin_image_generation',
    domainCode: 'admin_creative',
    label: 'AI 圖像生成',
    shortLabel: '圖像生成',
    summary: '為城市、地圖、POI 與故事內容生成可回填的封面、橫幅與插圖素材。',
    defaultGenerationType: 'image',
    operatorFocus: ['城市封面', '橫幅視覺', 'POI 疊加圖示', '故事海報'],
    samplePrompt: '生成澳門城市探索主視覺海報，寫實遊戲 CG 風格，兼具文化感與探索感。',
  },
  admin_tts_generation: {
    capabilityCode: 'admin_tts_generation',
    domainCode: 'admin_creative',
    label: 'AI 語音合成',
    shortLabel: '語音合成',
    summary: '為旁白、NPC 講解、活動播報與導覽語音生成可回填的音訊素材。',
    defaultGenerationType: 'tts',
    operatorFocus: ['景點旁白', 'NPC 講解', '活動播報', '導覽試聽'],
    samplePrompt: '請用溫暖而清晰的聲線朗讀以下導覽文案，語速中等，帶少量故事感。',
  },
  admin_prompt_drafting: {
    capabilityCode: 'admin_prompt_drafting',
    domainCode: 'admin_creative',
    label: 'AI 提示詞輔助',
    shortLabel: '提示詞輔助',
    summary: '根據表單內容自動組裝可再編輯的提示詞草稿，協助圖片、文案與語音創作。',
    defaultGenerationType: 'text',
    operatorFocus: ['封面描述', 'POI 圖示提示詞', '故事介紹文案', '工作台模板草稿'],
    samplePrompt: '根據城市介紹與視覺需求，自動生成可交給文生圖模型的高質量提示詞。',
  },
  itinerary_planning: {
    capabilityCode: 'itinerary_planning',
    domainCode: 'mini_program',
    label: '行程推薦規劃',
    shortLabel: '行程規劃',
    summary: '根據用戶時間、偏好、預算與地理動線生成可執行行程。',
    defaultGenerationType: 'text',
    operatorFocus: ['路線規劃', '時間分配', '預算約束', '結構化輸出'],
  },
  travel_qa: {
    capabilityCode: 'travel_qa',
    domainCode: 'mini_program',
    label: '旅行問答',
    shortLabel: '旅行問答',
    summary: '回答景點、美食、交通、玩法與故事內容相關問題。',
    defaultGenerationType: 'text',
    operatorFocus: ['景點問答', '美食問答', '交通問答', '故事內容問答'],
  },
  photo_positioning: {
    capabilityCode: 'photo_positioning',
    domainCode: 'mini_program',
    label: '拍照識別定位',
    shortLabel: '拍照定位',
    summary: '結合視覺錨點、樓層資訊與參考物識別用戶室內位置。',
    defaultGenerationType: 'image',
    operatorFocus: ['室內視覺定位', '樓層參考物識別', 'AR 輔助定位'],
  },
  npc_voice_dialogue: {
    capabilityCode: 'npc_voice_dialogue',
    domainCode: 'mini_program',
    label: 'NPC 語音對話',
    shortLabel: 'NPC 對話',
    summary: '用於景點 NPC 講解詞、互動對話與語音播報文案生成。',
    defaultGenerationType: 'tts',
    operatorFocus: ['角色聲線', '多輪互動', '旁白配音', '語音回覆'],
  },
  navigation_assist: {
    capabilityCode: 'navigation_assist',
    domainCode: 'mini_program',
    label: '導航輔助',
    shortLabel: '導航輔助',
    summary: '用於室內目標點導航與路線決策。',
    defaultGenerationType: 'text',
    operatorFocus: ['路徑決策', '室內導航', '提示文案', '後備路由'],
  },
};

export function getCapabilityCatalogItem(capabilityCode?: string) {
  if (!capabilityCode) {
    return undefined;
  }
  return aiCapabilityCatalog[capabilityCode];
}

export function inferCapabilitySummary(capabilityCode?: string, fallback?: string) {
  return getCapabilityCatalogItem(capabilityCode)?.summary || fallback || '此能力尚未補充摘要。';
}
