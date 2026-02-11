package com.codingplatform.controller;

import com.codingplatform.dto.SubmissionRequest;
import com.codingplatform.dto.SubmissionResponse;
import com.codingplatform.security.UserContext;
import com.codingplatform.service.JudgeService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST Controller for code submissions.
 * 
 * Endpoints:
 * - POST /api/submit - Submit code for evaluation
 * - GET /api/health - Health check
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class SubmissionController {

    private static final Logger logger = LoggerFactory.getLogger(SubmissionController.class);

    private final JudgeService judgeService;

    public SubmissionController(JudgeService judgeService) {
        this.judgeService = judgeService;
    }

    /**
     * Submit code for evaluation.
     */
    @PostMapping("/submit")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SubmissionResponse> submitCode(@Valid @RequestBody SubmissionRequest request) {
        Long userId = UserContext.getCurrentUserId();
        
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(SubmissionResponse.error("Authentication required"));
        }
        
        logger.info("POST /api/submit - problem={}, language={}, user={}",
                request.getProblemId(), request.getLanguage(), userId);

        try {
            SubmissionResponse response = judgeService.submitCode(request, userId);
            return ResponseEntity.ok(response);
        } catch (JudgeService.JudgeServiceException e) {
            logger.error("Submission error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(SubmissionResponse.error(e.getMessage()));
        }
    }

    /**
     * Health check endpoint.
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        boolean judgeHealthy = judgeService.isJudgeHealthy();

        Map<String, Object> status = Map.of(
                "status", judgeHealthy ? "healthy" : "degraded",
                "service", "backend",
                "version", "2.0.0",
                "judge", judgeHealthy);

        HttpStatus httpStatus = judgeHealthy ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE;
        return ResponseEntity.status(httpStatus).body(status);
    }

    /**
     * Get supported languages.
     */
    @GetMapping("/languages")
    public ResponseEntity<Map<String, Object>> getLanguages() {
        return ResponseEntity.ok(Map.of(
                "languages", new String[] { "python", "cpp", "java", "javascript" },
                "display", Map.of(
                        "python", "Python 3.10",
                        "cpp", "C++ 17",
                        "java", "Java 17",
                        "javascript", "Node.js LTS")));
    }

    /**
     * Exception handler for validation errors.
     */
    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public ResponseEntity<SubmissionResponse> handleValidationException(
            org.springframework.web.bind.MethodArgumentNotValidException e) {

        String errorMessage = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("Validation failed");

        return ResponseEntity.badRequest().body(SubmissionResponse.error(errorMessage));
    }
}
