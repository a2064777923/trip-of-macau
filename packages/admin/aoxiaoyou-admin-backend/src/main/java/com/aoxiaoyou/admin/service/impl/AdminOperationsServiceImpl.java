package com.aoxiaoyou.admin.service.impl;

import com.aoxiaoyou.admin.common.api.PageResponse;
import com.aoxiaoyou.admin.common.exception.BusinessException;
import com.aoxiaoyou.admin.dto.request.AdminActivityUpsertRequest;
import com.aoxiaoyou.admin.dto.response.AdminActivityResponse;
import com.aoxiaoyou.admin.entity.Activity;
import com.aoxiaoyou.admin.mapper.ActivityMapper;
import com.aoxiaoyou.admin.mapper.CityMapper;
import com.aoxiaoyou.admin.mapper.StoryLineMapper;
import com.aoxiaoyou.admin.mapper.SubMapMapper;
import com.aoxiaoyou.admin.service.AdminContentRelationService;
import com.aoxiaoyou.admin.service.AdminOperationsService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AdminOperationsServiceImpl implements AdminOperationsService {

    private final ActivityMapper activityMapper;
    private final CityMapper cityMapper;
    private final SubMapMapper subMapMapper;
    private final StoryLineMapper storyLineMapper;
    private final AdminContentRelationService adminContentRelationService;

    @Override
    public PageResponse<AdminActivityResponse> pageActivities(long pageNum, long pageSize, String keyword, String status, String activityType) {
        Page<Activity> page = activityMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<Activity>()
                        .and(StringUtils.hasText(keyword), wrapper -> wrapper
                                .like(Activity::getCode, keyword)
                                .or().like(Activity::getTitleZh, keyword)
                                .or().like(Activity::getTitleZht, keyword)
                                .or().like(Activity::getTitleEn, keyword)
                                .or().like(Activity::getVenueNameZh, keyword)
                                .or().like(Activity::getOrganizerName, keyword))
                        .eq(StringUtils.hasText(status), Activity::getStatus, status)
                        .eq(StringUtils.hasText(activityType), Activity::getActivityType, activityType)
                        .orderByDesc(Activity::getIsPinned)
                        .orderByAsc(Activity::getSortOrder)
                        .orderByDesc(Activity::getPublishStartAt)
                        .orderByDesc(Activity::getId));

        Page<AdminActivityResponse> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(page.getRecords().stream().map(this::toResponse).toList());
        return PageResponse.of(result);
    }

    @Override
    public AdminActivityResponse getActivity(Long activityId) {
        return toResponse(requireActivity(activityId));
    }

    @Override
    public AdminActivityResponse createActivity(AdminActivityUpsertRequest.Upsert request) {
        Activity activity = new Activity();
        applyRequest(activity, request);
        activityMapper.insert(activity);
        syncRelations(activity.getId(), request);
        return toResponse(requireActivity(activity.getId()));
    }

    @Override
    public AdminActivityResponse updateActivity(Long activityId, AdminActivityUpsertRequest.Upsert request) {
        Activity activity = requireActivity(activityId);
        applyRequest(activity, request);
        activityMapper.updateById(activity);
        syncRelations(activityId, request);
        return toResponse(requireActivity(activityId));
    }

    @Override
    public void deleteActivity(Long activityId) {
        requireActivity(activityId);
        activityMapper.deleteById(activityId);
    }

    private Activity requireActivity(Long activityId) {
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw new BusinessException(4046, "Activity not found");
        }
        return activity;
    }

    private void applyRequest(Activity activity, AdminActivityUpsertRequest.Upsert request) {
        LocalDateTime signupStartAt = parseDateTime(request.getSignupStartAt());
        LocalDateTime signupEndAt = parseDateTime(request.getSignupEndAt());
        LocalDateTime publishStartAt = parseDateTime(request.getPublishStartAt());
        LocalDateTime publishEndAt = parseDateTime(request.getPublishEndAt());
        validateWindow(signupStartAt, signupEndAt, "signup window");
        validateWindow(publishStartAt, publishEndAt, "publish window");

        String titleZh = requireText(request.getTitleZh(), "titleZh is required");
        String titleZht = firstText(request.getTitleZht(), titleZh);
        String summaryZh = firstText(request.getSummaryZh(), request.getDescriptionZh(), request.getDescriptionZht());
        String summaryZht = firstText(request.getSummaryZht(), request.getSummaryZh(), request.getDescriptionZht(), request.getDescriptionZh());
        String descriptionZh = firstText(request.getDescriptionZh(), request.getSummaryZh());
        String descriptionZht = firstText(request.getDescriptionZht(), request.getDescriptionZh(), request.getSummaryZht(), request.getSummaryZh());

        verifyIds("city", request.getCityBindings());
        verifyIds("sub_map", request.getSubMapBindings());
        verifyIds("storyline", request.getStorylineBindings());

        activity.setCode(requireText(request.getCode(), "code is required"));
        activity.setActivityType(StringUtils.hasText(request.getActivityType()) ? request.getActivityType().trim() : "official_event");
        activity.setTitle(firstText(request.getTitleZht(), request.getTitleZh(), request.getTitleEn(), request.getTitlePt()));
        activity.setDescription(firstText(request.getSummaryZht(), request.getSummaryZh(), request.getDescriptionZht(), request.getDescriptionZh()));
        activity.setTitleZh(titleZh);
        activity.setTitleEn(trimToNull(request.getTitleEn()));
        activity.setTitleZht(titleZht);
        activity.setTitlePt(trimToNull(request.getTitlePt()));
        activity.setSummaryZh(summaryZh);
        activity.setSummaryEn(trimToNull(request.getSummaryEn()));
        activity.setSummaryZht(summaryZht);
        activity.setSummaryPt(trimToNull(request.getSummaryPt()));
        activity.setDescriptionZh(descriptionZh);
        activity.setDescriptionEn(trimToNull(request.getDescriptionEn()));
        activity.setDescriptionZht(descriptionZht);
        activity.setDescriptionPt(trimToNull(request.getDescriptionPt()));
        activity.setHtmlZh(trimToNull(request.getHtmlZh()));
        activity.setHtmlEn(trimToNull(request.getHtmlEn()));
        activity.setHtmlZht(trimToNull(request.getHtmlZht()));
        activity.setHtmlPt(trimToNull(request.getHtmlPt()));
        activity.setVenueNameZh(trimToNull(request.getVenueNameZh()));
        activity.setVenueNameEn(trimToNull(request.getVenueNameEn()));
        activity.setVenueNameZht(trimToNull(request.getVenueNameZht()));
        activity.setVenueNamePt(trimToNull(request.getVenueNamePt()));
        activity.setAddressZh(trimToNull(request.getAddressZh()));
        activity.setAddressEn(trimToNull(request.getAddressEn()));
        activity.setAddressZht(trimToNull(request.getAddressZht()));
        activity.setAddressPt(trimToNull(request.getAddressPt()));
        activity.setOrganizerName(trimToNull(request.getOrganizerName()));
        activity.setOrganizerContact(trimToNull(request.getOrganizerContact()));
        activity.setOrganizerWebsite(trimToNull(request.getOrganizerWebsite()));
        activity.setSignupCapacity(request.getSignupCapacity());
        activity.setSignupFeeAmount(request.getSignupFeeAmount());
        activity.setSignupStartAt(signupStartAt);
        activity.setSignupEndAt(signupEndAt);
        activity.setPublishStartAt(publishStartAt);
        activity.setPublishEndAt(publishEndAt);
        activity.setStartTime(publishStartAt);
        activity.setEndTime(publishEndAt);
        activity.setIsPinned(request.getIsPinned() == null ? 0 : (request.getIsPinned() > 0 ? 1 : 0));
        activity.setCoverAssetId(request.getCoverAssetId());
        activity.setHeroAssetId(request.getHeroAssetId());
        activity.setParticipationCount(request.getParticipationCount() == null ? 0 : Math.max(request.getParticipationCount(), 0));
        activity.setStatus(StringUtils.hasText(request.getStatus()) ? request.getStatus().trim() : "draft");
        activity.setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder());
    }

    private void syncRelations(Long activityId, AdminActivityUpsertRequest.Upsert request) {
        adminContentRelationService.syncTargetIds("activity", activityId, "city_binding", "city", normalizeIds(request.getCityBindings()));
        adminContentRelationService.syncTargetIds("activity", activityId, "sub_map_binding", "sub_map", normalizeIds(request.getSubMapBindings()));
        adminContentRelationService.syncTargetIds("activity", activityId, "storyline_binding", "storyline", normalizeIds(request.getStorylineBindings()));
        adminContentRelationService.syncTargetIds("activity", activityId, "attachment_asset", "asset", normalizeIds(request.getAttachmentAssetIds()));
    }

    private AdminActivityResponse toResponse(Activity item) {
        return AdminActivityResponse.builder()
                .id(item.getId())
                .code(item.getCode())
                .activityType(item.getActivityType())
                .title(firstText(item.getTitleZht(), item.getTitleZh(), item.getTitleEn(), item.getTitlePt(), item.getTitle()))
                .description(firstText(item.getSummaryZht(), item.getSummaryZh(), item.getDescriptionZht(), item.getDescriptionZh(), item.getDescription()))
                .titleZh(item.getTitleZh())
                .titleEn(item.getTitleEn())
                .titleZht(item.getTitleZht())
                .titlePt(item.getTitlePt())
                .summaryZh(item.getSummaryZh())
                .summaryEn(item.getSummaryEn())
                .summaryZht(item.getSummaryZht())
                .summaryPt(item.getSummaryPt())
                .descriptionZh(item.getDescriptionZh())
                .descriptionEn(item.getDescriptionEn())
                .descriptionZht(item.getDescriptionZht())
                .descriptionPt(item.getDescriptionPt())
                .htmlZh(item.getHtmlZh())
                .htmlEn(item.getHtmlEn())
                .htmlZht(item.getHtmlZht())
                .htmlPt(item.getHtmlPt())
                .venueNameZh(item.getVenueNameZh())
                .venueNameEn(item.getVenueNameEn())
                .venueNameZht(item.getVenueNameZht())
                .venueNamePt(item.getVenueNamePt())
                .addressZh(item.getAddressZh())
                .addressEn(item.getAddressEn())
                .addressZht(item.getAddressZht())
                .addressPt(item.getAddressPt())
                .organizerName(item.getOrganizerName())
                .organizerContact(item.getOrganizerContact())
                .organizerWebsite(item.getOrganizerWebsite())
                .signupCapacity(item.getSignupCapacity())
                .signupFeeAmount(item.getSignupFeeAmount())
                .signupStartAt(item.getSignupStartAt())
                .signupEndAt(item.getSignupEndAt())
                .publishStartAt(item.getPublishStartAt())
                .publishEndAt(item.getPublishEndAt())
                .isPinned(item.getIsPinned())
                .coverAssetId(item.getCoverAssetId())
                .heroAssetId(item.getHeroAssetId())
                .participationCount(item.getParticipationCount())
                .status(item.getStatus())
                .sortOrder(item.getSortOrder())
                .cityBindings(adminContentRelationService.listTargetIds("activity", item.getId(), "city_binding", "city"))
                .subMapBindings(adminContentRelationService.listTargetIds("activity", item.getId(), "sub_map_binding", "sub_map"))
                .storylineBindings(adminContentRelationService.listTargetIds("activity", item.getId(), "storyline_binding", "storyline"))
                .attachmentAssetIds(adminContentRelationService.listTargetIds("activity", item.getId(), "attachment_asset", "asset"))
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }

    private void verifyIds(String targetType, List<Long> ids) {
        for (Long id : normalizeIds(ids)) {
            boolean exists = switch (targetType) {
                case "city" -> cityMapper.selectById(id) != null;
                case "sub_map" -> subMapMapper.selectById(id) != null;
                case "storyline" -> storyLineMapper.selectById(id) != null;
                default -> true;
            };
            if (!exists) {
                throw new BusinessException(4002, targetType + " relation target not found");
            }
        }
    }

    private List<Long> normalizeIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        return ids.stream().filter(Objects::nonNull).distinct().toList();
    }

    private void validateWindow(LocalDateTime startAt, LocalDateTime endAt, String label) {
        if (startAt != null && endAt != null && startAt.isAfter(endAt)) {
            throw new BusinessException(4002, label + " start must be before end");
        }
    }

    private LocalDateTime parseDateTime(String value) {
        return StringUtils.hasText(value) ? LocalDateTime.parse(value) : null;
    }

    private String requireText(String value, String message) {
        if (!StringUtils.hasText(value)) {
            throw new BusinessException(4001, message);
        }
        return value.trim();
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String firstText(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                return value.trim();
            }
        }
        return null;
    }
}
