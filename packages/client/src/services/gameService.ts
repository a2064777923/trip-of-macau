import Taro from '@tarojs/taro'
import {
  AppUserProfile,
  AuthStatus,
  ArrivalExperience,
  CheckinResult,
  CityProgressItem,
  DiscoverCardItem,
  NotificationItem,
  PoiItem,
  RewardItem,
  StampItem,
  StorylineItem,
  SubMapProgressItem,
  TipArticleItem,
  TravelAssessmentAnswer,
  TravelRecommendation,
} from '../types/game'
import {
  api,
  DEFAULT_PUBLIC_LOCALE,
  PublicCityDto,
  PublicDiscoverCardDto,
  PublicNotificationDto,
  PublicPoiDto,
  PublicRewardDto,
  PublicRuntimeGroupDto,
  PublicStampDto,
  PublicStoryChapterDto,
  PublicStorylineDto,
  PublicSubMapDto,
  PublicTipArticleDto,
  PublicLocaleCode,
  PublicUserPreferencesDto,
  PublicUserSessionDto,
  PublicUserStateDto,
} from './api'
import { USE_MOCK, WECHAT_DEV_BYPASS_ENABLED } from '../constants/env'
import { calculateDistance, formatDistance, isWithinTriggerRange } from '../utils/location'

const STORAGE_KEY = 'trip-of-macau-game-state'
const TOKEN_KEY = 'token'
const EMERGENCY_CONTACT_KEY = 'trip-of-macau-emergency-contact'
const PUBLIC_CONTENT_KEY = 'trip-of-macau-public-content'
const DEV_BYPASS_IDENTITY_KEY = 'trip-of-macau-dev-bypass-identity'
const PROFILE_AUTH_WALL_PATH = '/pages/profile/index'

const DEFAULT_UNLOCKED_CITY_ID = 'macau'
const DEFAULT_COLLECTED_STAMP_IDS = [101, 102, 103, 104]
const DEFAULT_COMPLETED_STORY_IDS = [1, 2]
const DEFAULT_COMPLETED_CHAPTER_IDS = [1011, 1012, 1021, 1022]
const DEFAULT_UNREAD_NOTIFICATION_IDS = [1, 2]
const DEFAULT_COLOR_PALETTE = ['#ffd9e5', '#dff7ef', '#dfeaff', '#fff0c8', '#e9defc', '#dff3ff']
const AMAP_CONFIG = {
  key: '6fea5cb20fa631562465356be078d086',
  defaultCenter: {
    latitude: 22.1987,
    longitude: 113.5439,
  },
}

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

interface PublicContentCache {
  locale: string
  updatedAt: string
  cities: PublicCityDto[]
  subMaps: PublicSubMapDto[]
  pois: PublicPoiDto[]
  storylines: PublicStorylineDto[]
  tips: PublicTipArticleDto[]
  rewards: PublicRewardDto[]
  stamps: PublicStampDto[]
  notifications: PublicNotificationDto[]
  discoverCards: PublicDiscoverCardDto[]
  runtimeGroups: Record<string, PublicRuntimeGroupDto>
}

let publicContentCache: PublicContentCache | null = null
let authPromptVisible = false

export class AuthRequiredError extends Error {
  constructor(message = '此功能需要先使用微信登入。') {
    super(message)
    this.name = 'AuthRequiredError'
  }
}

function createDefaultState(): GameStateSnapshot {
  const now = new Date().toISOString()

  return {
    user: {
      userId: '',
      nickname: 'Traveler',
      avatarUrl: '',
      authStatus: 'anonymous',
      localeCode: DEFAULT_PUBLIC_LOCALE,
      openId: '',
      level: 1,
      title: 'Traveler',
      totalStamps: 0,
      currentExp: 0,
      nextLevelExp: 120,
      interfaceMode: 'standard',
      fontScale: 1,
      highContrast: false,
      voiceGuideEnabled: true,
      unlockedStorylines: 0,
      badges: [],
      currentCityId: DEFAULT_UNLOCKED_CITY_ID,
      currentSubMapId: 'macau-peninsula',
    },
    collectedStampIds: [],
    completedStoryIds: [],
    completedChapterIds: [],
    activeStoryId: undefined,
    checkinHistory: [],
    redeemedRewardIds: [],
    publishedTips: [],
    unreadNotificationIds: [],
    cityUnlocks: [{ cityId: DEFAULT_UNLOCKED_CITY_ID, unlockedAt: now }],
  }
}

function createEmptyPublicContent(): PublicContentCache {
  return {
    locale: DEFAULT_PUBLIC_LOCALE,
    updatedAt: '',
    cities: [],
    subMaps: [],
    pois: [],
    storylines: [],
    tips: [],
    rewards: [],
    stamps: [],
    notifications: [],
    discoverCards: [],
    runtimeGroups: {},
  }
}

function hasText(value?: string | null): value is string {
  return typeof value === 'string' && value.trim().length > 0
}

function isAuthStatus(value: unknown): value is AuthStatus {
  return value === 'anonymous' || value === 'authenticated' || value === 'dev-bypass'
}

function getStoredToken() {
  return Taro.getStorageSync(TOKEN_KEY)
}

export function hasActiveSessionToken() {
  return hasText(getStoredToken())
}

function isTabbarPage(target: string) {
  return [
    '/pages/index/index',
    '/pages/map/index',
    '/pages/discover/index',
    '/pages/tips/index',
    '/pages/profile/index',
  ].includes(target)
}

function getAnonymousState() {
  return saveState(createDefaultState())
}

function clearAuthenticatedSession() {
  Taro.removeStorageSync(TOKEN_KEY)
  return getAnonymousState()
}

function assertAuthenticatedSnapshot(snapshot: GameStateSnapshot, message = '此功能需要先使用微信登入。') {
  if (snapshot.user.authStatus === 'anonymous' || !hasActiveSessionToken()) {
    throw new AuthRequiredError(message)
  }
}

function detectDevtoolsEnvironment() {
  try {
    const deviceInfo = wx.getDeviceInfo ? wx.getDeviceInfo() : null
    return deviceInfo?.platform === 'devtools'
  } catch (error) {
    console.warn('Failed to detect devtools environment.', error)
    return false
  }
}

export function isDevBypassAvailable() {
  return !USE_MOCK && WECHAT_DEV_BYPASS_ENABLED && detectDevtoolsEnvironment()
}

export function isAuthRequiredError(error: unknown) {
  return error instanceof AuthRequiredError || (error instanceof Error && error.name === 'AuthRequiredError')
}

async function openAuthTarget(target: string) {
  if (isTabbarPage(target)) {
    await Taro.switchTab({ url: target })
    return
  }
  await Taro.navigateTo({ url: target })
}

export async function requireAuth(reason: string, target = PROFILE_AUTH_WALL_PATH) {
  if (USE_MOCK) {
    return true
  }
  if (hasActiveSessionToken() && loadGameState().user.authStatus !== 'anonymous') {
    return true
  }
  if (authPromptVisible) {
    return false
  }

  authPromptVisible = true
  try {
    const result = await Taro.showModal({
      title: '需要登入',
      content: reason,
      confirmText: '去登入',
      cancelText: '稍後',
    })
    if (result.confirm) {
      await openAuthTarget(target)
    }
  } finally {
    authPromptVisible = false
  }

  return false
}

