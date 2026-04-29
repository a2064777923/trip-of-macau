import React, { useEffect, useState } from 'react';
import { Navigate, Outlet, Route, Routes } from 'react-router-dom';
import { Space, Spin, Typography } from 'antd';
import brandLogo from '@shared-client-assets/logo.png';
import DefaultLayout from './layouts/DefaultLayout';
import Login from './pages/Login';
import Dashboard from './pages/Dashboard';
import POIManagement from './pages/POIManagement';
import POIExperienceWorkbench from './pages/POIExperienceWorkbench';
import TestAccount from './pages/TestAccount';
import UserManagement from './pages/UserManagement';
import UserProgressWorkbench from './pages/UserManagement/UserProgressWorkbench';
import StorylineManagement from './pages/StorylineManagement';
import StorylineModeWorkbench from './pages/StorylineModeWorkbench';
import OperationsManagement from './pages/OperationsManagement';
import ModulePlaceholder from './pages/ModulePlaceholder';
import MediaLibraryManagement from './pages/Content/MediaLibraryManagement';
import StoryChapterWorkbench from './pages/Content/StoryChapterWorkbench';
import StoryContentBlockManagement from './pages/Content/StoryContentBlockManagement';
import StoryMaterialPackageManagement from './pages/Content/StoryMaterialPackageManagement';
import ExperienceOrchestrationWorkbench from './pages/Experience/ExperienceOrchestrationWorkbench';
import CityManagement from './pages/MapSpace/CityManagement';
import IndoorBuildingManagement from './pages/MapSpace/IndoorBuildingManagement';
import IndoorRuleCenter from './pages/MapSpace/IndoorRuleCenter';
import CollectibleManagement from './pages/Collectibles/CollectibleManagement';
import RedeemablePrizeManagement from './pages/Collectibles/RedeemablePrizeManagement';
import GameRewardManagement from './pages/Collectibles/GameRewardManagement';
import HonorManagement from './pages/Collectibles/HonorManagement';
import RewardRuleCenter from './pages/Collectibles/RewardRuleCenter';
import AdminUsersManagement from './pages/System/AdminUsersManagement';
import RolePermissionManagement from './pages/System/RolePermissionManagement';
import SystemManagement from './pages/SystemManagement';
import AiWorkspaceLayout from './pages/AiCapabilityCenter/AiWorkspaceLayout';
import OverviewPage from './pages/AiCapabilityCenter/OverviewPage';
import ProvidersPage from './pages/AiCapabilityCenter/ProvidersPage';
import ModelsPage from './pages/AiCapabilityCenter/ModelsPage';
import CapabilitiesPage from './pages/AiCapabilityCenter/CapabilitiesPage';
import CapabilityDetailPage from './pages/AiCapabilityCenter/CapabilityDetailPage';
import CreativeStudioPage from './pages/AiCapabilityCenter/CreativeStudioPage';
import ObservabilityPage from './pages/AiCapabilityCenter/ObservabilityPage';
import SettingsPage from './pages/AiCapabilityCenter/SettingsPage';
import VoiceWorkbenchPage from './pages/AiCapabilityCenter/VoiceWorkbenchPage';
import './App.css';
import { getCurrentAdmin } from './services/api';
import {
  clearAdminAuth,
  getAdminToken,
  getAdminUser,
  setAdminToken,
  setAdminUser,
  setRefreshToken,
} from './utils/auth';
import { useAuthStore } from './stores/auth';

const { Text } = Typography;

const placeholder = (props: React.ComponentProps<typeof ModulePlaceholder>) => (
  <ModulePlaceholder {...props} />
);

