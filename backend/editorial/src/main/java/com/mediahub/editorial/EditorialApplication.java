package com.mediahub.editorial;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EditorialApplication {

    private static final Logger log = LoggerFactory.getLogger(EditorialApplication.class);

    public static void main(String[] args) {
        log.info("Starting EditorialApplication");
        SpringApplication.run(EditorialApplication.class, args);
        log.info("EditorialApplication started successfully");
    }
}
