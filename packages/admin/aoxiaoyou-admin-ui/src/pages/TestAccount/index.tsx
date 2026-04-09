import React, { useRef, useState } from 'react';
import { PageContainer } from '@ant-design/pro-layout';
import ProTable from '@ant-design/pro-table';
import { Avatar, Button, message, Popover, Space, Switch, Tag } from 'antd';
import { EnvironmentOutlined, PlusOutlined, ToolOutlined, TrophyOutlined } from '@ant-design/icons';
import type { ActionType, ProColumns } from '@ant-design/pro-table';
import TestToolModal from './components/TestToolModal';
import { getAdminTestAccounts, updateTestAccountMock } from '../../services/api';
import type { AdminTestAccountListItem } from '../../types/admin';

const TestAccountList: React.FC = () => {
  const actionRef = useRef<ActionType>();
  const [toolModalVisible, setToolModalVisible] = useState(false);
  const [currentAccount, setCurrentAccount] = useState<AdminTestAccountListItem | null>(null);

  const handleOpenToolPanel = (record: AdminTestAccountListItem) => {
    setCurrentAccount(record);
    setToolModalVisible(true);
  };

  const handleToggleMock = async (record: AdminTestAccountListItem, enabled: boolean) => {
    try {
      await updateTestAccountMock(record.id, {
        enabled,
        latitude: record.mockLocation?.latitude,
        longitude: record.mockLocation?.longitude,
        address: record.mockLocation?.address,
      });
      message.success(`${enabled ? '启用' : '禁用'}模拟定位成功`);
      actionRef.current?.reload();
    } catch {
      message.error('操作失败，请重试');
    }
  };

  const columns: ProColumns<AdminTestAccountListItem>[] = [
    {
      title: '用户信息',
      dataIndex: 'nickname',
      render: (_, record) => (
        <Space>
          <Avatar src={record.avatar} size="small" />
          <div>
            <div>{record.nickname}</div>
            <div style={{ fontSize: 12, color: '#999' }}>ID: {record.userId}</div>
          </div>
        </Space>
      ),
    },
    {
      title: '分组',
      dataIndex: 'testGroup',
      render: (group) => <Tag color="blue">{group}</Tag>,
    },
    {
      title: '模拟位置',
      dataIndex: 'mockLocation',
      hideInSearch: true,
      render: (_, record) => {
        const location = record.mockLocation;
        return (
          <Space direction="vertical" size={0}>
            {location?.address ? (
              <Popover content={<div><p>纬度: {location.latitude}</p><p>经度: {location.longitude}</p></div>} title="坐标信息">
                <span><EnvironmentOutlined /> {location.address}</span>
              </Popover>
            ) : (
              <span style={{ color: '#999' }}>未设置</span>
            )}
            <Switch
              checked={record.isMockEnabled}
              onChange={(checked) => handleToggleMock(record, checked)}
              checkedChildren="启用"
              unCheckedChildren="禁用"
              size="small"
              style={{ marginTop: 4 }}
            />
          </Space>
        );
      },
    },
    {
      title: '游戏进度',
      key: 'progress',
      hideInSearch: true,
      render: (_, record) => (
        <Space direction="vertical" size={0}>
          <span><TrophyOutlined /> 印章: {record.stampCount}/12</span>
          <span>⭐ Lv.{record.level} {record.levelName}</span>
          <span style={{ fontSize: 12, color: '#999' }}>经验: {record.experience} XP</span>
        </Space>
      ),
    },
    {
      title: '创建时间',
      dataIndex: 'createTime',
      valueType: 'dateTime',
      hideInSearch: true,
    },
    {
      title: '最后操作',
      dataIndex: 'lastOperationTime',
      valueType: 'dateTime',
      hideInSearch: true,
    },
    {
      title: '操作',
      key: 'action',
      valueType: 'option',
      render: (_, record) => [
        <Button key="tool" type="primary" icon={<ToolOutlined />} onClick={() => handleOpenToolPanel(record)}>
          测试工具
        </Button>,
      ],
    },
  ];

  return (
    <PageContainer
      title="测试账号管理"
      subTitle="管理测试账号，使用测试工具调整数据"
      extra={[<Button key="add" type="primary" icon={<PlusOutlined />}>添加测试账号</Button>]}
    >
      <ProTable<AdminTestAccountListItem>
        actionRef={actionRef}
        columns={columns}
        request={async (params) => {
          const response = await getAdminTestAccounts({
            pageNum: params.current,
            pageSize: params.pageSize,
            testGroup: params.testGroup as string,
          });
          return {
            data: response.data?.list || [],
            total: response.data?.total || 0,
            success: response.success,
          };
        }}
        rowKey="id"
        search={{ labelWidth: 'auto' }}
        pagination={{ pageSize: 10 }}
        dateFormatter="string"
        headerTitle="测试账号列表"
        toolBarRender={() => [<Button key="export">导出数据</Button>]}
      />

      <TestToolModal
        visible={toolModalVisible}
        onCancel={() => setToolModalVisible(false)}
        testAccount={currentAccount as any}
        onSuccess={() => {
          message.success('操作成功');
          actionRef.current?.reload();
        }}
      />
    </PageContainer>
  );
};

export default TestAccountList;

