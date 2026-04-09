import React, { useState } from 'react';
import { Form, Input, Button, Card, App as AntdApp, Typography, Space, Tag } from 'antd';
import { UserOutlined, LockOutlined, SafetyCertificateOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
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
        message.success('登录成功');
        navigate('/dashboard');
      } else {
        message.error(response.message || '登录失败');
      }
    } catch (error: any) {
      message.error(error.message || '用户名或密码错误');
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
        style={{ width: 440, borderRadius: 24, boxShadow: '0 28px 80px rgba(60, 72, 140, 0.18)' }}
        styles={{ body: { padding: 32 } }}
        variant="borderless"
      >
        <Space direction="vertical" size={8} style={{ width: '100%', marginBottom: 28 }}>
          <Tag color="purple" icon={<SafetyCertificateOutlined />} style={{ width: 'fit-content' }}>
            CloudBase Admin Console
          </Tag>
          <Title level={2} style={{ margin: 0 }}>
            澳小遊后台管理
          </Title>
          <Paragraph type="secondary" style={{ marginBottom: 0 }}>
            用于管理小程序内容、用户进度、测试控制台与运营数据。
          </Paragraph>
        </Space>

        <Form name="login" onFinish={handleLogin} autoComplete="off" size="large" layout="vertical">
          <Form.Item name="username" label="用户名" rules={[{ required: true, message: '请输入用户名' }]}> 
            <Input prefix={<UserOutlined />} placeholder="请输入后台账号" />
          </Form.Item>

          <Form.Item name="password" label="密码" rules={[{ required: true, message: '请输入密码' }]}> 
            <Input.Password prefix={<LockOutlined />} placeholder="请输入密码" />
          </Form.Item>

          <Form.Item style={{ marginBottom: 12 }}>
            <Button type="primary" htmlType="submit" block loading={loading} size="large">
              登录后台系统
            </Button>
          </Form.Item>
        </Form>

        <div style={{ padding: 16, borderRadius: 16, background: '#f7f8ff' }}>
          <Text type="secondary">测试账号</Text>
          <br />
          <Text strong>admin / admin123</Text>
        </div>
      </Card>
    </div>
  );
};

export default Login;

