import React, { Suspense } from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { Spin } from 'antd';
import DefaultLayout from './layouts/DefaultLayout';
import TestAccount from './pages/TestAccount';
import './App.css';

// 懒加载其他页面
const Dashboard = React.lazy(() => import('./pages/Dashboard'));
const POIManagement = React.lazy(() => import('./pages/POIManagement'));
const Login = React.lazy(() => import('./pages/Login'));

const Loading = () => (
  <div style={{ 
    display: 'flex', 
    justifyContent: 'center', 
    alignItems: 'center', 
    height: '100vh' 
  }}>
    <Spin size="large" tip="加载中..." />
  </div>
);

function App() {
  return (
    <Suspense fallback={<Loading />}>
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route path="/" element={<DefaultLayout />}>
          <Route index element={<Navigate to="/dashboard" replace />} />
          <Route path="dashboard" element={<Dashboard />} />
          <Route path="poi" element={<POIManagement />} />
          <Route path="test-account" element={<TestAccount />} />
        </Route>
      </Routes>
    </Suspense>
  );
}

export default App;