const Loading = () => (
  <div
    style={{
      display: 'flex',
      justifyContent: 'center',
      alignItems: 'center',
      height: '100vh',
      background:
        'radial-gradient(circle at top left, rgba(124, 92, 255, 0.18), transparent 28%), linear-gradient(135deg, #eef2ff 0%, #f7f8ff 100%)',
    }}
  >
    <Space direction="vertical" size="middle" align="center">
      <img
        src={brandLogo}
        alt="Trip of Macau"
        style={{ width: 72, height: 72, borderRadius: 20, objectFit: 'cover' }}
      />
      <Spin size="large" />
      <Text type="secondary">正在載入澳小遊管理工作台...</Text>
    </Space>
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
        } else {
          clearAdminAuth();
          setUser(null);
        }
      } catch {
        clearAdminAuth();
        setUser(null);
      } finally {
        setChecking(false);
      }
    };

    void bootstrap();
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
          <Route path="space/indoor-rules" element={<IndoorRuleCenter />} />
          <Route
            path="space/map-tiles"
            element={placeholder({
              title: '瓦片地圖',
              subTitle: '此分欄預留給未來大地圖瓦片覆蓋圖層管理，與室內建築與小地圖工具分開。',
              tags: ['大地圖', '瓦片覆蓋', '預留模組'],
              description:
                '後續會在這裡管理城市級或子地圖級的瓦片覆蓋層、切片版本、發布路徑與展示策略。現階段室內建築、樓層圖資、標記與 CSV 匯入仍集中在「室內建築與小地圖」。',
              todoItems: ['大地圖瓦片圖層資料模型', '覆蓋層發布與版本管理', '前端地圖疊層渲染對接'],
            })}
          />
          <Route path="space/pois" element={<POIManagement />} />
          <Route path="space/poi-experience" element={<POIExperienceWorkbench />} />
          <Route path="space/pois/:poiId/experience" element={<POIExperienceWorkbench />} />
          <Route path="space/indoor-buildings" element={<IndoorBuildingManagement />} />
          <Route path="space/ai-navigation" element={<Navigate to="/ai" replace />} />

          <Route path="ai" element={<AiWorkspaceLayout />}>
            <Route index element={<OverviewPage />} />
            <Route path="providers" element={<ProvidersPage />} />
            <Route path="models" element={<ModelsPage />} />
            <Route path="voices" element={<VoiceWorkbenchPage />} />
            <Route path="capabilities" element={<CapabilitiesPage />} />
            <Route path="capabilities/:capabilityCode" element={<CapabilityDetailPage />} />
            <Route path="creative-studio" element={<CreativeStudioPage />} />
            <Route path="observability" element={<ObservabilityPage />} />
            <Route path="settings" element={<SettingsPage />} />
          </Route>

          <Route path="content/storylines" element={<StorylineManagement />} />
          <Route path="content/storyline-mode" element={<StorylineModeWorkbench />} />
          <Route path="content/storylines/:storylineId/mode" element={<StorylineModeWorkbench />} />
          <Route path="content/chapters" element={<StoryChapterWorkbench />} />
          <Route path="content/chapters/workbench" element={<StoryChapterWorkbench />} />
          <Route path="content/material-packages" element={<StoryMaterialPackageManagement />} />
          <Route path="content/blocks" element={<StoryContentBlockManagement />} />
          <Route path="content/experience" element={<ExperienceOrchestrationWorkbench initialTab="flows" />} />
          <Route path="content/experience/templates" element={<ExperienceOrchestrationWorkbench initialTab="templates" />} />
          <Route path="content/experience/bindings" element={<ExperienceOrchestrationWorkbench initialTab="bindings" />} />
          <Route path="content/experience/overrides" element={<ExperienceOrchestrationWorkbench initialTab="overrides" />} />
          <Route path="content/experience/exploration" element={<ExperienceOrchestrationWorkbench initialTab="exploration" />} />
          <Route path="content/experience/governance" element={<ExperienceOrchestrationWorkbench initialTab="governance" />} />
          <Route
            path="content/campaigns"
            element={placeholder({
              title: '任務與活動',
              subTitle: '集中管理全局任務、官方活動與發現頁活動卡片。',
              tags: ['任務', '活動', '發現頁'],
              description:
                '後續會在此承接活動主資料、報名欄位、圖文內容、媒體配置、上線下線節奏與置頂策略，不再混用其他控制台頁面。',
              todoItems: ['活動主資料與報名欄位', 'HTML 圖文與媒體編排', '定時上線下線與置頂策略'],
            })}
          />
          <Route path="content/media" element={<MediaLibraryManagement />} />

          <Route
            path="collection/rewards"
            element={<Navigate to="/collection/redeemable-prizes" replace />}
          />
          <Route path="collection/redeemable-prizes" element={<RedeemablePrizeManagement />} />
          <Route path="collection/game-rewards" element={<GameRewardManagement />} />
          <Route path="collection/honors" element={<HonorManagement />} />
          <Route path="collection/rule-center" element={<RewardRuleCenter />} />
          <Route path="collection/collectibles" element={<CollectibleManagement />} />
          <Route path="collection/badges" element={<Navigate to="/collection/honors" replace />} />

          <Route path="users/progress" element={<UserManagement />} />
          <Route path="users/progress/:userId" element={<UserProgressWorkbench />} />
          <Route path="users/story-progress" element={<Navigate to="/users/progress" replace />} />

          <Route path="ops/test-console" element={<TestAccount />} />
          <Route path="ops/activities" element={<OperationsManagement />} />
          <Route
            path="ops/sandbox"
            element={placeholder({
              title: '測試資源與沙盒',
              subTitle: '集中管理批量測試資料、沙盒快照與資料匯出。',
              tags: ['測試資料', '沙盒', '批量工具'],
              description:
                '此模組保留給後續測試與營運工具，包含批量匯入匯出、沙盒快照與資料校驗，不再使用錯置的英文 placeholder。',
              todoItems: ['批量匯入測試資料', '沙盒快照與回滾', '資料匯出與校驗工具'],
            })}
          />

          <Route path="system/admins" element={<AdminUsersManagement />} />
          <Route path="system/roles" element={<RolePermissionManagement />} />
          <Route path="system/configs" element={<SystemManagement />} />
          <Route
            path="system/audit"
            element={placeholder({
              title: '審計與日誌',
              subTitle: '查看管理操作、內容異動、登入安全與系統事件。',
              tags: ['審計', '日誌', '追蹤'],
              description:
                '後續會把操作日誌、內容版本、登入與安全事件整理成獨立審計中心，支援檢索、對比與告警視圖。',
              todoItems: ['管理操作審計索引', '內容版本差異對比', '登入安全事件追蹤'],
            })}
          />
        </Route>
      </Route>
      <Route path="*" element={<Navigate to="/dashboard" replace />} />
    </Routes>
  );
}

export default App;
