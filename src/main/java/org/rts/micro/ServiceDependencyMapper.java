package org.rts.micro;

import java.util.Map;
import java.util.Set;

public abstract class ServiceDependencyMapper {
    public abstract Map<String, Set<String>> getSvcDependencies(String monitoringServiceUrl) throws Exception;

}
