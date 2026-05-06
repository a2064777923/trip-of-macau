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
import type { CityProgressItem } from '../../types/game'
import './index.scss'


const ageGroups = ['18歲以下', '18-30歲', '31-55歲', '55歲以上']
const durations = ['1小時內', '半天慢遊', '一整天', '兩天以上']
const interests = ['歷史故事', '拍照打卡', '美食慢遊', '親子輕鬆', '海邊散步']
const FLAGSHIP_STORY_CODE = 'east_west_war_and_coexistence'
const FLAGSHIP_STORY_NAME = '東西方文明的戰火與共生'
const LEGACY_DUPLICATE_STORY_CODE = 'macau_fire_route'
const isFlagshipStory = (story: ReturnType<typeof getStorylines>[number]) => (
  story.code === FLAGSHIP_STORY_CODE
  || (story.code !== LEGACY_DUPLICATE_STORY_CODE && story.name === FLAGSHIP_STORY_NAME)
)

export default function IndexPage() {
  const [stories, setStories] = useState(() => getStorylines())
  const [rewards, setRewards] = useState(() => getRewards())
  const [cities, setCities] = useState(() => getCities())
  const [state, setState] = useState(() => loadGameState())
  const [showAssessment, setShowAssessment] = useState(() => !loadGameState().travelAssessment)
  const [assessmentResult, setAssessmentResult] = useState<ReturnType<typeof getTravelRecommendation> | null>(null)
  const [loadingAssessment, setLoadingAssessment] = useState(false)
  const [showMapSelector, setShowMapSelector] = useState(false)
  const [currentMap, setCurrentMap] = useState<CityProgressItem | null>(() => getCities()[0] || null)
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
      setCurrentMap(nextCities.find((item) => item.id === nextState.user.currentCityId) || nextCities[0] || null)
    }

    void hydrateHomePage()

    return () => {
      cancelled = true
    }
  }, [])

  const hasPublicCatalog = cities.length > 0 && stories.length > 0
  const featuredReward = rewards[0]
  const flagshipStory = stories.find((story) => story.code === FLAGSHIP_STORY_CODE)
    || stories.find((story) => story.code !== LEGACY_DUPLICATE_STORY_CODE && story.name === FLAGSHIP_STORY_NAME)
    || stories[0]
  const visibleStoryCount = stories.filter((story) => !story.locked).length || stories.length
  const flagshipFirstChapter = flagshipStory?.chapters?.[0]
  const flagshipStartName = flagshipFirstChapter?.locationName || '媽閣廟'
  const recommendation = useMemo(() => {
    const base = getTravelRecommendation(state.travelAssessment)
    if (flagshipStory && isFlagshipStory(flagshipStory)) {
      return {
        ...base,
        storyId: flagshipStory.id,
        storyName: FLAGSHIP_STORY_NAME,
        activityTitle: '五章主線旅程',
        poiName: flagshipStartName,
        ugcTitle: '跟隨海防銅鏡碎片前進',
        reason: state.travelAssessment
          ? '根據你的偏好，這條主線會以歷史劇情、現場打卡和分章任務串起今天的路線。'
          : '先從媽閣廟出發，沿亞婆井前地、崗頂前地、大炮台與議事亭前地逐步走入故事。',
      }
    }
    return base
  }, [flagshipFirstChapter?.locationName, flagshipStory?.id, state.travelAssessment, stories.length])
  const selectedInterestCount = assessment.interests.length

  const openStoryPage = (storyId?: number) => {
    const query = storyId ? `?storyId=${encodeURIComponent(String(storyId))}` : ''
    Taro.navigateTo({ url: `/pages/story/index${query}` })
  }

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
        const res = await Promise.race([
          Taro.getLocation({ type: 'gcj02' }),
          new Promise<never>((_, reject) => setTimeout(() => reject(new Error('location timeout')), 2500)),
        ])
        userLocation = { latitude: res.latitude, longitude: res.longitude }
      } catch (error) {
        Taro.showToast({ title: '先按你目前的偏好為你安排', icon: 'none' })
      }
    }
    
    // Simulate AI loading
    setTimeout(async () => {
      try {
        const isAnonymousPreview = loadGameState().user.authStatus === 'anonymous'
        const result = isAnonymousPreview
          ? getTravelRecommendation(assessment, userLocation || undefined)
          : await saveTravelAssessment(assessment, userLocation || undefined)
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

            <Text className='map-name'>{currentMap?.name || '載入地圖'}</Text>
          </View>
        </View>
        <View className='hero-content'>
          <Text className='app-subtitle'>跟著故事去散步，把沿途的風景、印章和驚喜都收進行囊裡。</Text>

          <View className='user-level-card'>
            <View className='level-badge'>
              <Text className='level-text'>Lv.{state.user.level}</Text>
            </View>
            <View className='level-info'>
              <Text className='level-title'>{state.user.title}</Text>
              <Text className='stamp-count'>已收集 {state.user.totalStamps} 枚印章 · 可探索 {visibleStoryCount} 條主線</Text>
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
                        <Text className='assessment-result__image-text'>先看看今日亮點，圖文會跟著旅程展開。</Text>
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
            <Text className='map-selector-modal__title'>選擇大地圖</Text>
            <ScrollView scrollX className='map-selector-scroll'>
              <View className='map-list'>
                {cities.map((map) => (
                  <View 
                    key={map.id} 
                    className={`map-item ${!map.unlocked ? 'locked' : ''} ${currentMap?.id === map.id ? 'active' : ''}`}
                    onClick={() => void handleMapSelection(map)}
                  >
                    <View
                      className='map-item__image-placeholder'
                      style={{ background: map.coverColor || 'linear-gradient(135deg, #2d405f 0%, #d58f57 100%)' }}
                    >
                      {!map.unlocked && <Text className='map-item__lock'>🔒</Text>}
                      {map.unlocked ? <Text className='map-item__mark'>{map.name.slice(0, 2)}</Text> : null}
                    </View>
                    <Text className='map-item__name'>{map.name}</Text>
                    <Text className='map-item__status'>{map.unlocked ? `${map.explorationProgress || 0}% 已探索` : '未解鎖'}</Text>
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

      {hasPublicCatalog ? (
      <View className='recommend-panel'>
        <Text className='section-title'>為你準備的今日路線</Text>
        <View className='recommend-card' onClick={() => openStoryPage(recommendation.storyId || flagshipStory?.id)}>
          <Text className='recommend-card__title'>{isFlagshipStory(flagshipStory) ? FLAGSHIP_STORY_NAME : recommendation.storyName}</Text>
          <Text className='recommend-card__reason'>{recommendation.reason}</Text>
          <Text className='recommend-card__meta'>旅程結構：{recommendation.activityTitle}</Text>
          <Text className='recommend-card__meta'>第一站：{recommendation.poiName}</Text>
          <Text className='recommend-card__meta'>故事線索：{recommendation.ugcTitle}</Text>
        </View>
      </View>
      ) : (
        <View className='recommend-panel'>
          <Text className='section-title'>正在整理今日旅程</Text>
          <View className='recommend-card'>
            <Text className='recommend-card__title'>載入真實故事與地圖</Text>
            <Text className='recommend-card__reason'>旅程內容正在同步，稍後會顯示可開始的故事線與地圖。</Text>
          </View>
        </View>
      )}

      <View className='quick-access'>
        <Text className='section-title'>今天想怎麼玩</Text>
        <View className='quick-grid'>
          <View className='quick-item' onClick={() => openStoryPage(flagshipStory?.id)}>
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

      {hasPublicCatalog && (
      <View className='featured-stories'>
        <Text className='section-title'>推薦探索路線</Text>
        <ScrollView className='story-scroll' scrollX>
          {stories.slice(0, 4).map((story) => (
            <View key={story.id} className='story-card' onClick={() => openStoryPage(story.id)}>
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
      )}

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
