import { useEffect, useMemo, useState } from 'react'
import { Button, ScrollView, Text, View, Image } from '@tarojs/components'
import Taro from '@tarojs/taro'
import PageShell from '../../components/PageShell'
import { cosAssetManifest } from '../../constants/assetUrls'
import {
  getCities,
  getRewards,
  getStorylines,
  getTravelRecommendation,
  isAuthRequiredError,
  loadGameState,
  refreshPublicContent,
  saveTravelAssessment,
  switchCurrentCity,
} from '../../services/gameService'
import './index.scss'


const ageGroups = ['18歲以下', '18-30歲', '31-55歲', '55歲以上']
const durations = ['1小時內', '半天慢遊', '一整天', '兩天以上']
const interests = ['歷史故事', '拍照打卡', '美食慢遊', '親子輕鬆', '海邊散步']

export default function IndexPage() {
  const [stories, setStories] = useState(() => getStorylines())
  const [rewards, setRewards] = useState(() => getRewards())
  const [cities, setCities] = useState(() => getCities())
  const [state, setState] = useState(() => loadGameState())
  const [showAssessment, setShowAssessment] = useState(() => !loadGameState().travelAssessment)
  const [assessmentResult, setAssessmentResult] = useState<ReturnType<typeof getTravelRecommendation> | null>(null)
  const [loadingAssessment, setLoadingAssessment] = useState(false)
  const [showMapSelector, setShowMapSelector] = useState(false)
  const [currentMap, setCurrentMap] = useState(() => getCities()[0] || {
    id: 'macau',
    name: '澳門',
    subtitle: '',
    coverColor: '#ffd9e5',
    unlocked: true,
    explorationProgress: 0,
    titleReward: 'Macau Explorer',
    landmarkCount: 0,
  })
  const [assessment, setAssessment] = useState({
    ageGroup: '18-30歲',
    playDuration: '半天慢遊',
    interests: ['歷史故事'],
    allowLocation: false,
  })

  useEffect(() => {
    let cancelled = false

    const hydrateHomePage = async () => {
      try {
        await refreshPublicContent()
      } catch (error) {
        console.warn('Failed to refresh home content.', error)
      }

      if (cancelled) {
        return
      }

      const nextState = loadGameState()
      const nextCities = getCities()
      setStories(getStorylines())
      setRewards(getRewards())
      setCities(nextCities)
      setState(nextState)
      setCurrentMap(nextCities.find((item) => item.id === nextState.user.currentCityId) || nextCities[0])
    }

    void hydrateHomePage()

    return () => {
      cancelled = true
    }
  }, [])

  const recommendation = useMemo(() => getTravelRecommendation(state.travelAssessment), [state])
  const featuredReward = rewards[0]
  const selectedInterestCount = assessment.interests.length

  const assessmentSteps = [
    { id: 'age', label: '年齡', value: assessment.ageGroup },
    { id: 'duration', label: '時長', value: assessment.playDuration },
    { id: 'interest', label: '偏好', value: `${selectedInterestCount}/3` },
  ]



  const toggleInterest = (value: string) => {
    const exists = assessment.interests.includes(value)
    const next = exists ? assessment.interests.filter((item) => item !== value) : [...assessment.interests, value]
    setAssessment({ ...assessment, interests: next.slice(0, 3) })
  }

  const handleAssessment = async () => {
    setLoadingAssessment(true)
    let userLocation: { latitude: number; longitude: number } | null = null
    if (assessment.allowLocation) {
      try {
        const res = await Taro.getLocation({ type: 'gcj02' })
        userLocation = { latitude: res.latitude, longitude: res.longitude }
      } catch (error) {
        Taro.showToast({ title: '先按你目前的偏好為你安排', icon: 'none' })
      }
    }
    
    // Simulate AI loading
    setTimeout(async () => {
      try {
        const result = await saveTravelAssessment(assessment, userLocation || undefined)
        setState(loadGameState())
        setStories(getStorylines())
        setAssessmentResult(result)
      } catch (error) {
        if (!isAuthRequiredError(error)) {
          Taro.showToast({ title: error instanceof Error ? error.message : '生成推薦失敗', icon: 'none' })
        }
      } finally {
        setLoadingAssessment(false)
      }
    }, 1500)
  }

  const closeAssessment = () => {
    setShowAssessment(false)
    setAssessmentResult(null)
  }

  const handleMapSelection = async (map: ReturnType<typeof getCities>[number]) => {
    if (!map.unlocked) {
      Taro.showToast({ title: '該城市尚未解鎖', icon: 'none' })
      return
    }

    try {
      await switchCurrentCity(map.id)
      
      // 重新加载所有相关数据
      const nextState = loadGameState()
      const nextCities = getCities()
      const nextStories = getStorylines()
      const nextRewards = getRewards()
      
      // 更新所有状态
      setCurrentMap(nextCities.find((item) => item.id === map.id) || map)
      setCities(nextCities)
      setState(nextState)
      setStories(nextStories)
      setRewards(nextRewards)
      setShowMapSelector(false)
      
      Taro.showToast({ title: `已切換至${map.name}`, icon: 'success' })
    } catch (error) {
      Taro.showToast({ title: error instanceof Error ? error.message : '切換城市失敗', icon: 'none' })
    }
  }

  return (
    <PageShell className='index-page'>
      <View className='hero-section'>
        <View className='hero-header'>
          <View className='title-wrapper'>
            <Image className='app-logo' src={cosAssetManifest.brand.appLogoMain} mode='aspectFit' />

            <Text className='app-title'>澳小遊</Text>
          </View>
          <View className='map-selector-btn' onClick={() => setShowMapSelector(true)}>
            <Image className='map-icon' src={cosAssetManifest.tabbar.iconMapActiveSvg} mode='aspectFit' />

            <Text className='map-name'>{currentMap.name}</Text>
          </View>
        </View>
        <View className='hero-content'>
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

      {showAssessment && (
        <View className={`assessment-modal ${loadingAssessment ? 'loading' : ''}`}>
          <View className='assessment-modal__mask' onClick={() => !loadingAssessment && setShowAssessment(false)} />
          <View className='assessment-modal__content'>
            {loadingAssessment ? (
              <View className='assessment-modal__loader'>
                <View className='loader-spinner'></View>
                <Text className='loader-text'>AI 正在為您規劃專屬旅程...</Text>
              </View>
            ) : (
              <ScrollView scrollY className='assessment-modal__scroll'>
                {assessmentResult ? (
                  <View className='assessment-result'>
                    <Text className='assessment-result__header'>專屬旅程已為您準備好</Text>
                    
                    <View className='assessment-result__card'>
                      <Text className='assessment-result__label'>推薦主線</Text>
                      <Text className='assessment-result__value'>{assessmentResult.storyName}</Text>
                      <Text className='assessment-result__reason'>基於您對{assessment.interests.join('、')}的偏好</Text>
                    </View>
                    
                    <View className='assessment-result__card'>
                      <Text className='assessment-result__label'>今日亮點活動</Text>
                      <Text className='assessment-result__value'>{assessmentResult.activityTitle}</Text>
                      <View className='assessment-result__image-placeholder'>
                        <Text className='assessment-result__image-text'>推薦圖文內容載入中...</Text>
                      </View>
                      <Text className='assessment-result__desc'>點擊查看達人分享的詳細圖文筆記，為您的旅程增添靈感。</Text>
                    </View>
                    
                    <View className='assessment-result__card'>
                      <Text className='assessment-result__label'>首站推薦</Text>
                      <Text className='assessment-result__value'>{assessmentResult.poiName}</Text>
                    </View>

                    <Button className='assessment-result__close' onClick={closeAssessment}>太棒了，開始探索！</Button>
                  </View>
                ) : (
                  <View>
                    <View className='assessment-modal__header'>
                      <Text className='assessment-modal__title'>旅程評估</Text>
                      <Text className='assessment-modal__desc'>回答幾個簡單問題，我會幫你挑最適合今天節奏的主線、景點和秘籍內容。</Text>
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

                    <View className='assessment-modal__actions'>
                      <Button className='assessment-card__skip' onClick={closeAssessment}>先看看再說</Button>
                      <Button className='assessment-card__submit' onClick={handleAssessment}>生成推薦</Button>
                    </View>
                  </View>
                )}
              </ScrollView>
            )}
          </View>
        </View>
      )}

      {showMapSelector && (
        <View className='map-selector-modal'>
          <View className='map-selector-modal__mask' onClick={() => setShowMapSelector(false)} />
          <View className='map-selector-modal__content'>
            <Text className='map-selector-modal__title'>選擇探索城市</Text>
            <ScrollView scrollX className='map-selector-scroll'>
              <View className='map-list'>
                {cities.map((map) => (
                  <View 
                    key={map.id} 
                    className={`map-item ${!map.unlocked ? 'locked' : ''} ${currentMap.id === map.id ? 'active' : ''}`}
                    onClick={() => void handleMapSelection(map)}
                  >
                    <View className='map-item__image-placeholder'>
                      {!map.unlocked && <Text className='map-item__lock'>🔒</Text>}
                    </View>
                    <Text className='map-item__name'>{map.name}</Text>
                    <Text className='map-item__status'>{map.unlocked ? '已解鎖' : '未解鎖'}</Text>
                  </View>
                ))}
              </View>
            </ScrollView>
            <Button className='map-selector-modal__close' onClick={() => setShowMapSelector(false)}>關閉</Button>
          </View>
        </View>
      )}

      {!showAssessment && (

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
          <View className='quick-item' onClick={() => Taro.switchTab({ url: '/pages/tips/index' })}>
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
