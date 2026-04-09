import React, { useMemo, useState } from 'react';
import { PageContainer } from '@ant-design/pro-layout';
import {
  Alert,
  Button,
  Card,
  Col,
  Divider,
  Form,
  Input,
  Progress,
  Row,
  Select,
  Space,
  Statistic,
  Table,
  Tag,
  Typography,
} from 'antd';
import {
  AudioOutlined,
  CameraOutlined,
  CheckCircleOutlined,
  ClockCircleOutlined,
  EnvironmentOutlined,
  MessageOutlined,
  RobotOutlined,
  WarningOutlined,
} from '@ant-design/icons';
import { useRequest } from 'ahooks';
import {
  getAiLogs,
  getAiPolicies,
  getAiProviders,
  type AiLogItem,
  type AiPolicyItem,
  type AiProviderItem,
} from '../../services/api';

const { Text, Paragraph, Title } = Typography;
const { TextArea } = Input;

const scenarioMeta: Record<string, { label: string; icon: React.ReactNode; color: string; desc: string }> = {
  planning: {
    label: '行程推荐规划',
    icon: <EnvironmentOutlined />,
    color: 'blue',
    desc: '根据用户时间、偏好、预算与地理动线生成可执行路线。',
  },
  qa: {
    label: '旅行问答',
    icon: <MessageOutlined />,
    color: 'gold',
    desc: '用于景点、美食、交通、玩法与故事内容问答。',
  },
  vision: {
    label: '拍照识别定位',
    icon: <CameraOutlined />,
    color: 'purple',
    desc: '结合视觉锚点、楼层信息与参考物识别用户室内位置。',
  },
  dialogue: {
    label: 'NPC 语音对话',
    icon: <AudioOutlined />,
    color: 'green',
    desc: '用于景点 NPC 讲解词、互动对话和语音播报文案生成。',
  },
  navigation: {
    label: '导航辅助',
    icon: <RobotOutlined />,
    color: 'cyan',
    desc: '用于室内目标点导航与路线决策。',
  },
};

const defaultTestValues = {
  scenarioGroup: 'planning',
  userIntent: '帮我规划 2 天澳门亲子路线，包含轻松步行、葡式美食和夜景体验。',
  constraints: '预算中等；入住澳门半岛；每天 10:00 出发；希望包含 1 个室内点位。',
  expectedFormat: '输出上午/下午/晚间安排，并附推荐理由与备选方案。',
};

