package org.rts.micro.models;

import java.util.Map;
import java.util.Set;

public class MicroserviceProject {
    private String repo;

    private int pr;
    private Map<String, Set<String>> testToSvcMapping;
    private Map<String, String> serviceToPathMapping;

    public String getObservabilityToolURL() {
        return observabilityToolURL;
    }

    private String observabilityToolURL;
    private String projectPath;

    public MicroserviceProject(String repo, int pr,
                               Map<String, Set<String>> testToSvcMapping, Map<String, String> serviceToPathMapping,
                               String observabilityToolURL, String projectPath) {
        this.repo = repo;
        this.testToSvcMapping = testToSvcMapping;
        this.serviceToPathMapping = serviceToPathMapping;
        this.observabilityToolURL = observabilityToolURL;
        this.projectPath = projectPath;
    }

    public Map<String, Set<String>> getTestToSvcMapping() { return testToSvcMapping; }

    public Map<String, String> getServiceToPathMapping() { return serviceToPathMapping; }

    public String getProjectPath() {
        return projectPath;
    }

    public void setProjectPath(String projectPath) {
        this.projectPath = projectPath;
    }

}
