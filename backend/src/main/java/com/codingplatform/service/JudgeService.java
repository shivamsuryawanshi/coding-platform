package com.codingplatform.service;

import com.codingplatform.config.JudgeServiceConfig;
import com.codingplatform.dto.JudgeResponse;
import com.codingplatform.dto.SubmissionRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Service responsible for communicating with Multi-Language Judge Services.
 * 
 * This service acts as a router between the backend and isolated
 * code execution environments. It:
 * 1. Routes submissions to the correct judge based on language
 * 2. Forwards user code to the appropriate Judge Service
 * 3. Handles communication errors gracefully
 * 4. Returns the verdict to the caller
 * 
 * IMPORTANT: This service NEVER executes code itself.
 * 
 * Language → Port Mapping:
 * - Python → 5000
 * - C++    → 5002
 * - Java   → 5003
 * - JS     → 5004
 */
@Service
public class JudgeService {

    private static final Logger logger = LoggerFactory.getLogger(JudgeService.class);

    private final RestTemplate restTemplate;
    private final JudgeServiceConfig judgeConfig;

    public JudgeService(RestTemplate restTemplate, JudgeServiceConfig judgeConfig) {
        this.restTemplate = restTemplate;
        this.judgeConfig = judgeConfig;
    }

    /**
     * Submit code to the appropriate Judge Service based on language.
     *
     * @param request The submission request containing language and user code
     * @return JudgeResponse containing the verdict
     * @throws JudgeServiceException if communication with judge fails
     */
    public JudgeResponse submitCode(SubmissionRequest request) {
        String language = request.getLanguage().toLowerCase();
        logger.info("Submitting {} code to judge service", language);
        logger.debug("Code length: {} characters", request.getCode().length());

        // Get the correct judge URL based on language
        String judgeUrl = judgeConfig.getJudgeEndpoint(language);
        logger.debug("Judge URL for {}: {}", language, judgeUrl);

        // Prepare the request body
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("code", request.getCode());

        // Set headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);

        try {
            // Make the HTTP call to the Judge Service
            ResponseEntity<JudgeResponse> response = restTemplate.exchange(
                    judgeUrl,
                    HttpMethod.POST,
                    entity,
                    JudgeResponse.class
            );

            JudgeResponse judgeResponse = response.getBody();
            
            if (judgeResponse == null) {
                throw new JudgeServiceException("Received null response from " + language + " judge service");
            }

            logger.info("Received verdict from {} judge: {}", language, judgeResponse.getVerdict());
            return judgeResponse;

        } catch (ResourceAccessException e) {
            // Judge service is not reachable
            logger.error("Cannot connect to {} judge service: {}", language, e.getMessage());
            throw new JudgeServiceException(
                    language.toUpperCase() + " judge service is unavailable. Please ensure the judge is running.",
                    e
            );
            
        } catch (HttpClientErrorException e) {
            // 4xx errors from judge
            logger.error("Client error from {} judge service: {} - {}", language, e.getStatusCode(), e.getResponseBodyAsString());
            throw new JudgeServiceException(
                    "Invalid request to " + language + " judge service: " + e.getResponseBodyAsString(),
                    e
            );
            
        } catch (HttpServerErrorException e) {
            // 5xx errors from judge
            logger.error("Server error from {} judge service: {} - {}", language, e.getStatusCode(), e.getResponseBodyAsString());
            throw new JudgeServiceException(
                    language.toUpperCase() + " judge service encountered an error",
                    e
            );
        } catch (IllegalArgumentException e) {
            // Unsupported language
            logger.error("Unsupported language: {}", language);
            throw new JudgeServiceException("Unsupported language: " + language, e);
        }
    }

    /**
     * Get the problem statement from the Judge Service.
     *
     * @return Map containing problem details
     * @throws JudgeServiceException if communication with judge fails
     */
    @SuppressWarnings("rawtypes")
    public Map getProblem() {
        logger.info("Fetching problem from judge service");
        
        String problemUrl = judgeConfig.getProblemEndpoint();
        
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(problemUrl, Map.class);
            return response.getBody();
        } catch (ResourceAccessException e) {
            logger.error("Cannot connect to judge service: {}", e.getMessage());
            throw new JudgeServiceException(
                    "Judge service is unavailable. Please ensure the judge is running.",
                    e
            );
        }
    }

    /**
     * Check if all Judge Services are healthy.
     *
     * @return Map of language to health status
     */
    public Map<String, Boolean> getAllJudgesHealth() {
        Map<String, Boolean> healthStatus = new HashMap<>();
        
        for (String language : judgeConfig.getSupportedLanguages()) {
            healthStatus.put(language, isJudgeHealthy(language));
        }
        
        return healthStatus;
    }

    /**
     * Check if a specific Judge Service is healthy.
     *
     * @param language The language judge to check
     * @return true if the judge service is reachable and healthy
     */
    public boolean isJudgeHealthy(String language) {
        try {
            String healthUrl = judgeConfig.getHealthEndpoint(language);
            ResponseEntity<Map> response = restTemplate.getForEntity(healthUrl, Map.class);
            return response.getStatusCode() == HttpStatus.OK;
        } catch (Exception e) {
            logger.warn("{} judge health check failed: {}", language, e.getMessage());
            return false;
        }
    }

    /**
     * Check if at least one judge is healthy.
     */
    public boolean isAnyJudgeHealthy() {
        return getAllJudgesHealth().values().stream().anyMatch(Boolean::booleanValue);
    }

    /**
     * Custom exception for Judge Service communication errors.
     */
    public static class JudgeServiceException extends RuntimeException {
        public JudgeServiceException(String message) {
            super(message);
        }

        public JudgeServiceException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}

