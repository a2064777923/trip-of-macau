import React, { useMemo } from 'react';
import { Link } from 'react-router-dom';
import { useRequest } from 'ahooks';
import { Alert, Card, Col, Empty, Row, Space, Table, Tag, Typography } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { getAiOverview, type AiCapabilityItem, type AiLogItem, type AiOverviewProviderHealth } from '../../services/api';
import { aiDomainLabels, getCapabilityCatalogItem, inferCapabilitySummary } from './catalog';

const { Paragraph, Text, Title } = Typography;

function statusColor(status?: string | number) {
  if (status === 1 || status === 'enabled' || status === 'healthy' || status === 'completed') {
    return 'green';
  }
  if (status === 'warning' || status === 'planned' || status === 'submitted' || status === 'pending') {
    return 'gold';
  }
  if (status === 0 || status === 'disabled' || status === 'failed' || status === 'error') {
    return 'red';
  }
  return 'default';
}

function statusText(status?: string | number) {
  if (status === 1) {
    return '成功';
  }
  if (status === 0) {
    return '失敗';
  }
  const normalized = String(status || '').toLowerCase();
  const labels: Record<string, string> = {
    enabled: '已啟用',
    disabled: '已停用',
    planned: '規劃中',
    draft: '草稿',
    healthy: '正常',
    warning: '警告',
    unknown: '未知',
    completed: '已完成',
    submitted: '已提交',
    pending: '等待中',
    idle: '閒置',
    failed: '失敗',
    error: '錯誤',
    success: '成功',
    succeeded: '成功',
  };
  return labels[normalized] || String(status || '未知');
}

const providerColumns: ColumnsType<AiOverviewProviderHealth> = [
  {
    title: '供應商',
    dataIndex: 'displayName',
    render: (_, provider) => (
      <Space direction="vertical" size={0}>
        <Text strong>{provider.displayName}</Text>
        <Text type="secondary" style={{ fontSize: 12 }}>
          {provider.providerName}
        </Text>
      </Space>
    ),
  },
  {
    title: '健康',
    dataIndex: 'healthStatus',
    width: 120,
    render: (value) => <Tag color={statusColor(value)}>{statusText(value)}</Tag>,
  },
  {
    title: '同步',
    dataIndex: 'lastInventorySyncStatus',
    width: 140,
    render: (value) => <Tag color={statusColor(value)}>{statusText(value || 'idle')}</Tag>,
  },
  {
    title: '庫存數',
    dataIndex: 'inventoryRecordCount',
    width: 100,
    render: (value) => value || 0,
  },
  {
    title: '24h 請求',
    dataIndex: 'requestCount24h',
    width: 100,
    render: (value) => value || 0,
  },
];

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
    width: 150,
    render: (value) => value || '-',
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

