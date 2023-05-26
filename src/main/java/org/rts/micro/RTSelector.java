package org.rts.micro;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RTSelector {
    public static void main(String[] args) throws Exception {
        String repoName = "Dilhasha/ecommerce-sample";
        int prNumber = 1;
        String monitoringURL = "http://localhost:16686";

        // Get the service dependencies
        Map<String, Set<String>> serviceDependenciesMap = MappingsProvider.getSvcDependencies(monitoringURL);
        // Get the affected services
        String branchName = "simple-microservices";
        Set<String> affectedServices = MappingsProvider.getAffectedServices(repoName, branchName, prNumber);
        // Given service dependencies and affected services, get the extended graph of affected services
        Set<String> allAffectedServices = getExtendedAffectedServices(serviceDependenciesMap, affectedServices);
        // Get the test to service mapping
        Map<String, Set<String>> testToServicesMap = MappingsProvider.getTestToSvcMapping(repoName, branchName);
        // Get the matching tests
        Set<String> matchingTests = getMatchingTests(allAffectedServices, testToServicesMap);
        System.out.println("Matching tests for repo " + repoName + ": " + matchingTests);

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

        // Iterate over each test and its associated services
        for (Map.Entry<String, Set<String>> entry : testToServicesMap.entrySet()) {
            // If there is any intersection between the services of the current test and the affected services
            if (!Collections.disjoint(entry.getValue(), affectedServices)) {
                // Add the test to the set of matching tests
                matchingTests.add(entry.getKey());
            }
        }
        return matchingTests;
    }

    public static List<String> readRepoList() throws IOException {
        return Files.readAllLines(Paths.get("repolist.txt"));
    }

}





