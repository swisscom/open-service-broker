package com.swisscom.cloud.sb.broker.services.kubernetes.endpoint.parameters

import com.swisscom.cloud.sb.broker.model.ProvisionRequest
import groovy.transform.AutoClone
import groovy.transform.CompileStatic

@AutoClone
@CompileStatic
class KubernetesConfigUrlParams {

    HashMap<String, String> getParameters(ProvisionRequest context) {
        return new HashMap<String, String>() {
            {
                put("serviceInstanceGuid", context.getServiceInstanceGuid())
            }
        }
    }

}
