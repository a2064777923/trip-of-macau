import { useEffect, useMemo, useState } from 'react'
import { ScrollView, Text, View } from '@tarojs/components'
import Taro from '@tarojs/taro'
import { getRewards, getStorylines, loadGameState } from '../../services/gameService'
import PageShell from '../../components/PageShell'
import './index.scss'

export default function IndexPage() {
  const [stories, setStories] = useState([])
  const [rewards, setRewards] = useState([])
  const state = useMemo(() => loadGameState(), [])

  useEffect(() => {
    setStories(getStorylines())
    setRewards(getRewards())
  }, [])

  const featuredReward = rewards[0]

  return (
    <PageShell className='index-page'>
      <View className='hero-section'>
        <View className='hero-content'>
          <Text className='app-title'>澳小遊</Text>
          <Text className='app-subtitle'>跟着故事去散步，把沿途的风景、印章和惊喜都收进行囊里。</Text>

          <View className='user-level-card'>
            <View className='level-badge'>
              <Text className='level-text'>Lv.{state.user.level}</Text>
            </View>
            <View className='level-info'>
              <Text className='level-title'>{state.user.title}</Text>
              <Text className='stamp-count'>已收集 {state.user.totalStamps} 枚印章 · 已解鎖 {state.user.unlockedStorylines} 條主線</Text>
            </View>
          </View>
        </View>
      </View>

      <View className='main-actions'>
        <View className='action-card primary' onClick={() => Taro.switchTab({ url: '/pages/map/index' })}>
          <View className='action-icon'>🗺️</View>
          <View className='action-text'>
            <Text className='action-title'>開始探索</Text>
            <Text className='action-desc'>看看附近有哪些地標和彩蛋，朝下一枚足跡章出發。</Text>
          </View>
        </View>
      </View>

      <View className='quick-access'>
        <Text className='section-title'>今天想怎麼玩</Text>
        <View className='quick-grid'>
          <View className='quick-item' onClick={() => Taro.navigateTo({ url: '/pages/story/index' })}>
            <View className='quick-icon'>📖</View>
            <Text className='quick-text'>故事主線</Text>
          </View>
          <View className='quick-item' onClick={() => Taro.switchTab({ url: '/pages/map/index' })}>
            <View className='quick-icon'>📍</View>
            <Text className='quick-text'>附近打卡</Text>
          </View>
          <View className='quick-item' onClick={() => Taro.navigateTo({ url: '/pages/stamps/index' })}>
            <View className='quick-icon'>🏆</View>
            <Text className='quick-text'>印章圖鑑</Text>
          </View>
          <View className='quick-item' onClick={() => Taro.navigateTo({ url: '/pages/rewards/index' })}>
            <View className='quick-icon'>🎁</View>
            <Text className='quick-text'>獎勵兌換</Text>
          </View>
        </View>
      </View>

      <View className='featured-stories'>
        <Text className='section-title'>推薦探索路线</Text>
        <ScrollView className='story-scroll' scrollX>
          {stories.map((story) => (
            <View key={story.id} className='story-card' onClick={() => Taro.navigateTo({ url: '/pages/story/index' })}>
              <View className='story-cover' style={{ background: story.coverColor }}>
                <Text className='story-emoji'>{story.icon}</Text>
              </View>
              <View className='story-info'>
                <Text className='story-title'>{story.name}</Text>
                <Text className='story-desc'>{story.description}</Text>
                <Text className='story-progress'>進度 {story.progress}% · {story.estimatedTime}</Text>
              </View>
            </View>
          ))}
        </ScrollView>
      </View>

      {featuredReward && (
        <View className='reward-highlight' onClick={() => Taro.navigateTo({ url: '/pages/rewards/index' })}>
          <Text className='reward-highlight__label'>熱門獎勵</Text>
          <Text className='reward-highlight__title'>{featuredReward.icon} {featuredReward.name}</Text>
          <Text className='reward-highlight__desc'>{featuredReward.description}</Text>
        </View>
      )}

      <View className='bottom-spacer' />
    </PageShell>
  )
}


