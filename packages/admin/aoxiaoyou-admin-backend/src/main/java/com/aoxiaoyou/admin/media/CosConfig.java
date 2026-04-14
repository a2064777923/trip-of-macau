package com.aoxiaoyou.admin.media;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.http.HttpProtocol;
import com.qcloud.cos.region.Region;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration
public class CosConfig {

    @Bean(destroyMethod = "shutdown")
    @ConditionalOnProperty(prefix = "app.cos", name = "enabled", havingValue = "true")
    public COSClient cosClient(CosProperties cosProperties) {
        if (!StringUtils.hasText(cosProperties.getSecretId())
                || !StringUtils.hasText(cosProperties.getSecretKey())
                || !StringUtils.hasText(cosProperties.getBucketName())
                || !StringUtils.hasText(cosProperties.getRegion())) {
            throw new IllegalStateException("COS is enabled but required COS configuration is incomplete.");
        }

        COSCredentials credentials = new BasicCOSCredentials(cosProperties.getSecretId(), cosProperties.getSecretKey());
        ClientConfig clientConfig = new ClientConfig(new Region(cosProperties.getRegion()));
        clientConfig.setHttpProtocol(HttpProtocol.https);
        clientConfig.setConnectionTimeout(cosProperties.getConnectionTimeoutMs());
        clientConfig.setSocketTimeout(cosProperties.getSocketTimeoutMs());
        return new COSClient(credentials, clientConfig);
    }
}
