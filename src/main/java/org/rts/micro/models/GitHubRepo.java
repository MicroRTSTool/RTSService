package org.rts.micro.models;

import java.util.Map;
import java.util.Set;

public class GitHubRepo {
    private int id;
    private String repo;
    private String branch;
    private String lastCommit;
    private Map<String, Set<String>> testToSvcMapping;
    private Map<String, String> serviceToPathMapping;
    private String monitoringUrl;

    // Constructors
    public GitHubRepo() {}

    public GitHubRepo(String repo, String branch, String lastCommit,
                      Map<String, Set<String>> testToSvcMapping, Map<String, String> serviceToPathMapping,
                      String monitoringUrl) {
        this.repo = repo;
        this.branch = branch;
        this.lastCommit = lastCommit;
        this.testToSvcMapping = testToSvcMapping;
        this.serviceToPathMapping = serviceToPathMapping;
        this.monitoringUrl = monitoringUrl;
    }

    // Getters and setters for all fields

    public String getRepo() { return repo; }
    public void setRepo(String repo) { this.repo = repo; }

    public String getBranch() { return branch; }
    public void setBranch(String branch) { this.branch = branch; }

    public String getLastCommit() { return lastCommit; }
    public void setLastCommit(String lastCommit) { this.lastCommit = lastCommit; }

    public Map<String, Set<String>> getTestToSvcMapping() { return testToSvcMapping; }
    public void setTestToSvcMapping(Map<String, Set<String>> testToSvcMapping) { this.testToSvcMapping = testToSvcMapping; }

    public Map<String, String> getServiceToPathMapping() { return serviceToPathMapping; }
    public void setServiceToPathMapping(Map<String, String> serviceToPathMapping) { this.serviceToPathMapping = serviceToPathMapping; }

    public String getMonitoringUrl() { return monitoringUrl; }
    public void setMonitoringUrl(String monitoringUrl) { this.monitoringUrl = monitoringUrl; }
}
