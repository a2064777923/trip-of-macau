import React, { useEffect, useMemo, useState } from 'react';
import { PageContainer } from '@ant-design/pro-components';
import {
  Alert,
  App as AntdApp,
  Button,
  Card,
  Col,
  DatePicker,
  Divider,
  Empty,
  Form,
  Input,
  InputNumber,
  Popconfirm,
  Row,
  Select,
  Space,
  Tag,
  Typography,
} from 'antd';
import { PlusOutlined } from '@ant-design/icons';
import dayjs, { Dayjs } from 'dayjs';
import {
  createStorylineChapter,
  deleteStorylineChapter,
  getAdminActivities,
  getAdminPois,
  getAdminRewards,
  getAdminStoryContentBlocks,
  getAdminStorylines,
  getAdminTranslationSettings,
  getBadges,
  getCollectibles,
  getStorylineChapterDetail,
  getStorylineChapters,
  updateStorylineChapter,
} from '../../services/api';
import type {
  AdminActivityItem,
  AdminPoiListItem,
  AdminRewardItem,
  AdminStoryChapterContentBlockLinkPayload,
  AdminStoryChapterItem,
  AdminStoryChapterPayload,
  AdminStoryContentBlockItem,
  AdminStorylineListItem,
  BadgeItem,
  CollectibleItem,
} from '../../types/admin';
import LocalizedFieldGroup, {
  buildLocalizedFieldNames,
} from '../../components/localization/LocalizedFieldGroup';
import MediaAssetPickerField from '../../components/media/MediaAssetPickerField';

const { Text, Title } = Typography;

const titleFields = buildLocalizedFieldNames('title');
const summaryFields = buildLocalizedFieldNames('summary');
const detailFields = buildLocalizedFieldNames('detail');
const achievementFields = buildLocalizedFieldNames('achievement');
const collectibleFields = buildLocalizedFieldNames('collectible');
const locationFields = buildLocalizedFieldNames('locationName');

const chapterStatusOptions = [
  { label: '編輯中', value: 'draft' },
  { label: '已發佈', value: 'published' },
  { label: '已封存', value: 'archived' },
];

const anchorTypeOptions = [
  { label: 'POI', value: 'poi' },
  { label: '任務 / 活動', value: 'activity' },
  { label: '收集物', value: 'collectible' },
  { label: '徽章 / 稱號', value: 'badge' },
  { label: '遊戲內獎勵', value: 'reward' },
  { label: '任務點代碼', value: 'task' },
  { label: '標記代碼', value: 'marker' },
  { label: '疊加物代碼', value: 'overlay' },
  { label: '手動錨點', value: 'manual' },
];

const unlockPresetOptions = [
  { label: '順序解鎖', value: 'sequence' },
  { label: '時間解鎖', value: 'time_window' },
  { label: '探索度門檻', value: 'exploration_progress' },
  { label: '印章 / 收集物門檻', value: 'stamp_collectible_gate' },
  { label: '自定義 JSON', value: 'custom' },
];

const prerequisitePresetOptions = [
  { label: '完成前章', value: 'completed_previous_chapter' },
  { label: '到達指定 POI', value: 'reach_poi' },
  { label: '擁有收集物', value: 'collectible_owned' },
  { label: '擁有徽章 / 稱號', value: 'badge_owned' },
  { label: '停留秒數', value: 'stay_duration' },
  { label: '自定義 JSON', value: 'custom' },
];

const completionPresetOptions = [
  { label: '閱讀完成', value: 'read_story' },
  { label: '點擊錨點', value: 'tap_anchor' },
  { label: '完成打卡', value: 'check_in_poi' },
  { label: '觸發互動', value: 'trigger_interaction' },
  { label: '自定義 JSON', value: 'custom' },
];

const effectPresetOptions = [
  { label: '解鎖下一章', value: 'unlock_next_chapter' },
  { label: '發放收集物', value: 'grant_collectible' },
  { label: '發放徽章 / 稱號', value: 'grant_badge' },
  { label: '發放遊戲內獎勵', value: 'grant_reward' },
  { label: '播放全屏媒體', value: 'fullscreen_media' },
  { label: '播放音效', value: 'play_audio' },
  { label: '顯示彈窗', value: 'show_modal' },
  { label: '更新進度值', value: 'progress_update' },
  { label: '自定義 JSON', value: 'custom' },
];

type ScopeType = 'city' | 'sub_map';

interface ChapterFormValues extends AdminStoryChapterPayload {
  unlockPresetType?: string;
  unlockStartAt?: Dayjs | null;
  unlockEndAt?: Dayjs | null;
  unlockScopeType?: ScopeType;
  unlockScopeId?: number;
  unlockProgressPercent?: number;
  unlockCollectibleId?: number;
  unlockBadgeId?: number;
  unlockRequiredStamps?: number;
  unlockRawJson?: string;
  prerequisitePresetType?: string;
  prerequisitePoiId?: number;
  prerequisiteCollectibleId?: number;
  prerequisiteBadgeId?: number;
  prerequisiteSeconds?: number;
  prerequisiteRawJson?: string;
  completionPresetType?: string;
  completionPoiId?: number;
  completionTargetCode?: string;
  completionRawJson?: string;
  effectPresetType?: string;
  effectCollectibleId?: number;
  effectBadgeId?: number;
  effectRewardId?: number;
  effectMediaAssetId?: number;
  effectAudioAssetId?: number;
  effectProgressMetric?: string;
  effectProgressValue?: number;
  effectModalTitle?: string;
  effectModalBody?: string;
  effectRawJson?: string;
}

function safeParseJson(value?: string | null) {
  if (!value || !value.trim()) {
    return null;
  }
  try {
    return JSON.parse(value);
  } catch {
    return null;
  }
}

function pickStorylineName(storyline?: Partial<AdminStorylineListItem> | null) {
  if (!storyline) {
    return '';
  }
  return storyline.nameZht || storyline.nameZh || storyline.nameEn || storyline.namePt || storyline.code || '';
}

