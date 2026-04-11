import { useEffect, useMemo, useState } from 'react'
import { Button, ScrollView, Text, View } from '@tarojs/components'
import Taro from '@tarojs/taro'
import PageShell from '../../components/PageShell'
import { getRewards, getStorylines, getTravelRecommendation, loadGameState, saveTravelAssessment } from '../../services/gameService'
import './index.scss'

const ageGroups = ['18歲以下', '18-30歲', '31-55歲', '55歲以上']
const durations = ['1小時內', '半天慢遊', '一整天', '兩天以上']
const interests = ['歷史故事', '拍照打卡', '美食慢遊', '親子輕鬆', '海邊散步']

export default function IndexPage() {
  const [stories, setStories] = useState(() => getStorylines())
  const [rewards, setRewards] = useState(() => getRewards())
  const [state, setState] = useState(() => loadGameState())
  const [showAssessment, setShowAssessment] = useState(() => !loadGameState().travelAssessment)
  const [assessment, setAssessment] = useState({
    ageGroup: '18-30歲',
    playDuration: '半天慢遊',
    interests: ['歷史故事'],
    allowLocation: false,
  })

  useEffect(() => {
    setStories(getStorylines())
    setRewards(getRewards())
    setState(loadGameState())
  }, [])

  const recommendation = useMemo(() => getTravelRecommendation(state.travelAssessment), [state])
  const featuredReward = rewards[0]
  const selectedInterestCount = assessment.interests.length

  const assessmentSteps = [
    { id: 'age', label: '年齡', value: assessment.ageGroup },
    { id: 'duration', label: '時長', value: assessment.playDuration },
    { id: 'interest', label: '偏好', value: `${selectedInterestCount}/3` },
    { id: 'location', label: '位置', value: assessment.allowLocation ? '已開啟' : '未開啟' },
  ]



  const toggleInterest = (value) => {
    const exists = assessment.interests.includes(value)
    const next = exists ? assessment.interests.filter((item) => item !== value) : [...assessment.interests, value]
    setAssessment({ ...assessment, interests: next.slice(0, 3) })
  }

  const handleAssessment = async () => {
    let userLocation = null
    if (assessment.allowLocation) {
      try {
        const res = await Taro.getLocation({ type: 'gcj02' })
        userLocation = { latitude: res.latitude, longitude: res.longitude }
      } catch (error) {
        Taro.showToast({ title: '先按你目前的偏好為你安排', icon: 'none' })
      }
    }
    const result = saveTravelAssessment(assessment, userLocation || undefined)
    setState(loadGameState())
    setStories(getStorylines())
    setShowAssessment(false)
    Taro.showModal({
      title: '專屬旅程已準備好',
      content: `推薦主線：${result.storyName}\n今日亮點：${result.activityTitle}\n推薦景點：${result.poiName}`,
      showCancel: false,
    })
  }

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

      {showAssessment ? (
        <View className='assessment-card'>
          <View className='assessment-card__header'>
            <View>
              <Text className='assessment-card__title'>開始前，先做個小小旅程評估</Text>
              <Text className='assessment-card__desc'>回答幾個簡單問題，我會幫你挑最適合今天節奏的主線、景點和秘籍內容。</Text>
            </View>
            <Button className='assessment-card__skip' onClick={() => setShowAssessment(false)}>先看看再說</Button>
          </View>

          <View className='assessment-card__steps'>
            {assessmentSteps.map((step) => (
              <View key={step.id} className='assessment-card__step'>
                <Text className='assessment-card__stepLabel'>{step.label}</Text>
                <Text className='assessment-card__stepValue'>{step.value}</Text>
              </View>
            ))}
          </View>

          <View className='assessment-card__section'>
            <Text className='assessment-card__label'>年齡層</Text>
            <View className='assessment-card__chips'>
              {ageGroups.map((item) => (
                <View key={item} className={`assessment-card__chip ${assessment.ageGroup === item ? 'active' : ''}`} onClick={() => setAssessment({ ...assessment, ageGroup: item })}>
                  <Text className='assessment-card__chipText'>{item}</Text>
                </View>
              ))}
            </View>
          </View>

          <View className='assessment-card__section'>
            <Text className='assessment-card__label'>今天預計玩多久</Text>
            <View className='assessment-card__chips'>
              {durations.map((item) => (
                <View key={item} className={`assessment-card__chip ${assessment.playDuration === item ? 'active' : ''}`} onClick={() => setAssessment({ ...assessment, playDuration: item })}>
                  <Text className='assessment-card__chipText'>{item}</Text>
                </View>
              ))}
            </View>
          </View>

          <View className='assessment-card__section'>
            <View className='assessment-card__labelRow'>
              <Text className='assessment-card__label'>最感興趣的內容</Text>
              <Text className='assessment-card__helper'>可選 3 個</Text>
            </View>
            <View className='assessment-card__chips'>
              {interests.map((item) => (
                <View key={item} className={`assessment-card__chip ${assessment.interests.includes(item) ? 'active' : ''}`} onClick={() => toggleInterest(item)}>
                  <Text className='assessment-card__chipText'>{item}</Text>
                </View>
              ))}
            </View>
          </View>

          <View className='assessment-card__location'>
            <View>
              <Text className='assessment-card__locationTitle'>位置加成</Text>
              <Text className='assessment-card__locationText'>如果願意分享位置，我也會把離你最近、最合適的城市旅程一起準備好。</Text>
            </View>
            <Button className={`assessment-card__locationBtn ${assessment.allowLocation ? 'active' : ''}`} onClick={() => setAssessment({ ...assessment, allowLocation: !assessment.allowLocation })}>{assessment.allowLocation ? '已開啟位置授權' : '同意取得位置'}</Button>
          </View>

          <View className='assessment-card__actions'>
            <Button className='assessment-card__secondary' onClick={() => setAssessment({ ...assessment, interests: ['歷史故事'] })}>回到基礎偏好</Button>
            <Button className='assessment-card__submit' onClick={handleAssessment}>生成我的今日推薦</Button>
          </View>
        </View>
      ) : (

        <View className='main-actions'>
          <View className='action-card primary' onClick={() => Taro.switchTab({ url: '/pages/map/index' })}>
            <View className='action-icon'>🗺️</View>
            <View className='action-text'>
              <Text className='action-title'>開始探索</Text>
              <Text className='action-desc'>看看附近有哪些地標和彩蛋，朝下一枚足跡章出發。</Text>
            </View>
          </View>
        </View>
      )}

      <View className='recommend-panel'>
        <Text className='section-title'>為你準備的今日路線</Text>
        <View className='recommend-card'>
          <Text className='recommend-card__title'>{recommendation.storyName}</Text>
          <Text className='recommend-card__reason'>{recommendation.reason}</Text>
          <Text className='recommend-card__meta'>推薦活動：{recommendation.activityTitle}</Text>
          <Text className='recommend-card__meta'>先去這裡：{recommendation.poiName}</Text>
          <Text className='recommend-card__meta'>搭配秘籍：{recommendation.ugcTitle}</Text>
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
          <View className='quick-item' onClick={() => Taro.navigateTo({ url: '/pages/tips/index' })}>
            <View className='quick-icon'>📝</View>
            <Text className='quick-text'>秘籍靈感</Text>
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
          {stories.slice(0, 4).map((story) => (
            <View key={story.id} className='story-card' onClick={() => Taro.navigateTo({ url: '/pages/story/index' })}>
              <View className='story-cover' style={{ background: story.coverColor }}>
                <Text className='story-emoji'>{story.icon}</Text>
              </View>
              <View className='story-info'>
                <Text className='story-title'>{story.name}</Text>
                <Text className='story-desc'>{story.description}</Text>
                <Text className='story-progress'>{story.locked ? '待解鎖' : `進度 ${story.progress}% · ${story.estimatedTime}`}</Text>
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
