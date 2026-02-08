package com.codingplatform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the Coding Platform Backend.
 * 
 * This application serves as the MANAGER component of the coding platform.
 * It receives code submissions from clients and forwards them to the
 * isolated Judge Service for execution and evaluation.
 * 
 * IMPORTANT: This backend NEVER executes user code directly.
 * All code execution happens in the separate Judge Service.
 */
@SpringBootApplication
public class CodingPlatformApplication {

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("  CODING PLATFORM BACKEND");
        System.out.println("========================================");
        System.out.println("Starting backend service...");
        System.out.println("Backend Port: 8080");
        System.out.println("Judge Service: http://localhost:5000");
        System.out.println("========================================");
        
        SpringApplication.run(CodingPlatformApplication.class, args);
    }
}

