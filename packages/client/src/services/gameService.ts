import Taro from '@tarojs/taro'
import {
  AppUserProfile,
  ArrivalExperience,
  CheckinResult,
  CityProgressItem,
  DiscoverCardItem,
  NotificationItem,
  PoiItem,
  RewardItem,
  StampItem,
  StorylineItem,
  TipArticleItem,
  TravelAssessmentAnswer,
  TravelRecommendation,
} from '../types/game'
import {
  amapConfig,
  mockArrivalExperiences,
  mockCheckinResults,
  mockCities,
  mockDiscoverCards,
  mockNotifications,
  mockPois,
  mockRewards,
  mockStamps,
  mockStorylines,
  mockTipArticles,
  mockTravelRecommendations,
  mockUserProfile,
} from './gameMock'
import { calculateDistance, formatDistance, isWithinTriggerRange } from '../utils/location'

const STORAGE_KEY = 'trip-of-macau-game-state'
const EMERGENCY_CONTACT_KEY = 'trip-of-macau-emergency-contact'

export interface GameStateSnapshot {
  user: AppUserProfile
  collectedStampIds: number[]
  completedStoryIds: number[]
  completedChapterIds: number[]
  activeStoryId?: number
  checkinHistory: Array<CheckinResult & { checkedAt: string }>
  redeemedRewardIds?: number[]
  publishedTips?: TipArticleItem[]
  unreadNotificationIds?: number[]
  cityUnlocks?: Array<{ cityId: string; unlockedAt: string }>
  travelAssessment?: TravelAssessmentAnswer
  recommendation?: TravelRecommendation
}

export interface NearbyPoiView extends PoiItem {
  distanceMeters: number
  distanceText: string
  inRange: boolean
  dynamicRadius: number
}

function createDefaultState(): GameStateSnapshot {
  return {
    user: { ...mockUserProfile },
    collectedStampIds: mockStamps.filter((stamp) => stamp.collected).map((stamp) => stamp.id),
    completedStoryIds: [1, 2],
    completedChapterIds: [1011, 1012, 1021, 1022],
    activeStoryId: 1,
    checkinHistory: [],
    redeemedRewardIds: [],
    publishedTips: [],
    unreadNotificationIds: mockNotifications.filter((item) => item.unread).map((item) => item.id),
    cityUnlocks: mockCities.filter((item) => item.unlocked && item.firstUnlockedAt).map((item) => ({ cityId: item.id, unlockedAt: item.firstUnlockedAt || new Date().toISOString() })),
  }
}

function normalizeState(rawState: any): GameStateSnapshot {
  const defaults = createDefaultState()
  const rawUser = rawState?.user || {}
  const rawAssessment = rawState?.travelAssessment || null

  return {
    ...defaults,
    ...rawState,
    user: {
      ...defaults.user,
      ...rawUser,
      badges: Array.isArray(rawUser.badges) ? rawUser.badges : defaults.user.badges,
    },
    collectedStampIds: Array.isArray(rawState?.collectedStampIds) ? rawState.collectedStampIds : defaults.collectedStampIds,
    completedStoryIds: Array.isArray(rawState?.completedStoryIds) ? rawState.completedStoryIds : defaults.completedStoryIds,
    completedChapterIds: Array.isArray(rawState?.completedChapterIds) ? rawState.completedChapterIds : defaults.completedChapterIds,
    checkinHistory: Array.isArray(rawState?.checkinHistory) ? rawState.checkinHistory : defaults.checkinHistory,
    redeemedRewardIds: Array.isArray(rawState?.redeemedRewardIds) ? rawState.redeemedRewardIds : defaults.redeemedRewardIds,
    publishedTips: Array.isArray(rawState?.publishedTips) ? rawState.publishedTips : defaults.publishedTips,
    unreadNotificationIds: Array.isArray(rawState?.unreadNotificationIds) ? rawState.unreadNotificationIds : defaults.unreadNotificationIds,
    cityUnlocks: Array.isArray(rawState?.cityUnlocks) ? rawState.cityUnlocks : defaults.cityUnlocks,
    travelAssessment: rawAssessment ? {
      ageGroup: rawAssessment.ageGroup || '18-30歲',
      playDuration: rawAssessment.playDuration || '半天慢遊',
      interests: Array.isArray(rawAssessment.interests) ? rawAssessment.interests : ['歷史故事'],
      allowLocation: !!rawAssessment.allowLocation,
    } : undefined,
    recommendation: rawState?.recommendation || defaults.recommendation,
  }
}



