import Taro from '@tarojs/taro'
import { API_BASE_URL } from '../constants/env'

export type PublicLocaleCode = 'zh-Hant' | 'zh-Hans' | 'en' | 'pt'

export const DEFAULT_PUBLIC_LOCALE: PublicLocaleCode = 'en'

type HttpMethod = 'GET' | 'POST' | 'PUT' | 'DELETE'
type Primitive = string | number | boolean
type RequestQuery = Record<string, Primitive | undefined | null>

interface RequestOptions<TBody = unknown> {
  url: string
  method?: HttpMethod
  data?: TBody
  query?: RequestQuery
  loading?: boolean
  header?: Record<string, string>
}

interface ApiEnvelope<T> {
  code: number
  message: string
  data: T
}

export interface PublicUserProfileDto {
  id: number
  openId: string
  nickname: string
  avatarUrl?: string
  level: number
  title: string
  totalStamps: number
  currentExp: number
  nextLevelExp: number
  currentCityId?: number
  currentCityCode?: string
  currentLocaleCode?: PublicLocaleCode
}

export interface PublicUserPreferencesDto {
  interfaceMode: 'standard' | 'elderly'
  fontScale: number
  highContrast: boolean
  voiceGuideEnabled: boolean
  seniorMode: boolean
  localeCode?: PublicLocaleCode
  emergencyContactName?: string
  emergencyContactPhone?: string
  runtimeOverrides?: Record<string, unknown>
}

export interface PublicUserCheckinHistoryDto {
  poiId: number
  poiName: string
  stampId?: number
  stampName?: string
  experienceGained: number
  triggerMode: 'gps' | 'manual' | 'mock'
  unlockedStorylineId?: number
  checkedAt: string
}

export interface PublicUserProgressDto {
  activeStoryId?: number
  collectedStampIds: number[]
  completedStoryIds: number[]
  completedChapterIds: number[]
  unlockedCityCodes: string[]
  redeemedRewardIds: number[]
  checkinHistory: PublicUserCheckinHistoryDto[]
}

export interface PublicUserRewardRedemptionDto {
  id: number
  rewardId: number
  rewardName: string
  redemptionStatus: string
  stampCostSnapshot: number
  qrCode: string
  redeemedAt?: string
  expiresAt?: string
  createdAt?: string
}

export interface PublicUserStateDto {
  profile: PublicUserProfileDto
  preferences: PublicUserPreferencesDto
  progress: PublicUserProgressDto
  rewardRedemptions: PublicUserRewardRedemptionDto[]
}

export interface PublicUserSessionDto {
  accessToken: string
  tokenType: string
  state: PublicUserStateDto
}

export interface PublicUserCheckinDto {
  success: boolean
  poiId: number
  poiName: string
  stampId?: number
  stampName?: string
  experienceGained: number
  triggerMode: 'gps' | 'manual' | 'mock'
  unlockedStorylineId?: number
  checkedAt: string
  state: PublicUserStateDto
}

export interface PublicUserRewardRedeemDto {
  rewardId: number
  rewardName: string
  redemptionStatus: string
  qrCode: string
  expiresAt?: string
  state: PublicUserStateDto
}

export interface PublicCityDto {
  id: number
  code: string
  name: string
  subtitle?: string
  description?: string
  countryCode?: string
  sourceCoordinateSystem?: string
  sourceCenterLat?: number
  sourceCenterLng?: number
  centerLat?: number
  centerLng?: number
  defaultZoom?: number
  unlockType?: string
  coverImageUrl?: string
  bannerImageUrl?: string
  popupConfigJson?: string
  displayConfigJson?: string
  subMaps?: PublicSubMapDto[]
  sortOrder?: number
}

export interface PublicSubMapDto {
  id: number
  cityId: number
  cityCode: string
  code: string
  name: string
  subtitle?: string
  description?: string
  sourceCoordinateSystem?: string
  sourceCenterLat?: number
  sourceCenterLng?: number
  centerLat?: number
  centerLng?: number
  boundsJson?: string
  popupConfigJson?: string
  displayConfigJson?: string
  coverImageUrl?: string
  sortOrder?: number
  publishedAt?: string
}

export interface PublicPoiDto {
  id: number
  cityId: number
  cityCode: string
  subMapId?: number
  subMapCode?: string
  subMapName?: string
  storylineId?: number
  storylineCode?: string
  storylineName?: string
  code: string
  name: string
  subtitle?: string
  address?: string
  sourceCoordinateSystem?: string
  sourceLatitude?: number
  sourceLongitude?: number
  latitude: number
  longitude: number
  triggerRadius?: number
  manualCheckinRadius?: number
  staySeconds?: number
  categoryCode?: string
  difficulty?: string
  district?: string
  description?: string
  introTitle?: string
  introSummary?: string
  coverImageUrl?: string
  mapIconUrl?: string
  audioUrl?: string
  popupConfigJson?: string
  displayConfigJson?: string
  sortOrder?: number
  publishedAt?: string
}

