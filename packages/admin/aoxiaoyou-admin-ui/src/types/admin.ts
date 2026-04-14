export interface AdminCurrentUser {
  userId: number;
  username: string;
  realName?: string;
  email?: string;
  roles: string[];
  permissions: string[];
  lastLoginAt?: string;
}

export interface AdminAuthResponse {
  token: string;
  refreshToken: string;
  expiresIn: number;
  user: AdminCurrentUser;
}

export type SupportedLocale = 'zh-Hant' | 'zh-Hans' | 'en' | 'pt';

export interface AdminTranslationSettings {
  primaryAuthoringLocale: SupportedLocale;
  enginePriority: string[];
  overwriteFilledLocales: boolean;
  bridgeEnabled: boolean;
  requestTimeoutMs: number;
  maxTextLength: number;
  bridgeScriptPath?: string;
}

export interface AdminTranslationSettingsUpdatePayload {
  primaryAuthoringLocale: SupportedLocale;
  enginePriority: string[];
  overwriteFilledLocales: boolean;
}

export interface AdminTranslateRequestPayload {
  sourceLocale: SupportedLocale;
  targetLocales: SupportedLocale[];
  text: string;
  enginePriority?: string[];
  overwriteFilledLocales?: boolean;
  existingTranslations?: Partial<Record<SupportedLocale, string>>;
}

export interface AdminTranslateLocaleResult {
  targetLocale: SupportedLocale;
  status: string;
  translatedText?: string;
  engine?: string;
  attemptedEngines?: string[];
  message?: string;
}

export interface AdminTranslateResponse {
  sourceLocale: SupportedLocale;
  targetLocales: SupportedLocale[];
  enginePriority: string[];
  overwriteFilledLocales: boolean;
  results: AdminTranslateLocaleResult[];
}

export interface PaginationResponse<T> {
  pageNum: number;
  pageSize: number;
  total: number;
  totalPages: number;
  list: T[];
}

export type CoordinateSystem = 'GCJ02' | 'WGS84' | 'BD09' | 'UNKNOWN';
export type SpatialEntityType = 'city' | 'sub_map' | 'poi';
export type SpatialAssetUsageType = 'cover' | 'gallery' | 'popup' | 'audio' | 'video' | 'map-icon';

export interface AdminSpatialAssetLinkItem {
  id?: number;
  entityType?: SpatialEntityType;
  entityId?: number;
  usageType: SpatialAssetUsageType;
  assetId: number;
  titleZh?: string;
  titleEn?: string;
  titleZht?: string;
  titlePt?: string;
  descriptionZh?: string;
  descriptionEn?: string;
  descriptionZht?: string;
  descriptionPt?: string;
  displayConfigJson?: string;
  sortOrder?: number;
  status?: string;
}

export interface AdminCoordinatePreviewPayload {
  sourceCoordinateSystem?: CoordinateSystem;
  latitude?: number | null;
  longitude?: number | null;
}

export interface AdminCoordinatePreviewResult {
  sourceCoordinateSystem: CoordinateSystem;
  sourceLatitude?: number | null;
  sourceLongitude?: number | null;
  normalizedLatitude?: number | null;
  normalizedLongitude?: number | null;
  normalizationStatus?: string;
  note?: string;
}

export interface AdminSpatialMetadataSuggestionPayload {
  entityType: SpatialEntityType;
  code?: string;
  nameZh?: string;
  nameEn?: string;
  nameZht?: string;
}

export interface AdminSpatialMetadataSuggestion {
  entityType: SpatialEntityType;
  code?: string;
  countryCode?: string;
  sourceCoordinateSystem?: CoordinateSystem;
  suggestedCenterLat?: number | null;
  suggestedCenterLng?: number | null;
  defaultZoom?: number | null;
  note?: string;
  amapAssisted?: boolean;
}

