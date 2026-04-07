import React, { useState } from 'react';
import { Modal, Tabs, Card, message, Space, Tag, Button, Descriptions, Divider } from 'antd';
import { 
  EnvironmentOutlined, 
  TrophyOutlined, 
  SyncOutlined, 
  HistoryOutlined,
  UserOutlined
} from '@ant-design/icons';
import type { TabsProps } from 'antd';
import MockLocationPanel from './MockLocationPanel';
import StampManager from './StampManager';
import LevelControl from './LevelControl';
import ProgressReset from './ProgressReset';
import OperationLog from './OperationLog';

interface TestAccount {
  id: number;
  userId: number;
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

interface TestToolModalProps {
  visible: boolean;
  onCancel: () => void;
  testAccount: TestAccount | null;
  onSuccess?: () => void;
}

const TestToolModal: React.FC<TestToolModalProps> = ({
  visible,
  onCancel,
  testAccount,
  onSuccess,
}) => {
  const [activeTab, setActiveTab] = useState('mock-location');

  if (!testAccount) return null;

  const handleOperationSuccess = (messageText?: string) => {
    if (messageText) {
      message.success(messageText);
    }
    onSuccess?.();
  };

  const items: TabsProps['items'] = [
    {
      key: 'mock-location',
      label: (
        <span>
          <EnvironmentOutlined /> 模拟定位
        </span>
      ),
      children: (
        <MockLocationPanel
          testAccountId={testAccount.id}
          mockLocation={testAccount.mockLocation}
          isMockEnabled={testAccount.isMockEnabled}
          onSuccess={handleOperationSuccess}
        />
      ),
    },
    {
      key: 'stamps',
      label: (
        <span>
          <TrophyOutlined /> 印章管理
        </span>
      ),
      children: (
        <StampManager
          testAccountId={testAccount.id}
          currentStampCount={testAccount.stampCount}
          onSuccess={handleOperationSuccess}
        />
      ),
    },
    {
      key: 'level',
      label: (
        <span>
          <UserOutlined /> 等级调整
        </span>
      ),
      children: (
        <LevelControl
          testAccountId={testAccount.id}
          currentLevel={testAccount.level}
          currentExperience={testAccount.experience}
          levelName={testAccount.levelName}
          onSuccess={handleOperationSuccess}
        />
      ),
    },
    {
      key: 'reset',
      label: (
        <span>
          <SyncOutlined /> 进度重置
        </span>
      ),
      children: (
        <ProgressReset
          testAccountId={testAccount.id}
          onSuccess={handleOperationSuccess}
        />
      ),
    },
    {
      key: 'logs',
      label: (
        <span>
          <HistoryOutlined /> 操作日志
        </span>
      ),
      children: (
        <OperationLog
          testAccountId={testAccount.id}
        />
      ),
    },
  ];

  return (
    <Modal
      title={
        <Space>
          <span>🔧 测试工具</span>
          <Tag color="blue">{testAccount.nickname}</Tag>
          <Tag color="purple">ID: {testAccount.userId}</Tag>
        </Space>
      }
      open={visible}
      onCancel={onCancel}
      width={900}
      footer={null}
      bodyStyle={{ padding: 0 }}
    >
      {/* 账号信息卡片 */}
      <Card size="small" style={{ margin: '16px 16px 0' }}>
        <Descriptions size="small" column={4}>
          <Descriptions.Item label="分组">
            <Tag color="blue">{testAccount.testGroup}</Tag>
          </Descriptions.Item>
          <Descriptions.Item label="印章">
            {testAccount.stampCount}/12
          </Descriptions.Item>
          <Descriptions.Item label="等级">
            Lv.{testAccount.level} {testAccount.levelName}
          </Descriptions.Item>
          <Descriptions.Item label="模拟定位">
            {testAccount.isMockEnabled ? (
              <Tag color="green">已启用</Tag>
            ) : (
              <Tag color="default">未启用</Tag>
            )}
          </Descriptions.Item>
        </Descriptions>
      </Card>

      {/* 工具标签页 */}
      <Tabs
        activeKey={activeTab}
        onChange={setActiveTab}
        items={items}
        style={{ padding: '0 16px 16px' }}
      />
    </Modal>
  );
};

export default TestToolModal;
