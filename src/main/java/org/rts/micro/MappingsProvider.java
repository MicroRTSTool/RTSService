package org.rts.micro;

import org.rts.micro.models.MicroserviceProject;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MappingsProvider {

    public static Set<String> getAffectedServices(MicroserviceProject project, List<String> changedFiles) throws IOException {
        return GitHubPRAnalyzer.affectedServices(project, changedFiles);
    }

    public static Map<String, Set<String>> getSvcDependencies(String monitoringServiceUrl) throws Exception {
        return ServiceDependencyMapper.getSvcDependencies(monitoringServiceUrl);
    }
}
