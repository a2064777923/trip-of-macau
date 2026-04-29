import React, { useEffect, useState } from 'react';
import {
  App as AntdApp,
  Button,
  Card,
  Col,
  Drawer,
  Input,
  Row,
  Select,
  Space,
  Statistic,
  Switch,
  Table,
  Tag,
  Tooltip,
  Typography,
} from 'antd';
import type { ColumnsType } from 'antd/es/table';
import {
  checkAdminExperienceGovernanceConflicts,
  getAdminExperienceGovernanceDetail,
  getAdminExperienceGovernanceItems,
  getAdminExperienceGovernanceOverview,
} from '../../services/api';
import type {
  AdminExperienceGovernanceDetail,
  AdminExperienceGovernanceFinding,
  AdminExperienceGovernanceItem,
  AdminExperienceGovernanceOverview,
  AdminExperienceGovernanceQuery,
} from '../../types/admin';

const { Title, Paragraph, Text } = Typography;

const ownerTypeOptions = [
  { label: 'POI', value: 'poi' },
  { label: '室內建築', value: 'indoor_building' },
  { label: '室內樓層', value: 'indoor_floor' },
  { label: '室內節點', value: 'indoor_node' },
  { label: '故事章節', value: 'story_chapter' },
  { label: '任務', value: 'task' },
  { label: '標記', value: 'marker' },
  { label: '疊加物', value: 'overlay' },
  { label: '獎勵規則', value: 'reward_rule' },
];

const templateTypeOptions = [
  { label: '展示模板', value: 'presentation' },
  { label: '顯示條件', value: 'display_condition' },
  { label: '觸發條件', value: 'trigger_condition' },
  { label: '觸發效果', value: 'trigger_effect' },
  { label: '任務玩法', value: 'task_gameplay' },
  { label: '獎勵演出', value: 'reward_presentation' },
  { label: '章節覆寫', value: 'story_override' },
  { label: '獎勵規則', value: 'reward_rule' },
];

const triggerTypeOptions = [
  { label: '點擊', value: 'tap' },
  { label: '點擊動作', value: 'tap_action' },
  { label: '靠近範圍', value: 'proximity' },
  { label: '停留時長', value: 'dwell' },
  { label: '媒體播放完成', value: 'media_finished' },
  { label: '任務完成', value: 'task_complete' },
  { label: '連續點擊', value: 'tap_sequence' },
  { label: '進入故事線', value: 'story_mode_enter' },
];
const effectTypeOptions = [
  { label: '全屏媒體', value: 'fullscreen_media' },
  { label: '圖文彈窗', value: 'rich_popup' },
  { label: 'Lottie 疊加', value: 'lottie_overlay' },
  { label: '地圖疊加物', value: 'map_overlay' },
  { label: '發放收集物', value: 'grant_collectible' },
  { label: '發放徽章 / 稱號', value: 'grant_badge_title' },
  { label: '發放遊戲內獎勵', value: 'grant_game_reward' },
  { label: '停用原效果', value: 'disable' },
];
const rewardTypeOptions = [
  { label: '徽章 / 稱號', value: 'badge_title' },
  { label: '收集物拾取', value: 'collectible_pickup' },
  { label: '城市金幣', value: 'coin' },
  { label: '遊戲內獎勵', value: 'game_reward' },
];
const statusOptions = [
  { label: '編輯中', value: 'draft' },
  { label: '已發佈', value: 'published' },
  { label: '已封存', value: 'archived' },
];

const triggerLabelMap = Object.fromEntries(triggerTypeOptions.map((option) => [option.value, option.label]));
const effectLabelMap = Object.fromEntries(effectTypeOptions.map((option) => [option.value, option.label]));
const rewardLabelMap = Object.fromEntries(rewardTypeOptions.map((option) => [option.value, option.label]));
const statusLabelMap = Object.fromEntries(statusOptions.map((option) => [option.value, option.label]));
const riskLabelMap: Record<string, string> = {
  critical: '極高',
  high: '高',
  medium: '中',
  low: '低',
  normal: '正常',
};

const severityClass = (severity?: string) => {
  if (severity === 'error') return 'experience-conflict-tag experience-conflict-tag-error';
  if (severity === 'warning') return 'experience-conflict-tag experience-conflict-tag-warning';
  return 'experience-conflict-tag experience-conflict-tag-info';
};

const riskColor = (risk?: string) => {
  if (risk === 'critical') return 'magenta';
  if (risk === 'high') return 'red';
  if (risk === 'low') return 'cyan';
  return 'blue';
};

const idFilterValue = (value?: number) => (typeof value === 'number' ? String(value) : '');

