import React, { useEffect, useMemo, useState } from 'react';
import { PageContainer } from '@ant-design/pro-components';
import { useNavigate, useParams, useSearchParams } from 'react-router-dom';
import {
  Alert,
  App as AntdApp,
  Badge,
  Button,
  Card,
  Col,
  Collapse,
  Divider,
  Empty,
  Form,
  Input,
  InputNumber,
  Popconfirm,
  Row,
  Select,
  Space,
  Spin,
  Switch,
  Tag,
  Timeline,
  Typography,
} from 'antd';
import {
  DeleteOutlined,
  PlusOutlined,
  ReloadOutlined,
  SaveOutlined,
} from '@ant-design/icons';
import {
  createAdminStorylineChapterOverrideStep,
  deleteAdminStorylineChapterOverrideStep,
  getAdminRewardRules,
  getAdminStorylineModeWorkbench,
  getAdminStorylineRuntimePreview,
  getAdminStorylines,
  updateAdminStorylineChapterAnchor,
  updateAdminStorylineChapterOverridePolicy,
  updateAdminStorylineChapterOverrideStep,
  updateAdminStorylineModeConfig,
} from '../../services/api';
import type {
  AdminRewardRuleItem,
  AdminStorylineListItem,
  AdminStorylineModeAnchorPayload,
  AdminStorylineModeChapterRuntime,
  AdminStorylineModeConfigPayload,
  AdminStorylineModeOverridePolicyPayload,
  AdminStorylineModeOverrideRule,
  AdminStorylineModeOverrideStepPayload,
  AdminStorylineModeRuntimePreview,
  AdminStorylineModeSnapshot,
  AdminStorylineModeStepSummary,
} from '../../types/admin';
import SpatialAssetPickerField from '../../components/spatial/SpatialAssetPickerField';
import { focusFirstInvalidField } from '../../utils/formErrorFeedback';
import './index.scss';

const { Paragraph, Text, Title } = Typography;

const MODE_FORM_NAME = 'storylineModeConfigForm';
const ANCHOR_FORM_NAME = 'storylineModeAnchorForm';
const POLICY_FORM_NAME = 'storylineModePolicyForm';
const OVERRIDE_FORM_NAME = 'storylineModeOverrideForm';

const anchorTypeOptions = [
  { label: 'POI 景點', value: 'poi' },
  { label: '室內建築', value: 'indoor_building' },
  { label: '室內樓層', value: 'indoor_floor' },
  { label: '室內標記 / 疊加物', value: 'indoor_node' },
  { label: '任務點', value: 'task' },
  { label: '地圖疊加物', value: 'overlay' },
  { label: '手動錨點', value: 'manual' },
];

const overrideModeOptions = [
  { label: '繼承', value: 'inherit' },
  { label: '停用', value: 'disable' },
  { label: '替換', value: 'replace' },
  { label: '追加', value: 'append' },
];

const branchSourceOptions = [
  { label: '附近 POI', value: 'nearby_poi' },
  { label: '同主題 POI', value: 'same_theme_poi' },
  { label: '同子地圖 POI', value: 'same_sub_map_poi' },
  { label: '營運手選', value: 'manual' },
];

const branchInsertOptions = [
  { label: '章節前', value: 'before_chapter' },
  { label: '章節後', value: 'after_chapter' },
  { label: '兩章節之間', value: 'between_chapters' },
];

const statusOptions = [
  { label: '編輯中', value: 'draft' },
  { label: '已發佈', value: 'published' },
  { label: '已封存', value: 'archived' },
];

const stepTypeOptions = [
  { label: '全屏媒體', value: 'fullscreen_media' },
  { label: '故事專屬內容', value: 'story_content' },
  { label: '主線疊加物', value: 'mainline_overlay' },
  { label: '支線拾取物', value: 'side_pickup' },
  { label: '隱藏挑戰', value: 'hidden_challenge' },
  { label: '獎勵與稱號', value: 'reward_title' },
  { label: '完成效果', value: 'completion_effect' },
  { label: '自定義', value: 'custom' },
];

const triggerTypeOptions = [
  { label: '故事模式進入', value: 'story_mode_enter' },
  { label: '到達錨點範圍', value: 'proximity' },
  { label: '點擊互動', value: 'tap' },
  { label: '完成前置步驟', value: 'after_step' },
  { label: '完成收集', value: 'pickup_complete' },
  { label: '完成挑戰', value: 'challenge_complete' },
  { label: '閱讀 / 播放完成', value: 'content_complete' },
];

const effectPresetOptions = [
  { label: '播放全屏媒體', value: 'fullscreen_media' },
  { label: '顯示故事內容', value: 'story_content' },
  { label: '生成主線疊加物', value: 'spawn_mainline_overlays' },
  { label: '生成支線拾取物', value: 'spawn_side_pickups' },
  { label: '啟動隱藏挑戰', value: 'start_hidden_challenge' },
  { label: '發放獎勵與稱號', value: 'grant_rewards_titles' },
  { label: '解鎖下一章', value: 'unlock_next_chapter' },
];

const explorationWeightOptions = [
  { label: '極少量', value: 'tiny' },
  { label: '少量', value: 'small' },
  { label: '中量', value: 'medium' },
  { label: '大量', value: 'large' },
  { label: '核心', value: 'core' },
];

