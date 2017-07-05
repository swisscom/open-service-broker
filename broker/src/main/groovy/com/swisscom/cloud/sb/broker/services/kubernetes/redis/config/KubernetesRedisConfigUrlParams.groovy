package com.swisscom.cloud.sb.broker.services.kubernetes.redis.config

import com.swisscom.cloud.sb.broker.model.ProvisionRequest
import groovy.transform.AutoClone
import groovy.transform.CompileStatic

@AutoClone
@CompileStatic
enum KubernetesRedisConfigUrlParams {
    INSTANCE

    HashMap<String, String> getParams(ProvisionRequest context) {
        return new HashMap<String, String>() {
            {
                put("serviceInstanceGuid", context.getServiceInstanceGuid())
            }
        }
    }
}
