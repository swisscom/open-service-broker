package com.swisscom.cloud.sb.broker.functional

import com.fasterxml.jackson.databind.ObjectMapper
import com.swisscom.cloud.sb.broker.error.ErrorCode
import com.swisscom.cloud.sb.broker.error.ServiceBrokerException
import com.swisscom.cloud.sb.broker.model.repository.ServiceContextDetailRepository
import com.swisscom.cloud.sb.broker.model.repository.ServiceInstanceRepository
import com.swisscom.cloud.sb.broker.services.common.ServiceProviderLookup
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
        serviceLifeCycler.createServiceIfDoesNotExist('CloudFoundryContextRestrictedAsyncDummy', ServiceProviderLookup.findInternalName(CloudFoundryContextRestrictedDummyServiceProvider.class))
    }

    def cleanupSpec() {
        serviceLifeCycler.cleanup()
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
        serviceLifeCycler.deleteServiceInstanceAndAssert(true, DummyServiceProvider.RETRY_INTERVAL_IN_SECONDS * 4)
    }

    def "should fail provisioning of async service instance with KubernetesContext"() {
        given:
        def serviceInstanceGuid = UUID.randomUUID().toString()
        serviceLifeCycler.setServiceInstanceId(serviceInstanceGuid)
        def context = new KubernetesContext("namespace_guid")

        when:
        serviceLifeCycler.createServiceInstanceAndAssert(0, false, false, null, context)

        then:
        def ex = thrown(HttpClientErrorException)
        ex.statusCode == HttpStatus.CONFLICT

        def responseBody = new ObjectMapper().readValue(ex.responseBodyAsString, ServiceBrokerException)
        responseBody.code == ErrorCode.CLOUDFOUNDRY_CONTEXT_REQUIRED.code
    }

    void assertCloudFoundryContext(String serviceInstanceGuid) {
        def serviceInstance = serviceInstanceRepository.findByGuid(serviceInstanceGuid)
        assert serviceInstance != null
        assert serviceInstance.serviceContext != null
        assert serviceInstance.serviceContext.platform == CloudFoundryContext.CLOUD_FOUNDRY_PLATFORM
        assert serviceInstance.serviceContext.details.find { it -> it.key == ServiceContextHelper.CF_ORGANIZATION_GUID }.value == "org_id"
        assert serviceInstance.serviceContext.details.find { it -> it.key == ServiceContextHelper.CF_SPACE_GUID }.value == "space_id"
    }

}
