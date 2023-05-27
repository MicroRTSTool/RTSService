package org.rts.micro;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.kohsuke.github.*;

import java.io.IOException;
import java.util.*;

public class GitHubRepoAnalyzer {

    private static final String OAUTH_TOKEN = "your-github-oauth-token";
//    private static final String REPO_NAME = "owner/repo"; // change to your repo name

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

    public static String getTestToServices(GHRepository repo, String branchName) throws IOException {
        // assuming test_svc_mappings.json is in the root of the repo
        GHContent content = repo.getFileContent("test_svc_mappings.json", branchName);
        return new String(content.read().readAllBytes());
    }

    public static String getServicePathMappings(GHRepository repo, String branchName) throws IOException {
        // assuming svc_path_mappings.json is in the root of the repo
        GHContent content = repo.getFileContent("svc_path_mappings.json", branchName);
        return new String(content.read().readAllBytes());
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
}
