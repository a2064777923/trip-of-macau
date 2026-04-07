import React, { useState } from 'react';
import { Card, Form, InputNumber, Slider, Button, Space, Tag, message, Row, Col, Statistic, Progress, Divider, Typography, Avatar, Badge } from 'antd';
import { TrophyOutlined, RiseOutlined, FallOutlined, CheckCircleOutlined, StarOutlined, CrownOutlined, GemOutlined } from '@ant-design/icons';

const { Title, Text, Paragraph } = Typography;

// 等级配置
const LEVEL_CONFIG = [
  { level: 1, name: '新手游客', minExp: 0, maxExp: 50, icon: '🌱', color: '#8c8c8c' },
  { level: 2, name: '澳门新手', minExp: 50, maxExp: 150, icon: '🌿', color: '#52c41a' },
  { level: 3, name: '澳门探索者', minExp: 150, maxExp: 300, icon: '🌳', color: '#1890ff' },
  { level: 4, name: '澳门达人', minExp: 300, maxExp: 500, icon: '⭐', color: '#722ed1' },
  { level: 5, name: '澳门通', minExp: 500, maxExp: 800, icon: '🌟', color: '#fa8c16' },
  { level: 6, name: '澳门大师', minExp: 800, maxExp: 1200, icon: '👑', color: '#eb2f96' },
  { level: 7, name: '澳门传奇', minExp: 1200, maxExp: 999999, icon: '🏆', color: '#f5222d' },
];

interface LevelControlProps {
  testAccountId: number;
  currentLevel: number;
  currentExperience: number;
  levelName: string;
  onSuccess?: (message?: string) => void;
}

