import React, { useEffect, useMemo, useState } from 'react';
import dayjs, { type Dayjs } from 'dayjs';
import {
  Alert,
  Button,
  Card,
  Col,
  Collapse,
  Form,
  Input,
  InputNumber,
  Row,
  Select,
  Space,
  Statistic,
  Tag,
  Typography,
} from 'antd';
import type { FormInstance } from 'antd/es/form';
import { DeleteOutlined, PlusOutlined, ReloadOutlined } from '@ant-design/icons';
import MediaAssetPickerField from '../media/MediaAssetPickerField';
import {
  applyCollectionLocaleFallback,
  CollectionBindingSection,
  CollectionLocalizedCoreFields,
  CollectionMediaSection,
  useCollectionAuthoringOptions,
} from '../../pages/Collectibles/CollectionAuthoringShared';
import { getAdminRewardPresentations, getAdminRewardRules } from '../../services/api';
import type {
  AdminGameRewardItem,
  AdminGameRewardPayload,
  AdminRedeemablePrizeItem,
  AdminRedeemablePrizePayload,
  AdminRewardGovernanceOverview,
  AdminRewardPresentationItem,
  AdminRewardPresentationPayload,
  AdminRewardRuleConditionGroupItem,
  AdminRewardRuleConditionItem,
  AdminRewardRuleItem,
  AdminRewardRulePayload,
  RewardRuleGroupMode,
} from '../../types/admin';

const { Paragraph, Text } = Typography;

type OptionItem = { label: string; value: string | number };

export const rewardStatusOptions: OptionItem[] = [
  { value: 'draft', label: '編輯中' },
  { value: 'published', label: '已發佈' },
  { value: 'archived', label: '已封存' },
];

export const prizeTypeOptions: OptionItem[] = [
  { value: 'merchandise', label: '實體周邊' },
  { value: 'postcard', label: '明信片 / 紙品' },
  { value: 'coupon', label: '優惠券' },
  { value: 'ticket', label: '門票' },
  { value: 'code', label: '序號 / 券碼' },
  { value: 'virtual_item_pack', label: '虛擬獎勵包' },
];

export const fulfillmentModeOptions: OptionItem[] = [
  { value: 'offline_pickup', label: '線下領取' },
  { value: 'postal_delivery', label: '郵寄配送' },
  { value: 'virtual_issue', label: '虛擬發放' },
  { value: 'voucher_code', label: '券碼 / 兌換碼' },
];

export const rewardTypeOptions: OptionItem[] = [
  { value: 'badge', label: '徽章' },
  { value: 'title', label: '稱號' },
  { value: 'city_currency', label: '城市限定貨幣' },
  { value: 'city_fragment', label: '城市限定碎片' },
  { value: 'unlock_pass', label: '解鎖通行證' },
  { value: 'voice_pack', label: '語音包' },
  { value: 'cosmetic', label: '外觀 / 特效' },
  { value: 'collectible_bonus', label: '收集加成' },
  { value: 'privilege', label: '特權' },
];

export const honorRewardTypeOptions: OptionItem[] = rewardTypeOptions.filter((item) =>
  ['badge', 'title'].includes(String(item.value)),
);

export const rarityOptions: OptionItem[] = [
  { value: 'common', label: '普通' },
  { value: 'rare', label: '稀有' },
  { value: 'epic', label: '史詩' },
  { value: 'legendary', label: '傳奇' },
];

export const ruleTypeOptions: OptionItem[] = [
  { value: 'redemption_rule', label: '兌換條件規則' },
  { value: 'grant_rule', label: '發放規則' },
  { value: 'interaction_grant_rule', label: '互動發放規則' },
  { value: 'composite_rule', label: '複合規則' },
];

export const groupModeOptions: OptionItem[] = [
  { value: 'all', label: '全部滿足' },
  { value: 'any', label: '符合任一項' },
  { value: 'at_least', label: '至少滿足幾項' },
];

export const conditionTypeOptions: OptionItem[] = [
  { value: 'numeric_progress', label: '數值進度' },
  { value: 'content_unlock_state', label: '內容解鎖狀態' },
  { value: 'interaction_history', label: '互動歷史' },
  { value: 'time_scope', label: '時間與範圍' },
];

export const metricTypeOptions: OptionItem[] = [
  { value: 'city_exploration_percent', label: '城市探索進度' },
  { value: 'sub_map_exploration_percent', label: '子地圖探索進度' },
  { value: 'indoor_exploration_percent', label: '室內探索進度' },
  { value: 'triggered_behavior', label: '曾觸發互動行為' },
  { value: 'owned_badge', label: '已擁有徽章' },
  { value: 'owned_title', label: '已擁有稱號' },
  { value: 'owned_fragment', label: '已擁有碎片' },
  { value: 'weekday_window', label: '時間窗口' },
];

export const comparatorOptions: OptionItem[] = [
  { value: 'gte', label: '大於等於' },
  { value: 'lte', label: '小於等於' },
  { value: 'eq', label: '等於' },
  { value: 'contains', label: '包含 / 曾觸發' },
  { value: 'between', label: '介於區間' },
];

export const stockModeOptions: OptionItem[] = [
  { value: 'limited', label: '固定庫存' },
  { value: 'rolling', label: '分批補貨' },
  { value: 'reservation', label: '預約名額' },
  { value: 'unlimited', label: '不限量' },
];

export const binaryFlagOptions: OptionItem[] = [
  { value: 1, label: '是' },
  { value: 0, label: '否' },
];

export const shippingFeeModeOptions: OptionItem[] = [
  { value: 'seller_paid', label: '由主辦方吸收' },
  { value: 'recipient_paid', label: '由用戶支付' },
  { value: 'cod', label: '到付' },
];

export const stepTypeOptions: OptionItem[] = [
  { value: 'popup_card', label: '彈窗卡片' },
  { value: 'fullscreen_video', label: '全屏影片' },
  { value: 'fullscreen_animation', label: '全屏動畫' },
  { value: 'toast', label: '短提示' },
  { value: 'voice_over', label: '語音播報' },
  { value: 'reward_badge', label: '徽章 / 稱號亮相' },
];

export const presentationTypeOptions: OptionItem[] = [
  { value: 'none', label: '不演出' },
  { value: 'toast', label: '短提示' },
  { value: 'popup_card', label: '彈窗卡片' },
  { value: 'fullscreen_animation', label: '全屏動畫' },
  { value: 'fullscreen_video', label: '全屏影片' },
  { value: 'fullscreen_video_with_overlay', label: '全屏影片 + 疊層' },
  { value: 'sequence', label: '分鏡序列' },
];

