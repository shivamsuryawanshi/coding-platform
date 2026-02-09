package com.codingplatform.dto;

/**
 * DTO for testcase data (sent to judge service).
 */
public class TestcaseDTO {

    private int id;
    private String input;
    private String expectedOutput;

    public TestcaseDTO() {}

    public TestcaseDTO(int id, String input, String expectedOutput) {
        this.id = id;
        this.input = input;
        this.expectedOutput = expectedOutput;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getInput() { return input; }
    public void setInput(String input) { this.input = input; }

    public String getExpectedOutput() { return expectedOutput; }
    public void setExpectedOutput(String expectedOutput) { this.expectedOutput = expectedOutput; }
}

