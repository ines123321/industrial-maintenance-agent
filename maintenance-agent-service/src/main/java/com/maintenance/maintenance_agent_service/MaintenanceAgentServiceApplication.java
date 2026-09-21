package com.maintenance.maintenance_agent_service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MaintenanceAgentServiceApplication {

    @Value("${GROQ_API_KEY}")
    private String groqApiKey;

    public static void main(String[] args) {
        SpringApplication.run(MaintenanceAgentServiceApplication.class, args);
    }

    @PostConstruct
    public void testKey() {
        System.out.println("GROQ_API_KEY loaded: " +
                (groqApiKey != null && !groqApiKey.isBlank()));
    }
}

