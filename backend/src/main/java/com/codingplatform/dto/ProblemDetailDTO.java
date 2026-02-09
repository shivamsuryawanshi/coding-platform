package com.codingplatform.dto;

import com.codingplatform.entity.Problem;
import java.util.List;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * DTO for problem detail view (full details).
 */
public class ProblemDetailDTO {

    private String id;
    private String title;
    private String category;
    private String difficulty;
    private String statement;
    private String inputFormat;
    private String outputFormat;
    private List<String> constraints;
    private Integer timeLimit;
    private Integer memoryLimit;
    private List<String> tags;
    private List<ExampleDTO> examples;
    private int testcaseCount;

    public ProblemDetailDTO() {}

    public ProblemDetailDTO(Problem problem, List<String> tags, List<ExampleDTO> examples, int testcaseCount) {
        this.id = problem.getId();
        this.title = problem.getTitle();
        this.category = problem.getCategory();
        this.difficulty = problem.getDifficulty().name();
        this.statement = problem.getStatement();
        this.inputFormat = problem.getInputFormat();
        this.outputFormat = problem.getOutputFormat();
        
        // Parse constraints (stored as newline-separated string)
        String constraintsStr = problem.getConstraints();
        if (constraintsStr != null && !constraintsStr.isEmpty()) {
            this.constraints = Arrays.stream(constraintsStr.split("\n"))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
        } else {
            this.constraints = List.of();
        }
        
        this.timeLimit = problem.getTimeLimit();
        this.memoryLimit = problem.getMemoryLimit();
        this.tags = tags;
        this.examples = examples;
        this.testcaseCount = testcaseCount;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public String getStatement() { return statement; }
    public void setStatement(String statement) { this.statement = statement; }

    public String getInputFormat() { return inputFormat; }
    public void setInputFormat(String inputFormat) { this.inputFormat = inputFormat; }

    public String getOutputFormat() { return outputFormat; }
    public void setOutputFormat(String outputFormat) { this.outputFormat = outputFormat; }

    public List<String> getConstraints() { return constraints; }
    public void setConstraints(List<String> constraints) { this.constraints = constraints; }

    public Integer getTimeLimit() { return timeLimit; }
    public void setTimeLimit(Integer timeLimit) { this.timeLimit = timeLimit; }

    public Integer getMemoryLimit() { return memoryLimit; }
    public void setMemoryLimit(Integer memoryLimit) { this.memoryLimit = memoryLimit; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }

    public List<ExampleDTO> getExamples() { return examples; }
    public void setExamples(List<ExampleDTO> examples) { this.examples = examples; }

    public int getTestcaseCount() { return testcaseCount; }
    public void setTestcaseCount(int testcaseCount) { this.testcaseCount = testcaseCount; }

    /**
     * Example DTO for sample testcases.
     */
    public static class ExampleDTO {
        private String input;
        private String output;
        private String explanation;

        public ExampleDTO() {}

        public ExampleDTO(String input, String output) {
            this.input = input;
            this.output = output;
        }

        public ExampleDTO(String input, String output, String explanation) {
            this.input = input;
            this.output = output;
            this.explanation = explanation;
        }

        public String getInput() { return input; }
        public void setInput(String input) { this.input = input; }

        public String getOutput() { return output; }
        public void setOutput(String output) { this.output = output; }

        public String getExplanation() { return explanation; }
        public void setExplanation(String explanation) { this.explanation = explanation; }
    }
}

