import request from '../utils/request';
import type {
  AdminActivityItem,
  AdminAssetUploadPayload,
  AdminAuthResponse,
  AdminContentAssetItem,
  AdminMapTileItem,
  AdminNotificationItem,
  AdminOperationLog,
  AdminPoiDetail,
  AdminPoiListItem,
  AdminRewardItem,
  AdminRuntimeSettingItem,
  AdminStampItem,
  AdminStoryChapterItem,
  AdminStorylineDetail,
  AdminStorylineListItem,
  AdminSystemConfigItem,
  AdminTestAccountListItem,
  AdminTestStampSummary,
  AdminTipArticleItem,
  AdminTranslateRequestPayload,
  AdminTranslateResponse,
  AdminTranslationSettings,
  AdminTranslationSettingsUpdatePayload,
  AdminUserDetail,
  AdminUserListItem,
  AdminCityPayload,
  AdminCoordinatePreviewPayload,
  AdminCoordinatePreviewResult,
  AdminPoiPayload,
  AdminSpatialMetadataSuggestion,
  AdminSpatialMetadataSuggestionPayload,
  AdminSubMapItem,
  AdminSubMapPayload,
  DashboardStats,
  PaginationResponse,
  CityItem,
} from '../types/admin';

export type { CityItem, AdminSubMapItem };

export const adminLogin = (data: { username: string; password: string }) => {
  return request.post<AdminAuthResponse>('/api/admin/v1/auth/login', data);
};

export const getCurrentAdmin = () => {
  return request.get<AdminAuthResponse>('/api/admin/v1/auth/me');
};

export const refreshAdminToken = (refreshToken: string) => {
  return request.post<AdminAuthResponse>('/api/admin/v1/auth/refresh', { refreshToken });
};

export const adminLogout = () => {
  return request.post<boolean>('/api/admin/v1/auth/logout');
};

export const getAdminUsers = (params?: {
  pageNum?: number;
  pageSize?: number;
  keyword?: string;
  isTestAccount?: boolean;
}) => {
  return request.get<PaginationResponse<AdminUserListItem>>('/api/admin/v1/users', { params });
};

export const getAdminUserDetail = (userId: number) => {
  return request.get<AdminUserDetail>(`/api/admin/v1/users/${userId}`);
};

export const updateAdminUserTestFlag = (
  userId: number,
  data: { isTestAccount: boolean; reason?: string },
) => {
  return request.post<AdminUserListItem>(`/api/admin/v1/users/${userId}/test-flag`, data);
};

export const getAdminPois = (params?: {
  pageNum?: number;
  pageSize?: number;
  keyword?: string;
  cityId?: number;
  subMapId?: number;
  storylineId?: number;
}) => {
  return request.get<PaginationResponse<AdminPoiListItem>>('/api/admin/v1/pois', { params });
};

export const getAdminPoiDetail = (poiId: number) => {
  return request.get<AdminPoiDetail>(`/api/admin/v1/pois/${poiId}`);
};

export const createAdminPoi = (data: AdminPoiPayload) => {
  return request.post<AdminPoiDetail>('/api/admin/v1/pois', data);
};

export const updateAdminPoi = (poiId: number, data: AdminPoiPayload) => {
  return request.put<AdminPoiDetail>(`/api/admin/v1/pois/${poiId}`, data);
};

export const deleteAdminPoi = (poiId: number) => {
  return request.delete<boolean>(`/api/admin/v1/pois/${poiId}`);
};

export const getAdminStorylines = (params?: {
  pageNum?: number;
  pageSize?: number;
  keyword?: string;
  status?: string;
}) => {
  return request.get<PaginationResponse<AdminStorylineListItem>>('/api/admin/v1/storylines', { params });
};

export const getAdminStorylineDetail = (storylineId: number) => {
  return request.get<AdminStorylineDetail>(`/api/admin/v1/storylines/${storylineId}`);
};

export const createAdminStoryline = (data: any) => {
  return request.post<AdminStorylineDetail>('/api/admin/v1/storylines', data);
};

export const updateAdminStoryline = (storylineId: number, data: any) => {
  return request.put<AdminStorylineDetail>(`/api/admin/v1/storylines/${storylineId}`, data);
};

export const deleteAdminStoryline = (storylineId: number) => {
  return request.delete<boolean>(`/api/admin/v1/storylines/${storylineId}`);
};

