import { useEffect, useState } from 'react'
import { Button, Switch, Text, View } from '@tarojs/components'
import Taro from '@tarojs/taro'
import { loadGameState, updateUserPreference } from '../../services/gameService'
import PageShell from '../../components/PageShell'
import './index.scss'

const scaleOptions = [1, 1.2, 1.4, 1.6, 1.8, 2]

export default function SettingsPage() {
  const [settings, setSettings] = useState({
    interfaceMode: 'standard',
    fontScale: 1,
    highContrast: false,
    voiceGuideEnabled: false,
  })

  useEffect(() => {
    const state = loadGameState()
    setSettings({
      interfaceMode: state.user.interfaceMode,
      fontScale: state.user.fontScale,
      highContrast: state.user.highContrast,
      voiceGuideEnabled: state.user.voiceGuideEnabled,
    })
  }, [])

  const applySetting = (patch) => {
    const next = { ...settings, ...patch }
    setSettings(next)
    updateUserPreference(next)
  }

  return (
    <PageShell className='settings-page'>
      <View className='page-header'>
        <Text className='page-title'>設定</Text>
        <Text className='page-subtitle'>把旅程调成你最舒服的节奏，字體大小、語音提醒與閱讀對比都可以自己決定。</Text>
      </View>

      <View className='settings-section'>
        <View className='section-header'>
          <Text className='section-title'>👴 長者模式</Text>
        </View>
        <View className='settings-list'>
          <View className='setting-item'>
            <View className='setting-info'>
              <Text className='setting-name'>開啟長者模式</Text>
              <Text className='setting-desc'>界面更清爽、按钮更大，每一步都更好看也更好点。</Text>
            </View>
            <Switch
              className='setting-switch'
              checked={settings.interfaceMode === 'elderly'}
              color='#FF8BA7'
              onChange={(e) => applySetting({ interfaceMode: e.detail.value ? 'elderly' : 'standard' })}
            />
          </View>
          <View className='setting-item column'>
            <View className='setting-info'>
              <Text className='setting-name'>字體倍率</Text>
              <Text className='setting-desc'>把重要内容放大一点，看路牌和故事会更轻松。</Text>
            </View>
            <View className='scale-options'>
              {scaleOptions.map((option) => (
                <View
                  key={option}
                  className={`scale-options__item ${settings.fontScale === option ? 'active' : ''}`}
                  onClick={() => applySetting({ fontScale: option })}
                >
                  <Text className='scale-options__text'>{option.toFixed(1)}x</Text>
                </View>
              ))}
            </View>
            <Text className='slider-value'>当前 {settings.fontScale.toFixed(1)}x</Text>
          </View>
        </View>
      </View>

      <View className='settings-section'>
        <View className='section-header'>
          <Text className='section-title'>🔊 探索体验</Text>
        </View>
        <View className='settings-list'>
          <View className='setting-item'>
            <View className='setting-info'>
              <Text className='setting-name'>自动语音导览</Text>
              <Text className='setting-desc'>走到景点附近时，让介绍自己响起，像有位同行导览员陪着你。</Text>
            </View>
            <Switch
              className='setting-switch'
              checked={settings.voiceGuideEnabled}
              color='#98FB98'
              onChange={(e) => applySetting({ voiceGuideEnabled: e.detail.value })}
            />
          </View>
          <View className='setting-item'>
            <View className='setting-info'>
              <Text className='setting-name'>高对比度</Text>
              <Text className='setting-desc'>让标题、按钮和重点信息更醒目，白天户外查看也更清楚。</Text>
            </View>
            <Switch
              className='setting-switch'
              checked={settings.highContrast}
              color='#63B3ED'
              onChange={(e) => applySetting({ highContrast: e.detail.value })}
            />
          </View>
        </View>
      </View>

      <View className='settings-section tips'>
        <Text className='tips-title'>旅程小提醒</Text>
        <Text className='tips-text'>- 想快速开始，就先去探索页挑一个附近地标</Text>
        <Text className='tips-text'>- 如果喜歡慢慢走，建議打開語音導覽和更大的字體</Text>
        <Text className='tips-text'>- 收集到更多足跡章后，可以回来兑换旅途惊喜</Text>
      </View>

      <View className='logout-section'>
        <Button className='logout-btn' onClick={() => Taro.switchTab({ url: '/pages/index/index' })}>返回首页</Button>
      </View>
    </PageShell>
  )
}