function pickPoiName(poi?: AdminPoiListItem | null) {
  if (!poi) {
    return '';
  }
  return poi.nameZht || poi.nameZh || poi.nameEn || poi.namePt || poi.code || '';
}

function pickBlockName(block?: Partial<AdminStoryContentBlockItem> | null) {
  if (!block) {
    return '';
  }
  return block.titleZht || block.titleZh || block.titleEn || block.titlePt || block.code || '';
}

function pickLocalizedOverride(rawJson?: string | null) {
  const parsed = safeParseJson(rawJson);
  if (!parsed || typeof parsed !== 'object') {
    return '';
  }
  return parsed['zh-Hant'] || parsed['zh_Hant'] || parsed['zht'] || parsed['zh'] || parsed['zh-CN'] || parsed['en'] || '';
}

function parseUnlockPreset(chapter?: Partial<AdminStoryChapterItem>) {
  const parsed = safeParseJson(chapter?.unlockParamJson);
  switch (chapter?.unlockType) {
    case 'time_window':
      return {
        unlockPresetType: 'time_window',
        unlockStartAt: parsed?.startAt ? dayjs(parsed.startAt) : undefined,
        unlockEndAt: parsed?.endAt ? dayjs(parsed.endAt) : undefined,
      };
    case 'exploration_progress':
      return {
        unlockPresetType: 'exploration_progress',
        unlockScopeType: parsed?.scopeType || 'city',
        unlockScopeId: parsed?.scopeId,
        unlockProgressPercent: parsed?.thresholdPercent,
      };
    case 'stamp_collectible_gate':
      return {
        unlockPresetType: 'stamp_collectible_gate',
        unlockCollectibleId: parsed?.collectibleId,
        unlockBadgeId: parsed?.badgeId,
        unlockRequiredStamps: parsed?.requiredStamps,
      };
    case 'custom':
      return {
        unlockPresetType: 'custom',
        unlockRawJson: chapter?.unlockParamJson,
        unlockType: chapter?.unlockType,
      };
    default:
      return {
        unlockPresetType: 'sequence',
        unlockType: chapter?.unlockType || 'sequence',
      };
  }
}

function parseConditionPreset(rawJson?: string | null, kind?: 'prerequisite' | 'completion') {
  const parsed = safeParseJson(rawJson);
  if (!parsed) {
    return {};
  }

  if (kind === 'prerequisite') {
    switch (parsed.type) {
      case 'completed_previous_chapter':
        return { prerequisitePresetType: 'completed_previous_chapter' };
      case 'reach_poi':
        return { prerequisitePresetType: 'reach_poi', prerequisitePoiId: parsed.poiId };
      case 'collectible_owned':
        return { prerequisitePresetType: 'collectible_owned', prerequisiteCollectibleId: parsed.collectibleId };
      case 'badge_owned':
        return { prerequisitePresetType: 'badge_owned', prerequisiteBadgeId: parsed.badgeId };
      case 'stay_duration':
        return {
          prerequisitePresetType: 'stay_duration',
          prerequisitePoiId: parsed.poiId,
          prerequisiteSeconds: parsed.seconds,
        };
      default:
        return {
          prerequisitePresetType: 'custom',
          prerequisiteRawJson: rawJson,
        };
    }
  }

  switch (parsed.type) {
    case 'read_story':
      return { completionPresetType: 'read_story' };
    case 'tap_anchor':
      return { completionPresetType: 'tap_anchor' };
    case 'check_in_poi':
      return { completionPresetType: 'check_in_poi', completionPoiId: parsed.poiId };
    case 'trigger_interaction':
      return {
        completionPresetType: 'trigger_interaction',
        completionTargetCode: parsed.targetCode,
      };
    default:
      return {
        completionPresetType: 'custom',
        completionRawJson: rawJson,
      };
  }
}

function parseEffectPreset(rawJson?: string | null) {
  const parsed = safeParseJson(rawJson);
  if (!parsed) {
    return { effectPresetType: 'unlock_next_chapter' };
  }
  switch (parsed.type) {
    case 'unlock_next_chapter':
      return { effectPresetType: 'unlock_next_chapter' };
    case 'grant_collectible':
      return { effectPresetType: 'grant_collectible', effectCollectibleId: parsed.collectibleId };
    case 'grant_badge':
      return { effectPresetType: 'grant_badge', effectBadgeId: parsed.badgeId };
    case 'grant_reward':
      return { effectPresetType: 'grant_reward', effectRewardId: parsed.rewardId };
    case 'fullscreen_media':
      return { effectPresetType: 'fullscreen_media', effectMediaAssetId: parsed.assetId };
    case 'play_audio':
      return { effectPresetType: 'play_audio', effectAudioAssetId: parsed.assetId };
    case 'show_modal':
      return {
        effectPresetType: 'show_modal',
        effectModalTitle: parsed.title,
        effectModalBody: parsed.body,
      };
    case 'progress_update':
      return {
        effectPresetType: 'progress_update',
        effectProgressMetric: parsed.metric,
        effectProgressValue: parsed.value,
      };
    default:
      return {
        effectPresetType: 'custom',
        effectRawJson: rawJson,
      };
  }
}

function buildDefaultValues(chapterCount: number, chapter?: AdminStoryChapterItem | null): Partial<ChapterFormValues> {
  return {
    chapterOrder: chapter?.chapterOrder ?? chapterCount + 1,
    sortOrder: chapter?.sortOrder ?? chapterCount + 1,
    status: chapter?.status || 'draft',
    anchorType: chapter?.anchorType || 'poi',
    contentBlocks: chapter?.contentBlocks?.map((item, index) => ({
      id: item.id,
      blockId: item.blockId,
      overrideTitleJson: item.overrideTitleJson,
      overrideSummaryJson: item.overrideSummaryJson,
      overrideBodyJson: item.overrideBodyJson,
      displayConditionJson: item.displayConditionJson,
      overrideConfigJson: item.overrideConfigJson,
      status: item.status || 'draft',
      sortOrder: item.sortOrder ?? index,
    })),
    ...chapter,
    ...parseUnlockPreset(chapter || undefined),
    ...parseConditionPreset(chapter?.prerequisiteJson, 'prerequisite'),
    ...parseConditionPreset(chapter?.completionJson, 'completion'),
    ...parseEffectPreset(chapter?.rewardJson),
  };
}

