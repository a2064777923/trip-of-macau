import React from 'react';
import { PageContainer } from '@ant-design/pro-layout';
import ProTable, { type ProColumns } from '@ant-design/pro-table';
import { Card, Col, Row, Statistic, Tag, Typography } from 'antd';
import { CompassOutlined, DashboardOutlined, DeploymentUnitOutlined, EnvironmentOutlined } from '@ant-design/icons';
import { getAdminMapTiles } from '../../services/api';
import type { AdminMapTileItem } from '../../types/admin';

const { Text } = Typography;

const MapTileManagement: React.FC = () => {
  const columns: ProColumns<AdminMapTileItem>[] = [
    { title: 'Map ID', dataIndex: 'mapId', width: 140 },
    { title: 'Style', dataIndex: 'style', width: 110, render: (_, record) => <Tag color="blue">{record.style || 'standard'}</Tag> },
    { title: 'Version', dataIndex: 'version', width: 100, render: (_, record) => <Tag>{record.version || 'v1'}</Tag> },
    { title: 'Zoom Range', width: 140, render: (_, record) => `${record.zoomMin ?? '-'} ~ ${record.zoomMax ?? '-'}` },
    { title: 'Default Zoom', dataIndex: 'defaultZoom', width: 110 },
    { title: 'Center', width: 180, render: (_, record) => record.centerLat != null && record.centerLng != null ? `${record.centerLat}, ${record.centerLng}` : '-' },
    { title: 'CDN Base', dataIndex: 'cdnBase', ellipsis: true, render: (_, record) => <Text ellipsis={{ tooltip: record.cdnBase }}>{record.cdnBase}</Text> },
    { title: 'Status', dataIndex: 'status', width: 100, render: (_, record) => <Tag color={record.status === 'active' || record.status === '1' ? 'green' : 'default'}>{record.status === '1' ? 'active' : (record.status || 'draft')}</Tag> },
    { title: 'Updated', dataIndex: 'updatedAt', width: 170 },
  ];

  return (
    <PageContainer title="Map Tile Management" subTitle="Tile metadata, zoom levels, map centering, and CDN resources">
      <Row gutter={[16, 16]} style={{ marginBottom: 16 }}>
        <Col xs={24} md={12} xl={6}>
          <Card><Statistic title="Tile Configs" valueStyle={{ color: '#7c5cff' }} prefix={<EnvironmentOutlined />} valueRender={() => <span>Live</span>} /></Card>
        </Col>
        <Col xs={24} md={12} xl={6}>
          <Card><Statistic title="Current Scope" prefix={<DeploymentUnitOutlined />} value="version / zoom / center" /></Card>
        </Col>
        <Col xs={24} md={12} xl={6}>
          <Card><Statistic title="Next Upgrade" prefix={<CompassOutlined />} value="preview / validation" /></Card>
        </Col>
        <Col xs={24} md={12} xl={6}>
          <Card><Statistic title="Planned" prefix={<DashboardOutlined />} value="tile canvas preview" /></Card>
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
        headerTitle="Map Tile Configurations"
        search={false}
        options={{ density: false, setting: false }}
      />
    </PageContainer>
  );
};

export default MapTileManagement;
