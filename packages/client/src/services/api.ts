// API 服务层
import Taro from '@tarojs/taro'

// API 基础配置
const API_BASE_URL = process.env.USE_MOCK === 'true' 
  ? '' 
  : (process.env.API_BASE_URL || 'https://api.tripofmacau.com/api/v1')

// 请求拦截器
const request = async (options: any) => {
  // 显示加载中
  if (options.loading !== false) {
    Taro.showLoading({ title: '加载中...' })
  }

  try {
    // 获取 token
    const token = Taro.getStorageSync('token')
    
    const res = await Taro.request({
      url: `${API_BASE_URL}${options.url}`,
      method: options.method || 'GET',
      data: options.data,
      header: {
        'Content-Type': 'application/json',
        ...(token && { 'Authorization': `Bearer ${token}` }),
        ...options.header
      }
    })

    // 隐藏加载中
    Taro.hideLoading()

    // 处理响应
    if (res.statusCode >= 200 && res.statusCode < 300) {
      return res.data
    } else if (res.statusCode === 401) {
      // Token 过期，清除并重新登录
      Taro.removeStorageSync('token')
      Taro.showToast({ title: '请重新登录', icon: 'none' })
      // 可以在这里跳转到登录页面
      throw new Error('Unauthorized')
    } else {
      Taro.showToast({ 
        title: res.data?.message || '请求失败', 
        icon: 'none' 
      })
      throw new Error(res.data?.message || '请求失败')
    }
  } catch (error) {
    Taro.hideLoading()
    console.error('请求错误:', error)
    throw error
  }
}

// API 方法
export const api = {
  // 地图相关
  map: {
    // 获取地图配置
    getConfig: () => request({ url: '/map/config' }),
    
    // 获取POI列表
    getPOIs: (params?: any) => request({ 
      url: '/map/pois', 
      data: params 
    }),
    
    // 上报位置
    updateLocation: (data: any) => request({
      url: '/location/update',
      method: 'POST',
      data
    }),
    
    // 签到打卡
    checkin: (data: any) => request({
      url: '/location/checkin',
      method: 'POST',
      data
    })
  },

  // 印章相关
  stamps: {
    // 获取用户印章
    getUserStamps: () => request({ url: '/stamps' }),
    
    // 领取印章
    collect: (data: any) => request({
      url: '/stamps/collect',
      method: 'POST',
      data
    })
  },

  // 故事线相关
  stories: {
    // 获取故事线列表
    getList: () => request({ url: '/stories' }),
    
    // 获取故事章节
    getChapters: (storyId: string) => request({ 
      url: `/stories/${storyId}/chapters` 
    })
  },

  // 用户相关
  user: {
    // 获取用户信息
    getInfo: () => request({ url: '/user/info' }),
    
    // 更新设置
    updateSettings: (data: any) => request({
      url: '/user/settings',
      method: 'PUT',
      data
    }),
    
    // 登录
    login: (code: string) => request({
      url: '/auth/login',
      method: 'POST',
      data: { code }
    })
  }
}

export default api