function normalizeState(rawState: any): GameStateSnapshot {
  const defaults = createDefaultState()
  const rawUser = rawState?.user || {}
  const rawAssessment = rawState?.travelAssessment || null
  const legacyCurrentCityId = rawUser.currentCityId || defaults.user.currentCityId
  const currentCityId = legacyCurrentCityId === 'taipa' || legacyCurrentCityId === 'coloane'
    ? DEFAULT_UNLOCKED_CITY_ID
    : legacyCurrentCityId
  const currentSubMapId = rawUser.currentSubMapId
    || (legacyCurrentCityId === 'taipa' || legacyCurrentCityId === 'coloane' ? legacyCurrentCityId : defaults.user.currentSubMapId)
  const derivedAuthStatus = isAuthStatus(rawUser.authStatus)
    ? rawUser.authStatus
    : rawUser.isGuest === false
      ? 'authenticated'
      : 'anonymous'

  return {
    ...defaults,
    ...rawState,
    user: {
      ...defaults.user,
      ...rawUser,
      authStatus: derivedAuthStatus,
      badges: Array.isArray(rawUser.badges) ? rawUser.badges : defaults.user.badges,
      currentCityId,
      currentSubMapId,
    },
    collectedStampIds: Array.isArray(rawState?.collectedStampIds) ? rawState.collectedStampIds : defaults.collectedStampIds,
    completedStoryIds: Array.isArray(rawState?.completedStoryIds) ? rawState.completedStoryIds : defaults.completedStoryIds,
    completedChapterIds: Array.isArray(rawState?.completedChapterIds) ? rawState.completedChapterIds : defaults.completedChapterIds,
    checkinHistory: Array.isArray(rawState?.checkinHistory) ? rawState.checkinHistory : defaults.checkinHistory,
    redeemedRewardIds: Array.isArray(rawState?.redeemedRewardIds) ? rawState.redeemedRewardIds : defaults.redeemedRewardIds,
    publishedTips: Array.isArray(rawState?.publishedTips) ? rawState.publishedTips : defaults.publishedTips,
    unreadNotificationIds: Array.isArray(rawState?.unreadNotificationIds) ? rawState.unreadNotificationIds : defaults.unreadNotificationIds,
    cityUnlocks: Array.isArray(rawState?.cityUnlocks) && rawState.cityUnlocks.length
      ? rawState.cityUnlocks
      : defaults.cityUnlocks,
    travelAssessment: rawAssessment
      ? {
          ageGroup: rawAssessment.ageGroup || '18-30',
          playDuration: rawAssessment.playDuration || 'Half day',
          interests: Array.isArray(rawAssessment.interests) ? rawAssessment.interests : ['History'],
          allowLocation: !!rawAssessment.allowLocation,
        }
      : undefined,
    recommendation: rawState?.recommendation || defaults.recommendation,
  }
}

function normalizePublicContent(raw: any): PublicContentCache {
  const defaults = createEmptyPublicContent()
  return {
    ...defaults,
    ...raw,
    locale: raw?.locale || defaults.locale,
    updatedAt: raw?.updatedAt || defaults.updatedAt,
    cities: Array.isArray(raw?.cities) ? raw.cities : defaults.cities,
    subMaps: Array.isArray(raw?.subMaps) ? raw.subMaps : defaults.subMaps,
    pois: Array.isArray(raw?.pois) ? raw.pois : defaults.pois,
    storylines: Array.isArray(raw?.storylines) ? raw.storylines : defaults.storylines,
    tips: Array.isArray(raw?.tips) ? raw.tips : defaults.tips,
    rewards: Array.isArray(raw?.rewards) ? raw.rewards : defaults.rewards,
    stamps: Array.isArray(raw?.stamps) ? raw.stamps : defaults.stamps,
    notifications: Array.isArray(raw?.notifications) ? raw.notifications : defaults.notifications,
    discoverCards: Array.isArray(raw?.discoverCards) ? raw.discoverCards : defaults.discoverCards,
    runtimeGroups: raw?.runtimeGroups && typeof raw.runtimeGroups === 'object' ? raw.runtimeGroups : defaults.runtimeGroups,
  }
}

function saveState(nextState: GameStateSnapshot) {
  const normalized = normalizeState(nextState)
  Taro.setStorageSync(STORAGE_KEY, normalized)
  Taro.setStorageSync('userInfo', normalized.user)
  return normalized
}

function loadPublicContent(): PublicContentCache {
  if (publicContentCache) {
    return publicContentCache
  }

  try {
    const stored = Taro.getStorageSync(PUBLIC_CONTENT_KEY)
    publicContentCache = normalizePublicContent(stored)
  } catch (error) {
    console.warn('Failed to read public content cache.', error)
    publicContentCache = createEmptyPublicContent()
  }

  return publicContentCache
}

function savePublicContent(next: PublicContentCache) {
  const normalized = normalizePublicContent(next)
  publicContentCache = normalized
  Taro.setStorageSync(PUBLIC_CONTENT_KEY, normalized)
  return normalized
}

function buildUserPreferencesPayload(user: AppUserProfile, extra?: Partial<PublicUserPreferencesDto> & {
  emergencyContactName?: string
  emergencyContactPhone?: string
}) {
  return {
    localeCode: user.localeCode || DEFAULT_PUBLIC_LOCALE,
    interfaceMode: user.interfaceMode,
    fontScale: Number(user.fontScale || 1),
    highContrast: !!user.highContrast,
    voiceGuideEnabled: !!user.voiceGuideEnabled,
    seniorMode: user.interfaceMode === 'elderly',
    ...extra,
  }
}

function applyRemoteUserState(remote: PublicUserStateDto, current = loadGameState(), authStatus: AuthStatus = 'authenticated') {
  const mergedUnlocks = Array.from(new Set([...(remote.progress.unlockedCityCodes || []), current.user.currentCityId || DEFAULT_UNLOCKED_CITY_ID]))
    .filter(Boolean)
    .map((cityId) => {
      const existing = (current.cityUnlocks || []).find((item) => item.cityId === cityId)
      return existing || { cityId, unlockedAt: new Date().toISOString() }
    })
  const currentCityCode = remote.profile.currentCityCode || current.user.currentCityId || DEFAULT_UNLOCKED_CITY_ID
  const currentSubMapCode = current.user.currentSubMapId
    && getCitySubMapDtos(currentCityCode).some((subMap) => subMap.code === current.user.currentSubMapId)
    ? current.user.currentSubMapId
    : getCitySubMapDtos(currentCityCode)[0]?.code

  const nextUser: AppUserProfile = {
    ...current.user,
    userId: String(remote.profile.id),
    authStatus,
    openId: remote.profile.openId || current.user.openId,
    nickname: remote.profile.nickname || current.user.nickname,
    avatarUrl: remote.profile.avatarUrl || current.user.avatarUrl,
    level: remote.profile.level,
    title: remote.profile.title,
    totalStamps: remote.profile.totalStamps,
    currentExp: remote.profile.currentExp,
    nextLevelExp: remote.profile.nextLevelExp,
    localeCode: remote.preferences.localeCode || remote.profile.currentLocaleCode || current.user.localeCode || DEFAULT_PUBLIC_LOCALE,
    interfaceMode: remote.preferences.interfaceMode || current.user.interfaceMode,
    fontScale: Number(remote.preferences.fontScale ?? current.user.fontScale ?? 1),
    highContrast: !!remote.preferences.highContrast,
    voiceGuideEnabled: !!remote.preferences.voiceGuideEnabled,
    unlockedStorylines: Array.isArray(remote.progress.completedStoryIds) ? remote.progress.completedStoryIds.length : current.user.unlockedStorylines,
    currentCityId: currentCityCode,
    currentSubMapId: currentSubMapCode,
  }

  const nextState = saveState({
    ...current,
    user: nextUser,
    collectedStampIds: Array.isArray(remote.progress.collectedStampIds) ? remote.progress.collectedStampIds : current.collectedStampIds,
    completedStoryIds: Array.isArray(remote.progress.completedStoryIds) ? remote.progress.completedStoryIds : current.completedStoryIds,
    completedChapterIds: Array.isArray(remote.progress.completedChapterIds) ? remote.progress.completedChapterIds : current.completedChapterIds,
    activeStoryId: remote.progress.activeStoryId || current.activeStoryId,
    checkinHistory: Array.isArray(remote.progress.checkinHistory)
      ? remote.progress.checkinHistory.map((item) => ({
          success: true,
          poiId: item.poiId,
          poiName: item.poiName,
          stampId: item.stampId || 0,
          stampName: item.stampName || '',
          experienceGained: item.experienceGained || 0,
          triggerMode: item.triggerMode,
          unlockedStorylineId: item.unlockedStorylineId,
          checkedAt: item.checkedAt,
        }))
      : current.checkinHistory,
    redeemedRewardIds: Array.isArray(remote.progress.redeemedRewardIds)
      ? remote.progress.redeemedRewardIds
      : (remote.rewardRedemptions || []).map((item) => item.rewardId),
    cityUnlocks: mergedUnlocks,
  })

  const emergencyContact = {
    name: remote.preferences.emergencyContactName || 'Emergency contact',
    phone: remote.preferences.emergencyContactPhone || '10086',
  }
  Taro.setStorageSync(EMERGENCY_CONTACT_KEY, emergencyContact)
  wx.setStorageSync('interfaceMode', nextUser.interfaceMode)
  return nextState
}

