import React, { useEffect, useMemo, useState } from 'react';
import { PageContainer } from '@ant-design/pro-layout';
import {
  Alert,
  Button,
  Card,
  Checkbox,
  DatePicker,
  Descriptions,
  Drawer,
  Form,
  Input,
  Select,
  Space,
  Statistic,
  Switch,
  Table,
  Tabs,
  Tag,
  Tooltip,
  Typography,
  message,
} from 'antd';
import type { ColumnsType } from 'antd/es/table';
import dayjs, { type Dayjs } from 'dayjs';
import {
  cancelLifecycleOperation,
  createLifecycleOperation,
  getLifecycleOperation,
  getLifecycleOperations,
  getLifecycleStatuses,
  getLifecycleTargets,
  getLifecycleTargetTypes,
  previewLifecycleOperation,
  runDueLifecycleOperations,
} from '../../services/api';
import type {
  AdminLifecycleImpact,
  AdminLifecycleOperationDetail,
  AdminLifecycleOperationSummary,
  AdminLifecyclePreviewResponse,
  AdminLifecycleStatus,
  AdminLifecycleTargetSummary,
  AdminLifecycleTargetType,
} from '../../types/admin';
import './OperationsLifecycleWorkbench.scss';

const { Text, Title, Paragraph } = Typography;

type ActionValue = 'publish' | 'unpublish' | 'remove';
type ExecutionMode = 'immediate' | 'scheduled';

interface ActionFormValues {
  action: ActionValue;
  executionMode: ExecutionMode;
  scheduledAt?: Dayjs;
  reason?: string;
  confirmedImpact?: boolean;
  cascade?: boolean;
}

const actionOptions = [
  { label: '發布', value: 'publish' },
  { label: '下線', value: 'unpublish' },
  { label: '移除', value: 'remove' },
];

function EllipsisText({ value, width = 260 }: { value?: string | number | null; width?: number }) {
  const text = value == null || value === '' ? '-' : String(value);
  return (
    <Tooltip title={text}>
      <span className="operations-lifecycle__ellipsis" style={{ maxWidth: width }}>
        {text}
      </span>
    </Tooltip>
  );
}

function severityColor(severity?: string) {
  if (severity === 'blocking') return 'red';
  if (severity === 'warning') return 'gold';
  return 'blue';
}

function statusColor(status?: string) {
  if (status === 'published') return 'green';
  if (status === 'deleted') return 'red';
  if (status === 'reviewing') return 'blue';
  if (status === 'unpublished') return 'default';
  return 'gold';
}