function serializeUnlock(values: ChapterFormValues) {
  switch (values.unlockPresetType) {
    case 'time_window':
      return {
        unlockType: 'time_window',
        unlockParamJson: JSON.stringify({
          startAt: values.unlockStartAt ? values.unlockStartAt.toISOString() : null,
          endAt: values.unlockEndAt ? values.unlockEndAt.toISOString() : null,
        }),
      };
    case 'exploration_progress':
      return {
        unlockType: 'exploration_progress',
        unlockParamJson: JSON.stringify({
          scopeType: values.unlockScopeType || 'city',
          scopeId: values.unlockScopeId,
          thresholdPercent: values.unlockProgressPercent || 0,
        }),
      };
    case 'stamp_collectible_gate':
      return {
        unlockType: 'stamp_collectible_gate',
        unlockParamJson: JSON.stringify({
          collectibleId: values.unlockCollectibleId,
          badgeId: values.unlockBadgeId,
          requiredStamps: values.unlockRequiredStamps || 0,
        }),
      };
    case 'custom':
      return {
        unlockType: values.unlockType || 'custom',
        unlockParamJson: values.unlockRawJson,
      };
    default:
      return {
        unlockType: 'sequence',
        unlockParamJson: undefined,
      };
  }
}

function serializePrerequisite(values: ChapterFormValues) {
  switch (values.prerequisitePresetType) {
    case 'completed_previous_chapter':
      return JSON.stringify({ type: 'completed_previous_chapter' });
    case 'reach_poi':
      return JSON.stringify({ type: 'reach_poi', poiId: values.prerequisitePoiId });
    case 'collectible_owned':
      return JSON.stringify({ type: 'collectible_owned', collectibleId: values.prerequisiteCollectibleId });
    case 'badge_owned':
      return JSON.stringify({ type: 'badge_owned', badgeId: values.prerequisiteBadgeId });
    case 'stay_duration':
      return JSON.stringify({
        type: 'stay_duration',
        poiId: values.prerequisitePoiId,
        seconds: values.prerequisiteSeconds || 0,
      });
    case 'custom':
      return values.prerequisiteRawJson;
    default:
      return undefined;
  }
}

function serializeCompletion(values: ChapterFormValues) {
  switch (values.completionPresetType) {
    case 'tap_anchor':
      return JSON.stringify({ type: 'tap_anchor' });
    case 'check_in_poi':
      return JSON.stringify({ type: 'check_in_poi', poiId: values.completionPoiId });
    case 'trigger_interaction':
      return JSON.stringify({ type: 'trigger_interaction', targetCode: values.completionTargetCode });
    case 'custom':
      return values.completionRawJson;
    default:
      return JSON.stringify({ type: 'read_story' });
  }
}

function serializeEffect(values: ChapterFormValues) {
  switch (values.effectPresetType) {
    case 'grant_collectible':
      return JSON.stringify({ type: 'grant_collectible', collectibleId: values.effectCollectibleId });
    case 'grant_badge':
      return JSON.stringify({ type: 'grant_badge', badgeId: values.effectBadgeId });
    case 'grant_reward':
      return JSON.stringify({ type: 'grant_reward', rewardId: values.effectRewardId });
    case 'fullscreen_media':
      return JSON.stringify({ type: 'fullscreen_media', assetId: values.effectMediaAssetId });
    case 'play_audio':
      return JSON.stringify({ type: 'play_audio', assetId: values.effectAudioAssetId });
    case 'show_modal':
      return JSON.stringify({
        type: 'show_modal',
        title: values.effectModalTitle,
        body: values.effectModalBody,
      });
    case 'progress_update':
      return JSON.stringify({
        type: 'progress_update',
        metric: values.effectProgressMetric,
        value: values.effectProgressValue || 0,
      });
    case 'custom':
      return values.effectRawJson;
    default:
      return JSON.stringify({ type: 'unlock_next_chapter' });
  }
}

