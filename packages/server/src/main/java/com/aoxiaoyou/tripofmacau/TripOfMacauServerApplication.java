package com.aoxiaoyou.tripofmacau;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.aoxiaoyou.tripofmacau.mapper")
public class TripOfMacauServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(TripOfMacauServerApplication.class, args);
    }
}
