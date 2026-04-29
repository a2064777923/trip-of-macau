import React, { useEffect, useMemo, useState } from 'react';
import { PageContainer } from '@ant-design/pro-components';
import { useNavigate, useParams } from 'react-router-dom';
import {
  Alert,
  App as AntdApp,
  Badge,
  Button,
  Card,
  Col,
  Collapse,
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
  StarOutlined,
} from '@ant-design/icons';
import {
  createAdminPoiExperienceStep,
  deleteAdminPoiExperienceStep,
  getAdminPoiExperienceDefault,
  getAdminPois,
  getAdminRewardRules,
  saveAdminPoiExperienceStepAsTemplate,
  updateAdminPoiExperienceStep,
  upsertAdminPoiExperienceDefaultFlow,
} from '../../services/api';
import type {
  AdminPoiExperienceSnapshot,
  AdminPoiExperienceStep,
  AdminPoiExperienceStructuredStepPayload,
  AdminRewardRuleItem,
} from '../../types/admin';
import SpatialAssetPickerField from '../../components/spatial/SpatialAssetPickerField';
import { focusFirstInvalidField } from '../../utils/formErrorFeedback';
import './index.scss';

const { Text, Title, Paragraph } = Typography;

const FORM_NAME = 'poiExperienceStepForm';

const statusOptions = [
  { label: '編輯中', value: 'draft' },
  { label: '已發佈', value: 'published' },
  { label: '已封存', value: 'archived' },
];

const stepTypeOptions = [
  { label: '點擊介紹', value: 'intro_modal' },
  { label: '路線導覽', value: 'route_guidance' },
  { label: '抵達媒體', value: 'proximity_media' },
  { label: '打卡任務', value: 'checkin_task' },
  { label: '拾取物', value: 'pickup' },
  { label: '隱藏挑戰', value: 'hidden_challenge' },
  { label: '完成獎勵', value: 'reward_grant' },
  { label: '自定義', value: 'custom' },
];

const triggerTypeOptions = [
  { label: '手動', value: 'manual' },
  { label: '點擊', value: 'tap' },
  { label: '點擊動作', value: 'tap_action' },
  { label: '靠近範圍', value: 'proximity' },
  { label: '媒體播放完成', value: 'media_finished' },
  { label: '停留', value: 'dwell' },
  { label: '內容完成', value: 'content_complete' },
  { label: '任務完成', value: 'task_complete' },
  { label: '拾取完成', value: 'pickup_complete' },
];

const triggerPresetOptions = [
  { label: '恆常可點擊', value: 'always_tap' },
  { label: '點擊前往探索', value: 'tap_start_explore' },
  { label: '靠近範圍出現', value: 'nearby_radius' },
  { label: '停留指定秒數', value: 'dwell_seconds' },
  { label: '前置步驟完成後', value: 'after_step' },
];

const conditionPresetOptions = [
  { label: '無條件', value: 'always' },
  { label: '每位用戶一次', value: 'once_per_user' },
  { label: '指定時間窗', value: 'time_window' },
  { label: '需要指定物品', value: 'required_items' },
  { label: '需要指定徽章 / 稱號', value: 'required_badges' },
];

const effectPresetOptions = [
  { label: '圖文彈窗', value: 'modal' },
  { label: '路線與推薦卡', value: 'route_cards' },
  { label: '全屏媒體播放', value: 'fullscreen_media' },
  { label: '派發任務', value: 'release_tasks' },
  { label: '生成拾取物', value: 'spawn_pickups' },
  { label: '發放獎勵 / 稱號', value: 'grant_reward' },
];

const routeCardTypeOptions = [
  { label: '交通方式', value: 'transport' },
  { label: '推薦故事線', value: 'storyline' },
  { label: '附近地點', value: 'nearby_poi' },
  { label: '途經地點', value: 'waypoint_poi' },
  { label: '目的地卡片', value: 'destination' },
];

const weightOptions = [
  { label: '極少量', value: 'tiny' },
  { label: '少量', value: 'small' },
  { label: '中量', value: 'medium' },
  { label: '大量', value: 'large' },
  { label: '核心', value: 'core' },
];

const groupDefs = [
  { key: 'tap', title: '點擊與前往', types: ['intro_modal', 'route_guidance'] },
  { key: 'arrival', title: '抵達與媒體', types: ['proximity_media'] },
  { key: 'task', title: '打卡與任務', types: ['checkin_task'] },
  { key: 'pickup', title: '拾取與隱藏', types: ['pickup', 'hidden_challenge'] },
  { key: 'reward', title: '完成與獎勵', types: ['reward_grant'] },
];

