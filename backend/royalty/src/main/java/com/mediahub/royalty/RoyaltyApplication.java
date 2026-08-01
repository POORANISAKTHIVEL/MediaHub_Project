package com.mediahub.royalty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Arrays;

@SpringBootApplication
public class RoyaltyApplication {

    private static final Logger logger = LoggerFactory.getLogger(RoyaltyApplication.class);

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(RoyaltyApplication.class, args);
        Environment env = context.getEnvironment();
        
        logger.info("=====================================");
        logger.info("Royalty and Revenue Management Module");
        logger.info("=====================================");
        logger.info("Application '{}' is running!", env.getProperty("spring.application.name"));
        logger.info("Active profiles: {}", Arrays.toString(env.getActiveProfiles()));
        logger.info("Database URL: {}", env.getProperty("spring.datasource.url"));
        logger.info("Server port: {}", env.getProperty("server.port"));
        logger.info("=====================================");
    }
}
