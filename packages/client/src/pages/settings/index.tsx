import { useEffect, useState } from 'react'
import { Button, Switch, Text, View } from '@tarojs/components'
import Taro from '@tarojs/taro'
import { isAuthRequiredError, loadGameState, requireAuth, updateUserPreference } from '../../services/gameService'
import PageShell from '../../components/PageShell'
import './index.scss'

const scaleOptions = [1, 1.2, 1.4, 1.6, 1.8, 2]

export default function SettingsPage() {
  const [settings, setSettings] = useState({
    authStatus: 'anonymous',
    interfaceMode: 'standard',
    fontScale: 1,
    highContrast: false,
    voiceGuideEnabled: false,
  })

  useEffect(() => {
    const state = loadGameState()
    setSettings({
      authStatus: state.user.authStatus,
      interfaceMode: state.user.interfaceMode,
      fontScale: state.user.fontScale,
      highContrast: state.user.highContrast,
      voiceGuideEnabled: state.user.voiceGuideEnabled,
    })
  }, [])

  useEffect(() => {
    if (settings.authStatus === 'anonymous') {
      void requireAuth('查看與保存設定前，請先使用微信登入。')
    }
  }, [settings.authStatus])

  const applySetting = async (patch) => {
    const previous = settings
    const next = { ...settings, ...patch }
    setSettings(next)
    try {
      const user = await updateUserPreference(next)
      setSettings({
        authStatus: user.authStatus,
        interfaceMode: user.interfaceMode,
        fontScale: user.fontScale,
        highContrast: user.highContrast,
        voiceGuideEnabled: user.voiceGuideEnabled,
      })
    } catch (error) {
      setSettings(previous)
      if (!isAuthRequiredError(error)) {
        Taro.showToast({ title: error instanceof Error ? error.message : '設定更新失敗', icon: 'none' })
      }
    }
  }

  if (settings.authStatus === 'anonymous') {
    return (
      <PageShell className='settings-page'>
        <View className='page-header'>
          <Text className='page-title'>需要登入</Text>
          <Text className='page-subtitle'>正在前往「我的」頁面完成登入。</Text>
        </View>
      </PageShell>
    )
  }

  return (
    <PageShell className='settings-page'>
      <View className='page-header'>
        <Text className='page-title'>設定</Text>
        <Text className='page-subtitle'>把旅程調整成你最舒服的節奏，字體大小、語音提示與高對比都可以自行決定。</Text>
      </View>

      <View className='settings-section'>
        <View className='section-header'>
          <Text className='section-title'>長者模式</Text>
        </View>
        <View className='settings-list'>
          <View className='setting-item'>
            <View className='setting-info'>
              <Text className='setting-name'>啟用長者模式</Text>
              <Text className='setting-desc'>讓按鈕、字體與對比更清楚，適合慢遊與戶外使用。</Text>
            </View>
            <Switch
              className='setting-switch'
              checked={settings.interfaceMode === 'elderly'}
              color='#FF8BA7'
              onChange={(e) => void applySetting({ interfaceMode: e.detail.value ? 'elderly' : 'standard' })}
            />
          </View>
          <View className='setting-item column'>
            <View className='setting-info'>
              <Text className='setting-name'>字體倍率</Text>
              <Text className='setting-desc'>把重要內容放大一些，路線、故事與提示都更容易閱讀。</Text>
            </View>
            <View className='scale-options'>
              {scaleOptions.map((option) => (
                <View
                  key={option}
                  className={`scale-options__item ${settings.fontScale === option ? 'active' : ''}`}
                  onClick={() => void applySetting({ fontScale: option })}
                >
                  <Text className='scale-options__text'>{option.toFixed(1)}x</Text>
                </View>
              ))}
            </View>
            <Text className='slider-value'>目前 {settings.fontScale.toFixed(1)}x</Text>
          </View>
        </View>
      </View>

      <View className='settings-section'>
        <View className='section-header'>
          <Text className='section-title'>探索輔助</Text>
        </View>
        <View className='settings-list'>
          <View className='setting-item'>
            <View className='setting-info'>
              <Text className='setting-name'>自動語音導覽</Text>
              <Text className='setting-desc'>走到景點附近時，自動播放語音介紹。</Text>
            </View>
            <Switch
              className='setting-switch'
              checked={settings.voiceGuideEnabled}
              color='#98FB98'
              onChange={(e) => void applySetting({ voiceGuideEnabled: e.detail.value })}
            />
          </View>
          <View className='setting-item'>
            <View className='setting-info'>
              <Text className='setting-name'>高對比模式</Text>
              <Text className='setting-desc'>讓頁面、按鈕與重點文字更醒目。</Text>
            </View>
            <Switch
              className='setting-switch'
              checked={settings.highContrast}
              color='#63B3ED'
              onChange={(e) => void applySetting({ highContrast: e.detail.value })}
            />
          </View>
        </View>
      </View>

      <View className='settings-section tips'>
        <Text className='tips-title'>旅程小提醒</Text>
        <Text className='tips-text'>- 想快速開始，可以先去地圖探索附近地標。</Text>
        <Text className='tips-text'>- 喜歡慢慢走，建議同時開啟大字體與語音導覽。</Text>
        <Text className='tips-text'>- 收集更多印章後，可以回到獎勵頁兌換旅途驚喜。</Text>
      </View>

      <View className='logout-section'>
        <Button className='logout-btn' onClick={() => Taro.switchTab({ url: '/pages/index/index' })}>返回首頁</Button>
      </View>
    </PageShell>
  )
}
