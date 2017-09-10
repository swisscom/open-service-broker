package com.swisscom.cloud.sb.broker.services.bosh

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

    int instanceCount() {
        return ((Map) new Yaml().load(processed)).'instance_groups'.'instances'[0] as int
    }

    void shuffleAzs() {
        TreeMap map = ((TreeMap) new Yaml().load(processed))
        List azs = map.'instance_groups'.'azs'[0]
        Collections.shuffle(azs)

        def options = new org.yaml.snakeyaml.DumperOptions()
        options.defaultFlowStyle = org.yaml.snakeyaml.DumperOptions.FlowStyle.BLOCK
        options.defaultScalarStyle = org.yaml.snakeyaml.DumperOptions.ScalarStyle.PLAIN
        processed = new Yaml(options).dump(map)
    }
}
