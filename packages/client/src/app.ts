import { useLaunch } from '@tarojs/taro'
import './styles/index.scss'

function App({ children }) {
  // 小程序启动时执行
  useLaunch(() => {
    console.log('🎮 澳小遊小程序启动')
    
    // 初始化用户信息
    initUserInfo()
    
    // 获取系统信息
    const systemInfo = wx.getSystemInfoSync()
    console.log('系统信息:', systemInfo)
  })

  // 初始化用户信息
  const initUserInfo = () => {
    try {
      const userInfo = wx.getStorageSync('userInfo')
      if (!userInfo) {
        // 首次使用，设置默认值
        const defaultUserInfo = {
          interfaceMode: 'standard', // standard | elderly
          fontScale: 1.0,
          highContrast: false,
          voiceGuideEnabled: false
        }
        wx.setStorageSync('userInfo', defaultUserInfo)
        console.log('初始化用户设置:', defaultUserInfo)
      }
    } catch (e) {
      console.error('初始化用户信息失败:', e)
    }
  }

  return children
}

export default App
