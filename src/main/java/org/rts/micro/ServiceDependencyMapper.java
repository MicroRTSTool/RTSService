package org.rts.micro;

import java.util.Map;
import java.util.Set;

public interface ServiceDependencyMapper {
    public Map<String, Set<String>> getSvcDependencies(String monitoringServiceUrl) throws Exception;

}
