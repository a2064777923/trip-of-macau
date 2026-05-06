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
  StoryContentBlockItem,
  StoryChapterRuntimeItem,
  StoryExplorationSummaryItem,
  StoryMediaAssetItem,
  StoryModeRouteChapter,
  StoryModeRouteContext,
  StoryModeSessionState,
  StoryRuntimeEventType,
  StoryRuntimeStepItem,
  StorySessionItem,
  StoryRulePayload,
  StorylineRuntimeItem,
  StorylineItem,
  SubMapProgressItem,
  TipArticleItem,
  TravelAssessmentAnswer,
  TravelRecommendation,
} from '../types/game'
import {
  api,
  DEFAULT_PUBLIC_LOCALE,
  PublicActivityDto,
  PublicBadgeDto,
  PublicCityDto,
  PublicCollectibleDto,
  PublicDiscoverCardDto,
  PublicNotificationDto,
  PublicPoiDto,
  PublicRewardDto,
  PublicRuntimeGroupDto,
  PublicStampDto,
  PublicStoryChapterDto,
  PublicStoryChapterConditionDto,
  PublicStoryChapterEffectDto,
  PublicStoryChapterRuntimeDto,
  PublicStoryChapterUnlockDto,
  PublicStoryContentBlockDto,
  PublicStorylineRuntimeDto,
  PublicStoryMediaAssetDto,
  PublicStorylineSessionDto,
  PublicStorylineDto,
  PublicExperienceRuntimeStepDto,
  PublicSubMapDto,
  PublicTipArticleDto,
  PublicLocaleCode,
  PublicUserPreferencesDto,
  PublicUserExplorationDto,
  PublicUserSessionDto,
  PublicUserStateDto,
  isPublicApiError,
} from './api'
import {
  DEV_RUNTIME_DIAGNOSTICS_ENABLED,
  PUBLIC_API_HOST_LABEL,
  isPublicApiMockMode,
  isWechatDevBypassEnabled,
} from '../constants/env'
import { calculateDistance, formatDistance, isWithinTriggerRange } from '../utils/location'

const STORAGE_KEY = 'trip-of-macau-game-state'
const TOKEN_KEY = 'token'
const EMERGENCY_CONTACT_KEY = 'trip-of-macau-emergency-contact'
const PUBLIC_CONTENT_KEY = 'trip-of-macau-public-content'
const DEV_BYPASS_IDENTITY_KEY = 'trip-of-macau-dev-bypass-identity'
const STORY_MODE_SESSION_KEY = 'trip-of-macau-story-mode-session'
const STORY_MODE_ROUTE_CONTEXT_KEY = 'trip-of-macau-story-mode-route-context'
const PROFILE_AUTH_WALL_PATH = '/pages/profile/index'

const DEFAULT_UNLOCKED_CITY_ID = 'macau'
const DEFAULT_COLLECTED_STAMP_IDS = [101, 102, 103, 104]
const DEFAULT_COMPLETED_STORY_IDS = [1, 2]
const DEFAULT_COMPLETED_CHAPTER_IDS = [1011, 1012, 1021, 1022]
const DEFAULT_UNREAD_NOTIFICATION_IDS = [1, 2]
const DEFAULT_COLOR_PALETTE = ['#ffd9e5', '#dff7ef', '#dfeaff', '#fff0c8', '#e9defc', '#dff3ff']
const FLAGSHIP_STORY_CODE = 'east_west_war_and_coexistence'
const FLAGSHIP_STORY_NAME = '東西方文明的戰火與共生'
const LEGACY_DUPLICATE_STORY_CODE = 'macau_fire_route'
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

type LoosePublicStoryMediaAssetDto = PublicStoryMediaAssetDto | string | Record<string, any> | null | undefined

interface PublicContentCache {
  locale: string
  updatedAt: string
  cities: PublicCityDto[]
  subMaps: PublicSubMapDto[]
  pois: PublicPoiDto[]
  storylines: PublicStorylineDto[]
  tips: PublicTipArticleDto[]
  activities: PublicActivityDto[]
  collectibles: PublicCollectibleDto[]
  badges: PublicBadgeDto[]
  rewards: PublicRewardDto[]
  stamps: PublicStampDto[]
  notifications: PublicNotificationDto[]
  discoverCards: PublicDiscoverCardDto[]
  runtimeGroups: Record<string, PublicRuntimeGroupDto>
}

let publicContentCache: PublicContentCache | null = null
let authPromptVisible = false

type PublicContentRefreshDiagnostics = {
  refreshedAt?: string
  status: 'idle' | 'success' | 'partial' | 'failed'
  message: string
  apiBaseMode: 'mock' | 'live'
  failedSections: string[]
  counts: {
    cities: number
    subMaps: number
    pois: number
    storylines: number
    tips: number
    activities: number
    collectibles: number
    badges: number
    rewards: number
    stamps: number
    notifications: number
    discoverCards: number
  }
}

let publicContentRefreshDiagnostics: PublicContentRefreshDiagnostics = {
  status: 'idle',
  message: '尚未同步公開內容。',
  apiBaseMode: isPublicApiMockMode() ? 'mock' : 'live',
  failedSections: [],
  counts: {
    cities: 0,
    subMaps: 0,
    pois: 0,
    storylines: 0,
    tips: 0,
    activities: 0,
    collectibles: 0,
    badges: 0,
    rewards: 0,
    stamps: 0,
    notifications: 0,
    discoverCards: 0,
  },
}

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
      nickname: '旅行者',
      avatarUrl: '',
      authStatus: 'anonymous',
      localeCode: DEFAULT_PUBLIC_LOCALE,
      openId: '',
      level: 1,
      title: '旅行者',
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
    activities: [],
    collectibles: [],
    badges: [],
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
  return !isPublicApiMockMode() && isWechatDevBypassEnabled() && detectDevtoolsEnvironment()
}

export async function ensureDevBypassSession() {
  if (!isDevBypassAvailable()) {
    return loadGameState().user
  }

  const current = loadGameState()
  if (hasActiveSessionToken() && current.user.authStatus !== 'anonymous') {
    return current.user
  }

  try {
    return await loginWithDevBypass()
  } catch (error) {
    console.warn('Failed to prepare local mini-program session.', error)
    return loadGameState().user
  }
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
  if (isPublicApiMockMode()) {
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
    activities: Array.isArray(raw?.activities) ? raw.activities : defaults.activities,
    collectibles: Array.isArray(raw?.collectibles) ? raw.collectibles : defaults.collectibles,
    badges: Array.isArray(raw?.badges) ? raw.badges : defaults.badges,
    rewards: Array.isArray(raw?.rewards) ? raw.rewards : defaults.rewards,
    stamps: Array.isArray(raw?.stamps) ? raw.stamps : defaults.stamps,
    notifications: Array.isArray(raw?.notifications) ? raw.notifications : defaults.notifications,
    discoverCards: Array.isArray(raw?.discoverCards) ? raw.discoverCards : defaults.discoverCards,
    runtimeGroups: raw?.runtimeGroups && typeof raw.runtimeGroups === 'object' ? raw.runtimeGroups : defaults.runtimeGroups,
  }
}

function toPersistableStoryline(story: PublicStorylineDto): PublicStorylineDto {
  const chapters = Array.isArray(story.chapters)
    ? story.chapters.map((chapter) => ({
        ...chapter,
        contentBlocks: undefined,
        primaryMediaAsset: undefined,
        runtime: undefined,
      }))
    : story.chapters

  return {
    ...story,
    chapters,
    runtime: undefined,
    attachmentAssets: undefined,
  }
}

function toPersistableRuntimeGroup(group: PublicRuntimeGroupDto): PublicRuntimeGroupDto {
  return {
    ...group,
    settings: group.settings,
    items: Array.isArray(group.items) ? group.items.slice(0, 20) : group.items,
  }
}

function buildPersistablePublicContent(content: PublicContentCache): PublicContentCache {
  const runtimeGroups: Record<string, PublicRuntimeGroupDto> = {}
  Object.entries(content.runtimeGroups || {}).forEach(([key, value]) => {
    runtimeGroups[key] = toPersistableRuntimeGroup(value)
  })

  return {
    ...content,
    storylines: content.storylines.map((story) => toPersistableStoryline(story)),
    runtimeGroups,
  }
}

function hasHeavyPersistedPublicContent(content: PublicContentCache) {
  return content.storylines.some((story) => (
    !!story.runtime
    || !!story.attachmentAssets?.length
    || (story.chapters || []).some((chapter) => (
      !!chapter.runtime
      || !!chapter.primaryMediaAsset
      || !!chapter.contentBlocks?.length
    ))
  ))
}

function reloadStoredPublicContent(): PublicContentCache | null {
  try {
    const stored = Taro.getStorageSync(PUBLIC_CONTENT_KEY)
    if (!stored) {
      return null
    }
    if (typeof stored !== 'object' || Array.isArray(stored)) {
      Taro.removeStorageSync(PUBLIC_CONTENT_KEY)
      return null
    }
    return normalizePublicContent(stored)
  } catch (error) {
    console.warn('Failed to reload public content cache from storage.', error)
    return null
  }
}

function contentHasCatalogData(content?: PublicContentCache | null) {
  return !!content && (
    content.cities.length > 0
    || content.subMaps.length > 0
    || content.pois.length > 0
    || content.storylines.length > 0
  )
}

