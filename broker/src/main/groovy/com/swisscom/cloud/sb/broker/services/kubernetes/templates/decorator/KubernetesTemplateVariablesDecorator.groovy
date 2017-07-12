package com.swisscom.cloud.sb.broker.services.kubernetes.templates.decorator

import com.swisscom.cloud.sb.broker.model.Plan
import com.swisscom.cloud.sb.broker.model.ProvisionRequest
import com.swisscom.cloud.sb.broker.services.kubernetes.templates.KubernetesTemplate
import groovy.transform.CompileStatic

@CompileStatic
class KubernetesTemplateVariablesDecorator {

    void replaceTemplate(KubernetesTemplate kubernetesTemplate, ProvisionRequest request, Map<String, String>... maps) {
        kubernetesTemplate.replace("SERVICE_ID", request.getServiceInstanceGuid())
        kubernetesTemplate.replace("SPACE_ID", request.getSpaceGuid())
        kubernetesTemplate.replace("ORG_ID", request.getOrganizationGuid())
        replaceWithMap(kubernetesTemplate, request, maps)
    }

    private void replaceWithMap(KubernetesTemplate kubernetesTemplate, ProvisionRequest request, Map<String, String>... maps) {
        for (Map<String, String> map : maps) {
            for (String key : map.keySet()) {
                kubernetesTemplate.replace(key, getPlanParameter(key, request.plan, map))
            }
        }
    }

    private String getPlanParameter(String key, Plan plan, Map<String, String> map) {
        def instanceTypeParam = plan.parameters.find { it.name == key }
        if (!instanceTypeParam) {
            return map.get(key)
        }
        return instanceTypeParam.value
    }
}
