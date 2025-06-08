package com.example.banking.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Spring Boot application class for the Banking REST API.
 * This application wraps the existing banking-core JAR and provides REST endpoints.
 */
@SpringBootApplication
public class BankingApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(BankingApiApplication.class, args);
    }
}
