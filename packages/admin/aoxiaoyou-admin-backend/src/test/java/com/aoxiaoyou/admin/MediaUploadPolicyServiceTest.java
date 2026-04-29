package com.aoxiaoyou.admin;

import com.aoxiaoyou.admin.dto.response.AdminMediaPolicySettingsResponse;
import com.aoxiaoyou.admin.entity.SysConfig;
import com.aoxiaoyou.admin.entity.SysAdmin;
import com.aoxiaoyou.admin.mapper.SysConfigMapper;
import com.aoxiaoyou.admin.media.MediaUploadPolicyService;
import com.aoxiaoyou.admin.media.ResolvedMediaUploadPolicy;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MediaUploadPolicyServiceTest {

    @Mock
    private SysConfigMapper sysConfigMapper;

    @Mock
    private JdbcTemplate jdbcTemplate;

    private MediaUploadPolicyService service;

    @BeforeEach
    void setUp() {
        service = new MediaUploadPolicyService(sysConfigMapper, jdbcTemplate, new ObjectMapper());
    }

    @Test
    void getSettingsReturnsDefaultsWhenNoStoredConfigExists() {
        when(sysConfigMapper.selectOne(any())).thenReturn(null);

        AdminMediaPolicySettingsResponse settings = service.getSettings();

        assertThat(settings.getMaxBatchCount()).isEqualTo(50);
        assertThat(settings.getImage().getPreferredPolicyCode()).isEqualTo("compressed");
        assertThat(settings.getVideo().getPreferredPolicyCode()).isEqualTo("passthrough");
    }

    @Test
    void resolvePolicyDowngradesLosslessWhenUploaderDoesNotHavePermission() throws Exception {
        when(sysConfigMapper.selectOne(any())).thenReturn(config("""
                {
                  "maxBatchCount": 20,
                  "maxBatchTotalBytes": 104857600,
                  "image": {
                    "maxFileSizeBytes": 5242880,
                    "preferredPolicyCode": "lossless",
                    "qualityPercent": 92,
                    "maxWidthPx": 1920,
                    "maxHeightPx": 1920,
                    "preserveMetadata": true,
                    "note": "Prefer lossless when uploader can use it"
                  }
                }
                """));

        SysAdmin admin = new SysAdmin();
        admin.setAllowLosslessUpload(false);
        MockMultipartFile file = new MockMultipartFile("file", "macau.png", "image/png", new byte[1024]);

        ResolvedMediaUploadPolicy policy = service.resolvePolicy("image", file, admin);

        assertThat(policy.getRequestedPolicyCode()).isEqualTo("lossless");
        assertThat(policy.getEffectivePolicyCode()).isEqualTo("image-compressed");
        assertThat(policy.getUploaderAllowsLossless()).isFalse();
        assertThat(policy.getSnapshot()).containsEntry("uploaderAllowsLossless", false);
    }

    @Test
    void resolvePolicyKeepsLosslessWhenUploaderHasPermission() throws Exception {
        when(sysConfigMapper.selectOne(any())).thenReturn(config("""
                {
                  "image": {
                    "maxFileSizeBytes": 5242880,
                    "preferredPolicyCode": "lossless",
                    "qualityPercent": 92,
                    "maxWidthPx": 1920,
                    "maxHeightPx": 1920,
                    "preserveMetadata": true,
                    "note": "Prefer lossless when uploader can use it"
                  }
                }
                """));

        SysAdmin admin = new SysAdmin();
        admin.setAllowLosslessUpload(true);
        MockMultipartFile file = new MockMultipartFile("file", "macau.png", "image/png", new byte[1024]);

        ResolvedMediaUploadPolicy policy = service.resolvePolicy("image", file, admin);

        assertThat(policy.getEffectivePolicyCode()).isEqualTo("lossless");
        assertThat(policy.getUploaderAllowsLossless()).isTrue();
    }

    private SysConfig config(String json) {
        SysConfig config = new SysConfig();
        config.setConfigKey("media.upload.policy");
        config.setConfigValue(json);
        return config;
    }
}