export interface PublicStoryChapterDto {
  id: number
  chapterOrder?: number
  title: string
  summary?: string
  detail?: string
  achievement?: string
  collectible?: string
  locationName?: string
  unlockType?: string
  mediaUrl?: string
  sortOrder?: number
}

export interface PublicStorylineDto {
  id: number
  cityId: number
  cityCode: string
  code: string
  name: string
  nameEn?: string
  description?: string
  estimatedMinutes?: number
  difficulty?: string
  rewardBadge?: string
  coverImageUrl?: string
  bannerImageUrl?: string
  totalChapters?: number
  sortOrder?: number
  chapters?: PublicStoryChapterDto[]
}

export interface PublicTipArticleDto {
  id: number
  cityId?: number
  cityCode?: string
  code: string
  categoryCode?: string
  title: string
  summary?: string
  contentParagraphs?: string[]
  authorDisplayName?: string
  locationName?: string
  tags?: string[]
  coverImageUrl?: string
  sourceType?: string
  sortOrder?: number
  publishedAt?: string
}

export interface PublicRewardDto {
  id: number
  code: string
  name: string
  subtitle?: string
  description?: string
  highlight?: string
  stampCost: number
  inventoryTotal?: number
  inventoryRedeemed?: number
  availableInventory?: number
  coverImageUrl?: string
  sortOrder?: number
}

export interface PublicStampDto {
  id: number
  code: string
  name: string
  description?: string
  stampType?: string
  rarity?: string
  iconImageUrl?: string
  relatedPoiId?: number | null
  relatedStorylineId?: number | null
  sortOrder?: number
}

export interface PublicNotificationDto {
  id: number
  code: string
  title: string
  content?: string
  notificationType?: string
  targetScope?: string
  actionUrl?: string
  coverImageUrl?: string
  sortOrder?: number
  publishedAt?: string
}

export interface PublicDiscoverCardDto {
  id: string
  title: string
  subtitle?: string
  description?: string
  tag?: string
  icon?: string
  type: 'activity' | 'merchant' | 'checkin'
  district?: string
  actionText?: string
  coverColor?: string
  actionUrl?: string | null
  sourceType?: string
  sourceId?: number
}

export interface PublicRuntimeSettingItemDto {
  id: number
  settingKey: string
  title?: string
  description?: string
  value: unknown
  assetUrl?: string
  sortOrder?: number
}

export interface PublicRuntimeGroupDto {
  group: string
  localeCode?: string
  settings?: Record<string, unknown>
  items?: PublicRuntimeSettingItemDto[]
}

function buildQuery(query?: RequestQuery) {
  if (!query) {
    return ''
  }

  const params = Object.entries(query).reduce((accumulator, [key, value]) => {
    if (value === undefined || value === null || value === '') {
      return accumulator
    }
    accumulator.append(key, String(value))
    return accumulator
  }, new URLSearchParams())

  const output = params.toString()
  return output ? `?${output}` : ''
}

async function request<TResponse, TBody = unknown>(options: RequestOptions<TBody>): Promise<TResponse> {
  if (options.loading !== false) {
    Taro.showLoading({ title: 'Loading...' })
  }

  try {
    const token = Taro.getStorageSync('token')
    const response = await Taro.request<ApiEnvelope<TResponse>>({
      url: `${API_BASE_URL}${options.url}${buildQuery(options.query)}`,
      method: options.method || 'GET',
      data: options.data,
      header: {
        'Content-Type': 'application/json',
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
        ...options.header,
      },
    })

    if (options.loading !== false) {
      Taro.hideLoading()
    }

    if (response.statusCode >= 200 && response.statusCode < 300) {
      if (response.data?.code !== 0) {
        throw new Error(response.data?.message || 'Request failed')
      }
      return response.data.data
    }

    if (response.statusCode === 401) {
      Taro.removeStorageSync('token')
      throw new Error('Unauthorized')
    }

    throw new Error(response.data?.message || 'Request failed')
  } catch (error) {
    if (options.loading !== false) {
      Taro.hideLoading()
    }
    throw error
  }
}

async function getPublicCities(locale: PublicLocaleCode = DEFAULT_PUBLIC_LOCALE) {
  return request<PublicCityDto[]>({
    url: '/cities',
    query: { locale },
    loading: false,
  })
}

async function getPublicSubMaps(locale: PublicLocaleCode = DEFAULT_PUBLIC_LOCALE, cityCode?: string) {
  return request<PublicSubMapDto[]>({
    url: '/sub-maps',
    query: { locale, cityCode },
    loading: false,
  })
}

async function getPublicPois(locale: PublicLocaleCode = DEFAULT_PUBLIC_LOCALE) {
  return request<PublicPoiDto[]>({
    url: '/pois',
    query: { locale },
    loading: false,
  })
}