export const interruptPolicyOptions: OptionItem[] = [
  { value: 'queue_after_current', label: '排隊等待當前互動結束' },
  { value: 'interrupt_allowed', label: '可中斷當前互動' },
  { value: 'block_until_idle', label: '僅在空閒時播放' },
];

export const queuePolicyOptions: OptionItem[] = [
  { value: 'enqueue', label: '加入佇列' },
  { value: 'replace_lower_priority', label: '取代低優先演出' },
  { value: 'drop_if_busy', label: '忙碌時直接略過' },
];

export interface RewardReferenceData {
  translationDefaults?: ReturnType<typeof useCollectionAuthoringOptions>['translationDefaults'];
  storylineOptions: ReturnType<typeof useCollectionAuthoringOptions>['storylineOptions'];
  cityOptions: ReturnType<typeof useCollectionAuthoringOptions>['cityOptions'];
  subMapOptions: ReturnType<typeof useCollectionAuthoringOptions>['subMapOptions'];
  indoorBuildingOptions: ReturnType<typeof useCollectionAuthoringOptions>['indoorBuildingOptions'];
  indoorFloorOptions: ReturnType<typeof useCollectionAuthoringOptions>['indoorFloorOptions'];
  ruleOptions: OptionItem[];
  presentationOptions: OptionItem[];
  rules: AdminRewardRuleItem[];
  presentations: AdminRewardPresentationItem[];
  refreshReferences: () => Promise<void>;
  loading: boolean;
}

export interface RedeemablePrizeFormValues extends Omit<Partial<AdminRedeemablePrizeItem>, 'publishStartAt' | 'publishEndAt'> {
  publishStartAt?: Dayjs | null;
  publishEndAt?: Dayjs | null;
  stockMode?: string;
  stockAlertThreshold?: number | null;
  pickupVenue?: string;
  pickupSchedule?: string;
  verificationMethod?: string;
  supportedRegions?: string;
  shippingFeeMode?: string;
  unlockTarget?: string;
  codePool?: string;
  claimTimeoutHours?: number | null;
}

export interface GameRewardFormValues extends Omit<Partial<AdminGameRewardItem>, 'publishStartAt' | 'publishEndAt'> {
  publishStartAt?: Dayjs | null;
  publishEndAt?: Dayjs | null;
  configProfileAccent?: string;
  configEconomyCode?: string;
  configGrantMode?: string;
  configDisplayTag?: string;
}

export interface RewardRuleFormValues extends Omit<Partial<AdminRewardRuleItem>, 'conditionGroups'> {
  conditionGroups?: AdminRewardRuleConditionGroupItem[];
}

export interface RewardPresentationFormValues extends Omit<Partial<AdminRewardPresentationItem>, 'steps'> {
  steps?: AdminRewardPresentationItem['steps'];
}

function hasText(value?: string | null) {
  return typeof value === 'string' && value.trim().length > 0;
}

function safeParseJson<T extends Record<string, any>>(value?: string | null): T {
  if (!hasText(value)) {
    return {} as T;
  }
  try {
    const parsed = JSON.parse(value);
    return parsed && typeof parsed === 'object' && !Array.isArray(parsed) ? (parsed as T) : ({} as T);
  } catch {
    return {} as T;
  }
}

function stringifyJson(value: Record<string, any>) {
  return JSON.stringify(value, null, 2);
}

function parseDate(value?: string | null) {
  return value ? dayjs(value) : undefined;
}

function formDate(value?: Dayjs | null) {
  return value ? value.format('YYYY-MM-DDTHH:mm:ss') : null;
}

function buildDefaultCondition(): AdminRewardRuleConditionItem {
  return {
    conditionType: 'numeric_progress',
    metricType: 'city_exploration_percent',
    operatorType: 'gte',
    comparatorValue: '80',
    comparatorUnit: 'percent',
    summaryText: '',
    configJson: '',
    sortOrder: 0,
  };
}

function buildDefaultGroup(): AdminRewardRuleConditionGroupItem {
  return {
    groupCode: 'group_1',
    operatorType: 'all',
    minimumMatchCount: undefined,
    summaryText: '',
    advancedConfigJson: '',
    sortOrder: 0,
    conditions: [buildDefaultCondition()],
  };
}

function summarizeGroup(group?: AdminRewardRuleConditionGroupItem) {
  const mode = (group?.operatorType || 'all') as RewardRuleGroupMode;
  const labels = {
    all: '全部滿足',
    any: '任一項成立',
    at_least: `至少 ${group?.minimumMatchCount || 1} 項`,
  };
  return `${labels[mode] || mode} · ${group?.conditions?.length || 0} 個條件`;
}

export function formatRuleSummary(rule?: Pick<AdminRewardRuleItem, 'summaryText' | 'nameZh' | 'nameZht'>) {
  return rule?.summaryText || rule?.nameZht || rule?.nameZh || '未命名規則';
}

function formatOwnerDomainLabel(ownerDomain?: string) {
  switch (ownerDomain) {
    case 'redeemable_prize':
      return '兌換獎勵';
    case 'game_reward':
      return '遊戲內獎勵';
    case 'indoor_behavior':
      return '室內互動';
    default:
      return ownerDomain || '未命名主體';
  }
}

function formatLinkedOwnerSummary(
  owners?: { ownerDomain?: string | null }[],
) {
  if (!owners?.length) {
    return '尚未綁定';
  }
  const groups = owners.reduce<Record<string, number>>((acc, owner) => {
    const label = formatOwnerDomainLabel(owner.ownerDomain || undefined);
    acc[label] = (acc[label] || 0) + 1;
    return acc;
  }, {});
  return Object.entries(groups)
    .map(([label, count]) => `${label} ${count}`)
    .join(' · ');
}

export function renderOwnerTags(owners?: { ownerDomain?: string; ownerName?: string; bindingRole?: string }[]) {
  if (!owners?.length) {
    return <Text type="secondary">尚未綁定</Text>;
  }
  return (
    <Space wrap size={[4, 4]}>
      {owners.slice(0, 6).map((owner, index) => (
        <Tag
          key={`${owner.ownerDomain}-${owner.ownerName}-${index}`}
          color={
            owner.ownerDomain === 'redeemable_prize'
              ? 'gold'
              : owner.ownerDomain === 'game_reward'
                ? 'blue'
                : owner.ownerDomain === 'indoor_behavior'
                  ? 'cyan'
                  : 'default'
          }
        >
          {formatOwnerDomainLabel(owner.ownerDomain)}
          {`：${owner.ownerName || '未命名綁定'}`}
          {owner.bindingRole ? ` · ${owner.bindingRole}` : ''}
        </Tag>
      ))}
      {owners.length > 6 ? <Tag>+{owners.length - 6}</Tag> : null}
    </Space>
  );
}