function saveState(nextState: GameStateSnapshot) {
  const normalized = normalizeState(nextState)
  Taro.setStorageSync(STORAGE_KEY, normalized)
  Taro.setStorageSync('userInfo', normalized.user)
  return normalized
}

export function loadGameState(): GameStateSnapshot {
  try {
    const stored = Taro.getStorageSync(STORAGE_KEY)
    if (!stored) {
      return saveState(createDefaultState())
    }
    const normalized = normalizeState(stored)
    Taro.setStorageSync(STORAGE_KEY, normalized)
    return normalized
  } catch (error) {
    console.warn('读取旅程状态失败，已回退到默认状态', error)
    return saveState(createDefaultState())
  }
}

export function updateUserPreference(patch: Partial<AppUserProfile>) {

  const current = loadGameState()
  const next = {
    ...current,
    user: {
      ...current.user,
      ...patch,
    },
  }
  saveState(next)
  wx.setStorageSync('interfaceMode', next.user.interfaceMode)
  return next.user
}

function inferCityByLocation(lat: number, lng: number) {
  if (lat > 22.17 && lat < 22.22 && lng > 113.52 && lng < 113.55) return 'macau'
  if (lat > 22.14 && lat < 22.17 && lng > 113.55 && lng < 113.58) return 'taipa'
  if (lat > 22.1 && lat < 22.14 && lng > 113.56 && lng < 113.59) return 'coloane'
  return 'macau'
}

export function registerCityVisitByLocation(lat: number, lng: number) {
  const state = loadGameState()
  const cityId = inferCityByLocation(lat, lng)
  const exists = (state.cityUnlocks || []).find((item) => item.cityId === cityId)
  const unlockAt = exists ? exists.unlockedAt : new Date().toISOString()
  const nextUnlocks = exists ? (state.cityUnlocks || []) : [...(state.cityUnlocks || []), { cityId, unlockedAt: unlockAt }]
  const city = mockCities.find((item) => item.id === cityId)

  const next = {
    ...state,
    cityUnlocks: nextUnlocks,
    user: {
      ...state.user,
      currentCityId: cityId,
      badges: city && !state.user.badges.includes(city.titleReward) && cityId !== 'macau'
        ? [...state.user.badges, city.titleReward]
        : state.user.badges,
    },
  }
  saveState(next)
  return cityId
}

export function getCities(): CityProgressItem[] {
  const state = loadGameState()
  return mockCities.map((city) => {
    const unlockRecord = (state.cityUnlocks || []).find((item) => item.cityId === city.id)
    const poiCount = mockPois.filter((poi) => poi.cityId === city.id).length
    const checkedPoiCount = state.checkinHistory.filter((item) => {
      const poi = mockPois.find((target) => target.id === item.poiId)
      return poi?.cityId === city.id
    }).length
    return {
      ...city,
      unlocked: !!unlockRecord || city.unlocked,
      firstUnlockedAt: unlockRecord?.unlockedAt || city.firstUnlockedAt,
      explorationProgress: poiCount ? Math.max(city.explorationProgress, Math.round((checkedPoiCount / poiCount) * 100)) : city.explorationProgress,
    }
  })
}

export function switchCurrentCity(cityId: string) {
  const state = loadGameState()
  const city = getCities().find((item) => item.id === cityId)
  if (!city?.unlocked) {
    throw new Error('先前往該城市範圍，即可點亮新的旅程')
  }
  const next = {
    ...state,
    user: {
      ...state.user,
      currentCityId: cityId,
    },
  }
  saveState(next)
  return city
}

export function saveTravelAssessment(answer: TravelAssessmentAnswer, userLocation?: { latitude: number; longitude: number }) {
  const state = loadGameState()
  if (userLocation) {
    registerCityVisitByLocation(userLocation.latitude, userLocation.longitude)
  }
  const recommendation = getTravelRecommendation(answer)
  const next = {
    ...loadGameState(),
    travelAssessment: answer,
    recommendation,
  }
  saveState(next)
  return recommendation
}

