import { useEffect, useState } from 'react'
import { View, Text, Image, Button, ScrollView } from '@tarojs/components'
import Taro from '@tarojs/taro'
import './index.scss'

// 首页 - 游戏入口
export default function IndexPage() {
  const [userInfo, setUserInfo] = useState<any>(null)
  const [stampCount, setStampCount] = useState(0)
  const [level, setLevel] = useState({ level: 1, title: '探索新手' })

  useEffect(() => {
    // 获取用户信息
    const storedUserInfo = Taro.getStorageSync('userInfo')
    if (storedUserInfo) {
      setUserInfo(storedUserInfo)
    }

    // 获取印章数量（Mock数据，后续对接API）
    setStampCount(3)
    
    // 检查是否需要显示新手引导
    checkFirstTimeUser()
  }, [])

  // 检查首次使用
  const checkFirstTimeUser = () => {
    const isFirstTime = !Taro.getStorageSync('hasSeenGuide')
    if (isFirstTime) {
      // 可以在这里显示新手引导弹窗
      console.log('首次使用，显示新手引导')
    }
  }

  // 开始探索 - 跳转到地图页面
  const handleStartExplore = () => {
    Taro.switchTab({
      url: '/pages/map/index'
    })
  }

  // 查看故事线
  const handleViewStories = () => {
    Taro.navigateTo({
      url: '/pages/story/index'
    })
  }

  // 查看印章
  const handleViewStamps = () => {
    Taro.navigateTo({
      url: '/pages/stamps/index'
    })
  }

  return (
    <View className='index-page'>
      {/* 顶部背景区域 */}
      <View className='hero-section'>
        <View className='hero-content'>
          <Text className='app-title'>澳小遊</Text>
          <Text className='app-subtitle'>发现澳门的另一种方式</Text>
          
          {/* 用户等级信息 */}
          <View className='user-level-card'>
            <View className='level-badge'>
              <Text className='level-text'>Lv.{level.level}</Text>
            </View>
            <View className='level-info'>
              <Text className='level-title'>{level.title}</Text>
              <Text className='stamp-count'>已收集 {stampCount} 枚印章</Text>
            </View>
          </View>
        </View>
      </View>

      {/* 主要功能入口 */}
      <View className='main-actions'>
        <View className='action-card primary' onClick={handleStartExplore}>
          <View className='action-icon'>🗺️</View>
          <View className='action-text'>
            <Text className='action-title'>开始探索</Text>
            <Text className='action-desc'>打开地图，发现身边的澳门</Text>
          </View>
        </View>
      </View>

      {/* 快捷入口 */}
      <View className='quick-access'>
        <Text className='section-title'>快捷入口</Text>
        <View className='quick-grid'>
          <View className='quick-item' onClick={handleViewStories}>
            <View className='quick-icon'>📖</View>
            <Text className='quick-text'>故事线</Text>
          </View>
          <View className='quick-item' onClick={handleViewStamps}>
            <View className='quick-icon'>🎯</View>
            <Text className='quick-text'>我的印章</Text>
          </View>
          <View className='quick-item'>
            <View className='quick-icon'>🎁</View>
            <Text className='quick-text'>奖励兑换</Text>
          </View>
          <View className='quick-item'>
            <View className='quick-icon'>👴</View>
            <Text className='quick-text'>长者模式</Text>
          </View>
        </View>
      </View>

      {/* 推荐故事线 */}
      <View className='featured-stories'>
        <Text className='section-title'>推荐探索路线</Text>
        <ScrollView className='story-scroll' scrollX>
          <View className='story-card'>
            <View className='story-cover'>
              <Text className='story-emoji'>🚢</Text>
            </View>
            <View className='story-info'>
              <Text className='story-title'>海上丝路</Text>
              <Text className='story-desc'>澳门开埠历史</Text>
              <Text className='story-progress'>已解锁 2/6 章节</Text>
            </View>
          </View>
          <View className='story-card'>
            <View className='story-cover'>
              <Text className='story-emoji'>⚔️</Text>
            </View>
            <View className='story-info'>
              <Text className='story-title'>东西方战事</Text>
              <Text className='story-desc'>葡澳防卫史</Text>
              <Text className='story-progress'>未开始</Text>
            </View>
          </View>
        </ScrollView>
      </View>

      {/* 底部留白 */}
      <View className='bottom-spacer' />
    </View>
  )
}
