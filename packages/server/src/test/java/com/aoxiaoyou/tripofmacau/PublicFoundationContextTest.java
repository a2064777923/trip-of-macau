package com.aoxiaoyou.tripofmacau;

import com.aoxiaoyou.tripofmacau.mapper.AppRuntimeSettingMapper;
import com.aoxiaoyou.tripofmacau.mapper.CityMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("local")
class PublicFoundationContextTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private CityMapper cityMapper;

    @Autowired
    private AppRuntimeSettingMapper appRuntimeSettingMapper;

    @Test
    void foundationMappersAreAvailableInContext() {
        assertThat(applicationContext).isNotNull();
        assertThat(cityMapper).isNotNull();
        assertThat(appRuntimeSettingMapper).isNotNull();
        assertThat(applicationContext.getBean(CityMapper.class)).isSameAs(cityMapper);
        assertThat(applicationContext.getBean(AppRuntimeSettingMapper.class)).isSameAs(appRuntimeSettingMapper);
    }
}
