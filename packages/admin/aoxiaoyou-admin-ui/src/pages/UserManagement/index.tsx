import React, { useMemo, useState } from 'react';
import { PageContainer } from '@ant-design/pro-layout';
import ProTable from '@ant-design/pro-table';
import { Avatar, Button, Drawer, Descriptions, Space, Switch, Tag, Typography, message } from 'antd';
import { EyeOutlined } from '@ant-design/icons';
import type { ActionType, ProColumns } from '@ant-design/pro-table';
import { getAdminUserDetail, getAdminUsers, updateAdminUserTestFlag } from '../../services/api';
import type { AdminUserDetail, AdminUserListItem } from '../../types/admin';

const { Text } = Typography;

const UserManagement: React.FC = () => {
  const [detailOpen, setDetailOpen] = useState(false);
  const [detail, setDetail] = useState<AdminUserDetail | null>(null);
  const [detailLoading, setDetailLoading] = useState(false);

  const columns = useMemo<ProColumns<AdminUserListItem>[]>(() => [
    {
      title: '用户',
      dataIndex: 'nickname',
      render: (_, record) => (
        <Space>
          <Avatar src={record.avatarUrl}>{record.nickname?.[0]}</Avatar>
          <div>
            <div>{record.nickname || '未命名用户'}</div>
            <Text type="secondary" style={{ fontSize: 12 }}>
              {record.openId}
            </Text>
          </div>
        </Space>
      ),
    },
    {
      title: '测试账号',
      dataIndex: 'isTestAccount',
      valueType: 'select',
      valueEnum: {
        true: { text: '是' },
        false: { text: '否' },
      },
      render: (_, record) => (
        <Switch
          checked={record.isTestAccount}
          checkedChildren="是"
          unCheckedChildren="否"
          onChange={async (checked) => {
            try {
              await updateAdminUserTestFlag(record.userId, { isTestAccount: checked, reason: '后台手动调整' });
              message.success('测试账号标记已更新');
            } catch (error) {
              message.error('更新失败');
            }
          }}
        />
      ),
    },
    {
      title: '等级',
      dataIndex: 'level',
      hideInSearch: true,
      render: (_, record) => <Tag color="purple">Lv.{record.level}</Tag>,
    },
    {
      title: '印章数',
      dataIndex: 'totalStamps',
      hideInSearch: true,
    },
    {
      title: '状态',
      dataIndex: 'accountStatus',
      hideInSearch: true,
      render: (value) => <Tag color={value === 'active' ? 'success' : 'default'}>{value}</Tag>,
    },
    {
      title: '创建时间',
      dataIndex: 'createdAt',
      valueType: 'dateTime',
      hideInSearch: true,
    },
    {
      title: '操作',
      key: 'action',
      valueType: 'option',
      render: (_, record) => [
        <Button
          key="detail"
          type="link"
          icon={<EyeOutlined />}
          onClick={async () => {
            setDetailOpen(true);
            setDetailLoading(true);
            try {
              const response = await getAdminUserDetail(record.userId);
              if (response.success) {
                setDetail(response.data);
              }
            } finally {
              setDetailLoading(false);
            }
          }}
        >
          查看详情
        </Button>,
      ],
    },
  ], []);

  return (
    <PageContainer title="用户管理" subTitle="查看小程序用户、等级进度与测试账号标记">
      <ProTable<AdminUserListItem>
        rowKey="userId"
        columns={columns}
        request={async (params) => {
          const response = await getAdminUsers({
            pageNum: params.current,
            pageSize: params.pageSize,
            keyword: params.nickname as string,
            isTestAccount: typeof params.isTestAccount === 'boolean' ? params.isTestAccount : undefined,
          });
          return {
            data: response.data?.list || [],
            success: response.success,
            total: response.data?.total || 0,
          };
        }}
        search={{ labelWidth: 'auto' }}
        pagination={{ pageSize: 10 }}
        headerTitle="用户列表"
      />

      <Drawer
        open={detailOpen}
        onClose={() => setDetailOpen(false)}
        width={560}
        title="用户详情"
        loading={detailLoading}
      >
        {detail && (
          <Space direction="vertical" style={{ width: '100%' }} size="large">
            <Descriptions title="基本信息" column={1} bordered>
              <Descriptions.Item label="昵称">{detail.basicInfo.nickname}</Descriptions.Item>
              <Descriptions.Item label="OpenID">{detail.basicInfo.openId}</Descriptions.Item>
              <Descriptions.Item label="等级">Lv.{detail.basicInfo.level}</Descriptions.Item>
              <Descriptions.Item label="印章数">{detail.basicInfo.totalStamps}</Descriptions.Item>
              <Descriptions.Item label="测试账号">{detail.basicInfo.isTestAccount ? '是' : '否'}</Descriptions.Item>
            </Descriptions>
            <Descriptions title="进度概览" column={1} bordered>
              <Descriptions.Item label="当前经验">{detail.progress.currentExp}</Descriptions.Item>
              <Descriptions.Item label="下一级目标">{detail.progress.nextLevelExp}</Descriptions.Item>
              <Descriptions.Item label="已解锁故事线">{detail.progress.unlockedStorylines}</Descriptions.Item>
              <Descriptions.Item label="已完成故事线">{detail.progress.completedStorylines}</Descriptions.Item>
            </Descriptions>
          </Space>
        )}
      </Drawer>
    </PageContainer>
  );
};

export default UserManagement;
