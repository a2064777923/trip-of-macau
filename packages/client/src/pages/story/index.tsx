import { useEffect, useMemo, useState } from 'react'
import { Button, ScrollView, Text, View } from '@tarojs/components'
import Taro from '@tarojs/taro'
import { getStorylines, loadGameState } from '../../services/gameService'
import './index.scss'

export default function StoryPage() {
  const [stories, setStories] = useState([])
  const state = useMemo(() => loadGameState(), [])

  useEffect(() => {
    setStories(getStorylines())
  }, [])

  const handleStartStory = (storyId) => {
    const story = stories.find((item) => item.id === storyId)
    if (!story) return

    Taro.showModal({
      title: `开始 ${story.name}`,
      content: `这条故事线包含 ${story.totalChapters} 个章节，建议预留 ${story.estimatedTime}。沿途会依次解鎖地標故事與小驚喜。`,
      confirmText: '进入探索',
      success: (res) => {
        if (res.confirm) {
          Taro.switchTab({ url: '/pages/map/index' })
        }
      },
    })
  }

  return (
    <View className='story-page'>
      <View className='page-header'>
        <Text className='page-title'>探索故事线</Text>
        <Text className='page-subtitle'>MVP 先上兩條主線：海上絲路與東西方戰事，故事推進由打卡節點驅動。</Text>
      </View>

      <View className='story-summary'>
        <View className='story-summary__card'>
          <Text className='story-summary__value'>{state.user.totalStamps}</Text>
          <Text className='story-summary__label'>累計印章</Text>
        </View>
        <View className='story-summary__card'>
          <Text className='story-summary__value'>{stories.length}</Text>
          <Text className='story-summary__label'>可玩故事</Text>
        </View>
        <View className='story-summary__card'>
          <Text className='story-summary__value'>{state.completedStoryIds.length}</Text>
          <Text className='story-summary__label'>已解鎖主線</Text>
        </View>
      </View>

      <ScrollView className='story-list' scrollY>
        {stories.map((story, index) => (
          <View
            key={story.id}
            className='story-card'
            style={{ animationDelay: `${index * 0.08}s` }}
          >
            <View className='story-cover' style={{ background: story.coverColor }}>
              <Text className='story-icon'>{story.icon}</Text>
              <View className='difficulty-badge'>
                <Text className='difficulty-text'>{story.difficulty === 'easy' ? '簡單' : story.difficulty === 'medium' ? '中等' : '困難'}</Text>
              </View>
            </View>

            <View className='story-content'>
              <View className='story-header'>
                <Text className='story-name'>{story.name}</Text>
                <Text className='story-en'>{story.nameEn}</Text>
              </View>

              <Text className='story-description'>{story.description}</Text>

              <View className='story-progress'>
                <View className='progress-header'>
                  <Text className='progress-text'>已完成 {story.completedChapters}/{story.totalChapters} 章</Text>
                  <Text className='progress-percent'>{story.progress}%</Text>
                </View>
                <View className='progress-bar'>
                  <View className='progress-fill' style={{ width: `${story.progress}%` }} />
                </View>
              </View>

              <View className='story-chapters'>
                {story.chapterTitles.slice(0, 3).map((chapter) => (
                  <Text key={chapter} className='story-chapter'>• {chapter}</Text>
                ))}
              </View>

              <View className='story-meta'>
                <View className='meta-item'>
                  <Text className='meta-icon'>⏱️</Text>
                  <Text className='meta-text'>{story.estimatedTime}</Text>
                </View>
                <View className='meta-item'>
                  <Text className='meta-icon'>📍</Text>
                  <Text className='meta-text'>{story.poiIds.length} 个地点</Text>
                </View>
                <View className='meta-item'>
                  <Text className='meta-icon'>🎁</Text>
                  <Text className='meta-text'>{story.rewardBadge || '印章奖励'}</Text>
                </View>
              </View>

              <Button className='story-start-btn' onClick={() => handleStartStory(story.id)}>去地圖繼續探索</Button>
            </View>
          </View>
        ))}

        <View className='bottom-spacer' />
      </ScrollView>
    </View>
  )
}

