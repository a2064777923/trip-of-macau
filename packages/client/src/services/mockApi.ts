// Mock API 服务
// 在开发阶段使用，模拟后端API响应

import { 
  mockUserData, 
  mockPOIs, 
  mockStamps, 
  mockStories,
  createMockResponse,
  mockDelay 
} from './mock'

// 模拟API服务
class MockAPIService {
  // 用户相关
  async getUserInfo() {
    await mockDelay()
    return createMockResponse(mockUserData)
  }

  async updateUserSettings(data: any) {
    await mockDelay()
    return createMockResponse({ ...mockUserData, ...data })
  }

  // 地图相关
  async getMapConfig() {
    await mockDelay()
    return createMockResponse({
      mapId: 'macau-peninsula',
      style: 'cartoon',
      cdnBase: 'https://cdn.tripofmacau.com/maps',
      zoomLevels: [1, 2, 3, 4]
    })
  }

  async getPOIs(params?: any) {
    await mockDelay()
    let pois = [...mockPOIs]
    
    // 根据参数过滤
    if (params?.nearby && params.lat && params.lng) {
      // 计算距离并排序
      pois = pois.map(poi => ({
        ...poi,
        distance: calculateDistance(
          params.lat, 
          params.lng, 
          poi.latitude, 
          poi.longitude
        )
      })).sort((a, b) => (a.distance || 0) - (b.distance || 0))
    }
    
    return createMockResponse(pois)
  }

  async checkin(data: any) {
    await mockDelay(500)
    const poi = mockPOIs.find(p => p.id === data.poiId)
    return createMockResponse({
      success: true,
      stampId: Math.floor(Math.random() * 1000),
      poiName: poi?.name || '未知地点',
      stampType: poi?.stampType || 'location',
      collectedAt: new Date().toISOString()
    })
  }

  // 印章相关
  async getUserStamps() {
    await mockDelay()
    return createMockResponse(mockStamps)
  }

  async collectStamp(data: any) {
    await mockDelay(500)
    return createMockResponse({
      success: true,
      stamp: {
        id: Date.now(),
        type: data.type || 'location',
        name: data.name || '新印章',
        collectedAt: new Date().toISOString()
      }
    })
  }

  // 故事线相关
  async getStories() {
    await mockDelay()
    return createMockResponse(mockStories)
  }

  async getStoryChapters(storyId: string) {
    await mockDelay()
    // 模拟章节数据
    const chapters = [
      { 
        id: 1, 
        order: 1, 
        title: '序章：东方之珠', 
        completed: storyId === '1',
        mediaType: 'video',
        duration: 120
      },
      { 
        id: 2, 
        order: 2, 
        title: '第一章：大航海时代', 
        completed: false,
        mediaType: 'audio',
        duration: 180
      },
      { 
        id: 3, 
        order: 3, 
        title: '第二章：东方十字路口', 
        completed: false,
        mediaType: 'video',
        duration: 150
      }
    ]
    return createMockResponse(chapters)
  }
}

// 辅助函数：计算两点间距离
function calculateDistance(
  lat1: number,
  lng1: number,
  lat2: number,
  lng2: number
): number {
  const R = 6371000
  const dLat = toRadians(lat2 - lat1)
  const dLng = toRadians(lng2 - lng1)
  
  const a =
    Math.sin(dLat / 2) * Math.sin(dLat / 2) +
    Math.cos(toRadians(lat1)) *
      Math.cos(toRadians(lat2)) *
      Math.sin(dLng / 2) *
      Math.sin(dLng / 2)
  
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
  return Math.round(R * c)
}

function toRadians(degrees: number): number {
  return degrees * (Math.PI / 180)
}

// 导出单例
export const mockAPI = new MockAPIService()
export default mockAPI
