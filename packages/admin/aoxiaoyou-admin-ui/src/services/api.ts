import request from '../utils/request';
import type {
  AdminActivityItem,
  AdminActivityPayload,
  AdminAssetBatchUploadPayload,
  AdminAssetBatchUploadResponse,
  AdminAssetUploadPayload,
  AdminAuthResponse,
  AdminCarryoverSettings,
  AdminCarryoverSettingsUpdatePayload,
  AdminContentAssetItem,
  AdminContentAssetUsageSummary,
  AdminExperienceBindingItem,
  AdminExperienceBindingPayload,
  AdminExperienceFlowItem,
  AdminExperienceFlowPayload,
  AdminExperienceGovernanceOverview,
  AdminExperienceOverrideItem,
  AdminExperienceOverridePayload,
  AdminExperienceStepItem,
  AdminExperienceStepPayload,
  AdminExperienceTemplateItem,
  AdminExperienceTemplatePayload,
  AdminExplorationElementItem,
  AdminExplorationElementPayload,
  AdminMediaPolicySettings,
  AdminMediaPolicySettingsUpdatePayload,
  AdminMapTileItem,
  AdminNotificationItem,
  AdminOperationLog,
  AdminPoiDetail,
  AdminPoiListItem,
  AdminIndoorBuildingDetail,
  AdminIndoorBuildingItem,
  AdminIndoorBuildingPayload,
  AdminIndoorFloorItem,
  AdminIndoorFloorPayload,
  AdminIndoorRuntimeSettings,
  AdminIndoorRuntimeSettingsUpdatePayload,
  AdminIndoorMarkerCsvImportResult,
  AdminIndoorMarkerCsvPreview,
  AdminIndoorMarkerItem,
  AdminIndoorMarkerPayload,
  AdminIndoorNodeItem,
  AdminIndoorNodePayload,
  AdminIndoorRuleConflictItem,
  AdminIndoorRuleGovernanceDetail,
  AdminIndoorRuleGovernanceItem,
  AdminIndoorRuleOverviewQuery,
  AdminIndoorRuleStatusUpdateResult,
  AdminIndoorRuleValidationResponse,
  AdminIndoorTilePreview,
  AdminRewardItem,
  AdminRuntimeSettingItem,
  AdminStampItem,
  AdminStoryChapterItem,
  AdminStoryContentBlockItem,
  AdminStoryContentBlockPayload,
  AdminStoryChapterPayload,
  AdminStorylineDetail,
  AdminStorylineListItem,
  AdminStorylinePayload,
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
  AdminRedeemablePrizeItem,
  AdminRedeemablePrizePayload,
  AdminGameRewardItem,
  AdminGameRewardPayload,
  AdminRewardGovernanceOverview,
  AdminRewardPresentationItem,
  AdminRewardPresentationPayload,
  AdminRewardRuleItem,
  AdminRewardRulePayload,
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

export const createAdminStoryline = (data: AdminStorylinePayload) => {
  return request.post<AdminStorylineDetail>('/api/admin/v1/storylines', data);
};

export const updateAdminStoryline = (storylineId: number, data: AdminStorylinePayload) => {
  return request.put<AdminStorylineDetail>(`/api/admin/v1/storylines/${storylineId}`, data);
};

export const deleteAdminStoryline = (storylineId: number) => {
  return request.delete<boolean>(`/api/admin/v1/storylines/${storylineId}`);
};

export const getStorylineChapters = (storylineId: number, params?: { pageNum?: number; pageSize?: number }) => {
  return request.get<PaginationResponse<AdminStoryChapterItem>>(`/api/admin/v1/storylines/${storylineId}/chapters`, { params });
};

export const getStorylineChapterDetail = (storylineId: number, chapterId: number) => {
  return request.get<AdminStoryChapterItem>(`/api/admin/v1/storylines/${storylineId}/chapters/${chapterId}`);
};

export const createStorylineChapter = (storylineId: number, data: AdminStoryChapterPayload) => {
  return request.post<AdminStoryChapterItem>(`/api/admin/v1/storylines/${storylineId}/chapters`, data);
};

export const updateStorylineChapter = (storylineId: number, chapterId: number, data: AdminStoryChapterPayload) => {
  return request.put<AdminStoryChapterItem>(`/api/admin/v1/storylines/${storylineId}/chapters/${chapterId}`, data);
};

export const deleteStorylineChapter = (storylineId: number, chapterId: number) => {
  return request.delete<boolean>(`/api/admin/v1/storylines/${storylineId}/chapters/${chapterId}`);
};

export const getAdminStoryContentBlocks = (params?: {
  pageNum?: number;
  pageSize?: number;
  keyword?: string;
  blockType?: string;
  status?: string;
}) => {
  return request.get<PaginationResponse<AdminStoryContentBlockItem>>('/api/admin/v1/content/blocks', { params });
};

export const getAdminStoryContentBlockDetail = (blockId: number) => {
  return request.get<AdminStoryContentBlockItem>(`/api/admin/v1/content/blocks/${blockId}`);
};

export const createAdminStoryContentBlock = (data: AdminStoryContentBlockPayload) => {
  return request.post<AdminStoryContentBlockItem>('/api/admin/v1/content/blocks', data);
};

export const updateAdminStoryContentBlock = (blockId: number, data: AdminStoryContentBlockPayload) => {
  return request.put<AdminStoryContentBlockItem>(`/api/admin/v1/content/blocks/${blockId}`, data);
};

export const deleteAdminStoryContentBlock = (blockId: number) => {
  return request.delete<boolean>(`/api/admin/v1/content/blocks/${blockId}`);
};

export const getAdminExperienceTemplates = (params?: {
  pageNum?: number;
  pageSize?: number;
  keyword?: string;
  templateType?: string;
  status?: string;
}) => {
  return request.get<PaginationResponse<AdminExperienceTemplateItem>>('/api/admin/v1/experience/templates', { params });
};

export const createAdminExperienceTemplate = (data: AdminExperienceTemplatePayload) => {
  return request.post<AdminExperienceTemplateItem>('/api/admin/v1/experience/templates', data);
};

export const updateAdminExperienceTemplate = (templateId: number, data: AdminExperienceTemplatePayload) => {
  return request.put<AdminExperienceTemplateItem>(`/api/admin/v1/experience/templates/${templateId}`, data);
};

export const deleteAdminExperienceTemplate = (templateId: number) => {
  return request.delete<boolean>(`/api/admin/v1/experience/templates/${templateId}`);
};

export const getAdminExperienceFlows = (params?: {
  pageNum?: number;
  pageSize?: number;
  keyword?: string;
  flowType?: string;
  status?: string;
}) => {
  return request.get<PaginationResponse<AdminExperienceFlowItem>>('/api/admin/v1/experience/flows', { params });
};

export const getAdminExperienceFlowDetail = (flowId: number) => {
  return request.get<AdminExperienceFlowItem>(`/api/admin/v1/experience/flows/${flowId}`);
};

export const createAdminExperienceFlow = (data: AdminExperienceFlowPayload) => {
  return request.post<AdminExperienceFlowItem>('/api/admin/v1/experience/flows', data);
};

export const updateAdminExperienceFlow = (flowId: number, data: AdminExperienceFlowPayload) => {
  return request.put<AdminExperienceFlowItem>(`/api/admin/v1/experience/flows/${flowId}`, data);
};

export const deleteAdminExperienceFlow = (flowId: number) => {
  return request.delete<boolean>(`/api/admin/v1/experience/flows/${flowId}`);
};

export const createAdminExperienceStep = (flowId: number, data: AdminExperienceStepPayload) => {
  return request.post<AdminExperienceStepItem>(`/api/admin/v1/experience/flows/${flowId}/steps`, data);
};

export const updateAdminExperienceStep = (flowId: number, stepId: number, data: AdminExperienceStepPayload) => {
  return request.put<AdminExperienceStepItem>(`/api/admin/v1/experience/flows/${flowId}/steps/${stepId}`, data);
};

export const deleteAdminExperienceStep = (flowId: number, stepId: number) => {
  return request.delete<boolean>(`/api/admin/v1/experience/flows/${flowId}/steps/${stepId}`);
};

export const getAdminExperienceBindings = (params?: {
  pageNum?: number;
  pageSize?: number;
  ownerType?: string;
  ownerId?: number;
  ownerCode?: string;
}) => {
  return request.get<PaginationResponse<AdminExperienceBindingItem>>('/api/admin/v1/experience/bindings', { params });
};

export const createAdminExperienceBinding = (data: AdminExperienceBindingPayload) => {
  return request.post<AdminExperienceBindingItem>('/api/admin/v1/experience/bindings', data);
};

export const updateAdminExperienceBinding = (bindingId: number, data: AdminExperienceBindingPayload) => {
  return request.put<AdminExperienceBindingItem>(`/api/admin/v1/experience/bindings/${bindingId}`, data);
};

export const deleteAdminExperienceBinding = (bindingId: number) => {
  return request.delete<boolean>(`/api/admin/v1/experience/bindings/${bindingId}`);
};

export const getAdminExperienceOverrides = (params?: {
  pageNum?: number;
  pageSize?: number;
  ownerType?: string;
  ownerId?: number;
}) => {
  return request.get<PaginationResponse<AdminExperienceOverrideItem>>('/api/admin/v1/experience/overrides', { params });
};

export const createAdminExperienceOverride = (data: AdminExperienceOverridePayload) => {
  return request.post<AdminExperienceOverrideItem>('/api/admin/v1/experience/overrides', data);
};

export const updateAdminExperienceOverride = (overrideId: number, data: AdminExperienceOverridePayload) => {
  return request.put<AdminExperienceOverrideItem>(`/api/admin/v1/experience/overrides/${overrideId}`, data);
};

export const deleteAdminExperienceOverride = (overrideId: number) => {
  return request.delete<boolean>(`/api/admin/v1/experience/overrides/${overrideId}`);
};

export const getAdminExplorationElements = (params?: {
  pageNum?: number;
  pageSize?: number;
  keyword?: string;
  ownerType?: string;
  ownerId?: number;
  cityId?: number;
  subMapId?: number;
  storylineId?: number;
  status?: string;
}) => {
  return request.get<PaginationResponse<AdminExplorationElementItem>>('/api/admin/v1/experience/exploration-elements', { params });
};

export const createAdminExplorationElement = (data: AdminExplorationElementPayload) => {
  return request.post<AdminExplorationElementItem>('/api/admin/v1/experience/exploration-elements', data);
};

export const updateAdminExplorationElement = (elementId: number, data: AdminExplorationElementPayload) => {
  return request.put<AdminExplorationElementItem>(`/api/admin/v1/experience/exploration-elements/${elementId}`, data);
};

export const deleteAdminExplorationElement = (elementId: number) => {
  return request.delete<boolean>(`/api/admin/v1/experience/exploration-elements/${elementId}`);
};

export const getAdminExperienceGovernanceOverview = () => {
  return request.get<AdminExperienceGovernanceOverview>('/api/admin/v1/experience/governance/overview');
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
  activityType?: string;
}) => {
  return request.get<PaginationResponse<AdminActivityItem>>('/api/admin/v1/operations/activities', { params });
};

