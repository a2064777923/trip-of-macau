package com.aoxiaoyou.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.aoxiaoyou.admin.common.api.PageResponse;
import com.aoxiaoyou.admin.dto.request.AdminCityUpsertRequest;
import com.aoxiaoyou.admin.dto.response.AdminCityResponse;
import com.aoxiaoyou.admin.entity.City;
import com.aoxiaoyou.admin.mapper.CityMapper;
import com.aoxiaoyou.admin.service.AdminCityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AdminCityServiceImpl implements AdminCityService {

    private final CityMapper cityMapper;

    @Override
    public PageResponse<AdminCityResponse> pageCities(long pageNum, long pageSize, String keyword, String status) {
        LambdaQueryWrapper<City> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(City::getNameZh, keyword).or().like(City::getCode, keyword);
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq(City::getStatus, status);
        }
        wrapper.orderByAsc(City::getSortOrder).orderByDesc(City::getId);

        Page<City> page = cityMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        Page<AdminCityResponse> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(page.getRecords().stream().map(this::toListItem).toList());
        return PageResponse.of(result);
    }

    @Override
    public AdminCityResponse getCityDetail(Long id) {
        City city = cityMapper.selectById(id);
        return toDetail(city);
    }

    @Override
    public AdminCityResponse createCity(AdminCityUpsertRequest request) {
        City city = new City();
        applyRequest(city, request.getUpsert());
        city.setPublishedAt(null);
        city.setStatus("0");
        cityMapper.insert(city);
        return toDetail(city);
    }

    @Override
    public AdminCityResponse updateCity(Long id, AdminCityUpsertRequest request) {
        City existing = cityMapper.selectById(id);
        if (existing == null) throw new RuntimeException("城市不存在");
        applyRequest(existing, request.getUpsert());
        cityMapper.updateById(existing);
        return toDetail(existing);
    }

    @Override
    public AdminCityResponse publishCity(Long id) {
        City city = cityMapper.selectById(id);
        if (city == null) throw new RuntimeException("城市不存在");
        city.setStatus("1");
        city.setPublishedAt(LocalDateTime.now());
        cityMapper.updateById(city);
        return toDetail(city);
    }

    private void applyRequest(City city, AdminCityUpsertRequest.Upsert r) {
        if (r == null) {
            return;
        }
        if (r.getCode() != null) city.setCode(r.getCode());
        if (r.getNameZh() != null) city.setNameZh(r.getNameZh());
        if (r.getNameEn() != null) city.setNameEn(r.getNameEn());
        if (r.getNameZht() != null) city.setNameZht(r.getNameZht());
        if (r.getCountryCode() != null) city.setCountryCode(r.getCountryCode());
        if (r.getCenterLat() != null) city.setCenterLat(java.math.BigDecimal.valueOf(r.getCenterLat()));
        if (r.getCenterLng() != null) city.setCenterLng(java.math.BigDecimal.valueOf(r.getCenterLng()));
        if (r.getDefaultZoom() != null) city.setDefaultZoom(r.getDefaultZoom());
        if (r.getUnlockType() != null) city.setUnlockType(r.getUnlockType());
        if (r.getCoverImageUrl() != null) city.setCoverImageUrl(r.getCoverImageUrl());
        if (r.getBannerUrl() != null) city.setBannerUrl(r.getBannerUrl());
        if (r.getDescriptionZh() != null) city.setDescriptionZh(r.getDescriptionZh());
        if (r.getSortOrder() != null) city.setSortOrder(r.getSortOrder());
    }

    private AdminCityResponse toListItem(City c) {
        return AdminCityResponse.builder()
                .id(c.getId()).code(c.getCode()).nameZh(c.getNameZh())
                .nameEn(c.getNameEn()).nameZht(c.getNameZht())
                .countryCode(c.getCountryCode())
                .centerLat(c.getCenterLat()).centerLng(c.getCenterLng())
                .defaultZoom(c.getDefaultZoom())
                .unlockType(c.getUnlockType())
                .coverImageUrl(c.getCoverImageUrl())
                .status(c.getStatus()).sortOrder(c.getSortOrder())
                .build();
    }

    private AdminCityResponse toDetail(City c) {
        AdminCityResponse resp = toListItem(c);
        resp.setBannerUrl(c.getBannerUrl());
        resp.setDescriptionZh(c.getDescriptionZh());
        return resp;
    }
}