async function requestWeChatLoginCode() {
  const loginRes = await Taro.login()
  const code = loginRes?.code || ''
  if (!hasText(code)) {
    throw new Error('未能取得微信登入憑證。')
  }
  return code
}

export async function loginWithWechat() {
  if (USE_MOCK) {
    return loadGameState().user
  }

  const current = loadGameState()
  const session: PublicUserSessionDto = await api.user.loginWithWechat({
    code: await requestWeChatLoginCode(),
    localeCode: (current.user.localeCode as PublicLocaleCode) || DEFAULT_PUBLIC_LOCALE,
    interfaceMode: current.user.interfaceMode,
  })
  Taro.setStorageSync(TOKEN_KEY, session.accessToken)
  return applyRemoteUserState(session.state, current, 'authenticated').user
}

export async function enrichProfileWithWeChatProfile() {
  if (!(await requireAuth('同步微信頭像與暱稱前，請先完成登入。'))) {
    throw new AuthRequiredError()
  }

  const profile = await Taro.getUserProfile({
    desc: '用於同步你的旅人頭像與暱稱',
  })
  const current = loadGameState()
  const session: PublicUserSessionDto = await api.user.loginWithWechat({
    code: await requestWeChatLoginCode(),
    nickname: profile.userInfo?.nickName || current.user.nickname,
    avatarUrl: profile.userInfo?.avatarUrl || current.user.avatarUrl,
    localeCode: (current.user.localeCode as PublicLocaleCode) || DEFAULT_PUBLIC_LOCALE,
    interfaceMode: current.user.interfaceMode,
  })
  Taro.setStorageSync(TOKEN_KEY, session.accessToken)
  return applyRemoteUserState(session.state, current, 'authenticated').user
}

export async function loginWithDevBypass() {
  if (!isDevBypassAvailable()) {
    throw new Error('本地調試登入未開啟。')
  }

  const current = loadGameState()
  const devIdentity = Taro.getStorageSync(DEV_BYPASS_IDENTITY_KEY) || 'devtools-local-user'
  Taro.setStorageSync(DEV_BYPASS_IDENTITY_KEY, devIdentity)
  const session: PublicUserSessionDto = await api.user.loginWithDevBypass({
    devIdentity,
    nickname: current.user.nickname || 'Local Developer',
    avatarUrl: current.user.avatarUrl,
    localeCode: (current.user.localeCode as PublicLocaleCode) || DEFAULT_PUBLIC_LOCALE,
    interfaceMode: current.user.interfaceMode,
  })
  Taro.setStorageSync(TOKEN_KEY, session.accessToken)
  return applyRemoteUserState(session.state, current, 'dev-bypass').user
}

export async function syncUserStateFromServer() {
  if (USE_MOCK) {
    return loadGameState()
  }

  const token = Taro.getStorageSync('token')
  if (!hasText(token)) {
    return loadGameState()
  }

  try {
    const remote = await api.user.getUserState()
    return applyRemoteUserState(remote)
  } catch (error) {
    console.warn('Failed to sync remote user state, resetting local session.', error)
    return clearAuthenticatedSession()
  }
}

function looksMojibake(value?: string | null) {
  if (!hasText(value)) {
    return false
  }
  return /(æ|å|ä|é|è|ç|ï¼|ã|�|馃|鉁)/.test(value)
}

function pickReadableText(...values: Array<string | undefined | null>) {
  const readable = values.find((value) => hasText(value) && !looksMojibake(value))
  if (readable) {
    return readable.trim()
  }
  const fallback = values.find((value) => hasText(value))
  return fallback ? fallback.trim() : ''
}

function humanizeCode(value?: string | null) {
  if (!hasText(value)) {
    return ''
  }
  return value
    .replace(/[_-]+/g, ' ')
    .replace(/\s+/g, ' ')
    .trim()
    .replace(/\b\w/g, (character) => character.toUpperCase())
}

function colorFromKey(key: string, palette = DEFAULT_COLOR_PALETTE) {
  if (!hasText(key)) {
    return palette[0]
  }
  const hash = Array.from(key).reduce((total, character) => total + character.charCodeAt(0), 0)
  return palette[hash % palette.length]
}

function formatMinutes(minutes?: number) {
  if (!minutes || minutes <= 0) {
    return 'Flexible walk'
  }
  if (minutes < 60) {
    return `${minutes} min`
  }
  const hours = Math.floor(minutes / 60)
  const remaining = minutes % 60
  return remaining ? `${hours}h ${remaining}m` : `${hours}h`
}

function formatPublishedTime(value?: string) {
  if (!hasText(value)) {
    return 'Recently'
  }
  const timestamp = new Date(value).getTime()
  if (Number.isNaN(timestamp)) {
    return 'Recently'
  }
  const diffMinutes = Math.max(1, Math.floor((Date.now() - timestamp) / 60000))
  if (diffMinutes < 60) {
    return `${diffMinutes} min ago`
  }
  const diffHours = Math.floor(diffMinutes / 60)
  if (diffHours < 24) {
    return `${diffHours} h ago`
  }
  const diffDays = Math.floor(diffHours / 24)
  if (diffDays < 7) {
    return `${diffDays} d ago`
  }
  return new Date(timestamp).toLocaleDateString()
}

function sanitizeDifficulty(value?: string): 'easy' | 'medium' | 'hard' {
  if (value === 'hard' || value === 'medium' || value === 'easy') {
    return value
  }
  if (value === 'difficult') {
    return 'hard'
  }
  return 'easy'
}

function sanitizeStampType(value?: string): 'location' | 'story' | 'mission' | 'secret' {
  if (value === 'story' || value === 'mission' || value === 'secret' || value === 'location') {
    return value
  }
  return 'location'
}

function sanitizeRarity(value?: string): 'common' | 'rare' | 'epic' {
  if (value === 'rare' || value === 'epic' || value === 'common') {
    return value
  }
  return 'common'
}

function resolveStoryIcon(code?: string) {
  if (!hasText(code)) {
    return '📖'
  }
  if (code.includes('silk')) {
    return '🌊'
  }
  if (code.includes('east')) {
    return '🏛️'
  }
  if (code.includes('night')) {
    return '🌃'
  }
  return '📖'
}

function resolvePoiIcon(categoryCode?: string) {
  switch (categoryCode) {
    case 'landmark':
      return '🏛️'
    case 'museum':
      return '🏠'
    case 'story_point':
      return '📍'
    default:
      return '📌'
  }
}

function resolvePoiMarkerKey(dto: PublicPoiDto): PoiItem['markerKey'] {
  if (dto.categoryCode === 'landmark') {
    return 'ruins'
  }
  if (dto.categoryCode === 'museum') {
    return 'lisboa'
  }
  if (dto.categoryCode === 'story_point') {
    return 'church'
  }
  return 'theater'
}

function resolveRewardIcon(code?: string) {
  if (code?.includes('coupon')) {
    return '🎟️'
  }
  return '🏆'
}

function resolveStampIcon(type: StampItem['type'], rarity: StampItem['rarity']) {
  if (type === 'secret') {
    return '🔒'
  }
  if (type === 'story') {
    return '📖'
  }
  if (type === 'mission') {
    return '🎯'
  }
  return rarity === 'epic' ? '✨' : rarity === 'rare' ? '⭐' : '📍'
}

function resolveCategoryLabel(code?: string) {
  switch (code) {
    case 'newbie':
      return 'Newbie'
    case 'slow-travel':
      return 'Slow travel'
    case 'photo':
      return 'Photo'
    default:
      return pickReadableText(humanizeCode(code), 'Guide')
  }
}

function resolveCityRewardTitle(cityName: string) {
  return `${cityName} Explorer`
}

function buildPoiSubtitle(dto: PublicPoiDto) {
  return pickReadableText(
    dto.subtitle,
    dto.storylineName ? `${dto.storylineName} stop` : '',
    humanizeCode(dto.categoryCode),
    'Featured route stop',
  )
}

