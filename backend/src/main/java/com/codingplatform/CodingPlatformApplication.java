package com.codingplatform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * CodeNexus - Online Coding Platform
 * 
 * A LeetCode-style coding platform with:
 * - RDS MySQL for problem metadata
 * - S3 for testcase storage
 * - Multi-language code execution
 */
@SpringBootApplication
public class CodingPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(CodingPlatformApplication.class, args);
    }
}
