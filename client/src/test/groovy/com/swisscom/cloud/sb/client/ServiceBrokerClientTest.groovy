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

package com.swisscom.cloud.sb.client

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.swisscom.cloud.sb.client.dummybroker.Application
import com.swisscom.cloud.sb.client.model.LastOperationResponse
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.junit.runner.RunWith
import org.springframework.boot.SpringApplication
import org.springframework.cloud.servicebroker.model.binding.BindResource
import org.springframework.cloud.servicebroker.model.binding.CreateServiceInstanceAppBindingResponse
import org.springframework.cloud.servicebroker.model.binding.CreateServiceInstanceBindingRequest
import org.springframework.cloud.servicebroker.model.catalog.Catalog
import org.springframework.cloud.servicebroker.model.catalog.Plan
import org.springframework.cloud.servicebroker.model.catalog.ServiceDefinition
import org.springframework.cloud.servicebroker.model.instance.CreateServiceInstanceRequest
import org.springframework.cloud.servicebroker.model.instance.UpdateServiceInstanceRequest
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.util.Assert
import org.springframework.web.client.HttpClientErrorException

/**
 * Verifies that we can use our client implementation to talk to a valid (spring boot) open service broker
 * implementation. Checks that we use the correct paths and objects and that we are using basic authentication
 * in the client correctly.
 */
@RunWith(SpringJUnit4ClassRunner.class)
class ServiceBrokerClientTest {
    private static ServiceBrokerClient serviceBrokerClient
    private static ServiceDefinition serviceDefinition
    private static Plan plan
    private static String serviceInstanceId = '4ca9439a-b002-11e6-80f5-76304dec7eb7'
    private static String bindingId = 'bindingId'
    @Rule
    public ExpectedException exception = ExpectedException.none()


    @BeforeClass
    static void setup(){
        SpringApplication.run(Application.class)
        serviceBrokerClient = new ServiceBrokerClient('http://localhost:8080','user','password')
        def om = new ObjectMapper()
        Catalog catalog = om.readValue(new File(this.getClass().getResource('/demo-service-definition.json').getFile()).text, Catalog.class)
        serviceDefinition = catalog.serviceDefinitions.first()
        plan = serviceDefinition.plans.first()
    }

    @Test
    void wrongPasswordCauses401(){
        exception.expect(HttpClientErrorException.class)
        exception.expectMessage("401")

        new ServiceBrokerClient('http://localhost:8080','user','WrongPassword').getCatalog()
    }

    @Test
    void getCatalog(){
        Catalog result = serviceBrokerClient.getCatalog().body
        Assert.notEmpty(result)
    }

    @Test
    void createServiceInstance() {
        def request = CreateServiceInstanceRequest.builder()
                .serviceDefinitionId(serviceDefinition.id)
                .planId(plan.id)
                .serviceInstanceId(serviceInstanceId)
                .build()

        def result = serviceBrokerClient.createServiceInstance(request).body
        Assert.notNull(result)
    }

    @Test
    void getServiceInstanceLastOperation() {
        LastOperationResponse result = serviceBrokerClient.getServiceInstanceLastOperation(serviceInstanceId).body
        Assert.notNull(result)
        Assert.notNull(result.state,'LastOperationState must be not null')
    }

    @Test
    void createServiceInstanceBinding(){
        def request = CreateServiceInstanceBindingRequest.builder()
                .serviceDefinitionId(serviceDefinition.id)
                .planId(plan.id)
                .serviceInstanceId(serviceInstanceId)
                .bindingId(bindingId)
                .bindResource(BindResource.builder().appGuid("appGuid").build())
                .build()

        CreateServiceInstanceAppBindingResponse result = serviceBrokerClient.createServiceInstanceBinding(request).body
        Assert.notNull(result)
    }

    @Test
    void deleteServiceInstanceBinding(){
        def request = new com.swisscom.cloud.sb.client.model.DeleteServiceInstanceBindingRequest(serviceInstanceId,bindingId,serviceDefinition.id,plan.id)
        serviceBrokerClient.deleteServiceInstanceBinding(request)
    }

    @Test
    void deleteServiceInstance(){
        def request = new com.swisscom.cloud.sb.client.model.DeleteServiceInstanceRequest(serviceInstanceId, serviceDefinition.id, plan.id, false)
        serviceBrokerClient.deleteServiceInstance(request)
    }

    @Test
    void updateServiceInstance() {
         def request = UpdateServiceInstanceRequest.builder()
                            .serviceDefinitionId(serviceDefinition.id)
                            .planId(plan.id)
                            .serviceInstanceId(serviceInstanceId)
                            .build()
        serviceBrokerClient.updateServiceInstance(request)
    }
}