function buildPoiDescription(dto: PublicPoiDto) {
  const storyName = pickReadableText(dto.storylineName, humanizeCode(dto.storylineCode))
  const poiName = pickReadableText(dto.name, humanizeCode(dto.code), 'Explorer stop')
  return pickReadableText(
    dto.description,
    storyName ? `${poiName} is a key stop on the ${storyName} route.` : `${poiName} is a featured Macau exploration stop.`,
  )
}

function buildPoiTags(dto: PublicPoiDto) {
  const tags = [
    humanizeCode(dto.categoryCode),
    pickReadableText(dto.storylineName, humanizeCode(dto.storylineCode)),
    humanizeCode(dto.cityCode),
  ].filter(Boolean)
  return Array.from(new Set(tags)).slice(0, 3)
}

function buildCollectibleHints(poiName: string, storyName?: string) {
  return [
    `${poiName} stamp`,
    storyName ? `${storyName} keepsake` : 'Route keepsake',
    'Traveler photo note',
  ]
}

function getSubMapCatalog() {
  const cache = loadPublicContent()
  if (cache.subMaps.length) {
    return cache.subMaps.slice().sort((left, right) => (left.sortOrder || 0) - (right.sortOrder || 0))
  }
  return cache.cities
    .flatMap((city) => city.subMaps || [])
    .slice()
    .sort((left, right) => (left.sortOrder || 0) - (right.sortOrder || 0))
}

function getCitySubMapDtos(cityCode?: string) {
  return getSubMapCatalog().filter((subMap) => !cityCode || subMap.cityCode === cityCode)
}

function inferSubMapByLocation(cityCode: string, lat: number, lng: number) {
  const subMaps = getCitySubMapDtos(cityCode)
    .filter((subMap) => typeof subMap.centerLat === 'number' && typeof subMap.centerLng === 'number')
    .map((subMap) => ({
      code: subMap.code,
      distanceMeters: calculateDistance(lat, lng, subMap.centerLat || 0, subMap.centerLng || 0),
    }))
    .sort((left, right) => left.distanceMeters - right.distanceMeters)

  return subMaps[0]?.code
}

function selectStampForPoi(poiId: number, storylineId?: number) {
  const stamps = loadPublicContent().stamps
  return stamps.find((stamp) => stamp.relatedPoiId === poiId)
    || stamps.find((stamp) => stamp.relatedStorylineId === storylineId)
}

function getPoiCatalog() {
  const cache = loadPublicContent()
  return cache.pois
    .slice()
    .sort((left, right) => (left.sortOrder || 0) - (right.sortOrder || 0))
    .map((dto) => {
      const storyName = pickReadableText(dto.storylineName, humanizeCode(dto.storylineCode))
      const name = pickReadableText(dto.name, humanizeCode(dto.code), `POI ${dto.id}`)
      const relatedStamp = selectStampForPoi(dto.id, dto.storylineId)
      return {
        id: dto.id,
        name,
        subtitle: buildPoiSubtitle(dto),
        icon: resolvePoiIcon(dto.categoryCode),
        latitude: dto.latitude,
        longitude: dto.longitude,
        gcj02Latitude: dto.latitude,
        gcj02Longitude: dto.longitude,
        address: pickReadableText(dto.address, name),
        geofenceRadius: dto.manualCheckinRadius || dto.triggerRadius || 200,
        triggerRadius: dto.triggerRadius || 50,
        difficulty: sanitizeDifficulty(dto.difficulty),
        category: pickReadableText(humanizeCode(dto.categoryCode), 'Explorer stop'),
        district: pickReadableText(humanizeCode(dto.district), humanizeCode(dto.cityCode), 'Macau'),
        storyLineId: dto.storylineId,
        storyName,
        description: buildPoiDescription(dto),
        checkInMethod: 'gps' as const,
        staySeconds: dto.staySeconds || 30,
        rewardStampId: relatedStamp?.id,
        tags: buildPoiTags(dto),
        coverColor: colorFromKey(dto.code || name),
        cityId: dto.cityCode,
        subMapId: dto.subMapCode,
        subMapName: pickReadableText(dto.subMapName, humanizeCode(dto.subMapCode)),
        markerKey: resolvePoiMarkerKey(dto),
        mapIconUrl: dto.mapIconUrl,
        introTitle: pickReadableText(dto.introTitle, name),
        introSummary: pickReadableText(dto.introSummary, dto.description, `Follow the route narrative at ${name}.`),
        indoorMapTitle: `${name} guide`,
        indoorMapHint: `Use the nearby route and story panels to plan your stop at ${name}.`,
        recommendedTipIds: [],
        recommendedDiscoverIds: [],
        collectibleHints: buildCollectibleHints(name, storyName),
      }
    })
}

function getRewardCatalog() {
  return loadPublicContent().rewards
    .slice()
    .sort((left, right) => (left.sortOrder || 0) - (right.sortOrder || 0))
    .map((reward) => {
      const availableInventory = reward.availableInventory
        ?? Math.max(0, (reward.inventoryTotal || 0) - (reward.inventoryRedeemed || 0))
      return {
        id: reward.id,
        name: pickReadableText(reward.name, humanizeCode(reward.code), `Reward ${reward.id}`),
        subtitle: pickReadableText(reward.subtitle, 'Redeemable reward'),
        icon: resolveRewardIcon(reward.code),
        stampCost: reward.stampCost,
        inventory: availableInventory,
        status: availableInventory > 0 ? 'available' as const : 'coming_soon' as const,
        description: pickReadableText(reward.description, 'Collect stamps to unlock this reward.'),
        highlight: pickReadableText(reward.highlight, `Redeem with ${reward.stampCost} stamps.`),
      }
    })
}

function getStampCatalog(state = loadGameState()) {
  return loadPublicContent().stamps
    .slice()
    .sort((left, right) => (left.sortOrder || 0) - (right.sortOrder || 0))
    .map((stamp) => {
      const type = sanitizeStampType(stamp.stampType)
      const rarity = sanitizeRarity(stamp.rarity)
      return {
        id: stamp.id,
        type,
        name: pickReadableText(stamp.name, humanizeCode(stamp.code), `Stamp ${stamp.id}`),
        description: pickReadableText(stamp.description, 'Unlock this stamp during exploration.'),
        icon: resolveStampIcon(type, rarity),
        collected: state.collectedStampIds.includes(stamp.id),
        poiId: stamp.relatedPoiId || undefined,
        storyId: stamp.relatedStorylineId || undefined,
        rarity,
      }
    })
}

function mapStoryChapter(
  chapter: PublicStoryChapterDto,
  index: number,
  storyUnlocked: boolean,
  completedChapters: number,
) {
  return {
    id: chapter.id,
    title: pickReadableText(chapter.title, `Chapter ${index + 1}`),
    summary: pickReadableText(chapter.summary, 'Continue the route to reveal the next beat.'),
    detail: pickReadableText(chapter.detail, chapter.summary, 'Explore the mapped stop to continue this story.'),
    achievement: pickReadableText(chapter.achievement, 'Unlock a new milestone.'),
    collectible: pickReadableText(chapter.collectible, 'Story keepsake'),
    locationName: pickReadableText(chapter.locationName, 'Macau'),
    locked: !storyUnlocked || index > completedChapters,
  }
}

function ensureAtLeastOneCity(state = loadGameState()) {
  const cities = loadPublicContent().cities
  if (cities.length) {
    return cities
  }

  return [{
    id: 1,
    code: state.user.currentCityId || DEFAULT_UNLOCKED_CITY_ID,
    name: 'Macau',
    subtitle: '',
    description: 'Macau live content is loading.',
    countryCode: 'MO',
    centerLat: AMAP_CONFIG.defaultCenter.latitude,
    centerLng: AMAP_CONFIG.defaultCenter.longitude,
    defaultZoom: 14,
    unlockType: 'auto',
    coverImageUrl: '',
    bannerImageUrl: '',
    subMaps: [{
      id: 11,
      cityId: 1,
      cityCode: DEFAULT_UNLOCKED_CITY_ID,
      code: 'macau-peninsula',
      name: 'Macau Peninsula',
      centerLat: AMAP_CONFIG.defaultCenter.latitude,
      centerLng: AMAP_CONFIG.defaultCenter.longitude,
      sortOrder: 1,
    }],
    sortOrder: 1,
  }]
}

