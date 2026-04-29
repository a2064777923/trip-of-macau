package com.aoxiaoyou.admin.service.impl;

import com.aoxiaoyou.admin.common.api.PageResponse;
import com.aoxiaoyou.admin.common.exception.BusinessException;
import com.aoxiaoyou.admin.dto.request.AdminContentAssetBatchUploadRequest;
import com.aoxiaoyou.admin.dto.request.AdminContentAssetUploadRequest;
import com.aoxiaoyou.admin.dto.request.AdminContentAssetUpsertRequest;
import com.aoxiaoyou.admin.dto.request.AdminNotificationUpsertRequest;
import com.aoxiaoyou.admin.dto.request.AdminRuntimeSettingUpsertRequest;
import com.aoxiaoyou.admin.dto.request.AdminStampUpsertRequest;
import com.aoxiaoyou.admin.dto.request.AdminTipArticleUpsertRequest;
import com.aoxiaoyou.admin.dto.response.AdminContentAssetBatchUploadResponse;
import com.aoxiaoyou.admin.dto.response.AdminContentAssetResponse;
import com.aoxiaoyou.admin.dto.response.AdminContentAssetUsageItemResponse;
import com.aoxiaoyou.admin.dto.response.AdminContentAssetUsageSummaryResponse;
import com.aoxiaoyou.admin.dto.response.AdminNotificationResponse;
import com.aoxiaoyou.admin.dto.response.AdminRuntimeSettingResponse;
import com.aoxiaoyou.admin.dto.response.AdminStampResponse;
import com.aoxiaoyou.admin.dto.response.AdminTipArticleResponse;
import com.aoxiaoyou.admin.entity.AppRuntimeSetting;
import com.aoxiaoyou.admin.entity.Badge;
import com.aoxiaoyou.admin.entity.Building;
import com.aoxiaoyou.admin.entity.City;
import com.aoxiaoyou.admin.entity.Collectible;
import com.aoxiaoyou.admin.entity.ContentAsset;
import com.aoxiaoyou.admin.entity.ContentAssetLink;
import com.aoxiaoyou.admin.entity.Notification;
import com.aoxiaoyou.admin.entity.Poi;
import com.aoxiaoyou.admin.entity.Reward;
import com.aoxiaoyou.admin.entity.Stamp;
import com.aoxiaoyou.admin.entity.StoryChapter;
import com.aoxiaoyou.admin.entity.StoryContentBlock;
import com.aoxiaoyou.admin.entity.StoryLine;
import com.aoxiaoyou.admin.entity.SubMap;
import com.aoxiaoyou.admin.entity.TipArticle;
import com.aoxiaoyou.admin.mapper.AppRuntimeSettingMapper;
import com.aoxiaoyou.admin.mapper.BadgeMapper;
import com.aoxiaoyou.admin.mapper.BuildingMapper;
import com.aoxiaoyou.admin.mapper.CityMapper;
import com.aoxiaoyou.admin.mapper.CollectibleMapper;
import com.aoxiaoyou.admin.mapper.ContentAssetLinkMapper;
import com.aoxiaoyou.admin.mapper.ContentAssetMapper;
import com.aoxiaoyou.admin.mapper.NotificationMapper;
import com.aoxiaoyou.admin.mapper.PoiMapper;
import com.aoxiaoyou.admin.mapper.RewardMapper;
import com.aoxiaoyou.admin.mapper.StampMapper;
import com.aoxiaoyou.admin.mapper.StoryChapterMapper;
import com.aoxiaoyou.admin.mapper.StoryContentBlockMapper;
import com.aoxiaoyou.admin.mapper.StoryLineMapper;
import com.aoxiaoyou.admin.mapper.SubMapMapper;
import com.aoxiaoyou.admin.mapper.TipArticleMapper;
import com.aoxiaoyou.admin.media.CosAssetStorageService;
import com.aoxiaoyou.admin.media.MediaIntakeService;
import com.aoxiaoyou.admin.service.AdminContentManagementService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminContentManagementServiceImpl implements AdminContentManagementService {

    private final AppRuntimeSettingMapper runtimeSettingMapper;
    private final ContentAssetMapper contentAssetMapper;
    private final ContentAssetLinkMapper contentAssetLinkMapper;
    private final TipArticleMapper tipArticleMapper;
    private final NotificationMapper notificationMapper;
    private final StampMapper stampMapper;
    private final CityMapper cityMapper;
    private final SubMapMapper subMapMapper;
    private final PoiMapper poiMapper;
    private final StoryLineMapper storyLineMapper;
    private final StoryChapterMapper storyChapterMapper;
    private final StoryContentBlockMapper storyContentBlockMapper;
    private final RewardMapper rewardMapper;
    private final CollectibleMapper collectibleMapper;
    private final BadgeMapper badgeMapper;
    private final BuildingMapper buildingMapper;
    private final CosAssetStorageService cosAssetStorageService;
    private final MediaIntakeService mediaIntakeService;

    @Override
    public PageResponse<AdminRuntimeSettingResponse> pageRuntimeSettings(long pageNum, long pageSize, String settingGroup, String status, String keyword) {
        Page<AppRuntimeSetting> page = runtimeSettingMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<AppRuntimeSetting>()
                        .eq(StringUtils.hasText(settingGroup), AppRuntimeSetting::getSettingGroup, settingGroup)
                        .eq(StringUtils.hasText(status), AppRuntimeSetting::getStatus, status)
                        .and(StringUtils.hasText(keyword), q -> q
                                .like(AppRuntimeSetting::getSettingKey, keyword)
                                .or().like(AppRuntimeSetting::getTitleZh, keyword)
                                .or().like(AppRuntimeSetting::getTitleEn, keyword)
                                .or().like(AppRuntimeSetting::getTitlePt, keyword))
                        .orderByAsc(AppRuntimeSetting::getSortOrder)
                        .orderByAsc(AppRuntimeSetting::getId));
        Page<AdminRuntimeSettingResponse> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(page.getRecords().stream().map(this::toRuntimeSettingResponse).toList());
        return PageResponse.of(result);
    }

    @Override
    public AdminRuntimeSettingResponse createRuntimeSetting(AdminRuntimeSettingUpsertRequest request) {
        AppRuntimeSetting item = new AppRuntimeSetting();
        applyRuntimeSettingRequest(item, request);
        runtimeSettingMapper.insert(item);
        return toRuntimeSettingResponse(requireRuntimeSetting(item.getId()));
    }

    @Override
    public AdminRuntimeSettingResponse updateRuntimeSetting(Long id, AdminRuntimeSettingUpsertRequest request) {
        AppRuntimeSetting item = requireRuntimeSetting(id);
        applyRuntimeSettingRequest(item, request);
        runtimeSettingMapper.updateById(item);
        return toRuntimeSettingResponse(requireRuntimeSetting(id));
    }

    @Override
    public void deleteRuntimeSetting(Long id) {
        requireRuntimeSetting(id);
        runtimeSettingMapper.deleteById(id);
    }

    @Override
    public PageResponse<AdminContentAssetResponse> pageAssets(
            long pageNum,
            long pageSize,
            String assetKind,
            String status,
            String uploadSource,
            String processingPolicyCode,
            String processingStatus,
            String keyword) {
        final Long keywordAssetId = parseKeywordAssetId(keyword);

        Page<ContentAsset> page = contentAssetMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<ContentAsset>()
                        .eq(StringUtils.hasText(assetKind), ContentAsset::getAssetKind, assetKind)
                        .eq(StringUtils.hasText(status), ContentAsset::getStatus, status)
                        .eq(StringUtils.hasText(uploadSource), ContentAsset::getUploadSource, uploadSource)
                        .eq(StringUtils.hasText(processingPolicyCode), ContentAsset::getProcessingPolicyCode, processingPolicyCode)
                        .eq(StringUtils.hasText(processingStatus), ContentAsset::getProcessingStatus, processingStatus)
                        .and(StringUtils.hasText(keyword), q -> q
                                .eq(keywordAssetId != null, ContentAsset::getId, keywordAssetId)
                                .or().like(ContentAsset::getObjectKey, keyword)
                                .or().like(ContentAsset::getCanonicalUrl, keyword)
                                .or().like(ContentAsset::getBucketName, keyword)
                                .or().like(ContentAsset::getOriginalFilename, keyword)
                                .or().like(ContentAsset::getClientRelativePath, keyword)
                                .or().like(ContentAsset::getUploadedByAdminName, keyword))
                        .orderByDesc(ContentAsset::getUpdatedAt)
                        .orderByDesc(ContentAsset::getId));
        Page<AdminContentAssetResponse> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(page.getRecords().stream().map(this::toContentAssetResponse).toList());
        return PageResponse.of(result);
    }

    private Long parseKeywordAssetId(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return null;
        }
        try {
            return Long.parseLong(keyword.trim());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    @Override
    public AdminContentAssetResponse uploadAsset(AdminContentAssetUploadRequest request, Long adminUserId, String adminUsername) {
        ContentAsset item = mediaIntakeService.intakeSingle(request, adminUserId, adminUsername);
        return toContentAssetResponse(requireAsset(item.getId()));
    }

    @Override
    public AdminContentAssetBatchUploadResponse batchUploadAssets(AdminContentAssetBatchUploadRequest request, Long adminUserId, String adminUsername) {
        java.util.List<ContentAsset> items = mediaIntakeService.intakeBatch(request, adminUserId, adminUsername);
        return AdminContentAssetBatchUploadResponse.builder()
                .uploadedCount(items.size())
                .failedCount(0)
                .items(items.stream().map(asset -> toContentAssetResponse(requireAsset(asset.getId()))).toList())
                .failures(java.util.List.of())
                .build();
    }

    @Override
    public AdminContentAssetResponse createAsset(AdminContentAssetUpsertRequest request) {
        ContentAsset item = new ContentAsset();
        applyAssetRequest(item, request);
        contentAssetMapper.insert(item);
        return toContentAssetResponse(requireAsset(item.getId()));
    }

    @Override
    public AdminContentAssetResponse updateAsset(Long id, AdminContentAssetUpsertRequest request) {
        ContentAsset item = requireAsset(id);
        applyAssetRequest(item, request);
        contentAssetMapper.updateById(item);
        return toContentAssetResponse(requireAsset(id));
    }

    @Override
    public AdminContentAssetUsageSummaryResponse getAssetUsages(Long id) {
        ContentAsset asset = requireAsset(id);
        List<AdminContentAssetUsageItemResponse> usages = listAssetUsages(asset);
        return AdminContentAssetUsageSummaryResponse.builder()
                .assetId(asset.getId())
                .usageCount(usages.size())
                .usages(usages)
                .build();
    }

    @Override
    public void deleteAsset(Long id) {
        ContentAsset item = requireAsset(id);
        List<AdminContentAssetUsageItemResponse> usages = listAssetUsages(item);
        if (!usages.isEmpty()) {
            throw new BusinessException(4055, "Content asset is still in use and cannot be deleted");
        }
        cosAssetStorageService.deleteAsset(item.getBucketName(), item.getObjectKey());
        contentAssetMapper.deleteById(id);
    }

    @Override
    public PageResponse<AdminTipArticleResponse> pageTips(long pageNum, long pageSize, Long cityId, String status, String keyword) {
        Page<TipArticle> page = tipArticleMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<TipArticle>()
                        .eq(cityId != null, TipArticle::getCityId, cityId)
                        .eq(StringUtils.hasText(status), TipArticle::getStatus, status)
                        .and(StringUtils.hasText(keyword), q -> q
                                .like(TipArticle::getCode, keyword)
                                .or().like(TipArticle::getTitleZh, keyword)
                                .or().like(TipArticle::getTitleEn, keyword)
                                .or().like(TipArticle::getTitlePt, keyword))
                        .orderByAsc(TipArticle::getSortOrder)
                        .orderByAsc(TipArticle::getId));
        Page<AdminTipArticleResponse> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(page.getRecords().stream().map(this::toTipArticleResponse).toList());
        return PageResponse.of(result);
    }

    @Override
    public AdminTipArticleResponse createTip(AdminTipArticleUpsertRequest request) {
        verifyCity(request.getCityId());
        TipArticle item = new TipArticle();
        applyTipRequest(item, request);
        tipArticleMapper.insert(item);
        return toTipArticleResponse(requireTip(item.getId()));
    }

    @Override
    public AdminTipArticleResponse updateTip(Long id, AdminTipArticleUpsertRequest request) {
        verifyCity(request.getCityId());
        TipArticle item = requireTip(id);
        applyTipRequest(item, request);
        tipArticleMapper.updateById(item);
        return toTipArticleResponse(requireTip(id));
    }

    @Override
    public void deleteTip(Long id) {
        requireTip(id);
        tipArticleMapper.deleteById(id);
    }

    @Override
    public PageResponse<AdminNotificationResponse> pageNotifications(long pageNum, long pageSize, String status, String keyword) {
        Page<Notification> page = notificationMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<Notification>()
                        .eq(StringUtils.hasText(status), Notification::getStatus, status)
                        .and(StringUtils.hasText(keyword), q -> q
                                .like(Notification::getCode, keyword)
                                .or().like(Notification::getTitleZh, keyword)
                                .or().like(Notification::getTitleEn, keyword)
                                .or().like(Notification::getTitlePt, keyword))
                        .orderByAsc(Notification::getSortOrder)
                        .orderByAsc(Notification::getId));
        Page<AdminNotificationResponse> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(page.getRecords().stream().map(this::toNotificationResponse).toList());
        return PageResponse.of(result);
    }

    @Override
    public AdminNotificationResponse createNotification(AdminNotificationUpsertRequest request) {
        Notification item = new Notification();
        applyNotificationRequest(item, request);
        notificationMapper.insert(item);
        return toNotificationResponse(requireNotification(item.getId()));
    }

    @Override
    public AdminNotificationResponse updateNotification(Long id, AdminNotificationUpsertRequest request) {
        Notification item = requireNotification(id);
        applyNotificationRequest(item, request);
        notificationMapper.updateById(item);
        return toNotificationResponse(requireNotification(id));
    }

    @Override
    public void deleteNotification(Long id) {
        requireNotification(id);
        notificationMapper.deleteById(id);
    }

    @Override
    public PageResponse<AdminStampResponse> pageStamps(long pageNum, long pageSize, String status, String keyword) {
        Page<Stamp> page = stampMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<Stamp>()
                        .eq(StringUtils.hasText(status), Stamp::getStatus, status)
                        .and(StringUtils.hasText(keyword), q -> q
                                .like(Stamp::getCode, keyword)
                                .or().like(Stamp::getNameZh, keyword)
                                .or().like(Stamp::getNameEn, keyword)
                                .or().like(Stamp::getNamePt, keyword))
                        .orderByAsc(Stamp::getSortOrder)
                        .orderByAsc(Stamp::getId));
        Page<AdminStampResponse> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(page.getRecords().stream().map(this::toStampResponse).toList());
        return PageResponse.of(result);
    }

    @Override
    public AdminStampResponse createStamp(AdminStampUpsertRequest request) {
        verifyPoi(request.getRelatedPoiId());
        verifyStoryline(request.getRelatedStorylineId());
        Stamp item = new Stamp();
        applyStampRequest(item, request);
        stampMapper.insert(item);
        return toStampResponse(requireStamp(item.getId()));
    }

    @Override
    public AdminStampResponse updateStamp(Long id, AdminStampUpsertRequest request) {
        verifyPoi(request.getRelatedPoiId());
        verifyStoryline(request.getRelatedStorylineId());
        Stamp item = requireStamp(id);
        applyStampRequest(item, request);
        stampMapper.updateById(item);
        return toStampResponse(requireStamp(id));
    }

    @Override
    public void deleteStamp(Long id) {
        requireStamp(id);
        stampMapper.deleteById(id);
    }

    private List<AdminContentAssetUsageItemResponse> listAssetUsages(ContentAsset asset) {
        List<AdminContentAssetUsageItemResponse> usages = new ArrayList<>();
        usages.addAll(listLinkedAssetUsages(asset.getId()));
        usages.addAll(listDirectAssetUsages(asset.getId()));
        usages.addAll(listUrlAssetUsages(asset.getCanonicalUrl()));
        usages.sort(Comparator
                .comparing(AdminContentAssetUsageItemResponse::getEntityType, Comparator.nullsLast(String::compareTo))
                .thenComparing(AdminContentAssetUsageItemResponse::getEntityName, Comparator.nullsLast(String::compareTo))
                .thenComparing(AdminContentAssetUsageItemResponse::getUsageType, Comparator.nullsLast(String::compareTo))
                .thenComparing(AdminContentAssetUsageItemResponse::getFieldName, Comparator.nullsLast(String::compareTo)));
        return usages;
    }

    private List<AdminContentAssetUsageItemResponse> listLinkedAssetUsages(Long assetId) {
        List<AdminContentAssetUsageItemResponse> usages = new ArrayList<>();
        List<ContentAssetLink> links = safeList(contentAssetLinkMapper.selectList(new LambdaQueryWrapper<ContentAssetLink>()
                .eq(ContentAssetLink::getAssetId, assetId)
                .orderByAsc(ContentAssetLink::getSortOrder)
                .orderByAsc(ContentAssetLink::getId)));
        for (ContentAssetLink link : links) {
            EntityUsageTarget target = resolveEntityTarget(link.getEntityType(), link.getEntityId());
            usages.add(buildUsage(
                    "link",
                    normalizeEntityType(link.getEntityType()),
                    link.getEntityId(),
                    target.code(),
                    target.name(),
                    defaultText(link.getUsageType(), "attachment"),
                    "assetId",
                    firstText(link.getStatus(), target.status()),
                    firstText(link.getTitleZht(), link.getTitleZh(), link.getTitleEn(), link.getTitlePt(), target.name())
            ));
        }
        return usages;
    }

    private List<AdminContentAssetUsageItemResponse> listDirectAssetUsages(Long assetId) {
        List<AdminContentAssetUsageItemResponse> usages = new ArrayList<>();

        for (AppRuntimeSetting item : safeList(runtimeSettingMapper.selectList(new LambdaQueryWrapper<AppRuntimeSetting>()
                .eq(AppRuntimeSetting::getAssetId, assetId)))) {
            usages.add(buildUsage("direct-field", "runtime_setting", item.getId(), item.getSettingKey(),
                    firstText(item.getTitleZht(), item.getTitleZh(), item.getTitleEn(), item.getTitlePt(), item.getSettingKey()),
                    "runtime-asset", "assetId", item.getStatus(), item.getSettingKey()));
        }

        for (City item : safeList(cityMapper.selectList(new LambdaQueryWrapper<City>()
                .and(q -> q.eq(City::getCoverAssetId, assetId).or().eq(City::getBannerAssetId, assetId))))) {
            if (assetId.equals(item.getCoverAssetId())) {
                usages.add(buildUsage("direct-field", "city", item.getId(), item.getCode(),
                        firstText(item.getNameZht(), item.getNameZh(), item.getNameEn(), item.getNamePt(), item.getCode()),
                        "cover", "coverAssetId", item.getStatus(), item.getCode()));
            }
            if (assetId.equals(item.getBannerAssetId())) {
                usages.add(buildUsage("direct-field", "city", item.getId(), item.getCode(),
                        firstText(item.getNameZht(), item.getNameZh(), item.getNameEn(), item.getNamePt(), item.getCode()),
                        "banner", "bannerAssetId", item.getStatus(), item.getCode()));
            }
        }

        for (SubMap item : safeList(subMapMapper.selectList(new LambdaQueryWrapper<SubMap>()
                .eq(SubMap::getCoverAssetId, assetId)))) {
            usages.add(buildUsage("direct-field", "sub_map", item.getId(), item.getCode(),
                    firstText(item.getNameZht(), item.getNameZh(), item.getNameEn(), item.getNamePt(), item.getCode()),
                    "cover", "coverAssetId", item.getStatus(), item.getCode()));
        }

        for (Poi item : safeList(poiMapper.selectList(new LambdaQueryWrapper<Poi>()
                .and(q -> q.eq(Poi::getCoverAssetId, assetId)
                        .or().eq(Poi::getMapIconAssetId, assetId)
                        .or().eq(Poi::getAudioAssetId, assetId))))) {
            String poiName = firstText(item.getNameZht(), item.getNameZh(), item.getNameEn(), item.getNamePt(), item.getCode());
            if (assetId.equals(item.getCoverAssetId())) {
                usages.add(buildUsage("direct-field", "poi", item.getId(), item.getCode(), poiName,
                        "cover", "coverAssetId", item.getStatus(), poiName));
            }
            if (assetId.equals(item.getMapIconAssetId())) {
                usages.add(buildUsage("direct-field", "poi", item.getId(), item.getCode(), poiName,
                        "map-icon", "mapIconAssetId", item.getStatus(), poiName));
            }
            if (assetId.equals(item.getAudioAssetId())) {
                usages.add(buildUsage("direct-field", "poi", item.getId(), item.getCode(), poiName,
                        "audio", "audioAssetId", item.getStatus(), poiName));
            }
        }

        for (StoryLine item : safeList(storyLineMapper.selectList(new LambdaQueryWrapper<StoryLine>()
                .and(q -> q.eq(StoryLine::getCoverAssetId, assetId).or().eq(StoryLine::getBannerAssetId, assetId))))) {
            String storylineName = firstText(item.getNameZht(), item.getNameZh(), item.getNameEn(), item.getNamePt(), item.getCode());
            if (assetId.equals(item.getCoverAssetId())) {
                usages.add(buildUsage("direct-field", "storyline", item.getId(), item.getCode(), storylineName,
                        "cover", "coverAssetId", item.getStatus(), storylineName));
            }
            if (assetId.equals(item.getBannerAssetId())) {
                usages.add(buildUsage("direct-field", "storyline", item.getId(), item.getCode(), storylineName,
                        "banner", "bannerAssetId", item.getStatus(), storylineName));
            }
        }

        for (StoryChapter item : safeList(storyChapterMapper.selectList(new LambdaQueryWrapper<StoryChapter>()
                .eq(StoryChapter::getMediaAssetId, assetId)))) {
            String chapterName = firstText(item.getTitleZht(), item.getTitleZh(), item.getTitleEn(), item.getTitlePt(),
                    "Chapter " + item.getChapterOrder());
            usages.add(buildUsage("direct-field", "story_chapter", item.getId(), String.valueOf(item.getStorylineId()), chapterName,
                    "media", "mediaAssetId", item.getStatus(), chapterName));
        }

        for (StoryContentBlock item : safeList(storyContentBlockMapper.selectList(new LambdaQueryWrapper<StoryContentBlock>()
                .and(q -> q.eq(StoryContentBlock::getPrimaryAssetId, assetId))))) {
            String blockName = firstText(item.getTitleZht(), item.getTitleZh(), item.getTitleEn(), item.getTitlePt(), item.getCode());
            usages.add(buildUsage("direct-field", "story_content_block", item.getId(), item.getCode(), blockName,
                    "primary", "primaryAssetId", item.getStatus(), blockName));
        }

        for (ContentAsset item : safeList(contentAssetMapper.selectList(new LambdaQueryWrapper<ContentAsset>()
                .and(q -> q.eq(ContentAsset::getPosterAssetId, assetId).or().eq(ContentAsset::getFallbackAssetId, assetId))))) {
            String assetName = firstText(item.getOriginalFilename(), item.getObjectKey(), "Asset #" + item.getId());
            if (assetId.equals(item.getPosterAssetId())) {
                usages.add(buildUsage("direct-field", "content_asset", item.getId(), String.valueOf(item.getId()), assetName,
                        "poster", "posterAssetId", item.getStatus(), assetName));
            }
            if (assetId.equals(item.getFallbackAssetId())) {
                usages.add(buildUsage("direct-field", "content_asset", item.getId(), String.valueOf(item.getId()), assetName,
                        "fallback", "fallbackAssetId", item.getStatus(), assetName));
            }
        }

        for (Reward item : safeList(rewardMapper.selectList(new LambdaQueryWrapper<Reward>()
                .eq(Reward::getCoverAssetId, assetId)))) {
            usages.add(buildUsage("direct-field", "reward", item.getId(), item.getCode(),
                    firstText(item.getNameZht(), item.getNameZh(), item.getNameEn(), item.getNamePt(), item.getCode()),
                    "cover", "coverAssetId", item.getStatus(), item.getCode()));
        }

        for (Notification item : safeList(notificationMapper.selectList(new LambdaQueryWrapper<Notification>()
                .eq(Notification::getCoverAssetId, assetId)))) {
            usages.add(buildUsage("direct-field", "notification", item.getId(), item.getCode(),
                    firstText(item.getTitleZht(), item.getTitleZh(), item.getTitleEn(), item.getTitlePt(), item.getCode()),
                    "cover", "coverAssetId", item.getStatus(), item.getCode()));
        }

        for (Stamp item : safeList(stampMapper.selectList(new LambdaQueryWrapper<Stamp>()
                .eq(Stamp::getIconAssetId, assetId)))) {
            usages.add(buildUsage("direct-field", "stamp", item.getId(), item.getCode(),
                    firstText(item.getNameZht(), item.getNameZh(), item.getNameEn(), item.getNamePt(), item.getCode()),
                    "icon", "iconAssetId", item.getStatus(), item.getCode()));
        }

        for (TipArticle item : safeList(tipArticleMapper.selectList(new LambdaQueryWrapper<TipArticle>()
                .eq(TipArticle::getCoverAssetId, assetId)))) {
            usages.add(buildUsage("direct-field", "tip_article", item.getId(), item.getCode(),
                    firstText(item.getTitleZht(), item.getTitleZh(), item.getTitleEn(), item.getTitlePt(), item.getCode()),
                    "cover", "coverAssetId", item.getStatus(), item.getCode()));
        }

        return usages;
    }

    private List<AdminContentAssetUsageItemResponse> listUrlAssetUsages(String canonicalUrl) {
        if (!StringUtils.hasText(canonicalUrl)) {
            return List.of();
        }
        List<AdminContentAssetUsageItemResponse> usages = new ArrayList<>();

        for (Collectible item : safeList(collectibleMapper.selectList(new LambdaQueryWrapper<Collectible>()
                .and(q -> q.eq(Collectible::getImageUrl, canonicalUrl).or().eq(Collectible::getAnimationUrl, canonicalUrl))))) {
            if (canonicalUrl.equals(item.getImageUrl())) {
                usages.add(buildUsage("url-field", "collectible", item.getId(), item.getCollectibleCode(), item.getNameZh(),
                        "image", "imageUrl", item.getStatus(), item.getCollectibleCode()));
            }
            if (canonicalUrl.equals(item.getAnimationUrl())) {
                usages.add(buildUsage("url-field", "collectible", item.getId(), item.getCollectibleCode(), item.getNameZh(),
                        "animation", "animationUrl", item.getStatus(), item.getCollectibleCode()));
            }
        }

        for (Badge item : safeList(badgeMapper.selectList(new LambdaQueryWrapper<Badge>()
                .and(q -> q.eq(Badge::getIconUrl, canonicalUrl)
                        .or().eq(Badge::getImageUrl, canonicalUrl)
                        .or().eq(Badge::getAnimationUnlock, canonicalUrl))))) {
            if (canonicalUrl.equals(item.getIconUrl())) {
                usages.add(buildUsage("url-field", "badge", item.getId(), item.getBadgeCode(), item.getNameZh(),
                        "icon", "iconUrl", item.getStatus(), item.getBadgeCode()));
            }
            if (canonicalUrl.equals(item.getImageUrl())) {
                usages.add(buildUsage("url-field", "badge", item.getId(), item.getBadgeCode(), item.getNameZh(),
                        "image", "imageUrl", item.getStatus(), item.getBadgeCode()));
            }
            if (canonicalUrl.equals(item.getAnimationUnlock())) {
                usages.add(buildUsage("url-field", "badge", item.getId(), item.getBadgeCode(), item.getNameZh(),
                        "animation", "animationUnlock", item.getStatus(), item.getBadgeCode()));
            }
        }

        for (Building item : safeList(buildingMapper.selectList(new LambdaQueryWrapper<Building>()
                .eq(Building::getCoverImageUrl, canonicalUrl)))) {
            usages.add(buildUsage("url-field", "building", item.getId(), item.getBuildingCode(), item.getNameZh(),
                    "cover", "coverImageUrl", item.getStatus(), item.getBuildingCode()));
        }

        return usages;
    }

    private AdminContentAssetUsageItemResponse buildUsage(
            String relationType,
            String entityType,
            Long entityId,
            String entityCode,
            String entityName,
            String usageType,
            String fieldName,
            String status,
            String title) {
        return AdminContentAssetUsageItemResponse.builder()
                .relationType(relationType)
                .entityType(entityType)
                .entityId(entityId)
                .entityCode(entityCode)
                .entityName(entityName)
                .usageType(usageType)
                .fieldName(fieldName)
                .status(status)
                .title(title)
                .build();
    }

    private EntityUsageTarget resolveEntityTarget(String entityType, Long entityId) {
        if (entityId == null) {
            return new EntityUsageTarget(null, null, null);
        }
        return switch (normalizeEntityType(entityType)) {
            case "city" -> {
                City item = cityMapper.selectById(entityId);
                yield item == null
                        ? new EntityUsageTarget(null, "#" + entityId, null)
                        : new EntityUsageTarget(item.getCode(),
                        firstText(item.getNameZht(), item.getNameZh(), item.getNameEn(), item.getNamePt(), item.getCode()),
                        item.getStatus());
            }
            case "sub_map" -> {
                SubMap item = subMapMapper.selectById(entityId);
                yield item == null
                        ? new EntityUsageTarget(null, "#" + entityId, null)
                        : new EntityUsageTarget(item.getCode(),
                        firstText(item.getNameZht(), item.getNameZh(), item.getNameEn(), item.getNamePt(), item.getCode()),
                        item.getStatus());
            }
            case "poi" -> {
                Poi item = poiMapper.selectById(entityId);
                yield item == null
                        ? new EntityUsageTarget(null, "#" + entityId, null)
                        : new EntityUsageTarget(item.getCode(),
                        firstText(item.getNameZht(), item.getNameZh(), item.getNameEn(), item.getNamePt(), item.getCode()),
                        item.getStatus());
            }
            case "storyline" -> {
                StoryLine item = storyLineMapper.selectById(entityId);
                yield item == null
                        ? new EntityUsageTarget(null, "#" + entityId, null)
                        : new EntityUsageTarget(item.getCode(),
                        firstText(item.getNameZht(), item.getNameZh(), item.getNameEn(), item.getNamePt(), item.getCode()),
                        item.getStatus());
            }
            case "story_chapter" -> {
                StoryChapter item = storyChapterMapper.selectById(entityId);
                yield item == null
                        ? new EntityUsageTarget(null, "#" + entityId, null)
                        : new EntityUsageTarget(String.valueOf(item.getStorylineId()),
                        firstText(item.getTitleZht(), item.getTitleZh(), item.getTitleEn(), item.getTitlePt(),
                                "Chapter " + item.getChapterOrder()),
                        item.getStatus());
            }
            case "story_content_block" -> {
                StoryContentBlock item = storyContentBlockMapper.selectById(entityId);
                yield item == null
                        ? new EntityUsageTarget(null, "#" + entityId, null)
                        : new EntityUsageTarget(item.getCode(),
                        firstText(item.getTitleZht(), item.getTitleZh(), item.getTitleEn(), item.getTitlePt(), item.getCode()),
                        item.getStatus());
            }
            case "notification" -> {
                Notification item = notificationMapper.selectById(entityId);
                yield item == null
                        ? new EntityUsageTarget(null, "#" + entityId, null)
                        : new EntityUsageTarget(item.getCode(),
                        firstText(item.getTitleZht(), item.getTitleZh(), item.getTitleEn(), item.getTitlePt(), item.getCode()),
                        item.getStatus());
            }
            case "stamp" -> {
                Stamp item = stampMapper.selectById(entityId);
                yield item == null
                        ? new EntityUsageTarget(null, "#" + entityId, null)
                        : new EntityUsageTarget(item.getCode(),
                        firstText(item.getNameZht(), item.getNameZh(), item.getNameEn(), item.getNamePt(), item.getCode()),
                        item.getStatus());
            }
            case "tip_article" -> {
                TipArticle item = tipArticleMapper.selectById(entityId);
                yield item == null
                        ? new EntityUsageTarget(null, "#" + entityId, null)
                        : new EntityUsageTarget(item.getCode(),
                        firstText(item.getTitleZht(), item.getTitleZh(), item.getTitleEn(), item.getTitlePt(), item.getCode()),
                        item.getStatus());
            }
            case "reward" -> {
                Reward item = rewardMapper.selectById(entityId);
                yield item == null
                        ? new EntityUsageTarget(null, "#" + entityId, null)
                        : new EntityUsageTarget(item.getCode(),
                        firstText(item.getNameZht(), item.getNameZh(), item.getNameEn(), item.getNamePt(), item.getCode()),
                        item.getStatus());
            }
            case "runtime_setting" -> {
                AppRuntimeSetting item = runtimeSettingMapper.selectById(entityId);
                yield item == null
                        ? new EntityUsageTarget(null, "#" + entityId, null)
                        : new EntityUsageTarget(item.getSettingKey(),
                        firstText(item.getTitleZht(), item.getTitleZh(), item.getTitleEn(), item.getTitlePt(), item.getSettingKey()),
                        item.getStatus());
            }
            default -> new EntityUsageTarget(null, "#" + entityId, null);
        };
    }

    private String normalizeEntityType(String entityType) {
        if (!StringUtils.hasText(entityType)) {
            return "unknown";
        }
        return entityType.trim().replace('-', '_');
    }

    private String defaultText(String value, String defaultValue) {
        return StringUtils.hasText(value) ? value : defaultValue;
    }

    private <T> List<T> safeList(List<T> values) {
        return values == null ? List.of() : values;
    }

    private String firstText(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                return value;
            }
        }
        return null;
    }

    private AppRuntimeSetting requireRuntimeSetting(Long id) {
        AppRuntimeSetting item = runtimeSettingMapper.selectById(id);
        if (item == null) {
            throw new BusinessException(4050, "Runtime setting not found");
        }
        return item;
    }

    private ContentAsset requireAsset(Long id) {
        ContentAsset item = contentAssetMapper.selectById(id);
        if (item == null) {
            throw new BusinessException(4051, "Content asset not found");
        }
        return item;
    }

    private TipArticle requireTip(Long id) {
        TipArticle item = tipArticleMapper.selectById(id);
        if (item == null) {
            throw new BusinessException(4052, "Tip article not found");
        }
        return item;
    }

    private Notification requireNotification(Long id) {
        Notification item = notificationMapper.selectById(id);
        if (item == null) {
            throw new BusinessException(4053, "Notification not found");
        }
        return item;
    }

    private Stamp requireStamp(Long id) {
        Stamp item = stampMapper.selectById(id);
        if (item == null) {
            throw new BusinessException(4054, "Stamp not found");
        }
        return item;
    }

    private void verifyCity(Long cityId) {
        if (cityId != null && cityMapper.selectById(cityId) == null) {
            throw new BusinessException(4043, "City not found");
        }
    }

    private void verifyPoi(Long poiId) {
        if (poiId != null && poiMapper.selectById(poiId) == null) {
            throw new BusinessException(4041, "POI not found");
        }
    }

    private void verifyStoryline(Long storylineId) {
        if (storylineId != null && storyLineMapper.selectById(storylineId) == null) {
            throw new BusinessException(4042, "Storyline not found");
        }
    }

    private void applyRuntimeSettingRequest(AppRuntimeSetting item, AdminRuntimeSettingUpsertRequest request) {
        item.setSettingGroup(request.getSettingGroup());
        item.setSettingKey(request.getSettingKey());
        item.setLocaleCode(StringUtils.hasText(request.getLocaleCode()) ? request.getLocaleCode() : "zh-Hans");
        item.setTitleZh(request.getTitleZh());
        item.setTitleEn(request.getTitleEn());
        item.setTitleZht(request.getTitleZht());
        item.setTitlePt(request.getTitlePt());
        item.setValueJson(request.getValueJson());
        item.setValueText(request.getValueText());
        item.setDescriptionZh(request.getDescriptionZh());
        item.setDescriptionEn(request.getDescriptionEn());
        item.setDescriptionZht(request.getDescriptionZht());
        item.setDescriptionPt(request.getDescriptionPt());
        item.setAssetId(request.getAssetId());
        item.setStatus(StringUtils.hasText(request.getStatus()) ? request.getStatus() : "draft");
        item.setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder());
        item.setPublishedAt(parseDateTime(request.getPublishedAt()));
    }

    private void applyAssetRequest(ContentAsset item, AdminContentAssetUpsertRequest request) {
        item.setAssetKind(request.getAssetKind());
        item.setBucketName(request.getBucketName());
        item.setRegion(request.getRegion());
        item.setObjectKey(request.getObjectKey());
        item.setCanonicalUrl(request.getCanonicalUrl());
        item.setMimeType(request.getMimeType());
        item.setAnimationSubtype(request.getAnimationSubtype());
        item.setPosterAssetId(request.getPosterAssetId());
        item.setFallbackAssetId(request.getFallbackAssetId());
        item.setDefaultLoop(request.getDefaultLoop());
        item.setDefaultAutoplay(request.getDefaultAutoplay());
        item.setLocaleCode(request.getLocaleCode());
        item.setOriginalFilename(request.getOriginalFilename());
        item.setFileExtension(request.getFileExtension());
        item.setUploadSource(request.getUploadSource());
        item.setClientRelativePath(request.getClientRelativePath());
        item.setUploadedByAdminId(request.getUploadedByAdminId());
        item.setUploadedByAdminName(request.getUploadedByAdminName());
        item.setFileSizeBytes(request.getFileSizeBytes() == null ? 0L : request.getFileSizeBytes());
        item.setWidthPx(request.getWidthPx());
        item.setHeightPx(request.getHeightPx());
        item.setChecksum(request.getChecksum());
        item.setEtag(request.getEtag());
        item.setProcessingPolicyCode(request.getProcessingPolicyCode());
        item.setProcessingProfileJson(request.getProcessingProfileJson());
        item.setProcessingStatus(request.getProcessingStatus());
        item.setProcessingNote(request.getProcessingNote());
        item.setStatus(StringUtils.hasText(request.getStatus()) ? request.getStatus() : "draft");
        item.setPublishedAt(parseDateTime(request.getPublishedAt()));
    }

    private void applyTipRequest(TipArticle item, AdminTipArticleUpsertRequest request) {
        item.setCityId(request.getCityId());
        item.setCode(request.getCode());
        item.setCategoryCode(request.getCategoryCode());
        item.setTitleZh(request.getTitleZh());
        item.setTitleEn(request.getTitleEn());
        item.setTitleZht(request.getTitleZht());
        item.setTitlePt(request.getTitlePt());
        item.setSummaryZh(request.getSummaryZh());
        item.setSummaryEn(request.getSummaryEn());
        item.setSummaryZht(request.getSummaryZht());
        item.setSummaryPt(request.getSummaryPt());
        item.setContentZh(request.getContentZh());
        item.setContentEn(request.getContentEn());
        item.setContentZht(request.getContentZht());
        item.setContentPt(request.getContentPt());
        item.setAuthorDisplayName(request.getAuthorDisplayName());
        item.setLocationNameZh(request.getLocationNameZh());
        item.setLocationNameEn(request.getLocationNameEn());
        item.setLocationNameZht(request.getLocationNameZht());
        item.setLocationNamePt(request.getLocationNamePt());
        item.setTagsJson(request.getTagsJson());
        item.setCoverAssetId(request.getCoverAssetId());
        item.setSourceType(StringUtils.hasText(request.getSourceType()) ? request.getSourceType() : "editorial");
        item.setStatus(StringUtils.hasText(request.getStatus()) ? request.getStatus() : "draft");
        item.setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder());
        item.setPublishedAt(parseDateTime(request.getPublishedAt()));
    }

    private void applyNotificationRequest(Notification item, AdminNotificationUpsertRequest request) {
        item.setCode(request.getCode());
        item.setTitleZh(request.getTitleZh());
        item.setTitleEn(request.getTitleEn());
        item.setTitleZht(request.getTitleZht());
        item.setTitlePt(request.getTitlePt());
        item.setContentZh(request.getContentZh());
        item.setContentEn(request.getContentEn());
        item.setContentZht(request.getContentZht());
        item.setContentPt(request.getContentPt());
        item.setNotificationType(StringUtils.hasText(request.getNotificationType()) ? request.getNotificationType() : "system");
        item.setTargetScope(StringUtils.hasText(request.getTargetScope()) ? request.getTargetScope() : "all");
        item.setCoverAssetId(request.getCoverAssetId());
        item.setActionUrl(request.getActionUrl());
        item.setStatus(StringUtils.hasText(request.getStatus()) ? request.getStatus() : "draft");
        item.setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder());
        item.setPublishStartAt(parseDateTime(request.getPublishStartAt()));
        item.setPublishEndAt(parseDateTime(request.getPublishEndAt()));
    }

    private void applyStampRequest(Stamp item, AdminStampUpsertRequest request) {
        item.setCode(request.getCode());
        item.setNameZh(request.getNameZh());
        item.setNameEn(request.getNameEn());
        item.setNameZht(request.getNameZht());
        item.setNamePt(request.getNamePt());
        item.setDescriptionZh(request.getDescriptionZh());
        item.setDescriptionEn(request.getDescriptionEn());
        item.setDescriptionZht(request.getDescriptionZht());
        item.setDescriptionPt(request.getDescriptionPt());
        item.setStampType(StringUtils.hasText(request.getStampType()) ? request.getStampType() : "location");
        item.setRarity(StringUtils.hasText(request.getRarity()) ? request.getRarity() : "common");
        item.setIconAssetId(request.getIconAssetId());
        item.setRelatedPoiId(request.getRelatedPoiId());
        item.setRelatedStorylineId(request.getRelatedStorylineId());
        item.setStatus(StringUtils.hasText(request.getStatus()) ? request.getStatus() : "draft");
        item.setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder());
        item.setPublishedAt(parseDateTime(request.getPublishedAt()));
    }

    private AdminRuntimeSettingResponse toRuntimeSettingResponse(AppRuntimeSetting item) {
        return AdminRuntimeSettingResponse.builder()
                .id(item.getId())
                .settingGroup(item.getSettingGroup())
                .settingKey(item.getSettingKey())
                .localeCode(item.getLocaleCode())
                .titleZh(item.getTitleZh())
                .titleEn(item.getTitleEn())
                .titleZht(item.getTitleZht())
                .titlePt(item.getTitlePt())
                .valueJson(item.getValueJson())
                .valueText(item.getValueText())
                .descriptionZh(item.getDescriptionZh())
                .descriptionEn(item.getDescriptionEn())
                .descriptionZht(item.getDescriptionZht())
                .descriptionPt(item.getDescriptionPt())
                .assetId(item.getAssetId())
                .status(item.getStatus())
                .sortOrder(item.getSortOrder())
                .publishedAt(item.getPublishedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }

    private AdminContentAssetResponse toContentAssetResponse(ContentAsset item) {
        return AdminContentAssetResponse.builder()
                .id(item.getId())
                .assetKind(item.getAssetKind())
                .bucketName(item.getBucketName())
                .region(item.getRegion())
                .objectKey(item.getObjectKey())
                .canonicalUrl(item.getCanonicalUrl())
                .mimeType(item.getMimeType())
                .animationSubtype(item.getAnimationSubtype())
                .posterAssetId(item.getPosterAssetId())
                .fallbackAssetId(item.getFallbackAssetId())
                .defaultLoop(item.getDefaultLoop())
                .defaultAutoplay(item.getDefaultAutoplay())
                .localeCode(item.getLocaleCode())
                .originalFilename(item.getOriginalFilename())
                .fileExtension(item.getFileExtension())
                .uploadSource(item.getUploadSource())
                .clientRelativePath(item.getClientRelativePath())
                .uploadedByAdminId(item.getUploadedByAdminId())
                .uploadedByAdminName(item.getUploadedByAdminName())
                .fileSizeBytes(item.getFileSizeBytes())
                .widthPx(item.getWidthPx())
                .heightPx(item.getHeightPx())
                .checksum(item.getChecksum())
                .etag(item.getEtag())
                .processingPolicyCode(item.getProcessingPolicyCode())
                .processingProfileJson(item.getProcessingProfileJson())
                .processingStatus(item.getProcessingStatus())
                .processingNote(item.getProcessingNote())
                .status(item.getStatus())
                .publishedAt(item.getPublishedAt())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }

    private AdminTipArticleResponse toTipArticleResponse(TipArticle item) {
        City city = item.getCityId() == null ? null : cityMapper.selectById(item.getCityId());
        return AdminTipArticleResponse.builder()
                .id(item.getId())
                .cityId(item.getCityId())
                .cityName(city == null ? null : city.getNameZh())
                .code(item.getCode())
                .categoryCode(item.getCategoryCode())
                .titleZh(item.getTitleZh())
                .titleEn(item.getTitleEn())
                .titleZht(item.getTitleZht())
                .titlePt(item.getTitlePt())
                .summaryZh(item.getSummaryZh())
                .summaryEn(item.getSummaryEn())
                .summaryZht(item.getSummaryZht())
                .summaryPt(item.getSummaryPt())
                .contentZh(item.getContentZh())
                .contentEn(item.getContentEn())
                .contentZht(item.getContentZht())
                .contentPt(item.getContentPt())
                .authorDisplayName(item.getAuthorDisplayName())
                .locationNameZh(item.getLocationNameZh())
                .locationNameEn(item.getLocationNameEn())
                .locationNameZht(item.getLocationNameZht())
                .locationNamePt(item.getLocationNamePt())
                .tagsJson(item.getTagsJson())
                .coverAssetId(item.getCoverAssetId())
                .sourceType(item.getSourceType())
                .status(item.getStatus())
                .sortOrder(item.getSortOrder())
                .publishedAt(item.getPublishedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }

    private AdminNotificationResponse toNotificationResponse(Notification item) {
        return AdminNotificationResponse.builder()
                .id(item.getId())
                .code(item.getCode())
                .titleZh(item.getTitleZh())
                .titleEn(item.getTitleEn())
                .titleZht(item.getTitleZht())
                .titlePt(item.getTitlePt())
                .contentZh(item.getContentZh())
                .contentEn(item.getContentEn())
                .contentZht(item.getContentZht())
                .contentPt(item.getContentPt())
                .notificationType(item.getNotificationType())
                .targetScope(item.getTargetScope())
                .coverAssetId(item.getCoverAssetId())
                .actionUrl(item.getActionUrl())
                .status(item.getStatus())
                .sortOrder(item.getSortOrder())
                .publishStartAt(item.getPublishStartAt())
                .publishEndAt(item.getPublishEndAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }

    private AdminStampResponse toStampResponse(Stamp item) {
        Poi poi = item.getRelatedPoiId() == null ? null : poiMapper.selectById(item.getRelatedPoiId());
        StoryLine storyline = item.getRelatedStorylineId() == null ? null : storyLineMapper.selectById(item.getRelatedStorylineId());
        return AdminStampResponse.builder()
                .id(item.getId())
                .code(item.getCode())
                .nameZh(item.getNameZh())
                .nameEn(item.getNameEn())
                .nameZht(item.getNameZht())
                .namePt(item.getNamePt())
                .descriptionZh(item.getDescriptionZh())
                .descriptionEn(item.getDescriptionEn())
                .descriptionZht(item.getDescriptionZht())
                .descriptionPt(item.getDescriptionPt())
                .stampType(item.getStampType())
                .rarity(item.getRarity())
                .iconAssetId(item.getIconAssetId())
                .relatedPoiId(item.getRelatedPoiId())
                .relatedPoiName(poi == null ? null : poi.getNameZh())
                .relatedStorylineId(item.getRelatedStorylineId())
                .relatedStorylineName(storyline == null ? null : storyline.getNameZh())
                .status(item.getStatus())
                .sortOrder(item.getSortOrder())
                .publishedAt(item.getPublishedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }

    private LocalDateTime parseDateTime(String value) {
        return StringUtils.hasText(value) ? LocalDateTime.parse(value) : null;
    }

    private record EntityUsageTarget(String code, String name, String status) {}
}