export interface AdminSubMapItem {
  id: number;
  cityId: number;
  cityCode?: string;
  cityName?: string;
  code: string;
  nameZh: string;
  nameEn?: string;
  nameZht?: string;
  namePt?: string;
  subtitleZh?: string;
  subtitleEn?: string;
  subtitleZht?: string;
  subtitlePt?: string;
  descriptionZh?: string;
  descriptionEn?: string;
  descriptionZht?: string;
  descriptionPt?: string;
  coverAssetId?: number | null;
  sourceCoordinateSystem?: CoordinateSystem;
  sourceCenterLat?: number | null;
  sourceCenterLng?: number | null;
  centerLat?: number | null;
  centerLng?: number | null;
  boundsJson?: string;
  popupConfigJson?: string;
  displayConfigJson?: string;
  sortOrder?: number;
  status?: string;
  publishedAt?: string | null;
  attachments?: AdminSpatialAssetLinkItem[];
}

export interface AdminCityPayload {
  code: string;
  nameZh: string;
  nameEn?: string;
  nameZht?: string;
  namePt?: string;
  subtitleZh?: string;
  subtitleEn?: string;
  subtitleZht?: string;
  subtitlePt?: string;
  countryCode?: string;
  customCountryName?: string;
  sourceCoordinateSystem?: CoordinateSystem;
  sourceCenterLat?: number | null;
  sourceCenterLng?: number | null;
  centerLat?: number | null;
  centerLng?: number | null;
  defaultZoom?: number | null;
  unlockType?: string;
  unlockConditionJson?: string;
  coverAssetId?: number | null;
  bannerAssetId?: number | null;
  descriptionZh?: string;
  descriptionEn?: string;
  descriptionZht?: string;
  descriptionPt?: string;
  popupConfigJson?: string;
  displayConfigJson?: string;
  attachments?: AdminSpatialAssetLinkItem[];
  sortOrder?: number | null;
  status?: string;
  publishedAt?: string | null;
}

export interface AdminSubMapPayload {
  cityId: number;
  code: string;
  nameZh: string;
  nameEn?: string;
  nameZht?: string;
  namePt?: string;
  subtitleZh?: string;
  subtitleEn?: string;
  subtitleZht?: string;
  subtitlePt?: string;
  descriptionZh?: string;
  descriptionEn?: string;
  descriptionZht?: string;
  descriptionPt?: string;
  coverAssetId?: number | null;
  sourceCoordinateSystem?: CoordinateSystem;
  sourceCenterLat?: number | null;
  sourceCenterLng?: number | null;
  centerLat?: number | null;
  centerLng?: number | null;
  boundsJson?: string;
  popupConfigJson?: string;
  displayConfigJson?: string;
  sortOrder?: number | null;
  status?: string;
  publishedAt?: string | null;
  attachments?: AdminSpatialAssetLinkItem[];
}

export interface AdminPoiPayload {
  cityId: number;
  subMapId?: number | null;
  storylineId?: number | null;
  code: string;
  nameZh: string;
  nameEn?: string;
  nameZht?: string;
  namePt?: string;
  subtitleZh?: string;
  subtitleEn?: string;
  subtitleZht?: string;
  subtitlePt?: string;
  sourceCoordinateSystem?: CoordinateSystem;
  sourceLatitude?: number | null;
  sourceLongitude?: number | null;
  latitude?: number | null;
  longitude?: number | null;
  addressZh?: string;
  addressEn?: string;
  addressZht?: string;
  addressPt?: string;
  triggerRadius?: number | null;
  manualCheckinRadius?: number | null;
  staySeconds?: number | null;
  categoryCode?: string;
  difficulty?: string;
  districtZh?: string;
  districtEn?: string;
  districtZht?: string;
  districtPt?: string;
  coverAssetId?: number | null;
  mapIconAssetId?: number | null;
  audioAssetId?: number | null;
  descriptionZh?: string;
  descriptionEn?: string;
  descriptionZht?: string;
  descriptionPt?: string;
  introTitleZh?: string;
  introTitleEn?: string;
  introTitleZht?: string;
  introTitlePt?: string;
  introSummaryZh?: string;
  introSummaryEn?: string;
  introSummaryZht?: string;
  introSummaryPt?: string;
  popupConfigJson?: string;
  displayConfigJson?: string;
  attachments?: AdminSpatialAssetLinkItem[];
  status?: string;
  sortOrder?: number | null;
  publishedAt?: string | null;
}

