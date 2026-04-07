export default defineAppConfig({
  pages: [
    'pages/index/index',      // 首页 - 游戏入口
    'pages/map/index',        // 地图 - 探索主入口
    'pages/profile/index',    // 我的
    'pages/story/index',      // 故事线
    'pages/stamps/index',     // 印章收集
    'pages/rewards/index',    // 奖励兑换
    'pages/settings/index',   // 设置
  ],
  tabBar: {
    custom: false,
    color: '#999999',
    selectedColor: '#C8102E',
    backgroundColor: '#FFFFFF',
    borderStyle: 'black',
    list: [
      {
        pagePath: 'pages/index/index',
        text: '首页',
        iconPath: 'assets/icons/home.png',
        selectedIconPath: 'assets/icons/home-active.png'
      },
      {
        pagePath: 'pages/map/index',
        text: '地图',
        iconPath: 'assets/icons/map.png',
        selectedIconPath: 'assets/icons/map-active.png'
      },
      {
        pagePath: 'pages/profile/index',
        text: '我的',
        iconPath: 'assets/icons/profile.png',
        selectedIconPath: 'assets/icons/profile-active.png'
      }
    ]
  },
  window: {
    backgroundTextStyle: 'light',
    navigationBarBackgroundColor: '#C8102E',
    navigationBarTitleText: '澳小遊',
    navigationBarTextStyle: 'white',
    navigationStyle: 'default'
  },
  // 使用分包加载优化主包体积
  subPackages: [
    {
      root: 'pages/story/',
      pages: [
        'index',
        'detail/index',
        'chapter/index'
      ]
    },
    {
      root: 'pages/stamps/',
      pages: [
        'index',
        'detail/index'
      ]
    },
    {
      root: 'pages/rewards/',
      pages: [
        'index',
        'detail/index',
        'exchange/index'
      ]
    }
  ],
  // 预加载分包
  preloadRule: {
    'pages/index/index': {
      network: 'all',
      packages: ['pages/story/', 'pages/stamps/']
    }
  },
  // 权限配置
  permission: {
    'scope.userLocation': {
      desc: '您的位置信息将用于澳小遊小程序的定位打卡功能'
    }
  },
  // 需要使用的地理位置接口
  requiredPrivateInfos: [
    'getLocation',
    'onLocationChange'
  ]
})
