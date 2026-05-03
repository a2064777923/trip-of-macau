import React, { useMemo, useState } from 'react';
import { useRequest } from 'ahooks';
import {
  Button,
  Card,
  Col,
  Collapse,
  Descriptions,
  Drawer,
  Empty,
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
  getAiGenerationJobDetail,
  getAiGenerationJobs,
  getAiInventory,
  getAiLogs,
  getAiOverview,
  getAiProviders,
  type AiGenerationJobItem,
  type AiInventoryItem,
  type AiLogItem,
} from '../../services/api';

const { Paragraph, Text, Title } = Typography;

type DetailRecord =
  | { type: 'job'; data: AiGenerationJobItem }
  | { type: 'log'; data: AiLogItem }
  | null;

const statusTextMap: Record<string, string> = {
  pending: '等待中',
  submitted: '已提交',
  running: '生成中',
  completed: '已完成',
  failed: '失敗',
  cancelled: '已取消',
};

const typeTextMap: Record<string, string> = {
  image: '圖片生成',
  audio: '語音生成',
  text: '文案生成',
  tts_generation: '語音合成',
  image_generation: '圖片生成',
  provider_test: '供應商測試',
};

function statusColor(value?: string) {
  if (value === 'healthy' || value === 'completed' || value === 'success') {
    return 'green';
  }
  if (value === 'warning' || value === 'unknown' || value === 'pending' || value === 'submitted' || value === 'idle') {
    return 'gold';
  }
  if (value === 'failed' || value === 'error') {
    return 'red';
  }
  return 'default';
}

function formatDate(value?: string) {
  return value?.replace('T', ' ').slice(0, 19) || '-';
}

function formatLatency(value?: number) {
  return value == null ? '-' : `${value} ms`;
}

function costTypeLabel(value?: string) {
  if (value === 'actual') {
    return '實際';
  }
  if (value === 'estimated') {
    return '估算';
  }
  return '供應商未返回';
}

function formatCost(record: { costLabel?: string; costUsd?: number; costType?: string }) {
  if (record.costLabel) {
    return record.costLabel;
  }
  if (record.costUsd != null) {
    return `US$${Number(record.costUsd).toFixed(4)}`;
  }
  return '供應商未返回';
}

function renderLongText(value?: React.ReactNode) {
  if (value === null || value === undefined || value === '') {
    return '-';
  }
  return (
    <Tooltip title={String(value)}>
      <Typography.Text ellipsis={{ tooltip: String(value) }} style={{ maxWidth: 260 }}>
        {value}
      </Typography.Text>
    </Tooltip>
  );
}

function renderCode(value?: React.ReactNode) {
  if (value === null || value === undefined || value === '') {
    return '-';
  }
  return (
    <Typography.Text code ellipsis={{ tooltip: String(value) }} style={{ maxWidth: 220 }}>
      {value}
    </Typography.Text>
  );
}

function renderCost(record: { costLabel?: string; costUsd?: number; costType?: string }) {
  return (
    <Space size={4}>
      <Text>{formatCost(record)}</Text>
      <Tag color={record.costType === 'provider_unavailable' || !record.costType ? 'default' : 'blue'}>
        {costTypeLabel(record.costType)}
      </Tag>
    </Space>
  );
}

function getList<T>(response: any): T[] {
  return response?.data?.list || response?.data?.records || [];
}

function getTotal(response: any) {
  return response?.data?.total || 0;
}

function buildModelOptions(inventory: AiInventoryItem[]) {
  return inventory.map((item) => ({
    value: item.inventoryCode,
    label: item.displayName ? `${item.displayName} (${item.inventoryCode})` : item.inventoryCode,
  }));
}

