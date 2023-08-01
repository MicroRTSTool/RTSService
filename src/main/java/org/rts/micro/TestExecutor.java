package org.rts.micro;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class TestExecutor {

    public static String executeAllTests(String repoName, int prNumber, Set<String> paths) throws IOException, InterruptedException {
        Path tempDir = cloneRepo(repoName, prNumber);

        List<String> outputs = new ArrayList<>();

        for (String path : paths) {
            // Find directories starting with each path in the list
            List<File> matchingDirs = Files.walk(tempDir)
                    .filter(p -> Files.isDirectory(p) && p.getFileName().toString().startsWith(path))
                    .map(Path::toFile)
                    .collect(Collectors.toList());

            for (File dir : matchingDirs) {
                // Start a process builder to execute the command
                ProcessBuilder testProcess = new ProcessBuilder("bal", "test");
                testProcess.environment().put("BALLERINA_DEV_CENTRAL", "true");
                testProcess.directory(dir);
                testProcess.redirectErrorStream(true); // Combine stdout and stderr
                Process process = testProcess.start();

                // Read the output to a variable
                String output = new String(process.getInputStream().readAllBytes());
                outputs.add(output);

                process.waitFor();
            }
        }

        return String.join("\n", outputs);
    }

    public static String executeSelectedTests(String repoName, int prNumber,
                                              Map<String,Set<String>> testsMap) throws IOException, InterruptedException {
        Path tempDir = cloneRepo(repoName, prNumber);

        List<String> outputs = new ArrayList<>();

        Set<String> keys = testsMap.keySet();
        for (String path : keys) {
            Set<String> tests = testsMap.get(path);

            // Find directories starting with each path in the list
            List<File> matchingDirs = Files.walk(tempDir)
                    .filter(p -> Files.isDirectory(p) && p.getFileName().toString().startsWith(path))
                    .map(Path::toFile)
                    .collect(Collectors.toList());

            for (File dir : matchingDirs) {
                // Start a process builder to execute the command
                ProcessBuilder testProcess = new ProcessBuilder("bal", "test", "--tests",
                        String.join(",", tests));
                testProcess.environment().put("BALLERINA_DEV_CENTRAL", "true");
                testProcess.directory(dir);
                testProcess.redirectErrorStream(true); // Combine stdout and stderr
                Process process = testProcess.start();

                // Read the output to a variable
                String output = new String(process.getInputStream().readAllBytes());
                outputs.add(output);

                process.waitFor();
            }
        }

        return String.join("\n", outputs);
    }

    private static Path cloneRepo(String repoName, int prNumber) throws IOException, InterruptedException {
        String repositoryUrl = "https://github.com/" + repoName + ".git";
        // Clone the main repo to a temp directory
        Path tempDir = Files.createTempDirectory("github-pr");
        System.out.println("Created temp directory: " + tempDir);
        ProcessBuilder cloneProcess = new ProcessBuilder("git", "clone", repositoryUrl, tempDir.toString());
        cloneProcess.inheritIO().start().waitFor();

        // Fetch the specific PR
        String branchName = "pr-" + prNumber;
        ProcessBuilder fetchPRProcess = new ProcessBuilder("git", "fetch", "origin", "pull/" + prNumber + "/head:" + branchName);
        fetchPRProcess.directory(tempDir.toFile()).inheritIO().start().waitFor();

        // Checkout the PR branch
        ProcessBuilder checkoutProcess = new ProcessBuilder("git", "checkout", branchName);
        checkoutProcess.directory(tempDir.toFile()).inheritIO().start().waitFor();
        return tempDir;
    }

}
