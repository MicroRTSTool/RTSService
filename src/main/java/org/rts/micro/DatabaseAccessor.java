package org.rts.micro;

import org.rts.micro.models.GitHubRepo;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;

public class DatabaseAccessor {
    private static String url = "jdbc:mysql://localhost:3306/repo_details";
    private static String user = "root";
    private static String password = "root123!";

    public static void insertIntoDb(String repo, String branch, String lastCommit, String testToSvcMapping,
                                    String serviceToPathMapping, String monitoringURL) throws SQLException {

        String sql =
                "INSERT INTO GithubRepos (repo, branch, last_commit, test_to_svc_mapping, service_to_path_mapping, monitoring_url) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setString(1, repo);
            pst.setString(2, branch);
            pst.setString(3, lastCommit);
            pst.setString(4, testToSvcMapping);
            pst.setString(5, serviceToPathMapping);
            pst.setString(6, monitoringURL);

            pst.executeUpdate();

        } catch (SQLException ex) {
            throw ex;
        }
    }

    public static GitHubRepo fetchDataFromDb(String repoName, String branchName, String commitHash) throws IOException, SQLException {

        String sql = "SELECT * FROM GithubRepos WHERE repo = ? AND branch = ? AND last_commit = ?";

        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setString(1, repoName);
            pst.setString(2, branchName);
            pst.setString(3, commitHash);

            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                try {
                    String testToSvcMapping = rs.getString("test_to_svc_mapping");
                    String serviceToPathMapping = rs.getString("service_to_path_mapping");
                    String monitoringUrl = rs.getString("monitoring_url");

                    Map<String, String> serviceToPathMap = GitHubRepoAnalyzer.getServicePathMappings(serviceToPathMapping);
                    Map<String, Set<String>> testToSvcMap = GitHubRepoAnalyzer.getTestToServicesMap(testToSvcMapping);
                    GitHubRepo gitHubRepo = new GitHubRepo(repoName, branchName, commitHash, testToSvcMap,
                            serviceToPathMap, monitoringUrl);
                    return gitHubRepo;

                } catch (IOException e) {
                    throw e;
                }
            }
        } catch (SQLException ex) {
            throw ex;
        }
        return null;
    }


}
