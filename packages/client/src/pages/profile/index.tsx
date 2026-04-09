import { useMemo, useState } from 'react'
import { Button, Image, Text, View } from '@tarojs/components'
import Taro from '@tarojs/taro'
import { getCheckinHistory, getStorylines, loadGameState, loginWithWeChatProfile } from '../../services/gameService'
import PageShell from '../../components/PageShell'
import './index.scss'

export default function ProfilePage() {
  const [state, setState] = useState(() => loadGameState())
  const stories = useMemo(() => getStorylines(), [])
  const history = useMemo(() => getCheckinHistory(), [])

  const handleWechatLogin = async () => {
    try {
      const user = await loginWithWeChatProfile()
      setState((prev) => ({
        ...prev,
        user,
      }))
      Taro.showToast({ title: '旅人名片已同步', icon: 'success' })
    } catch (error) {
      Taro.showToast({ title: '暂未完成授权', icon: 'none' })
    }
  }

  return (
    <PageShell className='profile-page'>
      <View className='header-bg'>
        <View className='header-content'>
          <View className='settings-btn' onClick={() => Taro.navigateTo({ url: '/pages/settings/index' })}>
            <Text className='settings-icon'>⚙️</Text>
          </View>

          <View className='user-info'>
            <View className='avatar-wrap'>
              {state.user.avatarUrl ? (
                <Image className='avatar-image' src={state.user.avatarUrl} mode='aspectFill' />
              ) : (
                <View className='avatar-placeholder'>🧭</View>
              )}
              <View className='level-badge'>
                <Text className='level-text'>Lv.{state.user.level}</Text>
              </View>
            </View>

            <View className='user-details'>
              <Text className='nickname'>{state.user.nickname}</Text>
              <Text className='title'>🏆 {state.user.title}</Text>
              <Text className='subtitle'>{state.user.isGuest ? '登入後可同步你的旅人名片與探索足跡' : '今日旅程已展開，去解鎖下一枚足跡章吧'}</Text>
            </View>
          </View>

          {state.user.isGuest && (
            <Button className='wechat-login-btn' onClick={handleWechatLogin}>使用微信頭像暱稱登入</Button>
          )}
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
          <Text className='section-title'>🎯 最近旅程</Text>
          <Text className='section-subtitle'>{history.length} 段回忆</Text>
        </View>

        <View className='timeline-list'>
          {history.length ? history.map((item) => (
            <View key={`${item.poiId}-${item.checkedAt}`} className='timeline-item'>
              <View className='timeline-item__dot' />
              <View className='timeline-item__content'>
                <Text className='timeline-item__title'>{item.poiName}</Text>
                <Text className='timeline-item__desc'>收下 {item.stampName} · +{item.experienceGained} EXP · {item.triggerMode === 'gps' ? '到站解鎖' : item.triggerMode === 'manual' ? '補簽成功' : '旅程紀錄已補全'}</Text>
              </View>
            </View>
          )) : (
            <View className='empty-panel'>
              <Text className='empty-panel__emoji'>🌤️</Text>
              <Text className='empty-panel__text'>還沒有新的旅程回憶，去探索頁走走吧。</Text>
            </View>
          )}
        </View>
      </View>
    </PageShell>
  )
}

