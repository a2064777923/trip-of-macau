import React, { useEffect, useState } from 'react';
import { PageContainer } from '@ant-design/pro-layout';
import {
  Alert,
  Button,
  Card,
  Collapse,
  Col,
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
  getAdminTravelerTimeline,
  getAdminUserProgressAudits,
  previewAdminUserProgressRecompute,
  previewAdminUserProgressRepair,
} from '../../services/api';
import type {
  AdminLegacyProgressSnapshot,
  AdminTravelerProgressWorkbench,
  AdminTravelerRewardRedemptionSummary,
  AdminTravelerTimelineEntry,
  AdminUserProgressAuditEntry,
  AdminUserProgressBreakdown,
  AdminUserProgressBreakdownElement,
  AdminUserProgressOperationPreview,
  AdminUserProgressOperationResult,
  AdminUserProgressSummary,
} from '../../types/admin';

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
  payloadTitle?: string;
  payloadContent?: string;
};

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

function stringifyPayload(value?: string | Record<string, unknown> | null) {
  if (!value) {
    return '';
  }
  if (typeof value === 'string') {
    return value;
  }
  return JSON.stringify(value, null, 2);
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
}) {
  const content = stringifyPayload(props.value);
  if (!content) {
    return <Text type="secondary">暫無詳細內容</Text>;
  }

  return (
    <Space wrap>
      <Button size="small" icon={<EyeOutlined />} onClick={() => props.onView(props.label, content)}>
        查看內容
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
  const [payloadViewer, setPayloadViewer] = useState<{ title: string; content: string } | null>(null);

  const [selectedScopeType, setSelectedScopeType] = useState('global');
  const [selectedScopeId, setSelectedScopeId] = useState<number | undefined>();
  const [selectedStorylineId, setSelectedStorylineId] = useState<number | undefined>();
  const [completionFilter, setCompletionFilter] = useState<CompletionFilter>('all');
  const [includeInactiveComparison, setIncludeInactiveComparison] = useState(false);
  const [timelineEventTypes, setTimelineEventTypes] = useState<string[]>([]);

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
  }, [hasValidUserId, refreshKey, selectedStorylineId, timelineEventTypes.join('|'), userId]);

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
      ...timelineEntries.map((item) => item.entryType),
      ...(auditEntries.length > 0 ? ['audit'] : []),
    ]),
  ).map((item) => ({
    label: formatTimelineTypeLabel(item),
    value: item,
  }));

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
        message.success('已產生重算預覽');
      } else {
        message.error(response.message || '重算預覽失敗');
      }
    } catch (error) {
      message.error('重算預覽失敗');
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
        message.success('重算已完成');
      } else {
        message.error(response.message || '重算確認失敗');
      }
    } catch (error) {
      message.error('重算確認失敗');
    } finally {
      setRecomputeBusy(false);
    }
  };

  const handlePreviewRepair = async () => {
    if (!repairReason.trim()) {
      message.warning('請先填寫修復原因');
      return;
    }
    if (!repairTargetEventId) {
      message.warning('請填寫目標事件 ID');
      return;
    }
    setRepairBusy(true);
    try {
      const response = await previewAdminUserProgressRepair(userId, {
        userId,
        scopeType: selectedScopeType,
        scopeId: selectedScopeId,
        storylineId: selectedStorylineId,
        actionType: repairActionType,
        targetEventId: repairTargetEventId || undefined,
        replacementElementId: repairReplacementElementId || undefined,
        replacementElementCode: repairReplacementElementCode.trim() || undefined,
        duplicateOfEventId: repairDuplicateOfEventId || undefined,
        reason: repairReason.trim(),
      });
      if (response.success) {
        setRepairPreview(response.data);
        setRepairResult(null);
        message.success('已產生修復預覽');
      } else {
        message.error(response.message || '修復預覽失敗');
      }
    } catch (error) {
      message.error('修復預覽失敗');
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
      const response = await applyAdminUserProgressRepair(userId, {
        userId,
        scopeType: selectedScopeType,
        scopeId: selectedScopeId,
        storylineId: selectedStorylineId,
        actionType: repairActionType,
        targetEventId: repairTargetEventId || undefined,
        replacementElementId: repairReplacementElementId || undefined,
        replacementElementCode: repairReplacementElementCode.trim() || undefined,
        duplicateOfEventId: repairDuplicateOfEventId || undefined,
        reason: repairReason.trim(),
        previewHash: repairPreview.previewHash,
        confirmationText: 'REPAIR',
      });
      if (response.success) {
        setRepairResult(response.data);
        setRepairPreview(null);
        setRepairConfirmText('');
        setRefreshKey((value) => value + 1);
        message.success('修復已完成');
      } else {
        message.error(response.message || '修復套用失敗');
      }
    } catch (error) {
      message.error('修復套用失敗');
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
              locale={{ emptyText: '目前範圍沒有可顯示的探索元素' }}
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

          <Card title="修復與重算">
            <Alert
              type="warning"
              showIcon
              style={{ marginBottom: 16 }}
              message="高影響操作"
              description="所有操作都必須先預覽，再輸入指定確認字樣後才能送出，避免誤改旅客衍生進度。"
            />
            <Row gutter={[16, 16]}>
              <Col xs={24} xl={12}>
                <Card title="重算快取" size="small">
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
                    <Button loading={recomputeBusy} icon={<ReloadOutlined />} onClick={handlePreviewRecompute}>
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
                <Card title="事件修復" size="small">
                  <Space direction="vertical" size={12} style={{ width: '100%' }}>
                    <Select
                      value={repairActionType}
                      options={[
                        { label: '補連孤兒事件', value: 'LINK_ORPHAN_EVENT' },
                        { label: '標記重複事件', value: 'MARK_DUPLICATE_CLIENT_EVENT' },
                      ]}
                      onChange={(value) => setRepairActionType(value)}
                    />
                    <InputNumber
                      style={{ width: '100%' }}
                      value={repairTargetEventId}
                      min={1}
                      placeholder="目標事件 ID"
                      onChange={(value) => setRepairTargetEventId(typeof value === 'number' ? value : null)}
                    />
                    <InputNumber
                      style={{ width: '100%' }}
                      value={repairReplacementElementId}
                      min={1}
                      placeholder="替代元素 ID（補連孤兒事件時可填）"
                      onChange={(value) =>
                        setRepairReplacementElementId(typeof value === 'number' ? value : null)
                      }
                    />
                    <Input
                      value={repairReplacementElementCode}
                      placeholder="替代元素 Code（可選）"
                      onChange={(event) => setRepairReplacementElementCode(event.target.value)}
                    />
                    <InputNumber
                      style={{ width: '100%' }}
                      value={repairDuplicateOfEventId}
                      min={1}
                      placeholder="重複來源事件 ID（標記重複事件時可填）"
                      onChange={(value) =>
                        setRepairDuplicateOfEventId(typeof value === 'number' ? value : null)
                      }
                    />
                    <Input.TextArea
                      rows={3}
                      value={repairReason}
                      placeholder="請填寫修復原因"
                      onChange={(event) => setRepairReason(event.target.value)}
                    />
                    <Button loading={repairBusy} icon={<ToolOutlined />} onClick={handlePreviewRepair}>
                      預覽修復影響
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
                          onClick={handleApplyRepair}
                        >
                          套用修復
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
