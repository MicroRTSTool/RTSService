package org.rts.micro;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;

import java.io.IOException;
import java.sql.SQLException;

public abstract class GitHubRepoAnalyzer {

    public static final String BALLERINA_TOML = "Ballerina.toml";
    public static final String BAL_EXTENSION = ".bal";
    public static final String TEST_SVC_MAPPINGS_JSON = "test_svc_mappings.json";
    public static final String SVC_PATH_MAPPINGS_JSON = "svc_path_mappings.json";


    public static String getLatestCommit(String repoName, String branchName) throws IOException {
        GitHub github = new GitHubBuilder().build();
        GHRepository repo = github.getRepository(repoName);
        return repo.getBranch(branchName).getSHA1();
    }

    public abstract void analyzeRepo(String repoName, int pr, String monitoringURL)
            throws IOException, GitAPIException, InterruptedException, SQLException;

}