function inferCityByLocation(lat: number, lng: number) {
  const candidate = ensureAtLeastOneCity()
    .map((city) => ({
      code: city.code,
      distanceMeters: calculateDistance(lat, lng, city.centerLat || AMAP_CONFIG.defaultCenter.latitude, city.centerLng || AMAP_CONFIG.defaultCenter.longitude),
    }))
    .sort((left, right) => left.distanceMeters - right.distanceMeters)[0]

  return candidate?.code || DEFAULT_UNLOCKED_CITY_ID
}

function getStoryCatalog(state = loadGameState()) {
  const pois = getPoiCatalog()
  const unlockedCities = new Set(getCities().filter((city) => city.unlocked).map((city) => city.id))

  return loadPublicContent().storylines
    .slice()
    .sort((left, right) => (left.sortOrder || 0) - (right.sortOrder || 0))
    .map((story) => {
      const storyPoiIds = pois.filter((poi) => poi.storyLineId === story.id).map((poi) => poi.id)
      const storyUnlocked = unlockedCities.has(story.cityCode)
        || state.completedStoryIds.includes(story.id)
        || story.cityCode === DEFAULT_UNLOCKED_CITY_ID
      const chapters = Array.isArray(story.chapters) ? story.chapters : []
      const completedChapters = chapters.filter((chapter) => state.completedChapterIds.includes(chapter.id)).length
      const totalChapters = story.totalChapters || chapters.length || 1
      const mappedChapters = chapters.map((chapter, index) => mapStoryChapter(chapter, index, storyUnlocked, completedChapters))
      return {
        id: story.id,
        name: pickReadableText(story.name, story.nameEn, humanizeCode(story.code), `Story ${story.id}`),
        nameEn: pickReadableText(story.nameEn, story.name, humanizeCode(story.code), `Story ${story.id}`),
        description: pickReadableText(story.description, `${pickReadableText(story.name, story.nameEn, 'This route')} connects major Macau story stops.`),
        icon: resolveStoryIcon(story.code),
        coverColor: colorFromKey(story.code || String(story.id)),
        totalChapters,
        completedChapters,
        estimatedTime: formatMinutes(story.estimatedMinutes),
        difficulty: sanitizeDifficulty(story.difficulty),
        poiIds: storyPoiIds,
        chapterTitles: mappedChapters.map((chapter) => chapter.title),
        progress: Math.round((completedChapters / totalChapters) * 100),
        rewardBadge: pickReadableText(story.rewardBadge, `${pickReadableText(story.name, story.nameEn, 'Story')} badge`),
        locked: !storyUnlocked,
        unlockHint: storyUnlocked ? '' : `Explore ${humanizeCode(story.cityCode)} to unlock this storyline.`,
        chapters: mappedChapters,
        moodTags: [
          sanitizeDifficulty(story.difficulty),
          humanizeCode(story.cityCode),
          humanizeCode(story.code),
        ].filter(Boolean),
      }
    })
}

function getLiveNotifications(state = loadGameState()) {
  return loadPublicContent().notifications
    .slice()
    .sort((left, right) => (left.sortOrder || 0) - (right.sortOrder || 0))
    .map((notification): NotificationItem => {
      const type: NotificationItem['type'] = notification.notificationType === 'ugc'
        ? 'ugc'
        : notification.notificationType === 'activity'
          ? 'activity'
          : 'system'

      return {
        id: notification.id,
        title: pickReadableText(notification.title, `Update ${notification.id}`),
        content: pickReadableText(notification.content, 'A new traveler update is available.'),
        timeLabel: formatPublishedTime(notification.publishedAt),
        unread: (state.unreadNotificationIds || []).includes(notification.id),
        type,
      }
    })
}

function getLiveTips(state = loadGameState()) {
  const liveTips = loadPublicContent().tips
    .slice()
    .sort((left, right) => (left.sortOrder || 0) - (right.sortOrder || 0))
    .map((tip) => ({
      id: tip.id,
      title: pickReadableText(tip.title, humanizeCode(tip.code), `Tip ${tip.id}`),
      summary: pickReadableText(tip.summary, 'Traveler guidance for this route.'),
      coverColor: colorFromKey(tip.code || String(tip.id)),
      category: resolveCategoryLabel(tip.categoryCode),
      author: pickReadableText(tip.authorDisplayName, 'Trip of Macau'),
      likes: 0,
      saves: 0,
      readMinutes: Math.max(3, (tip.contentParagraphs || []).length * 2),
      tags: Array.isArray(tip.tags) ? tip.tags : [],
      imageUrl: tip.coverImageUrl || undefined,
      locationName: pickReadableText(tip.locationName, humanizeCode(tip.cityCode), 'Macau'),
      contentParagraphs: Array.isArray(tip.contentParagraphs) ? tip.contentParagraphs : [],
      createdAt: tip.publishedAt,
    }))

  return [...(state.publishedTips || []), ...liveTips]
}

function getLiveDiscoverCards() {
  return loadPublicContent().discoverCards.map((card) => ({
    id: card.id,
    title: pickReadableText(card.title, `Discover ${card.sourceId || card.id}`),
    subtitle: pickReadableText(card.subtitle, humanizeCode(card.sourceType), humanizeCode(card.type)),
    description: pickReadableText(card.description, 'Discover a featured Macau experience.'),
    tag: pickReadableText(card.tag, humanizeCode(card.type), 'Featured'),
    icon: pickReadableText(card.icon, card.type === 'activity' ? '🌃' : card.type === 'merchant' ? '🎟️' : '🔥'),
    type: card.type,
    district: pickReadableText(card.district, 'Macau'),
    actionText: pickReadableText(
      card.actionText,
      card.type === 'merchant' ? 'Redeem' : card.type === 'checkin' ? 'Check in' : 'View',
    ),
    coverColor: pickReadableText(card.coverColor, colorFromKey(card.id)),
  }))
}

function getRuntimeGroupSettings(group: string): Record<string, any> {
  const settings = loadPublicContent().runtimeGroups[group]?.settings
  return settings && typeof settings === 'object' ? settings as Record<string, any> : {}
}

function getObjectRecord(value: unknown): Record<string, any> {
  return value && typeof value === 'object' && !Array.isArray(value) ? value as Record<string, any> : {}
}

function readNumber(value: unknown, fallback: number) {
  if (typeof value === 'number' && Number.isFinite(value)) {
    return value
  }
  if (typeof value === 'string') {
    const parsed = Number(value)
    if (Number.isFinite(parsed)) {
      return parsed
    }
  }
  return fallback
}

function readStringValue(value: unknown, fallback: string) {
  return typeof value === 'string' && value.trim().length ? value.trim() : fallback
}

function getTravelRecommendationProfiles() {
  const profiles = getRuntimeGroupSettings('travel').recommendation_profiles
  return Array.isArray(profiles)
    ? profiles.filter((item) => item && typeof item === 'object') as Array<Record<string, any>>
    : []
}

export async function refreshPublicContent(locale: PublicLocaleCode = (loadGameState().user.localeCode as PublicLocaleCode) || DEFAULT_PUBLIC_LOCALE) {
  if (USE_MOCK) {
    return loadPublicContent()
  }

  await syncUserStateFromServer()

  const existing = loadPublicContent()
  const [
    cities,
    subMaps,
    pois,
    storylines,
    tips,
    rewards,
    stamps,
    notifications,
    discoverCards,
    discoverRuntime,
    mapRuntime,
    travelRuntime,
  ] = await Promise.all([
    api.public.getPublicCities(locale),
    api.public.getPublicSubMaps(locale),
    api.public.getPublicPois(locale),
    api.public.getPublicStorylines(locale),
    api.public.getPublicTips(locale),
    api.public.getPublicRewards(locale),
    api.public.getPublicStamps(locale),
    api.public.getPublicNotifications(locale),
    api.public.getPublicDiscoverCards(locale),
    api.public.getPublicRuntimeGroup('discover', locale),
    api.public.getPublicRuntimeGroup('map', locale),
    api.public.getPublicRuntimeGroup('travel', locale),
  ])

  return savePublicContent({
    locale,
    updatedAt: new Date().toISOString(),
    cities,
    subMaps,
    pois,
    storylines,
    tips,
    rewards,
    stamps,
    notifications,
    discoverCards,
    runtimeGroups: {
      ...existing.runtimeGroups,
      discover: discoverRuntime,
      map: mapRuntime,
      travel: travelRuntime,
    },
  })
}

