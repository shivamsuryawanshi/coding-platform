package com.codingplatform.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

/**
 * AWS S3 Configuration.
 * Uses IAM Role-based authentication (DefaultCredentialsProvider).
 * NO AWS keys in code - credentials come from EC2 instance role.
 */
@Configuration
public class AwsConfig {

    @Value("${aws.region:eu-north-1}")
    private String awsRegion;

    @Value("${aws.s3.bucket:coding-platform-testcases}")
    private String s3Bucket;

    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .region(Region.of(awsRegion))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    @Bean
    public String s3BucketName() {
        return s3Bucket;
    }

    public String getAwsRegion() {
        return awsRegion;
    }

    public String getS3Bucket() {
        return s3Bucket;
    }
}

