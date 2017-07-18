package com.swisscom.cloud.sb.broker.services.kubernetes.templates

import spock.lang.Specification

class KubernetesTemplateSpec extends Specification {

    public static final String TEMPLATE = '/kubernetes/redis/v1/0_namespace.yml'

    def "if there are not replaced placeholders validation should fail"() {
        given:
        KubernetesTemplate template = new KubernetesTemplate(readTemplate())
        when:
        template.build()
        then:
        thrown(Exception)
    }

    def "placeholders are replaced correctly"() {
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
        template.replace('SERVICE_ID', 'SERVICE_ID')
        template.replace('SPACE_ID', 'SPACE_ID')
        template.replace('ORG_ID', 'ORG_ID')
        and:
        def expected = input.replace('{{', '').replace('}}', '')
        when:
        def output = template.build()
        then:
        output == expected
    }

    def "kind is obtained correctly"() {
        expect:
        new KubernetesTemplate(readTemplate()).getKind() == "Namespace"
    }

    private String readTemplate() {
        return new File(this.getClass().getResource(TEMPLATE).getFile()).text
    }
}