export function normalizeRuleGroups(groups?: AdminRewardRuleConditionGroupItem[]) {
  if (!groups?.length) {
    return [buildDefaultGroup()];
  }
  return groups.map((group, index) => ({
    groupCode: group.groupCode || `group_${index + 1}`,
    operatorType: group.operatorType || 'all',
    minimumMatchCount: group.minimumMatchCount ?? undefined,
    summaryText: group.summaryText,
    advancedConfigJson: group.advancedConfigJson,
    sortOrder: group.sortOrder ?? index,
    conditions: (group.conditions?.length ? group.conditions : [buildDefaultCondition()]).map((condition, conditionIndex) => ({
      conditionType: condition.conditionType || 'numeric_progress',
      metricType: condition.metricType || 'city_exploration_percent',
      operatorType: condition.operatorType || 'gte',
      comparatorValue: condition.comparatorValue || '',
      comparatorUnit: condition.comparatorUnit || '',
      summaryText: condition.summaryText || '',
      configJson: condition.configJson || '',
      sortOrder: condition.sortOrder ?? conditionIndex,
    })),
  }));
}

export function useRewardDomainReferenceData(form: FormInstance): RewardReferenceData {
  const bindingOptions = useCollectionAuthoringOptions(form);
  const [rules, setRules] = useState<AdminRewardRuleItem[]>([]);
  const [presentations, setPresentations] = useState<AdminRewardPresentationItem[]>([]);
  const [loading, setLoading] = useState(false);

  const refreshReferences = async () => {
    setLoading(true);
    try {
      const [ruleResponse, presentationResponse] = await Promise.all([
        getAdminRewardRules({ pageNum: 1, pageSize: 200, status: 'published' }),
        getAdminRewardPresentations({ pageNum: 1, pageSize: 200, status: 'published' }),
      ]);
      setRules(ruleResponse.data?.list || []);
      setPresentations(presentationResponse.data?.list || []);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void refreshReferences();
  }, []);

  return {
    ...bindingOptions,
    rules,
    presentations,
    ruleOptions: rules.map((rule) => ({
      value: rule.id,
      label: `${rule.nameZht || rule.nameZh || rule.code} · ${formatRuleSummary(rule)}`,
    })),
    presentationOptions: presentations.map((presentation) => ({
      value: presentation.id,
      label: `${presentation.nameZht || presentation.nameZh || presentation.code} · ${presentation.presentationType || 'presentation'}`,
    })),
    refreshReferences,
    loading,
  };
}

function buildPrizeStockPolicy(values: RedeemablePrizeFormValues) {
  return stringifyJson({
    stockMode: values.stockMode || 'limited',
    alertThreshold: values.stockAlertThreshold ?? null,
    inventoryTotal: values.inventoryTotal ?? 0,
    inventoryRedeemed: values.inventoryRedeemed ?? 0,
  });
}

function buildPrizeFulfillmentConfig(values: RedeemablePrizeFormValues) {
  return stringifyJson({
    pickupVenue: values.pickupVenue || '',
    pickupSchedule: values.pickupSchedule || '',
    verificationMethod: values.verificationMethod || '',
    supportedRegions: values.supportedRegions || '',
    shippingFeeMode: values.shippingFeeMode || '',
    unlockTarget: values.unlockTarget || '',
    codePool: values.codePool || '',
    claimTimeoutHours: values.claimTimeoutHours ?? null,
  });
}

function buildGameRewardConfig(values: GameRewardFormValues) {
  return stringifyJson({
    profileAccent: values.configProfileAccent || '',
    economyCode: values.configEconomyCode || '',
    grantMode: values.configGrantMode || '',
    displayTag: values.configDisplayTag || '',
  });
}

export function withRedeemablePrizeDefaults(detail?: Partial<AdminRedeemablePrizeItem>): RedeemablePrizeFormValues {
  const stockPolicy = safeParseJson<Record<string, any>>(detail?.stockPolicyJson);
  const fulfillmentConfig = safeParseJson<Record<string, any>>(detail?.fulfillmentConfigJson);
  return {
    prizeType: 'virtual_item_pack',
    fulfillmentMode: 'virtual_issue',
    stampCost: 0,
    inventoryTotal: 0,
    inventoryRedeemed: 0,
    stockMode: stockPolicy.stockMode || 'limited',
    stockAlertThreshold: stockPolicy.alertThreshold ?? undefined,
    pickupVenue: fulfillmentConfig.pickupVenue || '',
    pickupSchedule: fulfillmentConfig.pickupSchedule || '',
    verificationMethod: fulfillmentConfig.verificationMethod || '',
    supportedRegions: fulfillmentConfig.supportedRegions || '',
    shippingFeeMode: fulfillmentConfig.shippingFeeMode || '',
    unlockTarget: fulfillmentConfig.unlockTarget || '',
    codePool: fulfillmentConfig.codePool || '',
    claimTimeoutHours: fulfillmentConfig.claimTimeoutHours ?? undefined,
    status: 'draft',
    sortOrder: 0,
    storylineBindings: [],
    cityBindings: [],
    subMapBindings: [],
    indoorBuildingBindings: [],
    indoorFloorBindings: [],
    attachmentAssetIds: [],
    ruleIds: [],
    ...detail,
    publishStartAt: parseDate(detail?.publishStartAt),
    publishEndAt: parseDate(detail?.publishEndAt),
  };
}

export function buildRedeemablePrizePayload(values: RedeemablePrizeFormValues): AdminRedeemablePrizePayload {
  const normalized = applyCollectionLocaleFallback(values, ['name', 'subtitle', 'description', 'highlight']);
  return {
    code: normalized.code || '',
    ...normalized,
    nameZh: normalized.nameZh || normalized.nameZht || '',
    publishStartAt: formDate(normalized.publishStartAt),
    publishEndAt: formDate(normalized.publishEndAt),
    stockPolicyJson: buildPrizeStockPolicy(values),
    fulfillmentConfigJson: buildPrizeFulfillmentConfig(values),
    storylineBindings: normalized.storylineBindings || [],
    cityBindings: normalized.cityBindings || [],
    subMapBindings: normalized.subMapBindings || [],
    indoorBuildingBindings: normalized.indoorBuildingBindings || [],
    indoorFloorBindings: normalized.indoorFloorBindings || [],
    attachmentAssetIds: normalized.attachmentAssetIds || [],
    ruleIds: normalized.ruleIds || [],
  };
}

