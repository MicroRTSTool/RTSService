package org.rts.micro;

import org.rts.micro.models.MicroserviceProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RTSelector {

    private static final Logger logger = LoggerFactory.getLogger(RTSController.class);

    public static void configureRepo(String repoName, int pr, String monitoringURL) throws Exception {
        // static analysis
        GitHubRepoAnalyzer gitHubRepoAnalyzer = new BallerinaGHRepoAnalyzer();
        gitHubRepoAnalyzer.analyzeRepo(repoName, pr, monitoringURL);
    }

    public static Map<String, Set<String>> selectTests(String repoName, int prNumber) throws Exception {
        List<MicroserviceProject> projects = DatabaseAccessor.fetchDataFromDb(repoName, prNumber);
        if (projects.isEmpty()) {
            throw new Exception("No data found for the given repo, branch and commit hash. Please configure the repo first.");
        }
        List<String> changedFiles = GitHubPRAnalyzer.getChangedFiles(repoName, prNumber);
        logger.info("Got the changed files");
        // Get the service dependencies
        ServiceDependencyMapper mapper = new JaegerServiceDependencyMapper();
        Map<String, Set<String>> serviceDependenciesMap =
                mapper.getSvcDependencies(projects.get(0).getObservabilityToolURL());

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
        Map<String, Set<String>> testsToSvcMappings = new HashMap<>();
        // Get the matching tests
        for (MicroserviceProject microserviceProject : projects) {
            Map<String, Set<String>> testsMap = microserviceProject.getTestToSvcMapping();

            if (testsMap != null && !testsMap.isEmpty() &&
                    extendedAffectedServices != null && !extendedAffectedServices.isEmpty()) {
                Set<String> matchingTests = getMatchingTests(extendedAffectedServices, testsMap);
                testsToSvcMappings.put(Utils.extractRelativePath(microserviceProject.getProjectPath()), matchingTests);
            }
        }
        return testsToSvcMappings;
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





