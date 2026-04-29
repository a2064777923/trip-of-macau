import React from 'react';
import { NavLink, Outlet, useLocation } from 'react-router-dom';
import { useRequest } from 'ahooks';
import { Card, Col, Row, Space, Statistic, Tag, Typography } from 'antd';
import { PageContainer } from '@ant-design/pro-layout';
import { getAiOverview, getAiPlatformSettings } from '../../services/api';
import { aiWorkspaceNavItems } from './catalog';

const { Paragraph, Text, Title } = Typography;

const navLinkStyle = (active: boolean): React.CSSProperties => ({
  display: 'block',
  padding: '14px 16px',
  minWidth: 180,
  borderRadius: 18,
  border: active ? '1px solid rgba(124, 92, 255, 0.28)' : '1px solid rgba(15, 23, 42, 0.08)',
  background: active ? 'rgba(124, 92, 255, 0.08)' : '#fff',
  color: '#111827',
  textDecoration: 'none',
});

const AiWorkspaceLayout: React.FC = () => {
  const location = useLocation();
  const overviewReq = useRequest(() => getAiOverview());
  const settingsReq = useRequest(() => getAiPlatformSettings());

  const summary = overviewReq.data?.data?.summary;
  const settings = settingsReq.data?.data;

  return (
    <PageContainer
      header={{
        title: 'AI 能力中心',
        subTitle: '供應商、模型、能力路由與創作工作台的統一控制台',
      }}
    >
      <Space direction="vertical" size="large" style={{ width: '100%' }}>
        <Card
          variant="borderless"
          style={{
            borderRadius: 24,
            background:
              'linear-gradient(135deg, rgba(124, 92, 255, 0.12) 0%, rgba(59, 130, 246, 0.10) 50%, rgba(15, 118, 110, 0.08) 100%)',
          }}
        >
          <Row gutter={[20, 20]} align="middle">
            <Col xs={24} xl={10}>
              <Space direction="vertical" size={12} style={{ width: '100%' }}>
                <Tag color="purple" style={{ width: 'fit-content', marginInlineEnd: 0 }}>
                  Phase 24 語音與平台工作區
                </Tag>
                <Title level={3} style={{ margin: 0 }}>
                  以真供應商編排能力，讓創作與旅客服務共用同一套治理底盤
                </Title>
                <Paragraph style={{ marginBottom: 0 }}>
                  這裡不是單一 CRUD 頁，而是 AI 供應商接入、模型端點庫、能力路由、成本監控與創作工作台的完整操作面。
                </Paragraph>
                <Space wrap>
                  <Tag color="blue">供應商測試</Tag>
                  <Tag color="gold">同步新鮮度</Tag>
                  <Tag color="green">成本與回退</Tag>
                  <Tag color="magenta">工作台回填資產</Tag>
                </Space>
              </Space>
            </Col>
            <Col xs={24} xl={14}>
              <Row gutter={[16, 16]}>
                <Col xs={12} md={6}>
                  <Card size="small" style={{ borderRadius: 18 }}>
                    <Statistic title="能力總數" value={summary?.totalCapabilities || 0} />
                  </Card>
                </Col>
                <Col xs={12} md={6}>
                  <Card size="small" style={{ borderRadius: 18 }}>
                    <Statistic title="啟用供應商" value={summary?.enabledProviders || 0} />
                  </Card>
                </Col>
                <Col xs={12} md={6}>
                  <Card size="small" style={{ borderRadius: 18 }}>
                    <Statistic title="庫存紀錄" value={summary?.inventoryRecords || 0} />
                  </Card>
                </Col>
                <Col xs={12} md={6}>
                  <Card size="small" style={{ borderRadius: 18 }}>
                    <Statistic title="24h 請求" value={summary?.requests24h || 0} />
                  </Card>
                </Col>
              </Row>
              <Row gutter={[16, 16]} style={{ marginTop: 8 }}>
                <Col xs={24} md={12}>
                  <Card size="small" style={{ borderRadius: 18 }}>
                    <Text strong>治理門檻</Text>
                    <br />
                    <Text type="secondary">
                      庫存新鮮度 {settings?.inventoryFreshnessHours || 0} 小時，供應商失敗率預警{' '}
                      {settings?.providerFailureRateWarning != null
                        ? `${Math.round(settings.providerFailureRateWarning * 100)}%`
                        : '-'}
                    </Text>
                  </Card>
                </Col>
                <Col xs={24} md={12}>
                  <Card size="small" style={{ borderRadius: 18 }}>
                    <Text strong>歷史與成本</Text>
                    <br />
                    <Text type="secondary">
                      近窗 {settings?.recentWindowHours || 0} 小時，日成本警戒 US$
                      {settings?.dailyCostAlertUsd || 0}
                    </Text>
                  </Card>
                </Col>
              </Row>
            </Col>
          </Row>
        </Card>

        <Card size="small" title="工作區導覽" style={{ borderRadius: 22 }}>
          <Space wrap size={[12, 12]} style={{ width: '100%' }}>
            {aiWorkspaceNavItems.map((item) => {
              const active =
                location.pathname === item.path || location.pathname.startsWith(`${item.path}/`);

              return (
                <NavLink key={item.key} to={item.path} style={navLinkStyle(active)}>
                  <Text strong style={{ display: 'block', marginBottom: 4 }}>
                    {item.label}
                  </Text>
                  <Text type="secondary" style={{ fontSize: 12 }}>
                    {item.hint}
                  </Text>
                </NavLink>
              );
            })}
          </Space>
        </Card>

        <Outlet />
      </Space>
    </PageContainer>
  );
};

export default AiWorkspaceLayout;
