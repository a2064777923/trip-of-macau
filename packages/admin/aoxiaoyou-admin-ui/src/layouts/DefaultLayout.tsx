import React, { useEffect, useMemo, useState } from 'react';
import { Link, Outlet, useLocation, useNavigate } from 'react-router-dom';
import { Avatar, Button, Dropdown, Layout, Menu, Space, Typography, theme } from 'antd';
import type { MenuProps } from 'antd';
import {
  ApartmentOutlined,
  AuditOutlined,
  ClusterOutlined,
  DashboardOutlined,
  DatabaseOutlined,
  DeploymentUnitOutlined,
  EnvironmentOutlined,
  GlobalOutlined,
  LogoutOutlined,
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  NotificationOutlined,
  RocketOutlined,
  SafetyCertificateOutlined,
  SettingOutlined,
  TeamOutlined,
  ToolOutlined,
  TrophyOutlined,
  UserOutlined,
} from '@ant-design/icons';
import brandLogo from '@shared-client-assets/logo.png';
import { clearAdminAuth } from '../utils/auth';
import { useAuthStore } from '../stores/auth';

const { Header, Sider, Content } = Layout;
const { Text, Title } = Typography;
const rootMenuKeys = ['space', 'ai', 'content', 'collection', 'users', 'ops', 'system'] as const;

const roleLabelMap: Record<string, string> = {
  SUPER_ADMIN: '超級管理員',
  CONTENT_ADMIN: '內容管理員',
  OPS_ADMIN: '營運管理員',
  TEST_ADMIN: '測試管理員',
};

function formatRoles(roles?: string[]) {
  if (!roles?.length) {
    return '系統管理員';
  }
  return roles.map((role) => roleLabelMap[role] || role).join(' / ');
}

function resolveSelectedKey(pathname: string) {
  const routeKeys = [
    '/dashboard',
    '/space/cities',
    '/space/map-tiles',
    '/space/pois',
    '/space/indoor-buildings',
    '/space/indoor-rules',
    '/ai',
    '/ai/providers',
    '/ai/models',
    '/ai/voices',
    '/ai/capabilities',
    '/ai/creative-studio',
    '/ai/observability',
    '/ai/settings',
    '/content/storylines',
    '/content/chapters',
    '/content/chapters/workbench',
    '/content/blocks',
    '/content/experience',
    '/content/experience/templates',
    '/content/experience/bindings',
    '/content/experience/overrides',
    '/content/experience/exploration',
    '/content/experience/governance',
    '/content/campaigns',
    '/content/media',
    '/collection/rewards',
    '/collection/redeemable-prizes',
    '/collection/game-rewards',
    '/collection/honors',
    '/collection/rule-center',
    '/collection/collectibles',
    '/collection/badges',
    '/users/progress',
    '/users/story-progress',
    '/ops/test-console',
    '/ops/activities',
    '/ops/sandbox',
    '/system/admins',
    '/system/roles',
    '/system/configs',
    '/system/audit',
  ];

  const sorted = routeKeys.sort((left, right) => right.length - left.length);
  return sorted.find((routeKey) => pathname === routeKey || pathname.startsWith(`${routeKey}/`)) || '/dashboard';
}

