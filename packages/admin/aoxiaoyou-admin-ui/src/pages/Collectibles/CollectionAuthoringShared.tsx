import React, { useEffect, useMemo, useState } from 'react';
import { Card, Col, Form, Input, Row, Select, Space, Switch, Tag, Typography } from 'antd';
import type { FormInstance } from 'antd/es/form';
import type { NamePath } from 'antd/es/form/interface';
import { useRequest } from 'ahooks';
import LocalizedFieldGroup, {
  buildLocalizedFieldNames,
} from '../../components/localization/LocalizedFieldGroup';
import MediaAssetArrayField from '../../components/media/MediaAssetArrayField';
import MediaAssetPickerField from '../../components/media/MediaAssetPickerField';
import {
  getAdminStorylines,
  getAdminTranslationSettings,
  getCities,
  getIndoorBuildings,
  getIndoorFloors,
  getSubMaps,
} from '../../services/api';
import type {
  AdminIndoorFloorItem,
  AdminStorylineListItem,
  AdminTranslationSettings,
  CityItem,
} from '../../types/admin';

const { Paragraph, Text } = Typography;
const EMPTY_NUMBER_ARRAY: number[] = [];

interface OptionItem {
  label: string;
  value: number;
}

interface FloorOptionItem extends OptionItem {
  buildingId: number;
}

interface BindingOptions {
  translationDefaults?: Partial<AdminTranslationSettings>;
  storylineOptions: OptionItem[];
  cityOptions: OptionItem[];
  subMapOptions: OptionItem[];
  indoorBuildingOptions: OptionItem[];
  indoorFloorOptions: FloorOptionItem[];
}

type PresetKind = 'popup' | 'display' | 'trigger';

type JsonRecord = Record<string, unknown>;

interface CollectionPresetEditorProps {
  form: FormInstance;
  kind: PresetKind;
  presetFieldName: NamePath;
  configFieldName: NamePath;
}

interface CollectionBindingSectionProps {
  form: FormInstance;
  options: BindingOptions;
}

interface CollectionMediaSectionProps {
  includeIcon?: boolean;
  includeAnimation?: boolean;
  attachmentHelp?: string;
}

const nameFields = buildLocalizedFieldNames('name');
const descriptionFields = buildLocalizedFieldNames('description');
const exampleContentFields = buildLocalizedFieldNames('exampleContent');
const subtitleFields = buildLocalizedFieldNames('subtitle');
const highlightFields = buildLocalizedFieldNames('highlight');

const popupPresetOptions = [
  { value: 'story-modal', label: '故事彈窗' },
  { value: 'achievement-toast', label: '成就提示' },
  { value: 'reward-modal', label: '獎勵兌換彈窗' },
  { value: 'map-bubble', label: '地圖氣泡' },
  { value: 'custom', label: '自訂 JSON' },
];

const displayPresetOptions = [
  { value: 'map-keepsake', label: '地圖藏品卡' },
  { value: 'badge-ribbon', label: '徽章橫幅' },
  { value: 'inventory-card', label: '庫存卡片' },
  { value: 'gallery-rail', label: '橫向資源軌' },
  { value: 'custom', label: '自訂 JSON' },
];

const triggerPresetOptions = [
  { value: 'poi-arrival', label: '到點觸發' },
  { value: 'story-completion', label: '故事完成' },
  { value: 'chapter-completion', label: '章節完成' },
  { value: 'reward-redemption', label: '兌換觸發' },
  { value: 'manual-claim', label: '手動領取' },
  { value: 'custom', label: '自訂 JSON' },
];

function hasText(value?: string | null): value is string {
  return typeof value === 'string' && value.trim().length > 0;
}

function safeParseJson(value: unknown): JsonRecord {
  if (!hasText(typeof value === 'string' ? value : '')) {
    return {};
  }
  try {
    const parsed = JSON.parse(value as string);
    return parsed && typeof parsed === 'object' && !Array.isArray(parsed) ? parsed as JsonRecord : {};
  } catch {
    return {};
  }
}

function stringifyJson(value: JsonRecord) {
  return JSON.stringify(value, null, 2);
}

function withPatch(current: JsonRecord, patch: JsonRecord) {
  return stringifyJson({
    ...current,
    ...patch,
  });
}

