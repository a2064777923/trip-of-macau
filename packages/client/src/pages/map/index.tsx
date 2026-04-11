import { useEffect, useMemo, useRef, useState } from 'react'
import { Button, Input, Map, ScrollView, Text, View } from '@tarojs/components'
import Taro from '@tarojs/taro'
import PageShell from '../../components/PageShell'
import {
  getArrivalExperience,
  getCities,
  getEmergencyContact,
  getMapBootstrapConfig,
  getNearbyPois,
  getPoiSearchTips,
  getStoryById,
  getWalkingRouteSummary,
  loadGameState,
  performMockCheckin,
  registerCityVisitByLocation,
  switchCurrentCity,
} from '../../services/gameService'
import './index.scss'

const fallbackLocation = {
  latitude: 22.1987,
  longitude: 113.5439,
  accuracy: 35,
}

export default function MapPage() {
  const [state, setState] = useState(() => loadGameState())
  const [location, setLocation] = useState(fallbackLocation)
  const [cities, setCities] = useState(() => getCities())
  const [currentCityId, setCurrentCityId] = useState(() => loadGameState().user.currentCityId || 'macau')
  const bootstrap = useMemo(() => getMapBootstrapConfig(), [currentCityId, state])
  const emergencyContact = useMemo(() => getEmergencyContact(), [])
  const [pois, setPois] = useState([])
  const [selectedPoiId, setSelectedPoiId] = useState(null)
  const [tips, setTips] = useState([])
  const [keyword, setKeyword] = useState('')
  const [routeSummary, setRouteSummary] = useState(null)
  const [checkinResult, setCheckinResult] = useState(null)
  const [loading, setLoading] = useState(true)
  const [searching, setSearching] = useState(false)
  const [arrivalVisible, setArrivalVisible] = useState(false)
  const [voicePlaying, setVoicePlaying] = useState(false)
  const [dwellSeconds, setDwellSeconds] = useState(0)
  const audioRef = useRef(null)

  const selectedPoi = useMemo(() => {
    return selectedPoiId ? pois.find((poi) => poi.id === selectedPoiId) || null : pois[0] || null
  }, [pois, selectedPoiId])

  const relatedStory = useMemo(() => {
    return selectedPoi?.storyLineId ? getStoryById(selectedPoi.storyLineId) : null
  }, [selectedPoi?.storyLineId, state.completedStoryIds, state.completedChapterIds])

  const activeStoryChapter = useMemo(() => {
    if (!relatedStory?.chapters?.length) return null
    return relatedStory.chapters.find((chapter) => !chapter.locked) || relatedStory.chapters[0]
  }, [relatedStory])

  const arrivalExperience = useMemo(() => {
    return selectedPoi ? getArrivalExperience(selectedPoi.id) : null
  }, [selectedPoi?.id])

  const refreshByCity = (lat, lng, accuracy, cityId = currentCityId) => {

    const nearby = getNearbyPois(lat, lng, accuracy, cityId)
    setPois(nearby)
    setSelectedPoiId(nearby[0] ? nearby[0].id : null)
    if (nearby[0]) {
      setRouteSummary(getWalkingRouteSummary(nearby[0], { latitude: lat, longitude: lng }))
    }
  }

  useEffect(() => {
    bootstrapPage()
    if (wx.createInnerAudioContext) {
      audioRef.current = wx.createInnerAudioContext()
      audioRef.current.obeyMuteSwitch = false
      audioRef.current.onEnded(() => setVoicePlaying(false))
      audioRef.current.onStop(() => setVoicePlaying(false))
      audioRef.current.onError(() => setVoicePlaying(false))
    }
    return () => {
      if (audioRef.current) {
        audioRef.current.destroy()
      }
    }
  }, [])

  useEffect(() => {
    if (!selectedPoi) {
      setArrivalVisible(false)
      setDwellSeconds(0)
      return
    }
    setSelectedPoiId(selectedPoi.id)
    setRouteSummary(getWalkingRouteSummary(selectedPoi, location))
    setDwellSeconds(selectedPoi.inRange ? selectedPoi.staySeconds : 0)
    if (selectedPoi.inRange) {
      setArrivalVisible(true)
    }
  }, [selectedPoi && selectedPoi.id, selectedPoi && selectedPoi.inRange, location.latitude, location.longitude])

  useEffect(() => {
    const latestState = loadGameState()
    if (arrivalVisible && arrivalExperience && latestState.user.interfaceMode === 'elderly' && latestState.user.voiceGuideEnabled) {
      playArrivalAudio()
    }
  }, [arrivalVisible, arrivalExperience && arrivalExperience.poiId])

  const bootstrapPage = async () => {
    setLoading(true)
    try {
      const res = await Taro.getLocation({ type: 'gcj02' })
      const nextLocation = {
        latitude: res.latitude,
        longitude: res.longitude,
        accuracy: res.accuracy || 30,
      }
      const unlockedCityId = registerCityVisitByLocation(nextLocation.latitude, nextLocation.longitude)
      setLocation(nextLocation)
      setCurrentCityId(unlockedCityId)
      setState(loadGameState())
      setCities(getCities())
      refreshByCity(nextLocation.latitude, nextLocation.longitude, nextLocation.accuracy, unlockedCityId)
    } catch (error) {
      refreshByCity(fallbackLocation.latitude, fallbackLocation.longitude, fallbackLocation.accuracy, currentCityId)
      Taro.showToast({ title: '先为你打開當前城市的熱門地標', icon: 'none' })
    } finally {
      setLoading(false)
    }
  }

  const handleSearchInput = (event) => {
    const value = event.detail.value || ''
    setKeyword(value)
    if (!value.trim()) {
      setTips([])
      return
    }
    setSearching(true)
    const result = getPoiSearchTips(value, currentCityId)
    setTips(result.map((item) => ({ id: item.id, name: item.name, address: item.address })))
    setSearching(false)
  }

  const handleSelectPoi = (poiId) => {
    setSelectedPoiId(poiId)
    const poi = pois.find((item) => item.id === poiId)
    if (poi) {
      setKeyword(poi.name)
      setTips([])
      setRouteSummary(getWalkingRouteSummary(poi, location))
    }
  }

  const handleQuickLocate = async () => {
    await bootstrapPage()
  }

  const handleSwitchCity = (cityId) => {
    try {
      const city = switchCurrentCity(cityId)
      setCurrentCityId(cityId)
      setState(loadGameState())
      setCities(getCities())
      refreshByCity(location.latitude, location.longitude, location.accuracy, cityId)
      Taro.showToast({ title: `已切換到 ${city.name}`, icon: 'none' })
    } catch (error) {
      Taro.showToast({ title: error.message || '先走近該城市再試試', icon: 'none' })
    }
  }

  const handleOpenStoryChapter = () => {
    if (!relatedStory) {
      Taro.navigateTo({ url: '/pages/story/index' })
      return
    }
    const chapterId = activeStoryChapter?.id
    const query = chapterId ? `?storyId=${relatedStory.id}&chapterId=${chapterId}` : `?storyId=${relatedStory.id}`
    Taro.navigateTo({ url: `/pages/story/index${query}` })
  }





  const playArrivalAudio = () => {
    if (!arrivalExperience) return
    setVoicePlaying(true)
    if (audioRef.current) {
      audioRef.current.stop()
    }
    Taro.showToast({ title: `${arrivalExperience.audioTitle} 已開始播報`, icon: 'none' })
  }


  const stopArrivalAudio = () => {
    if (audioRef.current) {
      audioRef.current.stop()
    }
    setVoicePlaying(false)
  }


  const completeCheckin = (mode) => {
    if (!selectedPoi) return
    const result = performMockCheckin(selectedPoi.id, mode)
    setCheckinResult(result)
    setArrivalVisible(false)
    setState(loadGameState())
    setCities(getCities())
    Taro.showToast({ title: `已獲得 ${result.stampName}`, icon: 'success' })
    refreshByCity(location.latitude, location.longitude, location.accuracy, loadGameState().user.currentCityId || currentCityId)
  }

  const handleManualCheckin = () => {
    if (!arrivalExperience || !arrivalExperience.canManualCheckin) {
      Taro.showToast({ title: '此地點暫不支持手動補簽', icon: 'none' })
      return
    }

    Taro.showModal({
      title: '手動補簽確認',
      content: '若訊號不穩，可使用附近補簽方式完成這段旅程。是否繼續？',
      success: (res) => {
        if (res.confirm) {
          completeCheckin('manual')
        }
      },
    })
  }

  return (
    <PageShell className='map-page'>
      <View className='map-hero'>
        <View className='map-hero__content'>
          <View className='map-hero__main'>
            <View className='map-hero__text'>
              <Text className='map-hero__eyebrow'>城市探索進行中</Text>
              <Text className='map-hero__title'>澳門故事地圖</Text>
              <Text className='map-hero__subtitle'>沿着地標一路前行，聽見城市回聲，解鎖故事與旅途驚喜。</Text>
            </View>
            <View className='map-hero__stats'>
              <View className='map-hero__progressCard'>
                <Text className='map-hero__progressLabel'>探索進度</Text>
                <Text className='map-hero__progressValue'>{cities.find((city) => city.id === currentCityId)?.explorationProgress || 0}%</Text>
              </View>
              <View className='map-hero__titleBadge'>
                <Text className='map-hero__titleBadgeText'>{cities.find((city) => city.id === currentCityId)?.titleReward || state.user.title}</Text>
              </View>
            </View>
          </View>
          <View className='map-hero__storyCard' onClick={handleOpenStoryChapter}>

            <Text className='map-hero__storyLabel'>對應故事章節</Text>
            <Text className='map-hero__storyTitle'>{activeStoryChapter?.title || relatedStory?.name || '城市漫遊篇章'}</Text>
            <Text className='map-hero__storyDesc'>{activeStoryChapter?.summary || '點開後可直接查看這段地標對應的故事詳情。'}</Text>
            <Text className='map-hero__storyAction'>查看章節詳情</Text>
          </View>
        </View>
      </View>



      <View className='map-shell'>
        <ScrollView className='city-switcher' scrollX>
          {cities.map((city) => (
            <View key={city.id} className={`city-switcher__item ${currentCityId === city.id ? 'active' : ''} ${city.unlocked ? '' : 'locked'}`} onClick={() => handleSwitchCity(city.id)}>
              <Text className='city-switcher__name'>{city.name}</Text>
            </View>
          ))}
        </ScrollView>


        <View className='map-card'>
          <View className='map-toolbar'>
            <View className='map-search'>
              <Input className='map-search__input' value={keyword} placeholder='搜尋附近地標或故事關鍵字' onInput={handleSearchInput} />
              <Button className='map-search__button' size='mini' onClick={handleQuickLocate}>刷新位置</Button>
            </View>
            <Text className='map-toolbar__meta'>{bootstrap.city} · {searching ? '正在替你尋找靈感…' : '旅程已準備好'}</Text>
          </View>

          {!!tips.length && (
            <View className='tips-panel'>
              {tips.map((tip) => (
                <View key={tip.id} className='tips-panel__item' onClick={() => handleSelectPoi(Number(tip.id))}>
                  <Text className='tips-panel__name'>{tip.name}</Text>
                  <Text className='tips-panel__addr'>{tip.address}</Text>
                </View>
              ))}
            </View>
          )}

          <Map
            className='map-canvas'
            longitude={selectedPoi ? selectedPoi.gcj02Longitude : location.longitude}
            latitude={selectedPoi ? selectedPoi.gcj02Latitude : location.latitude}
            scale={15}
            showLocation
            enableRotate={false}
            enableOverlooking={false}
            circles={selectedPoi ? [{
              latitude: selectedPoi.gcj02Latitude,
              longitude: selectedPoi.gcj02Longitude,
              radius: selectedPoi.dynamicRadius,
              strokeColor: '#ff8ba7',
              fillColor: 'rgba(255,139,167,0.14)',
              strokeWidth: 2,
            }] : []}
          />



        </View>

        <View className='explore-grid'>
          <View className='explore-panel nearby-panel'>
            <View className='panel-header'>
              <Text className='panel-title'>附近任務點</Text>
              <Text className='panel-subtitle'>{loading ? '正在整理附近地標…' : `${pois.length} 個值得駐足的地點`}</Text>
            </View>
            <ScrollView scrollY className='poi-scroll'>
              {pois.map((poi) => (
                <View key={poi.id} className={`poi-card ${selectedPoiId === poi.id ? 'poi-card--active' : ''}`} onClick={() => handleSelectPoi(poi.id)}>
                  <View className='poi-card__cover' style={{ background: poi.coverColor }}>
                    <Text className='poi-card__emoji'>{poi.icon}</Text>
                  </View>
                  <View className='poi-card__body'>
                    <View className='poi-card__top'>
                      <Text className='poi-card__name'>{poi.name}</Text>
                      <Text className={`poi-card__badge ${poi.inRange ? 'ready' : ''}`}>{poi.inRange ? '可解鎖' : poi.distanceText}</Text>
                    </View>
                    <Text className='poi-card__subtitle'>{poi.subtitle}</Text>
                    <Text className='poi-card__meta'>{poi.category} · {poi.storyName || '城市漫遊篇章'} · 建議停留 {poi.staySeconds}s</Text>
                  </View>
                </View>
              ))}
            </ScrollView>
          </View>

          <View className='explore-panel detail-panel'>
            <View className='panel-header'>
              <Text className='panel-title'>玩法详情</Text>
              <Text className='panel-subtitle'>附近有哪些值得驻足的地点、故事与惊喜，一眼就能看见</Text>
            </View>

            {selectedPoi ? (
              <>
                <View className='selected-poi'>
                  <View className='selected-poi__hero' style={{ background: selectedPoi.coverColor }}>
                    <Text className='selected-poi__icon'>{selectedPoi.icon}</Text>
                    <View>
                      <Text className='selected-poi__name'>{selectedPoi.name}</Text>
                      <Text className='selected-poi__subtitle'>{selectedPoi.subtitle}</Text>
                    </View>
                  </View>
                  <Text className='selected-poi__desc'>{selectedPoi.description}</Text>
                  <View className='selected-poi__chips'>
                    {selectedPoi.tags.map((tag) => (
                      <Text key={tag} className='selected-poi__chip'>{tag}</Text>
                    ))}
                  </View>

                  <View className='selected-poi__stats'>
                    <View className='selected-poi__stat'>
                      <Text className='selected-poi__statLabel'>目前距離</Text>
                      <Text className='selected-poi__statValue'>{selectedPoi.inRange ? '已到達範圍內' : selectedPoi.distanceText}</Text>
                    </View>
                    <View className='selected-poi__stat'>
                      <Text className='selected-poi__statLabel'>停留建議</Text>
                      <Text className='selected-poi__statValue'>{selectedPoi.staySeconds} 秒</Text>
                    </View>
                    <View className='selected-poi__stat selected-poi__stat--full'>
                      <Text className='selected-poi__statLabel'>故事關聯</Text>
                      <Text className='selected-poi__statValue'>{selectedPoi.storyName || '城市漫遊篇章'}</Text>
                    </View>
                  </View>
                </View>

                <View className='route-card'>
                  <Text className='route-card__title'>前往路線</Text>
                  {routeSummary ? (
                    <>
                      <Text className='route-card__summary'>約 {Math.round(Number(routeSummary.distance) / 100) / 10} km · {Math.ceil(Number(routeSummary.duration) / 60)} 分鐘</Text>
                      {routeSummary.steps.slice(0, 3).map((step, index) => (
                        <Text key={`${step}-${index}`} className='route-card__step'>{index + 1}. {step}</Text>
                      ))}
                    </>
                  ) : (
                    <Text className='route-card__summary'>先跟著附近地標慢慢走，我會陪你把路線整理得更清楚。</Text>
                  )}
                </View>

                <View className='story-card-lite'>
                  <Text className='story-card-lite__title'>這段旅程會帶你去</Text>
                  <Text className='story-card-lite__name'>{(getStoryById(selectedPoi.storyLineId || 0) || {}).name || '城市漫遊篇章'}</Text>
                  <Text className='story-card-lite__progress'>已點亮 {state.completedStoryIds.length} 條主線 · 當前印章 {state.user.totalStamps} 枚</Text>
                </View>

                <View className='arrival-hint'>
                  <Text className='arrival-hint__title'>{selectedPoi.inRange ? '可以準備收下這枚印章了' : '再靠近一點就能觸發故事'}</Text>
                  <Text className='arrival-hint__desc'>{selectedPoi.inRange ? `建議停留 ${dwellSeconds}s 後完成這段旅程` : '先沿著推薦路線靠近，若訊號不穩也能稍後補簽。'}</Text>
                </View>

                <View className='action-bar'>
                  <Button className='action-bar__primary' onClick={() => completeCheckin(selectedPoi && selectedPoi.inRange ? 'gps' : 'mock')}>{selectedPoi.inRange ? '收下這枚印章' : '先記錄這次到達'}</Button>
                  <Button className='action-bar__secondary' onClick={() => Taro.navigateTo({ url: '/pages/story/index' })}>查看故事線</Button>
                </View>
              </>
            ) : (

              <Text className='empty-tip'>請選擇一個地點查看玩法詳情。</Text>
            )}
          </View>
        </View>

        {arrivalVisible && arrivalExperience && selectedPoi && (
          <View className='arrival-modal'>
            <View className='arrival-modal__card'>
              <Text className='arrival-modal__eyebrow'>到達觸發</Text>
              <Text className='arrival-modal__title'>{arrivalExperience.title}</Text>
              <Text className='arrival-modal__desc'>{arrivalExperience.narrative}</Text>

              <View className='arrival-modal__audio'>
                <View>
                  <Text className='arrival-modal__audioTitle'>{arrivalExperience.audioTitle}</Text>
                  <Text className='arrival-modal__audioMeta'>{arrivalExperience.audioDuration} · {voicePlaying ? '播放中' : '待播放'}</Text>
                </View>
                <Button className='arrival-modal__play' onClick={voicePlaying ? stopArrivalAudio : playArrivalAudio}>{voicePlaying ? '停止播報' : '播放介紹'}</Button>
              </View>

              <View className='arrival-modal__reward'>
                <Text className='arrival-modal__rewardLabel'>本次奖励</Text>
                <Text className='arrival-modal__rewardText'>{arrivalExperience.rewardLabel}</Text>
              </View>

              <View className='arrival-modal__actions'>
                <Button className='arrival-modal__primary' onClick={() => completeCheckin(selectedPoi.inRange ? 'gps' : 'mock')}>立即領取印章</Button>
                <Button className='arrival-modal__secondary' onClick={handleManualCheckin}>手動補簽</Button>
              </View>

              <View className='arrival-modal__elder'>
                <Text className='arrival-modal__elderTitle'>長者模式主操作</Text>
                <Button className='arrival-modal__elderButton' onClick={playArrivalAudio}>🔊 播放介紹</Button>
              </View>

              <View className='arrival-modal__footer'>
                <Button className='arrival-modal__ghost' onClick={() => Taro.makePhoneCall({ phoneNumber: emergencyContact.phone })}>一鍵求助 {emergencyContact.name}</Button>
                <Button className='arrival-modal__ghost' onClick={() => setArrivalVisible(false)}>稍後再說</Button>
              </View>
            </View>
          </View>
        )}

        {checkinResult && (
          <View className='reward-banner'>
            <Text className='reward-banner__title'>🎉 本次探索成功</Text>
            <Text className='reward-banner__desc'>你已在 {checkinResult.poiName} 获得 {checkinResult.stampName}，并获得 {checkinResult.experienceGained} 經驗值。</Text>
          </View>
        )}
      </View>
    </PageShell>
  )
}
