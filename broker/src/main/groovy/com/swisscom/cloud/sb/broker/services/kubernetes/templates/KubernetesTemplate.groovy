package com.swisscom.cloud.sb.broker.services.kubernetes.templates

import groovy.transform.ToString
import org.yaml.snakeyaml.Yaml

import java.util.regex.Pattern

@ToString
class KubernetesTemplate {
    static final String REGEX_PLACEHOLDER_PREFIX = '\\{\\{'
    static final String REGEX_PLACEHOLDER_POSTFIX = '\\}\\}'
    static final Pattern anyPlaceHolder = createPattern('.*')

    private final String template
    private String processed

    private static Pattern createPattern(String placeholder) {
        return ~(REGEX_PLACEHOLDER_PREFIX + placeholder + REGEX_PLACEHOLDER_POSTFIX)
    }

    KubernetesTemplate(String template) {
        this.template = template
        this.processed = template
    }

    void replace(String key, String value) {
        processed = processed.replaceAll(createPattern(key), value)
    }

    String build() {
        validate()
        return processed
    }

    private void validate() {
        def matcher = anyPlaceHolder.matcher(processed)
        if (matcher.find()) {
            throw new RuntimeException("Placeholder: ${matcher.group()} not processed!")
        }
    }

    String getKind() {
        return ((Map) new Yaml().load(processed)).'kind' as String
    }
}
