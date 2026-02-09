package com.codingplatform.dto;

import java.time.LocalDateTime;

/**
 * DTO for submission response (verdict).
 */
public class SubmissionResponse {

    private Long submissionId;
    private String problemId;
    private String language;
    private String verdict;
    private int passed;
    private int total;
    private FailedTestDTO failedTest;
    private String error;
    private LocalDateTime timestamp;

    public SubmissionResponse() {
        this.timestamp = LocalDateTime.now();
    }

    /**
     * Create success response.
     */
    public static SubmissionResponse success(Long submissionId, String problemId, String language,
                                              String verdict, int passed, int total) {
        SubmissionResponse response = new SubmissionResponse();
        response.submissionId = submissionId;
        response.problemId = problemId;
        response.language = language;
        response.verdict = verdict;
        response.passed = passed;
        response.total = total;
        return response;
    }

    /**
     * Create error response.
     */
    public static SubmissionResponse error(String errorMessage) {
        SubmissionResponse response = new SubmissionResponse();
        response.verdict = "Error";
        response.error = errorMessage;
        return response;
    }

    /**
     * Create response from judge result.
     */
    public static SubmissionResponse fromJudgeResult(Long submissionId, String problemId, 
                                                      String language, JudgeResultDTO judgeResult) {
        SubmissionResponse response = new SubmissionResponse();
        response.submissionId = submissionId;
        response.problemId = problemId;
        response.language = language;
        response.verdict = judgeResult.getVerdict();
        response.passed = judgeResult.getPassed();
        response.total = judgeResult.getTotal();
        response.failedTest = judgeResult.getFailedTest();
        response.error = judgeResult.getError();
        return response;
    }

    // Getters and Setters
    public Long getSubmissionId() { return submissionId; }
    public void setSubmissionId(Long submissionId) { this.submissionId = submissionId; }

    public String getProblemId() { return problemId; }
    public void setProblemId(String problemId) { this.problemId = problemId; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public String getVerdict() { return verdict; }
    public void setVerdict(String verdict) { this.verdict = verdict; }

    public int getPassed() { return passed; }
    public void setPassed(int passed) { this.passed = passed; }

    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }

    public FailedTestDTO getFailedTest() { return failedTest; }
    public void setFailedTest(FailedTestDTO failedTest) { this.failedTest = failedTest; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    /**
     * DTO for failed test details.
     */
    public static class FailedTestDTO {
        private int testId;
        private String input;
        private String expected;
        private String actual;
        private String error;

        public int getTestId() { return testId; }
        public void setTestId(int testId) { this.testId = testId; }

        public String getInput() { return input; }
        public void setInput(String input) { this.input = input; }

        public String getExpected() { return expected; }
        public void setExpected(String expected) { this.expected = expected; }

        public String getActual() { return actual; }
        public void setActual(String actual) { this.actual = actual; }

        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
    }
}