const quickAddPresets: Array<{
  code: string;
  label: string;
  group: string;
  payload: AdminPoiExperienceStructuredStepPayload;
}> = [
  {
    code: 'tap_intro',
    label: '點擊介紹彈窗',
    group: '點擊與前往',
    payload: {
      stepCode: 'tap_intro',
      stepType: 'intro_modal',
      stepNameZh: '點擊介紹彈窗',
      stepNameZht: '點擊介紹彈窗',
      descriptionZh: '點擊 POI 後顯示圖文簡介與前往探索按鈕。',
      triggerType: 'tap',
      triggerPreset: 'always_tap',
      conditionPreset: 'always',
      effectPreset: 'modal',
      modalTitle: '地點導覽',
      modalBody: '展示此地點的故事化簡介，並引導用戶前往探索。',
      primaryActionLabel: '前往探索該地',
      explorationWeightLevel: 'tiny',
      requiredForCompletion: false,
      status: 'draft',
      sortOrder: 10,
    },
  },
  {
    code: 'start_route_guidance',
    label: '路線規劃與推薦',
    group: '點擊與前往',
    payload: {
      stepCode: 'start_route_guidance',
      stepType: 'route_guidance',
      stepNameZh: '路線規劃與推薦',
      stepNameZht: '路線規劃與推薦',
      descriptionZh: '點擊前往後設定目的地，展示交通、故事線、附近與途經推薦。',
      triggerType: 'tap_action',
      triggerPreset: 'tap_start_explore',
      tapActionCode: 'start_explore',
      conditionPreset: 'after_step',
      afterStepCode: 'tap_intro',
      effectPreset: 'route_cards',
      routeCardTypes: ['destination', 'transport', 'storyline', 'nearby_poi', 'waypoint_poi'],
      explorationWeightLevel: 'tiny',
      requiredForCompletion: false,
      status: 'draft',
      sortOrder: 20,
    },
  },
  {
    code: 'arrival_intro_media',
    label: '抵達全屏媒體',
    group: '抵達與媒體',
    payload: {
      stepCode: 'arrival_intro_media',
      stepType: 'proximity_media',
      stepNameZh: '抵達全屏媒體',
      stepNameZht: '抵達全屏媒體',
      descriptionZh: '用戶進入指定半徑後播放全屏介紹動畫、影片或聲景。',
      triggerType: 'proximity',
      triggerPreset: 'nearby_radius',
      triggerRadiusMeters: 50,
      conditionPreset: 'once_per_user',
      oncePerUser: true,
      effectPreset: 'fullscreen_media',
      explorationWeightLevel: 'small',
      requiredForCompletion: false,
      status: 'draft',
      sortOrder: 30,
    },
  },
  {
    code: 'release_checkin_tasks',
    label: '打卡任務釋放',
    group: '打卡與任務',
    payload: {
      stepCode: 'release_checkin_tasks',
      stepType: 'checkin_task',
      stepNameZh: '打卡任務釋放',
      stepNameZht: '打卡任務釋放',
      descriptionZh: '介紹媒體完成後派發拍照、互動遊戲或現場任務。',
      triggerType: 'media_finished',
      triggerPreset: 'after_step',
      afterStepCode: 'arrival_intro_media',
      conditionPreset: 'once_per_user',
      effectPreset: 'release_tasks',
      taskCodes: ['gate_photo', 'cyber_incense'],
      explorationWeightLevel: 'medium',
      requiredForCompletion: true,
      status: 'draft',
      sortOrder: 40,
    },
  },
  {
    code: 'pickup_side_clues',
    label: '支線拾取物',
    group: '拾取與隱藏',
    payload: {
      stepCode: 'pickup_side_clues',
      stepType: 'pickup',
      stepNameZh: '支線拾取物',
      stepNameZht: '支線拾取物',
      descriptionZh: '在 POI 周邊生成可拾取線索、信物或疊加物。',
      triggerType: 'tap',
      triggerPreset: 'after_step',
      afterStepCode: 'release_checkin_tasks',
      conditionPreset: 'required_items',
      effectPreset: 'spawn_pickups',
      pickupCodes: ['poi_token', 'rare_clue', 'historical_fragment'],
      explorationWeightLevel: 'large',
      requiredForCompletion: false,
      status: 'draft',
      sortOrder: 50,
    },
  },
  {
    code: 'hidden_dwell_achievement',
    label: '停留隱藏成就',
    group: '拾取與隱藏',
    payload: {
      stepCode: 'hidden_dwell_achievement',
      stepType: 'hidden_challenge',
      stepNameZh: '停留隱藏成就',
      stepNameZht: '停留隱藏成就',
      descriptionZh: '在 POI 指定範圍停留足夠時間後觸發隱藏成就或挑戰。',
      triggerType: 'dwell',
      triggerPreset: 'dwell_seconds',
      triggerRadiusMeters: 30,
      dwellSeconds: 1800,
      conditionPreset: 'once_per_user',
      effectPreset: 'grant_reward',
      rewardSummary: '發放隱藏稱號與核心探索元素。',
      explorationWeightLevel: 'core',
      requiredForCompletion: false,
      status: 'draft',
      sortOrder: 60,
    },
  },
  {
    code: 'completion_reward_title',
    label: '完成獎勵與稱號',
    group: '完成與獎勵',
    payload: {
      stepCode: 'completion_reward_title',
      stepType: 'reward_grant',
      stepNameZh: '完成獎勵與稱號',
      stepNameZht: '完成獎勵與稱號',
      descriptionZh: '完成打卡、任務或互動後統一發放金币、徽章、稱號或遊戲內物品。',
      triggerType: 'task_complete',
      triggerPreset: 'after_step',
      afterStepCode: 'release_checkin_tasks',
      conditionPreset: 'once_per_user',
      effectPreset: 'grant_reward',
      rewardSummary: '完成地點探索後發放基礎獎勵與稱號。',
      explorationWeightLevel: 'core',
      requiredForCompletion: true,
      status: 'draft',
      sortOrder: 70,
    },
  },
];

