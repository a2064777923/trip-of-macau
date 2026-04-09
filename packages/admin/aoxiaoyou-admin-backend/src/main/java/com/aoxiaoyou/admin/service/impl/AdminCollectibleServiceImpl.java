package com.aoxiaoyou.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.aoxiaoyou.admin.common.api.PageResponse;
import com.aoxiaoyou.admin.dto.response.CollectibleResponse;
import com.aoxiaoyou.admin.dto.response.BadgeResponse;
import com.aoxiaoyou.admin.entity.Collectible;
import com.aoxiaoyou.admin.entity.Badge;
import com.aoxiaoyou.admin.mapper.CollectibleMapper;
import com.aoxiaoyou.admin.mapper.BadgeMapper;
import com.aoxiaoyou.admin.service.AdminCollectibleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminCollectibleServiceImpl implements AdminCollectibleService {

    private final CollectibleMapper collectibleMapper;
    private final BadgeMapper badgeMapper;

    @Override
    public PageResponse<CollectibleResponse> pageCollectibles(long pageNum, long pageSize, String keyword, String rarity) {
        LambdaQueryWrapper<Collectible> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Collectible::getNameZh, keyword);
        }
        if (rarity != null && !rarity.isEmpty()) {
            wrapper.eq(Collectible::getRarity, rarity);
        }
        wrapper.orderByDesc(Collectible::getId);
        Page<Collectible> page = collectibleMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        Page<CollectibleResponse> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(page.getRecords().stream().map(this::mapCollectible).toList());
        return PageResponse.of(result);
    }

    @Override
    public CollectibleResponse createCollectible(Object request) {
        Collectible c = new Collectible();
        c.setCollectibleCode("item_" + System.currentTimeMillis());
        c.setNameZh("新收集物");
        c.setCollectibleType("item");
        c.setRarity("common");
        c.setStatus("1");
        collectibleMapper.insert(c);
        return mapCollectible(c);
    }

    @Override
    public PageResponse<BadgeResponse> pageBadges(long pageNum, long pageSize) {
        LambdaQueryWrapper<Badge> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Badge::getId);
        Page<Badge> page = badgeMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        Page<BadgeResponse> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(page.getRecords().stream().map(this::mapBadge).toList());
        return PageResponse.of(result);
    }

    @Override
    public BadgeResponse createBadge(Object request) {
        Badge b = new Badge();
        b.setBadgeCode("badge_" + System.currentTimeMillis());
        b.setNameZh("新徽章");
        b.setBadgeType("special");
        b.setRarity("common");
        b.setStatus("1");
        badgeMapper.insert(b);
        return mapBadge(b);
    }

    private CollectibleResponse mapCollectible(Collectible c) {
        return CollectibleResponse.builder()
            .id(c.getId()).collectibleCode(c.getCollectibleCode())
            .nameZh(c.getNameZh()).collectibleType(c.getCollectibleType())
            .rarity(c.getRarity()).imageUrl(c.getImageUrl())
            .seriesId(c.getSeriesId()).acquisitionSource(c.getAcquisitionSource())
            .isRepeatable(c.getIsRepeatable()).isLimited(c.getIsLimited())
            .maxOwnership(c.getMaxOwnership()).status(c.getStatus())
            .build();
    }

    private BadgeResponse mapBadge(Badge b) {
        return BadgeResponse.builder()
            .id(b.getId()).badgeCode(b.getBadgeCode())
            .nameZh(b.getNameZh()).badgeType(b.getBadgeType())
            .rarity(b.getRarity()).isHidden(b.getIsHidden())
            .iconUrl(b.getIconUrl()).imageUrl(b.getImageUrl())
            .status(b.getStatus())
            .build();
    }
}
