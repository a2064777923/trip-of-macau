import React, { useEffect, useState } from 'react';
import type { Dayjs } from 'dayjs';
import { PageContainer } from '@ant-design/pro-layout';
import {
  Alert,
  Button,
  Card,
  Collapse,
  Col,
  DatePicker,
  Descriptions,
  Empty,
  Input,
  InputNumber,
  List,
  Modal,
  Row,
  Select,
  Space,
  Spin,
  Statistic,
  Switch,
  Table,
  Tabs,
  Tag,
  Typography,
  message,
} from 'antd';
import type { ColumnsType } from 'antd/es/table';
import {
  ArrowLeftOutlined,
  CopyOutlined,
  EyeOutlined,
  ReloadOutlined,
  ToolOutlined,
} from '@ant-design/icons';
import { useNavigate, useParams } from 'react-router-dom';
import {
  applyAdminUserProgressRepair,
  confirmAdminUserProgressRecompute,
  getAdminTravelerProgressBreakdown,
  getAdminTravelerProgressWorkbench,
  getAdminTravelerRewardRuleTrace,
  getAdminTravelerRewardState,
  getAdminTravelerTimeline,
  getAdminUserProgressAudits,
  previewAdminUserProgressRecompute,
  previewAdminUserProgressRepair,
} from '../../services/api';
import type {
  AdminLegacyProgressSnapshot,
  AdminTravelerProgressWorkbench,
  AdminTravelerBackpackItem,
  AdminTravelerGameRewardStateItem,
  AdminTravelerRewardRuleTrace,
  AdminTravelerRewardState,
  AdminTravelerRewardRedemptionSummary,
  AdminTravelerRuleTraceGrantNode,
  AdminTravelerRuleTraceRuleNode,
  AdminTravelerTimelineEntry,
  AdminTravelerTitleStateItem,
  AdminUserProgressAuditEntry,
  AdminUserProgressBreakdown,
  AdminUserProgressBreakdownElement,
  AdminUserProgressOperationPreview,
  AdminUserProgressOperationResult,
  AdminUserProgressSummary,
} from '../../types/admin';
import './UserProgressWorkbench.css';

const { Paragraph, Text, Title } = Typography;

type CompletionFilter = 'all' | 'completed' | 'pending';

type MergedTimelineEntry = {
  key: string;
  occurredAt?: string | null;
  kind: 'timeline' | 'audit';
  typeLabel: string;
  title: string;
  summary: string;
  sourceLabel: string;
  storylineId?: number | null;
  storylineName?: string | null;
  chapterId?: number | null;
  chapterName?: string | null;
  poiId?: number | null;
  poiName?: string | null;
  status?: string | null;
  rewardType?: string | null;
  rewardId?: number | null;
  gameRewardId?: number | null;
  payloadTitle?: string;
  payloadContent?: string;
};

type RangeValue = [Dayjs | null, Dayjs | null] | null;

function formatDateTime(value?: string | null) {
  if (!value) {
    return '暫無資料';
  }
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return value;
  }
  return date.toLocaleString('zh-HK', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: false,
  });
}

function formatPercent(value?: number | null) {
  if (typeof value !== 'number' || Number.isNaN(value)) {
    return '0.00%';
  }
  return `${value.toFixed(2)}%`;
}

function formatScopeTypeLabel(scopeType?: string | null) {
  switch (scopeType) {
    case 'global':
      return '全域';
    case 'city':
      return '城市';
    case 'sub_map':
      return '子地圖';
    case 'poi':
      return 'POI';
    case 'indoor_building':
      return '室內建築';
    case 'indoor_floor':
      return '室內樓層';
    case 'storyline':
      return '故事線';
    case 'story_chapter':
      return '故事章節';
    case 'task':
      return '任務';
    case 'collectible':
      return '收集物';
    case 'reward':
      return '獎勵';
    case 'media':
      return '媒體';
    default:
      return scopeType || '未標記';
  }
}

function formatTimelineTypeLabel(entryType: string) {
  switch (entryType) {
    case 'checkin':
      return '打卡';
    case 'exploration_event':
      return '探索事件';
    case 'storyline_session':
      return '故事 Session';
    case 'reward_redemption':
      return '獎勵兌換';
    case 'trigger_log':
      return '觸發紀錄';
    case 'audit':
      return '手動操作審計';
    default:
      return entryType;
  }
}

function formatStatusLabel(status?: string | null) {
  switch (status) {
    case 'completed':
      return '已完成';
    case 'active':
      return '進行中';
    case 'exited':
      return '已退出';
    case 'published':
      return '已發佈';
    case 'voided':
      return '已作廢';
    case 'ignored_duplicate':
      return '已標記重複';
    case 'granted':
      return '已發放';
    case 'redeemed':
      return '已兌換';
    default:
      return status || '未標記';
  }
}

function formatRewardTypeLabel(rewardType?: string | null) {
  switch (rewardType) {
    case 'badge':
      return '徽章';
    case 'title':
      return '稱號';
    case 'city_currency':
      return '城市貨幣';
    case 'city_fragment':
      return '城市碎片';
    case 'voice_pack':
      return '語音包';
    case 'unlock_pass':
      return '解鎖通行證';
    case 'redeemable':
      return '兌換獎勵';
    case 'collectible':
      return '收集物';
    default:
      return rewardType || '未標記';
  }
}

function formatRepairActionLabel(actionType: string) {
  switch (actionType) {
    case 'LINK_ORPHAN_EVENT':
      return '補連孤兒事件';
    case 'MARK_DUPLICATE_CLIENT_EVENT':
    case 'VOID_DUPLICATE_EVENT':
      return '標記重複事件';
    case 'RESEND_REWARD':
      return '補發獎勵';
    case 'ANNOTATE_ISSUE':
      return '留下問題註記';
    default:
      return actionType;
  }
}

function stringifyPayload(value?: string | Record<string, unknown> | null) {
  if (!value) {
    return '';
  }
  if (typeof value === 'string') {
    return value;
  }
  return JSON.stringify(value, null, 2);
}

function ellipsisText(value?: string | number | null) {
  if (value === undefined || value === null || value === '') {
    return '暫無';
  }
  return <span className="traveler-support-ellipsis experience-code-text" title={String(value)}>{String(value)}</span>;
}

function computePercentDelta(legacy: AdminLegacyProgressSnapshot, currentSummary?: AdminUserProgressSummary | null) {
  if (typeof legacy.legacyPercentValue !== 'number' || typeof currentSummary?.progressPercent !== 'number') {
    return null;
  }
  return legacy.legacyPercentValue - currentSummary.progressPercent;
}

function JsonDetailCard(props: {
  label: string;
  value?: string | Record<string, unknown> | null;
  onView: (title: string, content: string) => void;
  compact?: boolean;
}) {
  const content = stringifyPayload(props.value);
  if (!content) {
    return <Text type="secondary">暫無詳細內容</Text>;
  }

  return (
    <Space wrap>
      <Button size="small" icon={<EyeOutlined />} onClick={() => props.onView(props.label, content)}>
        {props.compact ? '查看詳情' : '查看內容'}
      </Button>
      <Button
        size="small"
        icon={<CopyOutlined />}
        onClick={async () => {
          try {
            await navigator.clipboard.writeText(content);
            message.success(`${props.label} 已複製`);
          } catch (error) {
            message.error('複製失敗');
          }
        }}
      >
        複製 JSON
      </Button>
      {props.compact ? null : (
        <Collapse
          size="small"
          items={[
            {
              key: 'payload',
              label: '內嵌預覽',
              children: (
                <Paragraph
                  style={{ marginBottom: 0, whiteSpace: 'pre-wrap', maxHeight: 220, overflow: 'auto' }}
                >
                  {content}
                </Paragraph>
              ),
            },
          ]}
        />
      )}
    </Space>
  );
}