function saveState(nextState: GameStateSnapshot) {
  const normalized = normalizeState(nextState)
  Taro.setStorageSync(STORAGE_KEY, normalized)
  Taro.setStorageSync('userInfo', normalized.user)
  return normalized
}

function loadPublicContent(): PublicContentCache {
  if (publicContentCache) {
    if (!contentHasCatalogData(publicContentCache)) {
      const stored = reloadStoredPublicContent()
      if (contentHasCatalogData(stored)) {
        publicContentCache = stored
      }
    }
    return publicContentCache
  }

  try {
    const stored = reloadStoredPublicContent()
    publicContentCache = stored || createEmptyPublicContent()
    if (hasHeavyPersistedPublicContent(publicContentCache)) {
      Taro.setStorageSync(PUBLIC_CONTENT_KEY, buildPersistablePublicContent(publicContentCache))
    }
  } catch (error) {
    console.warn('Failed to read public content cache.', error)
    publicContentCache = createEmptyPublicContent()
  }

  return publicContentCache
}

function savePublicContent(next: PublicContentCache) {
  const normalized = normalizePublicContent(next)
  publicContentCache = normalized
  try {
    Taro.setStorageSync(PUBLIC_CONTENT_KEY, buildPersistablePublicContent(normalized))
  } catch (error) {
    console.warn('Failed to persist slim public content cache; continuing with memory cache.', error)
  }
  return normalized
}

function mergePublicContent(next: Partial<PublicContentCache>) {
  const current = loadPublicContent()
  return savePublicContent({
    ...current,
    ...next,
    runtimeGroups: {
      ...current.runtimeGroups,
      ...(next.runtimeGroups || {}),
    },
  })
}

function buildPublicContentCounts(content = loadPublicContent()): PublicContentRefreshDiagnostics['counts'] {
  return {
    cities: content.cities.length,
    subMaps: content.subMaps.length,
    pois: content.pois.length,
    storylines: content.storylines.length,
    tips: content.tips.length,
    activities: content.activities.length,
    collectibles: content.collectibles.length,
    badges: content.badges.length,
    rewards: content.rewards.length,
    stamps: content.stamps.length,
    notifications: content.notifications.length,
    discoverCards: content.discoverCards.length,
  }
}

function setPublicContentRefreshDiagnostics(
  next: Partial<PublicContentRefreshDiagnostics> & Pick<PublicContentRefreshDiagnostics, 'status' | 'message'>,
) {
  publicContentRefreshDiagnostics = {
    ...publicContentRefreshDiagnostics,
    ...next,
    apiBaseMode: isPublicApiMockMode() ? 'mock' : 'live',
    refreshedAt: next.refreshedAt || new Date().toISOString(),
    failedSections: next.failedSections || [],
    counts: next.counts || buildPublicContentCounts(),
  }
}

function logPublicContentDiagnostics(context: string, diagnostics = publicContentRefreshDiagnostics) {
  if (!DEV_RUNTIME_DIAGNOSTICS_ENABLED) {
    return
  }

  console.info('[TripOfMacau][public-content]', {
    context,
    apiHost: PUBLIC_API_HOST_LABEL,
    status: diagnostics.status,
    failedSections: diagnostics.failedSections,
    counts: diagnostics.counts,
    refreshedAt: diagnostics.refreshedAt,
  })
}

export function getPublicContentRefreshDiagnostics(): PublicContentRefreshDiagnostics {
  return {
    ...publicContentRefreshDiagnostics,
    failedSections: [...publicContentRefreshDiagnostics.failedSections],
    counts: { ...publicContentRefreshDiagnostics.counts },
  }
}

