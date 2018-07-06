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

import com.fasterxml.jackson.databind.ObjectMapper
import com.swisscom.cloud.sb.broker.error.ErrorCode
import com.swisscom.cloud.sb.broker.error.ServiceBrokerException
import com.swisscom.cloud.sb.broker.model.repository.ServiceContextDetailRepository
import com.swisscom.cloud.sb.broker.model.repository.ServiceInstanceRepository
import com.swisscom.cloud.sb.broker.services.common.ServiceProviderLookup
import com.swisscom.cloud.sb.broker.util.servicecontext.ServiceContextHelper
import com.swisscom.cloud.sb.broker.util.test.DummyServiceProvider
import com.swisscom.cloud.sb.client.model.DeleteServiceInstanceRequest
import com.swisscom.cloud.sb.client.model.LastOperationState
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.servicebroker.model.CloudFoundryContext
import org.springframework.cloud.servicebroker.model.KubernetesContext
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.client.HttpClientErrorException
import spock.lang.Shared

class AsyncServiceFunctionalSpec extends BaseFunctionalSpec {
    @Autowired
    private ServiceInstanceRepository serviceInstanceRepository
    @Autowired
    private ServiceContextDetailRepository contextRepository

    private int processDelayInSeconds = DummyServiceProvider.RETRY_INTERVAL_IN_SECONDS * 2

    @Shared
    private String parentServiceInstanceGuid

    def setup() {
        serviceLifeCycler.createServiceIfDoesNotExist('AsyncDummy', ServiceProviderLookup.findInternalName(DummyServiceProvider.class))
    }

    def cleanupSpec() {
        serviceLifeCycler.cleanup()
    }


    def "provision async service instance without context"() {
        given:
        def serviceInstanceGuid = UUID.randomUUID().toString()
        serviceLifeCycler.setServiceInstanceId(serviceInstanceGuid)

        when:
        serviceLifeCycler.createServiceInstanceAndAssert(
                DummyServiceProvider.RETRY_INTERVAL_IN_SECONDS * 3,
                true,
                true,
                ['delay': String.valueOf(processDelayInSeconds)])

        then:
        serviceLifeCycler.getServiceInstanceStatus().state == LastOperationState.SUCCEEDED
        assertCloudFoundryContext(serviceInstanceGuid)

        def serviceInstance = serviceInstanceRepository.findByGuid(serviceInstanceGuid)
        serviceInstance.applicationUser.username == cfAdminUser.username
    }


    def "deprovision async service instance"() {
        when:
        serviceLifeCycler.deleteServiceInstanceAndAssert(true, DummyServiceProvider.RETRY_INTERVAL_IN_SECONDS * 3)
        then:
        serviceLifeCycler.getServiceInstanceStatus().state == LastOperationState.SUCCEEDED
    }

    def "deprovision not existing async service instance"() {
        when:
        serviceBrokerClient.deleteServiceInstance(new DeleteServiceInstanceRequest(UUID.randomUUID().toString(), serviceLifeCycler.cfService.guid, serviceLifeCycler.cfService.plans[0].guid, true))

        then:
        def ex = thrown(HttpClientErrorException)
        ex.statusCode == HttpStatus.GONE
    }

    def "fetch not existing async service instance"() {
        when:
        serviceBrokerClient.getServiceInstance(UUID.randomUUID().toString())

        then:
        def ex = thrown(HttpClientErrorException)
        ex.statusCode == HttpStatus.NOT_FOUND
    }

    def "provision async service instance"() {
        given:
        serviceLifeCycler.setServiceInstanceId(UUID.randomUUID().toString())
        serviceLifeCycler.setServiceBindingId(UUID.randomUUID().toString())
        this.parentServiceInstanceGuid = serviceLifeCycler.serviceInstanceId

        when:
        serviceLifeCycler.createServiceInstanceAndAssert(DummyServiceProvider.RETRY_INTERVAL_IN_SECONDS * 4, true, true,
                ['delay': String.valueOf(processDelayInSeconds)])
        then:
        serviceLifeCycler.getServiceInstanceStatus().state == LastOperationState.SUCCEEDED
    }

