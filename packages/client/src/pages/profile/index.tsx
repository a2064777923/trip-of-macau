import { useEffect, useState } from 'react'
import { Button, Image, Text, View } from '@tarojs/components'
import Taro from '@tarojs/taro'
import {
  getCheckinHistory,
  getStorylines,
  isDevBypassAvailable,
  loadGameState,
  loginWithDevBypass,
  loginWithWeChat,
  refreshPublicContent,
} from '../../services/gameService'
import { cosAssetManifest } from '../../constants/assetUrls'
import PageShell from '../../components/PageShell'
import './index.scss'

export default function ProfilePage() {
  const [state, setState] = useState(() => loadGameState())
  const [stories, setStories] = useState(() => getStorylines())
  const [history, setHistory] = useState(() => getCheckinHistory())
  const anonymous = state.user.authStatus === 'anonymous'

  useEffect(() => {
    let cancelled = false

    const hydrateProfile = async () => {
      try {
        await refreshPublicContent()
      } catch (error) {
        console.warn('Failed to refresh profile content.', error)
      }

      if (!cancelled) {
        setState(loadGameState())
        setStories(getStorylines())
        setHistory(getCheckinHistory())
      }
    }

    void hydrateProfile()

    return () => {
      cancelled = true
    }
  }, [])

  const syncLocalProfile = () => {
    setState(loadGameState())
    setStories(getStorylines())
    setHistory(getCheckinHistory())
  }

  const handleWechatLogin = async () => {
    try {
      await loginWithWeChat()
      syncLocalProfile()
      Taro.showToast({ title: '登入成功', icon: 'success' })
    } catch (error) {
      Taro.showToast({ title: error instanceof Error ? error.message : '登入未完成', icon: 'none' })
    }
  }

  const handleDevBypassLogin = async () => {
    try {
      await loginWithDevBypass()
      syncLocalProfile()
      Taro.showToast({ title: '本地調試登入成功', icon: 'success' })
    } catch (error) {
      Taro.showToast({ title: error instanceof Error ? error.message : '本地調試登入失敗', icon: 'none' })
    }
  }

  if (anonymous) {
    return (
      <PageShell className='profile-page'>
        <View className='header-bg'>
          <View className='header-content'>
            <View className='user-info'>
              <View className='avatar-wrap'>
                <Image className='avatar-image avatar-image--placeholder' src={cosAssetManifest.brand.appLogoMain} mode='aspectFill' />
              </View>

              <View className='user-details'>
                <Text className='nickname'>登入後開啟我的旅程</Text>
                <Text className='title'>微信帳號登入</Text>
                <Text className='subtitle'>未登入時可瀏覽首頁、地圖、發現、秘笈與故事內容，但無法使用互動與個人功能。</Text>
              </View>
            </View>

            <Button className='wechat-login-btn' onClick={handleWechatLogin}>使用微信登入</Button>
            {isDevBypassAvailable() && (
              <Button className='wechat-login-btn' onClick={handleDevBypassLogin}>本地調試登入</Button>
            )}
          </View>
        </View>
      </PageShell>
    )
  }

  return (
    <PageShell className='profile-page'>
      <View className='header-bg'>
        <View className='header-content'>
          <View className='settings-btn' onClick={() => Taro.navigateTo({ url: '/pages/settings/index' })}>
            <Text className='settings-icon'>⚙</Text>
          </View>

          <View className='user-info'>
            <View className='avatar-wrap'>
              {state.user.avatarUrl ? (
                <Image className='avatar-image' src={state.user.avatarUrl} mode='aspectFill' />
              ) : (
                <Image className='avatar-image avatar-image--placeholder' src={cosAssetManifest.brand.appLogoMain} mode='aspectFill' />
              )}

              <View className='level-badge'>
                <Text className='level-text'>Lv.{state.user.level}</Text>
              </View>
            </View>

            <View className='user-details'>
              <Text className='nickname'>{state.user.nickname}</Text>
              <Text className='title'>🎖 {state.user.title}</Text>
              <Text className='subtitle'>今日旅程已展開，去解鎖下一枚足跡章吧。</Text>
            </View>
          </View>
        </View>
      </View>

      <View className='stats-section'>
        <View className='stats-grid'>
          <View className='stat-item' onClick={() => Taro.navigateTo({ url: '/pages/stamps/index' })}>
            <Text className='stat-number'>{state.user.totalStamps}</Text>
            <Text className='stat-label'>印章</Text>
          </View>
          <View className='stat-divider' />
          <View className='stat-item' onClick={() => Taro.navigateTo({ url: '/pages/story/index' })}>
            <Text className='stat-number'>{stories.length}</Text>
            <Text className='stat-label'>故事</Text>
          </View>
          <View className='stat-divider' />
          <View className='stat-item' onClick={() => Taro.switchTab({ url: '/pages/map/index' })}>
            <Text className='stat-number'>{history.length}</Text>
            <Text className='stat-label'>足跡</Text>
          </View>
        </View>
      </View>

      <View className='achievements-section'>
        <View className='section-header'>
          <Text className='section-title'>🎆 最近旅程</Text>
          <Text className='section-subtitle'>{history.length} 段回憶</Text>
        </View>

        <View className='timeline-list'>
          {history.length ? history.map((item) => (
            <View key={`${item.poiId}-${item.checkedAt}`} className='timeline-item'>
              <View className='timeline-item__dot' />
              <View className='timeline-item__content'>
                <Text className='timeline-item__title'>{item.poiName}</Text>
                <Text className='timeline-item__desc'>
                  收下 {item.stampName}，+{item.experienceGained} EXP，
                  {item.triggerMode === 'gps' ? '到站解鎖' : item.triggerMode === 'manual' ? '補簽完成' : '旅程記錄'}
                </Text>
              </View>
            </View>
          )) : (
            <View className='empty-panel'>
              <Text className='empty-panel__emoji'>🧭</Text>
              <Text className='empty-panel__text'>還沒有新的旅程回憶，去探索下一個地標吧。</Text>
            </View>
          )}
        </View>
      </View>
    </PageShell>
  )
}