export function withGameRewardDefaults(detail?: Partial<AdminGameRewardItem>): GameRewardFormValues {
  const rewardConfig = safeParseJson<Record<string, any>>(detail?.rewardConfigJson);
  return {
    rewardType: 'badge',
    rarity: 'common',
    stackable: 0,
    maxOwned: 1,
    canEquip: 0,
    canConsume: 0,
    configProfileAccent: rewardConfig.profileAccent || '',
    configEconomyCode: rewardConfig.economyCode || '',
    configGrantMode: rewardConfig.grantMode || '',
    configDisplayTag: rewardConfig.displayTag || '',
    status: 'draft',
    sortOrder: 0,
    storylineBindings: [],
    cityBindings: [],
    subMapBindings: [],
    indoorBuildingBindings: [],
    indoorFloorBindings: [],
    attachmentAssetIds: [],
    ruleIds: [],
    ...detail,
    publishStartAt: parseDate(detail?.publishStartAt),
    publishEndAt: parseDate(detail?.publishEndAt),
  };
}

export function buildGameRewardPayload(values: GameRewardFormValues): AdminGameRewardPayload {
  const normalized = applyCollectionLocaleFallback(values, ['name', 'subtitle', 'description', 'highlight']);
  return {
    code: normalized.code || '',
    ...normalized,
    nameZh: normalized.nameZh || normalized.nameZht || '',
    publishStartAt: formDate(normalized.publishStartAt),
    publishEndAt: formDate(normalized.publishEndAt),
    rewardConfigJson: buildGameRewardConfig(values),
    storylineBindings: normalized.storylineBindings || [],
    cityBindings: normalized.cityBindings || [],
    subMapBindings: normalized.subMapBindings || [],
    indoorBuildingBindings: normalized.indoorBuildingBindings || [],
    indoorFloorBindings: normalized.indoorFloorBindings || [],
    attachmentAssetIds: normalized.attachmentAssetIds || [],
    ruleIds: normalized.ruleIds || [],
  };
}

export function withRewardRuleDefaults(detail?: Partial<AdminRewardRuleItem>): RewardRuleFormValues {
  return {
    ruleType: 'grant_rule',
    status: 'draft',
    conditionGroups: normalizeRuleGroups(detail?.conditionGroups),
    ...detail,
  };
}

export function buildRewardRulePayload(values: RewardRuleFormValues): AdminRewardRulePayload {
  return {
    code: values.code || '',
    nameZh: values.nameZh || values.nameZht || '',
    nameZht: values.nameZht,
    ruleType: values.ruleType,
    status: values.status,
    summaryText: values.summaryText,
    advancedConfigJson: values.advancedConfigJson,
    conditionGroups: (values.conditionGroups || []).map((group, groupIndex) => ({
      groupCode: group.groupCode || `group_${groupIndex + 1}`,
      operatorType: group.operatorType || 'all',
      minimumMatchCount: group.operatorType === 'at_least' ? group.minimumMatchCount || 1 : undefined,
      summaryText: group.summaryText || summarizeGroup(group),
      advancedConfigJson: group.advancedConfigJson,
      sortOrder: group.sortOrder ?? groupIndex,
      conditions: (group.conditions || []).map((condition, conditionIndex) => ({
        conditionType: condition.conditionType || 'numeric_progress',
        metricType: condition.metricType || 'city_exploration_percent',
        operatorType: condition.operatorType || 'gte',
        comparatorValue: condition.comparatorValue || '',
        comparatorUnit: condition.comparatorUnit || '',
        summaryText: condition.summaryText || '',
        configJson: condition.configJson || '',
        sortOrder: condition.sortOrder ?? conditionIndex,
      })),
    })),
  };
}

export function withRewardPresentationDefaults(detail?: Partial<AdminRewardPresentationItem>): RewardPresentationFormValues {
  return {
    presentationType: 'popup_card',
    firstTimeOnly: 1,
    skippable: 1,
    minimumDisplayMs: 1800,
    interruptPolicy: 'queue_after_current',
    queuePolicy: 'enqueue',
    priorityWeight: 50,
    status: 'draft',
    steps: detail?.steps?.length
      ? detail.steps.map((step, index) => ({ ...step, sortOrder: step.sortOrder ?? index }))
      : [
          {
            stepType: 'popup_card',
            stepCode: 'step_1',
            titleText: '獲得獎勵',
            durationMs: 1800,
            skippableOverride: 1,
            sortOrder: 0,
          },
        ],
    ...detail,
  };
}

export function buildRewardPresentationPayload(values: RewardPresentationFormValues): AdminRewardPresentationPayload {
  return {
    code: values.code || '',
    nameZh: values.nameZh || values.nameZht || '',
    nameZht: values.nameZht,
    presentationType: values.presentationType,
    firstTimeOnly: values.firstTimeOnly,
    skippable: values.skippable,
    minimumDisplayMs: values.minimumDisplayMs,
    interruptPolicy: values.interruptPolicy,
    queuePolicy: values.queuePolicy,
    priorityWeight: values.priorityWeight,
    coverAssetId: values.coverAssetId,
    voiceOverAssetId: values.voiceOverAssetId,
    sfxAssetId: values.sfxAssetId,
    summaryText: values.summaryText,
    configJson: values.configJson,
    status: values.status,
    steps: (values.steps || []).map((step, index) => ({
      ...step,
      sortOrder: step.sortOrder ?? index,
    })),
  };
}

interface RewardRelationSectionProps {
  form: FormInstance;
  references: RewardReferenceData;
  rewardFamily: 'redeemable' | 'game';
}

interface RewardBuilderSectionProps {
  form: FormInstance;
}

interface SplitRewardStatsProps {
  summary?: AdminRewardGovernanceOverview['summary'];
}

interface GameRewardConfigSectionProps extends RewardBuilderSectionProps {
  rewardTypeScope?: 'all' | 'honor';
}

