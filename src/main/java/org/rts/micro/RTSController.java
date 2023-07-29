package org.rts.micro;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RTSController {

    private static final Logger logger = LoggerFactory.getLogger(RTSController.class);

    @CrossOrigin
    @GetMapping("/configure")
    public ResponseEntity<String> configureRepo(@RequestParam String repoName, @RequestParam String branchName,
                                                @RequestParam String monitoringURL) {
        logger.info("Configuring Repo: " + repoName + ", Branch: " + branchName + ", Monitoring URL: " + monitoringURL);
        try {
            RTSelector.configureRepo(repoName, branchName, monitoringURL);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
        String message = "Successfully configured Repo: " + repoName + ", Branch: " + branchName;
        logger.info("Successfully configured Repo: " + repoName + ", Branch: " + branchName);
        return ResponseEntity.ok(message);
    }

    @CrossOrigin
    @GetMapping("/rts")
    public ResponseEntity<String> selectTests(@RequestParam int pr, @RequestParam String repoName,
                                              @RequestParam String branchName, @RequestParam String monitoringURL) {
        try {
            String selectedTests = RTSelector.selectTests(repoName, branchName, pr, monitoringURL);
            return ResponseEntity.ok(selectedTests);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }

    }

}
