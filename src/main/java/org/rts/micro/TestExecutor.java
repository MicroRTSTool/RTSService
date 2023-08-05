package org.rts.micro;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger logger = LoggerFactory.getLogger(RTSController.class);

    public static String executeAllTests(String repoName, int prNumber, Set<String> paths) throws IOException, InterruptedException {


        Path tempDir = Utils.cloneRepo(repoName, prNumber);

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
        Path tempDir = Utils.cloneRepo(repoName, prNumber);

        List<String> outputs = new ArrayList<>();

        Set<String> keys = testsMap.keySet();
        for (String path : keys) {
            Set<String> tests = testsMap.get(path);

            // Find directories starting with each path in the list
            List<File> matchingDirs = Files.walk(tempDir)
                    .filter(p -> Files.isDirectory(p) && p.getFileName().toString().startsWith(path))
                    .map(Path::toFile)
                    .collect(Collectors.toList());
            logger.info("Matching dirs: " + matchingDirs.toString());

            for (File dir : matchingDirs) {
                // Start a process builder to execute the command
                ProcessBuilder testProcess = new ProcessBuilder("bal", "test", "--tests",
                        String.join(",", tests));
                testProcess.directory(dir);
                testProcess.redirectErrorStream(true); // Combine stdout and stderr
                Process process = testProcess.start();

                // Read the output to a variable
                String output = new String(process.getInputStream().readAllBytes());
                outputs.add(output);

                process.waitFor();
            }
        }
        logger.info("Outputs: " + outputs.toString());
        return String.join("\n", outputs);
    }



}
