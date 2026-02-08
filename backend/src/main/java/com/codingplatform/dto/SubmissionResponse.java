package com.codingplatform.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.Map;

/**
 * Response DTO for submission results.
 * This is what the client receives after submitting code.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SubmissionResponse {

    private String verdict;
    private Integer passed;
    private Integer total;
    private Map<String, Object> failedTest;
    private String error;
    private String timestamp;

    public SubmissionResponse() {
        this.timestamp = Instant.now().toString();
    }

    // Static factory methods for common responses
    public static SubmissionResponse fromJudgeResponse(JudgeResponse judgeResponse) {
        SubmissionResponse response = new SubmissionResponse();
        response.setVerdict(judgeResponse.getVerdict());
        response.setPassed(judgeResponse.getPassed());
        response.setTotal(judgeResponse.getTotal());
        response.setFailedTest(judgeResponse.getFailedTest());
        return response;
    }

    public static SubmissionResponse error(String errorMessage) {
        SubmissionResponse response = new SubmissionResponse();
        response.setVerdict("Error");
        response.setError(errorMessage);
        return response;
    }

    public String getVerdict() {
        return verdict;
    }

    public void setVerdict(String verdict) {
        this.verdict = verdict;
    }

    public Integer getPassed() {
        return passed;
    }

    public void setPassed(Integer passed) {
        this.passed = passed;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public Map<String, Object> getFailedTest() {
        return failedTest;
    }

    public void setFailedTest(Map<String, Object> failedTest) {
        this.failedTest = failedTest;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}

