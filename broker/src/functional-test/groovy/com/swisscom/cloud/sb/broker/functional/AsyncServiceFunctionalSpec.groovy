package com.swisscom.cloud.sb.broker.functional

import com.fasterxml.jackson.databind.ObjectMapper
import com.swisscom.cloud.sb.broker.config.ParentAliasSchedulingConfig
import com.swisscom.cloud.sb.broker.error.ErrorCode
import com.swisscom.cloud.sb.broker.error.ServiceBrokerException
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
    @Autowired
    private ParentAliasSchedulingConfig parentAliasSchedulingConfig

    private int processDelayInSeconds = DummyServiceProvider.RETRY_INTERVAL_IN_SECONDS * 2

    private static final String serviceInstanceAlias = "service-instance-a"

    def setup() {
        parentAliasSchedulingConfig.retryIntervalInSeconds = 5
        parentAliasSchedulingConfig.maxRetryDurationInMinutes = 1
        parentAliasSchedulingConfig.delayInSeconds = 10
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
        serviceLifeCycler.createServiceInstanceAndAssert(DummyServiceProvider.RETRY_INTERVAL_IN_SECONDS * 4, true, true, ['delay': String.valueOf(processDelayInSeconds)])

        then:
        serviceLifeCycler.getServiceInstanceStatus().state == LastOperationState.SUCCEEDED
        assertCloudFoundryContext(serviceInstanceGuid)
    }


    def "deprovision async service instance"() {
        when:
        serviceLifeCycler.deleteServiceInstanceAndAssert(true, DummyServiceProvider.RETRY_INTERVAL_IN_SECONDS * 4)
        then:
        serviceLifeCycler.getServiceInstanceStatus().state == LastOperationState.SUCCEEDED
    }


    def "provision async service instance with alias"() {
        when:
        serviceLifeCycler.setServiceInstanceId(UUID.randomUUID().toString())
        serviceLifeCycler.setServiceBindingId(UUID.randomUUID().toString())

        serviceLifeCycler.createServiceInstanceAndAssert(DummyServiceProvider.RETRY_INTERVAL_IN_SECONDS * 4, true, true,
                ['delay': String.valueOf(processDelayInSeconds), 'alias': serviceInstanceAlias])
        then:
        serviceLifeCycler.getServiceInstanceStatus().state == LastOperationState.SUCCEEDED

        def si = serviceInstanceRepository.findByGuid(serviceLifeCycler.serviceInstanceId)
        assert si != null
        assert si.details.find { it.key == 'alias' && it.value == serviceInstanceAlias } != null
    }


    def "provision async service instance with parent alias"() {
        when:
        serviceLifeCycler.setServiceInstanceId(UUID.randomUUID().toString())
        serviceLifeCycler.setServiceBindingId(UUID.randomUUID().toString())

        serviceLifeCycler.createServiceInstanceAndAssert(DummyServiceProvider.RETRY_INTERVAL_IN_SECONDS * 4, true, true,
                ['delay': String.valueOf(processDelayInSeconds), 'parentAlias': serviceInstanceAlias])
        then:
        serviceLifeCycler.getServiceInstanceStatus().state == LastOperationState.SUCCEEDED

        def si = serviceInstanceRepository.findByGuid(serviceLifeCycler.serviceInstanceId)
        assert si != null
        assert si.parentServiceInstance != null
    }


    def "provision async service instance with non-existing parent"() {
        when:
        serviceLifeCycler.setServiceInstanceId(UUID.randomUUID().toString())
        serviceLifeCycler.setServiceBindingId(UUID.randomUUID().toString())

        serviceLifeCycler.createServiceInstanceAndAssert(DummyServiceProvider.RETRY_INTERVAL_IN_SECONDS * 4, true, true,
                ['delay': String.valueOf(processDelayInSeconds), 'parentAlias': 'service-instance-xxx'])
        then:
        serviceLifeCycler.waitUntilMaxTimeOrTargetState(60)
        serviceLifeCycler.getServiceInstanceStatus().state == LastOperationState.FAILED
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
        serviceLifeCycler.deleteServiceInstanceAndAssert(true, DummyServiceProvider.RETRY_INTERVAL_IN_SECONDS * 4)
    }

    def "provision async service instance with CloudFoundryContext and alias"() {
        given:
        def serviceInstanceGuid = UUID.randomUUID().toString()
        serviceLifeCycler.setServiceInstanceId(serviceInstanceGuid)
        def context = new CloudFoundryContext("org_id", "space_id")

        when:
        serviceLifeCycler.createServiceInstanceAndAssert(0, false, false, ['alias': "service-instance-b"], context)

        then:
        assertCloudFoundryContext(serviceInstanceGuid)
        noExceptionThrown()
    }

    def "provision async service instance with different context and parent alias"() {
        given:
        def serviceInstanceGuid = UUID.randomUUID().toString()
        serviceLifeCycler.setServiceInstanceId(serviceInstanceGuid)
        def context = new CloudFoundryContext("org_id1", "space_id1")

        when:
        serviceLifeCycler.createServiceInstanceAndAssert(0, false, false, ['parentAlias': "service-instance-b"], context)

        then:
        def ex = thrown(HttpClientErrorException)
        ex.statusCode == HttpStatus.CONFLICT

        def serviceBrokerException = new ObjectMapper().readValue(ex.responseBodyAsString, ServiceBrokerException)
        serviceBrokerException.code == ErrorCode.SERVICE_INSTANCE_PARENT_CONTEXT_MISMATCH.code
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

