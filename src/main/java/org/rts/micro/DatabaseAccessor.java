package org.rts.micro;

import org.rts.micro.models.MicroserviceProject;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DatabaseAccessor {
    private static String url =
            System.getenv("DB_HOST") != null ? System.getenv("DB_HOST") : "jdbc:mysql://localhost:3306/repo_details";
    private static String user = System.getenv("DB_USER") != null ? System.getenv("DB_USER") : "root";
    private static String password = System.getenv("DB_PWD") != null ? System.getenv("DB_PWD") : "root123!";


    public static void insertIntoDb(String repo, String branch, String lastCommit, String testToSvcMapping,
                                    String serviceToPathMapping, String monitoringURL, String projectPath) throws SQLException {

        String sql =
                "INSERT INTO ProjectDetails (repo, branch, last_commit, test_to_svc_mapping, service_to_path_mapping, " +
                        "monitoring_url, project_path) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setString(1, repo);
            pst.setString(2, branch);
            pst.setString(3, lastCommit);
            pst.setString(4, testToSvcMapping);
            pst.setString(5, serviceToPathMapping);
            pst.setString(6, monitoringURL);
            pst.setString(7, projectPath);

            pst.executeUpdate();

        } catch (SQLException ex) {
            throw ex;
        }
    }

    public static void deleteFromDb(String repo, String branch) throws SQLException {

        String sql =
                "DELETE FROM ProjectDetails WHERE repo = ? AND branch = ?";

        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setString(1, repo);
            pst.setString(2, branch);
            pst.executeUpdate();

        } catch (SQLException ex) {
            throw ex;
        }
    }

    public static List<MicroserviceProject> fetchDataFromDb(String repoName, String branchName, String commitHash)
            throws IOException, SQLException {

        List<MicroserviceProject> projects = new ArrayList<>();
        String sql = "SELECT * FROM ProjectDetails WHERE repo = ? AND branch = ? AND last_commit = ?";

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

                    Map<String, String> serviceToPathMap =
                            serviceToPathMapping != null ? Utils.parseJson(serviceToPathMapping) : null;
                    Map<String, Set<String>> testToSvcMap =
                            testToSvcMapping != null ? Utils.getMapFromJson(testToSvcMapping) : null;
                    MicroserviceProject microserviceProject = new MicroserviceProject(repoName, branchName, commitHash, testToSvcMap,
                            serviceToPathMap, monitoringUrl, rs.getString("project_path"));
                    projects.add(microserviceProject);

                } catch (IOException e) {
                    throw e;
                }
            }
        } catch (SQLException ex) {
            throw ex;
        }
        return projects;
    }


}
