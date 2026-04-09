import React from 'react';
import ReactDOM from 'react-dom/client';
import { HashRouter } from 'react-router-dom';
import { App as AntdApp, ConfigProvider } from 'antd';
import zhCN from 'antd/locale/zh_CN';
import App from './App';
import './index.css';

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <ConfigProvider
      locale={zhCN}
      theme={{
        token: {
          colorPrimary: '#7c5cff',
          borderRadius: 14,
        },
      }}
    >
      <AntdApp>
        <HashRouter>
          <App />
        </HashRouter>
      </AntdApp>
    </ConfigProvider>
  </React.StrictMode>,
);