export function getTravelRecommendation(answer?: TravelAssessmentAnswer | null) {
  const target = answer || loadGameState().travelAssessment
  if (!target) return mockTravelRecommendations[0]
  if (target.interests.includes('拍照') || target.interests.includes('美食')) return mockTravelRecommendations[2]
  if (target.ageGroup.includes('55') || target.playDuration.includes('半天')) return mockTravelRecommendations[0]
  return mockTravelRecommendations[1]
}

export function getStorylines(): StorylineItem[] {
  const state = loadGameState()
  const unlockedCities = getCities().filter((item) => item.unlocked).map((item) => item.id)
  return mockStorylines.map((storyline) => {
    const firstPoi = mockPois.find((poi) => poi.storyLineId === storyline.id)
    const cityUnlocked = firstPoi?.cityId ? unlockedCities.includes(firstPoi.cityId) : true
    const storyUnlocked = storyline.id <= 2 || cityUnlocked || state.completedStoryIds.includes(storyline.id)
    const completedChapters = (storyline.chapters || []).filter((chapter) => state.completedChapterIds.includes(chapter.id)).length
    const totalChapters = storyline.chapters?.length || storyline.totalChapters
    return {
      ...storyline,
      locked: !storyUnlocked,
      unlockHint: !storyUnlocked ? (storyline.unlockHint || '继续探索後解鎖') : '',
      completedChapters,
      totalChapters,
      progress: Math.round((completedChapters / totalChapters) * 100),
      chapters: (storyline.chapters || []).map((chapter, index) => ({
        ...chapter,
        locked: !storyUnlocked || index > completedChapters,
      })),
    }
  })
}

export function getStamps(): StampItem[] {
  const state = loadGameState()
  return mockStamps.map((stamp) => ({
    ...stamp,
    collected: state.collectedStampIds.includes(stamp.id),
  }))
}

export function getRewards(): RewardItem[] {
  const state = loadGameState()
  return mockRewards.map((reward) => ({
    ...reward,
    status: state.redeemedRewardIds?.includes(reward.id)
      ? 'redeemed'
      : state.user.totalStamps >= reward.stampCost
        ? reward.status
        : reward.status === 'redeemed'
          ? 'redeemed'
          : 'coming_soon',
  }))
}

export function redeemReward(rewardId: number) {
  const state = loadGameState()
  const reward = mockRewards.find((item) => item.id === rewardId)
  if (!reward) {
    throw new Error('该奖励暂不可用')
  }
  if (state.user.totalStamps < reward.stampCost) {
    throw new Error('印章数量不足')
  }

  const next = {
    ...state,
    user: {
      ...state.user,
      totalStamps: Math.max(0, state.user.totalStamps - reward.stampCost),
    },
    redeemedRewardIds: Array.from(new Set([...(state.redeemedRewardIds || []), rewardId])),
  }
  saveState(next)
  return reward
}

export function getNearbyPois(lat: number, lng: number, accuracy: number, cityId?: string): NearbyPoiView[] {
  const currentCityId = cityId || loadGameState().user.currentCityId || 'macau'
  return mockPois
    .filter((poi) => !cityId || poi.cityId === currentCityId)
    .map((poi) => {
      const evaluation = isWithinTriggerRange(lat, lng, poi.gcj02Latitude, poi.gcj02Longitude, poi.triggerRadius, accuracy)
      const distanceMeters = calculateDistance(lat, lng, poi.gcj02Latitude, poi.gcj02Longitude)
      return {
        ...poi,
        distanceMeters,
        distanceText: formatDistance(distanceMeters),
        inRange: evaluation.isInRange,
        dynamicRadius: Math.round(evaluation.dynamicRadius),
      }
    })
    .sort((a, b) => a.distanceMeters - b.distanceMeters)
}

