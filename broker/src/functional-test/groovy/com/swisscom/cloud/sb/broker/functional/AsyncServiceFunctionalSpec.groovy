package com.swisscom.cloud.sb.broker.functional

import com.swisscom.cloud.sb.broker.model.repository.ServiceContextDetailRepository
import com.swisscom.cloud.sb.broker.model.repository.ServiceInstanceRepository
import com.swisscom.cloud.sb.broker.services.common.ServiceProviderLookup
import com.swisscom.cloud.sb.broker.util.servicecontext.ServiceContextHelper
import com.swisscom.cloud.sb.broker.util.test.DummyServiceProvider
import com.swisscom.cloud.sb.client.model.LastOperationState
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.servicebroker.model.CloudFoundryContext
import org.springframework.cloud.servicebroker.model.KubernetesContext
import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpClientErrorException

class AsyncServiceFunctionalSpec extends BaseFunctionalSpec {
    @Autowired
    private ServiceInstanceRepository serviceInstanceRepository
    @Autowired
    private ServiceContextDetailRepository contextRepository

    private int processDelayInSeconds = DummyServiceProvider.RETRY_INTERVAL_IN_SECONDS * 2

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
        serviceLifeCycler.createServiceInstanceAndAssert(DummyServiceProvider.RETRY_INTERVAL_IN_SECONDS * 3, true, true, ['delay': String.valueOf(processDelayInSeconds)])

        then:
        serviceLifeCycler.getServiceInstanceStatus().state == LastOperationState.SUCCEEDED
        assertCloudFoundryContext(serviceInstanceGuid)
    }

    def "deprovision async service instance"() {
        when:
        serviceLifeCycler.deleteServiceInstanceAndAssert(true, DummyServiceProvider.RETRY_INTERVAL_IN_SECONDS * 3)
        then:
        serviceLifeCycler.getServiceInstanceStatus().state == LastOperationState.SUCCEEDED
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
        serviceLifeCycler.createServiceInstanceAndAssert(DummyServiceProvider.RETRY_INTERVAL_IN_SECONDS * 2, true, true, ['delay': String.valueOf(processDelayInSeconds)])
        def serviceInstance = serviceBrokerClient.getServiceInstance(serviceInstanceGuid)

        then:
        serviceInstance != null
        def instanceResponse = serviceInstance.getBody()
        instanceResponse.serviceId == serviceLifeCycler.cfService.guid
    }

    void assertCloudFoundryContext(String serviceInstanceGuid) {
        def serviceInstance = serviceInstanceRepository.findByGuid(serviceInstanceGuid)
        assert serviceInstance != null
        assert serviceInstance.serviceContext != null
        assert serviceInstance.serviceContext.platform == CloudFoundryContext.CLOUD_FOUNDRY_PLATFORM
        assert serviceInstance.serviceContext.details.find { it -> it.key == ServiceContextHelper.CF_ORGANIZATION_GUID }.value == "org_id"
        assert serviceInstance.serviceContext.details.find { it -> it.key == ServiceContextHelper.CF_SPACE_GUID }.value == "space_id"
    }

    void assertKubernetesContext(String serviceInstanceGuid) {
        def serviceInstance = serviceInstanceRepository.findByGuid(serviceInstanceGuid)
        assert serviceInstance != null
        assert serviceInstance.serviceContext != null
        assert serviceInstance.serviceContext.platform == KubernetesContext.KUBERNETES_PLATFORM
        assert serviceInstance.serviceContext.details.find { it -> it.key == ServiceContextHelper.KUBERNETES_NAMESPACE }.value == "namespace_guid"
    }
}
