package com.codingplatform.entity;

import jakarta.persistence.*;

/**
 * ProblemTag entity - maps to 'problem_tags' table in RDS.
 */
@Entity
@Table(name = "problem_tags")
public class ProblemTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

    @Column(name = "tag", length = 50, nullable = false)
    private String tag;

    // Constructors
    public ProblemTag() {}

    public ProblemTag(Problem problem, String tag) {
        this.problem = problem;
        this.tag = tag;
    }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Problem getProblem() { return problem; }
    public void setProblem(Problem problem) { this.problem = problem; }

    public String getTag() { return tag; }
    public void setTag(String tag) { this.tag = tag; }
}

