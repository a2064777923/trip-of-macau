import React, { useEffect, useState } from 'react';
import { PageContainer } from '@ant-design/pro-layout';
import { Card, Col, Descriptions, Empty, List, Progress, Row, Space, Statistic, Tag, Typography } from 'antd';
import {
  ApiOutlined,
  CloudServerOutlined,
  DatabaseOutlined,
  EnvironmentOutlined,
  NotificationOutlined,
  RiseOutlined,
  TeamOutlined,
  TrophyOutlined,
} from '@ant-design/icons';
import { getDashboardStats } from '../../services/api';
import type { DashboardStats } from '../../types/admin';

const { Text } = Typography;

const defaultStats: DashboardStats = {
  totalUsers: 0,
  totalStamps: 0,
  poiCount: 0,
  weeklyGrowth: 0,
  activeUsers: 0,
  storyLines: 0,
  activities: 0,
  rewards: 0,
  testAccounts: 0,
  recentActivities: [],
  systemStatus: {
    database: false,
    api: false,
    cloudRun: false,
  },
  contentSummary: {
    publishedCities: 0,
    publishedStoryLines: 0,
    publishedStoryChapters: 0,
    publishedPois: 0,
    publishedStamps: 0,
    publishedRewards: 0,
    publishedTips: 0,
    publishedNotifications: 0,
    publishedRuntimeSettings: 0,
  },
  integrationHealth: {
    database: {
      healthy: false,
      status: 'UNKNOWN',
      detail: 'No probe result yet.',
    },
    publicApi: {
      healthy: false,
      status: 'UNKNOWN',
      detail: 'No probe result yet.',
    },
    cos: {
      healthy: false,
      status: 'UNKNOWN',
      detail: 'No probe result yet.',
    },
    seedMigration: {
      seedKey: 'phase6-mock-dataset-migration',
      status: 'unknown',
      notes: 'No seed result yet.',
    },
  },
};

const healthTagColor = (status?: string) => {
  switch (status) {
    case 'UP':
      return 'success';
    case 'WARN':
    case 'DEGRADED':
      return 'warning';
    case 'missing':
    case 'error':
    case 'DOWN':
      return 'error';
    default:
      return 'default';
  }
};

