import React, { useMemo, useState } from 'react';
import { useRequest } from 'ahooks';
import { Card, Col, Row, Select, Space, Table, Tag, Typography } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { getAiLogs, getAiOverview, getAiProviders, type AiLogItem } from '../../services/api';

const { Paragraph, Text, Title } = Typography;

function statusColor(value?: string) {
  if (value === 'healthy' || value === 'completed') {
    return 'green';
  }
  if (value === 'warning' || value === 'unknown' || value === 'pending' || value === 'idle') {
    return 'gold';
  }
  if (value === 'failed' || value === 'error') {
    return 'red';
  }
  return 'default';
}

const logColumns: ColumnsType<AiLogItem> = [
  {
    title: '時間',
    dataIndex: 'createdAt',
    width: 168,
    render: (value) => value?.replace('T', ' ').slice(0, 19) || '-',
  },
  {
    title: '能力',
    dataIndex: 'capabilityCode',
    width: 160,
    render: (value) => <Text code>{value || '-'}</Text>,
  },
  {
    title: '供應商',
    dataIndex: 'providerName',
    width: 160,
    render: (value) => value || '-',
  },
  {
    title: '耗時',
    dataIndex: 'latencyMs',
    width: 110,
    render: (value) => (value ? `${value} ms` : '-'),
  },
  {
    title: '估算成本',
    dataIndex: 'costUsd',
    width: 120,
    render: (value) => (value != null ? `US$${Number(value).toFixed(4)}` : '-'),
  },
  {
    title: '狀態',
    dataIndex: 'success',
    width: 100,
    render: (value) => <Tag color={value === 1 ? 'green' : 'red'}>{value === 1 ? '成功' : '失敗'}</Tag>,
  },
  {
    title: '摘要',
    dataIndex: 'outputSummary',
    ellipsis: true,
    render: (_, item) => item.outputSummary || item.errorMessage || '-',
  },
];

const ObservabilityPage: React.FC = () => {
  const [capabilityCode, setCapabilityCode] = useState<string | undefined>();
  const [providerId, setProviderId] = useState<number | undefined>();
  const overviewReq = useRequest(() => getAiOverview());
  const providersReq = useRequest(() => getAiProviders());
  const logsReq = useRequest(
    () =>
      getAiLogs({
        pageNum: 1,
        pageSize: 50,
        capabilityCode,
        providerId,
      }),
    {
      refreshDeps: [capabilityCode, providerId],
    },
  );

  const overview = overviewReq.data?.data;
  const providers = providersReq.data?.data || [];
  const logs = logsReq.data?.data?.list || [];

  const totalCost = useMemo(() => logs.reduce((sum, item) => sum + Number(item.costUsd || 0), 0), [logs]);
  const totalFailures = useMemo(() => logs.filter((item) => item.success !== 1).length, [logs]);

  return (
    <Space direction="vertical" size="large" style={{ width: '100%' }}>
      <Card style={{ borderRadius: 22 }}>
        <Title level={4} style={{ marginTop: 0 }}>
          平台監控與估算成本
        </Title>
        <Paragraph type="secondary" style={{ marginBottom: 0 }}>
          此頁集中展示供應商健康、同步狀態、回退風險、近期請求與成本估算，不再只給原始日誌表格。
        </Paragraph>
      </Card>

      <Row gutter={[16, 16]}>
        <Col xs={24} md={8}>
          <Card style={{ borderRadius: 22 }}>
            <Text type="secondary">24h 估算成本</Text>
            <Title level={3} style={{ marginBottom: 0 }}>
              US${Number(overview?.summary?.estimatedCost24h || 0).toFixed(4)}
            </Title>
          </Card>
        </Col>
        <Col xs={24} md={8}>
          <Card style={{ borderRadius: 22 }}>
            <Text type="secondary">同步過期供應商</Text>
            <Title level={3} style={{ marginBottom: 0 }}>
              {overview?.summary?.staleProviders || 0}
            </Title>
          </Card>
        </Col>
        <Col xs={24} md={8}>
          <Card style={{ borderRadius: 22 }}>
            <Text type="secondary">24h 失敗 / 回退</Text>
            <Title level={3} style={{ marginBottom: 0 }}>
              {overview?.summary?.failures24h || 0} / {overview?.summary?.fallbacks24h || 0}
            </Title>
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]}>
        <Col xs={24} xl={10}>
          <Card title="供應商健康與同步" style={{ borderRadius: 22 }}>
            <Space direction="vertical" size={12} style={{ width: '100%' }}>
              {(overview?.providers || []).map((provider) => (
                <Card key={provider.providerId} size="small" style={{ borderRadius: 16 }}>
                  <Space direction="vertical" size={4} style={{ width: '100%' }}>
                    <Space wrap>
                      <Text strong>{provider.displayName}</Text>
                      <Tag color={statusColor(provider.healthStatus)}>{provider.healthStatus || 'unknown'}</Tag>
                      <Tag color={statusColor(provider.lastInventorySyncStatus)}>
                        {provider.lastInventorySyncStatus || 'idle'}
                      </Tag>
                    </Space>
                    <Text type="secondary">
                      請求 {provider.requestCount24h || 0} / 失敗 {provider.failureCount24h || 0} / 平均耗時{' '}
                      {provider.averageLatencyMs || 0} ms
                    </Text>
                    <Text type="secondary">
                      庫存 {provider.inventoryRecordCount || 0} 筆，最近同步{' '}
                      {provider.lastInventorySyncedAt?.replace('T', ' ').slice(0, 19) || '尚未同步'}
                    </Text>
                  </Space>
                </Card>
              ))}
            </Space>
          </Card>
        </Col>
        <Col xs={24} xl={14}>
          <Card
            title="請求日誌與成本樣本"
            extra={
              <Space wrap>
                <Select
                  allowClear
                  style={{ width: 200 }}
                  placeholder="按能力篩選"
                  options={(overview?.capabilities || []).map((item) => ({
                    value: item.capabilityCode,
                    label: item.displayNameZht,
                  }))}
                  onChange={(value) => setCapabilityCode(value)}
                />
                <Select
                  allowClear
                  style={{ width: 220 }}
                  placeholder="按供應商篩選"
                  options={providers.map((provider) => ({
                    value: provider.id,
                    label: provider.displayName,
                  }))}
                  onChange={(value) => setProviderId(value)}
                />
              </Space>
            }
            style={{ borderRadius: 22 }}
          >
            <Space direction="vertical" size={12} style={{ width: '100%' }}>
              <Space split={<Text type="secondary">/</Text>}>
                <Text type="secondary">樣本請求 {logs.length}</Text>
                <Text type="secondary">樣本失敗 {totalFailures}</Text>
                <Text type="secondary">樣本估算成本 US${totalCost.toFixed(4)}</Text>
              </Space>
              <Table
                rowKey="id"
                columns={logColumns}
                dataSource={logs}
                pagination={false}
                scroll={{ x: 1000 }}
              />
            </Space>
          </Card>
        </Col>
      </Row>
    </Space>
  );
};

export default ObservabilityPage;
