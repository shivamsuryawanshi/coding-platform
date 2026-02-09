package com.codingplatform.repository;

import com.codingplatform.entity.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Submission entity.
 */
@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {

    /**
     * Find all submissions for a problem, ordered by submission time.
     */
    @Query("SELECT s FROM Submission s WHERE s.problem.id = :problemId ORDER BY s.submittedAt DESC")
    List<Submission> findByProblemIdOrdered(@Param("problemId") String problemId);

    /**
     * Find recent submissions (limit by @Query or Pageable).
     */
    @Query("SELECT s FROM Submission s ORDER BY s.submittedAt DESC")
    List<Submission> findRecentSubmissions();

    /**
     * Count accepted submissions for a problem.
     */
    @Query("SELECT COUNT(s) FROM Submission s WHERE s.problem.id = :problemId AND s.verdict = 'Accepted'")
    long countAcceptedByProblemId(@Param("problemId") String problemId);
}

