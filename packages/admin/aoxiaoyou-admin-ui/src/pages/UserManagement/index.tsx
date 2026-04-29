import React, { useMemo } from 'react';
import { PageContainer } from '@ant-design/pro-layout';
import ProTable from '@ant-design/pro-table';
import type { ProColumns } from '@ant-design/pro-table';
import { Avatar, Button, Space, Switch, Tag, Typography, message } from 'antd';
import { ArrowRightOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { getAdminUsers, updateAdminUserTestFlag } from '../../services/api';
import type { AdminUserListItem } from '../../types/admin';

const { Text } = Typography;

const UserManagement: React.FC = () => {
  const navigate = useNavigate();

  const columns = useMemo<ProColumns<AdminUserListItem>[]>(
    () => [
      {
        title: '旅客',
        dataIndex: 'nickname',
        render: (_, record) => (
          <Space>
            <Avatar src={record.avatarUrl}>{record.nickname?.[0]}</Avatar>
            <div>
              <div>{record.nickname || '未命名旅客'}</div>
              <Text type="secondary" style={{ fontSize: 12 }}>
                {record.openId}
              </Text>
            </div>
          </Space>
        ),
      },
      {
        title: '測試帳號',
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
                const response = await updateAdminUserTestFlag(record.userId, {
                  isTestAccount: checked,
                  reason: '營運後台手動調整',
                });
                if (response.success) {
                  message.success('測試帳號標記已更新');
                } else {
                  message.error(response.message || '更新失敗');
                }
              } catch (error) {
                message.error('更新失敗');
              }
            }}
          />
        ),
      },
      {
        title: '等級',
        dataIndex: 'level',
        hideInSearch: true,
        render: (_, record) => <Tag color="purple">Lv.{record.level}</Tag>,
      },
      {
        title: '印章數',
        dataIndex: 'totalStamps',
        hideInSearch: true,
      },
      {
        title: '帳號狀態',
        dataIndex: 'accountStatus',
        hideInSearch: true,
        render: (value) => <Tag color={value === 'active' ? 'success' : 'default'}>{value || 'unknown'}</Tag>,
      },
      {
        title: '建立時間',
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
            key="open-progress-workbench"
            type="primary"
            icon={<ArrowRightOutlined />}
            onClick={() => {
              navigate(`/users/progress/${record.userId}`);
            }}
          >
            開啟旅客進度工作台
          </Button>,
        ],
      },
    ],
    [navigate],
  );

  return (
    <PageContainer
      title="旅客管理"
      subTitle="查看旅客名單、搜尋關鍵資料，並進入完整的進度工作台"
    >
      <ProTable<AdminUserListItem>
        rowKey="userId"
        columns={columns}
        request={async (params) => {
          const response = await getAdminUsers({
            pageNum: params.current,
            pageSize: params.pageSize,
            keyword: params.nickname as string,
            isTestAccount:
              typeof params.isTestAccount === 'boolean' ? params.isTestAccount : undefined,
          });
          return {
            data: response.data?.list || [],
            success: response.success,
            total: response.data?.total || 0,
          };
        }}
        search={{ labelWidth: 'auto' }}
        pagination={{ pageSize: 10 }}
        headerTitle="旅客名單"
      />
    </PageContainer>
  );
};

export default UserManagement;