export const getAdminActivityDetail = (activityId: number) => {
  return request.get<AdminActivityItem>(`/api/admin/v1/operations/activities/${activityId}`);
};

export const createAdminActivity = (data: AdminActivityPayload) => {
  return request.post<AdminActivityItem>('/api/admin/v1/operations/activities', data);
};

export const updateAdminActivity = (activityId: number, data: AdminActivityPayload) => {
  return request.put<AdminActivityItem>(`/api/admin/v1/operations/activities/${activityId}`, data);
};

export const deleteAdminActivity = (activityId: number) => {
  return request.delete<boolean>(`/api/admin/v1/operations/activities/${activityId}`);
};

export const getAdminRewards = (params?: {
  pageNum?: number;
  pageSize?: number;
  status?: string;
}) => {
  return request.get<PaginationResponse<AdminRewardItem>>('/api/admin/v1/collectibles/rewards', { params });
};

export const createAdminReward = (data: Partial<AdminRewardItem>) => {
  return request.post<AdminRewardItem>('/api/admin/v1/collectibles/rewards', data);
};

export const updateAdminReward = (rewardId: number, data: Partial<AdminRewardItem>) => {
  return request.put<AdminRewardItem>(`/api/admin/v1/collectibles/rewards/${rewardId}`, data);
};

export const deleteAdminReward = (rewardId: number) => {
  return request.delete<boolean>(`/api/admin/v1/collectibles/rewards/${rewardId}`);
};

export const getAdminRedeemablePrizes = (params?: {
  pageNum?: number;
  pageSize?: number;
  keyword?: string;
  status?: string;
  prizeType?: string;
  fulfillmentMode?: string;
}) => {
  return request.get<PaginationResponse<AdminRedeemablePrizeItem>>('/api/admin/v1/redeemable-prizes', { params });
};

