package org.rts.micro;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.kohsuke.github.GHRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BallerinaGHRepoAnalyzer extends GitHubRepoAnalyzer {

    private static final Logger logger = LoggerFactory.getLogger(BallerinaGHRepoAnalyzer.class);

    public void analyzeRepo(String repoName, int pr, String monitoringURL)
            throws IOException, GitAPIException, InterruptedException, SQLException {
        logger.info("Started analyzing repo: " + repoName + ", PR: " + pr);

        // Clear previous entries for the same repo and pr
        DatabaseAccessor.deleteFromDb(repoName, pr);

        Path tempDir = Utils.cloneRepo(repoName, pr);

        // Find Ballerina.toml files
        try (Stream<Path> paths = Files.walk(tempDir)) {
            List<Path> tomlFiles = paths
                    .filter(p -> p.getFileName().toString().equals(BALLERINA_TOML))
                    .collect(Collectors.toList());

            for (Path toml : tomlFiles) {
                Path parentDir = toml.getParent();

                // Find .bal files in the same directory
                try (Stream<Path> balPaths = Files.list(parentDir)) {
                    Optional<Path> firstBalFile = balPaths
                            .filter(p -> p.toString().endsWith(BAL_EXTENSION))
                            .findFirst();

                    if (firstBalFile.isPresent()) {
                        Path balFile = firstBalFile.get();
                        List<String> lines = Files.readAllLines(balFile);
                        lines.add(0, "import microrts/static_analyzer as _;");
                        Files.write(balFile, lines);
                    }
                }


                // Execute bal build
                ProcessBuilder processBuilder = new ProcessBuilder("bal", "build");
                processBuilder.environment().put("BALLERINA_DEV_CENTRAL", "true");
                processBuilder.directory(parentDir.toFile());
                processBuilder.inheritIO(); // This will output the logs to the console
                Process process = processBuilder.start();
                process.waitFor();

                String packageRoot = parentDir.toString();
                String testSvcMappingsContent = Utils.readContentIfPresentAndNotEmpty(parentDir, TEST_SVC_MAPPINGS_JSON);
                String svcPathMappingsContent = Utils.readContentIfPresentAndNotEmpty(parentDir, SVC_PATH_MAPPINGS_JSON);

                // Insert into DB
                DatabaseAccessor.insertIntoDb(repoName, pr,
                        testSvcMappingsContent, svcPathMappingsContent, monitoringURL, packageRoot);
            }
        }

        Utils.deleteDirectory(tempDir.toFile());
    }
}