export function getMapBootstrapConfig() {
  const state = loadGameState()
  const currentCity = getCities().find((city) => city.id === state.user.currentCityId) || mockCities[0]
  return {
    amapKey: amapConfig.key,
    center: amapConfig.defaultCenter,
    city: currentCity.name,
    cityCode: amapConfig.defaultCityCode,
    checkinRules: {
      gpsIntervals: '2秒',
      cooldownMinutes: 30,
      debounceSeconds: 2,
      radiusPolicy: '30/50/80米动态调整',
      manualFallback: '200米手动补签',
    },
  }
}

export function getArrivalExperience(poiId: number): ArrivalExperience | null {
  return mockArrivalExperiences[poiId] || null
}

export function getTipArticles(): TipArticleItem[] {
  const state = loadGameState()
  return [...(state.publishedTips || []), ...mockTipArticles]
}

export function publishTipPost(payload: {
  title: string
  summary: string
  category: string
  locationName: string
  imageUrl?: string
  contentParagraphs: string[]
}) {
  const state = loadGameState()
  const article: TipArticleItem = {
    id: Date.now(),
    title: payload.title,
    summary: payload.summary,
    category: payload.category,
    author: state.user.nickname || '未命名旅人',
    likes: 0,
    saves: 0,
    readMinutes: Math.max(3, payload.contentParagraphs.length * 2),
    tags: [payload.locationName, payload.category],
    coverColor: '#ffeef6',
    imageUrl: payload.imageUrl,
    locationName: payload.locationName,
    contentParagraphs: payload.contentParagraphs,
    createdAt: new Date().toISOString(),
    isPublishedByUser: true,
  }
  const next = {
    ...state,
    publishedTips: [article, ...(state.publishedTips || [])],
    unreadNotificationIds: Array.from(new Set([...(state.unreadNotificationIds || []), 2])),
  }
  saveState(next)
  return article
}

export function getNotifications(): NotificationItem[] {
  const state = loadGameState()
  return mockNotifications.map((item) => ({
    ...item,
    unread: (state.unreadNotificationIds || []).includes(item.id),
  }))
}

export function markNotificationsRead() {
  const state = loadGameState()
  const next = {
    ...state,
    unreadNotificationIds: [],
  }
  saveState(next)
}

export function getUnreadNotificationCount() {
  return (loadGameState().unreadNotificationIds || []).length
}


export function getDiscoverCards(): DiscoverCardItem[] {
  return mockDiscoverCards
}

export function getTipArticleById(id: number) {
  return getTipArticles().find((article) => article.id === id)
}



export function getPoiSearchTips(keyword: string, currentCityId?: string) {
  const cityId = currentCityId || loadGameState().user.currentCityId || 'macau'
  return mockPois
    .filter((poi) => poi.cityId === cityId)
    .filter((poi) => !keyword || poi.name.includes(keyword) || poi.address.includes(keyword) || poi.tags.some((tag) => tag.includes(keyword)))
    .slice(0, 6)
    .map((poi) => ({
      id: String(poi.id),
      name: poi.name,
      address: poi.address,
      location: `${poi.gcj02Longitude},${poi.gcj02Latitude}`,
      district: poi.district,
    }))
}

export function getWalkingRouteSummary(poi: PoiItem, location: { latitude: number; longitude: number }) {
  const distance = calculateDistance(location.latitude, location.longitude, poi.gcj02Latitude, poi.gcj02Longitude)
  const minutes = Math.max(3, Math.ceil(distance / 65))
  return {
    distance: String(Math.round(distance)),
    duration: String(minutes * 60),
    steps: [
      `從目前位置朝 ${poi.district} 方向慢慢前進`,
      `看到 ${poi.name} 附近地標後放慢腳步`,
      `在景點周邊停留約 ${poi.staySeconds} 秒，等故事與印章亮起`,
    ],
  }
}