export interface AdminUserListItem {
  userId: number;
  openId: string;
  nickname: string;
  avatarUrl?: string;
  isTestAccount: boolean;
  accountStatus: string;
  level: number;
  totalStamps: number;
  currentStorylineId?: number | null;
  currentStorylineName?: string | null;
  createdAt?: string;
  lastLoginAt?: string;
}

export interface AdminUserDetail {
  basicInfo: AdminUserListItem;
  progress: {
    level: number;
    currentExp: number;
    nextLevelExp: number;
    totalStamps: number;
    totalBadges: number;
    unlockedStorylines: number;
    completedStorylines: number;
  };
  activeStorylines: Array<{
    storylineId: number;
    name: string;
    currentPoiId?: number;
    currentPoiName?: string;
    completedPoiCount: number;
    totalPoiCount: number;
    progressPercent: number;
    startedAt?: string;
  }>;
  recentCheckIns: Array<{
    checkInId: number;
    poiName: string;
    checkInType: string;
    rewardGranted: boolean;
    createdAt?: string;
  }>;
}

export interface CityItem {
  id: number;
  code: string;
  nameZh: string;
  nameEn?: string;
  nameZht?: string;
  namePt?: string;
  subtitleZh?: string;
  subtitleEn?: string;
  subtitleZht?: string;
  subtitlePt?: string;
  countryCode?: string;
  customCountryName?: string;
  sourceCoordinateSystem?: CoordinateSystem;
  sourceCenterLat?: number | null;
  sourceCenterLng?: number | null;
  centerLat?: number | null;
  centerLng?: number | null;
  defaultZoom?: number | null;
  unlockType?: string;
  unlockConditionJson?: string;
  coverAssetId?: number | null;
  bannerAssetId?: number | null;
  descriptionZh?: string;
  descriptionEn?: string;
  descriptionZht?: string;
  descriptionPt?: string;
  popupConfigJson?: string;
  displayConfigJson?: string;
  subMaps?: AdminSubMapItem[];
  attachments?: AdminSpatialAssetLinkItem[];
  sortOrder?: number | null;
  status?: string;
  publishedAt?: string | null;
}

export interface AdminPoiListItem {
  poiId: number;
  cityId: number;
  cityName?: string;
  subMapId?: number | null;
  subMapCode?: string | null;
  subMapName?: string | null;
  storylineId?: number | null;
  storylineName?: string | null;
  code: string;
  nameZh: string;
  nameEn?: string;
  nameZht?: string;
  namePt?: string;
  subtitleZh?: string;
  subtitleEn?: string;
  subtitleZht?: string;
  subtitlePt?: string;
  categoryCode?: string;
  difficulty?: string;
  sourceCoordinateSystem?: CoordinateSystem;
  latitude: number;
  longitude: number;
  status?: string;
  sortOrder?: number;
  coverAssetId?: number | null;
  mapIconAssetId?: number | null;
  createdAt?: string;
}

export interface AdminPoiDetail extends AdminPoiListItem {
  sourceLatitude?: number | null;
  sourceLongitude?: number | null;
  addressZh?: string;
  addressEn?: string;
  addressZht?: string;
  addressPt?: string;
  triggerRadius?: number;
  manualCheckinRadius?: number;
  staySeconds?: number;
  districtZh?: string;
  districtEn?: string;
  districtZht?: string;
  districtPt?: string;
  audioAssetId?: number | null;
  descriptionZh?: string;
  descriptionEn?: string;
  descriptionZht?: string;
  descriptionPt?: string;
  introTitleZh?: string;
  introTitleEn?: string;
  introTitleZht?: string;
  introTitlePt?: string;
  introSummaryZh?: string;
  introSummaryEn?: string;
  introSummaryZht?: string;
  introSummaryPt?: string;
  popupConfigJson?: string;
  displayConfigJson?: string;
  attachments?: AdminSpatialAssetLinkItem[];
  publishedAt?: string | null;
  updatedAt?: string;
}

export interface AdminStorylineListItem {
  storylineId: number;
  cityId?: number | null;
  cityName?: string | null;
  code: string;
  nameZh: string;
  nameEn?: string;
  nameZht?: string;
  namePt?: string;
  difficulty?: string;
  status?: string;
  estimatedMinutes?: number;
  totalChapters?: number;
  coverAssetId?: number | null;
  sortOrder?: number;
  createdAt?: string;
}

