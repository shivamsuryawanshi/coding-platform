package com.codingplatform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO for code submission request.
 */
public class SubmissionRequest {

    @NotBlank(message = "Problem ID is required")
    private String problemId;

    @NotBlank(message = "Language is required")
    private String language;

    @NotBlank(message = "Code is required")
    @Size(max = 50000, message = "Code cannot exceed 50000 characters")
    private String code;

    public SubmissionRequest() {}

    public SubmissionRequest(String problemId, String language, String code) {
        this.problemId = problemId;
        this.language = language;
        this.code = code;
    }

    // Getters and Setters
    public String getProblemId() { return problemId; }
    public void setProblemId(String problemId) { this.problemId = problemId; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
}
