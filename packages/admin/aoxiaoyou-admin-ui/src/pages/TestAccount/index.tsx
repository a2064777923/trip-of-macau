import React, { useState, useRef } from 'react';
import { PageContainer } from '@ant-design/pro-layout';
import ProTable from '@ant-design/pro-table';
import { Button, Modal, message, Space, Tag, Avatar, Switch, Popover } from 'antd';
import { ToolOutlined, PlusOutlined, EnvironmentOutlined, TrophyOutlined } from '@ant-design/icons';
import type { ProColumns, ActionType } from '@ant-design/pro-table';
import TestToolModal from './components/TestToolModal';

// 测试账号数据类型
interface TestAccount {
  id: number;
  userId: number;
  openId: string;
  nickname: string;
  avatar: string;
  remark: string;
  testGroup: string;
  mockLocation?: {
    latitude: number;
    longitude: number;
    address: string;
  };
  isMockEnabled: boolean;
  stampCount: number;
  level: number;
  levelName: string;
  experience: number;
  createTime: string;
  lastOperationTime: string;
}

// Mock 数据
const mockTestAccounts: TestAccount[] = [
  {
    id: 10001,
    userId: 10001,
    openId: 'ou_xxxxxxxx1',
    nickname: '测试员01',
    avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=1',
    remark: '主要测试人员',
    testGroup: 'group_a',
    mockLocation: {
      latitude: 22.1973,
      longitude: 113.5408,
      address: '澳门半岛大三巴牌坊',
    },
    isMockEnabled: true,
    stampCount: 8,
    level: 4,
    levelName: '澳门达人',
    experience: 320,
    createTime: '2026-04-05T10:00:00+08:00',
    lastOperationTime: '2026-04-07T10:30:00+08:00',
  },
  {
    id: 10002,
    userId: 10002,
    openId: 'ou_xxxxxxxx2',
    nickname: '测试员02',
    avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=2',
    remark: '测试组成员',
    testGroup: 'default',
    mockLocation: undefined,
    isMockEnabled: false,
    stampCount: 3,
    level: 2,
    levelName: '澳门新手',
    experience: 45,
    createTime: '2026-04-06T09:00:00+08:00',
    lastOperationTime: '2026-04-06T15:20:00+08:00',
  },
  {
    id: 10003,
    userId: 10003,
    openId: 'ou_xxxxxxxx3',
    nickname: '测试员03',
    avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=3',
    remark: '自动化测试账号',
    testGroup: 'auto_test',
    mockLocation: {
      latitude: 22.1500,
      longitude: 113.5600,
      address: '氹仔威尼斯人',
    },
    isMockEnabled: true,
    stampCount: 12,
    level: 5,
    levelName: '澳门通',
    experience: 480,
    createTime: '2026-04-04T14:00:00+08:00',
    lastOperationTime: '2026-04-07T08:45:00+08:00',
  },
];

const TestAccountList: React.FC = () => {
  const actionRef = useRef<ActionType>();
  const [toolModalVisible, setToolModalVisible] = useState(false);
  const [currentAccount, setCurrentAccount] = useState<TestAccount | null>(null);

  // 打开测试工具面板
  const handleOpenToolPanel = (record: TestAccount) => {
    setCurrentAccount(record);
    setToolModalVisible(true);
  };

  // 切换模拟定位状态
  const handleToggleMock = async (record: TestAccount, enabled: boolean) => {
    message.success(`${enabled ? '启用' : '禁用'}模拟定位成功`);
    actionRef.current?.reload();
  };

  const columns: ProColumns<TestAccount>[] = [
    {
      title: '用户信息',
      dataIndex: 'nickname',
      key: 'nickname',
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
      key: 'testGroup',
      render: (group) => <Tag color="blue">{group}</Tag>,
    },
    {
      title: '模拟位置',
      dataIndex: 'mockLocation',
      key: 'mockLocation',
      render: (location, record) => (
        <Space direction="vertical" size={0}>
          {location ? (
            <Popover
              content={
                <div>
                  <p>纬度: {location.latitude}</p>
                  <p>经度: {location.longitude}</p>
                </div>
              }
              title="坐标信息"
            >
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
      ),
    },
    {
      title: '游戏进度',
      key: 'progress',
      render: (_, record) => (
        <Space direction="vertical" size={0}>
          <span><TrophyOutlined /> 印章: {record.stampCount}/12</span>
          <span>⭐ Lv.{record.level} {record.levelName}</span>
          <span style={{ fontSize: 12, color: '#999' }}>经验: {record.experience} XP</span>
        </Space>
      ),
    },
    {
      title: '操作时间',
      dataIndex: 'lastOperationTime',
      key: 'lastOperationTime',
      render: (time) => new Date(time).toLocaleString('zh-CN'),
    },
    {
      title: '操作',
      key: 'action',
      valueType: 'option',
      render: (_, record) => [
        <Button
          key="tool"
          type="primary"
          icon={<ToolOutlined />}
          onClick={() => handleOpenToolPanel(record)}
        >
          测试工具
        </Button>,
      ],
    },
  ];

  return (
    <PageContainer
      title="测试账号管理"
      subTitle="管理测试账号，使用测试工具调整数据"
      extra={[
        <Button key="add" type="primary" icon={<PlusOutlined />}>
          添加测试账号
        </Button>,
      ]}
    >
      <ProTable<TestAccount>
        actionRef={actionRef}
        columns={columns}
        dataSource={mockTestAccounts}
        rowKey="id"
        search={{
          labelWidth: 'auto',
        }}
        pagination={{
          pageSize: 10,
        }}
        dateFormatter="string"
        headerTitle="测试账号列表"
        toolBarRender={() => [
          <Button key="export">导出数据</Button>,
        ]}
      />

      {/* 测试工具面板弹窗 */}
      <TestToolModal
        visible={toolModalVisible}
        onCancel={() => setToolModalVisible(false)}
        testAccount={currentAccount}
        onSuccess={() => {
          message.success('操作成功');
          actionRef.current?.reload();
        }}
      />
    </PageContainer>
  );
};

export default TestAccountList;
