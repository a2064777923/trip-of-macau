import React, { useState } from 'react';
import { Form, Input, Button, Card, App as AntdApp, Typography, Space, Tag } from 'antd';
import { UserOutlined, LockOutlined, SafetyCertificateOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import brandLogo from '@shared-client-assets/logo.png';
import { adminLogin } from '../services/api';
import { setAdminToken, setAdminUser, setRefreshToken } from '../utils/auth';
import { useAuthStore } from '../stores/auth';

const { Title, Text, Paragraph } = Typography;

const Login: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();
  const { setUser } = useAuthStore();
  const { message } = AntdApp.useApp();

  const handleLogin = async (values: { username: string; password: string }) => {
    setLoading(true);
    try {
      const response = await adminLogin(values);
      if (response.success && response.data?.token) {
        setAdminToken(response.data.token);
        setRefreshToken(response.data.refreshToken);
        setAdminUser(response.data.user);
        setUser(response.data.user);
        message.success('登入成功');
        navigate('/dashboard');
      } else {
        message.error(response.message || '登入失敗');
      }
    } catch (error: any) {
      message.error(error.message || '帳號或密碼錯誤');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div
      style={{
        minHeight: '100vh',
        display: 'grid',
        placeItems: 'center',
        padding: 24,
        background:
          'radial-gradient(circle at top left, rgba(124,92,255,0.18), transparent 32%), linear-gradient(135deg, #eef2ff 0%, #f6f8ff 50%, #eef5ff 100%)',
      }}
    >
      <Card
        style={{ width: 460, borderRadius: 24, boxShadow: '0 28px 80px rgba(60, 72, 140, 0.18)' }}
        styles={{ body: { padding: 32 } }}
        variant="borderless"
      >
        <Space direction="vertical" size={12} style={{ width: '100%', marginBottom: 28 }}>
          <img
            src={brandLogo}
            alt="Trip of Macau"
            style={{ width: 72, height: 72, borderRadius: 18, objectFit: 'cover' }}
          />
          <Tag color="purple" icon={<SafetyCertificateOutlined />} style={{ width: 'fit-content', marginInlineEnd: 0 }}>
            Trip of Macau 後台
          </Tag>
          <Title level={2} style={{ margin: 0 }}>
            澳小遊後台管理系統
          </Title>
          <Paragraph type="secondary" style={{ marginBottom: 0 }}>
            統一管理小程序內容、地圖空間、用戶進度、翻譯設定與營運工具，作為 Trip of Macau 的唯一控制平面。
          </Paragraph>
        </Space>

        <Form name="login" onFinish={handleLogin} autoComplete="off" size="large" layout="vertical">
          <Form.Item name="username" label="帳號" rules={[{ required: true, message: '請輸入管理員帳號' }]}>
            <Input prefix={<UserOutlined />} placeholder="請輸入後台帳號" />
          </Form.Item>

          <Form.Item name="password" label="密碼" rules={[{ required: true, message: '請輸入密碼' }]}>
            <Input.Password prefix={<LockOutlined />} placeholder="請輸入密碼" />
          </Form.Item>

          <Form.Item style={{ marginBottom: 12 }}>
            <Button type="primary" htmlType="submit" block loading={loading} size="large">
              登入後台系統
            </Button>
          </Form.Item>
        </Form>

        <div style={{ padding: 16, borderRadius: 16, background: '#f7f8ff' }}>
          <Text type="secondary">本地測試帳號</Text>
          <br />
          <Text strong>admin / admin123</Text>
        </div>
      </Card>
    </div>
  );
};

export default Login;
