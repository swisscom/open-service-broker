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

package com.swisscom.cloud.sb.broker.services.genericserviceprovider.client

import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.broker.services.genericserviceprovider.GenericProvisionRequestPlanParameter
import com.swisscom.cloud.sb.broker.services.genericserviceprovider.ServiceBrokerServiceProvider
import com.swisscom.cloud.sb.broker.util.RestTemplateBuilder
import com.swisscom.cloud.sb.client.ServiceBrokerClient
import com.swisscom.cloud.sb.client.model.CreateServiceInstanceResponse
import com.swisscom.cloud.sb.client.model.DeleteServiceInstanceRequest
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.servicebroker.model.instance.CreateServiceInstanceRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component

@Component
@CompileStatic
@Slf4j
class ServiceBrokerServiceProviderRestClient {

    private RestTemplateBuilder restTemplateBuilder

    ServiceBrokerServiceProviderRestClient() {}

    @Autowired
    ServiceBrokerServiceProviderRestClient(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplateBuilder = restTemplateBuilder
    }
    /*
     * If the provisioning fails with an exception, the exception is caught in the execute method of the AbstractLastOperationJob
     * which will result in the LastOperation status being set to failed
     */

    boolean provisionServiceInstance(ServiceInstance serviceInstance) {
        log.info("Async provisioning of service instance with id ${serviceInstance.guid}")
        GenericProvisionRequestPlanParameter req = ServiceBrokerServiceProvider.populateGenericProvisionRequestPlanParameter(serviceInstance.plan.parameters)
        def createServiceInstanceRequest = CreateServiceInstanceRequest.builder()
                    .serviceDefinitionId(req.serviceId)
                    .planId(req.planId)
                    .build()
        ServiceBrokerClient serviceBrokerClient = ServiceBrokerServiceProvider.createServiceBrokerClient(req, ServiceBrokerServiceProvider.CustomServiceBrokerServiceProviderProvisioningErrorHandler.class)

        ResponseEntity<CreateServiceInstanceResponse> re = makeCreateServiceInstanceCall(serviceBrokerClient, createServiceInstanceRequest, serviceInstance.guid)
        log.info("Async provisioning status of service instance with id ${serviceInstance.guid}: ${re.statusCode}")
        // If the service is OSB-spec compliant, HttpStatus.CREATED does not need to be supported
        return re.statusCode == HttpStatus.ACCEPTED || re.statusCode == HttpStatus.CREATED
    }

    /*
    * making the call to create a service instance via the serviceBrokerClient is defined in its own method so only this
    * method can be overwritten to enable testing of the ServiceBrokerServiceProvider in the TestableServiceBrokerServiceProvider class
    * More details as to why this is necessary can be found in the TestableServiceBrokerServiceProvider class
    */

    ResponseEntity<CreateServiceInstanceResponse> makeCreateServiceInstanceCall(ServiceBrokerClient serviceBrokerClient, CreateServiceInstanceRequest createServiceInstanceRequest, String serviceInstanceId) {
        createServiceInstanceRequest.serviceInstanceId = serviceInstanceId
        createServiceInstanceRequest.asyncAccepted = true

        return serviceBrokerClient.createServiceInstance(createServiceInstanceRequest)
    }

    boolean deprovisionServiceInstance(ServiceInstance serviceInstance) {
        log.info("Async deprovisioning of service instance with id ${serviceInstance.guid}")
        GenericProvisionRequestPlanParameter req = ServiceBrokerServiceProvider.populateGenericProvisionRequestPlanParameter(serviceInstance.plan.parameters)
        ServiceBrokerClient serviceBrokerClient = ServiceBrokerServiceProvider.createServiceBrokerClient(req, ServiceBrokerServiceProvider.CustomServiceBrokerServiceProviderProvisioningErrorHandler.class)
        ResponseEntity<Void> re = makeDeleteServiceInstanceCall(serviceBrokerClient, serviceInstance.guid, req)

        // If the service is OSB-spec compliant, HttpStatus.OK does not need to be supported
        return re.statusCode == HttpStatus.ACCEPTED || re.statusCode == HttpStatus.OK
    }

    ResponseEntity<Void> makeDeleteServiceInstanceCall(ServiceBrokerClient serviceBrokerClient, String serviceInstanceId, GenericProvisionRequestPlanParameter req) {
        DeleteServiceInstanceRequest deleteServiceInstanceRequest = new DeleteServiceInstanceRequest(serviceInstanceId, req.serviceId, req.planId, true)
        return serviceBrokerClient.deleteServiceInstance(deleteServiceInstanceRequest)
    }
}
