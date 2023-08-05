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
    public ResponseEntity<String> configureRepo(@RequestParam String repoName, @RequestParam int pr,
                                                @RequestParam String observabilityURL) {
        logger.info("Configuring Repo: " + repoName + ", PR: " + pr + ", Monitoring URL: " + observabilityURL);
        try {
            RTSelector.configureRepo(repoName, pr, observabilityURL);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
        String message = "Successfully configured Repo: " + repoName + ", PR: " + pr;
        logger.info("Successfully configured Repo: " + repoName + ", PR: " + pr);
        return ResponseEntity.ok(message);
    }

    @CrossOrigin
    @GetMapping("/test-results")
    public ResponseEntity<String> executeTests(@RequestParam int pr, @RequestParam String repoName, @RequestParam Set<String> paths) {

        try {
            String combinedOutput = executeAllTests(repoName, pr, paths);
            logger.info("Successfully executed tests for Repo: " + repoName + ", PR: " + pr);
            return ResponseEntity.ok(combinedOutput);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    @CrossOrigin
    @GetMapping("/selected-test-results")
    public ResponseEntity<String> selectTests(@RequestParam String pr, @RequestParam String repoName,
                                                       @RequestParam(defaultValue = "true") boolean enableExecution) {
        try {
            Map<String, Set<String>> selectedTests = RTSelector.selectTests(repoName, Integer.valueOf(pr));
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
