import { useState, useEffect } from 'react'
import { View, Text, Image, ScrollView } from '@tarojs/components'
import Taro from '@tarojs/taro'
import './index.scss'

// 故事线页面
export default function StoryPage() {
  const [stories, setStories] = useState<any[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    loadStories()
  }, [])

  // 加载故事线数据
  const loadStories = async () => {
    try {
      // 模拟API调用
      const mockStories = [
        {
          id: 1,
          name: '海上丝路',
          nameEn: 'Maritime Silk Road',
          description: '澳门开埠历史，从大航海时代到东方明珠',
          icon: '🚢',
          coverColor: '#4A90D9',
          totalChapters: 6,
          completedChapters: 2,
          progress: 33,
          estimatedTime: '2-3小时',
          difficulty: 'easy',
          pois: [
            { id: 3, name: '妈阁庙', completed: true },
            { id: 4, name: '港务局大楼', completed: false }
          ]
        },
        {
          id: 2,
          name: '东西方战事',
          nameEn: 'East Meets West',
          description: '葡澳防卫史，见证中西方文明的碰撞与融合',
          icon: '⚔️',
          coverColor: '#E74C3C',
          totalChapters: 8,
          completedChapters: 0,
          progress: 0,
          estimatedTime: '3-4小时',
          difficulty: 'medium',
          pois: [
            { id: 1, name: '大三巴牌坊', completed: false },
            { id: 2, name: '议事亭前地', completed: false },
            { id: 5, name: '玫瑰堂', completed: false }
          ]
        },
        {
          id: 3,
          name: '家族故事',
          nameEn: 'Family Stories',
          description: '土生葡人历史，一段跨越几代人的家国情怀',
          icon: '🏠',
          coverColor: '#9B59B6',
          totalChapters: 5,
          completedChapters: 0,
          progress: 0,
          estimatedTime: '2小时',
          difficulty: 'easy',
          pois: [
            { id: 6, name: '龙嵩街', completed: false },
            { id: 7, name: '岗顶剧院', completed: false }
          ]
        }
      ]

      setStories(mockStories)
    } catch (error) {
      console.error('加载故事线失败:', error)
      Taro.showToast({ title: '加载失败', icon: 'none' })
    } finally {
      setLoading(false)
    }
  }

  // 查看故事详情
  const handleStoryClick = (storyId: number) => {
    Taro.navigateTo({
      url: `/pages/story/detail/index?id=${storyId}`
    })
  }

  // 渲染进度条
  const renderProgress = (progress: number) => {
    return (
      <View className='progress-bar'>
        <View 
          className='progress-fill' 
          style={{ width: `${progress}%` }}
        />
      </View>
    )
  }

  return (
    <View className='story-page'>
      {/* 页面标题 */}
      <View className='page-header'>
        <Text className='page-title'>探索故事线</Text>
        <Text className='page-subtitle'>通过故事了解澳门的历史与文化</Text>
      </View>

      {/* 故事线列表 */}
      <ScrollView className='story-list' scrollY>
        {loading ? (
          <View className='loading-state'>
            <Text className='loading-text'>加载中...</Text>
          </View>
        ) : (
          stories.map((story, index) => (
            <View 
              key={story.id}
              className='story-card'
              style={{ animationDelay: `${index * 0.1}s` }}
              onClick={() => handleStoryClick(story.id)}
            >
              {/* 封面 */}
              <View 
                className='story-cover'
                style={{ background: story.coverColor }}
              >
                <Text className='story-icon'>{story.icon}</Text>
                <View className='difficulty-badge'>
                  <Text className='difficulty-text'>
                    {story.difficulty === 'easy' ? '简单' : 
                     story.difficulty === 'medium' ? '中等' : '困难'}
                  </Text>
                </View>
              </View>

              {/* 内容 */}
              <View className='story-content'>
                <View className='story-header'>
                  <Text className='story-name'>{story.name}</Text>
                  <Text className='story-en'>{story.nameEn}</Text>
                </View>

                <Text className='story-description'>{story.description}</Text>

                {/* 进度 */}
                <View className='story-progress'>
                  <View className='progress-header'>
                    <Text className='progress-text'>
                      已完成 {story.completedChapters}/{story.totalChapters} 章节
                    </Text>
                    <Text className='progress-percent'>{story.progress}%</Text>
                  </View>
                  {renderProgress(story.progress)}
                </View>

                {/* 元信息 */}
                <View className='story-meta'>
                  <View className='meta-item'>
                    <Text className='meta-icon'>⏱️</Text>
                    <Text className='meta-text'>{story.estimatedTime}</Text>
                  </View>
                  <View className='meta-item'>
                    <Text className='meta-icon'>📍</Text>
                    <Text className='meta-text'>{story.pois.length} 个地点</Text>
                  </View>
                </View>
              </View>
            </View>
          ))
        )}

        {/* 底部留白 */}
        <View className='bottom-spacer' />
      </ScrollView>
    </View>
  )
}
