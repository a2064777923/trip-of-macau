import React, { useMemo } from 'react';
import { PageContainer } from '@ant-design/pro-layout';
import ProTable from '@ant-design/pro-table';
import { Card, Col, Row, Space, Statistic, Tag, Typography } from 'antd';
import { EnvironmentOutlined, CompassOutlined, DashboardOutlined, DeploymentUnitOutlined } from '@ant-design/icons';
import { getAdminMapTiles } from '../../services/api';
import type { AdminMapTileItem } from '../../types/admin';

const { Text } = Typography;

const MapTileManagement: React.FC = () => {
  const columns = useMemo(() => [
    { title: '地图标识', dataIndex: 'mapId', width: 140 },
    { title: '样式', dataIndex: 'style', width: 110, render: (value: string) => <Tag color="blue">{value || 'standard'}</Tag> },
    { title: '版本', dataIndex: 'version', width: 100, render: (value: string) => <Tag>{value || 'v1'}</Tag> },
    {
      title: '缩放范围',
      width: 140,
      render: (_: unknown, record: AdminMapTileItem) => `${record.zoomMin ?? '-'} ~ ${record.zoomMax ?? '-'}`,
    },
    { title: '默认缩放', dataIndex: 'defaultZoom', width: 90 },
    {
      title: '中心点',
      width: 180,
      render: (_: unknown, record: AdminMapTileItem) =>
        record.centerLat != null && record.centerLng != null ? `${record.centerLat}, ${record.centerLng}` : '-',
    },
    {
      title: '资源地址',
      dataIndex: 'cdnBase',
      ellipsis: true,
      render: (value: string) => <Text ellipsis={{ tooltip: value }}>{value}</Text>,
    },
    {
      title: '状态',
      dataIndex: 'status',
      width: 100,
      render: (value: string) => <Tag color={value === '1' || value === 'active' ? 'green' : 'default'}>{value === '1' || value === 'active' ? '启用' : value}</Tag>,
    },
    { title: '更新时间', dataIndex: 'updatedAt', valueType: 'dateTime', width: 170 },
  ], []);

  return (
    <PageContainer
      title="城市瓦片地图"
      subTitle="管理多城市瓦片资源、缩放层级、中心点与版本信息"
    >
      <Row gutter={[16, 16]} style={{ marginBottom: 16 }}>
        <Col xs={24} md={12} xl={6}>
          <Card><Statistic title="瓦片配置数" valueStyle={{ color: '#7c5cff' }} prefix={<EnvironmentOutlined />} valueRender={() => <span>实时加载</span>} /></Card>
        </Col>
        <Col xs={24} md={12} xl={6}>
          <Card><Statistic title="当前能力" prefix={<DeploymentUnitOutlined />} value="版本 / 缩放 / 中心点" /></Card>
        </Col>
        <Col xs={24} md={12} xl={6}>
          <Card><Statistic title="下一步" prefix={<CompassOutlined />} value="控制点 / 校准" /></Card>
        </Col>
        <Col xs={24} md={12} xl={6}>
          <Card><Statistic title="规划中" prefix={<DashboardOutlined />} value="地图画布预览" /></Card>
        </Col>
      </Row>

      <ProTable<AdminMapTileItem>
        rowKey="id"
        columns={columns}
        request={async (params) => {
          const response = await getAdminMapTiles({ pageNum: params.current, pageSize: params.pageSize });
          return {
            data: response.data?.list || [],
            success: response.success,
            total: response.data?.total || 0,
          };
        }}
        headerTitle="地图瓦片配置"
        search={false}
        options={{ density: false, setting: false }}
      />
    </PageContainer>
  );
};

export default MapTileManagement;
