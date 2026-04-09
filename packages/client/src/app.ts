import { useLaunch } from '@tarojs/taro'
import './styles/index.scss'
import { loadGameState } from './services/gameService'

function applyInterfaceMode(mode) {
  try {
    wx.setStorageSync('interfaceMode', mode)
  } catch (error) {
    console.warn('保存界面模式失败', error)
  }
}

function logSystemInfo() {
  try {
    const deviceInfo = wx.getDeviceInfo ? wx.getDeviceInfo() : {}
    const windowInfo = wx.getWindowInfo ? wx.getWindowInfo() : {}
    const appBaseInfo = wx.getAppBaseInfo ? wx.getAppBaseInfo() : {}
    console.log('设备信息:', {
      ...deviceInfo,
      ...windowInfo,
      ...appBaseInfo,
    })
  } catch (error) {
    console.warn('读取设备信息失败', error)
  }
}

function App({ children }) {
  useLaunch(() => {
    console.log('🎮 澳小遊小程序启动')

    const state = loadGameState()
    wx.setStorageSync('userInfo', state.user)
    applyInterfaceMode(state.user.interfaceMode)
    logSystemInfo()
  })

  return children
}

export default App


