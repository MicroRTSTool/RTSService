package org.rts.micro;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GitHubRepoAnalyzer {

    private static final String OAUTH_TOKEN = "xxxx";
    public static final String BALLERINA_TOML = "Ballerina.toml";
    private static final String BAL_EXTENSION = ".bal";
    public static final String TEST_SVC_MAPPINGS_JSON = "test_svc_mappings.json";
    public static final String SVC_PATH_MAPPINGS_JSON = "svc_path_mappings.json";


    public static String getLatestCommit(String repoName, String branchName) throws IOException {
        GitHub github = new GitHubBuilder().build();
        GHRepository repo = github.getRepository(repoName);
        return repo.getBranch(branchName).getSHA1();
    }
    public static Map<String, Set<String>> getTestToServicesMap(String json) throws IOException {
        // Use Jackson library to parse the JSON string into a Map
        ObjectMapper mapper = new ObjectMapper();
        Map<String, List<String>> map = mapper.readValue(json, Map.class);

        // Convert List to Set for each test
        Map<String, Set<String>> testToServicesMap = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : map.entrySet()) {
            testToServicesMap.put(entry.getKey(), new HashSet<>(entry.getValue()));
        }

        return testToServicesMap;
    }

    public static Map<String, String> getServicePathMappings(String json) throws IOException {
        // Use a JSON library like Jackson or Gson to parse the JSON string into a Map
        // This is a placeholder and won't compile
        Map<String, String> svcPathMappings = parseJson(json);
        return svcPathMappings;
    }

    private static Map<String, String> parseJson(String json) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(json, new TypeReference<Map<String,String>>(){});
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new HashMap<>();
    }

    public static void analyzeRepo(GHRepository repo, String branchName, String commitHash, String monitoringURL)
            throws IOException, GitAPIException, InterruptedException, SQLException {
        // TODO: Remove when pushed to prod
        //  Set environment variable
        System.setProperty("BALLERINA_DEV_CENTRAL", "true");

        String repoUrl = repo.getHttpTransportUrl();
        // Clear previous entries for the same repo and branch
        DatabaseAccessor.deleteFromDb(repo.getFullName(), branchName);

        Path tempDir = Files.createTempDirectory("gitCloneTempDir");
        Git.cloneRepository()
                .setURI(repoUrl)
                .setBranch(branchName)
                .setDirectory(tempDir.toFile())
                .call();

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
                String testSvcMappingsContent = readContentIfPresentAndNotEmpty(parentDir, TEST_SVC_MAPPINGS_JSON);
                String svcPathMappingsContent = readContentIfPresentAndNotEmpty(parentDir, SVC_PATH_MAPPINGS_JSON);

                // Insert into DB
                DatabaseAccessor.insertIntoDb(repo.getFullName(), branchName, commitHash,
                        testSvcMappingsContent, svcPathMappingsContent, monitoringURL, packageRoot);
            }
        }

        deleteDirectory(tempDir.toFile());
    }

    private static String readContentIfPresentAndNotEmpty(Path dir, String fileName) throws IOException {
        Path filePath = dir.resolve(fileName);
        if (Files.exists(filePath)) {
            String content = new String(Files.readAllBytes(filePath));
            if (!content.trim().isEmpty()) {
                return content;
            }
        }
        return null;
    }

    private static void deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        directoryToBeDeleted.delete();
    }

}
