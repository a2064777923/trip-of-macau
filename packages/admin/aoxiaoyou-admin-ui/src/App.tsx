import { Routes, Route, Navigate } from 'react-router-dom';
import { Layout } from 'antd';
import { UserOutlined, SettingOutlined, ToolOutlined } from '@ant-design/icons';
import { Content } from 'antd/es/layout/layout';

// 頁面組件 - 臨時佔位
const Dashboard = () => <div style={{ padding: 24 }}><h1>數據統計</h1></div>;
const UserManage = () => <div style={{ padding: 24 }}><h1>用戶管理</h1></div>;
const POIManage = () => <div style={{ padding: 24 }}><h1>POI 管理</h1></div>;
const StoryManage = () => <div style={{ padding: 24 }}><h1>故事線管理</h1></div>;
const TestTools = () => <div style={{ padding: 24 }}><h1>測試工具</h1></div>;
const SystemSettings = () => <div style={{ padding: 24 }}><h1>系統設置</h1></div>;
const Login = () => <div style={{ padding: 24 }}><h1>登錄頁</h1></div>;

const menuItems = [
  { key: '/dashboard', icon: <UserOutlined />, label: '數據統計' },
  { key: '/users', icon: <UserOutlined />, label: '用戶管理' },
  { key: '/pois', icon: <SettingOutlined />, label: 'POI 管理' },
  { key: '/stories', icon: <SettingOutlined />, label: '故事線管理' },
  { key: '/test-tools', icon: <ToolOutlined />, label: '測試工具' },
  { key: '/system', icon: <SettingOutlined />, label: '系統設置' },
];

function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route path="/" element={
        <Layout style={{ minHeight: '100vh' }}>
          <Layout.Sider breakpoint="lg" collapsedWidth="0">
            <div style={{ height: 64, display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#fff', fontSize: 18, fontWeight: 'bold' }}>
              澳小遊後台
            </div>
            {/* <Menu theme="dark" mode="inline" defaultSelectedKeys={['/dashboard']} items={menuItems} /> */}
          </Layout.Sider>
          <Layout>
            <Content style={{ margin: 24 }}>
              <Routes>
                <Route path="/dashboard" element={<Dashboard />} />
                <Route path="/users" element={<UserManage />} />
                <Route path="/pois" element={<POIManage />} />
                <Route path="/stories" element={<StoryManage />} />
                <Route path="/test-tools" element={<TestTools />} />
                <Route path="/system" element={<SystemSettings />} />
                <Route path="*" element={<Navigate to="/dashboard" replace />} />
              </Routes>
            </Content>
          </Layout>
        </Layout>
      } />
    </Routes>
  );
}

export default App;
