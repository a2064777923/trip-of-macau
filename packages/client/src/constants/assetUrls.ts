export const COS_ASSET_BASE = 'https://tripofmacau-1301163924.cos.ap-hongkong.myqcloud.com/miniapp/assets'

export const cosAssetDirectories = {
  brand: `${COS_ASSET_BASE}/brand`,
  poiPng: `${COS_ASSET_BASE}/poi/png`,
  poiSvg: `${COS_ASSET_BASE}/poi/svg`,
  tabbarPng: `${COS_ASSET_BASE}/tabbar/png`,
  tabbarSvg: `${COS_ASSET_BASE}/tabbar/svg`,
  indoorLisboeta: `${COS_ASSET_BASE}/indoor/lisboeta-macau`,
} as const

export const cosAssetManifest = {
  brand: {
    appLogoMain: `${cosAssetDirectories.brand}/logo.png`,
  },
  poi: {
    markerChurchPng: `${cosAssetDirectories.poiPng}/church.png`,
    markerGhostPng: `${cosAssetDirectories.poiPng}/ghost.png`,
    markerLisboaPng: `${cosAssetDirectories.poiPng}/lisboa.png`,
    markerRuinsPng: `${cosAssetDirectories.poiPng}/ruins.png`,
    markerTheaterPng: `${cosAssetDirectories.poiPng}/theater.png`,
    markerUserPng: `${cosAssetDirectories.poiPng}/user.png`,
    markerChurchSvg: `${cosAssetDirectories.poiSvg}/church.svg`,
    markerGhostSvg: `${cosAssetDirectories.poiSvg}/ghost.svg`,
    markerLisboaSvg: `${cosAssetDirectories.poiSvg}/lisboa.svg`,
    markerRuinsSvg: `${cosAssetDirectories.poiSvg}/ruins.svg`,
    markerTheaterSvg: `${cosAssetDirectories.poiSvg}/theater.svg`,
    markerUserSvg: `${cosAssetDirectories.poiSvg}/user.svg`,
  },
  tabbar: {
    iconHomeDefaultPng: `${cosAssetDirectories.tabbarPng}/home.png`,
    iconHomeActivePng: `${cosAssetDirectories.tabbarPng}/home-active.png`,
    iconMapDefaultPng: `${cosAssetDirectories.tabbarPng}/map.png`,
    iconMapActivePng: `${cosAssetDirectories.tabbarPng}/map-active.png`,
    iconDiscoverDefaultPng: `${cosAssetDirectories.tabbarPng}/discover.png`,
    iconDiscoverActivePng: `${cosAssetDirectories.tabbarPng}/discover-active.png`,
    iconTipsDefaultPng: `${cosAssetDirectories.tabbarPng}/tips.png`,
    iconTipsActivePng: `${cosAssetDirectories.tabbarPng}/tips-active.png`,
    iconProfileDefaultPng: `${cosAssetDirectories.tabbarPng}/profile.png`,
    iconProfileActivePng: `${cosAssetDirectories.tabbarPng}/profile-active.png`,
    iconHomeDefaultSvg: `${cosAssetDirectories.tabbarSvg}/home.svg`,
    iconHomeActiveSvg: `${cosAssetDirectories.tabbarSvg}/home-active.svg`,
    iconMapDefaultSvg: `${cosAssetDirectories.tabbarSvg}/map.svg`,
    iconMapActiveSvg: `${cosAssetDirectories.tabbarSvg}/map-active.svg`,
    iconDiscoverDefaultSvg: `${cosAssetDirectories.tabbarSvg}/discover.svg`,
    iconDiscoverActiveSvg: `${cosAssetDirectories.tabbarSvg}/discover-active.svg`,
    iconTipsDefaultSvg: `${cosAssetDirectories.tabbarSvg}/tips.svg`,
    iconTipsActiveSvg: `${cosAssetDirectories.tabbarSvg}/tips-active.svg`,
    iconProfileDefaultSvg: `${cosAssetDirectories.tabbarSvg}/profile.svg`,
    iconProfileActiveSvg: `${cosAssetDirectories.tabbarSvg}/profile-active.svg`,
  },
  indoor: {
    lisboeta: {
      manifestJson: `${cosAssetDirectories.indoorLisboeta}/manifest.json`,
      manifestJs: `${cosAssetDirectories.indoorLisboeta}/manifest.js`,
      poisJson: `${cosAssetDirectories.indoorLisboeta}/pois.json`,
      poisCsv: `${cosAssetDirectories.indoorLisboeta}/pois.csv`,
      floorImage: (floorId: string) => `${cosAssetDirectories.indoorLisboeta}/floors/${floorId}.png`,
      tileImage: (floorId: string, tileFile: string) => `${cosAssetDirectories.indoorLisboeta}/tiles/${floorId}/${tileFile}`,
    },
  },
} as const

export const localStaticAssetPolicy = {
  keepLocalInMiniProgramTabBar: true,
  reason: '微信小程序原生 tabBar 图标建议保留本地路径，不切换为网络资源。',
} as const