const ObservabilityPage: React.FC = () => {
  const [capabilityCode, setCapabilityCode] = useState<string | undefined>();
  const [providerId, setProviderId] = useState<number | undefined>();
  const [inventoryCode, setInventoryCode] = useState<string | undefined>();
  const [jobStatus, setJobStatus] = useState<string | undefined>();
  const [generationType, setGenerationType] = useState<string | undefined>();
  const [adminOwnerKeyword, setAdminOwnerKeyword] = useState<string>('');
  const [onlyFailures, setOnlyFailures] = useState(false);
  const [onlyWithAsset, setOnlyWithAsset] = useState(false);
  const [detailRecord, setDetailRecord] = useState<DetailRecord>(null);

  const overviewReq = useRequest(() => getAiOverview());
  const providersReq = useRequest(() => getAiProviders());
  const inventoryReq = useRequest(() => getAiInventory({ providerId, capabilityCode }), {
    refreshDeps: [providerId, capabilityCode],
  });
  const jobsReq = useRequest(
    () =>
      getAiGenerationJobs({
        pageNum: 1,
        pageSize: 30,
        capabilityCode,
        generationType,
        jobStatus,
      }),
    {
      refreshDeps: [capabilityCode, generationType, jobStatus],
    },
  );
  const logsReq = useRequest(
    () =>
      getAiLogs({
        pageNum: 1,
        pageSize: 50,
        capabilityCode,
        providerId,
        inventoryCode,
        success: onlyFailures ? 0 : undefined,
      }),
    {
      refreshDeps: [capabilityCode, providerId, inventoryCode, onlyFailures],
    },
  );
  const detailReq = useRequest((jobId: number) => getAiGenerationJobDetail(jobId), {
    manual: true,
    onSuccess: (response) => {
      if (response.success && response.data) {
        setDetailRecord({ type: 'job', data: response.data });
      }
    },
  });

  const overview = overviewReq.data?.data;
  const providers = providersReq.data?.data || [];
  const inventory = inventoryReq.data?.data || [];
  const rawJobs = getList<AiGenerationJobItem>(jobsReq.data);
  const rawLogs = getList<AiLogItem>(logsReq.data);

  const jobs = useMemo(() => {
    const ownerNeedle = adminOwnerKeyword.trim().toLowerCase();
    return rawJobs.filter((item) => {
      if (onlyWithAsset && !item.latestAssetUrl && !item.latestAssetName && !item.candidateCount) {
        return false;
      }
      if (providerId && item.providerId !== providerId) {
        return false;
      }
      if (inventoryCode && item.inventoryCode !== inventoryCode) {
        return false;
      }
      if (ownerNeedle && !(item.ownerAdminName || '').toLowerCase().includes(ownerNeedle)) {
        return false;
      }
      return true;
    });
  }, [adminOwnerKeyword, inventoryCode, onlyWithAsset, providerId, rawJobs]);

  const logs = useMemo(() => {
    const ownerNeedle = adminOwnerKeyword.trim().toLowerCase();
    return rawLogs.filter((item) => {
      if (ownerNeedle && !(item.adminOwnerName || '').toLowerCase().includes(ownerNeedle)) {
        return false;
      }
      return true;
    });
  }, [adminOwnerKeyword, rawLogs]);

  const latestError = useMemo(() => {
    return logs.find((item) => item.success !== 1)?.safeOutputSummary || logs.find((item) => item.success !== 1)?.errorMessage || '-';
  }, [logs]);

  const producedAssetCount = useMemo(() => jobs.filter((item) => item.latestAssetUrl || item.latestAssetName).length, [jobs]);

  const refreshAll = () => {
    void overviewReq.refresh();
    void providersReq.refresh();
    void inventoryReq.refresh();
    void jobsReq.refresh();
    void logsReq.refresh();
  };

  const openJobDetail = (record: AiGenerationJobItem) => {
    if (detailReq.loading) {
      return;
    }
    void detailReq.run(record.id);
  };

  const openLogDetail = (record: AiLogItem) => {
    setDetailRecord({ type: 'log', data: record });
  };

  const jobColumns: ColumnsType<AiGenerationJobItem> = [
    {
      title: '時間',
      dataIndex: 'createdAt',
      width: 168,
      render: formatDate,
    },
    {
      title: '操作人',
      dataIndex: 'ownerAdminName',
      width: 130,
      render: renderLongText,
    },
    {
      title: '能力',
      dataIndex: 'capabilityNameZht',
      width: 170,
      render: (_, item) => renderLongText(item.capabilityNameZht || item.capabilityCode),
    },
    {
      title: '作業類型',
      dataIndex: 'generationType',
      width: 130,
      render: (value) => typeTextMap[value] || value || '-',
    },
    {
      title: '供應商',
      dataIndex: 'providerName',
      width: 150,
      render: renderLongText,
    },
    {
      title: '模型',
      dataIndex: 'inventoryCode',
      width: 180,
      render: (_, item) => renderLongText(item.inventoryDisplayName || item.inventoryCode),
    },
    {
      title: '狀態',
      dataIndex: 'jobStatus',
      width: 110,
      render: (value) => <Tag color={statusColor(value)}>{statusTextMap[value] || value || '-'}</Tag>,
    },
    {
      title: '素材包 / 目標',
      dataIndex: 'sourceScope',
      width: 160,
      render: (_, item) => renderLongText([item.sourceScope, item.sourceScopeId].filter(Boolean).join(' #') || item.promptTitle),
    },
    {
      title: '輸出素材',
      dataIndex: 'latestAssetName',
      width: 190,
      render: (_, item) => renderLongText(item.latestAssetName || item.latestAssetUrl || `${item.candidateCount || 0} 個候選`),
    },
    {
      title: '耗時',
      width: 100,
      render: () => '-',
    },
    {
      title: '成本',
      width: 170,
      render: (_, item) => renderCost(item),
    },
    {
      title: '操作',
      fixed: 'right',
      width: 110,
      render: (_, item) => (
        <Button size="small" loading={detailReq.loading && detailRecord?.type !== 'log'} onClick={() => openJobDetail(item)}>
          查看詳情
        </Button>
      ),
    },
  ];

  const logColumns: ColumnsType<AiLogItem> = [
    {
      title: '時間',
      dataIndex: 'createdAt',
      width: 168,
      render: formatDate,
    },
    {
      title: '操作人',
      dataIndex: 'adminOwnerName',
      width: 130,
      render: renderLongText,
    },
    {
      title: '能力',
      dataIndex: 'capabilityCode',
      width: 160,
      render: renderCode,
    },
    {
      title: '供應商',
      dataIndex: 'providerName',
      width: 150,
      render: renderLongText,
    },
    {
      title: '模型',
      dataIndex: 'modelCode',
      width: 180,
      render: (_, item) => renderLongText(item.modelCode || item.inventoryCode),
    },
    {
      title: '成功',
      dataIndex: 'success',
      width: 90,
      render: (value) => <Tag color={value === 1 ? 'green' : 'red'}>{value === 1 ? '成功' : '失敗'}</Tag>,
    },
    {
      title: '耗時',
      dataIndex: 'latencyMs',
      width: 110,
      render: formatLatency,
    },
    {
      title: '成本',
      width: 170,
      render: (_, item) => renderCost(item),
    },
    {
      title: '摘要',
      dataIndex: 'safeOutputSummary',
      ellipsis: true,
      render: (_, item) => renderLongText(item.safeOutputSummary || item.errorMessage),
    },
    {
      title: '操作',
      fixed: 'right',
      width: 110,
      render: (_, item) => (
        <Button size="small" onClick={() => openLogDetail(item)}>
          查看詳情
        </Button>
      ),
    },
  ];

  const loading = overviewReq.loading || jobsReq.loading || logsReq.loading;
  const drawerTitle = detailRecord?.type === 'job' ? `查看詳情 #${detailRecord.data.id}` : detailRecord?.type === 'log' ? `查看詳情 #${detailRecord.data.id}` : '查看詳情';
  const activeRecord = detailRecord?.data;
  const activeJob = detailRecord?.type === 'job' ? detailRecord.data : undefined;
  const activeLog = detailRecord?.type === 'log' ? detailRecord.data : undefined;

  return (
    <Space direction="vertical" size="large" style={{ width: '100%' }}>
      <Card style={{ borderRadius: 22 }}>
        <Row gutter={[16, 16]} align="middle">
          <Col xs={24} lg={16}>
            <Title level={3} style={{ marginTop: 0 }}>
              監控與成本
            </Title>
            <Paragraph type="secondary" style={{ marginBottom: 0 }}>
              查看生成請求、模型用量、素材產出、錯誤與成本估算，協助完成 v3.1 發布驗收。
            </Paragraph>
          </Col>
          <Col xs={24} lg={8}>
            <Space wrap style={{ justifyContent: 'flex-end', width: '100%' }}>
              <Button onClick={refreshAll} loading={loading}>
                重新整理
              </Button>
              <Button disabled>匯出安全摘要</Button>
              <Button disabled>查看驗收清單</Button>
            </Space>
          </Col>
        </Row>
      </Card>

      <Row gutter={[16, 16]}>
        <Col xs={24} md={8} xl={4}>
          <Card style={{ borderRadius: 18 }}>
            <Statistic title="24h 估算成本" value={Number(overview?.summary?.estimatedCost24h || 0)} precision={4} prefix="US$" suffix="估算" />
          </Card>
        </Col>
        <Col xs={24} md={8} xl={4}>
          <Card style={{ borderRadius: 18 }}>
            <Statistic title="24h 請求數" value={overview?.summary?.requests24h || 0} />
          </Card>
        </Col>
        <Col xs={24} md={8} xl={4}>
          <Card style={{ borderRadius: 18 }}>
            <Statistic title="24h 失敗" value={overview?.summary?.failures24h || 0} />
          </Card>
        </Col>
        <Col xs={24} md={8} xl={4}>
          <Card style={{ borderRadius: 18 }}>
            <Statistic title="待處理生成作業" value={overview?.summary?.activeJobs || 0} />
          </Card>
        </Col>
        <Col xs={24} md={8} xl={4}>
          <Card style={{ borderRadius: 18 }}>
            <Statistic title="已產出素材" value={producedAssetCount} />
          </Card>
        </Col>
        <Col xs={24} md={8} xl={4}>
          <Card style={{ borderRadius: 18 }}>
            <Text type="secondary">最近一次錯誤</Text>
            <div style={{ marginTop: 8 }}>{renderLongText(latestError)}</div>
          </Card>
        </Col>
      </Row>

      <Card title="篩選條件" style={{ borderRadius: 22 }}>
        <Row gutter={[16, 16]}>
          <Col xs={24} md={8} xl={6}>
            <Text type="secondary">能力</Text>
            <Select
              allowClear
              style={{ width: '100%', marginTop: 6 }}
              placeholder="選擇能力"
              value={capabilityCode}
              options={(overview?.capabilities || []).map((item) => ({
                value: item.capabilityCode,
                label: item.displayNameZht || item.capabilityCode,
              }))}
              onChange={setCapabilityCode}
            />
          </Col>
          <Col xs={24} md={8} xl={6}>
            <Text type="secondary">供應商</Text>
            <Select
              allowClear
              style={{ width: '100%', marginTop: 6 }}
              placeholder="選擇供應商"
              value={providerId}
              options={providers.map((provider) => ({
                value: provider.id,
                label: provider.displayName,
              }))}
              onChange={setProviderId}
            />
          </Col>
          <Col xs={24} md={8} xl={6}>
            <Text type="secondary">模型</Text>
            <Select
              allowClear
              showSearch
              optionFilterProp="label"
              style={{ width: '100%', marginTop: 6 }}
              placeholder="選擇模型"
              value={inventoryCode}
              options={buildModelOptions(inventory)}
              onChange={setInventoryCode}
            />
          </Col>
          <Col xs={24} md={8} xl={6}>
            <Text type="secondary">狀態</Text>
            <Select
              allowClear
              style={{ width: '100%', marginTop: 6 }}
              placeholder="選擇狀態"
              value={jobStatus}
              options={[
                { value: 'pending', label: '等待中' },
                { value: 'submitted', label: '已提交' },
                { value: 'completed', label: '已完成' },
                { value: 'failed', label: '失敗' },
              ]}
              onChange={setJobStatus}
            />
          </Col>
          <Col xs={24} md={8} xl={6}>
            <Text type="secondary">作業類型</Text>
            <Select
              allowClear
              style={{ width: '100%', marginTop: 6 }}
              placeholder="選擇作業類型"
              value={generationType}
              options={[
                { value: 'image', label: '圖片生成' },
                { value: 'audio', label: '語音生成' },
                { value: 'text', label: '文案生成' },
                { value: 'tts_generation', label: '語音合成' },
              ]}
              onChange={setGenerationType}
            />
          </Col>
          <Col xs={24} md={8} xl={6}>
            <Text type="secondary">操作人</Text>
            <Input
              allowClear
              style={{ marginTop: 6 }}
              placeholder="輸入操作人名稱"
              value={adminOwnerKeyword}
              onChange={(event) => setAdminOwnerKeyword(event.target.value)}
            />
          </Col>
          <Col xs={24} md={8} xl={6}>
            <Text type="secondary">只看失敗</Text>
            <div style={{ marginTop: 10 }}>
              <Switch checked={onlyFailures} onChange={setOnlyFailures} checkedChildren="開" unCheckedChildren="關" />
            </div>
          </Col>
          <Col xs={24} md={8} xl={6}>
            <Text type="secondary">只看有素材</Text>
            <div style={{ marginTop: 10 }}>
              <Switch checked={onlyWithAsset} onChange={setOnlyWithAsset} checkedChildren="開" unCheckedChildren="關" />
            </div>
          </Col>
        </Row>
      </Card>

      <Card
        title="生成作業"
        extra={<Text type="secondary">共 {jobs.length} / {getTotal(jobsReq.data)} 筆</Text>}
        style={{ borderRadius: 22 }}
      >
        <Table
          rowKey="id"
          columns={jobColumns}
          dataSource={jobs}
          loading={jobsReq.loading}
          pagination={{ pageSize: 10, showSizeChanger: false }}
          scroll={{ x: 1500 }}
          locale={{ emptyText: <Empty description="目前沒有符合條件的生成作業" /> }}
        />
      </Card>

      <Card
        title="請求日誌"
        extra={<Text type="secondary">共 {logs.length} / {getTotal(logsReq.data)} 筆</Text>}
        style={{ borderRadius: 22 }}
      >
        <Table
          rowKey="id"
          columns={logColumns}
          dataSource={logs}
          loading={logsReq.loading}
          pagination={{ pageSize: 10, showSizeChanger: false }}
          scroll={{ x: 1300 }}
          locale={{ emptyText: <Empty description="目前沒有符合條件的請求日誌" /> }}
        />
      </Card>

      <Drawer
        title={drawerTitle}
        width={860}
        open={!!detailRecord}
        onClose={() => setDetailRecord(null)}
        destroyOnClose
      >
        {activeRecord ? (
          <Space direction="vertical" size="large" style={{ width: '100%' }}>
            <Card title="基本資訊" size="small">
              <Descriptions column={1} size="small" bordered>
                <Descriptions.Item label="記錄類型">{detailRecord?.type === 'job' ? '生成作業' : '請求日誌'}</Descriptions.Item>
                <Descriptions.Item label="時間">{formatDate(activeRecord.createdAt)}</Descriptions.Item>
                <Descriptions.Item label="操作人">{activeJob?.ownerAdminName || activeLog?.adminOwnerName || '-'}</Descriptions.Item>
                <Descriptions.Item label="能力">{activeJob?.capabilityNameZht || activeJob?.capabilityCode || activeLog?.capabilityCode || '-'}</Descriptions.Item>
                <Descriptions.Item label="狀態">
                  {activeJob ? (
                    <Tag color={statusColor(activeJob.jobStatus)}>{statusTextMap[activeJob.jobStatus] || activeJob.jobStatus}</Tag>
                  ) : (
                    <Tag color={activeLog?.success === 1 ? 'green' : 'red'}>{activeLog?.success === 1 ? '成功' : '失敗'}</Tag>
                  )}
                </Descriptions.Item>
              </Descriptions>
            </Card>

            <Card title="供應商與模型" size="small">
              <Descriptions column={1} size="small" bordered>
                <Descriptions.Item label="供應商">{activeJob?.providerName || activeLog?.providerName || '-'}</Descriptions.Item>
                <Descriptions.Item label="模型">{activeJob?.inventoryDisplayName || activeJob?.inventoryCode || activeLog?.modelCode || activeLog?.inventoryCode || '-'}</Descriptions.Item>
                <Descriptions.Item label="策略">{activeJob?.policyName || activeLog?.policyName || '-'}</Descriptions.Item>
                <Descriptions.Item label="作業 / 請求類型">{typeTextMap[activeJob?.generationType || activeLog?.requestType || ''] || activeJob?.generationType || activeLog?.requestType || '-'}</Descriptions.Item>
              </Descriptions>
            </Card>

            <Card title="輸入摘要" size="small">
              <Paragraph>{activeJob?.safePromptSummary || activeJob?.promptTitle || activeLog?.inputDataHash || '沒有可顯示的安全輸入摘要。'}</Paragraph>
            </Card>

            <Card title="輸出摘要" size="small">
              <Paragraph>{activeJob?.safeRequestSummary || activeLog?.safeOutputSummary || '沒有可顯示的安全輸出摘要。'}</Paragraph>
            </Card>

            <Card title="成本與用量" size="small">
              <Descriptions column={1} size="small" bordered>
                <Descriptions.Item label="成本">{renderCost(activeJob || activeLog || {})}</Descriptions.Item>
                <Descriptions.Item label="成本類型">{costTypeLabel(activeJob?.costType || activeLog?.costType)}</Descriptions.Item>
                <Descriptions.Item label="Token">{activeLog?.tokensUsed ?? '-'}</Descriptions.Item>
                <Descriptions.Item label="耗時">{formatLatency(activeLog?.latencyMs)}</Descriptions.Item>
              </Descriptions>
            </Card>

            <Card title="素材與版本" size="small">
              <Descriptions column={1} size="small" bordered>
                <Descriptions.Item label="候選數">{activeJob?.candidateCount ?? activeJob?.candidates?.length ?? '-'}</Descriptions.Item>
                <Descriptions.Item label="最新素材">{renderLongText(activeJob?.latestAssetName || activeJob?.latestAssetUrl)}</Descriptions.Item>
                <Descriptions.Item label="素材類型">{activeJob?.latestAssetKind || '-'}</Descriptions.Item>
                <Descriptions.Item label="已確認候選">{activeJob?.finalizedCandidateId || '-'}</Descriptions.Item>
              </Descriptions>
            </Card>

            <Card title="錯誤與重試" size="small">
              <Descriptions column={1} size="small" bordered>
                <Descriptions.Item label="錯誤">{renderLongText(activeJob?.errorMessage || activeLog?.errorMessage)}</Descriptions.Item>
                <Descriptions.Item label="回退">{activeLog?.fallbackTriggered === 1 ? '已回退' : '未回退 / 不適用'}</Descriptions.Item>
                <Descriptions.Item label="阻擋原因">{renderLongText(activeLog?.blockedReason)}</Descriptions.Item>
              </Descriptions>
            </Card>

            <Card title="安全檢查" size="small">
              <Space direction="vertical">
                <Text>預設資訊只顯示安全摘要、成本標籤、作業狀態與素材連結。</Text>
                <Text type="secondary">API Key、Bearer Token、COS Secret、本地路徑與完整 Prompt 不會在表格中展示。</Text>
              </Space>
            </Card>

            <Collapse
              items={[
                {
                  key: 'advanced',
                  label: '進階診斷',
                  children: (
                    <Descriptions column={1} size="small" bordered>
                      <Descriptions.Item label="Trace ID">{renderCode(activeLog?.traceId)}</Descriptions.Item>
                      <Descriptions.Item label="Provider Request ID">{renderCode(activeJob?.providerRequestId)}</Descriptions.Item>
                      <Descriptions.Item label="Prompt Variables JSON">{renderLongText(activeJob?.promptVariablesJson)}</Descriptions.Item>
                      <Descriptions.Item label="Request Payload JSON">{renderLongText(activeJob?.requestPayloadJson)}</Descriptions.Item>
                    </Descriptions>
                  ),
                },
              ]}
            />
          </Space>
        ) : (
          <Empty description="無此資料" />
        )}
      </Drawer>
    </Space>
  );
};

export default ObservabilityPage;