const OverviewPage: React.FC = () => {
  const overviewReq = useRequest(() => getAiOverview());
  const overview = overviewReq.data?.data;

  const groupedCapabilities = useMemo(() => {
    const groups: Record<string, AiCapabilityItem[]> = {
      admin_creative: [],
      mini_program: [],
    };
    (overview?.capabilities || []).forEach((item) => {
      groups[item.domainCode] = groups[item.domainCode] || [];
      groups[item.domainCode].push(item);
    });
    return groups;
  }, [overview?.capabilities]);

  return (
    <Space direction="vertical" size="large" style={{ width: '100%' }}>
      {(overview?.alerts || []).map((alert) => (
        <Alert
          key={`${alert.level}-${alert.title}`}
          type={alert.level === 'error' ? 'error' : 'warning'}
          showIcon
          message={alert.title}
          description={alert.message}
        />
      ))}

      <Row gutter={[16, 16]}>
        <Col xs={24} xl={15}>
          <Card title="能力地圖" style={{ borderRadius: 22 }}>
            <Space direction="vertical" size="large" style={{ width: '100%' }}>
              {Object.entries(groupedCapabilities).map(([domainCode, items]) => (
                <div key={domainCode}>
                  <Space align="center" size="small" style={{ marginBottom: 12 }}>
                    <Title level={4} style={{ margin: 0 }}>
                      {aiDomainLabels[domainCode as keyof typeof aiDomainLabels] || domainCode}
                    </Title>
                    <Tag color={domainCode === 'admin_creative' ? 'magenta' : 'blue'}>
                      {items.length} 項能力
                    </Tag>
                  </Space>
                  <Row gutter={[12, 12]}>
                    {items.map((item) => {
                      const catalogItem = getCapabilityCatalogItem(item.capabilityCode);
                      return (
                        <Col xs={24} md={12} key={item.id}>
                          <Card size="small" style={{ borderRadius: 18 }}>
                            <Space direction="vertical" size={8} style={{ width: '100%' }}>
                              <Space wrap>
                                <Text strong>{item.displayNameZht}</Text>
                                <Tag color={statusColor(item.status)}>{statusText(item.status)}</Tag>
                              </Space>
                              <Paragraph type="secondary" style={{ marginBottom: 0 }}>
                                {inferCapabilitySummary(item.capabilityCode, item.summaryZht)}
                              </Paragraph>
                              <Space wrap>
                                {catalogItem?.operatorFocus?.map((focus) => (
                                  <Tag key={focus}>{focus}</Tag>
                                ))}
                              </Space>
                              <Space split={<Text type="secondary">/</Text>}>
                                <Text type="secondary">策略 {item.policyCount || 0}</Text>
                                <Text type="secondary">24h 請求 {item.requestCount24h || 0}</Text>
                                <Text type="secondary">失敗 {item.failedCount24h || 0}</Text>
                              </Space>
                              <Link to={`/ai/capabilities/${item.capabilityCode}`}>查看能力配置</Link>
                            </Space>
                          </Card>
                        </Col>
                      );
                    })}
                  </Row>
                </div>
              ))}
            </Space>
          </Card>
        </Col>

        <Col xs={24} xl={9}>
          <Space direction="vertical" size="large" style={{ width: '100%' }}>
            <Card title="工作區入口" style={{ borderRadius: 22 }}>
              <Space direction="vertical" size={12} style={{ width: '100%' }}>
                <Card size="small" style={{ borderRadius: 18, background: '#faf5ff' }}>
                  <Text strong>能力路由</Text>
                  <Paragraph type="secondary" style={{ marginBottom: 8 }}>
                    統一配置行程規劃、旅行問答、拍照定位、NPC 語音與導航輔助等能力的主模型、後備模型與切換策略。
                  </Paragraph>
                  <Link to="/ai/capabilities">前往能力路由</Link>
                </Card>
                <Card size="small" style={{ borderRadius: 18, background: '#eff6ff' }}>
                  <Text strong>創作工作台</Text>
                  <Paragraph type="secondary" style={{ marginBottom: 8 }}>
                    從內容表單或平台入口啟動生成工作台，保留候選歷史並回填正式資源。
                  </Paragraph>
                  <Link to="/ai/creative-studio">前往創作工作台</Link>
                </Card>
                <Card size="small" style={{ borderRadius: 18, background: '#fff7ed' }}>
                  <Text strong>音色與聲音工坊</Text>
                  <Paragraph type="secondary" style={{ marginBottom: 8 }}>
                    依語音模型同步官方音色、試聽不同語言輸出，並建立可回用的自定義復刻音色。
                  </Paragraph>
                  <Link to="/ai/voices">前往音色與聲音工坊</Link>
                </Card>
                <Card size="small" style={{ borderRadius: 18, background: '#f0fdf4' }}>
                  <Text strong>監控與成本</Text>
                  <Paragraph type="secondary" style={{ marginBottom: 8 }}>
                    同步新鮮度、供應商健康、使用量、回退與估算成本集中在同一頁追蹤。
                  </Paragraph>
                  <Link to="/ai/observability">查看監控與成本</Link>
                </Card>
              </Space>
            </Card>

            <Card title="供應商健康" style={{ borderRadius: 22 }}>
              <Table
                rowKey="providerId"
                columns={providerColumns}
                dataSource={overview?.providers || []}
                pagination={false}
                locale={{ emptyText: '尚未接入供應商。' }}
              />
            </Card>
          </Space>
        </Col>
      </Row>

      <Row gutter={[16, 16]}>
        <Col xs={24} xl={14}>
          <Card title="最近請求與異常線索" style={{ borderRadius: 22 }}>
            <Table
              rowKey="id"
              columns={logColumns}
              dataSource={overview?.recentLogs || []}
              pagination={false}
              locale={{ emptyText: '目前沒有可用的請求紀錄。' }}
            />
          </Card>
        </Col>
        <Col xs={24} xl={10}>
          <Card title="最近生成作業" style={{ borderRadius: 22 }}>
            {(overview?.recentJobs || []).length ? (
              <Space direction="vertical" size={12} style={{ width: '100%' }}>
                {overview?.recentJobs.map((job) => (
                  <Card key={job.id} size="small" style={{ borderRadius: 18 }}>
                    <Space direction="vertical" size={6} style={{ width: '100%' }}>
                      <Space wrap>
                        <Text strong>{job.promptTitle || '未命名生成作業'}</Text>
                        <Tag color={statusColor(job.jobStatus)}>{statusText(job.jobStatus)}</Tag>
                      </Space>
                      <Text type="secondary">
                        {job.capabilityNameZht || job.capabilityCode || '-'} / {job.providerName || '自動路由'}
                      </Text>
                      <Text type="secondary">{job.resultSummary || job.errorMessage || '尚未產出摘要。'}</Text>
                    </Space>
                  </Card>
                ))}
              </Space>
            ) : (
              <Empty description="最近沒有生成作業，可從創作工作台發起。" />
            )}
          </Card>
        </Col>
      </Row>
    </Space>
  );
};

export default OverviewPage;
