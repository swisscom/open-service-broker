package com.swisscom.cloud.sb.broker.services.kubernetes.templates.generators

import com.swisscom.cloud.sb.broker.services.kubernetes.templates.constants.KubernetesTemplateConstants
import com.swisscom.cloud.sb.broker.util.StringGenerator
import groovy.transform.CompileStatic

@CompileStatic
class KubernetesTemplatePasswordPortGenerator {

    Map<String, String> generatePasswordAndPort() {
        Map<String, String> result = new HashMap()
        updateWithPassword(result)
        updateWithPort(result)
        return result
    }

    private String updateWithPassword(Map<String, String> result) {
        result.put(KubernetesTemplateConstants.REDIS_PASS.getValue(), (new StringGenerator()).randomAlphaNumeric(30))
    }

    private String updateWithPort(Map<String, String> result) {
        //TODO this method has to be more "clever" with finding the open ports
        result.put(KubernetesTemplateConstants.NODE_PORT_REDIS_MASTER.getValue(), 54028.toString())
        result.put(KubernetesTemplateConstants.NODE_PORT_REDIS_SLAVE0.getValue(), 54029.toString())
        result.put(KubernetesTemplateConstants.NODE_PORT_REDIS_SLAVE1.getValue(), 54030.toString())
    }
}
