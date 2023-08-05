package org.rts.micro;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class JaegerServiceDependencyMapper implements ServiceDependencyMapper {

    private static final Logger logger = LoggerFactory.getLogger(RTSController.class);

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class Dependency {
        public String parent;
        public String child;

    }

    /**
     * Get the service dependencies from Jaeger.
     * @param observabilityURL Observability URL
     * @return Map<String, Set<String>>
     * @throws Exception Exception
     */
    public Map<String, Set<String>> getSvcDependencies(String observabilityURL) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Set<String>> childToParentsMap = new HashMap<>();
        addToSvcMap(getDDG(observabilityURL), objectMapper, childToParentsMap);
        Map<String, Set<String>> affectedServicesMap = getMapOfAffectedServices(childToParentsMap);
        return affectedServicesMap;
    }

    private static String getDDG(String observabilityURL) throws Exception {
        long endTs = Instant.now().toEpochMilli();
        long lookBack = 6 * 60 * 60 * 1000; // 6 hours in milliseconds

        String url = String.format("%s/api/dependencies?endTs=%d&lookback=%d",
                observabilityURL, endTs, lookBack);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(url))
                .GET()
                .build();

        HttpClient httpClient = HttpClient.newHttpClient();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            logger.info("Got the DDG from Jaeger \n" + response.body());
            return response.body();
        } else {
            throw new Exception("Failed to retrieve DDG: " + response.statusCode());
        }
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

