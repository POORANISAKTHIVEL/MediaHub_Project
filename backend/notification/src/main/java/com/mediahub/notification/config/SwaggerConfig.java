package com.mediahub.notification.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI notificationAPI() {

        return new OpenAPI()
                .info(new Info()
                        .title("Notification API")
                        .version("1.0")
                        .description("Notification Service"));
    }
}