export const getStorylineChapters = (storylineId: number, params?: { pageNum?: number; pageSize?: number }) => {
  return request.get<PaginationResponse<AdminStoryChapterItem>>(`/api/admin/v1/storylines/${storylineId}/chapters`, { params });
};

export const createStorylineChapter = (storylineId: number, data: any) => {
  return request.post<AdminStoryChapterItem>(`/api/admin/v1/storylines/${storylineId}/chapters`, data);
};

export const updateStorylineChapter = (storylineId: number, chapterId: number, data: any) => {
  return request.put<AdminStoryChapterItem>(`/api/admin/v1/storylines/${storylineId}/chapters/${chapterId}`, data);
};

export const deleteStorylineChapter = (storylineId: number, chapterId: number) => {
  return request.delete<boolean>(`/api/admin/v1/storylines/${storylineId}/chapters/${chapterId}`);
};

export const getAdminTestAccounts = (params?: {
  pageNum?: number;
  pageSize?: number;
  testGroup?: string;
}) => {
  return request.get<PaginationResponse<AdminTestAccountListItem>>('/api/admin/v1/test-console/accounts', { params });
};

export const updateTestAccountMock = (
  testAccountId: number,
  data: {
    enabled: boolean;
    latitude?: number;
    longitude?: number;
    poiId?: number;
    address?: string;
    reason?: string;
  },
) => {
  return request.put<AdminTestAccountListItem>(`/api/admin/v1/test-console/accounts/${testAccountId}/mock`, data);
};

export const adjustTestAccountLevel = (
  testAccountId: number,
  data: { targetLevel: number; targetExp: number; reason?: string },
) => {
  return request.post<AdminTestAccountListItem>(`/api/admin/v1/test-console/accounts/${testAccountId}/level`, data);
};

export const grantTestAccountStamp = (
  testAccountId: number,
  data: { stampType: string; sourceId: number; reason?: string },
) => {
  return request.post<AdminTestAccountListItem>(`/api/admin/v1/test-console/accounts/${testAccountId}/stamps/grant`, data);
};

export const batchGrantTestAccountStamps = (
  testAccountId: number,
  data: { count: number; stampType?: string; reason?: string },
) => {
  return request.post<AdminTestAccountListItem>(`/api/admin/v1/test-console/accounts/${testAccountId}/stamps/batch-grant`, data);
};

export const clearTestAccountStamps = (testAccountId: number, reason?: string) => {
  return request.delete<AdminTestAccountListItem>(`/api/admin/v1/test-console/accounts/${testAccountId}/stamps`, {
    params: { reason },
  });
};

export const getTestAccountStampSummary = (testAccountId: number) => {
  return request.get<AdminTestStampSummary>(`/api/admin/v1/test-console/accounts/${testAccountId}/stamps/summary`);
};

export const resetTestAccountProgress = (
  testAccountId: number,
  data: { resetType?: string; reason?: string },
) => {
  return request.post<AdminTestAccountListItem>(`/api/admin/v1/test-console/accounts/${testAccountId}/progress/reset`, data);
};

export const getTestAccountOperationLogs = (
  testAccountId: number,
  params?: { pageNum?: number; pageSize?: number },
) => {
  return request.get<PaginationResponse<AdminOperationLog>>(`/api/admin/v1/test-console/accounts/${testAccountId}/logs`, { params });
};

export const getDashboardStats = () => {
  return request.get<DashboardStats>('/api/admin/v1/dashboard/stats');
};

export const getAdminActivities = (params?: {
  pageNum?: number;
  pageSize?: number;
  keyword?: string;
  status?: string;
}) => {
  return request.get<PaginationResponse<AdminActivityItem>>('/api/admin/v1/operations/activities', { params });
};

export const getAdminRewards = (params?: {
  pageNum?: number;
  pageSize?: number;
  status?: string;
}) => {
  return request.get<PaginationResponse<AdminRewardItem>>('/api/admin/v1/system/rewards', { params });
};

export const createAdminReward = (data: any) => {
  return request.post<AdminRewardItem>('/api/admin/v1/system/rewards', data);
};

export const updateAdminReward = (rewardId: number, data: any) => {
  return request.put<AdminRewardItem>(`/api/admin/v1/system/rewards/${rewardId}`, data);
};

export const deleteAdminReward = (rewardId: number) => {
  return request.delete<boolean>(`/api/admin/v1/system/rewards/${rewardId}`);
};

