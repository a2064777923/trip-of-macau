import { useEffect, useMemo, useRef, useState } from 'react'
import { Button, Input, Map, ScrollView, Text, View } from '@tarojs/components'
import Taro from '@tarojs/taro'
import { fetchAmapInputTips, fetchAmapWalkingRoute, buildAmapLocation } from '../../services/amap'
import {
  getArrivalExperience,
  getEmergencyContact,
  getMapBootstrapConfig,
  getNearbyPois,
  getPoiById,
  getStoryById,
  loadGameState,
  performMockCheckin,
} from '../../services/gameService'
import PageShell from '../../components/PageShell'
import './index.scss'

const fallbackLocation = {
  latitude: 22.1987,
  longitude: 113.5439,
  accuracy: 35,
}

export default function MapPage() {
  const bootstrap = useMemo(() => getMapBootstrapConfig(), [])
  const emergencyContact = useMemo(() => getEmergencyContact(), [])
  const [location, setLocation] = useState(fallbackLocation)
  const [pois, setPois] = useState([])
  const [selectedPoiId, setSelectedPoiId] = useState(null)
  const [tips, setTips] = useState([])
  const [keyword, setKeyword] = useState('大三巴')
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

  const arrivalExperience = useMemo(() => {
    return selectedPoi ? getArrivalExperience(selectedPoi.id) : null
  }, [selectedPoi && selectedPoi.id])

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
    updateRoute(selectedPoi)
    setDwellSeconds(selectedPoi.inRange ? selectedPoi.staySeconds : 0)
    if (selectedPoi.inRange) {
      setArrivalVisible(true)
    }
  }, [selectedPoi && selectedPoi.id, selectedPoi && selectedPoi.inRange])

  useEffect(() => {
    const state = loadGameState()
    if (arrivalVisible && arrivalExperience && state.user.interfaceMode === 'elderly' && state.user.voiceGuideEnabled) {
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
      setLocation(nextLocation)
      const nearby = getNearbyPois(nextLocation.latitude, nextLocation.longitude, nextLocation.accuracy)
      setPois(nearby)
      setSelectedPoiId(nearby[0] ? nearby[0].id : null)
    } catch (error) {
      const nearby = getNearbyPois(fallbackLocation.latitude, fallbackLocation.longitude, fallbackLocation.accuracy)
      setPois(nearby)
      setSelectedPoiId(nearby[0] ? nearby[0].id : null)
      Taro.showToast({ title: '先为你打开附近熱門地標', icon: 'none' })
    } finally {
      setLoading(false)
    }
  }

  const updateRoute = async (poi) => {
    const summary = await fetchAmapWalkingRoute(
      buildAmapLocation(location.latitude, location.longitude),
      buildAmapLocation(poi.gcj02Latitude, poi.gcj02Longitude),
    )
    setRouteSummary(summary)
  }

  const handleSearchInput = async (event) => {
    const value = event.detail.value || ''
    setKeyword(value)
    if (!value.trim()) {
      setTips([])
      return
    }

    setSearching(true)
    const result = await fetchAmapInputTips(value)
    setTips(result.map((item) => ({ id: item.id, name: item.name, address: item.address })))
    setSearching(false)
  }

  const handleSelectPoi = (poiId) => {
    setSelectedPoiId(poiId)
    const poi = pois.find((item) => item.id === poiId)
    if (poi) {
      setKeyword(poi.name)
      setTips([])
      updateRoute(poi)
    }
  }

  const handleQuickLocate = async () => {
    await bootstrapPage()
  }

  const playArrivalAudio = () => {
    if (!arrivalExperience) return
    setVoicePlaying(true)

    if (audioRef.current) {
      audioRef.current.stop()
      audioRef.current.src = 'https://cdn.jsdelivr.net/gh/mdn/webaudio-examples/audio-basics/techno.wav'
      audioRef.current.play()
      return
    }

    Taro.showToast({ title: `${arrivalExperience.audioTitle} 播放中`, icon: 'none' })
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
    Taro.showToast({ title: `已獲得 ${result.stampName}`, icon: 'success' })
    const refreshed = getNearbyPois(location.latitude, location.longitude, location.accuracy)
    setPois(refreshed)
  }

  const handleMockCheckin = () => {
    completeCheckin(selectedPoi && selectedPoi.inRange ? 'gps' : 'mock')
  }

  const handleManualCheckin = () => {
    if (!arrivalExperience || !arrivalExperience.canManualCheckin) {
      Taro.showToast({ title: '此地點暫不支持手動補簽', icon: 'none' })
      return
    }

    Taro.showModal({
      title: '手動補簽確認',
      content: '若 GPS 訊號不穩，可使用 200 米內補簽兜底。是否繼續？',
      success: (res) => {
        if (res.confirm) {
          completeCheckin('manual')
        }
      },
    })
  }

  const state = loadGameState()

  return (
    <PageShell className='map-page'>

      <View className='map-hero'>
        <View className='map-hero__content'>
          <Text className='map-hero__eyebrow'>探索主戰場</Text>
          <Text className='map-hero__title'>高德地圖 + 澳門故事玩法</Text>
          <Text className='map-hero__subtitle'>不做瓦片地圖，但把到達觸發、語音導覽、手動補簽、故事與獎勵閉環補齊。</Text>
        </View>
      </View>

      <View className='map-shell'>
        <View className='map-card'>
          <View className='map-toolbar'>
            <View className='map-search'>
              <Input
                className='map-search__input'
                value={keyword}
                placeholder='搜索附近地点，如：大三巴'
                onInput={handleSearchInput}
              />
              <Button className='map-search__button' size='mini' onClick={handleQuickLocate}>刷新定位</Button>
            </View>
            <Text className='map-toolbar__meta'>高德 Key 已接入 · 当前城市 {bootstrap.city} · {searching ? '搜索中…' : '探索就绪'}</Text>
          </View>

          {!!tips.length && (
            <View className='tips-panel'>
              {tips.map((tip) => (
                <View key={tip.id} className='tips-panel__item'>
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
            markers={pois.map((poi) => ({
              id: poi.id,
              latitude: poi.gcj02Latitude,
              longitude: poi.gcj02Longitude,
              width: poi.id === selectedPoiId ? 40 : 32,
              height: poi.id === selectedPoiId ? 40 : 32,
              iconPath: poi.id === selectedPoiId
                ? 'https://a.amap.com/jsapi_demos/static/demo-center/icons/poi-marker-red.png'
                : 'https://a.amap.com/jsapi_demos/static/demo-center/icons/poi-marker-default.png',
              callout: {
                content: poi.name,
                color: '#3d4350',
                fontSize: 12,
                borderRadius: 12,
                bgColor: '#fff7fb',
                padding: 8,
                display: 'BYCLICK',
              },
            }))}
            circles={selectedPoi ? [{
              latitude: selectedPoi.gcj02Latitude,
              longitude: selectedPoi.gcj02Longitude,
              radius: selectedPoi.dynamicRadius,
              strokeColor: '#ff8ba7',
              fillColor: 'rgba(255,139,167,0.14)',
              strokeWidth: 2,
            }] : []}
            onMarkerTap={(event) => handleSelectPoi(Number(event.detail.markerId))}
          />

          <View className='location-strip'>
            <Text className='location-strip__title'>目前定位</Text>
            <Text className='location-strip__value'>{location.latitude.toFixed(5)}, {location.longitude.toFixed(5)}</Text>
            <Text className='location-strip__tip'>精度 ±{Math.round(location.accuracy)} 米 · 动态半径规则 {bootstrap.checkinRules.radiusPolicy} · 手动补签 {bootstrap.checkinRules.manualFallback}</Text>
          </View>
        </View>

        <View className='explore-grid'>
          <View className='explore-panel nearby-panel'>
            <View className='panel-header'>
              <Text className='panel-title'>附近任务点</Text>
              <Text className='panel-subtitle'>{loading ? '定位中...' : `${pois.length} 个可探索地点`}</Text>
            </View>
            <ScrollView scrollY className='poi-scroll'>
              {pois.map((poi) => (
                <View
                  key={poi.id}
                  className={`poi-card ${selectedPoiId === poi.id ? 'poi-card--active' : ''}`}
                  onClick={() => handleSelectPoi(poi.id)}
                >
                  <View className='poi-card__cover' style={{ background: poi.coverColor }}>
                    <Text className='poi-card__emoji'>{poi.icon}</Text>
                  </View>
                  <View className='poi-card__body'>
                    <View className='poi-card__top'>
                      <Text className='poi-card__name'>{poi.name}</Text>
                      <Text className={`poi-card__badge ${poi.inRange ? 'ready' : ''}`}>{poi.inRange ? '可触发' : poi.distanceText}</Text>
                    </View>
                    <Text className='poi-card__subtitle'>{poi.subtitle}</Text>
                    <Text className='poi-card__meta'>{poi.category} · {poi.storyName || '自由探索'} · 需停留 {poi.staySeconds}s</Text>
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
                </View>

                <View className='route-card'>
                  <Text className='route-card__title'>高德步行路线</Text>
                  {routeSummary ? (
                    <>
                      <Text className='route-card__summary'>约 {Math.round(Number(routeSummary.distance) / 100) / 10} km · {Math.ceil(Number(routeSummary.duration) / 60)} 分钟</Text>
                      {routeSummary.steps.slice(0, 3).map((step, index) => (
                        <Text key={`${step}-${index}`} className='route-card__step'>{index + 1}. {step}</Text>
                      ))}
                    </>
                  ) : (
                    <Text className='route-card__summary'>当前以 Mock 方式展示路线说明，若高德接口不可用会自动回退。</Text>
                  )}
                </View>

                <View className='story-card-lite'>
                  <Text className='story-card-lite__title'>关联故事线</Text>
                  <Text className='story-card-lite__name'>{(getStoryById(selectedPoi.storyLineId || 0) || {}).name || '自由探索模式'}</Text>
                  <Text className='story-card-lite__progress'>已解锁 {state.completedStoryIds.length} 条故事线 · 当前印章 {state.user.totalStamps} 枚</Text>
                </View>

                <View className='arrival-hint'>
                  <Text className='arrival-hint__title'>{selectedPoi.inRange ? '已进入触发范围' : '尚未进入触发范围'}</Text>
                  <Text className='arrival-hint__desc'>{selectedPoi.inRange ? `建议停留 ${dwellSeconds}s 后完成打卡` : '可先跟随路线接近目的地，定位不稳时可尝试手动补签。'}</Text>
                </View>

                <View className='action-bar'>
                  <Button className='action-bar__primary' onClick={handleMockCheckin}>完成到达打卡</Button>
                  <Button className='action-bar__secondary' onClick={() => Taro.navigateTo({ url: '/pages/story/index' })}>查看故事线</Button>
                </View>
              </>
            ) : (
              <Text className='empty-tip'>请选择一个地点以查看玩法详情。</Text>
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
            <Text className='reward-banner__desc'>你已在 {checkinResult.poiName} 获得 {checkinResult.stampName}，并获得 {checkinResult.experienceGained} 经验。</Text>
          </View>
        )}
      </View>
    </PageShell>
  )
}
