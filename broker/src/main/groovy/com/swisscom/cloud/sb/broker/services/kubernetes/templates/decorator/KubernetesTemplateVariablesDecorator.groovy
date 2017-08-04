package com.swisscom.cloud.sb.broker.services.kubernetes.templates.decorator

import com.swisscom.cloud.sb.broker.model.Parameter
import com.swisscom.cloud.sb.broker.model.Plan
import com.swisscom.cloud.sb.broker.model.ProvisionRequest
import com.swisscom.cloud.sb.broker.services.kubernetes.templates.KubernetesTemplate
import com.swisscom.cloud.sb.broker.services.kubernetes.templates.constants.KubernetesTemplateConstants
import groovy.transform.CompileStatic

@CompileStatic
class KubernetesTemplateVariablesDecorator {

    void replaceTemplate(KubernetesTemplate kubernetesTemplate, ProvisionRequest request, Map<String, String>... maps) {
        kubernetesTemplate.replace(KubernetesTemplateConstants.SERVICE_ID.getValue(), request.getServiceInstanceGuid())
        kubernetesTemplate.replace(KubernetesTemplateConstants.SPACE_ID.getValue(), request.getSpaceGuid())
        kubernetesTemplate.replace(KubernetesTemplateConstants.ORG_ID.getValue(), request.getOrganizationGuid())
        replaceWithPlan(kubernetesTemplate, request)
        replaceWithMap(kubernetesTemplate, request, maps)
    }

    private void replaceWithPlan(KubernetesTemplate kubernetesTemplate, ProvisionRequest request) {
        for (Parameter param : request.plan.parameters) {
            kubernetesTemplate.replace(param.getName(), param.getValue())
        }
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