async function getPublicStorylines(locale: PublicLocaleCode = DEFAULT_PUBLIC_LOCALE) {
  return request<PublicStorylineDto[]>({
    url: '/story-lines',
    query: { locale },
    loading: false,
  })
}

async function getPublicTips(locale: PublicLocaleCode = DEFAULT_PUBLIC_LOCALE) {
  return request<PublicTipArticleDto[]>({
    url: '/tips',
    query: { locale },
    loading: false,
  })
}

async function getPublicRewards(locale: PublicLocaleCode = DEFAULT_PUBLIC_LOCALE) {
  return request<PublicRewardDto[]>({
    url: '/rewards',
    query: { locale },
    loading: false,
  })
}

async function getPublicStamps(locale: PublicLocaleCode = DEFAULT_PUBLIC_LOCALE) {
  return request<PublicStampDto[]>({
    url: '/stamps',
    query: { locale },
    loading: false,
  })
}

async function getPublicNotifications(locale: PublicLocaleCode = DEFAULT_PUBLIC_LOCALE) {
  return request<PublicNotificationDto[]>({
    url: '/notifications',
    query: { locale },
    loading: false,
  })
}

async function getPublicRuntimeGroup(group: string, locale: PublicLocaleCode = DEFAULT_PUBLIC_LOCALE) {
  return request<PublicRuntimeGroupDto>({
    url: `/runtime/${group}`,
    query: { locale },
    loading: false,
  })
}

async function getPublicDiscoverCards(locale: PublicLocaleCode = DEFAULT_PUBLIC_LOCALE) {
  return request<PublicDiscoverCardDto[]>({
    url: '/discover/cards',
    query: { locale },
    loading: false,
  })
}

async function loginWithWechat(data: {
  code: string
  nickname?: string
  avatarUrl?: string
  localeCode?: PublicLocaleCode
  interfaceMode?: string
}) {
  return request<PublicUserSessionDto>({
    url: '/user/login/wechat',
    method: 'POST',
    data,
    loading: false,
  })
}

async function loginWithDevBypass(data: {
  devIdentity: string
  nickname?: string
  avatarUrl?: string
  localeCode?: PublicLocaleCode
  interfaceMode?: string
}) {
  return request<PublicUserSessionDto>({
    url: '/user/login/dev-bypass',
    method: 'POST',
    data,
    loading: false,
  })
}

async function getUserState() {
  return request<PublicUserStateDto>({
    url: '/user/state',
    loading: false,
  })
}

async function getUserProfile() {
  return request<PublicUserProfileDto>({
    url: '/user/profile',
    loading: false,
  })
}

async function updateUserCurrentCity(data: { cityCode: string }) {
  return request<PublicUserProfileDto>({
    url: '/user/profile/current-city',
    method: 'PUT',
    data,
    loading: false,
  })
}

async function getUserProgress() {
  return request<PublicUserProgressDto>({
    url: '/user/progress',
    loading: false,
  })
}

async function getUserPreferences() {
  return request<PublicUserPreferencesDto>({
    url: '/user/preferences',
    loading: false,
  })
}

async function updateUserPreferences(data: Partial<PublicUserPreferencesDto> & {
  emergencyContactName?: string
  emergencyContactPhone?: string
}) {
  return request<PublicUserPreferencesDto>({
    url: '/user/preferences',
    method: 'PUT',
    data,
    loading: false,
  })
}

async function getUserRewardRedemptions() {
  return request<PublicUserRewardRedemptionDto[]>({
    url: '/user/progress/rewards',
    loading: false,
  })
}

async function createUserCheckin(data: {
  poiId: number
  triggerMode: 'gps' | 'manual' | 'mock'
  distanceMeters?: number
  gpsAccuracy?: number
  latitude?: number
  longitude?: number
}) {
  return request<PublicUserCheckinDto>({
    url: '/user/checkins',
    method: 'POST',
    data,
    loading: false,
  })
}

async function redeemUserReward(rewardId: number) {
  return request<PublicUserRewardRedeemDto>({
    url: `/user/rewards/${rewardId}/redeem`,
    method: 'POST',
    loading: false,
  })
}

export const api = {
  public: {
    getPublicCities,
    getPublicSubMaps,
    getPublicPois,
    getPublicStorylines,
    getPublicTips,
    getPublicRewards,
    getPublicStamps,
    getPublicNotifications,
    getPublicRuntimeGroup,
    getPublicDiscoverCards,
  },
  user: {
    loginWithWechat,
    loginWithDevBypass,
    getUserState,
    getUserProfile,
    updateUserCurrentCity,
    getUserProgress,
    getUserPreferences,
    updateUserPreferences,
    getUserRewardRedemptions,
    createUserCheckin,
    redeemUserReward,
  },
}

export default api
