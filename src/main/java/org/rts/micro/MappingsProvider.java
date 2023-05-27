package org.rts.micro;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MappingsProvider {

    // Expects the following at repo root. test refers to a test suite.
    // Can implement specific agent at each repo based on languages, tools that are utilized
    // 1. test_svc_mappings.json
    //    {"test1": ["svc1", "svc2"], "test2": ["svc3", "svc4"]}
    // 2. svc_path_mappings.json
    //    {"svc1": "path1", "svc2": "path2", "svc3": "path3", "svc4": "path4"}
    // 3. enabled_tests.txt
    //    test1
    //    test2

    public static Set<String> getAffectedServices(String repoName, int prNumber) throws IOException {
        return GitHubPRAnalyzer.affectedServices(repoName, prNumber);
    }

    public static Map<String, Set<String>> getSvcDependencies(String monitoringServiceUrl) throws Exception {
        return ServiceDependencyMapper.getSvcDependencies(monitoringServiceUrl);
    }
}
