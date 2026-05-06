import { useEffect, useState } from 'react'
import { Button, ScrollView, Text, View } from '@tarojs/components'
import Taro from '@tarojs/taro'
import {
  getRewards,
  isAuthRequiredError,
  loadGameState,
  redeemReward,
  refreshPublicContent,
  requireAuth,
} from '../../services/gameService'
import PageShell from '../../components/PageShell'
import './index.scss'

export default function RewardsPage() {
  const [rewards, setRewards] = useState(() => getRewards())
  const [state, setState] = useState(() => loadGameState())

  const syncRewardState = () => {
    setRewards(getRewards())
    setState(loadGameState())
  }

  const refreshRewardState = async () => {
    try {
      await refreshPublicContent()
    } catch (error) {
      console.warn('Failed to refresh rewards.', error)
    }
    syncRewardState()
  }

  useEffect(() => {
    void refreshRewardState()
  }, [])

  useEffect(() => {
    if (state.user.authStatus === 'anonymous') {
      void requireAuth('查看獎勵前，請先使用微信登入。')
    }
  }, [state.user.authStatus])

  const handleRedeem = async (reward: ReturnType<typeof getRewards>[number]) => {
    if (reward.status === 'coming_soon') {
      Taro.showToast({ title: '此獎勵即將開放', icon: 'none' })
      return
    }

    if (reward.status === 'redeemed') {
      Taro.showToast({ title: '你已兌換過這個獎勵', icon: 'none' })
      return
    }

    try {
      const redeemed = await redeemReward(reward.id)
      syncRewardState()
      Taro.showToast({ title: `${redeemed.name} 兌換成功`, icon: 'success' })
    } catch (error) {
      if (!isAuthRequiredError(error)) {
        Taro.showToast({ title: error instanceof Error ? error.message : '兌換失敗', icon: 'none' })
      }
    }
  }

  const handleContinueExploration = () => {
    void Taro.switchTab({ url: '/pages/map/index' })
  }

  if (state.user.authStatus === 'anonymous') {
    return (
      <PageShell className='rewards-page'>
        <View className='rewards-hero'>
          <Text className='rewards-hero__title'>需要登入</Text>
          <Text className='rewards-hero__subtitle'>正在前往「我的」頁面完成登入。</Text>
        </View>
      </PageShell>
    )
  }

  return (
    <PageShell className='rewards-page'>
      <View className='rewards-hero'>
        <Text className='rewards-hero__title'>獎勵兌換</Text>
        <Text className='rewards-hero__subtitle'>把一路收集的印章兌換成旅途驚喜，留下這段探索的紀念。</Text>
        <View className='rewards-hero__badge'>目前可用印章：{state.user.totalStamps}</View>
      </View>

      <ScrollView className='rewards-list' scrollY>
        {rewards.map((reward) => {
          const canRedeem = state.user.totalStamps >= reward.stampCost && reward.status === 'available'
          const isStampShort = reward.status === 'available' && state.user.totalStamps < reward.stampCost
          const statusText = reward.status === 'redeemed'
            ? '已兌換'
            : reward.status === 'coming_soon'
              ? '即將開放'
              : canRedeem
                ? '可兌換'
                : '印章不足'
          return (
            <View key={reward.id} className='reward-card'>
              <View className='reward-card__icon'>{reward.icon}</View>
              <View className='reward-card__body'>
                <View className='reward-card__top'>
                  <Text className='reward-card__name'>{reward.name}</Text>
                  <Text className={`reward-card__status ${reward.status} ${isStampShort ? 'short' : ''}`}>
                    {statusText}
                  </Text>
                </View>
                <Text className='reward-card__subtitle'>{reward.subtitle}</Text>
                <Text className='reward-card__desc'>{reward.description}</Text>
                <Text className='reward-card__meta'>
                  {reward.status === 'coming_soon'
                    ? '這份獎勵仍在準備中'
                    : `需要 ${reward.stampCost} 枚印章`}
                </Text>
                <Text className='reward-card__highlight'>{reward.highlight}</Text>
              </View>
              <Button
                className={`reward-card__btn ${canRedeem ? 'active' : ''}`}
                onClick={() => {
                  if (canRedeem || reward.status === 'redeemed' || reward.status === 'coming_soon') {
                    void handleRedeem(reward)
                    return
                  }
                  handleContinueExploration()
                }}
              >
                {reward.status === 'redeemed' ? '已完成兌換' : canRedeem ? '立即兌換' : reward.status === 'coming_soon' ? '稍後回來看看' : '回到探索路線'}
              </Button>
            </View>
          )
        })}
        <View className='bottom-spacer' />
      </ScrollView>
    </PageShell>
  )
}
