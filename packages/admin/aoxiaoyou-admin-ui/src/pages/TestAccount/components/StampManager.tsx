import React, { useState } from 'react';
import { Card, Button, Checkbox, Space, Tag, message, Progress, Typography, Row, Col, Divider, Badge } from 'antd';
import { TrophyOutlined, PlusOutlined, DeleteOutlined, ClearOutlined } from '@ant-design/icons';

const { Title, Text } = Typography;

// 模拟印章数据
const MOCK_STAMPS = [
  { id: 1, name: '大三巴', icon: '🏛️', poiId: 101, collected: true },
  { id: 2, name: '威尼斯人', icon: '🎰', poiId: 102, collected: true },
  { id: 3, name: '官也街', icon: '🍜', poiId: 103, collected: true },
  { id: 4, name: '妈阁庙', icon: '🛕', poiId: 104, collected: false },
  { id: 5, name: '新葡京', icon: '🎲', poiId: 105, collected: false },
  { id: 6, name: '龙环葡韵', icon: '🌳', poiId: 106, collected: false },
  { id: 7, name: '渔人码头', icon: '⚓', poiId: 107, collected: false },
  { id: 8, name: '澳门塔', icon: '🗼', poiId: 108, collected: false },
  { id: 9, name: '路环圣方济各堂', icon: '⛪', poiId: 109, collected: false },
  { id: 10, name: '黑沙滩', icon: '🏖️', poiId: 110, collected: false },
  { id: 11, name: '议事亭前地', icon: '🏛️', poiId: 111, collected: false },
  { id: 12, name: '玫瑰堂', icon: '🌹', poiId: 112, collected: false },
];

interface StampManagerProps {
  testAccountId: number;
  currentStampCount: number;
  onSuccess?: (message?: string) => void;
}

const StampManager: React.FC<StampManagerProps> = ({
  testAccountId,
  currentStampCount,
  onSuccess,
}) => {
  const [selectedStamps, setSelectedStamps] = useState<number[]>([]);
  const [loading, setLoading] = useState(false);

  // 计算已收集和未收集的印章
  const collectedStamps = MOCK_STAMPS.filter(s => s.collected);
  const uncollectedStamps = MOCK_STAMPS.filter(s => !s.collected);

  // 快速获得印章
  const handleGrantStamps = async () => {
    if (selectedStamps.length === 0) {
      message.warning('请至少选择一个印章');
      return;
    }

    setLoading(true);
    try {
      // TODO: 调用 API
      // await grantStamps(testAccountId, selectedStamps);
      
      setTimeout(() => {
        const stampNames = selectedStamps.map(id => 
          MOCK_STAMPS.find(s => s.id === id)?.name
        ).join(', ');
        
        message.success(`成功获得 ${selectedStamps.length} 个印章: ${stampNames}`);
        onSuccess?.(`成功获得 ${selectedStamps.length} 个印章`);
        setSelectedStamps([]);
        setLoading(false);
      }, 500);
    } catch (error) {
      message.error('获得印章失败');
      setLoading(false);
    }
  };

  // 删除印章
  const handleDeleteStamp = async (stampId: number) => {
    setLoading(true);
    try {
      // TODO: 调用 API
      // await deleteStamp(testAccountId, stampId);
      
      setTimeout(() => {
        const stampName = MOCK_STAMPS.find(s => s.id === stampId)?.name;
        message.success(`已删除印章: ${stampName}`);
        onSuccess?.(`已删除印章: ${stampName}`);
        setLoading(false);
      }, 500);
    } catch (error) {
      message.error('删除印章失败');
      setLoading(false);
    }
  };

  // 清空所有印章
  const handleClearAllStamps = async () => {
    setLoading(true);
    try {
      // TODO: 调用 API
      // await clearStamps(testAccountId);
      
      setTimeout(() => {
        message.success('已清空所有印章');
        onSuccess?.('已清空所有印章');
        setLoading(false);
      }, 500);
    } catch (error) {
      message.error('清空印章失败');
      setLoading(false);
    }
  };

  return (
    <Space direction="vertical" style={{ width: '100%' }} size="large">
      {/* 进度概览 */}
      <Card size="small">
        <Row gutter={16} align="middle">
          <Col span={8}>
            <div style={{ textAlign: 'center' }}>
              <Progress
                type="circle"
                percent={Math.round((currentStampCount / 12) * 100)}
                size={80}
                format={() => `${currentStampCount}/12`}
              />
              <div style={{ marginTop: 8 }}>
                <Text type="secondary">印章收集进度</Text>
              </div>
            </div>
          </Col>
          <Col span={16}>
            <Space wrap>
              {collectedStamps.map(stamp => (
                <Tag key={stamp.id} color="success" icon={<TrophyOutlined />}>
                  {stamp.icon} {stamp.name}
                </Tag>
              ))}
            </Space>
          </Col>
        </Row>
      </Card>

      {/* 快速获得印章 */}
      <Card size="small" title="快速获得印章">
        <Space direction="vertical" style={{ width: '100%' }}>
          <Text type="secondary">选择要获得的印章（可多选）：</Text>
          <Checkbox.Group
            value={selectedStamps}
            onChange={(values) => setSelectedStamps(values as number[])}
          >
            <Space wrap>
              {uncollectedStamps.map(stamp => (
                <Checkbox key={stamp.id} value={stamp.id}>
                  <Tag>{stamp.icon} {stamp.name}</Tag>
                </Checkbox>
              ))}
            </Space>
          </Checkbox.Group>
          <Space>
            <Button
              type="primary"
              icon={<PlusOutlined />}
              onClick={handleGrantStamps}
              loading={loading}
              disabled={selectedStamps.length === 0}
            >
              获得选中印章 ({selectedStamps.length})
            </Button>
            <Button onClick={() => setSelectedStamps([])}>清空选择</Button>
          </Space>
        </Space>
      </Card>

      {/* 管理已收集的印章 */}
      <Card size="small" title="管理已收集印章">
        <Space direction="vertical" style={{ width: '100%' }}>
          <Text type="secondary">删除指定印章或清空所有印章：</Text>
          <Space wrap>
            {collectedStamps.map(stamp => (
              <Button
                key={stamp.id}
                size="small"
                icon={<DeleteOutlined />}
                onClick={() => handleDeleteStamp(stamp.id)}
                loading={loading}
              >
                {stamp.icon} {stamp.name}
              </Button>
            ))}
          </Space>
          <Divider style={{ margin: '12px 0' }} />
          <Button
            danger
            icon={<ClearOutlined />}
            onClick={handleClearAllStamps}
            loading={loading}
          >
            清空所有印章
          </Button>
        </Space>
      </Card>
    </Space>
  );
};

export default StampManager;
