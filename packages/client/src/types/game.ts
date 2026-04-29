export type AuthStatus = 'anonymous' | 'authenticated' | 'dev-bypass'

export interface AppUserProfile {
  userId: string
  nickname: string
  avatarUrl: string
  authStatus: AuthStatus
  localeCode?: 'zh-Hant' | 'zh-Hans' | 'en' | 'pt'
  openId?: string
  level: number
  title: string
  totalStamps: number
  currentExp: number
  nextLevelExp: number
  interfaceMode: 'standard' | 'elderly'
  fontScale: number
  highContrast: boolean
  voiceGuideEnabled: boolean
  unlockedStorylines: number
  badges: string[]
  currentCityId?: string
  currentSubMapId?: string
}

export interface PoiItem {
  id: number
  name: string
  subtitle: string
  icon: string
  latitude: number
  longitude: number
  latitude: number
  longitude: number
  address: string
  geofenceRadius: number
  triggerRadius: number
  difficulty: 'easy' | 'medium' | 'hard'
  category: string
  district: string
  storyLineId?: number
  storyName?: string
  description: string
  checkInMethod: 'gps' | 'manual' | 'mock'
  staySeconds: number
  rewardStampId?: number
  tags: string[]
  coverColor: string
  cityId?: string
  subMapId?: string
  subMapName?: string
  markerKey?: 'church' | 'ghost' | 'lisboa' | 'ruins' | 'theater' | 'user'
  mapIconUrl?: string
  introTitle?: string
  introSummary?: string
  indoorMapTitle?: string
  indoorMapHint?: string
  recommendedTipIds?: number[]
  recommendedDiscoverIds?: number[]
  collectibleHints?: string[]
}


export interface StoryChapterItem {
  id: number
  title: string
  summary: string
  detail: string
  achievement: string
  collectible: string
  locationName: string
  anchorType?: string
  anchorTargetId?: number
  anchorTargetCode?: string
  primaryMediaUrl?: string
  primaryMediaAsset?: StoryMediaAssetItem
  unlock?: StoryRulePayload
  prerequisite?: StoryRulePayload
  completion?: StoryRulePayload
  effect?: StoryRulePayload
  contentBlocks?: StoryContentBlockItem[]
  prerequisiteJson?: string
  completionJson?: string
  rewardJson?: string
  locked: boolean
  runtime?: StoryChapterRuntimeItem
  runtimeSteps?: StoryRuntimeStepItem[]
  runtimeStatus?: StoryRuntimeStatus
}

export interface StorylineItem {
  id: number
  name: string
  nameEn: string
  description: string
  icon: string
  coverColor: string
  coverImageUrl?: string
  bannerImageUrl?: string
  attachmentAssets?: StoryMediaAssetItem[]
  totalChapters: number
  completedChapters: number
  estimatedTime: string
  difficulty: 'easy' | 'medium' | 'hard'
  poiIds: number[]
  chapterTitles: string[]
  progress: number
  rewardBadge?: string
  locked?: boolean
  unlockHint?: string
  chapters?: StoryChapterItem[]
  moodTags?: string[]
  cityBindingCodes?: string[]
  subMapBindingCodes?: string[]
  runtime?: StorylineRuntimeItem
  runtimeSyncedAt?: string
  runtimeSource?: 'live' | 'fallback'
  runtimeStatusText?: string
}

export interface StoryMediaAssetItem {
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
  posterUrl?: string
  fallbackUrl?: string
}

export interface StoryContentBlockItem {
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
  primaryAsset?: StoryMediaAssetItem
  attachmentAssets?: StoryMediaAssetItem[]
}

export interface StoryRulePayload {
  type?: string
  config?: Record<string, unknown>
  rawJson?: string
}

export type StoryRuntimeStatus = 'live' | 'fallback' | 'pending' | 'unsupported' | string

export interface StoryRuntimeTemplateItem {
  id?: number
  code?: string
  templateType?: string
  category?: string
  name?: string
  summary?: string
  riskLevel?: string
}

export interface StoryRuntimeStepItem {
  id?: number
  flowId?: number
  stepCode?: string
  stepType?: string
  displayCategory?: string
  displayCategoryLabel?: string
  unsupported?: boolean
  unsupportedReason?: string
  travelerActionLabel?: string
  eventType?: string
  elementCode?: string
  elementId?: number
  name?: string
  description?: string
  triggerType?: string
  mediaAssetId?: number
  mediaAsset?: StoryMediaAssetItem
  explorationWeightLevel?: string
  explorationWeightValue?: number
  requiredForCompletion?: boolean
  inheritKey?: string
  template?: StoryRuntimeTemplateItem
  sortOrder?: number
}