function pickStorylineLabel(item: Partial<AdminStorylineListItem> | null | undefined) {
  if (!item) {
    return '';
  }
  return item.nameZht || item.nameZh || item.nameEn || item.namePt || item.code || '';
}

function pickCityLabel(item: CityItem | null | undefined) {
  if (!item) {
    return '';
  }
  return item.nameZht || item.nameZh || item.nameEn || item.namePt || item.code;
}

function pickIndoorFloorLabel(item: AdminIndoorFloorItem) {
  return item.floorNameZht || item.floorNameZh || item.floorNameEn || item.floorNamePt || item.floorCode || `Floor ${item.floorNumber}`;
}

function buildPopupPatch(preset: string): JsonRecord {
  switch (preset) {
    case 'achievement-toast':
      return {
        enabled: true,
        mode: 'toast',
        title: '達成條件後顯示成就提示',
        body: '',
        mediaUsageType: 'icon',
      };
    case 'reward-modal':
      return {
        enabled: true,
        mode: 'modal',
        title: '顯示獎勵內容與兌換說明',
        body: '',
        ctaLabel: '立即查看',
        mediaUsageType: 'cover',
      };
    case 'map-bubble':
      return {
        enabled: true,
        mode: 'bubble',
        title: '地圖氣泡說明',
        body: '',
        mediaUsageType: 'gallery',
      };
    case 'story-modal':
    default:
      return {
        enabled: true,
        mode: 'sheet',
        title: '故事彈窗',
        body: '',
        ctaLabel: '繼續探索',
        mediaUsageType: 'cover',
      };
  }
}

function buildDisplayPatch(preset: string): JsonRecord {
  switch (preset) {
    case 'badge-ribbon':
      return {
        layout: 'ribbon',
        theme: 'achievement',
        accent: 'gold',
        showSubtitle: true,
        showLocationBinding: true,
        showIndoorBinding: true,
      };
    case 'inventory-card':
      return {
        layout: 'inventory-card',
        theme: 'reward',
        accent: 'ruby',
        showInventory: true,
        showSubtitle: true,
      };
    case 'gallery-rail':
      return {
        layout: 'rail',
        theme: 'media-first',
        accent: 'teal',
        showSubtitle: true,
        showLocationBinding: false,
        showIndoorBinding: false,
      };
    case 'map-keepsake':
    default:
      return {
        layout: 'map-card',
        theme: 'collectible',
        accent: 'amber',
        showSubtitle: true,
        showLocationBinding: true,
        showIndoorBinding: true,
      };
  }
}

function buildTriggerPatch(preset: string): JsonRecord {
  switch (preset) {
    case 'story-completion':
      return {
        triggerType: 'story_completion',
        radiusMeters: 0,
        dwellSeconds: 0,
        requiresIndoorFloor: false,
        consumeStamps: false,
      };
    case 'chapter-completion':
      return {
        triggerType: 'chapter_completion',
        radiusMeters: 0,
        dwellSeconds: 0,
        requiresIndoorFloor: false,
        consumeStamps: false,
      };
    case 'reward-redemption':
      return {
        triggerType: 'reward_redemption',
        radiusMeters: 0,
        dwellSeconds: 0,
        requiresIndoorFloor: true,
        consumeStamps: true,
      };
    case 'manual-claim':
      return {
        triggerType: 'manual_claim',
        radiusMeters: 0,
        dwellSeconds: 0,
        requiresIndoorFloor: false,
        consumeStamps: false,
      };
    case 'poi-arrival':
    default:
      return {
        triggerType: 'poi_arrival',
        radiusMeters: 50,
        dwellSeconds: 10,
        requiresIndoorFloor: false,
        consumeStamps: false,
      };
  }
}

export function applyCollectionLocaleFallback<T extends Record<string, any>>(values: T, baseNames: string[]) {
  const next = { ...values } as Record<string, any>;
  baseNames.forEach((baseName) => {
    const zhKey = `${baseName}Zh`;
    const zhtKey = `${baseName}Zht`;
    const enKey = `${baseName}En`;
    const ptKey = `${baseName}Pt`;
    const zhValue = [next[zhKey], next[zhtKey], next[enKey], next[ptKey]].find(hasText);
    const zhtValue = [next[zhtKey], next[zhKey], next[enKey], next[ptKey]].find(hasText);
    if (zhValue) {
      next[zhKey] = zhValue;
    }
    if (zhtValue) {
      next[zhtKey] = zhtValue;
    }
  });
  return next as T;
}

