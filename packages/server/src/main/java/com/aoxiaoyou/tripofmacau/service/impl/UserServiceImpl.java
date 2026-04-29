package com.aoxiaoyou.tripofmacau.service.impl;

import com.aoxiaoyou.tripofmacau.common.auth.JwtUtil;
import com.aoxiaoyou.tripofmacau.common.auth.WechatAuthService;
import com.aoxiaoyou.tripofmacau.common.auth.WechatCode2SessionResponse;
import com.aoxiaoyou.tripofmacau.common.exception.BusinessException;
import com.aoxiaoyou.tripofmacau.common.util.LocalizedContentSupport;
import com.aoxiaoyou.tripofmacau.dto.request.UserBootstrapCheckinRequest;
import com.aoxiaoyou.tripofmacau.dto.request.UserBootstrapStateRequest;
import com.aoxiaoyou.tripofmacau.dto.request.UserCheckinRequest;
import com.aoxiaoyou.tripofmacau.dto.request.UserCurrentCityUpdateRequest;
import com.aoxiaoyou.tripofmacau.dto.request.UserDevBypassLoginRequest;
import com.aoxiaoyou.tripofmacau.dto.request.UserPreferencesUpdateRequest;
import com.aoxiaoyou.tripofmacau.dto.request.UserWechatLoginRequest;
import com.aoxiaoyou.tripofmacau.dto.response.UserCheckinHistoryItemResponse;
import com.aoxiaoyou.tripofmacau.dto.response.UserCheckinResponse;
import com.aoxiaoyou.tripofmacau.dto.response.UserPreferencesResponse;
import com.aoxiaoyou.tripofmacau.dto.response.UserProfileResponse;
import com.aoxiaoyou.tripofmacau.dto.response.UserProgressResponse;
import com.aoxiaoyou.tripofmacau.dto.response.UserRewardRedeemResponse;
import com.aoxiaoyou.tripofmacau.dto.response.UserRewardRedemptionResponse;
import com.aoxiaoyou.tripofmacau.dto.response.UserSessionResponse;
import com.aoxiaoyou.tripofmacau.dto.response.UserStampProgressResponse;
import com.aoxiaoyou.tripofmacau.dto.response.UserStateResponse;
import com.aoxiaoyou.tripofmacau.dto.response.TestModeResponse;
import com.aoxiaoyou.tripofmacau.entity.City;
import com.aoxiaoyou.tripofmacau.entity.TestAccount;
import com.aoxiaoyou.tripofmacau.entity.Poi;
import com.aoxiaoyou.tripofmacau.entity.Reward;
import com.aoxiaoyou.tripofmacau.entity.RewardRedemption;
import com.aoxiaoyou.tripofmacau.entity.Stamp;
import com.aoxiaoyou.tripofmacau.entity.StoryChapter;
import com.aoxiaoyou.tripofmacau.entity.UserCheckin;
import com.aoxiaoyou.tripofmacau.entity.UserPreference;
import com.aoxiaoyou.tripofmacau.entity.UserProfile;
import com.aoxiaoyou.tripofmacau.entity.UserProgress;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.aoxiaoyou.tripofmacau.mapper.CityMapper;
import com.aoxiaoyou.tripofmacau.mapper.PoiMapper;
import com.aoxiaoyou.tripofmacau.mapper.RewardMapper;
import com.aoxiaoyou.tripofmacau.mapper.RewardRedemptionMapper;
import com.aoxiaoyou.tripofmacau.mapper.StampMapper;
import com.aoxiaoyou.tripofmacau.mapper.UserCheckinMapper;
import com.aoxiaoyou.tripofmacau.mapper.UserPreferenceMapper;
import com.aoxiaoyou.tripofmacau.mapper.UserProfileMapper;
import com.aoxiaoyou.tripofmacau.mapper.UserProgressMapper;
import com.aoxiaoyou.tripofmacau.service.CatalogFoundationService;
import com.aoxiaoyou.tripofmacau.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final TypeReference<List<Long>> LONG_LIST_TYPE = new TypeReference<>() {
    };
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    private final UserProfileMapper userProfileMapper;
    private final UserPreferenceMapper userPreferenceMapper;
    private final UserProgressMapper userProgressMapper;
    private final UserCheckinMapper userCheckinMapper;
    private final RewardRedemptionMapper rewardRedemptionMapper;
    private final RewardMapper rewardMapper;
    private final CityMapper cityMapper;
    private final PoiMapper poiMapper;
    private final StampMapper stampMapper;
    private final CatalogFoundationService catalogFoundationService;
    private final LocalizedContentSupport localizedContentSupport;
    private final ObjectMapper objectMapper;
    private final JwtUtil jwtUtil;
    private final WechatAuthService wechatAuthService;
    private final com.aoxiaoyou.tripofmacau.service.TestAccountService testAccountService;

    @Value("${app.trigger.cooldown-seconds:1800}")
    private long checkinCooldownSeconds;

    @Override
    @Transactional
    public UserSessionResponse loginWithWechat(UserWechatLoginRequest request) {
        WechatCode2SessionResponse session = wechatAuthService.exchangeCode(request.getCode());
        return loginByOpenId(
                session.getOpenid(),
                request.getNickname(),
                request.getAvatarUrl(),
                request.getLocaleCode(),
                request.getInterfaceMode()
        );
    }

    @Override
    @Transactional
    public UserSessionResponse loginWithDevBypass(UserDevBypassLoginRequest request) {
        return loginByOpenId(
                wechatAuthService.buildDevBypassOpenId(request.getDevIdentity()),
                request.getNickname(),
                request.getAvatarUrl(),
                request.getLocaleCode(),
                request.getInterfaceMode()
        );
    }

    private UserSessionResponse loginByOpenId(
            String openId,
            String nickname,
            String avatarUrl,
            String localeCode,
            String interfaceMode
    ) {
        UserProfile profile = userProfileMapper.selectOne(new LambdaQueryWrapper<UserProfile>()
                .eq(UserProfile::getOpenId, openId)
                .last("LIMIT 1"));
        if (profile == null) {
            profile = new UserProfile();
            profile.setOpenId(openId);
            profile.setNickname(resolveDisplayName(nickname));
            profile.setAvatarUrl(avatarUrl);
            profile.setLevel(1);
            profile.setTotalStamps(0);
            profile.setCurrentExp(0);
            profile.setNextLevelExp(120);
            profile.setCurrentLocaleCode(resolveLocaleCode(localeCode));
            TitleBundle title = titleForLevel(1, null);
            profile.setTitleZh(title.zh);
            profile.setTitleEn(title.en);
            profile.setTitleZht(title.zht);
            profile.setTitlePt(title.pt);
            userProfileMapper.insert(profile);
        } else {
            if (StringUtils.hasText(nickname)) {
                profile.setNickname(nickname.trim());
            }
            if (StringUtils.hasText(avatarUrl)) {
                profile.setAvatarUrl(avatarUrl.trim());
            }
            if (StringUtils.hasText(localeCode)) {
                profile.setCurrentLocaleCode(localeCode.trim());
            }
            userProfileMapper.updateById(profile);
        }

        UserPreference preference = ensurePreference(profile);
        if (StringUtils.hasText(interfaceMode)) {
            preference.setInterfaceMode(interfaceMode);
            preference.setSeniorMode("elderly".equalsIgnoreCase(interfaceMode));
        }
        if (StringUtils.hasText(localeCode)) {
            preference.setLocaleCode(localeCode.trim());
        }
        savePreference(preference);

        return UserSessionResponse.builder()
                .accessToken(jwtUtil.generateAccessToken(profile.getId(), profile.getOpenId(), profile.getNickname()))
                .tokenType("Bearer")
                .state(buildState(profile.getId(), localeCode))
                .build();
    }

    @Override
    public UserStateResponse getState(Long userId, String localeHint) {
        return buildState(userId, localeHint);
    }

    @Override
    public UserProfileResponse getProfile(Long userId, String localeHint) {
        UserProfile profile = requireProfile(userId);
        return toProfile(profile, ensurePreference(profile), localeHint);
    }

    @Override
    @Transactional
    public UserProfileResponse updateCurrentCity(Long userId, UserCurrentCityUpdateRequest request, String localeHint) {
        UserProfile profile = requireProfile(userId);
        City city = requireCity(request.getCityCode());
        profile.setCurrentCityId(city.getId());
        userProfileMapper.updateById(profile);
        return toProfile(profile, ensurePreference(profile), localeHint);
    }

    @Override
    public UserProgressResponse getProgress(Long userId, String localeHint) {
        return buildProgress(userId, localeHint);
    }

    @Override
    public UserPreferencesResponse getPreferences(Long userId) {
        return toPreferences(ensurePreference(requireProfile(userId)));
    }

    @Override
    @Transactional
    public UserPreferencesResponse updatePreferences(Long userId, UserPreferencesUpdateRequest request) {
        UserProfile profile = requireProfile(userId);
        UserPreference preference = ensurePreference(profile);
        if (StringUtils.hasText(request.getInterfaceMode())) {
            preference.setInterfaceMode(request.getInterfaceMode());
            if (request.getSeniorMode() == null) {
                preference.setSeniorMode("elderly".equalsIgnoreCase(request.getInterfaceMode()));
            }
        }
        if (request.getFontScale() != null) {
            preference.setFontScale(request.getFontScale().setScale(1, RoundingMode.HALF_UP));
        }
        if (request.getHighContrast() != null) {
            preference.setHighContrast(request.getHighContrast());
        }
        if (request.getVoiceGuideEnabled() != null) {
            preference.setVoiceGuideEnabled(request.getVoiceGuideEnabled());
        }
        if (request.getSeniorMode() != null) {
            preference.setSeniorMode(request.getSeniorMode());
        }
        if (StringUtils.hasText(request.getLocaleCode())) {
            preference.setLocaleCode(request.getLocaleCode());
            profile.setCurrentLocaleCode(request.getLocaleCode());
            userProfileMapper.updateById(profile);
        }
        if (request.getEmergencyContactName() != null) {
            preference.setEmergencyContactName(request.getEmergencyContactName());
        }
        if (request.getEmergencyContactPhone() != null) {
            preference.setEmergencyContactPhone(request.getEmergencyContactPhone());
        }
        if (request.getRuntimeOverrides() != null) {
            preference.setRuntimeOverridesJson(writeJson(request.getRuntimeOverrides()));
        }
        savePreference(preference);
        return toPreferences(preference);
    }

    @Override
    public List<UserStampProgressResponse> getStampProgress(Long userId) {
        ProgressState state = loadProgress(userId);
        return state.collectedStampIds.stream()
                .map(stampId -> UserStampProgressResponse.builder().stampId(stampId).collectedAt(null).build())
                .toList();
    }

    @Override
    public List<UserRewardRedemptionResponse> getRewardRedemptions(Long userId, String localeHint) {
        return listRedemptions(userId, localeHint);
    }

    @Override
    @Transactional
    public UserCheckinResponse checkin(Long userId, UserCheckinRequest request, String localeHint) {
        UserProfile profile = requireProfile(userId);
        UserPreference preference = ensurePreference(profile);
        String locale = resolveLocale(localeHint, profile, preference);
        Poi poi = catalogFoundationService.getPublishedPoi(request.getPoiId())
                .orElseThrow(() -> new BusinessException(4041, "POI not found"));
        String triggerMode = normalizeTriggerMode(request.getTriggerMode());

        UserCheckin lastCheckin = userCheckinMapper.selectOne(new LambdaQueryWrapper<UserCheckin>()
                .eq(UserCheckin::getUserId, userId)
                .eq(UserCheckin::getPoiId, poi.getId())
                .orderByDesc(UserCheckin::getCheckedAt)
                .orderByDesc(UserCheckin::getId)
                .last("LIMIT 1"));
        LocalDateTime now = LocalDateTime.now();

        ProgressState state = loadProgress(userId);
        Stamp stamp = selectStamp(poi, state.collectedStampIds);
        if (stamp == null) {
            throw new BusinessException(4004, "No stamp is configured for this POI");
        }
        if (lastCheckin != null && lastCheckin.getCheckedAt() != null
                && lastCheckin.getCheckedAt().isAfter(now.minusSeconds(checkinCooldownSeconds))) {
            return UserCheckinResponse.builder()
                    .success(Boolean.TRUE)
                    .poiId(poi.getId())
                    .poiName(localizedContentSupport.resolveText(locale, poi.getNameZh(), poi.getNameEn(), poi.getNameZht(), poi.getNamePt()))
                    .stampId(stamp.getId())
                    .stampName(localizedContentSupport.resolveText(locale, stamp.getNameZh(), stamp.getNameEn(), stamp.getNameZht(), stamp.getNamePt()))
                    .experienceGained(0)
                    .triggerMode(triggerMode)
                    .unlockedStorylineId(poi.getStorylineId())
                    .checkedAt(lastCheckin.getCheckedAt())
                    .state(buildState(userId, locale))
                    .build();
        }

        boolean newStamp = state.collectedStampIds.add(stamp.getId());
        if (poi.getStorylineId() != null) {
            state.completedStoryIds.add(poi.getStorylineId());
            state.activeStoryId = poi.getStorylineId();
        }
        StoryChapter nextChapter = nextChapter(poi.getStorylineId(), state.completedChapterIds);
        if (nextChapter != null) {
            state.completedChapterIds.add(nextChapter.getId());
        }

        int gained = experienceGain(poi.getDifficulty());
        profile.setTotalStamps(intValue(profile.getTotalStamps()) + (newStamp ? 1 : 0));
        profile.setCurrentExp(intValue(profile.getCurrentExp()) + gained);
        profile.setCurrentCityId(poi.getCityId());
        levelUp(profile);
        userProfileMapper.updateById(profile);

        UserCheckin checkin = new UserCheckin();
        checkin.setUserId(userId);
        checkin.setPoiId(poi.getId());
        checkin.setTriggerMode(triggerMode);
        checkin.setDistanceMeters(request.getDistanceMeters());
        checkin.setGpsAccuracy(request.getGpsAccuracy());
        checkin.setLatitude(request.getLatitude());
        checkin.setLongitude(request.getLongitude());
        checkin.setCheckedAt(now);
        userCheckinMapper.insert(checkin);

        saveProgress(userId, state);

        return UserCheckinResponse.builder()
                .success(Boolean.TRUE)
                .poiId(poi.getId())
                .poiName(localizedContentSupport.resolveText(locale, poi.getNameZh(), poi.getNameEn(), poi.getNameZht(), poi.getNamePt()))
                .stampId(stamp.getId())
                .stampName(localizedContentSupport.resolveText(locale, stamp.getNameZh(), stamp.getNameEn(), stamp.getNameZht(), stamp.getNamePt()))
                .experienceGained(gained)
                .triggerMode(triggerMode)
                .unlockedStorylineId(poi.getStorylineId())
                .checkedAt(now)
                .state(buildState(userId, locale))
                .build();
    }

    @Override
    @Transactional
    public UserRewardRedeemResponse redeemReward(Long userId, Long rewardId, String localeHint) {
        UserProfile profile = requireProfile(userId);
        UserPreference preference = ensurePreference(profile);
        String locale = resolveLocale(localeHint, profile, preference);
        Reward reward = catalogFoundationService.listPublishedRewards().stream()
                .filter(item -> Objects.equals(item.getId(), rewardId))
                .findFirst()
                .orElseThrow(() -> new BusinessException(4044, "Reward not found"));

        RewardRedemption existing = rewardRedemptionMapper.selectOne(new LambdaQueryWrapper<RewardRedemption>()
                .eq(RewardRedemption::getUserId, userId)
                .eq(RewardRedemption::getRewardId, rewardId)
                .last("LIMIT 1"));
        if (existing != null) {
            throw new BusinessException(4091, "You already redeemed this reward");
        }
        if (intValue(profile.getTotalStamps()) < intValue(reward.getStampCost())) {
            throw new BusinessException(4092, "Not enough stamps yet");
        }
        if (intValue(reward.getInventoryRedeemed()) >= intValue(reward.getInventoryTotal())) {
            throw new BusinessException(4093, "Reward inventory is exhausted");
        }

        int updated = rewardMapper.update(null, new LambdaUpdateWrapper<Reward>()
                .eq(Reward::getId, rewardId)
                .lt(Reward::getInventoryRedeemed, intValue(reward.getInventoryTotal()))
                .setSql("inventory_redeemed = inventory_redeemed + 1"));
        if (updated == 0) {
            throw new BusinessException(4093, "Reward inventory is exhausted");
        }

        profile.setTotalStamps(intValue(profile.getTotalStamps()) - intValue(reward.getStampCost()));
        userProfileMapper.updateById(profile);

        RewardRedemption redemption = new RewardRedemption();
        redemption.setUserId(userId);
        redemption.setRewardId(rewardId);
        redemption.setRedemptionStatus("created");
        redemption.setStampCostSnapshot(reward.getStampCost());
        redemption.setQrCode("TOM-" + userId + "-" + rewardId + "-" + UUID.randomUUID().toString().substring(0, 8));
        redemption.setRedeemedAt(LocalDateTime.now());
        redemption.setExpiresAt(LocalDateTime.now().plusDays(30));
        rewardRedemptionMapper.insert(redemption);

        return UserRewardRedeemResponse.builder()
                .rewardId(rewardId)
                .rewardName(localizedContentSupport.resolveText(locale, reward.getNameZh(), reward.getNameEn(), reward.getNameZht(), reward.getNamePt()))
                .redemptionStatus(redemption.getRedemptionStatus())
                .qrCode(redemption.getQrCode())
                .expiresAt(redemption.getExpiresAt())
                .state(buildState(userId, locale))
                .build();
    }

    private UserProfile requireProfile(Long userId) {
        UserProfile profile = userProfileMapper.selectById(userId);
        if (profile == null) {
            throw new BusinessException(4040, "User not found");
        }
        return profile;
    }

    private UserPreference ensurePreference(UserProfile profile) {
        UserPreference preference = userPreferenceMapper.selectOne(new LambdaQueryWrapper<UserPreference>()
                .eq(UserPreference::getUserId, profile.getId())
                .last("LIMIT 1"));
        if (preference != null) {
            return preference;
        }
        preference = new UserPreference();
        preference.setUserId(profile.getId());
        preference.setInterfaceMode("standard");
        preference.setFontScale(BigDecimal.ONE.setScale(1, RoundingMode.HALF_UP));
        preference.setHighContrast(false);
        preference.setVoiceGuideEnabled(true);
        preference.setSeniorMode(false);
        preference.setLocaleCode(StringUtils.hasText(profile.getCurrentLocaleCode()) ? profile.getCurrentLocaleCode() : "en");
        preference.setEmergencyContactName("");
        preference.setEmergencyContactPhone("");
        userPreferenceMapper.insert(preference);
        return preference;
    }

    private void savePreference(UserPreference preference) {
        if (preference.getId() == null) {
            userPreferenceMapper.insert(preference);
        } else {
            userPreferenceMapper.updateById(preference);
        }
    }

    private String resolveLocale(String localeHint, UserProfile profile, UserPreference preference) {
        if (StringUtils.hasText(localeHint)) {
            return localeHint;
        }
        if (preference != null && StringUtils.hasText(preference.getLocaleCode())) {
            return preference.getLocaleCode();
        }
        if (profile != null && StringUtils.hasText(profile.getCurrentLocaleCode())) {
            return profile.getCurrentLocaleCode();
        }
        return "en";
    }

    private UserProfileResponse toProfile(UserProfile profile, UserPreference preference, String localeHint) {
        String locale = resolveLocale(localeHint, profile, preference);
        City city = profile.getCurrentCityId() == null ? null : cityMapper.selectById(profile.getCurrentCityId());
        return UserProfileResponse.builder()
                .id(profile.getId())
                .openId(profile.getOpenId())
                .nickname(profile.getNickname())
                .avatarUrl(profile.getAvatarUrl())
                .level(profile.getLevel())
                .title(localizedContentSupport.resolveText(locale, profile.getTitleZh(), profile.getTitleEn(), profile.getTitleZht(), profile.getTitlePt()))
                .totalStamps(profile.getTotalStamps())
                .currentExp(profile.getCurrentExp())
                .nextLevelExp(profile.getNextLevelExp())
                .currentCityId(profile.getCurrentCityId())
                .currentCityCode(city == null ? "" : city.getCode())
                .currentLocaleCode(resolveLocale(null, profile, preference))
                .createdAt(profile.getCreatedAt())
                .updatedAt(profile.getUpdatedAt())
                .build();
    }

    private UserPreferencesResponse toPreferences(UserPreference preference) {
        return UserPreferencesResponse.builder()
                .interfaceMode(preference.getInterfaceMode())
                .fontScale(preference.getFontScale())
                .highContrast(Boolean.TRUE.equals(preference.getHighContrast()))
                .voiceGuideEnabled(Boolean.TRUE.equals(preference.getVoiceGuideEnabled()))
                .seniorMode(Boolean.TRUE.equals(preference.getSeniorMode()))
                .localeCode(preference.getLocaleCode())
                .emergencyContactName(preference.getEmergencyContactName())
                .emergencyContactPhone(preference.getEmergencyContactPhone())
                .runtimeOverrides(readMap(preference.getRuntimeOverridesJson()))
                .build();
    }

    private City requireCity(String cityCode) {
        City city = cityMapper.selectOne(new LambdaQueryWrapper<City>()
                .eq(City::getCode, cityCode)
                .last("LIMIT 1"));
        if (city == null) {
            throw new BusinessException(4045, "City not found");
        }
        return city;
    }

    private Set<Long> uniqueIds(Collection<Long> values) {
        if (values == null || values.isEmpty()) {
            return new LinkedHashSet<>();
        }
        return values.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private List<Long> readIds(String json) {
        if (!StringUtils.hasText(json)) {
            return Collections.emptyList();
        }
        try {
            List<Long> ids = objectMapper.readValue(json, LONG_LIST_TYPE);
            return ids == null ? Collections.emptyList() : ids.stream().filter(Objects::nonNull).distinct().toList();
        } catch (Exception ignored) {
            return Collections.emptyList();
        }
    }

    private String writeIds(Collection<Long> values) {
        return writeJson(uniqueIds(values));
    }

    private Map<String, Object> readMap(String json) {
        if (!StringUtils.hasText(json)) {
            return Collections.emptyMap();
        }
        try {
            Map<String, Object> value = objectMapper.readValue(json, MAP_TYPE);
            return value == null ? Collections.emptyMap() : value;
        } catch (Exception ignored) {
            return Collections.emptyMap();
        }
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception ex) {
            throw new BusinessException(5001, "Failed to serialize user state");
        }
    }

    private LocalDateTime parseDateTime(String value) {
        if (!StringUtils.hasText(value)) {
            return LocalDateTime.now();
        }
        try {
            return OffsetDateTime.parse(value).toLocalDateTime();
        } catch (Exception ignored) {
        }
        try {
            return LocalDateTime.parse(value);
        } catch (Exception ignored) {
        }
        try {
            return LocalDateTime.ofInstant(Instant.parse(value), ZoneOffset.UTC);
        } catch (Exception ignored) {
        }
        return LocalDateTime.now();
    }

    private int intValue(Integer value) {
        return intValue(value, 0);
    }

    private int intValue(Integer value, int fallback) {
        return value == null ? fallback : value;
    }

    private TitleBundle titleForLevel(Integer level, String explicitTitle) {
        if (StringUtils.hasText(explicitTitle)) {
            return new TitleBundle(explicitTitle, explicitTitle, explicitTitle);
        }
        int current = Math.max(1, intValue(level, 1));
        if (current >= 5) {
            return new TitleBundle("城市故事向导", "City Story Guide", "城市故事嚮導");
        }
        if (current >= 4) {
            return new TitleBundle("路线探索者", "Route Explorer", "路線探索者");
        }
        if (current >= 3) {
            return new TitleBundle("澳门见习生", "Macau Walker", "澳門見習生");
        }
        return new TitleBundle("探索新手", "Explorer Rookie", "探索新手");
    }

    private String resolveDisplayName(String nickname) {
        return StringUtils.hasText(nickname) ? nickname.trim() : "Traveler";
    }

    private String resolveLocaleCode(String localeCode) {
        return StringUtils.hasText(localeCode) ? localeCode.trim() : "en";
    }

    private void levelUp(UserProfile profile) {
        int level = intValue(profile.getLevel(), 1);
        int currentExp = intValue(profile.getCurrentExp());
        int nextLevelExp = Math.max(120, intValue(profile.getNextLevelExp(), 120));
        while (currentExp >= nextLevelExp) {
            currentExp -= nextLevelExp;
            level += 1;
            nextLevelExp += 120;
        }
        profile.setLevel(level);
        profile.setCurrentExp(currentExp);
        profile.setNextLevelExp(nextLevelExp);
        TitleBundle title = titleForLevel(level, null);
        profile.setTitleZh(title.zh);
        profile.setTitleEn(title.en);
        profile.setTitleZht(title.zht);
        profile.setTitlePt(title.pt);
    }

    private UserStateResponse buildState(Long userId, String localeHint) {
        UserProfile profile = requireProfile(userId);
        UserPreference preference = ensurePreference(profile);
        return UserStateResponse.builder()
                .profile(toProfile(profile, preference, localeHint))
                .preferences(toPreferences(preference))
                .progress(buildProgress(userId, localeHint))
                .rewardRedemptions(listRedemptions(userId, localeHint))
                .build();
    }

    private UserProgressResponse buildProgress(Long userId, String localeHint) {
        UserProfile profile = requireProfile(userId);
        UserPreference preference = ensurePreference(profile);
        String locale = resolveLocale(localeHint, profile, preference);
        ProgressState state = loadProgress(userId);
        return UserProgressResponse.builder()
                .activeStoryId(state.activeStoryId)
                .collectedStampIds(new ArrayList<>(state.collectedStampIds))
                .completedStoryIds(new ArrayList<>(state.completedStoryIds))
                .completedChapterIds(new ArrayList<>(state.completedChapterIds))
                .unlockedCityCodes(unlockedCities(profile))
                .redeemedRewardIds(rewardRedemptionMapper.selectList(new LambdaQueryWrapper<RewardRedemption>()
                                .eq(RewardRedemption::getUserId, userId))
                        .stream()
                        .map(RewardRedemption::getRewardId)
                        .toList())
                .checkinHistory(history(userId, locale, state.collectedStampIds))
                .build();
    }

    private List<UserRewardRedemptionResponse> listRedemptions(Long userId, String localeHint) {
        UserProfile profile = requireProfile(userId);
        UserPreference preference = ensurePreference(profile);
        String locale = resolveLocale(localeHint, profile, preference);
        List<RewardRedemption> items = rewardRedemptionMapper.selectList(new LambdaQueryWrapper<RewardRedemption>()
                .eq(RewardRedemption::getUserId, userId)
                .orderByDesc(RewardRedemption::getCreatedAt)
                .orderByDesc(RewardRedemption::getId));
        if (items.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Long, Reward> rewardsById = rewardMapper.selectBatchIds(items.stream().map(RewardRedemption::getRewardId).distinct().toList()).stream()
                .map(Reward.class::cast)
                .collect(Collectors.toMap(Reward::getId, reward -> reward, (left, right) -> left, LinkedHashMap::new));
        return items.stream()
                .map(item -> UserRewardRedemptionResponse.builder()
                        .id(item.getId())
                        .rewardId(item.getRewardId())
                        .rewardName(rewardsById.containsKey(item.getRewardId())
                                ? localizedContentSupport.resolveText(
                                        locale,
                                        rewardsById.get(item.getRewardId()).getNameZh(),
                                        rewardsById.get(item.getRewardId()).getNameEn(),
                                        rewardsById.get(item.getRewardId()).getNameZht(),
                                        rewardsById.get(item.getRewardId()).getNamePt())
                                : "")
                        .redemptionStatus(item.getRedemptionStatus())
                        .stampCostSnapshot(item.getStampCostSnapshot())
                        .qrCode(item.getQrCode())
                        .redeemedAt(item.getRedeemedAt())
                        .expiresAt(item.getExpiresAt())
                        .createdAt(item.getCreatedAt())
                        .build())
                .toList();
    }

    private ProgressState loadProgress(Long userId) {
        ProgressState state = new ProgressState();
        UserProgress aggregate = userProgressMapper.selectOne(new LambdaQueryWrapper<UserProgress>()
                .eq(UserProgress::getUserId, userId)
                .isNull(UserProgress::getStorylineId)
                .last("LIMIT 1"));
        if (aggregate != null) {
            state.activeStoryId = aggregate.getActiveStorylineId();
            state.collectedStampIds.addAll(readIds(aggregate.getCollectedStampIdsJson()));
            state.completedChapterIds.addAll(readIds(aggregate.getCompletedChapterIdsJson()));
        }
        userProgressMapper.selectList(new LambdaQueryWrapper<UserProgress>()
                        .eq(UserProgress::getUserId, userId)
                        .isNotNull(UserProgress::getStorylineId)
                        .eq(UserProgress::getCompletedStoryline, true))
                .forEach(row -> state.completedStoryIds.add(row.getStorylineId()));
        return state;
    }

    private void saveProgress(Long userId, ProgressState state) {
        UserProgress aggregate = userProgressMapper.selectOne(new LambdaQueryWrapper<UserProgress>()
                .eq(UserProgress::getUserId, userId)
                .isNull(UserProgress::getStorylineId)
                .last("LIMIT 1"));
        if (aggregate == null) {
            aggregate = new UserProgress();
            aggregate.setUserId(userId);
        }
        aggregate.setActiveStorylineId(state.activeStoryId);
        aggregate.setCompletedStoryline(false);
        aggregate.setCollectedStampIdsJson(writeIds(state.collectedStampIds));
        aggregate.setCompletedChapterIdsJson(writeIds(state.completedChapterIds));
        aggregate.setProgressPercent(0);
        aggregate.setLastSeenAt(LocalDateTime.now());
        saveProgressRow(aggregate);

        for (Long storyId : state.completedStoryIds) {
            UserProgress row = userProgressMapper.selectOne(new LambdaQueryWrapper<UserProgress>()
                    .eq(UserProgress::getUserId, userId)
                    .eq(UserProgress::getStorylineId, storyId)
                    .last("LIMIT 1"));
            if (row == null) {
                row = new UserProgress();
                row.setUserId(userId);
                row.setStorylineId(storyId);
            }
            row.setActiveStorylineId(state.activeStoryId);
            row.setCompletedStoryline(true);
            row.setLastSeenAt(LocalDateTime.now());
            row.setCompletedAt(LocalDateTime.now());
            saveProgressRow(row);
        }
    }

    private void saveProgressRow(UserProgress row) {
        if (row.getId() == null) {
            userProgressMapper.insert(row);
        } else {
            userProgressMapper.updateById(row);
        }
    }

    private boolean bootstrapEligible(Long userId) {
        return userProgressMapper.selectCount(new LambdaQueryWrapper<UserProgress>().eq(UserProgress::getUserId, userId)) == 0
                && userCheckinMapper.selectCount(new LambdaQueryWrapper<UserCheckin>().eq(UserCheckin::getUserId, userId)) == 0
                && rewardRedemptionMapper.selectCount(new LambdaQueryWrapper<RewardRedemption>().eq(RewardRedemption::getUserId, userId)) == 0;
    }

    private void applyBootstrap(UserProfile profile, UserBootstrapStateRequest bootstrap) {
        profile.setLevel(Math.max(1, intValue(bootstrap.getLevel(), intValue(profile.getLevel(), 1))));
        profile.setTotalStamps(Math.max(0, intValue(bootstrap.getTotalStamps(), intValue(profile.getTotalStamps()))));
        profile.setCurrentExp(Math.max(0, intValue(bootstrap.getCurrentExp(), intValue(profile.getCurrentExp()))));
        profile.setNextLevelExp(Math.max(120, intValue(bootstrap.getNextLevelExp(), intValue(profile.getNextLevelExp(), 120))));
        if (StringUtils.hasText(bootstrap.getCurrentCityCode())) {
            profile.setCurrentCityId(requireCity(bootstrap.getCurrentCityCode()).getId());
        }
        TitleBundle title = titleForLevel(profile.getLevel(), bootstrap.getTitle());
        profile.setTitleZh(title.zh);
        profile.setTitleEn(title.en);
        profile.setTitleZht(title.zht);
        profile.setTitlePt(title.pt);
        userProfileMapper.updateById(profile);

        if (bootstrap.getPreferences() != null) {
            updatePreferences(profile.getId(), bootstrap.getPreferences());
        }

        ProgressState state = new ProgressState();
        state.activeStoryId = bootstrap.getActiveStoryId();
        state.collectedStampIds.addAll(uniqueIds(bootstrap.getCollectedStampIds()));
        state.completedStoryIds.addAll(uniqueIds(bootstrap.getCompletedStoryIds()));
        state.completedChapterIds.addAll(uniqueIds(bootstrap.getCompletedChapterIds()));
        saveProgress(profile.getId(), state);

        if (bootstrap.getCheckinHistory() != null) {
            Set<Long> publishedPoiIds = catalogFoundationService.listPublishedPois(null, null, null, null).stream()
                    .map(Poi::getId)
                    .collect(Collectors.toSet());
            for (UserBootstrapCheckinRequest item : bootstrap.getCheckinHistory()) {
                if (item == null || item.getPoiId() == null || !publishedPoiIds.contains(item.getPoiId())) {
                    continue;
                }
                UserCheckin checkin = new UserCheckin();
                checkin.setUserId(profile.getId());
                checkin.setPoiId(item.getPoiId());
                checkin.setTriggerMode(normalizeTriggerMode(item.getTriggerMode()));
                checkin.setCheckedAt(parseDateTime(item.getCheckedAt()));
                userCheckinMapper.insert(checkin);
            }
        }

        importRewardRedemptions(profile.getId(), bootstrap.getRedeemedRewardIds());
    }

    private void importRewardRedemptions(Long userId, Collection<Long> rewardIds) {
        Set<Long> uniqueRewardIds = uniqueIds(rewardIds);
        if (uniqueRewardIds.isEmpty()) {
            return;
        }
        Map<Long, Reward> rewardsById = rewardMapper.selectBatchIds(new ArrayList<>(uniqueRewardIds)).stream()
                .map(Reward.class::cast)
                .collect(Collectors.toMap(Reward::getId, reward -> reward, (left, right) -> left, LinkedHashMap::new));
        LocalDateTime importedAt = LocalDateTime.now();
        for (Long rewardId : uniqueRewardIds) {
            Reward reward = rewardsById.get(rewardId);
            if (reward == null) {
                continue;
            }
            RewardRedemption redemption = new RewardRedemption();
            redemption.setUserId(userId);
            redemption.setRewardId(rewardId);
            redemption.setRedemptionStatus("imported");
            redemption.setStampCostSnapshot(intValue(reward.getStampCost()));
            redemption.setQrCode("IMPORTED-" + userId + "-" + rewardId);
            redemption.setRedeemedAt(importedAt);
            rewardRedemptionMapper.insert(redemption);

            if (intValue(reward.getInventoryTotal()) > 0) {
                rewardMapper.update(null, new LambdaUpdateWrapper<Reward>()
                        .eq(Reward::getId, rewardId)
                        .lt(Reward::getInventoryRedeemed, intValue(reward.getInventoryTotal()))
                        .setSql("inventory_redeemed = inventory_redeemed + 1"));
            }
        }
    }

    private Stamp selectStamp(Poi poi, Set<Long> collectedStampIds) {
        List<Stamp> stamps = catalogFoundationService.listPublishedStamps();
        return stamps.stream()
                .filter(stamp -> Objects.equals(stamp.getRelatedPoiId(), poi.getId()))
                .findFirst()
                .or(() -> stamps.stream()
                        .filter(stamp -> Objects.equals(stamp.getRelatedStorylineId(), poi.getStorylineId()) && !collectedStampIds.contains(stamp.getId()))
                        .findFirst())
                .or(() -> stamps.stream().filter(stamp -> !collectedStampIds.contains(stamp.getId())).findFirst())
                .orElseGet(() -> stamps.isEmpty() ? null : stamps.get(0));
    }

    private StoryChapter nextChapter(Long storylineId, Set<Long> completedChapterIds) {
        if (storylineId == null) {
            return null;
        }
        return catalogFoundationService.listPublishedStoryChapters(List.of(storylineId)).stream()
                .filter(chapter -> !completedChapterIds.contains(chapter.getId()))
                .findFirst()
                .orElse(null);
    }

    private int experienceGain(String difficulty) {
        if ("hard".equalsIgnoreCase(difficulty)) {
            return 50;
        }
        if ("medium".equalsIgnoreCase(difficulty)) {
            return 35;
        }
        return 25;
    }

    private String normalizeTriggerMode(String triggerMode) {
        String mode = StringUtils.hasText(triggerMode) ? triggerMode.trim().toLowerCase() : "gps";
        if (!List.of("gps", "manual", "mock").contains(mode)) {
            throw new BusinessException(4005, "Unsupported triggerMode");
        }
        return mode;
    }

    private List<String> unlockedCities(UserProfile profile) {
        Set<Long> cityIds = new LinkedHashSet<>();
        if (profile.getCurrentCityId() != null) {
            cityIds.add(profile.getCurrentCityId());
        }
        List<UserCheckin> checkins = userCheckinMapper.selectList(new LambdaQueryWrapper<UserCheckin>()
                .eq(UserCheckin::getUserId, profile.getId()));
        if (!checkins.isEmpty()) {
            Map<Long, Poi> poisById = poiMapper.selectBatchIds(checkins.stream().map(UserCheckin::getPoiId).distinct().toList()).stream()
                    .map(Poi.class::cast)
                    .collect(Collectors.toMap(Poi::getId, poi -> poi, (left, right) -> left, LinkedHashMap::new));
            for (UserCheckin checkin : checkins) {
                Poi poi = poisById.get(checkin.getPoiId());
                if (poi != null && poi.getCityId() != null) {
                    cityIds.add(poi.getCityId());
                }
            }
        }
        if (cityIds.isEmpty()) {
            return Collections.emptyList();
        }
        return cityMapper.selectBatchIds(new ArrayList<>(cityIds)).stream()
                .map(City.class::cast)
                .map(City::getCode)
                .filter(StringUtils::hasText)
                .toList();
    }

    private List<UserCheckinHistoryItemResponse> history(Long userId, String locale, Set<Long> collectedStampIds) {
        List<UserCheckin> items = userCheckinMapper.selectList(new LambdaQueryWrapper<UserCheckin>()
                .eq(UserCheckin::getUserId, userId)
                .orderByDesc(UserCheckin::getCheckedAt)
                .orderByDesc(UserCheckin::getId)
                .last("LIMIT 20"));
        if (items.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Long, Poi> poisById = poiMapper.selectBatchIds(items.stream().map(UserCheckin::getPoiId).distinct().toList()).stream()
                .map(Poi.class::cast)
                .collect(Collectors.toMap(Poi::getId, poi -> poi, (left, right) -> left, LinkedHashMap::new));
        return items.stream()
                .map(item -> {
                    Poi poi = poisById.get(item.getPoiId());
                    Stamp stamp = poi == null ? null : selectStamp(poi, collectedStampIds);
                    return UserCheckinHistoryItemResponse.builder()
                            .poiId(item.getPoiId())
                            .poiName(poi == null ? "Unknown POI" : localizedContentSupport.resolveText(locale, poi.getNameZh(), poi.getNameEn(), poi.getNameZht(), poi.getNamePt()))
                            .stampId(stamp == null ? null : stamp.getId())
                            .stampName(stamp == null ? "" : localizedContentSupport.resolveText(locale, stamp.getNameZh(), stamp.getNameEn(), stamp.getNameZht(), stamp.getNamePt()))
                            .experienceGained(poi == null ? 0 : experienceGain(poi.getDifficulty()))
                            .triggerMode(item.getTriggerMode())
                            .unlockedStorylineId(poi == null ? null : poi.getStorylineId())
                            .checkedAt(item.getCheckedAt())
                            .build();
                })
                .toList();
    }

    private static class ProgressState {
        private Long activeStoryId;
        private final Set<Long> collectedStampIds = new LinkedHashSet<>();
        private final Set<Long> completedStoryIds = new LinkedHashSet<>();
        private final Set<Long> completedChapterIds = new LinkedHashSet<>();
    }

    private static class TitleBundle {
        private final String zh;
        private final String en;
        private final String zht;
        private final String pt;

        private TitleBundle(String zh, String en, String zht) {
            this(zh, en, zht, en);
        }

        private TitleBundle(String zh, String en, String zht, String pt) {
            this.zh = zh;
            this.en = en;
            this.zht = zht;
            this.pt = pt;
        }
    }
    
    @Override
    public TestModeResponse getTestMode(Long userId) {
        TestAccount testAccount = testAccountService.getByUserId(userId);
        
        if (testAccount == null) {
            return TestModeResponse.builder()
                    .isTestAccount(false)
                    .mockEnabled(false)
                    .build();
        }
        
        return TestModeResponse.builder()
                .isTestAccount(true)
                .testGroup(testAccount.getTestGroup())
                .mockEnabled(testAccount.getMockEnabled() != null && testAccount.getMockEnabled())
                .mockLatitude(testAccount.getMockLatitude())
                .mockLongitude(testAccount.getMockLongitude())
                .mockPoiId(testAccount.getMockPoiId())
                .build();
    }
}