const OperationsLifecycleWorkbench: React.FC = () => {
  const [targetTypes, setTargetTypes] = useState<AdminLifecycleTargetType[]>([]);
  const [statuses, setStatuses] = useState<AdminLifecycleStatus[]>([]);
  const [targets, setTargets] = useState<AdminLifecycleTargetSummary[]>([]);
  const [targetTotal, setTargetTotal] = useState(0);
  const [targetLoading, setTargetLoading] = useState(false);
  const [history, setHistory] = useState<AdminLifecycleOperationSummary[]>([]);
  const [historyTotal, setHistoryTotal] = useState(0);
  const [historyLoading, setHistoryLoading] = useState(false);
  const [selectedTarget, setSelectedTarget] = useState<AdminLifecycleTargetSummary | null>(null);
  const [preview, setPreview] = useState<AdminLifecyclePreviewResponse | null>(null);
  const [previewOpen, setPreviewOpen] = useState(false);
  const [previewLoading, setPreviewLoading] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [detailOpen, setDetailOpen] = useState(false);
  const [operationDetail, setOperationDetail] = useState<AdminLifecycleOperationDetail | null>(null);
  const [filterForm] = Form.useForm();
  const [actionForm] = Form.useForm<ActionFormValues>();
  const confirmedImpact = Form.useWatch('confirmedImpact', actionForm);
  const executionMode = Form.useWatch('executionMode', actionForm);

  const targetTypeOptions = useMemo(
    () => targetTypes.map((item) => ({ label: item.label, value: item.targetType })),
    [targetTypes],
  );
  const statusOptions = useMemo(
    () => statuses.map((item) => ({ label: item.label, value: item.canonicalStatus || item.status })),
    [statuses],
  );

  const loadCatalog = async () => {
    const [typeResponse, statusResponse] = await Promise.all([getLifecycleTargetTypes(), getLifecycleStatuses()]);
    setTargetTypes(typeResponse.data || []);
    setStatuses(statusResponse.data || []);
  };

  const loadTargets = async (params?: any) => {
    setTargetLoading(true);
    try {
      const values = params || filterForm.getFieldsValue();
      const response = await getLifecycleTargets({
        pageNum: values.pageNum || 1,
        pageSize: values.pageSize || 20,
        keyword: values.keyword,
        targetType: values.targetType,
        status: values.status,
        publishedOnly: values.publishedOnly,
        withDependenciesOnly: values.withDependenciesOnly,
      });
      setTargets(response.data?.list || []);
      setTargetTotal(response.data?.total || 0);
    } finally {
      setTargetLoading(false);
    }
  };

  const loadHistory = async (params?: any) => {
    setHistoryLoading(true);
    try {
      const response = await getLifecycleOperations({
        pageNum: params?.pageNum || 1,
        pageSize: params?.pageSize || 10,
        keyword: params?.keyword,
        targetType: params?.targetType,
        action: params?.action,
        operationStatus: params?.operationStatus,
      });
      setHistory(response.data?.list || []);
      setHistoryTotal(response.data?.total || 0);
    } finally {
      setHistoryLoading(false);
    }
  };

  useEffect(() => {
    void loadCatalog();
    void loadTargets();
    void loadHistory();
  }, []);

  const openPreview = async (record: AdminLifecycleTargetSummary, action: ActionValue = 'unpublish') => {
    setSelectedTarget(record);
    actionForm.setFieldsValue({
      action,
      executionMode: 'immediate',
      confirmedImpact: false,
      cascade: false,
      reason: '',
    });
    setPreview(null);
    setPreviewOpen(true);
    await requestPreview(record, action, false);
  };

  const requestPreview = async (
    record = selectedTarget,
    action = actionForm.getFieldValue('action') || 'unpublish',
    cascade = Boolean(actionForm.getFieldValue('cascade')),
  ) => {
    if (!record) return;
    setPreviewLoading(true);
    try {
      const response = await previewLifecycleOperation({
        targetType: record.targetType,
        targetId: record.targetId,
        targetCode: record.targetCode,
        action,
        executionMode: actionForm.getFieldValue('executionMode') || 'immediate',
        cascade,
      });
      setPreview(response.data || null);
      actionForm.setFieldsValue({ confirmedImpact: false });
    } finally {
      setPreviewLoading(false);
    }
  };

  const submitOperation = async () => {
    if (!selectedTarget || !preview) {
      message.warning('請先預覽影響');
      return;
    }
    const values = await actionForm.validateFields();
    if (!values.confirmedImpact) {
      message.warning('請先確認已閱讀影響');
      return;
    }
    if ((values.action === 'remove' || values.action === 'unpublish') && !values.reason?.trim()) {
      message.warning('下線或移除前需要填寫操作原因');
      return;
    }
    setSubmitting(true);
    try {
      const response = await createLifecycleOperation({
        targetType: selectedTarget.targetType,
        targetId: selectedTarget.targetId,
        targetCode: selectedTarget.targetCode,
        action: values.action,
        executionMode: values.executionMode,
        scheduledAt: values.scheduledAt?.format('YYYY-MM-DDTHH:mm:ss'),
        reason: values.reason,
        previewHash: preview.previewHash,
        confirmedImpact: true,
        cascade: values.cascade,
      });
      message.success(values.executionMode === 'scheduled' ? '已加入排程' : '已立即套用');
      setPreviewOpen(false);
      setSelectedTarget(null);
      setPreview(null);
      await Promise.all([loadTargets(), loadHistory()]);
      if (response.data?.id) {
        void openOperationDetail(response.data.id);
      }
    } finally {
      setSubmitting(false);
    }
  };

  const runDue = async () => {
    setSubmitting(true);
    try {
      const response = await runDueLifecycleOperations();
      message.success(`已執行 ${response.data?.length || 0} 個到期排程`);
      await Promise.all([loadTargets(), loadHistory()]);
    } finally {
      setSubmitting(false);
    }
  };

  const openOperationDetail = async (operationId: number) => {
    const response = await getLifecycleOperation(operationId);
    setOperationDetail(response.data || null);
    setDetailOpen(true);
  };

  const cancelOperation = async (operationId: number) => {
    await cancelLifecycleOperation(operationId, { reason: '管理員在生命週期工作台取消排程' });
    message.success('已取消排程');
    await loadHistory();
  };

  const targetColumns: ColumnsType<AdminLifecycleTargetSummary> = [
    {
      title: '主體',
      key: 'target',
      render: (_, record) => (
        <Space direction="vertical" size={0}>
          <Text strong>
            <EllipsisText value={record.targetName} width={260} />
          </Text>
          <Text type="secondary">
            <EllipsisText value={record.targetCode || `#${record.targetId}`} width={280} />
          </Text>
        </Space>
      ),
    },
    {
      title: '主體類型',
      dataIndex: 'targetTypeLabel',
      width: 150,
      render: (value) => <Tag>{value}</Tag>,
    },
    {
      title: '目前狀態',
      dataIndex: 'canonicalStatus',
      width: 140,
      render: (_, record) => <Tag color={statusColor(record.canonicalStatus)}>{record.statusLabel || record.status}</Tag>,
    },
    {
      title: '公開內容',
      dataIndex: 'travelerVisible',
      width: 120,
      render: (value) => (value ? <Tag color="green">小程序可見</Tag> : <Tag>不公開</Tag>),
    },
    {
      title: '依賴',
      dataIndex: 'dependencyCount',
      width: 100,
      render: (value) => <Text>{value || 0}</Text>,
    },
    {
      title: '更新時間',
      dataIndex: 'updatedAt',
      width: 180,
      render: (value) => (value ? dayjs(value).format('YYYY-MM-DD HH:mm') : '-'),
    },
    {
      title: '操作',
      width: 220,
      render: (_, record) => (
        <Space>
          <Button size="small" type="primary" onClick={() => openPreview(record, 'publish')}>
            發布
          </Button>
          <Button size="small" onClick={() => openPreview(record, 'unpublish')}>
            下線
          </Button>
          <Button danger size="small" onClick={() => openPreview(record, 'remove')}>
            移除
          </Button>
        </Space>
      ),
    },
  ];

  const historyColumns: ColumnsType<AdminLifecycleOperationSummary> = [
    {
      title: '時間',
      dataIndex: 'createdAt',
      width: 170,
      render: (value) => (value ? dayjs(value).format('YYYY-MM-DD HH:mm') : '-'),
    },
    {
      title: '主體',
      key: 'target',
      render: (_, record) => (
        <Space direction="vertical" size={0}>
          <Text>{record.targetName}</Text>
          <Text type="secondary">{record.targetTypeLabel} / {record.targetCode || record.targetId}</Text>
        </Space>
      ),
    },
    {
      title: '操作',
      dataIndex: 'actionLabel',
      width: 100,
      render: (value, record) => <Tag color={record.action === 'remove' ? 'red' : record.action === 'publish' ? 'green' : 'gold'}>{value || record.action}</Tag>,
    },
    {
      title: '狀態變更',
      key: 'statusChange',
      width: 180,
      render: (_, record) => `${record.fromStatusLabel || record.fromStatus || '-'} → ${record.toStatusLabel || record.toStatus || '-'}`,
    },
    {
      title: '排程狀態',
      dataIndex: 'operationStatusLabel',
      width: 120,
    },
    {
      title: '操作人',
      dataIndex: 'requestedByName',
      width: 130,
      render: (value) => value || '-',
    },
    {
      title: '影響數',
      key: 'impact',
      width: 110,
      render: (_, record) => (
        <Space size={4}>
          <Tag>{record.impactCount || 0}</Tag>
          {!!record.blockingImpactCount && <Tag color="red">{record.blockingImpactCount} 阻擋</Tag>}
        </Space>
      ),
    },
    {
      title: '結果',
      dataIndex: 'errorMessage',
      render: (value, record) => value ? <Text type="danger">{value}</Text> : <Text>{record.operationStatusLabel}</Text>,
    },
    {
      title: '操作',
      width: 150,
      render: (_, record) => (
        <Space>
          <Button size="small" onClick={() => openOperationDetail(record.id)}>
            詳情
          </Button>
          {record.operationStatus === 'scheduled' && (
            <Button size="small" danger onClick={() => cancelOperation(record.id)}>
              取消排程
            </Button>
          )}
        </Space>
      ),
    },
  ];

  const impactGroups = useMemo(() => {
    const list = preview?.impacts || [];
    return list.reduce<Record<string, AdminLifecycleImpact[]>>((acc, item) => {
      const key = item.impactTypeLabel || item.impactType;
      acc[key] = acc[key] || [];
      acc[key].push(item);
      return acc;
    }, {});
  }, [preview]);

  const renderImpactCard = (item: AdminLifecycleImpact) => (
    <div key={`${item.impactType}-${item.targetType}-${item.targetId}-${item.sortOrder}`} className={`operations-lifecycle__impact-card operations-lifecycle__impact-card--${item.severity || 'info'}`}>
      <Space direction="vertical" size={6} style={{ width: '100%' }}>
        <Space wrap>
          <Tag color={severityColor(item.severity)}>{item.severityLabel || item.severity}</Tag>
          <Tag>{item.impactTypeLabel || item.impactType}</Tag>
        </Space>
        <Text>{item.impactSummary}</Text>
        <Text type="secondary">
          <EllipsisText value={item.targetName || item.targetCode || item.sourceName || item.sourceCode} width={280} />
        </Text>
      </Space>
    </div>
  );

  return (
    <PageContainer className="operations-lifecycle" title={false}>
      <Card className="operations-lifecycle__hero">
        <Space direction="vertical" size={6}>
          <Title level={3} style={{ margin: 0 }}>生命週期與發布排程</Title>
          <Paragraph style={{ margin: 0 }}>
            跨地圖、故事、室內、獎勵與體驗流程檢查依賴後再發布、下線或移除。
          </Paragraph>
          <Space wrap>
            <Button onClick={() => Promise.all([loadTargets(), loadHistory()])}>重新載入</Button>
            <Button loading={submitting} onClick={runDue}>執行到期排程</Button>
          </Space>
        </Space>
      </Card>

      <div className="operations-lifecycle__summary-grid">
        <Card><Statistic title="待執行排程" value={history.filter((item) => item.operationStatus === 'scheduled').length} /></Card>
        <Card><Statistic title="今日已套用" value={history.filter((item) => item.operationStatus === 'applied').length} /></Card>
        <Card><Statistic title="高風險操作" value={history.filter((item) => (item.blockingImpactCount || 0) > 0).length} /></Card>
        <Card><Statistic title="失敗操作" value={history.filter((item) => item.operationStatus === 'failed').length} /></Card>
      </div>

      <Tabs
        items={[
          {
            key: 'targets',
            label: '內容主體',
            children: (
              <Space direction="vertical" size="middle" style={{ width: '100%' }}>
                <Card>
                  <Form form={filterForm} layout="vertical" onFinish={(values) => loadTargets({ ...values, pageNum: 1 })}>
                    <div className="operations-lifecycle__filter-grid">
                      <Form.Item label="關鍵字" name="keyword">
                        <Input allowClear placeholder="名稱、代碼、ID" />
                      </Form.Item>
                      <Form.Item label="主體類型" name="targetType">
                        <Select allowClear options={targetTypeOptions} placeholder="全部主體" />
                      </Form.Item>
                      <Form.Item label="目前狀態" name="status">
                        <Select allowClear options={statusOptions} placeholder="全部狀態" />
                      </Form.Item>
                      <Form.Item label="篩選">
                        <Space wrap>
                          <Form.Item name="publishedOnly" valuePropName="checked" noStyle>
                            <Switch checkedChildren="只看已發布" unCheckedChildren="全部" />
                          </Form.Item>
                          <Form.Item name="withDependenciesOnly" valuePropName="checked" noStyle>
                            <Switch checkedChildren="只看有依賴" unCheckedChildren="含無依賴" />
                          </Form.Item>
                          <Button type="primary" htmlType="submit">查詢</Button>
                        </Space>
                      </Form.Item>
                    </div>
                  </Form>
                </Card>
                <Table
                  rowKey={(record) => `${record.targetType}-${record.targetId}`}
                  columns={targetColumns}
                  dataSource={targets}
                  loading={targetLoading}
                  pagination={{
                    total: targetTotal,
                    pageSize: 20,
                    onChange: (pageNum) => loadTargets({ ...filterForm.getFieldsValue(), pageNum }),
                  }}
                />
              </Space>
            ),
          },
          {
            key: 'history',
            label: '操作歷史',
            children: (
              <Table
                rowKey="id"
                columns={historyColumns}
                dataSource={history}
                loading={historyLoading}
                pagination={{ total: historyTotal, pageSize: 10, onChange: (pageNum) => loadHistory({ pageNum }) }}
              />
            ),
          },
        ]}
      />

      <Drawer
        width={760}
        title="預覽影響"
        open={previewOpen}
        onClose={() => setPreviewOpen(false)}
        destroyOnClose
      >
        {selectedTarget && (
          <Space direction="vertical" size="middle" style={{ width: '100%' }}>
            <Descriptions title="操作摘要" bordered size="small" column={1}>
              <Descriptions.Item label="主體">
                {selectedTarget.targetTypeLabel} / <EllipsisText value={selectedTarget.targetName} width={420} />
              </Descriptions.Item>
              <Descriptions.Item label="狀態轉換">
                {preview?.currentStatusLabel || selectedTarget.statusLabel} → {preview?.targetStatusLabel || '-'}
              </Descriptions.Item>
              <Descriptions.Item label="Preview Hash">
                <EllipsisText value={preview?.previewHash} width={420} />
              </Descriptions.Item>
            </Descriptions>

            <Form
              form={actionForm}
              layout="vertical"
              onValuesChange={(changed) => {
                if (changed.action || changed.cascade) {
                  void requestPreview(undefined, actionForm.getFieldValue('action'), Boolean(actionForm.getFieldValue('cascade')));
                }
              }}
            >
              <div className="operations-lifecycle__filter-grid">
                <Form.Item label="操作" name="action" rules={[{ required: true }]}>
                  <Select options={actionOptions} />
                </Form.Item>
                <Form.Item label="執行方式" name="executionMode" rules={[{ required: true }]}>
                  <Select
                    options={[
                      { label: '立即執行', value: 'immediate' },
                      { label: '排程執行', value: 'scheduled' },
                    ]}
                  />
                </Form.Item>
                <Form.Item noStyle shouldUpdate={(prev, cur) => prev.executionMode !== cur.executionMode}>
                  {({ getFieldValue }) =>
                    getFieldValue('executionMode') === 'scheduled' ? (
                      <Form.Item label="排程時間" name="scheduledAt" rules={[{ required: true, message: '請選擇排程時間' }]}>
                        <DatePicker showTime style={{ width: '100%' }} />
                      </Form.Item>
                    ) : (
                      <Form.Item label="排程時間">
                        <Input disabled value="立即執行" />
                      </Form.Item>
                    )
                  }
                </Form.Item>
                <Form.Item label="級聯檢查" name="cascade" valuePropName="checked">
                  <Switch checkedChildren="允許級聯" unCheckedChildren="不級聯" />
                </Form.Item>
              </div>
              <Form.Item label="操作原因" name="reason">
                <Input.TextArea rows={3} placeholder="下線或移除前請填寫原因，方便審計與追蹤。" />
              </Form.Item>
              <Form.Item name="confirmedImpact" valuePropName="checked">
                <Checkbox>確認已閱讀影響</Checkbox>
              </Form.Item>
            </Form>

            {preview?.hasBlockingImpacts && (
              <Alert type="error" showIcon message="風險與阻擋" description="目前預覽包含阻擋項，請先處理依賴或改用較安全的操作。" />
            )}

            <Card loading={previewLoading} title="依賴影響">
              {!preview?.impacts?.length ? (
                <Text type="secondary">暫無依賴影響。</Text>
              ) : (
                <Space direction="vertical" size="middle" style={{ width: '100%' }}>
                  {Object.entries(impactGroups).map(([group, items]) => (
                    <div className="operations-lifecycle__preview-section" key={group}>
                      <Title level={5}>{group}</Title>
                      {group === '小程序公開內容' && <Text type="secondary">小程序公開內容影響</Text>}
                      {group === '探索度與用戶進度' && <Text type="secondary">探索度與用戶進度影響</Text>}
                      {group === '被其他內容綁定' && <Text type="secondary">被其他內容綁定</Text>}
                      {group === '下游子內容' && <Text type="secondary">下游子內容</Text>}
                      <div className="operations-lifecycle__impact-grid">
                        {items.map(renderImpactCard)}
                      </div>
                    </div>
                  ))}
                </Space>
              )}
            </Card>
          </Space>
        )}
        <div className="operations-lifecycle__drawer-footer">
          <Space>
            <Button onClick={() => requestPreview()} loading={previewLoading}>預覽影響</Button>
            <Button
              type="primary"
              loading={submitting}
              disabled={!preview || preview.hasBlockingImpacts || !confirmedImpact}
              onClick={submitOperation}
            >
              {executionMode === 'scheduled' ? '加入排程' : '立即套用'}
            </Button>
            <Button onClick={() => setPreviewOpen(false)}>關閉</Button>
          </Space>
        </div>
      </Drawer>

      <Drawer
        width={720}
        title="操作歷史詳情"
        open={detailOpen}
        onClose={() => setDetailOpen(false)}
      >
        {operationDetail?.summary && (
          <Space direction="vertical" size="middle" style={{ width: '100%' }}>
            <Descriptions bordered size="small" column={1}>
              <Descriptions.Item label="主體">{operationDetail.summary.targetName}</Descriptions.Item>
              <Descriptions.Item label="操作">{operationDetail.summary.actionLabel}</Descriptions.Item>
              <Descriptions.Item label="狀態變更">
                {operationDetail.summary.fromStatusLabel} → {operationDetail.summary.toStatusLabel}
              </Descriptions.Item>
              <Descriptions.Item label="排程狀態">{operationDetail.summary.operationStatusLabel}</Descriptions.Item>
              <Descriptions.Item label="操作人">{operationDetail.summary.requestedByName || '-'}</Descriptions.Item>
              <Descriptions.Item label="結果">{operationDetail.summary.errorMessage || '已記錄'}</Descriptions.Item>
            </Descriptions>
            <Card title="影響記錄">
              <Space direction="vertical" style={{ width: '100%' }}>
                {(operationDetail.impacts || []).map(renderImpactCard)}
              </Space>
            </Card>
          </Space>
        )}
      </Drawer>
    </PageContainer>
  );
};

export default OperationsLifecycleWorkbench;
