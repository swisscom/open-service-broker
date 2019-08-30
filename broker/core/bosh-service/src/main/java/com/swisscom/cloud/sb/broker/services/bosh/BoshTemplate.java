package com.swisscom.cloud.sb.broker.services.bosh;

import com.swisscom.cloud.sb.broker.model.Parameter;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.runtime.StringGroovyMethods;
import org.springframework.util.Assert;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BoshTemplate {

    private static final String BOSH_TEMPLATE_REGEX_FOR_VARIABLES = "\"\\{\\{VARIABLE}}\"|\\{\\{VARIABLE}}";
    private static final String REGEX_PLACEHOLDER_PREFIX = "\\{\\{";
    private static final String REGEX_PLACEHOLDER_POSTFIX = "\\}\\}";
    private static final Pattern anyPlaceHolder = createPattern(".*");
    private final String template;
    private String processed;

    private static Pattern createPatternWithQuotes(String placeholder) {
        return StringGroovyMethods.bitwiseNegate(("\\\"" + REGEX_PLACEHOLDER_PREFIX + placeholder + REGEX_PLACEHOLDER_POSTFIX + "\\\""));
    }

    private static Pattern createPattern(String placeholder) {
        return StringGroovyMethods.bitwiseNegate((REGEX_PLACEHOLDER_PREFIX + placeholder + REGEX_PLACEHOLDER_POSTFIX));
    }

    private BoshTemplate(String template) {
        this.template = template;
        this.processed = template;
    }

    public static BoshTemplate boshTemplateOf(String template) {
        Assert.hasText(template, "Template cannot be empty!");
        return new BoshTemplate(template);
    }

    @Deprecated
    public void replace(String key, String value) {
        processed = StringGroovyMethods.replaceAll(processed, createPatternWithQuotes(key), value);
        processed = StringGroovyMethods.replaceAll(processed, createPattern(key), value);
    }

    public BoshTemplate replaceAllNamed(final Map<String, String> parameters) {
        parameters.keySet().forEach(key -> replaceAllNamed(key, parameters.get(key)));
        return this;
    }

    public BoshTemplate replaceAllNamed(String boshVariableName, String replacement) {
        replaceAll(BOSH_TEMPLATE_REGEX_FOR_VARIABLES.replaceAll("VARIABLE", boshVariableName), replacement);
        return this;
    }

    public BoshTemplate replaceAllNamed(Set<Parameter> parameters) {
        parameters.forEach(p -> replaceAllNamed(p.getName(), p.getValue()));
        return this;
    }

    public void replaceAll(String regularExpression, String replacement) {
        processed = processed.replaceAll(regularExpression, replacement);
    }

    public String build() {
        validate();
        return processed;
    }

    private void validate() {
        final Matcher matcher = anyPlaceHolder.matcher(processed);
        if (matcher.find()) {
            throw new RuntimeException("Placeholder: " + matcher.group() + " not processed!");
        }

    }

    public int instanceCount() {
        Map<String, Object> processedYaml = new Yaml().load(processed);
        List instanceGroups = (List) processedYaml.get("instance_groups");

        return (Integer)((Map)instanceGroups.get(0)).get("instances");
    }

}
