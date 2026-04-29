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
      detail: '尚未取得檢查結果。',
    },
    publicApi: {
      healthy: false,
      status: 'UNKNOWN',
      detail: '尚未取得檢查結果。',
    },
    cos: {
      healthy: false,
      status: 'UNKNOWN',
      detail: '尚未取得檢查結果。',
    },
    seedMigration: {
      seedKey: 'phase6-mock-dataset-migration',
      status: 'unknown',
      notes: '尚未取得種子資料執行結果。',
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

const statusText = (status?: string) => {
  const labels: Record<string, string> = {
    UP: '正常',
    DOWN: '異常',
    WARN: '警告',
    DEGRADED: '降級',
    UNKNOWN: '未知',
    completed: '已完成',
    missing: '缺失',
    error: '錯誤',
    unknown: '未知',
  };
  return labels[status || ''] || status || '未知';
};

const localizeDetail = (detail?: string) => {
  if (!detail) {
    return '-';
  }
  if (detail === 'MySQL query probe succeeded.') {
    return 'MySQL 查詢探針通過。';
  }
  if (detail === 'MySQL query probe failed.') {
    return 'MySQL 查詢探針失敗。';
  }
  if (detail.startsWith('MySQL probe failed:')) {
    return detail.replace('MySQL probe failed:', 'MySQL 探針失敗：');
  }
  if (detail.startsWith('Public API health responded from')) {
    return detail
      .replace('Public API health responded from', '公開 API 健康檢查已回應：')
      .replace(', curated discover cards:', '，發現頁精選卡片配置：');
  }
  if (detail.startsWith('Public API probe failed:')) {
    return detail.replace('Public API probe failed:', '公開 API 探針失敗：');
  }
  if (/^COS bucket .+ in .+ is configured\.$/.test(detail)) {
    return detail.replace(/^COS bucket (.+) in (.+) is configured\.$/, 'COS bucket $1 已配置，區域：$2。');
  }
  if (detail === 'COS is enabled but runtime configuration is incomplete.') {
    return 'COS 已啟用，但當前運行配置不完整。';
  }
  if (detail === 'COS upload is disabled in the current admin runtime.') {
    return '當前後台運行環境未啟用 COS 上傳。';
  }
  if (detail === 'Upserts cities, storylines, chapters, POIs, stamps, rewards, tips, notifications, and runtime settings for the final live cutover.') {
    return '已寫入城市、故事線、章節、POI、印章、獎勵、貼士、通知與運行設定，供正式切換使用。';
  }
  return detail;
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
      title="營運儀表盤"
      subTitle="集中查看線上內容、服務整合健康度與種子資料狀態"
    >
      <Row gutter={[16, 16]}>
        <Col xs={24} sm={12} xl={6}>
          <Card loading={loading}>
            <Statistic title="旅客總數" value={stats.totalUsers} prefix={<TeamOutlined />} />
          </Card>
        </Col>
        <Col xs={24} sm={12} xl={6}>
          <Card loading={loading}>
            <Statistic title="已儲存印章" value={stats.totalStamps} prefix={<TrophyOutlined />} />
          </Card>
        </Col>
        <Col xs={24} sm={12} xl={6}>
          <Card loading={loading}>
            <Statistic title="已發布 POI" value={stats.contentSummary?.publishedPois || 0} prefix={<EnvironmentOutlined />} />
          </Card>
        </Col>
        <Col xs={24} sm={12} xl={6}>
          <Card loading={loading}>
            <Statistic title="近 7 日用戶增長" value={stats.weeklyGrowth} suffix="%" prefix={<RiseOutlined />} />
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]} style={{ marginTop: 8 }}>
        <Col xs={24} lg={14}>
          <Card title="線上內容總覽" loading={loading}>
            <Row gutter={[16, 16]}>
              <Col xs={24} md={8}>
                <Card variant="borderless" style={{ background: '#f6f8ff' }}>
                  <Statistic title="已發布城市" value={stats.contentSummary?.publishedCities || 0} />
                  <Text type="secondary">{stats.contentSummary?.publishedStoryLines || 0} 條故事線上線中</Text>
                </Card>
              </Col>
              <Col xs={24} md={8}>
                <Card variant="borderless" style={{ background: '#fffaf0' }}>
                  <Statistic title="故事章節" value={stats.contentSummary?.publishedStoryChapters || 0} />
                  <Text type="secondary">{stats.contentSummary?.publishedStamps || 0} 個印章已配置</Text>
                </Card>
              </Col>
              <Col xs={24} md={8}>
                <Card variant="borderless" style={{ background: '#f6ffed' }}>
                  <Statistic title="貼士與通知" value={(stats.contentSummary?.publishedTips || 0) + (stats.contentSummary?.publishedNotifications || 0)} prefix={<NotificationOutlined />} />
                  <Text type="secondary">{stats.contentSummary?.publishedRuntimeSettings || 0} 項運行設定生效中</Text>
                </Card>
              </Col>
            </Row>
            <Card variant="borderless" style={{ background: '#fafafa', marginTop: 16 }}>
              <Statistic title="旅客活躍度" value={stats.activeUsers} suffix="人" />
              <Progress percent={publishedRatio} showInfo={false} />
              <Text type="secondary">
                已上線獎勵：{stats.contentSummary?.publishedRewards || 0} · 已上線通知：{stats.contentSummary?.publishedNotifications || 0}
              </Text>
            </Card>
          </Card>
        </Col>
        <Col xs={24} lg={10}>
          <Card title="整合健康度" loading={loading}>
            <Descriptions column={1} size="small">
              <Descriptions.Item label={<Space><DatabaseOutlined /><span>資料庫</span></Space>}>
                <Space direction="vertical" size={4}>
                  <Tag color={healthTagColor(stats.integrationHealth?.database?.status)}>
                    {statusText(stats.integrationHealth?.database?.status)}
                  </Tag>
                  <Text type="secondary">{localizeDetail(stats.integrationHealth?.database?.detail)}</Text>
                </Space>
              </Descriptions.Item>
              <Descriptions.Item label={<Space><ApiOutlined /><span>公開 API</span></Space>}>
                <Space direction="vertical" size={4}>
                  <Space>
                    <Tag color={healthTagColor(stats.integrationHealth?.publicApi?.status)}>
                      {statusText(stats.integrationHealth?.publicApi?.status)}
                    </Tag>
                    {typeof stats.integrationHealth?.publicApi?.latencyMs === 'number' ? (
                      <Text type="secondary">{stats.integrationHealth?.publicApi?.latencyMs} ms</Text>
                    ) : null}
                  </Space>
                  <Text type="secondary">{localizeDetail(stats.integrationHealth?.publicApi?.detail)}</Text>
                </Space>
              </Descriptions.Item>
              <Descriptions.Item label={<Space><CloudServerOutlined /><span>COS</span></Space>}>
                <Space direction="vertical" size={4}>
                  <Tag color={healthTagColor(stats.integrationHealth?.cos?.status)}>
                    {statusText(stats.integrationHealth?.cos?.status)}
                  </Tag>
                  <Text type="secondary">{localizeDetail(stats.integrationHealth?.cos?.detail)}</Text>
                </Space>
              </Descriptions.Item>
              <Descriptions.Item label="Phase 6 種子資料">
                <Space direction="vertical" size={4}>
                  <Space>
                    <Tag color={healthTagColor(stats.integrationHealth?.seedMigration?.status)}>
                      {statusText(stats.integrationHealth?.seedMigration?.status)}
                    </Tag>
                    <Text type="secondary">{stats.integrationHealth?.seedMigration?.seedKey}</Text>
                  </Space>
                  <Text type="secondary">
                    {stats.integrationHealth?.seedMigration?.executedAt
                      ? `執行時間：${stats.integrationHealth?.seedMigration?.executedAt}`
                      : '未取得種子資料執行時間。'}
                  </Text>
                  <Text type="secondary">{localizeDetail(stats.integrationHealth?.seedMigration?.notes)}</Text>
                </Space>
              </Descriptions.Item>
            </Descriptions>
          </Card>
        </Col>
      </Row>

      <Card title="近期管理操作" style={{ marginTop: 16 }} loading={loading}>
        <List
          locale={{ emptyText: <Empty description="暫無近期管理操作" /> }}
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