export function useCollectionAuthoringOptions(form: FormInstance): BindingOptions {
  const translationRequest = useRequest(getAdminTranslationSettings);
  const [storylineOptions, setStorylineOptions] = useState<OptionItem[]>([]);
  const [cityOptions, setCityOptions] = useState<OptionItem[]>([]);
  const [subMapOptions, setSubMapOptions] = useState<OptionItem[]>([]);
  const [indoorBuildingOptions, setIndoorBuildingOptions] = useState<OptionItem[]>([]);
  const [indoorFloorOptions, setIndoorFloorOptions] = useState<FloorOptionItem[]>([]);
  const watchedBuildingBindings = Form.useWatch('indoorBuildingBindings', form) as number[] | undefined;
  const selectedBuildingBindings = useMemo(
    () => watchedBuildingBindings ?? EMPTY_NUMBER_ARRAY,
    [watchedBuildingBindings],
  );
  const selectedBuildingBindingKey = useMemo(
    () => selectedBuildingBindings.slice().sort((left, right) => left - right).join(','),
    [selectedBuildingBindings],
  );

  useEffect(() => {
    let active = true;
    const load = async () => {
      const [storylineRes, cityRes, subMapRes, buildingRes] = await Promise.all([
        getAdminStorylines({ pageNum: 1, pageSize: 500 }),
        getCities({ pageNum: 1, pageSize: 200 }),
        getSubMaps({ pageNum: 1, pageSize: 500 }),
        getIndoorBuildings({ pageNum: 1, pageSize: 500 }),
      ]);
      if (!active) {
        return;
      }
      setStorylineOptions(
        (storylineRes.data?.list || []).map((item) => ({
          value: item.storylineId,
          label: `${pickStorylineLabel(item)} (${item.code})`,
        })),
      );
      setCityOptions(
        (cityRes.data?.list || []).map((item) => ({
          value: item.id,
          label: `${pickCityLabel(item)} (${item.code})`,
        })),
      );
      setSubMapOptions(
        (subMapRes.data?.list || []).map((item) => ({
          value: item.id,
          label: `${item.nameZht || item.nameZh || item.nameEn || item.namePt || item.code} (${item.code})`,
        })),
      );
      setIndoorBuildingOptions(
        (buildingRes.data?.list || []).map((item) => ({
          value: item.id,
          label: `${item.nameZht || item.nameZh || item.nameEn || item.namePt || item.buildingCode} (${item.buildingCode})`,
        })),
      );
    };
    void load();
    return () => {
      active = false;
    };
  }, []);

  useEffect(() => {
    let active = true;
    const loadFloors = async () => {
      if (!selectedBuildingBindings.length) {
        setIndoorFloorOptions((current) => (current.length ? [] : current));
        const currentFloorBindings = (form.getFieldValue('indoorFloorBindings') as number[] | undefined) ?? EMPTY_NUMBER_ARRAY;
        if (currentFloorBindings.length) {
          form.setFieldValue('indoorFloorBindings', []);
        }
        return;
      }
      const results = await Promise.all(
        selectedBuildingBindings.map(async (buildingId) => {
          const response = await getIndoorFloors(buildingId, { pageNum: 1, pageSize: 200 });
          return {
            buildingId,
            floors: response.data?.list || [],
          };
        }),
      );
      if (!active) {
        return;
      }
      const nextOptions = results.flatMap(({ buildingId, floors }) =>
        floors.map((floor) => ({
          value: floor.id,
          buildingId,
          label: `${pickIndoorFloorLabel(floor)} · ${floor.floorCode || `F${floor.floorNumber}`}`,
        })),
      );
      setIndoorFloorOptions(nextOptions);
      const currentFloorBindings = (form.getFieldValue('indoorFloorBindings') as number[] | undefined) ?? EMPTY_NUMBER_ARRAY;
      const allowedIds = new Set(nextOptions.map((item) => item.value));
      const filteredFloorBindings = currentFloorBindings.filter((item) => allowedIds.has(item));
      const bindingsChanged =
        filteredFloorBindings.length !== currentFloorBindings.length
        || filteredFloorBindings.some((item, index) => item !== currentFloorBindings[index]);
      if (bindingsChanged) {
        form.setFieldValue('indoorFloorBindings', filteredFloorBindings);
      }
    };
    void loadFloors();
    return () => {
      active = false;
    };
  }, [form, selectedBuildingBindingKey]);

  return {
    translationDefaults: translationRequest.data?.data,
    storylineOptions,
    cityOptions,
    subMapOptions,
    indoorBuildingOptions,
    indoorFloorOptions,
  };
}