    def "provision async service instance with parent reference"() {
        given:
        serviceLifeCycler.setServiceInstanceId(UUID.randomUUID().toString())
        serviceLifeCycler.setServiceBindingId(UUID.randomUUID().toString())
        serviceLifeCycler.createServiceInstanceAndAssert(DummyServiceProvider.RETRY_INTERVAL_IN_SECONDS * 2, true, true,
                ['delay': String.valueOf(processDelayInSeconds)])
        this.parentServiceInstanceGuid = serviceLifeCycler.serviceInstanceId

        when:
        serviceLifeCycler.setServiceInstanceId(UUID.randomUUID().toString())
        serviceLifeCycler.createServiceInstanceAndAssert(DummyServiceProvider.RETRY_INTERVAL_IN_SECONDS * 2, true, true,
                ['delay': String.valueOf(processDelayInSeconds), 'parent_reference': this.parentServiceInstanceGuid])
        then:
        serviceLifeCycler.getServiceInstanceStatus().state == LastOperationState.SUCCEEDED

        def si = serviceInstanceRepository.findByGuid(serviceLifeCycler.serviceInstanceId)
        assert si != null
        assert si.parentServiceInstance != null
    }


    def "provision async service instance with non-existing parent reference"() {
        given:
        serviceLifeCycler.setServiceInstanceId(UUID.randomUUID().toString())
        serviceLifeCycler.setServiceBindingId(UUID.randomUUID().toString())

        when:
        serviceLifeCycler.createServiceInstanceAndAssert(DummyServiceProvider.RETRY_INTERVAL_IN_SECONDS * 4, true, true,
                ['delay': String.valueOf(processDelayInSeconds), 'parent_reference': 'service-instance-xxx'])

        then:
        def ex = thrown(HttpClientErrorException)
        ex.statusCode == HttpStatus.NOT_FOUND
        ex.responseBodyAsString != null
        def serviceBrokerException = new ObjectMapper().readValue(ex.responseBodyAsString, ServiceBrokerException)
        serviceBrokerException.code == ErrorCode.PARENT_SERVICE_INSTANCE_NOT_FOUND.code
    }


    def "provision async service instance with CloudFoundryContext"() {
        given:
        def serviceInstanceGuid = UUID.randomUUID().toString()
        serviceLifeCycler.setServiceInstanceId(serviceInstanceGuid)
        def context = new CloudFoundryContext("org_id", "space_id")

        when:
        serviceLifeCycler.createServiceInstanceAndAssert(0, false, false, null, context)

        then:
        assertCloudFoundryContext(serviceInstanceGuid)
        noExceptionThrown()

        cleanup:
        serviceLifeCycler.deleteServiceInstanceAndAssert(true, DummyServiceProvider.RETRY_INTERVAL_IN_SECONDS * 3)
    }


    def "provision async service instance with KubernetesContext"() {
        given:
        def serviceInstanceGuid = UUID.randomUUID().toString()
        serviceLifeCycler.setServiceInstanceId(serviceInstanceGuid)
        def context = new KubernetesContext("namespace_guid")

        when:
        serviceLifeCycler.createServiceInstanceAndAssert(0, false, false, null, context)

        then:
        assertKubernetesContext(serviceInstanceGuid)
        noExceptionThrown()

        cleanup:
        serviceLifeCycler.deleteServiceInstanceAndAssert(true, DummyServiceProvider.RETRY_INTERVAL_IN_SECONDS * 3)
    }

    def "provision async service instance and get instance is not retrievable"() {
        given:
        def serviceInstanceGuid = UUID.randomUUID().toString()
        serviceLifeCycler.setServiceInstanceId(serviceInstanceGuid)

        when:
        serviceLifeCycler.createServiceInstanceAndAssert(DummyServiceProvider.RETRY_INTERVAL_IN_SECONDS * 2, true, true, ['delay': String.valueOf(processDelayInSeconds)])
        serviceBrokerClient.getServiceInstance(serviceInstanceGuid)

        then:
        def ex = thrown(HttpClientErrorException)
        ex.statusCode == HttpStatus.NOT_FOUND
    }

