package org.rts.micro;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.rts.micro.models.MicroserviceProject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Utils {
    public static String extractRelativePath(String fullPath) {
        String[] parts = fullPath.split("gitCloneTempDir[0-9]+/");
        if (parts.length > 1) {
            return parts[1];
        }
        return fullPath;
    }

    public static Map<String, String> parseJson(String json) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(json, new TypeReference<Map<String,String>>(){});
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new HashMap<>();
    }

    public static String readContentIfPresentAndNotEmpty(Path dir, String fileName) throws IOException {
        Path filePath = dir.resolve(fileName);
        if (Files.exists(filePath)) {
            String content = new String(Files.readAllBytes(filePath));
            if (!content.trim().isEmpty()) {
                return content;
            }
        }
        return null;
    }

    public static void deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        directoryToBeDeleted.delete();
    }

    public static Map<String, Set<String>> getMapFromJson(String json) throws IOException {
        // Use Jackson library to parse the JSON string into a Map
        ObjectMapper mapper = new ObjectMapper();
        Map<String, List<String>> map = mapper.readValue(json, Map.class);

        // Convert List to Set for each test
        Map<String, Set<String>> stringSetHashMap = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : map.entrySet()) {
            stringSetHashMap.put(entry.getKey(), new HashSet<>(entry.getValue()));
        }

        return stringSetHashMap;
    }

    public static boolean hasIntersection(Set<String> set1, Set<String> set2) {
        for (String s1 : set1) {
            for (String s2 : set2) {
                if (s1.endsWith(s2)) {
                    return true;
                }
            }
        }
        return false;
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

    public static String matchingTestsArray(Set<String> matchingTests) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(matchingTests);
    }

    public static Set<String> affectedServices(MicroserviceProject project, List<String> changedFiles) {
        Map<String, String> svcPathMappings = project.getServiceToPathMapping();

        Set<String> affectedServices = new HashSet<>();
        if (svcPathMappings != null) {
            for (String fileName : changedFiles) {
                for (Map.Entry<String, String> entry : svcPathMappings.entrySet()) {
                    if (fileName.startsWith(Utils.extractRelativePath(entry.getValue()))) {
                        affectedServices.add(entry.getKey());
                        break;
                    }
                }
            }
        }
        return affectedServices;
    }
}
