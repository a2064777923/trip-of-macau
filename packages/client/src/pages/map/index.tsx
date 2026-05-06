import { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import { Button, Input, Map as NativeMap, ScrollView, Text, View } from '@tarojs/components'
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
  clearStoryModeRouteContext,
  ensureDevBypassSession,
  getArrivalExperience,
  getCities,
  getCitySubMaps,
  getEmergencyContact,
  getNearbyPois,
  getPoiById,
  getPoiSearchTips,
  getStoryModeRouteContext,
  getStoryById,
  getWalkingRouteSummary,
  hasActiveSessionToken,
  loadGameState,
  performMockCheckin,
  refreshPublicContent,
  registerCityVisitByLocation,
  resolveStoryRouteDestination,
} from '../../services/gameService'
import type { CityProgressItem, PoiItem, StoryModeRouteContext, SubMapProgressItem } from '../../types/game'
import './index.scss'

const toFiniteNumber = (value: unknown): number | null => {
  if (typeof value === 'number' && Number.isFinite(value)) {
    return value
  }
  if (typeof value === 'string' && value.trim()) {
    const parsed = Number(value)
    return Number.isFinite(parsed) ? parsed : null
  }
  return null
}

const isFiniteCoord = (value: unknown): value is number => typeof value === 'number' && Number.isFinite(value)
const isPositiveFiniteId = (value: unknown): value is number => typeof value === 'number' && Number.isFinite(value) && value > 0
const hasValidCoordinate = (value?: Partial<Pick<PoiItem, 'latitude' | 'longitude'>> | null) => (
  toFiniteNumber(value?.latitude) !== null
  && toFiniteNumber(value?.longitude) !== null
  && Math.abs(toFiniteNumber(value?.latitude) || 0) <= 90
  && Math.abs(toFiniteNumber(value?.longitude) || 0) <= 180
)

const fallbackLocation = {
  latitude: 22.1957608,
  longitude: 113.5490033,
  accuracy: 35,
}

