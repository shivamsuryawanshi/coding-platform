package com.codingplatform.service;

import com.codingplatform.dto.SubmissionHistoryDTO;
import com.codingplatform.dto.PaginatedResponse;
import com.codingplatform.entity.Submission;
import com.codingplatform.repository.SubmissionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class SubmissionHistoryService {
    
    private final SubmissionRepository submissionRepository;
    
    public SubmissionHistoryService(SubmissionRepository submissionRepository) {
        this.submissionRepository = submissionRepository;
    }
    
    public PaginatedResponse<SubmissionHistoryDTO> getUserSubmissions(
            Long userId, int page, int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Submission> submissions = submissionRepository
                .findByUserIdOrderBySubmittedAtDesc(userId, pageable);
        
        return convertToPaginatedResponse(submissions);
    }
    
    public PaginatedResponse<SubmissionHistoryDTO> getUserSubmissionsForProblem(
            Long userId, String problemId, int page, int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Submission> submissions = submissionRepository
                .findByUserIdAndProblemIdOrderBySubmittedAtDesc(userId, problemId, pageable);
        
        return convertToPaginatedResponse(submissions);
    }
    
    public SubmissionHistoryDTO getSubmission(Long submissionId, Long userId) {
        Submission submission = submissionRepository
                .findByIdAndUserId(submissionId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Submission not found"));
        
        return convertToDTO(submission);
    }
    
    private PaginatedResponse<SubmissionHistoryDTO> convertToPaginatedResponse(
            Page<Submission> page) {
        
        var content = page.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        return new PaginatedResponse<>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
    
    private SubmissionHistoryDTO convertToDTO(Submission submission) {
        SubmissionHistoryDTO dto = new SubmissionHistoryDTO();
        dto.setId(submission.getId());
        dto.setProblemId(submission.getProblem().getId());
        dto.setProblemTitle(submission.getProblem().getTitle());
        dto.setLanguage(submission.getLanguage().name());
        dto.setStatus(submission.getStatus().name());
        dto.setVerdict(submission.getVerdict());
        dto.setPassedTests(submission.getPassedTests() != null ? submission.getPassedTests() : 0);
        dto.setTotalTests(submission.getTotalTests() != null ? submission.getTotalTests() : 0);
        dto.setSubmittedAt(submission.getSubmittedAt());
        return dto;
    }
}