export interface StoryChapterRuntimeItem {
  chapterId: number
  chapterOrder?: number
  runtimeStatus?: StoryRuntimeStatus
  runtimeStatusLabel?: string
  compiledStepCount?: number
  unsupportedStepCount?: number
  anchorType?: string
  anchorTargetId?: number
  anchorTargetCode?: string
  storyModeConfig?: Record<string, unknown>
  runtimeSteps?: StoryRuntimeStepItem[]
}

export interface StorylineRuntimeItem {
  runtimeVersion?: string
  source?: string
  generatedAt?: string
  publishedChapterCount?: number
  unsupportedStepCount?: number
  storyModeConfig?: Record<string, unknown>
  chapters?: StoryChapterRuntimeItem[]
}

export interface StorySessionItem {
  storylineId: number
  sessionId: string
  currentChapterId?: number
  status?: string
  startedAt?: string
  lastEventAt?: string
  exitedAt?: string
  eventCount?: number
  exitClearedTemporaryState?: boolean
}

export interface StampItem {
  id: number
  type: 'location' | 'story' | 'mission' | 'secret'
  name: string
  description: string
  icon: string
  collected: boolean
  collectedAt?: string
  poiId?: number
  storyId?: number
  rarity: 'common' | 'rare' | 'epic'
}

export interface RewardItem {
  id: number
  name: string
  subtitle: string
  icon: string
  stampCost: number
  inventory: number
  status: 'available' | 'coming_soon' | 'redeemed'
  description: string
  highlight: string
  popupPresetCode?: string
  popupConfigJson?: string
  displayPresetCode?: string
  displayConfigJson?: string
  triggerPresetCode?: string
  triggerConfigJson?: string
  exampleContent?: string
  relatedIndoorBuildings?: Array<{ id: number; code: string; name: string }>
  relatedIndoorFloors?: Array<{ id: number; code: string; name: string }>
  attachmentAssetUrls?: string[]
}

export interface RewardRuleSummaryItem {
  id: number
  code: string
  name: string
  ruleType?: string
  summaryText?: string
}

export interface RewardPresentationStepItem {
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

export interface RewardPresentationItem {
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
  steps?: RewardPresentationStepItem[]
}

export interface RedeemablePrizeItem {
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
  presentation?: RewardPresentationItem
  ruleSummaries?: RewardRuleSummaryItem[]
  attachmentAssetUrls?: string[]
}

export interface GameRewardItem {
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
  presentation?: RewardPresentationItem
  ruleSummaries?: RewardRuleSummaryItem[]
  attachmentAssetUrls?: string[]
}

export interface CheckinResult {
  success: boolean
  poiId: number
  poiName: string
  stampId: number
  stampName: string
  experienceGained: number
  triggerMode: 'gps' | 'manual' | 'mock'
  unlockedStorylineId?: number
}

export interface MapCameraState {
  latitude: number
  longitude: number
  scale: number
}

export interface DiscoverCardItem {
  id: number | string
  title: string
  subtitle: string
  description: string
  tag: string
  icon: string
  type: 'activity' | 'merchant' | 'checkin'
  district: string
  actionText: string
  coverColor: string
}

export interface TipArticleItem {
  id: number
  title: string
  summary: string
  coverColor: string
  category: string
  author: string
  likes: number
  saves: number
  readMinutes: number
  tags: string[]
  imageUrl?: string
  locationName?: string
  contentParagraphs?: string[]
  createdAt?: string
  isPublishedByUser?: boolean
}

export interface ArrivalExperience {
  poiId: number
  title: string
  narrative: string
  audioTitle: string
  audioDuration: string
  rewardLabel: string
  canManualCheckin: boolean
}

export interface NotificationItem {
  id: number
  title: string
  content: string
  timeLabel: string
  unread?: boolean
  type: 'system' | 'ugc' | 'activity'
}

export interface CityProgressItem {
  id: string
  name: string
  subtitle: string
  coverColor: string
  centerLat?: number
  centerLng?: number
  unlocked: boolean
  firstUnlockedAt?: string
  explorationProgress: number
  titleReward: string
  landmarkCount: number
  currentSubMapId?: string
  subMaps?: SubMapProgressItem[]
}

export interface SubMapProgressItem {
  id: string
  cityId: string
  name: string
  subtitle: string
  coverColor: string
  centerLat?: number
  centerLng?: number
  unlocked: boolean
  explorationProgress: number
  landmarkCount: number
}

export interface TravelAssessmentAnswer {
  ageGroup: string
  playDuration: string
  interests: string[]
  allowLocation: boolean
}

export interface TravelRecommendation {
  storyId: number
  storyName: string
  activityTitle: string
  poiName: string
  ugcTitle: string
  reason: string
  isInMacau?: boolean
  distanceToStart?: number | null
  difficulty?: string
  estimatedMinutes?: number
  tags?: string[]
}