const quickOverridePresets: Array<{
  key: string;
  label: string;
  description: string;
  values: Partial<OverrideFormValues>;
}> = [
  {
    key: 'story_media_replace',
    label: '替換抵達動畫',
    description: '把 POI 預設抵達動畫替換為故事線專屬沉浸動畫。',
    values: {
      targetStepCode: 'arrival_intro_media',
      overrideMode: 'replace',
      stepCode: 'story_chapter_arrival_media',
      stepType: 'fullscreen_media',
      stepNameZht: '故事專屬抵達動畫',
      stepNameZh: '故事專屬抵達動畫',
      triggerType: 'proximity',
      effectPreset: 'fullscreen_media',
      explorationWeightLevel: 'small',
      requiredForCompletion: false,
      status: 'published',
    },
  },
  {
    key: 'mainline_overlays',
    label: '追加主線疊加物',
    description: '在章節開始後生成主線必做疊加物與點擊互動。',
    values: {
      targetStepCode: 'arrival_intro_media',
      overrideMode: 'append',
      stepCode: 'story_chapter_mainline_overlays',
      stepType: 'mainline_overlay',
      stepNameZht: '主線疊加物',
      stepNameZh: '主線疊加物',
      triggerType: 'after_step',
      effectPreset: 'spawn_mainline_overlays',
      pickupCodes: ['main_overlay_01', 'main_overlay_02', 'main_overlay_03'],
      explorationWeightLevel: 'medium',
      requiredForCompletion: true,
      status: 'published',
    },
  },
  {
    key: 'side_pickups',
    label: '追加支線拾取物',
    description: '配置章節內可拾取信物、稀有線索與秘寶碎片。',
    values: {
      targetStepCode: 'story_chapter_mainline_overlays',
      overrideMode: 'append',
      stepCode: 'story_chapter_side_pickups',
      stepType: 'side_pickup',
      stepNameZht: '支線拾取物',
      stepNameZh: '支線拾取物',
      triggerType: 'after_step',
      effectPreset: 'spawn_side_pickups',
      pickupCodes: ['common_token', 'rare_clue', 'mirror_fragment'],
      explorationWeightLevel: 'large',
      requiredForCompletion: false,
      status: 'published',
    },
  },
  {
    key: 'hidden_challenge',
    label: '追加隱藏挑戰',
    description: '全收集或停留後開啟問答、拼圖、路線還原等挑戰。',
    values: {
      targetStepCode: 'story_chapter_side_pickups',
      overrideMode: 'append',
      stepCode: 'story_chapter_hidden_challenge',
      stepType: 'hidden_challenge',
      stepNameZht: '隱藏挑戰',
      stepNameZh: '隱藏挑戰',
      triggerType: 'pickup_complete',
      effectPreset: 'start_hidden_challenge',
      challengeCode: 'chapter_hidden_challenge',
      explorationWeightLevel: 'core',
      requiredForCompletion: false,
      status: 'published',
    },
  },
  {
    key: 'reward_titles',
    label: '追加獎勵與稱號',
    description: '章節完成後發放徽章、稱號、遊戲內獎勵與完成效果。',
    values: {
      targetStepCode: 'story_chapter_hidden_challenge',
      overrideMode: 'append',
      stepCode: 'story_chapter_reward_titles',
      stepType: 'reward_title',
      stepNameZht: '獎勵與稱號',
      stepNameZh: '獎勵與稱號',
      triggerType: 'challenge_complete',
      effectPreset: 'grant_rewards_titles',
      explorationWeightLevel: 'core',
      requiredForCompletion: true,
      status: 'published',
    },
  },
];

interface OverrideFormValues {
  targetStepCode?: string;
  overrideMode?: string;
  replacementStepId?: number | null;
  stepId?: number | null;
  stepCode?: string;
  stepType?: string;
  stepNameZh?: string;
  stepNameZht?: string;
  descriptionZh?: string;
  descriptionZht?: string;
  triggerType?: string;
  effectPreset?: string;
  mediaAssetId?: number | null;
  fullScreenMediaAssetId?: number | null;
  audioAssetId?: number | null;
  rewardRuleIds?: number[];
  pickupCodes?: string[];
  challengeCode?: string;
  explorationWeightLevel?: string;
  requiredForCompletion?: boolean;
  inheritKey?: string;
  status?: string;
  sortOrder?: number;
  advancedJsonEnabled?: boolean;
  advancedTriggerConfigJson?: string;
  advancedConditionConfigJson?: string;
  advancedEffectConfigJson?: string;
  advancedOverrideConfigJson?: string;
}

function pickStorylineName(storyline?: Partial<AdminStorylineListItem> | AdminStorylineModeSnapshot['storyline'] | null) {
  if (!storyline) {
    return '';
  }
  return storyline.nameZht || storyline.nameZh || storyline.nameEn || storyline.namePt || storyline.code || '';
}

function pickChapterTitle(runtime?: AdminStorylineModeChapterRuntime | null) {
  const chapter = runtime?.chapter;
  if (!chapter) {
    return '';
  }
  return chapter.titleZht || chapter.titleZh || chapter.titleEn || chapter.titlePt || `第 ${chapter.chapterOrder} 章`;
}

function pickStepName(step?: AdminStorylineModeStepSummary | null) {
  if (!step) {
    return '';
  }
  return step.stepNameZht || step.stepNameZh || step.stepCode || `步驟 #${step.id}`;
}

function renderStatus(status?: string) {
  if (status === 'published') {
    return <Tag color="green">已發佈</Tag>;
  }
  if (status === 'archived') {
    return <Tag>已封存</Tag>;
  }
  return <Tag color="gold">編輯中</Tag>;
}

function renderOverrideBadge(mode?: string) {
  const normalized = mode || 'inherit';
  const label = overrideModeOptions.find((item) => item.value === normalized)?.label || normalized;
  const colorMap: Record<string, string> = {
    inherit: 'blue',
    disable: 'red',
    replace: 'purple',
    append: 'green',
  };
  return <Tag color={colorMap[normalized] || 'default'}>{label}</Tag>;
}

function parseConfigJson(value?: string) {
  if (!value) {
    return {};
  }
  try {
    const parsed = JSON.parse(value);
    return parsed && typeof parsed === 'object' && !Array.isArray(parsed) ? parsed : {};
  } catch {
    return {};
  }
}

function parseRewardRuleIds(value?: string) {
  if (!value) {
    return [];
  }
  try {
    const parsed = JSON.parse(value);
    return Array.isArray(parsed) ? parsed.filter((item) => typeof item === 'number') : [];
  } catch {
    return [];
  }
}

function modeConfigDefaults(snapshot?: AdminStorylineModeSnapshot | null): AdminStorylineModeConfigPayload {
  const route = snapshot?.routeStrategy || {};
  return {
    hideUnrelatedContent: route.hideUnrelatedContent ?? true,
    nearbyRevealEnabled: route.nearbyRevealEnabled ?? true,
    nearbyRevealRadiusMeters: route.nearbyRevealRadiusMeters ?? route.nearbyRevealMeters ?? 120,
    currentRouteHighlight: route.currentRouteHighlight || route.currentRouteStyle || 'copper_flame',
    inactiveRouteStyle: route.inactiveRouteStyle || 'muted_ink',
    clearTemporaryProgressOnExit: route.clearTemporaryProgressOnExit ?? route.exitResetsSessionProgress ?? true,
    preservePermanentEvents: route.preservePermanentEvents ?? true,
    branchSourceType: route.branchSourceType || 'nearby_poi',
    branchInsertPosition: route.branchInsertPosition || 'between_chapters',
    branchSkippable: route.branchSkippable ?? true,
    branchAffectsStoryProgress: route.branchAffectsStoryProgress ?? false,
    manualBranchPoiIds: route.manualBranchPoiIds || [],
    advancedJsonEnabled: false,
    advancedStoryModeConfigJson: JSON.stringify(route || { schemaVersion: 1 }, null, 2),
  };
}

