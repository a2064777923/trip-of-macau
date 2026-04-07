import React, { useState } from 'react';
import { Layout, Menu, Breadcrumb, Avatar, Badge, Dropdown, Space, Typography, theme } from 'antd';
import {
  DashboardOutlined,
  EnvironmentOutlined,
  UserOutlined,
  ToolOutlined,
  SettingOutlined,
  BellOutlined,
  LogoutOutlined,
  DownOutlined,
} from '@ant-design/icons';
import type { MenuProps } from 'antd';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import './DefaultLayout.css';

const { Header, Sider, Content, Footer } = Layout;
const { Title, Text } = Typography;

type MenuItem = Required<MenuProps>['items'][number];

const DefaultLayout: React.FC<{ children?: React.ReactNode }> = ({ children }) => {
  const [collapsed, setCollapsed] = useState(false);
  const location = useLocation();
  const navigate = useNavigate();
  const {
    token: { colorBgContainer, borderRadiusLG },
  } = theme.useToken();

  // 菜单项配置
  const menuItems: MenuItem[] = [
    {
      key: '/dashboard',
      icon: <DashboardOutlined />,
      label: <Link to="/dashboard">数据概览</Link>,
    },
    {
      key: '/poi',
      icon: <EnvironmentOutlined />,
      label: <Link to="/poi">POI管理</Link>,
    },
    {
      key: '/test-account',
      icon: <ToolOutlined />,
      label: <Link to="/test-account">测试账号</Link>,
    },
    {
      key: '/user',
      icon: <UserOutlined />,
      label: '用户管理',
      children: [
        {
          key: '/user/list',
          label: '用户列表',
        },
        {
          key: '/user/feedback',
          label: '用户反馈',
        },
      ],
    },
    {
      key: '/system',
      icon: <SettingOutlined />,
      label: '系统设置',
      children: [
        {
          key: '/system/config',
          label: '系统配置',
        },
        {
          key: '/system/log',
          label: '操作日志',
        },
      ],
    },
  ];

  // 用户下拉菜单
  const userMenuItems: MenuProps['items'] = [
    {
      key: 'profile',
      label: '个人中心',
      icon: <UserOutlined />,
    },
    {
      key: 'settings',
      label: '账号设置',
      icon: <SettingOutlined />,
    },
    {
      type: 'divider',
    },
    {
      key: 'logout',
      label: '退出登录',
      icon: <LogoutOutlined />,
      danger: true,
    },
  ];

  // 处理菜单点击
  const handleMenuClick: MenuProps['onClick'] = (e) => {
    if (e.key === 'logout') {
      // 处理登出
      navigate('/login');
    }
  };

  // 生成面包屑
  const getBreadcrumb = () => {
    const pathSnippets = location.pathname.split('/').filter(i => i);
    const breadcrumbItems = [
      <Breadcrumb.Item key="home">
        <Link to="/">首页</Link>
      </Breadcrumb.Item>,
    ];
    
    let url = '';
    pathSnippets.forEach((snippet, index) => {
      url += `/${snippet}`;
      const isLast = index === pathSnippets.length - 1;
      breadcrumbItems.push(
        <Breadcrumb.Item key={url}>
          {isLast ? (
            <span style={{ fontWeight: 500 }}>{getPageTitle(url)}</span>
          ) : (
            <Link to={url}>{getPageTitle(url)}</Link>
          )}
        </Breadcrumb.Item>
      );
    });
    
    return breadcrumbItems;
  };

  // 获取页面标题
  const getPageTitle = (path: string) => {
    const titles: Record<string, string> = {
      '/dashboard': '数据概览',
      '/poi': 'POI管理',
      '/test-account': '测试账号',
      '/user': '用户管理',
      '/user/list': '用户列表',
      '/user/feedback': '用户反馈',
      '/system': '系统设置',
      '/system/config': '系统配置',
      '/system/log': '操作日志',
    };
    return titles[path] || '未命名页面';
  };

  return (
    <Layout style={{ minHeight: '100vh' }}>
      {/* 侧边栏 */}
      <Sider
        trigger={null}
        collapsible
        collapsed={collapsed}
        theme="light"
        style={{
          boxShadow: '2px 0 8px rgba(0,0,0,0.05)',
          zIndex: 100,
        }}
      >
        {/* Logo区域 */}
        <div
          style={{
            height: 64,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            borderBottom: '1px solid #f0f0f0',
          }}
        >
          {collapsed ? (
            <div
              style={{
                width: 32,
                height: 32,
                background: 'linear-gradient(135deg, #1890ff 0%, #722ed1 100%)',
                borderRadius: 6,
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                color: '#fff',
                fontWeight: 'bold',
                fontSize: 14,
              }}
            >
              澳
            </div>
          ) : (
            <Space>
              <div
                style={{
                  width: 32,
                  height: 32,
                  background: 'linear-gradient(135deg, #1890ff 0%, #722ed1 100%)',
                  borderRadius: 6,
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  color: '#fff',
                  fontWeight: 'bold',
                  fontSize: 14,
                }}
              >
                澳
              </div>
              <Title level={5} style={{ margin: 0, color: '#1890ff' }}>
                澳小遊后台管理
              </Title>
            </Space>
          )}
        </div>

        {/* 菜单 */}
        <Menu
          mode="inline"
          selectedKeys={[location.pathname]}
          items={menuItems}
          style={{ borderRight: 0 }}
        />
      </Sider>

      {/* 主内容区 */}
      <Layout>
        {/* 顶部导航栏 */}
        <Header
          style={{
            padding: '0 24px',
            background: colorBgContainer,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            boxShadow: '0 2px 8px rgba(0,0,0,0.05)',
            zIndex: 99,
          }}
        >
          <Space>
            <Button
              type="text"
              icon={collapsed ? <MenuUnfoldOutlined /> : <MenuFoldOutlined />}
              onClick={() => setCollapsed(!collapsed)}
              style={{ fontSize: 16 }}
            />
            <Breadcrumb>{getBreadcrumb()}</Breadcrumb>
          </Space>

          <Space size="large">
            <Badge count={5} size="small">
              <Button type="text" icon={<BellOutlined style={{ fontSize: 18 }} />} />
            </Badge>
            <Dropdown
              menu={{ items: userMenuItems, onClick: handleMenuClick }}
              placement="bottomRight"
              arrow
            >
              <Space style={{ cursor: 'pointer' }}>
                <Avatar
                  style={{ backgroundColor: '#1890ff' }}
                  icon={<UserOutlined />}
                />
                <span style={{ fontWeight: 500 }}>管理员</span>
                <DownOutlined style={{ fontSize: 12 }} />
              </Space>
            </Dropdown>
          </Space>
        </Header>

        {/* 页面内容 */}
        <Content
          style={{
            margin: 24,
            padding: 24,
            background: colorBgContainer,
            borderRadius: borderRadiusLG,
            minHeight: 280,
          }}
        >
          {children}
        </Content>

        {/* 页脚 */}
        <Footer style={{ textAlign: 'center', padding: '12px 50px' }}>
          <Text type="secondary" style={{ fontSize: 12 }}>
            澳小遊后台管理系统 ©2026 Created by 澳小遊团队
          </Text>
        </Footer>
      </Layout>
    </Layout>
  );
};

export default DefaultLayout;
