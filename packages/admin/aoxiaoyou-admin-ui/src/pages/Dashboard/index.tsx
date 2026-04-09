import React, { useEffect, useState } from 'react';
import { PageContainer } from '@ant-design/pro-layout';
import { Card, Col, Empty, List, Progress, Row, Space, Statistic, Tag, Typography } from 'antd';
import {
  EnvironmentOutlined,
  NotificationOutlined,
  RiseOutlined,
  SafetyCertificateOutlined,
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
    database: true,
    api: true,
    cloudRun: true,
  },
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
          setStats(response.data);
        }
      } finally {
        setLoading(false);
      }
    };
    loadStats();
  }, []);

  return (
    <PageContainer
      title="仪表盘"
      subTitle="围绕澳门故事线、运营活动与测试调度的后台总览"
    >
      <Row gutter={[16, 16]}>
        <Col xs={24} sm={12} xl={6}>
          <Card loading={loading}>
            <Statistic title="总用户数" value={stats.totalUsers} prefix={<TeamOutlined />} />
          </Card>
        </Col>
        <Col xs={24} sm={12} xl={6}>
          <Card loading={loading}>
            <Statistic title="总印章数" value={stats.totalStamps} prefix={<TrophyOutlined />} />
          </Card>
        </Col>
        <Col xs={24} sm={12} xl={6}>
          <Card loading={loading}>
            <Statistic title="POI 数量" value={stats.poiCount} prefix={<EnvironmentOutlined />} />
          </Card>
        </Col>
        <Col xs={24} sm={12} xl={6}>
          <Card loading={loading}>
            <Statistic title="近 7 日增长" value={stats.weeklyGrowth} suffix="%" prefix={<RiseOutlined />} />
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]} style={{ marginTop: 8 }}>
        <Col xs={24} lg={16}>
          <Card title="运营概览" loading={loading}>
            <Row gutter={[16, 16]}>
              <Col xs={24} md={8}>
                <Card variant="borderless" style={{ background: '#f6f8ff' }}>
                  <Statistic title="活跃用户" value={stats.activeUsers} suffix="人" />
                  <Progress percent={Math.min(100, Math.round((stats.activeUsers / Math.max(stats.totalUsers || 1, 1)) * 100))} showInfo={false} />
                </Card>
              </Col>
              <Col xs={24} md={8}>
                <Card variant="borderless" style={{ background: '#fffaf0' }}>
                  <Statistic title="进行中活动" value={stats.activities} prefix={<NotificationOutlined />} />
                  <Text type="secondary">面向 Q2 MVP 的运营节奏与投放</Text>
                </Card>
              </Col>
              <Col xs={24} md={8}>
                <Card variant="borderless" style={{ background: '#f6ffed' }}>
                  <Statistic title="奖励配置" value={stats.rewards} prefix={<SafetyCertificateOutlined />} />
                  <Text type="secondary">含印章、优惠权益与兑换库存</Text>
                </Card>
              </Col>
            </Row>
          </Card>
        </Col>
        <Col xs={24} lg={8}>
          <Card title="系统健康" loading={loading}>
            <Space direction="vertical" style={{ width: '100%' }} size="middle">
              <Space style={{ justifyContent: 'space-between', width: '100%' }}>
                <span>数据库</span>
                <Tag color={stats.systemStatus.database ? 'success' : 'error'}>{stats.systemStatus.database ? '正常' : '异常'}</Tag>
              </Space>
              <Space style={{ justifyContent: 'space-between', width: '100%' }}>
                <span>API 服务</span>
                <Tag color={stats.systemStatus.api ? 'success' : 'error'}>{stats.systemStatus.api ? '正常' : '异常'}</Tag>
              </Space>
              <Space style={{ justifyContent: 'space-between', width: '100%' }}>
                <span>CloudBase 托管</span>
                <Tag color={stats.systemStatus.cloudRun ? 'success' : 'error'}>{stats.systemStatus.cloudRun ? '在线' : '离线'}</Tag>
              </Space>
              <Space style={{ justifyContent: 'space-between', width: '100%' }}>
                <span>测试账号</span>
                <Text strong>{stats.testAccounts} 个</Text>
              </Space>
              <Space style={{ justifyContent: 'space-between', width: '100%' }}>
                <span>故事线</span>
                <Text strong>{stats.storyLines} 条</Text>
              </Space>
            </Space>
          </Card>
        </Col>
      </Row>

      <Card title="最近后台活动" style={{ marginTop: 16 }} loading={loading}>
        <List
          locale={{ emptyText: <Empty description="暂无后台操作记录" /> }}
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
