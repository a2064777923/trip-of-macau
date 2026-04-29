import { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import { Button, Input, Map, ScrollView, Text, View } from '@tarojs/components'
import Taro from '@tarojs/taro'
import PageShell from '../../components/PageShell'
import PoiTriggerModal from '../../components/PoiTriggerModal'
import TestJoystick from '../../components/TestJoystick'
import { cosAssetManifest } from '../../constants/assetUrls'
import { api } from '../../services/api'
import {
  updatePoiTriggerState,
  getPoiTriggerSession,
} from '../../services/poiTriggerService'
import {
  CheckinResult,
  getArrivalExperience,
  getCities,
  getCitySubMaps,
  getEmergencyContact,
  getNearbyPois,
  getPoiSearchTips,
  getStoryById,
  getWalkingRouteSummary,
  loadGameState,
  performMockCheckin,
  refreshPublicContent,
  registerCityVisitByLocation,
  switchCurrentCity,
  switchCurrentSubMap,
} from '../../services/gameService'
import type { CityProgressItem, PoiItem, SubMapProgressItem } from '../../types/game'
import './index.scss'

const isFiniteCoord = (value: unknown): value is number => typeof value === 'number' && Number.isFinite(value)

const fallbackLocation = {
  latitude: 22.1987,
  longitude: 113.5439,
  accuracy: 35,
}

const sanitizeLocation = (candidate?: Partial<typeof fallbackLocation> | null) => ({
  latitude: isFiniteCoord(candidate?.latitude) ? candidate.latitude : fallbackLocation.latitude,
  longitude: isFiniteCoord(candidate?.longitude) ? candidate.longitude : fallbackLocation.longitude,
  accuracy: isFiniteCoord(candidate?.accuracy) ? candidate.accuracy : fallbackLocation.accuracy,
})

const haversineDistanceM = (lat1: number, lng1: number, lat2: number, lng2: number): number => {
  const R = 6371000
  const toRad = (deg: number) => deg * Math.PI / 180
  const dLat = toRad(lat2 - lat1)
  const dLng = toRad(lng2 - lng1)
  const a = Math.sin(dLat / 2) ** 2 + Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) * Math.sin(dLng / 2) ** 2
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
  return R * c
}

const poiMarkerIconMap: Record<string, string> = {
  user: cosAssetManifest.poi.markerUserPng,
  ruins: cosAssetManifest.poi.markerRuinsPng,
  church: cosAssetManifest.poi.markerChurchPng,
  theater: cosAssetManifest.poi.markerTheaterPng,
  lisboa: cosAssetManifest.poi.markerLisboaPng,
  ghost: cosAssetManifest.poi.markerGhostPng,
}

const resolvePoiMarkerKey = (poiName: string, cityId?: string): string => {
  if (poiName.includes('大三巴')) return 'ruins'
  if (poiName.includes('玫瑰') || poiName.includes('媽閣')) return 'church'
  if (poiName.includes('崗頂') || poiName.includes('劇院')) return 'theater'
  if (poiName.includes('葡京')) return 'lisboa'
  if (poiName.includes('黑沙') || poiName.includes('路環')) return 'ruins'
  if (cityId === 'ecnu') return 'ghost'
  return 'church'
}

const getSpatialCenter = (
  cities: CityProgressItem[],
  subMaps: SubMapProgressItem[],
  cityId: string,
  subMapId?: string,
) => {
  const currentSubMap = subMaps.find((item) => item.id === subMapId)
  if (currentSubMap?.centerLat && currentSubMap?.centerLng) {
    return { latitude: currentSubMap.centerLat, longitude: currentSubMap.centerLng }
  }
  const currentCity = cities.find((item) => item.id === cityId)
  if (currentCity?.centerLat && currentCity?.centerLng) {
    return { latitude: currentCity.centerLat, longitude: currentCity.centerLng }
  }
  return { latitude: fallbackLocation.latitude, longitude: fallbackLocation.longitude }
}