const Dashboard: React.FC = () => {
  const [stats, setStats] = useState<DashboardStats>(defaultStats);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const loadStats = async () => {
      setLoading(true);
      try {
        const response = await getDashboardStats();
        if (response.success && response.data) {
          setStats({
            ...defaultStats,
            ...response.data,
            contentSummary: {
              ...defaultStats.contentSummary,
              ...response.data.contentSummary,
            },
            integrationHealth: {
              ...defaultStats.integrationHealth,
              ...response.data.integrationHealth,
            },
          });
        }
      } finally {
        setLoading(false);
      }
    };
    loadStats();
  }, []);

  const publishedRatio = Math.min(
    100,
    Math.round(((stats.contentSummary?.publishedPois || 0) / Math.max(stats.poiCount || 1, 1)) * 100),
  );

  return (
    <PageContainer
      title="Operations Dashboard"
      subTitle="Live content, integration health, and seed status for the Trip of Macau stack"
    >
      <Row gutter={[16, 16]}>
        <Col xs={24} sm={12} xl={6}>
          <Card loading={loading}>
            <Statistic title="Total travelers" value={stats.totalUsers} prefix={<TeamOutlined />} />
          </Card>
        </Col>
        <Col xs={24} sm={12} xl={6}>
          <Card loading={loading}>
            <Statistic title="Stored stamps" value={stats.totalStamps} prefix={<TrophyOutlined />} />
          </Card>
        </Col>
        <Col xs={24} sm={12} xl={6}>
          <Card loading={loading}>
            <Statistic title="Published POIs" value={stats.contentSummary?.publishedPois || 0} prefix={<EnvironmentOutlined />} />
          </Card>
        </Col>
        <Col xs={24} sm={12} xl={6}>
          <Card loading={loading}>
            <Statistic title="7-day user growth" value={stats.weeklyGrowth} suffix="%" prefix={<RiseOutlined />} />
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]} style={{ marginTop: 8 }}>
        <Col xs={24} lg={14}>
          <Card title="Live content overview" loading={loading}>
            <Row gutter={[16, 16]}>
              <Col xs={24} md={8}>
                <Card variant="borderless" style={{ background: '#f6f8ff' }}>
                  <Statistic title="Published cities" value={stats.contentSummary?.publishedCities || 0} />
                  <Text type="secondary">{stats.contentSummary?.publishedStoryLines || 0} storylines live</Text>
                </Card>
              </Col>
              <Col xs={24} md={8}>
                <Card variant="borderless" style={{ background: '#fffaf0' }}>
                  <Statistic title="Story chapters" value={stats.contentSummary?.publishedStoryChapters || 0} />
                  <Text type="secondary">{stats.contentSummary?.publishedStamps || 0} stamps configured</Text>
                </Card>
              </Col>
              <Col xs={24} md={8}>
                <Card variant="borderless" style={{ background: '#f6ffed' }}>
                  <Statistic title="Tips + notices" value={(stats.contentSummary?.publishedTips || 0) + (stats.contentSummary?.publishedNotifications || 0)} prefix={<NotificationOutlined />} />
                  <Text type="secondary">{stats.contentSummary?.publishedRuntimeSettings || 0} runtime settings live</Text>
                </Card>
              </Col>
            </Row>
            <Card variant="borderless" style={{ background: '#fafafa', marginTop: 16 }}>
              <Statistic title="Traveler activity" value={stats.activeUsers} suffix="users" />
              <Progress percent={publishedRatio} showInfo={false} />
              <Text type="secondary">
                Rewards live: {stats.contentSummary?.publishedRewards || 0} · Notifications live: {stats.contentSummary?.publishedNotifications || 0}
              </Text>
            </Card>
          </Card>
        </Col>
        <Col xs={24} lg={10}>
          <Card title="Integration health" loading={loading}>
            <Descriptions column={1} size="small">
              <Descriptions.Item label={<Space><DatabaseOutlined /><span>Database</span></Space>}>
                <Space direction="vertical" size={4}>
                  <Tag color={healthTagColor(stats.integrationHealth?.database?.status)}>
                    {stats.integrationHealth?.database?.status || 'UNKNOWN'}
                  </Tag>
                  <Text type="secondary">{stats.integrationHealth?.database?.detail || '-'}</Text>
                </Space>
              </Descriptions.Item>
              <Descriptions.Item label={<Space><ApiOutlined /><span>Public API</span></Space>}>
                <Space direction="vertical" size={4}>
                  <Space>
                    <Tag color={healthTagColor(stats.integrationHealth?.publicApi?.status)}>
                      {stats.integrationHealth?.publicApi?.status || 'UNKNOWN'}
                    </Tag>
                    {typeof stats.integrationHealth?.publicApi?.latencyMs === 'number' ? (
                      <Text type="secondary">{stats.integrationHealth?.publicApi?.latencyMs} ms</Text>
                    ) : null}
                  </Space>
                  <Text type="secondary">{stats.integrationHealth?.publicApi?.detail || '-'}</Text>
                </Space>
              </Descriptions.Item>
              <Descriptions.Item label={<Space><CloudServerOutlined /><span>COS</span></Space>}>
                <Space direction="vertical" size={4}>
                  <Tag color={healthTagColor(stats.integrationHealth?.cos?.status)}>
                    {stats.integrationHealth?.cos?.status || 'UNKNOWN'}
                  </Tag>
                  <Text type="secondary">{stats.integrationHealth?.cos?.detail || '-'}</Text>
                </Space>
              </Descriptions.Item>
              <Descriptions.Item label="Phase 6 seed">
                <Space direction="vertical" size={4}>
                  <Space>
                    <Tag color={healthTagColor(stats.integrationHealth?.seedMigration?.status)}>
                      {stats.integrationHealth?.seedMigration?.status || 'unknown'}
                    </Tag>
                    <Text type="secondary">{stats.integrationHealth?.seedMigration?.seedKey}</Text>
                  </Space>
                  <Text type="secondary">
                    {stats.integrationHealth?.seedMigration?.executedAt
                      ? `Executed at ${stats.integrationHealth?.seedMigration?.executedAt}`
                      : 'Seed execution time not available.'}
                  </Text>
                  <Text type="secondary">{stats.integrationHealth?.seedMigration?.notes || '-'}</Text>
                </Space>
              </Descriptions.Item>
            </Descriptions>
          </Card>
        </Col>
      </Row>

      <Card title="Recent admin activity" style={{ marginTop: 16 }} loading={loading}>
        <List
          locale={{ emptyText: <Empty description="No recent admin activity" /> }}
          dataSource={stats.recentActivities}
          renderItem={(item) => (
            <List.Item>
              <List.Item.Meta
                title={<Space><Text strong>{item.user}</Text><Tag color="blue">{item.type}</Tag></Space>}
                description={item.action}
              />
              <Text type="secondary">{item.time}</Text>
            </List.Item>
          )}
        />
      </Card>
    </PageContainer>
  );
};

export default Dashboard;
