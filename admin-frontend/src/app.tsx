import { RunTimeLayoutConfig } from '@umijs/max';
import { history } from '@umijs/max';
import { message } from 'antd';

// 运行时配置
export const layout: RunTimeLayoutConfig = ({ initialState }) => {
  return {
    logo: '/logo.png',
    title: '澳小遊后台管理',
    layout: 'mix',
    contentWidth: 'Fluid',
    fixedHeader: true,
    fixSiderbar: true,
    colorWeak: false,
    menu: {
      locale: false,
    },
    avatarProps: {
      src: initialState?.currentUser?.avatar,
      title: initialState?.currentUser?.nickname || initialState?.currentUser?.username,
    },
    onMenuHeaderClick: () => {
      history.push('/');
    },
  };
};

// 请求配置
export const request = {
  timeout: 30000,
  errorConfig: {
    errorHandler: (error: any) => {
      const { response } = error;
      if (response?.status === 401) {
        message.error('登录已过期，请重新登录');
        localStorage.clear();
        history.push('/login');
      } else {
        message.error(response?.data?.message || '请求失败');
      }
      return Promise.reject(error);
    },
  },
  requestInterceptors: [
    (url: string, options: any) => {
      const token = localStorage.getItem('access_token');
      if (token) {
        options.headers = {
          ...options.headers,
          Authorization: `Bearer ${token}`,
        };
      }
      return { url, options };
    },
  ],
  responseInterceptors: [
    async (response: any) => {
      const data = await response.clone().json();
      if (data.code !== 200) {
        throw new Error(data.message || '请求失败');
      }
      return response;
    },
  ],
};