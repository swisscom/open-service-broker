package com.swisscom.cloud.sb.broker.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Some {@link com.swisscom.cloud.sb.broker.services.common.ServiceProvider}s
 * require sensitive information, like passwords and certificates,
 * in provision or update requests. All such providers must be marked with this
 * interface.
 */
public interface SensitiveParameterProvider {
    /**
     * Provides the non-critical parts of parameters given
     * in a provision or update request
     * @param parameters given in the provision or update request
     * @return the sanitized parameters
     */
    default Map<String, Object> getSanitizedParameters(Map<String, Object> parameters) {
        Map<String, Object> result = new HashMap<>();
        if (parameters == null) return result;
        result.put("CONFIDENTIAL", parameters.keySet());
        return result;
    }
}