function CollectionPresetEditor({
  form,
  kind,
  presetFieldName,
  configFieldName,
}: CollectionPresetEditorProps) {
  const [advanced, setAdvanced] = useState(false);
  const presetValue = Form.useWatch(presetFieldName, form) as string | undefined;
  const configValue = Form.useWatch(configFieldName, form) as string | undefined;
  const parsedConfig = useMemo(() => safeParseJson(configValue), [configValue]);

  const meta = useMemo(() => {
    if (kind === 'popup') {
      return {
        title: '彈窗設定',
        description: '先選模板，再細化標題、摘要與媒體呈現；只有需要額外控制時才打開 JSON。',
        options: popupPresetOptions,
      };
    }
    if (kind === 'display') {
      return {
        title: '展示設定',
        description: '控制列表卡片、地圖展示與室內綁定的顯示策略。',
        options: displayPresetOptions,
      };
    }
    return {
      title: '觸發設定',
      description: '定義領取或解鎖方式，支援到點、章節完成、兌換與手動領取等模板。',
      options: triggerPresetOptions,
    };
  }, [kind]);

  useEffect(() => {
    if (!presetValue || hasText(configValue)) {
      return;
    }
    if (presetValue === 'custom') {
      form.setFieldValue(configFieldName, stringifyJson({}));
      return;
    }
    const patch = kind === 'popup'
      ? buildPopupPatch(presetValue)
      : kind === 'display'
        ? buildDisplayPatch(presetValue)
        : buildTriggerPatch(presetValue);
    form.setFieldValue(configFieldName, stringifyJson(patch));
  }, [configFieldName, configValue, form, kind, presetValue]);

  const commitPreset = (nextPreset: string) => {
    form.setFieldValue(presetFieldName, nextPreset);
    if (nextPreset === 'custom') {
      return;
    }
    const patch = kind === 'popup'
      ? buildPopupPatch(nextPreset)
      : kind === 'display'
        ? buildDisplayPatch(nextPreset)
        : buildTriggerPatch(nextPreset);
    form.setFieldValue(configFieldName, withPatch(parsedConfig, patch));
  };

  const updateConfig = (patch: JsonRecord) => {
    form.setFieldValue(configFieldName, withPatch(parsedConfig, patch));
  };

  return (
    <Card
      size="small"
      title={meta.title}
      extra={<Tag color={advanced ? 'purple' : 'blue'}>{advanced ? '進階 JSON' : '預設優先'}</Tag>}
      style={{ height: '100%' }}
    >
      <Paragraph type="secondary" style={{ marginBottom: 16 }}>
        {meta.description}
      </Paragraph>

      <Form.Item label="模板" style={{ marginBottom: 12 }}>
        <Select
          value={presetValue || meta.options[0]?.value}
          options={meta.options}
          onChange={commitPreset}
        />
      </Form.Item>

      {kind === 'popup' ? (
        <>
          <Form.Item label="啟用彈窗" style={{ marginBottom: 12 }}>
            <Switch checked={parsedConfig.enabled !== false} onChange={(checked) => updateConfig({ enabled: checked })} />
          </Form.Item>
          <Form.Item label="彈窗模式" style={{ marginBottom: 12 }}>
            <Select
              value={(parsedConfig.mode as string) || 'sheet'}
              options={[
                { value: 'sheet', label: '底部資訊卡' },
                { value: 'modal', label: '置中彈窗' },
                { value: 'bubble', label: '地圖氣泡' },
                { value: 'toast', label: '短提示' },
              ]}
              onChange={(value) => updateConfig({ mode: value })}
            />
          </Form.Item>
          <Form.Item label="標題" style={{ marginBottom: 12 }}>
            <Input
              value={(parsedConfig.title as string) || ''}
              onChange={(event) => updateConfig({ title: event.target.value })}
              placeholder="例如：夜巡通行證已解鎖"
            />
          </Form.Item>
          <Form.Item label="摘要" style={{ marginBottom: 12 }}>
            <Input.TextArea
              rows={3}
              value={(parsedConfig.body as string) || ''}
              onChange={(event) => updateConfig({ body: event.target.value })}
              placeholder="填寫彈窗中的主文案或操作提示"
            />
          </Form.Item>
          <Form.Item label="媒體優先類型" style={{ marginBottom: 0 }}>
            <Select
              value={(parsedConfig.mediaUsageType as string) || 'cover'}
              options={[
                { value: 'cover', label: '封面' },
                { value: 'gallery', label: '附件圖庫' },
                { value: 'icon', label: '圖示' },
                { value: 'video', label: '影片' },
              ]}
              onChange={(value) => updateConfig({ mediaUsageType: value })}
            />
          </Form.Item>
        </>
      ) : null}

      {kind === 'display' ? (
        <>
          <Form.Item label="展示布局" style={{ marginBottom: 12 }}>
            <Select
              value={(parsedConfig.layout as string) || 'map-card'}
              options={[
                { value: 'map-card', label: '地圖卡片' },
                { value: 'inventory-card', label: '庫存卡片' },
                { value: 'ribbon', label: '橫幅' },
                { value: 'rail', label: '橫向資源軌' },
              ]}
              onChange={(value) => updateConfig({ layout: value })}
            />
          </Form.Item>
          <Form.Item label="主題風格" style={{ marginBottom: 12 }}>
            <Select
              value={(parsedConfig.theme as string) || 'collectible'}
              options={[
                { value: 'collectible', label: '收集物' },
                { value: 'achievement', label: '成就' },
                { value: 'reward', label: '獎勵' },
                { value: 'media-first', label: '媒體優先' },
              ]}
              onChange={(value) => updateConfig({ theme: value })}
            />
          </Form.Item>
          <Form.Item label="重點色" style={{ marginBottom: 12 }}>
            <Select
              value={(parsedConfig.accent as string) || 'amber'}
              options={[
                { value: 'amber', label: '琥珀' },
                { value: 'gold', label: '金色' },
                { value: 'ruby', label: '紅寶石' },
                { value: 'teal', label: '青綠' },
              ]}
              onChange={(value) => updateConfig({ accent: value })}
            />
          </Form.Item>
          <Space direction="vertical" size={8} style={{ width: '100%' }}>
            <Space>
              <Text type="secondary">顯示副標</Text>
              <Switch checked={parsedConfig.showSubtitle !== false} onChange={(checked) => updateConfig({ showSubtitle: checked })} />
            </Space>
            <Space>
              <Text type="secondary">顯示地圖綁定</Text>
              <Switch checked={!!parsedConfig.showLocationBinding} onChange={(checked) => updateConfig({ showLocationBinding: checked })} />
            </Space>
            <Space>
              <Text type="secondary">顯示室內綁定</Text>
              <Switch checked={!!parsedConfig.showIndoorBinding} onChange={(checked) => updateConfig({ showIndoorBinding: checked })} />
            </Space>
            <Space>
              <Text type="secondary">顯示庫存資訊</Text>
              <Switch checked={!!parsedConfig.showInventory} onChange={(checked) => updateConfig({ showInventory: checked })} />
            </Space>
          </Space>
        </>
      ) : null}

      {kind === 'trigger' ? (
        <>
          <Form.Item label="觸發類型" style={{ marginBottom: 12 }}>
            <Select
              value={(parsedConfig.triggerType as string) || 'poi_arrival'}
              options={[
                { value: 'poi_arrival', label: '到點或靠近時觸發' },
                { value: 'story_completion', label: '故事線完成' },
                { value: 'chapter_completion', label: '章節完成' },
                { value: 'reward_redemption', label: '兌換時觸發' },
                { value: 'manual_claim', label: '手動領取' },
              ]}
              onChange={(value) => updateConfig({ triggerType: value })}
            />
          </Form.Item>
          <Row gutter={12}>
            <Col span={12}>
              <Form.Item label="半徑（米）" style={{ marginBottom: 12 }}>
                <Input
                  value={String(parsedConfig.radiusMeters ?? '')}
                  onChange={(event) => updateConfig({ radiusMeters: Number(event.target.value || 0) })}
                  placeholder="50"
                />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item label="停留秒數" style={{ marginBottom: 12 }}>
                <Input
                  value={String(parsedConfig.dwellSeconds ?? '')}
                  onChange={(event) => updateConfig({ dwellSeconds: Number(event.target.value || 0) })}
                  placeholder="10"
                />
              </Form.Item>
            </Col>
          </Row>
          <Space direction="vertical" size={8} style={{ width: '100%' }}>
            <Space>
              <Text type="secondary">需要室內樓層命中</Text>
              <Switch checked={!!parsedConfig.requiresIndoorFloor} onChange={(checked) => updateConfig({ requiresIndoorFloor: checked })} />
            </Space>
            <Space>
              <Text type="secondary">兌換時消耗印章</Text>
              <Switch checked={!!parsedConfig.consumeStamps} onChange={(checked) => updateConfig({ consumeStamps: checked })} />
            </Space>
          </Space>
          <Form.Item label="前置徽章代碼" style={{ marginTop: 12, marginBottom: 0 }}>
            <Input
              value={(parsedConfig.requiresBadgeCode as string) || ''}
              onChange={(event) => updateConfig({ requiresBadgeCode: event.target.value })}
              placeholder="例如：badge_lisboeta_pathfinder"
            />
          </Form.Item>
        </>
      ) : null}

      <Space style={{ display: 'flex', justifyContent: 'space-between', marginTop: 16 }}>
        <Text type="secondary">若需要額外欄位，可切到 JSON 模式微調。</Text>
        <Switch checked={advanced} checkedChildren="JSON" unCheckedChildren="表單" onChange={setAdvanced} />
      </Space>

      {advanced ? (
        <Form.Item label="進階 JSON" style={{ marginTop: 16, marginBottom: 0 }}>
          <Input.TextArea
            rows={8}
            value={configValue || ''}
            onChange={(event) => form.setFieldValue(configFieldName, event.target.value)}
            placeholder='例如：{"enabled":true,"mode":"sheet"}'
          />
        </Form.Item>
      ) : null}

      <Form.Item name={presetFieldName} hidden>
        <Input />
      </Form.Item>
      <Form.Item name={configFieldName} hidden>
        <Input />
      </Form.Item>
    </Card>
  );
}

