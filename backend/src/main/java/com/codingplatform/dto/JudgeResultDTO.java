package com.codingplatform.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for judge service response.
 */
public class JudgeResultDTO {

    private String verdict;
    private int passed;
    private int total;

    @JsonProperty("failed_test")
    private SubmissionResponse.FailedTestDTO failedTest;

    private String error;

    public JudgeResultDTO() {}

    // Getters and Setters
    public String getVerdict() { return verdict; }
    public void setVerdict(String verdict) { this.verdict = verdict; }

    public int getPassed() { return passed; }
    public void setPassed(int passed) { this.passed = passed; }

    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }

    public SubmissionResponse.FailedTestDTO getFailedTest() { return failedTest; }
    public void setFailedTest(SubmissionResponse.FailedTestDTO failedTest) { this.failedTest = failedTest; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
}

