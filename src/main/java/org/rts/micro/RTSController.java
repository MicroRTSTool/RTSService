package org.rts.micro;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RTSController {

    @GetMapping("/configure")
    public ResponseEntity<String> configureRepo(@RequestParam String repoName, @RequestParam String branchName) {
        try {
            RTSelector.configureRepo(repoName, branchName);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
        String message = "Successfully configured Repo: " + repoName + ", Branch: " + branchName;
        return ResponseEntity.ok(message);
    }

}
