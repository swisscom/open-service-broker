package com.swisscom.cloud.sb.broker.services.kubernetes.templates.generators

import com.swisscom.cloud.sb.broker.services.kubernetes.templates.constants.KubernetesTemplateConstants
import com.swisscom.cloud.sb.broker.util.StringGenerator
import groovy.transform.CompileStatic

@CompileStatic
class KubernetesTemplatePasswordGenerator {

    Map<String, String> generatePassword() {
        Map<String, String> result = new HashMap()
        updateWithPassword(result)
        return result
    }

    private String updateWithPassword(Map<String, String> result) {
        result.put(KubernetesTemplateConstants.REDIS_PASS.getValue(), (new StringGenerator()).randomAlphaNumeric(30))
    }
}
