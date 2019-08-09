/*
 * Copyright (c) 2018 Swisscom (Switzerland) Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.swisscom.cloud.sb.broker.services.bosh

import org.springframework.util.Assert
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml

import java.util.regex.Pattern

class BoshTemplate {
    public static final String REGEX_PLACEHOLDER_PREFIX = '\\{\\{'
    public static final String REGEX_PLACEHOLDER_POSTFIX = '\\}\\}'
    public static final Pattern anyPlaceHolder = createPattern('.*')

    private final String template
    private String processed

    private static Pattern createPatternWithQuotes(String placeholder) {
        return ~('\\"' + REGEX_PLACEHOLDER_PREFIX + placeholder + REGEX_PLACEHOLDER_POSTFIX + '\\"')
    }

    private static Pattern createPattern(String placeholder) {
        return ~(REGEX_PLACEHOLDER_PREFIX + placeholder + REGEX_PLACEHOLDER_POSTFIX)
    }

    private BoshTemplate(String template) {
        this.template = template
        this.processed = template
    }

    public static BoshTemplate of(String template) {
        Assert.hasText(template, "Template cannot be empty!")
        return new BoshTemplate(template)
    }

    void replace(String key, String value) {
        processed = processed.replaceAll(createPatternWithQuotes(key), value)
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

        def options = new DumperOptions()
        options.defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
        options.defaultScalarStyle = DumperOptions.ScalarStyle.PLAIN
        processed = new Yaml(options).dump(map)
    }
}