export function loadGameState(): GameStateSnapshot {
  try {
    const stored = Taro.getStorageSync(STORAGE_KEY)
    if (!stored) {
      return saveState(createDefaultState())
    }
    const normalized = normalizeState(stored)
    if (!hasActiveSessionToken() && normalized.user.authStatus !== 'anonymous') {
      return getAnonymousState()
    }
    Taro.setStorageSync(STORAGE_KEY, normalized)
    return normalized
  } catch (error) {
    console.warn('Failed to read game state.', error)
    return saveState(createDefaultState())
  }
}

export async function updateUserPreference(patch: Partial<AppUserProfile>) {
  if (!(await requireAuth('調整個人設定前，請先使用微信登入。'))) {
    throw new AuthRequiredError()
  }
  const current = await syncUserStateFromServer()
  assertAuthenticatedSnapshot(current)
  const nextUser = {
    ...current.user,
    ...patch,
  }
  const preferences = await api.user.updateUserPreferences(buildUserPreferencesPayload(nextUser))
  const merged = saveState({
    ...current,
    user: {
      ...current.user,
      ...nextUser,
      localeCode: preferences.localeCode || nextUser.localeCode || current.user.localeCode || DEFAULT_PUBLIC_LOCALE,
      interfaceMode: preferences.interfaceMode || nextUser.interfaceMode,
      fontScale: Number(preferences.fontScale ?? nextUser.fontScale ?? 1),
      highContrast: !!preferences.highContrast,
      voiceGuideEnabled: !!preferences.voiceGuideEnabled,
    },
  })
  wx.setStorageSync('interfaceMode', merged.user.interfaceMode)
  return merged.user
}

export async function registerCityVisitByLocation(lat: number, lng: number) {
  const state = loadGameState()
  const cityId = inferCityByLocation(lat, lng)
  const subMapId = inferSubMapByLocation(cityId, lat, lng)
  const exists = (state.cityUnlocks || []).find((item) => item.cityId === cityId)
  const unlockedAt = exists ? exists.unlockedAt : new Date().toISOString()
  const nextUnlocks = exists ? (state.cityUnlocks || []) : [...(state.cityUnlocks || []), { cityId, unlockedAt }]
  const city = getCities().find((item) => item.id === cityId)

  const next = {
    ...state,
    cityUnlocks: nextUnlocks,
    user: {
      ...state.user,
      currentCityId: cityId,
      currentSubMapId: subMapId,
      badges: city && !state.user.badges.includes(city.titleReward)
        ? [...state.user.badges, city.titleReward]
        : state.user.badges,
    },
  }
  saveState(next)
  if (!USE_MOCK && state.user.authStatus !== 'anonymous' && hasActiveSessionToken()) {
    try {
      const current = await syncUserStateFromServer()
      assertAuthenticatedSnapshot(current)
      await api.user.updateUserCurrentCity({ cityCode: cityId })
      await syncUserStateFromServer()
    } catch (error) {
      console.warn('Failed to persist current city.', error)
    }
  }
  return cityId
}

export function getCities(): CityProgressItem[] {
  const state = loadGameState()
  const publicCities = ensureAtLeastOneCity(state)
  const publicPois = getPoiCatalog()

  return publicCities.map((city) => {
    const unlockRecord = (state.cityUnlocks || []).find((item) => item.cityId === city.code)
    const currentCitySelected = state.user.currentCityId === city.code
    const cityPois = publicPois.filter((poi) => poi.cityId === city.code)
    const checkedPoiCount = state.checkinHistory.filter((item) => cityPois.some((poi) => poi.id === item.poiId)).length
    const subMaps = getCitySubMapDtos(city.code).map((subMap) => {
      const subMapPois = cityPois.filter((poi) => poi.subMapId === subMap.code)
      const checkedSubMapPoiCount = state.checkinHistory.filter((item) => subMapPois.some((poi) => poi.id === item.poiId)).length
      return {
        id: subMap.code,
        cityId: city.code,
        name: pickReadableText(subMap.name, humanizeCode(subMap.code), 'Sub-map'),
        subtitle: pickReadableText(subMap.subtitle, 'Map zone'),
        coverColor: colorFromKey(subMap.code || String(subMap.id)),
        centerLat: subMap.centerLat || undefined,
        centerLng: subMap.centerLng || undefined,
        unlocked: true,
        explorationProgress: subMapPois.length ? Math.round((checkedSubMapPoiCount / subMapPois.length) * 100) : 0,
        landmarkCount: subMapPois.length,
      } as SubMapProgressItem
    })

    return {
      id: city.code,
      name: pickReadableText(city.name, humanizeCode(city.code), 'Macau'),
      subtitle: pickReadableText(city.subtitle, 'Story driven city exploration'),
      coverColor: colorFromKey(city.code || String(city.id)),
      centerLat: city.centerLat || undefined,
      centerLng: city.centerLng || undefined,
      unlocked: true,
      firstUnlockedAt: unlockRecord?.unlockedAt,
      explorationProgress: cityPois.length ? Math.round((checkedPoiCount / cityPois.length) * 100) : 0,
      titleReward: resolveCityRewardTitle(pickReadableText(city.name, humanizeCode(city.code), 'City')),
      landmarkCount: cityPois.length,
      currentSubMapId: currentCitySelected ? state.user.currentSubMapId : undefined,
      subMaps,
    }
  })
}

export function getCitySubMaps(cityId?: string): SubMapProgressItem[] {
  const currentCityId = cityId || loadGameState().user.currentCityId || DEFAULT_UNLOCKED_CITY_ID
  return getCities().find((city) => city.id === currentCityId)?.subMaps || []
}

export async function switchCurrentCity(cityId: string) {
  const state = loadGameState()
  const city = getCities().find((item) => item.id === cityId)
  if (!city?.unlocked) {
    throw new Error('Explore this city first to unlock it.')
  }
  const defaultSubMapId = city.subMaps?.[0]?.id

  const next = {
    ...state,
    user: {
      ...state.user,
      currentCityId: cityId,
      currentSubMapId: defaultSubMapId,
    },
  }
  saveState(next)
  if (!USE_MOCK && state.user.authStatus !== 'anonymous' && hasActiveSessionToken()) {
    const current = await syncUserStateFromServer()
    assertAuthenticatedSnapshot(current)
    await api.user.updateUserCurrentCity({ cityCode: cityId })
    await syncUserStateFromServer()
  }
  return city
}

export async function switchCurrentSubMap(subMapId?: string) {
  const state = loadGameState()
  const currentCityId = state.user.currentCityId || DEFAULT_UNLOCKED_CITY_ID
  const availableSubMaps = getCitySubMaps(currentCityId)

  if (!subMapId) {
    saveState({
      ...state,
      user: {
        ...state.user,
        currentSubMapId: undefined,
      },
    })
    return undefined
  }

  const target = availableSubMaps.find((subMap) => subMap.id === subMapId)
  if (!target) {
    throw new Error('This sub-map is not available in the current city.')
  }

  saveState({
    ...state,
    user: {
      ...state.user,
      currentSubMapId: subMapId,
    },
  })
  return subMapId
}

export async function saveTravelAssessment(answer: TravelAssessmentAnswer, userLocation?: { latitude: number; longitude: number }) {
  if (!(await requireAuth('生成個人化推薦前，請先使用微信登入。'))) {
    throw new AuthRequiredError()
  }
  const state = loadGameState()
  if (userLocation) {
    void registerCityVisitByLocation(userLocation.latitude, userLocation.longitude)
  }
  const recommendation = getTravelRecommendation(answer)
  const next = {
    ...state,
    travelAssessment: answer,
    recommendation,
  }
  saveState(next)
  return recommendation
}

