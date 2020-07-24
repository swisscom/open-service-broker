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

package com.swisscom.cloud.sb.client.dummybroker.service

import com.fasterxml.jackson.databind.ObjectMapper
import groovy.transform.CompileStatic
import org.springframework.cloud.servicebroker.model.catalog.Catalog
import org.springframework.cloud.servicebroker.model.catalog.ServiceDefinition
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

import javax.annotation.PostConstruct

@Service
@CompileStatic
class CatalogService implements org.springframework.cloud.servicebroker.service.CatalogService {
    private Catalog catalog

    @PostConstruct
    public void init(){
        catalog = new ObjectMapper().readValue(new File(this.getClass().getResource('/demo-service-definition.json').getFile()).text, Catalog.class)
    }

    @Override
    Mono<Catalog> getCatalog() {
        return Mono.just(catalog)
    }

    @Override
    Mono<ServiceDefinition> getServiceDefinition(String serviceId) {
        return Mono.just(catalog.serviceDefinitions.find {it.id == serviceId})
    }
}
