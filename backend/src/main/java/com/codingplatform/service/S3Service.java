package com.codingplatform.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

/**
 * Service for S3 operations.
 * Fetches testcase files from S3 bucket.
 * Uses IAM Role authentication (no credentials in code).
 */
@Service
public class S3Service {

    private static final Logger logger = LoggerFactory.getLogger(S3Service.class);

    private final S3Client s3Client;
    private final String bucketName;

    public S3Service(S3Client s3Client, @Qualifier("s3BucketName") String bucketName) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
    }

    /**
     * Fetch file content from S3.
     *
     * @param s3Key the S3 object key (path)
     * @return file content as string
     * @throws S3ServiceException if fetch fails
     */
    public String getFileContent(String s3Key) {
        logger.debug("Fetching S3 object: s3://{}/{}", bucketName, s3Key);
        
        try {
            GetObjectRequest request = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();

            ResponseInputStream<GetObjectResponse> response = s3Client.getObject(request);
            
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(response, StandardCharsets.UTF_8))) {
                return reader.lines().collect(Collectors.joining("\n"));
            }
            
        } catch (S3Exception e) {
            logger.error("S3 error fetching {}: {}", s3Key, e.awsErrorDetails().errorMessage());
            throw new S3ServiceException("Failed to fetch from S3: " + s3Key, e);
        } catch (Exception e) {
            logger.error("Error fetching S3 object {}: {}", s3Key, e.getMessage());
            throw new S3ServiceException("Failed to read S3 object: " + s3Key, e);
        }
    }

    /**
     * Check if an S3 object exists.
     */
    public boolean objectExists(String s3Key) {
        try {
            s3Client.headObject(builder -> builder.bucket(bucketName).key(s3Key));
            return true;
        } catch (S3Exception e) {
            return false;
        }
    }

    /**
     * Get the bucket name.
     */
    public String getBucketName() {
        return bucketName;
    }

    /**
     * Custom exception for S3 operations.
     */
    public static class S3ServiceException extends RuntimeException {
        public S3ServiceException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}

