package com.codingplatform.repository;

import com.codingplatform.entity.Submission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Submission entity.
 */
@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {

    /**
     * Find all submissions for a user, ordered by submission time (descending).
     */
    Page<Submission> findByUserIdOrderBySubmittedAtDesc(Long userId, Pageable pageable);

    /**
     * Find all submissions for a user and problem, ordered by submission time
     * (descending).
     */
    Page<Submission> findByUserIdAndProblemIdOrderBySubmittedAtDesc(
            Long userId, String problemId, Pageable pageable);

    /**
     * Find submission by ID and user ID (for security).
     */
    Optional<Submission> findByIdAndUserId(Long id, Long userId);

    /**
     * Find all testcases for a problem, ordered by testcase number.
     */
    @Query("SELECT t FROM Testcase t WHERE t.problem.id = :problemId ORDER BY t.testcaseNumber")
    List<com.codingplatform.entity.Testcase> findByProblemIdOrdered(@Param("problemId") String problemId);

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
