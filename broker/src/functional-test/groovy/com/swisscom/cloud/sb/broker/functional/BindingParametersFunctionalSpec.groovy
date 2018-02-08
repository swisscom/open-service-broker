package com.swisscom.cloud.sb.broker.functional

import com.swisscom.cloud.sb.broker.context.ContextPersistenceService
import com.swisscom.cloud.sb.broker.model.repository.ContextRepository
import com.swisscom.cloud.sb.broker.model.repository.ServiceInstanceRepository
import com.swisscom.cloud.sb.broker.services.common.ServiceProviderLookup
import com.swisscom.cloud.sb.broker.util.test.DummySynchronousServiceProvider
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.servicebroker.model.CloudFoundryContext

import static org.junit.Assert.assertNotNull

class BindingParametersFunctionalSpec extends BaseFunctionalSpec {

    @Autowired
    private ContextRepository contextRepository
    @Autowired
    private ServiceInstanceRepository serviceInstanceRepository

    def setup() {
        serviceLifeCycler.createServiceIfDoesNotExist('SyncDummy', ServiceProviderLookup.findInternalName(DummySynchronousServiceProvider.class))
    }

    def cleanupSpec() {
        serviceLifeCycler.cleanup()
    }

    def "provision async service instance"() {
        given:
        serviceLifeCycler.createServiceInstanceAndAssert(0, false, false, null)

        when:
        serviceLifeCycler.bindServiceInstanceAndAssert(null, ['key1': 'value1'])

        then:
        noExceptionThrown()
    }

    def "provision async service instance with Context"() {
        given:
        def serviceInstanceGuid = UUID.randomUUID().toString()
        serviceLifeCycler.setServiceInstanceId(serviceInstanceGuid)
        serviceLifeCycler.setServiceBindingId(UUID.randomUUID().toString())
        def context = new CloudFoundryContext("org_id", "space_id")
        serviceLifeCycler.createServiceInstanceAndAssert(0, false, false, null, context)

        when:
        serviceLifeCycler.bindServiceInstanceAndAssert(null, ['key1': 'value1'])

        then:
        assertCloudFoundryContext(serviceInstanceGuid)
        noExceptionThrown()
    }

    def "provision async service instance and Binding with Context"() {
        given:
        def serviceInstanceGuid = UUID.randomUUID().toString()
        serviceLifeCycler.setServiceInstanceId(serviceInstanceGuid)
        serviceLifeCycler.setServiceBindingId(UUID.randomUUID().toString())
        serviceLifeCycler.createServiceInstanceAndAssert(0, false, false, null)

        when:
        def context = new CloudFoundryContext("org_id", "space_id")
        serviceLifeCycler.bindServiceInstanceAndAssert(null, ['key1': 'value1'], true, context)

        then:
        assertCloudFoundryContext(serviceInstanceGuid)
        noExceptionThrown()
    }

    def "deprovision async service instance"() {
        when:
        serviceLifeCycler.deleteServiceBindingAndServiceInstaceAndAssert()

        then:
        noExceptionThrown()
    }

    void assertCloudFoundryContext(String serviceInstanceGuid) {
        def serviceInstance = serviceInstanceRepository.findByGuid(serviceInstanceGuid)
        assertNotNull(serviceInstance)

        assertNotNull(contextRepository.findByKeyAndServiceInstance(ContextPersistenceService.PLATFORM, serviceInstance))
        assertNotNull(contextRepository.findByKeyAndServiceInstance(ContextPersistenceService.CF_ORGANIZATION_GUID, serviceInstance))
        assertNotNull(contextRepository.findByKeyAndServiceInstance(ContextPersistenceService.CF_SPACE_GUID, serviceInstance))
    }

}