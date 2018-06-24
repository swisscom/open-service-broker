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

package com.swisscom.cloud.sb.broker.services.kubernetes.templates

import com.swisscom.cloud.sb.broker.services.common.TemplateConfig
import groovy.transform.CompileStatic
import groovy.util.logging.Log4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Log4j
@Component
@CompileStatic
class KubernetesTemplateManager {
    private final TemplateConfig templateConfig

    @Autowired
    KubernetesTemplateManager(TemplateConfig templateConfig) {
        this.templateConfig = templateConfig
    }

    List<KubernetesTemplate> getTemplates(String templateUniqueIdentifier) {
        return splitTemplatesFromYamlDoucments(templateConfig.getTemplateForServiceKey(templateUniqueIdentifier))
    }

    List<KubernetesTemplate> getTemplates(String templateUniqueIdentifier, String templateVersion) {
        return splitTemplatesFromYamlDoucments(templateConfig.getTemplateForServiceKey(templateUniqueIdentifier, templateVersion))
    }

    private List<KubernetesTemplate> splitTemplatesFromYamlDoucments(List<String> templates) {
        def deploymentTemplates = templates.collect{it.split("---")}.flatten()
        return deploymentTemplates.collect{new KubernetesTemplate(it as String)}
    }
}
