import { create } from 'zustand'

// 用户状态接口
interface UserState {
  // 用户基本信息
  userInfo: {
    openId: string
    nickname: string
    avatarUrl: string
    language: string
  } | null
  
  // 游戏进度
  level: number
  title: string
  totalStamps: number
  
  // 界面设置
  interfaceMode: 'standard' | 'elderly'
  fontScale: number
  highContrast: boolean
  voiceGuideEnabled: boolean
  
  // 方法
  setUserInfo: (userInfo: any) => void
  setLevel: (level: number, title: string) => void
  addStamps: (count: number) => void
  setInterfaceMode: (mode: 'standard' | 'elderly') => void
  setFontScale: (scale: number) => void
  toggleHighContrast: () => void
  toggleVoiceGuide: () => void
  reset: () => void
}

// 创建用户状态存储
export const useUserStore = create<UserState>((set, get) => ({
  // 初始状态
  userInfo: null,
  level: 1,
  title: '探索新手',
  totalStamps: 0,
  interfaceMode: 'standard',
  fontScale: 1.0,
  highContrast: false,
  voiceGuideEnabled: false,

  // 设置用户信息
  setUserInfo: (userInfo) => {
    set({ userInfo })
    // 持久化到本地存储
    try {
      wx.setStorageSync('userInfo', userInfo)
    } catch (e) {
      console.error('保存用户信息失败:', e)
    }
  },

  // 设置等级
  setLevel: (level, title) => {
    set({ level, title })
  },

  // 增加印章
  addStamps: (count) => {
    const currentTotal = get().totalStamps
    set({ totalStamps: currentTotal + count })
  },

  // 设置界面模式
  setInterfaceMode: (mode) => {
    set({ interfaceMode: mode })
    // 持久化
    try {
      const settings = wx.getStorageSync('userSettings') || {}
      settings.interfaceMode = mode
      wx.setStorageSync('userSettings', settings)
    } catch (e) {
      console.error('保存设置失败:', e)
    }
  },

  // 设置字体缩放
  setFontScale: (scale) => {
    set({ fontScale: scale })
  },

  // 切换高对比度
  toggleHighContrast: () => {
    const current = get().highContrast
    set({ highContrast: !current })
  },

  // 切换语音导览
  toggleVoiceGuide: () => {
    const current = get().voiceGuideEnabled
    set({ voiceGuideEnabled: !current })
  },

  // 重置状态
  reset: () => {
    set({
      userInfo: null,
      level: 1,
      title: '探索新手',
      totalStamps: 0,
      interfaceMode: 'standard',
      fontScale: 1.0,
      highContrast: false,
      voiceGuideEnabled: false
    })
  }
}))

// 从本地存储恢复用户状态
export const restoreUserState = async () => {
  try {
    const userInfo = wx.getStorageSync('userInfo')
    const userSettings = wx.getStorageSync('userSettings')
    
    if (userInfo) {
      useUserStore.getState().setUserInfo(userInfo)
    }
    
    if (userSettings) {
      if (userSettings.interfaceMode) {
        useUserStore.getState().setInterfaceMode(userSettings.interfaceMode)
      }
    }
    
    console.log('用户状态已从本地存储恢复')
  } catch (e) {
    console.error('恢复用户状态失败:', e)
  }
}
