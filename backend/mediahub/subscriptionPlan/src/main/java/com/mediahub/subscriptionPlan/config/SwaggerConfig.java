package com.mediahub.subscriptionPlan.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {

        return new OpenAPI()
                .info(new Info()
                        .title("MediaHub Subscription Service")
                        .version("1.0")
                        .description("Subscription & Plan Management Microservice")
                        .contact(new Contact()
                                .name("MediaHub Team")
                                .email("mediahub@gmail.com")));
    }
}