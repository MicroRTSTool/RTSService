package org.rts.micro;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
public class RTSController {

    @CrossOrigin
    @GetMapping("/configure")
    public ResponseEntity<String> configureRepo(@RequestParam String repoName, @RequestParam String branchName,
                                                @RequestParam String monitoringURL) {
        try {
            RTSelector.configureRepo(repoName, branchName, monitoringURL);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
        String message = "Successfully configured Repo: " + repoName + ", Branch: " + branchName;
        System.out.println(message);
        return ResponseEntity.ok(message);
    }

    @CrossOrigin
    @GetMapping("/rts")
    public ResponseEntity<String> selectTests(@RequestParam int pr, @RequestParam String repoName,
                                              @RequestParam String branchName) {
        try {
            String selectedTests = RTSelector.selectTests(repoName, branchName,pr);
            return ResponseEntity.ok(selectedTests);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }

    }

}
