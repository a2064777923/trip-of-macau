import React, { useEffect, useState } from 'react';
import { Navigate, Outlet, Route, Routes } from 'react-router-dom';
import { Space, Spin, Typography } from 'antd';
import brandLogo from '@shared-client-assets/logo.png';
import DefaultLayout from './layouts/DefaultLayout';
import Login from './pages/Login';
import Dashboard from './pages/Dashboard';
import POIManagement from './pages/POIManagement';
import TestAccount from './pages/TestAccount';
import UserManagement from './pages/UserManagement';
import StorylineManagement from './pages/StorylineManagement';
import OperationsManagement from './pages/OperationsManagement';
import ModulePlaceholder from './pages/ModulePlaceholder';
import CityManagement from './pages/MapSpace/CityManagement';
import IndoorBuildingManagement from './pages/MapSpace/IndoorBuildingManagement';
import MapTileManagement from './pages/MapSpace/MapTileManagement';
import AiCapabilityCenter from './pages/MapSpace/AiCapabilityCenter';
import CollectibleManagement from './pages/Collectibles/CollectibleManagement';
import BadgeManagement from './pages/Collectibles/BadgeManagement';
import AdminUsersManagement from './pages/System/AdminUsersManagement';
import RolePermissionManagement from './pages/System/RolePermissionManagement';
import SystemManagement from './pages/SystemManagement';
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
      background: 'linear-gradient(135deg, #eef2ff 0%, #f7f8ff 100%)',
    }}
  >
    <Space direction="vertical" size="middle" align="center">
      <img
        src={brandLogo}
        alt="Trip of Macau"
        style={{ width: 72, height: 72, borderRadius: 20, objectFit: 'cover' }}
      />
      <Spin size="large" />
      <Text type="secondary">正在載入 Trip of Macau 後台工作台...</Text>
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
          <Route path="space/map-tiles" element={<MapTileManagement />} />
          <Route path="space/pois" element={<POIManagement />} />
          <Route path="space/indoor-buildings" element={<IndoorBuildingManagement />} />
          <Route path="space/ai-navigation" element={<AiCapabilityCenter />} />

          <Route path="content/storylines" element={<StorylineManagement />} />
          <Route
            path="content/chapters"
            element={placeholder({
              title: '章節編排',
              subTitle: '選定既有故事線後，進一步編排章節、互動條件與完成效果。',
              tags: ['故事線', '章節', '互動編排'],
              description:
                '這一頁保留獨立入口承接故事線的章節設計，不再錯誤導回故事線列表。後續將補上 POI、任務點、標記物與疊加物的完整綁定能力。',
              todoItems: ['選取故事線並建立章節骨架', '設定前置條件與完成效果', '管理章節媒體與互動資源'],
            })}
          />
          <Route
            path="content/campaigns"
            element={placeholder({
              title: '任務與活動',
              subTitle: '管理全域任務、官方活動與發現頁活動卡片。',
              tags: ['任務', '活動', '發現頁'],
              description:
                '此入口預留給任務與活動的正式管理頁，將支援名額、報名費、主辦方、地址、圖片、HTML 圖文，以及定時上下線與置頂設定。',
              todoItems: ['活動主資料與報名欄位', '定時上線與下線規則', '資源顯示勾選與置頂控制'],
            })}
          />
          <Route
            path="content/media"
            element={placeholder({
              title: '媒體資源',
              subTitle: '統一檢索所有已上傳到後台與 COS 的媒體資源。',
              tags: ['媒體庫', 'COS', '檢索'],
              description:
                '這一頁會成為全域媒體資源中心，支援搜尋、篩選與跨模組引用回查，避免各模組各自維護碎片化附件列表。',
              todoItems: ['媒體列表與搜尋', '依模組與資源類型篩選', '查看引用來源與上傳資訊'],
            })}
          />

          <Route
            path="collection/rewards"
            element={placeholder({
              title: '獎勵配置',
              subTitle: '規劃可兌換獎勵、庫存與展示資訊。',
              tags: ['獎勵', '兌換', '營運'],
              description:
                '獎勵配置頁將獨立承接獎品內容、兌換條件、展示圖示與投放策略，不再借用其他控制台或臨時入口。',
              todoItems: ['獎勵清單與庫存管理', '綁定故事線與地圖範圍', '展示圖示與兌換限制'],
            })}
          />
          <Route path="collection/collectibles" element={<CollectibleManagement />} />
          <Route path="collection/badges" element={<BadgeManagement />} />

          <Route path="users/progress" element={<UserManagement />} />
          <Route
            path="users/story-progress"
            element={placeholder({
              title: '用戶進度與軌跡',
              subTitle: '追蹤探索進度、互動日誌與故事完成情況。',
              tags: ['用戶進度', '軌跡', '探索度'],
              description:
                '後續會補齊地圖與子地圖探索度、故事完成進度、收集物與任務進度、互動日誌與回放檢視能力。',
              todoItems: ['地圖與子地圖探索度計算', '故事與章節完成進度', '互動日誌與操作軌跡'],
            })}
          />

          <Route path="ops/test-console" element={<TestAccount />} />
          <Route path="ops/activities" element={<OperationsManagement />} />
          <Route
            path="ops/sandbox"
            element={placeholder({
              title: '測試資料與沙盒',
              subTitle: '集中管理批量測試資料、沙盒快照與資料匯出。',
              tags: ['測試資料', '沙盒', '批量工具'],
              description:
                '這個模組保留給後續測試與營運工具，包括批量匯入匯出、沙盒快照與資料校驗，不再重用錯誤的英文佔位頁。',
              todoItems: ['批量匯入測試資料', '沙盒快照與回復', '資料匯出與檢查工具'],
            })}
          />

          <Route path="system/admins" element={<AdminUsersManagement />} />
          <Route path="system/roles" element={<RolePermissionManagement />} />
          <Route path="system/configs" element={<SystemManagement />} />
          <Route
            path="system/audit"
            element={placeholder({
              title: '審計與日誌',
              subTitle: '查看管理操作、內容變更與安全事件。',
              tags: ['審計', '日誌', '追蹤'],
              description:
                '審計模組會獨立承接操作日誌、內容版本變更、匯入匯出記錄與安全事件追蹤，作為後台營運與排障的重要入口。',
              todoItems: ['管理操作審計索引', '內容版本與變更追蹤', '安全事件與異常檢索'],
            })}
          />
        </Route>
      </Route>
      <Route path="*" element={<Navigate to="/dashboard" replace />} />
    </Routes>
  );
}

export default App;
