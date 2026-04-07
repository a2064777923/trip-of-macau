import { Footer } from '@ant-design/pro-layout';
import { Alert, Button, message, Tabs } from 'antd';
import type { FormInstance } from 'antd/es/form';
import React, { useRef, useState } from 'react';
import { history } from '@umijs/max';
import styles from './index.less';

const LoginMessage: React.FC<{ content: string }> = ({ content }) => {
  return (
    <Alert
      style={{
        marginBottom: 24,
      }}
      message={content}
      type="error"
      showIcon
    />
  );
};

const Login: React.FC = () => {
  const formRef = useRef<FormInstance | null>(null);
  const [userLoginState, setUserLoginState] = useState<API.LoginResult>({});
  const [type, setType] = useState<string>('account');
  const [loading, setLoading] = useState(false);

  const fetchUserInfo = async () => {
    const userInfo = await initialState?.fetchUserInfo?.();
    if (userInfo) {
      await setInitialState((s) => ({
        ...s,
        currentUser: userInfo,
      }));
    }
  };

  const handleSubmit = async (values: API.LoginParams) => {
    try {
      setLoading(true);
      // Mock login - simulate API call
      await new Promise(resolve => setTimeout(resolve, 1000));
      
      if (values.username === 'admin' && values.password === '123456' && values.captcha === 'a3b5') {
        const mockToken = `mock_token_${Date.now()}`;
        localStorage.setItem('access_token', mockToken);
        localStorage.setItem('user_info', JSON.stringify({
          id: 1,
          username: 'admin',
          nickname: '超级管理员',
          avatar: 'https://gw.alipayobjects.com/zos/antfincdn/XAosXuNZyF/BiazfanxmamNRoxxVxka.png',
        }));
        
        message.success('登录成功！');
        
        // 跳转到首页
        if (!history) return;
        const { query } = history.location;
        const { redirect } = query as { redirect: string };
        history.push(redirect || '/');
        return;
      } else {
        throw new Error('用户名或密码错误');
      }
    } catch (error: any) {
      message.error(error.message || '登录失败，请重试');
      setUserLoginState({ status: 'error', type: 'account' });
    } finally {
      setLoading(false);
    }
  };

  const { status, type: loginType } = userLoginState;

  return (
    <div className={styles.container}>
      <div className={styles.content}>
        <div className={styles.top}>
          <div className={styles.header}>
            <span className={styles.title}>澳小遊后台管理系统</span>
          </div>
          <div className={styles.desc}>
            基于 React + Umi + Ant Design Pro 构建
          </div>
        </div>

        <div className={styles.main}>
          <Tabs
            activeKey={type}
            onChange={setType}
            centered
            items={[
              { key: 'account', label: '账号密码登录' },
            ]}
          />

          {status === 'error' && loginType === 'account' && (
            <LoginMessage content="账户或密码错误" />
          )}

          <form
            onSubmit={(e) => {
              e.preventDefault();
              const formData = new FormData(e.currentTarget);
              const values = Object.fromEntries(formData.entries()) as API.LoginParams;
              handleSubmit(values);
            }}
            className={styles.loginForm}
          >
            <div className={styles.formItem}>
              <input
                name="username"
                placeholder="用户名: admin"
                required
                className={styles.input}
              />
            </div>
            <div className={styles.formItem}>
              <input
                name="password"
                type="password"
                placeholder="密码: 123456"
                required
                className={styles.input}
              />
            </div>
            <div className={styles.formItem}>
              <div className={styles.captchaRow}>
                <input
                  name="captcha"
                  placeholder="验证码: a3b5"
                  required
                  className={styles.captchaInput}
                />
                <div className={styles.captchaImg}>a3b5</div>
              </div>
            </div>
            <Button
              type="primary"
              htmlType="submit"
              loading={loading}
              block
              size="large"
            >
              登录
            </Button>
          </form>

          <div className={styles.otherLogin}>
            其他登录方式：
            <span className={styles.icon}>企业微信</span>
            <span className={styles.icon}>飞书</span>
          </div>
        </div>
      </div>

      <Footer />
    </div>
  );
};

export default Login;