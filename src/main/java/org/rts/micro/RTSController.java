package org.rts.micro;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Set;

import static org.rts.micro.TestExecutor.executeAllTests;
import static org.rts.micro.TestExecutor.executeSelectedTests;

@RestController
public class RTSController {

    private static final Logger logger = LoggerFactory.getLogger(RTSController.class);

    @CrossOrigin
    @PostMapping("/configured-repos")
    public ResponseEntity<String> configureRepo(@RequestParam String repoName, @RequestParam String branchName,
                                                @RequestParam String observabilityURL) {
        logger.info("Configuring Repo: " + repoName + ", Branch: " + branchName + ", Monitoring URL: " + observabilityURL);
        try {
            RTSelector.configureRepo(repoName, branchName, observabilityURL);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
        String message = "Successfully configured Repo: " + repoName + ", Branch: " + branchName;
        logger.info("Successfully configured Repo: " + repoName + ", Branch: " + branchName);
        return ResponseEntity.ok(message);
    }

    @CrossOrigin
    @GetMapping("/selected-tests")
    public ResponseEntity<String> selectTests(@RequestParam int pr, @RequestParam String repoName,
                                              @RequestParam String branchName) {
        try {
            String selectedTests = RTSelector.selectTests(repoName, branchName, pr).toString();
            return ResponseEntity.ok(selectedTests);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @CrossOrigin
    @GetMapping("/test-results")
    public ResponseEntity<String> executeTests(@RequestParam int pr, @RequestParam String repoName,
                               @RequestParam String branchName, @RequestParam Set<String> paths) {

        try {
            String combinedOutput = executeAllTests(repoName, pr, paths);
            logger.info("Successfully executed tests for Repo: " + repoName + ", Branch: " + branchName + ", PR: " + pr);
            return ResponseEntity.ok(combinedOutput);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    @CrossOrigin
    @GetMapping("/selected-test-results")
    public ResponseEntity<String> selectTests(@RequestParam int pr, @RequestParam String repoName,
                                              @RequestParam String branchName,
                                                       @RequestParam(defaultValue = "true") boolean enableExecution) {
        try {
            Map<String, Set<String>> selectedTests = RTSelector.selectTests(repoName, branchName, pr);
            if (!enableExecution) {
                return ResponseEntity.ok(selectedTests.toString());
            }
            String combinedOutput = executeSelectedTests(repoName, pr, selectedTests);
            return ResponseEntity.ok(combinedOutput);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

}
