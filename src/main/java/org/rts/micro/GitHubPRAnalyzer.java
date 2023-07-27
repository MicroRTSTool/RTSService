package org.rts.micro;

import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHPullRequestFileDetail;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.rts.micro.models.MicroserviceProject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GitHubPRAnalyzer {

    private static final String OAUTH_TOKEN = "your-github-oauth-token";

    public static Set<String> affectedServices(MicroserviceProject project, List<String> changedFiles) {
        Map<String, String> svcPathMappings = project.getServiceToPathMapping();

        Set<String> affectedServices = new HashSet<>();
        if (svcPathMappings != null) {
            for (String fileName : changedFiles) {
                for (Map.Entry<String, String> entry : svcPathMappings.entrySet()) {
                    if (fileName.startsWith(Utils.extractRelativePath(entry.getValue()))) {
                        affectedServices.add(entry.getKey());
                        break;
                    }
                }
            }
        }
        return affectedServices;
    }

    public static List<String> getChangedFiles(String repoName, int prNumber) throws IOException {
        GitHub github = new GitHubBuilder().build();
        GHRepository repo = github.getRepository(repoName);
        GHPullRequest pr = repo.getPullRequest(prNumber);

        List<String> changedFiles = new ArrayList<>();
        for (GHPullRequestFileDetail fileDetail : pr.listFiles()) {
            String fileName = fileDetail.getFilename();
            changedFiles.add(fileName);
        }
        return changedFiles;
    }
}
