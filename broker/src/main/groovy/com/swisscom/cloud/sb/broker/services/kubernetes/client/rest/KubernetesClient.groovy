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

package com.swisscom.cloud.sb.broker.services.kubernetes.client.rest

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.swisscom.cloud.sb.broker.error.ServiceBrokerException
import com.swisscom.cloud.sb.broker.services.kubernetes.config.KubernetesConfig
import com.swisscom.cloud.sb.broker.util.RestTemplateBuilder
import groovy.json.JsonOutput
import groovy.transform.CompileStatic
import groovy.util.logging.Log4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.*
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException

@CompileStatic
@Component
@Log4j
class KubernetesClient<RESPONSE> {

    KubernetesConfig kubernetesConfig
    RestTemplateBuilder restTemplateBuilder

    @Autowired
    KubernetesClient(KubernetesConfig kubernetesConfig, RestTemplateBuilder restTemplateBuilder) {
        this.kubernetesConfig = kubernetesConfig
        this.restTemplateBuilder = restTemplateBuilder
    }

    ResponseEntity<RESPONSE> exchange(String url, HttpMethod method,
                                      String body, Class<RESPONSE> responseType) {
        //TODO get rid of SSL validation disabling, trust the server side certificate instead
        def restTemplate = restTemplateBuilder.withSSLValidationDisabled().
                withClientSideCertificate(kubernetesConfig.kubernetesClientCertificate, kubernetesConfig.kubernetesClientKey).build()
        log.info(url + " - " + convertYamlToJson(body))
        try {
            return restTemplate.exchange(
                    "https://" + kubernetesConfig.getKubernetesHost() + ":" + kubernetesConfig.getKubernetesPort() + "/" +
                            url, method, new HttpEntity<String>(convertYamlToJson(body), getJsonHeaders()), responseType)
        } catch (HttpClientErrorException e) {
            log.error("HttpStatus: ${e.statusCode}, ${e.statusText}, Body: ${e.responseBodyAsString}")
            throw new ServiceBrokerException("Call to backend system failed")
        }
    }

    ResponseEntity<RESPONSE> exchange(String url, HttpMethod method,
                                      String body, String override, Class<RESPONSE> responseType) {
        String overrideJson = convertYamlToJson(override)
        def slurper = new groovy.json.JsonSlurper()
        def json1 = slurper.parseText(body) as Map
        def json2 = slurper.parseText(overrideJson) as Map
        def finalBody = merge(json1, json2)
        this.exchange(url, method, JsonOutput.toJson(finalBody), responseType)
    }

    Map merge(Map onto, Map... overrides) {
        if (!overrides)
            return onto
        else if (overrides.length == 1) {
            overrides[0]?.each { k, v ->
                if (v instanceof Map && onto[k] instanceof Map)
                    merge((Map) onto[k], (Map) v)
                else
                    onto[k] = v
            }
            return onto
        }
        overrides.inject(onto, { acc, override -> merge(acc, override ?: [:]) }) as Map
    }

    private HttpHeaders getJsonHeaders() {
        HttpHeaders headers = new HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_JSON)
        return headers
    }

    String convertYamlToJson(String yaml) {
        if (yaml == null || yaml.isEmpty()) {
            return ""
        }
        ObjectMapper yamlReader = new ObjectMapper(new YAMLFactory())
        Object obj = yamlReader.readValue(yaml, Object.class)
        ObjectMapper jsonWriter = new ObjectMapper()
        return jsonWriter.writeValueAsString(obj)
    }

}
