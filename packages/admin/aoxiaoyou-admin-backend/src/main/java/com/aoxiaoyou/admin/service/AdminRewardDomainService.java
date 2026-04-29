package com.aoxiaoyou.admin.service;

import com.aoxiaoyou.admin.common.api.PageResponse;
import com.aoxiaoyou.admin.dto.request.AdminGameRewardUpsertRequest;
import com.aoxiaoyou.admin.dto.request.AdminRedeemablePrizeUpsertRequest;
import com.aoxiaoyou.admin.dto.request.AdminRewardPresentationUpsertRequest;
import com.aoxiaoyou.admin.dto.request.AdminRewardRuleUpsertRequest;
import com.aoxiaoyou.admin.dto.response.AdminGameRewardResponse;
import com.aoxiaoyou.admin.dto.response.AdminRedeemablePrizeResponse;
import com.aoxiaoyou.admin.dto.response.AdminRewardGovernanceResponse;
import com.aoxiaoyou.admin.dto.response.AdminRewardPresentationResponse;
import com.aoxiaoyou.admin.dto.response.AdminRewardRuleResponse;

public interface AdminRewardDomainService {

    PageResponse<AdminRedeemablePrizeResponse> pageRedeemablePrizes(long pageNum, long pageSize, String keyword, String status, String prizeType, String fulfillmentMode);

    AdminRedeemablePrizeResponse getRedeemablePrize(Long prizeId);

    AdminRedeemablePrizeResponse createRedeemablePrize(AdminRedeemablePrizeUpsertRequest request);

    AdminRedeemablePrizeResponse updateRedeemablePrize(Long prizeId, AdminRedeemablePrizeUpsertRequest request);

    void deleteRedeemablePrize(Long prizeId);

    PageResponse<AdminGameRewardResponse> pageGameRewards(long pageNum, long pageSize, String keyword, String status, String rewardType, Boolean honorsOnly);

    AdminGameRewardResponse getGameReward(Long rewardId);

    AdminGameRewardResponse createGameReward(AdminGameRewardUpsertRequest request);

    AdminGameRewardResponse updateGameReward(Long rewardId, AdminGameRewardUpsertRequest request);

    void deleteGameReward(Long rewardId);

    PageResponse<AdminRewardRuleResponse> pageRewardRules(long pageNum, long pageSize, String keyword, String status, String ruleType);

    AdminRewardRuleResponse getRewardRule(Long ruleId);

    AdminRewardRuleResponse createRewardRule(AdminRewardRuleUpsertRequest request);

    AdminRewardRuleResponse updateRewardRule(Long ruleId, AdminRewardRuleUpsertRequest request);

    void deleteRewardRule(Long ruleId);

    PageResponse<AdminRewardPresentationResponse> pageRewardPresentations(long pageNum, long pageSize, String keyword, String status, String presentationType);

    AdminRewardPresentationResponse getRewardPresentation(Long presentationId);

    AdminRewardPresentationResponse createRewardPresentation(AdminRewardPresentationUpsertRequest request);

    AdminRewardPresentationResponse updateRewardPresentation(Long presentationId, AdminRewardPresentationUpsertRequest request);

    void deleteRewardPresentation(Long presentationId);

    AdminRewardGovernanceResponse getRewardGovernanceOverview();
}
