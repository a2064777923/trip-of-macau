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

export interface PaginationResponse<T> {
  pageNum: number;
  pageSize: number;
  total: number;
  totalPages: number;
  list: T[];
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

export interface AdminPoiListItem {
  poiId: number;
  name: string;
  subtitle?: string;
  regionCode?: string;
  regionName?: string;
  poiType?: string;
  latitude: number;
  longitude: number;
  categoryId?: number;
  categoryName?: string;
  importance?: string;
  geofenceRadius?: number;
  status?: string;
  storylineId?: number;
  storylineName?: string;
  checkInCount?: number;
  createdAt?: string;
}

export interface AdminPoiDetail extends AdminPoiListItem {
  description?: string;
  gcj02Latitude?: number;
  gcj02Longitude?: number;
  address?: string;
  checkInMethod?: string;
  coverImageUrl?: string;
  imageUrls?: string[];
  audioGuideUrl?: string;
  videoUrl?: string;
  arContentUrl?: string;
  tags?: string[];
  difficulty?: string;
  openTime?: string;
  suggestedVisitMinutes?: number;
  favoriteCount?: number;
  stampType?: string;
  updatedAt?: string;
}

export interface AdminStorylineListItem {
  storylineId: number;
  code: string;
  name: string;
  description?: string;
  coverImageUrl?: string;
  category?: string;
  difficulty?: string;
  status?: string;
  poiCount?: number;
  participationCount?: number;
  completionCount?: number;
  createdAt?: string;
}

export interface AdminStorylineDetail extends AdminStorylineListItem {
  bannerImageUrl?: string;
  estimatedDurationMinutes?: number;
  tags?: string[];
  totalChapters?: number;
  averageCompletionTime?: number;
  publishAt?: string | null;
  startAt?: string | null;
  endAt?: string | null;
  updatedAt?: string;
}

export interface AdminStoryChapterItem {
  id: number;
  storyLineId: number;
  chapterOrder: number;
  titleZh: string;
  titleEn?: string;
  titleZht?: string;
  mediaType?: string;
  mediaUrl?: string;
  scriptZh?: string;
  scriptEn?: string;
  scriptZht?: string;
  unlockType?: string;
  unlockParam?: string;
  duration?: number;
  createdAt?: string;
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
  name: string;
  description?: string;
  stampsRequired?: number;
  totalQuantity?: number;
  redeemedCount?: number;
  remainingQuantity?: number;
  startTime?: string;
  endTime?: string;
  status?: string;
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
