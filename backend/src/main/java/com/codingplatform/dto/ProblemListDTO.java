package com.codingplatform.dto;

import com.codingplatform.entity.Problem;

/**
 * DTO for problem list view (lightweight).
 */
public class ProblemListDTO {

    private String id;
    private String title;
    private String category;
    private String difficulty;
    private Integer timeLimit;
    private Integer memoryLimit;

    public ProblemListDTO() {}

    public ProblemListDTO(Problem problem) {
        this.id = problem.getId();
        this.title = problem.getTitle();
        this.category = problem.getCategory();
        this.difficulty = problem.getDifficulty().name();
        this.timeLimit = problem.getTimeLimit();
        this.memoryLimit = problem.getMemoryLimit();
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

    public Integer getTimeLimit() { return timeLimit; }
    public void setTimeLimit(Integer timeLimit) { this.timeLimit = timeLimit; }

    public Integer getMemoryLimit() { return memoryLimit; }
    public void setMemoryLimit(Integer memoryLimit) { this.memoryLimit = memoryLimit; }
}

