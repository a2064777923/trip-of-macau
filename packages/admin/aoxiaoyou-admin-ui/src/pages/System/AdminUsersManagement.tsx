import React, { useMemo, useState } from 'react';
import { PageContainer } from '@ant-design/pro-layout';
import ProTable from '@ant-design/pro-table';
import { Avatar, Button, Drawer, Descriptions, Form, Input, Space, Tag, Typography } from 'antd';
import { UserOutlined, SafetyCertificateOutlined, EyeOutlined } from '@ant-design/icons';
import type { ProColumns } from '@ant-design/pro-table';
import { getAdminUsersRbac, type AdminUserItem } from '../../services/api';

const { Text } = Typography;

const AdminUsersManagement: React.FC = () => {
  const [detailOpen, setDetailOpen] = useState(false);
  const [current, setCurrent] = useState<AdminUserItem | null>(null);

  const columns = useMemo<ProColumns<AdminUserItem>[]>(() => [
    {
      title: '管理员',
      dataIndex: 'username',
      render: (_, record) => (
        <Space>
          <Avatar icon={<UserOutlined />} style={{ backgroundColor: '#7c5cff' }} />
          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
              <Text strong>{record.displayName || record.username}</Text>
              {record.isSuperuser ? <Tag color="gold">超管</Tag> : null}
            </div>
            <Text type="secondary" style={{ fontSize: 12 }}>{record.username}</Text>
          </div>
        </Space>
      ),
    },
    { title: '邮箱', dataIndex: 'email', hideInSearch: true, ellipsis: true },
    { title: '部门/域', dataIndex: 'department', hideInSearch: true },
    {
      title: '状态', dataIndex: 'status', width: 100,
      render: (value: string) => <Tag color={value === 'active' || value === '1' ? 'green' : 'default'}>{value === 'active' || value === '1' ? '启用' : '禁用'}</Tag>,
    },
    { title: '最后登录', dataIndex: 'lastLoginAt', valueType: 'dateTime', hideInSearch: true, width: 180 },
    {
      title: '操作', key: 'option', valueType: 'option', width: 120,
      render: (_, record) => [
        <Button key="detail" type="link" icon={<EyeOutlined />} onClick={() => { setCurrent(record); setDetailOpen(true); }}>查看</Button>,
      ],
    },
  ], []);

  return (
    <PageContainer
      title="管理员账号"
      subTitle="查看当前后台管理员、状态与最近登录行为"
      extra={[
        <Button key="new" type="primary" icon={<SafetyCertificateOutlined />} disabled>
          新建账号（下一批）
        </Button>,
      ]}
    >
      <ProTable<AdminUserItem>
        rowKey="id"
        columns={columns}
        request={async (params) => {
          const response = await getAdminUsersRbac({
            pageNum: params.current,
            pageSize: params.pageSize,
            keyword: params.username as string,
          });
          return {
            data: response.data?.list || [],
            success: response.success,
            total: response.data?.total || 0,
          };
        }}
        headerTitle="管理员列表"
        search={{ labelWidth: 'auto' }}
      />

      <Drawer title="管理员详情" open={detailOpen} width={520} onClose={() => setDetailOpen(false)}>
        {current && (
          <Descriptions column={1} bordered>
            <Descriptions.Item label="用户名">{current.username}</Descriptions.Item>
            <Descriptions.Item label="显示名称">{current.displayName || '-'}</Descriptions.Item>
            <Descriptions.Item label="邮箱">{current.email || '-'}</Descriptions.Item>
            <Descriptions.Item label="部门/域">{current.department || '-'}</Descriptions.Item>
            <Descriptions.Item label="状态">{current.status}</Descriptions.Item>
            <Descriptions.Item label="是否超级管理员">{current.isSuperuser ? '是' : '否'}</Descriptions.Item>
            <Descriptions.Item label="最后登录">{current.lastLoginAt || '-'}</Descriptions.Item>
          </Descriptions>
        )}
      </Drawer>
    </PageContainer>
  );
};

export default AdminUsersManagement;
