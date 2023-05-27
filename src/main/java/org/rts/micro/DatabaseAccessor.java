package org.rts.micro;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseAccessor {
    private static String url = "jdbc:mysql://localhost:3306/repo_details";
    private static String user = "root";
    private static String password = "root123!";
    public static void insertIntoDb(String repo, String branch, String lastCommit, String testToSvcMapping, String serviceToPathMapping) {


        String sql = "INSERT INTO GithubRepos (repo, branch, last_commit, test_to_svc_mapping, service_to_path_mapping) VALUES (?, ?, ?, ?, ?)";

        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setString(1, repo);
            pst.setString(2, branch);
            pst.setString(3, lastCommit);
            pst.setString(4, testToSvcMapping);
            pst.setString(5, serviceToPathMapping);

            pst.executeUpdate();

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static void fetchDataFromDb(String repoName, String branchName, String commitHash) {
        String sql = "SELECT * FROM GithubRepos WHERE repo = ? AND branch = ? AND last_commit = ?";

        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setString(1, repoName);
            pst.setString(2, branchName);
            pst.setString(3, commitHash);

            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                String testToSvcMapping = rs.getString("test_to_svc_mapping");
                String serviceToPathMapping = rs.getString("service_to_path_mapping");

                System.out.println("Test to Service Mapping: " + testToSvcMapping);
                System.out.println("Service to Path Mapping: " + serviceToPathMapping);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }


}
