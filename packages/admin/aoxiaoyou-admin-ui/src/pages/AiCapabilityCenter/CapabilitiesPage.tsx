import React, { useMemo } from 'react';
import { Link } from 'react-router-dom';
import { useRequest } from 'ahooks';
import { Card, Col, Empty, Row, Space, Tag, Typography } from 'antd';
import { getAiCapabilities } from '../../services/api';
import { aiDomainLabels, getCapabilityCatalogItem, inferCapabilitySummary } from './catalog';

const { Paragraph, Text, Title } = Typography;

function statusColor(status?: string) {
  if (status === 'enabled') {
    return 'green';
  }
  if (status === 'planned' || status === 'draft') {
    return 'gold';
  }
  if (status === 'disabled' || status === 'archived') {
    return 'red';
  }
  return 'default';
}

const CapabilitiesPage: React.FC = () => {
  const capabilitiesReq = useRequest(() => getAiCapabilities());
  const capabilities = capabilitiesReq.data?.data || [];

  const grouped = useMemo(() => {
    const result: Record<string, typeof capabilities> = {
      admin_creative: [],
      mini_program: [],
    };
    capabilities.forEach((item) => {
      result[item.domainCode] = result[item.domainCode] || [];
      result[item.domainCode].push(item);
    });
    return result;
  }, [capabilities]);

  if (!capabilities.length) {
    return <Empty description="尚未配置 AI 能力。" />;
  }

  return (
    <Space direction="vertical" size="large" style={{ width: '100%' }}>
      <Card style={{ borderRadius: 22 }}>
        <Title level={4} style={{ marginTop: 0 }}>
          能力路由總表
        </Title>
        <Paragraph type="secondary" style={{ marginBottom: 0 }}>
          每個能力頁都承接自己的策略、供應商綁定、配額規則與提示詞模板，不再被塞進同一塊混雜的設定區。
        </Paragraph>
      </Card>

      {Object.entries(grouped).map(([domainCode, items]) => (
        <Card
          key={domainCode}
          title={aiDomainLabels[domainCode as keyof typeof aiDomainLabels] || domainCode}
          style={{ borderRadius: 22 }}
        >
          <Row gutter={[16, 16]}>
            {items.map((capability) => {
              const catalogItem = getCapabilityCatalogItem(capability.capabilityCode);
              return (
                <Col xs={24} md={12} xl={8} key={capability.id}>
                  <Card size="small" style={{ borderRadius: 18, height: '100%' }}>
                    <Space direction="vertical" size={10} style={{ width: '100%' }}>
                      <Space wrap>
                        <Text strong>{capability.displayNameZht}</Text>
                        <Tag color={statusColor(capability.status)}>{capability.status}</Tag>
                      </Space>
                      <Paragraph type="secondary" style={{ marginBottom: 0 }}>
                        {inferCapabilitySummary(capability.capabilityCode, capability.summaryZht)}
                      </Paragraph>
                      <Space wrap>
                        {catalogItem?.operatorFocus.map((focus) => (
                          <Tag key={focus}>{focus}</Tag>
                        ))}
                      </Space>
                      <Space split={<Text type="secondary">/</Text>}>
                        <Text type="secondary">策略 {capability.policyCount || 0}</Text>
                        <Text type="secondary">24h 請求 {capability.requestCount24h || 0}</Text>
                        <Text type="secondary">回退 {capability.fallbackCount24h || 0}</Text>
                      </Space>
                      <Link to={`/ai/capabilities/${capability.capabilityCode}`}>查看此能力的詳細配置</Link>
                    </Space>
                  </Card>
                </Col>
              );
            })}
          </Row>
        </Card>
      ))}
    </Space>
  );
};

export default CapabilitiesPage;
