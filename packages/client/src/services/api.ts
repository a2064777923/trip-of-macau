import Taro from '@tarojs/taro'
import { API_BASE_URL } from '../constants/env'

export type PublicLocaleCode = 'zh-Hant' | 'zh-Hans' | 'en' | 'pt'

export const DEFAULT_PUBLIC_LOCALE: PublicLocaleCode = 'zh-Hant'

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

export interface PublicTestModeDto {
  isTestAccount: boolean
  testGroup?: string
  mockEnabled: boolean
  mockLatitude?: number
  mockLongitude?: number
  mockPoiId?: number
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

export interface PublicCatalogRelationBindingDto {
  id: number
  code: string
  name: string
}

export interface PublicActivityDto {
  id: number
  code: string
  activityType?: string
  title: string
  summary?: string
  description?: string
  htmlContent?: string
  venueName?: string
  address?: string
  organizerName?: string
  organizerContact?: string
  organizerWebsite?: string
  signupCapacity?: number
  signupFeeAmount?: number
  signupStartAt?: string
  signupEndAt?: string
  publishStartAt?: string
  publishEndAt?: string
  isPinned?: number
  coverImageUrl?: string
  heroImageUrl?: string
  cityBindings?: PublicCatalogRelationBindingDto[]
  subMapBindings?: PublicCatalogRelationBindingDto[]
  storylineBindings?: PublicCatalogRelationBindingDto[]
  attachmentAssetUrls?: string[]
  participationCount?: number
  sortOrder?: number
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

export interface PublicIndoorFloorDto {
  id: number
  floorCode?: string
  floorNumber: number
  name: string
  description?: string
  coverImageUrl?: string
  floorPlanUrl?: string
  tileSourceType?: string
  tilePreviewImageUrl?: string
  tileRootUrl?: string
  tileManifestJson?: string
  tileZoomDerivationJson?: string
  imageWidthPx?: number
  imageHeightPx?: number
  tileSizePx?: number
  gridCols?: number
  gridRows?: number
  tileLevelCount?: number
  tileEntryCount?: number
  importStatus?: string
  importNote?: string
  altitudeMeters?: number
  areaSqm?: number
  zoomMin?: number
  zoomMax?: number
  defaultZoom?: number
  popupConfigJson?: string
  displayConfigJson?: string
  markers?: PublicIndoorMarkerDto[]
}

export interface PublicIndoorMarkerDto {
  id: number
  markerCode?: string
  nodeType?: string
  name: string
  description?: string
  relativeX?: number
  relativeY?: number
  relatedPoiId?: number
  iconUrl?: string
  animationUrl?: string
  linkedEntityType?: string
  linkedEntityId?: number
  tagsJson?: string
  popupConfigJson?: string
  displayConfigJson?: string
  metadataJson?: string
  sortOrder?: number
  status?: string
}

export interface PublicIndoorRuntimeRuleConditionDto {
  id?: string
  category?: string
  label?: string
  config?: Record<string, unknown> | null
}

export interface PublicIndoorRuntimeTriggerRuleDto {
  id?: string
  category?: string
  label?: string
  dependsOnTriggerId?: string
  config?: Record<string, unknown> | null
}

export interface PublicIndoorRuntimeCoordinatePointDto {
  x?: number
  y?: number
  order?: number
}

export interface PublicIndoorRuntimePathGraphDto {
  points?: PublicIndoorRuntimeCoordinatePointDto[]
  durationMs?: number
  holdMs?: number
  loop?: boolean
  easing?: string
}

export interface PublicIndoorRuntimeOverlayGeometryDto {
  geometryType?: string
  points?: PublicIndoorRuntimeCoordinatePointDto[]
  properties?: Record<string, unknown> | null
}

export interface PublicIndoorRuntimeEffectRuleDto {
  id?: string
  category?: string
  label?: string
  config?: Record<string, unknown> | null
}

export interface PublicIndoorRuntimeBehaviorDto {
  behaviorId: number
  behaviorCode?: string
  name?: string
  status?: string
  sortOrder?: number
  runtimeSupportLevel?: string
  supported?: boolean
  requiresAuth?: boolean
  blockedReason?: string
  appearanceRules?: PublicIndoorRuntimeRuleConditionDto[]
  triggerRules?: PublicIndoorRuntimeTriggerRuleDto[]
  effectRules?: PublicIndoorRuntimeEffectRuleDto[]
  pathGraph?: PublicIndoorRuntimePathGraphDto | null
  overlayGeometry?: PublicIndoorRuntimeOverlayGeometryDto | null
}

export interface PublicIndoorRuntimeNodeDto {
  nodeId: number
  markerCode?: string
  nodeType?: string
  presentationMode?: string
  overlayType?: string
  name?: string
  description?: string
  relativeX?: number
  relativeY?: number
  relatedPoiId?: number
  iconUrl?: string
  animationUrl?: string
  linkedEntityType?: string
  linkedEntityId?: number
  popupConfigJson?: string
  displayConfigJson?: string
  sortOrder?: number
  status?: string
  runtimeSupportLevel?: string
  overlayGeometry?: PublicIndoorRuntimeOverlayGeometryDto | null
  behaviors?: PublicIndoorRuntimeBehaviorDto[]
}

export interface PublicIndoorRuntimeFloorDto {
  floorId: number
  floorCode?: string
  floorNumber?: number
  buildingId: number
  buildingCode?: string
  name?: string
  description?: string
  coverImageUrl?: string
  floorPlanUrl?: string
  tileSourceType?: string
  tilePreviewImageUrl?: string
  tileRootUrl?: string
  tileManifestJson?: string
  tileZoomDerivationJson?: string
  imageWidthPx?: number
  imageHeightPx?: number
  tileSizePx?: number
  gridCols?: number
  gridRows?: number
  tileLevelCount?: number
  tileEntryCount?: number
  importStatus?: string
  importNote?: string
  altitudeMeters?: number
  areaSqm?: number
  zoomMin?: number
  zoomMax?: number
  defaultZoom?: number
  popupConfigJson?: string
  displayConfigJson?: string
  runtimeVersion?: string
  nodes?: PublicIndoorRuntimeNodeDto[]
}

export interface PublicIndoorRuntimeTriggeredEffectDto {
  effectId?: string
  category?: string
  label?: string
  config?: Record<string, unknown> | null
  pathGraph?: PublicIndoorRuntimePathGraphDto | null
  overlayGeometry?: PublicIndoorRuntimeOverlayGeometryDto | null
}

export interface PublicIndoorRuntimeInteractionDto {
  interactionAccepted?: boolean
  visible?: boolean
  matchedTriggerId?: string
  blockedReason?: string | null
  requiresAuth?: boolean
  effects?: PublicIndoorRuntimeTriggeredEffectDto[]
  interactionLogId?: number
  cooldownUntil?: string | null
}

export interface PublicIndoorBuildingDto {
  id: number
  buildingCode: string
  bindingMode?: string
  cityId?: number
  cityCode?: string
  subMapId?: number
  poiId?: number
  name: string
  address?: string
  description?: string
  coverImageUrl?: string
  popupConfigJson?: string
  displayConfigJson?: string
  sourceCoordinateSystem?: string
  sourceLatitude?: number
  sourceLongitude?: number
  latitude?: number
  longitude?: number
  totalFloors?: number
  basementFloors?: number
  floors?: PublicIndoorFloorDto[]
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
  anchorType?: string
  anchorTargetId?: number
  anchorTargetCode?: string
  unlockType?: string
  mediaUrl?: string
  primaryMediaUrl?: string
  primaryMediaAsset?: PublicStoryMediaAssetDto
  unlock?: PublicStoryChapterUnlockDto
  prerequisite?: PublicStoryChapterConditionDto
  completion?: PublicStoryChapterConditionDto
  effect?: PublicStoryChapterEffectDto
  contentBlocks?: PublicStoryContentBlockDto[]
  prerequisiteJson?: string
  completionJson?: string
  rewardJson?: string
  sortOrder?: number
}

export interface PublicStoryMediaAssetDto {
  id: number
  assetKind?: string
  url?: string
  mimeType?: string
  originalFilename?: string
  widthPx?: number
  heightPx?: number
  animationSubtype?: string
  defaultLoop?: boolean
  defaultAutoplay?: boolean
  posterAssetId?: number
  posterUrl?: string
  fallbackAssetId?: number
  fallbackUrl?: string
}

export interface PublicStoryContentBlockDto {
  id: number
  code?: string
  blockType?: string
  title?: string
  summary?: string
  body?: string
  stylePreset?: string
  displayMode?: string
  visibilityJson?: string
  displayConditionJson?: string
  configJson?: string
  sortOrder?: number
  primaryAsset?: PublicStoryMediaAssetDto
  attachmentAssets?: PublicStoryMediaAssetDto[]
}

export interface PublicStoryChapterUnlockDto {
  type?: string
  config?: Record<string, unknown>
  rawJson?: string
}

export interface PublicStoryChapterConditionDto {
  type?: string
  config?: Record<string, unknown>
  rawJson?: string
}

export interface PublicStoryChapterEffectDto {
  type?: string
  config?: Record<string, unknown>
  rawJson?: string
}

export interface PublicStorylineDto {
  id: number
  cityId: number
  cityCode: string
  cityBindings?: PublicCatalogRelationBindingDto[]
  subMapBindings?: PublicCatalogRelationBindingDto[]
  code: string
  name: string
  nameEn?: string
  description?: string
  estimatedMinutes?: number
  difficulty?: string
  rewardBadge?: string
  coverImageUrl?: string
  bannerImageUrl?: string
  attachmentAssets?: PublicStoryMediaAssetDto[]
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
  popupPresetCode?: string
  popupConfigJson?: string
  displayPresetCode?: string
  displayConfigJson?: string
  triggerPresetCode?: string
  triggerConfigJson?: string
  exampleContent?: string
  relatedStorylines?: PublicCatalogRelationBindingDto[]
  relatedCities?: PublicCatalogRelationBindingDto[]
  relatedSubMaps?: PublicCatalogRelationBindingDto[]
  relatedIndoorBuildings?: PublicCatalogRelationBindingDto[]
  relatedIndoorFloors?: PublicCatalogRelationBindingDto[]
  attachmentAssetUrls?: string[]
  sortOrder?: number
}

export interface PublicRewardRuleSummaryDto {
  id: number
  code: string
  name: string
  ruleType?: string
  summaryText?: string
}

export interface PublicRewardPresentationStepDto {
  stepType?: string
  stepCode?: string
  titleText?: string
  assetUrl?: string
  durationMs?: number
  skippableOverride?: number
  triggerSfxUrl?: string
  voiceOverUrl?: string
  overlayConfigJson?: string
  sortOrder?: number
}

export interface PublicRewardPresentationDto {
  id: number
  code: string
  name: string
  presentationType?: string
  firstTimeOnly?: number
  skippable?: number
  minimumDisplayMs?: number
  interruptPolicy?: string
  queuePolicy?: string
  priorityWeight?: number
  coverImageUrl?: string
  voiceOverUrl?: string
  sfxUrl?: string
  summaryText?: string
  configJson?: string
  steps?: PublicRewardPresentationStepDto[]
}

export interface PublicRedeemablePrizeDto {
  id: number
  code: string
  prizeType?: string
  fulfillmentMode?: string
  name: string
  subtitle?: string
  description?: string
  highlight?: string
  coverImageUrl?: string
  stampCost?: number
  inventoryTotal?: number
  inventoryRedeemed?: number
  availableInventory?: number
  stockPolicyJson?: string
  fulfillmentConfigJson?: string
  presentationId?: number
  presentation?: PublicRewardPresentationDto
  ruleSummaries?: PublicRewardRuleSummaryDto[]
  relatedStorylines?: PublicCatalogRelationBindingDto[]
  relatedCities?: PublicCatalogRelationBindingDto[]
  relatedSubMaps?: PublicCatalogRelationBindingDto[]
  relatedIndoorBuildings?: PublicCatalogRelationBindingDto[]
  relatedIndoorFloors?: PublicCatalogRelationBindingDto[]
  attachmentAssetUrls?: string[]
  sortOrder?: number
}

export interface PublicGameRewardDto {
  id: number
  code: string
  rewardType?: string
  rarity?: string
  stackable?: number
  maxOwned?: number
  canEquip?: number
  canConsume?: number
  name: string
  subtitle?: string
  description?: string
  highlight?: string
  coverImageUrl?: string
  iconUrl?: string
  animationUrl?: string
  rewardConfigJson?: string
  presentationId?: number
  presentation?: PublicRewardPresentationDto
  ruleSummaries?: PublicRewardRuleSummaryDto[]
  relatedStorylines?: PublicCatalogRelationBindingDto[]
  relatedCities?: PublicCatalogRelationBindingDto[]
  relatedSubMaps?: PublicCatalogRelationBindingDto[]
  relatedIndoorBuildings?: PublicCatalogRelationBindingDto[]
  relatedIndoorFloors?: PublicCatalogRelationBindingDto[]
  attachmentAssetUrls?: string[]
  sortOrder?: number
}

export interface PublicCollectibleDto {
  id: number
  code: string
  name: string
  description?: string
  collectibleType?: string
  rarity?: string
  acquisitionSource?: string
  coverImageUrl?: string
  iconImageUrl?: string
  animationUrl?: string
  popupPresetCode?: string
  popupConfigJson?: string
  displayPresetCode?: string
  displayConfigJson?: string
  triggerPresetCode?: string
  triggerConfigJson?: string
  exampleContent?: string
  relatedStorylines?: PublicCatalogRelationBindingDto[]
  relatedCities?: PublicCatalogRelationBindingDto[]
  relatedSubMaps?: PublicCatalogRelationBindingDto[]
  relatedIndoorBuildings?: PublicCatalogRelationBindingDto[]
  relatedIndoorFloors?: PublicCatalogRelationBindingDto[]
  attachmentAssetUrls?: string[]
  sortOrder?: number
}

export interface PublicBadgeDto {
  id: number
  code: string
  name: string
  description?: string
  badgeType?: string
  rarity?: string
  hidden?: boolean
  coverImageUrl?: string
  iconImageUrl?: string
  animationUrl?: string
  popupPresetCode?: string
  popupConfigJson?: string
  displayPresetCode?: string
  displayConfigJson?: string
  triggerPresetCode?: string
  triggerConfigJson?: string
  exampleContent?: string
  relatedStorylines?: PublicCatalogRelationBindingDto[]
  relatedCities?: PublicCatalogRelationBindingDto[]
  relatedSubMaps?: PublicCatalogRelationBindingDto[]
  relatedIndoorBuildings?: PublicCatalogRelationBindingDto[]
  relatedIndoorFloors?: PublicCatalogRelationBindingDto[]
  attachmentAssetUrls?: string[]
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
  const maxRetries = 3
  const retryDelay = 1000 // 1 second
  let lastError: Error | null = null

