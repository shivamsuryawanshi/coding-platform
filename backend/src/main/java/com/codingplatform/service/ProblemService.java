package com.codingplatform.service;

import com.codingplatform.dto.ProblemDetailDTO;
import com.codingplatform.dto.ProblemDetailDTO.ExampleDTO;
import com.codingplatform.dto.ProblemListDTO;
import com.codingplatform.entity.Problem;
import com.codingplatform.entity.Problem.Difficulty;
import com.codingplatform.entity.Testcase;
import com.codingplatform.repository.ProblemRepository;
import com.codingplatform.repository.ProblemTagRepository;
import com.codingplatform.repository.TestcaseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for problem operations.
 * Fetches problem data from RDS MySQL.
 */
@Service
@Transactional(readOnly = true)
public class ProblemService {

    private static final Logger logger = LoggerFactory.getLogger(ProblemService.class);

    private final ProblemRepository problemRepository;
    private final ProblemTagRepository tagRepository;
    private final TestcaseRepository testcaseRepository;
    private final S3Service s3Service;

    public ProblemService(ProblemRepository problemRepository,
                          ProblemTagRepository tagRepository,
                          TestcaseRepository testcaseRepository,
                          S3Service s3Service) {
        this.problemRepository = problemRepository;
        this.tagRepository = tagRepository;
        this.testcaseRepository = testcaseRepository;
        this.s3Service = s3Service;
    }

    /**
     * Get all problems (lightweight list).
     */
    public List<ProblemListDTO> getAllProblems() {
        logger.info("Fetching all problems");
        return problemRepository.findAll().stream()
                .map(ProblemListDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * Get problems by category.
     */
    public List<ProblemListDTO> getProblemsByCategory(String category) {
        logger.info("Fetching problems by category: {}", category);
        return problemRepository.findByCategory(category).stream()
                .map(ProblemListDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * Get problems by difficulty.
     */
    public List<ProblemListDTO> getProblemsByDifficulty(String difficulty) {
        logger.info("Fetching problems by difficulty: {}", difficulty);
        Difficulty diff = Difficulty.valueOf(difficulty.toLowerCase());
        return problemRepository.findByDifficulty(diff).stream()
                .map(ProblemListDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * Get problems by category and difficulty.
     */
    public List<ProblemListDTO> getProblemsByCategoryAndDifficulty(String category, String difficulty) {
        logger.info("Fetching problems by category: {} and difficulty: {}", category, difficulty);
        Difficulty diff = Difficulty.valueOf(difficulty.toLowerCase());
        return problemRepository.findByCategoryAndDifficulty(category, diff).stream()
                .map(ProblemListDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * Get problem details by ID.
     */
    public Optional<ProblemDetailDTO> getProblemById(String id) {
        logger.info("Fetching problem details: {}", id);
        
        Optional<Problem> problemOpt = problemRepository.findById(id);
        if (problemOpt.isEmpty()) {
            return Optional.empty();
        }

        Problem problem = problemOpt.get();
        
        // Get tags
        List<String> tags = tagRepository.findTagsByProblemId(id);
        
        // Get sample testcases and fetch content from S3
        List<Testcase> sampleTestcases = testcaseRepository.findSamplesByProblemId(id);
        List<ExampleDTO> examples = new ArrayList<>();
        
        for (Testcase tc : sampleTestcases) {
            try {
                String input = s3Service.getFileContent(tc.getS3InputKey());
                String output = s3Service.getFileContent(tc.getS3OutputKey());
                examples.add(new ExampleDTO(input.trim(), output.trim()));
            } catch (Exception e) {
                logger.warn("Failed to fetch sample testcase from S3: {}", e.getMessage());
            }
        }
        
        // Get total testcase count
        int testcaseCount = (int) testcaseRepository.countByProblemId(id);
        
        return Optional.of(new ProblemDetailDTO(problem, tags, examples, testcaseCount));
    }

    /**
     * Get all distinct categories.
     */
    public List<String> getAllCategories() {
        return problemRepository.findAllCategories();
    }

    /**
     * Get all distinct tags.
     */
    public List<String> getAllTags() {
        return tagRepository.findAllDistinctTags();
    }

    /**
     * Search problems by title.
     */
    public List<ProblemListDTO> searchProblems(String query) {
        logger.info("Searching problems: {}", query);
        return problemRepository.searchByTitle(query).stream()
                .map(ProblemListDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * Check if problem exists.
     */
    public boolean problemExists(String id) {
        return problemRepository.existsById(id);
    }
}