export interface AdminStorylineDetail extends AdminStorylineListItem {
  descriptionZh?: string;
  descriptionEn?: string;
  descriptionZht?: string;
  descriptionPt?: string;
  bannerAssetId?: number | null;
  rewardBadgeZh?: string;
  rewardBadgeEn?: string;
  rewardBadgeZht?: string;
  rewardBadgePt?: string;
  publishedAt?: string | null;
  updatedAt?: string;
}

export interface AdminStoryChapterItem {
  id: number;
  storylineId: number;
  chapterOrder: number;
  titleZh: string;
  titleEn?: string;
  titleZht?: string;
  titlePt?: string;
  summaryZh?: string;
  summaryEn?: string;
  summaryZht?: string;
  summaryPt?: string;
  detailZh?: string;
  detailEn?: string;
  detailZht?: string;
  detailPt?: string;
  achievementZh?: string;
  achievementEn?: string;
  achievementZht?: string;
  achievementPt?: string;
  collectibleZh?: string;
  collectibleEn?: string;
  collectibleZht?: string;
  collectiblePt?: string;
  locationNameZh?: string;
  locationNameEn?: string;
  locationNameZht?: string;
  locationNamePt?: string;
  mediaAssetId?: number | null;
  unlockType?: string;
  unlockParamJson?: string;
  status?: string;
  sortOrder?: number;
  publishedAt?: string | null;
  createdAt?: string;
  updatedAt?: string;
}

export interface AdminRuntimeSettingItem {
  id: number;
  settingGroup: string;
  settingKey: string;
  localeCode?: string;
  titleZh?: string;
  titleEn?: string;
  titleZht?: string;
  titlePt?: string;
  valueJson?: string;
  valueText?: string;
  descriptionZh?: string;
  descriptionEn?: string;
  descriptionZht?: string;
  descriptionPt?: string;
  assetId?: number | null;
  status?: string;
  sortOrder?: number;
  publishedAt?: string | null;
  updatedAt?: string;
}