const LevelControl: React.FC<LevelControlProps> = ({
  testAccountId,
  currentLevel,
  currentExperience,
  levelName,
  onSuccess,
}) => {
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);
  const [previewLevel, setPreviewLevel] = useState(currentLevel);
  const [previewExp, setPreviewExp] = useState(currentExperience);

  // 获取等级信息
  const getLevelInfo = (level: number) => {
    return LEVEL_CONFIG.find(l => l.level === level) || LEVEL_CONFIG[0];
  };

  const currentLevelInfo = getLevelInfo(currentLevel);
  const previewLevelInfo = getLevelInfo(previewLevel);

  // 计算经验值进度
  const calculateProgress = (exp: number, level: number) => {
    const levelInfo = getLevelInfo(level);
    const range = levelInfo.maxExp - levelInfo.minExp;
    const current = exp - levelInfo.minExp;
    return Math.min(100, Math.max(0, (current / range) * 100));
  };

  // 调整等级
  const handleAdjustLevel = async (values: any) => {
    setLoading(true);
    try {
      // TODO: 调用 API
      // await adjustLevel(testAccountId, values.level, values.experience);
      
      setTimeout(() => {
        message.success(`等级调整成功！当前等级: Lv.${values.level} ${getLevelInfo(values.level).name}`);
        onSuccess?.(`等级调整成功！当前等级: Lv.${values.level}`);
        setLoading(false);
      }, 500);
    } catch (error) {
      message.error('等级调整失败');
      setLoading(false);
    }
  };

  // 预览等级变化
  const handlePreview = () => {
    const values = form.getFieldsValue();
    setPreviewLevel(values.level || currentLevel);
    setPreviewExp(values.experience || currentExperience);
  };

  // 快速设置等级
  const quickSetLevel = (level: number) => {
    const levelInfo = getLevelInfo(level);
    form.setFieldsValue({
      level,
      experience: levelInfo.minExp,
    });
    setPreviewLevel(level);
    setPreviewExp(levelInfo.minExp);
  };

  return (
    <Space direction="vertical" style={{ width: '100%' }} size="large">
      {/* 当前等级状态 */}
      <Card size="small">
        <Row gutter={16} align="middle">
          <Col span={6}>
            <div style={{ textAlign: 'center' }}>
              <Avatar
                size={64}
                style={{
                  backgroundColor: currentLevelInfo.color,
                  fontSize: 32,
                }}
              >
                {currentLevelInfo.icon}
              </Avatar>
              <div style={{ marginTop: 8 }}>
                <Tag color={currentLevelInfo.color} style={{ fontSize: 14, padding: '2px 8px' }}>
                  Lv.{currentLevel} {currentLevelInfo.name}
                </Tag>
              </div>
            </div>
          </Col>
          <Col span={18}>
            <Row gutter={16}>
              <Col span={12}>
                <Statistic
                  title="当前经验值"
                  value={currentExperience}
                  suffix={`/ ${currentLevelInfo.maxExp} XP`}
                />
              </Col>
              <Col span={12}>
                <div style={{ marginTop: 8 }}>
                  <Text type="secondary">升级到下一级还需:</Text>
                  <Progress
                    percent={calculateProgress(currentExperience, currentLevel)}
                    size="small"
                    format={() => `${currentLevelInfo.maxExp - currentExperience} XP`}
                    strokeColor={currentLevelInfo.color}
                  />
                </div>
              </Col>
            </Row>
          </Col>
        </Row>
      </Card>

      {/* 等级调整表单 */}
      <Card size="small" title="调整等级">
        <Form
          form={form}
          layout="vertical"
          onFinish={handleAdjustLevel}
          initialValues={{
            level: currentLevel,
            experience: currentExperience,
          }}
        >
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                label="目标等级"
                name="level"
                rules={[{ required: true, message: '请选择等级' }]}
              >
                <InputNumber
                  min={1}
                  max={7}
                  style={{ width: '100%' }}
                  onChange={(value) => {
                    if (value) {
                      const levelInfo = getLevelInfo(value);
                      form.setFieldValue('experience', levelInfo.minExp);
                      handlePreview();
                    }
                  }}
                />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item
                label="经验值"
                name="experience"
                rules={[{ required: true, message: '请输入经验值' }]}
              >
                <InputNumber
                  min={0}
                  style={{ width: '100%' }}
                  onChange={() => handlePreview()}
                />
              </Form.Item>
            </Col>
          </Row>

          {/* 快速设置按钮 */}
          <Form.Item label="快速设置">
            <Space wrap>
              {LEVEL_CONFIG.map((level) => (
                <Button
                  key={level.level}
                  size="small"
                  onClick={() => quickSetLevel(level.level)}
                  type={previewLevel === level.level ? 'primary' : 'default'}
                  style={{
                    borderColor: previewLevel === level.level ? level.color : undefined,
                    backgroundColor: previewLevel === level.level ? level.color : undefined,
                  }}
                >
                  {level.icon} Lv.{level.level}
                </Button>
              ))}
            </Space>
          </Form.Item>

          {/* 预览 */}
          <Card size="small" style={{ marginBottom: 16, background: '#f6ffed' }}>
            <Row align="middle">
              <Col span={12}>
                <Space>
                  <span>调整预览:</span>
                  <Tag 
                    color={previewLevelInfo.color}
                    style={{ fontSize: 14, padding: '2px 8px' }}
                  >
                    {previewLevelInfo.icon} Lv.{previewLevel} {previewLevelInfo.name}
                  </Tag>
                </Space>
              </Col>
              <Col span={12} style={{ textAlign: 'right' }}>
                <Text type="secondary">
                  经验值: {previewExp} / {previewLevelInfo.maxExp} XP
                </Text>
              </Col>
            </Row>
          </Card>

          <Form.Item>
            <Space>
              <Button
                type="primary"
                htmlType="submit"
                icon={<TrophyOutlined />}
                loading={loading}
              >
                确认调整等级
              </Button>
              <Button onClick={handlePreview}>预览</Button>
            </Space>
          </Form.Item>
        </Form>
      </Card>
    </Space>
  );
};

export default LevelControl;