const DefaultLayout: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { user, setUser } = useAuthStore();
  const [collapsed, setCollapsed] = useState(false);
  const {
    token: { colorBgContainer, borderRadiusLG },
  } = theme.useToken();

  const menuItems = useMemo<MenuProps['items']>(
    () => [
      {
        key: '/dashboard',
        icon: <DashboardOutlined />,
        label: <Link to="/dashboard">首頁儀表盤</Link>,
      },
      {
        key: 'space',
        icon: <GlobalOutlined />,
        label: '地圖與空間管理',
        children: [
          {
            key: '/space/cities',
            icon: <DeploymentUnitOutlined />,
            label: <Link to="/space/cities">城市與子地圖</Link>,
          },
          {
            key: '/space/map-tiles',
            icon: <EnvironmentOutlined />,
            label: <Link to="/space/map-tiles">瓦片地圖</Link>,
          },
          {
            key: '/space/pois',
            icon: <ClusterOutlined />,
            label: <Link to="/space/pois">POI 管理</Link>,
          },
          {
            key: '/space/indoor-buildings',
            icon: <ApartmentOutlined />,
            label: <Link to="/space/indoor-buildings">室內建築與小地圖</Link>,
          },
          {
            key: '/space/indoor-rules',
            icon: <AuditOutlined />,
            label: <Link to="/space/indoor-rules">互動規則治理中心</Link>,
          },
        ],
      },
      {
        key: 'ai',
        icon: <RocketOutlined />,
        label: 'AI 能力中心',
        children: [
          { key: '/ai', icon: <DashboardOutlined />, label: <Link to="/ai">總覽</Link> },
          {
            key: '/ai/providers',
            icon: <SafetyCertificateOutlined />,
            label: <Link to="/ai/providers">供應商與金鑰</Link>,
          },
          {
            key: '/ai/models',
            icon: <DatabaseOutlined />,
            label: <Link to="/ai/models">模型與端點庫</Link>,
          },
          {
            key: '/ai/voices',
            icon: <NotificationOutlined />,
            label: <Link to="/ai/voices">聲音與音色工作台</Link>,
          },
          {
            key: '/ai/capabilities',
            icon: <ClusterOutlined />,
            label: <Link to="/ai/capabilities">能力配置</Link>,
          },
          {
            key: '/ai/creative-studio',
            icon: <ToolOutlined />,
            label: <Link to="/ai/creative-studio">AI 創作工作台</Link>,
          },
          {
            key: '/ai/observability',
            icon: <AuditOutlined />,
            label: <Link to="/ai/observability">監控與用量</Link>,
          },
          {
            key: '/ai/settings',
            icon: <SettingOutlined />,
            label: <Link to="/ai/settings">治理設定</Link>,
          },
        ],
      },
      {
        key: 'content',
        icon: <NotificationOutlined />,
        label: '故事與內容管理',
        children: [
          {
            key: '/content/storylines',
            icon: <ApartmentOutlined />,
            label: <Link to="/content/storylines">故事線管理</Link>,
          },
          {
            key: '/content/chapters',
            icon: <ClusterOutlined />,
            label: <Link to="/content/chapters">章節管理</Link>,
          },
          {
            key: '/content/blocks',
            icon: <DatabaseOutlined />,
            label: <Link to="/content/blocks">內容積木庫</Link>,
          },
          {
            key: '/content/experience',
            icon: <RocketOutlined />,
            label: <Link to="/content/experience">體驗流程工作台</Link>,
          },
          {
            key: '/content/experience/templates',
            icon: <ToolOutlined />,
            label: <Link to="/content/experience/templates">互動與任務模板庫</Link>,
          },
          {
            key: '/content/experience/governance',
            icon: <AuditOutlined />,
            label: <Link to="/content/experience/governance">體驗規則治理中心</Link>,
          },
          {
            key: '/content/experience/exploration',
            icon: <SafetyCertificateOutlined />,
            label: <Link to="/content/experience/exploration">探索元素與進度規則</Link>,
          },
          {
            key: '/content/campaigns',
            icon: <NotificationOutlined />,
            label: <Link to="/content/campaigns">任務與活動</Link>,
          },
          {
            key: '/content/media',
            icon: <EnvironmentOutlined />,
            label: <Link to="/content/media">媒體資源</Link>,
          },
        ],
      },
      {
        key: 'collection',
        icon: <TrophyOutlined />,
        label: '收集物與獎勵',
        children: [
          {
            key: '/collection/redeemable-prizes',
            icon: <TrophyOutlined />,
            label: <Link to="/collection/redeemable-prizes">兌換獎勵物品管理</Link>,
          },
          {
            key: '/collection/game-rewards',
            icon: <RocketOutlined />,
            label: <Link to="/collection/game-rewards">遊戲內獎勵配置</Link>,
          },
          {
            key: '/collection/honors',
            icon: <SafetyCertificateOutlined />,
            label: <Link to="/collection/honors">榮譽與稱號</Link>,
          },
          {
            key: '/collection/rule-center',
            icon: <AuditOutlined />,
            label: <Link to="/collection/rule-center">獎勵規則中心</Link>,
          },
          {
            key: '/collection/collectibles',
            icon: <DatabaseOutlined />,
            label: <Link to="/collection/collectibles">收集物管理</Link>,
          },
        ],
      },
      {
        key: 'users',
        icon: <TeamOutlined />,
        label: '用戶與進度管理',
        children: [
          {
            key: '/users/progress',
            icon: <TeamOutlined />,
            label: <Link to="/users/progress">用戶管理</Link>,
          },
          {
            key: '/users/story-progress',
            icon: <UserOutlined />,
            label: <Link to="/users/story-progress">用戶進度與軌跡</Link>,
          },
        ],
      },
      {
        key: 'ops',
        icon: <ToolOutlined />,
        label: '測試與營運管理',
        children: [
          {
            key: '/ops/test-console',
            icon: <ToolOutlined />,
            label: <Link to="/ops/test-console">測試控制台</Link>,
          },
          {
            key: '/ops/activities',
            icon: <NotificationOutlined />,
            label: <Link to="/ops/activities">營運活動</Link>,
          },
          {
            key: '/ops/sandbox',
            icon: <DatabaseOutlined />,
            label: <Link to="/ops/sandbox">測試資源與沙盒</Link>,
          },
        ],
      },
      {
        key: 'system',
        icon: <SettingOutlined />,
        label: '系統與權限管理',
        children: [
          {
            key: '/system/admins',
            icon: <UserOutlined />,
            label: <Link to="/system/admins">管理員帳號</Link>,
          },
          {
            key: '/system/roles',
            icon: <SafetyCertificateOutlined />,
            label: <Link to="/system/roles">角色與權限</Link>,
          },
          {
            key: '/system/configs',
            icon: <SettingOutlined />,
            label: <Link to="/system/configs">系統設定</Link>,
          },
          {
            key: '/system/audit',
            icon: <DatabaseOutlined />,
            label: <Link to="/system/audit">審計與日誌</Link>,
          },
        ],
      },
    ],
    [],
  );

  const selectedKey = useMemo(() => resolveSelectedKey(location.pathname), [location.pathname]);
  const routeOpenKeys = useMemo(() => {
    const selected = resolveSelectedKey(location.pathname);
    if (selected.startsWith('/space/')) {
      return ['space'];
    }
    if (selected.startsWith('/ai')) {
      return ['ai'];
    }
    if (selected.startsWith('/content/')) {
      return ['content'];
    }
    if (selected.startsWith('/collection/')) {
      return ['collection'];
    }
    if (selected.startsWith('/users/')) {
      return ['users'];
    }
    if (selected.startsWith('/ops/')) {
      return ['ops'];
    }
    if (selected.startsWith('/system/')) {
      return ['system'];
    }
    return [];
  }, [location.pathname]);
  const [openKeys, setOpenKeys] = useState<string[]>(routeOpenKeys);

  useEffect(() => {
    setOpenKeys((previous) => {
      const next = previous.filter((key) => rootMenuKeys.includes(key as (typeof rootMenuKeys)[number]));
      for (const routeKey of routeOpenKeys) {
        if (!next.includes(routeKey)) {
          next.unshift(routeKey);
        }
      }
      return next;
    });
  }, [routeOpenKeys]);

  const userMenuItems: MenuProps['items'] = [
    {
      key: 'profile',
      icon: <UserOutlined />,
      label: '管理員帳號',
    },
    {
      key: 'settings',
      icon: <SettingOutlined />,
      label: '系統設定',
    },
    {
      type: 'divider',
    },
    {
      key: 'logout',
      icon: <LogoutOutlined />,
      label: '登出',
      danger: true,
    },
  ];

  const handleUserMenuClick: MenuProps['onClick'] = ({ key }) => {
    if (key === 'logout') {
      clearAdminAuth();
      setUser(null);
      navigate('/login');
      return;
    }

    if (key === 'profile') {
      navigate('/system/admins');
      return;
    }

    if (key === 'settings') {
      navigate('/system/configs');
    }
  };

  return (
    <Layout style={{ minHeight: '100vh', background: 'transparent' }}>
      <Sider
        collapsible
        trigger={null}
        collapsed={collapsed}
        width={288}
        theme="light"
        style={{
          margin: 16,
          borderRadius: 24,
          overflow: 'hidden',
          boxShadow: '0 20px 60px rgba(91, 102, 181, 0.12)',
          background: '#ffffff',
        }}
      >
        <div
          style={{
            minHeight: 104,
            display: 'flex',
            alignItems: 'center',
            justifyContent: collapsed ? 'center' : 'flex-start',
            padding: collapsed ? '20px 12px' : '22px 22px 18px',
            gap: 14,
            background:
              'linear-gradient(180deg, rgba(124, 92, 255, 0.08) 0%, rgba(124, 92, 255, 0.02) 100%)',
          }}
        >
          <img
            src={brandLogo}
            alt="Trip of Macau"
            style={{ width: 44, height: 44, borderRadius: 14, objectFit: 'cover', flexShrink: 0 }}
          />
          {!collapsed ? (
            <div style={{ minWidth: 0 }}>
              <Title level={5} style={{ margin: 0 }}>
                澳小遊後台
              </Title>
              <Text type="secondary" style={{ fontSize: 12 }}>
                內容、空間、AI 與營運的統一控制台
              </Text>
            </div>
          ) : null}
        </div>

        <Menu
          mode="inline"
          selectedKeys={[selectedKey]}
          openKeys={openKeys}
          onOpenChange={(keys) =>
            setOpenKeys(keys.filter((key) => rootMenuKeys.includes(key as (typeof rootMenuKeys)[number])))
          }
          items={menuItems}
          style={{ borderInlineEnd: 'none', padding: '8px 12px 16px' }}
        />
      </Sider>

      <Layout style={{ background: 'transparent' }}>
        <Header
          style={{
            margin: '16px 16px 0 0',
            padding: '0 24px',
            minHeight: 86,
            background: colorBgContainer,
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
            borderRadius: 24,
            boxShadow: '0 16px 40px rgba(91, 102, 181, 0.08)',
          }}
        >
          <Space size="middle" align="center">
            <Button
              type="text"
              icon={collapsed ? <MenuUnfoldOutlined /> : <MenuFoldOutlined />}
              onClick={() => setCollapsed((previous) => !previous)}
            />
            <div>
              <Text strong style={{ display: 'block', fontSize: 16 }}>
                澳小遊管理系統
              </Text>
              <Text type="secondary" style={{ fontSize: 12 }}>
                管理小程序內容、空間配置、媒體資產與 AI 能力
              </Text>
            </div>
          </Space>

          <Dropdown menu={{ items: userMenuItems, onClick: handleUserMenuClick }}>
            <Space style={{ cursor: 'pointer' }} size="middle">
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
              minHeight: 'calc(100vh - 118px)',
              background: colorBgContainer,
              borderRadius: borderRadiusLG + 10,
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