  for (let attempt = 0; attempt <= maxRetries; attempt++) {
    if (options.loading !== false && attempt === 0) {
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
        timeout: 30000,
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
        throw new Error('AUTH_REQUIRED')
      }

      throw new Error(response.data?.message || 'Request failed')
    } catch (error: any) {
      lastError = error

      // Don't retry on auth errors or client errors
      if (error.message === 'AUTH_REQUIRED' || (error.statusCode && error.statusCode >= 400 && error.statusCode < 500)) {
        if (options.loading !== false) {
          Taro.hideLoading()
        }
        throw error
      }

      // Retry on network errors or server errors
      if (attempt < maxRetries) {
        console.warn(`Request failed (attempt ${attempt + 1}/${maxRetries + 1}), retrying...`, error)
        await new Promise(resolve => setTimeout(resolve, retryDelay * Math.pow(2, attempt))) // Exponential backoff
        continue
      }

      // All retries exhausted
      if (options.loading !== false) {
        Taro.hideLoading()
      }
      throw error
    }
  }

  // This should never be reached, but TypeScript needs it
  throw lastError || new Error('Request failed after all retries')
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

async function getPublicRedeemablePrizes(locale: PublicLocaleCode = DEFAULT_PUBLIC_LOCALE) {
  return request<PublicRedeemablePrizeDto[]>({
    url: '/redeemable-prizes',
    query: { locale },
    loading: false,
  })
}

async function getPublicGameRewards(locale: PublicLocaleCode = DEFAULT_PUBLIC_LOCALE, honorsOnly?: boolean) {
  return request<PublicGameRewardDto[]>({
    url: '/game-rewards',
    query: { locale, honorsOnly },
    loading: false,
  })
}

async function getPublicRewardPresentation(presentationId: number, locale: PublicLocaleCode = DEFAULT_PUBLIC_LOCALE) {
  return request<PublicRewardPresentationDto>({
    url: `/reward-presentations/${presentationId}`,
    query: { locale },
    loading: false,
  })
}

async function getPublicActivities(locale: PublicLocaleCode = DEFAULT_PUBLIC_LOCALE) {
  return request<PublicActivityDto[]>({
    url: '/activities',
    query: { locale },
    loading: false,
  })
}

async function getPublicCollectibles(locale: PublicLocaleCode = DEFAULT_PUBLIC_LOCALE) {
  return request<PublicCollectibleDto[]>({
    url: '/collectibles',
    query: { locale },
    loading: false,
  })
}

async function getPublicBadges(locale: PublicLocaleCode = DEFAULT_PUBLIC_LOCALE) {
  return request<PublicBadgeDto[]>({
    url: '/badges',
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

async function getPublicIndoorBuilding(params: {
  buildingId?: number
  poiId?: number
  locale?: PublicLocaleCode
}) {
  if (params.buildingId) {
    return request<PublicIndoorBuildingDto>({
      url: `/indoor/buildings/${params.buildingId}`,
      query: { locale: params.locale || DEFAULT_PUBLIC_LOCALE },
      loading: false,
    })
  }
  if (params.poiId) {
    return request<PublicIndoorBuildingDto>({
      url: `/indoor/buildings/by-poi/${params.poiId}`,
      query: { locale: params.locale || DEFAULT_PUBLIC_LOCALE },
      loading: false,
    })
  }
  throw new Error('Indoor building id or poi id is required')
}

async function getPublicIndoorFloor(params: {
  floorId: number
  locale?: PublicLocaleCode
}) {
  return request<PublicIndoorFloorDto>({
    url: `/indoor/floors/${params.floorId}`,
    query: { locale: params.locale || DEFAULT_PUBLIC_LOCALE },
    loading: false,
  })
}

async function getPublicIndoorFloorMarkers(params: {
  floorId: number
  locale?: PublicLocaleCode
}) {
  return request<PublicIndoorMarkerDto[]>({
    url: `/indoor/floors/${params.floorId}/markers`,
    query: { locale: params.locale || DEFAULT_PUBLIC_LOCALE },
    loading: false,
  })
}

async function getPublicIndoorFloorRuntime(params: {
  floorId: number
  locale?: PublicLocaleCode
}) {
  return request<PublicIndoorRuntimeFloorDto>({
    url: `/indoor/floors/${params.floorId}/runtime`,
    query: { locale: params.locale || DEFAULT_PUBLIC_LOCALE },
    loading: false,
  })
}

async function submitPublicIndoorRuntimeInteraction(data: {
  floorId: number
  nodeId: number
  behaviorId: number
  triggerId?: string
  eventType: string
  eventTimestamp?: string
  relativeX?: number
  relativeY?: number
  dwellMs?: number
  clientSessionId?: string
  locale?: PublicLocaleCode
}) {
  return request<PublicIndoorRuntimeInteractionDto>({
    url: '/indoor/runtime/interactions',
    method: 'POST',
    data,
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

async function getUserTestMode() {
  return request<PublicTestModeDto>({
    url: '/user/test-mode',
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
    getPublicRedeemablePrizes,
    getPublicGameRewards,
    getPublicRewardPresentation,
    getPublicActivities,
    getPublicCollectibles,
    getPublicBadges,
    getPublicStamps,
    getPublicNotifications,
    getPublicRuntimeGroup,
    getPublicDiscoverCards,
    getPublicIndoorBuilding,
    getPublicIndoorFloor,
    getPublicIndoorFloorMarkers,
    getPublicIndoorFloorRuntime,
    submitPublicIndoorRuntimeInteraction,
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
    getUserTestMode,
  },
}

export default api
