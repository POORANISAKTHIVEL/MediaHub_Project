package com.mediahub.eureka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * MediaHub Eureka Service Discovery Server
 *
 * All microservices register here on startup.
 * Dashboard: http://localhost:8761
 *
 * Registered Services:
 *  - mediahub-iam            (8091)
 *  - mediahub-content        (8093)
 *  - mediahub-subscription   (8086)
 *  - mediahub-licensing      (8083)
 *  - mediahub-editorial      (9097)
 *  - mediahub-royalty        (8045)
 *  - mediahub-notification   (8085)
 *  - mediahub-analytics      (8098)
 *  - mediahub-gateway        (8094)
 */
@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
        System.out.println("=========================================");
        System.out.println(" MediaHub Eureka Server is running!");
        System.out.println(" Dashboard: http://localhost:8761");
        System.out.println("=========================================");
    }
}
