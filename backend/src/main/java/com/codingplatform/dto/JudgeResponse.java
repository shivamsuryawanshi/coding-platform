package com.codingplatform.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

/**
 * Response DTO from the Judge Service.
 * Contains the verdict and test case results.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class JudgeResponse {

    private String verdict;
    private Integer passed;
    private Integer total;
    
    @JsonProperty("failed_test")
    private Map<String, Object> failedTest;
    
    private String error;

    // Default constructor
    public JudgeResponse() {
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

    @Override
    public String toString() {
        return "JudgeResponse{" +
                "verdict='" + verdict + '\'' +
                ", passed=" + passed +
                ", total=" + total +
                ", failedTest=" + failedTest +
                ", error='" + error + '\'' +
                '}';
    }
}

