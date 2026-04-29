package com.aoxiaoyou.admin.service.impl;

import com.aoxiaoyou.admin.common.api.PageResponse;
import com.aoxiaoyou.admin.common.exception.BusinessException;
import com.aoxiaoyou.admin.dto.request.AdminGameRewardUpsertRequest;
import com.aoxiaoyou.admin.dto.request.AdminRedeemablePrizeUpsertRequest;
import com.aoxiaoyou.admin.dto.request.AdminRewardPresentationUpsertRequest;
import com.aoxiaoyou.admin.dto.request.AdminRewardRuleUpsertRequest;
import com.aoxiaoyou.admin.dto.response.AdminGameRewardResponse;
import com.aoxiaoyou.admin.dto.response.AdminRedeemablePrizeResponse;
import com.aoxiaoyou.admin.dto.response.AdminRewardGovernanceResponse;
import com.aoxiaoyou.admin.dto.response.AdminRewardLinkedEntityResponse;
import com.aoxiaoyou.admin.dto.response.AdminRewardPresentationResponse;
import com.aoxiaoyou.admin.dto.response.AdminRewardPresentationSummaryResponse;
import com.aoxiaoyou.admin.dto.response.AdminRewardRuleLinkResponse;
import com.aoxiaoyou.admin.dto.response.AdminRewardRuleResponse;
import com.aoxiaoyou.admin.entity.Building;
import com.aoxiaoyou.admin.entity.City;
import com.aoxiaoyou.admin.entity.ContentAsset;
import com.aoxiaoyou.admin.entity.GameReward;
import com.aoxiaoyou.admin.entity.IndoorFloor;
import com.aoxiaoyou.admin.entity.IndoorNodeBehavior;
import com.aoxiaoyou.admin.entity.RedeemablePrize;
import com.aoxiaoyou.admin.entity.RewardCondition;
import com.aoxiaoyou.admin.entity.RewardConditionGroup;
import com.aoxiaoyou.admin.entity.RewardPresentation;
import com.aoxiaoyou.admin.entity.RewardPresentationStep;
import com.aoxiaoyou.admin.entity.RewardRule;
import com.aoxiaoyou.admin.entity.RewardRuleBinding;
import com.aoxiaoyou.admin.entity.StoryLine;
import com.aoxiaoyou.admin.entity.SubMap;
import com.aoxiaoyou.admin.mapper.BuildingMapper;
import com.aoxiaoyou.admin.mapper.CityMapper;
import com.aoxiaoyou.admin.mapper.ContentAssetMapper;
import com.aoxiaoyou.admin.mapper.GameRewardMapper;
import com.aoxiaoyou.admin.mapper.IndoorFloorMapper;
import com.aoxiaoyou.admin.mapper.IndoorNodeBehaviorMapper;
import com.aoxiaoyou.admin.mapper.RedeemablePrizeMapper;
import com.aoxiaoyou.admin.mapper.RewardConditionGroupMapper;
import com.aoxiaoyou.admin.mapper.RewardConditionMapper;
import com.aoxiaoyou.admin.mapper.RewardPresentationMapper;
import com.aoxiaoyou.admin.mapper.RewardPresentationStepMapper;
import com.aoxiaoyou.admin.mapper.RewardRuleBindingMapper;
import com.aoxiaoyou.admin.mapper.RewardRuleMapper;
import com.aoxiaoyou.admin.mapper.StoryLineMapper;
import com.aoxiaoyou.admin.mapper.SubMapMapper;
import com.aoxiaoyou.admin.service.AdminContentRelationService;
import com.aoxiaoyou.admin.service.AdminRewardDomainService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminRewardDomainServiceImpl implements AdminRewardDomainService {

    private static final String OWNER_TYPE_REDEEMABLE_PRIZE = "redeemable_prize";
    private static final String OWNER_TYPE_GAME_REWARD = "game_reward";
    private static final String OWNER_TYPE_INDOOR_BEHAVIOR = "indoor_behavior";
    private static final String RELATION_STORYLINE = "storyline_binding";
    private static final String RELATION_CITY = "city_binding";
    private static final String RELATION_SUB_MAP = "sub_map_binding";
    private static final String RELATION_INDOOR_BUILDING = "indoor_building_binding";
    private static final String RELATION_INDOOR_FLOOR = "indoor_floor_binding";
    private static final String RELATION_ATTACHMENT = "attachment_asset";
    private static final Set<String> HONOR_REWARD_TYPES = Set.of("badge", "title");

    private final RedeemablePrizeMapper redeemablePrizeMapper;
    private final GameRewardMapper gameRewardMapper;
    private final RewardRuleMapper rewardRuleMapper;
    private final RewardConditionGroupMapper rewardConditionGroupMapper;
    private final RewardConditionMapper rewardConditionMapper;
    private final RewardRuleBindingMapper rewardRuleBindingMapper;
    private final RewardPresentationMapper rewardPresentationMapper;
    private final RewardPresentationStepMapper rewardPresentationStepMapper;
    private final StoryLineMapper storyLineMapper;
    private final CityMapper cityMapper;
    private final SubMapMapper subMapMapper;
    private final BuildingMapper buildingMapper;
    private final IndoorFloorMapper indoorFloorMapper;
    private final ContentAssetMapper contentAssetMapper;
    private final IndoorNodeBehaviorMapper indoorNodeBehaviorMapper;
    private final AdminContentRelationService adminContentRelationService;

    @Override
    public PageResponse<AdminRedeemablePrizeResponse> pageRedeemablePrizes(
            long pageNum,
            long pageSize,
            String keyword,
            String status,
            String prizeType,
            String fulfillmentMode
    ) {
        Page<RedeemablePrize> page = redeemablePrizeMapper.selectPage(
                new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<RedeemablePrize>()
                        .eq(StringUtils.hasText(status), RedeemablePrize::getStatus, status)
                        .eq(StringUtils.hasText(prizeType), RedeemablePrize::getPrizeType, prizeType)
                        .eq(StringUtils.hasText(fulfillmentMode), RedeemablePrize::getFulfillmentMode, fulfillmentMode)
                        .and(StringUtils.hasText(keyword), wrapper -> wrapper
                                .like(RedeemablePrize::getCode, keyword)
                                .or().like(RedeemablePrize::getNameZh, keyword)
                                .or().like(RedeemablePrize::getNameZht, keyword))
                        .orderByAsc(RedeemablePrize::getSortOrder)
                        .orderByAsc(RedeemablePrize::getId)
        );
        Page<AdminRedeemablePrizeResponse> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(page.getRecords().stream().map(this::mapRedeemablePrize).toList());
        return PageResponse.of(result);
    }

    @Override
    public AdminRedeemablePrizeResponse getRedeemablePrize(Long prizeId) {
        return mapRedeemablePrize(requireRedeemablePrize(prizeId));
    }

    @Override
    public AdminRedeemablePrizeResponse createRedeemablePrize(AdminRedeemablePrizeUpsertRequest request) {
        validatePrizeRequest(request);
        RedeemablePrize prize = new RedeemablePrize();
        applyPrizeRequest(prize, request);
        redeemablePrizeMapper.insert(prize);
        syncScopeBindings(OWNER_TYPE_REDEEMABLE_PRIZE, prize.getId(), request.getStorylineBindings(), request.getCityBindings(),
                request.getSubMapBindings(), request.getIndoorBuildingBindings(), request.getIndoorFloorBindings(), request.getAttachmentAssetIds());
        syncRuleBindings(OWNER_TYPE_REDEEMABLE_PRIZE, prize.getId(), prize.getCode(), request.getRuleIds());
        return mapRedeemablePrize(requireRedeemablePrize(prize.getId()));
    }

    @Override
    public AdminRedeemablePrizeResponse updateRedeemablePrize(Long prizeId, AdminRedeemablePrizeUpsertRequest request) {
        RedeemablePrize prize = requireRedeemablePrize(prizeId);
        validatePrizeRequest(request);
        applyPrizeRequest(prize, request);
        redeemablePrizeMapper.updateById(prize);
        syncScopeBindings(OWNER_TYPE_REDEEMABLE_PRIZE, prizeId, request.getStorylineBindings(), request.getCityBindings(),
                request.getSubMapBindings(), request.getIndoorBuildingBindings(), request.getIndoorFloorBindings(), request.getAttachmentAssetIds());
        syncRuleBindings(OWNER_TYPE_REDEEMABLE_PRIZE, prizeId, prize.getCode(), request.getRuleIds());
        return mapRedeemablePrize(requireRedeemablePrize(prizeId));
    }

    @Override
    public void deleteRedeemablePrize(Long prizeId) {
        requireRedeemablePrize(prizeId);
        redeemablePrizeMapper.deleteById(prizeId);
        clearScopeBindings(OWNER_TYPE_REDEEMABLE_PRIZE, prizeId);
        clearRuleBindingsByOwner(OWNER_TYPE_REDEEMABLE_PRIZE, prizeId);
    }

    @Override
    public PageResponse<AdminGameRewardResponse> pageGameRewards(
            long pageNum,
            long pageSize,
            String keyword,
            String status,
            String rewardType,
            Boolean honorsOnly
    ) {
        LambdaQueryWrapper<GameReward> wrapper = new LambdaQueryWrapper<GameReward>()
                .eq(StringUtils.hasText(status), GameReward::getStatus, status)
                .eq(StringUtils.hasText(rewardType), GameReward::getRewardType, rewardType)
                .in(Boolean.TRUE.equals(honorsOnly), GameReward::getRewardType, HONOR_REWARD_TYPES)
                .and(StringUtils.hasText(keyword), query -> query
                        .like(GameReward::getCode, keyword)
                        .or().like(GameReward::getNameZh, keyword)
                        .or().like(GameReward::getNameZht, keyword))
                .orderByAsc(GameReward::getSortOrder)
                .orderByAsc(GameReward::getId);
        Page<GameReward> page = gameRewardMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        Page<AdminGameRewardResponse> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(page.getRecords().stream().map(this::mapGameReward).toList());
        return PageResponse.of(result);
    }

    @Override
    public AdminGameRewardResponse getGameReward(Long rewardId) {
        return mapGameReward(requireGameReward(rewardId));
    }

    @Override
    public AdminGameRewardResponse createGameReward(AdminGameRewardUpsertRequest request) {
        validateGameRewardRequest(request);
        GameReward reward = new GameReward();
        applyGameRewardRequest(reward, request);
        gameRewardMapper.insert(reward);
        syncScopeBindings(OWNER_TYPE_GAME_REWARD, reward.getId(), request.getStorylineBindings(), request.getCityBindings(),
                request.getSubMapBindings(), request.getIndoorBuildingBindings(), request.getIndoorFloorBindings(), request.getAttachmentAssetIds());
        syncRuleBindings(OWNER_TYPE_GAME_REWARD, reward.getId(), reward.getCode(), request.getRuleIds());
        return mapGameReward(requireGameReward(reward.getId()));
    }

    @Override
    public AdminGameRewardResponse updateGameReward(Long rewardId, AdminGameRewardUpsertRequest request) {
        GameReward reward = requireGameReward(rewardId);
        validateGameRewardRequest(request);
        applyGameRewardRequest(reward, request);
        gameRewardMapper.updateById(reward);
        syncScopeBindings(OWNER_TYPE_GAME_REWARD, rewardId, request.getStorylineBindings(), request.getCityBindings(),
                request.getSubMapBindings(), request.getIndoorBuildingBindings(), request.getIndoorFloorBindings(), request.getAttachmentAssetIds());
        syncRuleBindings(OWNER_TYPE_GAME_REWARD, rewardId, reward.getCode(), request.getRuleIds());
        return mapGameReward(requireGameReward(rewardId));
    }

    @Override
    public void deleteGameReward(Long rewardId) {
        requireGameReward(rewardId);
        gameRewardMapper.deleteById(rewardId);
        clearScopeBindings(OWNER_TYPE_GAME_REWARD, rewardId);
        clearRuleBindingsByOwner(OWNER_TYPE_GAME_REWARD, rewardId);
    }

    @Override
    public PageResponse<AdminRewardRuleResponse> pageRewardRules(long pageNum, long pageSize, String keyword, String status, String ruleType) {
        Page<RewardRule> page = rewardRuleMapper.selectPage(
                new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<RewardRule>()
                        .eq(StringUtils.hasText(status), RewardRule::getStatus, status)
                        .eq(StringUtils.hasText(ruleType), RewardRule::getRuleType, ruleType)
                        .and(StringUtils.hasText(keyword), wrapper -> wrapper
                                .like(RewardRule::getCode, keyword)
                                .or().like(RewardRule::getNameZh, keyword)
                                .or().like(RewardRule::getNameZht, keyword)
                                .or().like(RewardRule::getSummaryText, keyword))
                        .orderByDesc(RewardRule::getCreatedAt)
                        .orderByAsc(RewardRule::getId)
        );
        Page<AdminRewardRuleResponse> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(page.getRecords().stream().map(this::mapRewardRule).toList());
        return PageResponse.of(result);
    }

    @Override
    public AdminRewardRuleResponse getRewardRule(Long ruleId) {
        return mapRewardRule(requireRewardRule(ruleId));
    }

    @Override
    public AdminRewardRuleResponse createRewardRule(AdminRewardRuleUpsertRequest request) {
        validateRuleRequest(request);
        RewardRule rule = new RewardRule();
        applyRewardRuleRequest(rule, request);
        rewardRuleMapper.insert(rule);
        syncConditionGroups(rule, request);
        return mapRewardRule(requireRewardRule(rule.getId()));
    }

    @Override
    public AdminRewardRuleResponse updateRewardRule(Long ruleId, AdminRewardRuleUpsertRequest request) {
        RewardRule existing = requireRewardRule(ruleId);
        validateRuleRequest(request);
        RewardRule probe = new RewardRule();
        applyRewardRuleRequest(probe, request);
        String nextStatus = normalizeStatus(probe.getStatus(), existing.getStatus());
        if ("disabled".equalsIgnoreCase(nextStatus) && countRuleBindings(ruleId) > 0) {
            throw new BusinessException(4091, buildRuleBindingMessage(ruleId, "停用"));
        }
        applyRewardRuleRequest(existing, request);
        rewardRuleMapper.updateById(existing);
        syncConditionGroups(existing, request);
        return mapRewardRule(requireRewardRule(ruleId));
    }

    @Override
    public void deleteRewardRule(Long ruleId) {
        requireRewardRule(ruleId);
        if (countRuleBindings(ruleId) > 0) {
            throw new BusinessException(4091, buildRuleBindingMessage(ruleId, "刪除"));
        }
        deleteConditionTree(ruleId);
        rewardRuleMapper.deleteById(ruleId);
    }

    @Override
    public PageResponse<AdminRewardPresentationResponse> pageRewardPresentations(
            long pageNum,
            long pageSize,
            String keyword,
            String status,
            String presentationType
    ) {
        Page<RewardPresentation> page = rewardPresentationMapper.selectPage(
                new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<RewardPresentation>()
                        .eq(StringUtils.hasText(status), RewardPresentation::getStatus, status)
                        .eq(StringUtils.hasText(presentationType), RewardPresentation::getPresentationType, presentationType)
                        .and(StringUtils.hasText(keyword), wrapper -> wrapper
                                .like(RewardPresentation::getCode, keyword)
                                .or().like(RewardPresentation::getNameZh, keyword)
                                .or().like(RewardPresentation::getNameZht, keyword)
                                .or().like(RewardPresentation::getSummaryText, keyword))
                        .orderByDesc(RewardPresentation::getCreatedAt)
                        .orderByAsc(RewardPresentation::getId)
        );
        Page<AdminRewardPresentationResponse> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(page.getRecords().stream().map(this::mapRewardPresentation).toList());
        return PageResponse.of(result);
    }

    @Override
    public AdminRewardPresentationResponse getRewardPresentation(Long presentationId) {
        return mapRewardPresentation(requireRewardPresentation(presentationId));
    }

    @Override
    public AdminRewardPresentationResponse createRewardPresentation(AdminRewardPresentationUpsertRequest request) {
        validatePresentationRequest(request);
        RewardPresentation presentation = new RewardPresentation();
        applyRewardPresentationRequest(presentation, request);
        rewardPresentationMapper.insert(presentation);
        syncPresentationSteps(presentation.getId(), request.getSteps());
        return mapRewardPresentation(requireRewardPresentation(presentation.getId()));
    }

    @Override
    public AdminRewardPresentationResponse updateRewardPresentation(Long presentationId, AdminRewardPresentationUpsertRequest request) {
        RewardPresentation presentation = requireRewardPresentation(presentationId);
        validatePresentationRequest(request);
        applyRewardPresentationRequest(presentation, request);
        rewardPresentationMapper.updateById(presentation);
        syncPresentationSteps(presentationId, request.getSteps());
        return mapRewardPresentation(requireRewardPresentation(presentationId));
    }

    @Override
    public void deleteRewardPresentation(Long presentationId) {
        requireRewardPresentation(presentationId);
        if (countPresentationLinks(presentationId) > 0) {
            throw new BusinessException(4092, "目前仍有獎勵主體使用此演出配置，請先解除綁定後再刪除。");
        }
        rewardPresentationStepMapper.delete(new LambdaQueryWrapper<RewardPresentationStep>()
                .eq(RewardPresentationStep::getPresentationId, presentationId));
        rewardPresentationMapper.deleteById(presentationId);
    }

    @Override
    public AdminRewardGovernanceResponse getRewardGovernanceOverview() {
        List<RewardRule> rules = rewardRuleMapper.selectList(new LambdaQueryWrapper<RewardRule>()
                .orderByDesc(RewardRule::getCreatedAt)
                .orderByAsc(RewardRule::getId));
        List<RewardPresentation> presentations = rewardPresentationMapper.selectList(new LambdaQueryWrapper<RewardPresentation>()
                .orderByDesc(RewardPresentation::getCreatedAt)
                .orderByAsc(RewardPresentation::getId));
        List<RewardRuleBinding> allBindings = rewardRuleBindingMapper.selectList(new LambdaQueryWrapper<RewardRuleBinding>()
                .orderByAsc(RewardRuleBinding::getRuleId)
                .orderByAsc(RewardRuleBinding::getSortOrder)
                .orderByAsc(RewardRuleBinding::getId));

        int linkedIndoorBehaviorCount = (int) allBindings.stream()
                .filter(binding -> OWNER_TYPE_INDOOR_BEHAVIOR.equals(binding.getOwnerDomain()))
                .map(RewardRuleBinding::getOwnerId)
                .filter(Objects::nonNull)
                .distinct()
                .count();

        return AdminRewardGovernanceResponse.builder()
                .summary(AdminRewardGovernanceResponse.GovernanceSummary.builder()
                        .redeemablePrizeCount(Math.toIntExact(redeemablePrizeMapper.selectCount(new LambdaQueryWrapper<>())))
                        .gameRewardCount(Math.toIntExact(gameRewardMapper.selectCount(new LambdaQueryWrapper<>())))
                        .honorCount(Math.toIntExact(gameRewardMapper.selectCount(new LambdaQueryWrapper<GameReward>()
                                .in(GameReward::getRewardType, HONOR_REWARD_TYPES))))
                        .ruleCount(rules.size())
                        .presentationCount(presentations.size())
                        .linkedIndoorBehaviorCount(linkedIndoorBehaviorCount)
                        .build())
                .rules(rules.stream().map(this::mapRewardRule).toList())
                .presentations(presentations.stream().map(this::mapRewardPresentation).toList())
                .build();
    }

    private void applyPrizeRequest(RedeemablePrize prize, AdminRedeemablePrizeUpsertRequest request) {
        prize.setCode(requireText(request.getCode(), "code is required"));
        prize.setPrizeType(defaultText(request.getPrizeType(), "physical_prize"));
        prize.setFulfillmentMode(defaultText(request.getFulfillmentMode(), "offline_pickup"));
        prize.setNameZh(requireText(request.getNameZh(), "nameZh is required"));
        prize.setNameEn(trimToNull(request.getNameEn()));
        prize.setNameZht(trimToNull(request.getNameZht()));
        prize.setNamePt(trimToNull(request.getNamePt()));
        prize.setSubtitleZh(trimToNull(request.getSubtitleZh()));
        prize.setSubtitleEn(trimToNull(request.getSubtitleEn()));
        prize.setSubtitleZht(trimToNull(request.getSubtitleZht()));
        prize.setSubtitlePt(trimToNull(request.getSubtitlePt()));
        prize.setDescriptionZh(trimToNull(request.getDescriptionZh()));
        prize.setDescriptionEn(trimToNull(request.getDescriptionEn()));
        prize.setDescriptionZht(trimToNull(request.getDescriptionZht()));
        prize.setDescriptionPt(trimToNull(request.getDescriptionPt()));
        prize.setHighlightZh(trimToNull(request.getHighlightZh()));
        prize.setHighlightEn(trimToNull(request.getHighlightEn()));
        prize.setHighlightZht(trimToNull(request.getHighlightZht()));
        prize.setHighlightPt(trimToNull(request.getHighlightPt()));
        prize.setCoverAssetId(request.getCoverAssetId());
        prize.setStampCost(defaultNumber(request.getStampCost(), 0));
        prize.setInventoryTotal(defaultNumber(request.getInventoryTotal(), 0));
        prize.setInventoryRedeemed(defaultNumber(request.getInventoryRedeemed(), 0));
        prize.setStockPolicyJson(trimToNull(request.getStockPolicyJson()));
        prize.setFulfillmentConfigJson(trimToNull(request.getFulfillmentConfigJson()));
        prize.setOperatorNotes(trimToNull(request.getOperatorNotes()));
        prize.setPresentationId(request.getPresentationId());
        prize.setStatus(normalizeStatus(request.getStatus(), "draft"));
        prize.setSortOrder(defaultNumber(request.getSortOrder(), 0));
        prize.setPublishStartAt(parseDateTime(request.getPublishStartAt()));
        prize.setPublishEndAt(parseDateTime(request.getPublishEndAt()));
    }

    private void applyGameRewardRequest(GameReward reward, AdminGameRewardUpsertRequest request) {
        reward.setCode(requireText(request.getCode(), "code is required"));
        reward.setRewardType(defaultText(request.getRewardType(), "badge"));
        reward.setRarity(defaultText(request.getRarity(), "common"));
        reward.setStackable(defaultNumber(request.getStackable(), 0));
        reward.setMaxOwned(defaultNumber(request.getMaxOwned(), 1));
        reward.setCanEquip(defaultNumber(request.getCanEquip(), 0));
        reward.setCanConsume(defaultNumber(request.getCanConsume(), 0));
        reward.setNameZh(requireText(request.getNameZh(), "nameZh is required"));
        reward.setNameEn(trimToNull(request.getNameEn()));
        reward.setNameZht(trimToNull(request.getNameZht()));
        reward.setNamePt(trimToNull(request.getNamePt()));
        reward.setSubtitleZh(trimToNull(request.getSubtitleZh()));
        reward.setSubtitleEn(trimToNull(request.getSubtitleEn()));
        reward.setSubtitleZht(trimToNull(request.getSubtitleZht()));
        reward.setSubtitlePt(trimToNull(request.getSubtitlePt()));
        reward.setDescriptionZh(trimToNull(request.getDescriptionZh()));
        reward.setDescriptionEn(trimToNull(request.getDescriptionEn()));
        reward.setDescriptionZht(trimToNull(request.getDescriptionZht()));
        reward.setDescriptionPt(trimToNull(request.getDescriptionPt()));
        reward.setHighlightZh(trimToNull(request.getHighlightZh()));
        reward.setHighlightEn(trimToNull(request.getHighlightEn()));
        reward.setHighlightZht(trimToNull(request.getHighlightZht()));
        reward.setHighlightPt(trimToNull(request.getHighlightPt()));
        reward.setCoverAssetId(request.getCoverAssetId());
        reward.setIconAssetId(request.getIconAssetId());
        reward.setAnimationAssetId(request.getAnimationAssetId());
        reward.setRewardConfigJson(trimToNull(request.getRewardConfigJson()));
        reward.setPresentationId(request.getPresentationId());
        reward.setStatus(normalizeStatus(request.getStatus(), "draft"));
        reward.setSortOrder(defaultNumber(request.getSortOrder(), 0));
        reward.setPublishStartAt(parseDateTime(request.getPublishStartAt()));
        reward.setPublishEndAt(parseDateTime(request.getPublishEndAt()));
    }

    private void applyRewardRuleRequest(RewardRule rule, AdminRewardRuleUpsertRequest request) {
        rule.setCode(requireText(request.getCode(), "code is required"));
        rule.setNameZh(requireText(request.getNameZh(), "nameZh is required"));
        rule.setNameZht(trimToNull(request.getNameZht()));
        rule.setRuleType(defaultText(request.getRuleType(), "composite_rule"));
        rule.setStatus(normalizeStatus(request.getStatus(), "draft"));
        rule.setSummaryText(trimToNull(request.getSummaryText()));
        rule.setAdvancedConfigJson(trimToNull(request.getAdvancedConfigJson()));
    }

    private void applyRewardPresentationRequest(RewardPresentation presentation, AdminRewardPresentationUpsertRequest request) {
        presentation.setCode(requireText(request.getCode(), "code is required"));
        presentation.setNameZh(requireText(request.getNameZh(), "nameZh is required"));
        presentation.setNameZht(trimToNull(request.getNameZht()));
        presentation.setPresentationType(defaultText(request.getPresentationType(), "popup_card"));
        presentation.setFirstTimeOnly(defaultNumber(request.getFirstTimeOnly(), 0));
        presentation.setSkippable(defaultNumber(request.getSkippable(), 1));
        presentation.setMinimumDisplayMs(defaultNumber(request.getMinimumDisplayMs(), 1200));
        presentation.setInterruptPolicy(defaultText(request.getInterruptPolicy(), "queue"));
        presentation.setQueuePolicy(defaultText(request.getQueuePolicy(), "replace_same_priority"));
        presentation.setPriorityWeight(defaultNumber(request.getPriorityWeight(), 100));
        presentation.setCoverAssetId(request.getCoverAssetId());
        presentation.setVoiceOverAssetId(request.getVoiceOverAssetId());
        presentation.setSfxAssetId(request.getSfxAssetId());
        presentation.setSummaryText(trimToNull(request.getSummaryText()));
        presentation.setConfigJson(trimToNull(request.getConfigJson()));
        presentation.setStatus(normalizeStatus(request.getStatus(), "draft"));
    }

    private void validatePrizeRequest(AdminRedeemablePrizeUpsertRequest request) {
        if (request == null) {
            throw new BusinessException(4001, "redeemable prize request is required");
        }
        validateAssetId(request.getCoverAssetId(), "coverAssetId");
        validatePresentationId(request.getPresentationId());
        validateRuleIds(request.getRuleIds());
        validateBindings(request.getStorylineBindings(), request.getCityBindings(), request.getSubMapBindings(),
                request.getIndoorBuildingBindings(), request.getIndoorFloorBindings(), request.getAttachmentAssetIds());
    }

    private void validateGameRewardRequest(AdminGameRewardUpsertRequest request) {
        if (request == null) {
            throw new BusinessException(4001, "game reward request is required");
        }
        validateAssetId(request.getCoverAssetId(), "coverAssetId");
        validateAssetId(request.getIconAssetId(), "iconAssetId");
        validateAssetId(request.getAnimationAssetId(), "animationAssetId");
        validatePresentationId(request.getPresentationId());
        validateRuleIds(request.getRuleIds());
        validateBindings(request.getStorylineBindings(), request.getCityBindings(), request.getSubMapBindings(),
                request.getIndoorBuildingBindings(), request.getIndoorFloorBindings(), request.getAttachmentAssetIds());
    }

    private void validateRuleRequest(AdminRewardRuleUpsertRequest request) {
        if (request == null) {
            throw new BusinessException(4001, "請提供獎勵規則資料。");
        }
        List<AdminRewardRuleUpsertRequest.ConditionGroupPayload> groups =
                request.getConditionGroups() == null ? Collections.emptyList() : request.getConditionGroups();
        if (groups.isEmpty()) {
            throw new BusinessException(4001, "獎勵規則至少需要一組條件設定。");
        }
        for (AdminRewardRuleUpsertRequest.ConditionGroupPayload group : groups) {
            if (group == null || !StringUtils.hasText(group.getOperatorType())) {
                throw new BusinessException(4001, "condition group operatorType is required");
            }
            if ("at_least".equalsIgnoreCase(group.getOperatorType()) && defaultNumber(group.getMinimumMatchCount(), 0) <= 0) {
                throw new BusinessException(4001, "minimumMatchCount is required when operatorType is at_least");
            }
        }
    }

    private void validatePresentationRequest(AdminRewardPresentationUpsertRequest request) {
        if (request == null) {
            throw new BusinessException(4001, "請提供獎勵演出資料。");
        }
        validateAssetId(request.getCoverAssetId(), "coverAssetId");
        validateAssetId(request.getVoiceOverAssetId(), "voiceOverAssetId");
        validateAssetId(request.getSfxAssetId(), "sfxAssetId");
        if (request.getSteps() == null) {
            return;
        }
        for (AdminRewardPresentationUpsertRequest.StepPayload step : request.getSteps()) {
            if (step == null) {
                continue;
            }
            validateAssetId(step.getAssetId(), "step.assetId");
            validateAssetId(step.getTriggerSfxAssetId(), "step.triggerSfxAssetId");
            validateAssetId(step.getVoiceOverAssetId(), "step.voiceOverAssetId");
        }
    }

    private AdminRedeemablePrizeResponse mapRedeemablePrize(RedeemablePrize prize) {
        List<Long> ruleIds = listRuleIds(OWNER_TYPE_REDEEMABLE_PRIZE, prize.getId());
        return AdminRedeemablePrizeResponse.builder()
                .id(prize.getId())
                .code(prize.getCode())
                .prizeType(prize.getPrizeType())
                .fulfillmentMode(prize.getFulfillmentMode())
                .nameZh(prize.getNameZh())
                .nameEn(prize.getNameEn())
                .nameZht(prize.getNameZht())
                .namePt(prize.getNamePt())
                .subtitleZh(prize.getSubtitleZh())
                .subtitleEn(prize.getSubtitleEn())
                .subtitleZht(prize.getSubtitleZht())
                .subtitlePt(prize.getSubtitlePt())
                .descriptionZh(prize.getDescriptionZh())
                .descriptionEn(prize.getDescriptionEn())
                .descriptionZht(prize.getDescriptionZht())
                .descriptionPt(prize.getDescriptionPt())
                .highlightZh(prize.getHighlightZh())
                .highlightEn(prize.getHighlightEn())
                .highlightZht(prize.getHighlightZht())
                .highlightPt(prize.getHighlightPt())
                .coverAssetId(prize.getCoverAssetId())
                .stampCost(prize.getStampCost())
                .inventoryTotal(defaultNumber(prize.getInventoryTotal(), 0))
                .inventoryRedeemed(defaultNumber(prize.getInventoryRedeemed(), 0))
                .inventoryRemaining(Math.max(defaultNumber(prize.getInventoryTotal(), 0) - defaultNumber(prize.getInventoryRedeemed(), 0), 0))
                .stockPolicyJson(prize.getStockPolicyJson())
                .fulfillmentConfigJson(prize.getFulfillmentConfigJson())
                .operatorNotes(prize.getOperatorNotes())
                .presentationId(prize.getPresentationId())
                .presentation(toPresentationSummary(prize.getPresentationId()))
                .ruleIds(ruleIds)
                .linkedRules(toRuleLinks(ruleIds))
                .storylineBindings(bindingIds(OWNER_TYPE_REDEEMABLE_PRIZE, prize.getId(), RELATION_STORYLINE, "storyline"))
                .cityBindings(bindingIds(OWNER_TYPE_REDEEMABLE_PRIZE, prize.getId(), RELATION_CITY, "city"))
                .subMapBindings(bindingIds(OWNER_TYPE_REDEEMABLE_PRIZE, prize.getId(), RELATION_SUB_MAP, "sub_map"))
                .indoorBuildingBindings(bindingIds(OWNER_TYPE_REDEEMABLE_PRIZE, prize.getId(), RELATION_INDOOR_BUILDING, "indoor_building"))
                .indoorFloorBindings(bindingIds(OWNER_TYPE_REDEEMABLE_PRIZE, prize.getId(), RELATION_INDOOR_FLOOR, "indoor_floor"))
                .attachmentAssetIds(bindingIds(OWNER_TYPE_REDEEMABLE_PRIZE, prize.getId(), RELATION_ATTACHMENT, "asset"))
                .status(prize.getStatus())
                .sortOrder(prize.getSortOrder())
                .publishStartAt(prize.getPublishStartAt())
                .publishEndAt(prize.getPublishEndAt())
                .createdAt(prize.getCreatedAt())
                .build();
    }

    private AdminGameRewardResponse mapGameReward(GameReward reward) {
        List<Long> ruleIds = listRuleIds(OWNER_TYPE_GAME_REWARD, reward.getId());
        return AdminGameRewardResponse.builder()
                .id(reward.getId())
                .code(reward.getCode())
                .rewardType(reward.getRewardType())
                .rarity(reward.getRarity())
                .stackable(reward.getStackable())
                .maxOwned(reward.getMaxOwned())
                .canEquip(reward.getCanEquip())
                .canConsume(reward.getCanConsume())
                .nameZh(reward.getNameZh())
                .nameEn(reward.getNameEn())
                .nameZht(reward.getNameZht())
                .namePt(reward.getNamePt())
                .subtitleZh(reward.getSubtitleZh())
                .subtitleEn(reward.getSubtitleEn())
                .subtitleZht(reward.getSubtitleZht())
                .subtitlePt(reward.getSubtitlePt())
                .descriptionZh(reward.getDescriptionZh())
                .descriptionEn(reward.getDescriptionEn())
                .descriptionZht(reward.getDescriptionZht())
                .descriptionPt(reward.getDescriptionPt())
                .highlightZh(reward.getHighlightZh())
                .highlightEn(reward.getHighlightEn())
                .highlightZht(reward.getHighlightZht())
                .highlightPt(reward.getHighlightPt())
                .coverAssetId(reward.getCoverAssetId())
                .iconAssetId(reward.getIconAssetId())
                .animationAssetId(reward.getAnimationAssetId())
                .rewardConfigJson(reward.getRewardConfigJson())
                .presentationId(reward.getPresentationId())
                .presentation(toPresentationSummary(reward.getPresentationId()))
                .ruleIds(ruleIds)
                .linkedRules(toRuleLinks(ruleIds))
                .storylineBindings(bindingIds(OWNER_TYPE_GAME_REWARD, reward.getId(), RELATION_STORYLINE, "storyline"))
                .cityBindings(bindingIds(OWNER_TYPE_GAME_REWARD, reward.getId(), RELATION_CITY, "city"))
                .subMapBindings(bindingIds(OWNER_TYPE_GAME_REWARD, reward.getId(), RELATION_SUB_MAP, "sub_map"))
                .indoorBuildingBindings(bindingIds(OWNER_TYPE_GAME_REWARD, reward.getId(), RELATION_INDOOR_BUILDING, "indoor_building"))
                .indoorFloorBindings(bindingIds(OWNER_TYPE_GAME_REWARD, reward.getId(), RELATION_INDOOR_FLOOR, "indoor_floor"))
                .attachmentAssetIds(bindingIds(OWNER_TYPE_GAME_REWARD, reward.getId(), RELATION_ATTACHMENT, "asset"))
                .status(reward.getStatus())
                .sortOrder(reward.getSortOrder())
                .publishStartAt(reward.getPublishStartAt())
                .publishEndAt(reward.getPublishEndAt())
                .createdAt(reward.getCreatedAt())
                .build();
    }

    private AdminRewardRuleResponse mapRewardRule(RewardRule rule) {
        List<RewardConditionGroup> groups = rewardConditionGroupMapper.selectList(new LambdaQueryWrapper<RewardConditionGroup>()
                .eq(RewardConditionGroup::getRuleId, rule.getId())
                .orderByAsc(RewardConditionGroup::getSortOrder)
                .orderByAsc(RewardConditionGroup::getId));
        Map<Long, List<RewardCondition>> conditionsByGroup = rewardConditionMapper.selectList(new LambdaQueryWrapper<RewardCondition>()
                        .in(!groups.isEmpty(), RewardCondition::getGroupId, groups.stream().map(RewardConditionGroup::getId).toList())
                        .orderByAsc(RewardCondition::getSortOrder)
                        .orderByAsc(RewardCondition::getId))
                .stream()
                .collect(Collectors.groupingBy(RewardCondition::getGroupId, LinkedHashMap::new, Collectors.toList()));
        return AdminRewardRuleResponse.builder()
                .id(rule.getId())
                .code(rule.getCode())
                .ruleType(rule.getRuleType())
                .status(rule.getStatus())
                .nameZh(rule.getNameZh())
                .nameZht(rule.getNameZht())
                .summaryText(rule.getSummaryText())
                .advancedConfigJson(rule.getAdvancedConfigJson())
                .conditionGroups(groups.stream().map(group -> AdminRewardRuleResponse.ConditionGroupItem.builder()
                        .id(group.getId())
                        .groupCode(group.getGroupCode())
                        .operatorType(group.getOperatorType())
                        .minimumMatchCount(group.getMinimumMatchCount())
                        .summaryText(group.getSummaryText())
                        .advancedConfigJson(group.getAdvancedConfigJson())
                        .sortOrder(group.getSortOrder())
                        .conditions(conditionsByGroup.getOrDefault(group.getId(), Collections.emptyList()).stream()
                                .map(condition -> AdminRewardRuleResponse.ConditionItem.builder()
                                        .id(condition.getId())
                                        .conditionType(condition.getConditionType())
                                        .metricType(condition.getMetricType())
                                        .operatorType(condition.getOperatorType())
                                        .comparatorValue(condition.getComparatorValue())
                                        .comparatorUnit(condition.getComparatorUnit())
                                        .summaryText(condition.getSummaryText())
                                        .configJson(condition.getConfigJson())
                                        .sortOrder(condition.getSortOrder())
                                        .build())
                                .toList())
                        .build()).toList())
                .linkedOwners(loadLinkedOwnersForRule(rule.getId()))
                .createdAt(rule.getCreatedAt())
                .build();
    }

    private AdminRewardPresentationResponse mapRewardPresentation(RewardPresentation presentation) {
        List<RewardPresentationStep> steps = rewardPresentationStepMapper.selectList(new LambdaQueryWrapper<RewardPresentationStep>()
                .eq(RewardPresentationStep::getPresentationId, presentation.getId())
                .orderByAsc(RewardPresentationStep::getSortOrder)
                .orderByAsc(RewardPresentationStep::getId));
        return AdminRewardPresentationResponse.builder()
                .id(presentation.getId())
                .code(presentation.getCode())
                .nameZh(presentation.getNameZh())
                .nameZht(presentation.getNameZht())
                .presentationType(presentation.getPresentationType())
                .firstTimeOnly(presentation.getFirstTimeOnly())
                .skippable(presentation.getSkippable())
                .minimumDisplayMs(presentation.getMinimumDisplayMs())
                .interruptPolicy(presentation.getInterruptPolicy())
                .queuePolicy(presentation.getQueuePolicy())
                .priorityWeight(presentation.getPriorityWeight())
                .coverAssetId(presentation.getCoverAssetId())
                .voiceOverAssetId(presentation.getVoiceOverAssetId())
                .sfxAssetId(presentation.getSfxAssetId())
                .summaryText(presentation.getSummaryText())
                .configJson(presentation.getConfigJson())
                .status(presentation.getStatus())
                .steps(steps.stream().map(step -> AdminRewardPresentationResponse.StepItem.builder()
                        .id(step.getId())
                        .stepType(step.getStepType())
                        .stepCode(step.getStepCode())
                        .titleText(step.getTitleText())
                        .assetId(step.getAssetId())
                        .durationMs(step.getDurationMs())
                        .skippableOverride(step.getSkippableOverride())
                        .triggerSfxAssetId(step.getTriggerSfxAssetId())
                        .voiceOverAssetId(step.getVoiceOverAssetId())
                        .overlayConfigJson(step.getOverlayConfigJson())
                        .sortOrder(step.getSortOrder())
                        .build()).toList())
                .linkedOwners(loadLinkedOwnersForPresentation(presentation.getId()))
                .createdAt(presentation.getCreatedAt())
                .build();
    }

    private void syncConditionGroups(RewardRule rule, AdminRewardRuleUpsertRequest request) {
        deleteConditionTree(rule.getId());
        List<AdminRewardRuleUpsertRequest.ConditionGroupPayload> groups =
                request.getConditionGroups() == null ? Collections.emptyList() : request.getConditionGroups();
        Long rootGroupId = null;
        for (int groupIndex = 0; groupIndex < groups.size(); groupIndex++) {
            AdminRewardRuleUpsertRequest.ConditionGroupPayload groupPayload = groups.get(groupIndex);
            if (groupPayload == null) {
                continue;
            }
            RewardConditionGroup group = new RewardConditionGroup();
            group.setRuleId(rule.getId());
            group.setGroupCode(defaultText(groupPayload.getGroupCode(), "group_" + (groupIndex + 1)));
            group.setOperatorType(defaultText(groupPayload.getOperatorType(), "all"));
            group.setMinimumMatchCount(defaultNumber(groupPayload.getMinimumMatchCount(), 0));
            group.setSummaryText(trimToNull(groupPayload.getSummaryText()));
            group.setAdvancedConfigJson(trimToNull(groupPayload.getAdvancedConfigJson()));
            group.setSortOrder(defaultNumber(groupPayload.getSortOrder(), groupIndex));
            rewardConditionGroupMapper.insert(group);
            if (rootGroupId == null) {
                rootGroupId = group.getId();
            }
            List<AdminRewardRuleUpsertRequest.ConditionPayload> conditions =
                    groupPayload.getConditions() == null ? Collections.emptyList() : groupPayload.getConditions();
            for (int conditionIndex = 0; conditionIndex < conditions.size(); conditionIndex++) {
                AdminRewardRuleUpsertRequest.ConditionPayload conditionPayload = conditions.get(conditionIndex);
                if (conditionPayload == null) {
                    continue;
                }
                RewardCondition condition = new RewardCondition();
                condition.setGroupId(group.getId());
                condition.setConditionType(defaultText(conditionPayload.getConditionType(), "metric"));
                condition.setMetricType(trimToNull(conditionPayload.getMetricType()));
                condition.setOperatorType(defaultText(conditionPayload.getOperatorType(), "eq"));
                condition.setComparatorValue(trimToNull(conditionPayload.getComparatorValue()));
                condition.setComparatorUnit(trimToNull(conditionPayload.getComparatorUnit()));
                condition.setSummaryText(trimToNull(conditionPayload.getSummaryText()));
                condition.setConfigJson(trimToNull(conditionPayload.getConfigJson()));
                condition.setSortOrder(defaultNumber(conditionPayload.getSortOrder(), conditionIndex));
                rewardConditionMapper.insert(condition);
            }
        }
        rule.setRootConditionGroupId(rootGroupId);
        rewardRuleMapper.updateById(rule);
    }

    private void deleteConditionTree(Long ruleId) {
        List<RewardConditionGroup> groups = rewardConditionGroupMapper.selectList(new LambdaQueryWrapper<RewardConditionGroup>()
                .eq(RewardConditionGroup::getRuleId, ruleId));
        if (!groups.isEmpty()) {
            rewardConditionMapper.delete(new LambdaQueryWrapper<RewardCondition>()
                    .in(RewardCondition::getGroupId, groups.stream().map(RewardConditionGroup::getId).toList()));
            rewardConditionGroupMapper.delete(new LambdaQueryWrapper<RewardConditionGroup>()
                    .eq(RewardConditionGroup::getRuleId, ruleId));
        }
        rewardRuleBindingMapper.delete(new LambdaQueryWrapper<RewardRuleBinding>()
                .eq(RewardRuleBinding::getRuleId, ruleId));
    }

    private void syncPresentationSteps(Long presentationId, List<AdminRewardPresentationUpsertRequest.StepPayload> steps) {
        rewardPresentationStepMapper.delete(new LambdaQueryWrapper<RewardPresentationStep>()
                .eq(RewardPresentationStep::getPresentationId, presentationId));
        List<AdminRewardPresentationUpsertRequest.StepPayload> normalized = steps == null ? Collections.emptyList() : steps;
        for (int index = 0; index < normalized.size(); index++) {
            AdminRewardPresentationUpsertRequest.StepPayload payload = normalized.get(index);
            if (payload == null) {
                continue;
            }
            RewardPresentationStep step = new RewardPresentationStep();
            step.setPresentationId(presentationId);
            step.setStepType(defaultText(payload.getStepType(), "modal"));
            step.setStepCode(defaultText(payload.getStepCode(), "step_" + (index + 1)));
            step.setTitleText(trimToNull(payload.getTitleText()));
            step.setAssetId(payload.getAssetId());
            step.setDurationMs(defaultNumber(payload.getDurationMs(), 1200));
            step.setSkippableOverride(payload.getSkippableOverride());
            step.setTriggerSfxAssetId(payload.getTriggerSfxAssetId());
            step.setVoiceOverAssetId(payload.getVoiceOverAssetId());
            step.setOverlayConfigJson(trimToNull(payload.getOverlayConfigJson()));
            step.setSortOrder(defaultNumber(payload.getSortOrder(), index));
            rewardPresentationStepMapper.insert(step);
        }
    }

    private void syncScopeBindings(
            String ownerType,
            Long ownerId,
            List<Long> storylineBindings,
            List<Long> cityBindings,
            List<Long> subMapBindings,
            List<Long> indoorBuildingBindings,
            List<Long> indoorFloorBindings,
            List<Long> attachmentAssetIds
    ) {
        adminContentRelationService.syncTargetIds(ownerType, ownerId, RELATION_STORYLINE, "storyline", storylineBindings);
        adminContentRelationService.syncTargetIds(ownerType, ownerId, RELATION_CITY, "city", cityBindings);
        adminContentRelationService.syncTargetIds(ownerType, ownerId, RELATION_SUB_MAP, "sub_map", subMapBindings);
        adminContentRelationService.syncTargetIds(ownerType, ownerId, RELATION_INDOOR_BUILDING, "indoor_building", indoorBuildingBindings);
        adminContentRelationService.syncTargetIds(ownerType, ownerId, RELATION_INDOOR_FLOOR, "indoor_floor", indoorFloorBindings);
        adminContentRelationService.syncTargetIds(ownerType, ownerId, RELATION_ATTACHMENT, "asset", attachmentAssetIds);
    }

    private void clearScopeBindings(String ownerType, Long ownerId) {
        syncScopeBindings(ownerType, ownerId, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(),
                Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
    }

    private void syncRuleBindings(String ownerDomain, Long ownerId, String ownerCode, List<Long> ruleIds) {
        List<Long> normalizedRuleIds = normalizeIds(ruleIds);
        List<RewardRuleBinding> existing = rewardRuleBindingMapper.selectList(new LambdaQueryWrapper<RewardRuleBinding>()
                .eq(RewardRuleBinding::getOwnerDomain, ownerDomain)
                .eq(RewardRuleBinding::getOwnerId, ownerId));
        Map<Long, RewardRuleBinding> existingByRuleId = existing.stream()
                .collect(Collectors.toMap(RewardRuleBinding::getRuleId, Function.identity(), (left, right) -> left, LinkedHashMap::new));
        Set<Long> retained = new LinkedHashSet<>(normalizedRuleIds);
        for (RewardRuleBinding binding : existing) {
            if (binding.getRuleId() != null && !retained.contains(binding.getRuleId())) {
                rewardRuleBindingMapper.deleteById(binding.getId());
            }
        }
        for (int index = 0; index < normalizedRuleIds.size(); index++) {
            Long ruleId = normalizedRuleIds.get(index);
            RewardRuleBinding binding = existingByRuleId.get(ruleId);
            if (binding == null) {
                binding = new RewardRuleBinding();
                binding.setRuleId(ruleId);
                binding.setOwnerDomain(ownerDomain);
                binding.setOwnerId(ownerId);
                binding.setOwnerCode(ownerCode);
                binding.setBindingRole("attached");
                binding.setSortOrder(index);
                rewardRuleBindingMapper.insert(binding);
                continue;
            }
            binding.setOwnerCode(ownerCode);
            binding.setBindingRole("attached");
            binding.setSortOrder(index);
            rewardRuleBindingMapper.updateById(binding);
        }
    }

    private void clearRuleBindingsByOwner(String ownerDomain, Long ownerId) {
        rewardRuleBindingMapper.delete(new LambdaQueryWrapper<RewardRuleBinding>()
                .eq(RewardRuleBinding::getOwnerDomain, ownerDomain)
                .eq(RewardRuleBinding::getOwnerId, ownerId));
    }

    private int countRuleBindings(Long ruleId) {
        return Math.toIntExact(rewardRuleBindingMapper.selectCount(new LambdaQueryWrapper<RewardRuleBinding>()
                .eq(RewardRuleBinding::getRuleId, ruleId)));
    }

    private int countPresentationLinks(Long presentationId) {
        long prizeCount = redeemablePrizeMapper.selectCount(new LambdaQueryWrapper<RedeemablePrize>()
                .eq(RedeemablePrize::getPresentationId, presentationId));
        long rewardCount = gameRewardMapper.selectCount(new LambdaQueryWrapper<GameReward>()
                .eq(GameReward::getPresentationId, presentationId));
        return Math.toIntExact(prizeCount + rewardCount);
    }

    private String buildRuleBindingMessage(Long ruleId, String actionLabel) {
        List<RewardRuleBinding> bindings = rewardRuleBindingMapper.selectList(new LambdaQueryWrapper<RewardRuleBinding>()
                .eq(RewardRuleBinding::getRuleId, ruleId));
        long linkedPrizeCount = bindings.stream().filter(item -> OWNER_TYPE_REDEEMABLE_PRIZE.equals(item.getOwnerDomain())).count();
        long linkedGameRewardCount = bindings.stream().filter(item -> OWNER_TYPE_GAME_REWARD.equals(item.getOwnerDomain())).count();
        long linkedBehaviorCount = bindings.stream().filter(item -> OWNER_TYPE_INDOOR_BEHAVIOR.equals(item.getOwnerDomain())).count();
        return String.format(
                Locale.ROOT,
                "目前仍有 %d 個兌換獎勵、%d 個遊戲內獎勵、%d 個室內互動行為引用此規則，不能直接%s。",
                linkedPrizeCount,
                linkedGameRewardCount,
                linkedBehaviorCount,
                actionLabel
        );
    }

    private List<Long> listRuleIds(String ownerDomain, Long ownerId) {
        return rewardRuleBindingMapper.selectList(new LambdaQueryWrapper<RewardRuleBinding>()
                        .eq(RewardRuleBinding::getOwnerDomain, ownerDomain)
                        .eq(RewardRuleBinding::getOwnerId, ownerId)
                        .orderByAsc(RewardRuleBinding::getSortOrder)
                        .orderByAsc(RewardRuleBinding::getId))
                .stream()
                .map(RewardRuleBinding::getRuleId)
                .filter(Objects::nonNull)
                .toList();
    }

    private List<AdminRewardRuleLinkResponse> toRuleLinks(List<Long> ruleIds) {
        List<Long> normalized = normalizeIds(ruleIds);
        if (normalized.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Long, RewardRule> rules = rewardRuleMapper.selectBatchIds(normalized).stream()
                .collect(Collectors.toMap(RewardRule::getId, Function.identity(), (left, right) -> left, LinkedHashMap::new));
        return normalized.stream()
                .map(rules::get)
                .filter(Objects::nonNull)
                .map(rule -> AdminRewardRuleLinkResponse.builder()
                        .id(rule.getId())
                        .code(rule.getCode())
                        .nameZh(rule.getNameZh())
                        .nameZht(rule.getNameZht())
                        .summaryText(rule.getSummaryText())
                        .status(rule.getStatus())
                        .build())
                .toList();
    }

    private AdminRewardPresentationSummaryResponse toPresentationSummary(Long presentationId) {
        if (presentationId == null) {
            return null;
        }
        RewardPresentation presentation = rewardPresentationMapper.selectById(presentationId);
        if (presentation == null) {
            return null;
        }
        return AdminRewardPresentationSummaryResponse.builder()
                .id(presentation.getId())
                .code(presentation.getCode())
                .nameZh(presentation.getNameZh())
                .nameZht(presentation.getNameZht())
                .presentationType(presentation.getPresentationType())
                .status(presentation.getStatus())
                .build();
    }

    private List<AdminRewardLinkedEntityResponse> loadLinkedOwnersForRule(Long ruleId) {
        List<RewardRuleBinding> bindings = rewardRuleBindingMapper.selectList(new LambdaQueryWrapper<RewardRuleBinding>()
                .eq(RewardRuleBinding::getRuleId, ruleId)
                .orderByAsc(RewardRuleBinding::getSortOrder)
                .orderByAsc(RewardRuleBinding::getId));
        if (bindings.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> prizeIds = ownerIds(bindings, OWNER_TYPE_REDEEMABLE_PRIZE);
        List<Long> gameRewardIds = ownerIds(bindings, OWNER_TYPE_GAME_REWARD);
        List<Long> behaviorIds = ownerIds(bindings, OWNER_TYPE_INDOOR_BEHAVIOR);
        Map<Long, RedeemablePrize> prizes = prizeIds.isEmpty()
                ? Collections.emptyMap()
                : redeemablePrizeMapper.selectBatchIds(prizeIds).stream()
                .collect(Collectors.toMap(RedeemablePrize::getId, Function.identity(), (left, right) -> left, LinkedHashMap::new));
        Map<Long, GameReward> gameRewards = gameRewardIds.isEmpty()
                ? Collections.emptyMap()
                : gameRewardMapper.selectBatchIds(gameRewardIds).stream()
                .collect(Collectors.toMap(GameReward::getId, Function.identity(), (left, right) -> left, LinkedHashMap::new));
        Map<Long, IndoorNodeBehavior> behaviors = behaviorIds.isEmpty()
                ? Collections.emptyMap()
                : indoorNodeBehaviorMapper.selectBatchIds(behaviorIds).stream()
                .collect(Collectors.toMap(IndoorNodeBehavior::getId, Function.identity(), (left, right) -> left, LinkedHashMap::new));
        return bindings.stream()
                .map(binding -> toLinkedOwner(binding, prizes, gameRewards, behaviors))
                .filter(Objects::nonNull)
                .toList();
    }

    private List<AdminRewardLinkedEntityResponse> loadLinkedOwnersForPresentation(Long presentationId) {
        List<AdminRewardLinkedEntityResponse> items = new ArrayList<>();
        redeemablePrizeMapper.selectList(new LambdaQueryWrapper<RedeemablePrize>()
                        .eq(RedeemablePrize::getPresentationId, presentationId)
                        .orderByAsc(RedeemablePrize::getSortOrder)
                        .orderByAsc(RedeemablePrize::getId))
                .forEach(prize -> items.add(AdminRewardLinkedEntityResponse.builder()
                        .ownerDomain(OWNER_TYPE_REDEEMABLE_PRIZE)
                        .ownerId(prize.getId())
                        .ownerCode(prize.getCode())
                        .ownerName(firstNonBlank(prize.getNameZht(), prize.getNameZh(), prize.getCode()))
                        .bindingRole("presentation")
                        .build()));
        gameRewardMapper.selectList(new LambdaQueryWrapper<GameReward>()
                        .eq(GameReward::getPresentationId, presentationId)
                        .orderByAsc(GameReward::getSortOrder)
                        .orderByAsc(GameReward::getId))
                .forEach(reward -> items.add(AdminRewardLinkedEntityResponse.builder()
                        .ownerDomain(OWNER_TYPE_GAME_REWARD)
                        .ownerId(reward.getId())
                        .ownerCode(reward.getCode())
                        .ownerName(firstNonBlank(reward.getNameZht(), reward.getNameZh(), reward.getCode()))
                        .bindingRole("presentation")
                        .build()));
        return items;
    }

    private AdminRewardLinkedEntityResponse toLinkedOwner(
            RewardRuleBinding binding,
            Map<Long, RedeemablePrize> prizes,
            Map<Long, GameReward> gameRewards,
            Map<Long, IndoorNodeBehavior> behaviors
    ) {
        if (binding == null) {
            return null;
        }
        if (OWNER_TYPE_REDEEMABLE_PRIZE.equals(binding.getOwnerDomain())) {
            RedeemablePrize prize = prizes.get(binding.getOwnerId());
            return AdminRewardLinkedEntityResponse.builder()
                    .ownerDomain(binding.getOwnerDomain())
                    .ownerId(binding.getOwnerId())
                    .ownerCode(prize == null ? binding.getOwnerCode() : prize.getCode())
                    .ownerName(prize == null ? binding.getOwnerCode() : firstNonBlank(prize.getNameZht(), prize.getNameZh(), prize.getCode()))
                    .bindingRole(binding.getBindingRole())
                    .build();
        }
        if (OWNER_TYPE_GAME_REWARD.equals(binding.getOwnerDomain())) {
            GameReward reward = gameRewards.get(binding.getOwnerId());
            return AdminRewardLinkedEntityResponse.builder()
                    .ownerDomain(binding.getOwnerDomain())
                    .ownerId(binding.getOwnerId())
                    .ownerCode(reward == null ? binding.getOwnerCode() : reward.getCode())
                    .ownerName(reward == null ? binding.getOwnerCode() : firstNonBlank(reward.getNameZht(), reward.getNameZh(), reward.getCode()))
                    .bindingRole(binding.getBindingRole())
                    .build();
        }
        IndoorNodeBehavior behavior = behaviors.get(binding.getOwnerId());
        return AdminRewardLinkedEntityResponse.builder()
                .ownerDomain(binding.getOwnerDomain())
                .ownerId(binding.getOwnerId())
                .ownerCode(behavior == null ? binding.getOwnerCode() : behavior.getBehaviorCode())
                .ownerName(behavior == null ? binding.getOwnerCode() : firstNonBlank(behavior.getBehaviorNameZht(), behavior.getBehaviorNameZh(), behavior.getBehaviorCode()))
                .bindingRole(binding.getBindingRole())
                .build();
    }

    private List<Long> ownerIds(List<RewardRuleBinding> bindings, String ownerDomain) {
        if (bindings == null || bindings.isEmpty()) {
            return Collections.emptyList();
        }
        return bindings.stream()
                .filter(binding -> ownerDomain.equals(binding.getOwnerDomain()))
                .map(RewardRuleBinding::getOwnerId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    private RedeemablePrize requireRedeemablePrize(Long prizeId) {
        RedeemablePrize prize = redeemablePrizeMapper.selectById(prizeId);
        if (prize == null) {
            throw new BusinessException(4046, "找不到對應的兌換獎勵。");
        }
        return prize;
    }

    private GameReward requireGameReward(Long rewardId) {
        GameReward reward = gameRewardMapper.selectById(rewardId);
        if (reward == null) {
            throw new BusinessException(4047, "找不到對應的遊戲內獎勵。");
        }
        return reward;
    }

    private RewardRule requireRewardRule(Long ruleId) {
        RewardRule rule = rewardRuleMapper.selectById(ruleId);
        if (rule == null) {
            throw new BusinessException(4048, "找不到對應的獎勵規則。");
        }
        return rule;
    }

    private RewardPresentation requireRewardPresentation(Long presentationId) {
        RewardPresentation presentation = rewardPresentationMapper.selectById(presentationId);
        if (presentation == null) {
            throw new BusinessException(4049, "找不到對應的獎勵演出。");
        }
        return presentation;
    }

    private void validatePresentationId(Long presentationId) {
        if (presentationId != null && rewardPresentationMapper.selectById(presentationId) == null) {
            throw new BusinessException(4002, "指定的獎勵演出不存在。");
        }
    }

    private void validateRuleIds(List<Long> ruleIds) {
        List<Long> normalized = normalizeIds(ruleIds);
        if (!normalized.isEmpty() && rewardRuleMapper.selectBatchIds(normalized).size() != normalized.size()) {
            throw new BusinessException(4002, "指定的獎勵規則關聯目標不存在。");
        }
    }

    private void validateAssetId(Long assetId, String fieldName) {
        if (assetId != null && contentAssetMapper.selectById(assetId) == null) {
            throw new BusinessException(4002, fieldName + " 對應的資源不存在。");
        }
    }

    private void validateBindings(List<Long> storylineBindings, List<Long> cityBindings, List<Long> subMapBindings, List<Long> indoorBuildingBindings, List<Long> indoorFloorBindings, List<Long> attachmentAssetIds) {
        List<Long> normalizedStorylines = normalizeIds(storylineBindings);
        List<Long> normalizedCities = normalizeIds(cityBindings);
        List<Long> normalizedSubMaps = normalizeIds(subMapBindings);
        List<Long> normalizedBuildings = normalizeIds(indoorBuildingBindings);
        List<Long> normalizedFloors = normalizeIds(indoorFloorBindings);
        List<Long> normalizedAssets = normalizeIds(attachmentAssetIds);
        validateIdsExist(normalizedStorylines, id -> storyLineMapper.selectById(id) != null, "指定的故事線綁定目標不存在。");
        validateIdsExist(normalizedCities, id -> cityMapper.selectById(id) != null, "指定的城市綁定目標不存在。");
        validateIdsExist(normalizedSubMaps, id -> subMapMapper.selectById(id) != null, "指定的子地圖綁定目標不存在。");
        validateIdsExist(normalizedBuildings, id -> buildingMapper.selectById(id) != null, "指定的室內建築綁定目標不存在。");
        validateIdsExist(normalizedFloors, id -> indoorFloorMapper.selectById(id) != null, "指定的室內樓層綁定目標不存在。");
        validateIdsExist(normalizedAssets, id -> contentAssetMapper.selectById(id) != null, "指定的附件資源綁定目標不存在。");
        if (!normalizedFloors.isEmpty() && !normalizedBuildings.isEmpty()) {
            List<Long> invalidFloorIds = normalizedFloors.stream()
                    .filter(floorId -> {
                        IndoorFloor floor = indoorFloorMapper.selectById(floorId);
                        return floor == null || floor.getBuildingId() == null || !normalizedBuildings.contains(floor.getBuildingId());
                    }).toList();
            if (!invalidFloorIds.isEmpty()) {
                throw new BusinessException(4002, "室內樓層綁定必須隸屬於已選擇的室內建築。");
            }
        }
    }

    private void validateIdsExist(List<Long> ids, java.util.function.Predicate<Long> predicate, String message) {
        for (Long id : ids) {
            if (!predicate.test(id)) {
                throw new BusinessException(4002, message);
            }
        }
    }

    private List<Long> bindingIds(String ownerType, Long ownerId, String relationType, String targetType) {
        return adminContentRelationService.listTargetIds(ownerType, ownerId, relationType, targetType);
    }

    private List<Long> normalizeIds(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        return ids.stream().filter(Objects::nonNull).distinct().toList();
    }

    private String requireText(String value, String message) {
        if (!StringUtils.hasText(value)) {
            throw new BusinessException(4001, message);
        }
        return value.trim();
    }

    private String defaultText(String value, String defaultValue) {
        return StringUtils.hasText(value) ? value.trim() : defaultValue;
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private Integer defaultNumber(Integer value, Integer defaultValue) {
        return value == null ? defaultValue : value;
    }

    private String normalizeStatus(String value, String defaultValue) {
        if (!StringUtils.hasText(value)) {
            return defaultValue;
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        if ("1".equals(normalized) || "active".equals(normalized)) {
            return "published";
        }
        if ("0".equals(normalized) || "inactive".equals(normalized)) {
            return "archived";
        }
        return normalized;
    }

    private LocalDateTime parseDateTime(String value) {
        return StringUtils.hasText(value) ? LocalDateTime.parse(value.trim()) : null;
    }

    private String firstNonBlank(String... values) {
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