    def "provision and get service instance is retrievable"() {
        given:
        serviceLifeCycler.createServiceIfDoesNotExist('AsyncDummyInstancesRetrievable', ServiceProviderLookup.findInternalName(DummyServiceProvider.class), null, null, null, 0, true, true)

        def serviceInstanceGuid = UUID.randomUUID().toString()
        serviceLifeCycler.setServiceInstanceId(serviceInstanceGuid)

        when:
        serviceLifeCycler.createServiceInstanceAndAssert(DummyServiceProvider.DEFAULT_PROCESSING_DELAY_IN_SECONDS, true, true, ['delay': String.valueOf(processDelayInSeconds)])
        def serviceInstance = serviceBrokerClient.getServiceInstance(serviceInstanceGuid)

        then:
        serviceInstance != null
        def instanceResponse = serviceInstance.getBody()
        instanceResponse.serviceId == serviceLifeCycler.cfService.guid
        instanceResponse.parameters == "{\"delay\":\"${String.valueOf(processDelayInSeconds)}\"}"
    }

    def "provision async service instance without dashboard_url"() {
        given:
        def serviceInstanceGuid = UUID.randomUUID().toString()
        serviceLifeCycler.setServiceInstanceId(serviceInstanceGuid)

        when:
        ResponseEntity provisionResponse = serviceLifeCycler.requestServiceProvisioning(
                true,
                null,
                ['delay': String.valueOf(processDelayInSeconds)]
        )

        then:
        assert provisionResponse.statusCode == HttpStatus.ACCEPTED
        provisionResponse.body.dashboardUrl == null

        and:
        serviceLifeCycler.waitUntilMaxTimeOrTargetState(DummyServiceProvider.RETRY_INTERVAL_IN_SECONDS * 3, serviceLifeCycler.serviceInstanceId)
        serviceLifeCycler.getServiceInstanceStatus().state == LastOperationState.SUCCEEDED

        and:
        serviceLifeCycler.deleteServiceInstanceAndAssert(true, DummyServiceProvider.RETRY_INTERVAL_IN_SECONDS * 3)
    }

    def "provision async service instance with dashboard_url"() {
        given:
        def serviceInstanceGuid = UUID.randomUUID().toString()
        def myDashboardUrl = 'https://somedashboardhwithoauth.com'
        serviceLifeCycler.setServiceInstanceId(serviceInstanceGuid)

        when:
        ResponseEntity provisionResponse = serviceLifeCycler.requestServiceProvisioning(
                true,
                null,
                ['delay': String.valueOf(processDelayInSeconds),
                 'dashboard_url': myDashboardUrl]
        )

        then:
        assert provisionResponse.statusCode == HttpStatus.ACCEPTED
        provisionResponse.body.dashboardUrl == myDashboardUrl

        and:
        serviceLifeCycler.waitUntilMaxTimeOrTargetState(DummyServiceProvider.RETRY_INTERVAL_IN_SECONDS * 3, serviceLifeCycler.serviceInstanceId)
        serviceLifeCycler.getServiceInstanceStatus().state == LastOperationState.SUCCEEDED

        and:
        serviceLifeCycler.deleteServiceInstanceAndAssert(true, DummyServiceProvider.RETRY_INTERVAL_IN_SECONDS * 3)
    }

    void assertCloudFoundryContext(String serviceInstanceGuid, String org_guid = "org_id", String space_guid = "space_id") {
        def serviceInstance = serviceInstanceRepository.findByGuid(serviceInstanceGuid)
        assert serviceInstance != null
        assert serviceInstance.serviceContext != null
        assert serviceInstance.serviceContext.platform == CloudFoundryContext.CLOUD_FOUNDRY_PLATFORM
        assert serviceInstance.serviceContext.details.find { it -> it.key == ServiceContextHelper.CF_ORGANIZATION_GUID }.value == org_guid
        assert serviceInstance.serviceContext.details.find { it -> it.key == ServiceContextHelper.CF_SPACE_GUID }.value == space_guid
    }

    void assertKubernetesContext(String serviceInstanceGuid) {
        def serviceInstance = serviceInstanceRepository.findByGuid(serviceInstanceGuid)
        assert serviceInstance != null
        assert serviceInstance.serviceContext != null
        assert serviceInstance.serviceContext.platform == KubernetesContext.KUBERNETES_PLATFORM
        assert serviceInstance.serviceContext.details.find { it -> it.key == ServiceContextHelper.KUBERNETES_NAMESPACE }.value == "namespace_guid"
    }

}

