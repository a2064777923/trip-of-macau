package com.aoxiaoyou.admin;

import com.aoxiaoyou.admin.common.exception.BusinessException;
import com.aoxiaoyou.admin.common.spatial.CoordinateNormalizationService;
import com.aoxiaoyou.admin.dto.request.AdminPoiUpsertRequest;
import com.aoxiaoyou.admin.entity.City;
import com.aoxiaoyou.admin.entity.SubMap;
import com.aoxiaoyou.admin.mapper.CityMapper;
import com.aoxiaoyou.admin.mapper.PoiMapper;
import com.aoxiaoyou.admin.mapper.StoryLineMapper;
import com.aoxiaoyou.admin.mapper.SubMapMapper;
import com.aoxiaoyou.admin.service.AdminSpatialAssetLinkService;
import com.aoxiaoyou.admin.service.impl.AdminPoiServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminPoiServiceImplTest {

    @Mock
    private PoiMapper poiMapper;

    @Mock
    private StoryLineMapper storyLineMapper;

    @Mock
    private CityMapper cityMapper;

    @Mock
    private SubMapMapper subMapMapper;

    @Mock
    private AdminSpatialAssetLinkService adminSpatialAssetLinkService;

    @Test
    void rejectsSubMapFromDifferentCity() {
        AdminPoiServiceImpl service = new AdminPoiServiceImpl(
                poiMapper,
                storyLineMapper,
                cityMapper,
                subMapMapper,
                new CoordinateNormalizationService(),
                adminSpatialAssetLinkService
        );

        City city = new City();
        city.setId(1L);
        when(cityMapper.selectById(1L)).thenReturn(city);

        SubMap subMap = new SubMap();
        subMap.setId(10L);
        subMap.setCityId(2L);
        when(subMapMapper.selectById(10L)).thenReturn(subMap);

        AdminPoiUpsertRequest request = new AdminPoiUpsertRequest();
        request.setCityId(1L);
        request.setSubMapId(10L);
        request.setCode("poi-1");
        request.setNameZh("测试点");
        request.setLatitude(BigDecimal.valueOf(22.1));
        request.setLongitude(BigDecimal.valueOf(113.5));

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Sub-map");
    }
}
