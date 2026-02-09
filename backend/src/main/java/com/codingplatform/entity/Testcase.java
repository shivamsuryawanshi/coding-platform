package com.codingplatform.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Testcase entity - maps to 'testcases' table in RDS.
 * Contains S3 keys for input/output files.
 */
@Entity
@Table(name = "testcases")
public class Testcase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

    @Column(name = "testcase_number", nullable = false)
    private Integer testcaseNumber;

    @Column(name = "s3_input_key", nullable = false)
    private String s3InputKey;

    @Column(name = "s3_output_key", nullable = false)
    private String s3OutputKey;

    @Column(name = "is_sample")
    private Boolean isSample = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Constructors
    public Testcase() {}

    public Testcase(Problem problem, Integer testcaseNumber, String s3InputKey, String s3OutputKey) {
        this.problem = problem;
        this.testcaseNumber = testcaseNumber;
        this.s3InputKey = s3InputKey;
        this.s3OutputKey = s3OutputKey;
    }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Problem getProblem() { return problem; }
    public void setProblem(Problem problem) { this.problem = problem; }

    public Integer getTestcaseNumber() { return testcaseNumber; }
    public void setTestcaseNumber(Integer testcaseNumber) { this.testcaseNumber = testcaseNumber; }

    public String getS3InputKey() { return s3InputKey; }
    public void setS3InputKey(String s3InputKey) { this.s3InputKey = s3InputKey; }

    public String getS3OutputKey() { return s3OutputKey; }
    public void setS3OutputKey(String s3OutputKey) { this.s3OutputKey = s3OutputKey; }

    public Boolean getIsSample() { return isSample; }
    public void setIsSample(Boolean isSample) { this.isSample = isSample; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

