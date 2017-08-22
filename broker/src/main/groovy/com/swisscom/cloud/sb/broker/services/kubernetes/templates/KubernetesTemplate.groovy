package com.swisscom.cloud.sb.broker.services.kubernetes.templates

import groovy.transform.ToString
import org.yaml.snakeyaml.Yaml

import java.util.regex.Pattern

@ToString
class KubernetesTemplate {
    final String template

    KubernetesTemplate(String template) {
        this.template = template
    }
}
