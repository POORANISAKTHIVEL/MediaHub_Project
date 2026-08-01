package com.mediahub;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@DisplayName("MediaHub Combined Application Context Test")
class MediaHubApplicationTests {

    @Test
    @DisplayName("TC-APP-01: Spring Boot context loads with both modules")
    void contextLoads() {
    }
}
