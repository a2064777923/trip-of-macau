package com.aoxiaoyou.tripofmacau;

import com.aoxiaoyou.tripofmacau.common.config.WechatAuthProperties;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@MapperScan("com.aoxiaoyou.tripofmacau.mapper")
@EnableConfigurationProperties(WechatAuthProperties.class)
public class TripOfMacauServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(TripOfMacauServerApplication.class, args);
    }
}
