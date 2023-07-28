package org.rts.micro;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class JaegerServiceDependencyMapper implements ServiceDependencyMapper {

    static class Dependency {
        public String parent;
        public String child;
    }

    public Map<String, Set<String>> getSvcDependencies(String monitoringServiceUrl) throws Exception {
        List<String> serviceList = getAllServices(monitoringServiceUrl);
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Set<String>> childToParentsMap = new HashMap<>();

        for (String service : serviceList) {
            String ddgJson = getDDG(service, monitoringServiceUrl);
            addToSvcMap(ddgJson, objectMapper, childToParentsMap);
        }
        Map<String, Set<String>> affectedServicesMap = getMapOfAffectedServices(childToParentsMap);
        return affectedServicesMap;
    }

    private static void addToSvcMap(String ddgJson, ObjectMapper objectMapper,
                                    Map<String, Set<String>> childToParentsMap) throws JsonProcessingException {
        Map<String, Object> ddgMap = objectMapper.readValue(ddgJson, new TypeReference<>() {});
        List<Dependency> dependencies = objectMapper.convertValue(ddgMap.get("data"),
                new TypeReference<>() {});
        for (Dependency dependency : dependencies) {
            childToParentsMap.putIfAbsent(dependency.child, new HashSet<>());
            childToParentsMap.get(dependency.child).add(dependency.parent);
        }
    }

    private static List<String> getAllServices(String monitoringSvcUrl) throws Exception{
        String urlStr = monitoringSvcUrl + "/api/services";

        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");

        if (conn.getResponseCode() != 200) {
            throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
        }

        BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

        String output;
        StringBuilder response = new StringBuilder();
        while ((output = br.readLine()) != null) {
            response.append(output);
        }

        conn.disconnect();

        String responseStr = response.toString();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(responseStr);

        List<String> serviceList = new ArrayList<>();
        if (jsonNode.has("data")) {
            for (JsonNode service : jsonNode.get("data")) {
                serviceList.add(service.textValue());
            }
        }
        return serviceList;
    }

    private static String getDDG(String serviceName, String monitoringSvcUrl) throws Exception {
        String urlStr = monitoringSvcUrl + "/api/dependencies?service=" + serviceName;

        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");

        if (conn.getResponseCode() != 200) {
            throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
        }

        BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

        String output;
        StringBuilder response = new StringBuilder();
        while ((output = br.readLine()) != null) {
            response.append(output);
        }

        conn.disconnect();

        return response.toString();
    }

    /**
     * Get a map of services to the services that depend on them by reversing the map of services to their dependencies.
     *
     * @param serviceDependencies
     * @return
     */
    private static Map<String, Set<String>> getMapOfAffectedServices(Map<String, Set<String>> serviceDependencies) {
        Map<String, Set<String>> affectedServices = new HashMap<>();

        for (String service : serviceDependencies.keySet()) {
            Set<String> affected = new HashSet<>();
            findAffectedServices(service, serviceDependencies, affected);
            affectedServices.put(service, affected);
        }
        return affectedServices;
    }

    private static void findAffectedServices(String service, Map<String, Set<String>> serviceDependencies, Set<String> affected) {

        if (serviceDependencies.containsKey(service)) {
            for (String dependentService : serviceDependencies.get(service)) {
                affected.add(dependentService);
                findAffectedServices(dependentService, serviceDependencies, affected);
            }
        }
    }
}

