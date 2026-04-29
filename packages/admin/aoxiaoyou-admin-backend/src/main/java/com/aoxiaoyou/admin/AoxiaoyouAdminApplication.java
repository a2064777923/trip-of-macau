package com.aoxiaoyou.admin;

import com.aoxiaoyou.admin.ai.config.AiCapabilityProperties;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@MapperScan("com.aoxiaoyou.admin.mapper")
@EnableConfigurationProperties({AiCapabilityProperties.class})
@SpringBootApplication
public class AoxiaoyouAdminApplication {
    public static void main(String[] args) {
        SpringApplication.run(AoxiaoyouAdminApplication.class, args);
    }
}

