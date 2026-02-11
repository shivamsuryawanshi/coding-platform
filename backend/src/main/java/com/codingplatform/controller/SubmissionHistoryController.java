package com.codingplatform.controller;

import com.codingplatform.dto.PaginatedResponse;
import com.codingplatform.dto.SubmissionHistoryDTO;
import com.codingplatform.security.UserContext;
import com.codingplatform.service.SubmissionHistoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/submissions")
@CrossOrigin(origins = "*")
public class SubmissionHistoryController {
    
    private static final Logger logger = LoggerFactory.getLogger(SubmissionHistoryController.class);
    
    private final SubmissionHistoryService submissionHistoryService;
    
    public SubmissionHistoryController(SubmissionHistoryService submissionHistoryService) {
        this.submissionHistoryService = submissionHistoryService;
    }
    
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PaginatedResponse<SubmissionHistoryDTO>> getMySubmissions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        logger.info("GET /api/submissions/me - user={}, page={}, size={}", userId, page, size);
        
        PaginatedResponse<SubmissionHistoryDTO> response = 
                submissionHistoryService.getUserSubmissions(userId, page, size);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/me/problem/{problemId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PaginatedResponse<SubmissionHistoryDTO>> getMySubmissionsForProblem(
            @PathVariable String problemId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        logger.info("GET /api/submissions/me/problem/{} - user={}, page={}, size={}", 
                problemId, userId, page, size);
        
        PaginatedResponse<SubmissionHistoryDTO> response = 
                submissionHistoryService.getUserSubmissionsForProblem(userId, problemId, page, size);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SubmissionHistoryDTO> getSubmission(@PathVariable Long id) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            SubmissionHistoryDTO submission = 
                    submissionHistoryService.getSubmission(id, userId);
            return ResponseEntity.ok(submission);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}

