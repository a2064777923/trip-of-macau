import React, { useState } from 'react';
import { Card, Form, Input, Button, Switch, Space, Tag, message, Row, Col, Descriptions } from 'antd';
import { EnvironmentOutlined, CheckCircleOutlined } from '@ant-design/icons';

interface MockLocationPanelProps {
  testAccountId: number;
  mockLocation?: {
    latitude: number;
    longitude: number;
    address: string;
  };
  isMockEnabled: boolean;
  onSuccess?: (message?: string) => void;
}

const MockLocationPanel: React.FC<MockLocationPanelProps> = ({
  testAccountId,
  mockLocation,
  isMockEnabled,
  onSuccess,
}) => {
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);
  const [enabled, setEnabled] = useState(isMockEnabled);

  // 设置模拟定位
  const handleSetLocation = async (values: any) => {
    setLoading(true);
    try {
      // TODO: 调用 API
      // await setMockLocation(testAccountId, values);
      
      setTimeout(() => {
        message.success('模拟定位设置成功');
        onSuccess?.('模拟定位设置成功');
        setLoading(false);
      }, 500);
    } catch (error) {
      message.error('设置失败');
      setLoading(false);
    }
  };

  // 切换模拟定位状态
  const handleToggleMock = async (checked: boolean) => {
    setLoading(true);
    try {
      // TODO: 调用 API
      // await toggleMockLocation(testAccountId, checked);
      
      setTimeout(() => {
        setEnabled(checked);
        message.success(`${checked ? '启用' : '禁用'}模拟定位成功`);
        onSuccess?.(`${checked ? '启用' : '禁用'}模拟定位成功`);
        setLoading(false);
      }, 500);
    } catch (error) {
      message.error('操作失败');
      setLoading(false);
    }
  };

  // 清除模拟定位
  const handleClearLocation = async () => {
    setLoading(true);
    try {
      // TODO: 调用 API
      // await clearMockLocation(testAccountId);
      
      setTimeout(() => {
        form.resetFields();
        message.success('模拟定位已清除');
        onSuccess?.('模拟定位已清除');
        setLoading(false);
      }, 500);
    } catch (error) {
      message.error('清除失败');
      setLoading(false);
    }
  };

  return (
    <Space direction="vertical" style={{ width: '100%' }} size="large">
      {/* 当前状态 */}
      <Card size="small" title="当前模拟定位状态">
        <Descriptions column={2} size="small">
          <Descriptions.Item label="状态">
            {enabled ? (
              <Tag color="success" icon={<CheckCircleOutlined />}>已启用</Tag>
            ) : (
              <Tag color="default">未启用</Tag>
            )}
          </Descriptions.Item>
          <Descriptions.Item label="地址">
            {mockLocation?.address || '未设置'}
          </Descriptions.Item>
          <Descriptions.Item label="纬度">
            {mockLocation?.latitude || '-'}
          </Descriptions.Item>
          <Descriptions.Item label="经度">
            {mockLocation?.longitude || '-'}
          </Descriptions.Item>
        </Descriptions>
        
        <Divider style={{ margin: '12px 0' }} />
        
        <Space>
          <span>启用模拟定位:</span>
          <Switch
            checked={enabled}
            onChange={handleToggleMock}
            loading={loading}
            checkedChildren="已启用"
            unCheckedChildren="已禁用"
          />
        </Space>
      </Card>

      {/* 设置模拟定位 */}
      <Card size="small" title="设置模拟定位">
        <Form
          form={form}
          layout="vertical"
          onFinish={handleSetLocation}
          initialValues={{
            latitude: mockLocation?.latitude,
            longitude: mockLocation?.longitude,
            address: mockLocation?.address,
          }}
        >
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                label="纬度"
                name="latitude"
                rules={[
                  { required: true, message: '请输入纬度' },
                  { type: 'number', min: 22.1, max: 22.2, message: '纬度必须在澳门范围内(22.1-22.2)' },
                ]}
              >
                <Input type="number" step="0.0001" placeholder="例如: 22.1973" />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item
                label="经度"
                name="longitude"
                rules={[
                  { required: true, message: '请输入经度' },
                  { type: 'number', min: 113.5, max: 113.6, message: '经度必须在澳门范围内(113.5-113.6)' },
                ]}
              >
                <Input type="number" step="0.0001" placeholder="例如: 113.5408" />
              </Form.Item>
            </Col>
          </Row>

          <Form.Item
            label="地址描述"
            name="address"
            rules={[{ required: true, message: '请输入地址描述' }]}
          >
            <Input placeholder="例如: 澳门半岛大三巴牌坊" />
          </Form.Item>

          <Form.Item>
            <Space>
              <Button type="primary" htmlType="submit" loading={loading} icon={<EnvironmentOutlined />}>
                设置模拟定位
              </Button>
              <Button onClick={handleClearLocation} loading={loading} danger>
                清除定位
              </Button>
            </Space>
          </Form.Item>
        </Form>
      </Card>
    </Space>
  );
};

export default MockLocationPanel;