const sanitizeLocation = (candidate?: Partial<typeof fallbackLocation> | null) => ({
  latitude: toFiniteNumber(candidate?.latitude) ?? fallbackLocation.latitude,
  longitude: toFiniteNumber(candidate?.longitude) ?? fallbackLocation.longitude,
  accuracy: toFiniteNumber(candidate?.accuracy) ?? fallbackLocation.accuracy,
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

const getNativeMapIconPath = (poi: Pick<PoiItem, 'name' | 'cityId' | 'markerKey' | 'mapIconUrl'>) => {
  const remoteIcon = poi.mapIconUrl || ''
  if (/^https?:\/\/.+\.(png|jpg|jpeg)(\?.*)?$/i.test(remoteIcon)) {
    return remoteIcon
  }
  return poiMarkerIconMap[poi.markerKey || resolvePoiMarkerKey(poi.name, poi.cityId)] || poiMarkerIconMap.church
}

const toMapPoint = (value?: Partial<Pick<PoiItem, 'latitude' | 'longitude'>> | null) => {
  const latitude = toFiniteNumber(value?.latitude)
  const longitude = toFiniteNumber(value?.longitude)
  if (latitude === null || longitude === null || Math.abs(latitude) > 90 || Math.abs(longitude) > 180) {
    return null
  }
  return { latitude, longitude }
}

const toNativeMarkerId = (value: unknown, fallback: number) => {
  const parsed = toFiniteNumber(value)
  return parsed !== null && parsed > 0 ? Math.trunc(parsed) : fallback
}

const sanitizeMapScale = (value: number) => Math.min(20, Math.max(3, Math.trunc(value || 17)))

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
  const [storyModeRouteContext, setStoryModeRouteContext] = useState<StoryModeRouteContext | null>(null)
  const [storyModeDestination, setStoryModeDestination] = useState<PoiItem | null>(null)
  const [nativeMapReady, setNativeMapReady] = useState(false)
  
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
  const currentMapDisplayName = currentCity?.name || '澳門'
  const currentRouteAreaName = currentSubMap
    ? `${currentMapDisplayName} · ${currentSubMap.name}`
    : currentMapDisplayName
  const spatialCenter = useMemo(
    () => getSpatialCenter(cities, subMaps, currentCityId, currentSubMapId),
    [cities, currentCityId, currentSubMapId, subMaps],
  )

  const refreshNearby = useCallback((
    lat: number,
    lng: number,
    accuracy: number,
    cityId = currentCityId,
    subMapId = currentSubMapId,
    preferredPoiId?: number,
  ) => {
    const nearby = getNearbyPois(lat, lng, accuracy, cityId, subMapId)
    setRawPois(nearby)
    const nextSelectedPoi = (preferredPoiId ? nearby.find((poi) => poi.id === preferredPoiId) : null)
      || nearby.find((poi) => poi.id === selectedPoiId)
      || nearby[0]
      || null
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
  const selectedPoi = useMemo(
    () => pois.find((poi) => poi.id === selectedPoiId)
      || (storyModeDestination && storyModeDestination.id === selectedPoiId ? storyModeDestination : null)
      || pois[0]
      || storyModeDestination
      || null,
    [pois, selectedPoiId, storyModeDestination],
  )
  const relatedStory = useMemo(() => (selectedPoi?.storyLineId ? getStoryById(selectedPoi.storyLineId) : null), [selectedPoi?.storyLineId, state.completedStoryIds])
  const arrivalExperience = useMemo(() => (selectedPoi ? getArrivalExperience(selectedPoi.id) : null), [selectedPoi?.id])
  const emergencyContact = useMemo(() => getEmergencyContact(), [])
  const hasEmergencyContact = useMemo(() => {
    const name = emergencyContact.name?.trim()
    const phone = emergencyContact.phone?.trim()
    return !!phone && !!name && name !== '緊急聯絡人'
  }, [emergencyContact.name, emergencyContact.phone])
  const storyCurrentChapter = useMemo(
    () => storyModeRouteContext?.chapters.find((chapter) => chapter.status === 'current')
      || storyModeRouteContext?.chapters.find((chapter) => chapter.status !== 'locked'),
    [storyModeRouteContext],
  )
  const storyNextChapter = useMemo(
    () => storyModeRouteContext?.chapters
      .filter((chapter) => chapter.status !== 'locked')
      .find((chapter) => (chapter.chapterOrder || 0) > (storyCurrentChapter?.chapterOrder || 0)),
    [storyModeRouteContext, storyCurrentChapter],
  )
  const storyRoutePois = useMemo(() => {
    if (!storyModeRouteContext) {
      return []
    }
    return storyModeRouteContext.chapters
      .map((chapter) => {
        const directPoi = chapter.anchorTargetId ? getPoiById(chapter.anchorTargetId) : null
        if (hasValidCoordinate(directPoi)) {
          return { chapter, poi: directPoi }
        }
        const chapterPoint = toMapPoint(chapter)
        if (chapterPoint) {
          return {
            chapter,
            poi: {
              id: toNativeMarkerId(chapter.anchorTargetId, chapter.chapterId),
              name: chapter.locationName || chapter.title,
              latitude: chapterPoint.latitude,
              longitude: chapterPoint.longitude,
            },
          }
        }
        return null
      })
      .filter(Boolean) as Array<{
        chapter: StoryModeRouteContext['chapters'][number]
        poi: Pick<PoiItem, 'id' | 'name' | 'latitude' | 'longitude'>
      }>
  }, [storyModeRouteContext])

  useEffect(() => {
    const timer = setInterval(() => {
      setUserPulseRadius((previous) => (previous >= 38 ? 22 : previous + 4))
    }, 260)
    return () => clearInterval(timer)
  }, [])

  useEffect(() => {
    void bootstrapPage()
    const nativeMapTimer = setTimeout(() => {
      setNativeMapReady(true)
    }, 600)

    Taro.startCompass({
      success: () => {
        Taro.onCompassChange((result) => {
          setHeading(result.direction)
        })
      },
      fail: (error) => {
        console.warn('Compass is not available in the current environment.', error)
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
      clearTimeout(nativeMapTimer)
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
      await ensureDevBypassSession()
      await refreshPublicContent()
      const nextState = loadGameState()
      const nextCities = getCities()
      const nextCityId = nextState.user.currentCityId || 'macau'
      const nextSubMaps = getCitySubMaps(nextCityId)
      const nextSubMapId = nextState.user.currentSubMapId

      if (hasActiveSessionToken() && nextState.user.authStatus !== 'anonymous') {
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
      } else {
        setIsTestAccount(false)
        setMockLocation(null)
      }

      let nextLocation = fallbackLocation
      try {
        const rawLocation = await Promise.race([
          Taro.getLocation({ type: 'gcj02' }),
          new Promise<never>((_, reject) => setTimeout(() => reject(new Error('location timeout')), 2500)),
        ])
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
      const storyContext = getStoryModeRouteContext()
      const storyDestination = resolveStoryRouteDestination(storyContext)
      setStoryModeRouteContext(storyContext)
      setStoryModeDestination(storyDestination)
      if (storyDestination) {
        setSelectedPoiId(storyDestination.id)
        setKeyword(storyDestination.name)
        setRouteSummary(getWalkingRouteSummary(storyDestination, nextLocation))
      }
      refreshNearby(
        nextLocation.latitude,
        nextLocation.longitude,
        nextLocation.accuracy,
        latestCityId,
        latestSubMapId,
        storyDestination?.id,
      )
    } finally {
      setLoading(false)
    }
  }

  const handleRecenter = async () => {
    try {
      const rawLocation = await Promise.race([
        Taro.getLocation({ type: 'gcj02' }),
        new Promise<never>((_, reject) => setTimeout(() => reject(new Error('location timeout')), 2500)),
      ])
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

  const handleReturnToStory = () => {
    if (!storyModeRouteContext || !isPositiveFiniteId(storyModeRouteContext.storylineId)) {
      Taro.navigateTo({ url: '/pages/story/index' })
      return
    }
    const params = [`storyId=${encodeURIComponent(String(storyModeRouteContext.storylineId))}`]
    if (storyModeRouteContext.currentChapterId && isPositiveFiniteId(storyModeRouteContext.currentChapterId)) {
      params.push(`chapterId=${encodeURIComponent(String(storyModeRouteContext.currentChapterId))}`)
    }
    Taro.navigateTo({ url: `/pages/story/index?${params.join('&')}` })
  }

  const handleClearStoryRoute = () => {
    clearStoryModeRouteContext()
    setStoryModeRouteContext(null)
    setStoryModeDestination(null)
    Taro.showToast({ title: '已清除本次故事路線強調', icon: 'success' })
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

  const handlePrimaryPoiAction = async () => {
    if (!selectedPoi) {
      return
    }
    if (selectedPoi.inRange) {
      await completeCheckin('gps')
      return
    }
    setKeyword(selectedPoi.name)
    setRouteSummary(getWalkingRouteSummary(selectedPoi, location))
    Taro.showToast({
      title: '已標記目的地，靠近後再完成打卡',
      icon: 'none',
      duration: 2200,
    })
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
      const rawLocation = await Promise.race([
        Taro.getLocation({ type: 'gcj02' }),
        new Promise<never>((_, reject) => setTimeout(() => reject(new Error('location timeout')), 2500)),
      ])
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

  const storyDestinationPoint = toMapPoint(storyModeDestination)
  const selectedPoiPoint = toMapPoint(selectedPoi)
  const spatialCenterPoint = toMapPoint(spatialCenter)
  const centerCoord = storyDestinationPoint
    || selectedPoiPoint
    || spatialCenterPoint
    || { latitude: fallbackLocation.latitude, longitude: fallbackLocation.longitude }
  const mapMarkers = [
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
    ...(storyModeRouteContext && storyRoutePois.length ? storyRoutePois.map((routeItem) => {
      const poi = routeItem.poi
      const isStoryCurrent = !!storyModeDestination && storyModeDestination.id === poi.id
      const isStoryInactive = !isStoryCurrent
      const point = toMapPoint(poi)
      if (!point) {
        return null
      }
      return {
        id: toNativeMarkerId(poi.id, routeItem.chapter.chapterId),
        latitude: point.latitude,
        longitude: point.longitude,
        iconPath: getNativeMapIconPath(poi),
        width: isStoryCurrent ? 58 : isStoryInactive ? 36 : 44,
        height: isStoryCurrent ? 58 : isStoryInactive ? 36 : 44,
        anchor: { x: 0.5, y: 0.88 },
        zIndex: isStoryCurrent ? 900 : isStoryInactive ? 80 : 120,
      }
    }).filter((marker): marker is NonNullable<typeof marker> => !!marker) : pois
      .map((poi) => {
        const point = toMapPoint(poi)
        if (!point) {
          return null
        }
        return {
          id: toNativeMarkerId(poi.id, 1),
          latitude: point.latitude,
          longitude: point.longitude,
          iconPath: getNativeMapIconPath(poi),
          width: 44,
          height: 44,
          anchor: { x: 0.5, y: 0.88 },
        }
      })
      .filter((marker): marker is NonNullable<typeof marker> => !!marker)),
  ].filter((marker) => (
    Number.isFinite(marker.id)
    && marker.id > 0
    && !!marker.iconPath
    && hasValidCoordinate(marker)
  ))
  const mapCircles = [
    {
      latitude: safeLocation.latitude,
      longitude: safeLocation.longitude,
      radius: userPulseRadius,
      color: '#4A90E673',
      fillColor: '#4A90E61A',
      strokeWidth: 2,
    },
    ...(selectedPoiPoint ? [{
      latitude: selectedPoiPoint.latitude,
      longitude: selectedPoiPoint.longitude,
      radius: Math.max(10, toFiniteNumber(selectedPoi?.triggerRadius) ?? 50),
      color: '#FF8BA7',
      fillColor: '#FF8BA714',
      strokeWidth: 2,
    }] : []),
  ]
  const storyRoutePolylinePoints = storyRoutePois
    .map((item) => toMapPoint(item.poi))
    .filter((point): point is { latitude: number; longitude: number } => !!point)
  const storyPolyline = storyRoutePolylinePoints.length >= 2 ? [
    {
      points: storyRoutePolylinePoints,
      color: '#9d513b',
      width: 6,
      dottedLine: false,
    },
  ] : []
  const shouldMountNativeMap = nativeMapReady && hasValidCoordinate(centerCoord)
  const selectedPoiStoryTitle = relatedStory?.name || selectedPoi?.storyName || '等待故事開放'
  const selectedPoiActionText = selectedPoi?.inRange ? '立即打卡' : '前往附近'

  return (
    <PageShell className='map-page'>
      <View className='map-hero'>
        <View className='map-hero__content'>
          <View className='map-hero__main'>
            <View className='map-hero__text'>
              <Text className='map-hero__eyebrow'>當前探索地圖</Text>
              <Text className='map-hero__title'>{currentMapDisplayName}</Text>
              <Text className='map-hero__subtitle'>{currentSubMap?.subtitle || currentCity?.subtitle || '從首頁選好探索地圖後，就在這裡跟著故事、地標與現場提示前進。'}</Text>
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
            <Text className='map-hero__storyLabel'>今日主線</Text>
            <Text className='map-hero__storyTitle'>{relatedStory?.name || '先選一條主線旅程'}</Text>
            <Text className='map-hero__storyDesc'>
              {selectedPoi ? `你正查看「${selectedPoi.name}」，可從這裡進入故事、前往現場並完成打卡。` : '選擇探索點後，會看到前往路線與可觸發內容。'}
            </Text>
            <Text className='map-hero__storyAction'>前往故事頁</Text>
          </View>
        </View>
      </View>

      <View className='map-shell'>
        {storyModeRouteContext ? (
          <View className='story-route-panel story-route-panel--map'>
            <View className='story-route-panel__header'>
              <View>
                <Text className='story-route-panel__eyebrow'>正在追蹤主線</Text>
                <Text className='story-route-panel__title'>{storyModeRouteContext.storylineName}</Text>
              </View>
              <Text className='story-route-panel__status'>線索開啟</Text>
            </View>
            <View className='story-route-panel__metaGrid'>
              <View className='story-route-panel__metaItem'>
                <Text className='story-route-panel__metaLabel'>目前章節</Text>
                <Text className='story-route-panel__metaValue'>{storyCurrentChapter?.title || '等待出發'}</Text>
              </View>
              <View className='story-route-panel__metaItem'>
                <Text className='story-route-panel__metaLabel'>下一站</Text>
                <Text className='story-route-panel__metaValue'>
                  {storyNextChapter?.locationName || storyNextChapter?.title || '已到達目前終點'}
                </Text>
              </View>
            </View>
            {!storyModeDestination ? (
              <Text className='story-route-panel__fallback'>
                這一站先依章節提示前往目的地，抵達附近後會解鎖更完整的現場內容。
              </Text>
            ) : null}
            <Text className='story-route-panel__sectionTitle'>故事路線</Text>
            <ScrollView className='story-route-strip' scrollX>
              {storyModeRouteContext.chapters.map((chapter) => (
                <View key={chapter.chapterId} className={`story-route-step story-route-step--${chapter.status}`}>
                  <Text className='story-route-step__order'>第 {chapter.chapterOrder} 章</Text>
                  <Text className='story-route-step__title'>{chapter.title}</Text>
                  <Text className='story-route-step__place'>{chapter.locationName || '故事地點'}</Text>
                </View>
              ))}
            </ScrollView>
            <View className='story-route-panel__actions'>
              <Button className='story-route-panel__primary' onClick={handleReturnToStory}>返回故事</Button>
              <Button className='story-route-panel__secondary' onClick={handleClearStoryRoute}>退出故事路線</Button>
            </View>
          </View>
        ) : null}

        <View className='map-card'>
          <View className='map-toolbar'>
            <View className='map-search'>
              <Input className='map-search__input' value={keyword} placeholder='搜尋當前城市或子地圖中的探索點' onInput={handleSearchInput} />
              <Button className='map-search__button' size='mini' onClick={handleRecenter}>回到我的位置</Button>
            </View>
            <View className='map-toolbar__row'>
              <Text className='map-toolbar__meta'>
                {currentRouteAreaName}
                {' · '}
                {searching ? '正在整理探索點...' : `${pois.length} 個探索點`}
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
            {shouldMountNativeMap ? (
              <NativeMap
                id='trip-map-canvas'
                className={`map-canvas ${fullscreen ? 'fullscreen' : ''}`}
                longitude={centerCoord.longitude}
                latitude={centerCoord.latitude}
                scale={sanitizeMapScale(scale)}
                showLocation={false}
                enableRotate={false}
                enableOverlooking={false}
                markers={mapMarkers.length ? mapMarkers : []}
                onError={(event) => {
                  console.warn('Native map failed to render.', event.detail)
                }}
                onMarkerTap={(event) => {
                  const markerId = toNativeMarkerId(event.detail.markerId, 0)
                  if (markerId !== 9999) {
                    handleSelectPoi(markerId)
                  }
                }}
                circles={mapCircles}
                polyline={storyPolyline}
              />
            ) : (
              <View className='map-canvas map-canvas--loading'>
                <Text className='map-canvas__loadingTitle'>正在展開探索地圖</Text>
                <Text className='map-canvas__loadingDesc'>故事路線與附近探索點載入後，會在這裡亮起。</Text>
              </View>
            )}

            {storyModeRouteContext && storyRoutePois.length ? (
              <View className='map-route-overlay'>
                <Text className='map-route-overlay__label'>主線追蹤中</Text>
                <Text className='map-route-overlay__title'>
                  {storyCurrentChapter?.locationName || storyCurrentChapter?.title || '前往目前章節地點'}
                </Text>
                <Text className='map-route-overlay__desc'>
                  先跟著亮起的線索前進，其他站點會在故事推進後逐步揭曉。
                </Text>
              </View>
            ) : null}

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
              <Text className='out-of-region-text'>目前距離這張探索地圖較遠，仍可瀏覽路線；現場打卡請以實際位置為準。</Text>
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
                <View
                  key={poi.id}
                  className={`poi-card ${selectedPoi?.id === poi.id ? 'poi-card--active' : ''} ${storyModeDestination?.id === poi.id ? 'poi-card--story-current' : ''}`}
                  onClick={() => handleSelectPoi(poi.id)}
                >
                  <View className='poi-card__cover' style={{ background: poi.coverColor }}>
                    <Text className='poi-card__emoji'>{poi.icon}</Text>
                  </View>
                  <View className='poi-card__body'>
                    <View className='poi-card__top'>
                      <Text className='poi-card__name'>{poi.name}</Text>
                      <Text className={`poi-card__badge ${poi.inRange ? 'ready' : ''}`}>
                        {storyModeDestination?.id === poi.id ? '目前故事目的地' : poi.inRange ? '可打卡' : poi.distanceText}
                      </Text>
                    </View>
                    <Text className='poi-card__subtitle'>{poi.subtitle}</Text>
                    <Text className='poi-card__meta'>
                      {poi.subMapName || poi.district}
                      {poi.inRange ? ' · 可立即打卡' : ' · 靠近後解鎖現場提示'}
                    </Text>
                  </View>
                </View>
              ))}
            </ScrollView>
          </View>

          <View className='explore-panel detail-panel'>
            <View className='panel-header'>
              <Text className='panel-title'>當前探索點</Text>
              <Text className='panel-subtitle'>查看現場簡介、前往路線與可觸發的故事內容。</Text>
            </View>
            {selectedPoi ? (
              <>
                <View className='selected-poi'>
                  <View className='selected-poi__hero' style={{ background: selectedPoi.coverColor }}>
                    <Text className='selected-poi__icon'>{selectedPoi.icon}</Text>
                    <View>
                      {storyModeDestination?.id === selectedPoi.id ? (
                        <Text className='selected-poi__storyBadge'>目前故事目的地</Text>
                      ) : null}
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
                      <Text className='selected-poi__statLabel'>探索區域</Text>
                      <Text className='selected-poi__statValue'>{selectedPoi.subMapName || selectedPoi.district || '城市探索'}</Text>
                    </View>
                    <View className='selected-poi__stat selected-poi__stat--full'>
                      <Text className='selected-poi__statLabel'>附近故事</Text>
                      <Text className='selected-poi__statValue'>{selectedPoiStoryTitle}</Text>
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
                    <Text className='story-card-lite__title'>靠近後會發生</Text>
                    <Text className='story-card-lite__name'>{arrivalExperience.rewardLabel}</Text>
                    <Text className='story-card-lite__progress'>{arrivalExperience.audioTitle}</Text>
                  </View>
                ) : null}

                <View className='action-bar'>
                  <Button className='action-bar__primary' onClick={() => void handlePrimaryPoiAction()}>
                    {selectedPoiActionText}
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

        {hasEmergencyContact ? (
          <View className='reward-banner' style={{ marginTop: 12 }}>
            <Text className='reward-banner__title'>緊急聯絡</Text>
            <Text className='reward-banner__desc'>{emergencyContact.name} · {emergencyContact.phone}</Text>
          </View>
        ) : null}
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
