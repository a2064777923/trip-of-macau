import { defineConfig } from '@umijs/max';

export default defineConfig({
  antd: {},
  access: {},
  model: {},
  initialState: {},
  request: {},
  layout: {
    title: '澳小遊后台管理',
    logo: '/logo.png',
    locale: false,
  },
  routes: [
    {
      path: '/login',
      component: './Login',
      layout: false,
    },
    {
      path: '/',
      redirect: '/dashboard',
    },
    {
      path: '/dashboard',
      name: '首页',
      icon: 'HomeOutlined',
      component: './Dashboard',
    },
    {
      path: '/content',
      name: '内容管理',
      icon: 'AppstoreOutlined',
      routes: [
        {
          path: '/content/poi',
          name: 'POI管理',
          component: './Content/POI',
        },
        {
          path: '/content/story',
          name: '故事线管理',
          component: './Content/Story',
        },
        {
          path: '/content/map',
          name: '地图瓦片',
          component: './Content/Map',
        },
        {
          path: '/content/media',
          name: '多媒体资源',
          component: './Content/Media',
        },
      ],
    },
    {
      path: '/user',
      name: '用户管理',
      icon: 'UserOutlined',
      routes: [
        {
          path: '/user/list',
          name: '用户列表',
          component: './User/List',
        },
        {
          path: '/user/test-account',
          name: '测试账号',
          component: './User/TestAccount',
        },
      ],
    },
    {
      path: '/operation',
      name: '运营管理',
      icon: 'BarChartOutlined',
      routes: [
        {
          path: '/operation/activity',
          name: '活动发布',
          component: './Operation/Activity',
        },
        {
          path: '/operation/ad',
          name: '广告管理',
          component: './Operation/Ad',
        },
        {
          path: '/operation/reward',
          name: '奖励配置',
          component: './Operation/Reward',
        },
        {
          path: '/operation/statistics',
          name: '数据统计',
          component: './Operation/Statistics',
        },
      ],
    },
    {
      path: '/system',
      name: '系统管理',
      icon: 'SettingOutlined',
      routes: [
        {
          path: '/system/admin',
          name: '账号管理',
          component: './System/Admin',
        },
        {
          path: '/system/role',
          name: '角色权限',
          component: './System/Role',
        },
        {
          path: '/system/log',
          name: '操作日志',
          component: './System/Log',
        },
        {
          path: '/system/config',
          name: '系统配置',
          component: './System/Config',
        },
      ],
    },
  ],
  npmClient: 'npm',
  mock: {
    include: ['mock/**/*'],
  },
  tailwindcss: {},
});