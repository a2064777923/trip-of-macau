package com.aoxiaoyou.admin;

import com.aoxiaoyou.admin.common.exception.BusinessException;
import com.aoxiaoyou.admin.dto.request.AdminSpatialAssetLinkUpsertRequest;
import com.aoxiaoyou.admin.entity.ContentAsset;
import com.aoxiaoyou.admin.mapper.ContentAssetLinkMapper;
import com.aoxiaoyou.admin.mapper.ContentAssetMapper;
import com.aoxiaoyou.admin.service.impl.AdminSpatialAssetLinkServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminSpatialAssetLinkServiceImplTest {

    @Mock
    private ContentAssetLinkMapper contentAssetLinkMapper;

    @Mock
    private ContentAssetMapper contentAssetMapper;

    @Test
    void rejectsArchivedAssetsForSpatialLinks() {
        AdminSpatialAssetLinkServiceImpl service = new AdminSpatialAssetLinkServiceImpl(contentAssetLinkMapper, contentAssetMapper);

        ContentAsset archivedAsset = new ContentAsset();
        archivedAsset.setId(9L);
        archivedAsset.setStatus("archived");
        when(contentAssetMapper.selectById(9L)).thenReturn(archivedAsset);
        when(contentAssetLinkMapper.selectList(any())).thenReturn(List.of());

        AdminSpatialAssetLinkUpsertRequest request = new AdminSpatialAssetLinkUpsertRequest();
        request.setAssetId(9L);
        request.setUsageType("gallery");

        assertThatThrownBy(() -> service.syncLinks("poi", 1L, List.of(request)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("draft or published assets");
    }
}