export function getTravelRecommendation(answer?: TravelAssessmentAnswer | null) {
  const target = answer || loadGameState().travelAssessment
  const stories = getStorylines()
  const discoverCards = getDiscoverCards()
  const tips = getTipArticles().filter((tip) => !tip.isPublishedByUser)
  const pois = getPoiCatalog()
  const profiles = getTravelRecommendationProfiles()

  if (profiles.length) {
    const normalizedInterests = (target?.interests || []).map((interest) => interest.toLowerCase())
    const selectedProfile = profiles
      .slice()
      .sort((left, right) => {
        const leftInterests = Array.isArray(left.interests) ? left.interests.map((item: string) => String(item).toLowerCase()) : []
        const rightInterests = Array.isArray(right.interests) ? right.interests.map((item: string) => String(item).toLowerCase()) : []
        const leftScore = leftInterests.filter((interest: string) => normalizedInterests.some((targetInterest) => targetInterest.includes(interest) || interest.includes(targetInterest))).length
          + (target?.playDuration && String(left.playDuration || '').toLowerCase() === target.playDuration.toLowerCase() ? 1 : 0)
        const rightScore = rightInterests.filter((interest: string) => normalizedInterests.some((targetInterest) => targetInterest.includes(interest) || interest.includes(targetInterest))).length
          + (target?.playDuration && String(right.playDuration || '').toLowerCase() === target.playDuration.toLowerCase() ? 1 : 0)
        return rightScore - leftScore
      })[0]

    const selectedStory = stories.find((story) => story.id === Number(selectedProfile.storyId))
      || stories.find((story) => humanizeCode(story.name).toLowerCase() === humanizeCode(selectedProfile.storyCode).toLowerCase())
      || stories[0]
    const selectedPoi = pois.find((poi) => poi.id === Number(selectedProfile.poiId))
      || pois.find((poi) => humanizeCode(poi.name).toLowerCase() === humanizeCode(selectedProfile.poiCode).toLowerCase())
      || pois.find((poi) => poi.storyLineId === selectedStory?.id)
      || pois[0]
    const selectedTip = tips.find((tip) => tip.id === Number(selectedProfile.tipId))
      || tips.find((tip) => humanizeCode(tip.title).toLowerCase() === humanizeCode(selectedProfile.tipCode).toLowerCase())
      || tips[0]

    return {
      storyId: selectedStory?.id || 0,
      storyName: selectedStory?.name || pickReadableText(humanizeCode(selectedProfile.storyCode), 'Macau Highlights'),
      activityTitle: pickReadableText(selectedProfile.activityTitle, discoverCards[0]?.title, 'Featured route'),
      poiName: selectedPoi?.name || pickReadableText(humanizeCode(selectedProfile.poiCode), 'Macau'),
      ugcTitle: selectedTip?.title || pickReadableText(humanizeCode(selectedProfile.tipCode), 'Traveler guide'),
      reason: pickReadableText(
        selectedProfile.reason,
        target
          ? `Based on your selected interests, ${selectedStory?.name || 'this route'} fits the current journey best.`
          : `Start with ${selectedStory?.name || 'the highlighted route'} to connect the main live story path.`,
      ),
    }
  }

  if (!stories.length || !tips.length || !pois.length) {
    return {
      storyId: 0,
      storyName: 'Macau Highlights',
      activityTitle: discoverCards[0]?.title || 'Featured route',
      poiName: 'Macau',
      ugcTitle: tips[0]?.title || 'Traveler guide',
      reason: 'Live content is still loading. Refresh once public data is available.',
    }
  }

  let selectedStory = stories[0]
  if (target?.interests.some((interest) => interest.toLowerCase().includes('photo'))) {
    selectedStory = stories.find((story) => story.difficulty !== 'easy') || selectedStory
  } else if (target?.interests.some((interest) => interest.toLowerCase().includes('history'))) {
    selectedStory = stories.find((story) => story.name.toLowerCase().includes('silk')) || selectedStory
  }

  const selectedPoi = pois.find((poi) => poi.storyLineId === selectedStory.id) || pois[0]
  const selectedTip = tips.find((tip) => tip.locationName?.includes(selectedPoi.name)) || tips[0]

  return {
    storyId: selectedStory.id,
    storyName: selectedStory.name,
    activityTitle: discoverCards[0]?.title || 'Featured route',
    poiName: selectedPoi.name,
    ugcTitle: selectedTip.title,
    reason: target
      ? `Based on your selected interests, ${selectedStory.name} and ${selectedPoi.name} fit the current route best.`
      : `Start with ${selectedStory.name} to connect the main public story route.`,
  }
}

export function getStorylines(): StorylineItem[] {
  return getStoryCatalog()
}

export function getStamps(): StampItem[] {
  return getStampCatalog()
}

export function getRewards(): RewardItem[] {
  const state = loadGameState()
  return getRewardCatalog().map((reward) => ({
    ...reward,
    status: state.redeemedRewardIds?.includes(reward.id) ? 'redeemed' : reward.status,
  }))
}