export interface AdminContentAssetItem {
  id: number;
  assetKind: string;
  bucketName?: string;
  region?: string;
  objectKey?: string;
  canonicalUrl?: string;
  mimeType?: string;
  localeCode?: string;
  fileSizeBytes?: number;
  widthPx?: number;
  heightPx?: number;
  checksum?: string;
  etag?: string;
  status?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface AdminAssetUploadPayload {
  file: File;
  assetKind: string;
  localeCode?: string;
  status?: string;
}

export interface AdminTipArticleItem {
  id: number;
  cityId?: number | null;
  cityName?: string | null;
  code: string;
  categoryCode?: string;
  titleZh?: string;
  titleEn?: string;
  titleZht?: string;
  titlePt?: string;
  summaryZh?: string;
  summaryEn?: string;
  summaryZht?: string;
  summaryPt?: string;
  contentZh?: string;
  contentEn?: string;
  contentZht?: string;
  contentPt?: string;
  authorDisplayName?: string;
  locationNameZh?: string;
  locationNameEn?: string;
  locationNameZht?: string;
  locationNamePt?: string;
  tagsJson?: string;
  coverAssetId?: number | null;
  sourceType?: string;
  status?: string;
  sortOrder?: number;
  publishedAt?: string | null;
  updatedAt?: string;
}

export interface AdminNotificationItem {
  id: number;
  code: string;
  titleZh?: string;
  titleEn?: string;
  titleZht?: string;
  titlePt?: string;
  contentZh?: string;
  contentEn?: string;
  contentZht?: string;
  contentPt?: string;
  notificationType?: string;
  targetScope?: string;
  coverAssetId?: number | null;
  actionUrl?: string;
  status?: string;
  sortOrder?: number;
  publishStartAt?: string | null;
  publishEndAt?: string | null;
  updatedAt?: string;
}

export interface AdminStampItem {
  id: number;
  code: string;
  nameZh?: string;
  nameEn?: string;
  nameZht?: string;
  namePt?: string;
  descriptionZh?: string;
  descriptionEn?: string;
  descriptionZht?: string;
  descriptionPt?: string;
  stampType?: string;
  rarity?: string;
  iconAssetId?: number | null;
  relatedPoiId?: number | null;
  relatedPoiName?: string | null;
  relatedStorylineId?: number | null;
  relatedStorylineName?: string | null;
  status?: string;
  sortOrder?: number;
  publishedAt?: string | null;
  updatedAt?: string;
}

export interface AdminTestAccountListItem {
  id: number;
  userId: number;
  openId?: string;
  nickname: string;
  avatar?: string;
  remark?: string;
  testGroup: string;
  mockLocation?: {
    latitude?: number;
    longitude?: number;
    address?: string;
  };
  isMockEnabled: boolean;
  stampCount: number;
  level: number;
  levelName: string;
  experience: number;
  createTime?: string;
  lastOperationTime?: string;
}

export interface AdminTestStampSummary {
  testAccountId: number;
  userId: number;
  stampCount: number;
  currentLevel: number;
  levelName: string;
  nextLevelTarget: number;
  remainingToNextLevel: number;
  maxStamps: number;
}

export interface DashboardStats {
  totalUsers: number;
  totalStamps: number;
  poiCount: number;
  weeklyGrowth: number;
  activeUsers: number;
  storyLines: number;
  activities: number;
  rewards: number;
  testAccounts: number;
  recentActivities: Array<{
    id: number;
    type: string;
    user: string;
    action: string;
    time: string;
  }>;
  systemStatus: {
    database: boolean;
    api: boolean;
    cloudRun: boolean;
  };
  contentSummary?: {
    publishedCities: number;
    publishedStoryLines: number;
    publishedStoryChapters: number;
    publishedPois: number;
    publishedStamps: number;
    publishedRewards: number;
    publishedTips: number;
    publishedNotifications: number;
    publishedRuntimeSettings: number;
  };
  integrationHealth?: {
    database?: {
      healthy: boolean;
      status: string;
      detail: string;
      latencyMs?: number;
      checkedAt?: string;
    };
    publicApi?: {
      healthy: boolean;
      status: string;
      detail: string;
      latencyMs?: number;
      checkedAt?: string;
    };
    cos?: {
      healthy: boolean;
      status: string;
      detail: string;
      latencyMs?: number;
      checkedAt?: string;
    };
    seedMigration?: {
      seedKey: string;
      status: string;
      executedAt?: string | null;
      notes?: string | null;
    };
  };
}

export interface AdminActivityItem {
  id: number;
  code: string;
  title: string;
  description?: string;
  coverUrl?: string;
  startTime?: string;
  endTime?: string;
  status?: string;
  participationCount?: number;
  createdAt?: string;
}

export interface AdminRewardItem {
  id: number;
  code: string;
  nameZh: string;
  nameEn?: string;
  nameZht?: string;
  namePt?: string;
  subtitleZh?: string;
  subtitleEn?: string;
  subtitleZht?: string;
  subtitlePt?: string;
  descriptionZh?: string;
  descriptionEn?: string;
  descriptionZht?: string;
  descriptionPt?: string;
  highlightZh?: string;
  highlightEn?: string;
  highlightZht?: string;
  highlightPt?: string;
  stampCost?: number;
  inventoryTotal?: number;
  inventoryRedeemed?: number;
  inventoryRemaining?: number;
  coverAssetId?: number | null;
  status?: string;
  sortOrder?: number;
  publishStartAt?: string | null;
  publishEndAt?: string | null;
  createdAt?: string;
}

export interface AdminSystemConfigItem {
  id: number;
  configKey: string;
  configValue?: string;
  configType?: string;
  description?: string;
  updatedAt?: string;
}

export interface AdminMapTileItem {
  id: number;
  mapId: string;
  style?: string;
  cdnBase?: string;
  controlPointsUrl?: string;
  poisUrl?: string;
  zoomLevels?: string;
  zoomMin?: number;
  zoomMax?: number;
  defaultZoom?: number;
  centerLat?: number;
  centerLng?: number;
  version?: string;
  status?: string;
  updatedAt?: string;
}

export interface AdminOperationLog {
  id: number;
  operationType: string;
  operationTypeName: string;
  operationDesc?: string;
  adminName?: string;
  ipAddress?: string;
  createTime?: string;
}
