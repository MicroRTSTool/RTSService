package org.rts.micro;

import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.rts.micro.models.MicroserviceProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RTSelector {

    private static final Logger logger = LoggerFactory.getLogger(RTSController.class);

    public static void configureRepo(String repoName, String branchName, String monitoringURL) throws Exception {
        // Get the affected services
        GitHub github = new GitHubBuilder().build();
        GHRepository repo = github.getRepository(repoName);
        logger.info("Got the github repo");
        GHCommit commit = repo.getCommit(repo.getBranch(branchName).getSHA1());
        String commitHash = commit.getSHA1();
        logger.info("Got the commit hash for the branch " + commitHash);
        GitHubRepoAnalyzer gitHubRepoAnalyzer = new BallerinaGHRepoAnalyzer();
        gitHubRepoAnalyzer.analyzeRepo(repo, branchName, commitHash, monitoringURL);
    }

    public static String selectTests(String repoName, String branchName, int prNumber, String observabilityToolURL) throws Exception {
        String latestCommit = GitHubRepoAnalyzer.getLatestCommit(repoName, branchName);
        List<MicroserviceProject> projects = DatabaseAccessor.fetchDataFromDb(repoName, branchName, latestCommit);
        if (projects.isEmpty()) {
            throw new Exception("No data found for the given repo, branch and commit hash. Please configure the repo first.");
        }
        List<String> changedFiles = GitHubPRAnalyzer.getChangedFiles(repoName, prNumber);
        // Get the service dependencies
        ServiceDependencyMapper mapper = new JaegerServiceDependencyMapper();
        Map<String, Set<String>> serviceDependenciesMap =
                mapper.getSvcDependencies(observabilityToolURL);

        StringBuilder stringBuilder = new StringBuilder();
        Set<String> allAffectedServices = new HashSet<>();
        for (MicroserviceProject microserviceProject : projects) {
            System.out.println("Analyzing project: " + microserviceProject.getProjectPath());
            // Get the affected services
            Set<String> affectedServices = Utils.affectedServices(microserviceProject, changedFiles);
            // Given service dependencies and affected services, get the extended graph of affected services
            if (affectedServices != null || !affectedServices.isEmpty()) {
                    allAffectedServices.addAll(affectedServices);
            }
        }
        Set<String> extendedAffectedServices = Utils.getExtendedAffectedServices(serviceDependenciesMap, allAffectedServices);
        // Get the matching tests
        for (MicroserviceProject microserviceProject : projects) {
            Map<String, Set<String>> testToSvcMappings = microserviceProject.getTestToSvcMapping();

            if (testToSvcMappings != null && !testToSvcMappings.isEmpty() &&
                    extendedAffectedServices != null && !extendedAffectedServices.isEmpty()) {
                Set<String> matchingTests = getMatchingTests(extendedAffectedServices, testToSvcMappings);
                if (matchingTests != null && !matchingTests.isEmpty()) {
                    stringBuilder.append(Utils.extractRelativePath(microserviceProject.getProjectPath()) +
                            " : " + Utils.matchingTestsArray(matchingTests));
                    stringBuilder.append("\n");
                }
            }
        }
        return stringBuilder.toString();
    }

    private static Set<String> getMatchingTests(Set<String> affectedServices, Map<String, Set<String>> testToServicesMap) {
        // Prepare a set to store matching tests
        Set<String> matchingTests = new HashSet<>();
        if (testToServicesMap != null) {
            // Iterate over each test and its associated services
            for (Map.Entry<String, Set<String>> entry : testToServicesMap.entrySet()) {
                // If there is any intersection between the services of the current test and the affected services
                if (Utils.hasIntersection(entry.getValue(), affectedServices)) {
                    // Add the test to the set of matching tests
                    matchingTests.add(entry.getKey());
                }
            }
        }
        return matchingTests;
    }


}





