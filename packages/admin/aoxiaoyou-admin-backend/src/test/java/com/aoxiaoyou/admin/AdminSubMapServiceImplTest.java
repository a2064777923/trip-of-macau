package com.aoxiaoyou.admin;

import com.aoxiaoyou.admin.dto.request.AdminSubMapUpsertRequest;
import com.aoxiaoyou.admin.entity.City;
import com.aoxiaoyou.admin.entity.SubMap;
import com.aoxiaoyou.admin.mapper.CityMapper;
import com.aoxiaoyou.admin.mapper.SubMapMapper;
import com.aoxiaoyou.admin.service.AdminSpatialAssetLinkService;
import com.aoxiaoyou.admin.service.impl.AdminSubMapServiceImpl;
import com.aoxiaoyou.admin.common.spatial.CoordinateNormalizationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminSubMapServiceImplTest {

    @Mock
    private SubMapMapper subMapMapper;

    @Mock
    private CityMapper cityMapper;

    @Mock
    private AdminSpatialAssetLinkService adminSpatialAssetLinkService;

    @Test
    void createSubMapNormalizesCoordinatesAndSyncsAttachments() {
        AdminSubMapServiceImpl service = new AdminSubMapServiceImpl(
                subMapMapper,
                cityMapper,
                new CoordinateNormalizationService(),
                adminSpatialAssetLinkService
        );

        City city = new City();
        city.setId(1L);
        city.setCode("macau");
        city.setNameZh("澳门");
        when(cityMapper.selectById(1L)).thenReturn(city);

        SubMap stored = new SubMap();
        doAnswer(invocation -> {
            SubMap inserted = invocation.getArgument(0);
            inserted.setId(88L);
            stored.setId(88L);
            stored.setCityId(inserted.getCityId());
            stored.setCode(inserted.getCode());
            stored.setNameZh(inserted.getNameZh());
            stored.setSourceCoordinateSystem(inserted.getSourceCoordinateSystem());
            stored.setSourceCenterLat(inserted.getSourceCenterLat());
            stored.setSourceCenterLng(inserted.getSourceCenterLng());
            stored.setCenterLat(inserted.getCenterLat());
            stored.setCenterLng(inserted.getCenterLng());
            stored.setStatus(inserted.getStatus());
            return 1;
        }).when(subMapMapper).insert(any(SubMap.class));
        when(subMapMapper.selectById(88L)).thenReturn(stored);

        AdminSubMapUpsertRequest request = new AdminSubMapUpsertRequest();
        request.setCityId(1L);
        request.setCode("macau-peninsula");
        request.setNameZh("澳门半岛");
        request.setSourceCoordinateSystem("WGS84");
        request.setSourceCenterLat(22.1987);
        request.setSourceCenterLng(113.5439);

        var response = service.createSubMap(request);

        assertThat(response.getSourceCoordinateSystem()).isEqualTo("WGS84");
        assertThat(response.getSourceCenterLat()).isNotNull();
        assertThat(response.getCenterLat()).isNotEqualByComparingTo(response.getSourceCenterLat());
        verify(adminSpatialAssetLinkService).syncLinks("sub_map", 88L, request.getAttachments());

        ArgumentCaptor<SubMap> captor = ArgumentCaptor.forClass(SubMap.class);
        verify(subMapMapper).insert(captor.capture());
        assertThat(captor.getValue().getCenterLng()).isNotEqualByComparingTo(captor.getValue().getSourceCenterLng());
    }
}
