// Mock 数据服务
// 用于开发阶段模拟API响应

import { sleep } from '../utils/common'

// 模拟网络延迟
const MOCK_DELAY = 300

// Mock 用户数据
export const mockUserData = {
  id: 1,
  openId: 'mock_open_id_123',
  nickname: '探索者小明',
  avatarUrl: 'https://cdn.tripofmacau.com/avatars/default.png',
  language: 'zh_CN',
  level: 3,
  title: '澳门见习生',
  totalStamps: 12,
  interfaceMode: 'standard' as const,
  fontScale: 1.0,
  highContrast: false,
  voiceGuideEnabled: false
}

// Mock POI数据
export const mockPOIs = [
  {
    id: 1,
    name: '大三巴牌坊',
    nameEn: 'Ruins of St. Paul\'s',
    latitude: 22.1972,
    longitude: 113.5408,
    address: '澳门特别行政区花王堂区炮台山下',
    triggerRadius: 50,
    importance: 'very_important',
    icon: '🏛️',
    category: '历史遗迹',
    description: '圣保禄大教堂遗址，澳门最著名的地标',
    storyLineId: 2,
    stampType: 'location'
  },
  {
    id: 2,
    name: '议事亭前地',
    nameEn: 'Senado Square',
    latitude: 22.1937,
    longitude: 113.5403,
    address: '澳门特别行政区大堂区',
    triggerRadius: 50,
    importance: 'very_important',
    icon: '🏢',
    category: '广场',
    description: '澳门历史城区核心区域',
    storyLineId: 2,
    stampType: 'location'
  },
  {
    id: 3,
    name: '妈阁庙',
    nameEn: 'A-Ma Temple',
    latitude: 22.1862,
    longitude: 113.5319,
    address: '澳门特别行政区风顺堂区妈阁上街',
    triggerRadius: 50,
    importance: 'very_important',
    icon: '🛕',
    category: '庙宇',
    description: '澳门最古老的庙宇，澳门名称的由来',
    storyLineId: 1,
    stampType: 'location'
  },
  {
    id: 4,
    name: '港务局大楼',
    nameEn: 'Moorish Barracks',
    latitude: 22.1857,
    longitude: 113.5334,
    address: '澳门特别行政区风顺堂区妈阁河边新街',
    triggerRadius: 40,
    importance: 'important',
    icon: '🏰',
    category: '建筑',
    description: '具有摩尔人建筑风格的军事建筑',
    storyLineId: 1,
    stampType: 'location'
  },
  {
    id: 5,
    name: '玫瑰堂',
    nameEn: 'St. Dominic\'s Church',
    latitude: 22.1939,
    longitude: 113.5405,
    address: '澳门特别行政区大堂区板樟堂前地',
    triggerRadius: 40,
    importance: 'important',
    icon: '⛪',
    category: '教堂',
    description: '巴洛克式天主教堂，供奉玫瑰圣母',
    storyLineId: 2,
    stampType: 'location'
  }
]

// Mock 印章数据
export const mockStamps = [
  {
    id: 1,
    type: 'location',
    name: '大三巴足迹',
    description: '到达大三巴牌坊',
    icon: '🏛️',
    collectedAt: '2026-04-01T10:30:00Z',
    poiId: 1
  },
  {
    id: 2,
    type: 'location',
    name: '议事亭足迹',
    description: '到达议事亭前地',
    icon: '🏢',
    collectedAt: '2026-04-01T11:15:00Z',
    poiId: 2
  },
  {
    id: 3,
    type: 'story',
    name: '海上丝路 · 第一章',
    description: '完成海上丝路第一章节',
    icon: '📖',
    collectedAt: '2026-04-02T14:20:00Z',
    storyId: 1,
    chapterId: 1
  },
  {
    id: 4,
    type: 'story',
    name: '东西方战事 · 序章',
    description: '完成东西方战事序章',
    icon: '⚔️',
    collectedAt: '2026-04-03T09:45:00Z',
    storyId: 2,
    chapterId: 0
  }
]

// Mock 故事线数据
export const mockStories = [
  {
    id: 1,
    name: '海上丝路',
    nameEn: 'Maritime Silk Road',
    description: '澳门开埠历史，从大航海时代到东方明珠',
    icon: '🚢',
    coverImage: 'https://cdn.tripofmacau.com/stories/maritime-silk-road/cover.jpg',
    totalChapters: 6,
    completedChapters: 1,
    pois: [3, 4],
    difficulty: 'easy',
    estimatedTime: '2-3小时'
  },
  {
    id: 2,
    name: '东西方战事',
    nameEn: 'East Meets West',
    description: '葡澳防卫史，见证中西方文明的碰撞与融合',
    icon: '⚔️',
    coverImage: 'https://cdn.tripofmacau.com/stories/east-meets-west/cover.jpg',
    totalChapters: 8,
    completedChapters: 0,
    pois: [1, 2, 5],
    difficulty: 'medium',
    estimatedTime: '3-4小时'
  }
]

// Mock API 响应生成器
export const createMockResponse = <T>(data: T, message = 'success') => ({
  code: 200,
  message,
  data,
  timestamp: Date.now()
})

// Mock API 错误响应
export const createMockError = (message: string, code = 400) => ({
  code,
  message,
  data: null,
  timestamp: Date.now()
})

// 模拟网络延迟
export const mockDelay = (ms = MOCK_DELAY) => sleep(ms)

// 基础工具函数
function sleep(ms: number): Promise<void> {
  return new Promise(resolve => setTimeout(resolve, ms))
}

// Mock API 路由
export const mockAPIRoutes = {
  // 用户相关
  'GET /api/v1/user/info': () => createMockResponse(mockUserData),
  'PUT /api/v1/user/settings': (data: any) => createMockResponse({ ...mockUserData, ...data }),
  
  // 地图相关
  'GET /api/v1/map/config': () => createMockResponse({
    mapId: 'macau-peninsula',
    style: 'cartoon',
    cdnBase: 'https://cdn.tripofmacau.com/maps',
    zoomLevels: [1, 2, 3, 4]
  }),
  'GET /api/v1/map/pois': () => createMockResponse(mockPOIs),
  'POST /api/v1/location/checkin': (data: any) => createMockResponse({
    success: true,
    stampId: Math.floor(Math.random() * 1000),
    poiName: mockPOIs.find(p => p.id === data.poiId)?.name || '未知地点'
  }),
  
  // 印章相关
  'GET /api/v1/stamps': () => createMockResponse(mockStamps),
  'POST /api/v1/stamps/collect': (data: any) => createMockResponse({
    success: true,
    stamp: mockStamps[0]
  }),
  
  // 故事线相关
  'GET /api/v1/stories': () => createMockResponse(mockStories),
  'GET /api/v1/stories/1/chapters': () => createMockResponse([
    { id: 1, order: 1, title: '序章：东方之珠', completed: true },
    { id: 2, order: 2, title: '第一章：大航海时代', completed: false }
  ])
}

// 模拟API请求处理
export const handleMockRequest = async (method: string, url: string, data?: any) => {
  const key = `${method.toUpperCase()} ${url}`
  const handler = mockAPIRoutes[key as keyof typeof mockAPIRoutes]
  
  if (handler) {
    await mockDelay()
    return handler(data)
  }
  
  throw new Error(`Mock API not found: ${key}`)
}
