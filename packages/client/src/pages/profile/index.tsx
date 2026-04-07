import { useState, useEffect } from 'react'
import { View, Text, Button, Image } from '@tarojs/components'
import Taro from '@tarojs/taro'
import './index.scss'

// 个人中心页面
export default function ProfilePage() {
  const [userInfo, setUserInfo] = useState<any>(null)
  const [stats, setStats] = useState({
    stamps: 12,
    stories: 2,
    pois: 8,
    level: 3
  })
  const [achievements, setAchievements] = useState<any[]>([])

  useEffect(() => {
    loadUserInfo()
    loadAchievements()
  }, [])

  // 加载用户信息
  const loadUserInfo = () => {
    const storedUserInfo = Taro.getStorageSync('userInfo')
    if (storedUserInfo) {
      setUserInfo(storedUserInfo)
    } else {
      setUserInfo({
        nickname: '探索者',
        avatarUrl: '',
        level: 3,
        title: '澳门见习生'
      })
    }
  }

  // 加载成就
  const loadAchievements = () => {
    setAchievements([
      { id: 1, name: '初探澳门', desc: '收集3个印章', icon: '🎯', completed: true },
      { id: 2, name: '历史爱好者', desc: '完成1条故事线', icon: '📖', completed: true },
      { id: 3, name: '走遍澳门', desc: '收集15个印章', icon: '🏃', completed: false },
      { id: 4, name: '故事大师', desc: '完成3条故事线', icon: '🎭', completed: false }
    ])
  }

  // 编辑资料
  const handleEditProfile = () => {
    Taro.navigateTo({
      url: '/pages/profile/edit/index'
    })
  }

  // 查看设置
  const handleSettings = () => {
    Taro.navigateTo({
      url: '/pages/settings/index'
    })
  }

  // 查看印章
  const handleViewStamps = () => {
    Taro.navigateTo({
      url: '/pages/stamps/index'
    })
  }

  // 查看故事
  const handleViewStories = () => {
    Taro.navigateTo({
      url: '/pages/story/index'
    })
  }

  return (
    <View className='profile-page'>
      {/* 头部背景 */}
      <View className='header-bg'>
        <View className='header-content'>
          {/* 设置按钮 */}
          <View className='settings-btn' onClick={handleSettings}>
            <Text className='settings-icon'>⚙️</Text>
          </View>

          {/* 用户信息 */}
          <View className='user-info'>
            <View className='avatar-wrap'>
              {userInfo?.avatarUrl ? (
                <Image className='avatar' src={userInfo.avatarUrl} mode='aspectFill' />
              ) : (
                <View className='avatar-placeholder'>👤</View>
              )}
              <View className='level-badge'>
                <Text className='level-text'>Lv.{stats.level}</Text>
              </View>
            </View>

            <View className='user-details'>
              <Text className='nickname'>{userInfo?.nickname || '探索者'}</Text>
              <Text className='title'>🏆 {userInfo?.title || '澳门见习生'}</Text>
            </View>

            <View className='edit-btn' onClick={handleEditProfile}>
              <Text className='edit-text'>编辑资料</Text>
            </View>
          </View>
        </View>
      </View>

      {/* 数据统计 */}
      <View className='stats-section'>
        <View className='stats-grid'>
          <View className='stat-item' onClick={handleViewStamps}>
            <Text className='stat-number'>{stats.stamps}</Text>
            <Text className='stat-label'>印章</Text>
          </View>
          <View className='stat-divider' />
          <View className='stat-item' onClick={handleViewStories}>
            <Text className='stat-number'>{stats.stories}</Text>
            <Text className='stat-label'>故事</Text>
          </View>
          <View className='stat-divider' />
          <View className='stat-item'>
            <Text className='stat-number'>{stats.pois}</Text>
            <Text className='stat-label'>地点</Text>
          </View>
        </View>
      </View>

      {/* 成就列表 */}
      <View className='achievements-section'>
        <View className='section-header'>
          <Text className='section-title'>🏆 我的成就</Text>
          <Text className='section-subtitle'>
            {achievements.filter(a => a.completed).length}/{achievements.length} 已完成
          </Text>
        </View>

        <View className='achievements-list'>
          {achievements.map((achievement) => (
            <View 
              key={achievement.id}
              className={`achievement-item ${achievement.completed ? 'completed' : ''}`}
            >
              <View className='achievement-icon-wrap'>
                <Text className='achievement-icon'>{achievement.icon}</Text>
                {achievement.completed && (
                  <View className='completed-mark'>✓</View>
                )}
              </View>
              <View className='achievement-info'>
                <Text className='achievement-name'>{achievement.name}</Text>
                <Text className='achievement-desc'>{achievement.desc}</Text>
              </View>
            </View>
          ))}
        </View>
      </View>

      {/* 底部留白 */}
      <View className='bottom-spacer' />
    </View>
  )
}