const StoryChapterWorkbench: React.FC = () => {
  const { message } = AntdApp.useApp();
  const [form] = Form.useForm<ChapterFormValues>();
  const [storylines, setStorylines] = useState<AdminStorylineListItem[]>([]);
  const [selectedStorylineId, setSelectedStorylineId] = useState<number>();
  const [chapterList, setChapterList] = useState<AdminStoryChapterItem[]>([]);
  const [editingChapter, setEditingChapter] = useState<AdminStoryChapterItem | null>(null);
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [translationDefaults, setTranslationDefaults] = useState<any>();
  const [pois, setPois] = useState<AdminPoiListItem[]>([]);
  const [activities, setActivities] = useState<AdminActivityItem[]>([]);
  const [collectibles, setCollectibles] = useState<CollectibleItem[]>([]);
  const [badges, setBadges] = useState<BadgeItem[]>([]);
  const [rewards, setRewards] = useState<AdminRewardItem[]>([]);
  const [contentBlocks, setContentBlocks] = useState<AdminStoryContentBlockItem[]>([]);

  const anchorType = Form.useWatch('anchorType', form);
  const unlockPresetType = Form.useWatch('unlockPresetType', form);
  const prerequisitePresetType = Form.useWatch('prerequisitePresetType', form);
  const completionPresetType = Form.useWatch('completionPresetType', form);
  const effectPresetType = Form.useWatch('effectPresetType', form);
  const watchedContentBlocks = Form.useWatch('contentBlocks', form) as AdminStoryChapterContentBlockLinkPayload[] | undefined;

  const loadCatalog = async () => {
    const [
      storylinesRes,
      poiRes,
      activityRes,
      collectibleRes,
      badgeRes,
      rewardRes,
      blockRes,
      translationRes,
    ] = await Promise.all([
      getAdminStorylines({ pageNum: 1, pageSize: 200 }),
      getAdminPois({ pageNum: 1, pageSize: 500 }),
      getAdminActivities({ pageNum: 1, pageSize: 200 }),
      getCollectibles({ pageNum: 1, pageSize: 200 }),
      getBadges({ pageNum: 1, pageSize: 200 }),
      getAdminRewards({ pageNum: 1, pageSize: 200 }),
      getAdminStoryContentBlocks({ pageNum: 1, pageSize: 500 }),
      getAdminTranslationSettings(),
    ]);

    setStorylines(storylinesRes.data?.list || []);
    setPois(poiRes.data?.list || []);
    setActivities(activityRes.data?.list || []);
    setCollectibles(collectibleRes.data?.list || []);
    setBadges(badgeRes.data?.list || []);
    setRewards(rewardRes.data?.list || []);
    setContentBlocks(blockRes.data?.list || []);
    if (translationRes.success && translationRes.data) {
      setTranslationDefaults(translationRes.data);
    }
  };

  const loadChapters = async (storylineId: number) => {
    setLoading(true);
    try {
      const response = await getStorylineChapters(storylineId, { pageNum: 1, pageSize: 200 });
      const items = response.data?.list || [];
      setChapterList(items);
      if (!editingChapter) {
        form.resetFields();
        form.setFieldsValue(buildDefaultValues(items.length, null));
      }
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void loadCatalog();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => {
    if (!selectedStorylineId && storylines.length) {
      setSelectedStorylineId(storylines[0].storylineId);
    }
  }, [storylines, selectedStorylineId]);

  useEffect(() => {
    if (!selectedStorylineId) {
      return;
    }
    setEditingChapter(null);
    form.resetFields();
    void loadChapters(selectedStorylineId);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [selectedStorylineId]);

  const filteredPoiOptions = useMemo(() => {
    const activeStoryline = storylines.find((item) => item.storylineId === selectedStorylineId);
    if (!activeStoryline) {
      return pois;
    }
    return pois.filter((poi) => {
      if (activeStoryline.cityBindings?.length && !activeStoryline.cityBindings.includes(poi.cityId)) {
        return false;
      }
      if (!poi.subMapId || !activeStoryline.subMapBindings?.length) {
        return true;
      }
      return activeStoryline.subMapBindings.includes(poi.subMapId);
    });
  }, [pois, selectedStorylineId, storylines]);

  const assembledContentPreview = useMemo(() => {
    return (watchedContentBlocks || [])
      .map((link, index) => {
        const block = contentBlocks.find((item) => item.id === Number(link?.blockId));
        const title = pickLocalizedOverride(link?.overrideTitleJson) || pickBlockName(block) || `未選內容積木 ${index + 1}`;
        const summary = pickLocalizedOverride(link?.overrideSummaryJson) || block?.summaryZht || block?.summaryZh || block?.summaryEn || '';
        const warnings: string[] = [];
        if (!block) {
          warnings.push('缺失內容積木引用');
        } else if (block.status !== 'published') {
          warnings.push(`內容積木狀態為 ${block.status || 'draft'}，發布前需確認`);
        }
        if (link?.status && link.status !== 'published') {
          warnings.push(`章節內關聯狀態為 ${link.status}`);
        }
        if (block && ['image', 'gallery', 'audio', 'video', 'lottie', 'attachment_list'].includes(block.blockType) && !block.primaryAssetId && !block.attachmentAssetIds?.length) {
          warnings.push('媒體型積木尚未綁定主資源或附件資源');
        }
        return {
          key: `${link?.blockId || 'missing'}-${index}`,
          index,
          link,
          block,
          title,
          summary,
          warnings,
        };
      })
      .sort((a, b) => (a.link?.sortOrder ?? a.index) - (b.link?.sortOrder ?? b.index));
  }, [contentBlocks, watchedContentBlocks]);

  const openCreateChapter = () => {
    setEditingChapter(null);
    form.resetFields();
    form.setFieldsValue(buildDefaultValues(chapterList.length, null));
  };

  const openEditChapter = async (chapterId: number) => {
    if (!selectedStorylineId) {
      return;
    }
    const response = await getStorylineChapterDetail(selectedStorylineId, chapterId);
    if (!response.success || !response.data) {
      message.error(response.message || '載入章節詳情失敗');
      return;
    }
    setEditingChapter(response.data);
    form.resetFields();
    form.setFieldsValue(buildDefaultValues(chapterList.length, response.data));
  };

  const handleDeleteChapter = async (chapterId: number) => {
    if (!selectedStorylineId) {
      return;
    }
    const response = await deleteStorylineChapter(selectedStorylineId, chapterId);
    if (!response.success) {
      message.error(response.message || '刪除章節失敗');
      return;
    }
    message.success('章節已刪除');
    if (editingChapter?.id === chapterId) {
      openCreateChapter();
    }
    void loadChapters(selectedStorylineId);
  };

  const handleSave = async () => {
    if (!selectedStorylineId) {
      message.warning('請先選擇故事線');
      return;
    }
    const values = await form.validateFields();
    const unlock = serializeUnlock(values);
    const payload: AdminStoryChapterPayload = {
      ...values,
      unlockType: unlock.unlockType,
      unlockParamJson: unlock.unlockParamJson,
      prerequisiteJson: serializePrerequisite(values),
      completionJson: serializeCompletion(values),
      rewardJson: serializeEffect(values),
      contentBlocks: (values.contentBlocks || []).map((item, index) => ({
        ...item,
        sortOrder: item.sortOrder ?? index,
        status: item.status || 'draft',
      })) as AdminStoryChapterContentBlockLinkPayload[],
    };

    setSaving(true);
    try {
      const response = editingChapter
        ? await updateStorylineChapter(selectedStorylineId, editingChapter.id, payload)
        : await createStorylineChapter(selectedStorylineId, payload);
      if (!response.success || !response.data) {
        throw new Error(response.message || '保存章節失敗');
      }
      message.success(editingChapter ? '章節已更新' : '章節已建立');
      setEditingChapter(response.data);
      form.setFieldsValue(buildDefaultValues(chapterList.length, response.data));
      void loadChapters(selectedStorylineId);
    } catch (error) {
      message.error(error instanceof Error ? error.message : '保存章節失敗');
    } finally {
      setSaving(false);
    }
  };

  return (
    <PageContainer
      title="章節管理"
      subTitle="用專用工作台編排章節的錨點、內容積木、解鎖條件與完成效果，不再跳回其他模組。"
      extra={[
        <Button key="new" type="primary" icon={<PlusOutlined />} onClick={openCreateChapter}>
          新增章節
        </Button>,
      ]}
    >
      <Space direction="vertical" size="large" style={{ width: '100%' }}>
        <Alert
          type="info"
          showIcon
          message="章節工作台"
          description="故事線只綁定城市與子地圖。POI、任務點、標記與疊加物等具體錨點，都在章節層處理。"
        />

        <Card>
          <Space wrap size="large">
            <Space direction="vertical" size={4}>
              <Text type="secondary">故事線</Text>
              <Select
                style={{ width: 320 }}
                value={selectedStorylineId}
                options={storylines.map((item) => ({
                  label: pickStorylineName(item),
                  value: item.storylineId,
                }))}
                onChange={setSelectedStorylineId}
              />
            </Space>
            <Space direction="vertical" size={4}>
              <Text type="secondary">已載入章節數</Text>
              <Title level={3} style={{ margin: 0 }}>
                {chapterList.length}
              </Title>
            </Space>
          </Space>
        </Card>

        {!selectedStorylineId ? (
          <Empty description="請先選擇故事線" />
        ) : (
          <Row gutter={24} align="top">
            <Col xs={24} xl={8}>
              <Card title="章節列表" loading={loading}>
                <Space direction="vertical" size="middle" style={{ width: '100%' }}>
                  {chapterList.map((chapter) => (
                    <Card
                      key={chapter.id}
                      size="small"
                      hoverable
                      style={{
                        borderColor: editingChapter?.id === chapter.id ? '#7c5cff' : undefined,
                      }}
                      onClick={() => void openEditChapter(chapter.id)}
                    >
                      <Space direction="vertical" size={6} style={{ width: '100%' }}>
                        <Space wrap>
                          <Tag color="blue">第 {chapter.chapterOrder} 章</Tag>
                          <Tag color={chapter.status === 'published' ? 'green' : chapter.status === 'archived' ? 'default' : 'gold'}>
                            {chapter.status === 'published' ? '已發佈' : chapter.status === 'archived' ? '已封存' : '編輯中'}
                          </Tag>
                        </Space>
                        <Text strong>{chapter.titleZht || chapter.titleZh || `章節 #${chapter.id}`}</Text>
                        <Text type="secondary" ellipsis>
                          {chapter.summaryZht || chapter.summaryZh || '未填寫摘要'}
                        </Text>
                        <Space>
                          <Button
                            size="small"
                            type="link"
                            onClick={(event) => {
                              event.stopPropagation();
                              void openEditChapter(chapter.id);
                            }}
                          >
                            編輯
                          </Button>
                          <Popconfirm title="確定刪除此章節？" onConfirm={() => void handleDeleteChapter(chapter.id)}>
                            <Button size="small" danger type="link" onClick={(event) => event.stopPropagation()}>
                              刪除
                            </Button>
                          </Popconfirm>
                        </Space>
                      </Space>
                    </Card>
                  ))}
                  {!chapterList.length ? <Empty description="這條故事線尚未建立章節" /> : null}
                </Space>
              </Card>
            </Col>

            <Col xs={24} xl={16}>
              <Card
                title={editingChapter ? `編輯章節：${editingChapter.titleZht || editingChapter.titleZh}` : '新增章節'}
                extra={
                  <Space>
                    <Button onClick={openCreateChapter}>重置</Button>
                    <Button type="primary" loading={saving} onClick={() => void handleSave()}>
                      保存章節
                    </Button>
                  </Space>
                }
              >
                <Form form={form} layout="vertical" initialValues={buildDefaultValues(chapterList.length, null)}>
                  <Row gutter={16}>
                    <Col xs={24} md={6}>
                      <Form.Item
                        name="chapterOrder"
                        label="章節順序"
                        rules={[{ required: true, message: '請填寫章節順序' }]}
                      >
                        <InputNumber min={1} style={{ width: '100%' }} />
                      </Form.Item>
                    </Col>
                    <Col xs={24} md={6}>
                      <Form.Item name="sortOrder" label="排序">
                        <InputNumber min={0} style={{ width: '100%' }} />
                      </Form.Item>
                    </Col>
                    <Col xs={24} md={6}>
                      <Form.Item name="status" label="狀態">
                        <Select options={chapterStatusOptions} />
                      </Form.Item>
                    </Col>
                    <Col xs={24} md={6}>
                      <MediaAssetPickerField
                        name="mediaAssetId"
                        label="主媒體"
                        valueMode="asset-id"
                        help="如舊章節仍使用單一主媒體，這裡仍保留兼容欄位。"
                      />
                    </Col>
                  </Row>

                  <LocalizedFieldGroup
                    form={form}
                    label="章節標題"
                    fieldNames={titleFields}
                    required
                    translationDefaults={translationDefaults}
                  />

                  <LocalizedFieldGroup
                    form={form}
                    label="章節摘要"
                    fieldNames={summaryFields}
                    multiline
                    rows={3}
                    translationDefaults={translationDefaults}
                  />

                  <LocalizedFieldGroup
                    form={form}
                    label="章節正文"
                    fieldNames={detailFields}
                    multiline
                    rows={6}
                    translationDefaults={translationDefaults}
                  />

                  <Divider>錨點綁定</Divider>

                  <Row gutter={16}>
                    <Col xs={24} md={8}>
                      <Form.Item name="anchorType" label="錨點類型">
                        <Select options={anchorTypeOptions} />
                      </Form.Item>
                    </Col>
                    <Col xs={24} md={8}>
                      {anchorType === 'poi' ? (
                        <Form.Item name="anchorTargetId" label="綁定 POI" rules={[{ required: true, message: '請選擇 POI' }]}>
                          <Select
                            showSearch
                            optionFilterProp="label"
                            options={filteredPoiOptions.map((poi) => ({
                              label: `${pickPoiName(poi)} (${poi.code})`,
                              value: poi.poiId,
                            }))}
                          />
                        </Form.Item>
                      ) : null}
                      {anchorType === 'activity' ? (
                        <Form.Item name="anchorTargetId" label="綁定任務 / 活動" rules={[{ required: true, message: '請選擇任務或活動' }]}>
                          <Select
                            showSearch
                            optionFilterProp="label"
                            options={activities.map((activity: AdminActivityItem) => ({
                              label: `${activity.title} (${activity.code})`,
                              value: activity.id,
                            }))}
                          />
                        </Form.Item>
                      ) : null}
                      {anchorType === 'collectible' ? (
                        <Form.Item name="anchorTargetId" label="綁定收集物" rules={[{ required: true, message: '請選擇收集物' }]}>
                          <Select
                            showSearch
                            optionFilterProp="label"
                            options={collectibles.map((item: CollectibleItem) => ({
                              label: `${item.nameZht || item.nameZh} (${item.collectibleCode})`,
                              value: item.id,
                            }))}
                          />
                        </Form.Item>
                      ) : null}
                      {anchorType === 'badge' ? (
                        <Form.Item name="anchorTargetId" label="綁定徽章 / 稱號" rules={[{ required: true, message: '請選擇徽章或稱號' }]}>
                          <Select
                            showSearch
                            optionFilterProp="label"
                            options={badges.map((item: BadgeItem) => ({
                              label: `${item.nameZht || item.nameZh} (${item.badgeCode})`,
                              value: item.id,
                            }))}
                          />
                        </Form.Item>
                      ) : null}
                      {anchorType === 'reward' ? (
                        <Form.Item name="anchorTargetId" label="綁定遊戲內獎勵" rules={[{ required: true, message: '請選擇獎勵' }]}>
                          <Select
                            showSearch
                            optionFilterProp="label"
                            options={rewards.map((item: AdminRewardItem) => ({
                              label: `${item.nameZht || item.nameZh} (${item.code})`,
                              value: item.id,
                            }))}
                          />
                        </Form.Item>
                      ) : null}
                    </Col>
                    <Col xs={24} md={8}>
                      <Form.Item name="anchorTargetCode" label="錨點代碼">
                        <Input placeholder="例如：cannon_overlay_01" />
                      </Form.Item>
                    </Col>
                  </Row>

                  <Divider>內容編排</Divider>

                  <Form.List name="contentBlocks">
                    {(fields, { add, remove, move }) => (
                      <Space direction="vertical" size="middle" style={{ width: '100%' }}>
                        {fields.map((field, index) => (
                          <Card
                            key={field.key}
                            size="small"
                            title={`內容積木 ${index + 1}`}
                            extra={
                              <Space>
                                <Button size="small" onClick={() => move(index, Math.max(0, index - 1))} disabled={index === 0}>
                                  上移
                                </Button>
                                <Button size="small" onClick={() => move(index, Math.min(fields.length - 1, index + 1))} disabled={index === fields.length - 1}>
                                  下移
                                </Button>
                                <Button size="small" danger onClick={() => remove(field.name)}>
                                  移除
                                </Button>
                              </Space>
                            }
                          >
                            <Row gutter={16}>
                              <Col xs={24} md={12}>
                                <Form.Item
                                  {...field}
                                  name={[field.name, 'blockId']}
                                  label="選擇內容積木"
                                  rules={[{ required: true, message: '請選擇內容積木' }]}
                                >
                                  <Select
                                    showSearch
                                    optionFilterProp="label"
                                    options={contentBlocks.map((item) => ({
                                      label: `${pickBlockName(item)} (${item.blockType})`,
                                      value: item.id,
                                    }))}
                                  />
                                </Form.Item>
                              </Col>
                              <Col xs={24} md={4}>
                                <Form.Item {...field} name={[field.name, 'sortOrder']} label="排序">
                                  <InputNumber min={0} style={{ width: '100%' }} />
                                </Form.Item>
                              </Col>
                              <Col xs={24} md={8}>
                                <Form.Item {...field} name={[field.name, 'status']} label="狀態">
                                  <Select options={chapterStatusOptions} />
                                </Form.Item>
                              </Col>
                            </Row>
                            <Form.Item {...field} name={[field.name, 'overrideTitleJson']} label="標題覆寫 JSON">
                              <Input.TextArea rows={2} placeholder='例如：{"zh-Hant":"新標題","en":"New title"}' />
                            </Form.Item>
                            <Form.Item {...field} name={[field.name, 'overrideSummaryJson']} label="摘要覆寫 JSON">
                              <Input.TextArea rows={2} />
                            </Form.Item>
                            <Form.Item {...field} name={[field.name, 'overrideBodyJson']} label="正文覆寫 JSON">
                              <Input.TextArea rows={3} />
                            </Form.Item>
                            <Form.Item {...field} name={[field.name, 'displayConditionJson']} label="顯示條件 JSON">
                              <Input.TextArea rows={2} />
                            </Form.Item>
                            <Form.Item {...field} name={[field.name, 'overrideConfigJson']} label="配置覆寫 JSON">
                              <Input.TextArea rows={2} />
                            </Form.Item>
                          </Card>
                        ))}

                        <Button
                          type="dashed"
                          onClick={() =>
                            add({
                              status: 'draft',
                              sortOrder: fields.length,
                            })
                          }
                        >
                          新增內容積木
                        </Button>
                      </Space>
                    )}
                  </Form.List>

                  <Card
                    size="small"
                    title="發布前組裝預覽"
                    style={{ marginTop: 16, marginBottom: 16 }}
                  >
                    <Space direction="vertical" size="middle" style={{ width: '100%' }}>
                      <Text type="secondary">
                        按章節內排序即時組裝內容預覽，集中檢查 Lottie、圖片、音頻、視頻與附件積木的發布狀態、缺失引用與資源摘要。
                      </Text>
                      {!assembledContentPreview.length ? (
                        <Empty description="尚未加入內容積木，發布前請至少加入一個可展示的故事內容。" />
                      ) : (
                        assembledContentPreview.map((item) => (
                          <Card key={item.key} size="small" type="inner">
                            <Space direction="vertical" size={6} style={{ width: '100%' }}>
                              <Space wrap>
                                <Tag color="blue">第 {item.index + 1} 段</Tag>
                                <Tag>{item.block?.blockType || 'missing'}</Tag>
                                <Tag color={item.block?.status === 'published' ? 'green' : 'gold'}>
                                  {item.block?.status === 'published' ? '已發佈' : item.block?.status || '缺失'}
                                </Tag>
                              </Space>
                              <Text strong>{item.title}</Text>
                              {item.summary ? <Text type="secondary">{item.summary}</Text> : null}
                              <Space wrap>
                                <Tag>主資源 ID：{item.block?.primaryAssetId || '未設定'}</Tag>
                                <Tag>附件數：{item.block?.attachmentAssetIds?.length || 0}</Tag>
                                <Tag>章節關聯狀態：{item.link?.status || 'draft'}</Tag>
                              </Space>
                              {item.warnings.length ? (
                                <Alert
                                  type="warning"
                                  showIcon
                                  message="發布前檢查"
                                  description={item.warnings.join('；')}
                                />
                              ) : null}
                            </Space>
                          </Card>
                        ))
                      )}
                    </Space>
                  </Card>

                  <Divider>規則與效果</Divider>
                  <Card size="small" title="解鎖規則" style={{ marginBottom: 16 }}>
                    <Row gutter={16}>
                      <Col xs={24} md={8}>
                        <Form.Item name="unlockPresetType" label="解鎖方式">
                          <Select options={unlockPresetOptions} />
                        </Form.Item>
                      </Col>
                      {unlockPresetType === 'time_window' ? (
                        <>
                          <Col xs={24} md={8}>
                            <Form.Item name="unlockStartAt" label="開始時間">
                              <DatePicker showTime style={{ width: '100%' }} />
                            </Form.Item>
                          </Col>
                          <Col xs={24} md={8}>
                            <Form.Item name="unlockEndAt" label="結束時間">
                              <DatePicker showTime style={{ width: '100%' }} />
                            </Form.Item>
                          </Col>
                        </>
                      ) : null}
                      {unlockPresetType === 'exploration_progress' ? (
                        <>
                          <Col xs={24} md={6}>
                            <Form.Item name="unlockScopeType" label="作用範圍">
                              <Select
                                options={[
                                  { label: '城市', value: 'city' },
                                  { label: '子地圖', value: 'sub_map' },
                                ]}
                              />
                            </Form.Item>
                          </Col>
                          <Col xs={24} md={8}>
                            <Form.Item name="unlockScopeId" label="範圍 ID">
                              <InputNumber min={1} style={{ width: '100%' }} />
                            </Form.Item>
                          </Col>
                          <Col xs={24} md={10}>
                            <Form.Item name="unlockProgressPercent" label="探索度門檻 (%)">
                              <InputNumber min={0} max={100} style={{ width: '100%' }} />
                            </Form.Item>
                          </Col>
                        </>
                      ) : null}
                      {unlockPresetType === 'stamp_collectible_gate' ? (
                        <>
                          <Col xs={24} md={8}>
                            <Form.Item name="unlockCollectibleId" label="收集物門檻">
                              <Select
                                allowClear
                                showSearch
                                optionFilterProp="label"
                                options={collectibles.map((item: CollectibleItem) => ({
                                  label: `${item.nameZht || item.nameZh} (${item.collectibleCode})`,
                                  value: item.id,
                                }))}
                              />
                            </Form.Item>
                          </Col>
                          <Col xs={24} md={8}>
                            <Form.Item name="unlockBadgeId" label="徽章門檻">
                              <Select
                                allowClear
                                showSearch
                                optionFilterProp="label"
                                options={badges.map((item: BadgeItem) => ({
                                  label: `${item.nameZht || item.nameZh} (${item.badgeCode})`,
                                  value: item.id,
                                }))}
                              />
                            </Form.Item>
                          </Col>
                          <Col xs={24} md={8}>
                            <Form.Item name="unlockRequiredStamps" label="印章數">
                              <InputNumber min={0} style={{ width: '100%' }} />
                            </Form.Item>
                          </Col>
                        </>
                      ) : null}
                    </Row>
                    {unlockPresetType === 'custom' ? (
                      <Form.Item name="unlockRawJson" label="自定義 JSON">
                        <Input.TextArea rows={4} />
                      </Form.Item>
                    ) : null}
                  </Card>

                  <Card size="small" title="前置條件" style={{ marginBottom: 16 }}>
                    <Row gutter={16}>
                      <Col xs={24} md={8}>
                        <Form.Item name="prerequisitePresetType" label="條件模板">
                          <Select options={prerequisitePresetOptions} />
                        </Form.Item>
                      </Col>
                      {prerequisitePresetType === 'reach_poi' || prerequisitePresetType === 'stay_duration' ? (
                        <Col xs={24} md={8}>
                          <Form.Item name="prerequisitePoiId" label="指定 POI">
                            <Select
                              showSearch
                              optionFilterProp="label"
                              options={filteredPoiOptions.map((poi) => ({
                                label: `${pickPoiName(poi)} (${poi.code})`,
                                value: poi.poiId,
                              }))}
                            />
                          </Form.Item>
                        </Col>
                      ) : null}
                      {prerequisitePresetType === 'collectible_owned' ? (
                        <Col xs={24} md={8}>
                          <Form.Item name="prerequisiteCollectibleId" label="指定收集物">
                            <Select
                              showSearch
                              optionFilterProp="label"
                              options={collectibles.map((item: CollectibleItem) => ({
                                label: `${item.nameZht || item.nameZh} (${item.collectibleCode})`,
                                value: item.id,
                              }))}
                            />
                          </Form.Item>
                        </Col>
                      ) : null}
                      {prerequisitePresetType === 'badge_owned' ? (
                        <Col xs={24} md={8}>
                          <Form.Item name="prerequisiteBadgeId" label="指定徽章 / 稱號">
                            <Select
                              showSearch
                              optionFilterProp="label"
                              options={badges.map((item: BadgeItem) => ({
                                label: `${item.nameZht || item.nameZh} (${item.badgeCode})`,
                                value: item.id,
                              }))}
                            />
                          </Form.Item>
                        </Col>
                      ) : null}
                      {prerequisitePresetType === 'stay_duration' ? (
                        <Col xs={24} md={8}>
                          <Form.Item name="prerequisiteSeconds" label="停留秒數">
                            <InputNumber min={1} style={{ width: '100%' }} />
                          </Form.Item>
                        </Col>
                      ) : null}
                    </Row>
                    {prerequisitePresetType === 'custom' ? (
                      <Form.Item name="prerequisiteRawJson" label="自定義 JSON">
                        <Input.TextArea rows={4} />
                      </Form.Item>
                    ) : null}
                  </Card>

                  <Card size="small" title="完成條件" style={{ marginBottom: 16 }}>
                    <Row gutter={16}>
                      <Col xs={24} md={8}>
                        <Form.Item name="completionPresetType" label="條件模板">
                          <Select options={completionPresetOptions} />
                        </Form.Item>
                      </Col>
                      {completionPresetType === 'check_in_poi' ? (
                        <Col xs={24} md={8}>
                          <Form.Item name="completionPoiId" label="打卡 POI">
                            <Select
                              showSearch
                              optionFilterProp="label"
                              options={filteredPoiOptions.map((poi) => ({
                                label: `${pickPoiName(poi)} (${poi.code})`,
                                value: poi.poiId,
                              }))}
                            />
                          </Form.Item>
                        </Col>
                      ) : null}
                      {completionPresetType === 'trigger_interaction' ? (
                        <Col xs={24} md={8}>
                          <Form.Item name="completionTargetCode" label="互動代碼">
                            <Input placeholder="例如：lookout_marker_01" />
                          </Form.Item>
                        </Col>
                      ) : null}
                    </Row>
                    {completionPresetType === 'custom' ? (
                      <Form.Item name="completionRawJson" label="自定義 JSON">
                        <Input.TextArea rows={4} />
                      </Form.Item>
                    ) : null}
                  </Card>

                  <Card size="small" title="完成效果" style={{ marginBottom: 16 }}>
                    <Row gutter={16}>
                      <Col xs={24} md={8}>
                        <Form.Item name="effectPresetType" label="效果模板">
                          <Select options={effectPresetOptions} />
                        </Form.Item>
                      </Col>
                      {effectPresetType === 'grant_collectible' ? (
                        <Col xs={24} md={8}>
                          <Form.Item name="effectCollectibleId" label="收集物">
                            <Select
                              showSearch
                              optionFilterProp="label"
                              options={collectibles.map((item: CollectibleItem) => ({
                                label: `${item.nameZht || item.nameZh} (${item.collectibleCode})`,
                                value: item.id,
                              }))}
                            />
                          </Form.Item>
                        </Col>
                      ) : null}
                      {effectPresetType === 'grant_badge' ? (
                        <Col xs={24} md={8}>
                          <Form.Item name="effectBadgeId" label="徽章 / 稱號">
                            <Select
                              showSearch
                              optionFilterProp="label"
                              options={badges.map((item: BadgeItem) => ({
                                label: `${item.nameZht || item.nameZh} (${item.badgeCode})`,
                                value: item.id,
                              }))}
                            />
                          </Form.Item>
                        </Col>
                      ) : null}
                      {effectPresetType === 'grant_reward' ? (
                        <Col xs={24} md={8}>
                          <Form.Item name="effectRewardId" label="遊戲內獎勵">
                            <Select
                              showSearch
                              optionFilterProp="label"
                              options={rewards.map((item: AdminRewardItem) => ({
                                label: `${item.nameZht || item.nameZh} (${item.code})`,
                                value: item.id,
                              }))}
                            />
                          </Form.Item>
                        </Col>
                      ) : null}
                      {effectPresetType === 'progress_update' ? (
                        <>
                          <Col xs={24} md={8}>
                            <Form.Item name="effectProgressMetric" label="進度欄位">
                              <Input placeholder="例如：city_progress.macau" />
                            </Form.Item>
                          </Col>
                          <Col xs={24} md={8}>
                            <Form.Item name="effectProgressValue" label="增加值">
                              <InputNumber min={0} style={{ width: '100%' }} />
                            </Form.Item>
                          </Col>
                        </>
                      ) : null}
                    </Row>

                    {effectPresetType === 'fullscreen_media' ? (
                      <MediaAssetPickerField
                        name="effectMediaAssetId"
                        label="全屏媒體資源"
                        valueMode="asset-id"
                      />
                    ) : null}

                    {effectPresetType === 'play_audio' ? (
                      <MediaAssetPickerField
                        name="effectAudioAssetId"
                        label="音效資源"
                        valueMode="asset-id"
                        assetKind="audio"
                      />
                    ) : null}

                    {effectPresetType === 'show_modal' ? (
                      <>
                        <Form.Item name="effectModalTitle" label="彈窗標題">
                          <Input />
                        </Form.Item>
                        <Form.Item name="effectModalBody" label="彈窗內容">
                          <Input.TextArea rows={3} />
                        </Form.Item>
                      </>
                    ) : null}

                    {effectPresetType === 'custom' ? (
                      <Form.Item name="effectRawJson" label="自定義 JSON">
                        <Input.TextArea rows={4} />
                      </Form.Item>
                    ) : null}
                  </Card>

                  <LocalizedFieldGroup
                    form={form}
                    label="成就文案"
                    fieldNames={achievementFields}
                    translationDefaults={translationDefaults}
                  />

                  <LocalizedFieldGroup
                    form={form}
                    label="收集物提示"
                    fieldNames={collectibleFields}
                    translationDefaults={translationDefaults}
                  />

                  <LocalizedFieldGroup
                    form={form}
                    label="地點名稱"
                    fieldNames={locationFields}
                    translationDefaults={translationDefaults}
                  />
                </Form>
              </Card>
            </Col>
          </Row>
        )}
      </Space>
    </PageContainer>
  );
};

export default StoryChapterWorkbench;