function anchorDefaults(runtime?: AdminStorylineModeChapterRuntime | null): AdminStorylineModeAnchorPayload {
  return {
    anchorType: runtime?.anchor?.anchorType || runtime?.chapter?.anchorType || 'manual',
    anchorTargetId: runtime?.anchor?.anchorTargetId ?? runtime?.chapter?.anchorTargetId ?? null,
    anchorTargetCode: runtime?.anchor?.anchorTargetCode || runtime?.chapter?.anchorTargetCode,
    anchorLabelOverride: runtime?.anchor?.anchorLabel || runtime?.chapter?.anchorTargetLabel,
    routeOrder: runtime?.anchor?.routeOrder || runtime?.chapter?.chapterOrder || 1,
    routeSegmentStyle: runtime?.anchor?.routeSegmentStyle || 'current_highlight',
  };
}

function policyDefaults(runtime?: AdminStorylineModeChapterRuntime | null): AdminStorylineModeOverridePolicyPayload {
  const parsed = parseConfigJson(runtime?.chapter?.overridePolicyJson);
  return {
    inheritDefaultFlow: parsed.inheritDefaultFlow ?? true,
    disableDefaultArrivalMedia: parsed.disableDefaultArrivalMedia ?? false,
    appendStorySpecificRewards: parsed.appendStorySpecificRewards ?? true,
    advancedJsonEnabled: false,
    advancedOverridePolicyJson: runtime?.chapter?.overridePolicyJson || JSON.stringify({ schemaVersion: 1 }, null, 2),
  };
}

function overrideToFormValues(override?: AdminStorylineModeOverrideRule | null): OverrideFormValues {
  if (!override) {
    return {
      overrideMode: 'append',
      stepType: 'story_content',
      triggerType: 'story_mode_enter',
      effectPreset: 'story_content',
      explorationWeightLevel: 'small',
      requiredForCompletion: false,
      status: 'draft',
      sortOrder: 10,
      advancedJsonEnabled: false,
    };
  }
  const replacement = override.replacementStep;
  const config = parseConfigJson(override.overrideConfigJson);
  return {
    targetStepCode: override.targetStepCode,
    overrideMode: override.overrideMode || 'inherit',
    replacementStepId: override.replacementStepId,
    stepId: replacement?.id,
    stepCode: replacement?.stepCode,
    stepType: replacement?.stepType,
    stepNameZh: replacement?.stepNameZh,
    stepNameZht: replacement?.stepNameZht || replacement?.stepNameZh,
    triggerType: replacement?.triggerType,
    effectPreset: config.effectPreset,
    mediaAssetId: replacement?.mediaAssetId,
    fullScreenMediaAssetId: config.fullScreenMediaAssetId,
    audioAssetId: config.audioAssetId,
    rewardRuleIds: parseRewardRuleIds(replacement?.rewardRuleIdsJson),
    pickupCodes: config.pickupCodes || [],
    challengeCode: config.challengeCode,
    explorationWeightLevel: replacement?.explorationWeightLevel || config.explorationWeightLevel || 'small',
    requiredForCompletion: replacement?.requiredForCompletion,
    inheritKey: replacement?.inheritKey,
    status: override.status || 'draft',
    sortOrder: override.sortOrder || replacement?.sortOrder || 10,
    advancedJsonEnabled: false,
    advancedOverrideConfigJson: override.overrideConfigJson,
  };
}

function buildOverridePayload(values: OverrideFormValues): AdminStorylineModeOverrideStepPayload {
  const overrideConfig = {
    schemaVersion: 1,
    effectPreset: values.effectPreset,
    fullScreenMediaAssetId: values.fullScreenMediaAssetId,
    audioAssetId: values.audioAssetId,
    pickupCodes: values.pickupCodes || [],
    challengeCode: values.challengeCode,
    explorationWeightLevel: values.explorationWeightLevel,
  };
  const triggerConfig = {
    schemaVersion: 1,
    triggerType: values.triggerType,
  };
  const conditionConfig = {
    schemaVersion: 1,
    requiredForCompletion: Boolean(values.requiredForCompletion),
  };
  const effectConfig = {
    schemaVersion: 1,
    effectPreset: values.effectPreset,
    fullScreenMediaAssetId: values.fullScreenMediaAssetId,
    audioAssetId: values.audioAssetId,
    pickupCodes: values.pickupCodes || [],
    challengeCode: values.challengeCode,
  };

  const needsReplacementDraft = values.overrideMode === 'replace' || values.overrideMode === 'append';
  return {
    targetStepCode: values.targetStepCode,
    overrideMode: values.overrideMode || 'inherit',
    replacementStepId: values.replacementStepId || undefined,
    replacementStepDraft: needsReplacementDraft
      ? {
          stepId: values.stepId || undefined,
          stepCode: values.stepCode,
          stepType: values.stepType || 'story_content',
          stepNameZh: values.stepNameZh || values.stepNameZht || '故事專屬步驟',
          stepNameZht: values.stepNameZht || values.stepNameZh || '故事專屬步驟',
          descriptionZh: values.descriptionZh || values.descriptionZht,
          descriptionZht: values.descriptionZht || values.descriptionZh,
          triggerType: values.triggerType || 'story_mode_enter',
          triggerConfigJson: values.advancedJsonEnabled ? values.advancedTriggerConfigJson : JSON.stringify(triggerConfig),
          conditionConfigJson: values.advancedJsonEnabled ? values.advancedConditionConfigJson : JSON.stringify(conditionConfig),
          effectConfigJson: values.advancedJsonEnabled ? values.advancedEffectConfigJson : JSON.stringify(effectConfig),
          mediaAssetId: values.mediaAssetId,
          rewardRuleIds: values.rewardRuleIds || [],
          explorationWeightLevel: values.explorationWeightLevel || 'small',
          requiredForCompletion: Boolean(values.requiredForCompletion),
          inheritKey: values.inheritKey,
          status: values.status || 'draft',
          sortOrder: values.sortOrder || 10,
        }
      : undefined,
    effectPreset: values.effectPreset,
    mediaAssetId: values.mediaAssetId,
    rewardRuleIds: values.rewardRuleIds || [],
    pickupCodes: values.pickupCodes || [],
    challengeCode: values.challengeCode,
    explorationWeightLevel: values.explorationWeightLevel || 'small',
    sortOrder: values.sortOrder || 10,
    status: values.status || 'draft',
    advancedJsonEnabled: Boolean(values.advancedJsonEnabled),
    advancedOverrideConfigJson: values.advancedJsonEnabled
      ? values.advancedOverrideConfigJson
      : JSON.stringify(overrideConfig),
  };
}