export function CollectionLocalizedCoreFields({
  form,
  translationDefaults,
  entityLabel,
  includeSubtitle,
  includeHighlight,
  includeExampleContent = true,
}: {
  form: FormInstance;
  translationDefaults?: Partial<AdminTranslationSettings>;
  entityLabel: string;
  includeSubtitle?: boolean;
  includeHighlight?: boolean;
  includeExampleContent?: boolean;
}) {
  return (
    <>
      <LocalizedFieldGroup
        form={form}
        label={`${entityLabel}名稱`}
        fieldNames={nameFields}
        required
        translationDefaults={translationDefaults}
      />
      {includeSubtitle ? (
        <LocalizedFieldGroup
          form={form}
          label={`${entityLabel}副標`}
          fieldNames={subtitleFields}
          translationDefaults={translationDefaults}
        />
      ) : null}
      <LocalizedFieldGroup
        form={form}
        label={`${entityLabel}介紹`}
        fieldNames={descriptionFields}
        multiline
        rows={4}
        translationDefaults={translationDefaults}
      />
      {includeHighlight ? (
        <LocalizedFieldGroup
          form={form}
          label={`${entityLabel}亮點文案`}
          fieldNames={highlightFields}
          translationDefaults={translationDefaults}
        />
      ) : null}
      {includeExampleContent ? (
        <LocalizedFieldGroup
          form={form}
          label="示例內容"
          fieldNames={exampleContentFields}
          multiline
          rows={5}
          help="這組內容會作為後續小程序展示與驗證用的示例文案，可直接描述觸發效果、故事節奏與資源展示方式。"
          translationDefaults={translationDefaults}
        />
      ) : null}
    </>
  );
}

