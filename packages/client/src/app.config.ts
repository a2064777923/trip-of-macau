export default defineAppConfig({
  pages: [
    'pages/index/index',
    'pages/map/index',
    'pages/tips/index',
    'pages/tips/notifications/index',
    'pages/tips/publish/index',
    'pages/tips/detail/index',
    'pages/discover/index',
    'pages/story/index',
    'pages/stamps/index',
    'pages/rewards/index',
    'pages/profile/index',
    'pages/settings/index',
    'pages/senior/index',
  ],
  tabBar: {
    custom: false,
    color: '#A0AEC0',
    selectedColor: '#FFB6C1',
    backgroundColor: '#FFFFFF',
    borderStyle: 'white',
    list: [
      {
        pagePath: 'pages/index/index',
        text: '首頁',
      },
      {
        pagePath: 'pages/map/index',
        text: '探索',
      },
      {
        pagePath: 'pages/discover/index',
        text: '發現',
      },
      {
        pagePath: 'pages/tips/index',
        text: '秘籍',
      },
      {
        pagePath: 'pages/profile/index',
        text: '我的',
      },
    ],
  },
  window: {
    backgroundTextStyle: 'light',
    backgroundColor: '#FFFAF0',
    navigationBarBackgroundColor: '#FFFAF0',
    navigationBarTitleText: '澳小遊',
    navigationBarTextStyle: 'black',
    navigationStyle: 'default',
  },
  preloadRule: {
    'pages/index/index': {
      network: 'all',
      packages: [],
    },
  },
  permission: {
    'scope.userLocation': {
      desc: '您的位置信息将用于澳小遊小程序的定位探索、附近玩法和故事觸發。',
    },
  },
  requiredPrivateInfos: ['getLocation', 'onLocationChange'],
  requiredBackgroundModes: ['location'],
  lazyCodeLoading: 'requiredComponents',
  sitemapLocation: 'sitemap.json',
})