const AiCapabilityCenter: React.FC = () => {
  const [logFilters, setLogFilters] = useState<{ scenarioGroup?: string; success?: number; providerId?: number }>({});
  const [logPagination, setLogPagination] = useState({ current: 1, pageSize: 8 });
  const [testForm] = Form.useForm();

  const providers = useRequest(() => getAiProviders());
  const policies = useRequest(() => getAiPolicies());
  const logs = useRequest(
    () => getAiLogs({
      pageNum: logPagination.current,
      pageSize: logPagination.pageSize,
      ...logFilters,
    }),
    { refreshDeps: [logFilters, logPagination.current, logPagination.pageSize] },
  );

  const providerList = providers.data?.data || [];
  const policyList = policies.data?.data || [];
  const logPage = logs.data?.data;
  const logList = logPage?.list || [];

  const overview = useMemo(() => {
    const total = logPage?.total || 0;
    const successCount = logList.filter((item) => item.success === 1).length;
    const failedCount = logList.filter((item) => item.success !== 1).length;
    const avgLatency = logList.length
      ? Math.round(logList.reduce((sum, item) => sum + (item.latencyMs || 0), 0) / logList.length)
      : 0;
    const totalCost = logList.reduce((sum, item) => sum + Number(item.costUsd || 0), 0);

    return { total, successCount, failedCount, avgLatency, totalCost };
  }, [logList, logPage?.total]);

  const providerColumns = [
    { title: '供应商', dataIndex: 'displayName', width: 140 },
    { title: 'Provider Key', dataIndex: 'providerName', width: 150 },
    { title: '模型', dataIndex: 'modelName', width: 220 },
    {
      title: '能力',
      dataIndex: 'capabilities',
      render: (value: string) => {
        try {
          const parsed = JSON.parse(value || '{}');
          return (
            <Space wrap>
              {Object.entries(parsed)
                .filter(([, enabled]) => enabled)
                .map(([key]) => (
                  <Tag key={key} color="blue">
                    {key}
                  </Tag>
                ))}
            </Space>
          );
        } catch {
          return <Text type="secondary">{value}</Text>;
        }
      },
    },
    { title: '超时', dataIndex: 'requestTimeoutMs', width: 90, render: (v: number) => `${v} ms` },
    { title: '重试', dataIndex: 'maxRetries', width: 70 },
    { title: '日配额', dataIndex: 'quotaDaily', width: 90 },
    {
      title: '状态',
      dataIndex: 'status',
      width: 80,
      render: (v: number) => <Tag color={v === 1 ? 'green' : 'default'}>{v === 1 ? '启用' : '停用'}</Tag>,
    },
  ];

  const policyColumns = [
    { title: '策略名', dataIndex: 'policyName', width: 180 },
    { title: '场景编码', dataIndex: 'scenarioCode', width: 180 },
    {
      title: '场景分组',
      dataIndex: 'scenarioGroup',
      width: 120,
      render: (value: string) => {
        const meta = scenarioMeta[value] || { label: value, color: 'default', icon: null };
        return <Tag color={meta.color}>{meta.label}</Tag>;
      },
    },
    { title: '策略类型', dataIndex: 'policyType', width: 140 },
    { title: '供应商', dataIndex: 'providerName', width: 140 },
    { title: '模型覆盖', dataIndex: 'modelOverride', width: 180, render: (v: string) => v || '-' },
    { title: '多模态', dataIndex: 'multimodalEnabled', width: 80, render: (v: number) => (v ? <Tag color="purple">开</Tag> : '-') },
    { title: '语音', dataIndex: 'voiceEnabled', width: 80, render: (v: number) => (v ? <Tag color="green">开</Tag> : '-') },
    { title: '温度', dataIndex: 'temperature', width: 80 },
    { title: 'MaxTokens', dataIndex: 'maxTokens', width: 100 },
  ];

  const logColumns = [
    {
      title: '时间',
      dataIndex: 'createdAt',
      width: 168,
      render: (value: string) => value?.replace('T', ' ').slice(0, 19) || '-',
    },
    {
      title: '场景',
      dataIndex: 'scenarioGroup',
      width: 120,
      render: (value: string) => {
        const meta = scenarioMeta[value] || { label: value || '未知', color: 'default' };
        return <Tag color={meta.color}>{meta.label}</Tag>;
      },
    },
    { title: '策略', dataIndex: 'policyName', width: 160, render: (value: string) => value || '-' },
    { title: '供应商', dataIndex: 'providerName', width: 120, render: (value: string) => value || '-' },
    { title: '类型', dataIndex: 'requestType', width: 110, render: (value: string) => value || '-' },
    { title: '耗时', dataIndex: 'latencyMs', width: 90, render: (value: number) => (value ? `${value} ms` : '-') },
    { title: 'Tokens', dataIndex: 'tokensUsed', width: 90, render: (value: number) => value ?? '-' },
    {
      title: '成本',
      dataIndex: 'costUsd',
      width: 100,
      render: (value: number | string) => (value !== null && value !== undefined ? `$${Number(value).toFixed(4)}` : '-'),
    },
    {
      title: '状态',
      dataIndex: 'success',
      width: 90,
      render: (value: number) =>
        value === 1 ? <Tag color="success">成功</Tag> : <Tag color="error">失败</Tag>,
    },
    {
      title: '结果摘要',
      dataIndex: 'outputSummary',
      ellipsis: true,
      render: (value: string, record: AiLogItem) => value || record.errorMessage || '-',
    },
  ];

  const selectedScenario = Form.useWatch('scenarioGroup', testForm) || defaultTestValues.scenarioGroup;
  const matchingPolicies = policyList.filter((item) => item.scenarioGroup === selectedScenario);
  const recommendedPolicy = matchingPolicies[0];
  const recommendedProvider = providerList.find((item) => item.id === recommendedPolicy?.providerId);

  const handleLogFilterChange = (changed: Partial<typeof logFilters>) => {
    setLogPagination((prev) => ({ ...prev, current: 1 }));
    setLogFilters((prev) => ({ ...prev, ...changed }));
  };

  return (
    <PageContainer title="AI 能力中心" subTitle="统一管理行程规划、问答、多模态识别定位、NPC 语音对话与导航策略">
      <Space direction="vertical" size="large" style={{ width: '100%' }}>
        <Alert
          type="info"
          showIcon
          message="AI 中台已从单一导航扩展为多场景能力中心"
          description="当前页面已覆盖场景概览、供应商、策略矩阵、请求日志与基础测试台，便于运营、排障与后续真实接模调试。"
        />

        <Row gutter={[16, 16]}>
          {Object.entries(scenarioMeta).map(([key, meta]) => (
            <Col xs={24} sm={12} xl={6} key={key}>
              <Card>
                <Space align="start">
                  <div style={{ fontSize: 20, color: '#7c5cff' }}>{meta.icon}</div>
                  <div>
                    <Text strong>{meta.label}</Text>
                    <Paragraph type="secondary" style={{ marginBottom: 0, marginTop: 8 }}>
                      {meta.desc}
                    </Paragraph>
                  </div>
                </Space>
              </Card>
            </Col>
          ))}
        </Row>

        <Row gutter={[16, 16]}>
          <Col xs={24} md={12} xl={6}>
            <Card>
              <Statistic title="日志总量" value={overview.total} prefix={<RobotOutlined />} />
            </Card>
          </Col>
          <Col xs={24} md={12} xl={6}>
            <Card>
              <Statistic title="当前页成功数" value={overview.successCount} valueStyle={{ color: '#389e0d' }} prefix={<CheckCircleOutlined />} />
            </Card>
          </Col>
          <Col xs={24} md={12} xl={6}>
            <Card>
              <Statistic title="平均耗时" value={overview.avgLatency} suffix="ms" prefix={<ClockCircleOutlined />} />
            </Card>
          </Col>
          <Col xs={24} md={12} xl={6}>
            <Card>
              <Statistic title="当前页成本" value={overview.totalCost} precision={4} prefix="$" />
            </Card>
          </Col>
        </Row>

        <Card title="AI 供应商配置">
          <Table<AiProviderItem>
            rowKey="id"
            pagination={false}
            loading={providers.loading}
            dataSource={providerList}
            columns={providerColumns}
            scroll={{ x: 1100 }}
          />
        </Card>

        <Card title="场景策略矩阵">
          <Table<AiPolicyItem>
            rowKey="id"
            pagination={false}
            loading={policies.loading}
            dataSource={policyList}
            columns={policyColumns}
            scroll={{ x: 1280 }}
          />
        </Card>

        <Card
          title="AI 请求日志"
          extra={
            <Space wrap>
              <Select
                allowClear
                placeholder="按场景筛选"
                style={{ width: 180 }}
                options={Object.entries(scenarioMeta).map(([value, meta]) => ({ value, label: meta.label }))}
                onChange={(value) => handleLogFilterChange({ scenarioGroup: value })}
              />
              <Select
                allowClear
                placeholder="按状态筛选"
                style={{ width: 140 }}
                options={[
                  { value: 1, label: '成功' },
                  { value: 0, label: '失败' },
                ]}
                onChange={(value) => handleLogFilterChange({ success: value })}
              />
              <Select
                allowClear
                placeholder="按供应商筛选"
                style={{ width: 180 }}
                options={providerList.map((item) => ({ value: item.id, label: item.displayName }))}
                onChange={(value) => handleLogFilterChange({ providerId: value })}
              />
            </Space>
          }
        >
          <Table<AiLogItem>
            rowKey="id"
            loading={logs.loading}
            dataSource={logList}
            columns={logColumns}
            scroll={{ x: 1350 }}
            pagination={{
              current: logPagination.current,
              pageSize: logPagination.pageSize,
              total: logPage?.total || 0,
              showSizeChanger: true,
              onChange: (current, pageSize) => setLogPagination({ current, pageSize }),
            }}
          />
        </Card>

        <Card title="场景测试台（基础版）">
          <Row gutter={[24, 24]}>
            <Col xs={24} xl={14}>
              <Form form={testForm} layout="vertical" initialValues={defaultTestValues}>
                <Form.Item label="测试场景" name="scenarioGroup">
                  <Select options={Object.entries(scenarioMeta).map(([value, meta]) => ({ value, label: meta.label }))} />
                </Form.Item>
                <Form.Item label="用户意图" name="userIntent">
                  <TextArea rows={4} placeholder="输入一段真实用户请求" />
                </Form.Item>
                <Form.Item label="业务约束 / 上下文" name="constraints">
                  <TextArea rows={4} placeholder="输入预算、城市、路线限制、楼层信息等上下文" />
                </Form.Item>
                <Form.Item label="期望输出格式" name="expectedFormat">
                  <TextArea rows={3} placeholder="例如返回 JSON、分时段行程、讲解词草案等" />
                </Form.Item>
                <Space>
                  <Button type="primary" onClick={() => testForm.validateFields()}>
                    校验测试输入
                  </Button>
                  <Button onClick={() => testForm.resetFields()}>重置</Button>
                </Space>
              </Form>
            </Col>
            <Col xs={24} xl={10}>
              <Card type="inner" title="推荐执行策略">
                <Space direction="vertical" style={{ width: '100%' }} size="middle">
                  <div>
                    <Text type="secondary">当前场景</Text>
                    <div style={{ marginTop: 8 }}>
                      <Tag color={(scenarioMeta[selectedScenario] || { color: 'default' }).color}>
                        {scenarioMeta[selectedScenario]?.label || selectedScenario}
                      </Tag>
                    </div>
                  </div>
                  <div>
                    <Text type="secondary">推荐策略</Text>
                    <div style={{ marginTop: 8 }}>
                      <Text strong>{recommendedPolicy?.policyName || '暂未配置策略'}</Text>
                    </div>
                  </div>
                  <div>
                    <Text type="secondary">推荐供应商 / 模型</Text>
                    <div style={{ marginTop: 8 }}>
                      <Text>{recommendedProvider?.displayName || '-'}</Text>
                      <Divider type="vertical" />
                      <Text code>{recommendedPolicy?.modelOverride || recommendedProvider?.modelName || '-'}</Text>
                    </div>
                  </div>
                  <div>
                    <Text type="secondary">能力预估</Text>
                    <div style={{ marginTop: 8 }}>
                      <Space wrap>
                        {recommendedPolicy?.multimodalEnabled ? <Tag color="purple">多模态</Tag> : <Tag>纯文本</Tag>}
                        {recommendedPolicy?.voiceEnabled ? <Tag color="green">语音</Tag> : <Tag>无语音</Tag>}
                      </Space>
                    </div>
                  </div>
                  <div>
                    <Text type="secondary">上线准备度</Text>
                    <div style={{ marginTop: 8 }}>
                      <Progress percent={recommendedPolicy ? 65 : 20} status={recommendedPolicy ? 'active' : 'exception'} />
                    </div>
                  </div>
                  <Alert
                    type="warning"
                    showIcon
                    icon={<WarningOutlined />}
                    message="测试执行接口待接入"
                    description="当前测试台已支持输入校验、策略推荐和上线准备度预览；下一步可直接对接 `/api/admin/v1/ai/test` 执行真实调试。"
                  />
                </Space>
              </Card>
            </Col>
          </Row>
        </Card>
      </Space>
    </PageContainer>
  );
};

export default AiCapabilityCenter;
