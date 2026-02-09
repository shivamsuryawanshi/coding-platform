package com.codingplatform.repository;

import com.codingplatform.entity.ProblemTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for ProblemTag entity.
 */
@Repository
public interface ProblemTagRepository extends JpaRepository<ProblemTag, Integer> {

    /**
     * Find all tags for a problem.
     */
    @Query("SELECT pt.tag FROM ProblemTag pt WHERE pt.problem.id = :problemId")
    List<String> findTagsByProblemId(@Param("problemId") String problemId);

    /**
     * Get all distinct tags.
     */
    @Query("SELECT DISTINCT pt.tag FROM ProblemTag pt ORDER BY pt.tag")
    List<String> findAllDistinctTags();
}