function pickPoiName(poi?: AdminPoiExperienceSnapshot['poi'] | null) {
  return poi?.nameZht || poi?.nameZh || poi?.nameEn || poi?.namePt || poi?.code || '';
}

function parseObjectJson(value?: string) {
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

function stepToFormValues(step: AdminPoiExperienceStep): AdminPoiExperienceStructuredStepPayload {
  const trigger = parseObjectJson(step.triggerConfigJson);
  const condition = parseObjectJson(step.conditionConfigJson);
  const effect = parseObjectJson(step.effectConfigJson);
  return {
    stepCode: step.stepCode,
    stepType: step.stepType,
    templateId: step.templateId,
    stepNameZh: step.stepNameZh,
    stepNameZht: step.stepNameZht || step.stepNameZh,
    stepNameEn: step.stepNameEn,
    stepNamePt: step.stepNamePt,
    descriptionZh: step.descriptionZh,
    descriptionZht: step.descriptionZht || step.descriptionZh,
    descriptionEn: step.descriptionEn,
    descriptionPt: step.descriptionPt,
    triggerType: step.triggerType,
    mediaAssetId: step.mediaAssetId,
    explorationWeightLevel: step.explorationWeightLevel || 'small',
    requiredForCompletion: step.requiredForCompletion,
    status: step.status || 'draft',
    sortOrder: step.sortOrder || 0,
    triggerPreset: trigger.preset,
    triggerRadiusMeters: trigger.radiusMeters,
    dwellSeconds: trigger.dwellSeconds,
    tapActionCode: trigger.tapActionCode,
    afterStepCode: trigger.afterStepCode || step.inheritKey,
    conditionPreset: condition.preset,
    oncePerUser: condition.oncePerUser,
    timeWindowStart: condition.timeWindowStart,
    timeWindowEnd: condition.timeWindowEnd,
    requiredItemCodes: condition.requiredItemCodes || [],
    requiredBadgeCodes: condition.requiredBadgeCodes || [],
    effectPreset: effect.preset,
    modalTitle: effect.modalTitle,
    modalBody: effect.modalBody,
    primaryActionLabel: effect.primaryActionLabel,
    routeCardTypes: effect.routeCardTypes || [],
    taskCodes: effect.taskCodes || [],
    pickupCodes: effect.pickupCodes || [],
    rewardRuleIds: parseRewardRuleIds(step.rewardRuleIdsJson),
    rewardSummary: effect.rewardSummary,
    fullScreenMediaAssetId: effect.fullScreenMediaAssetId,
    audioAssetId: effect.audioAssetId,
    advancedJsonEnabled: false,
    advancedTriggerConfigJson: step.triggerConfigJson,
    advancedConditionConfigJson: step.conditionConfigJson,
    advancedEffectConfigJson: step.effectConfigJson,
  };
}

function normalizeStepSubmitPayload(
  values: AdminPoiExperienceStructuredStepPayload,
): AdminPoiExperienceStructuredStepPayload {
  if (values.advancedJsonEnabled) {
    return values;
  }
  return {
    ...values,
    advancedTriggerConfigJson: undefined,
    advancedConditionConfigJson: undefined,
    advancedEffectConfigJson: undefined,
  };
}

function tagStatus(status?: string) {
  if (status === 'published') {
    return <Tag color="green">已發佈</Tag>;
  }
  if (status === 'archived') {
    return <Tag>已封存</Tag>;
  }
  return <Tag color="gold">編輯中</Tag>;
}

const POIExperienceWorkbench: React.FC = () => {
  const { poiId: poiIdParam } = useParams();
  const navigate = useNavigate();
  const { message } = AntdApp.useApp();
  const [form] = Form.useForm<AdminPoiExperienceStructuredStepPayload>();
  const [pois, setPois] = useState<AdminPoiExperienceSnapshot['poi'][]>([]);
  const [rewardRules, setRewardRules] = useState<AdminRewardRuleItem[]>([]);
  const [selectedPoiId, setSelectedPoiId] = useState<number | undefined>(
    poiIdParam ? Number(poiIdParam) : undefined,
  );
  const [snapshot, setSnapshot] = useState<AdminPoiExperienceSnapshot | null>(null);
  const [selectedStepId, setSelectedStepId] = useState<number | null>(null);
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);

  const selectedStep = useMemo(
    () => snapshot?.steps.find((step) => step.id === selectedStepId) || null,
    [snapshot?.steps, selectedStepId],
  );
  const advancedJsonEnabled = Form.useWatch('advancedJsonEnabled', form);

  const templateOptions = useMemo(
    () =>
      (snapshot?.templates || []).map((template) => ({
        label: `${template.nameZht || template.nameZh || template.code} · ${template.templateType}`,
        value: template.id,
      })),
    [snapshot?.templates],
  );

  const rewardRuleOptions = useMemo(
    () =>
      rewardRules.map((rule) => ({
        label: `${rule.nameZht || rule.nameZh || rule.code} · #${rule.id}`,
        value: rule.id,
      })),
    [rewardRules],
  );

  const loadPois = async () => {
    const response = await getAdminPois({ pageNum: 1, pageSize: 300 });
    if (response.success && response.data) {
      setPois(
        (response.data.list || []).map((poi) => ({
          ...poi,
          poiId: poi.poiId,
          triggerRadius: undefined,
          manualCheckinRadius: undefined,
          staySeconds: undefined,
        })),
      );
    }
  };

  const loadRewardRules = async () => {
    const response = await getAdminRewardRules({ pageNum: 1, pageSize: 200 });
    if (response.success && response.data) {
      setRewardRules(response.data.list || []);
    }
  };

  const loadSnapshot = async (poiId: number) => {
    setLoading(true);
    try {
      const response = await getAdminPoiExperienceDefault(poiId);
      if (!response.success || !response.data) {
        message.error(response.message || '無法載入 POI 地點體驗');
        return;
      }
      setSnapshot(response.data);
      setSelectedStepId(response.data.steps[0]?.id || null);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void loadPois();
    void loadRewardRules();
  }, []);

  useEffect(() => {
    const routePoiId = poiIdParam ? Number(poiIdParam) : undefined;
    if (routePoiId && routePoiId !== selectedPoiId) {
      setSelectedPoiId(routePoiId);
    }
  }, [poiIdParam]);

  useEffect(() => {
    if (selectedPoiId) {
      void loadSnapshot(selectedPoiId);
    }
  }, [selectedPoiId]);

  useEffect(() => {
    if (selectedStep) {
      form.setFieldsValue(stepToFormValues(selectedStep));
    } else {
      form.resetFields();
    }
  }, [selectedStep, form]);

  const handlePoiChange = (poiId: number) => {
    setSelectedPoiId(poiId);
    navigate(`/space/pois/${poiId}/experience`);
  };

  const handleQuickAdd = async (payload: AdminPoiExperienceStructuredStepPayload) => {
    if (!selectedPoiId) {
      message.warning('請先選擇 POI');
      return;
    }
    setSaving(true);
    try {
      const response = await createAdminPoiExperienceStep(selectedPoiId, payload);
      if (!response.success || !response.data) {
        throw new Error(response.message || '新增步驟失敗');
      }
      message.success(`已添加 ${payload.stepCode}`);
      await loadSnapshot(selectedPoiId);
      setSelectedStepId(response.data.id);
    } catch (error) {
      message.error(error instanceof Error ? error.message : '新增步驟失敗');
    } finally {
      setSaving(false);
    }
  };

  const handleNewCustomStep = () => {
    setSelectedStepId(null);
    form.setFieldsValue({
      stepCode: 'custom_step',
      stepType: 'custom',
      stepNameZh: '自定義體驗步驟',
      stepNameZht: '自定義體驗步驟',
      triggerType: 'manual',
      triggerPreset: 'always_tap',
      conditionPreset: 'always',
      effectPreset: 'modal',
      explorationWeightLevel: 'small',
      requiredForCompletion: false,
      status: 'draft',
      sortOrder: (snapshot?.steps.length || 0) * 10 + 10,
      advancedJsonEnabled: false,
    });
  };

  const handleSaveStep = async () => {
    if (!selectedPoiId) {
      message.warning('請先選擇 POI');
      return;
    }
    setSaving(true);
    try {
      const values = normalizeStepSubmitPayload(await form.validateFields());
      const response = selectedStep
        ? await updateAdminPoiExperienceStep(selectedPoiId, selectedStep.id, values)
        : await createAdminPoiExperienceStep(selectedPoiId, values);
      if (!response.success || !response.data) {
        throw new Error(response.message || '保存步驟失敗');
      }
      message.success('步驟已保存');
      await loadSnapshot(selectedPoiId);
      setSelectedStepId(response.data.id);
    } catch (error) {
      focusFirstInvalidField(form, FORM_NAME, error);
      if (error instanceof Error) {
        message.error(error.message);
      }
    } finally {
      setSaving(false);
    }
  };

  const handleDeleteStep = async () => {
    if (!selectedPoiId || !selectedStep) {
      return;
    }
    const response = await deleteAdminPoiExperienceStep(selectedPoiId, selectedStep.id);
    if (!response.success) {
      message.error(response.message || '刪除步驟失敗');
      return;
    }
    message.success('步驟已刪除');
    await loadSnapshot(selectedPoiId);
  };

  const handleSaveTemplate = async () => {
    if (!selectedPoiId || !selectedStep) {
      message.warning('請先選擇一個已保存的步驟');
      return;
    }
    const response = await saveAdminPoiExperienceStepAsTemplate(selectedPoiId, selectedStep.id, {
      code: `tpl_poi_${selectedStep.stepCode}`,
      nameZh: `${selectedStep.stepNameZh}模板`,
      nameZht: `${selectedStep.stepNameZht || selectedStep.stepNameZh}模板`,
      summaryZh: selectedStep.descriptionZh || '由 POI 地點體驗工作台保存的可重用模板。',
      status: 'draft',
    });
    if (!response.success) {
      message.error(response.message || '保存模板失敗');
      return;
    }
    message.success('已保存為可重用模板');
    await loadSnapshot(selectedPoiId);
  };

  const handlePublishFlow = async () => {
    if (!selectedPoiId || !snapshot) {
      return;
    }
    const response = await upsertAdminPoiExperienceDefaultFlow(selectedPoiId, {
      code: snapshot.flow.code,
      nameZh: snapshot.flow.nameZh,
      nameZht: snapshot.flow.nameZht || snapshot.flow.nameZh,
      descriptionZh: snapshot.flow.descriptionZh,
      descriptionZht: snapshot.flow.descriptionZht,
      mapPolicyJson: snapshot.flow.mapPolicyJson,
      advancedConfigJson: snapshot.flow.advancedConfigJson,
      status: 'published',
      sortOrder: snapshot.flow.sortOrder,
    });
    if (!response.success || !response.data) {
      message.error(response.message || '發布流程失敗');
      return;
    }
    setSnapshot(response.data);
    message.success('流程已標記為已發佈');
  };

  const groupedSteps = useMemo(
    () =>
      groupDefs.map((group) => ({
        ...group,
        steps: (snapshot?.steps || []).filter((step) => group.types.includes(step.stepType)),
      })),
    [snapshot?.steps],
  );

  return (
    <PageContainer
      title="POI 地點體驗工作台"
      subTitle="配置自然 walk-in、點擊前往、抵達媒體、打卡任務、拾取物、隱藏成就與完成獎勵。"
      className="poi-experience-workbench"
    >
      <Spin spinning={loading}>
        <Row gutter={[16, 16]}>
          <Col xs={24} xl={6}>
            <Space direction="vertical" size="middle" style={{ width: '100%' }}>
              <Card title="POI 選擇">
                <Select
                  showSearch
                  allowClear
                  placeholder="選擇要編排體驗的 POI"
                  value={selectedPoiId}
                  style={{ width: '100%' }}
                  optionFilterProp="label"
                  options={pois.map((poi) => ({
                    label: `${pickPoiName(poi)} (${poi.code})`,
                    value: poi.poiId,
                  }))}
                  onChange={handlePoiChange}
                />
              </Card>

              <Card
                title="流程摘要"
                extra={
                  snapshot ? (
                    <Button size="small" icon={<ReloadOutlined />} onClick={() => void loadSnapshot(snapshot.poi.poiId)}>
                      重新載入
                    </Button>
                  ) : null
                }
              >
                {snapshot ? (
                  <Space direction="vertical" size="small" style={{ width: '100%' }}>
                    <Title level={5} style={{ margin: 0 }}>
                      {pickPoiName(snapshot.poi)}
                    </Title>
                    <Text type="secondary">流程：{snapshot.flow.code}</Text>
                    <Text type="secondary">公開路徑：{snapshot.publicRuntimePath}</Text>
                    <Space wrap>
                      <Tag color="purple">{snapshot.flow.flowType}</Tag>
                      <Tag color="blue">{snapshot.flow.mode}</Tag>
                      {tagStatus(snapshot.flow.status)}
                    </Space>
                    <Button type="primary" block onClick={() => void handlePublishFlow()}>
                      發佈 POI 預設流程
                    </Button>
                  </Space>
                ) : (
                  <Empty description="請先選擇 POI" />
                )}
              </Card>

              <Card title="快速添加">
                <Space direction="vertical" size="small" style={{ width: '100%' }}>
                  {quickAddPresets.map((preset) => (
                    <Button
                      key={preset.code}
                      block
                      icon={<PlusOutlined />}
                      loading={saving}
                      onClick={() => void handleQuickAdd(preset.payload)}
                    >
                      {preset.label}
                    </Button>
                  ))}
                  <Button block onClick={handleNewCustomStep}>
                    新增自定義步驟
                  </Button>
                </Space>
              </Card>

              <Card title="時間線">
                <Timeline
                  items={(snapshot?.steps || []).map((step) => ({
                    color: selectedStepId === step.id ? 'blue' : step.status === 'published' ? 'green' : 'gray',
                    children: (
                      <button
                        type="button"
                        className={`poi-experience-timeline-item${selectedStepId === step.id ? ' is-active' : ''}`}
                        onClick={() => setSelectedStepId(step.id)}
                      >
                        <Text strong>{step.stepNameZht || step.stepNameZh}</Text>
                        <Text type="secondary">{step.stepCode}</Text>
                      </button>
                    ),
                  }))}
                />
              </Card>
            </Space>
          </Col>

          <Col xs={24} xl={10}>
            <Card title="視覺編排區">
              {!snapshot ? (
                <Empty description="選擇 POI 後可開始編排地點體驗" />
              ) : (
                <Space direction="vertical" size="middle" style={{ width: '100%' }}>
                  {groupedSteps.map((group) => (
                    <Card
                      key={group.key}
                      size="small"
                      title={group.title}
                      className="poi-experience-group-card"
                      extra={<Badge count={group.steps.length} showZero />}
                    >
                      {group.steps.length ? (
                        <Space direction="vertical" size="small" style={{ width: '100%' }}>
                          {group.steps.map((step) => (
                            <button
                              type="button"
                              key={step.id}
                              className={`poi-experience-step-card${selectedStepId === step.id ? ' is-active' : ''}`}
                              onClick={() => setSelectedStepId(step.id)}
                            >
                              <Space direction="vertical" size={2} style={{ width: '100%' }}>
                                <Space wrap>
                                  <Text strong>{step.stepNameZht || step.stepNameZh}</Text>
                                  <Tag>{step.stepType}</Tag>
                                  {tagStatus(step.status)}
                                </Space>
                                <Text type="secondary">{step.descriptionZht || step.descriptionZh || '尚未填寫說明'}</Text>
                              </Space>
                            </button>
                          ))}
                        </Space>
                      ) : (
                        <Text type="secondary">尚未添加此類步驟，可從左側快速添加。</Text>
                      )}
                    </Card>
                  ))}
                </Space>
              )}
            </Card>
          </Col>

          <Col xs={24} xl={8}>
            <Card
              title={selectedStep ? `編輯步驟：${selectedStep.stepNameZht || selectedStep.stepNameZh}` : '新增 / 編輯步驟'}
              extra={
                <Space>
                  {selectedStep ? (
                    <Popconfirm title="確定刪除此步驟？" onConfirm={() => void handleDeleteStep()}>
                      <Button danger icon={<DeleteOutlined />} />
                    </Popconfirm>
                  ) : null}
                  <Button icon={<StarOutlined />} disabled={!selectedStep} onClick={() => void handleSaveTemplate()}>
                    保存為模板
                  </Button>
                  <Button type="primary" icon={<SaveOutlined />} loading={saving} onClick={() => void handleSaveStep()}>
                    保存
                  </Button>
                </Space>
              }
            >
              <Form form={form} name={FORM_NAME} layout="vertical" scrollToFirstError>
                <Row gutter={12}>
                  <Col span={12}>
                    <Form.Item name="stepCode" label="步驟代碼" rules={[{ required: true, message: '請輸入步驟代碼' }]}>
                      <Input placeholder="tap_intro" />
                    </Form.Item>
                  </Col>
                  <Col span={12}>
                    <Form.Item name="stepType" label="步驟類型" rules={[{ required: true, message: '請選擇步驟類型' }]}>
                      <Select options={stepTypeOptions} />
                    </Form.Item>
                  </Col>
                  <Col span={12}>
                    <Form.Item name="triggerType" label="觸發方式">
                      <Select options={triggerTypeOptions} />
                    </Form.Item>
                  </Col>
                  <Col span={12}>
                    <Form.Item name="templateId" label="套用模板">
                      <Select allowClear showSearch optionFilterProp="label" options={templateOptions} />
                    </Form.Item>
                  </Col>
                  <Col span={24}>
                    <Form.Item name="stepNameZht" label="繁體步驟名" rules={[{ required: true, message: '請輸入繁體步驟名' }]}>
                      <Input />
                    </Form.Item>
                  </Col>
                  <Col span={24}>
                    <Form.Item name="stepNameZh" label="簡體步驟名">
                      <Input />
                    </Form.Item>
                  </Col>
                  <Col span={24}>
                    <Form.Item name="descriptionZht" label="繁體說明">
                      <Input.TextArea rows={3} />
                    </Form.Item>
                  </Col>
                </Row>

                <Card size="small" title="條件卡" className="poi-experience-editor-card">
                  <Row gutter={12}>
                    <Col span={12}>
                      <Form.Item name="triggerPreset" label="觸發預設">
                        <Select allowClear options={triggerPresetOptions} />
                      </Form.Item>
                    </Col>
                    <Col span={12}>
                      <Form.Item name="conditionPreset" label="出現條件預設">
                        <Select allowClear options={conditionPresetOptions} />
                      </Form.Item>
                    </Col>
                    <Col span={8}>
                      <Form.Item name="triggerRadiusMeters" label="半徑（米）">
                        <InputNumber min={0} style={{ width: '100%' }} />
                      </Form.Item>
                    </Col>
                    <Col span={8}>
                      <Form.Item name="dwellSeconds" label="停留秒數">
                        <InputNumber min={0} style={{ width: '100%' }} />
                      </Form.Item>
                    </Col>
                    <Col span={8}>
                      <Form.Item name="oncePerUser" label="每人一次" valuePropName="checked">
                        <Switch />
                      </Form.Item>
                    </Col>
                    <Col span={12}>
                      <Form.Item name="afterStepCode" label="前置步驟代碼">
                        <Input placeholder="例如 arrival_intro_media" />
                      </Form.Item>
                    </Col>
                    <Col span={12}>
                      <Form.Item name="tapActionCode" label="點擊動作代碼">
                        <Input placeholder="例如 start_explore" />
                      </Form.Item>
                    </Col>
                    <Col span={12}>
                      <Form.Item name="timeWindowStart" label="時間窗開始">
                        <Input placeholder="18:00" />
                      </Form.Item>
                    </Col>
                    <Col span={12}>
                      <Form.Item name="timeWindowEnd" label="時間窗結束">
                        <Input placeholder="24:00" />
                      </Form.Item>
                    </Col>
                    <Col span={12}>
                      <Form.Item name="requiredItemCodes" label="需要物品代碼">
                        <Select mode="tags" tokenSeparators={[',', '，']} />
                      </Form.Item>
                    </Col>
                    <Col span={12}>
                      <Form.Item name="requiredBadgeCodes" label="需要徽章 / 稱號代碼">
                        <Select mode="tags" tokenSeparators={[',', '，']} />
                      </Form.Item>
                    </Col>
                  </Row>
                </Card>

                <Card size="small" title="效果卡" className="poi-experience-editor-card">
                  <Row gutter={12}>
                    <Col span={12}>
                      <Form.Item name="effectPreset" label="效果預設">
                        <Select allowClear options={effectPresetOptions} />
                      </Form.Item>
                    </Col>
                    <Col span={12}>
                      <Form.Item name="primaryActionLabel" label="主操作文案">
                        <Input placeholder="前往探索該地" />
                      </Form.Item>
                    </Col>
                    <Col span={24}>
                      <Form.Item name="modalTitle" label="彈窗標題">
                        <Input />
                      </Form.Item>
                    </Col>
                    <Col span={24}>
                      <Form.Item name="modalBody" label="彈窗正文">
                        <Input.TextArea rows={3} />
                      </Form.Item>
                    </Col>
                    <Col span={24}>
                      <Form.Item name="routeCardTypes" label="路線卡片類型">
                        <Select mode="multiple" options={routeCardTypeOptions} />
                      </Form.Item>
                    </Col>
                    <Col span={12}>
                      <Form.Item name="taskCodes" label="任務代碼">
                        <Select mode="tags" tokenSeparators={[',', '，']} />
                      </Form.Item>
                    </Col>
                    <Col span={12}>
                      <Form.Item name="pickupCodes" label="拾取物代碼">
                        <Select mode="tags" tokenSeparators={[',', '，']} />
                      </Form.Item>
                    </Col>
                  </Row>
                </Card>

                <Card size="small" title="媒體卡" className="poi-experience-editor-card">
                  <Row gutter={12}>
                    <Col span={24}>
                      <SpatialAssetPickerField name="mediaAssetId" label="主媒體資源" assetKind="image" />
                    </Col>
                    <Col span={24}>
                      <SpatialAssetPickerField name="fullScreenMediaAssetId" label="全屏媒體資源" assetKind="video" />
                    </Col>
                    <Col span={24}>
                      <SpatialAssetPickerField name="audioAssetId" label="音效 / 背景音資源" assetKind="audio" />
                    </Col>
                  </Row>
                </Card>

                <Card size="small" title="獎勵卡" className="poi-experience-editor-card">
                  <Row gutter={12}>
                    <Col span={24}>
                      <Form.Item name="rewardRuleIds" label="獎勵規則">
                        <Select mode="multiple" showSearch optionFilterProp="label" options={rewardRuleOptions} />
                      </Form.Item>
                    </Col>
                    <Col span={24}>
                      <Form.Item name="rewardSummary" label="獎勵摘要">
                        <Input.TextArea rows={2} placeholder="例如完成打卡後發放金币、徽章與稱號。" />
                      </Form.Item>
                    </Col>
                    <Col span={12}>
                      <Form.Item name="explorationWeightLevel" label="探索權重">
                        <Select options={weightOptions} />
                      </Form.Item>
                    </Col>
                    <Col span={12}>
                      <Form.Item name="requiredForCompletion" label="主流程必做" valuePropName="checked">
                        <Switch />
                      </Form.Item>
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
                </Card>

                <Collapse
                  className="poi-experience-advanced"
                  items={[
                    {
                      key: 'advanced',
                      label: '進階 JSON',
                      children: (
                        <>
                          <Alert
                            type="info"
                            showIcon
                            style={{ marginBottom: 12 }}
                            message="進階 JSON 只作 fallback"
                            description="開啟後後端會優先保存這裡填寫的 JSON，但仍要求 schemaVersion。一般編排請使用上方結構化卡片。"
                          />
                          <Form.Item name="advancedJsonEnabled" label="使用進階 JSON 覆蓋" valuePropName="checked">
                            <Switch />
                          </Form.Item>
                          <Form.Item name="advancedTriggerConfigJson" label="Trigger JSON">
                            <Input.TextArea rows={4} disabled={!advancedJsonEnabled} />
                          </Form.Item>
                          <Form.Item name="advancedConditionConfigJson" label="Condition JSON">
                            <Input.TextArea rows={4} disabled={!advancedJsonEnabled} />
                          </Form.Item>
                          <Form.Item name="advancedEffectConfigJson" label="Effect JSON">
                            <Input.TextArea rows={4} disabled={!advancedJsonEnabled} />
                          </Form.Item>
                        </>
                      ),
                    },
                  ]}
                />
              </Form>
            </Card>
          </Col>
        </Row>

        <Card title="驗證結果與 runtime 對齊" className="poi-experience-validation-card">
          {snapshot ? (
            <Space direction="vertical" size="small" style={{ width: '100%' }}>
              <Alert
                type="info"
                showIcon
                message={`公開 runtime：${snapshot.publicRuntimePath}`}
                description="公開端只應讀取已發佈流程、綁定與步驟；草稿內容保留在後台編排。"
              />
              {snapshot.validationFindings?.length ? (
                snapshot.validationFindings.map((finding, index) => (
                  <Alert
                    key={`${finding.findingType}-${finding.stepCode || index}`}
                    type={finding.severity === 'warning' ? 'warning' : finding.severity === 'error' ? 'error' : 'info'}
                    showIcon
                    message={finding.stepCode ? `${finding.title} · ${finding.stepCode}` : finding.title}
                    description={finding.description}
                  />
                ))
              ) : (
                <Alert type="success" showIcon message="目前沒有阻擋發布的驗證提示" />
              )}
            </Space>
          ) : (
            <Empty description="選擇 POI 後會顯示 runtime 路徑、缺媒體 / 缺獎勵提示與未發布警告。" />
          )}
        </Card>
      </Spin>
    </PageContainer>
  );
};

export default POIExperienceWorkbench;
