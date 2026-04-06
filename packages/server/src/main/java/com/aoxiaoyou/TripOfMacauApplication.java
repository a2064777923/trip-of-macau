package com.aoxiaoyou;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.aoxiaoyou.mapper")
public class TripOfMacauApplication {
    public static void main(String[] args) {
        SpringApplication.run(TripOfMacauApplication.class, args);
    }
}