export const getAdminRedeemablePrizeDetail = (prizeId: number) => {
  return request.get<AdminRedeemablePrizeItem>(`/api/admin/v1/redeemable-prizes/${prizeId}`);
};

export const createAdminRedeemablePrize = (data: AdminRedeemablePrizePayload) => {
  return request.post<AdminRedeemablePrizeItem>('/api/admin/v1/redeemable-prizes', data);
};

export const updateAdminRedeemablePrize = (prizeId: number, data: AdminRedeemablePrizePayload) => {
  return request.put<AdminRedeemablePrizeItem>(`/api/admin/v1/redeemable-prizes/${prizeId}`, data);
};

export const deleteAdminRedeemablePrize = (prizeId: number) => {
  return request.delete<boolean>(`/api/admin/v1/redeemable-prizes/${prizeId}`);
};

export const getAdminGameRewards = (params?: {
  pageNum?: number;
  pageSize?: number;
  keyword?: string;
  status?: string;
  rewardType?: string;
  honorsOnly?: boolean;
}) => {
  return request.get<PaginationResponse<AdminGameRewardItem>>('/api/admin/v1/game-rewards', { params });
};

export const getAdminGameRewardDetail = (rewardId: number) => {
  return request.get<AdminGameRewardItem>(`/api/admin/v1/game-rewards/${rewardId}`);
};

export const createAdminGameReward = (data: AdminGameRewardPayload) => {
  return request.post<AdminGameRewardItem>('/api/admin/v1/game-rewards', data);
};

export const updateAdminGameReward = (rewardId: number, data: AdminGameRewardPayload) => {
  return request.put<AdminGameRewardItem>(`/api/admin/v1/game-rewards/${rewardId}`, data);
};

export const deleteAdminGameReward = (rewardId: number) => {
  return request.delete<boolean>(`/api/admin/v1/game-rewards/${rewardId}`);
};

export const getAdminRewardRules = (params?: {
  pageNum?: number;
  pageSize?: number;
  keyword?: string;
  status?: string;
  ruleType?: string;
}) => {
  return request.get<PaginationResponse<AdminRewardRuleItem>>('/api/admin/v1/reward-rules', { params });
};

export const getAdminRewardRuleDetail = (ruleId: number) => {
  return request.get<AdminRewardRuleItem>(`/api/admin/v1/reward-rules/${ruleId}`);
};

export const createAdminRewardRule = (data: AdminRewardRulePayload) => {
  return request.post<AdminRewardRuleItem>('/api/admin/v1/reward-rules', data);
};

export const updateAdminRewardRule = (ruleId: number, data: AdminRewardRulePayload) => {
  return request.put<AdminRewardRuleItem>(`/api/admin/v1/reward-rules/${ruleId}`, data);
};

export const deleteAdminRewardRule = (ruleId: number) => {
  return request.delete<boolean>(`/api/admin/v1/reward-rules/${ruleId}`);
};

export const getAdminRewardPresentations = (params?: {
  pageNum?: number;
  pageSize?: number;
  keyword?: string;
  status?: string;
  presentationType?: string;
}) => {
  return request.get<PaginationResponse<AdminRewardPresentationItem>>('/api/admin/v1/reward-presentations', { params });
};

export const getAdminRewardPresentationDetail = (presentationId: number) => {
  return request.get<AdminRewardPresentationItem>(`/api/admin/v1/reward-presentations/${presentationId}`);
};

export const createAdminRewardPresentation = (data: AdminRewardPresentationPayload) => {
  return request.post<AdminRewardPresentationItem>('/api/admin/v1/reward-presentations', data);
};

export const updateAdminRewardPresentation = (presentationId: number, data: AdminRewardPresentationPayload) => {
  return request.put<AdminRewardPresentationItem>(`/api/admin/v1/reward-presentations/${presentationId}`, data);
};

export const deleteAdminRewardPresentation = (presentationId: number) => {
  return request.delete<boolean>(`/api/admin/v1/reward-presentations/${presentationId}`);
};