function reconcileCurrentCatalogSelection(content: PublicContentCache) {
  const state = loadGameState()
  const cityCodes = new Set(content.cities.map((city) => city.code).filter((value) => hasText(value)))
  const fallbackCityCode = content.cities[0]?.code || DEFAULT_UNLOCKED_CITY_ID
  const nextCityId = cityCodes.has(state.user.currentCityId)
    ? state.user.currentCityId
    : fallbackCityCode

  const subMapsForCity = content.subMaps.filter((subMap) => subMap.cityCode === nextCityId)
  const subMapCodes = new Set(subMapsForCity.map((subMap) => subMap.code).filter((value) => hasText(value)))
  const nextSubMapId = state.user.currentSubMapId && subMapCodes.has(state.user.currentSubMapId)
    ? state.user.currentSubMapId
    : subMapsForCity[0]?.code

  const knownUnlocks = new Set((state.cityUnlocks || []).map((item) => item.cityId))
  const nextUnlocks = (state.cityUnlocks || []).slice()
  if (!knownUnlocks.has(nextCityId)) {
    nextUnlocks.push({ cityId: nextCityId, unlockedAt: new Date().toISOString() })
  }

  if (
    state.user.currentCityId !== nextCityId
    || state.user.currentSubMapId !== nextSubMapId
    || nextUnlocks.length !== (state.cityUnlocks || []).length
  ) {
    saveState({
      ...state,
      cityUnlocks: nextUnlocks,
      user: {
        ...state.user,
        currentCityId: nextCityId,
        currentSubMapId: nextSubMapId,
      },
    })
  }
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
    name: remote.preferences.emergencyContactName || '緊急聯絡人',
    phone: remote.preferences.emergencyContactPhone || '',
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
  if (isPublicApiMockMode()) {
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
  if (isPublicApiMockMode()) {
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

function localizeTravelerText(value?: string | null) {
  const text = pickReadableText(value)
  if (!text) {
    return ''
  }
  const replacements: Array<[RegExp, string]> = [
    [/\bOld Town Story Walk\b/gi, '老城故事漫步'],
    [/\bMacau\b/g, '澳門'],
    [/\bCity\b/g, '城市'],
    [/\bExplorer\b/g, '探索者'],
    [/\bStory\b/g, '故事'],
    [/\bSub-map\b/g, '子地圖'],
    [/\bMap zone\b/g, '地圖區域'],
    [/\bStory driven city exploration\b/g, '故事驅動城市探索'],
    [/\bUnlock this stamp during exploration\./g, '在探索中解鎖這枚印章。'],
    [/\bRedeem with (\d+) stamps\./g, '使用 $1 枚印章兌換。'],
    [/\bCheck in\b/gi, '去打卡'],
    [/\bRedeem\b/gi, '去兌換'],
    [/\bView\b/gi, '查看'],
    [/\bRecently\b/gi, '剛剛'],
    [/\b(\d+)\s*min ago\b/gi, '$1 分鐘前'],
    [/\bCity Story Guide\b/gi, '城市故事導覽員'],
    [/\bRoute Explorer\b/gi, '路線探索者'],
    [/\bMacau Walker\b/gi, '澳門漫遊者'],
  ]
  return replacements.reduce((current, [pattern, replacement]) => current.replace(pattern, replacement), text).trim()
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

function findFlagshipStoryDto(content = loadPublicContent()) {
  return content.storylines.find((story) => story.code === FLAGSHIP_STORY_CODE)
    || content.storylines.find((story) => story.code !== LEGACY_DUPLICATE_STORY_CODE && pickReadableText(story.name) === FLAGSHIP_STORY_NAME)
}

function isLegacyDuplicateStoryDto(story: PublicStorylineDto, content = loadPublicContent()) {
  return story.code === LEGACY_DUPLICATE_STORY_CODE
}

function getCanonicalStoryReference(storylineId?: number, storylineCode?: string, storylineName?: string) {
  const content = loadPublicContent()
  const flagship = findFlagshipStoryDto(content)
  if (flagship && (storylineCode === LEGACY_DUPLICATE_STORY_CODE || pickReadableText(storylineName).includes('濠江烽煙'))) {
    return {
      id: flagship.id,
      code: flagship.code,
      name: pickReadableText(flagship.name, FLAGSHIP_STORY_NAME),
    }
  }
  return {
    id: storylineId,
    code: storylineCode,
    name: pickReadableText(storylineName, humanizeCode(storylineCode)),
  }
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
    return '彈性漫步'
  }
  if (minutes < 60) {
    return `${minutes} 分鐘`
  }
  const hours = Math.floor(minutes / 60)
  const remaining = minutes % 60
  return remaining ? `${hours} 小時 ${remaining} 分鐘` : `${hours} 小時`
}

function formatPublishedTime(value?: string) {
  if (!hasText(value)) {
    return '剛剛'
  }
  const timestamp = new Date(value).getTime()
  if (Number.isNaN(timestamp)) {
    return '剛剛'
  }
  const diffMinutes = Math.max(1, Math.floor((Date.now() - timestamp) / 60000))
  if (diffMinutes < 60) {
    return `${diffMinutes} 分鐘前`
  }
  const diffHours = Math.floor(diffMinutes / 60)
  if (diffHours < 24) {
    return `${diffHours} 小時前`
  }
  const diffDays = Math.floor(diffHours / 24)
  if (diffDays < 7) {
    return `${diffDays} 天前`
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
      return '新手攻略'
    case 'slow-travel':
      return '慢遊推薦'
    case 'photo':
      return '拍照秘籍'
    default:
      return pickReadableText(humanizeCode(code), '指南')
  }
}

function resolveCityRewardTitle(cityName: string) {
  return `${cityName}探索者`
}

function buildPoiSubtitle(dto: PublicPoiDto) {
  const storyName = getCanonicalStoryReference(dto.storylineId, dto.storylineCode, dto.storylineName).name
  return pickReadableText(
    dto.subtitle,
    storyName ? `${storyName} 站點` : '',
    humanizeCode(dto.categoryCode),
    '精選路線站點',
  )
}

function buildPoiDescription(dto: PublicPoiDto) {
  const storyName = getCanonicalStoryReference(dto.storylineId, dto.storylineCode, dto.storylineName).name
  const poiName = pickReadableText(dto.name, humanizeCode(dto.code), '探索點')
  return pickReadableText(
    dto.description,
    storyName ? `${poiName}是${storyName}路線的重要站點` : `${poiName}是澳門精選探索點`,
  )
}

function buildPoiTags(dto: PublicPoiDto) {
  const storyName = getCanonicalStoryReference(dto.storylineId, dto.storylineCode, dto.storylineName).name
  const tags = [
    humanizeCode(dto.categoryCode),
    storyName || humanizeCode(dto.storylineCode),
    humanizeCode(dto.cityCode),
  ].filter(Boolean)
  return Array.from(new Set(tags)).slice(0, 3)
}

function buildCollectibleHints(poiName: string, storyName?: string) {
  const liveHints = getCollectibleCatalog()
    .filter((collectible) => {
      const names = [
        ...collectible.relatedStorylines.map((binding) => binding.name),
        ...collectible.relatedCities.map((binding) => binding.name),
        ...collectible.relatedSubMaps.map((binding) => binding.name),
      ]
      const normalizedPoiName = poiName.toLowerCase()
      const normalizedStoryName = storyName?.toLowerCase() || ''
      return names.some((name) => {
        const normalized = name.toLowerCase()
        return normalized.includes(normalizedPoiName) || (!!normalizedStoryName && normalized.includes(normalizedStoryName))
      })
    })
    .map((collectible) => collectible.name)
    .slice(0, 3)
  if (liveHints.length) {
    return liveHints
  }
  return [
    `${poiName}印章`,
    storyName ? `${storyName}紀念品` : '路線紀念品',
    '旅人照片筆記',
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
      const canonicalStory = getCanonicalStoryReference(dto.storylineId, dto.storylineCode, dto.storylineName)
      const storyName = canonicalStory.name
      const name = pickReadableText(dto.name, humanizeCode(dto.code), `POI ${dto.id}`)
      const relatedStamp = selectStampForPoi(dto.id, dto.storylineId)
      return {
        id: dto.id,
        code: dto.code,
        name,
        subtitle: buildPoiSubtitle(dto),
        icon: resolvePoiIcon(dto.categoryCode),
        latitude: dto.latitude,
        longitude: dto.longitude,
        address: pickReadableText(dto.address, name),
        geofenceRadius: dto.manualCheckinRadius || dto.triggerRadius || 200,
        triggerRadius: dto.triggerRadius || 50,
        difficulty: sanitizeDifficulty(dto.difficulty),
        category: pickReadableText(humanizeCode(dto.categoryCode), '探索點'),
        district: pickReadableText(humanizeCode(dto.district), humanizeCode(dto.cityCode), '澳門'),
        storyLineId: canonicalStory.id,
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
        coverImageUrl: dto.coverImageUrl,
        introTitle: pickReadableText(dto.introTitle, name),
        introSummary: pickReadableText(dto.introSummary, dto.description, `在${name}跟隨路線故事`),
        indoorMapTitle: `${name}指南`,
        indoorMapHint: `使用附近的路線和故事面板規劃您在${name}的停留`,
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
        ...reward,
        id: reward.id,
        name: pickReadableText(reward.name, humanizeCode(reward.code), `獎勵 ${reward.id}`),
        subtitle: pickReadableText(reward.subtitle, '可兌換獎勵'),
        icon: resolveRewardIcon(reward.code),
        stampCost: reward.stampCost,
        inventory: availableInventory,
        status: availableInventory > 0 ? 'available' as const : 'coming_soon' as const,
        description: pickReadableText(reward.description, '收集印章解鎖此獎勵'),
        highlight: localizeTravelerText(pickReadableText(reward.highlight, `使用 ${reward.stampCost} 枚印章兌換。`)),
        relatedStorylines: reward.relatedStorylines || [],
        relatedCities: reward.relatedCities || [],
        relatedSubMaps: reward.relatedSubMaps || [],
        relatedIndoorBuildings: reward.relatedIndoorBuildings || [],
        relatedIndoorFloors: reward.relatedIndoorFloors || [],
        attachmentAssetUrls: reward.attachmentAssetUrls || [],
      }
    })
}

function firstRelationName(bindings?: Array<{ name: string; code: string }>) {
  if (!Array.isArray(bindings) || !bindings.length) {
    return ''
  }
  return pickReadableText(bindings[0].name, humanizeCode(bindings[0].code))
}

function getActivityCatalog() {
  return loadPublicContent().activities
    .slice()
    .sort((left, right) => {
      const leftPinned = left.isPinned || 0
      const rightPinned = right.isPinned || 0
      if (leftPinned !== rightPinned) {
        return rightPinned - leftPinned
      }
      return (left.sortOrder || 0) - (right.sortOrder || 0)
    })
}

function getCollectibleCatalog() {
  return loadPublicContent().collectibles
    .slice()
    .sort((left, right) => (left.sortOrder || 0) - (right.sortOrder || 0))
    .map((collectible) => ({
      ...collectible,
      name: pickReadableText(collectible.name, humanizeCode(collectible.code), `Collectible ${collectible.id}`),
      relatedStorylines: collectible.relatedStorylines || [],
      relatedCities: collectible.relatedCities || [],
      relatedSubMaps: collectible.relatedSubMaps || [],
      relatedIndoorBuildings: collectible.relatedIndoorBuildings || [],
      relatedIndoorFloors: collectible.relatedIndoorFloors || [],
      attachmentAssetUrls: collectible.attachmentAssetUrls || [],
    }))
}

function getBadgeCatalog() {
  return loadPublicContent().badges
    .slice()
    .map((badge) => ({
      ...badge,
      name: pickReadableText(badge.name, humanizeCode(badge.code), `Badge ${badge.id}`),
      relatedStorylines: badge.relatedStorylines || [],
      relatedCities: badge.relatedCities || [],
      relatedSubMaps: badge.relatedSubMaps || [],
      relatedIndoorBuildings: badge.relatedIndoorBuildings || [],
      relatedIndoorFloors: badge.relatedIndoorFloors || [],
      attachmentAssetUrls: badge.attachmentAssetUrls || [],
    }))
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
  const contentBlocks = Array.isArray(chapter.contentBlocks)
    ? chapter.contentBlocks
      .slice()
      .sort((left, right) => (left.sortOrder || 0) - (right.sortOrder || 0))
      .map((block) => mapStoryContentBlock(block))
    : []

  return {
    id: chapter.id,
    title: pickReadableText(chapter.title, `Chapter ${index + 1}`),
    summary: pickReadableText(chapter.summary, 'Continue the route to reveal the next beat.'),
    detail: pickReadableText(chapter.detail, chapter.summary, 'Explore the mapped stop to continue this story.'),
    achievement: pickReadableText(chapter.achievement, 'Unlock a new milestone.'),
    collectible: pickReadableText(chapter.collectible, 'Story keepsake'),
    locationName: pickReadableText(chapter.locationName, 'Macau'),
    anchorType: chapter.anchorType,
    anchorTargetId: chapter.anchorTargetId,
    anchorTargetCode: chapter.anchorTargetCode,
    primaryMediaUrl: chapter.primaryMediaUrl || chapter.mediaUrl,
    primaryMediaAsset: mapStoryMediaAsset(chapter.primaryMediaAsset),
    unlock: mapStoryRulePayload(chapter.unlock),
    prerequisite: mapStoryRulePayload(chapter.prerequisite, chapter.prerequisiteJson),
    completion: mapStoryRulePayload(chapter.completion, chapter.completionJson),
    effect: mapStoryRulePayload(chapter.effect, chapter.rewardJson),
    contentBlocks,
    prerequisiteJson: chapter.prerequisiteJson,
    completionJson: chapter.completionJson,
    rewardJson: chapter.rewardJson,
    locked: !storyUnlocked || index > completedChapters,
  }
}

function getRuntimeStepsFromChapter(chapter: StoryChapterItem) {
  return (chapter.runtimeSteps || chapter.runtime?.runtimeSteps || [])
}

function mapRuntimeStep(step: PublicExperienceRuntimeStepDto): StoryRuntimeStepItem {
  return {
    id: step.id,
    flowId: step.flowId,
    stepCode: step.stepCode,
    stepType: step.stepType,
    displayCategory: step.displayCategory,
    displayCategoryLabel: step.displayCategoryLabel,
    unsupported: !!step.unsupported,
    unsupportedReason: step.unsupportedReason,
    travelerActionLabel: step.travelerActionLabel,
    eventType: step.eventType,
    elementCode: step.elementCode,
    elementId: step.elementId,
    name: pickReadableText(step.name, step.template?.name, humanizeCode(step.stepCode), humanizeCode(step.stepType), '故事互動'),
    description: pickReadableText(step.description, step.template?.summary, '依照故事提示完成這一步互動。'),
    triggerType: step.triggerType,
    triggerConfig: step.triggerConfig || undefined,
    conditionConfig: step.conditionConfig || undefined,
    effectConfig: step.effectConfig || undefined,
    mediaAssetId: step.mediaAssetId,
    mediaAsset: mapStoryMediaAsset(step.mediaAsset),
    rewardRuleIds: step.rewardRuleIds,
    explorationWeightLevel: step.explorationWeightLevel,
    explorationWeightValue: step.explorationWeightValue,
    requiredForCompletion: !!step.requiredForCompletion,
    inheritKey: step.inheritKey,
    template: step.template
      ? {
          id: step.template.id,
          code: step.template.code,
          templateType: step.template.templateType,
          category: step.template.category,
          name: step.template.name,
          summary: step.template.summary,
          riskLevel: step.template.riskLevel,
        }
      : undefined,
    sortOrder: step.sortOrder,
  }
}

function mapChapterRuntime(runtime: PublicStoryChapterRuntimeDto): StoryChapterRuntimeItem {
  const runtimeSteps = Array.isArray(runtime.compiledSteps)
    ? runtime.compiledSteps
      .slice()
      .sort((left, right) => (left.sortOrder || 0) - (right.sortOrder || 0))
      .map((step) => mapRuntimeStep(step))
    : []

  return {
    chapterId: runtime.chapterId,
    chapterOrder: runtime.chapterOrder,
    runtimeStatus: runtime.runtimeStatus,
    runtimeStatusLabel: runtime.runtimeStatusLabel,
    compiledStepCount: runtime.compiledStepCount,
    unsupportedStepCount: runtime.unsupportedStepCount,
    anchorType: runtime.anchorType,
    anchorTargetId: runtime.anchorTargetId,
    anchorTargetCode: runtime.anchorTargetCode,
    storyModeConfig: runtime.storyModeConfig as Record<string, unknown> | undefined,
    runtimeSteps,
  }
}

function mapStorylineRuntime(runtime: PublicStorylineRuntimeDto): StorylineRuntimeItem {
  return {
    runtimeVersion: runtime.runtimeVersion,
    source: runtime.source,
    generatedAt: runtime.generatedAt,
    publishedChapterCount: runtime.publishedChapterCount,
    unsupportedStepCount: runtime.unsupportedStepCount,
    storyModeConfig: runtime.storyModeConfig as Record<string, unknown> | undefined,
    chapters: Array.isArray(runtime.chapters)
      ? runtime.chapters
        .slice()
        .sort((left, right) => (left.chapterOrder || 0) - (right.chapterOrder || 0))
        .map((chapter) => mapChapterRuntime(chapter))
      : [],
  }
}

function parsePowerShellObjectLiteral(value: string): Record<string, any> | null {
  const trimmed = value.trim()
  if (!trimmed.startsWith('@{') || !trimmed.endsWith('}')) {
    return null
  }

  const body = trimmed.slice(2, -1)
  const output: Record<string, any> = {}
  body.split(';').forEach((segment) => {
    const separatorIndex = segment.indexOf('=')
    if (separatorIndex <= 0) {
      return
    }
    const key = segment.slice(0, separatorIndex).trim()
    const rawValue = segment.slice(separatorIndex + 1).trim()
    if (!key) {
      return
    }
    if (rawValue === '') {
      output[key] = undefined
      return
    }
    if (rawValue === 'True' || rawValue === 'true') {
      output[key] = true
      return
    }
    if (rawValue === 'False' || rawValue === 'false') {
      output[key] = false
      return
    }
    const numericValue = Number(rawValue)
    output[key] = Number.isFinite(numericValue) && String(numericValue) === rawValue
      ? numericValue
      : rawValue
  })
  return output
}

function normalizeStoryMediaAssetDto(asset?: LoosePublicStoryMediaAssetDto): PublicStoryMediaAssetDto | undefined {
  if (!asset) {
    return undefined
  }
  if (typeof asset === 'string') {
    const parsed = parsePowerShellObjectLiteral(asset)
    if (!parsed) {
      return undefined
    }
    return normalizeStoryMediaAssetDto(parsed)
  }
  if (typeof asset !== 'object') {
    return undefined
  }

  return asset as PublicStoryMediaAssetDto
}

function mapStoryMediaAsset(asset?: LoosePublicStoryMediaAssetDto): StoryMediaAssetItem | undefined {
  const normalized = normalizeStoryMediaAssetDto(asset)
  if (!normalized) {
    return undefined
  }

  return {
    id: Number(normalized.id || 0),
    assetKind: normalized.assetKind,
    url: normalized.url,
    mimeType: normalized.mimeType,
    originalFilename: normalized.originalFilename,
    widthPx: normalized.widthPx,
    heightPx: normalized.heightPx,
    animationSubtype: normalized.animationSubtype,
    defaultLoop: normalized.defaultLoop,
    defaultAutoplay: normalized.defaultAutoplay,
    posterAssetId: normalized.posterAssetId,
    posterUrl: normalized.posterUrl,
    fallbackAssetId: normalized.fallbackAssetId,
    fallbackUrl: normalized.fallbackUrl,
    availability: normalized.availability,
    unavailableReason: normalized.unavailableReason,
    fallbackUsed: normalized.fallbackUsed,
    runtimeKind: normalized.runtimeKind,
    fileSizeBytes: normalized.fileSizeBytes,
    durationMs: normalized.durationMs,
    usageHint: normalized.usageHint
      ? {
          materialItemKey: normalized.usageHint.materialItemKey,
          usageTarget: normalized.usageHint.usageTarget,
          chapterCode: normalized.usageHint.chapterCode,
          targetType: normalized.usageHint.targetType,
          targetCode: normalized.usageHint.targetCode,
          displayRole: normalized.usageHint.displayRole,
          sourceScope: normalized.usageHint.sourceScope,
        }
      : undefined,
  }
}

export function resolveStoryMediaUrl(asset?: StoryMediaAssetItem | null): string {
  return asset?.url || asset?.fallbackUrl || asset?.posterUrl || ''
}

export function getStoryMediaFallbackReason(
  asset?: StoryMediaAssetItem | null,
  fallback = '媒體資源暫時未能載入',
): string {
  if (hasText(asset?.unavailableReason)) {
    return asset.unavailableReason.trim()
  }
  if (asset?.availability === 'unsupported') {
    return '此媒體暫時未能播放，請稍後再試。'
  }
  return fallback
}

export function isStoryMediaPlayable(asset?: StoryMediaAssetItem | null): boolean {
  return asset?.availability !== 'unsupported' && hasText(resolveStoryMediaUrl(asset))
}

function mapStoryContentBlock(block: PublicStoryContentBlockDto): StoryContentBlockItem {
  return {
    id: block.id,
    code: block.code,
    blockType: block.blockType,
    title: block.title,
    summary: block.summary,
    body: block.body,
    stylePreset: block.stylePreset,
    displayMode: block.displayMode,
    visibilityJson: block.visibilityJson,
    displayConditionJson: block.displayConditionJson,
    configJson: block.configJson,
    sortOrder: block.sortOrder,
    primaryAsset: mapStoryMediaAsset(block.primaryAsset as LoosePublicStoryMediaAssetDto),
    attachmentAssets: Array.isArray(block.attachmentAssets)
      ? block.attachmentAssets.map((asset) => mapStoryMediaAsset(asset as LoosePublicStoryMediaAssetDto)).filter(Boolean) as StoryMediaAssetItem[]
      : [],
  }
}

function mapStoryRulePayload(
  payload?: PublicStoryChapterUnlockDto | PublicStoryChapterConditionDto | PublicStoryChapterEffectDto | null,
  rawJson?: string | null,
): StoryRulePayload | undefined {
  if (!payload && !rawJson) {
    return undefined
  }

  return {
    type: payload?.type,
    config: payload?.config,
    rawJson: payload?.rawJson || rawJson || undefined,
  }
}

function getStoryCityBindingCodes(story: PublicStorylineDto) {
  const bindingCodes = Array.isArray(story.cityBindings)
    ? story.cityBindings.map((binding) => binding.code).filter((value) => hasText(value))
    : []
  if (bindingCodes.length) {
    return bindingCodes
  }
  return hasText(story.cityCode) ? [story.cityCode] : [DEFAULT_UNLOCKED_CITY_ID]
}

function getStorySubMapBindingCodes(story: PublicStorylineDto) {
  return Array.isArray(story.subMapBindings)
    ? story.subMapBindings.map((binding) => binding.code).filter((value) => hasText(value))
    : []
}

function inferCityByLocation(lat: number, lng: number) {
  const candidate = loadPublicContent().cities
    .map((city) => ({
      code: city.code,
      distanceMeters: calculateDistance(lat, lng, city.centerLat || AMAP_CONFIG.defaultCenter.latitude, city.centerLng || AMAP_CONFIG.defaultCenter.longitude),
    }))
    .sort((left, right) => left.distanceMeters - right.distanceMeters)[0]

  return candidate?.code || loadGameState().user.currentCityId || DEFAULT_UNLOCKED_CITY_ID
}

function getStoryCatalog(state = loadGameState()) {
  const pois = getPoiCatalog()
  const unlockedCities = new Set(getCities().filter((city) => city.unlocked).map((city) => city.id))
  const content = loadPublicContent()

  return content.storylines
    .slice()
    .filter((story) => !isLegacyDuplicateStoryDto(story, content))
    .sort((left, right) => (left.sortOrder || 0) - (right.sortOrder || 0))
    .map((story) => {
      const storyPoiIds = pois.filter((poi) => poi.storyLineId === story.id).map((poi) => poi.id)
      const cityBindingCodes = getStoryCityBindingCodes(story)
      const subMapBindingCodes = getStorySubMapBindingCodes(story)
      const primaryCityCode = cityBindingCodes[0] || DEFAULT_UNLOCKED_CITY_ID
      const storyUnlocked = cityBindingCodes.some((cityCode) => unlockedCities.has(cityCode))
        || state.completedStoryIds.includes(story.id)
        || primaryCityCode === DEFAULT_UNLOCKED_CITY_ID
      const chapters = Array.isArray(story.chapters) ? story.chapters : []
      const completedChapters = chapters.filter((chapter) => state.completedChapterIds.includes(chapter.id)).length
      const totalChapters = story.totalChapters || chapters.length || 1
      const mappedChapters = chapters.map((chapter, index) => mapStoryChapter(chapter, index, storyUnlocked, completedChapters))
      const runtime = story.runtime ? mapStorylineRuntime(story.runtime) : undefined
      const runtimeChaptersById = new Map((runtime?.chapters || []).map((chapter) => [chapter.chapterId, chapter]))
      const runtimeAwareChapters = mappedChapters.map((chapter) => {
        const chapterRuntime = runtimeChaptersById.get(chapter.id)
        return chapterRuntime
          ? {
              ...chapter,
              runtime: chapterRuntime,
              runtimeSteps: chapterRuntime.runtimeSteps,
              runtimeStatus: chapterRuntime.runtimeStatus,
            }
          : chapter
      })
      return {
        id: story.id,
        code: story.code,
        name: pickReadableText(story.name, story.nameEn, humanizeCode(story.code), `Story ${story.id}`),
        nameEn: pickReadableText(story.nameEn, story.name, humanizeCode(story.code), `Story ${story.id}`),
        description: localizeTravelerText(pickReadableText(story.description, `${pickReadableText(story.name, story.nameEn, '這條路線')}串連澳門主要故事地點。`)),
        icon: resolveStoryIcon(story.code),
        coverColor: colorFromKey(story.code || String(story.id)),
        coverImageUrl: story.coverImageUrl,
        bannerImageUrl: story.bannerImageUrl,
        attachmentAssets: Array.isArray(story.attachmentAssets)
          ? story.attachmentAssets.map((asset) => mapStoryMediaAsset(asset)).filter(Boolean) as StoryMediaAssetItem[]
          : [],
        totalChapters,
        completedChapters,
        estimatedTime: formatMinutes(story.estimatedMinutes),
        difficulty: sanitizeDifficulty(story.difficulty),
        poiIds: storyPoiIds,
        chapterTitles: runtimeAwareChapters.map((chapter) => chapter.title),
        progress: Math.round((completedChapters / totalChapters) * 100),
        rewardBadge: localizeTravelerText(pickReadableText(story.rewardBadge, `${pickReadableText(story.name, story.nameEn, '故事')}徽章`)),
        locked: !storyUnlocked,
        unlockHint: storyUnlocked ? '' : `探索${localizeTravelerText(humanizeCode(primaryCityCode))}後解鎖這條主線。`,
        chapters: runtimeAwareChapters,
        cityBindingCodes,
        subMapBindingCodes,
        runtime,
        runtimeSyncedAt: story.runtimeSyncedAt,
        runtimeSource: story.runtimeSource,
        runtimeStatusText: story.runtimeStatusText,
        moodTags: [
          sanitizeDifficulty(story.difficulty),
          localizeTravelerText(humanizeCode(primaryCityCode)),
          localizeTravelerText(humanizeCode(story.code)),
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
        title: pickReadableText(notification.title, `更新 ${notification.id}`),
        content: pickReadableText(notification.content, '有新的旅人更新可用'),
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
      title: pickReadableText(tip.title, humanizeCode(tip.code), `秘笈 ${tip.id}`),
      summary: pickReadableText(tip.summary, '旅人路線指南'),
      coverColor: colorFromKey(tip.code || String(tip.id)),
      category: resolveCategoryLabel(tip.categoryCode),
      author: pickReadableText(tip.authorDisplayName, '澳門之旅'),
      likes: 0,
      saves: 0,
      readMinutes: Math.max(3, (tip.contentParagraphs || []).length * 2),
      tags: Array.isArray(tip.tags) ? tip.tags : [],
      imageUrl: tip.coverImageUrl || undefined,
      locationName: pickReadableText(tip.locationName, humanizeCode(tip.cityCode), '澳門'),
      contentParagraphs: Array.isArray(tip.contentParagraphs) ? tip.contentParagraphs : [],
      createdAt: tip.publishedAt,
    }))

  return [...(state.publishedTips || []), ...liveTips]
}

function getLiveDiscoverCards() {
  const activitiesById = new Map(getActivityCatalog().map((item) => [item.id, item]))
  const rewardsById = new Map(loadPublicContent().rewards.map((item) => [item.id, item]))
  const poisById = new Map(loadPublicContent().pois.map((item) => [item.id, item]))

  return loadPublicContent().discoverCards.map((card) => {
    const activity = card.sourceType === 'activity' && card.sourceId ? activitiesById.get(card.sourceId) : undefined
    const reward = card.sourceType === 'reward' && card.sourceId ? rewardsById.get(card.sourceId) : undefined
    const poi = card.sourceType === 'poi' && card.sourceId ? poisById.get(card.sourceId) : undefined

    return {
      id: card.id,
      title: pickReadableText(
        card.title,
        activity?.title,
        reward?.name,
        poi?.name,
        `探索 ${card.sourceId || card.id}`,
      ),
      subtitle: pickReadableText(
        card.subtitle,
        activity?.venueName,
        activity?.activityType,
        reward?.subtitle,
        poi?.subtitle,
        humanizeCode(card.sourceType),
        humanizeCode(card.type),
      ),
      description: pickReadableText(
        card.description,
        activity?.summary,
        activity?.description,
        reward?.description,
        poi?.description,
        '探索澳門精選體驗',
      ),
      tag: pickReadableText(
        card.tag,
        activity?.isPinned ? '精選' : '',
        humanizeCode(activity?.activityType),
        humanizeCode(card.type),
        '精選',
      ),
      icon: pickReadableText(
        card.icon,
        card.type === 'activity' ? '🌃' : card.type === 'merchant' ? '🎟️' : '🔥',
      ),
      type: card.type,
      district: pickReadableText(
        card.district,
        firstRelationName(activity?.subMapBindings),
        firstRelationName(activity?.cityBindings),
        firstRelationName(reward?.relatedSubMaps),
        firstRelationName(reward?.relatedCities),
        poi?.district,
        activity?.venueName,
        'Macau',
      ),
      actionText: pickReadableText(
        card.actionText,
        card.type === 'merchant' ? '去兌換' : card.type === 'checkin' ? '去打卡' : '查看',
      ),
      coverColor: pickReadableText(card.coverColor, colorFromKey(String(card.id))),
    }
  })
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

function mergeStorylineRuntimeIntoCache(
  storylineId: number,
  runtime: PublicStorylineRuntimeDto,
) {
  const existing = loadPublicContent()
  const statusText = '即時故事資料已同步'
  const syncedAt = new Date().toISOString()
  const runtimeStoryline = runtime.storyline
  const hasExistingStoryline = existing.storylines.some((story) => story.id === storylineId)
  const nextStorylines = hasExistingStoryline
    ? existing.storylines.map((story) => {
        if (story.id !== storylineId) {
          return story
        }
        return {
          ...story,
          ...(runtimeStoryline || {}),
          chapters: runtimeStoryline?.chapters || story.chapters,
          runtime,
          runtimeSyncedAt: syncedAt,
          runtimeSource: 'live',
          runtimeStatusText: statusText,
        }
      })
    : runtimeStoryline
      ? [
          ...existing.storylines,
          {
            ...runtimeStoryline,
            runtime,
            runtimeSyncedAt: syncedAt,
            runtimeSource: 'live',
            runtimeStatusText: statusText,
          },
        ]
      : existing.storylines

  savePublicContent({
    ...existing,
    storylines: nextStorylines,
  })

  return getStoryById(storylineId)
}

export async function refreshPublicContent(locale: PublicLocaleCode = (loadGameState().user.localeCode as PublicLocaleCode) || DEFAULT_PUBLIC_LOCALE) {
  if (isPublicApiMockMode()) {
    setPublicContentRefreshDiagnostics({
      status: 'success',
      message: '目前使用本機示例內容。',
    })
    return loadPublicContent()
  }

  if (hasActiveSessionToken() && loadGameState().user.authStatus !== 'anonymous') {
    void syncUserStateFromServer().catch((error) => {
      console.warn('Failed to sync user state before public content refresh.', error)
    })
  }

  const [
    cities,
    subMaps,
    pois,
    storylines,
    tips,
    activities,
    collectibles,
    badges,
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
    api.public.getPublicActivities(locale),
    api.public.getPublicCollectibles(locale),
    api.public.getPublicBadges(locale),
    api.public.getPublicRewards(locale),
    api.public.getPublicStamps(locale),
    api.public.getPublicNotifications(locale),
    api.public.getPublicDiscoverCards(locale),
    api.public.getPublicRuntimeGroup('discover', locale),
    api.public.getPublicRuntimeGroup('map', locale),
    api.public.getPublicRuntimeGroup('travel', locale),
  ])

  const saved = savePublicContent({
    locale,
    updatedAt: new Date().toISOString(),
    cities,
    subMaps,
    pois,
    storylines,
    tips,
    activities,
    collectibles,
    badges,
    rewards,
    stamps,
    notifications,
    discoverCards,
    runtimeGroups: {
      discover: discoverRuntime,
      map: mapRuntime,
      travel: travelRuntime,
    },
  })
  reconcileCurrentCatalogSelection(saved)

  setPublicContentRefreshDiagnostics({
    status: 'success',
    message: '公開內容已同步。',
    failedSections: [],
    counts: buildPublicContentCounts(saved),
  })
  logPublicContentDiagnostics('refreshPublicContent')

  if (!saved.storylines.length) {
    throw new Error('故事內容暫時未能載入。')
  }

  return saved
}

export async function refreshStorylineCatalog(
  locale: PublicLocaleCode = (loadGameState().user.localeCode as PublicLocaleCode) || DEFAULT_PUBLIC_LOCALE,
) {
  if (isPublicApiMockMode()) {
    return getStorylines()
  }

  const existing = loadPublicContent()
  const storylines = await api.public.getPublicStorylines(locale)
  const saved = mergePublicContent({
    locale,
    updatedAt: new Date().toISOString(),
    storylines,
  })

  setPublicContentRefreshDiagnostics({
    status: 'success',
    message: '故事內容已同步。',
    failedSections: publicContentRefreshDiagnostics.failedSections.filter((section) => section !== 'storylines'),
    counts: buildPublicContentCounts(saved),
  })
  logPublicContentDiagnostics('refreshStorylineCatalog')

  return getStorylines()
}

export function isStorylineUnavailableError(error: unknown) {
  return isPublicApiError(error, 4042)
    || (error instanceof Error && /Storyline not found/i.test(error.message))
}

export function clearStaleStorylineRuntimeSelection(storylineId: number) {
  const numericStorylineId = Number(storylineId)
  if (!Number.isFinite(numericStorylineId)) {
    return
  }
  const state = loadGameState()
  if (Number(state.activeStoryId) === numericStorylineId) {
    saveState({
      ...state,
      activeStoryId: undefined,
    })
  }
  const activeSession = getActiveStoryModeSession(numericStorylineId)
  if (activeSession) {
    saveActiveStoryModeSession(null)
  }
  const routeContext = getStoryModeRouteContext()
  if (routeContext?.storylineId === numericStorylineId) {
    clearStoryModeRouteContext()
  }
}

export async function refreshStorylineRuntime(
  storylineId: number,
  locale: PublicLocaleCode = (loadGameState().user.localeCode as PublicLocaleCode) || DEFAULT_PUBLIC_LOCALE,
): Promise<StorylineItem | undefined> {
  if (isPublicApiMockMode()) {
    return getStoryById(storylineId)
  }

  try {
    const runtime = await api.public.getPublicStorylineRuntime(storylineId, locale)
    return mergeStorylineRuntimeIntoCache(storylineId, runtime)
  } catch (error) {
    console.warn('Failed to refresh storyline runtime.', error)
    if (isStorylineUnavailableError(error)) {
      clearStaleStorylineRuntimeSelection(storylineId)
    }
    throw error
  }
}

export function getStorylineRuntime(storylineId: number): StorylineRuntimeItem | undefined {
  return getStoryById(storylineId)?.runtime
}

function mapStorylineSession(session: PublicStorylineSessionDto): StorySessionItem {
  return {
    storylineId: session.storylineId,
    sessionId: session.sessionId,
    currentChapterId: session.currentChapterId,
    status: session.status,
    startedAt: session.startedAt,
    lastEventAt: session.lastEventAt,
    exitedAt: session.exitedAt,
    eventCount: session.eventCount,
    exitClearedTemporaryState: session.exitClearedTemporaryState,
  }
}

export async function startStorylineRuntimeSession(storylineId: number): Promise<StorySessionItem | undefined> {
  if (isPublicApiMockMode() || !hasActiveSessionToken() || loadGameState().user.authStatus === 'anonymous') {
    return undefined
  }
  const session = await api.public.startPublicStorylineSession(storylineId)
  return mapStorylineSession(session)
}

export async function exitStorylineRuntimeSession(storylineId: number, sessionId: string): Promise<StorySessionItem | undefined> {
  if (isPublicApiMockMode() || !hasActiveSessionToken() || loadGameState().user.authStatus === 'anonymous') {
    return undefined
  }
  const session = await api.public.exitPublicStorylineSession(storylineId, sessionId)
  return mapStorylineSession(session)
}

export function getActiveStoryModeSession(storylineId?: number): StoryModeSessionState | null {
  try {
    const stored = Taro.getStorageSync(STORY_MODE_SESSION_KEY)
    if (!stored || typeof stored !== 'object') {
      return null
    }
    const session = stored as StoryModeSessionState
    if (!session.active || !Number.isFinite(Number(session.storylineId))) {
      return null
    }
    if (storylineId && Number(session.storylineId) !== Number(storylineId)) {
      return null
    }
    return session
  } catch (error) {
    console.warn('Failed to read story mode session.', error)
    return null
  }
}

export function saveActiveStoryModeSession(session: StoryModeSessionState | null): StoryModeSessionState | null {
  if (!session) {
    Taro.removeStorageSync(STORY_MODE_SESSION_KEY)
    return null
  }
  Taro.setStorageSync(STORY_MODE_SESSION_KEY, session)
  return session
}

function toPositiveInteger(value: unknown): number | undefined {
  const numeric = Number(value)
  if (!Number.isFinite(numeric)) {
    return undefined
  }
  const integer = Math.trunc(numeric)
  return integer > 0 ? integer : undefined
}

function readText(value: unknown): string | undefined {
  return typeof value === 'string' && value.trim().length ? value.trim() : undefined
}

function sanitizeStoryModeRouteChapter(value: unknown): StoryModeRouteChapter | null {
  const raw = value as Partial<StoryModeRouteChapter> | null | undefined
  const chapterId = toPositiveInteger(raw?.chapterId)
  const chapterOrder = toPositiveInteger(raw?.chapterOrder) || chapterId
  const title = readText(raw?.title)
  if (!chapterId || !title) {
    return null
  }
  const status = raw?.status === 'completed' || raw?.status === 'current' || raw?.status === 'locked'
    ? raw.status
    : 'inactive'
  return {
    chapterId,
    chapterOrder,
    title,
    summary: readText(raw?.summary),
    locationName: readText(raw?.locationName),
    anchorType: readText(raw?.anchorType),
    anchorTargetId: toPositiveInteger(raw?.anchorTargetId),
    anchorTargetCode: readText(raw?.anchorTargetCode),
    latitude: Number.isFinite(raw?.latitude) ? Number(raw?.latitude) : undefined,
    longitude: Number.isFinite(raw?.longitude) ? Number(raw?.longitude) : undefined,
    status,
  }
}

function sanitizeStoryModeRouteContext(value: unknown): StoryModeRouteContext | null {
  const raw = value as Partial<StoryModeRouteContext> | null | undefined
  const storylineId = toPositiveInteger(raw?.storylineId)
  const storylineName = readText(raw?.storylineName)
  if (!storylineId || !storylineName || raw?.source !== 'story_page' || !Array.isArray(raw?.chapters)) {
    return null
  }
  const chapters = raw.chapters
    .map((chapter) => sanitizeStoryModeRouteChapter(chapter))
    .filter(Boolean) as StoryModeRouteChapter[]
  if (!chapters.length) {
    return null
  }
  const currentChapterId = toPositiveInteger(raw.currentChapterId)
  const currentChapter = chapters.find((chapter) => chapter.chapterId === currentChapterId)
    || chapters.find((chapter) => chapter.status === 'current')
    || chapters.find((chapter) => chapter.status !== 'locked')
    || chapters[0]
  return {
    storylineId,
    storylineName,
    sessionId: readText(raw.sessionId),
    currentChapterId: currentChapter?.chapterId,
    currentChapterTitle: readText(raw.currentChapterTitle) || currentChapter?.title,
    currentDestinationName: readText(raw.currentDestinationName) || currentChapter?.locationName,
    currentAnchorType: readText(raw.currentAnchorType) || currentChapter?.anchorType,
    currentAnchorTargetId: toPositiveInteger(raw.currentAnchorTargetId) || currentChapter?.anchorTargetId,
    currentAnchorTargetCode: readText(raw.currentAnchorTargetCode) || currentChapter?.anchorTargetCode,
    chapters,
    source: 'story_page',
    savedAt: readText(raw.savedAt) || new Date().toISOString(),
  }
}

export function buildStoryModeRouteContext(
  story: StorylineItem,
  currentChapterId?: number,
  sessionId?: string,
): StoryModeRouteContext {
  const state = loadGameState()
  const completedChapterIds = new Set(state.completedChapterIds || [])
  const safeStorylineId = toPositiveInteger(story.id) || 0
  const safeCurrentChapterId = toPositiveInteger(currentChapterId)
  const sortedChapters = (story.chapters || [])
    .slice()
    .sort((left, right) => {
      const leftOrder = toPositiveInteger(left.runtime?.chapterOrder) || toPositiveInteger(left.id) || 0
      const rightOrder = toPositiveInteger(right.runtime?.chapterOrder) || toPositiveInteger(right.id) || 0
      return leftOrder - rightOrder
    })
  const fallbackCurrent = sortedChapters.find((chapter) => !chapter.locked) || sortedChapters[0]
  const selectedChapterId = safeCurrentChapterId || toPositiveInteger(fallbackCurrent?.id)
  const chapters = sortedChapters
    .map((chapter, index) => {
      const chapterId = toPositiveInteger(chapter.id)
      if (!chapterId) {
        return null
      }
      const chapterOrder = toPositiveInteger(chapter.runtime?.chapterOrder) || index + 1
      const runtimeAnchorTargetId = toPositiveInteger(chapter.runtime?.anchorTargetId)
      const chapterAnchorTargetId = toPositiveInteger(chapter.anchorTargetId)
      const anchorTargetId = runtimeAnchorTargetId || chapterAnchorTargetId
      const anchorPoi = anchorTargetId ? getPoiById(anchorTargetId) : null
      const status: StoryModeRouteChapter['status'] = chapter.locked
        ? 'locked'
        : chapterId === selectedChapterId
          ? 'current'
          : completedChapterIds.has(chapterId)
            ? 'completed'
            : 'inactive'
      return {
        chapterId,
        chapterOrder,
        title: pickReadableText(chapter.title, `第 ${chapterOrder} 章`),
        summary: pickReadableText(chapter.summary),
        locationName: pickReadableText(chapter.locationName),
        anchorType: pickReadableText(chapter.runtime?.anchorType, chapter.anchorType),
        anchorTargetId,
        anchorTargetCode: pickReadableText(chapter.runtime?.anchorTargetCode, chapter.anchorTargetCode),
        latitude: anchorPoi?.latitude,
        longitude: anchorPoi?.longitude,
        status,
      }
    })
    .filter(Boolean) as StoryModeRouteChapter[]
  const currentChapter = chapters.find((chapter) => chapter.status === 'current')
    || chapters.find((chapter) => chapter.status !== 'locked')
    || chapters[0]

  return {
    storylineId: safeStorylineId,
    storylineName: pickReadableText(story.name, story.nameEn, story.code, `Story ${safeStorylineId}`),
    sessionId: readText(sessionId),
    currentChapterId: currentChapter?.chapterId,
    currentChapterTitle: currentChapter?.title,
    currentDestinationName: currentChapter?.locationName,
    currentAnchorType: currentChapter?.anchorType,
    currentAnchorTargetId: currentChapter?.anchorTargetId,
    currentAnchorTargetCode: currentChapter?.anchorTargetCode,
    chapters,
    source: 'story_page',
    savedAt: new Date().toISOString(),
  }
}

export function saveStoryModeRouteContext(context: StoryModeRouteContext | null): StoryModeRouteContext | null {
  if (!context) {
    Taro.removeStorageSync(STORY_MODE_ROUTE_CONTEXT_KEY)
    return null
  }
  const sanitized = sanitizeStoryModeRouteContext(context)
  if (!sanitized) {
    Taro.removeStorageSync(STORY_MODE_ROUTE_CONTEXT_KEY)
    return null
  }
  Taro.setStorageSync(STORY_MODE_ROUTE_CONTEXT_KEY, sanitized)
  return sanitized
}

export function getStoryModeRouteContext(): StoryModeRouteContext | null {
  try {
    return sanitizeStoryModeRouteContext(Taro.getStorageSync(STORY_MODE_ROUTE_CONTEXT_KEY))
  } catch (error) {
    console.warn('Failed to read story route context.', error)
    return null
  }
}

export function clearStoryModeRouteContext(): void {
  Taro.removeStorageSync(STORY_MODE_ROUTE_CONTEXT_KEY)
}

export function resolveStoryRouteDestination(context: StoryModeRouteContext | null): PoiItem | null {
  const sanitized = sanitizeStoryModeRouteContext(context)
  if (!sanitized) {
    return null
  }
  const byId = sanitized.currentAnchorTargetId ? getPoiById(sanitized.currentAnchorTargetId) : null
  if (byId) {
    return byId
  }
  const code = readText(sanitized.currentAnchorTargetCode)?.toLowerCase()
  if (!code) {
    return null
  }
  return getPoiCatalog().find((poi) => poi.code?.toLowerCase() === code) || null
}

export async function startStoryModeSession(storylineId: number, currentChapterId?: number): Promise<StoryModeSessionState> {
  if (!(await requireAuth('開始故事模式前，請先使用微信登入。'))) {
    throw new AuthRequiredError()
  }

  const session = await startStorylineRuntimeSession(storylineId)
  if (!session?.sessionId) {
    throw new AuthRequiredError()
  }

  const now = new Date().toISOString()
  return saveActiveStoryModeSession({
    storylineId,
    sessionId: session.sessionId,
    currentChapterId: currentChapterId || session.currentChapterId,
    active: true,
    startedAt: session.startedAt || now,
    lastEventAt: session.lastEventAt || now,
    statusText: '故事模式進行中',
  })!
}

export async function exitStoryModeSession(storylineId: number): Promise<StoryModeSessionState | null> {
  const stored = getActiveStoryModeSession(storylineId)
  const now = new Date().toISOString()
  let exited: StorySessionItem | undefined
  if (stored?.sessionId) {
    exited = await exitStorylineRuntimeSession(storylineId, stored.sessionId)
  }
  return saveActiveStoryModeSession({
    storylineId,
    sessionId: stored?.sessionId || exited?.sessionId,
    currentChapterId: stored?.currentChapterId || exited?.currentChapterId,
    active: false,
    startedAt: stored?.startedAt || exited?.startedAt,
    lastEventAt: exited?.lastEventAt || stored?.lastEventAt || now,
    exitedAt: exited?.exitedAt || now,
    statusText: '已離開故事模式，永久探索進度會保留。',
  })
}

export async function refreshStoryExplorationSummary(storylineId: number): Promise<StoryExplorationSummaryItem | null> {
  if (!hasActiveSessionToken() || loadGameState().user.authStatus === 'anonymous') {
    return null
  }
  const response = await api.public.getPublicUserExploration({
    locale: DEFAULT_PUBLIC_LOCALE,
    scopeType: 'storyline',
    scopeId: storylineId,
  })
  return mapStoryExplorationSummary(response)
}

export function mapStoryExplorationSummary(response?: PublicUserExplorationDto | null): StoryExplorationSummaryItem | null {
  if (!response) {
    return null
  }
  return {
    progressPercent: response.progressPercent,
    completedElementCount: response.completedElementCount,
    availableElementCount: response.availableElementCount,
    completedWeight: response.completedWeight,
    availableWeight: response.availableWeight,
  }
}

const STORY_RUNTIME_EVENT_NAME_MAP: Record<string, StoryRuntimeEventType> = {
  'story_open': 'story_opened',
  'chapter_open': 'chapter_started',
  'content_read': 'content_viewed',
  'tap': 'click_interacted',
  'tap_interacted': 'click_interacted',
  'click': 'click_interacted',
  'click_interaction': 'click_interacted',
  'arrival': 'proximity_reached',
  'arrived': 'proximity_reached',
  'poi_arrival': 'proximity_reached',
  'nearby_reached': 'proximity_reached',
  'range_reached': 'proximity_reached',
  'checkin': 'checkin_completed',
  'check_in': 'checkin_completed',
  'poi_checkin': 'checkin_completed',
  'pickup': 'pickup_interacted',
  'collectible_pickup': 'pickup_interacted',
  'task_complete': 'task_completed',
  'reward_claimed': 'reward_acquired',
  'unsupported_interaction_view': 'unsupported_viewed',
}

function normalizeStoryRuntimeEventType(eventType: StoryRuntimeEventType): StoryRuntimeEventType {
  return STORY_RUNTIME_EVENT_NAME_MAP[eventType] || eventType
}

export function buildStoryRuntimeClientEventId(input: {
  storylineId: number
  sessionId?: string
  chapterId?: number
  stepId?: number
  blockId?: number
  elementCode?: string
  elementId?: number
  idempotencyScope?: string
  eventType: StoryRuntimeEventType
}) {
  const normalizedEventType = normalizeStoryRuntimeEventType(input.eventType)
  return [
    'story-runtime:',
    input.storylineId,
    input.sessionId || 'read',
    input.chapterId || 'story',
    input.blockId || input.stepId || input.elementCode || input.elementId || input.idempotencyScope || 'root',
    normalizedEventType,
  ].join(':')
}

export async function recordStoryRuntimeEvent(input: {
  storylineId: number
  sessionId?: string
  clientEventId?: string
  idempotencyScope?: string
  chapterId?: number
  stepId?: number
  blockId?: number
  eventType: StoryRuntimeEventType
  elementCode?: string
  elementId?: number
  mediaKind?: string
  payload?: Record<string, unknown>
}) {
  const eventType = normalizeStoryRuntimeEventType(input.eventType)
  const activeSession = input.sessionId
    ? { sessionId: input.sessionId }
    : getActiveStoryModeSession(input.storylineId)
  const sessionId = activeSession?.sessionId
  const readOnlyEvents = new Set<StoryRuntimeEventType>(['story_opened', 'content_viewed', 'unsupported_viewed'])

  if (!sessionId && !readOnlyEvents.has(eventType)) {
    if (!(await requireAuth('這個故事進度需要先開始故事模式。'))) {
      throw new AuthRequiredError()
    }
    throw new AuthRequiredError('這個故事進度需要先開始故事模式。')
  }

  if (isPublicApiMockMode() || (!sessionId && (!hasActiveSessionToken() || loadGameState().user.authStatus === 'anonymous'))) {
    return undefined
  }

  const clientEventId = input.clientEventId || buildStoryRuntimeClientEventId({
    ...input,
    sessionId,
    eventType,
  })
  const payloadJson = input.payload
    ? JSON.stringify({
        ...input.payload,
        storylineId: input.storylineId,
        chapterId: input.chapterId,
        stepId: input.stepId,
        blockId: input.blockId,
        mediaKind: input.mediaKind,
      })
    : undefined

  const request = {
    elementId: input.elementId,
    elementCode: input.elementCode,
    eventType,
    eventSource: 'mini_program_story',
    storylineSessionId: sessionId,
    clientEventId,
    payloadJson,
    occurredAt: new Date().toISOString(),
  }
  if (sessionId) {
    const response = await api.public.recordPublicStorylineSessionEvent(input.storylineId, sessionId, request)
    const stored = getActiveStoryModeSession(input.storylineId)
    if (stored?.active) {
      saveActiveStoryModeSession({
        ...stored,
        lastEventAt: request.occurredAt,
        currentChapterId: response.currentChapterId || input.chapterId || stored.currentChapterId,
      })
    }
    return response
  }
  return api.public.recordPublicExperienceEvent(request)
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
  if (!isPublicApiMockMode() && state.user.authStatus !== 'anonymous' && hasActiveSessionToken()) {
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
  const publicCities = loadPublicContent().cities
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
  if (!isPublicApiMockMode() && state.user.authStatus !== 'anonymous' && hasActiveSessionToken()) {
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
  const recommendation = getTravelRecommendation(answer, userLocation)
  const next = {
    ...state,
    travelAssessment: answer,
    recommendation,
  }
  saveState(next)
  return recommendation
}

export function getTravelRecommendation(answer?: TravelAssessmentAnswer | null, userLocation?: { latitude: number; longitude: number }) {
  const target = answer || loadGameState().travelAssessment
  const stories = getStorylines()
  const discoverCards = getDiscoverCards()
  const tips = getTipArticles().filter((tip) => !tip.isPublishedByUser)
  const pois = getPoiCatalog()
  const profiles = getTravelRecommendationProfiles()

  // Check if user is in Macau (within 30km of center)
  const macauCenter = { latitude: 22.1987, longitude: 113.5439 }
  const isInMacau = userLocation 
    ? calculateDistance(userLocation.latitude, userLocation.longitude, macauCenter.latitude, macauCenter.longitude) < 30000
    : false

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

    // Calculate distance to starting POI if user location is available
    const distanceToStart = userLocation && selectedPoi
      ? calculateDistance(userLocation.latitude, userLocation.longitude, selectedPoi.latitude, selectedPoi.longitude)
      : null

    // Generate detailed recommendation reasons
    const reasons = []
    if (isInMacau) {
      reasons.push('您目前在澳門地區')
    }
    if (distanceToStart !== null) {
      reasons.push(`距離起點 ${formatDistance(distanceToStart)}`)
    }
    if (selectedStory?.difficulty) {
      const difficultyMap: Record<string, string> = { easy: '簡單', medium: '中等', hard: '困難' }
      reasons.push(`難度：${difficultyMap[selectedStory.difficulty] || selectedStory.difficulty}`)
    }
    if (selectedStory?.estimatedMinutes) {
      const hours = Math.floor(selectedStory.estimatedMinutes / 60)
      const minutes = selectedStory.estimatedMinutes % 60
      reasons.push(`預計 ${hours > 0 ? `${hours}小時` : ''}${minutes > 0 ? `${minutes}分鐘` : ''}`)
    }
    if (target?.interests && target.interests.length > 0) {
      reasons.push(`符合您的興趣：${target.interests.slice(0, 2).join('、')}`)
    }

    return {
      storyId: selectedStory?.id || 0,
      storyName: selectedStory?.name || pickReadableText(humanizeCode(selectedProfile.storyCode), '澳門精選'),
      activityTitle: pickReadableText(selectedProfile.activityTitle, discoverCards[0]?.title, '精選路線'),
      poiName: selectedPoi?.name || pickReadableText(humanizeCode(selectedProfile.poiCode), '澳門'),
      ugcTitle: selectedTip?.title || pickReadableText(humanizeCode(selectedProfile.tipCode), '旅行指南'),
      reason: reasons.length > 0 ? reasons.join(' • ') : pickReadableText(
        selectedProfile.reason,
        target
          ? `根據您選擇的興趣，${selectedStory?.name || '此路線'}最適合當前的旅程。`
          : `從${selectedStory?.name || '精選路線'}開始，連接主要的故事路徑。`,
      ),
      isInMacau,
      distanceToStart,
      difficulty: selectedStory?.difficulty,
      estimatedMinutes: selectedStory?.estimatedMinutes,
      tags: target?.interests || [],
    }
  }

  if (!stories.length || !tips.length || !pois.length) {
    return {
      storyId: 0,
      storyName: '澳門精選',
      activityTitle: discoverCards[0]?.title || '精選路線',
      poiName: '澳門',
      ugcTitle: tips[0]?.title || '旅行指南',
      reason: '內容正在加載中，請稍後刷新。',
    }
  }

  let selectedStory = stories.find((story) => story.code === FLAGSHIP_STORY_CODE)
    || stories.find((story) => story.code !== LEGACY_DUPLICATE_STORY_CODE && story.name === FLAGSHIP_STORY_NAME)
    || stories[0]
  if (target?.interests.some((interest) => interest.toLowerCase().includes('photo'))) {
    selectedStory = stories.find((story) => story.difficulty !== 'easy') || selectedStory
  } else if (target?.interests.some((interest) => interest.toLowerCase().includes('history'))) {
    selectedStory = stories.find((story) => story.code === FLAGSHIP_STORY_CODE)
      || stories.find((story) => story.code !== LEGACY_DUPLICATE_STORY_CODE && story.name === FLAGSHIP_STORY_NAME)
      || selectedStory
  }

  const selectedPoi = pois.find((poi) => poi.storyLineId === selectedStory.id) || pois[0]
  const selectedTip = tips.find((tip) => tip.locationName?.includes(selectedPoi.name)) || tips[0]

  return {
    storyId: selectedStory.id,
    storyName: selectedStory.name,
    activityTitle: discoverCards[0]?.title || '精選路線',
    poiName: selectedPoi.name,
    ugcTitle: selectedTip.title,
    reason: target
      ? `根據您選擇的興趣，${selectedStory.name}和${selectedPoi.name}最適合當前路線。`
      : `從${selectedStory.name}開始，連接主要的公開故事路線。`,
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
  if (!isPublicApiMockMode()) {
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
    title: `抵達${poi.name}`,
    narrative: pickReadableText(poi.introSummary, poi.description, `${poi.name}已準備好迎接您的下一個故事節拍`),
    audioTitle: pickReadableText(poi.introTitle, `${poi.name}語音導覽`),
    audioDuration: formatMinutes(Math.max(1, Math.round((poi.staySeconds || 30) / 60))),
    rewardLabel: stamp ? `解鎖${stamp.name}` : `推進${storyline?.name || '您的路線'}`,
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
      const evaluation = isWithinTriggerRange(lat, lng, poi.latitude, poi.longitude, poi.triggerRadius, accuracy)
      const distanceMeters = calculateDistance(lat, lng, poi.latitude, poi.longitude)
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
  const cities = loadPublicContent().cities
  const currentCity = cities.find((city) => city.code === state.user.currentCityId) || cities[0]
  const currentSubMap = currentCity
    ? getCitySubMapDtos(currentCity.code).find((subMap) => subMap.code === state.user.currentSubMapId)
    : undefined
  const mapRules = getObjectRecord(getRuntimeGroupSettings('map').checkin_rules)
  return {
    amapKey: AMAP_CONFIG.key,
    center: {
      latitude: currentSubMap?.centerLat || currentCity?.centerLat || AMAP_CONFIG.defaultCenter.latitude,
      longitude: currentSubMap?.centerLng || currentCity?.centerLng || AMAP_CONFIG.defaultCenter.longitude,
    },
    city: currentCity ? pickReadableText(currentCity.name, humanizeCode(currentCity.code)) : '',
    cityCode: currentCity?.code || '',
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
      location: `${poi.longitude},${poi.latitude}`,
      district: poi.district,
    }))
}

export function getWalkingRouteSummary(poi: PoiItem, location: { latitude: number; longitude: number }) {
  const distance = calculateDistance(location.latitude, location.longitude, poi.latitude, poi.longitude)
  const minutes = Math.max(3, Math.ceil(distance / 65))
  return {
    distance: String(Math.round(distance)),
    duration: String(minutes * 60),
    steps: [
      `朝${poi.district || '當前區域'}方向前進。`,
      `把「${poi.name}」設為下一個故事目的地。`,
      `抵達後在附近停留約 ${poi.staySeconds || 30} 秒，現場提示會接上。`,
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

  if (!isPublicApiMockMode()) {
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
    next.user.title = next.user.level >= 5 ? '城市故事導覽員' : next.user.level >= 4 ? '路線探索者' : '澳門漫遊者'
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
  const stored = Taro.getStorageSync(EMERGENCY_CONTACT_KEY)
  if (stored && typeof stored === 'object') {
    return {
      name: stored.name || '緊急聯絡人',
      phone: stored.phone || '',
    }
  }
  return {
    name: '緊急聯絡人',
    phone: '',
  }
}

export async function updateEmergencyContact(contact: { name: string; phone: string }) {
  if (!(await requireAuth('保存緊急聯絡人前，請先使用微信登入。'))) {
    throw new AuthRequiredError()
  }
  if (!isPublicApiMockMode()) {
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