const UserProgressWorkbench: React.FC = () => {
  const navigate = useNavigate();
  const params = useParams<{ userId: string }>();
  const userId = Number(params.userId);
  const hasValidUserId = Number.isFinite(userId) && userId > 0;

  const [refreshKey, setRefreshKey] = useState(0);
  const [workbenchLoading, setWorkbenchLoading] = useState(false);
  const [workbench, setWorkbench] = useState<AdminTravelerProgressWorkbench | null>(null);
  const [breakdownLoading, setBreakdownLoading] = useState(false);
  const [breakdown, setBreakdown] = useState<AdminUserProgressBreakdown | null>(null);
  const [timelineLoading, setTimelineLoading] = useState(false);
  const [timelineEntries, setTimelineEntries] = useState<AdminTravelerTimelineEntry[]>([]);
  const [auditsLoading, setAuditsLoading] = useState(false);
  const [auditEntries, setAuditEntries] = useState<AdminUserProgressAuditEntry[]>([]);
  const [rewardStateLoading, setRewardStateLoading] = useState(false);
  const [rewardState, setRewardState] = useState<AdminTravelerRewardState | null>(null);
  const [ruleTraceLoading, setRuleTraceLoading] = useState(false);
  const [ruleTrace, setRuleTrace] = useState<AdminTravelerRewardRuleTrace | null>(null);
  const [payloadViewer, setPayloadViewer] = useState<{ title: string; content: string } | null>(null);

  const [selectedScopeType, setSelectedScopeType] = useState('global');
  const [selectedScopeId, setSelectedScopeId] = useState<number | undefined>();
  const [selectedStorylineId, setSelectedStorylineId] = useState<number | undefined>();
  const [timelineChapterId, setTimelineChapterId] = useState<number | undefined>();
  const [timelinePoiId, setTimelinePoiId] = useState<number | undefined>();
  const [timelineMapScopeType, setTimelineMapScopeType] = useState<string | undefined>();
  const [timelineMapScopeId, setTimelineMapScopeId] = useState<number | undefined>();
  const [timelineStatus, setTimelineStatus] = useState<string | undefined>();
  const [timelineRewardType, setTimelineRewardType] = useState<string | undefined>();
  const [timelineRange, setTimelineRange] = useState<RangeValue>(null);
  const [completionFilter, setCompletionFilter] = useState<CompletionFilter>('all');
  const [includeInactiveComparison, setIncludeInactiveComparison] = useState(false);
  const [timelineEventTypes, setTimelineEventTypes] = useState<string[]>([]);
  const [traceSourceEventId, setTraceSourceEventId] = useState<number | null>(null);
  const [traceRuleId, setTraceRuleId] = useState<number | null>(null);
  const [traceRewardId, setTraceRewardId] = useState<number | null>(null);
  const [traceGameRewardId, setTraceGameRewardId] = useState<number | null>(null);

  const [recomputeReason, setRecomputeReason] = useState('');
  const [recomputePreview, setRecomputePreview] = useState<AdminUserProgressOperationPreview | null>(null);
  const [recomputeConfirmText, setRecomputeConfirmText] = useState('');
  const [recomputeBusy, setRecomputeBusy] = useState(false);
  const [recomputeResult, setRecomputeResult] = useState<AdminUserProgressOperationResult | null>(null);

  const [repairActionType, setRepairActionType] = useState('LINK_ORPHAN_EVENT');
  const [repairTargetEventId, setRepairTargetEventId] = useState<number | null>(null);
  const [repairReplacementElementId, setRepairReplacementElementId] = useState<number | null>(null);
  const [repairReplacementElementCode, setRepairReplacementElementCode] = useState('');
  const [repairDuplicateOfEventId, setRepairDuplicateOfEventId] = useState<number | null>(null);
  const [repairRewardId, setRepairRewardId] = useState<number | null>(null);
  const [repairGameRewardId, setRepairGameRewardId] = useState<number | null>(null);
  const [repairRuleId, setRepairRuleId] = useState<number | null>(null);
  const [repairSourceEventId, setRepairSourceEventId] = useState<number | null>(null);
  const [repairAnnotationText, setRepairAnnotationText] = useState('');
  const [repairIssueSeverity, setRepairIssueSeverity] = useState('info');
  const [repairReason, setRepairReason] = useState('');
  const [repairConfirmText, setRepairConfirmText] = useState('');
  const [repairPreview, setRepairPreview] = useState<AdminUserProgressOperationPreview | null>(null);
  const [repairBusy, setRepairBusy] = useState(false);
  const [repairResult, setRepairResult] = useState<AdminUserProgressOperationResult | null>(null);

  useEffect(() => {
    if (!hasValidUserId) {
      return;
    }

    let cancelled = false;
    const loadWorkbench = async () => {
      setWorkbenchLoading(true);
      try {
        const response = await getAdminTravelerProgressWorkbench(userId);
        if (cancelled) {
          return;
        }
        if (response.success) {
          setWorkbench(response.data);
        } else {
          message.error(response.message || '無法載入旅客進度工作台');
        }
      } catch (error) {
        if (!cancelled) {
          message.error('載入旅客進度工作台失敗');
        }
      } finally {
        if (!cancelled) {
          setWorkbenchLoading(false);
        }
      }
    };

    void loadWorkbench();
    return () => {
      cancelled = true;
    };
  }, [hasValidUserId, refreshKey, userId]);

  useEffect(() => {
    if (!hasValidUserId) {
      return;
    }

    let cancelled = false;
    const loadRewardState = async () => {
      setRewardStateLoading(true);
      try {
        const response = await getAdminTravelerRewardState(userId);
        if (cancelled) {
          return;
        }
        if (response.success) {
          setRewardState(response.data);
        } else {
          message.error(response.message || '無法載入背包與獎勵狀態');
        }
      } catch (error) {
        if (!cancelled) {
          message.error('載入背包與獎勵狀態失敗');
        }
      } finally {
        if (!cancelled) {
          setRewardStateLoading(false);
        }
      }
    };

    void loadRewardState();
    return () => {
      cancelled = true;
    };
  }, [hasValidUserId, refreshKey, userId]);

  useEffect(() => {
    if (!hasValidUserId) {
      return;
    }

    let cancelled = false;
    const loadRuleTrace = async () => {
      setRuleTraceLoading(true);
      try {
        const response = await getAdminTravelerRewardRuleTrace(userId, {
          sourceEventId: traceSourceEventId || undefined,
          ruleId: traceRuleId || undefined,
          rewardId: traceRewardId || undefined,
          gameRewardId: traceGameRewardId || undefined,
        });
        if (cancelled) {
          return;
        }
        if (response.success) {
          setRuleTrace(response.data);
        } else {
          message.error(response.message || '無法載入規則追蹤');
        }
      } catch (error) {
        if (!cancelled) {
          message.error('載入規則追蹤失敗');
        }
      } finally {
        if (!cancelled) {
          setRuleTraceLoading(false);
        }
      }
    };

    void loadRuleTrace();
    return () => {
      cancelled = true;
    };
  }, [
    hasValidUserId,
    refreshKey,
    traceGameRewardId,
    traceRewardId,
    traceRuleId,
    traceSourceEventId,
    userId,
  ]);

  useEffect(() => {
    if (!workbench) {
      return;
    }

    const hasSelectedScopedSummary =
      selectedScopeType === 'global' ||
      workbench.dynamicProgress.scopedSummaries.some(
        (item) => item.scopeType === selectedScopeType && Number(item.scopeId ?? 0) === Number(selectedScopeId ?? 0),
      );

    if (!hasSelectedScopedSummary) {
      setSelectedScopeType('global');
      setSelectedScopeId(undefined);
    }

    if (
      selectedStorylineId &&
      !workbench.storylineSessions.some((session) => session.storylineId === selectedStorylineId)
    ) {
      setSelectedStorylineId(undefined);
    }
  }, [selectedScopeId, selectedScopeType, selectedStorylineId, workbench]);

  useEffect(() => {
    if (!hasValidUserId) {
      return;
    }

    let cancelled = false;
    const loadBreakdown = async () => {
      setBreakdownLoading(true);
      try {
        const response = await getAdminTravelerProgressBreakdown(userId, {
          scopeType: selectedScopeType,
          scopeId: selectedScopeId,
          includeInactiveElements: includeInactiveComparison,
        });
        if (cancelled) {
          return;
        }
        if (response.success) {
          setBreakdown(response.data);
        } else {
          message.error(response.message || '無法載入探索元素明細');
        }
      } catch (error) {
        if (!cancelled) {
          message.error('載入探索元素明細失敗');
        }
      } finally {
        if (!cancelled) {
          setBreakdownLoading(false);
        }
      }
    };

    void loadBreakdown();
    return () => {
      cancelled = true;
    };
  }, [hasValidUserId, includeInactiveComparison, refreshKey, selectedScopeId, selectedScopeType, userId]);

  useEffect(() => {
    if (!hasValidUserId) {
      return;
    }

    let cancelled = false;
    const loadTimeline = async () => {
      setTimelineLoading(true);
      try {
        const response = await getAdminTravelerTimeline(userId, {
          pageNum: 1,
          pageSize: 50,
          eventTypes: timelineEventTypes.filter((item) => item !== 'audit'),
          storylineId: selectedStorylineId,
          chapterId: timelineChapterId,
          poiId: timelinePoiId,
          mapScopeType: timelineMapScopeType,
          mapScopeId: timelineMapScopeId,
          status: timelineStatus,
          rewardType: timelineRewardType,
          from: timelineRange?.[0]?.format('YYYY-MM-DDTHH:mm:ss'),
          to: timelineRange?.[1]?.format('YYYY-MM-DDTHH:mm:ss'),
        });
        if (cancelled) {
          return;
        }
        if (response.success) {
          setTimelineEntries(response.data?.list || []);
        } else {
          message.error(response.message || '無法載入互動時間線');
        }
      } catch (error) {
        if (!cancelled) {
          message.error('載入互動時間線失敗');
        }
      } finally {
        if (!cancelled) {
          setTimelineLoading(false);
        }
      }
    };

    void loadTimeline();
    return () => {
      cancelled = true;
    };
  }, [
    hasValidUserId,
    refreshKey,
    selectedStorylineId,
    timelineChapterId,
    timelineEventTypes.join('|'),
    timelineMapScopeId,
    timelineMapScopeType,
    timelinePoiId,
    timelineRange?.[0]?.valueOf(),
    timelineRange?.[1]?.valueOf(),
    timelineRewardType,
    timelineStatus,
    userId,
  ]);

  useEffect(() => {
    if (!hasValidUserId) {
      return;
    }

    let cancelled = false;
    const loadAudits = async () => {
      setAuditsLoading(true);
      try {
        const response = await getAdminUserProgressAudits(userId, {
          pageNum: 1,
          pageSize: 50,
        });
        if (cancelled) {
          return;
        }
        if (response.success) {
          setAuditEntries(response.data?.list || []);
        } else {
          message.error(response.message || '無法載入審計紀錄');
        }
      } catch (error) {
        if (!cancelled) {
          message.error('載入審計紀錄失敗');
        }
      } finally {
        if (!cancelled) {
          setAuditsLoading(false);
        }
      }
    };

    void loadAudits();
    return () => {
      cancelled = true;
    };
  }, [hasValidUserId, refreshKey, userId]);

  const scopeOptions = workbench
    ? [
        {
          label: '全域',
          value: 'global:',
        },
        ...workbench.dynamicProgress.scopedSummaries.map((item) => ({
          label: `${formatScopeTypeLabel(item.scopeType)}${item.scopeName ? `｜${item.scopeName}` : ''}${item.scopeId ? ` (#${item.scopeId})` : ''}`,
          value: `${item.scopeType}:${item.scopeId ?? ''}`,
        })),
      ]
    : [];

  const storylineOptions = workbench
    ? Array.from(
        new Map(
          workbench.storylineSessions
            .filter((session) => session.storylineId)
            .map((session) => [
              session.storylineId as number,
              {
                label: session.storylineName || `故事線 #${session.storylineId}`,
                value: session.storylineId as number,
              },
            ]),
        ).values(),
      )
    : [];

  const activeScopeSummary =
    selectedScopeType === 'global'
      ? workbench?.dynamicProgress.globalSummary || null
      : workbench?.dynamicProgress.scopedSummaries.find(
          (item) => item.scopeType === selectedScopeType && Number(item.scopeId ?? 0) === Number(selectedScopeId ?? 0),
        )?.summary || null;

  const activeScopeName =
    selectedScopeType === 'global'
      ? '全域'
      : workbench?.dynamicProgress.scopedSummaries.find(
          (item) => item.scopeType === selectedScopeType && Number(item.scopeId ?? 0) === Number(selectedScopeId ?? 0),
        )?.scopeName || formatScopeTypeLabel(selectedScopeType);

  const filteredElements = (breakdown?.elements || []).filter((item) => {
    if (completionFilter === 'completed') {
      return item.completed;
    }
    if (completionFilter === 'pending') {
      return !item.completed;
    }
    return true;
  });

  const filteredRetiredElements = includeInactiveComparison
    ? (breakdown?.retiredElements || []).filter((item) => {
        if (completionFilter === 'pending') {
          return false;
        }
        return true;
      })
    : [];

  const mergedTimelineEntries: MergedTimelineEntry[] = [
    ...timelineEntries.map((entry) => ({
      key: `timeline-${entry.entryId}`,
      occurredAt: entry.occurredAt,
      kind: 'timeline' as const,
      typeLabel: formatTimelineTypeLabel(entry.entryType),
      title: entry.title || entry.poiName || entry.storylineName || entry.entryType,
      summary: entry.summary || entry.payloadPreview || '暫無摘要',
      sourceLabel: entry.sourceTable || 'timeline',
      storylineId: entry.storylineId,
      storylineName: entry.storylineName,
      chapterId: entry.chapterId,
      chapterName: entry.chapterName,
      poiId: entry.poiId,
      poiName: entry.poiName,
      status: entry.status,
      rewardType: entry.rewardType,
      rewardId: entry.rewardId,
      gameRewardId: entry.gameRewardId,
      payloadTitle: `${formatTimelineTypeLabel(entry.entryType)} 詳細內容`,
      payloadContent: stringifyPayload(entry.rawPayload || entry.payloadPreview),
    })),
    ...auditEntries.map((entry) => ({
      key: `audit-${entry.id}`,
      occurredAt: entry.timestamp,
      kind: 'audit' as const,
      typeLabel: formatTimelineTypeLabel('audit'),
      title: `${entry.actionType}｜${entry.operatorName || '未知操作員'}`,
      summary: entry.reason || '未填寫原因',
      sourceLabel: 'user_progress_operation_audits',
      storylineId: entry.storylineId,
      storylineName:
        storylineOptions.find((item) => item.value === entry.storylineId)?.label || undefined,
      status: entry.actionType,
      payloadTitle: '審計詳細內容',
      payloadContent: stringifyPayload({
        previewSummary: entry.previewSummary || {},
        resultSummary: entry.resultSummary || {},
      }),
    })),
  ]
    .filter((entry) => {
      if (!selectedStorylineId) {
        return true;
      }
      return Number(entry.storylineId ?? 0) === Number(selectedStorylineId);
    })
    .filter((entry) => {
      if (timelineEventTypes.length === 0) {
        return true;
      }
      if (entry.kind === 'audit') {
        return timelineEventTypes.includes('audit');
      }
      const originalType = timelineEntries.find((item) => `timeline-${item.entryId}` === entry.key)?.entryType;
      return originalType ? timelineEventTypes.includes(originalType) : true;
    })
    .sort((left, right) => {
      const leftTime = left.occurredAt ? new Date(left.occurredAt).getTime() : 0;
      const rightTime = right.occurredAt ? new Date(right.occurredAt).getTime() : 0;
      return rightTime - leftTime;
    });

  const timelineEventTypeOptions = Array.from(
    new Set([
      'storyline_session',
      'exploration_event',
      'reward_redemption',
      'checkin',
      'trigger_log',
      ...timelineEntries.map((item) => item.entryType),
      ...(auditEntries.length > 0 ? ['audit'] : []),
    ]),
  ).map((item) => ({
    label: formatTimelineTypeLabel(item),
    value: item,
  }));

  const timelineStatusOptions = Array.from(
    new Set([
      'completed',
      'active',
      'exited',
      'granted',
      'redeemed',
      'ignored_duplicate',
      ...timelineEntries.map((item) => item.status).filter(Boolean),
    ]),
  ).map((item) => ({
    label: formatStatusLabel(item),
    value: item as string,
  }));

  const timelineRewardTypeOptions = Array.from(
    new Set([
      'badge',
      'title',
      'city_currency',
      'city_fragment',
      'voice_pack',
      'redeemable',
      ...timelineEntries.map((item) => item.rewardType).filter(Boolean),
    ]),
  ).map((item) => ({
    label: formatRewardTypeLabel(item),
    value: item as string,
  }));

  const mapScopeTypeOptions = [
    { label: '城市', value: 'city' },
    { label: '大地圖 / 子地圖', value: 'sub_map' },
    { label: 'POI', value: 'poi' },
    { label: '故事線', value: 'storyline' },
    { label: '章節', value: 'story_chapter' },
    { label: '室內建築', value: 'indoor_building' },
    { label: '室內樓層', value: 'indoor_floor' },
  ];

  const resetTraceFilters = () => {
    setTraceSourceEventId(null);
    setTraceRuleId(null);
    setTraceRewardId(null);
    setTraceGameRewardId(null);
  };

  const elementColumns: ColumnsType<AdminUserProgressBreakdownElement> = [
    {
      title: '元素',
      dataIndex: 'title',
      key: 'title',
      render: (_, record) => (
        <Space direction="vertical" size={0}>
          <Text strong>{record.title || record.elementCode}</Text>
          <Text type="secondary">
            {record.elementCode} · {record.elementType || '未分類'}
          </Text>
        </Space>
      ),
    },
    {
      title: '權重',
      dataIndex: 'weightValue',
      key: 'weightValue',
      render: (_, record) => (
        <Tag color="blue">
          {record.weightLevel || '未標記'} / {record.weightValue}
        </Tag>
      ),
    },
    {
      title: '完成狀態',
      dataIndex: 'completed',
      key: 'completed',
      render: (value) => (value ? <Tag color="success">已完成</Tag> : <Tag>未完成</Tag>),
    },
    {
      title: '百分比計算',
      dataIndex: 'includedInCurrentPercentage',
      key: 'includedInCurrentPercentage',
      render: (value) =>
        value ? (
          <Tag color="processing">納入目前加權百分比</Tag>
        ) : (
          <Tag color="warning">已退役，不計入目前百分比</Tag>
        ),
    },
    {
      title: '來源事件',
      dataIndex: 'sourceEventId',
      key: 'sourceEventId',
      render: (_, record) => (
        <Space direction="vertical" size={0}>
          <Text>{record.sourceEventId ? `#${record.sourceEventId}` : '暫無'}</Text>
          <Text type="secondary">{formatDateTime(record.eventOccurredAt)}</Text>
        </Space>
      ),
    },
  ];

  const sessionColumns: ColumnsType<NonNullable<AdminTravelerProgressWorkbench['storylineSessions']>[number]> = [
    {
      title: 'Session',
      dataIndex: 'sessionId',
      key: 'sessionId',
      render: (value) => <Text code>{value}</Text>,
    },
    {
      title: '故事線',
      dataIndex: 'storylineName',
      key: 'storylineName',
      render: (_, record) => (
        <Space direction="vertical" size={0}>
          <Text strong>{record.storylineName || `故事線 #${record.storylineId}`}</Text>
          <Text type="secondary">章節 #{record.currentChapterId || '未設定'}</Text>
        </Space>
      ),
    },
    {
      title: '狀態',
      dataIndex: 'status',
      key: 'status',
      render: (value) => (
        <Tag color={value === 'active' ? 'success' : value === 'exited' ? 'default' : 'processing'}>
          {value || 'unknown'}
        </Tag>
      ),
    },
    {
      title: '事件數',
      dataIndex: 'eventCount',
      key: 'eventCount',
    },
    {
      title: '時間',
      key: 'time',
      render: (_, record) => (
        <Space direction="vertical" size={0}>
          <Text>開始：{formatDateTime(record.startedAt)}</Text>
          <Text type="secondary">最後事件：{formatDateTime(record.lastEventAt)}</Text>
          <Text type="secondary">退出：{formatDateTime(record.exitedAt)}</Text>
        </Space>
      ),
    },
    {
      title: '臨時狀態',
      key: 'temporaryState',
      render: (_, record) => (
        <JsonDetailCard
          label={`Session ${record.sessionId}`}
          value={record.temporaryStepStateJson}
          onView={(title, content) => setPayloadViewer({ title, content })}
        />
      ),
    },
  ];

  const rewardColumns: ColumnsType<AdminTravelerRewardRedemptionSummary> = [
    {
      title: '獎勵',
      dataIndex: 'rewardName',
      key: 'rewardName',
      render: (_, record) => (
        <Space direction="vertical" size={0}>
          <Text strong>{record.rewardName || `獎勵 #${record.rewardId}`}</Text>
          <Text type="secondary">Redemption #{record.redemptionId}</Text>
        </Space>
      ),
    },
    {
      title: '狀態',
      dataIndex: 'redemptionStatus',
      key: 'redemptionStatus',
      render: (value) => <Tag color="gold">{value || 'unknown'}</Tag>,
    },
    {
      title: '印章成本',
      dataIndex: 'stampCostSnapshot',
      key: 'stampCostSnapshot',
      render: (value) => value ?? '暫無',
    },
    {
      title: '兌換時間',
      dataIndex: 'redeemedAt',
      key: 'redeemedAt',
      render: (value) => formatDateTime(value),
    },
    {
      title: '到期時間',
      dataIndex: 'expiresAt',
      key: 'expiresAt',
      render: (value) => formatDateTime(value),
    },
  ];

  const backpackColumns: ColumnsType<AdminTravelerBackpackItem> = [
    {
      title: '收集物',
      key: 'name',
      render: (_, record) => (
        <Space direction="vertical" size={0}>
          <Text strong>{record.name || record.code || `來源 #${record.sourceId}`}</Text>
          <Text type="secondary">{ellipsisText(record.code || record.description)}</Text>
        </Space>
      ),
    },
    {
      title: '數量 / 稀有度',
      key: 'quantity',
      render: (_, record) => (
        <Space wrap>
          <Tag color="blue">x{record.quantity ?? 1}</Tag>
          <Tag>{record.rarity || '未標記'}</Tag>
        </Space>
      ),
    },
    {
      title: '來源',
      key: 'source',
      render: (_, record) => (
        <Space direction="vertical" size={0}>
          <Text>{record.sourceType || '未知來源'}</Text>
          <Text type="secondary">{ellipsisText(record.sourceId ? `#${record.sourceId}` : record.sourceEventId ? `事件 #${record.sourceEventId}` : '')}</Text>
        </Space>
      ),
    },
    {
      title: '取得時間',
      dataIndex: 'earnedAt',
      key: 'earnedAt',
      render: (value) => formatDateTime(value),
    },
  ];

  const gameRewardColumns: ColumnsType<AdminTravelerGameRewardStateItem> = [
    {
      title: '獎勵',
      key: 'name',
      render: (_, record) => (
        <Space direction="vertical" size={0}>
          <Text strong>{record.name || record.code || `獎勵 #${record.rewardId}`}</Text>
          <Text type="secondary">{ellipsisText(record.code)}</Text>
        </Space>
      ),
    },
    {
      title: '類型 / 稀有度',
      key: 'type',
      render: (_, record) => (
        <Space wrap>
          <Tag color={record.rewardType === 'title' ? 'purple' : 'gold'}>{formatRewardTypeLabel(record.rewardType)}</Tag>
          <Tag>{record.rarity || '未標記'}</Tag>
        </Space>
      ),
    },
    {
      title: '規則追蹤',
      key: 'trace',
      render: (_, record) => (
        <Button
          size="small"
          onClick={() => {
            setTraceRewardId(record.rewardId || null);
            setTraceGameRewardId(record.rewardId || null);
            setTraceRuleId(record.sourceRuleId || null);
            setTraceSourceEventId(record.sourceEventId || null);
          }}
        >
          查看規則追蹤
        </Button>
      ),
    },
    {
      title: '取得時間',
      dataIndex: 'earnedAt',
      key: 'earnedAt',
      render: (value) => formatDateTime(value),
    },
  ];

  const titleColumns: ColumnsType<AdminTravelerTitleStateItem> = [
    {
      title: '稱號',
      key: 'name',
      render: (_, record) => (
        <Space direction="vertical" size={0}>
          <Text strong>{record.name || record.code || `稱號 #${record.rewardId}`}</Text>
          <Text type="secondary">{ellipsisText(record.code)}</Text>
        </Space>
      ),
    },
    {
      title: '狀態',
      key: 'status',
      render: (_, record) => (
        <Space wrap>
          <Tag color={record.equipped ? 'success' : 'default'}>{record.equipped ? '已裝備' : '未裝備'}</Tag>
          <Tag>{formatStatusLabel(record.status)}</Tag>
        </Space>
      ),
    },
    {
      title: '規則追蹤',
      key: 'trace',
      render: (_, record) => (
        <Button
          size="small"
          onClick={() => {
            setTraceRewardId(record.rewardId || null);
            setTraceGameRewardId(record.rewardId || null);
            setTraceRuleId(record.sourceRuleId || null);
            setTraceSourceEventId(record.sourceEventId || null);
          }}
        >
          查看規則追蹤
        </Button>
      ),
    },
    {
      title: '取得時間',
      dataIndex: 'earnedAt',
      key: 'earnedAt',
      render: (value) => formatDateTime(value),
    },
  ];

  const ruleTraceRuleColumns: ColumnsType<AdminTravelerRuleTraceRuleNode> = [
    {
      title: '規則',
      key: 'rule',
      render: (_, record) => (
        <Space direction="vertical" size={0}>
          <Text strong>{record.name || record.code || `規則 #${record.ruleId}`}</Text>
          <Text type="secondary">{ellipsisText(record.code)}</Text>
        </Space>
      ),
    },
    {
      title: '狀態',
      dataIndex: 'status',
      key: 'status',
      render: (value) => <Tag>{formatStatusLabel(value)}</Tag>,
    },
    {
      title: '綁定',
      key: 'binding',
      render: (_, record) => (
        <Space direction="vertical" size={0}>
          <Text>{record.bindingOwnerDomain || '未綁定'}</Text>
          <Text type="secondary">{record.bindingOwnerId ? `#${record.bindingOwnerId}` : record.bindingRole || '暫無'}</Text>
        </Space>
      ),
    },
    {
      title: '條件摘要',
      key: 'conditions',
      render: (_, record) => (
        <Space direction="vertical" size={4}>
          {(record.conditionGroups || []).slice(0, 3).map((group) => (
            <Text key={group.groupId || group.groupCode} type="secondary">
              {group.summaryText || group.groupCode || group.operatorType || '未命名條件組'}
            </Text>
          ))}
          {(record.conditionGroups || []).length === 0 ? <Text type="secondary">暫無條件組</Text> : null}
        </Space>
      ),
    },
  ];

  const ruleTraceGrantColumns: ColumnsType<AdminTravelerRuleTraceGrantNode> = [
    {
      title: '發放來源',
      dataIndex: 'grantSource',
      key: 'grantSource',
      render: (value) => <Tag color="gold">{value || '未知'}</Tag>,
    },
    {
      title: '獎勵',
      key: 'reward',
      render: (_, record) => (
        <Space direction="vertical" size={0}>
          <Text>{record.rewardId ? `兌換獎勵 #${record.rewardId}` : '無兌換獎勵'}</Text>
          <Text type="secondary">{record.gameRewardId ? `遊戲內獎勵 #${record.gameRewardId}` : '無遊戲獎勵'}</Text>
        </Space>
      ),
    },
    {
      title: '狀態',
      dataIndex: 'grantStatus',
      key: 'grantStatus',
      render: (value) => <Tag>{formatStatusLabel(value)}</Tag>,
    },
    {
      title: '發放時間',
      dataIndex: 'grantedAt',
      key: 'grantedAt',
      render: (value) => formatDateTime(value),
    },
  ];

  const mergedTimelineColumns: ColumnsType<MergedTimelineEntry> = [
    {
      title: '時間',
      dataIndex: 'occurredAt',
      key: 'occurredAt',
      width: 180,
      render: (value) => formatDateTime(value),
    },
    {
      title: '類型',
      dataIndex: 'typeLabel',
      key: 'typeLabel',
      width: 160,
      render: (value, record) => (
        <Space direction="vertical" size={0}>
          <Tag color={record.kind === 'audit' ? 'volcano' : 'processing'}>{value}</Tag>
          <Text type="secondary">{record.sourceLabel}</Text>
        </Space>
      ),
    },
    {
      title: '內容',
      key: 'content',
      render: (_, record) => (
        <Space direction="vertical" size={4} style={{ width: '100%' }}>
          <Text strong>{record.title}</Text>
          <Text>{record.summary}</Text>
          {record.storylineName ? <Text type="secondary">故事線：{record.storylineName}</Text> : null}
          {record.chapterName || record.chapterId ? (
            <Text type="secondary">章節：{record.chapterName || `#${record.chapterId}`}</Text>
          ) : null}
          {record.poiName || record.poiId ? (
            <Text type="secondary">POI：{record.poiName || `#${record.poiId}`}</Text>
          ) : null}
          <Space wrap>
            {record.status ? <Tag>{formatStatusLabel(record.status)}</Tag> : null}
            {record.rewardType ? <Tag color="gold">{formatRewardTypeLabel(record.rewardType)}</Tag> : null}
            {record.rewardId ? <Tag>Reward #{record.rewardId}</Tag> : null}
            {record.gameRewardId ? <Tag>Game #{record.gameRewardId}</Tag> : null}
          </Space>
        </Space>
      ),
    },
    {
      title: '詳細',
      key: 'details',
      width: 260,
      render: (_, record) => (
        <JsonDetailCard
          label={record.payloadTitle || record.title}
          value={record.payloadContent}
          onView={(title, content) => setPayloadViewer({ title, content })}
          compact
        />
      ),
    },
  ];

  const auditColumns: ColumnsType<AdminUserProgressAuditEntry> = [
    {
      title: '時間',
      dataIndex: 'timestamp',
      key: 'timestamp',
      width: 180,
      render: (value) => formatDateTime(value),
    },
    {
      title: '操作',
      dataIndex: 'actionType',
      key: 'actionType',
      render: (value, record) => (
        <Space direction="vertical" size={0}>
          <Tag color="volcano">{value}</Tag>
          <Text type="secondary">{record.operatorName || '未知操作員'}</Text>
        </Space>
      ),
    },
    {
      title: '範圍',
      key: 'scope',
      render: (_, record) => (
        <Space direction="vertical" size={0}>
          <Text>{formatScopeTypeLabel(record.scopeType)}</Text>
          <Text type="secondary">
            {record.scopeId ? `#${record.scopeId}` : '全域'}
            {record.storylineId ? ` · 故事線 #${record.storylineId}` : ''}
          </Text>
        </Space>
      ),
    },
    {
      title: '原因',
      dataIndex: 'reason',
      key: 'reason',
      render: (value) => value || '未填寫原因',
    },
    {
      title: '詳細',
      key: 'details',
      width: 260,
      render: (_, record) => (
        <JsonDetailCard
          label={`審計 #${record.id}`}
          value={{
            previewSummary: record.previewSummary || {},
            resultSummary: record.resultSummary || {},
            requestIp: record.requestIp,
          }}
          onView={(title, content) => setPayloadViewer({ title, content })}
          compact
        />
      ),
    },
  ];

  const openPayloadViewer = (title: string, content: string) => {
    setPayloadViewer({ title, content });
  };

  const handleScopeChange = (value: string) => {
    const [nextScopeType, rawScopeId] = value.split(':');
    setSelectedScopeType(nextScopeType || 'global');
    setSelectedScopeId(rawScopeId ? Number(rawScopeId) : undefined);
    setRecomputePreview(null);
    setRecomputeConfirmText('');
    setRepairPreview(null);
    setRepairConfirmText('');
  };

  const handleStorylineChange = (value?: number) => {
    setSelectedStorylineId(value);
    if (!value || !workbench) {
      return;
    }
    const storylineScope = workbench.dynamicProgress.scopedSummaries.find(
      (item) => item.scopeType === 'storyline' && Number(item.scopeId ?? 0) === Number(value),
    );
    if (storylineScope) {
      setSelectedScopeType('storyline');
      setSelectedScopeId(value);
    }
  };

  const handlePreviewRecompute = async () => {
    if (!hasValidUserId) {
      return;
    }
    if (!recomputeReason.trim()) {
      message.warning('請先填寫重算原因');
      return;
    }
    setRecomputeBusy(true);
    try {
      const response = await previewAdminUserProgressRecompute(userId, {
        userId,
        scopeType: selectedScopeType,
        scopeId: selectedScopeId,
        storylineId: selectedStorylineId,
        reason: recomputeReason.trim(),
      });
      if (response.success) {
        setRecomputePreview(response.data);
        setRecomputeResult(null);
        Modal.info({
          title: '重新計算進度預覽',
          width: 720,
          content: (
            <JsonDetailCard
              label="重新計算進度預覽摘要"
              value={response.data?.previewSummary}
              onView={openPayloadViewer}
            />
          ),
        });
      } else {
        message.error(response.message || '無法產生操作預覽');
      }
    } catch (error) {
      message.error('無法產生操作預覽');
    } finally {
      setRecomputeBusy(false);
    }
  };

  const handleConfirmRecompute = async () => {
    if (!recomputePreview) {
      message.warning('請先完成預覽');
      return;
    }
    if (recomputeConfirmText.trim().toUpperCase() !== recomputePreview.confirmationText) {
      message.warning(`請輸入 ${recomputePreview.confirmationText} 以確認`);
      return;
    }
    setRecomputeBusy(true);
    try {
      const response = await confirmAdminUserProgressRecompute(userId, {
        userId,
        scopeType: selectedScopeType,
        scopeId: selectedScopeId,
        storylineId: selectedStorylineId,
        reason: recomputeReason.trim(),
        previewHash: recomputePreview.previewHash,
        confirmationText: 'RECOMPUTE',
      });
      if (response.success) {
        setRecomputeResult(response.data);
        setRecomputePreview(null);
        setRecomputeConfirmText('');
        setRefreshKey((value) => value + 1);
        message.success('支援操作已寫入審計');
      } else {
        message.error(response.message || '支援操作未完成');
      }
    } catch (error) {
      message.error('支援操作未完成');
    } finally {
      setRecomputeBusy(false);
    }
  };

  const buildRepairPayload = (withConfirmation = false) => ({
    userId,
    scopeType: selectedScopeType,
    scopeId: selectedScopeId,
    storylineId: selectedStorylineId,
    actionType: repairActionType,
    targetEventId: repairTargetEventId || undefined,
    replacementElementId: repairReplacementElementId || undefined,
    replacementElementCode: repairReplacementElementCode.trim() || undefined,
    duplicateOfEventId: repairDuplicateOfEventId || undefined,
    rewardId: repairRewardId || undefined,
    gameRewardId: repairGameRewardId || undefined,
    ruleId: repairRuleId || undefined,
    sourceEventId: repairSourceEventId || undefined,
    annotationText: repairAnnotationText.trim() || undefined,
    issueSeverity: repairIssueSeverity || undefined,
    reason: repairReason.trim(),
    previewHash: withConfirmation ? repairPreview?.previewHash : undefined,
    confirmationToken: withConfirmation ? repairPreview?.confirmationToken || repairPreview?.previewHash : undefined,
    confirmationText: withConfirmation ? ('REPAIR' as const) : undefined,
  });

  const handlePreviewRepair = async () => {
    if (!repairReason.trim()) {
      message.warning('請先填寫修復原因');
      return;
    }
    if (['VOID_DUPLICATE_EVENT', 'LINK_ORPHAN_EVENT', 'MARK_DUPLICATE_CLIENT_EVENT'].includes(repairActionType) && !repairTargetEventId) {
      message.warning('請填寫目標事件 ID');
      return;
    }
    if (repairActionType === 'RESEND_REWARD' && !repairRewardId && !repairGameRewardId) {
      message.warning('補發獎勵需要填寫 rewardId 或 gameRewardId');
      return;
    }
    if (repairActionType === 'ANNOTATE_ISSUE' && !repairAnnotationText.trim()) {
      message.warning('請填寫問題註記內容');
      return;
    }
    setRepairBusy(true);
    try {
      const response = await previewAdminUserProgressRepair(userId, buildRepairPayload());
      if (response.success) {
        setRepairPreview(response.data);
        setRepairResult(null);
        Modal.info({
          title: `${formatRepairActionLabel(repairActionType)}預覽`,
          width: 720,
          content: (
            <JsonDetailCard
              label={`${formatRepairActionLabel(repairActionType)}預覽摘要`}
              value={response.data?.previewSummary}
              onView={openPayloadViewer}
            />
          ),
        });
      } else {
        message.error(response.message || '無法產生操作預覽');
      }
    } catch (error) {
      message.error('無法產生操作預覽');
    } finally {
      setRepairBusy(false);
    }
  };

  const handleApplyRepair = async () => {
    if (!repairPreview) {
      message.warning('請先完成修復預覽');
      return;
    }
    if (repairConfirmText.trim().toUpperCase() !== repairPreview.confirmationText) {
      message.warning(`請輸入 ${repairPreview.confirmationText} 以確認`);
      return;
    }
    setRepairBusy(true);
    try {
      const response = await applyAdminUserProgressRepair(userId, buildRepairPayload(true));
      if (response.success) {
        setRepairResult(response.data);
        setRepairPreview(null);
        setRepairConfirmText('');
        setRefreshKey((value) => value + 1);
        message.success('支援操作已寫入審計');
      } else {
        message.error(response.message || '支援操作未完成');
      }
    } catch (error) {
      message.error('支援操作未完成');
    } finally {
      setRepairBusy(false);
    }
  };

  if (!hasValidUserId) {
    return (
      <PageContainer title="旅客進度工作台">
        <Alert
          type="error"
          showIcon
          message="無效的旅客 ID"
          description="請從旅客列表重新進入工作台。"
        />
      </PageContainer>
    );
  }

  const loading = workbenchLoading && !workbench;

  return (
    <PageContainer
      title={workbench?.identity.nickname ? `${workbench.identity.nickname} 的旅客進度工作台` : '旅客進度工作台'}
      subTitle={workbench ? `旅客 #${workbench.userId}` : `旅客 #${userId}`}
      extra={[
        <Button key="back" icon={<ArrowLeftOutlined />} onClick={() => navigate('/users/progress')}>
          返回旅客名單
        </Button>,
        <Button
          key="refresh"
          icon={<ReloadOutlined />}
          onClick={() => setRefreshKey((value) => value + 1)}
        >
          重新整理
        </Button>,
      ]}
    >
      {loading ? (
        <Card>
          <Spin />
        </Card>
      ) : !workbench ? (
        <Card>
          <Empty description="找不到旅客資料" />
        </Card>
      ) : (
        <Space direction="vertical" size={16} style={{ width: '100%' }}>
          <Card title="支援工作台分頁" className="traveler-support-tabs-card">
            <Tabs
              type="card"
              items={[
                {
                  key: 'overview',
                  label: '總覽',
                  children: '身份、偏好、關聯範圍與動態探索摘要集中在下方總覽區。',
                },
                {
                  key: 'sessions',
                  label: '故事 Session',
                  children: '檢視旅客正在進行或已退出的故事模式 Session。',
                },
                {
                  key: 'timeline',
                  label: '事件時間線',
                  children: '使用下方篩選器查找探索事件、打卡、獎勵、觸發與審計記錄。',
                },
                {
                  key: 'breakdown',
                  label: '探索度明細',
                  children: '按範圍查看動態探索元素分母、權重與完成來源。',
                },
                {
                  key: 'backpack',
                  label: '背包 / 收集物',
                  children: '查看已取得的拾取物、來源事件、稀有度與取得時間。',
                },
                {
                  key: 'rewards',
                  label: '獎勵與稱號',
                  children: '核對遊戲內獎勵、稱號與可兌換獎勵是否與公開端一致。',
                },
                {
                  key: 'trace',
                  label: '規則追蹤',
                  children: '按事件、規則或獎勵追蹤為何已發放、未發放或找不到綁定。',
                },
                {
                  key: 'ops',
                  label: '修復與審計',
                  children: '所有支援操作都先預覽，再輸入確認字樣後寫入審計。',
                },
              ]}
            />
          </Card>

          <Card title="時間線與規則上下文篩選" className="traveler-support-filter-card">
            <Row gutter={[12, 12]}>
              <Col xs={24} md={12} xl={6}>
                <Space direction="vertical" size={4} className="traveler-support-filter-wide">
                  <Text type="secondary">故事線</Text>
                  <Select
                    allowClear
                    placeholder="選擇故事線"
                    value={selectedStorylineId}
                    options={storylineOptions}
                    onChange={handleStorylineChange}
                    className="traveler-support-filter-wide"
                  />
                </Space>
              </Col>
              <Col xs={24} md={12} xl={6}>
                <Space direction="vertical" size={4} className="traveler-support-filter-field">
                  <Text type="secondary">章節 ID</Text>
                  <InputNumber
                    min={1}
                    value={timelineChapterId}
                    onChange={(value) => setTimelineChapterId(typeof value === 'number' ? value : undefined)}
                    className="traveler-support-filter-field"
                    placeholder="chapterId"
                  />
                </Space>
              </Col>
              <Col xs={24} md={12} xl={6}>
                <Space direction="vertical" size={4} className="traveler-support-filter-field">
                  <Text type="secondary">POI ID</Text>
                  <InputNumber
                    min={1}
                    value={timelinePoiId}
                    onChange={(value) => setTimelinePoiId(typeof value === 'number' ? value : undefined)}
                    className="traveler-support-filter-field"
                    placeholder="poiId"
                  />
                </Space>
              </Col>
              <Col xs={24} md={12} xl={6}>
                <Space direction="vertical" size={4} className="traveler-support-filter-field">
                  <Text type="secondary">地圖範圍類型</Text>
                  <Select
                    allowClear
                    placeholder="mapScopeType"
                    value={timelineMapScopeType}
                    options={mapScopeTypeOptions}
                    onChange={(value) => setTimelineMapScopeType(value)}
                    className="traveler-support-filter-field"
                  />
                </Space>
              </Col>
              <Col xs={24} md={12} xl={6}>
                <Space direction="vertical" size={4} className="traveler-support-filter-field">
                  <Text type="secondary">地圖範圍 ID</Text>
                  <InputNumber
                    min={1}
                    value={timelineMapScopeId}
                    onChange={(value) => setTimelineMapScopeId(typeof value === 'number' ? value : undefined)}
                    className="traveler-support-filter-field"
                    placeholder="mapScopeId"
                  />
                </Space>
              </Col>
              <Col xs={24} md={12} xl={6}>
                <Space direction="vertical" size={4} className="traveler-support-filter-wide">
                  <Text type="secondary">事件類型</Text>
                  <Select
                    mode="multiple"
                    allowClear
                    placeholder="選擇事件類型"
                    value={timelineEventTypes}
                    options={timelineEventTypeOptions}
                    onChange={(value) => setTimelineEventTypes(value)}
                    className="traveler-support-filter-wide"
                  />
                </Space>
              </Col>
              <Col xs={24} md={12} xl={6}>
                <Space direction="vertical" size={4} className="traveler-support-filter-field">
                  <Text type="secondary">狀態</Text>
                  <Select
                    allowClear
                    placeholder="選擇狀態"
                    value={timelineStatus}
                    options={timelineStatusOptions}
                    onChange={(value) => setTimelineStatus(value)}
                    className="traveler-support-filter-field"
                  />
                </Space>
              </Col>
              <Col xs={24} md={12} xl={6}>
                <Space direction="vertical" size={4} className="traveler-support-filter-field">
                  <Text type="secondary">獎勵類型</Text>
                  <Select
                    allowClear
                    placeholder="選擇獎勵類型"
                    value={timelineRewardType}
                    options={timelineRewardTypeOptions}
                    onChange={(value) => setTimelineRewardType(value)}
                    className="traveler-support-filter-field"
                  />
                </Space>
              </Col>
              <Col xs={24} xl={12}>
                <Space direction="vertical" size={4} className="traveler-support-filter-wide">
                  <Text type="secondary">時間範圍</Text>
                  <DatePicker.RangePicker
                    showTime
                    value={timelineRange}
                    onChange={(value) => setTimelineRange(value)}
                    className="traveler-support-filter-wide"
                  />
                </Space>
              </Col>
            </Row>
          </Card>

          <Card title="身份與偏好">
            <Row gutter={[16, 16]}>
              <Col xs={24} xl={12}>
                <Card size="small" title="旅客身份">
                  <Descriptions column={1} size="small" bordered>
                    <Descriptions.Item label="暱稱">
                      {workbench.identity.nickname || '未命名旅客'}
                    </Descriptions.Item>
                    <Descriptions.Item label="OpenID">
                      <Text code>{workbench.identity.openId}</Text>
                    </Descriptions.Item>
                    <Descriptions.Item label="等級">
                      Lv.{workbench.identity.level ?? 0}
                    </Descriptions.Item>
                    <Descriptions.Item label="印章數">
                      {workbench.identity.totalStamps ?? 0}
                    </Descriptions.Item>
                    <Descriptions.Item label="經驗值">
                      {workbench.identity.currentExp ?? 0} / {workbench.identity.nextLevelExp ?? 0}
                    </Descriptions.Item>
                    <Descriptions.Item label="目前語系">
                      {workbench.identity.currentLocaleCode || '未設定'}
                    </Descriptions.Item>
                    <Descriptions.Item label="測試帳號">
                      {workbench.identity.testAccount ? '是' : '否'}
                    </Descriptions.Item>
                    <Descriptions.Item label="目前城市">
                      {workbench.identity.currentCityName || '未設定'}
                    </Descriptions.Item>
                  </Descriptions>
                </Card>
              </Col>
              <Col xs={24} xl={12}>
                <Card size="small" title="旅客偏好">
                  <Descriptions column={1} size="small" bordered>
                    <Descriptions.Item label="介面模式">
                      {workbench.preferences.interfaceMode || '未設定'}
                    </Descriptions.Item>
                    <Descriptions.Item label="字級倍率">
                      {workbench.preferences.fontScale ?? '未設定'}
                    </Descriptions.Item>
                    <Descriptions.Item label="高對比">
                      {workbench.preferences.highContrast ? '開啟' : '關閉'}
                    </Descriptions.Item>
                    <Descriptions.Item label="語音導覽">
                      {workbench.preferences.voiceGuideEnabled ? '開啟' : '關閉'}
                    </Descriptions.Item>
                    <Descriptions.Item label="長者模式">
                      {workbench.preferences.seniorMode ? '開啟' : '關閉'}
                    </Descriptions.Item>
                    <Descriptions.Item label="偏好語系">
                      {workbench.preferences.localeCode || '未設定'}
                    </Descriptions.Item>
                    <Descriptions.Item label="緊急聯絡人">
                      {workbench.preferences.emergencyContactName || '未設定'}
                    </Descriptions.Item>
                    <Descriptions.Item label="緊急聯絡電話">
                      {workbench.preferences.emergencyContactPhone || '未設定'}
                    </Descriptions.Item>
                  </Descriptions>
                  <div style={{ marginTop: 12 }}>
                    <Text strong>Runtime Overrides</Text>
                    <div style={{ marginTop: 8 }}>
                      <JsonDetailCard
                        label="旅客偏好 Runtime Overrides"
                        value={workbench.preferences.runtimeOverridesJson}
                        onView={openPayloadViewer}
                      />
                    </div>
                  </div>
                </Card>
              </Col>
            </Row>

            <Card size="small" title="關聯範圍" style={{ marginTop: 16 }}>
              {workbench.linkedScopes.length === 0 ? (
                <Empty description="暫無關聯範圍" image={Empty.PRESENTED_IMAGE_SIMPLE} />
              ) : (
                <List
                  dataSource={workbench.linkedScopes}
                  renderItem={(item) => (
                    <List.Item>
                      <Space wrap>
                        <Tag color="blue">{formatScopeTypeLabel(item.scopeType)}</Tag>
                        <Text strong>{item.scopeName || `#${item.scopeId}`}</Text>
                        {item.scopeId ? <Text type="secondary">ID: {item.scopeId}</Text> : null}
                        {item.relationLabel ? <Text type="secondary">{item.relationLabel}</Text> : null}
                        {item.source ? <Text type="secondary">來源：{item.source}</Text> : null}
                      </Space>
                    </List.Item>
                  )}
                />
              )}
            </Card>

            <Alert
              style={{ marginTop: 16 }}
              type={workbench.explorationContext?.routeTrace?.sourceStatus === 'unavailable' ? 'warning' : 'info'}
              showIcon
              message="路徑追蹤來源狀態"
              description={
                workbench.explorationContext?.routeTrace?.message ||
                '目前沒有額外的路徑追蹤資料來源。'
              }
            />
          </Card>

          <Card title="進度總覽" loading={breakdownLoading}>
            <Row gutter={[16, 16]}>
              <Col xs={24} md={12} xl={6}>
                <Statistic
                  title="目前動態加權進度"
                  value={activeScopeSummary?.progressPercent || 0}
                  precision={2}
                  suffix="%"
                />
              </Col>
              <Col xs={24} md={12} xl={6}>
                <Statistic
                  title="目前分母元素"
                  value={breakdown?.availableElementCount || activeScopeSummary?.availableElementCount || 0}
                />
              </Col>
              <Col xs={24} md={12} xl={6}>
                <Statistic
                  title="已完成元素"
                  value={breakdown?.completedElementCount || activeScopeSummary?.completedElementCount || 0}
                />
              </Col>
              <Col xs={24} md={12} xl={6}>
                <Statistic
                  title="退役完成元素"
                  value={breakdown?.retiredCompletedCount || activeScopeSummary?.retiredCompletedCount || 0}
                />
              </Col>
            </Row>

            <Row gutter={[16, 16]} style={{ marginTop: 8 }}>
              <Col xs={24} md={12}>
                <Card size="small" title="目前檢視範圍">
                  <Space direction="vertical" size={6}>
                    <Text>
                      {formatScopeTypeLabel(selectedScopeType)}｜{activeScopeName}
                    </Text>
                    <Text type="secondary">
                      完成權重 {breakdown?.completedWeight ?? activeScopeSummary?.completedWeight ?? 0} / 可用權重{' '}
                      {breakdown?.availableWeight ?? activeScopeSummary?.availableWeight ?? 0}
                    </Text>
                    <Text type="secondary">
                      最近重算：{formatDateTime(breakdown?.lastRecomputeTime || activeScopeSummary?.lastRecomputeTime)}
                    </Text>
                    {workbench.dynamicProgress.comparisonHint ? (
                      <Alert
                        type="info"
                        showIcon
                        message={workbench.dynamicProgress.comparisonHint}
                      />
                    ) : null}
                  </Space>
                </Card>
              </Col>
              <Col xs={24} md={12}>
                <Card size="small" title="全域摘要">
                  <Descriptions column={1} size="small" bordered>
                    <Descriptions.Item label="全域動態加權進度">
                      {formatPercent(workbench.dynamicProgress.globalSummary.progressPercent)}
                    </Descriptions.Item>
                    <Descriptions.Item label="全域權重">
                      {workbench.dynamicProgress.globalSummary.completedWeight} /{' '}
                      {workbench.dynamicProgress.globalSummary.availableWeight}
                    </Descriptions.Item>
                    <Descriptions.Item label="退役比較">
                      {workbench.dynamicProgress.globalSummary.retiredCompletedWeight} /{' '}
                      {workbench.dynamicProgress.globalSummary.retiredCompletedCount}
                    </Descriptions.Item>
                  </Descriptions>
                </Card>
              </Col>
            </Row>

            <Card size="small" title="範圍摘要卡片" style={{ marginTop: 16 }}>
              <Row gutter={[12, 12]}>
                {workbench.dynamicProgress.scopedSummaries.map((item) => (
                  <Col xs={24} md={12} xl={8} key={`${item.scopeType}-${item.scopeId}`}>
                    <Card
                      size="small"
                      style={{
                        borderColor:
                          item.scopeType === selectedScopeType &&
                          Number(item.scopeId ?? 0) === Number(selectedScopeId ?? 0)
                            ? '#1677ff'
                            : undefined,
                      }}
                    >
                      <Space direction="vertical" size={4} style={{ width: '100%' }}>
                        <Text strong>{item.scopeName || formatScopeTypeLabel(item.scopeType)}</Text>
                        <Text type="secondary">
                          {formatScopeTypeLabel(item.scopeType)}
                          {item.scopeId ? ` · #${item.scopeId}` : ''}
                        </Text>
                        <Text>{formatPercent(item.summary.progressPercent)}</Text>
                        <Text type="secondary">
                          {item.summary.completedWeight} / {item.summary.availableWeight}
                        </Text>
                        <Button
                          size="small"
                          onClick={() => {
                            setSelectedScopeType(item.scopeType);
                            setSelectedScopeId(item.scopeId || undefined);
                          }}
                        >
                          查看此範圍
                        </Button>
                      </Space>
                    </Card>
                  </Col>
                ))}
              </Row>
            </Card>

            <Card size="small" title="舊進度快照（兼容）" style={{ marginTop: 16 }}>
              <Alert
                type="warning"
                showIcon
                message="legacyProgressSnapshot 只作兼容對照"
                description="此區塊來自 traveler_progress 舊表快照，compatibilityOnly=true，只用來對照歷史值，不會取代目前的動態加權百分比。"
              />
              <Row gutter={[12, 12]} style={{ marginTop: 12 }}>
                {workbench.legacyProgressSnapshot.length === 0 ? (
                  <Col span={24}>
                    <Empty description="沒有 legacyProgressSnapshot 資料" image={Empty.PRESENTED_IMAGE_SIMPLE} />
                  </Col>
                ) : (
                  workbench.legacyProgressSnapshot.map((item, index) => {
                    const delta = computePercentDelta(item, activeScopeSummary);
                    return (
                      <Col xs={24} xl={12} key={`${item.legacyScopeType}-${item.legacyScopeId}-${index}`}>
                        <Card size="small">
                          <Space direction="vertical" size={4} style={{ width: '100%' }}>
                            <Space wrap>
                              <Tag color="default">{item.label || '舊進度快照（兼容）'}</Tag>
                              <Tag>traveler_progress</Tag>
                              <Tag color={item.compatibilityOnly ? 'warning' : 'success'}>
                                compatibilityOnly={String(item.compatibilityOnly)}
                              </Tag>
                            </Space>
                            <Text strong>
                              {item.legacyScopeName || formatScopeTypeLabel(item.legacyScopeType)}
                            </Text>
                            <Text>舊值：{item.legacyPercentValue ?? 0}%</Text>
                            <Text>目前動態加權：{formatPercent(activeScopeSummary?.progressPercent)}</Text>
                            <Text type="secondary">
                              差異：{delta === null ? '暫無' : `${delta.toFixed(2)}%`}
                            </Text>
                            <Text type="secondary">
                              最近出現：{formatDateTime(item.lastSeenAt)} · 更新時間：{formatDateTime(item.updatedAt)}
                            </Text>
                          </Space>
                        </Card>
                      </Col>
                    );
                  })
                )}
              </Row>
            </Card>
          </Card>

          <Card title="探索元素明細" loading={breakdownLoading}>
            <Space wrap style={{ marginBottom: 16 }}>
              <Select
                style={{ minWidth: 240 }}
                value={`${selectedScopeType}:${selectedScopeId ?? ''}`}
                options={scopeOptions}
                onChange={handleScopeChange}
              />
              <Select
                allowClear
                placeholder="故事線篩選"
                style={{ minWidth: 220 }}
                value={selectedStorylineId}
                options={storylineOptions}
                onChange={handleStorylineChange}
              />
              <Select
                style={{ minWidth: 180 }}
                value={completionFilter}
                options={[
                  { label: '全部完成狀態', value: 'all' },
                  { label: '只看已完成', value: 'completed' },
                  { label: '只看未完成', value: 'pending' },
                ]}
                onChange={(value) => setCompletionFilter(value)}
              />
              <Space>
                <Switch
                  checked={includeInactiveComparison}
                  onChange={(checked) => setIncludeInactiveComparison(checked)}
                />
                <Text>啟用退役比較模式</Text>
              </Space>
            </Space>

            <Row gutter={[16, 16]} style={{ marginBottom: 16 }}>
              <Col xs={24} md={12} xl={6}>
                <Statistic title="完成權重" value={breakdown?.completedWeight || 0} />
              </Col>
              <Col xs={24} md={12} xl={6}>
                <Statistic title="目前可用權重" value={breakdown?.availableWeight || 0} />
              </Col>
              <Col xs={24} md={12} xl={6}>
                <Statistic title="已完成元素" value={breakdown?.completedElementCount || 0} />
              </Col>
              <Col xs={24} md={12} xl={6}>
                <Statistic title="退役完成元素" value={breakdown?.retiredCompletedCount || 0} />
              </Col>
            </Row>

            <Table
              rowKey="elementId"
              columns={elementColumns}
              dataSource={filteredElements}
              pagination={{ pageSize: 8 }}
              locale={{ emptyText: '暫無符合條件的旅客進度資料' }}
            />

            {includeInactiveComparison ? (
              <Card
                size="small"
                title="退役完成比較"
                style={{ marginTop: 16 }}
                extra={<Tag color="warning">已退役，不計入目前百分比</Tag>}
              >
                <Table
                  rowKey="elementId"
                  columns={elementColumns}
                  dataSource={filteredRetiredElements}
                  pagination={{ pageSize: 5 }}
                  locale={{ emptyText: '沒有退役完成元素' }}
                />
              </Card>
            ) : null}
          </Card>

          <Card title="故事模式 Session" loading={workbenchLoading}>
            <Table
              rowKey="sessionId"
              columns={sessionColumns}
              dataSource={workbench.storylineSessions}
              pagination={{ pageSize: 5 }}
              locale={{ emptyText: '暫無故事模式 Session' }}
            />
          </Card>

          <Card title="互動時間線" loading={timelineLoading || auditsLoading}>
            <Space wrap style={{ marginBottom: 16 }}>
              <Select
                mode="multiple"
                allowClear
                placeholder="事件類型篩選"
                style={{ minWidth: 260 }}
                value={timelineEventTypes}
                options={timelineEventTypeOptions}
                onChange={(value) => setTimelineEventTypes(value)}
              />
              <Select
                allowClear
                placeholder="故事線篩選"
                style={{ minWidth: 220 }}
                value={selectedStorylineId}
                options={storylineOptions}
                onChange={handleStorylineChange}
              />
            </Space>
            <Table
              rowKey="key"
              columns={mergedTimelineColumns}
              dataSource={mergedTimelineEntries}
              pagination={{ pageSize: 8 }}
              locale={{ emptyText: '目前沒有符合條件的互動時間線' }}
            />
          </Card>

          <Card title="背包 / 收集物" loading={rewardStateLoading}>
            <Row gutter={[16, 16]} style={{ marginBottom: 16 }}>
              <Col xs={24} md={12} xl={6}>
                <Statistic title="收集物" value={rewardState?.summary?.backpackCount ?? rewardState?.backpackItems?.length ?? 0} />
              </Col>
              <Col xs={24} md={12} xl={6}>
                <Statistic title="遊戲內獎勵" value={rewardState?.summary?.gameRewardCount ?? rewardState?.gameRewards?.length ?? 0} />
              </Col>
              <Col xs={24} md={12} xl={6}>
                <Statistic title="稱號" value={rewardState?.summary?.titleCount ?? rewardState?.titles?.length ?? 0} />
              </Col>
              <Col xs={24} md={12} xl={6}>
                <Statistic title="最後取得時間" value={formatDateTime(rewardState?.summary?.lastEarnedAt)} />
              </Col>
            </Row>
            <Table
              rowKey={(record) => `${record.sourceType || 'item'}-${record.sourceId || record.code || record.sourceEventId}`}
              columns={backpackColumns}
              dataSource={rewardState?.backpackItems || []}
              pagination={{ pageSize: 6 }}
              locale={{ emptyText: '暫無背包或收集物資料' }}
            />
          </Card>

          <Card title="獎勵與稱號" loading={rewardStateLoading}>
            <Tabs
              items={[
                {
                  key: 'gameRewards',
                  label: '遊戲內獎勵',
                  children: (
                    <Table
                      rowKey={(record) => `${record.rewardType || 'reward'}-${record.rewardId}-${record.sourceEventId || ''}`}
                      columns={gameRewardColumns}
                      dataSource={rewardState?.gameRewards || []}
                      pagination={{ pageSize: 6 }}
                      locale={{ emptyText: '暫無遊戲內獎勵' }}
                    />
                  ),
                },
                {
                  key: 'titles',
                  label: '榮譽稱號',
                  children: (
                    <Table
                      rowKey={(record) => `${record.rewardId}-${record.sourceEventId || ''}`}
                      columns={titleColumns}
                      dataSource={rewardState?.titles || []}
                      pagination={{ pageSize: 6 }}
                      locale={{ emptyText: '暫無稱號' }}
                    />
                  ),
                },
                {
                  key: 'redeemable',
                  label: '兌換獎勵',
                  children: (
                    <Table
                      rowKey="redemptionId"
                      columns={rewardColumns}
                      dataSource={rewardState?.redeemableRewards || workbench.rewardRedemptions}
                      pagination={{ pageSize: 6 }}
                      locale={{ emptyText: '暫無兌換獎勵' }}
                    />
                  ),
                },
              ]}
            />
          </Card>

          <Card
            title="規則追蹤"
            loading={ruleTraceLoading}
            extra={
              <Space wrap>
                <InputNumber
                  min={1}
                  value={traceSourceEventId}
                  placeholder="sourceEventId"
                  onChange={(value) => setTraceSourceEventId(typeof value === 'number' ? value : null)}
                />
                <InputNumber
                  min={1}
                  value={traceRuleId}
                  placeholder="ruleId"
                  onChange={(value) => setTraceRuleId(typeof value === 'number' ? value : null)}
                />
                <InputNumber
                  min={1}
                  value={traceRewardId}
                  placeholder="rewardId"
                  onChange={(value) => setTraceRewardId(typeof value === 'number' ? value : null)}
                />
                <InputNumber
                  min={1}
                  value={traceGameRewardId}
                  placeholder="gameRewardId"
                  onChange={(value) => setTraceGameRewardId(typeof value === 'number' ? value : null)}
                />
                <Button onClick={resetTraceFilters}>清除追蹤條件</Button>
              </Space>
            }
          >
            <Space direction="vertical" size={16} style={{ width: '100%' }}>
              <Alert
                type={ruleTrace?.traceStatus === 'eligible_granted' ? 'success' : ruleTrace?.traceStatus === 'missing_link' ? 'warning' : 'info'}
                showIcon
                message={ruleTrace?.traceStatusLabel || '規則追蹤狀態'}
                description={ruleTrace?.explanation || '可按事件、規則、兌換獎勵或遊戲內獎勵查詢發放鏈路。'}
              />
              <Descriptions size="small" bordered column={2}>
                <Descriptions.Item label="事件">
                  {ruleTrace?.event?.eventId ? `#${ruleTrace.event.eventId}｜${ruleTrace.event.eventType || '未知事件'}` : '暫無'}
                </Descriptions.Item>
                <Descriptions.Item label="探索元素">
                  {ruleTrace?.explorationElement?.title || ruleTrace?.explorationElement?.elementCode || '暫無'}
                </Descriptions.Item>
                <Descriptions.Item label="體驗步驟">
                  {ruleTrace?.experienceStep?.stepName || ruleTrace?.experienceStep?.stepCode || '暫無'}
                </Descriptions.Item>
                <Descriptions.Item label="缺失鏈路">
                  {(ruleTrace?.missingLinks || []).length > 0 ? (ruleTrace?.missingLinks || []).join('、') : '暫無'}
                </Descriptions.Item>
              </Descriptions>
              <Table
                rowKey={(record) => record.ruleId || record.code || 'rule'}
                columns={ruleTraceRuleColumns}
                dataSource={ruleTrace?.rules || []}
                pagination={{ pageSize: 4 }}
                locale={{ emptyText: '目前沒有可顯示的規則鏈路' }}
              />
              <Table
                rowKey={(record) => `${record.grantSource || 'grant'}-${record.grantRowId || record.rewardId || record.gameRewardId}`}
                columns={ruleTraceGrantColumns}
                dataSource={ruleTrace?.grants || []}
                pagination={{ pageSize: 4 }}
                locale={{ emptyText: '目前沒有發放紀錄' }}
              />
            </Space>
          </Card>

          <Card title="收集與獎勵來源">
            <Row gutter={[16, 16]} style={{ marginBottom: 16 }}>
              <Col xs={24} md={8}>
                <Statistic
                  title="最近打卡數"
                  value={workbench.explorationContext?.recentCheckinCount || 0}
                />
              </Col>
              <Col xs={24} md={8}>
                <Statistic
                  title="最近探索事件數"
                  value={workbench.explorationContext?.recentExplorationEventCount || 0}
                />
              </Col>
              <Col xs={24} md={8}>
                <Statistic
                  title="最近觸發數"
                  value={workbench.explorationContext?.recentTriggerCount || 0}
                />
              </Col>
            </Row>
            <Table
              rowKey="redemptionId"
              columns={rewardColumns}
              dataSource={workbench.rewardRedemptions}
              pagination={{ pageSize: 5 }}
              locale={{ emptyText: '暫無收集與獎勵來源資料' }}
            />
          </Card>

          <Card title="修復與審計">
            <Alert
              type="warning"
              showIcon
              style={{ marginBottom: 16 }}
              message="高影響操作"
              description="所有修復動作都會先預覽影響範圍，確認後才寫入審計紀錄。所有操作都必須先預覽，再輸入指定確認字樣後才能送出，避免誤改旅客衍生進度。"
            />
            <Row gutter={[16, 16]}>
              <Col xs={24} xl={12}>
                <Card title="重新計算進度" size="small">
                  <Space direction="vertical" size={12} style={{ width: '100%' }}>
                    <Text>
                      目前範圍：{formatScopeTypeLabel(selectedScopeType)}｜{activeScopeName}
                    </Text>
                    <Input.TextArea
                      rows={3}
                      value={recomputeReason}
                      placeholder="請填寫重算原因"
                      onChange={(event) => setRecomputeReason(event.target.value)}
                    />
                    <Button
                      loading={recomputeBusy}
                      disabled={recomputeBusy}
                      icon={<ReloadOutlined />}
                      onClick={handlePreviewRecompute}
                    >
                      預覽重算影響
                    </Button>

                    {recomputePreview ? (
                      <Card size="small" type="inner" title="預覽結果">
                        <Descriptions column={1} size="small" bordered>
                          <Descriptions.Item label="確認字樣">
                            <Text code>{recomputePreview.confirmationText}</Text>
                          </Descriptions.Item>
                          <Descriptions.Item label="受影響旅客">
                            {recomputePreview.affectedUserCount ?? 0}
                          </Descriptions.Item>
                          <Descriptions.Item label="受影響範圍">
                            {recomputePreview.affectedScopeCount ?? 0}
                          </Descriptions.Item>
                          <Descriptions.Item label="匹配事件數">
                            {recomputePreview.matchingEventCount ?? 0}
                          </Descriptions.Item>
                        </Descriptions>
                        <div style={{ marginTop: 12 }}>
                          <JsonDetailCard
                            label="重算預覽摘要"
                            value={recomputePreview.previewSummary}
                            onView={openPayloadViewer}
                          />
                        </div>
                        <Input
                          style={{ marginTop: 12 }}
                          value={recomputeConfirmText}
                          placeholder={`請輸入 ${recomputePreview.confirmationText}`}
                          onChange={(event) => setRecomputeConfirmText(event.target.value)}
                        />
                        <Button
                          style={{ marginTop: 12 }}
                          type="primary"
                          danger
                          loading={recomputeBusy}
                          disabled={recomputeBusy}
                          onClick={handleConfirmRecompute}
                        >
                          確認重算
                        </Button>
                      </Card>
                    ) : null}

                    {recomputeResult ? (
                      <Card size="small" type="inner" title="最近一次重算結果">
                        <Descriptions column={1} size="small" bordered>
                          <Descriptions.Item label="狀態">
                            {recomputeResult.status || 'unknown'}
                          </Descriptions.Item>
                          <Descriptions.Item label="寫入快取列數">
                            {recomputeResult.writtenStateRows ?? 0}
                          </Descriptions.Item>
                        </Descriptions>
                        <div style={{ marginTop: 12 }}>
                          <JsonDetailCard
                            label="重算結果摘要"
                            value={recomputeResult.resultSummary}
                            onView={openPayloadViewer}
                          />
                        </div>
                      </Card>
                    ) : null}
                  </Space>
                </Card>
              </Col>

              <Col xs={24} xl={12}>
                <Card title="支援操作" size="small">
                  <Space direction="vertical" size={12} style={{ width: '100%' }}>
                    <Select
                      value={repairActionType}
                      options={[
                        { label: '補連孤兒事件', value: 'LINK_ORPHAN_EVENT' },
                        { label: '標記重複事件', value: 'MARK_DUPLICATE_CLIENT_EVENT' },
                        { label: '標記重複事件（作廢重複）', value: 'VOID_DUPLICATE_EVENT' },
                        { label: '補發獎勵', value: 'RESEND_REWARD' },
                        { label: '留下問題註記', value: 'ANNOTATE_ISSUE' },
                      ]}
                      onChange={(value) => {
                        setRepairActionType(value);
                        setRepairPreview(null);
                        setRepairConfirmText('');
                      }}
                    />
                    <Row gutter={[12, 12]}>
                      <Col xs={24} md={12}>
                        <InputNumber
                          style={{ width: '100%' }}
                          value={repairTargetEventId}
                          min={1}
                          placeholder="目標事件 ID"
                          onChange={(value) => setRepairTargetEventId(typeof value === 'number' ? value : null)}
                        />
                      </Col>
                      <Col xs={24} md={12}>
                        <InputNumber
                          style={{ width: '100%' }}
                          value={repairDuplicateOfEventId}
                          min={1}
                          placeholder="重複來源事件 ID"
                          onChange={(value) =>
                            setRepairDuplicateOfEventId(typeof value === 'number' ? value : null)
                          }
                        />
                      </Col>
                      <Col xs={24} md={12}>
                        <InputNumber
                          style={{ width: '100%' }}
                          value={repairReplacementElementId}
                          min={1}
                          placeholder="替代元素 ID"
                          onChange={(value) =>
                            setRepairReplacementElementId(typeof value === 'number' ? value : null)
                          }
                        />
                      </Col>
                      <Col xs={24} md={12}>
                        <Input
                          value={repairReplacementElementCode}
                          placeholder="替代元素 Code"
                          onChange={(event) => setRepairReplacementElementCode(event.target.value)}
                        />
                      </Col>
                      <Col xs={24} md={12}>
                        <InputNumber
                          style={{ width: '100%' }}
                          value={repairSourceEventId}
                          min={1}
                          placeholder="sourceEventId"
                          onChange={(value) => setRepairSourceEventId(typeof value === 'number' ? value : null)}
                        />
                      </Col>
                      <Col xs={24} md={12}>
                        <InputNumber
                          style={{ width: '100%' }}
                          value={repairRuleId}
                          min={1}
                          placeholder="ruleId"
                          onChange={(value) => setRepairRuleId(typeof value === 'number' ? value : null)}
                        />
                      </Col>
                      <Col xs={24} md={12}>
                        <InputNumber
                          style={{ width: '100%' }}
                          value={repairRewardId}
                          min={1}
                          placeholder="rewardId"
                          onChange={(value) => setRepairRewardId(typeof value === 'number' ? value : null)}
                        />
                      </Col>
                      <Col xs={24} md={12}>
                        <InputNumber
                          style={{ width: '100%' }}
                          value={repairGameRewardId}
                          min={1}
                          placeholder="gameRewardId"
                          onChange={(value) => setRepairGameRewardId(typeof value === 'number' ? value : null)}
                        />
                      </Col>
                    </Row>
                    <Select
                      value={repairIssueSeverity}
                      options={[
                        { label: 'info 一般註記', value: 'info' },
                        { label: 'warning 需要跟進', value: 'warning' },
                        { label: 'critical 高風險', value: 'critical' },
                      ]}
                      onChange={(value) => setRepairIssueSeverity(value)}
                    />
                    <Input.TextArea
                      rows={3}
                      value={repairAnnotationText}
                      placeholder="問題註記內容（留下問題註記時必填）"
                      onChange={(event) => setRepairAnnotationText(event.target.value)}
                    />
                    <Input.TextArea
                      rows={3}
                      value={repairReason}
                      placeholder="請填寫修復原因"
                      onChange={(event) => setRepairReason(event.target.value)}
                    />
                    <Button
                      loading={repairBusy}
                      disabled={repairBusy}
                      icon={<ToolOutlined />}
                      onClick={handlePreviewRepair}
                    >
                      預覽{formatRepairActionLabel(repairActionType)}影響
                    </Button>

                    {repairPreview ? (
                      <Card size="small" type="inner" title="修復預覽">
                        <Descriptions column={1} size="small" bordered>
                          <Descriptions.Item label="確認字樣">
                            <Text code>{repairPreview.confirmationText}</Text>
                          </Descriptions.Item>
                          <Descriptions.Item label="匹配事件數">
                            {repairPreview.matchingEventCount ?? 0}
                          </Descriptions.Item>
                          <Descriptions.Item label="可用元素數">
                            {repairPreview.availableElementCount ?? 0}
                          </Descriptions.Item>
                          <Descriptions.Item label="已完成元素數">
                            {repairPreview.completedElementCount ?? 0}
                          </Descriptions.Item>
                        </Descriptions>
                        <div style={{ marginTop: 12 }}>
                          <JsonDetailCard
                            label="修復預覽摘要"
                            value={repairPreview.previewSummary}
                            onView={openPayloadViewer}
                          />
                        </div>
                        <Input
                          style={{ marginTop: 12 }}
                          value={repairConfirmText}
                          placeholder={`請輸入 ${repairPreview.confirmationText}`}
                          onChange={(event) => setRepairConfirmText(event.target.value)}
                        />
                        <Button
                          style={{ marginTop: 12 }}
                          type="primary"
                          danger
                          loading={repairBusy}
                          disabled={repairBusy}
                          onClick={handleApplyRepair}
                        >
                          套用{formatRepairActionLabel(repairActionType)}
                        </Button>
                      </Card>
                    ) : null}

                    {repairResult ? (
                      <Card size="small" type="inner" title="最近一次修復結果">
                        <Descriptions column={1} size="small" bordered>
                          <Descriptions.Item label="狀態">
                            {repairResult.status || 'unknown'}
                          </Descriptions.Item>
                          <Descriptions.Item label="異動事件列數">
                            {repairResult.mutatedEventRows ?? 0}
                          </Descriptions.Item>
                        </Descriptions>
                        <div style={{ marginTop: 12 }}>
                          <JsonDetailCard
                            label="修復結果摘要"
                            value={repairResult.resultSummary}
                            onView={openPayloadViewer}
                          />
                        </div>
                      </Card>
                    ) : null}
                  </Space>
                </Card>
              </Col>
            </Row>
          </Card>

          <Card title="審計紀錄" loading={auditsLoading}>
            <Table
              rowKey="id"
              columns={auditColumns}
              dataSource={auditEntries}
              pagination={{ pageSize: 6 }}
              locale={{ emptyText: '暫無審計紀錄' }}
            />
          </Card>
        </Space>
      )}

      <Modal
        width={860}
        open={Boolean(payloadViewer)}
        title={payloadViewer?.title}
        onCancel={() => setPayloadViewer(null)}
        footer={
          <Space>
            <Button
              icon={<CopyOutlined />}
              onClick={async () => {
                if (!payloadViewer?.content) {
                  return;
                }
                try {
                  await navigator.clipboard.writeText(payloadViewer.content);
                  message.success('內容已複製');
                } catch (error) {
                  message.error('複製失敗');
                }
              }}
            >
              複製 JSON
            </Button>
            <Button onClick={() => setPayloadViewer(null)}>關閉</Button>
          </Space>
        }
      >
        <Paragraph style={{ whiteSpace: 'pre-wrap', maxHeight: 520, overflow: 'auto', marginBottom: 0 }}>
          {payloadViewer?.content}
        </Paragraph>
      </Modal>
    </PageContainer>
  );
};

export default UserProgressWorkbench;
