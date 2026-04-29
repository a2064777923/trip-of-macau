import React, { useEffect } from 'react';
import { useRequest } from 'ahooks';
import { App as AntdApp, Button, Card, Form, InputNumber, Space, Switch, Typography } from 'antd';
import { getAiPlatformSettings, updateAiPlatformSettings } from '../../services/api';

const { Paragraph, Title } = Typography;

const SettingsPage: React.FC = () => {
  const { message } = AntdApp.useApp();
  const [form] = Form.useForm();
  const settingsReq = useRequest(() => getAiPlatformSettings());

  useEffect(() => {
    if (settingsReq.data?.data) {
      form.setFieldsValue(settingsReq.data.data);
    }
  }, [form, settingsReq.data?.data]);

  const handleSave = async () => {
    try {
      const values = await form.validateFields();
      const response = await updateAiPlatformSettings(values);
      if (!response.success || !response.data) {
        throw new Error(response.message || '更新治理設定失敗');
      }
      form.setFieldsValue(response.data);
      message.success('已更新 AI 平台治理設定');
    } catch (error: any) {
      message.error(error?.message || '更新治理設定失敗');
    }
  };

  return (
    <Space direction="vertical" size="large" style={{ width: '100%' }}>
      <Card style={{ borderRadius: 22 }}>
        <Title level={4} style={{ marginTop: 0 }}>
          治理設定
        </Title>
        <Paragraph type="secondary" style={{ marginBottom: 0 }}>
          設定模型庫新鮮度、同步歷史保留數、估算成本警戒值與歷史查看邊界，作為整個 AI 平台的基礎治理參數。
        </Paragraph>
      </Card>

      <Card style={{ borderRadius: 22 }}>
        <Form form={form} layout="vertical">
          <Form.Item
            name="inventoryFreshnessHours"
            label="模型庫新鮮度門檻（小時）"
            rules={[{ required: true, message: '請填寫模型庫新鮮度門檻' }]}
          >
            <InputNumber min={1} style={{ width: '100%' }} />
          </Form.Item>

          <Form.Item
            name="syncHistoryLimit"
            label="保留同步歷史筆數"
            rules={[{ required: true, message: '請填寫同步歷史保留數' }]}
          >
            <InputNumber min={1} style={{ width: '100%' }} />
          </Form.Item>

          <Form.Item
            name="dailyCostAlertUsd"
            label="日估算成本警戒值（USD）"
            rules={[{ required: true, message: '請填寫日估算成本警戒值' }]}
          >
            <InputNumber min={0} step={1} style={{ width: '100%' }} />
          </Form.Item>

          <Form.Item
            name="providerFailureRateWarning"
            label="供應商失敗率預警值"
            rules={[{ required: true, message: '請填寫供應商失敗率預警值' }]}
          >
            <InputNumber min={0} max={1} step={0.01} style={{ width: '100%' }} />
          </Form.Item>

          <Form.Item
            name="recentWindowHours"
            label="近期監控視窗（小時）"
            rules={[{ required: true, message: '請填寫近期監控視窗' }]}
          >
            <InputNumber min={1} style={{ width: '100%' }} />
          </Form.Item>

          <Form.Item
            name="allowOperatorGlobalHistory"
            label="允許一般編輯者查看全域歷史"
            valuePropName="checked"
          >
            <Switch checkedChildren="允許" unCheckedChildren="僅限自身" />
          </Form.Item>

          <Button type="primary" onClick={() => void handleSave()}>
            儲存治理設定
          </Button>
        </Form>
      </Card>
    </Space>
  );
};

export default SettingsPage;