export function CollectionBehaviorSection({ form }: { form: FormInstance }) {
  return (
    <Card
      size="small"
      title="彈窗 / 展示 / 觸發"
      style={{ marginBottom: 24 }}
      extra={<Tag color="purple">模板優先</Tag>}
    >
      <Row gutter={[16, 16]}>
        <Col xs={24} lg={8}>
          <CollectionPresetEditor
            form={form}
            kind="popup"
            presetFieldName="popupPresetCode"
            configFieldName="popupConfigJson"
          />
        </Col>
        <Col xs={24} lg={8}>
          <CollectionPresetEditor
            form={form}
            kind="display"
            presetFieldName="displayPresetCode"
            configFieldName="displayConfigJson"
          />
        </Col>
        <Col xs={24} lg={8}>
          <CollectionPresetEditor
            form={form}
            kind="trigger"
            presetFieldName="triggerPresetCode"
            configFieldName="triggerConfigJson"
          />
        </Col>
      </Row>
    </Card>
  );
}

export function CollectionBindingSection({ form, options }: CollectionBindingSectionProps) {
  return (
    <Card size="small" title="綁定關係" style={{ marginBottom: 24 }}>
      <Paragraph type="secondary">
        這些綁定會同步寫入 canonical relation graph，公開接口與小程序會直接依此讀取城市、子地圖、故事線與室內上下文。
      </Paragraph>
      <Row gutter={[16, 16]}>
        <Col xs={24} md={12}>
          <Form.Item name="storylineBindings" label="故事線">
            <Select
              mode="multiple"
              allowClear
              showSearch
              optionFilterProp="label"
              options={options.storylineOptions}
              placeholder="可綁定多條故事線"
            />
          </Form.Item>
        </Col>
        <Col xs={24} md={12}>
          <Form.Item name="cityBindings" label="城市 / 大地圖">
            <Select
              mode="multiple"
              allowClear
              showSearch
              optionFilterProp="label"
              options={options.cityOptions}
              placeholder="可綁定多個城市"
            />
          </Form.Item>
        </Col>
        <Col xs={24} md={12}>
          <Form.Item name="subMapBindings" label="子地圖">
            <Select
              mode="multiple"
              allowClear
              showSearch
              optionFilterProp="label"
              options={options.subMapOptions}
              placeholder="可綁定多個子地圖"
            />
          </Form.Item>
        </Col>
        <Col xs={24} md={12}>
          <Form.Item name="indoorBuildingBindings" label="室內建築">
            <Select
              mode="multiple"
              allowClear
              showSearch
              optionFilterProp="label"
              options={options.indoorBuildingOptions}
              placeholder="綁定後才可再選樓層"
            />
          </Form.Item>
        </Col>
        <Col xs={24}>
          <Form.Item name="indoorFloorBindings" label="室內樓層">
            <Select
              mode="multiple"
              allowClear
              showSearch
              optionFilterProp="label"
              options={options.indoorFloorOptions}
              placeholder={options.indoorFloorOptions.length ? '選擇已綁定建築下的樓層' : '請先選擇室內建築'}
              disabled={!options.indoorFloorOptions.length}
            />
          </Form.Item>
        </Col>
      </Row>
    </Card>
  );
}

