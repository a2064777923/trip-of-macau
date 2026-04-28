export interface AdminCurrentUser {
  userId: number;
  username: string;
  realName?: string;
  email?: string;
  allowLosslessUpload?: boolean;
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

export interface AdminMediaPolicyKindSettings {
  maxFileSizeBytes?: number;
  preferredPolicyCode?: string;
  qualityPercent?: number;
  maxWidthPx?: number | null;
  maxHeightPx?: number | null;
  preserveMetadata?: boolean;
  note?: string;
}

export interface AdminMediaPolicySettings {
  maxBatchCount: number;
  maxBatchTotalBytes: number;
  image: AdminMediaPolicyKindSettings;
  video: AdminMediaPolicyKindSettings;
  audio: AdminMediaPolicyKindSettings;
  file: AdminMediaPolicyKindSettings;
}

export type AdminMediaPolicySettingsUpdatePayload = AdminMediaPolicySettings;

export interface AdminIndoorRuntimeSettings {
  minScaleMeters: number;
  maxScaleMeters: number;
  referenceViewportPx: number;
  defaultTileSizePx: number;
  indoorZoomDefaultMinScale?: number;
  indoorZoomDefaultMaxScale?: number;
}

export type AdminIndoorRuntimeSettingsUpdatePayload = AdminIndoorRuntimeSettings;

export interface AdminCarryoverSettings {
  translationDefaultLocale: SupportedLocale;
  translationEnginePriority: string[];
  mediaUploadDefaultPolicyCode: string;
  mapZoomDefaultMinScale: number;
  mapZoomDefaultMaxScale: number;
  indoorZoomDefaultMinScale: number;
  indoorZoomDefaultMaxScale: number;
}

export type AdminCarryoverSettingsUpdatePayload = AdminCarryoverSettings;

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

export type IndoorBindingMode = 'map' | 'poi';

export interface AdminIndoorFloorItem {
  id: number;
  buildingId: number;
  indoorMapId?: number | null;
  floorCode?: string;
  floorNumber: number;
  floorNameZh: string;
  floorNameEn?: string;
  floorNameZht?: string;
  floorNamePt?: string;
  descriptionZh?: string;
  descriptionEn?: string;
  descriptionZht?: string;
  descriptionPt?: string;
  coverAssetId?: number | null;
  floorPlanAssetId?: number | null;
  floorPlanUrl?: string | null;
  tileSourceType?: string | null;
  tileSourceAssetId?: number | null;
  tileSourceFilename?: string | null;
  tilePreviewImageUrl?: string | null;
  tileRootUrl?: string | null;
  tileManifestJson?: string | null;
  tileZoomDerivationJson?: string | null;
  imageWidthPx?: number | null;
  imageHeightPx?: number | null;
  tileSizePx?: number | null;
  gridCols?: number | null;
  gridRows?: number | null;
  tileLevelCount?: number | null;
  tileEntryCount?: number | null;
  importStatus?: string | null;
  importNote?: string | null;
  importedAt?: string | null;
  altitudeMeters?: number | null;
  areaSqm?: number | null;
  zoomMin?: number | null;
  zoomMax?: number | null;
  defaultZoom?: number | null;
  popupConfigJson?: string;
  displayConfigJson?: string;
  attachments?: AdminSpatialAssetLinkItem[];
  attachmentAssetIds?: number[];
  markerCount?: number | null;
  sortOrder?: number | null;
  status?: string;
  publishedAt?: string | null;
  createdAt?: string;
  updatedAt?: string;
  nodes?: AdminIndoorNodeItem[];
  markers?: AdminIndoorMarkerItem[];
}

export interface AdminIndoorBuildingItem {
  id: number;
  buildingCode: string;
  bindingMode: IndoorBindingMode;
  cityId: number;
  cityCode?: string;
  cityName?: string;
  subMapId?: number | null;
  subMapCode?: string | null;
  subMapName?: string | null;
  poiId?: number | null;
  poiName?: string | null;
  nameZh: string;
  nameEn?: string;
  nameZht?: string;
  namePt?: string;
  addressZh?: string;
  addressEn?: string;
  addressZht?: string;
  addressPt?: string;
  sourceCoordinateSystem?: CoordinateSystem;
  sourceLatitude?: number | null;
  sourceLongitude?: number | null;
  lat?: number | null;
  lng?: number | null;
  totalFloors?: number | null;
  basementFloors?: number | null;
  floorCount?: number | null;
  coverAssetId?: number | null;
  coverImageUrl?: string | null;
  status?: string;
  sortOrder?: number | null;
  publishedAt?: string | null;
  createdAt?: string;
  updatedAt?: string;
}

export interface AdminIndoorBuildingDetail extends AdminIndoorBuildingItem {
  descriptionZh?: string;
  descriptionEn?: string;
  descriptionZht?: string;
  descriptionPt?: string;
  popupConfigJson?: string;
  displayConfigJson?: string;
  attachments?: AdminSpatialAssetLinkItem[];
  attachmentAssetIds?: number[];
  floors?: AdminIndoorFloorItem[];
}

export interface AdminIndoorBuildingPayload {
  buildingCode: string;
  bindingMode?: IndoorBindingMode;
  cityId: number;
  subMapId?: number | null;
  poiId?: number | null;
  nameZh: string;
  nameEn?: string;
  nameZht?: string;
  namePt?: string;
  addressZh?: string;
  addressEn?: string;
  addressZht?: string;
  addressPt?: string;
  sourceCoordinateSystem?: CoordinateSystem;
  sourceLatitude?: number | null;
  sourceLongitude?: number | null;
  lat?: number | null;
  lng?: number | null;
  totalFloors?: number | null;
  basementFloors?: number | null;
  coverImageUrl?: string | null;
  coverAssetId?: number | null;
  descriptionZh?: string;
  descriptionEn?: string;
  descriptionZht?: string;
  descriptionPt?: string;
  popupConfigJson?: string;
  displayConfigJson?: string;
  attachments?: AdminSpatialAssetLinkItem[];
  attachmentAssetIds?: number[];
  status?: string;
  sortOrder?: number | null;
  publishedAt?: string | null;
}

export interface AdminIndoorFloorPayload {
  indoorMapId?: number | null;
  floorCode?: string;
  floorNumber: number;
  floorNameZh: string;
  floorNameEn?: string;
  floorNameZht?: string;
  floorNamePt?: string;
  descriptionZh?: string;
  descriptionEn?: string;
  descriptionZht?: string;
  descriptionPt?: string;
  coverAssetId?: number | null;
  floorPlanAssetId?: number | null;
  tilePreviewImageUrl?: string | null;
  altitudeMeters?: number | null;
  areaSqm?: number | null;
  zoomMin?: number | null;
  zoomMax?: number | null;
  defaultZoom?: number | null;
  popupConfigJson?: string;
  displayConfigJson?: string;
  attachments?: AdminSpatialAssetLinkItem[];
  attachmentAssetIds?: number[];
  sortOrder?: number | null;
  status?: string;
  publishedAt?: string | null;
}

export interface AdminIndoorTilePreview {
  floorId: number;
  sourceType: string;
  sourceFilename?: string;
  imageWidthPx?: number | null;
  imageHeightPx?: number | null;
  tileSizePx?: number | null;
  gridCols?: number | null;
  gridRows?: number | null;
  tileLevelCount?: number | null;
  tileEntryCount?: number | null;
  zoomMin?: number | null;
  defaultZoom?: number | null;
  zoomMax?: number | null;
  derivationJson?: string;
  manifestJson?: string;
  notes?: string[];
}

export interface AdminIndoorNodePoint {
  x?: number | null;
  y?: number | null;
  order?: number | null;
}

export interface AdminIndoorOverlayGeometry {
  geometryType?: 'point' | 'polyline' | 'polygon' | string;
  points?: AdminIndoorNodePoint[];
  properties?: Record<string, any> | null;
}

export interface AdminIndoorRuleCondition {
  id?: string;
  category?: string;
  label?: string;
  config?: Record<string, any> | null;
}

export interface AdminIndoorTriggerStep {
  id?: string;
  category?: string;
  label?: string;
  dependsOnTriggerId?: string;
  config?: Record<string, any> | null;
}

export interface AdminIndoorEffectDefinition {
  id?: string;
  category?: string;
  label?: string;
  config?: Record<string, any> | null;
}

export interface AdminIndoorPathGraph {
  points?: AdminIndoorNodePoint[];
  durationMs?: number | null;
  holdMs?: number | null;
  loop?: boolean;
  easing?: string;
}

export interface AdminIndoorBehaviorProfile {
  behaviorCode?: string;
  behaviorNameZh?: string;
  behaviorNameEn?: string;
  behaviorNameZht?: string;
  behaviorNamePt?: string;
  appearancePresetCode?: string;
  triggerTemplateCode?: string;
  effectTemplateCode?: string;
  appearanceRules?: AdminIndoorRuleCondition[];
  triggerRules?: AdminIndoorTriggerStep[];
  effectRules?: AdminIndoorEffectDefinition[];
  rewardRuleIds?: number[];
  linkedRewardRules?: AdminRewardRuleLinkItem[];
  pathGraph?: AdminIndoorPathGraph | null;
  overlayGeometry?: AdminIndoorOverlayGeometry | null;
  inheritMode?: string;
  runtimeSupportLevel?: string;
  sortOrder?: number | null;
  status?: string;
}

export interface AdminIndoorNodeItem {
  id: number;
  buildingId: number;
  floorId: number;
  markerCode?: string;
  nodeType?: string;
  presentationMode?: string;
  overlayType?: string | null;
  nodeNameZh: string;
  nodeNameEn?: string;
  nodeNameZht?: string;
  nodeNamePt?: string;
  descriptionZh?: string;
  descriptionEn?: string;
  descriptionZht?: string;
  descriptionPt?: string;
  relativeX?: number | null;
  relativeY?: number | null;
  relatedPoiId?: number | null;
  iconAssetId?: number | null;
  iconUrl?: string | null;
  animationAssetId?: number | null;
  animationUrl?: string | null;
  linkedEntityType?: string | null;
  linkedEntityId?: number | null;
  tags?: string[];
  tagsJson?: string | null;
  popupConfigJson?: string | null;
  displayConfigJson?: string | null;
  overlayGeometry?: AdminIndoorOverlayGeometry | null;
  overlayGeometryJson?: string | null;
  inheritLinkedEntityRules?: boolean;
  runtimeSupportLevel?: string;
  metadataJson?: string | null;
  importBatchId?: number | null;
  sortOrder?: number | null;
  status?: string;
  behaviors?: AdminIndoorBehaviorProfile[];
  createdAt?: string;
  updatedAt?: string;
}

export type AdminIndoorMarkerItem = AdminIndoorNodeItem;

export interface AdminIndoorNodePayload {
  markerCode?: string;
  nodeType?: string;
  presentationMode?: string;
  overlayType?: string | null;
  nodeNameZh: string;
  nodeNameEn?: string;
  nodeNameZht?: string;
  nodeNamePt?: string;
  descriptionZh?: string;
  descriptionEn?: string;
  descriptionZht?: string;
  descriptionPt?: string;
  relativeX?: number | null;
  relativeY?: number | null;
  relatedPoiId?: number | null;
  iconAssetId?: number | null;
  animationAssetId?: number | null;
  linkedEntityType?: string | null;
  linkedEntityId?: number | null;
  tags?: string[];
  tagsJson?: string | null;
  popupConfigJson?: string | null;
  displayConfigJson?: string | null;
  overlayGeometry?: AdminIndoorOverlayGeometry | null;
  inheritLinkedEntityRules?: boolean;
  runtimeSupportLevel?: string;
  metadataJson?: string | null;
  behaviors?: AdminIndoorBehaviorProfile[];
  sortOrder?: number | null;
  status?: string;
}

export type AdminIndoorMarkerPayload = AdminIndoorNodePayload;

export interface AdminIndoorMarkerCsvPreviewRow {
  rowNumber: number;
  markerCode?: string;
  nodeType?: string;
  nodeNameZh?: string;
  nodeNameEn?: string;
  nodeNameZht?: string;
  nodeNamePt?: string;
  descriptionZh?: string;
  descriptionEn?: string;
  descriptionZht?: string;
  descriptionPt?: string;
  relativeX?: number | null;
  relativeY?: number | null;
  relatedPoiId?: number | null;
  iconAssetId?: number | null;
  animationAssetId?: number | null;
  linkedEntityType?: string | null;
  linkedEntityId?: number | null;
  tagsJson?: string | null;
  popupConfigJson?: string | null;
  displayConfigJson?: string | null;
  metadataJson?: string | null;
  sortOrder?: number | null;
  status?: string;
  presentationMode?: string;
  appearancePresetCode?: string | null;
  triggerTemplateCode?: string | null;
  effectTemplateCode?: string | null;
  inheritMode?: string | null;
  valid: boolean;
  errors: string[];
}

export interface AdminIndoorMarkerCsvPreview {
  floorId: number;
  sourceFilename?: string;
  totalRows: number;
  validRows: number;
  invalidRows: number;
  rows: AdminIndoorMarkerCsvPreviewRow[];
}

export interface AdminIndoorMarkerCsvImportResult {
  batchId: number;
  floorId: number;
  totalRows: number;
  importedRows: number;
  skippedRows: number;
  markers: AdminIndoorMarkerItem[];
}

export interface AdminIndoorRuleValidationResponse {
  valid: boolean;
  errors: string[];
  warnings?: string[];
  normalizedRelativeX?: number | null;
  normalizedRelativeY?: number | null;
  resolvedOverlayType?: string | null;
  behaviorCount?: number | null;
}

export interface AdminIndoorRuleOverviewQuery {
  keyword?: string;
  cityId?: number;
  buildingId?: number;
  floorId?: number;
  relatedPoiId?: number;
  linkedEntityType?: string;
  linkedEntityId?: number;
  status?: string;
  runtimeSupportLevel?: string;
  conflictOnly?: boolean;
  enabledOnly?: boolean;
}

export interface AdminIndoorRuleConflictItem {
  behaviorId: number;
  nodeId: number;
  buildingId: number;
  floorId: number;
  relatedBehaviorId?: number | null;
  relatedNodeId?: number | null;
  conflictCode: string;
  severity: string;
  message: string;
}

export interface AdminIndoorRuleGovernanceItem {
  nodeId: number;
  behaviorId: number;
  behaviorCode?: string;
  behaviorNameZh?: string;
  behaviorNameZht?: string;
  behaviorNameEn?: string;
  behaviorNamePt?: string;
  markerCode?: string;
  presentationMode?: string;
  overlayType?: string | null;
  buildingId: number;
  buildingNameZht?: string;
  floorId: number;
  floorCode?: string;
  linkedEntityType?: string | null;
  linkedEntityId?: number | null;
  runtimeSupportLevel?: string;
  status?: string;
  appearanceRuleCount?: number;
  triggerRuleCount?: number;
  effectRuleCount?: number;
  hasPathGraph?: boolean;
  conflictCount?: number;
}

export interface AdminIndoorRuleGovernanceDetail extends AdminIndoorRuleGovernanceItem {
  appearanceRules: AdminIndoorRuleCondition[];
  triggerRules: AdminIndoorTriggerStep[];
  effectRules: AdminIndoorEffectDefinition[];
  pathGraph?: AdminIndoorPathGraph | null;
  conflicts: AdminIndoorRuleConflictItem[];
  parentNode: {
    nodeId: number;
    markerCode?: string;
    nodeNameZht?: string;
    nodeStatus?: string;
    presentationMode?: string;
    overlayType?: string | null;
    buildingId: number;
    buildingNameZht?: string;
    floorId: number;
    floorCode?: string;
    relatedPoiId?: number | null;
  };
}

export interface AdminIndoorRuleStatusUpdateResult {
  behaviorId: number;
  status?: string;
  parentNodeStatus?: string;
  warnings: string[];
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
  cityProgress?: {
    completedCount: number;
    totalCount: number;
    progressPercent: number;
    summary: string;
  };
  subMapProgress?: {
    completedCount: number;
    totalCount: number;
    progressPercent: number;
    summary: string;
  };
  collectibleProgress?: {
    completedCount: number;
    totalCount: number;
    progressPercent: number;
    summary: string;
  };
  badgeProgress?: {
    completedCount: number;
    totalCount: number;
    progressPercent: number;
    summary: string;
  };
  rewardProgress?: {
    completedCount: number;
    totalCount: number;
    progressPercent: number;
    summary: string;
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
  recentTriggerLogs?: Array<{
    triggerLogId: number;
    poiName: string;
    triggerType: string;
    distanceMeters?: string;
    gpsAccuracyMeters?: string;
    wifiUsed?: boolean;
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
  cityBindings?: number[];
  subMapBindings?: number[];
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
  attachmentAssetIds?: number[];
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
  experienceFlowId?: number | null;
  overridePolicyJson?: string;
  storyModeConfigJson?: string;
  anchorType?: string;
  anchorTargetId?: number | null;
  anchorTargetCode?: string;
  anchorTargetLabel?: string;
  unlockType?: string;
  unlockParamJson?: string;
  prerequisiteJson?: string;
  completionJson?: string;
  rewardJson?: string;
  status?: string;
  sortOrder?: number;
  publishedAt?: string | null;
  createdAt?: string;
  updatedAt?: string;
  contentBlocks?: AdminStoryChapterContentBlockLinkItem[];
}

export interface AdminStorylinePayload {
  cityId?: number | null;
  cityBindings?: number[];
  subMapBindings?: number[];
  attachmentAssetIds?: number[];
  code: string;
  nameZh: string;
  nameEn?: string;
  nameZht?: string;
  namePt?: string;
  descriptionZh?: string;
  descriptionEn?: string;
  descriptionZht?: string;
  descriptionPt?: string;
  estimatedMinutes?: number;
  difficulty?: string;
  coverAssetId?: number | null;
  bannerAssetId?: number | null;
  rewardBadgeZh?: string;
  rewardBadgeEn?: string;
  rewardBadgeZht?: string;
  rewardBadgePt?: string;
  status?: string;
  sortOrder?: number;
  publishedAt?: string | null;
}

export interface AdminStoryChapterPayload {
  storylineId?: number;
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
  experienceFlowId?: number | null;
  overridePolicyJson?: string;
  storyModeConfigJson?: string;
  anchorType?: string;
  anchorTargetId?: number | null;
  anchorTargetCode?: string;
  unlockType?: string;
  unlockParamJson?: string;
  prerequisiteJson?: string;
  completionJson?: string;
  rewardJson?: string;
  status?: string;
  sortOrder?: number;
  publishedAt?: string | null;
  contentBlocks?: AdminStoryChapterContentBlockLinkPayload[];
}

export interface AdminStoryContentBlockItem {
  id: number;
  code: string;
  blockType: string;
  titleZh?: string;
  titleEn?: string;
  titleZht?: string;
  titlePt?: string;
  summaryZh?: string;
  summaryEn?: string;
  summaryZht?: string;
  summaryPt?: string;
  bodyZh?: string;
  bodyEn?: string;
  bodyZht?: string;
  bodyPt?: string;
  primaryAssetId?: number | null;
  attachmentAssetIds?: number[];
  stylePreset?: string;
  displayMode?: string;
  visibilityJson?: string;
  configJson?: string;
  status?: string;
  sortOrder?: number;
  publishedAt?: string | null;
  createdAt?: string;
  updatedAt?: string;
}

export interface AdminStoryContentBlockPayload {
  code?: string;
  blockType: string;
  titleZh?: string;
  titleEn?: string;
  titleZht?: string;
  titlePt?: string;
  summaryZh?: string;
  summaryEn?: string;
  summaryZht?: string;
  summaryPt?: string;
  bodyZh?: string;
  bodyEn?: string;
  bodyZht?: string;
  bodyPt?: string;
  primaryAssetId?: number | null;
  attachmentAssetIds?: number[];
  stylePreset?: string;
  displayMode?: string;
  visibilityJson?: string;
  configJson?: string;
  status?: string;
  sortOrder?: number;
  publishedAt?: string | null;
}

export interface AdminStoryChapterContentBlockLinkItem {
  id?: number;
  chapterId?: number;
  blockId: number;
  overrideTitleJson?: string;
  overrideSummaryJson?: string;
  overrideBodyJson?: string;
  displayConditionJson?: string;
  overrideConfigJson?: string;
  status?: string;
  sortOrder?: number;
  block?: AdminStoryContentBlockItem;
}

export interface AdminStoryChapterContentBlockLinkPayload {
  id?: number;
  blockId: number;
  overrideTitleJson?: string;
  overrideSummaryJson?: string;
  overrideBodyJson?: string;
  displayConditionJson?: string;
  overrideConfigJson?: string;
  status?: string;
  sortOrder?: number;
}

export interface AdminExperienceTemplateItem {
  id: number;
  code: string;
  templateType: string;
  category?: string;
  nameZh: string;
  nameEn?: string;
  nameZht?: string;
  namePt?: string;
  summaryZh?: string;
  summaryEn?: string;
  summaryZht?: string;
  summaryPt?: string;
  configJson?: string;
  schemaJson?: string;
  riskLevel?: string;
  status?: string;
  sortOrder?: number;
  createdAt?: string;
  updatedAt?: string;
  usageCount?: number;
}

export interface AdminExperienceTemplatePayload {
  code?: string;
  templateType: string;
  category?: string;
  nameZh: string;
  nameEn?: string;
  nameZht?: string;
  namePt?: string;
  summaryZh?: string;
  summaryEn?: string;
  summaryZht?: string;
  summaryPt?: string;
  configJson?: string;
  schemaJson?: string;
  riskLevel?: string;
  status?: string;
  sortOrder?: number;
}

export interface AdminExperienceStepItem {
  id: number;
  flowId: number;
  stepCode: string;
  stepType: string;
  templateId?: number | null;
  template?: AdminExperienceTemplateItem | null;
  stepNameZh: string;
  stepNameEn?: string;
  stepNameZht?: string;
  stepNamePt?: string;
  descriptionZh?: string;
  descriptionEn?: string;
  descriptionZht?: string;
  descriptionPt?: string;
  triggerType?: string;
  triggerConfigJson?: string;
  conditionConfigJson?: string;
  effectConfigJson?: string;
  mediaAssetId?: number | null;
  rewardRuleIdsJson?: string;
  explorationWeightLevel?: string;
  requiredForCompletion?: boolean;
  inheritKey?: string;
  status?: string;
  sortOrder?: number;
  createdAt?: string;
  updatedAt?: string;
}

export interface AdminExperienceStepPayload {
  stepCode?: string;
  stepType: string;
  templateId?: number | null;
  stepNameZh: string;
  stepNameEn?: string;
  stepNameZht?: string;
  stepNamePt?: string;
  descriptionZh?: string;
  descriptionEn?: string;
  descriptionZht?: string;
  descriptionPt?: string;
  triggerType?: string;
  triggerConfigJson?: string;
  conditionConfigJson?: string;
  effectConfigJson?: string;
  mediaAssetId?: number | null;
  rewardRuleIdsJson?: string;
  explorationWeightLevel?: string;
  requiredForCompletion?: boolean;
  inheritKey?: string;
  status?: string;
  sortOrder?: number;
}

export interface AdminExperienceBindingItem {
  id: number;
  ownerType: string;
  ownerId?: number | null;
  ownerCode?: string;
  bindingRole?: string;
  flowId: number;
  flowName?: string;
  priority?: number;
  inheritPolicy?: string;
  status?: string;
  sortOrder?: number;
  createdAt?: string;
  updatedAt?: string;
}

export interface AdminExperienceBindingPayload {
  ownerType: string;
  ownerId?: number | null;
  ownerCode?: string;
  bindingRole?: string;
  flowId: number;
  priority?: number;
  inheritPolicy?: string;
  status?: string;
  sortOrder?: number;
}

export interface AdminExperienceOverrideItem {
  id: number;
  ownerType: string;
  ownerId: number;
  targetOwnerType?: string;
  targetOwnerId?: number | null;
  targetStepCode?: string;
  overrideMode?: string;
  replacementStepId?: number | null;
  overrideConfigJson?: string;
  status?: string;
  sortOrder?: number;
  createdAt?: string;
  updatedAt?: string;
}

export interface AdminExperienceOverridePayload {
  ownerType: string;
  ownerId: number;
  targetOwnerType?: string;
  targetOwnerId?: number | null;
  targetStepCode?: string;
  overrideMode?: string;
  replacementStepId?: number | null;
  overrideConfigJson?: string;
  status?: string;
  sortOrder?: number;
}

export interface AdminExperienceFlowItem {
  id: number;
  code: string;
  flowType: string;
  mode?: string;
  nameZh: string;
  nameEn?: string;
  nameZht?: string;
  namePt?: string;
  descriptionZh?: string;
  descriptionEn?: string;
  descriptionZht?: string;
  descriptionPt?: string;
  mapPolicyJson?: string;
  advancedConfigJson?: string;
  status?: string;
  sortOrder?: number;
  publishedAt?: string | null;
  createdAt?: string;
  updatedAt?: string;
  steps?: AdminExperienceStepItem[];
  bindings?: AdminExperienceBindingItem[];
  overrides?: AdminExperienceOverrideItem[];
}

export interface AdminExperienceFlowPayload {
  code?: string;
  flowType?: string;
  mode?: string;
  nameZh: string;
  nameEn?: string;
  nameZht?: string;
  namePt?: string;
  descriptionZh?: string;
  descriptionEn?: string;
  descriptionZht?: string;
  descriptionPt?: string;
  mapPolicyJson?: string;
  advancedConfigJson?: string;
  status?: string;
  sortOrder?: number;
  publishedAt?: string | null;
}

export interface AdminExplorationElementItem {
  id: number;
  elementCode: string;
  elementType: string;
  ownerType: string;
  ownerId?: number | null;
  ownerCode?: string;
  cityId?: number | null;
  subMapId?: number | null;
  storylineId?: number | null;
  storyChapterId?: number | null;
  titleZh: string;
  titleEn?: string;
  titleZht?: string;
  titlePt?: string;
  weightLevel?: string;
  weightValue?: number;
  includeInExploration?: boolean;
  metadataJson?: string;
  status?: string;
  sortOrder?: number;
  createdAt?: string;
  updatedAt?: string;
}

export interface AdminExplorationElementPayload {
  elementCode?: string;
  elementType: string;
  ownerType: string;
  ownerId?: number | null;
  ownerCode?: string;
  cityId?: number | null;
  subMapId?: number | null;
  storylineId?: number | null;
  storyChapterId?: number | null;
  titleZh: string;
  titleEn?: string;
  titleZht?: string;
  titlePt?: string;
  weightLevel?: string;
  weightValue?: number;
  includeInExploration?: boolean;
  metadataJson?: string;
  status?: string;
  sortOrder?: number;
}

export interface AdminExperienceGovernanceFinding {
  severity: string;
  findingType: string;
  title: string;
  description?: string;
  ownerType?: string;
  ownerId?: number | null;
  flowId?: number | null;
  stepId?: number | null;
}

export interface AdminExperienceGovernanceOverview {
  templateCount: number;
  flowCount: number;
  bindingCount: number;
  overrideCount: number;
  explorationElementCount: number;
  highRiskTemplateCount: number;
  findings: AdminExperienceGovernanceFinding[];
}

export interface CollectibleItem {
  id: number;
  collectibleCode: string;
  nameZh: string;
  nameEn?: string | null;
  nameZht?: string | null;
  namePt?: string | null;
  descriptionZh?: string | null;
  descriptionEn?: string | null;
  descriptionZht?: string | null;
  descriptionPt?: string | null;
  collectibleType: string;
  rarity: string;
  coverAssetId?: number | null;
  iconAssetId?: number | null;
  animationAssetId?: number | null;
  imageUrl?: string | null;
  animationUrl?: string | null;
  seriesId?: number | null;
  acquisitionSource?: string | null;
  popupPresetCode?: string | null;
  popupConfigJson?: string | null;
  displayPresetCode?: string | null;
  displayConfigJson?: string | null;
  triggerPresetCode?: string | null;
  triggerConfigJson?: string | null;
  exampleContentZh?: string | null;
  exampleContentEn?: string | null;
  exampleContentZht?: string | null;
  exampleContentPt?: string | null;
  isRepeatable?: number;
  isLimited?: number;
  maxOwnership?: number;
  status?: string;
  sortOrder?: number | null;
  storylineBindings?: number[];
  cityBindings?: number[];
  subMapBindings?: number[];
  indoorBuildingBindings?: number[];
  indoorFloorBindings?: number[];
  attachmentAssetIds?: number[];
}

export interface BadgeItem {
  id: number;
  badgeCode: string;
  nameZh: string;
  nameEn?: string | null;
  nameZht?: string | null;
  namePt?: string | null;
  descriptionZh?: string | null;
  descriptionEn?: string | null;
  descriptionZht?: string | null;
  descriptionPt?: string | null;
  badgeType?: string;
  rarity?: string;
  isHidden?: number;
  coverAssetId?: number | null;
  iconAssetId?: number | null;
  animationAssetId?: number | null;
  iconUrl?: string | null;
  imageUrl?: string | null;
  animationUnlock?: string | null;
  popupPresetCode?: string | null;
  popupConfigJson?: string | null;
  displayPresetCode?: string | null;
  displayConfigJson?: string | null;
  triggerPresetCode?: string | null;
  triggerConfigJson?: string | null;
  exampleContentZh?: string | null;
  exampleContentEn?: string | null;
  exampleContentZht?: string | null;
  exampleContentPt?: string | null;
  status?: string;
  storylineBindings?: number[];
  cityBindings?: number[];
  subMapBindings?: number[];
  indoorBuildingBindings?: number[];
  indoorFloorBindings?: number[];
  attachmentAssetIds?: number[];
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
  originalFilename?: string;
  fileExtension?: string;
  uploadSource?: string;
  clientRelativePath?: string | null;
  uploadedByAdminId?: number | null;
  uploadedByAdminName?: string;
  fileSizeBytes?: number;
  widthPx?: number;
  heightPx?: number;
  animationSubtype?: string;
  posterAssetId?: number | null;
  fallbackAssetId?: number | null;
  defaultLoop?: boolean;
  defaultAutoplay?: boolean;
  checksum?: string;
  etag?: string;
  processingPolicyCode?: string;
  processingProfileJson?: string;
  processingStatus?: string;
  processingNote?: string;
  status?: string;
  publishedAt?: string | null;
  createdAt?: string;
  updatedAt?: string;
}

export interface AdminContentAssetUsageItem {
  relationType: string;
  entityType: string;
  entityId?: number | null;
  entityCode?: string | null;
  entityName?: string | null;
  usageType?: string | null;
  fieldName?: string | null;
  status?: string | null;
  title?: string | null;
}

export interface AdminContentAssetUsageSummary {
  assetId: number;
  usageCount: number;
  usages: AdminContentAssetUsageItem[];
}

export interface AdminAssetUploadPayload {
  file: File;
  assetKind?: string;
  localeCode?: string;
  status?: string;
  uploadSource?: string;
  clientRelativePath?: string;
}

export interface AdminAssetBatchUploadPayload {
  files: File[];
  assetKind?: string;
  localeCode?: string;
  status?: string;
  uploadSource?: string;
  clientRelativePaths?: string[];
}

export interface AdminAssetBatchUploadResponse {
  uploadedCount: number;
  failedCount: number;
  items: AdminContentAssetItem[];
  failures: Array<{
    originalFilename?: string;
    clientRelativePath?: string;
    message: string;
  }>;
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
  activityType?: string;
  title: string;
  description?: string;
  titleZh?: string;
  titleEn?: string;
  titleZht?: string;
  titlePt?: string;
  summaryZh?: string;
  summaryEn?: string;
  summaryZht?: string;
  summaryPt?: string;
  descriptionZh?: string;
  descriptionEn?: string;
  descriptionZht?: string;
  descriptionPt?: string;
  htmlZh?: string;
  htmlEn?: string;
  htmlZht?: string;
  htmlPt?: string;
  venueNameZh?: string;
  venueNameEn?: string;
  venueNameZht?: string;
  venueNamePt?: string;
  addressZh?: string;
  addressEn?: string;
  addressZht?: string;
  addressPt?: string;
  organizerName?: string;
  organizerContact?: string;
  organizerWebsite?: string;
  signupCapacity?: number;
  signupFeeAmount?: number;
  signupStartAt?: string;
  signupEndAt?: string;
  publishStartAt?: string;
  publishEndAt?: string;
  isPinned?: number;
  coverAssetId?: number | null;
  heroAssetId?: number | null;
  cityBindings?: number[];
  subMapBindings?: number[];
  storylineBindings?: number[];
  attachmentAssetIds?: number[];
  sortOrder?: number;
  coverUrl?: string;
  startTime?: string;
  endTime?: string;
  status?: string;
  participationCount?: number;
  createdAt?: string;
  updatedAt?: string;
}

export interface AdminActivityPayload {
  code: string;
  activityType?: string;
  titleZh: string;
  titleEn?: string;
  titleZht?: string;
  titlePt?: string;
  summaryZh?: string;
  summaryEn?: string;
  summaryZht?: string;
  summaryPt?: string;
  descriptionZh?: string;
  descriptionEn?: string;
  descriptionZht?: string;
  descriptionPt?: string;
  htmlZh?: string;
  htmlEn?: string;
  htmlZht?: string;
  htmlPt?: string;
  venueNameZh?: string;
  venueNameEn?: string;
  venueNameZht?: string;
  venueNamePt?: string;
  addressZh?: string;
  addressEn?: string;
  addressZht?: string;
  addressPt?: string;
  organizerName?: string;
  organizerContact?: string;
  organizerWebsite?: string;
  signupCapacity?: number;
  signupFeeAmount?: number;
  signupStartAt?: string | null;
  signupEndAt?: string | null;
  publishStartAt?: string | null;
  publishEndAt?: string | null;
  isPinned?: number;
  coverAssetId?: number | null;
  heroAssetId?: number | null;
  participationCount?: number;
  status?: string;
  sortOrder?: number;
  cityBindings?: number[];
  subMapBindings?: number[];
  storylineBindings?: number[];
  attachmentAssetIds?: number[];
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
  popupPresetCode?: string | null;
  popupConfigJson?: string | null;
  displayPresetCode?: string | null;
  displayConfigJson?: string | null;
  triggerPresetCode?: string | null;
  triggerConfigJson?: string | null;
  exampleContentZh?: string | null;
  exampleContentEn?: string | null;
  exampleContentZht?: string | null;
  exampleContentPt?: string | null;
  status?: string;
  sortOrder?: number;
  publishStartAt?: string | null;
  publishEndAt?: string | null;
  createdAt?: string;
  storylineBindings?: number[];
  cityBindings?: number[];
  subMapBindings?: number[];
  indoorBuildingBindings?: number[];
  indoorFloorBindings?: number[];
  attachmentAssetIds?: number[];
}

export type RewardRuleGroupMode = 'all' | 'any' | 'at_least';

export interface AdminRewardLinkedEntityItem {
  ownerDomain: string;
  ownerId: number;
  ownerCode?: string;
  ownerName?: string;
  bindingRole?: string;
}

export interface AdminRewardRuleLinkItem {
  id: number;
  code: string;
  nameZh?: string;
  nameZht?: string;
  summaryText?: string;
  status?: string;
}

export interface AdminRewardPresentationSummaryItem {
  id: number;
  code: string;
  nameZh?: string;
  nameZht?: string;
  presentationType?: string;
  status?: string;
}

export interface AdminRewardRuleConditionItem {
  id?: number;
  conditionType?: string;
  metricType?: string;
  operatorType?: string;
  comparatorValue?: string;
  comparatorUnit?: string;
  summaryText?: string;
  configJson?: string;
  sortOrder?: number;
}

export interface AdminRewardRuleConditionGroupItem {
  id?: number;
  groupCode?: string;
  operatorType?: RewardRuleGroupMode | string;
  minimumMatchCount?: number | null;
  summaryText?: string;
  advancedConfigJson?: string;
  sortOrder?: number;
  conditions?: AdminRewardRuleConditionItem[];
}

export interface AdminRewardRuleItem {
  id: number;
  code: string;
  ruleType?: string;
  status?: string;
  nameZh?: string;
  nameZht?: string;
  summaryText?: string;
  advancedConfigJson?: string;
  conditionGroups?: AdminRewardRuleConditionGroupItem[];
  linkedOwners?: AdminRewardLinkedEntityItem[];
  createdAt?: string;
}

export interface AdminRewardRulePayload {
  code: string;
  nameZh: string;
  nameZht?: string;
  ruleType?: string;
  status?: string;
  summaryText?: string;
  advancedConfigJson?: string;
  conditionGroups?: AdminRewardRuleConditionGroupItem[];
}

export interface AdminRewardPresentationStepItem {
  id?: number;
  stepType?: string;
  stepCode?: string;
  titleText?: string;
  assetId?: number | null;
  durationMs?: number | null;
  skippableOverride?: number | null;
  triggerSfxAssetId?: number | null;
  voiceOverAssetId?: number | null;
  overlayConfigJson?: string;
  sortOrder?: number;
}

export interface AdminRewardPresentationItem {
  id: number;
  code: string;
  nameZh?: string;
  nameZht?: string;
  presentationType?: string;
  firstTimeOnly?: number;
  skippable?: number;
  minimumDisplayMs?: number;
  interruptPolicy?: string;
  queuePolicy?: string;
  priorityWeight?: number;
  coverAssetId?: number | null;
  voiceOverAssetId?: number | null;
  sfxAssetId?: number | null;
  summaryText?: string;
  configJson?: string;
  status?: string;
  steps?: AdminRewardPresentationStepItem[];
  linkedOwners?: AdminRewardLinkedEntityItem[];
  createdAt?: string;
}

export interface AdminRewardPresentationPayload {
  code: string;
  nameZh: string;
  nameZht?: string;
  presentationType?: string;
  firstTimeOnly?: number;
  skippable?: number;
  minimumDisplayMs?: number;
  interruptPolicy?: string;
  queuePolicy?: string;
  priorityWeight?: number;
  coverAssetId?: number | null;
  voiceOverAssetId?: number | null;
  sfxAssetId?: number | null;
  summaryText?: string;
  configJson?: string;
  status?: string;
  steps?: AdminRewardPresentationStepItem[];
}

export interface AdminRedeemablePrizeItem {
  id: number;
  code: string;
  prizeType?: string;
  fulfillmentMode?: string;
  nameZh?: string;
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
  coverAssetId?: number | null;
  stampCost?: number;
  inventoryTotal?: number;
  inventoryRedeemed?: number;
  inventoryRemaining?: number;
  stockPolicyJson?: string;
  fulfillmentConfigJson?: string;
  operatorNotes?: string;
  presentationId?: number | null;
  presentation?: AdminRewardPresentationSummaryItem | null;
  ruleIds?: number[];
  linkedRules?: AdminRewardRuleLinkItem[];
  storylineBindings?: number[];
  cityBindings?: number[];
  subMapBindings?: number[];
  indoorBuildingBindings?: number[];
  indoorFloorBindings?: number[];
  attachmentAssetIds?: number[];
  status?: string;
  sortOrder?: number;
  publishStartAt?: string;
  publishEndAt?: string;
  createdAt?: string;
}

export interface AdminRedeemablePrizePayload {
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
  prizeType?: string;
  fulfillmentMode?: string;
  coverAssetId?: number | null;
  stampCost?: number;
  inventoryTotal?: number;
  inventoryRedeemed?: number;
  stockPolicyJson?: string;
  fulfillmentConfigJson?: string;
  operatorNotes?: string;
  presentationId?: number | null;
  ruleIds?: number[];
  storylineBindings?: number[];
  cityBindings?: number[];
  subMapBindings?: number[];
  indoorBuildingBindings?: number[];
  indoorFloorBindings?: number[];
  attachmentAssetIds?: number[];
  status?: string;
  sortOrder?: number;
  publishStartAt?: string | null;
  publishEndAt?: string | null;
}

export interface AdminGameRewardItem {
  id: number;
  code: string;
  rewardType?: string;
  rarity?: string;
  stackable?: number;
  maxOwned?: number | null;
  canEquip?: number;
  canConsume?: number;
  nameZh?: string;
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
  coverAssetId?: number | null;
  iconAssetId?: number | null;
  animationAssetId?: number | null;
  rewardConfigJson?: string;
  presentationId?: number | null;
  presentation?: AdminRewardPresentationSummaryItem | null;
  ruleIds?: number[];
  linkedRules?: AdminRewardRuleLinkItem[];
  storylineBindings?: number[];
  cityBindings?: number[];
  subMapBindings?: number[];
  indoorBuildingBindings?: number[];
  indoorFloorBindings?: number[];
  attachmentAssetIds?: number[];
  status?: string;
  sortOrder?: number;
  publishStartAt?: string;
  publishEndAt?: string;
  createdAt?: string;
}

export interface AdminGameRewardPayload {
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
  rewardType?: string;
  rarity?: string;
  stackable?: number;
  maxOwned?: number | null;
  canEquip?: number;
  canConsume?: number;
  coverAssetId?: number | null;
  iconAssetId?: number | null;
  animationAssetId?: number | null;
  rewardConfigJson?: string;
  presentationId?: number | null;
  ruleIds?: number[];
  storylineBindings?: number[];
  cityBindings?: number[];
  subMapBindings?: number[];
  indoorBuildingBindings?: number[];
  indoorFloorBindings?: number[];
  attachmentAssetIds?: number[];
  status?: string;
  sortOrder?: number;
  publishStartAt?: string | null;
  publishEndAt?: string | null;
}

export interface AdminRewardGovernanceOverview {
  summary: {
    redeemablePrizeCount?: number;
    gameRewardCount?: number;
    honorCount?: number;
    ruleCount?: number;
    presentationCount?: number;
    linkedIndoorBehaviorCount?: number;
  };
  rules: AdminRewardRuleItem[];
  presentations: AdminRewardPresentationItem[];
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
