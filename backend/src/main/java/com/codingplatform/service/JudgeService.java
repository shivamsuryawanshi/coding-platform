package com.codingplatform.service;

import com.codingplatform.config.JudgeConfig;
import com.codingplatform.dto.JudgeResultDTO;
import com.codingplatform.dto.SubmissionRequest;
import com.codingplatform.dto.SubmissionResponse;
import com.codingplatform.dto.TestcaseDTO;
import com.codingplatform.entity.Problem;
import com.codingplatform.entity.Submission;
import com.codingplatform.entity.Submission.Language;
import com.codingplatform.entity.Submission.SubmissionStatus;
import com.codingplatform.entity.Testcase;
import com.codingplatform.entity.User;
import com.codingplatform.repository.ProblemRepository;
import com.codingplatform.repository.SubmissionRepository;
import com.codingplatform.repository.TestcaseRepository;
import com.codingplatform.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for code submission and judging.
 * Coordinates between backend, S3, and Judge service.
 */
@Service
public class JudgeService {

    private static final Logger logger = LoggerFactory.getLogger(JudgeService.class);

    private final RestTemplate restTemplate;
    private final JudgeConfig judgeConfig;
    private final ProblemRepository problemRepository;
    private final TestcaseRepository testcaseRepository;
    private final SubmissionRepository submissionRepository;
    private final S3Service s3Service;
    private final UserRepository userRepository;

    public JudgeService(RestTemplate restTemplate,
                        JudgeConfig judgeConfig,
                        ProblemRepository problemRepository,
                        TestcaseRepository testcaseRepository,
                        SubmissionRepository submissionRepository,
                        S3Service s3Service,
                        UserRepository userRepository) {
        this.restTemplate = restTemplate;
        this.judgeConfig = judgeConfig;
        this.problemRepository = problemRepository;
        this.testcaseRepository = testcaseRepository;
        this.submissionRepository = submissionRepository;
        this.s3Service = s3Service;
        this.userRepository = userRepository;
    }

    /**
     * Submit code for evaluation.
     */
    @Transactional
    public SubmissionResponse submitCode(SubmissionRequest request, Long userId) {
        String problemId = request.getProblemId();
        String languageStr = request.getLanguage().toLowerCase();
        String code = request.getCode();

        logger.info("Received submission for problem: {} in {} by user: {}", 
                problemId, languageStr, userId);

        // Validate problem exists
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new JudgeServiceException("Problem not found: " + problemId));

        // Get user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new JudgeServiceException("User not found"));

        // Parse language
        Language language;
        try {
            language = Language.valueOf(languageStr.equals("js") ? "javascript" : languageStr);
        } catch (IllegalArgumentException e) {
            throw new JudgeServiceException("Unsupported language: " + languageStr);
        }

        // Create submission record with QUEUED status
        Submission submission = new Submission(user, problem, language, code);
        submission.setStatus(SubmissionStatus.QUEUED);
        submission = submissionRepository.save(submission);

        try {
            // Update status to RUNNING
            submission.setStatus(SubmissionStatus.RUNNING);
            submissionRepository.save(submission);

            // Fetch testcases from S3
            List<Testcase> testcases = testcaseRepository.findByProblemIdOrdered(problemId);
            if (testcases.isEmpty()) {
                throw new JudgeServiceException("No testcases found for problem: " + problemId);
            }

            List<TestcaseDTO> testcaseDTOs = testcases.stream()
                    .map(tc -> {
                        String input = s3Service.getFileContent(tc.getS3InputKey());
                        String expectedOutput = s3Service.getFileContent(tc.getS3OutputKey());
                        return new TestcaseDTO(tc.getTestcaseNumber(), input, expectedOutput);
                    })
                    .collect(Collectors.toList());

            // Send to judge service
            JudgeResultDTO judgeResult = callJudgeService(languageStr, code, testcaseDTOs);

            // Map judge verdict to status
            SubmissionStatus finalStatus = mapVerdictToStatus(judgeResult.getVerdict());

            // Update submission with result
            submission.setStatus(finalStatus);
            submission.setVerdict(judgeResult.getVerdict());
            submission.setPassedTests(judgeResult.getPassed());
            submission.setTotalTests(judgeResult.getTotal());
            if (judgeResult.getError() != null) {
                submission.setErrorMessage(judgeResult.getError());
            }
            submissionRepository.save(submission);

            return SubmissionResponse.fromJudgeResult(
                    submission.getId(), problemId, languageStr, judgeResult);

        } catch (S3Service.S3ServiceException e) {
            logger.error("S3 error during submission: {}", e.getMessage());
            submission.setStatus(SubmissionStatus.RE);
            submission.setVerdict("Error");
            submission.setErrorMessage("Failed to fetch testcases");
            submissionRepository.save(submission);
            return SubmissionResponse.error("Failed to fetch testcases from storage");
            
        } catch (JudgeServiceException e) {
            logger.error("Judge error: {}", e.getMessage());
            submission.setStatus(SubmissionStatus.RE);
            submission.setVerdict("Error");
            submission.setErrorMessage(e.getMessage());
            submissionRepository.save(submission);
            return SubmissionResponse.error(e.getMessage());
        }
    }

    /**
     * Map judge verdict string to SubmissionStatus enum.
     */
    private SubmissionStatus mapVerdictToStatus(String verdict) {
        if (verdict == null) return SubmissionStatus.RE;
        
        return switch (verdict) {
            case "Accepted" -> SubmissionStatus.ACCEPTED;
            case "Wrong Answer" -> SubmissionStatus.WRONG_ANSWER;
            case "Time Limit Exceeded" -> SubmissionStatus.TLE;
            case "Runtime Error" -> SubmissionStatus.RE;
            case "Compilation Error" -> SubmissionStatus.CE;
            default -> SubmissionStatus.RE;
        };
    }

    /**
     * Call the judge service with code and testcases.
     */
    private JudgeResultDTO callJudgeService(String language, String code, List<TestcaseDTO> testcases) {
        String judgeUrl = judgeConfig.getJudgeBaseUrl() + "/judge";
        logger.debug("Calling judge service: {}", judgeUrl);

        // Prepare request body
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("language", language);
        requestBody.put("code", code);
        requestBody.put("testcases", testcases);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<JudgeResultDTO> response = restTemplate.exchange(
                    judgeUrl,
                    HttpMethod.POST,
                    entity,
                    JudgeResultDTO.class
            );

            JudgeResultDTO result = response.getBody();
            if (result == null) {
                throw new JudgeServiceException("Empty response from judge service");
            }

            logger.info("Judge verdict: {} ({}/{})", 
                    result.getVerdict(), result.getPassed(), result.getTotal());
            return result;

        } catch (ResourceAccessException e) {
            logger.error("Judge service unreachable: {}", e.getMessage());
            throw new JudgeServiceException("Judge service is unavailable");
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            logger.error("Judge service error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new JudgeServiceException("Judge service error: " + e.getMessage());
        }
    }

    /**
     * Check if judge service is healthy.
     */
    public boolean isJudgeHealthy() {
        try {
            String healthUrl = judgeConfig.getJudgeBaseUrl() + "/health";
            ResponseEntity<Map<String, Object>> response = restTemplate.getForEntity(healthUrl, Map.class);
            return response.getStatusCode() == HttpStatus.OK;
        } catch (Exception e) {
            logger.warn("Judge health check failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Custom exception for judge service errors.
     */
    public static class JudgeServiceException extends RuntimeException {
        public JudgeServiceException(String message) {
            super(message);
        }
    }
}