export const getAdminAuditLogs = (params?: {
  pageNum?: number;
  pageSize?: number;
  module?: string;
}) => {
  return request.get<PaginationResponse<AdminOperationLog>>('/api/admin/v1/system/audit-logs', { params });
};

export const getAdminSystemConfigs = (params?: {
  pageNum?: number;
  pageSize?: number;
  keyword?: string;
}) => {
  return request.get<PaginationResponse<AdminSystemConfigItem>>('/api/admin/v1/system/configs', { params });
};

export const getAdminTranslationSettings = () => {
  return request.get<AdminTranslationSettings>('/api/admin/v1/system/translation-settings');
};

export const updateAdminTranslationSettings = (data: AdminTranslationSettingsUpdatePayload) => {
  return request.put<AdminTranslationSettings>('/api/admin/v1/system/translation-settings', data);
};

export const translateAdminText = (data: AdminTranslateRequestPayload) => {
  return request.post<AdminTranslateResponse>('/api/admin/v1/system/translate', data);
};

export const getAdminMapTiles = (params?: {
  pageNum?: number;
  pageSize?: number;
}) => {
  return request.get<PaginationResponse<AdminMapTileItem>>('/api/admin/v1/system/map-tiles', { params });
};

export const getAdminRuntimeSettings = (params?: {
  pageNum?: number;
  pageSize?: number;
  settingGroup?: string;
  status?: string;
  keyword?: string;
}) => {
  return request.get<PaginationResponse<AdminRuntimeSettingItem>>('/api/admin/v1/content/runtime-settings', { params });
};

export const createAdminRuntimeSetting = (data: any) => {
  return request.post<AdminRuntimeSettingItem>('/api/admin/v1/content/runtime-settings', data);
};

export const updateAdminRuntimeSetting = (id: number, data: any) => {
  return request.put<AdminRuntimeSettingItem>(`/api/admin/v1/content/runtime-settings/${id}`, data);
};

export const deleteAdminRuntimeSetting = (id: number) => {
  return request.delete<boolean>(`/api/admin/v1/content/runtime-settings/${id}`);
};

export const getAdminContentAssets = (params?: {
  pageNum?: number;
  pageSize?: number;
  assetKind?: string;
  status?: string;
  keyword?: string;
}) => {
  return request.get<PaginationResponse<AdminContentAssetItem>>('/api/admin/v1/content/assets', { params });
};

export const createAdminContentAsset = (data: any) => {
  return request.post<AdminContentAssetItem>('/api/admin/v1/content/assets', data);
};

