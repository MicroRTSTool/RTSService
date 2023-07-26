package org.rts.micro;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.rts.micro.models.MicroserviceProject;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RTSelector {

    public static void configureRepo(String repoName, String branchName, String monitoringURL) throws Exception {
        // Get the affected services
        GitHub github = new GitHubBuilder().build();
        GHRepository repo = github.getRepository(repoName);
        GHCommit commit = repo.getCommit(repo.getBranch(branchName).getSHA1());
        String commitHash = commit.getSHA1();
        GitHubRepoAnalyzer.analyzeRepo(repo, branchName, commitHash, monitoringURL);
    }

    public static String selectTests(String repoName, String branchName, int prNumber, String monitoringUrl) throws Exception {
        System.out.println("hit the select tests");
        String latestCommit = GitHubRepoAnalyzer.getLatestCommit(repoName, branchName);
        System.out.println("latest commit: " + latestCommit);
        List<MicroserviceProject> projects = DatabaseAccessor.fetchDataFromDb(repoName, branchName, latestCommit);
        if (projects.isEmpty()) {
            throw new Exception("No data found for the given repo, branch and commit hash. Please configure the repo first.");
        }
        List<String> changedFiles = GitHubPRAnalyzer.getChangedFiles(repoName, prNumber);
        // Get the service dependencies
        Map<String, Set<String>> serviceDependenciesMap =
                MappingsProvider.getSvcDependencies(monitoringUrl);

        StringBuilder stringBuilder = new StringBuilder();
        Set<String> allAffectedServices = new HashSet<>();
        for (MicroserviceProject microserviceProject : projects) {
            System.out.println("Analyzing project: " + microserviceProject.getProjectPath());
            // Get the affected services
            Set<String> affectedServices = MappingsProvider.getAffectedServices(microserviceProject, changedFiles);
            // Given service dependencies and affected services, get the extended graph of affected services
            if (affectedServices != null || !affectedServices.isEmpty()) {
                    allAffectedServices.addAll(affectedServices);
            }
        }
        Set<String> extendedAffectedServices = getExtendedAffectedServices(serviceDependenciesMap, allAffectedServices);
        // Get the matching tests
        for (MicroserviceProject microserviceProject : projects) {
            Map<String, Set<String>> testToSvcMappings = microserviceProject.getTestToSvcMapping();

            if (testToSvcMappings != null && !testToSvcMappings.isEmpty() &&
                    extendedAffectedServices != null && !extendedAffectedServices.isEmpty()) {
                Set<String> matchingTests = getMatchingTests(extendedAffectedServices, testToSvcMappings);
                if (matchingTests != null && !matchingTests.isEmpty()) {
                    stringBuilder.append(GitHubPRAnalyzer.extractRelativePath(microserviceProject.getProjectPath()) +
                            " : " + matchingTestsArray(matchingTests));
                    stringBuilder.append("\n");
                }
            }
        }
        return stringBuilder.toString();
    }

    private static String matchingTestsArray(Set<String> matchingTests) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(matchingTests);
    }


    public static Set<String> getExtendedAffectedServices(Map<String, Set<String>> serviceDependenciesMap,
                                                          Set<String> affectedServices) {
        // Prepare a set to store all affected services
        Set<String> allAffectedServices = new HashSet<>(affectedServices);

        // Iterate over the directly affected services
        for (String service : affectedServices) {
            // Get the dependencies of the current service
            Set<String> dependencies = serviceDependenciesMap.get(service);

            // Add the dependencies to the set of all affected services
            if (dependencies != null) {
                allAffectedServices.addAll(dependencies);
            }
        }
        return allAffectedServices;
    }


    public static Set<String> getMatchingTests(Set<String> affectedServices, Map<String, Set<String>> testToServicesMap) {
        // Prepare a set to store matching tests
        Set<String> matchingTests = new HashSet<>();
        if (testToServicesMap != null) {
            // Iterate over each test and its associated services
            for (Map.Entry<String, Set<String>> entry : testToServicesMap.entrySet()) {
                // If there is any intersection between the services of the current test and the affected services
                if (hasIntersection(entry.getValue(), affectedServices)) {
                    // Add the test to the set of matching tests
                    matchingTests.add(entry.getKey());
                }
            }
        }
        return matchingTests;
    }

    public static boolean hasIntersection(Set<String> set1, Set<String> set2) {
        for (String s1 : set1) {
            for (String s2 : set2) {
                if (s1.endsWith(s2)) {
                    return true;
                }
            }
        }
        return false;
    }

}





