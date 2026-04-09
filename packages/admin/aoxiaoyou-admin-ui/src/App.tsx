import React, { useEffect, useState } from 'react';
import { Navigate, Outlet, Route, Routes } from 'react-router-dom';
import { Spin } from 'antd';
import DefaultLayout from './layouts/DefaultLayout';
import Login from './pages/Login';
import Dashboard from './pages/Dashboard';
import POIManagement from './pages/POIManagement';
import TestAccount from './pages/TestAccount';
import UserManagement from './pages/UserManagement';
import StorylineManagement from './pages/StorylineManagement';
import OperationsManagement from './pages/OperationsManagement';
import SystemManagement from './pages/SystemManagement';
import ModulePlaceholder from './pages/ModulePlaceholder';
import CityManagement from './pages/MapSpace/CityManagement';
import IndoorBuildingManagement from './pages/MapSpace/IndoorBuildingManagement';
import MapTileManagement from './pages/MapSpace/MapTileManagement';
import AiCapabilityCenter from './pages/MapSpace/AiCapabilityCenter';
import CollectibleManagement from './pages/Collectibles/CollectibleManagement';
import BadgeManagement from './pages/Collectibles/BadgeManagement';
import AdminUsersManagement from './pages/System/AdminUsersManagement';
import RolePermissionManagement from './pages/System/RolePermissionManagement';
import './App.css';
import { getCurrentAdmin } from './services/api';
import { clearAdminAuth, getAdminToken, getAdminUser, setAdminToken, setAdminUser, setRefreshToken } from './utils/auth';
import { useAuthStore } from './stores/auth';

const Loading = () => (
  <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}>
    <Spin size="large" tip="加载中..." />
  </div>
);

const ProtectedRoute: React.FC = () => {
  const { user, setUser } = useAuthStore();
  const [checking, setChecking] = useState(true);

  useEffect(() => {
    const bootstrap = async () => {
      const token = getAdminToken();
      const cachedUser = getAdminUser();
      if (!token) {
        setChecking(false);
        return;
      }
      if (cachedUser) {
        setUser(cachedUser);
      }
      try {
        const response = await getCurrentAdmin();
        if (response.success && response.data) {
          setAdminToken(response.data.token || token);
          if (response.data.refreshToken) {
            setRefreshToken(response.data.refreshToken);
          }
          setAdminUser(response.data.user);
          setUser(response.data.user);
        }
      } catch {
        clearAdminAuth();
        setUser(null);
      } finally {
        setChecking(false);
      }
    };
    bootstrap();
  }, [setUser]);

  if (checking) {
    return <Loading />;
  }

  if (!getAdminToken() || !user) {
    return <Navigate to="/login" replace />;
  }

  return <Outlet />;
};

function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route element={<ProtectedRoute />}>
        <Route path="/" element={<DefaultLayout />}>
          <Route index element={<Navigate to="/dashboard" replace />} />
          <Route path="dashboard" element={<Dashboard />} />

          <Route path="space/cities" element={<CityManagement />} />
          <Route path="space/map-tiles" element={<MapTileManagement />} />
          <Route path="space/pois" element={<POIManagement />} />
          <Route path="space/indoor-buildings" element={<IndoorBuildingManagement />} />
          <Route path="space/ai-navigation" element={<AiCapabilityCenter />} />

          <Route path="content/storylines" element={<StorylineManagement />} />
          <Route path="content/chapters" element={<Navigate to="/content/storylines" replace />} />
          <Route path="content/campaigns" element={<OperationsManagement />} />
          <Route path="content/media" element={<ModulePlaceholder title="媒体资源库" subTitle="图片、音频、视频与引用关系管理" tags={["媒体库", "引用关系", "多语言"]} description="这里将承接统一媒体资源池、引用追踪、版本与替换流程。" todoItems={["媒体资源主数据","资源引用统计","多语言媒体映射","资源版本与失效策略"]} />} />

          <Route path="collection/rewards" element={<SystemManagement />} />
          <Route path="collection/collectibles" element={<CollectibleManagement />} />
          <Route path="collection/badges" element={<BadgeManagement />} />

          <Route path="users/progress" element={<UserManagement />} />
          <Route path="users/story-progress" element={<ModulePlaceholder title="用户进度与轨迹" subTitle="城市、故事线、收集与触发记录" tags={["用户进度", "轨迹", "客服视图"]} description="这里将承载用户城市进度、总探索进度、故事线进度、收集物进度和触发日志。" todoItems={["user_city_progress / user_story_progress","用户触发记录与客服视图","城市探索漏斗","AI 请求轨迹与排障"]} />} />

          <Route path="ops/test-console" element={<TestAccount />} />
          <Route path="ops/activities" element={<OperationsManagement />} />
          <Route path="ops/sandbox" element={<ModulePlaceholder title="测试数据与沙盒" subTitle="批量造数、导入导出与测试场景模板" tags={["沙盒", "批量造数", "导入导出"]} description="这里将形成正式测试沙盒能力，用于多城市、室内、故事分支与 AI 场景验证。" todoItems={["测试数据模板","批量导入导出","场景快照与恢复","回归测试用例入口"]} />} />

          <Route path="system/admins" element={<AdminUsersManagement />} />
          <Route path="system/roles" element={<RolePermissionManagement />} />
          <Route path="system/configs" element={<SystemManagement />} />
          <Route path="system/audit" element={<SystemManagement />} />
        </Route>
      </Route>
      <Route path="*" element={<Navigate to="/dashboard" replace />} />
    </Routes>
  );
}

export default App;


