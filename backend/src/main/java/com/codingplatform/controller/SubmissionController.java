package com.codingplatform.controller;

import com.codingplatform.dto.JudgeResponse;
import com.codingplatform.dto.SubmissionRequest;
import com.codingplatform.dto.SubmissionResponse;
import com.codingplatform.service.JudgeService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST Controller for handling code submissions.
 * 
 * This controller is the entry point for all code submission requests.
 * It validates incoming requests and delegates to the JudgeService
 * for actual code evaluation.
 * 
 * Endpoints:
 * - GET  /problem - Get problem statement
 * - POST /submit  - Submit code for evaluation
 * - GET  /health  - Health check
 */
@RestController
@RequestMapping
@CrossOrigin(origins = "*")  // Allow frontend to call backend
public class SubmissionController {

    private static final Logger logger = LoggerFactory.getLogger(SubmissionController.class);

    private final JudgeService judgeService;

    public SubmissionController(JudgeService judgeService) {
        this.judgeService = judgeService;
    }

    /**
     * Submit code for evaluation.
     *
     * @param request The submission request containing language and user code
     * @return SubmissionResponse with the verdict
     */
    @PostMapping("/submit")
    public ResponseEntity<SubmissionResponse> submitCode(@Valid @RequestBody SubmissionRequest request) {
        logger.info("Received {} code submission", request.getLanguage());
        
        try {
            // Forward to appropriate judge service based on language
            JudgeResponse judgeResponse = judgeService.submitCode(request);
            
            // Convert to submission response
            SubmissionResponse response = SubmissionResponse.fromJudgeResponse(judgeResponse);
            
            return ResponseEntity.ok(response);
            
        } catch (JudgeService.JudgeServiceException e) {
            logger.error("Judge service error: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(SubmissionResponse.error(e.getMessage()));
        }
    }

    /**
     * Get the problem statement.
     *
     * @return Problem details from the judge service
     */
    @SuppressWarnings("rawtypes")
    @GetMapping("/problem")
    public ResponseEntity<Map> getProblem() {
        logger.info("Fetching problem statement");
        
        try {
            Map problem = judgeService.getProblem();
            return ResponseEntity.ok(problem);
        } catch (JudgeService.JudgeServiceException e) {
            logger.error("Failed to fetch problem: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Health check endpoint for the backend.
     * Returns health status for all language judges.
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Boolean> judgesHealth = judgeService.getAllJudgesHealth();
        boolean anyHealthy = judgeService.isAnyJudgeHealthy();
        
        Map<String, Object> healthStatus = Map.of(
                "status", anyHealthy ? "healthy" : "degraded",
                "service", "backend",
                "version", "1.0.0",
                "judges", judgesHealth
        );
        
        HttpStatus status = anyHealthy ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE;
        return ResponseEntity.status(status).body(healthStatus);
    }

    /**
     * Get list of supported languages.
     */
    @GetMapping("/languages")
    public ResponseEntity<Map<String, Object>> getLanguages() {
        return ResponseEntity.ok(Map.of(
                "languages", new String[]{"python", "cpp", "java", "js"},
                "display", Map.of(
                        "python", "Python",
                        "cpp", "C++",
                        "java", "Java",
                        "js", "JavaScript"
                )
        ));
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
        
        return ResponseEntity
                .badRequest()
                .body(SubmissionResponse.error(errorMessage));
    }
}