export function RewardRelationSection({
  form,
  references,
  rewardFamily,
}: RewardRelationSectionProps) {
  const selectedRuleIds = (Form.useWatch('ruleIds', form) as number[] | undefined) || [];
  const selectedPresentationId = Form.useWatch('presentationId', form) as number | undefined;

  const selectedRules = useMemo(
    () => references.rules.filter((rule) => selectedRuleIds.includes(rule.id)),
    [references.rules, selectedRuleIds],
  );
  const selectedPresentation = useMemo(
    () => references.presentations.find((item) => item.id === selectedPresentationId),
    [references.presentations, selectedPresentationId],
  );

  return (
    <Card
      size="small"
      title="規則與獲得演出"
      extra={
        <Button size="small" icon={<ReloadOutlined />} onClick={() => void references.refreshReferences()}>
          重新整理引用
        </Button>
      }
      style={{ marginBottom: 24 }}
    >
      <Alert
        type="info"
        showIcon
        style={{ marginBottom: 16 }}
        message={rewardFamily === 'redeemable' ? '兌換條件與發放演出' : '獲得條件與展示演出'}
        description={
          rewardFamily === 'redeemable'
            ? '同一份共享規則可同時被兌換獎勵、遊戲內獎勵與室內互動行為引用。這裡只需挑選已建立的規則與演出，不必重複手寫 JSON。'
            : '遊戲內獎勵、稱號與徽章會共用這套規則與演出中心，確保獲得條件、全屏演出與語音播報行為一致。'
        }
      />

      <Row gutter={[16, 16]}>
        <Col xs={24} xl={14}>
          <Form.Item name="ruleIds" label="共享規則">
            <Select
              mode="multiple"
              allowClear
              showSearch
              optionFilterProp="label"
              loading={references.loading}
              options={references.ruleOptions}
              placeholder="選擇一條或多條共享規則"
            />
          </Form.Item>
        </Col>
        <Col xs={24} xl={10}>
          <Form.Item name="presentationId" label="獲得演出">
            <Select
              allowClear
              showSearch
              optionFilterProp="label"
              loading={references.loading}
              options={references.presentationOptions}
              placeholder="選擇獎勵獲得演出"
            />
          </Form.Item>
        </Col>
      </Row>

      <Row gutter={[16, 16]}>
        <Col xs={24} xl={14}>
          <Card size="small" title="目前已選規則">
            {selectedRules.length ? (
              <Space direction="vertical" size="middle" style={{ width: '100%' }}>
                {selectedRules.map((rule) => (
                  <Card key={rule.id} size="small">
                    <Space direction="vertical" size={6} style={{ width: '100%' }}>
                      <Space wrap>
                        <Tag color="blue">{rule.code}</Tag>
                        <Tag>{rule.ruleType || 'rule'}</Tag>
                        <Tag color={rule.status === 'published' ? 'green' : 'orange'}>
                          {rule.status === 'published' ? '已發佈' : rule.status || '編輯中'}
                        </Tag>
                      </Space>
                      <Text strong>{rule.nameZht || rule.nameZh || rule.code}</Text>
                      <Text type="secondary">{formatRuleSummary(rule)}</Text>
                      <Text type="secondary">
                        綁定摘要：{formatLinkedOwnerSummary(rule.linkedOwners)}
                      </Text>
                      {renderOwnerTags(rule.linkedOwners)}
                    </Space>
                  </Card>
                ))}
              </Space>
            ) : (
              <Text type="secondary">尚未選擇共享規則。</Text>
            )}
          </Card>
        </Col>
        <Col xs={24} xl={10}>
          <Card size="small" title="目前已選演出">
            {selectedPresentation ? (
              <Space direction="vertical" size={10} style={{ width: '100%' }}>
                <Space wrap>
                  <Tag color="purple">{selectedPresentation.code}</Tag>
                  <Tag>{selectedPresentation.presentationType || 'presentation'}</Tag>
                  <Tag color={selectedPresentation.status === 'published' ? 'green' : 'orange'}>
                    {selectedPresentation.status === 'published' ? '已發佈' : selectedPresentation.status || '編輯中'}
                  </Tag>
                </Space>
                <Text strong>{selectedPresentation.nameZht || selectedPresentation.nameZh || selectedPresentation.code}</Text>
                <Text type="secondary">{selectedPresentation.summaryText || '未填寫摘要'}</Text>
                <Space wrap>
                  <Tag>{selectedPresentation.minimumDisplayMs || 0} ms</Tag>
                  <Tag>{selectedPresentation.interruptPolicy || 'queue_after_current'}</Tag>
                  <Tag>{selectedPresentation.queuePolicy || 'enqueue'}</Tag>
                </Space>
                <Text type="secondary">
                  綁定摘要：{formatLinkedOwnerSummary(selectedPresentation.linkedOwners)}
                </Text>
                {renderOwnerTags(selectedPresentation.linkedOwners)}
              </Space>
            ) : (
              <Text type="secondary">尚未選擇獎勵演出。</Text>
            )}
          </Card>
        </Col>
      </Row>
    </Card>
  );
}

