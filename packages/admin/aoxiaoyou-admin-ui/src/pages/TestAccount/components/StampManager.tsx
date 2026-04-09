import React, { useEffect, useState } from 'react';
import { Card, Button, Space, Tag, message, Progress, Typography, Row, Col, Divider, InputNumber, Modal } from 'antd';
import { TrophyOutlined, PlusOutlined, DeleteOutlined } from '@ant-design/icons';
import { batchGrantTestAccountStamps, clearTestAccountStamps, getTestAccountStampSummary } from '../../../services/api';
import type { AdminTestStampSummary } from '../../../types/admin';

const { Text } = Typography;

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
  const [summary, setSummary] = useState<AdminTestStampSummary | null>(null);
  const [stampCount, setStampCount] = useState(currentStampCount);
  const [batchCount, setBatchCount] = useState(3);
  const [loading, setLoading] = useState(false);

  const loadSummary = async () => {
    const response = await getTestAccountStampSummary(testAccountId);
    if (response.success && response.data) {
      setSummary(response.data);
      setStampCount(response.data.stampCount);
    }
  };

  useEffect(() => {
    loadSummary();
  }, [testAccountId]);

  const handleBatchGrant = async (count: number) => {
    setLoading(true);
    try {
      await batchGrantTestAccountStamps(testAccountId, {
        count,
        stampType: 'check_in',
        reason: `批量增加 ${count} 个印章`,
      });
      await loadSummary();
      message.success(`成功增加 ${count} 个印章`);
      onSuccess?.(`成功增加 ${count} 个印章`);
    } catch (error) {
      message.error('批量加章失败');
    } finally {
      setLoading(false);
    }
  };

  const handleClear = () => {
    Modal.confirm({
      title: '确认清空该账号的全部印章？',
      content: '清空后等级会同步回到初始状态，此操作会写入审计日志。',
      okButtonProps: { danger: true },
      onOk: async () => {
        setLoading(true);
        try {
          await clearTestAccountStamps(testAccountId, '测试控制台清空全部印章');
          await loadSummary();
          message.success('印章已清空');
          onSuccess?.('印章已清空');
        } catch (error) {
          message.error('清空印章失败');
        } finally {
          setLoading(false);
        }
      },
    });
  };

  const totalStamps = summary?.maxStamps || 12;
  const progress = Math.round((stampCount / totalStamps) * 100);

  return (
    <Space direction="vertical" style={{ width: '100%' }} size="large">
      <Card size="small">
        <Row gutter={16} align="middle">
          <Col span={8}>
            <div style={{ textAlign: 'center' }}>
              <Progress
                type="circle"
                percent={progress}
                size={80}
                format={() => `${stampCount}/${totalStamps}`}
              />
              <div style={{ marginTop: 8 }}>
                <Text type="secondary">印章收集进度</Text>
              </div>
            </div>
          </Col>
          <Col span={16}>
            <Space direction="vertical">
              <Text>当前已收集 <Tag color="blue">{stampCount}</Tag> 个印章</Text>
              <Text>当前等级 <Tag color="purple">Lv.{summary?.currentLevel || 1} {summary?.levelName || '新手旅者'}</Tag></Text>
              <Text type="secondary">距离下一等级还需 <Tag color="orange">{summary?.remainingToNextLevel ?? 0}</Tag> 个印章</Text>
            </Space>
          </Col>
        </Row>
      </Card>

      <Card size="small" title="批量印章操作">
        <Space direction="vertical" style={{ width: '100%' }}>
          <Space wrap>
            {[1, 3, 5].map((n) => (
              <Button
                key={n}
                icon={<PlusOutlined />}
                onClick={() => handleBatchGrant(n)}
                loading={loading}
              >
                +{n}
              </Button>
            ))}
            <Divider type="vertical" />
            <InputNumber min={1} max={12} value={batchCount} onChange={(value) => setBatchCount(value || 1)} />
            <Button type="primary" onClick={() => handleBatchGrant(batchCount)} loading={loading}>
              自定义批量加章
            </Button>
            <Button icon={<TrophyOutlined />} onClick={() => handleBatchGrant(Math.max(1, totalStamps - stampCount))} loading={loading}>
              补满印章
            </Button>
          </Space>
          <Button danger icon={<DeleteOutlined />} onClick={handleClear} loading={loading}>
            清空全部印章
          </Button>
        </Space>
      </Card>

      {stampCount >= totalStamps && (
        <Card size="small" style={{ background: '#f6ffed', border: '1px solid #b7eb8f' }}>
          <Space>
            <TrophyOutlined style={{ color: '#52c41a', fontSize: 20 }} />
            <Text strong style={{ color: '#52c41a' }}>恭喜！已收集全部 {totalStamps} 个印章！</Text>
          </Space>
        </Card>
      )}
    </Space>
  );
};

export default StampManager;