export const uploadAdminContentAsset = (payload: AdminAssetUploadPayload) => {
  const formData = new FormData();
  formData.append('file', payload.file);
  formData.append('assetKind', payload.assetKind);
  if (payload.localeCode) {
    formData.append('localeCode', payload.localeCode);
  }
  if (payload.status) {
    formData.append('status', payload.status);
  }
  return request.post<AdminContentAssetItem>('/api/admin/v1/content/assets/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  });
};

export const updateAdminContentAsset = (id: number, data: any) => {
  return request.put<AdminContentAssetItem>(`/api/admin/v1/content/assets/${id}`, data);
};

export const deleteAdminContentAsset = (id: number) => {
  return request.delete<boolean>(`/api/admin/v1/content/assets/${id}`);
};

export const getAdminTipArticles = (params?: {
  pageNum?: number;
  pageSize?: number;
  cityId?: number;
  status?: string;
  keyword?: string;
}) => {
  return request.get<PaginationResponse<AdminTipArticleItem>>('/api/admin/v1/content/tips', { params });
};

export const createAdminTipArticle = (data: any) => {
  return request.post<AdminTipArticleItem>('/api/admin/v1/content/tips', data);
};

export const updateAdminTipArticle = (id: number, data: any) => {
  return request.put<AdminTipArticleItem>(`/api/admin/v1/content/tips/${id}`, data);
};

export const deleteAdminTipArticle = (id: number) => {
  return request.delete<boolean>(`/api/admin/v1/content/tips/${id}`);
};

export const getAdminNotifications = (params?: {
  pageNum?: number;
  pageSize?: number;
  status?: string;
  keyword?: string;
}) => {
  return request.get<PaginationResponse<AdminNotificationItem>>('/api/admin/v1/content/notifications', { params });
};

export const createAdminNotification = (data: any) => {
  return request.post<AdminNotificationItem>('/api/admin/v1/content/notifications', data);
};

export const updateAdminNotification = (id: number, data: any) => {
  return request.put<AdminNotificationItem>(`/api/admin/v1/content/notifications/${id}`, data);
};

export const deleteAdminNotification = (id: number) => {
  return request.delete<boolean>(`/api/admin/v1/content/notifications/${id}`);
};

export const getAdminStamps = (params?: {
  pageNum?: number;
  pageSize?: number;
  status?: string;
  keyword?: string;
}) => {
  return request.get<PaginationResponse<AdminStampItem>>('/api/admin/v1/content/stamps', { params });
};

export const createAdminStamp = (data: any) => {
  return request.post<AdminStampItem>('/api/admin/v1/content/stamps', data);
};

export const updateAdminStamp = (id: number, data: any) => {
  return request.put<AdminStampItem>(`/api/admin/v1/content/stamps/${id}`, data);
};

export const deleteAdminStamp = (id: number) => {
  return request.delete<boolean>(`/api/admin/v1/content/stamps/${id}`);
};

export const getCities = (params?: {
  pageNum?: number;
  pageSize?: number;
  keyword?: string;
  status?: string;
}) => {
  return request.get<PaginationResponse<CityItem>>('/api/admin/v1/map/cities', { params });
};

export const getCityDetail = (id: number) => {
  return request.get<CityItem>(`/api/admin/v1/map/cities/${id}`);
};

export const createCity = (data: AdminCityPayload) => {
  return request.post<CityItem>('/api/admin/v1/map/cities', { upsert: data });
};

export const updateCity = (id: number, data: AdminCityPayload) => {
  return request.put<CityItem>(`/api/admin/v1/map/cities/${id}`, { upsert: data });
};

export const publishCity = (id: number) => {
  return request.put<CityItem>(`/api/admin/v1/map/cities/${id}/publish`);
};

export const getSubMaps = (params?: {
  pageNum?: number;
  pageSize?: number;
  cityId?: number;
  keyword?: string;
  status?: string;
}) => {
  return request.get<PaginationResponse<AdminSubMapItem>>('/api/admin/v1/map/sub-maps', { params });
};

export const getSubMapDetail = (id: number) => {
  return request.get<AdminSubMapItem>(`/api/admin/v1/map/sub-maps/${id}`);
};

export const createSubMap = (data: AdminSubMapPayload) => {
  return request.post<AdminSubMapItem>('/api/admin/v1/map/sub-maps', data);
};

export const updateSubMap = (id: number, data: AdminSubMapPayload) => {
  return request.put<AdminSubMapItem>(`/api/admin/v1/map/sub-maps/${id}`, data);
};

export const publishSubMap = (id: number) => {
  return request.put<AdminSubMapItem>(`/api/admin/v1/map/sub-maps/${id}/publish`);
};

export const previewSpatialCoordinate = (data: AdminCoordinatePreviewPayload) => {
  return request.post<AdminCoordinatePreviewResult>('/api/admin/v1/map/spatial/coordinate-preview', data);
};

export const suggestSpatialMetadata = (data: AdminSpatialMetadataSuggestionPayload) => {
  return request.post<AdminSpatialMetadataSuggestion>('/api/admin/v1/map/spatial/metadata/suggest', data);
};

export interface BuildingItem {
  id: number;
  buildingCode: string;
  nameZh: string;
  addressZh: string;
  cityCode: string;
  lat: number | null;
  lng: number | null;
  totalFloors: number;
  coverImageUrl: string | null;
  status: string;
}

export const getBuildings = (params?: { pageNum?: number; pageSize?: number; cityCode?: string }) => {
  return request.get<PaginationResponse<BuildingItem>>('/api/admin/v1/map/indoor/buildings', { params });
};

export const createBuilding = (data: any) => {
  return request.post<BuildingItem>('/api/admin/v1/map/indoor/buildings', data);
};

export const updateBuilding = (id: number, data: any) => {
  return request.put<BuildingItem>(`/api/admin/v1/map/indoor/buildings/${id}`, data);
};

export interface CollectibleItem {
  id: number;
  collectibleCode: string;
  nameZh: string;
  collectibleType: string;
  rarity: string;
  imageUrl: string | null;
  seriesId: number | null;
  acquisitionSource: string | null;
  isRepeatable: number;
  isLimited: number;
  maxOwnership: number;
  status: string;
}

export const getCollectibles = (params?: {
  pageNum?: number;
  pageSize?: number;
  keyword?: string;
  rarity?: string;
}) => {
  return request.get<PaginationResponse<CollectibleItem>>('/api/admin/v1/collectibles/items', { params });
};

export const createCollectible = (data: any) => {
  return request.post<CollectibleItem>('/api/admin/v1/collectibles/items', data);
};

export interface BadgeItem {
  id: number;
  badgeCode: string;
  nameZh: string;
  badgeType: string;
  rarity: string;
  isHidden: number;
  iconUrl: string | null;
  imageUrl: string | null;
  status: string;
}

export const getBadges = (params?: { pageNum?: number; pageSize?: number }) => {
  return request.get<PaginationResponse<BadgeItem>>('/api/admin/v1/collectibles/badges', { params });
};

export const createBadge = (data: any) => {
  return request.post<BadgeItem>('/api/admin/v1/collectibles/badges', data);
};

export interface RoleItem {
  id: number;
  roleCode: string;
  roleName: string;
  description: string;
  isSystem: number;
  sortOrder: number | null;
  status: string;
}

export interface PermissionItem {
  id: number;
  permCode: string;
  permName: string;
  module: string;
  permType: string;
  parentId: number;
  path: string | null;
  method: string | null;
  description: string | null;
  sortOrder: number;
}

export interface AdminUserItem {
  id: number;
  username: string;
  displayName: string | null;
  email: string | null;
  department: string | null;
  isSuperuser: number;
  status: string;
  lastLoginAt: string | null;
}

export const getAdminUsersRbac = (params?: { pageNum?: number; pageSize?: number; keyword?: string }) => {
  return request.get<PaginationResponse<AdminUserItem>>('/api/admin/v1/system/admin-users', { params });
};

export const getRoles = () => {
  return request.get<RoleItem[]>('/api/admin/v1/system/roles');
};

export const createRole = (data: { roleCode: string; roleName: string; description?: string; sortOrder?: number; isSystem?: number; status?: string }) => {
  return request.post<RoleItem>('/api/admin/v1/system/roles', data);
};

export const getPermissions = (params?: { module?: string; type?: string }) => {
  return request.get<PermissionItem[]>('/api/admin/v1/system/permissions', { params });
};

export const getRolePermissions = (roleId: number) => {
  return request.get<PermissionItem[]>(`/api/admin/v1/system/roles/${roleId}/permissions`);
};

export const updateRolePermissions = (roleId: number, permissionIds: number[]) => {
  return request.put<void>(`/api/admin/v1/system/roles/${roleId}/permissions`, permissionIds);
};

export interface AiProviderItem {
  id: number;
  providerName: string;
  displayName: string;
  apiBaseUrl: string;
  modelName: string;
  capabilities: string;
  requestTimeoutMs: number;
  maxRetries: number;
  quotaDaily: number;
  costPer1kTokens?: number;
  status: number;
}

export interface AiPolicyItem {
  id: number;
  policyName: string;
  scenarioCode: string;
  policyType: string;
  scenarioGroup: string;
  providerId: number;
  providerName?: string;
  modelOverride?: string;
  multimodalEnabled: number;
  voiceEnabled: number;
  temperature?: number;
  maxTokens?: number;
  status: number;
}

export interface AiLogItem {
  id: number;
  providerId?: number;
  providerName?: string;
  policyId?: number;
  policyName?: string;
  scenarioCode?: string;
  scenarioGroup?: string;
  userOpenid?: string;
  requestType?: string;
  inputDataHash?: string;
  outputSummary?: string;
  latencyMs?: number;
  tokensUsed?: number;
  costUsd?: number;
  success: number;
  errorMessage?: string;
  createdAt?: string;
}

export const getAiProviders = () => {
  return request.get<AiProviderItem[]>('/api/admin/v1/ai/providers');
};

export const getAiPolicies = (params?: { scenarioGroup?: string }) => {
  return request.get<AiPolicyItem[]>('/api/admin/v1/ai/policies', { params });
};

export const getAiLogs = (params?: {
  pageNum?: number;
  pageSize?: number;
  scenarioGroup?: string;
  success?: number;
  providerId?: number;
}) => {
  return request.get<PaginationResponse<AiLogItem>>('/api/admin/v1/ai/logs', { params });
};
