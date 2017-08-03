package com.swisscom.cloud.sb.broker.services.kubernetes.templates.decorator

import com.swisscom.cloud.sb.broker.model.Parameter
import com.swisscom.cloud.sb.broker.model.Plan
import com.swisscom.cloud.sb.broker.model.ProvisionRequest
import com.swisscom.cloud.sb.broker.services.kubernetes.templates.KubernetesTemplate
import spock.lang.Specification

class KubernetesTemplateVariablesDecoratorSpec extends Specification {


    KubernetesTemplateVariablesDecorator kubernetesTemplateVariablesDecorator
    ProvisionRequest request

    def setup() {
        kubernetesTemplateVariablesDecorator = new KubernetesTemplateVariablesDecorator()
        request = Stub()
        request.getServiceInstanceGuid() >> "SERVICE_ID"
        request.getSpaceGuid() >> "SPACE_ID"
        request.getOrganizationGuid() >> "ORG_ID"
    }

    def "replace fields with request"() {
        given:
        def input = """
                        apiVersion: v1
                        kind: Namespace
                        metadata:
                          name: {{SERVICE_ID}}
                          labels:
                            service_id: {{SERVICE_ID}}
                            service_type: redis-sentinel
                            space: {{SPACE_ID}}
                            org: {{ORG_ID}}
                          annotations:
                            appcloud.swisscom.com/disk-quota: \"1g\""""
        KubernetesTemplate template = new KubernetesTemplate(input)
        and:
        def expected = input.replace('{{', '').replace('}}', '')
        when:
        kubernetesTemplateVariablesDecorator.replaceTemplate(template, request)
        then:
        template.build() == expected
    }

    def "replace fields with a map"() {
        given:
        def input = """
                        apiVersion: v1
                        kind: Namespace
                        metadata:
                          name: {{SERVICE_ID}}
                          labels:
                            service_id: {{MAP_ID}}
                            service_id: {{SERVICE_ID}}
                            service_type: redis-sentinel
                            space: {{SPACE_ID}}
                            org: {{ORG_ID}}
                          annotations:
                            appcloud.swisscom.com/disk-quota: \"1g\""""
        KubernetesTemplate template = new KubernetesTemplate(input)
        and:
        def expected = input.replace('{{', '').replace('}}', '')
        when:
        HashMap hashMap = new HashMap();
        hashMap.put("MAP_ID", "MAP_ID")
        kubernetesTemplateVariablesDecorator.replaceTemplate(template, request, hashMap)
        then:
        template.build() == expected
    }

    def "replace fields with a request plan"() {
        given:
        def input = """
                        apiVersion: v1
                        kind: Namespace
                        metadata:
                          name: {{SERVICE_ID}}
                          labels:
                            service_id: {{PLAN_ID}}
                            service_id: {{SERVICE_ID}}
                            service_type: redis-sentinel
                            space: {{SPACE_ID}}
                            org: {{ORG_ID}}
                          annotations:
                            appcloud.swisscom.com/disk-quota: \"1g\""""
        KubernetesTemplate template = new KubernetesTemplate(input)
        and:
        def expected = input.replace('{{', '').replace('}}', '')
        mockParameters()
        when:
        kubernetesTemplateVariablesDecorator.replaceTemplate(template, request)
        then:
        template.build() == expected
    }

    private void mockParameters() {
        Set<Parameter> parameters = new HashSet();
        Parameter parameter = new Parameter()
        parameter.setName("PLAN_ID")
        parameter.setValue("PLAN_ID")
        parameters.add(parameter)
        Plan plan = Stub()
        request.plan >> plan
        plan.parameters >> parameters
    }


}
