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
import com.swisscom.cloud.sb.broker.model.ServiceContextDetail
import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.broker.repository.ServiceContextDetailRepository
import com.swisscom.cloud.sb.broker.repository.ServiceInstanceRepository
import com.swisscom.cloud.sb.broker.services.ServiceProviderService
import com.swisscom.cloud.sb.broker.util.servicecontext.ServiceContextHelper
import com.swisscom.cloud.sb.broker.util.test.CloudFoundryContextRestrictedDummyServiceProvider
import com.swisscom.cloud.sb.broker.util.test.DummyServiceProvider
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.servicebroker.model.CloudFoundryContext
import org.springframework.cloud.servicebroker.model.KubernetesContext
import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpClientErrorException

class CloudFoundryContextRestrictedAsyncServiceFunctionalSpec extends BaseFunctionalSpec {
    @Autowired
    private ServiceInstanceRepository serviceInstanceRepository
    @Autowired
    private ServiceContextDetailRepository contextRepository

    def setup() {
        serviceLifeCycler.createServiceIfDoesNotExist('CloudFoundryContextRestrictedAsyncDummy', ServiceProviderService.findInternalName(CloudFoundryContextRestrictedDummyServiceProvider.class))
    }

    def cleanupSpec() {
        serviceLifeCycler.cleanup()
    }

    def "provision async service instance with CloudFoundryContext"() {
        given:
        def serviceInstanceGuid = UUID.randomUUID().toString()
        serviceLifeCycler.setServiceInstanceId(serviceInstanceGuid)
        def context = CloudFoundryContext.builder().organizationGuid("org_id").spaceGuid("space_id").build()

        when:
        serviceLifeCycler.createServiceInstanceAndAssert(
                DummyServiceProvider.RETRY_INTERVAL_IN_SECONDS * 3,
                true,
                true,
                [] as Map,
                context
        )

        then:
        assertCloudFoundryContext(serviceInstanceGuid)
        noExceptionThrown()

        cleanup:
        serviceLifeCycler.deleteServiceInstanceAndAssert(true, DummyServiceProvider.RETRY_INTERVAL_IN_SECONDS * 4)
    }

    def "should fail provisioning of async service instance with KubernetesContext"() {
        given:
        def serviceInstanceGuid = UUID.randomUUID().toString()
        serviceLifeCycler.setServiceInstanceId(serviceInstanceGuid)
        def context = KubernetesContext.builder().namespace("namespace_guid").build()

        when:
        serviceLifeCycler.createServiceInstanceAndAssert(
                DummyServiceProvider.RETRY_INTERVAL_IN_SECONDS * 3,
                true,
                true,
                [] as Map,
                context
        )

        then:
        def ex = thrown(HttpClientErrorException)
        ex.statusCode == HttpStatus.CONFLICT

        def responseBody = new ObjectMapper().readValue(ex.responseBodyAsString, ServiceBrokerException)
        responseBody.code == ErrorCode.CLOUDFOUNDRY_CONTEXT_REQUIRED.code
    }

    void assertCloudFoundryContext(String serviceInstanceGuid, String org_guid = "org_id", String space_guid = "space_id") {
        ServiceInstance serviceInstance = serviceInstanceRepository.findByGuid(serviceInstanceGuid) as ServiceInstance
        assert serviceInstance != null
        assert serviceInstance.serviceContext != null
        assert serviceInstance.serviceContext.platform == CloudFoundryContext.CLOUD_FOUNDRY_PLATFORM
        assert serviceInstance.serviceContext.details.<ServiceContextDetail>find { it.key == ServiceContextHelper.CF_ORGANIZATION_GUID }.value == org_guid
        assert serviceInstance.serviceContext.details.<ServiceContextDetail>find { it.key == ServiceContextHelper.CF_SPACE_GUID }.value == space_guid
    }

}