const StorylineModeWorkbench: React.FC = () => {
  const params = useParams();
  const navigate = useNavigate();
  const [searchParams, setSearchParams] = useSearchParams();
  const { message } = AntdApp.useApp();
  const [modeForm] = Form.useForm<AdminStorylineModeConfigPayload>();
  const [anchorForm] = Form.useForm<AdminStorylineModeAnchorPayload>();
  const [policyForm] = Form.useForm<AdminStorylineModeOverridePolicyPayload>();
  const [overrideForm] = Form.useForm<OverrideFormValues>();
  const [storylines, setStorylines] = useState<AdminStorylineListItem[]>([]);
  const [rewardRules, setRewardRules] = useState<AdminRewardRuleItem[]>([]);
  const [selectedStorylineId, setSelectedStorylineId] = useState<number | undefined>(
    params.storylineId ? Number(params.storylineId) : undefined,
  );
  const [snapshot, setSnapshot] = useState<AdminStorylineModeSnapshot | null>(null);
  const [runtimePreview, setRuntimePreview] = useState<AdminStorylineModeRuntimePreview | null>(null);
  const [selectedChapterId, setSelectedChapterId] = useState<number | null>(
    searchParams.get('chapterId') ? Number(searchParams.get('chapterId')) : null,
  );
  const [selectedOverrideId, setSelectedOverrideId] = useState<number | null>(null);
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);

  const selectedRuntime = useMemo(
    () => snapshot?.chapterRuntimes?.find((runtime) => runtime.chapter.id === selectedChapterId) || null,
    [selectedChapterId, snapshot?.chapterRuntimes],
  );

  const selectedOverride = useMemo(
    () => selectedRuntime?.overrides?.find((override) => override.id === selectedOverrideId) || null,
    [selectedOverrideId, selectedRuntime?.overrides],
  );

  const advancedModeJsonEnabled = Form.useWatch('advancedJsonEnabled', modeForm);
  const advancedPolicyJsonEnabled = Form.useWatch('advancedJsonEnabled', policyForm);
  const advancedOverrideJsonEnabled = Form.useWatch('advancedJsonEnabled', overrideForm);
  const overrideMode = Form.useWatch('overrideMode', overrideForm);

  const rewardRuleOptions = useMemo(
    () =>
      rewardRules.map((rule) => ({
        label: `${rule.nameZht || rule.nameZh || rule.code} · #${rule.id}`,
        value: rule.id,
      })),
    [rewardRules],
  );

  const inheritedStepOptions = useMemo(
    () =>
      [
        ...(selectedRuntime?.inheritedFlow?.steps || []),
        ...(selectedRuntime?.chapterFlow?.steps || []),
        ...(selectedRuntime?.compiledStepPreview || []),
      ]
        .filter((step, index, array) => {
          const key = step.stepCode || `step-${step.id}`;
          return key && array.findIndex((item) => (item.stepCode || `step-${item.id}`) === key) === index;
        })
        .map((step) => ({
          label: `${pickStepName(step)} (${step.stepCode || `step-${step.id}`})`,
          value: step.stepCode || `step-${step.id}`,
        })),
    [selectedRuntime],
  );

  const loadStorylines = async () => {
    const response = await getAdminStorylines({ pageNum: 1, pageSize: 300 });
    if (response.success && response.data) {
      setStorylines(response.data.list || []);
    }
  };

  const loadRewardRules = async () => {
    const response = await getAdminRewardRules({ pageNum: 1, pageSize: 300 });
    if (response.success && response.data) {
      setRewardRules(response.data.list || []);
    }
  };

  const loadSnapshot = async (storylineId: number, preferredChapterId?: number | null) => {
    setLoading(true);
    try {
      const response = await getAdminStorylineModeWorkbench(storylineId);
      if (!response.success || !response.data) {
        throw new Error(response.message || '無法載入故事路線與章節覆寫工作台');
      }
      setSnapshot(response.data);
      modeForm.setFieldsValue(modeConfigDefaults(response.data));
      const targetChapterId = preferredChapterId || selectedChapterId || response.data.chapterRuntimes?.[0]?.chapter.id || null;
      setSelectedChapterId(targetChapterId);
      const previewResponse = await getAdminStorylineRuntimePreview(storylineId);
      if (previewResponse.success && previewResponse.data) {
        setRuntimePreview(previewResponse.data);
      }
    } catch (error) {
      message.error(error instanceof Error ? error.message : '工作台載入失敗');
      setSnapshot(null);
      setRuntimePreview(null);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void loadStorylines();
    void loadRewardRules();
  }, []);

  useEffect(() => {
    const routeStorylineId = params.storylineId ? Number(params.storylineId) : undefined;
    if (routeStorylineId && routeStorylineId !== selectedStorylineId) {
      setSelectedStorylineId(routeStorylineId);
    }
  }, [params.storylineId]);

  useEffect(() => {
    if (selectedStorylineId) {
      void loadSnapshot(selectedStorylineId, searchParams.get('chapterId') ? Number(searchParams.get('chapterId')) : null);
    } else {
      setSnapshot(null);
      setRuntimePreview(null);
    }
  }, [selectedStorylineId]);

  useEffect(() => {
    anchorForm.setFieldsValue(anchorDefaults(selectedRuntime));
    policyForm.setFieldsValue(policyDefaults(selectedRuntime));
    const firstOverride = selectedRuntime?.overrides?.[0] || null;
    setSelectedOverrideId(firstOverride?.id || null);
    overrideForm.setFieldsValue(overrideToFormValues(firstOverride));
  }, [anchorForm, overrideForm, policyForm, selectedRuntime]);

  useEffect(() => {
    overrideForm.setFieldsValue(overrideToFormValues(selectedOverride));
  }, [overrideForm, selectedOverride]);

  const handleStorylineChange = (storylineId?: number) => {
    setSelectedStorylineId(storylineId);
    setSelectedChapterId(null);
    setSelectedOverrideId(null);
    if (storylineId) {
      navigate(`/content/storylines/${storylineId}/mode`);
    } else {
      navigate('/content/storyline-mode');
    }
  };

  const handleChapterSelect = (chapterId: number) => {
    setSelectedChapterId(chapterId);
    const next = new URLSearchParams(searchParams);
    next.set('chapterId', String(chapterId));
    setSearchParams(next);
  };

  const reloadCurrent = async () => {
    if (selectedStorylineId) {
      await loadSnapshot(selectedStorylineId, selectedChapterId);
    }
  };

  const handleSaveModeConfig = async () => {
    if (!selectedStorylineId) {
      message.warning('請先選擇故事線');
      return;
    }
    setSaving(true);
    try {
      const values = await modeForm.validateFields();
      const response = await updateAdminStorylineModeConfig(selectedStorylineId, values);
      if (!response.success || !response.data) {
        throw new Error(response.message || '故事模式設定保存失敗');
      }
      message.success('故事模式設定已保存');
      setSnapshot(response.data);
      await reloadCurrent();
    } catch (error) {
      focusFirstInvalidField(modeForm, MODE_FORM_NAME, error);
      if (error instanceof Error) {
        message.error(error.message);
      }
    } finally {
      setSaving(false);
    }
  };

  const handleSaveAnchor = async () => {
    if (!selectedStorylineId || !selectedRuntime) {
      message.warning('請先選擇章節');
      return;
    }
    setSaving(true);
    try {
      const values = await anchorForm.validateFields();
      const response = await updateAdminStorylineChapterAnchor(
        selectedStorylineId,
        selectedRuntime.chapter.id,
        values,
      );
      if (!response.success || !response.data) {
        throw new Error(response.message || '章節錨點保存失敗');
      }
      message.success('章節錨點已保存');
      setSnapshot(response.data);
      await reloadCurrent();
    } catch (error) {
      focusFirstInvalidField(anchorForm, ANCHOR_FORM_NAME, error);
      if (error instanceof Error) {
        message.error(error.message);
      }
    } finally {
      setSaving(false);
    }
  };

  const handleSavePolicy = async () => {
    if (!selectedStorylineId || !selectedRuntime) {
      message.warning('請先選擇章節');
      return;
    }
    setSaving(true);
    try {
      const values = await policyForm.validateFields();
      const response = await updateAdminStorylineChapterOverridePolicy(
        selectedStorylineId,
        selectedRuntime.chapter.id,
        values,
      );
      if (!response.success || !response.data) {
        throw new Error(response.message || '覆寫策略保存失敗');
      }
      message.success('覆寫策略已保存');
      setSnapshot(response.data);
      await reloadCurrent();
    } catch (error) {
      focusFirstInvalidField(policyForm, POLICY_FORM_NAME, error);
      if (error instanceof Error) {
        message.error(error.message);
      }
    } finally {
      setSaving(false);
    }
  };

  const handleNewOverride = (preset?: Partial<OverrideFormValues>) => {
    setSelectedOverrideId(null);
    overrideForm.resetFields();
    overrideForm.setFieldsValue({
      ...overrideToFormValues(null),
      sortOrder: (selectedRuntime?.overrides?.length || 0) * 10 + 10,
      ...preset,
    });
  };

  const handleSaveOverride = async () => {
    if (!selectedStorylineId || !selectedRuntime) {
      message.warning('請先選擇章節');
      return;
    }
    setSaving(true);
    try {
      const values = await overrideForm.validateFields();
      const payload = buildOverridePayload(values);
      const response = selectedOverride
        ? await updateAdminStorylineChapterOverrideStep(
            selectedStorylineId,
            selectedRuntime.chapter.id,
            selectedOverride.id,
            payload,
          )
        : await createAdminStorylineChapterOverrideStep(selectedStorylineId, selectedRuntime.chapter.id, payload);
      if (!response.success || !response.data) {
        throw new Error(response.message || '章節覆寫保存失敗');
      }
      message.success('章節覆寫已保存');
      await reloadCurrent();
      setSelectedOverrideId(response.data.id);
    } catch (error) {
      focusFirstInvalidField(overrideForm, OVERRIDE_FORM_NAME, error);
      if (error instanceof Error) {
        message.error(error.message);
      }
    } finally {
      setSaving(false);
    }
  };

  const handleDeleteOverride = async () => {
    if (!selectedStorylineId || !selectedRuntime || !selectedOverride) {
      return;
    }
    const response = await deleteAdminStorylineChapterOverrideStep(
      selectedStorylineId,
      selectedRuntime.chapter.id,
      selectedOverride.id,
    );
    if (!response.success) {
      message.error(response.message || '刪除章節覆寫失敗');
      return;
    }
    message.success('章節覆寫已刪除');
    await reloadCurrent();
  };

  const renderStepCard = (step: AdminStorylineModeStepSummary) => (
    <div key={`${step.overrideMode || 'inherit'}-${step.stepCode || step.id}`} className="storyline-mode-step-card">
      <Space direction="vertical" size={4} style={{ width: '100%' }}>
        <Space wrap>
          {renderOverrideBadge(step.overrideMode)}
          <Text strong>{pickStepName(step)}</Text>
          <Tag>{step.stepType || 'custom'}</Tag>
          {step.explorationWeightLevel ? <Tag color="cyan">{step.explorationWeightLevel}</Tag> : null}
          {step.requiredForCompletion ? <Tag color="volcano">主線必做</Tag> : null}
        </Space>
        <Text type="secondary">{step.stepCode || `step-${step.id}`}</Text>
      </Space>
    </div>
  );

  return (
    <PageContainer
      title="故事路線與章節覆寫"
      subTitle="編排故事模式地圖策略、章節路線、錨點繼承流程，以及逐步停用、替換或追加故事專屬效果。"
      className="storyline-mode-workbench"
    >
      <Spin spinning={loading}>
        <Row gutter={[16, 16]}>
          <Col xs={24} xl={6}>
            <Space direction="vertical" size="middle" style={{ width: '100%' }}>
              <Card title="故事線選擇">
                <Select
                  showSearch
                  allowClear
                  placeholder="選擇要編排故事模式的故事線"
                  value={selectedStorylineId}
                  style={{ width: '100%' }}
                  optionFilterProp="label"
                  options={storylines.map((storyline) => ({
                    label: `${pickStorylineName(storyline)} (${storyline.code})`,
                    value: storyline.storylineId,
                  }))}
                  onChange={(value) => handleStorylineChange(value ?? undefined)}
                />
              </Card>

              <Card
                title="故事總覽"
                extra={
                  selectedStorylineId ? (
                    <Button size="small" icon={<ReloadOutlined />} onClick={() => void reloadCurrent()}>
                      重新載入
                    </Button>
                  ) : null
                }
              >
                {snapshot ? (
                  <Space direction="vertical" size="small" style={{ width: '100%' }}>
                    <Title level={5} style={{ margin: 0 }}>
                      {pickStorylineName(snapshot.storyline)}
                    </Title>
                    <Text type="secondary">{snapshot.storyline.descriptionZht || snapshot.storyline.descriptionZh}</Text>
                    <Space wrap>
                      {renderStatus(snapshot.storyline.status)}
                      <Tag color="blue">共 {snapshot.chapterRuntimes?.length || 0} 章</Tag>
                      <Tag color="purple">{snapshot.publicRuntimePath}</Tag>
                    </Space>
                  </Space>
                ) : (
                  <Empty description="請先選擇故事線" />
                )}
              </Card>

              <Card title="路線編排">
                {snapshot?.chapterRuntimes?.length ? (
                  <Timeline
                    items={snapshot.chapterRuntimes.map((runtime) => ({
                      color: selectedChapterId === runtime.chapter.id ? 'blue' : runtime.anchor?.anchorType ? 'green' : 'gray',
                      children: (
                        <button
                          type="button"
                          className={`storyline-mode-chapter-node${selectedChapterId === runtime.chapter.id ? ' is-active' : ''}`}
                          onClick={() => handleChapterSelect(runtime.chapter.id)}
                        >
                          <Space direction="vertical" size={2} style={{ width: '100%' }}>
                            <Space wrap>
                              <Text strong>{pickChapterTitle(runtime)}</Text>
                              <Badge count={runtime.compiledStepPreview?.length || 0} size="small" />
                            </Space>
                            <Text type="secondary">
                              {runtime.anchor?.anchorLabel || runtime.anchor?.anchorTargetCode || '未綁定錨點'}
                            </Text>
                          </Space>
                        </button>
                      ),
                    }))}
                  />
                ) : (
                  <Empty description="尚未建立章節" />
                )}
              </Card>

              <Card title="快速故事覆寫">
                <Space direction="vertical" size="small" style={{ width: '100%' }}>
                  {quickOverridePresets.map((preset) => (
                    <Button
                      key={preset.key}
                      block
                      disabled={!selectedRuntime}
                      onClick={() => handleNewOverride(preset.values)}
                    >
                      {preset.label}
                    </Button>
                  ))}
                  <Button block icon={<PlusOutlined />} disabled={!selectedRuntime} onClick={() => handleNewOverride()}>
                    新增自定義覆寫
                  </Button>
                </Space>
              </Card>
            </Space>
          </Col>

          <Col xs={24} xl={10}>
            <Space direction="vertical" size="middle" style={{ width: '100%' }}>
              <Card
                title="故事模式設定"
                extra={
                  <Button type="primary" icon={<SaveOutlined />} loading={saving} disabled={!snapshot} onClick={() => void handleSaveModeConfig()}>
                    保存策略
                  </Button>
                }
              >
                <Form form={modeForm} name={MODE_FORM_NAME} layout="vertical" scrollToFirstError>
                  <Row gutter={12}>
                    <Col span={12}>
                      <Form.Item name="hideUnrelatedContent" label="隱藏無關內容" valuePropName="checked">
                        <Switch />
                      </Form.Item>
                    </Col>
                    <Col span={12}>
                      <Form.Item name="nearbyRevealEnabled" label="靠近顯示無關內容" valuePropName="checked">
                        <Switch />
                      </Form.Item>
                    </Col>
                    <Col span={12}>
                      <Form.Item
                        name="nearbyRevealRadiusMeters"
                        label="靠近顯示半徑（米）"
                        rules={[{ required: true, message: '請設定靠近顯示半徑' }]}
                      >
                        <InputNumber min={0} style={{ width: '100%' }} />
                      </Form.Item>
                    </Col>
                    <Col span={12}>
                      <Form.Item name="currentRouteHighlight" label="當前章節路線高亮">
                        <Input placeholder="copper_flame" />
                      </Form.Item>
                    </Col>
                    <Col span={12}>
                      <Form.Item name="inactiveRouteStyle" label="非當前路線樣式">
                        <Input placeholder="muted_ink" />
                      </Form.Item>
                    </Col>
                    <Col span={12}>
                      <Form.Item name="clearTemporaryProgressOnExit" label="退出故事線清空臨時進度" valuePropName="checked">
                        <Switch />
                      </Form.Item>
                    </Col>
                    <Col span={12}>
                      <Form.Item name="preservePermanentEvents" label="保留永久探索事件" valuePropName="checked">
                        <Switch />
                      </Form.Item>
                    </Col>
                    <Col span={12}>
                      <Form.Item name="branchSourceType" label="支線推薦來源">
                        <Select options={branchSourceOptions} />
                      </Form.Item>
                    </Col>
                    <Col span={12}>
                      <Form.Item name="branchInsertPosition" label="支線插入位置">
                        <Select options={branchInsertOptions} />
                      </Form.Item>
                    </Col>
                    <Col span={12}>
                      <Form.Item name="branchSkippable" label="支線可跳過" valuePropName="checked">
                        <Switch />
                      </Form.Item>
                    </Col>
                    <Col span={12}>
                      <Form.Item name="branchAffectsStoryProgress" label="支線影響故事進度" valuePropName="checked">
                        <Switch />
                      </Form.Item>
                    </Col>
                    <Col span={24}>
                      <Form.Item name="manualBranchPoiIds" label="手選支線 POI ID">
                        <Select mode="tags" tokenSeparators={[',', '，']} placeholder="輸入 POI ID 後按 Enter" />
                      </Form.Item>
                    </Col>
                  </Row>
                  <Collapse
                    className="storyline-mode-advanced"
                    items={[
                      {
                        key: 'advanced',
                        label: '進階 JSON',
                        children: (
                          <>
                            <Alert
                              type="info"
                              showIcon
                              message="進階 JSON 只作備援"
                              description="一般情況請使用上方結構化設定；若開啟進階 JSON，後端仍會要求 schemaVersion。"
                            />
                            <Form.Item name="advancedJsonEnabled" label="使用進階 JSON 覆蓋" valuePropName="checked">
                              <Switch />
                            </Form.Item>
                            <Form.Item name="advancedStoryModeConfigJson" label="故事模式 JSON">
                              <Input.TextArea rows={5} disabled={!advancedModeJsonEnabled} />
                            </Form.Item>
                          </>
                        ),
                      },
                    ]}
                  />
                </Form>
              </Card>

              <Card title="繼承流程">
                {!selectedRuntime ? (
                  <Empty description="請先選擇章節" />
                ) : (
                  <Row gutter={[12, 12]}>
                    <Col span={12}>
                      <Card size="small" title="錨點預設流程">
                        {selectedRuntime.inheritedFlow ? (
                          <Space direction="vertical" size="small" style={{ width: '100%' }}>
                            <Text strong>{selectedRuntime.inheritedFlow.nameZht || selectedRuntime.inheritedFlow.nameZh}</Text>
                            <Text type="secondary">{selectedRuntime.inheritedFlow.code}</Text>
                            <Space wrap>
                              <Tag>{selectedRuntime.inheritedFlow.flowType}</Tag>
                              {renderStatus(selectedRuntime.inheritedFlow.status)}
                            </Space>
                            {(selectedRuntime.inheritedFlow.steps || []).map(renderStepCard)}
                          </Space>
                        ) : (
                          <Alert type="warning" showIcon message="此錨點未找到可繼承的預設流程" />
                        )}
                      </Card>
                    </Col>
                    <Col span={12}>
                      <Card size="small" title="章節專屬流程">
                        {selectedRuntime.chapterFlow ? (
                          <Space direction="vertical" size="small" style={{ width: '100%' }}>
                            <Text strong>{selectedRuntime.chapterFlow.nameZht || selectedRuntime.chapterFlow.nameZh}</Text>
                            <Text type="secondary">{selectedRuntime.chapterFlow.code}</Text>
                            {(selectedRuntime.chapterFlow.steps || []).map(renderStepCard)}
                          </Space>
                        ) : (
                          <Text type="secondary">尚未建立章節專屬流程；保存替換或追加步驟後會自動建立。</Text>
                        )}
                      </Card>
                    </Col>
                  </Row>
                )}
              </Card>

              <Card title="覆寫後 runtime 預覽">
                {selectedRuntime?.compiledStepPreview?.length ? (
                  <Space direction="vertical" size="small" style={{ width: '100%' }}>
                    {selectedRuntime.compiledStepPreview.map(renderStepCard)}
                  </Space>
                ) : (
                  <Empty description="目前沒有可預覽的編譯步驟" />
                )}
              </Card>
            </Space>
          </Col>

          <Col xs={24} xl={8}>
            <Space direction="vertical" size="middle" style={{ width: '100%' }}>
              <Card
                title="章節錨點"
                extra={
                  <Button type="primary" icon={<SaveOutlined />} loading={saving} disabled={!selectedRuntime} onClick={() => void handleSaveAnchor()}>
                    保存錨點
                  </Button>
                }
              >
                <Form form={anchorForm} name={ANCHOR_FORM_NAME} layout="vertical" scrollToFirstError>
                  <Row gutter={12}>
                    <Col span={12}>
                      <Form.Item name="anchorType" label="錨點類型" rules={[{ required: true, message: '請選擇錨點類型' }]}>
                        <Select options={anchorTypeOptions} />
                      </Form.Item>
                    </Col>
                    <Col span={12}>
                      <Form.Item name="anchorTargetId" label="錨點目標 ID">
                        <InputNumber min={1} style={{ width: '100%' }} />
                      </Form.Item>
                    </Col>
                    <Col span={12}>
                      <Form.Item name="anchorTargetCode" label="錨點目標代碼">
                        <Input placeholder="task_or_overlay_code" />
                      </Form.Item>
                    </Col>
                    <Col span={12}>
                      <Form.Item name="routeOrder" label="路線順序">
                        <InputNumber min={1} style={{ width: '100%' }} />
                      </Form.Item>
                    </Col>
                    <Col span={12}>
                      <Form.Item name="routeSegmentStyle" label="路段視覺樣式">
                        <Input placeholder="current_highlight" />
                      </Form.Item>
                    </Col>
                    <Col span={12}>
                      <Form.Item name="anchorLabelOverride" label="錨點顯示名稱">
                        <Input placeholder="例如 媽閣廟" />
                      </Form.Item>
                    </Col>
                  </Row>
                </Form>
              </Card>

              <Card
                title="覆寫策略"
                extra={
                  <Button type="primary" icon={<SaveOutlined />} loading={saving} disabled={!selectedRuntime} onClick={() => void handleSavePolicy()}>
                    保存策略
                  </Button>
                }
              >
                <Form form={policyForm} name={POLICY_FORM_NAME} layout="vertical" scrollToFirstError>
                  <Row gutter={12}>
                    <Col span={12}>
                      <Form.Item name="inheritDefaultFlow" label="繼承錨點預設流程" valuePropName="checked">
                        <Switch />
                      </Form.Item>
                    </Col>
                    <Col span={12}>
                      <Form.Item name="disableDefaultArrivalMedia" label="停用預設抵達媒體" valuePropName="checked">
                        <Switch />
                      </Form.Item>
                    </Col>
                    <Col span={12}>
                      <Form.Item name="appendStorySpecificRewards" label="追加故事專屬獎勵" valuePropName="checked">
                        <Switch />
                      </Form.Item>
                    </Col>
                  </Row>
                  <Collapse
                    className="storyline-mode-advanced"
                    items={[
                      {
                        key: 'advanced',
                        label: '進階 JSON',
                        children: (
                          <>
                            <Form.Item name="advancedJsonEnabled" label="使用進階 JSON 覆蓋" valuePropName="checked">
                              <Switch />
                            </Form.Item>
                            <Form.Item name="advancedOverridePolicyJson" label="覆寫策略 JSON">
                              <Input.TextArea rows={4} disabled={!advancedPolicyJsonEnabled} />
                            </Form.Item>
                          </>
                        ),
                      },
                    ]}
                  />
                </Form>
              </Card>

              <Card
                title="故事專屬內容"
                extra={
                  <Space>
                    {selectedOverride ? (
                      <Popconfirm title="確定刪除此覆寫？" onConfirm={() => void handleDeleteOverride()}>
                        <Button danger icon={<DeleteOutlined />} />
                      </Popconfirm>
                    ) : null}
                    <Button icon={<PlusOutlined />} disabled={!selectedRuntime} onClick={() => handleNewOverride()}>
                      新覆寫
                    </Button>
                    <Button type="primary" icon={<SaveOutlined />} loading={saving} disabled={!selectedRuntime} onClick={() => void handleSaveOverride()}>
                      保存
                    </Button>
                  </Space>
                }
              >
                {selectedRuntime ? (
                  <>
                    <Space wrap style={{ marginBottom: 12 }}>
                      {(selectedRuntime.overrides || []).map((override) => (
                        <Button
                          key={override.id}
                          size="small"
                          type={selectedOverrideId === override.id ? 'primary' : 'default'}
                          onClick={() => setSelectedOverrideId(override.id)}
                        >
                          {renderOverrideBadge(override.overrideMode)}
                          {override.targetStepCode || override.replacementStep?.stepCode || `#${override.id}`}
                        </Button>
                      ))}
                    </Space>
                    <Form form={overrideForm} name={OVERRIDE_FORM_NAME} layout="vertical" scrollToFirstError>
                      <Row gutter={12}>
                        <Col span={12}>
                          <Form.Item name="overrideMode" label="覆寫模式" rules={[{ required: true, message: '請選擇覆寫模式' }]}>
                            <Select options={overrideModeOptions} />
                          </Form.Item>
                        </Col>
                        <Col span={12}>
                          <Form.Item
                            name="targetStepCode"
                            label="目標繼承步驟"
                            rules={[
                              {
                                required: overrideMode === 'disable' || overrideMode === 'replace',
                                message: '停用或替換時必須選擇目標步驟',
                              },
                            ]}
                          >
                            <Select allowClear showSearch optionFilterProp="label" options={inheritedStepOptions} />
                          </Form.Item>
                        </Col>
                      </Row>

                      <Divider orientation="left">主線疊加物 / 支線拾取物 / 隱藏挑戰</Divider>
                      <Row gutter={12}>
                        <Col span={12}>
                          <Form.Item name="stepCode" label="故事步驟代碼">
                            <Input placeholder="story_chapter_mainline_overlays" />
                          </Form.Item>
                        </Col>
                        <Col span={12}>
                          <Form.Item name="stepType" label="故事步驟類型">
                            <Select options={stepTypeOptions} />
                          </Form.Item>
                        </Col>
                        <Col span={24}>
                          <Form.Item name="stepNameZht" label="繁體步驟名">
                            <Input placeholder="例如 鏡海初戰主線疊加物" />
                          </Form.Item>
                        </Col>
                        <Col span={24}>
                          <Form.Item name="descriptionZht" label="繁體說明">
                            <Input.TextArea rows={2} />
                          </Form.Item>
                        </Col>
                        <Col span={12}>
                          <Form.Item name="triggerType" label="觸發條件">
                            <Select options={triggerTypeOptions} />
                          </Form.Item>
                        </Col>
                        <Col span={12}>
                          <Form.Item name="effectPreset" label="完成效果">
                            <Select options={effectPresetOptions} />
                          </Form.Item>
                        </Col>
                        <Col span={12}>
                          <Form.Item name="pickupCodes" label="拾取物 / 疊加物代碼">
                            <Select mode="tags" tokenSeparators={[',', '，']} />
                          </Form.Item>
                        </Col>
                        <Col span={12}>
                          <Form.Item name="challengeCode" label="隱藏挑戰代碼">
                            <Input placeholder="chapter_hidden_challenge" />
                          </Form.Item>
                        </Col>
                      </Row>

                      <Divider orientation="left">獎勵與稱號</Divider>
                      <Row gutter={12}>
                        <Col span={24}>
                          <Form.Item name="rewardRuleIds" label="獎勵規則">
                            <Select mode="multiple" showSearch optionFilterProp="label" options={rewardRuleOptions} />
                          </Form.Item>
                        </Col>
                        <Col span={12}>
                          <Form.Item name="explorationWeightLevel" label="探索權重">
                            <Select options={explorationWeightOptions} />
                          </Form.Item>
                        </Col>
                        <Col span={12}>
                          <Form.Item name="requiredForCompletion" label="章節完成必做" valuePropName="checked">
                            <Switch />
                          </Form.Item>
                        </Col>
                      </Row>

                      <Divider orientation="left">媒體與完成效果</Divider>
                      <Row gutter={12}>
                        <Col span={24}>
                          <SpatialAssetPickerField name="mediaAssetId" label="主媒體資源" assetKind="image" />
                        </Col>
                        <Col span={24}>
                          <SpatialAssetPickerField name="fullScreenMediaAssetId" label="全屏媒體 / Lottie 資源" assetKind="video" />
                        </Col>
                        <Col span={24}>
                          <SpatialAssetPickerField name="audioAssetId" label="音效 / 背景音資源" assetKind="audio" />
                        </Col>
                        <Col span={12}>
                          <Form.Item name="status" label="狀態">
                            <Select options={statusOptions} />
                          </Form.Item>
                        </Col>
                        <Col span={12}>
                          <Form.Item name="sortOrder" label="排序">
                            <InputNumber min={0} style={{ width: '100%' }} />
                          </Form.Item>
                        </Col>
                      </Row>

                      <Collapse
                        className="storyline-mode-advanced"
                        items={[
                          {
                            key: 'advanced',
                            label: '進階 JSON',
                            children: (
                              <>
                                <Alert
                                  type="info"
                                  showIcon
                                  message="進階 JSON 只作 fallback"
                                  description="結構化欄位會自動編譯成 schemaVersion: 1 JSON；只有需要精修 runtime payload 時才開啟。"
                                />
                                <Form.Item name="advancedJsonEnabled" label="使用進階 JSON 覆蓋" valuePropName="checked">
                                  <Switch />
                                </Form.Item>
                                <Form.Item name="advancedTriggerConfigJson" label="Trigger JSON">
                                  <Input.TextArea rows={3} disabled={!advancedOverrideJsonEnabled} />
                                </Form.Item>
                                <Form.Item name="advancedConditionConfigJson" label="Condition JSON">
                                  <Input.TextArea rows={3} disabled={!advancedOverrideJsonEnabled} />
                                </Form.Item>
                                <Form.Item name="advancedEffectConfigJson" label="Effect JSON">
                                  <Input.TextArea rows={3} disabled={!advancedOverrideJsonEnabled} />
                                </Form.Item>
                                <Form.Item name="advancedOverrideConfigJson" label="Override JSON">
                                  <Input.TextArea rows={3} disabled={!advancedOverrideJsonEnabled} />
                                </Form.Item>
                              </>
                            ),
                          },
                        ]}
                      />
                    </Form>
                  </>
                ) : (
                  <Empty description="請先選擇章節" />
                )}
              </Card>
            </Space>
          </Col>
        </Row>

        <Card title="驗證結果與公開 runtime" className="storyline-mode-validation-card">
          {snapshot ? (
            <Space direction="vertical" size="small" style={{ width: '100%' }}>
              <Alert
                type="info"
                showIcon
                message={`公開 runtime：${snapshot.publicRuntimePath}`}
                description="這裡顯示後端編譯後的小程序消費路徑；管理端草稿狀態不應直接流入 public runtime。"
              />
              <Space wrap>
                <Tag color="blue">支線來源：{snapshot.routeStrategy?.branchSourceType || '-'}</Tag>
                <Tag color="purple">插入位置：{snapshot.routeStrategy?.branchInsertPosition || '-'}</Tag>
                <Tag color="cyan">
                  半徑：{snapshot.routeStrategy?.nearbyRevealRadiusMeters || snapshot.routeStrategy?.nearbyRevealMeters || 0} 米
                </Tag>
                {runtimePreview ? <Tag color="green">runtime-preview 已載入</Tag> : <Tag>runtime-preview 未載入</Tag>}
              </Space>
              {snapshot.validationFindings?.length ? (
                snapshot.validationFindings.map((finding, index) => (
                  <Alert
                    key={`${finding.findingType}-${finding.chapterId}-${finding.stepCode || index}`}
                    type={finding.severity === 'error' ? 'error' : finding.severity === 'warning' ? 'warning' : 'info'}
                    showIcon
                    message={finding.stepCode ? `${finding.title} · ${finding.stepCode}` : finding.title}
                    description={finding.description}
                  />
                ))
              ) : (
                <Alert type="success" showIcon message="目前沒有阻擋發布的驗證提示" />
              )}
              <Paragraph type="secondary" style={{ marginBottom: 0 }}>
                完成效果、獎勵與稱號、支線拾取物與隱藏挑戰在此以步驟形式接入；全局衝突治理留給 Phase 31。
              </Paragraph>
            </Space>
          ) : (
            <Empty description="選擇故事線後會顯示缺錨點、缺繼承流程、覆寫不完整與 runtime 對齊提示。" />
          )}
        </Card>
      </Spin>
    </PageContainer>
  );
};

export default StorylineModeWorkbench;