export const getAdminRewardGovernanceOverview = () => {
  return request.get<AdminRewardGovernanceOverview>('/api/admin/v1/reward-governance');
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

export const getAdminCarryoverSettings = () => {
  return request.get<AdminCarryoverSettings>('/api/admin/v1/system/carryover-settings');
};

export const updateAdminCarryoverSettings = (data: AdminCarryoverSettingsUpdatePayload) => {
  return request.put<AdminCarryoverSettings>('/api/admin/v1/system/carryover-settings', data);
};

export const getAdminMediaPolicySettings = () => {
  return request.get<AdminMediaPolicySettings>('/api/admin/v1/system/media-policy');
};

export const updateAdminMediaPolicySettings = (data: AdminMediaPolicySettingsUpdatePayload) => {
  return request.put<AdminMediaPolicySettings>('/api/admin/v1/system/media-policy', data);
};

export const getAdminIndoorRuntimeSettings = () => {
  return request.get<AdminIndoorRuntimeSettings>('/api/admin/v1/system/indoor-runtime');
};

export const updateAdminIndoorRuntimeSettings = (data: AdminIndoorRuntimeSettingsUpdatePayload) => {
  return request.put<AdminIndoorRuntimeSettings>('/api/admin/v1/system/indoor-runtime', data);
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
  uploadSource?: string;
  processingPolicyCode?: string;
  processingStatus?: string;
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
  if (payload.assetKind) {
    formData.append('assetKind', payload.assetKind);
  }
  if (payload.localeCode) {
    formData.append('localeCode', payload.localeCode);
  }
  if (payload.status) {
    formData.append('status', payload.status);
  }
  if (payload.uploadSource) {
    formData.append('uploadSource', payload.uploadSource);
  }
  if (payload.clientRelativePath) {
    formData.append('clientRelativePath', payload.clientRelativePath);
  }
  return request.post<AdminContentAssetItem>('/api/admin/v1/content/assets/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  });
};

export const batchUploadAdminContentAssets = (payload: AdminAssetBatchUploadPayload) => {
  const formData = new FormData();
  payload.files.forEach((file) => {
    formData.append('files', file);
  });
  if (payload.assetKind) {
    formData.append('assetKind', payload.assetKind);
  }
  if (payload.localeCode) {
    formData.append('localeCode', payload.localeCode);
  }
  if (payload.status) {
    formData.append('status', payload.status);
  }
  if (payload.uploadSource) {
    formData.append('uploadSource', payload.uploadSource);
  }
  payload.clientRelativePaths?.forEach((path) => {
    formData.append('clientRelativePaths', path);
  });
  return request.post<AdminAssetBatchUploadResponse>('/api/admin/v1/content/assets/batch-upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  });
};

export const updateAdminContentAsset = (id: number, data: any) => {
  return request.put<AdminContentAssetItem>(`/api/admin/v1/content/assets/${id}`, data);
};

export const getAdminContentAssetUsages = (id: number) => {
  return request.get<AdminContentAssetUsageSummary>(`/api/admin/v1/content/assets/${id}/usages`);
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

export const updateCityStatus = (id: number, status: 'published' | 'archived') => {
  return request.put<CityItem>(`/api/admin/v1/map/cities/${id}/status`, { status });
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

export const updateSubMapStatus = (id: number, status: 'published' | 'archived') => {
  return request.put<AdminSubMapItem>(`/api/admin/v1/map/sub-maps/${id}/status`, { status });
};

export const previewSpatialCoordinate = (data: AdminCoordinatePreviewPayload) => {
  return request.post<AdminCoordinatePreviewResult>('/api/admin/v1/map/spatial/coordinate-preview', data);
};

export const suggestSpatialMetadata = (data: AdminSpatialMetadataSuggestionPayload) => {
  return request.post<AdminSpatialMetadataSuggestion>('/api/admin/v1/map/spatial/metadata/suggest', data);
};

export const getIndoorBuildings = (params?: {
  pageNum?: number;
  pageSize?: number;
  keyword?: string;
  cityId?: number;
  subMapId?: number;
  poiId?: number;
  status?: string;
}) => {
  return request.get<PaginationResponse<AdminIndoorBuildingItem>>('/api/admin/v1/map/indoor/buildings', { params });
};

export const getIndoorBuildingDetail = (id: number) => {
  return request.get<AdminIndoorBuildingDetail>(`/api/admin/v1/map/indoor/buildings/${id}`);
};

export const createIndoorBuilding = (data: AdminIndoorBuildingPayload) => {
  return request.post<AdminIndoorBuildingDetail>('/api/admin/v1/map/indoor/buildings', data);
};

export const updateIndoorBuilding = (id: number, data: AdminIndoorBuildingPayload) => {
  return request.put<AdminIndoorBuildingDetail>(`/api/admin/v1/map/indoor/buildings/${id}`, data);
};

export const getIndoorFloors = (
  buildingId: number,
  params?: { pageNum?: number; pageSize?: number; status?: string },
) => {
  return request.get<PaginationResponse<AdminIndoorFloorItem>>(`/api/admin/v1/map/indoor/buildings/${buildingId}/floors`, {
    params,
  });
};

export const createIndoorFloor = (buildingId: number, data: AdminIndoorFloorPayload) => {
  return request.post<AdminIndoorFloorItem>(`/api/admin/v1/map/indoor/buildings/${buildingId}/floors`, data);
};

export const updateIndoorFloor = (floorId: number, data: AdminIndoorFloorPayload) => {
  return request.put<AdminIndoorFloorItem>(`/api/admin/v1/map/indoor/floors/${floorId}`, data);
};

export const deleteIndoorFloor = (floorId: number) => {
  return request.delete<boolean>(`/api/admin/v1/map/indoor/floors/${floorId}`);
};

export const getIndoorFloorDetail = (floorId: number) => {
  return request.get<AdminIndoorFloorItem>(`/api/admin/v1/map/indoor/floors/${floorId}`);
};

export const previewIndoorTileZip = (floorId: number, file: File, tileSizePx?: number) => {
  const formData = new FormData();
  formData.append('file', file);
  if (tileSizePx) {
    formData.append('tileSizePx', String(tileSizePx));
  }
  return request.post<AdminIndoorTilePreview>(`/api/admin/v1/map/indoor/floors/${floorId}/tile-import/zip-preview`, formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  });
};

export const importIndoorTileZip = (floorId: number, file: File, tileSizePx?: number) => {
  const formData = new FormData();
  formData.append('file', file);
  if (tileSizePx) {
    formData.append('tileSizePx', String(tileSizePx));
  }
  return request.post<AdminIndoorFloorItem>(`/api/admin/v1/map/indoor/floors/${floorId}/tile-import/zip`, formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  });
};

export const importIndoorFloorImage = (floorId: number, file: File, tileSizePx?: number) => {
  const formData = new FormData();
  formData.append('file', file);
  if (tileSizePx) {
    formData.append('tileSizePx', String(tileSizePx));
  }
  return request.post<AdminIndoorFloorItem>(`/api/admin/v1/map/indoor/floors/${floorId}/tile-import/image`, formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  });
};

export const getIndoorMarkers = (floorId: number, params?: { status?: string }) => {
  return request.get<AdminIndoorMarkerItem[]>(`/api/admin/v1/map/indoor/floors/${floorId}/markers`, { params });
};

export const getIndoorNodes = (floorId: number, params?: { status?: string }) => {
  return request.get<AdminIndoorNodeItem[]>(`/api/admin/v1/map/indoor/floors/${floorId}/nodes`, { params });
};

export const validateIndoorRuleGraph = (
  data: AdminIndoorNodePayload,
  params?: { floorId?: number; nodeId?: number },
) => {
  return request.post<AdminIndoorRuleValidationResponse>('/api/admin/v1/map/indoor/nodes/validate-rule-graph', data, {
    params,
  });
};

export const getIndoorRuleOverview = (params?: AdminIndoorRuleOverviewQuery) => {
  return request.get<AdminIndoorRuleGovernanceItem[]>('/api/admin/v1/map/indoor/rules/overview', { params });
};

export const getIndoorRuleConflicts = (params?: AdminIndoorRuleOverviewQuery) => {
  return request.get<AdminIndoorRuleConflictItem[]>('/api/admin/v1/map/indoor/rules/conflicts', { params });
};

export const getIndoorRuleBehaviorDetail = (behaviorId: number) => {
  return request.get<AdminIndoorRuleGovernanceDetail>(`/api/admin/v1/map/indoor/rules/behaviors/${behaviorId}`);
};

export const updateIndoorRuleBehaviorStatus = (behaviorId: number, status: string) => {
  return request.patch<AdminIndoorRuleStatusUpdateResult>(`/api/admin/v1/map/indoor/rules/behaviors/${behaviorId}/status`, { status });
};

export const createIndoorNode = (floorId: number, data: AdminIndoorNodePayload) => {
  return request.post<AdminIndoorNodeItem>(`/api/admin/v1/map/indoor/floors/${floorId}/nodes`, data);
};

export const updateIndoorNode = (nodeId: number, data: AdminIndoorNodePayload) => {
  return request.put<AdminIndoorNodeItem>(`/api/admin/v1/map/indoor/nodes/${nodeId}`, data);
};

export const deleteIndoorNode = (nodeId: number) => {
  return request.delete<boolean>(`/api/admin/v1/map/indoor/nodes/${nodeId}`);
};

export const createIndoorMarker = (floorId: number, data: AdminIndoorMarkerPayload) => {
  return request.post<AdminIndoorMarkerItem>(`/api/admin/v1/map/indoor/floors/${floorId}/markers`, data);
};

export const updateIndoorMarker = (markerId: number, data: AdminIndoorMarkerPayload) => {
  return request.put<AdminIndoorMarkerItem>(`/api/admin/v1/map/indoor/markers/${markerId}`, data);
};

export const deleteIndoorMarker = (markerId: number) => {
  return request.delete<boolean>(`/api/admin/v1/map/indoor/markers/${markerId}`);
};

export const previewIndoorMarkerCsv = (floorId: number, file: File) => {
  const formData = new FormData();
  formData.append('file', file);
  return request.post<AdminIndoorMarkerCsvPreview>(`/api/admin/v1/map/indoor/floors/${floorId}/markers/csv-preview`, formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  });
};

export const confirmIndoorMarkerCsv = (floorId: number, data: {
  sourceFilename?: string;
  rows: Array<{
    rowNumber?: number;
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
  }>;
}) => {
  return request.post<AdminIndoorMarkerCsvImportResult>(`/api/admin/v1/map/indoor/floors/${floorId}/markers/csv-confirm`, data);
};

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
  imageUrl: string | null;
  animationUrl?: string | null;
  seriesId: number | null;
  acquisitionSource: string | null;
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
  isRepeatable: number;
  isLimited: number;
  maxOwnership: number;
  status: string;
  sortOrder?: number | null;
  storylineBindings?: number[];
  cityBindings?: number[];
  subMapBindings?: number[];
  indoorBuildingBindings?: number[];
  indoorFloorBindings?: number[];
  attachmentAssetIds?: number[];
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

export const updateCollectible = (collectibleId: number, data: Partial<CollectibleItem>) => {
  return request.put<CollectibleItem>(`/api/admin/v1/collectibles/items/${collectibleId}`, data);
};

export const deleteCollectible = (collectibleId: number) => {
  return request.delete<boolean>(`/api/admin/v1/collectibles/items/${collectibleId}`);
};

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
  badgeType: string;
  rarity: string;
  isHidden: number;
  coverAssetId?: number | null;
  iconAssetId?: number | null;
  animationAssetId?: number | null;
  iconUrl: string | null;
  imageUrl: string | null;
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
  status: string;
  storylineBindings?: number[];
  cityBindings?: number[];
  subMapBindings?: number[];
  indoorBuildingBindings?: number[];
  indoorFloorBindings?: number[];
  attachmentAssetIds?: number[];
}

