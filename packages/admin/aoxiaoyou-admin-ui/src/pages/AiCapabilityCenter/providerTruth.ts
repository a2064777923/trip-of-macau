import type { AiProviderItem, AiProviderTemplateItem } from '../../services/api';

export interface ProviderTruthState {
  code: 'LIVE_VERIFIED' | 'TEMPLATE_ONLY' | 'CREDENTIAL_MISSING';
  color: 'green' | 'gold' | 'red';
  summary: string;
  detail: string;
}

export interface SemanticsDescriptor {
  label: string;
  detail: string;
}

const syncSemanticsMap: Record<string, SemanticsDescriptor> = {
  list_api: {
    label: '直連列表 API',
    detail: '以官方模型列表 API 為主，能直接取得可用模型清單。',
  },
  hybrid_list_or_catalog: {
    label: '列表 / 目錄混合',
    detail: '優先走列表 API，失敗時回退到官方目錄或內建清單。',
  },
  documented_catalog: {
    label: '官方文檔目錄',
    detail: '依官方文檔或平台目錄建檔，不代表目前工作站已完成 live 驗證。',
  },
  endpoint_discovery: {
    label: '端點探索',
    detail: '以 endpoint 或 deployment 清單為主，需由操作者確認可用端點。',
  },
  manual: {
    label: '手動維護',
    detail: '完全由後台手動配置與維護，不會自動證明可連通。',
  },
};

const inventorySemanticsMap: Record<string, SemanticsDescriptor> = {
  model_list: {
    label: '模型列表',
    detail: '庫存來自供應商模型清單。',
  },
  hybrid_catalog: {
    label: '混合型目錄',
    detail: '庫存可能來自列表 API，也可能回退到官方目錄。',
  },
  catalog: {
    label: '文檔目錄',
    detail: '庫存主要根據官方文檔維護。',
  },
  endpoint_inventory: {
    label: '端點庫存',
    detail: '庫存代表實際 endpoint / deployment，而不只是模型名。',
  },
  manual: {
    label: '手動庫存',
    detail: '庫存由後台手動建立與維護。',
  },
};

export function describeSyncSemantics(syncStrategy?: string): SemanticsDescriptor {
  return (
    (syncStrategy ? syncSemanticsMap[syncStrategy] : undefined) || {
      label: syncStrategy || '未知策略',
      detail: '此供應商尚未補齊同步策略說明。',
    }
  );
}

export function describeInventorySemantics(inventorySemantics?: string): SemanticsDescriptor {
  return (
    (inventorySemantics ? inventorySemanticsMap[inventorySemantics] : undefined) || {
      label: inventorySemantics || '未知語義',
      detail: '此供應商尚未補齊庫存語義說明。',
    }
  );
}

export function resolveTemplateForProvider(
  provider: Pick<AiProviderItem, 'platformCode'>,
  templates: AiProviderTemplateItem[],
) {
  return templates.find((template) => template.platformCode === provider.platformCode);
}

export function resolveProviderTruth(
  provider: Pick<
    AiProviderItem,
    'hasApiKey' | 'hasApiSecret' | 'healthStatus' | 'healthMessage' | 'syncStrategy'
  >,
  template?: AiProviderTemplateItem,
): ProviderTruthState {
  const hasCredential = provider.hasApiKey === 1 || provider.hasApiSecret === 1;
  const syncDescriptor = describeSyncSemantics(
    provider.syncStrategy || template?.syncStrategy || undefined,
  );

  if (!hasCredential) {
    return {
      code: 'CREDENTIAL_MISSING',
      color: 'red',
      summary: '尚未配置可用憑證',
      detail: `請先補齊金鑰，再按 ${provider.syncStrategy || template?.syncStrategy || 'manual'} 流程做連通驗證。`,
    };
  }

  if (provider.healthStatus === 'healthy') {
    return {
      code: 'LIVE_VERIFIED',
      color: 'green',
      summary: '目前工作站已有 live 證據',
      detail: provider.healthMessage || '最近一次連通檢查已成功。',
    };
  }

  return {
    code: 'TEMPLATE_ONLY',
    color: 'gold',
    summary: '模板已就緒，但尚未取得 live 證據',
    detail: provider.healthMessage || syncDescriptor.detail,
  };
}
