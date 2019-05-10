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

package com.swisscom.cloud.sb.broker.services.common

import com.swisscom.cloud.sb.broker.config.Config
import org.yaml.snakeyaml.Yaml

class TemplateConfig implements Config {
    public static final TemplateConfig EMPTY = new TemplateConfig()
    List<ServiceTemplate> serviceTemplates

    private TemplateConfig() {
        this.serviceTemplates = Collections.emptyList()
    }

    private TemplateConfig(List<ServiceTemplate> serviceTemplates) {
        this.serviceTemplates = serviceTemplates
    }

    public static TemplateConfig of(List<ServiceTemplate> serviceTemplates) {
        return new TemplateConfig(serviceTemplates)
    }

    List<String> getTemplateForServiceKey(String templateUniqueIdentifier) {
        serviceTemplates.find {it.name == templateUniqueIdentifier}?.templates ?: Collections.EMPTY_LIST
    }

    List<String> getTemplateForServiceKey(String templateUniqueIdentifier, String templateVersion) {
        serviceTemplates.find {
            it.name == templateUniqueIdentifier && it.version == templateVersion
        }?.templates ?: Collections.EMPTY_LIST
    }

    List<String> getTemplates(String templateUniqueIdentifier) {
        return splitTemplatesFromYamlDocuments(getTemplateForServiceKey(templateUniqueIdentifier))
    }

    List<String> getTemplates(String templateUniqueIdentifier, String templateVersion) {
        return splitTemplatesFromYamlDocuments(getTemplateForServiceKey(templateUniqueIdentifier, templateVersion))
    }

    private static List<String> splitTemplatesFromYamlDocuments(List<String> templates) {
        def deploymentTemplates = templates.collect {it.split("---")}.flatten()
        return deploymentTemplates.collect {it as String}
    }

    static String getKindForTemplate(String template) {
        return ((Map) new Yaml().load(template)).'kind' as String
    }

    static String getNameForTemplate(String template) {
        return ((Map) new Yaml().load(template)).'metadata'.'name' as String
    }
}
