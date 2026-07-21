package com.farmerassistant;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableCaching
@EnableScheduling
@EnableAsync
@Slf4j
public class FarmerAssistantApplication {

    public static void main(String[] args) {
        SpringApplication.run(FarmerAssistantApplication.class, args);
        log.info("============================================================");
        log.info("  AI Farmer Assistant Platform started successfully!");
        log.info("  Swagger UI: http://localhost:8080/swagger-ui.html");
        log.info("  API Docs  : http://localhost:8080/api-docs");
        log.info("============================================================");
    }
}
