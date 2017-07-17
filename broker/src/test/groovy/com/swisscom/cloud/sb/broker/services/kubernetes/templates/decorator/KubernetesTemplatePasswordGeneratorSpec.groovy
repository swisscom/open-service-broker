package com.swisscom.cloud.sb.broker.services.kubernetes.templates.decorator

import com.swisscom.cloud.sb.broker.services.kubernetes.templates.constants.KubernetesTemplateConstants
import com.swisscom.cloud.sb.broker.services.kubernetes.templates.generators.KubernetesTemplatePasswordGenerator
import spock.lang.Specification

class KubernetesTemplatePasswordGeneratorSpec extends Specification {


    KubernetesTemplatePasswordGenerator kubernetesTemplatePasswordPortDecorator

    def setup() {
        kubernetesTemplatePasswordPortDecorator = new KubernetesTemplatePasswordGenerator()
    }

    def "generated password has 30 characters"() {
        expect:
        30 == kubernetesTemplatePasswordPortDecorator.generatePassword().get(KubernetesTemplateConstants.REDIS_PASS.getValue()).size()
    }

    def "generated password is random"() {
        String pass1 =
                kubernetesTemplatePasswordPortDecorator.generatePassword().get(KubernetesTemplateConstants.REDIS_PASS.getValue())
        String pass2 =
                kubernetesTemplatePasswordPortDecorator.generatePassword().get(KubernetesTemplateConstants.REDIS_PASS.getValue())
        expect:
        pass1 != pass2
    }

}
