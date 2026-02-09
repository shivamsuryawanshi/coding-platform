package com.codingplatform.repository;

import com.codingplatform.entity.Problem;
import com.codingplatform.entity.Problem.Difficulty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Problem entity.
 */
@Repository
public interface ProblemRepository extends JpaRepository<Problem, String> {

    /**
     * Find all problems by category.
     */
    List<Problem> findByCategory(String category);

    /**
     * Find all problems by difficulty.
     */
    List<Problem> findByDifficulty(Difficulty difficulty);

    /**
     * Find all problems by category and difficulty.
     */
    List<Problem> findByCategoryAndDifficulty(String category, Difficulty difficulty);

    /**
     * Get all distinct categories.
     */
    @Query("SELECT DISTINCT p.category FROM Problem p ORDER BY p.category")
    List<String> findAllCategories();

    /**
     * Count problems by category.
     */
    long countByCategory(String category);

    /**
     * Count problems by difficulty.
     */
    long countByDifficulty(Difficulty difficulty);

    /**
     * Find problems with a specific tag.
     */
    @Query("SELECT p FROM Problem p JOIN p.tags t WHERE t.tag = :tag")
    List<Problem> findByTag(@Param("tag") String tag);

    /**
     * Search problems by title.
     */
    @Query("SELECT p FROM Problem p WHERE LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Problem> searchByTitle(@Param("query") String query);
}

