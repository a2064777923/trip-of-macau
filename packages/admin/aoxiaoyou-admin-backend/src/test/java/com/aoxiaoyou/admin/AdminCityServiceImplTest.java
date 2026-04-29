package com.aoxiaoyou.admin;

import com.aoxiaoyou.admin.common.exception.BusinessException;
import com.aoxiaoyou.admin.common.spatial.CoordinateNormalizationService;
import com.aoxiaoyou.admin.dto.request.AdminCityUpsertRequest;
import com.aoxiaoyou.admin.entity.City;
import com.aoxiaoyou.admin.mapper.CityMapper;
import com.aoxiaoyou.admin.mapper.SubMapMapper;
import com.aoxiaoyou.admin.service.AdminSpatialAssetLinkService;
import com.aoxiaoyou.admin.service.impl.AdminCityServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminCityServiceImplTest {

    @Mock
    private CityMapper cityMapper;

    @Mock
    private SubMapMapper subMapMapper;

    @Mock
    private AdminSpatialAssetLinkService adminSpatialAssetLinkService;

    @Test
    void updateCityKeepsExistingLifecycleStatus() {
        AdminCityServiceImpl service = new AdminCityServiceImpl(
                cityMapper,
                subMapMapper,
                new CoordinateNormalizationService(),
                adminSpatialAssetLinkService
        );

        when(subMapMapper.selectList(any())).thenReturn(List.of());
        when(adminSpatialAssetLinkService.listLinks(anyString(), anyLong())).thenReturn(List.of());

        LocalDateTime publishedAt = LocalDateTime.of(2026, 4, 19, 14, 0);
        City stored = new City();
        stored.setId(3L);
        stored.setCode("taipa");
        stored.setNameZh("氹仔旧城区");
        stored.setStatus("published");
        stored.setPublishedAt(publishedAt);
        when(cityMapper.selectById(3L)).thenReturn(stored);
        doAnswer(invocation -> {
            City updated = invocation.getArgument(0);
            stored.setCode(updated.getCode());
            stored.setNameZh(updated.getNameZh());
            stored.setStatus(updated.getStatus());
            stored.setPublishedAt(updated.getPublishedAt());
            return 1;
        }).when(cityMapper).updateById(any(City.class));

        AdminCityUpsertRequest request = new AdminCityUpsertRequest();
        request.getUpsert().setCode("taipa-updated");
        request.getUpsert().setNameZh("氹仔旧城区更新");

        var response = service.updateCity(3L, request);

        assertThat(stored.getStatus()).isEqualTo("published");
        assertThat(stored.getPublishedAt()).isEqualTo(publishedAt);
        assertThat(response.getStatus()).isEqualTo("published");
        assertThat(response.getPublishedAt()).isEqualTo(publishedAt);
    }

    @Test
    void updateCityStatusOnlyAcceptsOperableStates() {
        AdminCityServiceImpl service = new AdminCityServiceImpl(
                cityMapper,
                subMapMapper,
                new CoordinateNormalizationService(),
                adminSpatialAssetLinkService
        );

        when(subMapMapper.selectList(any())).thenReturn(List.of());
        when(adminSpatialAssetLinkService.listLinks(anyString(), anyLong())).thenReturn(List.of());

        City stored = new City();
        stored.setId(4L);
        stored.setCode("taipa");
        stored.setNameZh("氹仔旧城区");
        stored.setStatus("published");
        stored.setPublishedAt(LocalDateTime.of(2026, 4, 19, 15, 0));
        when(cityMapper.selectById(4L)).thenReturn(stored);
        doAnswer(invocation -> {
            City updated = invocation.getArgument(0);
            stored.setStatus(updated.getStatus());
            stored.setPublishedAt(updated.getPublishedAt());
            return 1;
        }).when(cityMapper).updateById(any(City.class));

        var archived = service.updateCityStatus(4L, "archived");

        assertThat(stored.getStatus()).isEqualTo("archived");
        assertThat(stored.getPublishedAt()).isNull();
        assertThat(archived.getStatus()).isEqualTo("archived");
        assertThat(archived.getPublishedAt()).isNull();

        assertThatThrownBy(() -> service.updateCityStatus(4L, "draft"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("not manually operable");
    }
}
