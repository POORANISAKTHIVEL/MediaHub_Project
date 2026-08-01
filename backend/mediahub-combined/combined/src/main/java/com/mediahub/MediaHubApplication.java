package com.mediahub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.mediahub")
public class MediaHubApplication {
    public static void main(String[] args) {
        SpringApplication.run(MediaHubApplication.class, args);
    }
}
