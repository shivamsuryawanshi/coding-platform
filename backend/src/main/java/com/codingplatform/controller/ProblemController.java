package com.codingplatform.controller;

import com.codingplatform.dto.ProblemDetailDTO;
import com.codingplatform.dto.ProblemListDTO;
import com.codingplatform.service.ProblemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for problem operations.
 * 
 * Endpoints:
 * - GET  /api/problems         - Get all problems (with optional filters)
 * - GET  /api/problems/{id}    - Get problem details
 * - GET  /api/categories       - Get all categories
 * - GET  /api/tags             - Get all tags
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ProblemController {

    private static final Logger logger = LoggerFactory.getLogger(ProblemController.class);

    private final ProblemService problemService;

    public ProblemController(ProblemService problemService) {
        this.problemService = problemService;
    }

    /**
     * Get all problems with optional filters.
     * 
     * @param category   Filter by category (optional)
     * @param difficulty Filter by difficulty (optional)
     * @param search     Search by title (optional)
     */
    @GetMapping("/problems")
    public ResponseEntity<List<ProblemListDTO>> getProblems(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) String search) {
        
        logger.info("GET /api/problems - category={}, difficulty={}, search={}", 
                category, difficulty, search);

        List<ProblemListDTO> problems;

        if (search != null && !search.isEmpty()) {
            problems = problemService.searchProblems(search);
        } else if (category != null && difficulty != null) {
            problems = problemService.getProblemsByCategoryAndDifficulty(category, difficulty);
        } else if (category != null) {
            problems = problemService.getProblemsByCategory(category);
        } else if (difficulty != null) {
            problems = problemService.getProblemsByDifficulty(difficulty);
        } else {
            problems = problemService.getAllProblems();
        }

        return ResponseEntity.ok(problems);
    }

    /**
     * Get problem details by ID.
     */
    @GetMapping("/problems/{id}")
    public ResponseEntity<?> getProblemById(@PathVariable String id) {
        logger.info("GET /api/problems/{}", id);

        return problemService.getProblemById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get all categories.
     */
    @GetMapping("/categories")
    public ResponseEntity<List<String>> getCategories() {
        logger.info("GET /api/categories");
        return ResponseEntity.ok(problemService.getAllCategories());
    }

    /**
     * Get all tags.
     */
    @GetMapping("/tags")
    public ResponseEntity<List<String>> getTags() {
        logger.info("GET /api/tags");
        return ResponseEntity.ok(problemService.getAllTags());
    }

    /**
     * Get problem statistics.
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        logger.info("GET /api/stats");
        
        List<ProblemListDTO> allProblems = problemService.getAllProblems();
        
        long easyCount = allProblems.stream()
                .filter(p -> "easy".equalsIgnoreCase(p.getDifficulty())).count();
        long mediumCount = allProblems.stream()
                .filter(p -> "medium".equalsIgnoreCase(p.getDifficulty())).count();
        long hardCount = allProblems.stream()
                .filter(p -> "hard".equalsIgnoreCase(p.getDifficulty())).count();

        Map<String, Object> stats = Map.of(
                "total", allProblems.size(),
                "easy", easyCount,
                "medium", mediumCount,
                "hard", hardCount,
                "categories", problemService.getAllCategories().size()
        );

        return ResponseEntity.ok(stats);
    }
}

