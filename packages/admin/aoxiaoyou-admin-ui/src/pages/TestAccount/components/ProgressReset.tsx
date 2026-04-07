import React, { useState } from 'react';
import { Card, Button, Radio, Space, Alert, Typography, Descriptions, Tag, Modal, message, Divider } from 'antd';
import { ExclamationCircleOutlined, DeleteOutlined, SyncOutlined, WarningOutlined } from '@ant-design/icons';

const { Title, Text, Paragraph } = Typography;
const { confirm } = Modal;

interface ProgressResetProps {
  testAccountId: number;
  onSuccess?: (message?: string) => void;
}

const ProgressReset: React.FC<ProgressResetProps> = ({
  testAccountId,
  onSuccess,
}) => {
  const [resetType, setResetType] = useState<string>('all');
  const [loading, setLoading] = useState(false);

  // 重置选项配置
  const resetOptions = [
    {
      value: 'all',
      label: '重置所有进度',
      description: '清空所有印章、等级、经验值、成就、故事线进度',
      color: 'red',
      icon: <DeleteOutlined />,
      detail: '账号将回到初始状态，所有数据清零',
    },
    {
      value: 'stamps',
      label: '仅清空印章',
      description: '清空所有收集的印章，保留等级和经验值',
      color: 'orange',
      icon: <SyncOutlined />,
      detail: '等级、经验值、成就等其他数据保留',
    },
    {
      value: 'level',
      label: '仅重置等级',
      description: '重置等级为1级，经验值为0，保留印章',
      color: 'blue',
      icon: <SyncOutlined />,
      detail: '已收集的印章全部保留，等级和经验值重置',
    },
    {
      value: 'story',
      label: '仅重置故事线',
      description: '重置所有故事线进度，保留印章和等级',
      color: 'purple',
      icon: <SyncOutlined />,
      detail: '故事线进度清零，可以重新体验故事',
    },
  ];

  // 执行重置
  const handleReset = () => {
    const selectedOption = resetOptions.find(opt => opt.value === resetType);
    
    confirm({
      title: '⚠️ 确认重置游戏进度？',
      icon: <WarningOutlined style={{ color: '#ff4d4f' }} />,
      width: 500,
      content: (
        <Space direction="vertical" style={{ marginTop: 16, width: '100%' }}>
          <Card size="small" style={{ background: '#fff1f0' }}>
            <Space direction="vertical">
              <Text strong>您选择的重置类型：</Text>
              <Tag color={selectedOption?.color} style={{ fontSize: 14, padding: '4px 8px' }}>
                {selectedOption?.icon} {selectedOption?.label}
              </Tag>
              <Text type="secondary" style={{ fontSize: 12 }}>
                {selectedOption?.detail}
              </Text>
            </Space>
          </Card>
          <Alert
            message="此操作不可逆！"
            description="重置后的数据无法恢复，请确认后再执行。"
            type="error"
            showIcon
          />
        </Space>
      ),
      okText: '我已确认，执行重置',
      okButtonProps: {
        danger: true,
        loading: loading,
        size: 'large',
      },
      cancelText: '取消',
      cancelButtonProps: {
        size: 'large',
      },
      onOk: async () => {
        setLoading(true);
        try {
          // TODO: 调用 API
          // await resetProgress(testAccountId, resetType);
          
          setTimeout(() => {
            message.success(`✅ 已成功${selectedOption?.label}！`);
            onSuccess?.(`已成功${selectedOption?.label}！`);
            setLoading(false);
          }, 800);
        } catch (error) {
          message.error('❌ 重置失败，请重试');
          setLoading(false);
        }
      },
    });
  };

  return (
    <Space direction="vertical" style={{ width: '100%' }} size="large">
      {/* 警告提示 */}
      <Alert
        message="⚠️ 危险操作区域"
        description="此区域的操作用于重置测试账号的游戏进度。重置后的数据无法恢复，请谨慎使用。所有操作将被记录到操作日志中。"
        type="warning"
        showIcon
        banner
      />

      {/* 重置选项 */}
      <Card size="small" title="选择重置类型">
        <Radio.Group
          value={resetType}
          onChange={(e) => setResetType(e.target.value)}
          style={{ width: '100%' }}
        >
          <Space direction="vertical" style={{ width: '100%' }}>
            {resetOptions.map((option) => (
              <Radio.Button
                key={option.value}
                value={option.value}
                style={{
                  width: '100%',
                  height: 'auto',
                  padding: '12px 16px',
                  textAlign: 'left',
                  borderRadius: 6,
                  border: `1px solid ${resetType === option.value ? option.color : '#d9d9d9'}`,
                  background: resetType === option.value ? `${option.color}10` : 'white',
                }}
              >
                <Space align="start">
                  <span style={{ fontSize: 20, color: option.color }}>{option.icon}</span>
                  <Space direction="vertical" size={0}>
                    <Text strong style={{ color: resetType === option.value ? option.color : 'inherit' }}>
                      {option.label}
                    </Text>
                    <Text type="secondary" style={{ fontSize: 12 }}>
                      {option.description}
                    </Text>
                  </Space>
                </Space>
              </Radio.Button>
            ))}
          </Space>
        </Radio.Group>
      </Card>

      {/* 执行按钮 */}
      <Card size="small" style={{ background: '#fff1f0', border: '1px solid #ffa39e' }}>
        <Space direction="vertical" style={{ width: '100%' }}>
          <Space align="center">
            <WarningOutlined style={{ color: '#ff4d4f', fontSize: 20 }} />
            <Text type="danger" strong style={{ fontSize: 16 }}>
              确认执行重置操作
            </Text>
          </Space>
          
          <Divider style={{ margin: '8px 0' }} />
          
          <Descriptions column={1} size="small">
            <Descriptions.Item label="重置类型">
              <Tag color={resetOptions.find(opt => opt.value === resetType)?.color} style={{ fontSize: 13 }}>
                {resetOptions.find(opt => opt.value === resetType)?.icon}
                {' '}
                {resetOptions.find(opt => opt.value === resetType)?.label}
              </Tag>
            </Descriptions.Item>
            <Descriptions.Item label="影响范围">
              <Text type="warning">
                {resetOptions.find(opt => opt.value === resetType)?.description}
              </Text>
            </Descriptions.Item>
          </Descriptions>
          
          <Alert
            message="⚠️ 警告：此操作不可逆！"
            description="重置后的数据将无法恢复，请再次确认后再执行操作。"
            type="error"
            showIcon
            style={{ marginTop: 8 }}
          />
          
          <Button
            type="primary"
            danger
            size="large"
            icon={<DeleteOutlined />}
            onClick={handleReset}
            loading={loading}
            style={{ width: '100%', marginTop: 8 }}
          >
            我已确认，执行重置
          </Button>
        </Space>
      </Card>
    </Space>
  );
};

export default ProgressReset;
