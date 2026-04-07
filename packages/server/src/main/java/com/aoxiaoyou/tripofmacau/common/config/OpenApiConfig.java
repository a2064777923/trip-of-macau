package com.aoxiaoyou.tripofmacau.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI tripOfMacauOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Trip of Macau API")
                        .description("澳小遊小程序后端 MVP 接口文档")
                        .version("0.1.0")
                        .contact(new Contact().name("Trip of Macau Backend"))
                        .license(new License().name("Proprietary")));
    }
}