export function performMockCheckin(poiId: number, triggerMode: 'gps' | 'manual' | 'mock' = 'mock'): CheckinResult {
  const state = loadGameState()
  const base = mockCheckinResults[poiId]
  const poi = mockPois.find((item) => item.id === poiId)

  if (!base || !poi) {
    throw new Error('該地點暫時不可打卡')
  }

  const stampIds = new Set(state.collectedStampIds)
  stampIds.add(base.stampId)

  const completedStoryIds = new Set(state.completedStoryIds)
  if (base.unlockedStorylineId) {
    completedStoryIds.add(base.unlockedStorylineId)
  }

  const relatedStory = mockStorylines.find((story) => story.id === poi.storyLineId)
  const completedChapterIds = new Set(state.completedChapterIds)
  if (relatedStory?.chapters?.length) {
    const nextUnlockedChapter = relatedStory.chapters.find((chapter) => !completedChapterIds.has(chapter.id))
    if (nextUnlockedChapter) {
      completedChapterIds.add(nextUnlockedChapter.id)
    }
  }

  const cityId = poi.cityId || 'macau'
  const hasCityUnlock = (state.cityUnlocks || []).some((item) => item.cityId === cityId)
  const nextUnlocks = hasCityUnlock
    ? (state.cityUnlocks || [])
    : [...(state.cityUnlocks || []), { cityId, unlockedAt: new Date().toISOString() }]

  const next: GameStateSnapshot = {
    ...state,
    collectedStampIds: Array.from(stampIds),
    completedStoryIds: Array.from(completedStoryIds),
    completedChapterIds: Array.from(completedChapterIds),
    cityUnlocks: nextUnlocks,
    user: {
      ...state.user,
      totalStamps: Array.from(stampIds).length,
      currentExp: state.user.currentExp + base.experienceGained,
      unlockedStorylines: Array.from(completedStoryIds).length,
      currentCityId: cityId,
    },
    checkinHistory: [
      {
        ...base,
        triggerMode,
        checkedAt: new Date().toISOString(),
      },
      ...state.checkinHistory,
    ].slice(0, 20),
  }

  if (next.user.currentExp >= next.user.nextLevelExp) {
    next.user.level += 1
    next.user.currentExp = next.user.currentExp - next.user.nextLevelExp
    next.user.nextLevelExp += 120
    next.user.title = next.user.level >= 5 ? '城市傳說旅人' : next.user.level >= 4 ? '澳門達人' : '澳門見習生'
  }

  saveState(next)

  return {
    ...base,
    triggerMode,
  }
}

export function getPoiById(id: number) {
  return mockPois.find((poi) => poi.id === id)
}

export function getStoryById(id: number) {
  return getStorylines().find((story) => story.id === id)
}

export function getCheckinHistory() {
  return loadGameState().checkinHistory
}

export function getEmergencyContact() {
  return Taro.getStorageSync(EMERGENCY_CONTACT_KEY) || {
    name: '旅伴或家人',
    phone: '10086',
  }
}

export function updateEmergencyContact(contact: { name: string; phone: string }) {
  Taro.setStorageSync(EMERGENCY_CONTACT_KEY, contact)
  return contact
}

export async function loginWithWeChatProfile() {
  const current = loadGameState()
  const deviceInfo = wx.getDeviceInfo ? wx.getDeviceInfo() : null
  const isDevtools = deviceInfo?.platform === 'devtools'

  let loginCode = ''
  try {
    const loginRes = await Taro.login()
    loginCode = loginRes?.code || ''
  } catch (error) {
    console.warn('微信登入 code 获取失败', error)
  }

  let profileUserInfo: WechatMiniprogram.UserInfo | null = null
  try {
    const profile = await Taro.getUserProfile({
      desc: '用于完善你的旅人名片与头像',
    })
    profileUserInfo = profile.userInfo || null
  } catch (error) {
    console.warn('微信头像昵称授权未完成，尝试使用现有旅人名片继续登入', error)
  }

  if (!loginCode && !profileUserInfo && !isDevtools) {
    throw new Error('微信授權未完成')
  }

  const fallbackName = current.user.nickname && current.user.nickname !== '未命名旅人' ? current.user.nickname : '微信旅人'
  const fallbackOpenId = current.user.openId || `devtools-openid-${Date.now()}`
  const fallbackUserId = current.user.userId || `devtools-user-${Date.now()}`

  const nextUser = {
    ...current.user,
    userId: loginCode || fallbackUserId,
    openId: loginCode || fallbackOpenId,
    nickname: profileUserInfo?.nickName || fallbackName,
    avatarUrl: profileUserInfo?.avatarUrl || current.user.avatarUrl,
    isGuest: false,
  }

  const next = {
    ...current,
    user: nextUser,
  }

  saveState(next)
  return nextUser
}

