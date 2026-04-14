import React, { useMemo, useState } from 'react';
import { Outlet, Link, useLocation, useNavigate } from 'react-router-dom';
import { Layout, Menu, theme, Avatar, Dropdown, Space, Typography, Button } from 'antd';
import {
  DashboardOutlined,
  EnvironmentOutlined,
  ToolOutlined,
  UserOutlined,
  LogoutOutlined,
  SettingOutlined,
  ApartmentOutlined,
  TeamOutlined,
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  NotificationOutlined,
  SafetyCertificateOutlined,
  GlobalOutlined,
  ClusterOutlined,
  DeploymentUnitOutlined,
  DatabaseOutlined,
  TrophyOutlined,
} from '@ant-design/icons';
import brandLogo from '@shared-client-assets/logo.png';
import { clearAdminAuth } from '../utils/auth';
import { useAuthStore } from '../stores/auth';

const { Header, Sider, Content } = Layout;
const { Text } = Typography;

const roleLabelMap: Record<string, string> = {
  SUPER_ADMIN: '超級管理員',
  CONTENT_ADMIN: '內容管理員',
  OPS_ADMIN: '營運管理員',
  TEST_ADMIN: '測試管理員',
};

function formatRoles(roles?: string[]) {
  if (!roles?.length) {
    return '超級管理員';
  }
  return roles.map((role) => roleLabelMap[role] || role).join(' / ');
}

