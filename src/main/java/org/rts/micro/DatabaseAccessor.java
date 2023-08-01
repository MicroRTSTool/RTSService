package org.rts.micro;

import org.rts.micro.models.MicroserviceProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
            System.getenv("DB_HOST") != null ? System.getenv("DB_HOST") : "jdbc:mysql://10.101.160.3:3306/configsdb";
    private static String user = System.getenv("DB_USER") != null ? System.getenv("DB_USER") : "root";
    private static String password = System.getenv("DB_PWD") != null ? System.getenv("DB_PWD") : "microrts";

    private static final Logger logger = LoggerFactory.getLogger(DatabaseAccessor.class);

    public static void insertIntoDb(String repo, int pr, String testToSvcMapping,
                                    String serviceToPathMapping, String observabilityUrl, String projectPath) throws SQLException {
        logger.info("Trying to connect to " + url + " with user " + user);

        String sql =
                "INSERT INTO repository_info (repo, pr, test_to_svc_mapping, service_to_path_mapping, " +
                        "observability_url, project_path) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setString(1, repo);
            pst.setInt(2, pr);
            pst.setString(3, testToSvcMapping);
            pst.setString(4, serviceToPathMapping);
            pst.setString(5, observabilityUrl);
            pst.setString(6, projectPath);

            pst.executeUpdate();
            logger.info("Inserted new entry for repo: " + repo + ", pr: " + pr);

        } catch (SQLException ex) {
            throw ex;
        }
    }

    public static void deleteFromDb(String repo, int pr) throws SQLException {
        logger.info("Trying to connect to " + url + " with user " + user);
        String sql =
                "DELETE FROM repository_info WHERE repo = ? AND pr = ?";

        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setString(1, repo);
            pst.setInt(2, pr);
            pst.executeUpdate();
            logger.info("Deleted previous entries for repo: " + repo + ", pr: " + pr);

        } catch (SQLException ex) {
            logger.error("Error deleting previous entries for repo: " + repo + ", pr: " + pr);
            logger.error(ex.getMessage());
            throw ex;
        }
    }

    public static List<MicroserviceProject> fetchDataFromDb(String repoName, int pr)
            throws IOException, SQLException {
        logger.info("Trying to connect to " + url + " with user " + user);

        List<MicroserviceProject> projects = new ArrayList<>();
        String sql = "SELECT * FROM repository_info WHERE repo = ? AND pr = ? ";

        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setString(1, repoName);
            pst.setInt(2, pr);

            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                try {
                    String testToSvcMapping = rs.getString("test_to_svc_mapping");
                    String serviceToPathMapping = rs.getString("service_to_path_mapping");
                    String observabilityToolURL = rs.getString("observability_url");

                    Map<String, String> serviceToPathMap =
                            serviceToPathMapping != null ? Utils.parseJson(serviceToPathMapping) : null;
                    Map<String, Set<String>> testToSvcMap =
                            testToSvcMapping != null ? Utils.getMapFromJson(testToSvcMapping) : null;
                    MicroserviceProject microserviceProject = new MicroserviceProject(repoName, pr, testToSvcMap,
                            serviceToPathMap, observabilityToolURL, rs.getString("project_path"));
                    projects.add(microserviceProject);

                } catch (IOException e) {
                    throw e;
                }
            }
            logger.info("Fetched data from db for repo: " + repoName + ", pr: " + pr);
        } catch (SQLException ex) {
            throw ex;
        }
        return projects;
    }


}
