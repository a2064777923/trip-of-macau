import { useState, useEffect } from 'react'
import { View, Text, Switch, Button } from '@tarojs/components'
import Taro from '@tarojs/taro'
import './index.scss'

// 设置页面
export default function SettingsPage() {
  const [settings, setSettings] = useState({
    // 界面设置
    elderlyMode: false,
    fontScale: 1.0,
    highContrast: false,
    
    // 功能设置
    voiceGuide: false,
    autoPlayStory: false,
    backgroundLocation: true,
    
    // 通知设置
    pushNotification: true,
    storyUpdateAlert: true,
    rewardAlert: true
  })

  useEffect(() => {
    loadSettings()
  }, [])

  // 加载设置
  const loadSettings = () => {
    try {
      const storedSettings = Taro.getStorageSync('appSettings')
      if (storedSettings) {
        setSettings(prev => ({ ...prev, ...storedSettings }))
      }
    } catch (e) {
      console.error('加载设置失败:', e)
    }
  }

  // 保存设置
  const saveSettings = (newSettings: any) => {
    try {
      Taro.setStorageSync('appSettings', newSettings)
    } catch (e) {
      console.error('保存设置失败:', e)
    }
  }

  // 更新设置
  const updateSetting = (key: string, value: any) => {
    const newSettings = { ...settings, [key]: value }
    setSettings(newSettings)
    saveSettings(newSettings)

    // 特殊处理
    if (key === 'elderlyMode' && value) {
      Taro.showModal({
        title: '长者模式已开启',
        content: '界面将切换为大字体、高对比度模式，操作按钮也会相应放大。',
        showCancel: false
      })
    }
  }

  // 清除缓存
  const handleClearCache = () => {
    Taro.showModal({
      title: '清除缓存',
      content: '确定要清除所有缓存数据吗？这将重置您的本地设置（但不会删除已收集的印章数据）。',
      success: (res) => {
        if (res.confirm) {
          try {
            // 清除本地存储（保留用户信息和印章）
            const userInfo = Taro.getStorageSync('userInfo')
            const stamps = Taro.getStorageSync('userStamps')
            
            Taro.clearStorageSync()
            
            // 恢复重要数据
            if (userInfo) Taro.setStorageSync('userInfo', userInfo)
            if (stamps) Taro.setStorageSync('userStamps', stamps)
            
            Taro.showToast({ title: '缓存已清除', icon: 'success' })
          } catch (e) {
            Taro.showToast({ title: '清除失败', icon: 'none' })
          }
        }
      }
    })
  }

  // 关于我们
  const handleAbout = () => {
    Taro.navigateTo({
      url: '/pages/settings/about/index'
    })
  }

  // 退出登录
  const handleLogout = () => {
    Taro.showModal({
      title: '退出登录',
      content: '确定要退出登录吗？',
      success: (res) => {
        if (res.confirm) {
          // 清除登录状态
          Taro.removeStorageSync('token')
          Taro.removeStorageSync('userInfo')
          
          Taro.showToast({ 
            title: '已退出登录', 
            icon: 'success',
            complete: () => {
              // 跳转到首页
              Taro.switchTab({ url: '/pages/index/index' })
            }
          })
        }
      }
    })
  }

  return (
    <View className='settings-page'>
      {/* 页面标题 */}
      <View className='page-header'>
        <Text className='page-title'>设置</Text>
      </View>

      {/* 界面设置 */}
      <View className='settings-section'>
        <View className='section-header'>
          <Text className='section-title'>🎨 界面设置</Text>
        </View>

        <View className='settings-list'>
          {/* 长者模式 */}
          <View className='setting-item'>
            <View className='setting-info'>
              <Text className='setting-name'>长者模式</Text>
              <Text className='setting-desc'>大字体、高对比度、简化操作</Text>
            </View>
            <Switch
              className='setting-switch'
              checked={settings.elderlyMode}
              onChange={(e) => updateSetting('elderlyMode', e.detail.value)}
              color='#C8102E'
            />
          </View>

          {/* 语音导览 */}
          <View className='setting-item'>
            <View className='setting-info'>
              <Text className='setting-name'>语音导览</Text>
              <Text className='setting-desc'>自动播放景点语音介绍</Text>
            </View>
            <Switch
              className='setting-switch'
              checked={settings.voiceGuide}
              onChange={(e) => updateSetting('voiceGuide', e.detail.value)}
              color='#C8102E'
            />
          </View>
        </View>
      </View>

      {/* 功能设置 */}
      <View className='settings-section'>
        <View className='section-header'>
          <Text className='section-title'>⚙️ 功能设置</Text>
        </View>

        <View className='settings-list'>
          {/* 后台定位 */}
          <View className='setting-item'>
            <View className='setting-info'>
              <Text className='setting-name'>后台定位</Text>
              <Text className='setting-desc'>允许后台获取位置用于打卡</Text>
            </View>
            <Switch
              className='setting-switch'
              checked={settings.backgroundLocation}
              onChange={(e) => updateSetting('backgroundLocation', e.detail.value)}
              color='#C8102E'
            />
          </View>

          {/* 自动播放故事 */}
          <View className='setting-item'>
            <View className='setting-info'>
              <Text className='setting-name'>自动播放故事</Text>
              <Text className='setting-desc'>到达景点后自动播放故事动画</Text>
            </View>
            <Switch
              className='setting-switch'
              checked={settings.autoPlayStory}
              onChange={(e) => updateSetting('autoPlayStory', e.detail.value)}
              color='#C8102E'
            />
          </View>
        </View>
      </View>

      {/* 通知设置 */}
      <View className='settings-section'>
        <View className='section-header'>
          <Text className='section-title'>🔔 通知设置</Text>
        </View>

        <View className='settings-list'>
          {/* 推送通知 */}
          <View className='setting-item'>
            <View className='setting-info'>
              <Text className='setting-name'>推送通知</Text>
              <Text className='setting-desc'>接收系统和活动通知</Text>
            </View>
            <Switch
              className='setting-switch'
              checked={settings.pushNotification}
              onChange={(e) => updateSetting('pushNotification', e.detail.value)}
              color='#C8102E'
            />
          </View>

          {/* 故事更新提醒 */}
          <View className='setting-item'>
            <View className='setting-info'>
              <Text className='setting-name'>故事更新提醒</Text>
              <Text className='setting-desc'>新章节上线时通知我</Text>
            </View>
            <Switch
              className='setting-switch'
              checked={settings.storyUpdateAlert}
              onChange={(e) => updateSetting('storyUpdateAlert', e.detail.value)}
              color='#C8102E'
            />
          </View>

          {/* 奖励到账提醒 */}
          <View className='setting-item'>
            <View className='setting-info'>
              <Text className='setting-name'>奖励到账提醒</Text>
              <Text className='setting-desc'>获得奖励时通知我</Text>
            </View>
            <Switch
              className='setting-switch'
              checked={settings.rewardAlert}
              onChange={(e) => updateSetting('rewardAlert', e.detail.value)}
              color='#C8102E'
            />
          </View>
        </View>
      </View>

      {/* 其他选项 */}
      <View className='settings-section'>
        <View className='section-header'>
          <Text className='section-title'>📱 其他</Text>
        </View>

        <View className='settings-list'>
          {/* 清除缓存 */}
          <View className='setting-item action' onClick={handleClearCache}>
            <View className='setting-info'>
              <Text className='setting-name'>🗑️ 清除缓存</Text>
              <Text className='setting-desc'>清除本地缓存数据</Text>
            </View>
            <Text className='action-arrow'>›</Text>
          </View>

          {/* 关于我们 */}
          <View className='setting-item action' onClick={handleAbout}>
            <View className='setting-info'>
              <Text className='setting-name'>ℹ️ 关于我们</Text>
              <Text className='setting-desc'>版本信息、用户协议</Text>
            </View>
            <Text className='action-arrow'>›</Text>
          </View>
        </View>
      </View>

      {/* 退出登录 */}
      <View className='logout-section'>
        <Button className='logout-btn' onClick={handleLogout}>
          退出登录
        </Button>
      </View>

      {/* 底部留白 */}
      <View className='bottom-spacer' />
    </View>
  )
}
