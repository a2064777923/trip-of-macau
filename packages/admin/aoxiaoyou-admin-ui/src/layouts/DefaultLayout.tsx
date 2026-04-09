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
import { clearAdminAuth } from '../utils/auth';
import { useAuthStore } from '../stores/auth';

const { Header, Sider, Content } = Layout;
const { Text } = Typography;

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
        label: <Link to="/dashboard">仪表盘</Link>,
      },
      {
        key: 'space',
        icon: <GlobalOutlined />,
        label: '地图与空间管理',
        children: [
          { key: '/space/cities', icon: <DeploymentUnitOutlined />, label: <Link to="/space/cities">城市管理</Link> },
          { key: '/space/map-tiles', icon: <EnvironmentOutlined />, label: <Link to="/space/map-tiles">城市瓦片地图</Link> },
          { key: '/space/pois', icon: <ClusterOutlined />, label: <Link to="/space/pois">POI 管理</Link> },
          { key: '/space/indoor-buildings', icon: <ApartmentOutlined />, label: <Link to="/space/indoor-buildings">室内建筑与楼层</Link> },
          { key: '/space/ai-navigation', icon: <SafetyCertificateOutlined />, label: <Link to="/space/ai-navigation">AI 导航配置</Link> },
        ],
      },
      {
        key: 'content',
        icon: <NotificationOutlined />,
        label: '故事与内容管理',
        children: [
          { key: '/content/storylines', icon: <ApartmentOutlined />, label: <Link to="/content/storylines">故事线</Link> },
          { key: '/content/chapters', icon: <ClusterOutlined />, label: <Link to="/content/chapters">章节编排</Link> },
          { key: '/content/campaigns', icon: <NotificationOutlined />, label: <Link to="/content/campaigns">任务与活动</Link> },
          { key: '/content/media', icon: <DatabaseOutlined />, label: <Link to="/content/media">媒体资源</Link> },
        ],
      },
      {
        key: 'collection',
        icon: <TrophyOutlined />,
        label: '收集与激励管理',
        children: [
          { key: '/collection/rewards', icon: <TrophyOutlined />, label: <Link to="/collection/rewards">奖励配置</Link> },
          { key: '/collection/collectibles', icon: <DatabaseOutlined />, label: <Link to="/collection/collectibles">收集物与系列</Link> },
          { key: '/collection/badges', icon: <SafetyCertificateOutlined />, label: <Link to="/collection/badges">徽章与奖章</Link> },
        ],
      },
      {
        key: 'users',
        icon: <TeamOutlined />,
        label: '用户与进度管理',
        children: [
          { key: '/users/progress', icon: <TeamOutlined />, label: <Link to="/users/progress">用户管理</Link> },
          { key: '/users/story-progress', icon: <UserOutlined />, label: <Link to="/users/story-progress">用户进度与轨迹</Link> },
        ],
      },
      {
        key: 'ops',
        icon: <ToolOutlined />,
        label: '测试与运营管理',
        children: [
          { key: '/ops/test-console', icon: <ToolOutlined />, label: <Link to="/ops/test-console">测试控制台</Link> },
          { key: '/ops/activities', icon: <NotificationOutlined />, label: <Link to="/ops/activities">运营活动</Link> },
          { key: '/ops/sandbox', icon: <DatabaseOutlined />, label: <Link to="/ops/sandbox">测试数据与沙盒</Link> },
        ],
      },
      {
        key: 'system',
        icon: <SettingOutlined />,
        label: '系统与权限管理',
        children: [
          { key: '/system/admins', icon: <UserOutlined />, label: <Link to="/system/admins">管理员账号</Link> },
          { key: '/system/roles', icon: <SafetyCertificateOutlined />, label: <Link to="/system/roles">角色与权限</Link> },
          { key: '/system/configs', icon: <SettingOutlined />, label: <Link to="/system/configs">系统配置</Link> },
          { key: '/system/audit', icon: <DatabaseOutlined />, label: <Link to="/system/audit">审计与日志</Link> },
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
      label: '个人设置',
    },
    {
      key: 'settings',
      icon: <SettingOutlined />,
      label: '系统设置',
    },
    {
      type: 'divider' as const,
    },
    {
      key: 'logout',
      icon: <LogoutOutlined />,
      label: '退出登录',
      danger: true,
    },
  ];

  const handleMenuClick = (e: { key: string }) => {
    if (e.key === 'logout') {
      clearAdminAuth();
      setUser(null);
      navigate('/login');
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
            padding: collapsed ? '0 8px' : '18px 20px',
            gap: 12,
          }}
        >
          <div
            style={{
              width: 40,
              height: 40,
              borderRadius: 12,
              display: 'grid',
              placeItems: 'center',
              background: 'linear-gradient(135deg, #7c5cff, #56ccf2)',
              color: '#fff',
            }}
          >
            澳
          </div>
          {!collapsed && (
            <div>
              <div>澳小遊后台</div>
              <Text type="secondary" style={{ fontSize: 12 }}>
                多城市内容、空间、运营一体化平台
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
                澳小遊后台管理系统
              </Text>
              <Text type="secondary" style={{ fontSize: 12 }}>
                Trip of Macau Admin Platform
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
                <Text strong>{user?.realName || user?.username || '管理员'}</Text>
                <br />
                <Text type="secondary" style={{ fontSize: 12 }}>
                  {(user?.roles || []).join(' / ') || 'SUPER_ADMIN'}
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