export async function redeemReward(rewardId: number) {
  if (!(await requireAuth('兌換獎勵前，請先使用微信登入。'))) {
    throw new AuthRequiredError()
  }
  const state = loadGameState()
  const reward = getRewardCatalog().find((item) => item.id === rewardId)

  if (!reward) {
    throw new Error('This reward is not available.')
  }

  if (state.redeemedRewardIds?.includes(rewardId)) {
    throw new Error('You already redeemed this reward.')
  }

  if (state.user.totalStamps < reward.stampCost) {
    throw new Error('Not enough stamps yet.')
  }
  if (!USE_MOCK) {
    const current = await syncUserStateFromServer()
    assertAuthenticatedSnapshot(current)
    const result = await api.user.redeemUserReward(rewardId)
    applyRemoteUserState(result.state)
    return reward
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

function pickCheckinStamp(poiId: number, storylineId?: number) {
  const stamps = getStamps()
  return stamps.find((stamp) => stamp.poiId === poiId)
    || stamps.find((stamp) => stamp.storyId === storylineId && !stamp.collected)
    || stamps.find((stamp) => !stamp.collected)
    || stamps[0]
}

function buildArrivalExperience(poi: PoiItem) {
  const storyline = poi.storyLineId ? getStoryById(poi.storyLineId) : null
  const stamp = pickCheckinStamp(poi.id, poi.storyLineId)
  return {
    poiId: poi.id,
    title: `Arrived at ${poi.name}`,
    narrative: pickReadableText(poi.introSummary, poi.description, `${poi.name} is ready for your next story beat.`),
    audioTitle: pickReadableText(poi.introTitle, `${poi.name} audio guide`),
    audioDuration: formatMinutes(Math.max(1, Math.round((poi.staySeconds || 30) / 60))),
    rewardLabel: stamp ? `Unlock ${stamp.name}` : `Advance ${storyline?.name || 'your route'}`,
    canManualCheckin: (poi.geofenceRadius || 0) > (poi.triggerRadius || 0),
  }
}

function getCheckinExperienceGain(poi: PoiItem) {
  switch (poi.difficulty) {
    case 'hard':
      return 50
    case 'medium':
      return 35
    default:
      return 25
  }
}

export function getNearbyPois(lat: number, lng: number, accuracy: number, cityId?: string, subMapId?: string): NearbyPoiView[] {
  const state = loadGameState()
  const currentCityId = cityId || state.user.currentCityId || DEFAULT_UNLOCKED_CITY_ID
  const currentSubMapId = subMapId ?? state.user.currentSubMapId
  return getPoiCatalog()
    .filter((poi) => poi.cityId === currentCityId)
    .filter((poi) => !currentSubMapId || poi.subMapId === currentSubMapId)
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
    .sort((left, right) => left.distanceMeters - right.distanceMeters)
}

export function getMapBootstrapConfig() {
  const state = loadGameState()
  const currentCity = ensureAtLeastOneCity(state).find((city) => city.code === state.user.currentCityId) || ensureAtLeastOneCity(state)[0]
  const currentSubMap = getCitySubMapDtos(currentCity.code).find((subMap) => subMap.code === state.user.currentSubMapId)
  const mapRules = getObjectRecord(getRuntimeGroupSettings('map').checkin_rules)
  return {
    amapKey: AMAP_CONFIG.key,
    center: {
      latitude: currentSubMap?.centerLat || currentCity.centerLat || AMAP_CONFIG.defaultCenter.latitude,
      longitude: currentSubMap?.centerLng || currentCity.centerLng || AMAP_CONFIG.defaultCenter.longitude,
    },
    city: pickReadableText(currentCity.name, humanizeCode(currentCity.code), 'Macau'),
    cityCode: currentCity.code || DEFAULT_UNLOCKED_CITY_ID,
    subMapCode: currentSubMap?.code,
    subMapName: currentSubMap ? pickReadableText(currentSubMap.name, humanizeCode(currentSubMap.code)) : '',
    checkinRules: {
      gpsIntervals: `${Math.max(1, Math.round(readNumber(mapRules.gpsIntervalSeconds, 2)))}s`,
      cooldownMinutes: Math.max(1, Math.round(readNumber(mapRules.cooldownSeconds, 1800) / 60)),
      debounceSeconds: Math.max(1, Math.round(readNumber(mapRules.debounceMillis, 2000) / 1000)),
      radiusPolicy: readStringValue(mapRules.radiusPolicy, 'dynamic'),
      manualFallback: `${Math.max(50, Math.round(readNumber(mapRules.manualRadius, 200)))}m`,
    },
  }
}

export function getArrivalExperience(poiId: number): ArrivalExperience | null {
  const poi = getPoiById(poiId)
  if (poi) {
    return buildArrivalExperience(poi)
  }
  return null
}

export function getTipArticles(): TipArticleItem[] {
  return getLiveTips()
}

export async function publishTipPost(payload: {
  title: string
  summary: string
  category: string
  locationName: string
  imageUrl?: string
  contentParagraphs: string[]
}) {
  if (!(await requireAuth('發佈旅人秘笈前，請先使用微信登入。'))) {
    throw new AuthRequiredError()
  }
  const state = loadGameState()
  const article: TipArticleItem = {
    id: Date.now(),
    title: payload.title,
    summary: payload.summary,
    category: payload.category,
    author: state.user.nickname || 'Traveler',
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
  return getLiveNotifications()
}

export async function markNotificationsRead() {
  if (!(await requireAuth('查看你的通知前，請先使用微信登入。'))) {
    throw new AuthRequiredError()
  }
  const state = loadGameState()
  saveState({
    ...state,
    unreadNotificationIds: [],
  })
}

export function getUnreadNotificationCount() {
  return getNotifications().filter((item) => item.unread).length
}

export function getDiscoverCards(): DiscoverCardItem[] {
  return getLiveDiscoverCards()
}

export function getTipArticleById(id: number) {
  return getTipArticles().find((article) => article.id === id)
}

export function getPoiSearchTips(keyword: string, currentCityId?: string, currentSubMapId?: string) {
  const state = loadGameState()
  const cityId = currentCityId || state.user.currentCityId || DEFAULT_UNLOCKED_CITY_ID
  const subMapId = currentSubMapId ?? state.user.currentSubMapId
  const normalizedKeyword = keyword.trim().toLowerCase()
  return getPoiCatalog()
    .filter((poi) => poi.cityId === cityId)
    .filter((poi) => !subMapId || poi.subMapId === subMapId)
    .filter((poi) => {
      if (!normalizedKeyword) {
        return true
      }
      return poi.name.toLowerCase().includes(normalizedKeyword)
        || poi.address.toLowerCase().includes(normalizedKeyword)
        || poi.tags.some((tag) => tag.toLowerCase().includes(normalizedKeyword))
    })
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
      `Head toward ${poi.district}.`,
      `Use ${poi.name} as your next route anchor.`,
      `Stay around ${poi.name} for about ${poi.staySeconds} seconds to complete the stop.`,
    ],
  }
}

export async function performMockCheckin(poiId: number, triggerMode: 'gps' | 'manual' | 'mock' = 'mock'): Promise<CheckinResult> {
  if (!(await requireAuth('完成打卡前，請先使用微信登入。'))) {
    throw new AuthRequiredError()
  }
  const poi = getPoiById(poiId)
  if (!poi) {
    throw new Error('This stop is not available for check-in.')
  }

  if (!USE_MOCK) {
    const current = await syncUserStateFromServer()
    assertAuthenticatedSnapshot(current)
    const remote = await api.user.createUserCheckin({
      poiId,
      triggerMode,
    })
    applyRemoteUserState(remote.state)
    return {
      success: remote.success,
      poiId: remote.poiId,
      poiName: remote.poiName,
      stampId: remote.stampId || 0,
      stampName: remote.stampName || '',
      experienceGained: remote.experienceGained || 0,
      triggerMode: remote.triggerMode,
      unlockedStorylineId: remote.unlockedStorylineId,
    }
  }

  const state = loadGameState()
  const stamp = pickCheckinStamp(poi.id, poi.storyLineId)
  if (!stamp) {
    throw new Error('No stamp is configured for this stop yet.')
  }

  const stampIds = new Set(state.collectedStampIds)
  const isNewStamp = !stampIds.has(stamp.id)
  stampIds.add(stamp.id)

  const completedStoryIds = new Set(state.completedStoryIds)
  if (poi.storyLineId) {
    completedStoryIds.add(poi.storyLineId)
  }

  const completedChapterIds = new Set(state.completedChapterIds)
  const relatedStory = poi.storyLineId ? getStoryById(poi.storyLineId) : null
  const nextUnlockedChapter = relatedStory?.chapters?.find((chapter) => !completedChapterIds.has(chapter.id) && !chapter.locked)
    || relatedStory?.chapters?.find((chapter) => !completedChapterIds.has(chapter.id))
  if (nextUnlockedChapter) {
    completedChapterIds.add(nextUnlockedChapter.id)
  }

  const cityId = poi.cityId || DEFAULT_UNLOCKED_CITY_ID
  const hasCityUnlock = (state.cityUnlocks || []).some((item) => item.cityId === cityId)
  const nextUnlocks = hasCityUnlock
    ? (state.cityUnlocks || [])
    : [...(state.cityUnlocks || []), { cityId, unlockedAt: new Date().toISOString() }]

  const result: CheckinResult = {
    success: true,
    poiId: poi.id,
    poiName: poi.name,
    stampId: stamp.id,
    stampName: stamp.name,
    experienceGained: getCheckinExperienceGain(poi),
    triggerMode,
    unlockedStorylineId: poi.storyLineId,
  }

  const next: GameStateSnapshot = {
    ...state,
    collectedStampIds: Array.from(stampIds),
    completedStoryIds: Array.from(completedStoryIds),
    completedChapterIds: Array.from(completedChapterIds),
    cityUnlocks: nextUnlocks,
    user: {
      ...state.user,
      totalStamps: state.user.totalStamps + (isNewStamp ? 1 : 0),
      currentExp: state.user.currentExp + result.experienceGained,
      unlockedStorylines: Array.from(completedStoryIds).length,
      currentCityId: cityId,
    },
    checkinHistory: [
      {
        ...result,
        checkedAt: new Date().toISOString(),
      },
      ...state.checkinHistory,
    ].slice(0, 20),
  }

  if (next.user.currentExp >= next.user.nextLevelExp) {
    next.user.level += 1
    next.user.currentExp -= next.user.nextLevelExp
    next.user.nextLevelExp += 120
    next.user.title = next.user.level >= 5 ? 'City Story Guide' : next.user.level >= 4 ? 'Route Explorer' : 'Macau Walker'
  }

  saveState(next)
  return result
}

export function getPoiById(id: number) {
  return getPoiCatalog().find((poi) => poi.id === id)
}

export function getStoryById(id: number) {
  return getStorylines().find((story) => story.id === id)
}

export function getCheckinHistory() {
  return loadGameState().checkinHistory
}

export function getEmergencyContact() {
  return Taro.getStorageSync(EMERGENCY_CONTACT_KEY) || {
    name: 'Emergency contact',
    phone: '10086',
  }
}

export async function updateEmergencyContact(contact: { name: string; phone: string }) {
  if (!(await requireAuth('保存緊急聯絡人前，請先使用微信登入。'))) {
    throw new AuthRequiredError()
  }
  if (!USE_MOCK) {
    const current = await syncUserStateFromServer()
    assertAuthenticatedSnapshot(current)
    const preferences = await api.user.updateUserPreferences(buildUserPreferencesPayload(current.user, {
      emergencyContactName: contact.name,
      emergencyContactPhone: contact.phone,
    }))
    const savedContact = {
      name: preferences.emergencyContactName || contact.name,
      phone: preferences.emergencyContactPhone || contact.phone,
    }
    Taro.setStorageSync(EMERGENCY_CONTACT_KEY, savedContact)
    return savedContact
  }
  Taro.setStorageSync(EMERGENCY_CONTACT_KEY, contact)
  return contact
}

export async function loginWithWeChatProfile() {
  return loginWithWechat()
}

export const loginWithWeChat = loginWithWechat
