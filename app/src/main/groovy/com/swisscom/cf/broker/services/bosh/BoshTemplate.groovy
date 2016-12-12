package com.swisscom.cf.broker.services.bosh

import groovy.transform.CompileStatic
import org.yaml.snakeyaml.Yaml

import java.util.regex.Pattern

class BoshTemplate {
    public static final String REGEX_PLACEHOLDER_PREFIX = '\\{\\{'
    public static final String REGEX_PLACEHOLDER_POSTFIX = '\\}\\}'
    public static final Pattern anyPlaceHolder = createPattern('.*')

    private final String template
    private String processed

    private static Pattern createPattern(String placeholder) {
        return ~(REGEX_PLACEHOLDER_PREFIX + placeholder + REGEX_PLACEHOLDER_POSTFIX)
    }

    BoshTemplate(String template) {
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

    public int instanceCount() {
        return ((Map) new Yaml().load(processed)).'instance_groups'.'instances'[0] as int
    }
}
