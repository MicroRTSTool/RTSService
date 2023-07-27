package org.rts.micro.models;

import java.util.Map;
import java.util.Set;

public class MicroserviceProject {
    private String repo;
    private String branch;
    private String lastCommit;
    private Map<String, Set<String>> testToSvcMapping;
    private Map<String, String> serviceToPathMapping;
    private String monitoringUrl;
    private String projectPath;

    public MicroserviceProject(String repo, String branch, String lastCommit,
                               Map<String, Set<String>> testToSvcMapping, Map<String, String> serviceToPathMapping,
                               String monitoringUrl, String projectPath) {
        this.repo = repo;
        this.branch = branch;
        this.lastCommit = lastCommit;
        this.testToSvcMapping = testToSvcMapping;
        this.serviceToPathMapping = serviceToPathMapping;
        this.monitoringUrl = monitoringUrl;
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
