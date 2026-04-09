import Taro from '@tarojs/taro'
import {
  AppUserProfile,
  ArrivalExperience,
  CheckinResult,
  DiscoverCardItem,
  PoiItem,
  RewardItem,
  StampItem,
  StorylineItem,
  TipArticleItem,
} from '../types/game'
import {
  amapConfig,
  mockArrivalExperiences,
  mockCheckinResults,
  mockDiscoverCards,
  mockPois,
  mockRewards,
  mockStamps,
  mockStorylines,
  mockTipArticles,
  mockUserProfile,
} from './gameMock'
import { calculateDistance, formatDistance, isWithinTriggerRange } from '../utils/location'

const STORAGE_KEY = 'trip-of-macau-game-state'
const EMERGENCY_CONTACT_KEY = 'trip-of-macau-emergency-contact'

export interface GameStateSnapshot {
  user: AppUserProfile
  collectedStampIds: number[]
  completedStoryIds: number[]
  activeStoryId?: number
  checkinHistory: Array<CheckinResult & { checkedAt: string }>
  redeemedRewardIds?: number[]
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
    completedStoryIds: [],
    activeStoryId: 1,
    checkinHistory: [],
    redeemedRewardIds: [],
  }
}

function saveState(state: GameStateSnapshot) {
  Taro.setStorageSync(STORAGE_KEY, state)
}

export function loadGameState(): GameStateSnapshot {
  const cached = Taro.getStorageSync(STORAGE_KEY)
  if (cached?.user) {
    return cached as GameStateSnapshot
  }

  const next = createDefaultState()
  saveState(next)
  return next
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

export function getStorylines(): StorylineItem[] {
  const state = loadGameState()
  return mockStorylines.map((storyline) => {
    const completedChapters = state.completedStoryIds.includes(storyline.id)
      ? storyline.totalChapters
      : storyline.completedChapters
    const progress = Math.min(100, Math.round((completedChapters / storyline.totalChapters) * 100))

    return {
      ...storyline,
      completedChapters,
      progress,
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

export function getNearbyPois(lat: number, lng: number, accuracy: number): NearbyPoiView[] {
  return mockPois
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
  return {
    amapKey: amapConfig.key,
    center: amapConfig.defaultCenter,
    city: amapConfig.defaultCity,
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
  return mockTipArticles
}

export function getDiscoverCards(): DiscoverCardItem[] {
  return mockDiscoverCards
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

  const next: GameStateSnapshot = {
    ...state,
    collectedStampIds: Array.from(stampIds),
    completedStoryIds: Array.from(completedStoryIds),
    user: {
      ...state.user,
      totalStamps: Array.from(stampIds).length,
      currentExp: state.user.currentExp + base.experienceGained,
      unlockedStorylines: Array.from(completedStoryIds).length,
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
    next.user.title = next.user.level >= 4 ? '澳門達人' : '澳門見習生'
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

  const profile = await Taro.getUserProfile({
    desc: '用于完善你的旅人名片与头像',
  })

  const loginRes = await Taro.login()
  const nextUser = {
    ...current.user,
    userId: loginRes.code || current.user.userId,
    openId: loginRes.code || current.user.openId || '',
    nickname: profile.userInfo.nickName || current.user.nickname,
    avatarUrl: profile.userInfo.avatarUrl || current.user.avatarUrl,
    isGuest: false,
  }

  const next = {
    ...current,
    user: nextUser,
  }

  saveState(next)
  return nextUser
}

