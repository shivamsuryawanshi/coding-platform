package com.codingplatform.repository;

import com.codingplatform.entity.Testcase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Testcase entity.
 */
@Repository
public interface TestcaseRepository extends JpaRepository<Testcase, Integer> {

    /**
     * Find all testcases for a problem, ordered by testcase number.
     */
    @Query("SELECT t FROM Testcase t WHERE t.problem.id = :problemId ORDER BY t.testcaseNumber")
    List<Testcase> findByProblemIdOrdered(@Param("problemId") String problemId);

    /**
     * Find sample testcases for a problem (for display in UI).
     */
    @Query("SELECT t FROM Testcase t WHERE t.problem.id = :problemId AND t.isSample = true ORDER BY t.testcaseNumber")
    List<Testcase> findSamplesByProblemId(@Param("problemId") String problemId);

    /**
     * Count testcases for a problem.
     */
    long countByProblemId(String problemId);
}

