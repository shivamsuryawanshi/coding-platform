package com.codingplatform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for code submission.
 * Contains the user's code and target language to be evaluated.
 */
public class SubmissionRequest {

    @NotBlank(message = "Language cannot be empty")
    @Pattern(regexp = "python|cpp|java|js", message = "Language must be one of: python, cpp, java, js")
    private String language;

    @NotBlank(message = "Code cannot be empty")
    @Size(max = 65536, message = "Code exceeds maximum length of 65536 characters")
    private String code;

    // Default constructor (required for JSON deserialization)
    public SubmissionRequest() {
    }

    public SubmissionRequest(String language, String code) {
        this.language = language;
        this.code = code;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return "SubmissionRequest{" +
                "language='" + language + '\'' +
                ", code='" + (code != null ? code.substring(0, Math.min(50, code.length())) + "..." : null) + '\'' +
                '}';
    }
}