export default function MapPage() {
  const initialState = loadGameState()
  const initialCities = getCities()
  const initialCityId = initialState.user.currentCityId || 'macau'
  const initialSubMaps = getCitySubMaps(initialCityId)
  const initialSubMapId = initialState.user.currentSubMapId

  const [state, setState] = useState(initialState)
  const [cities, setCities] = useState<CityProgressItem[]>(initialCities)
  const [currentCityId, setCurrentCityId] = useState(initialCityId)
  const [subMaps, setSubMaps] = useState<SubMapProgressItem[]>(initialSubMaps)
  const [currentSubMapId, setCurrentSubMapId] = useState<string | undefined>(initialSubMapId)
  const [location, setLocation] = useState(fallbackLocation)
  const [rawPois, setRawPois] = useState<PoiItem[]>([])
  const [selectedPoiId, setSelectedPoiId] = useState<number | null>(null)
  const [keyword, setKeyword] = useState('')
  const [tips, setTips] = useState<Array<{ id: string; name: string; address: string }>>([])
  const [routeSummary, setRouteSummary] = useState<ReturnType<typeof getWalkingRouteSummary> | null>(null)
  const [checkinResult, setCheckinResult] = useState<CheckinResult | null>(null)
  const [loading, setLoading] = useState(true)
  const [searching, setSearching] = useState(false)
  const [scale, setScale] = useState(17)
  const [fullscreen, setFullscreen] = useState(false)
  const [inRegion, setInRegion] = useState(true)
  const [heading, setHeading] = useState(0)
  const [userPulseRadius, setUserPulseRadius] = useState(22)
  
  // 测试模式状态
  const [isTestAccount, setIsTestAccount] = useState(false)
  const [mockLocation, setMockLocation] = useState<{ latitude: number; longitude: number } | null>(null)
  
  // 探索點触发系统状态
  const [triggerModalVisible, setTriggerModalVisible] = useState(false)
  const [triggeredPoi, setTriggeredPoi] = useState<PoiItem | null>(null)
  
  const audioRef = useRef<any>(null)
  const locationWatcherRef = useRef<number | null>(null)

  const currentCity = useMemo(
    () => cities.find((city) => city.id === currentCityId) || cities[0],
    [cities, currentCityId],
  )
  const currentSubMap = useMemo(
    () => subMaps.find((subMap) => subMap.id === currentSubMapId),
    [subMaps, currentSubMapId],
  )
  const spatialCenter = useMemo(
    () => getSpatialCenter(cities, subMaps, currentCityId, currentSubMapId),
    [cities, currentCityId, currentSubMapId, subMaps],
  )

  const refreshNearby = useCallback((lat: number, lng: number, accuracy: number, cityId = currentCityId, subMapId = currentSubMapId) => {
    const nearby = getNearbyPois(lat, lng, accuracy, cityId, subMapId)
    setRawPois(nearby)
    const nextSelectedPoi = nearby.find((poi) => poi.id === selectedPoiId) || nearby[0] || null
    setSelectedPoiId(nextSelectedPoi?.id || null)
    if (nextSelectedPoi) {
      setRouteSummary(getWalkingRouteSummary(nextSelectedPoi, { latitude: lat, longitude: lng }))
    } else {
      setRouteSummary(null)
    }
  }, [currentCityId, currentSubMapId, selectedPoiId])

  const checkRegion = useCallback((lat: number, lng: number, cityId = currentCityId, subMapId = currentSubMapId) => {
    const center = getSpatialCenter(cities, getCitySubMaps(cityId), cityId, subMapId)
    return haversineDistanceM(lat, lng, center.latitude, center.longitude) <= 30000
  }, [cities, currentCityId, currentSubMapId])

  const safeLocation = useMemo(() => sanitizeLocation(location), [location])
  const pois = useMemo(() => rawPois, [rawPois])
  const selectedPoi = useMemo(() => pois.find((poi) => poi.id === selectedPoiId) || pois[0] || null, [pois, selectedPoiId])
  const relatedStory = useMemo(() => (selectedPoi?.storyLineId ? getStoryById(selectedPoi.storyLineId) : null), [selectedPoi?.storyLineId, state.completedStoryIds])
  const arrivalExperience = useMemo(() => (selectedPoi ? getArrivalExperience(selectedPoi.id) : null), [selectedPoi?.id])
  const emergencyContact = useMemo(() => getEmergencyContact(), [])

  useEffect(() => {
    const timer = setInterval(() => {
      setUserPulseRadius((previous) => (previous >= 38 ? 22 : previous + 4))
    }, 260)
    return () => clearInterval(timer)
  }, [])

  useEffect(() => {
    void bootstrapPage()

    Taro.startCompass({
      success: () => {
        Taro.onCompassChange((result) => {
          setHeading(result.direction)
        })
      },
    })

    if (wx.createInnerAudioContext) {
      audioRef.current = wx.createInnerAudioContext()
      audioRef.current.obeyMuteSwitch = false
    }

    return () => {
      if (audioRef.current) {
        audioRef.current.destroy()
      }
      Taro.stopCompass()
    }
  }, [])

  useEffect(() => {
    setInRegion(checkRegion(location.latitude, location.longitude, currentCityId, currentSubMapId))
  }, [checkRegion, currentCityId, currentSubMapId, location.latitude, location.longitude])

  // 探索點触发检测
  useEffect(() => {
    if (loading || pois.length === 0) return

    const currentLocation = mockLocation || location
    
    // 每 2 秒检测一次探索點触发
    const interval = setInterval(() => {
      for (const poi of pois) {
        const result = updatePoiTriggerState(poi, {
          latitude: currentLocation.latitude,
          longitude: currentLocation.longitude,
          accuracy: location.accuracy,
        })

        if (result.shouldTrigger) {
          // 触发探索點
          setTriggeredPoi(poi)
          setTriggerModalVisible(true)
          
          // 播放提示音
          if (audioRef.current) {
            audioRef.current.src = 'https://cdn.tripofmacau.com/audio/poi-trigger.mp3'
            audioRef.current.play()
          }
          
          // 震动反馈
          Taro.vibrateShort()
          
          break // 一次只触发一个探索點
        }
      }
    }, 2000)

    return () => clearInterval(interval)
  }, [loading, pois, location, mockLocation])

  const bootstrapPage = async () => {
    setLoading(true)
    try {
      await refreshPublicContent()
      const nextState = loadGameState()
      const nextCities = getCities()
      const nextCityId = nextState.user.currentCityId || 'macau'
      const nextSubMaps = getCitySubMaps(nextCityId)
      const nextSubMapId = nextState.user.currentSubMapId

      // 检测测试模式
      try {
        const testMode = await api.user.getUserTestMode()
        setIsTestAccount(testMode.isTestAccount)
        if (testMode.mockEnabled && testMode.mockLatitude && testMode.mockLongitude) {
          setMockLocation({
            latitude: testMode.mockLatitude,
            longitude: testMode.mockLongitude,
          })
        }
      } catch (error) {
        console.warn('Failed to check test mode:', error)
      }

      let nextLocation = fallbackLocation
      try {
        const rawLocation = await Taro.getLocation({ type: 'gcj02' })
        nextLocation = sanitizeLocation({
          latitude: rawLocation.latitude,
          longitude: rawLocation.longitude,
          accuracy: rawLocation.accuracy || 30,
        })
        await registerCityVisitByLocation(nextLocation.latitude, nextLocation.longitude)
      } catch {
        nextLocation = {
          ...spatialCenter,
          accuracy: 35,
        }
      }

      const latestState = loadGameState()
      const latestCityId = latestState.user.currentCityId || nextCityId
      const latestSubMaps = getCitySubMaps(latestCityId)
      const latestSubMapId = latestState.user.currentSubMapId
      const latestCities = getCities()

      setState(latestState)
      setCities(latestCities)
      setCurrentCityId(latestCityId)
      setSubMaps(latestSubMaps)
      setCurrentSubMapId(latestSubMapId)
      setLocation(nextLocation)
      refreshNearby(nextLocation.latitude, nextLocation.longitude, nextLocation.accuracy, latestCityId, latestSubMapId)
    } finally {
      setLoading(false)
    }
  }

  const handleRecenter = async () => {
    try {
      const rawLocation = await Taro.getLocation({ type: 'gcj02' })
      const nextLocation = sanitizeLocation({
        latitude: rawLocation.latitude,
        longitude: rawLocation.longitude,
        accuracy: rawLocation.accuracy || 30,
      })
      setLocation(nextLocation)
      setScale(17)
      setInRegion(checkRegion(nextLocation.latitude, nextLocation.longitude, currentCityId, currentSubMapId))
      refreshNearby(nextLocation.latitude, nextLocation.longitude, nextLocation.accuracy, currentCityId, currentSubMapId)
    } catch {
      const center = getSpatialCenter(cities, subMaps, currentCityId, currentSubMapId)
      setLocation({ ...center, accuracy: 35 })
      refreshNearby(center.latitude, center.longitude, 35, currentCityId, currentSubMapId)
    }
  }

  const handleSearchInput = (event: any) => {
    const value = event.detail.value || ''
    setKeyword(value)
    if (!value.trim()) {
      setTips([])
      return
    }
    setSearching(true)
    const nextTips = getPoiSearchTips(value, currentCityId, currentSubMapId)
    setTips(nextTips.map((item) => ({ id: item.id, name: item.name, address: item.address })))
    setSearching(false)
  }

  const handleSelectPoi = (poiId: number) => {
    const poi = pois.find((item) => item.id === poiId)
    if (!poi) return
    setSelectedPoiId(poiId)
    setKeyword(poi.name)
    setTips([])
    setRouteSummary(getWalkingRouteSummary(poi, location))
  }

  const handleSwitchCity = async (cityId: string) => {
    // 禁止切换到其他城市，只能在当前城市的子地图间切换
    if (cityId !== currentCityId) {
      Taro.showToast({ 
        title: '暫不支持切換城市，請在當前城市的子地圖間切換', 
        icon: 'none',
        duration: 2000
      })
      return
    }
    
    try {
      await switchCurrentCity(cityId)
      const nextState = loadGameState()
      const nextCities = getCities()
      const nextSubMaps = getCitySubMaps(cityId)
      const nextSubMapId = nextState.user.currentSubMapId
      const center = getSpatialCenter(nextCities, nextSubMaps, cityId, nextSubMapId)
      setState(nextState)
      setCities(nextCities)
      setCurrentCityId(cityId)
      setSubMaps(nextSubMaps)
      setCurrentSubMapId(nextSubMapId)
      setLocation({ ...center, accuracy: location.accuracy || 35 })
      refreshNearby(center.latitude, center.longitude, location.accuracy || 35, cityId, nextSubMapId)
      Taro.showToast({ title: `已切換至 ${nextCities.find((item) => item.id === cityId)?.name || cityId}`, icon: 'success' })
    } catch (error) {
      Taro.showToast({ title: error instanceof Error ? error.message : '切換城市失敗', icon: 'none' })
    }
  }

  const handleSwitchSubMap = async (subMapId?: string) => {
    try {
      await switchCurrentSubMap(subMapId)
      const nextState = loadGameState()
      const nextSubMapId = nextState.user.currentSubMapId
      const nextSubMaps = getCitySubMaps(currentCityId)
      const center = getSpatialCenter(cities, nextSubMaps, currentCityId, nextSubMapId)
      setState(nextState)
      setSubMaps(nextSubMaps)
      setCurrentSubMapId(nextSubMapId)
      setLocation({ ...center, accuracy: location.accuracy || 35 })
      refreshNearby(center.latitude, center.longitude, location.accuracy || 35, currentCityId, nextSubMapId)
    } catch (error) {
      Taro.showToast({ title: error instanceof Error ? error.message : '切換子地圖失敗', icon: 'none' })
    }
  }

  const completeCheckin = async (mode: 'gps' | 'manual' | 'mock') => {
    if (!selectedPoi) return
    try {
      const result = await performMockCheckin(selectedPoi.id, mode)
      setCheckinResult(result)
      const nextState = loadGameState()
      setState(nextState)
      setCities(getCities())
      setSubMaps(getCitySubMaps(nextState.user.currentCityId || currentCityId))
      refreshNearby(location.latitude, location.longitude, location.accuracy, nextState.user.currentCityId || currentCityId, nextState.user.currentSubMapId)
      Taro.showToast({ title: `已獲得 ${result.stampName}`, icon: 'success' })
    } catch (error) {
      Taro.showToast({ title: error instanceof Error ? error.message : '打卡失敗', icon: 'none' })
    }
  }

  const handleOpenIndoorMap = () => {
    if (!selectedPoi) return
    Taro.navigateTo({
      url: `/pages/map/indoor/index?poiId=${selectedPoi.id}&name=${encodeURIComponent(selectedPoi.name)}`,
    })
  }

  const handlePoiCheckin = async () => {
    if (!triggeredPoi) return
    
    try {
      const result = await performMockCheckin(triggeredPoi.id, 'gps')
      const latestState = loadGameState()
      setState(latestState)
      const currentLocation = mockLocation || location
      refreshNearby(currentLocation.latitude, currentLocation.longitude, location.accuracy, currentCityId, currentSubMapId)
      return result
    } catch (error) {
      console.error('Checkin failed:', error)
      throw error
    }
  }

  const handleMockLocationChange = (newLocation: { latitude: number; longitude: number }) => {
    setMockLocation(newLocation)
    setLocation({ ...newLocation, accuracy: location.accuracy })
    refreshNearby(newLocation.latitude, newLocation.longitude, location.accuracy, currentCityId, currentSubMapId)
  }

  const handleResetMockLocation = async () => {
    try {
      const rawLocation = await Taro.getLocation({ type: 'gcj02' })
      const realLocation = sanitizeLocation({
        latitude: rawLocation.latitude,
        longitude: rawLocation.longitude,
        accuracy: rawLocation.accuracy || 30,
      })
      setMockLocation(null)
      setLocation(realLocation)
      refreshNearby(realLocation.latitude, realLocation.longitude, realLocation.accuracy, currentCityId, currentSubMapId)
    } catch (error) {
      console.error('Failed to reset location:', error)
    }
  }

  const centerCoord = selectedPoi
    ? { latitude: selectedPoi.latitude, longitude: selectedPoi.longitude }
    : spatialCenter

  return (
    <PageShell className='map-page'>
      <View className='map-hero'>
        <View className='map-hero__content'>
          <View className='map-hero__main'>
            <View className='map-hero__text'>
              <Text className='map-hero__eyebrow'>城市探索地圖</Text>
              <Text className='map-hero__title'>{currentCity?.name || '澳門探索'}{currentSubMap ? ` · ${currentSubMap.name}` : ''}</Text>
              <Text className='map-hero__subtitle'>{currentSubMap?.subtitle || currentCity?.subtitle || '從頂層城市進入，再下鑽到子地圖，沿著故事與探索點持續探索。'}</Text>
            </View>
            <View className='map-hero__stats'>
              <View className='map-hero__progressCard'>
                <Text className='map-hero__progressLabel'>探索進度</Text>
                <Text className='map-hero__progressValue'>{currentSubMap?.explorationProgress ?? currentCity?.explorationProgress ?? 0}%</Text>
              </View>
              <View className='map-hero__titleBadge'>
                <Text className='map-hero__titleBadgeText'>{currentCity?.titleReward || state.user.title}</Text>
              </View>
            </View>
          </View>
          <View className='map-hero__storyCard' onClick={() => Taro.navigateTo({ url: '/pages/story/index' })}>
            <Text className='map-hero__storyLabel'>目前故事關聯</Text>
            <Text className='map-hero__storyTitle'>{relatedStory?.name || '尚未選定故事章節'}</Text>
            <Text className='map-hero__storyDesc'>
              {selectedPoi ? `目前選中的探索點為「${selectedPoi.name}」，可直接查看其故事內容與打卡回饋。` : '選擇一個探索點，即可查看對應的故事線、路線摘要與互動入口。'}
            </Text>
            <Text className='map-hero__storyAction'>前往故事頁</Text>
          </View>
        </View>
      </View>

      <View className='map-shell'>
        <ScrollView className='city-switcher' scrollX>
          {cities.filter(city => city.unlocked).map((city) => (
            <View
              key={city.id}
              className={`city-switcher__item ${currentCityId === city.id ? 'active' : ''}`}
              onClick={() => void handleSwitchCity(city.id)}
            >
              <Text className='city-switcher__name'>{city.name}</Text>
            </View>
          ))}
        </ScrollView>

        {subMaps.length ? (
          <ScrollView className='submap-switcher' scrollX>
            <View
              className={`submap-switcher__item ${!currentSubMapId ? 'active' : ''}`}
              onClick={() => void handleSwitchSubMap(undefined)}
            >
              <Text className='submap-switcher__name'>整個城市</Text>
            </View>
            {subMaps.map((subMap) => (
              <View
                key={subMap.id}
                className={`submap-switcher__item ${currentSubMapId === subMap.id ? 'active' : ''}`}
                onClick={() => void handleSwitchSubMap(subMap.id)}
              >
                <Text className='submap-switcher__name'>{subMap.name}</Text>
              </View>
            ))}
          </ScrollView>
        ) : null}

        <View className='map-card'>
          <View className='map-toolbar'>
            <View className='map-search'>
              <Input className='map-search__input' value={keyword} placeholder='搜尋當前城市或子地圖中的探索點' onInput={handleSearchInput} />
              <Button className='map-search__button' size='mini' onClick={handleRecenter}>回到我的位置</Button>
            </View>
            <View className='map-toolbar__row'>
              <Text className='map-toolbar__meta'>
                {currentCity?.name || '澳門'}
                {currentSubMap ? ` · ${currentSubMap.name}` : ''}
                {' · '}
                {searching ? '正在整理探索點...' : `目前共 ${pois.length} 個探索點`}
              </Text>
            </View>
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

          <View className={`map-canvas-wrap ${fullscreen ? 'map-canvas-wrap--fullscreen' : ''}`}>
            <Map
              className={`map-canvas ${fullscreen ? 'fullscreen' : ''}`}
              longitude={centerCoord.longitude}
              latitude={centerCoord.latitude}
              scale={scale}
              showLocation={false}
              enableRotate={false}
              enableOverlooking={false}
              markers={[
                {
                  id: 9999,
                  latitude: safeLocation.latitude,
                  longitude: safeLocation.longitude,
                  iconPath: poiMarkerIconMap.user,
                  width: 40,
                  height: 40,
                  zIndex: 999,
                  rotate: heading,
                },
                ...pois.map((poi) => ({
                  id: poi.id,
                  latitude: poi.latitude,
                  longitude: poi.longitude,
                  iconPath: poi.mapIconUrl || poiMarkerIconMap[poi.markerKey || resolvePoiMarkerKey(poi.name, poi.cityId)],
                  width: 44,
                  height: 44,
                  anchor: { x: 0.5, y: 0.88 },
                })),
              ]}
              onMarkerTap={(event) => {
                const markerId = event.detail.markerId
                if (markerId !== 9999) {
                  handleSelectPoi(markerId)
                }
              }}
              circles={[
                {
                  latitude: safeLocation.latitude,
                  longitude: safeLocation.longitude,
                  radius: userPulseRadius,
                  strokeColor: 'rgba(74,144,226,0.45)',
                  fillColor: 'rgba(74,144,226,0.10)',
                  strokeWidth: 2,
                },
                ...(selectedPoi ? [{
                  latitude: selectedPoi.latitude,
                  longitude: selectedPoi.longitude,
                  radius: selectedPoi.triggerRadius || 50,
                  strokeColor: '#ff8ba7',
                  fillColor: 'rgba(255,139,167,0.08)',
                  strokeWidth: 2,
                }] : []),
              ]}
            />

            <View className='map-floating-actions'>
              <View className='recenter-btn' onClick={handleRecenter}>
                <Text className='recenter-btn__icon'>定位</Text>
              </View>
              {selectedPoi ? (
                <View className='indoor-entry-btn' onClick={handleOpenIndoorMap}>
                  <Text className='indoor-entry-btn__icon'>室內</Text>
                  <Text className='indoor-entry-btn__label'>室內地圖</Text>
                </View>
              ) : null}
            </View>

            <Button className='fullscreen-btn' onClick={() => setFullscreen((previous) => !previous)}>
              {fullscreen ? '退出全屏' : '全屏查看'}
            </Button>

            {!inRegion ? (
              <View className='out-of-region-mask'>
                <View className='out-of-region-overlay' />
                <View className='out-of-region-content'>
                  <Text className='out-of-region-icon'>⚠️</Text>
                  <Text className='out-of-region-title'>您已離開當前區域</Text>
                  <Text className='out-of-region-text'>目前距離此城市/子地圖中心較遠（超過 30km），地圖仍可瀏覽，但打卡判定可能不準確。</Text>
                  <Button className='out-of-region-button' size='mini' onClick={handleRecenter}>
                    返回我的位置
                  </Button>
                </View>
              </View>
            ) : null}
          </View>
        </View>

        <View className='explore-grid'>
          <View className='explore-panel nearby-panel'>
            <View className='panel-header'>
              <Text className='panel-title'>附近探索點</Text>
              <Text className='panel-subtitle'>{loading ? '正在載入...' : `${pois.length} 個可探索點位`}</Text>
            </View>
            <ScrollView scrollY className='poi-scroll'>
              {pois.map((poi) => (
                <View key={poi.id} className={`poi-card ${selectedPoi?.id === poi.id ? 'poi-card--active' : ''}`} onClick={() => handleSelectPoi(poi.id)}>
                  <View className='poi-card__cover' style={{ background: poi.coverColor }}>
                    <Text className='poi-card__emoji'>{poi.icon}</Text>
                  </View>
                  <View className='poi-card__body'>
                    <View className='poi-card__top'>
                      <Text className='poi-card__name'>{poi.name}</Text>
                      <Text className={`poi-card__badge ${poi.inRange ? 'ready' : ''}`}>{poi.inRange ? '可打卡' : poi.distanceText}</Text>
                    </View>
                    <Text className='poi-card__subtitle'>{poi.subtitle}</Text>
                    <Text className='poi-card__meta'>{poi.subMapName || poi.district} · 建議停留 {poi.staySeconds}s</Text>
                  </View>
                </View>
              ))}
            </ScrollView>
          </View>

          <View className='explore-panel detail-panel'>
            <View className='panel-header'>
              <Text className='panel-title'>當前探索點</Text>
              <Text className='panel-subtitle'>查看故事關聯、路線摘要與打卡入口。</Text>
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
                      <Text className='selected-poi__statLabel'>當前距離</Text>
                      <Text className='selected-poi__statValue'>{selectedPoi.distanceText}</Text>
                    </View>
                    <View className='selected-poi__stat'>
                      <Text className='selected-poi__statLabel'>所屬子地圖</Text>
                      <Text className='selected-poi__statValue'>{selectedPoi.subMapName || '整個城市'}</Text>
                    </View>
                    <View className='selected-poi__stat selected-poi__stat--full'>
                      <Text className='selected-poi__statLabel'>故事線</Text>
                      <Text className='selected-poi__statValue'>{relatedStory?.name || selectedPoi.storyName || '尚未綁定故事線'}</Text>
                    </View>
                  </View>
                </View>

                {routeSummary ? (
                  <View className='route-card'>
                    <Text className='route-card__title'>前往摘要</Text>
                    <Text className='route-card__summary'>距離 {Math.round(Number(routeSummary.distance) / 10) / 100} km · 約 {Math.ceil(Number(routeSummary.duration) / 60)} 分鐘</Text>
                    {routeSummary.steps.slice(0, 3).map((step, index) => (
                      <Text key={`${step}-${index}`} className='route-card__step'>{index + 1}. {step}</Text>
                    ))}
                  </View>
                ) : null}

                {arrivalExperience ? (
                  <View className='story-card-lite'>
                    <Text className='story-card-lite__title'>到達後可觸發</Text>
                    <Text className='story-card-lite__name'>{arrivalExperience.rewardLabel}</Text>
                    <Text className='story-card-lite__progress'>{arrivalExperience.audioTitle}</Text>
                  </View>
                ) : null}

                <View className='action-bar'>
                  <Button className='action-bar__primary' onClick={() => void completeCheckin(selectedPoi.inRange ? 'gps' : 'mock')}>
                    {selectedPoi.inRange ? '立即打卡' : '模擬到達'}
                  </Button>
                  <Button className='action-bar__secondary' onClick={() => Taro.navigateTo({ url: '/pages/story/index' })}>
                    查看故事
                  </Button>
                </View>
              </>
            ) : (
              <Text className='empty-tip'>請先選擇一個探索點。</Text>
            )}
          </View>
        </View>

        {checkinResult ? (
          <View className='reward-banner'>
            <Text className='reward-banner__title'>探索成功</Text>
            <Text className='reward-banner__desc'>你已在 {checkinResult.poiName} 獲得 {checkinResult.stampName}，並得到 {checkinResult.experienceGained} 經驗值。</Text>
          </View>
        ) : null}

        <View className='reward-banner' style={{ marginTop: 12 }}>
          <Text className='reward-banner__title'>緊急聯絡</Text>
          <Text className='reward-banner__desc'>{emergencyContact.name} · {emergencyContact.phone}</Text>
        </View>
      </View>

      {/* POI 触发弹窗 */}
      {triggeredPoi && (
        <PoiTriggerModal
          poi={triggeredPoi}
          visible={triggerModalVisible}
          onClose={() => {
            setTriggerModalVisible(false)
            setTriggeredPoi(null)
          }}
          onCheckin={handlePoiCheckin}
        />
      )}

      {/* 测试摇杆（仅测试账号可见） */}
      {isTestAccount && (
        <TestJoystick
          currentLocation={mockLocation || location}
          onLocationChange={handleMockLocationChange}
          onReset={handleResetMockLocation}
        />
      )}
    </PageShell>
  )
}