export function RedeemablePrizeConfigSection({ form }: RewardBuilderSectionProps) {
  const fulfillmentMode = (Form.useWatch('fulfillmentMode', form) as string | undefined) || 'virtual_issue';

  return (
    <Card size="small" title="兌換規格" style={{ marginBottom: 24 }}>
      <Row gutter={[16, 16]}>
        <Col xs={24} md={8}>
          <Form.Item name="prizeType" label="物品類型">
            <Select options={prizeTypeOptions} />
          </Form.Item>
        </Col>
        <Col xs={24} md={8}>
          <Form.Item name="fulfillmentMode" label="兌換方式">
            <Select options={fulfillmentModeOptions} />
          </Form.Item>
        </Col>
        <Col xs={24} md={8}>
          <Form.Item name="stampCost" label="兌換成本（印章）">
            <InputNumber min={0} style={{ width: '100%' }} />
          </Form.Item>
        </Col>
        <Col xs={24} md={8}>
          <Form.Item name="inventoryTotal" label="總庫存 / 名額">
            <InputNumber min={0} style={{ width: '100%' }} />
          </Form.Item>
        </Col>
        <Col xs={24} md={8}>
          <Form.Item name="inventoryRedeemed" label="已兌換 / 已用量">
            <InputNumber min={0} style={{ width: '100%' }} />
          </Form.Item>
        </Col>
        <Col xs={24} md={8}>
          <Form.Item name="stockMode" label="庫存策略">
            <Select options={stockModeOptions} />
          </Form.Item>
        </Col>
        <Col xs={24} md={8}>
          <Form.Item name="stockAlertThreshold" label="庫存提醒門檻">
            <InputNumber min={0} style={{ width: '100%' }} placeholder="低於此數值提醒營運" />
          </Form.Item>
        </Col>
        <Col xs={24}>
          <Form.Item name="operatorNotes" label="營運備註">
            <Input.TextArea rows={3} placeholder="記錄兌換流程、實體領取注意事項、供應商協調資訊等。" />
          </Form.Item>
        </Col>
      </Row>

      <Alert
        type="info"
        showIcon
        style={{ marginBottom: 16 }}
        message={`目前正在設定「${fulfillmentModeOptions.find((item) => item.value === fulfillmentMode)?.label || fulfillmentMode}」`}
        description="下方欄位會根據兌換方式切換到對應的可填資料，不需要再自己拼接 fulfillment JSON。"
      />

      <Row gutter={[16, 16]}>
        {fulfillmentMode === 'offline_pickup' ? (
          <>
            <Col xs={24} md={12}>
              <Form.Item name="pickupVenue" label="領取地點">
                <Input placeholder="例如：議事亭前地旅遊服務站" />
              </Form.Item>
            </Col>
            <Col xs={24} md={12}>
              <Form.Item name="pickupSchedule" label="領取時段">
                <Input placeholder="例如：每日 11:00 - 19:00" />
              </Form.Item>
            </Col>
            <Col xs={24}>
              <Form.Item name="verificationMethod" label="核銷方式">
                <Input placeholder="例如：出示 QR Code + 管理端核銷" />
              </Form.Item>
            </Col>
          </>
        ) : null}

        {fulfillmentMode === 'postal_delivery' ? (
          <>
            <Col xs={24} md={12}>
              <Form.Item name="supportedRegions" label="可配送地區">
                <Input placeholder="例如：澳門、香港、橫琴" />
              </Form.Item>
            </Col>
            <Col xs={24} md={12}>
              <Form.Item name="shippingFeeMode" label="運費策略">
                <Select allowClear options={shippingFeeModeOptions} placeholder="選擇運費由誰承擔" />
              </Form.Item>
            </Col>
          </>
        ) : null}

        {fulfillmentMode === 'voucher_code' ? (
          <>
            <Col xs={24} md={12}>
              <Form.Item name="codePool" label="券碼池標識">
                <Input placeholder="例如：voucher_macau_fire_route_batch_a" />
              </Form.Item>
            </Col>
            <Col xs={24} md={12}>
              <Form.Item name="claimTimeoutHours" label="領取有效期（小時）">
                <InputNumber min={0} style={{ width: '100%' }} />
              </Form.Item>
            </Col>
          </>
        ) : null}

        {fulfillmentMode === 'virtual_issue' ? (
          <>
            <Col xs={24} md={12}>
              <Form.Item name="unlockTarget" label="發放目標">
                <Input placeholder="例如：voice_pack / collectible_bundle / title_unlock" />
              </Form.Item>
            </Col>
            <Col xs={24} md={12}>
              <Form.Item name="claimTimeoutHours" label="領取有效期（小時）">
                <InputNumber min={0} style={{ width: '100%' }} />
              </Form.Item>
            </Col>
          </>
        ) : null}
      </Row>
    </Card>
  );
}

export function GameRewardConfigSection({
  form,
  rewardTypeScope = 'all',
}: GameRewardConfigSectionProps) {
  const rewardType = (Form.useWatch('rewardType', form) as string | undefined) || 'badge';
  const availableRewardTypeOptions = rewardTypeScope === 'honor' ? honorRewardTypeOptions : rewardTypeOptions;

  return (
    <Card size="small" title="遊戲內獎勵規格" style={{ marginBottom: 24 }}>
      <Row gutter={[16, 16]}>
        <Col xs={24} md={8}>
          <Form.Item name="rewardType" label="獎勵類型">
            <Select options={availableRewardTypeOptions} />
          </Form.Item>
        </Col>
        <Col xs={24} md={8}>
          <Form.Item name="rarity" label="稀有度">
            <Select options={rarityOptions} />
          </Form.Item>
        </Col>
        <Col xs={24} md={8}>
          <Form.Item name="maxOwned" label="最多可持有數量">
            <InputNumber min={0} style={{ width: '100%' }} />
          </Form.Item>
        </Col>
        <Col xs={24} md={8}>
          <Form.Item name="stackable" label="可堆疊">
            <Select options={binaryFlagOptions} />
          </Form.Item>
        </Col>
        <Col xs={24} md={8}>
          <Form.Item name="canEquip" label="可裝備">
            <Select options={binaryFlagOptions} />
          </Form.Item>
        </Col>
        <Col xs={24} md={8}>
          <Form.Item name="canConsume" label="可消耗">
            <Select options={binaryFlagOptions} />
          </Form.Item>
        </Col>
      </Row>

      <Alert
        type={['badge', 'title'].includes(rewardType) ? 'success' : 'info'}
        showIcon
        style={{ marginBottom: 16 }}
        message={['badge', 'title'].includes(rewardType) ? '此獎勵會同時出現在「榮譽與稱號」視角' : '此獎勵屬於遊戲內通用資產'}
        description={
          ['badge', 'title'].includes(rewardType)
            ? '徽章與稱號不是另一套資料模型，而是遊戲內獎勵的榮譽子視角。這能保證獲得條件、演出與展示完全一致。'
            : '可用於城市限定貨幣、碎片、語音包、外觀或權限解鎖等遊戲內資產。'
        }
      />

      <Row gutter={[16, 16]}>
        <Col xs={24} md={12}>
          <Form.Item name="configProfileAccent" label="展示主色 / 風格標記">
            <Input placeholder="例如：ember-gold / neon-teal / lisboa-red" />
          </Form.Item>
        </Col>
        <Col xs={24} md={12}>
          <Form.Item name="configDisplayTag" label="展示標籤">
            <Input placeholder="例如：戰火主題 / 限時收藏 / 城市碎片" />
          </Form.Item>
        </Col>
        <Col xs={24} md={12}>
          <Form.Item name="configEconomyCode" label="經濟系統代碼">
            <Input placeholder="例如：macau_coin / fire_route_fragment" />
          </Form.Item>
        </Col>
        <Col xs={24} md={12}>
          <Form.Item name="configGrantMode" label="發放模式">
            <Input placeholder="例如：auto_grant / interaction_unlock / redeem_convert" />
          </Form.Item>
        </Col>
      </Row>
    </Card>
  );
}