const parseIdFilter = (value: string) => {
  const parsed = Number(value.trim());
  return Number.isFinite(parsed) && parsed > 0 ? parsed : undefined;
};

const sourceLabel = (source?: string) => {
  if (source === 'experience_step') return '體驗流程';
  if (source === 'story_override') return '故事覆寫';
  if (source === 'indoor_behavior') return '室內互動';
  if (source === 'reward_rule') return '獎勵規則';
  return source || '-';
};

const defaultQuery: AdminExperienceGovernanceQuery = {
  pageNum: 1,
  pageSize: 20,
};

const ExperienceGovernanceCenter: React.FC = () => {
  const { message } = AntdApp.useApp();
  const [query, setQuery] = useState<AdminExperienceGovernanceQuery>(defaultQuery);
  const [overview, setOverview] = useState<AdminExperienceGovernanceOverview | null>(null);
  const [items, setItems] = useState<AdminExperienceGovernanceItem[]>([]);
  const [total, setTotal] = useState(0);
  const [loading, setLoading] = useState(false);
  const [checking, setChecking] = useState(false);
  const [detail, setDetail] = useState<AdminExperienceGovernanceDetail | null>(null);
  const [conflicts, setConflicts] = useState<AdminExperienceGovernanceFinding[]>([]);

  const loadData = async (nextQuery = query) => {
    setLoading(true);
    try {
      const [overviewRes, itemsRes] = await Promise.all([
        getAdminExperienceGovernanceOverview(),
        getAdminExperienceGovernanceItems(nextQuery),
      ]);
      if (overviewRes.success && overviewRes.data) setOverview(overviewRes.data);
      if (itemsRes.success && itemsRes.data) {
        setItems(itemsRes.data.list || []);
        setTotal(itemsRes.data.total || 0);
      }
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const updateQuery = (patch: Partial<AdminExperienceGovernanceQuery>) => {
    setQuery((prev) => ({ ...prev, pageNum: 1, ...patch }));
  };

  const openDetail = async (item: AdminExperienceGovernanceItem) => {
    const response = await getAdminExperienceGovernanceDetail(item.itemKey);
    if (!response.success || !response.data) {
      message.error(response.message || '讀取治理詳情失敗');
      return;
    }
    setDetail(response.data);
  };

  const runCheck = async () => {
    setChecking(true);
    try {
      const response = await checkAdminExperienceGovernanceConflicts(query);
      if (!response.success) throw new Error(response.message || '衝突檢查失敗');
      setConflicts(response.data || []);
      message.success(`已完成衝突檢查：${(response.data || []).length} 項`);
      await loadData(query);
    } catch (error) {
      if (error instanceof Error) message.error(error.message);
    } finally {
      setChecking(false);
    }
  };

  const columns: ColumnsType<AdminExperienceGovernanceItem> = [
    { title: '來源', dataIndex: 'sourceDomain', width: 110, render: (value) => <Tag>{sourceLabel(value)}</Tag> },
    {
      title: '主體',
      render: (_, record) => (
        <Space direction="vertical" size={2}>
          <Text>{record.ownerName || record.ownerCode || record.ownerType}</Text>
          <Tooltip title={record.itemKey}><Text type="secondary" className="experience-code-text">{record.ownerType} #{record.ownerId || record.ownerCode || '-'}</Text></Tooltip>
        </Space>
      ),
    },
    {
      title: '模板 / 步驟',
      render: (_, record) => (
        <Space direction="vertical" size={2}>
          <Text strong>{record.templateNameZh || record.templateCode || record.stepCode}</Text>
          <Text type="secondary" className="experience-code-text">{record.templateType} · {record.stepCode || record.templateCode}</Text>
        </Space>
      ),
    },
    {
      title: '觸發與效果',
      render: (_, record) => (
        <Space wrap>
          <Tag>{triggerLabelMap[record.triggerType || ''] || record.triggerType || '-'}</Tag>
          <Tag>{effectLabelMap[record.effectFamily || ''] || record.effectFamily || '-'}</Tag>
          {record.rewardType ? <Tag>{rewardLabelMap[record.rewardType] || record.rewardType}</Tag> : null}
        </Space>
      ),
    },
    { title: '風險', dataIndex: 'riskLevel', width: 90, render: (value) => <Tag color={riskColor(value)}>{riskLabelMap[value || 'normal'] || value || '正常'}</Tag> },
    { title: '衝突', dataIndex: 'conflictCount', width: 80, render: (value) => value ? <Tag color="red">{value}</Tag> : <Tag>0</Tag> },
    { title: '狀態', dataIndex: 'status', width: 100, render: (value) => <Tag color={value === 'published' ? 'green' : 'gold'}>{statusLabelMap[value || ''] || value || '未知'}</Tag> },
    { title: '操作', width: 100, render: (_, record) => <Button type="link" onClick={() => openDetail(record)}>查看詳情</Button> },
  ];

  return (
    <div className="experience-workbench-shell">
      <Card className="experience-workbench-hero">
        <Title level={3} style={{ marginTop: 0 }}>體驗規則治理中心</Title>
        <Paragraph style={{ marginBottom: 0 }}>
          聚合 POI 預設流程、故事章節覆寫、室內互動行為與獎勵規則，用同一套篩選與衝突檢查查看觸發鏈是否重合、獎勵是否重複、必要步驟是否被錯誤關閉。
        </Paragraph>
      </Card>

      <Row gutter={12}>
        <Col xs={12} md={4}><Card><Statistic title="模板" value={overview?.templateCount || 0} /></Card></Col>
        <Col xs={12} md={4}><Card><Statistic title="流程" value={overview?.flowCount || 0} /></Card></Col>
        <Col xs={12} md={4}><Card><Statistic title="綁定" value={overview?.bindingCount || 0} /></Card></Col>
        <Col xs={12} md={4}><Card><Statistic title="覆寫" value={overview?.overrideCount || 0} /></Card></Col>
        <Col xs={12} md={4}><Card><Statistic title="衝突" value={(overview?.findings || []).length + conflicts.length} /></Card></Col>
        <Col xs={12} md={4}><Card><Statistic title="高風險" value={overview?.highRiskTemplateCount || 0} /></Card></Col>
      </Row>

      <Card title="篩選條件" extra={<Space><Button onClick={() => loadData(query)} loading={loading}>查詢</Button><Button type="primary" onClick={runCheck} loading={checking}>重新檢查衝突</Button></Space>}>
        <div className="experience-governance-filter-grid">
          <label className="experience-filter-field"><span className="experience-filter-field-label">關鍵字</span><Input allowClear placeholder="搜尋主體、模板、步驟代碼" value={query.keyword} onChange={(event) => updateQuery({ keyword: event.target.value })} /></label>
          <label className="experience-filter-field"><span className="experience-filter-field-label">城市 ID</span><Input allowClear inputMode="numeric" placeholder="例如 1" value={idFilterValue(query.cityId)} onChange={(event) => updateQuery({ cityId: parseIdFilter(event.target.value) })} /></label>
          <label className="experience-filter-field"><span className="experience-filter-field-label">子地圖 ID</span><Input allowClear inputMode="numeric" placeholder="例如 12" value={idFilterValue(query.subMapId)} onChange={(event) => updateQuery({ subMapId: parseIdFilter(event.target.value) })} /></label>
          <label className="experience-filter-field"><span className="experience-filter-field-label">POI ID</span><Input allowClear inputMode="numeric" placeholder="例如 300038" value={idFilterValue(query.poiId)} onChange={(event) => updateQuery({ poiId: parseIdFilter(event.target.value) })} /></label>
          <label className="experience-filter-field"><span className="experience-filter-field-label">室內建築 ID</span><Input allowClear inputMode="numeric" placeholder="例如 5" value={idFilterValue(query.indoorBuildingId)} onChange={(event) => updateQuery({ indoorBuildingId: parseIdFilter(event.target.value) })} /></label>
          <label className="experience-filter-field"><span className="experience-filter-field-label">故事線 ID</span><Input allowClear inputMode="numeric" placeholder="例如 2" value={idFilterValue(query.storylineId)} onChange={(event) => updateQuery({ storylineId: parseIdFilter(event.target.value) })} /></label>
          <label className="experience-filter-field"><span className="experience-filter-field-label">章節 ID</span><Input allowClear inputMode="numeric" placeholder="例如 9" value={idFilterValue(query.storyChapterId)} onChange={(event) => updateQuery({ storyChapterId: parseIdFilter(event.target.value) })} /></label>
          <label className="experience-filter-field"><span className="experience-filter-field-label">主體類型</span><Select allowClear value={query.ownerType} options={ownerTypeOptions} onChange={(value) => updateQuery({ ownerType: value })} /></label>
          <label className="experience-filter-field"><span className="experience-filter-field-label">模板類型</span><Select allowClear value={query.templateType} options={templateTypeOptions} onChange={(value) => updateQuery({ templateType: value })} /></label>
          <label className="experience-filter-field"><span className="experience-filter-field-label">觸發類型</span><Select allowClear value={query.triggerType} options={triggerTypeOptions} onChange={(value) => updateQuery({ triggerType: value })} /></label>
          <label className="experience-filter-field"><span className="experience-filter-field-label">效果類型</span><Select allowClear value={query.effectFamily} options={effectTypeOptions} onChange={(value) => updateQuery({ effectFamily: value })} /></label>
          <label className="experience-filter-field"><span className="experience-filter-field-label">獎勵類型</span><Select allowClear value={query.rewardType} options={rewardTypeOptions} onChange={(value) => updateQuery({ rewardType: value })} /></label>
          <label className="experience-filter-field"><span className="experience-filter-field-label">狀態</span><Select allowClear value={query.status} options={statusOptions} onChange={(value) => updateQuery({ status: value })} /></label>
          <label className="experience-filter-field"><span className="experience-filter-field-label">只看故事覆寫</span><Switch checked={!!query.storyOverrideOnly} onChange={(checked) => updateQuery({ storyOverrideOnly: checked })} /></label>
          <label className="experience-filter-field"><span className="experience-filter-field-label">只看高風險</span><Switch checked={!!query.highRiskOnly} onChange={(checked) => updateQuery({ highRiskOnly: checked })} /></label>
          <label className="experience-filter-field"><span className="experience-filter-field-label">只看衝突</span><Switch checked={!!query.conflictOnly} onChange={(checked) => updateQuery({ conflictOnly: checked })} /></label>
        </div>
      </Card>

      <Card title="治理項目">
        <Table
          rowKey="itemKey"
          loading={loading}
          columns={columns}
          dataSource={items}
          pagination={{
            current: query.pageNum || 1,
            pageSize: query.pageSize || 20,
            total,
            onChange: (pageNum, pageSize) => {
              const nextQuery = { ...query, pageNum, pageSize };
              setQuery(nextQuery);
              loadData(nextQuery);
            },
          }}
        />
      </Card>

      {(conflicts.length > 0 || (overview?.findings || []).length > 0) && (
        <Card title="最近衝突檢查">
          <Space direction="vertical" style={{ width: '100%' }}>
            {[...conflicts, ...(overview?.findings || [])].slice(0, 12).map((conflict, index) => (
              <Card key={`${conflict.findingType}-${index}`} size="small">
                <Space direction="vertical" size={4}>
                  <Tag className={severityClass(conflict.severity)}>{conflict.severity}</Tag>
                  <Text strong>{conflict.title}</Text>
                  <Text type="secondary">{conflict.findingType} · {conflict.description}</Text>
                </Space>
              </Card>
            ))}
          </Space>
        </Card>
      )}

      <Drawer open={!!detail} title="治理詳情" width={820} onClose={() => setDetail(null)}>
        <Space direction="vertical" style={{ width: '100%' }} size="large">
          <Card title="主體與模板">
            <Space direction="vertical">
              <Text strong>{detail?.item.templateNameZh || detail?.item.templateCode}</Text>
              <Text type="secondary">{detail?.item.itemKey}</Text>
              <Space wrap>
                <Tag>{sourceLabel(detail?.item.sourceDomain)}</Tag>
                <Tag>{detail?.item.ownerType}</Tag>
                <Tag color={riskColor(detail?.item.riskLevel)}>{detail?.item.riskLevel}</Tag>
              </Space>
            </Space>
          </Card>
          <Card title="使用關係">
            <Table
              rowKey={(record) =>
                [
                  record.sourceDomain,
                  record.relationType,
                  record.ownerType,
                  record.ownerId || '-',
                  record.flowId || '-',
                  record.stepId || '-',
                  record.description || '-',
                ].join(':')
              }
              dataSource={detail?.usageRefs || []}
              pagination={false}
              columns={[
                { title: '來源', dataIndex: 'sourceDomain' },
                { title: '關係', dataIndex: 'relationType' },
                { title: '主體', render: (_, record) => `${record.ownerType} #${record.ownerId || '-'}` },
                { title: '描述', dataIndex: 'description' },
              ]}
            />
          </Card>
          <Card title="衝突">
            <Space direction="vertical" style={{ width: '100%' }}>
              {(detail?.conflicts || []).length === 0 ? <Text type="secondary">暫無衝突。</Text> : null}
              {(detail?.conflicts || []).map((conflict, index) => (
                <Card key={`${conflict.findingType}-${index}`} size="small">
                  <Tag className={severityClass(conflict.severity)}>{conflict.severity}</Tag>
                  <Text strong style={{ marginLeft: 8 }}>{conflict.title}</Text>
                  <Paragraph type="secondary" style={{ marginBottom: 0 }}>{conflict.description}</Paragraph>
                </Card>
              ))}
            </Space>
          </Card>
        </Space>
      </Drawer>
    </div>
  );
};

export default ExperienceGovernanceCenter;
