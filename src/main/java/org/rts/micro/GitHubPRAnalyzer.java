package org.rts.micro;

import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHPullRequestFileDetail;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GitHubPRAnalyzer {

    private static final String OAUTH_TOKEN = "your-github-oauth-token";

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