export function RewardRuleBuilderSection({ form }: RewardBuilderSectionProps) {
  const watchedGroups = (Form.useWatch('conditionGroups', form) as AdminRewardRuleConditionGroupItem[] | undefined) || [];

  return (
    <Card
      size="small"
      title="條件群組編排"
      extra={<Tag color="blue">支援 all / any / at_least</Tag>}
      style={{ marginBottom: 24 }}
    >
      <Alert
        type="info"
        showIcon
        style={{ marginBottom: 16 }}
        message="先用表單組裝條件，只有無法表達的特殊情況才打開進階 JSON。"
        description="每個群組代表一層邏輯，群組內部可再設多個條件。條件會在治理中心、室內互動與獎勵主體中共用。"
      />

      <Form.List name="conditionGroups">
        {(groupFields, { add, remove }) => (
          <Space direction="vertical" size="middle" style={{ width: '100%' }}>
            {groupFields.map((groupField, index) => {
              const groupValue = watchedGroups[groupField.name] || buildDefaultGroup();
              return (
                <Card
                  key={groupField.key}
                  size="small"
                  title={`條件群組 ${index + 1}`}
                  extra={
                    <Space>
                      <Tag color="purple">{summarizeGroup(groupValue)}</Tag>
                      <Button type="text" danger icon={<DeleteOutlined />} onClick={() => remove(groupField.name)}>
                        移除群組
                      </Button>
                    </Space>
                  }
                >
                  <Row gutter={[16, 16]}>
                    <Col xs={24} md={8}>
                      <Form.Item
                        name={[groupField.name, 'groupCode']}
                        label="群組代碼"
                        rules={[{ required: true, message: '請填寫群組代碼' }]}
                      >
                        <Input placeholder={`group_${index + 1}`} />
                      </Form.Item>
                    </Col>
                    <Col xs={24} md={8}>
                      <Form.Item name={[groupField.name, 'operatorType']} label="成立方式">
                        <Select options={groupModeOptions} />
                      </Form.Item>
                    </Col>
                    <Col xs={24} md={8}>
                      <Form.Item name={[groupField.name, 'minimumMatchCount']} label="至少命中數">
                        <InputNumber
                          min={1}
                          disabled={(groupValue.operatorType || 'all') !== 'at_least'}
                          style={{ width: '100%' }}
                        />
                      </Form.Item>
                    </Col>
                    <Col xs={24}>
                      <Form.Item name={[groupField.name, 'summaryText']} label="群組摘要">
                        <Input placeholder="例如：完成主線探索或在限定時段內達到指定進度" />
                      </Form.Item>
                    </Col>
                  </Row>

                  <Form.List name={[groupField.name, 'conditions']}>
                    {(conditionFields, conditionOps) => (
                      <Space direction="vertical" size="middle" style={{ width: '100%' }}>
                        {conditionFields.map((conditionField, conditionIndex) => (
                          <Card
                            key={conditionField.key}
                            size="small"
                            title={`條件 ${conditionIndex + 1}`}
                            extra={
                              <Button
                                type="text"
                                danger
                                icon={<DeleteOutlined />}
                                onClick={() => conditionOps.remove(conditionField.name)}
                              >
                                移除條件
                              </Button>
                            }
                          >
                            <Row gutter={[16, 16]}>
                              <Col xs={24} md={6}>
                                <Form.Item name={[conditionField.name, 'conditionType']} label="條件分類">
                                  <Select options={conditionTypeOptions} />
                                </Form.Item>
                              </Col>
                              <Col xs={24} md={6}>
                                <Form.Item name={[conditionField.name, 'metricType']} label="指標類型">
                                  <Select options={metricTypeOptions} />
                                </Form.Item>
                              </Col>
                              <Col xs={24} md={6}>
                                <Form.Item name={[conditionField.name, 'operatorType']} label="比較方式">
                                  <Select options={comparatorOptions} />
                                </Form.Item>
                              </Col>
                              <Col xs={24} md={3}>
                                <Form.Item name={[conditionField.name, 'comparatorValue']} label="目標值">
                                  <Input placeholder="80 / badge_x / 2026-01-01" />
                                </Form.Item>
                              </Col>
                              <Col xs={24} md={3}>
                                <Form.Item name={[conditionField.name, 'comparatorUnit']} label="單位">
                                  <Input placeholder="percent / times / code" />
                                </Form.Item>
                              </Col>
                              <Col xs={24}>
                                <Form.Item name={[conditionField.name, 'summaryText']} label="條件說明">
                                  <Input placeholder="例如：澳門半島探索度大於等於 80%" />
                                </Form.Item>
                              </Col>
                            </Row>

                            <Collapse
                              items={[
                                {
                                  key: 'advanced',
                                  label: '進階 JSON',
                                  children: (
                                    <Form.Item name={[conditionField.name, 'configJson']} style={{ marginBottom: 0 }}>
                                      <Input.TextArea
                                        rows={4}
                                        placeholder='只有結構化欄位不足時才填，例如：{"weekday":["sat","sun"]}'
                                      />
                                    </Form.Item>
                                  ),
                                },
                              ]}
                            />
                          </Card>
                        ))}

                        <Button
                          type="dashed"
                          icon={<PlusOutlined />}
                          onClick={() => conditionOps.add(buildDefaultCondition())}
                        >
                          新增條件
                        </Button>
                      </Space>
                    )}
                  </Form.List>

                  <Collapse
                    style={{ marginTop: 16 }}
                    items={[
                      {
                        key: 'advanced-group',
                        label: '群組進階 JSON',
                        children: (
                          <Form.Item name={[groupField.name, 'advancedConfigJson']} style={{ marginBottom: 0 }}>
                            <Input.TextArea
                              rows={4}
                              placeholder='例如：{"matchWindow":"session","resolvePolicy":"first_match"}'
                            />
                          </Form.Item>
                        ),
                      },
                    ]}
                  />
                </Card>
              );
            })}

            <Button type="dashed" icon={<PlusOutlined />} onClick={() => add(buildDefaultGroup())}>
              新增條件群組
            </Button>
          </Space>
        )}
      </Form.List>
    </Card>
  );
}