export function CollectionMediaSection({
  includeIcon = false,
  includeAnimation = false,
  attachmentHelp,
}: CollectionMediaSectionProps) {
  return (
    <Card size="small" title="媒體與附件" style={{ marginBottom: 24 }}>
      <Row gutter={[16, 16]}>
        <Col xs={24} lg={includeIcon || includeAnimation ? 8 : 12}>
          <MediaAssetPickerField
            name="coverAssetId"
            label="封面資源"
            assetKind="image"
            valueMode="asset-id"
            required
            help="支援直接開資料夾選取、拖拽或貼上；上傳成功後會自動回填封面資源。"
          />
        </Col>
        {includeIcon ? (
          <Col xs={24} lg={includeAnimation ? 8 : 12}>
            <MediaAssetPickerField
              name="iconAssetId"
              label="圖示資源"
              assetKind="icon"
              valueMode="asset-id"
              help="供地圖、卡片或成就徽章等小型展示使用。"
            />
          </Col>
        ) : null}
        {includeAnimation ? (
          <Col xs={24} lg={includeIcon ? 8 : 12}>
            <MediaAssetPickerField
              name="animationAssetId"
              label="動效資源"
              valueMode="asset-id"
              help="可選 GIF、影片或動態圖示，用於解鎖演出或地圖特效。"
            />
          </Col>
        ) : null}
      </Row>
      <MediaAssetArrayField
        name="attachmentAssetIds"
        label="附件資源"
        help={attachmentHelp || '附件會跟隨主體一起同步到公開接口，可用於圖集、音訊、影片或補充檔案。'}
      />
    </Card>
  );
}