export const getBadges = (params?: { pageNum?: number; pageSize?: number }) => {
  return request.get<PaginationResponse<BadgeItem>>('/api/admin/v1/collectibles/badges', { params });
};

export const createBadge = (data: any) => {
  return request.post<BadgeItem>('/api/admin/v1/collectibles/badges', data);
};

export const updateBadge = (badgeId: number, data: Partial<BadgeItem>) => {
  return request.put<BadgeItem>(`/api/admin/v1/collectibles/badges/${badgeId}`, data);
};

export const deleteBadge = (badgeId: number) => {
  return request.delete<boolean>(`/api/admin/v1/collectibles/badges/${badgeId}`);
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
  phone?: string | null;
  department: string | null;
  isSuperuser: number;
  allowLosslessUpload?: boolean;
  status: string;
  lastLoginAt: string | null;
  lastLoginIp?: string | null;
}

export const getAdminUsersRbac = (params?: { pageNum?: number; pageSize?: number; keyword?: string }) => {
  return request.get<PaginationResponse<AdminUserItem>>('/api/admin/v1/system/admin-users', { params });
};

export const updateAdminUserRbac = (
  adminId: number,
  data: {
    displayName?: string;
    email?: string;
    phone?: string;
    status?: string;
    allowLosslessUpload?: boolean;
  },
) => {
  return request.put<AdminUserItem>(`/api/admin/v1/system/admin-users/${adminId}`, data);
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

export interface AiProviderTemplateItem {
  platformCode: string;
  platformLabel: string;
  description?: string;
  providerType?: string;
  endpointStyle?: string;
  defaultBaseUrl?: string;
  docsUrl?: string;
  authScheme?: string;
  syncStrategy?: string;
  inventorySemantics?: string;
  defaultModelName?: string;
  supportedModalities?: string[];
  credentialFields?: Array<Record<string, unknown>>;
}

export interface AiProviderItem {
  id: number;
  providerName: string;
  platformCode?: string;
  displayName: string;
  platformLabel?: string;
  providerType?: string;
  endpointStyle?: string;
  syncStrategy?: string;
  authScheme?: string;
  apiBaseUrl: string;
  docsUrl?: string;
  modelName?: string;
  capabilityCodes: string[];
  featureFlagsJson?: string;
  credentialSchemaJson?: string;
  providerSettingsJson?: string;
  hasApiKey: number;
  hasApiSecret: number;
  apiKeyMasked?: string;
  apiSecretMasked?: string;
  requestTimeoutMs?: number;
  maxRetries?: number;
  quotaDaily?: number;
  costPer1kTokens?: number;
  status: number;
  healthStatus?: string;
  healthMessage?: string;
  lastInventorySyncStatus?: string;
  lastInventorySyncMessage?: string;
  lastInventorySyncedAt?: string;
  inventoryRecordCount?: number;
  lastHealthCheckedAt?: string;
  lastSuccessAt?: string;
  lastFailureAt?: string;
  secretUpdatedAt?: string;
}

export interface AiProviderPayload {
  providerName: string;
  platformCode?: string;
  displayName: string;
  platformLabel?: string;
  providerType?: string;
  endpointStyle?: string;
  syncStrategy?: string;
  authScheme?: string;
  apiBaseUrl: string;
  docsUrl?: string;
  modelName?: string;
  capabilityCodes?: string[];
  featureFlagsJson?: string;
  credentialSchemaJson?: string;
  providerSettingsJson?: string;
  requestTimeoutMs?: number;
  maxRetries?: number;
  quotaDaily?: number;
  costPer1kTokens?: number;
  status?: number;
  apiKey?: string;
  replaceApiKey?: boolean;
  apiSecret?: string;
  replaceApiSecret?: boolean;
}

export interface AiProviderTestPayload {
  capabilityCode?: string;
  prompt?: string;
  modelOverride?: string;
}

export interface AiProviderTestResult {
  providerId: number;
  providerName: string;
  endpointStyle?: string;
  success: number;
  latencyMs?: number;
  resolvedModel?: string;
  message?: string;
  preview?: string;
  taskId?: string;
}

export interface AiProviderSyncJobItem {
  id: number;
  providerId: number;
  providerName?: string;
  providerDisplayName?: string;
  platformCode?: string;
  syncStrategy?: string;
  jobStatus: string;
  message?: string;
  errorDetail?: string;
  discoveredCount?: number;
  createdCount?: number;
  updatedCount?: number;
  staleCount?: number;
  rawPayloadJson?: string;
  startedAt?: string;
  finishedAt?: string;
  createdAt?: string;
}

export interface AiInventoryItem {
  id: number;
  providerId: number;
  providerName?: string;
  providerDisplayName?: string;
  providerPlatformCode?: string;
  inventoryCode: string;
  externalId: string;
  displayName: string;
  inventoryType?: string;
  modalityCodes?: string[];
  capabilityCodes?: string[];
  syncStrategy?: string;
  sourceType?: string;
  availabilityStatus?: string;
  endpointPath?: string;
  contextWindowTokens?: number;
  inputPricePer1k?: number;
  outputPricePer1k?: number;
  imagePricePerCall?: number;
  audioPricePerMinute?: number;
  featureFlagsJson?: string;
  rawPayloadJson?: string;
  isDefault?: number;
  sortOrder?: number;
  lastSeenAt?: string;
  syncedAt?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface AiVoiceItem {
  id: number;
  providerId: number;
  providerName?: string;
  providerDisplayName?: string;
  providerPlatformCode?: string;
  inventoryCode: string;
  externalId: string;
  voiceCode: string;
  parentModelCode?: string;
  displayName: string;
  sourceType?: string;
  availabilityStatus?: string;
  cloneStatus?: string;
  previewUrl?: string;
  previewText?: string;
  languageCodes?: string[];
  ownerAdminId?: number;
  ownerAdminName?: string;
  sourceAssetId?: number;
  featureFlagsJson?: string;
  rawPayloadJson?: string;
  syncedAt?: string;
  createdAt?: string;
  updatedAt?: string;
  lastVerifiedAt?: string;
}

export interface AiVoicePreviewPayload {
  providerId: number;
  modelCode: string;
  voiceCode: string;
  scriptText?: string;
  languageCode?: string;
  instruction?: string;
  format?: string;
  sampleRate?: number;
  rate?: number;
  pitch?: number;
  volume?: number;
}

export interface AiVoicePreviewResult {
  providerId: number;
  modelCode: string;
  voiceCode: string;
  previewUrl: string;
  mimeType?: string;
  fileSizeBytes?: number;
  metadataJson?: string;
}

export interface AiVoiceClonePayload {
  providerId: number;
  targetModel: string;
  voiceName: string;
  sourceAssetId?: number;
  sourceUrl?: string;
  previewText?: string;
  languageCodes?: string[];
}

export interface AiInventoryPayload {
  providerId: number;
  inventoryCode: string;
  externalId: string;
  displayName: string;
  inventoryType?: string;
  modalityCodes?: string[];
  capabilityCodes?: string[];
  syncStrategy?: string;
  sourceType?: string;
  availabilityStatus?: string;
  endpointPath?: string;
  contextWindowTokens?: number;
  inputPricePer1k?: number;
  outputPricePer1k?: number;
  imagePricePerCall?: number;
  audioPricePerMinute?: number;
  featureFlagsJson?: string;
  rawPayloadJson?: string;
  isDefault?: number;
  sortOrder?: number;
}

export interface AiOverviewSummary {
  totalCapabilities: number;
  enabledCapabilities: number;
  enabledProviders: number;
  healthyProviders: number;
  inventoryRecords: number;
  staleProviders: number;
  requests24h: number;
  failures24h: number;
  fallbacks24h: number;
  estimatedCost24h?: number;
  activeJobs: number;
}

export interface AiCapabilityItem {
  id: number;
  domainCode: string;
  capabilityCode: string;
  displayNameZht: string;
  summaryZht?: string;
  supportsPublicRuntime: number;
  supportsAdminCreative: number;
  supportsText: number;
  supportsImage: number;
  supportsAudio: number;
  supportsVision: number;
  status: string;
  sortOrder?: number;
  policyCount?: number;
  requestCount24h?: number;
  failedCount24h?: number;
  fallbackCount24h?: number;
}

export interface AiOverviewProviderHealth {
  providerId: number;
  providerName: string;
  displayName: string;
  healthStatus?: string;
  healthMessage?: string;
  endpointStyle?: string;
  providerType?: string;
  status: number;
  lastInventorySyncStatus?: string;
  lastInventorySyncedAt?: string;
  inventoryRecordCount?: number;
  requestCount24h?: number;
  failureCount24h?: number;
  averageLatencyMs?: number;
}

export interface AiOverviewAlert {
  level: string;
  title: string;
  message: string;
}

export interface AiOverview {
  summary: AiOverviewSummary;
  capabilities: AiCapabilityItem[];
  providers: AiOverviewProviderHealth[];
  alerts: AiOverviewAlert[];
  recentJobs: AiGenerationJobItem[];
  recentLogs: AiLogItem[];
}

export interface AiProviderBindingItem {
  id?: number;
  providerId: number;
  providerName?: string;
  providerDisplayName?: string;
  inventoryId?: number;
  inventoryCode?: string;
  inventoryDisplayName?: string;
  bindingRole?: string;
  routeMode?: string;
  sortOrder?: number;
  enabled?: number;
  modelOverride?: string;
  weightPercent?: number;
  timeoutMsOverride?: number;
  retryCountOverride?: number;
  parameterOverrideJson?: string;
  notes?: string;
}

export interface AiPolicyItem {
  id: number;
  capabilityId: number;
  capabilityCode: string;
  capabilityNameZht?: string;
  policyCode: string;
  policyName: string;
  policyType?: string;
  executionMode?: string;
  responseMode?: string;
  scenePresetCode?: string;
  manualSwitchProviderId?: number;
  manualSwitchProviderName?: string;
  defaultModel?: string;
  systemPrompt?: string;
  promptTemplate?: string;
  parameterConfigJson?: string;
  expertOverrideJson?: string;
  multimodalEnabled?: number;
  voiceEnabled?: number;
  structuredOutputEnabled?: number;
  temperature?: number;
  maxTokens?: number;
  responseSchemaJson?: string;
  postProcessRulesJson?: string;
  status: string;
  sortOrder?: number;
  notes?: string;
  providerBindings?: AiProviderBindingItem[];
}

export interface AiPolicyPayload {
  capabilityCode: string;
  policyCode: string;
  policyName: string;
  policyType?: string;
  executionMode?: string;
  responseMode?: string;
  scenePresetCode?: string;
  defaultModel?: string;
  systemPrompt?: string;
  promptTemplate?: string;
  parameterConfigJson?: string;
  expertOverrideJson?: string;
  responseSchemaJson?: string;
  postProcessRulesJson?: string;
  manualSwitchProviderId?: number;
  multimodalEnabled?: number;
  voiceEnabled?: number;
  structuredOutputEnabled?: number;
  temperature?: number;
  maxTokens?: number;
  status?: string;
  sortOrder?: number;
  notes?: string;
  providerBindings?: AiProviderBindingItem[];
}

export interface AiQuotaRuleItem {
  id: number;
  capabilityId: number;
  capabilityCode: string;
  capabilityNameZht?: string;
  policyId?: number;
  scopeType?: string;
  scopeValue?: string;
  windowType?: string;
  windowSize?: number;
  requestLimit?: number;
  tokenLimit?: number;
  suspiciousConcurrencyThreshold?: number;
  actionMode?: string;
  status: string;
  notes?: string;
}

export interface AiQuotaRulePayload {
  capabilityCode: string;
  policyId?: number;
  scopeType?: string;
  scopeValue?: string;
  windowType?: string;
  windowSize?: number;
  requestLimit?: number;
  tokenLimit?: number;
  suspiciousConcurrencyThreshold?: number;
  actionMode?: string;
  status?: string;
  notes?: string;
}

export interface AiPromptTemplateItem {
  id: number;
  capabilityId: number;
  capabilityCode: string;
  capabilityNameZht?: string;
  templateCode: string;
  templateName: string;
  templateType?: string;
  assetSlotCode?: string;
  systemPrompt?: string;
  promptTemplate: string;
  variableSchemaJson?: string;
  outputConstraintsJson?: string;
  defaultProviderId?: number;
  defaultProviderName?: string;
  defaultPolicyId?: number;
  defaultPolicyName?: string;
  status: string;
  sortOrder?: number;
  updatedAt?: string;
}

export interface AiPromptTemplatePayload {
  capabilityCode: string;
  templateCode: string;
  templateName: string;
  templateType?: string;
  assetSlotCode?: string;
  systemPrompt?: string;
  promptTemplate: string;
  variableSchemaJson?: string;
  outputConstraintsJson?: string;
  defaultProviderId?: number;
  defaultPolicyId?: number;
  status?: string;
  sortOrder?: number;
}

export interface AiGenerationCandidateItem {
  id: number;
  candidateIndex?: number;
  candidateType: string;
  storageUrl?: string;
  mimeType?: string;
  fileSizeBytes?: number;
  widthPx?: number;
  heightPx?: number;
  durationMs?: number;
  transcriptText?: string;
  previewText?: string;
  metadataJson?: string;
  isSelected?: number;
  isFinalized?: number;
  finalizedAssetId?: number;
  createdAt?: string;
}

export interface AiGenerationJobItem {
  id: number;
  capabilityId: number;
  capabilityCode?: string;
  capabilityNameZht?: string;
  policyId?: number;
  policyName?: string;
  promptTemplateId?: number;
  promptTemplateName?: string;
  providerId?: number;
  providerName?: string;
  inventoryId?: number;
  inventoryCode?: string;
  inventoryDisplayName?: string;
  ownerAdminId?: number;
  ownerAdminName?: string;
  generationType: string;
  sourceScope?: string;
  sourceScopeId?: number;
  jobStatus: string;
  promptTitle?: string;
  promptText?: string;
  promptVariablesJson?: string;
  requestPayloadJson?: string;
  providerRequestId?: string;
  resultSummary?: string;
  errorMessage?: string;
  latestCandidateId?: number;
  finalizedCandidateId?: number;
  createdAt?: string;
  updatedAt?: string;
  candidates?: AiGenerationCandidateItem[];
}

export interface AiGenerationJobPayload {
  capabilityCode: string;
  policyId?: number;
  promptTemplateId?: number;
  providerId?: number;
  inventoryId?: number;
  generationType: string;
  sourceScope?: string;
  sourceScopeId?: number;
  promptTitle?: string;
  promptText?: string;
  promptVariablesJson?: string;
  requestPayloadJson?: string;
}

export interface AiCandidateFinalizePayload {
  assetKind?: string;
  localeCode?: string;
  status?: string;
}

export interface AiLogItem {
  id: number;
  providerId?: number;
  providerName?: string;
  policyId?: number;
  policyName?: string;
  capabilityCode?: string;
  inventoryId?: number;
  inventoryCode?: string;
  adminOwnerId?: number;
  adminOwnerName?: string;
  userOpenid?: string;
  requestType?: string;
  inputDataHash?: string;
  outputSummary?: string;
  latencyMs?: number;
  tokensUsed?: number;
  costUsd?: number;
  success: number;
  fallbackTriggered?: number;
  blockedReason?: string;
  traceId?: string;
  errorMessage?: string;
  createdAt?: string;
}

export interface AiPlatformSettings {
  inventoryFreshnessHours: number;
  syncHistoryLimit: number;
  dailyCostAlertUsd: number;
  providerFailureRateWarning: number;
  recentWindowHours: number;
  allowOperatorGlobalHistory: number;
}

export interface AiPlatformSettingsPayload {
  inventoryFreshnessHours?: number;
  syncHistoryLimit?: number;
  dailyCostAlertUsd?: number;
  providerFailureRateWarning?: number;
  recentWindowHours?: number;
  allowOperatorGlobalHistory?: number;
}

export const getAiOverview = () => request.get<AiOverview>('/api/admin/v1/ai/overview');

export const getAiCapabilities = () => request.get<AiCapabilityItem[]>('/api/admin/v1/ai/capabilities');

export const getAiProviderTemplates = () => request.get<AiProviderTemplateItem[]>('/api/admin/v1/ai/provider-templates');

export const getAiProviders = () => request.get<AiProviderItem[]>('/api/admin/v1/ai/providers');

export const createAiProvider = (data: AiProviderPayload) => request.post<AiProviderItem>('/api/admin/v1/ai/providers', data);

export const updateAiProvider = (providerId: number, data: AiProviderPayload) =>
  request.put<AiProviderItem>(`/api/admin/v1/ai/providers/${providerId}`, data);

export const deleteAiProvider = (providerId: number) => request.delete<boolean>(`/api/admin/v1/ai/providers/${providerId}`);

export const testAiProvider = (providerId: number, data?: AiProviderTestPayload) =>
  request.post<AiProviderTestResult>(`/api/admin/v1/ai/providers/${providerId}/test`, data);

export const syncAiProviderInventory = (providerId: number) =>
  request.post<AiProviderSyncJobItem>(`/api/admin/v1/ai/providers/${providerId}/sync-inventory`);

export const getAiProviderSyncJobs = (providerId: number) =>
  request.get<AiProviderSyncJobItem[]>(`/api/admin/v1/ai/providers/${providerId}/sync-jobs`);

export const getAiInventory = (params?: { providerId?: number; capabilityCode?: string; sourceType?: string }) =>
  request.get<AiInventoryItem[]>('/api/admin/v1/ai/inventory', { params });

export const getAiVoices = (params?: {
  providerId?: number;
  modelCode?: string;
  languageCode?: string;
  sourceType?: string;
}) => request.get<AiVoiceItem[]>('/api/admin/v1/ai/voices', { params });

export const syncAiProviderVoices = (providerId: number, data?: { modelCode?: string }) =>
  request.post<AiVoiceItem[]>(`/api/admin/v1/ai/providers/${providerId}/sync-voices`, data);

export const previewAiVoice = (data: AiVoicePreviewPayload) =>
  request.post<AiVoicePreviewResult>('/api/admin/v1/ai/voices/preview', data);

export const createAiVoiceClone = (data: AiVoiceClonePayload) =>
  request.post<AiVoiceItem>('/api/admin/v1/ai/voices/clone', data);

export const refreshAiVoice = (voiceId: number) =>
  request.post<AiVoiceItem>(`/api/admin/v1/ai/voices/${voiceId}/refresh`);

export const deleteAiVoice = (voiceId: number) =>
  request.delete<boolean>(`/api/admin/v1/ai/voices/${voiceId}`);

export const createAiInventory = (data: AiInventoryPayload) =>
  request.post<AiInventoryItem>('/api/admin/v1/ai/inventory', data);

export const updateAiInventory = (inventoryId: number, data: AiInventoryPayload) =>
  request.put<AiInventoryItem>(`/api/admin/v1/ai/inventory/${inventoryId}`, data);

export const deleteAiInventory = (inventoryId: number) =>
  request.delete<boolean>(`/api/admin/v1/ai/inventory/${inventoryId}`);

export const getAiPolicies = (params?: { capabilityCode?: string }) =>
  request.get<AiPolicyItem[]>('/api/admin/v1/ai/policies', { params });

export const createAiPolicy = (data: AiPolicyPayload) => request.post<AiPolicyItem>('/api/admin/v1/ai/policies', data);

export const updateAiPolicy = (policyId: number, data: AiPolicyPayload) =>
  request.put<AiPolicyItem>(`/api/admin/v1/ai/policies/${policyId}`, data);

export const deleteAiPolicy = (policyId: number) => request.delete<boolean>(`/api/admin/v1/ai/policies/${policyId}`);

export const getAiQuotaRules = (params?: { capabilityCode?: string }) =>
  request.get<AiQuotaRuleItem[]>('/api/admin/v1/ai/quota-rules', { params });

export const createAiQuotaRule = (data: AiQuotaRulePayload) =>
  request.post<AiQuotaRuleItem>('/api/admin/v1/ai/quota-rules', data);

export const updateAiQuotaRule = (quotaRuleId: number, data: AiQuotaRulePayload) =>
  request.put<AiQuotaRuleItem>(`/api/admin/v1/ai/quota-rules/${quotaRuleId}`, data);

export const deleteAiQuotaRule = (quotaRuleId: number) =>
  request.delete<boolean>(`/api/admin/v1/ai/quota-rules/${quotaRuleId}`);

export const getAiLogs = (params?: {
  pageNum?: number;
  pageSize?: number;
  capabilityCode?: string;
  success?: number;
  providerId?: number;
}) => request.get<PaginationResponse<AiLogItem>>('/api/admin/v1/ai/logs', { params });

export const getAiPromptTemplates = (params?: { capabilityCode?: string; templateType?: string }) =>
  request.get<AiPromptTemplateItem[]>('/api/admin/v1/ai/prompt-templates', { params });

export const createAiPromptTemplate = (data: AiPromptTemplatePayload) =>
  request.post<AiPromptTemplateItem>('/api/admin/v1/ai/prompt-templates', data);

export const updateAiPromptTemplate = (templateId: number, data: AiPromptTemplatePayload) =>
  request.put<AiPromptTemplateItem>(`/api/admin/v1/ai/prompt-templates/${templateId}`, data);

export const deleteAiPromptTemplate = (templateId: number) =>
  request.delete<boolean>(`/api/admin/v1/ai/prompt-templates/${templateId}`);

export const getAiGenerationJobs = (params?: {
  pageNum?: number;
  pageSize?: number;
  capabilityCode?: string;
  generationType?: string;
  jobStatus?: string;
}) => request.get<PaginationResponse<AiGenerationJobItem>>('/api/admin/v1/ai/generation-jobs', { params });

export const getAiGenerationJobDetail = (jobId: number) =>
  request.get<AiGenerationJobItem>(`/api/admin/v1/ai/generation-jobs/${jobId}`);

export const createAiGenerationJob = (data: AiGenerationJobPayload) =>
  request.post<AiGenerationJobItem>('/api/admin/v1/ai/generation-jobs', data);

export const refreshAiGenerationJob = (jobId: number) =>
  request.post<AiGenerationJobItem>(`/api/admin/v1/ai/generation-jobs/${jobId}/refresh`);

export const finalizeAiGenerationCandidate = (candidateId: number, data?: AiCandidateFinalizePayload) =>
  request.post<AiGenerationJobItem>(`/api/admin/v1/ai/generation-candidates/${candidateId}/finalize`, data);

export const restoreAiGenerationCandidate = (candidateId: number) =>
  request.post<AiGenerationJobItem>(`/api/admin/v1/ai/generation-candidates/${candidateId}/restore`);

export const getAiPlatformSettings = () =>
  request.get<AiPlatformSettings>('/api/admin/v1/ai/platform-settings');

export const updateAiPlatformSettings = (data: AiPlatformSettingsPayload) =>
  request.put<AiPlatformSettings>('/api/admin/v1/ai/platform-settings', data);
