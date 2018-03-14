package com.swisscom.cloud.sb.client

import com.fasterxml.jackson.databind.ObjectMapper
import com.swisscom.cloud.sb.client.dummybroker.Application
import com.swisscom.cloud.sb.client.model.LastOperationResponse
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.junit.runner.RunWith
import org.springframework.boot.SpringApplication
import org.springframework.cloud.servicebroker.model.*
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.util.Assert
import org.springframework.web.client.HttpClientErrorException

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
        Catalog catalog = new ObjectMapper().readValue(new File(this.getClass().getResource('/demo-service-definition.json').getFile()).text, Catalog.class)
        serviceDefinition = catalog.serviceDefinitions.first()
        plan = serviceDefinition.plans.first()
    }

    @Test
    void wrongPasswordCauses401(){
        exception.expect(HttpClientErrorException.class)
        exception.expectMessage("401");
        new ServiceBrokerClient('http://localhost:8080','user','WrongPassword').getCatalog()
    }

    @Test
    void getCatalog(){
        Catalog result = serviceBrokerClient.getCatalog().body
        Assert.notEmpty(result)
    }

    @Test
    void createServiceInstance(){
        def request = new CreateServiceInstanceRequest(serviceDefinition.id, plan.id, 'orgId', 'spaceGuid', null, null)
        request.serviceInstanceId = serviceInstanceId
        CreateServiceInstanceResponse result = serviceBrokerClient.createServiceInstance(request).body
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
        def request = new CreateServiceInstanceBindingRequest(serviceDefinition.id,plan.id,'appGuid',null)
        request.serviceInstanceId = serviceInstanceId
        request.bindingId = bindingId
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
    void updateServiceInstance(){
         def request = new UpdateServiceInstanceRequest(serviceDefinition.id,plan.id).withServiceInstanceId(serviceInstanceId)
        serviceBrokerClient.updateServiceInstance(request)
    }
}