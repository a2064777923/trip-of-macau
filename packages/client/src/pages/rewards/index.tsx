import { useEffect, useState } from 'react'
import { Button, ScrollView, Text, View } from '@tarojs/components'
import Taro from '@tarojs/taro'
import { getRewards, loadGameState, redeemReward } from '../../services/gameService'
import PageShell from '../../components/PageShell'
import './index.scss'

export default function RewardsPage() {
  const [rewards, setRewards] = useState([])
  const [state, setState] = useState(() => loadGameState())

  useEffect(() => {
    setRewards(getRewards())
    setState(loadGameState())
  }, [])

  const refreshRewardState = () => {
    setRewards(getRewards())
    setState(loadGameState())
  }

  const handleRedeem = (reward) => {
    if (reward.status === 'coming_soon') {
      Taro.showToast({ title: '该奖励即将开放', icon: 'none' })
      return
    }

    if (reward.status === 'redeemed') {
      Taro.showToast({ title: '你已领取过该奖励', icon: 'none' })
      return
    }

    try {
      const redeemed = redeemReward(reward.id)
      refreshRewardState()
      Taro.showToast({ title: `${redeemed.name} 兑换成功`, icon: 'success' })
    } catch (error) {
      Taro.showToast({ title: error.message || '兑换失败', icon: 'none' })
    }
  }

  return (
    <PageShell className='rewards-page'>
      <View className='rewards-hero'>
        <Text className='rewards-hero__title'>奖励兑换</Text>
        <Text className='rewards-hero__subtitle'>把一路收集的足跡章換成旅途驚喜，留住這趟澳門慢遊的紀念。</Text>
        <View className='rewards-hero__badge'>当前可用印章：{state.user.totalStamps}</View>
      </View>

      <ScrollView className='rewards-list' scrollY>
        {rewards.map((reward) => {
          const canRedeem = state.user.totalStamps >= reward.stampCost && reward.status === 'available'
          return (
            <View key={reward.id} className='reward-card'>
              <View className='reward-card__icon'>{reward.icon}</View>
              <View className='reward-card__body'>
                <View className='reward-card__top'>
                  <Text className='reward-card__name'>{reward.name}</Text>
                  <Text className={`reward-card__status ${reward.status}`}>{canRedeem ? '可兑换' : reward.status === 'coming_soon' ? '即将开放' : '已领取'}</Text>
                </View>
                <Text className='reward-card__subtitle'>{reward.subtitle}</Text>
                <Text className='reward-card__desc'>{reward.description}</Text>
                <Text className='reward-card__meta'>需 {reward.stampCost} 枚印章 · 库存 {reward.inventory}</Text>
                <Text className='reward-card__highlight'>{reward.highlight}</Text>
              </View>
              <Button
                className={`reward-card__btn ${canRedeem ? 'active' : ''}`}
                onClick={() => handleRedeem(reward)}
              >
                {reward.status === 'redeemed' ? '已完成兑换' : canRedeem ? '立即兑换' : reward.status === 'coming_soon' ? '敬请期待' : '继续探索'}
              </Button>
            </View>
          )
        })}
        <View className='bottom-spacer' />
      </ScrollView>
    </PageShell>
  )
}

