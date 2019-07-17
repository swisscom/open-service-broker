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

package com.swisscom.cloud.sb.broker.functional

import com.swisscom.cloud.sb.broker.util.JsonHelper
import org.skyscreamer.jsonassert.JSONAssert
import org.springframework.cloud.servicebroker.model.catalog.Catalog
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

class CatalogFunctionalSpec extends BaseFunctionalSpec {
    public static final String serviceName = 'Supercalifragilisticexpialidocious ' + new Random().nextInt()

    def setup() {
        serviceLifeCycler.createServiceIfDoesNotExist(serviceName, null)
    }

    def cleanupSpec() {
        serviceLifeCycler.cleanup()
    }

    def 'catalog dto is created correctly'() {
        when:
        ResponseEntity<Catalog> response = serviceBrokerClient.getCatalog()
        then:
        response.statusCode == HttpStatus.OK
        println(response.body.toString())
        response.body.serviceDefinitions.size() > 0
        def newAddedService = response.body.serviceDefinitions.find { it.name == serviceName }
        newAddedService.tags.size() == 1
        newAddedService.plans.size() == 1
    }

    def 'catalog dto with plan json schema is created correctly'() {
        given:
        def serviceInstanceCreateSchema = '{"$schema":"http://json-schema.org/draft-04/schema#","properties":{"billing-account":{"description":"Service instance create","type":"string"}},"type":"object"}'
        def serviceInstanceUpdateSchema = '{"$schema":"http://json-schema.org/draft-04/schema#","properties":{"billing-account":{"description":"Service instance update","type":"string"}},"type":"object"}'
        def serviceBindingCreateSchema = '{"$schema":"http://json-schema.org/draft-04/schema#","properties":{"billing-account":{"description":"Service binding create","type":"string"}},"type":"object"}'

        def serviceName1 = serviceName + Math.abs(new Random().nextInt())
        serviceLifeCycler.createServiceIfDoesNotExist(serviceName1, null, null, null, null, 0,
                false, false, serviceInstanceCreateSchema, serviceInstanceUpdateSchema, serviceBindingCreateSchema)

        when:
        ResponseEntity<Catalog> response = serviceBrokerClient.getCatalog()

        then:
        response.statusCode == HttpStatus.OK
        println(response.body.toString())
        response.body.serviceDefinitions.size() > 0
        def newAddedService = response.body.serviceDefinitions.find { it.name == serviceName1 }
        newAddedService.tags.size() == 1
        newAddedService.plans.size() == 1

        def plan = newAddedService.plans[0]

        and:
        JSONAssert.assertEquals(serviceInstanceCreateSchema, JsonHelper.toJsonString(plan.schemas.serviceInstanceSchema.createMethodSchema.parameters), false)
        JSONAssert.assertEquals(serviceInstanceUpdateSchema, JsonHelper.toJsonString(plan.schemas.serviceInstanceSchema.updateMethodSchema.parameters), false)
        JSONAssert.assertEquals(serviceBindingCreateSchema, JsonHelper.toJsonString(plan.schemas.serviceBindingSchema.createMethodSchema.parameters), false)
    }
}