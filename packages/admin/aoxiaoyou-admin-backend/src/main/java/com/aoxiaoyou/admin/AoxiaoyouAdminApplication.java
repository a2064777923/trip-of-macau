package com.aoxiaoyou.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.aoxiaoyou.admin.mapper")
public class AoxiaoyouAdminApplication {
    public static void main(String[] args) {
        SpringApplication.run(AoxiaoyouAdminApplication.class, args);
    }
}