export function RewardPresentationBuilderSection({ form }: RewardBuilderSectionProps) {
  const steps = (Form.useWatch('steps', form) as AdminRewardPresentationItem['steps'] | undefined) || [];

  return (
    <Card
      size="small"
      title="獲得演出編排"
      extra={<Tag color="purple">全屏 / 音效 / 語音皆可配置</Tag>}
      style={{ marginBottom: 24 }}
    >
      <Row gutter={[16, 16]}>
        <Col xs={24} md={8}>
          <Form.Item name="presentationType" label="主演出類型">
            <Select options={presentationTypeOptions} />
          </Form.Item>
        </Col>
        <Col xs={24} md={4}>
          <Form.Item name="firstTimeOnly" label="首次限定">
            <Select options={binaryFlagOptions} />
          </Form.Item>
        </Col>
        <Col xs={24} md={4}>
          <Form.Item name="skippable" label="可跳過">
            <Select options={binaryFlagOptions} />
          </Form.Item>
        </Col>
        <Col xs={24} md={4}>
          <Form.Item name="minimumDisplayMs" label="最短播放（ms）">
            <InputNumber min={0} step={100} style={{ width: '100%' }} />
          </Form.Item>
        </Col>
        <Col xs={24} md={4}>
          <Form.Item name="priorityWeight" label="優先級">
            <InputNumber min={0} style={{ width: '100%' }} />
          </Form.Item>
        </Col>
        <Col xs={24} md={12}>
          <Form.Item name="interruptPolicy" label="中斷策略">
            <Select options={interruptPolicyOptions} />
          </Form.Item>
        </Col>
        <Col xs={24} md={12}>
          <Form.Item name="queuePolicy" label="佇列策略">
            <Select options={queuePolicyOptions} />
          </Form.Item>
        </Col>
        <Col xs={24}>
          <Form.Item name="summaryText" label="演出摘要">
            <Input placeholder="例如：首次獲得稱號時播放全屏影片，再接語音播報與獎勵卡片。" />
          </Form.Item>
        </Col>
      </Row>

      <Row gutter={[16, 16]} style={{ marginBottom: 8 }}>
        <Col xs={24} lg={8}>
          <MediaAssetPickerField
            name="coverAssetId"
            label="主視覺 / 演出封面"
            assetKind="image"
            valueMode="asset-id"
          />
        </Col>
        <Col xs={24} lg={8}>
          <MediaAssetPickerField
            name="voiceOverAssetId"
            label="預設語音"
            assetKind="audio"
            valueMode="asset-id"
          />
        </Col>
        <Col xs={24} lg={8}>
          <MediaAssetPickerField
            name="sfxAssetId"
            label="預設音效"
            assetKind="audio"
            valueMode="asset-id"
          />
        </Col>
      </Row>

      <Collapse
        style={{ marginBottom: 16 }}
        items={[
          {
            key: 'presentation-advanced',
            label: '演出進階 JSON',
            children: (
              <Form.Item name="configJson" style={{ marginBottom: 0 }}>
                <Input.TextArea
                  rows={4}
                  placeholder='例如：{"fullscreenMask":"embers","subtitleMode":"cinematic"}'
                />
              </Form.Item>
            ),
          },
        ]}
      />

      <Form.List name="steps">
        {(stepFields, { add, remove }) => (
          <Space direction="vertical" size="middle" style={{ width: '100%' }}>
            {stepFields.map((stepField, index) => (
              <Card
                key={stepField.key}
                size="small"
                title={`演出步驟 ${index + 1}`}
                extra={
                  <Space>
                    <Tag>{steps[index]?.stepType || 'popup_card'}</Tag>
                    <Button type="text" danger icon={<DeleteOutlined />} onClick={() => remove(stepField.name)}>
                      移除步驟
                    </Button>
                  </Space>
                }
              >
                <Row gutter={[16, 16]}>
                  <Col xs={24} md={6}>
                    <Form.Item name={[stepField.name, 'stepType']} label="步驟類型">
                      <Select options={stepTypeOptions} />
                    </Form.Item>
                  </Col>
                  <Col xs={24} md={6}>
                    <Form.Item name={[stepField.name, 'stepCode']} label="步驟代碼">
                      <Input placeholder={`step_${index + 1}`} />
                    </Form.Item>
                  </Col>
                  <Col xs={24} md={8}>
                    <Form.Item name={[stepField.name, 'titleText']} label="步驟標題">
                      <Input placeholder="例如：濠江通史已解鎖" />
                    </Form.Item>
                  </Col>
                  <Col xs={24} md={4}>
                    <Form.Item name={[stepField.name, 'durationMs']} label="播放時長（ms）">
                      <InputNumber min={0} step={100} style={{ width: '100%' }} />
                    </Form.Item>
                  </Col>
                  <Col xs={24} md={4}>
                    <Form.Item name={[stepField.name, 'skippableOverride']} label="本步驟可跳過">
                      <Select allowClear options={binaryFlagOptions} />
                    </Form.Item>
                  </Col>
                  <Col xs={24} md={8}>
                    <MediaAssetPickerField
                      name={[stepField.name, 'assetId']}
                      label="主媒體"
                      valueMode="asset-id"
                    />
                  </Col>
                  <Col xs={24} md={8}>
                    <MediaAssetPickerField
                      name={[stepField.name, 'voiceOverAssetId']}
                      label="步驟語音"
                      assetKind="audio"
                      valueMode="asset-id"
                    />
                  </Col>
                  <Col xs={24} md={8}>
                    <MediaAssetPickerField
                      name={[stepField.name, 'triggerSfxAssetId']}
                      label="步驟音效"
                      assetKind="audio"
                      valueMode="asset-id"
                    />
                  </Col>
                </Row>

                <Collapse
                  items={[
                    {
                      key: 'step-advanced',
                      label: '步驟疊層 / 進階 JSON',
                      children: (
                        <Form.Item name={[stepField.name, 'overlayConfigJson']} style={{ marginBottom: 0 }}>
                          <Input.TextArea
                            rows={4}
                            placeholder='例如：{"overlay":"title_burst","safeArea":"top"}'
                          />
                        </Form.Item>
                      ),
                    },
                  ]}
                />
              </Card>
            ))}

            <Button
              type="dashed"
              icon={<PlusOutlined />}
              onClick={() =>
                add({
                  stepType: 'popup_card',
                  stepCode: `step_${stepFields.length + 1}`,
                  titleText: '獲得獎勵',
                  durationMs: 1800,
                  skippableOverride: 1,
                  sortOrder: stepFields.length,
                })
              }
            >
              新增演出步驟
            </Button>
          </Space>
        )}
      </Form.List>
    </Card>
  );
}

export function SplitRewardStats({ summary }: SplitRewardStatsProps) {
  const items = [
    { label: '兌換獎勵物品', value: summary?.redeemablePrizeCount || 0 },
    { label: '遊戲內獎勵', value: summary?.gameRewardCount || 0 },
    { label: '榮譽與稱號', value: summary?.honorCount || 0 },
    { label: '共享規則', value: summary?.ruleCount || 0 },
    { label: '獲得演出', value: summary?.presentationCount || 0 },
    { label: '室內互動同步', value: summary?.linkedIndoorBehaviorCount || 0 },
  ];

  return (
    <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
      {items.map((item) => (
        <Col xs={12} md={8} xl={4} key={item.label}>
          <Card size="small">
            <Statistic title={item.label} value={item.value} />
          </Card>
        </Col>
      ))}
    </Row>
  );
}

export {
  CollectionBindingSection,
  CollectionLocalizedCoreFields,
  CollectionMediaSection,
};