const DefaultLayout: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { user, setUser } = useAuthStore();
  const [collapsed, setCollapsed] = useState(false);
  const {
    token: { colorBgContainer, borderRadiusLG },
  } = theme.useToken();

  const menuItems = useMemo(
    () => [
      {
        key: '/dashboard',
        icon: <DashboardOutlined />,
        label: <Link to="/dashboard">總覽儀表板</Link>,
      },
      {
        key: 'space',
        icon: <GlobalOutlined />,
        label: '地圖與空間管理',
        children: [
          { key: '/space/cities', icon: <DeploymentUnitOutlined />, label: <Link to="/space/cities">城市與子地圖</Link> },
          { key: '/space/map-tiles', icon: <EnvironmentOutlined />, label: <Link to="/space/map-tiles">瓦片地圖</Link> },
          { key: '/space/pois', icon: <ClusterOutlined />, label: <Link to="/space/pois">POI 管理</Link> },
          { key: '/space/indoor-buildings', icon: <ApartmentOutlined />, label: <Link to="/space/indoor-buildings">室內建築與小地圖</Link> },
          { key: '/space/ai-navigation', icon: <SafetyCertificateOutlined />, label: <Link to="/space/ai-navigation">AI 能力中心</Link> },
        ],
      },
      {
        key: 'content',
        icon: <NotificationOutlined />,
        label: '故事與內容管理',
        children: [
          { key: '/content/storylines', icon: <ApartmentOutlined />, label: <Link to="/content/storylines">故事線</Link> },
          { key: '/content/chapters', icon: <ClusterOutlined />, label: <Link to="/content/chapters">章節編排</Link> },
          { key: '/content/campaigns', icon: <NotificationOutlined />, label: <Link to="/content/campaigns">任務與活動</Link> },
          { key: '/content/media', icon: <DatabaseOutlined />, label: <Link to="/content/media">媒體資源</Link> },
        ],
      },
      {
        key: 'collection',
        icon: <TrophyOutlined />,
        label: '收集物與獎勵',
        children: [
          { key: '/collection/rewards', icon: <TrophyOutlined />, label: <Link to="/collection/rewards">獎勵配置</Link> },
          { key: '/collection/collectibles', icon: <DatabaseOutlined />, label: <Link to="/collection/collectibles">收集物管理</Link> },
          { key: '/collection/badges', icon: <SafetyCertificateOutlined />, label: <Link to="/collection/badges">徽章與勳章</Link> },
        ],
      },
      {
        key: 'users',
        icon: <TeamOutlined />,
        label: '用戶與進度管理',
        children: [
          { key: '/users/progress', icon: <TeamOutlined />, label: <Link to="/users/progress">用戶管理</Link> },
          { key: '/users/story-progress', icon: <UserOutlined />, label: <Link to="/users/story-progress">用戶進度與軌跡</Link> },
        ],
      },
      {
        key: 'ops',
        icon: <ToolOutlined />,
        label: '測試與營運管理',
        children: [
          { key: '/ops/test-console', icon: <ToolOutlined />, label: <Link to="/ops/test-console">測試控制台</Link> },
          { key: '/ops/activities', icon: <NotificationOutlined />, label: <Link to="/ops/activities">營運活動</Link> },
          { key: '/ops/sandbox', icon: <DatabaseOutlined />, label: <Link to="/ops/sandbox">測試資料與沙盒</Link> },
        ],
      },
      {
        key: 'system',
        icon: <SettingOutlined />,
        label: '系統與權限管理',
        children: [
          { key: '/system/admins', icon: <UserOutlined />, label: <Link to="/system/admins">管理員帳號</Link> },
          { key: '/system/roles', icon: <SafetyCertificateOutlined />, label: <Link to="/system/roles">角色與權限</Link> },
          { key: '/system/configs', icon: <SettingOutlined />, label: <Link to="/system/configs">系統配置</Link> },
          { key: '/system/audit', icon: <DatabaseOutlined />, label: <Link to="/system/audit">審計與日誌</Link> },
        ],
      },
    ],
    [],
  );

  const selectedKey = useMemo(() => {
    const path = location.pathname;
    return [path === '/' ? '/dashboard' : path];
  }, [location.pathname]);

  const openKeys = useMemo(() => {
    const segments = location.pathname.split('/').filter(Boolean);
    return segments.length > 0 ? [segments[0]] : ['dashboard'];
  }, [location.pathname]);

  const userMenuItems = [
    {
      key: 'profile',
      icon: <UserOutlined />,
      label: '管理員帳號',
    },
    {
      key: 'settings',
      icon: <SettingOutlined />,
      label: '系統配置',
    },
    {
      type: 'divider' as const,
    },
    {
      key: 'logout',
      icon: <LogoutOutlined />,
      label: '登出',
      danger: true,
    },
  ];

  const handleMenuClick = (e: { key: string }) => {
    if (e.key === 'logout') {
      clearAdminAuth();
      setUser(null);
      navigate('/login');
      return;
    }
    if (e.key === 'profile') {
      navigate('/system/admins');
      return;
    }
    if (e.key === 'settings') {
      navigate('/system/configs');
    }
  };

  return (
    <Layout style={{ minHeight: '100vh', background: 'transparent' }}>
      <Sider
        collapsible
        trigger={null}
        collapsed={collapsed}
        width={268}
        theme="light"
        style={{
          margin: 16,
          borderRadius: 20,
          overflow: 'hidden',
          boxShadow: '0 20px 60px rgba(91, 102, 181, 0.12)',
          background: '#ffffff',
        }}
      >
        <div
          style={{
            minHeight: 88,
            display: 'flex',
            alignItems: 'center',
            justifyContent: collapsed ? 'center' : 'flex-start',
            color: '#2b2f42',
            fontSize: collapsed ? 18 : 20,
            fontWeight: 700,
            padding: collapsed ? '18px 8px' : '18px 20px',
            gap: 12,
          }}
        >
          <img
            src={brandLogo}
            alt="Trip of Macau"
            style={{ width: 40, height: 40, borderRadius: 12, objectFit: 'cover', flexShrink: 0 }}
          />
          {!collapsed && (
            <div>
              <div>澳小遊後台</div>
              <Text type="secondary" style={{ fontSize: 12 }}>
                多城市內容、地圖與營運一體化管理
              </Text>
            </div>
          )}
        </div>
        <Menu
          mode="inline"
          selectedKeys={selectedKey}
          defaultOpenKeys={openKeys}
          items={menuItems}
          style={{ borderInlineEnd: 'none', padding: '0 10px 12px' }}
        />
      </Sider>

      <Layout style={{ background: 'transparent' }}>
        <Header
          style={{
            margin: '16px 16px 0 0',
            padding: '0 24px',
            background: colorBgContainer,
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
            borderRadius: 20,
            boxShadow: '0 16px 40px rgba(91, 102, 181, 0.08)',
          }}
        >
          <Space>
            <Button
              type="text"
              icon={collapsed ? <MenuUnfoldOutlined /> : <MenuFoldOutlined />}
              onClick={() => setCollapsed((prev) => !prev)}
            />
            <div>
              <Text strong style={{ display: 'block', fontSize: 16 }}>
                澳小遊後台管理系統
              </Text>
              <Text type="secondary" style={{ fontSize: 12 }}>
                管理小程序內容、設定、媒體與營運資料
              </Text>
            </div>
          </Space>
          <Dropdown
            menu={{
              items: userMenuItems,
              onClick: handleMenuClick,
            }}
          >
            <Space style={{ cursor: 'pointer' }}>
              <Avatar style={{ backgroundColor: '#7c5cff' }} icon={<UserOutlined />} />
              <div style={{ lineHeight: 1.2 }}>
                <Text strong>{user?.realName || user?.username || '管理員'}</Text>
                <br />
                <Text type="secondary" style={{ fontSize: 12 }}>
                  {formatRoles(user?.roles)}
                </Text>
              </div>
            </Space>
          </Dropdown>
        </Header>

        <Content style={{ margin: '16px 16px 16px 0' }}>
          <div
            style={{
              padding: 24,
              minHeight: 'calc(100vh - 112px)',
              background: colorBgContainer,
              borderRadius: borderRadiusLG + 8,
              boxShadow: '0 20px 60px rgba(91, 102, 181, 0.08)',
            }}
          >
            <Outlet />
          </div>
        </Content>
      </Layout>
    </Layout>
  );
};

export default DefaultLayout;
