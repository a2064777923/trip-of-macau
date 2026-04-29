import React, { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { PageContainer } from '@ant-design/pro-components';
import {
  App as AntdApp,
  Button,
  Card,
  Col,
  Collapse,
  Drawer,
  Form,
  Input,
  InputNumber,
  Popconfirm,
  Row,
  Select,
  Space,
  Statistic,
  Switch,
  Table,
  Tabs,
  Tag,
  Timeline,
  Typography,
} from 'antd';
import type { ColumnsType } from 'antd/es/table';
import {
  createAdminExperienceBinding,
  createAdminExperienceFlow,
  createAdminExperienceOverride,
  createAdminExperienceStep,
  createAdminExperienceTemplate,
  createAdminExplorationElement,
  deleteAdminExperienceBinding,
  deleteAdminExperienceFlow,
  deleteAdminExperienceOverride,
  deleteAdminExperienceStep,
  deleteAdminExperienceTemplate,
  deleteAdminExplorationElement,
  getAdminExperienceBindings,
  getAdminExperienceFlowDetail,
  getAdminExperienceFlows,
  getAdminExperienceGovernanceOverview,
  getAdminExperienceOverrides,
  getAdminExperienceTemplates,
  getAdminExplorationElements,
  updateAdminExperienceBinding,
  updateAdminExperienceFlow,
  updateAdminExperienceOverride,
  updateAdminExperienceStep,
  updateAdminExperienceTemplate,
  updateAdminExplorationElement,
} from '../../services/api';
import type {
  AdminExperienceBindingItem,
  AdminExperienceBindingPayload,
  AdminExperienceFlowItem,
  AdminExperienceFlowPayload,
  AdminExperienceGovernanceOverview,
  AdminExperienceOverrideItem,
  AdminExperienceOverridePayload,
  AdminExperienceStepItem,
  AdminExperienceStepPayload,
  AdminExperienceTemplateItem,
  AdminExperienceTemplatePayload,
  AdminExplorationElementItem,
  AdminExplorationElementPayload,
} from '../../types/admin';
import { focusFirstInvalidField } from '../../utils/formErrorFeedback';
import ExperienceTemplateLibrary from './ExperienceTemplateLibrary';
import ExperienceGovernanceCenter from './ExperienceGovernanceCenter';
import './ExperienceWorkbench.css';

const { Text, Title, Paragraph } = Typography;

type TabKey = 'templates' | 'flows' | 'bindings' | 'overrides' | 'exploration' | 'governance';

interface Props {
  initialTab?: TabKey;
}

const tabRoutes: Record<TabKey, string> = {
  flows: '/content/experience',
  templates: '/content/experience/templates',
  bindings: '/content/experience/bindings',
  overrides: '/content/experience/overrides',
  exploration: '/content/experience/exploration',
  governance: '/content/experience/governance',
};

type EditorState =
  | { type: 'template'; item?: AdminExperienceTemplateItem }
  | { type: 'flow'; item?: AdminExperienceFlowItem }
  | { type: 'step'; flowId: number; item?: AdminExperienceStepItem }
  | { type: 'binding'; item?: AdminExperienceBindingItem }
  | { type: 'override'; item?: AdminExperienceOverrideItem }
  | { type: 'element'; item?: AdminExplorationElementItem }
  | null;

const statusOptions = [
  { label: '編輯中', value: 'draft' },
  { label: '已發佈', value: 'published' },
  { label: '已封存', value: 'archived' },
];

const templateTypeOptions = [
  { label: '展示模板', value: 'presentation' },
  { label: '效果演出', value: 'effect' },
  { label: '觸發效果', value: 'trigger_effect' },
  { label: '互動玩法', value: 'gameplay' },
  { label: '顯示條件', value: 'display_condition' },
  { label: '觸發條件', value: 'trigger_condition' },
  { label: '任務玩法', value: 'task_gameplay' },
  { label: '獎勵演出', value: 'reward_presentation' },
];

const stepTypeOptions = [
  { label: '點擊介紹', value: 'intro_modal' },
  { label: '導航卡片', value: 'route_guidance' },
  { label: '靠近全屏媒體', value: 'proximity_media' },
  { label: '打卡任務', value: 'checkin_task' },
  { label: '拾取物', value: 'pickup' },
  { label: '隱藏挑戰', value: 'hidden_challenge' },
  { label: '獎勵發放', value: 'reward_grant' },
  { label: '自定義', value: 'custom' },
];

const triggerTypeOptions = [
  { label: '手動', value: 'manual' },
  { label: '點擊', value: 'tap' },
  { label: '點擊動作', value: 'tap_action' },
  { label: '靠近', value: 'proximity' },
  { label: '媒體播放完成', value: 'media_finished' },
  { label: '停留', value: 'dwell' },
  { label: '進入故事線模式', value: 'story_mode_enter' },
  { label: '連續點擊序列', value: 'tap_sequence' },
  { label: '混合條件', value: 'mixed' },
  { label: '複合條件', value: 'compound' },
  { label: '完成任務', value: 'task_complete' },
  { label: '完成內容閱讀', value: 'content_complete' },
  { label: '拾取完成', value: 'pickup_complete' },
];

const flowTypeOptions = [
  { label: 'POI 預設體驗', value: 'default_poi' },
  { label: '室內建築預設體驗', value: 'default_indoor_building' },
  { label: '室內樓層預設體驗', value: 'default_indoor_floor' },
  { label: '室內節點預設體驗', value: 'default_indoor_node' },
  { label: '任務預設體驗', value: 'default_task' },
  { label: '標記預設體驗', value: 'default_marker' },
  { label: '疊加物預設體驗', value: 'default_overlay' },
  { label: '活動預設體驗', value: 'default_activity' },
  { label: '故事章節覆寫', value: 'story_chapter_override' },
  { label: '手動目標流程', value: 'manual_target' },
];

const ownerTypeOptions = [
  { label: 'POI', value: 'poi' },
  { label: '室內建築', value: 'indoor_building' },
  { label: '室內樓層', value: 'indoor_floor' },
  { label: '室內節點 / 標記', value: 'indoor_node' },
  { label: '故事章節', value: 'story_chapter' },
  { label: '任務', value: 'task' },
  { label: '標記', value: 'marker' },
  { label: '疊加物', value: 'overlay' },
  { label: '活動', value: 'activity' },
  { label: '手動目標', value: 'manual_target' },
];

const bindingRoleOptions = [
  { label: '錨點預設體驗流程', value: 'default_experience_flow' },
  { label: '故事章節覆寫流程', value: 'story_override_flow' },
];

const weightOptions = [
  { label: '極小', value: 'tiny' },
  { label: '少量', value: 'small' },
  { label: '中量', value: 'medium' },
  { label: '大量', value: 'large' },
  { label: '核心', value: 'core' },
];

const statusColor = (status?: string) => {
  if (status === 'published') return 'green';
  if (status === 'reviewing') return 'blue';
  if (status === 'archived') return 'default';
  if (status === 'unpublished') return 'volcano';
  return 'gold';
};

const jsonPreset = (extra?: Record<string, unknown>) => JSON.stringify({ schemaVersion: 1, ...(extra || {}) }, null, 2);

const amaTempleStepPresets: Array<{
  key: string;
  label: string;
  description: string;
  values: Partial<AdminExperienceStepPayload>;
}> = [
  {
    key: 'ama_intro_popup',
    label: '媽閣廟圖文介紹',
    description: '點擊 POI 後顯示簡介與「前往探索該地」按鈕。',
    values: {
      stepCode: 'ama_intro_popup',
      stepType: 'intro_modal',
      triggerType: 'tap',
      stepNameZh: '媽閣廟圖文介紹',
      descriptionZh: '點擊媽閣廟後彈出圖文窗口，底部提供前往探索該地的操作。',
      explorationWeightLevel: 'tiny',
      requiredForCompletion: false,
      triggerConfigJson: jsonPreset({ trigger: 'tap', target: 'poi_card' }),
      conditionConfigJson: jsonPreset({ condition: 'always' }),
      effectConfigJson: jsonPreset({ effect: 'show_modal', primaryAction: '前往探索該地', targetPoi: '媽閣廟' }),
    },
  },
  {
    key: 'ama_route_guidance',
    label: '路線規劃與推薦卡',
    description: '點擊前往後規劃路線，展示交通、故事線、附近與途經推薦。',
    values: {
      stepCode: 'ama_route_guidance',
      stepType: 'route_guidance',
      triggerType: 'tap',
      stepNameZh: '媽閣廟路線規劃與推薦卡',
      descriptionZh: '設定目的地為媽閣廟，顯示路線、交通方式、推薦故事線與附近地點卡片。',
      explorationWeightLevel: 'tiny',
      requiredForCompletion: false,
      triggerConfigJson: jsonPreset({ trigger: 'tap_action', action: 'start_explore' }),
      conditionConfigJson: jsonPreset({ condition: 'after_step', stepCode: 'ama_intro_popup' }),
      effectConfigJson: jsonPreset({ effect: 'route_guidance', cards: ['transport', 'storyline', 'nearby_poi', 'waypoint_poi'] }),
    },
  },
  {
    key: 'ama_proximity_media',
    label: '靠近全屏媒體',
    description: '進入 50 米範圍後播放全屏介紹動畫或影片。',
    values: {
      stepCode: 'ama_arrival_fullscreen_media',
      stepType: 'proximity_media',
      triggerType: 'proximity',
      stepNameZh: '媽閣廟到達全屏媒體',
      descriptionZh: '用戶進入媽閣廟 50 米範圍後播放全屏介紹動畫與背景音。',
      explorationWeightLevel: 'small',
      requiredForCompletion: false,
      triggerConfigJson: jsonPreset({ trigger: 'proximity', radiusMeters: 50 }),
      conditionConfigJson: jsonPreset({ condition: 'once_per_user' }),
      effectConfigJson: jsonPreset({ effect: 'fullscreen_media', mediaSlot: 'arrival_intro', audioSlot: 'arrival_bgm' }),
    },
  },
  {
    key: 'ama_checkin_tasks',
    label: '打卡任務派發',
    description: '全屏媒體完成後派發大門拍照與賽博點香任務。',
    values: {
      stepCode: 'ama_release_checkin_tasks',
      stepType: 'checkin_task',
      triggerType: 'content_complete',
      stepNameZh: '媽閣廟打卡任務派發',
      descriptionZh: '動畫播放完後開放打卡，派發「大門照片」與「賽博點香」兩個任務。',
      explorationWeightLevel: 'medium',
      requiredForCompletion: true,
      triggerConfigJson: jsonPreset({ trigger: 'media_finished', sourceStepCode: 'ama_arrival_fullscreen_media' }),
      conditionConfigJson: jsonPreset({ condition: 'near_poi', radiusMeters: 50 }),
      effectConfigJson: jsonPreset({ effect: 'release_tasks', taskCodes: ['ama_gate_photo', 'ama_cyber_incense'] }),
    },
  },
  {
    key: 'ama_pickups',
    label: '拾取物與支線線索',
    description: '配置令牌、殘片、契約殘頁等可拾取疊加物。',
    values: {
      stepCode: 'ama_side_pickups',
      stepType: 'pickup',
      triggerType: 'tap',
      stepNameZh: '媽閣廟拾取物與支線線索',
      descriptionZh: '在媽閣廟範圍內配置可拾取疊加物，完成後寫入背包與探索事件。',
      explorationWeightLevel: 'medium',
      requiredForCompletion: false,
      triggerConfigJson: jsonPreset({ trigger: 'tap_overlay' }),
      conditionConfigJson: jsonPreset({ condition: 'after_step', stepCode: 'ama_release_checkin_tasks' }),
      effectConfigJson: jsonPreset({ effect: 'grant_collectibles', items: ['明朝海防銅令牌', '濠江漁民禦敵漁網殘片', '葡人通商納稅契約殘頁'] }),
    },
  },
  {
    key: 'ama_hidden_achievement',
    label: '隱藏成就與稱號',
    description: '停留 30 分鐘或完成全收集後發放隱藏成就與稱號。',
    values: {
      stepCode: 'ama_hidden_achievement_title',
      stepType: 'hidden_challenge',
      triggerType: 'dwell',
      stepNameZh: '媽閣廟隱藏成就與稱號',
      descriptionZh: '在 POI 30 米範圍內累計停留 30 分鐘後，發放隱藏成就「媽祖最愛心誠的孩子」。',
      explorationWeightLevel: 'large',
      requiredForCompletion: false,
      triggerConfigJson: jsonPreset({ trigger: 'dwell', radiusMeters: 30, dwellSeconds: 1800 }),
      conditionConfigJson: jsonPreset({ condition: 'all_optional_or_dwell' }),
      effectConfigJson: jsonPreset({ effect: 'grant_title', title: '媽祖最愛心誠的孩子', achievement: '鏡海守護者' }),
    },
  },
  {
    key: 'ama_reward_grant',
    label: '獎勵與稱號發放',
    description: '完成任務後發放金幣、徽章、稱號或故事線限定物品。',
    values: {
      stepCode: 'ama_checkin_reward_grant',
      stepType: 'reward_grant',
      triggerType: 'task_complete',
      stepNameZh: '媽閣廟獎勵與稱號發放',
      descriptionZh: '完成打卡與互動任務後，發放金幣、徽章、稱號及可選故事線限定碎片。',
      explorationWeightLevel: 'core',
      requiredForCompletion: true,
      triggerConfigJson: jsonPreset({ trigger: 'tasks_completed', taskCodes: ['ama_gate_photo', 'ama_cyber_incense'] }),
      conditionConfigJson: jsonPreset({ condition: 'checkin_ready' }),
      effectConfigJson: jsonPreset({ effect: 'reward_grant', grants: ['coins', 'badge', 'title', 'story_fragment'] }),
    },
  },
];

const jsonRule = (versioned = true) => ({
  validator(_: unknown, value?: string) {
    if (!value) return Promise.resolve();
    try {
      const parsed = JSON.parse(value);
      if (versioned && (!parsed || typeof parsed !== 'object' || Array.isArray(parsed) || !('schemaVersion' in parsed))) {
        return Promise.reject(new Error('JSON 必須是物件，並包含 schemaVersion'));
      }
      return Promise.resolve();
    } catch {
      return Promise.reject(new Error('JSON 格式不正確'));
    }
  },
});

const pickTemplateName = (item?: AdminExperienceTemplateItem | null) =>
  item?.nameZht || item?.nameZh || item?.nameEn || item?.code || '';

const pickFlowName = (item?: AdminExperienceFlowItem | null) =>
  item?.nameZht || item?.nameZh || item?.nameEn || item?.code || '';

const pickStepName = (item?: AdminExperienceStepItem | null) =>
  item?.stepNameZht || item?.stepNameZh || item?.stepNameEn || item?.stepCode || '';

const ExperienceOrchestrationWorkbench: React.FC<Props> = ({ initialTab = 'flows' }) => {
  const { message } = AntdApp.useApp();
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState<TabKey>(initialTab);
  const [templates, setTemplates] = useState<AdminExperienceTemplateItem[]>([]);
  const [flows, setFlows] = useState<AdminExperienceFlowItem[]>([]);
  const [bindings, setBindings] = useState<AdminExperienceBindingItem[]>([]);
  const [overrides, setOverrides] = useState<AdminExperienceOverrideItem[]>([]);
  const [elements, setElements] = useState<AdminExplorationElementItem[]>([]);
  const [governance, setGovernance] = useState<AdminExperienceGovernanceOverview>();
  const [selectedFlow, setSelectedFlow] = useState<AdminExperienceFlowItem | null>(null);
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [editor, setEditor] = useState<EditorState>(null);

  const [templateForm] = Form.useForm<AdminExperienceTemplatePayload>();
  const [flowForm] = Form.useForm<AdminExperienceFlowPayload>();
  const [stepForm] = Form.useForm<AdminExperienceStepPayload>();
  const [bindingForm] = Form.useForm<AdminExperienceBindingPayload>();
  const [overrideForm] = Form.useForm<AdminExperienceOverridePayload>();
  const [elementForm] = Form.useForm<AdminExplorationElementPayload>();

  const flowOptions = useMemo(
    () => flows.map((flow) => ({ label: `${pickFlowName(flow)} · #${flow.id}`, value: flow.id })),
    [flows],
  );
  const templateOptions = useMemo(
    () => templates.map((template) => ({ label: `${pickTemplateName(template)} · ${template.templateType}`, value: template.id })),
    [templates],
  );

  const refreshAll = async () => {
    setLoading(true);
    try {
      const [templateRes, flowRes, bindingRes, overrideRes, elementRes, governanceRes] = await Promise.all([
        getAdminExperienceTemplates({ pageNum: 1, pageSize: 100 }),
        getAdminExperienceFlows({ pageNum: 1, pageSize: 100 }),
        getAdminExperienceBindings({ pageNum: 1, pageSize: 100 }),
        getAdminExperienceOverrides({ pageNum: 1, pageSize: 100 }),
        getAdminExplorationElements({ pageNum: 1, pageSize: 100 }),
        getAdminExperienceGovernanceOverview(),
      ]);
      if (templateRes.success && templateRes.data) setTemplates(templateRes.data.list || []);
      if (flowRes.success && flowRes.data) setFlows(flowRes.data.list || []);
      if (bindingRes.success && bindingRes.data) setBindings(bindingRes.data.list || []);
      if (overrideRes.success && overrideRes.data) setOverrides(overrideRes.data.list || []);
      if (elementRes.success && elementRes.data) setElements(elementRes.data.list || []);
      if (governanceRes.success && governanceRes.data) setGovernance(governanceRes.data);
      if (selectedFlow?.id) {
        const detail = await getAdminExperienceFlowDetail(selectedFlow.id);
        if (detail.success && detail.data) setSelectedFlow(detail.data);
      }
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void refreshAll();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => {
    setActiveTab(initialTab);
  }, [initialTab]);

  const openTemplate = (item?: AdminExperienceTemplateItem) => {
    setEditor({ type: 'template', item });
    templateForm.resetFields();
    templateForm.setFieldsValue({
      templateType: 'presentation',
      category: 'modal',
      riskLevel: 'normal',
      status: 'draft',
      sortOrder: 0,
      configJson: jsonPreset({ presentation: 'rich_modal' }),
      schemaJson: jsonPreset({ required: [] }),
      ...item,
    });
  };

  const openFlow = (item?: AdminExperienceFlowItem) => {
    setEditor({ type: 'flow', item });
    flowForm.resetFields();
    flowForm.setFieldsValue({
      flowType: 'default_poi',
      mode: 'walk_in',
      status: 'draft',
      sortOrder: 0,
      mapPolicyJson: jsonPreset({ storyMode: false, hideIrrelevantContent: false }),
      advancedConfigJson: jsonPreset({ branchRecommendation: 'manual' }),
      ...item,
    });
  };

  const applyAmaTempleFlowPreset = () => {
    flowForm.setFieldsValue({
      code: 'poi_ama_temple_default_experience',
      flowType: 'default_poi',
      mode: 'walk_in',
      nameZh: '媽閣廟預設體驗流程',
      descriptionZh: '自然 walk-in 或點擊前往媽閣廟時使用的地點預設體驗，涵蓋圖文介紹、路線規劃、靠近全屏媒體、打卡任務、拾取物、隱藏成就、獎勵與稱號。',
      mapPolicyJson: jsonPreset({ storyMode: false, hideIrrelevantContent: false, routePlanning: true }),
      advancedConfigJson: jsonPreset({ preset: 'ama_temple', recommendedStorylines: true, nearbyRecommendations: true }),
    });
    message.success('已套用媽閣廟 POI 預設體驗流程');
  };

  const openStep = (flowId: number, item?: AdminExperienceStepItem) => {
    setEditor({ type: 'step', flowId, item });
    stepForm.resetFields();
    stepForm.setFieldsValue({
      stepType: 'intro_modal',
      triggerType: 'tap',
      explorationWeightLevel: 'small',
      requiredForCompletion: false,
      status: 'draft',
      sortOrder: selectedFlow?.steps?.length || 0,
      triggerConfigJson: jsonPreset({ trigger: 'tap' }),
      conditionConfigJson: jsonPreset({ condition: 'always' }),
      effectConfigJson: jsonPreset({ effect: 'show_modal' }),
      rewardRuleIdsJson: '[]',
      ...item,
    });
  };

  const applyAmaTempleStepPreset = (presetKey: string) => {
    const preset = amaTempleStepPresets.find((item) => item.key === presetKey);
    if (!preset) {
      return;
    }
    stepForm.setFieldsValue(preset.values);
    message.success(`已套用「${preset.label}」預設`);
  };

  const openBinding = (item?: AdminExperienceBindingItem) => {
    setEditor({ type: 'binding', item });
    bindingForm.resetFields();
    bindingForm.setFieldsValue({
      ownerType: 'poi',
      bindingRole: 'default_experience_flow',
      inheritPolicy: 'inherit',
      priority: 0,
      status: 'draft',
      sortOrder: 0,
      ...item,
    });
  };

  const openOverride = (item?: AdminExperienceOverrideItem) => {
    setEditor({ type: 'override', item });
    overrideForm.resetFields();
    overrideForm.setFieldsValue({
      ownerType: 'story_chapter',
      targetOwnerType: 'poi',
      overrideMode: 'disable',
      status: 'draft',
      sortOrder: 0,
      overrideConfigJson: jsonPreset({ reason: 'storyline-specific' }),
      ...item,
    });
  };

  const openElement = (item?: AdminExplorationElementItem) => {
    setEditor({ type: 'element', item });
    elementForm.resetFields();
    elementForm.setFieldsValue({
      elementType: 'story_chapter_complete',
      ownerType: 'story_chapter',
      weightLevel: 'small',
      includeInExploration: true,
      status: 'draft',
      sortOrder: 0,
      metadataJson: jsonPreset({ source: 'admin' }),
      ...item,
    });
  };

  const validateForm = async <T,>(form: ReturnType<typeof Form.useForm<T>>[0], formName: string) => {
    try {
      return await form.validateFields();
    } catch (error) {
      // focusFirstInvalidField 會 scrollToField、focus，並加上 codex-form-item-shake 視覺提示。
      focusFirstInvalidField(form, formName, error);
      throw error;
    }
  };

  const handleSave = async () => {
    if (!editor) return;
    setSaving(true);
    try {
      if (editor.type === 'template') {
        const values = await validateForm(templateForm, 'experienceTemplateForm');
        const response = editor.item
          ? await updateAdminExperienceTemplate(editor.item.id, values)
          : await createAdminExperienceTemplate(values);
        if (!response.success) throw new Error(response.message || '保存模板失敗');
        message.success('模板已保存');
      }
      if (editor.type === 'flow') {
        const values = await validateForm(flowForm, 'experienceFlowForm');
        const response = editor.item
          ? await updateAdminExperienceFlow(editor.item.id, values)
          : await createAdminExperienceFlow(values);
        if (!response.success) throw new Error(response.message || '保存流程失敗');
        message.success('流程已保存');
        if (response.data) setSelectedFlow(response.data);
      }
      if (editor.type === 'step') {
        const values = await validateForm(stepForm, 'experienceStepForm');
        const response = editor.item
          ? await updateAdminExperienceStep(editor.flowId, editor.item.id, values)
          : await createAdminExperienceStep(editor.flowId, values);
        if (!response.success) throw new Error(response.message || '保存流程步驟失敗');
        message.success('流程步驟已保存');
      }
      if (editor.type === 'binding') {
        const values = await validateForm(bindingForm, 'experienceBindingForm');
        const response = editor.item
          ? await updateAdminExperienceBinding(editor.item.id, values)
          : await createAdminExperienceBinding(values);
        if (!response.success) throw new Error(response.message || '保存流程綁定失敗');
        message.success('流程綁定已保存');
      }
      if (editor.type === 'override') {
        const values = await validateForm(overrideForm, 'experienceOverrideForm');
        const response = editor.item
          ? await updateAdminExperienceOverride(editor.item.id, values)
          : await createAdminExperienceOverride(values);
        if (!response.success) throw new Error(response.message || '保存覆寫規則失敗');
        message.success('覆寫規則已保存');
      }
      if (editor.type === 'element') {
        const values = await validateForm(elementForm, 'explorationElementForm');
        const response = editor.item
          ? await updateAdminExplorationElement(editor.item.id, values)
          : await createAdminExplorationElement(values);
        if (!response.success) throw new Error(response.message || '保存探索元素失敗');
        message.success('探索元素已保存');
      }
      setEditor(null);
      await refreshAll();
    } catch (error) {
      if (error instanceof Error) message.error(error.message);
    } finally {
      setSaving(false);
    }
  };

  const selectFlow = async (record: AdminExperienceFlowItem) => {
    setLoading(true);
    try {
      const response = await getAdminExperienceFlowDetail(record.id);
      if (response.success && response.data) {
        setSelectedFlow(response.data);
        setActiveTab('flows');
      }
    } finally {
      setLoading(false);
    }
  };

  const templateColumns: ColumnsType<AdminExperienceTemplateItem> = [
    {
      title: '模板',
      render: (_, record) => (
        <Space direction="vertical" size={2}>
          <Text strong>{pickTemplateName(record)}</Text>
          <Text type="secondary">{record.code}</Text>
        </Space>
      ),
    },
    { title: '類型', dataIndex: 'templateType', width: 160, render: (value) => <Tag>{value}</Tag> },
    { title: '分類', dataIndex: 'category', width: 140 },
    { title: '風險', dataIndex: 'riskLevel', width: 100, render: (value) => <Tag color={value === 'high' ? 'red' : 'blue'}>{value || 'normal'}</Tag> },
    { title: '使用', dataIndex: 'usageCount', width: 90 },
    { title: '狀態', dataIndex: 'status', width: 110, render: (value) => <Tag color={statusColor(value)}>{value}</Tag> },
    {
      title: '操作',
      width: 160,
      render: (_, record) => (
        <Space>
          <Button type="link" onClick={() => openTemplate(record)}>編輯</Button>
          <Popconfirm title="確認刪除此模板？" onConfirm={() => deleteAdminExperienceTemplate(record.id).then(refreshAll)}>
            <Button type="link" danger>刪除</Button>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  const flowColumns: ColumnsType<AdminExperienceFlowItem> = [
    {
      title: '流程',
      render: (_, record) => (
        <Space direction="vertical" size={4} style={{ width: '100%', minWidth: 0 }}>
          <Text strong ellipsis={{ tooltip: pickFlowName(record) }}>
            {pickFlowName(record)}
          </Text>
          <Text type="secondary" ellipsis={{ tooltip: record.code }}>
            {record.code}
          </Text>
          <Space size={4} wrap>
            <Tag>{record.flowType}</Tag>
            <Tag>{record.mode}</Tag>
            <Tag color={statusColor(record.status)}>{record.status}</Tag>
          </Space>
        </Space>
      ),
    },
    {
      title: '操作',
      width: 150,
      render: (_, record) => (
        <Space size={0} wrap>
          <Button type="link" onClick={() => selectFlow(record)}>打開流程</Button>
          <Button type="link" onClick={() => openFlow(record)}>編輯</Button>
          <Popconfirm title="確認刪除此流程？已綁定流程不可刪除。" onConfirm={() => deleteAdminExperienceFlow(record.id).then(refreshAll)}>
            <Button type="link" danger>刪除</Button>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  const stepColumns: ColumnsType<AdminExperienceStepItem> = [
    { title: '順序', dataIndex: 'sortOrder', width: 80 },
    {
      title: '步驟',
      render: (_, record) => (
        <Space direction="vertical" size={2}>
          <Text strong>{pickStepName(record)}</Text>
          <Text type="secondary">{record.stepCode}</Text>
        </Space>
      ),
    },
    { title: '類型', dataIndex: 'stepType', width: 150, render: (value) => <Tag>{value}</Tag> },
    { title: '觸發', dataIndex: 'triggerType', width: 130 },
    { title: '權重', dataIndex: 'explorationWeightLevel', width: 100 },
    { title: '必需', dataIndex: 'requiredForCompletion', width: 80, render: (value) => (value ? <Tag color="red">必需</Tag> : <Tag>可選</Tag>) },
    {
      title: '操作',
      width: 150,
      render: (_, record) => (
        <Space>
          <Button type="link" onClick={() => selectedFlow && openStep(selectedFlow.id, record)}>編輯</Button>
          <Popconfirm title="確認刪除此步驟？" onConfirm={() => selectedFlow && deleteAdminExperienceStep(selectedFlow.id, record.id).then(refreshAll)}>
            <Button type="link" danger>刪除</Button>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  const bindingColumns: ColumnsType<AdminExperienceBindingItem> = [
    { title: '擁有者', render: (_, record) => `${record.ownerType} #${record.ownerId || record.ownerCode || '-'}` },
    { title: '角色', dataIndex: 'bindingRole', width: 180 },
    { title: '流程', render: (_, record) => record.flowName || `#${record.flowId}` },
    { title: '繼承', dataIndex: 'inheritPolicy', width: 110 },
    { title: '狀態', dataIndex: 'status', width: 110, render: (value) => <Tag color={statusColor(value)}>{value}</Tag> },
    {
      title: '操作',
      width: 150,
      render: (_, record) => (
        <Space>
          <Button type="link" onClick={() => openBinding(record)}>編輯</Button>
          <Popconfirm title="確認刪除此綁定？" onConfirm={() => deleteAdminExperienceBinding(record.id).then(refreshAll)}>
            <Button type="link" danger>刪除</Button>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  const overrideColumns: ColumnsType<AdminExperienceOverrideItem> = [
    { title: '章節 / 擁有者', render: (_, record) => `${record.ownerType} #${record.ownerId}` },
    { title: '目標', render: (_, record) => `${record.targetOwnerType || '-'} ${record.targetStepCode || ''}` },
    { title: '覆寫方式', dataIndex: 'overrideMode', width: 120, render: (value) => <Tag>{value}</Tag> },
    { title: '替換步驟', dataIndex: 'replacementStepId', width: 110 },
    { title: '狀態', dataIndex: 'status', width: 110, render: (value) => <Tag color={statusColor(value)}>{value}</Tag> },
    {
      title: '操作',
      width: 150,
      render: (_, record) => (
        <Space>
          <Button type="link" onClick={() => openOverride(record)}>編輯</Button>
          <Popconfirm title="確認刪除此覆寫？" onConfirm={() => deleteAdminExperienceOverride(record.id).then(refreshAll)}>
            <Button type="link" danger>刪除</Button>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  const elementColumns: ColumnsType<AdminExplorationElementItem> = [
    {
      title: '探索元素',
      render: (_, record) => (
        <Space direction="vertical" size={2}>
          <Text strong>{record.titleZht || record.titleZh}</Text>
          <Text type="secondary">{record.elementCode}</Text>
        </Space>
      ),
    },
    { title: '類型', dataIndex: 'elementType', width: 170, render: (value) => <Tag>{value}</Tag> },
    { title: '擁有者', render: (_, record) => `${record.ownerType} #${record.ownerId || record.ownerCode || '-'}` },
    { title: '權重', render: (_, record) => `${record.weightLevel || 'small'} / ${record.weightValue ?? '-'}`, width: 130 },
    { title: '計入', dataIndex: 'includeInExploration', width: 80, render: (value) => (value ? '是' : '否') },
    { title: '狀態', dataIndex: 'status', width: 110, render: (value) => <Tag color={statusColor(value)}>{value}</Tag> },
    {
      title: '操作',
      width: 150,
      render: (_, record) => (
        <Space>
          <Button type="link" onClick={() => openElement(record)}>編輯</Button>
          <Popconfirm title="確認刪除此探索元素？" onConfirm={() => deleteAdminExplorationElement(record.id).then(refreshAll)}>
            <Button type="link" danger>刪除</Button>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  const renderFlowWorkbench = () => (
    <Row gutter={16}>
      <Col xs={24} xl={14}>
        <Card
          title="體驗流程"
          extra={<Button type="primary" onClick={() => openFlow()}>新增流程</Button>}
        >
          <Table
            rowKey="id"
            loading={loading}
            columns={flowColumns}
            dataSource={flows}
            pagination={{ pageSize: 8 }}
            size="small"
          />
        </Card>
      </Col>
      <Col xs={24} xl={10}>
        <Card
          title={selectedFlow ? `流程編排：${pickFlowName(selectedFlow)}` : '流程時間線'}
          extra={selectedFlow ? <Button type="primary" onClick={() => openStep(selectedFlow.id)}>新增步驟</Button> : null}
        >
          {selectedFlow ? (
            <Space direction="vertical" style={{ width: '100%' }} size="large">
              <Timeline
                items={(selectedFlow.steps || []).map((step) => ({
                  color: step.requiredForCompletion ? 'red' : 'blue',
                  children: (
                    <Space direction="vertical" size={2}>
                      <Text strong>{pickStepName(step)}</Text>
                      <Text type="secondary">{step.stepType} · {step.triggerType} · {step.explorationWeightLevel}</Text>
                    </Space>
                  ),
                }))}
              />
              <Table rowKey="id" columns={stepColumns} dataSource={selectedFlow.steps || []} pagination={false} size="small" />
            </Space>
          ) : (
            <Paragraph type="secondary">
              從左側打開一條流程後，可按時間線編排「點擊介紹、導航、靠近播放媒體、派任務、拾取物、獎勵」等步驟。
            </Paragraph>
          )}
        </Card>
      </Col>
    </Row>
  );

  const renderGovernance = () => (
    <Space direction="vertical" style={{ width: '100%' }} size="large">
      <Row gutter={16}>
        <Col span={4}><Card><Statistic title="模板" value={governance?.templateCount || 0} /></Card></Col>
        <Col span={4}><Card><Statistic title="流程" value={governance?.flowCount || 0} /></Card></Col>
        <Col span={4}><Card><Statistic title="綁定" value={governance?.bindingCount || 0} /></Card></Col>
        <Col span={4}><Card><Statistic title="覆寫" value={governance?.overrideCount || 0} /></Card></Col>
        <Col span={4}><Card><Statistic title="探索元素" value={governance?.explorationElementCount || 0} /></Card></Col>
        <Col span={4}><Card><Statistic title="高風險模板" value={governance?.highRiskTemplateCount || 0} /></Card></Col>
      </Row>
      <Card title="衝突與治理提示">
        <Table
          rowKey={(record) =>
            [
              record.findingType,
              record.ownerType || '-',
              record.ownerId || '-',
              record.title || '-',
              record.severity || '-',
            ].join(':')
          }
          dataSource={governance?.findings || []}
          pagination={false}
          columns={[
            { title: '嚴重度', dataIndex: 'severity', width: 110, render: (value) => <Tag color={value === 'error' ? 'red' : 'gold'}>{value}</Tag> },
            { title: '類型', dataIndex: 'findingType', width: 180 },
            { title: '問題', dataIndex: 'title' },
            { title: '說明', dataIndex: 'description' },
            { title: '關聯', render: (_, record) => `${record.ownerType || '-'} #${record.ownerId || '-'}` },
          ]}
        />
      </Card>
    </Space>
  );

  return (
    <PageContainer
      title="v3.0 體驗編排系統"
      subTitle="地點預設體驗、故事線覆寫、互動模板、探索元素與治理中心"
      extra={<Button onClick={refreshAll} loading={loading}>重新載入</Button>}
    >
      <Space direction="vertical" style={{ width: '100%' }} size="large">
        <Card>
          <Title level={4} style={{ marginTop: 0 }}>編排方式</Title>
          <Paragraph type="secondary" style={{ marginBottom: 0 }}>
            POI、室內節點、故事章節等主體可綁定一條預設流程；故事章節默認繼承錨點流程，再用覆寫規則關閉、替換或追加步驟。探索度只配置元素權重，百分比由後端根據已發布元素動態計算。
          </Paragraph>
        </Card>

        <Tabs
          activeKey={activeTab}
          onChange={(key) => {
            const nextTab = key as TabKey;
            setActiveTab(nextTab);
            navigate(tabRoutes[nextTab]);
          }}
          items={[
            {
              key: 'flows',
              label: '體驗流程工作台',
              children: renderFlowWorkbench(),
            },
            {
              key: 'templates',
              label: '互動與任務模板庫',
              children: <ExperienceTemplateLibrary />,
            },
            {
              key: 'bindings',
              label: '體驗流程綁定',
              children: (
                <Card title="主體與流程綁定" extra={<Button type="primary" onClick={() => openBinding()}>新增綁定</Button>}>
                  <Table rowKey="id" loading={loading} columns={bindingColumns} dataSource={bindings} pagination={{ pageSize: 10 }} />
                </Card>
              ),
            },
            {
              key: 'overrides',
              label: '故事章節覆寫',
              children: (
                <Card title="繼承流程覆寫規則" extra={<Button type="primary" onClick={() => openOverride()}>新增覆寫</Button>}>
                  <Table rowKey="id" loading={loading} columns={overrideColumns} dataSource={overrides} pagination={{ pageSize: 10 }} />
                </Card>
              ),
            },
            {
              key: 'exploration',
              label: '探索元素與進度規則',
              children: (
                <Card title="探索元素註冊表" extra={<Button type="primary" onClick={() => openElement()}>新增探索元素</Button>}>
                  <Table rowKey="id" loading={loading} columns={elementColumns} dataSource={elements} pagination={{ pageSize: 10 }} />
                </Card>
              ),
            },
            {
              key: 'governance',
              label: '體驗規則治理中心',
              children: <ExperienceGovernanceCenter />,
            },
          ]}
        />
      </Space>

      {!editor && (
        <>
          <Form form={templateForm} style={{ display: 'none' }}><Form.Item name="__formConnector" hidden preserve={false}><Input /></Form.Item></Form>
          <Form form={flowForm} style={{ display: 'none' }}><Form.Item name="__formConnector" hidden preserve={false}><Input /></Form.Item></Form>
          <Form form={stepForm} style={{ display: 'none' }}><Form.Item name="__formConnector" hidden preserve={false}><Input /></Form.Item></Form>
          <Form form={bindingForm} style={{ display: 'none' }}><Form.Item name="__formConnector" hidden preserve={false}><Input /></Form.Item></Form>
          <Form form={overrideForm} style={{ display: 'none' }}><Form.Item name="__formConnector" hidden preserve={false}><Input /></Form.Item></Form>
          <Form form={elementForm} style={{ display: 'none' }}><Form.Item name="__formConnector" hidden preserve={false}><Input /></Form.Item></Form>
        </>
      )}

      <Drawer
        open={!!editor}
        title={drawerTitle(editor)}
        width={720}
        onClose={() => setEditor(null)}
        extra={<Space><Button onClick={() => setEditor(null)}>取消</Button><Button type="primary" loading={saving} onClick={handleSave}>保存</Button></Space>}
      >
        {editor?.type === 'template' && (
          <Form form={templateForm} name="experienceTemplateForm" layout="vertical" scrollToFirstError>
            <Row gutter={12}>
              <Col span={12}><Form.Item label="模板代碼" name="code"><Input placeholder="留空自動生成" /></Form.Item></Col>
              <Col span={12}><Form.Item label="模板類型" name="templateType" rules={[{ required: true }]}><Select options={templateTypeOptions} /></Form.Item></Col>
              <Col span={12}><Form.Item label="分類" name="category"><Input /></Form.Item></Col>
              <Col span={12}><Form.Item label="風險等級" name="riskLevel"><Select options={[{ label: '普通', value: 'normal' }, { label: '高風險', value: 'high' }]} /></Form.Item></Col>
              <Col span={24}><Form.Item label="繁體名稱" name="nameZh" rules={[{ required: true }]}><Input /></Form.Item></Col>
              <Col span={24}><Form.Item label="繁體摘要" name="summaryZh"><Input.TextArea rows={3} /></Form.Item></Col>
              <Col span={12}><Form.Item label="狀態" name="status"><Select options={statusOptions} /></Form.Item></Col>
              <Col span={12}><Form.Item label="排序" name="sortOrder"><InputNumber style={{ width: '100%' }} /></Form.Item></Col>
            </Row>
            <Collapse
              items={[{
                key: 'advanced',
                label: '進階 JSON',
                children: (
                  <>
                    <Form.Item label="模板配置 JSON" name="configJson" rules={[jsonRule(true)]}><Input.TextArea rows={6} /></Form.Item>
                    <Form.Item label="表單 Schema JSON" name="schemaJson" rules={[jsonRule(true)]}><Input.TextArea rows={5} /></Form.Item>
                  </>
                ),
              }]}
            />
          </Form>
        )}

        {editor?.type === 'flow' && (
          <Form form={flowForm} name="experienceFlowForm" layout="vertical" scrollToFirstError>
            <Card
              size="small"
              title="地點預設體驗快速套用"
              style={{ marginBottom: 16 }}
              extra={<Button onClick={applyAmaTempleFlowPreset}>套用媽閣廟預設</Button>}
            >
              <Text type="secondary">
                一鍵建立媽閣廟 POI 預設體驗流程骨架，後續可在時間線逐步加入介紹、路線規劃、靠近全屏媒體、打卡任務、拾取物、隱藏成就、獎勵與稱號。
              </Text>
            </Card>
            <Row gutter={12}>
              <Col span={12}><Form.Item label="流程代碼" name="code"><Input placeholder="留空自動生成" /></Form.Item></Col>
              <Col span={12}><Form.Item label="流程類型" name="flowType"><Select options={flowTypeOptions} /></Form.Item></Col>
              <Col span={12}><Form.Item label="啟動模式" name="mode"><Select options={[{ label: '自然 Walk-in', value: 'walk_in' }, { label: '故事線模式', value: 'story_mode' }, { label: '手動觸發', value: 'manual' }]} /></Form.Item></Col>
              <Col span={12}><Form.Item label="繁體名稱" name="nameZh" rules={[{ required: true }]}><Input /></Form.Item></Col>
              <Col span={24}><Form.Item label="繁體說明" name="descriptionZh"><Input.TextArea rows={3} /></Form.Item></Col>
              <Col span={12}><Form.Item label="狀態" name="status"><Select options={statusOptions} /></Form.Item></Col>
              <Col span={12}><Form.Item label="排序" name="sortOrder"><InputNumber style={{ width: '100%' }} /></Form.Item></Col>
            </Row>
            <Collapse
              items={[{
                key: 'advanced',
                label: '故事模式與地圖策略 JSON',
                children: (
                  <>
                    <Form.Item label="地圖策略 JSON" name="mapPolicyJson" rules={[jsonRule(true)]}><Input.TextArea rows={6} /></Form.Item>
                    <Form.Item label="進階配置 JSON" name="advancedConfigJson" rules={[jsonRule(true)]}><Input.TextArea rows={6} /></Form.Item>
                  </>
                ),
              }]}
            />
          </Form>
        )}

        {editor?.type === 'step' && (
          <Form form={stepForm} name="experienceStepForm" layout="vertical" scrollToFirstError>
            <Card size="small" title="媽閣廟互動步驟預設" style={{ marginBottom: 16 }}>
              <Space direction="vertical" size="small" style={{ width: '100%' }}>
                <Text type="secondary">
                  這些按鈕會回填結構化欄位與版本化 JSON，讓操作員不用手寫完整條件與效果 payload。
                </Text>
                <Space wrap>
                  {amaTempleStepPresets.map((preset) => (
                    <Button key={preset.key} onClick={() => applyAmaTempleStepPreset(preset.key)}>
                      {preset.label}
                    </Button>
                  ))}
                </Space>
                <Space wrap>
                  {amaTempleStepPresets.map((preset) => (
                    <Tag key={preset.key}>{preset.description}</Tag>
                  ))}
                </Space>
              </Space>
            </Card>
            <Row gutter={12}>
              <Col span={12}><Form.Item label="步驟代碼" name="stepCode"><Input placeholder="留空自動生成" /></Form.Item></Col>
              <Col span={12}><Form.Item label="步驟類型" name="stepType" rules={[{ required: true }]}><Select options={stepTypeOptions} /></Form.Item></Col>
              <Col span={12}><Form.Item label="套用模板" name="templateId"><Select allowClear showSearch options={templateOptions} /></Form.Item></Col>
              <Col span={12}><Form.Item label="觸發方式" name="triggerType"><Select options={triggerTypeOptions} /></Form.Item></Col>
              <Col span={24}><Form.Item label="繁體步驟名" name="stepNameZh" rules={[{ required: true }]}><Input /></Form.Item></Col>
              <Col span={24}><Form.Item label="繁體說明" name="descriptionZh"><Input.TextArea rows={3} /></Form.Item></Col>
              <Col span={8}><Form.Item label="探索權重" name="explorationWeightLevel"><Select options={weightOptions} /></Form.Item></Col>
              <Col span={8}><Form.Item label="必需完成" name="requiredForCompletion" valuePropName="checked"><Switch /></Form.Item></Col>
              <Col span={8}><Form.Item label="狀態" name="status"><Select options={statusOptions} /></Form.Item></Col>
            </Row>
            <Collapse
              items={[{
                key: 'advanced',
                label: '條件、效果與獎勵 JSON',
                children: (
                  <>
                    <Form.Item label="觸發配置 JSON" name="triggerConfigJson" rules={[jsonRule(true)]}><Input.TextArea rows={5} /></Form.Item>
                    <Form.Item label="出現 / 生效條件 JSON" name="conditionConfigJson" rules={[jsonRule(true)]}><Input.TextArea rows={5} /></Form.Item>
                    <Form.Item label="觸發效果 JSON" name="effectConfigJson" rules={[jsonRule(true)]}><Input.TextArea rows={5} /></Form.Item>
                    <Form.Item label="獎勵規則 ID JSON" name="rewardRuleIdsJson" rules={[jsonRule(false)]}><Input.TextArea rows={3} /></Form.Item>
                  </>
                ),
              }]}
            />
          </Form>
        )}

        {editor?.type === 'binding' && (
          <Form form={bindingForm} name="experienceBindingForm" layout="vertical" scrollToFirstError>
            <Row gutter={12}>
              <Col span={12}><Form.Item label="主體類型" name="ownerType" rules={[{ required: true }]}><Select options={ownerTypeOptions} /></Form.Item></Col>
              <Col span={12}><Form.Item label="主體 ID" name="ownerId"><InputNumber style={{ width: '100%' }} /></Form.Item></Col>
              <Col span={12}><Form.Item label="主體代碼" name="ownerCode"><Input /></Form.Item></Col>
              <Col span={12}><Form.Item label="綁定角色" name="bindingRole"><Select options={bindingRoleOptions} /></Form.Item></Col>
              <Col span={24}><Form.Item label="體驗流程" name="flowId" rules={[{ required: true }]}><Select showSearch options={flowOptions} /></Form.Item></Col>
              <Col span={8}><Form.Item label="優先級" name="priority"><InputNumber style={{ width: '100%' }} /></Form.Item></Col>
              <Col span={8}><Form.Item label="繼承策略" name="inheritPolicy"><Select options={[{ label: '繼承', value: 'inherit' }, { label: '覆寫', value: 'override' }]} /></Form.Item></Col>
              <Col span={8}><Form.Item label="狀態" name="status"><Select options={statusOptions} /></Form.Item></Col>
            </Row>
          </Form>
        )}

        {editor?.type === 'override' && (
          <Form form={overrideForm} name="experienceOverrideForm" layout="vertical" scrollToFirstError>
            <Row gutter={12}>
              <Col span={12}><Form.Item label="覆寫擁有者類型" name="ownerType" rules={[{ required: true }]}><Select options={ownerTypeOptions} /></Form.Item></Col>
              <Col span={12}><Form.Item label="覆寫擁有者 ID" name="ownerId" rules={[{ required: true }]}><InputNumber style={{ width: '100%' }} /></Form.Item></Col>
              <Col span={12}><Form.Item label="目標主體類型" name="targetOwnerType"><Select allowClear options={ownerTypeOptions} /></Form.Item></Col>
              <Col span={12}><Form.Item label="目標主體 ID" name="targetOwnerId"><InputNumber style={{ width: '100%' }} /></Form.Item></Col>
              <Col span={12}><Form.Item label="目標步驟代碼" name="targetStepCode"><Input /></Form.Item></Col>
              <Col span={12}><Form.Item label="覆寫方式" name="overrideMode"><Select options={[{ label: '繼承', value: 'inherit' }, { label: '關閉', value: 'disable' }, { label: '替換', value: 'replace' }, { label: '追加', value: 'append' }]} /></Form.Item></Col>
              <Col span={12}><Form.Item label="替換 / 追加步驟 ID" name="replacementStepId"><InputNumber style={{ width: '100%' }} /></Form.Item></Col>
              <Col span={12}><Form.Item label="狀態" name="status"><Select options={statusOptions} /></Form.Item></Col>
              <Col span={24}><Form.Item label="覆寫配置 JSON" name="overrideConfigJson" rules={[jsonRule(true)]}><Input.TextArea rows={6} /></Form.Item></Col>
            </Row>
          </Form>
        )}

        {editor?.type === 'element' && (
          <Form form={elementForm} name="explorationElementForm" layout="vertical" scrollToFirstError>
            <Row gutter={12}>
              <Col span={12}><Form.Item label="元素代碼" name="elementCode"><Input placeholder="留空自動生成" /></Form.Item></Col>
              <Col span={12}><Form.Item label="元素類型" name="elementType" rules={[{ required: true }]}><Input /></Form.Item></Col>
              <Col span={12}><Form.Item label="主體類型" name="ownerType" rules={[{ required: true }]}><Select options={ownerTypeOptions} /></Form.Item></Col>
              <Col span={12}><Form.Item label="主體 ID" name="ownerId"><InputNumber style={{ width: '100%' }} /></Form.Item></Col>
              <Col span={24}><Form.Item label="繁體名稱" name="titleZh" rules={[{ required: true }]}><Input /></Form.Item></Col>
              <Col span={8}><Form.Item label="權重級別" name="weightLevel"><Select options={weightOptions} /></Form.Item></Col>
              <Col span={8}><Form.Item label="權重值" name="weightValue"><InputNumber style={{ width: '100%' }} /></Form.Item></Col>
              <Col span={8}><Form.Item label="計入探索度" name="includeInExploration" valuePropName="checked"><Switch /></Form.Item></Col>
              <Col span={12}><Form.Item label="城市 ID" name="cityId"><InputNumber style={{ width: '100%' }} /></Form.Item></Col>
              <Col span={12}><Form.Item label="子地圖 ID" name="subMapId"><InputNumber style={{ width: '100%' }} /></Form.Item></Col>
              <Col span={12}><Form.Item label="故事線 ID" name="storylineId"><InputNumber style={{ width: '100%' }} /></Form.Item></Col>
              <Col span={12}><Form.Item label="章節 ID" name="storyChapterId"><InputNumber style={{ width: '100%' }} /></Form.Item></Col>
              <Col span={12}><Form.Item label="狀態" name="status"><Select options={statusOptions} /></Form.Item></Col>
              <Col span={12}><Form.Item label="排序" name="sortOrder"><InputNumber style={{ width: '100%' }} /></Form.Item></Col>
              <Col span={24}><Form.Item label="元資料 JSON" name="metadataJson" rules={[jsonRule(true)]}><Input.TextArea rows={5} /></Form.Item></Col>
            </Row>
          </Form>
        )}
      </Drawer>
    </PageContainer>
  );
};

function drawerTitle(editor: EditorState) {
  if (!editor) return '';
  const prefix = editor.item ? '編輯' : '新增';
  switch (editor.type) {
    case 'template':
      return `${prefix}互動與任務模板`;
    case 'flow':
      return `${prefix}體驗流程`;
    case 'step':
      return `${prefix}流程步驟`;
    case 'binding':
      return `${prefix}流程綁定`;
    case 'override':
      return `${prefix}覆寫規則`;
    case 'element':
      return `${prefix}探索元素`;
    default:
      return '';
  }
}

export default ExperienceOrchestrationWorkbench;
