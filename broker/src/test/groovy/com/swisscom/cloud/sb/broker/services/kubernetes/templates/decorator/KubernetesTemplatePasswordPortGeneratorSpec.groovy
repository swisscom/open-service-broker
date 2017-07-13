package com.swisscom.cloud.sb.broker.services.kubernetes.templates.decorator

import com.swisscom.cloud.sb.broker.services.kubernetes.templates.constants.KubernetesTemplateConstants
import com.swisscom.cloud.sb.broker.services.kubernetes.templates.generators.KubernetesTemplatePasswordPortGenerator
import spock.lang.Specification

class KubernetesTemplatePasswordPortGeneratorSpec extends Specification {


    KubernetesTemplatePasswordPortGenerator kubernetesTemplatePasswordPortDecorator

    def setup() {
        kubernetesTemplatePasswordPortDecorator = new KubernetesTemplatePasswordPortGenerator()
    }

    def "generated password has 30 characters"() {
        expect:
        30 == kubernetesTemplatePasswordPortDecorator.generatePasswordAndPort().get(KubernetesTemplateConstants.REDIS_PASS.getValue()).size()
    }

    def "generated password is random"() {
        String pass1 =
                kubernetesTemplatePasswordPortDecorator.generatePasswordAndPort().get(KubernetesTemplateConstants.REDIS_PASS.getValue())
        String pass2 =
                kubernetesTemplatePasswordPortDecorator.generatePasswordAndPort().get(KubernetesTemplateConstants.REDIS_PASS.getValue())
        expect:
        pass1 != pass2
    }

}
