package com.swisscom.cloud.sb.broker.provisioning

import com.swisscom.cloud.sb.broker.binding.ServiceBindingPersistenceService
import com.swisscom.cloud.sb.broker.model.LastOperation
import com.swisscom.cloud.sb.broker.model.ServiceBinding
import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.broker.provisioning.lastoperation.LastOperationPersistenceService
import com.swisscom.cloud.sb.broker.repository.LastOperationRepository
import com.swisscom.cloud.sb.broker.repository.PlanRepository
import com.swisscom.cloud.sb.broker.repository.ServiceBindingRepository
import com.swisscom.cloud.sb.broker.repository.ServiceInstanceRepository
import com.swisscom.cloud.sb.broker.services.ServiceProviderLookup
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification
import spock.lang.Unroll

@ContextConfiguration
@ActiveProfiles("default,test,secrets")
@SpringBootTest(properties = "spring.autoconfigure.exclude=com.swisscom.cloud.sb.broker.util.httpserver.WebSecurityConfig")
@ComponentScan(excludeFilters = @ComponentScan.Filter(type = FilterType.
        ASPECTJ, pattern = "com.swisscom.cloud.sb.broker.util.httpserver.*"))
class ServiceInstanceCleanupSpec extends Specification {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceInstanceCleanupSpec.class)

    @Autowired
    private ServiceInstanceRepository serviceInstanceRepository

    @Autowired
    private LastOperationRepository lastOperationRepository

    @Autowired
    private ProvisioningPersistenceService provisioningPersistenceService

    @Autowired
    private LastOperationPersistenceService lastOperationPersistenceService

    @Autowired
    private ServiceBindingPersistenceService serviceBindingPersistenceService

    @Autowired
    private ServiceBindingRepository serviceBindingRepository

    @Autowired
    private ServiceProviderLookup serviceProviderLookup

    @Autowired
    private PlanRepository planRepository

    private ServiceInstanceCleanup sut

    def setup() {
        sut = new ServiceInstanceCleanup(provisioningPersistenceService,
                                         serviceInstanceRepository,
                                         lastOperationPersistenceService,
                                         lastOperationRepository,
                                         serviceBindingPersistenceService,
                                         serviceProviderLookup)
    }

    @Unroll
    def "should successfully mark service instance '#serviceInstanceGuid' for purge also remove binding '#bindingGuid'"() {
        given: "the service instance to be cleaned up"
        LOGGER.info("{}", planRepository.findAll().each({p -> p.getService().getName()}))
        LOGGER.info("Plans: {}", planRepository.findAll())
        ServiceInstance serviceInstance = new ServiceInstance(guid: serviceInstanceGuid, plan: planRepository.findByGuid(planGuid))
        serviceInstanceRepository.save(serviceInstance)

        and: "a service binding associated to the service instance to be purged"
        serviceBindingPersistenceService.
                create(serviceInstance, '{"foo": "bar"}', "no parameters", bindingGuid, [], null, "cc_admin")

        when:
        ServiceInstance result = sut.markServiceInstanceForPurge(serviceInstanceGuid)

        then: "should return the purged service instance"
        result != null
        result.guid == serviceInstanceGuid

        and: "should have marked the service instance to be cleaned up"
        ServiceInstance markedServiceInstance = serviceInstanceRepository.findByGuid(serviceInstanceGuid)
        markedServiceInstance.isDeleted()
        markedServiceInstance.getDateDeleted().before(new Date())

        and: "should have created a successful deprovision last operation"
        LastOperation lastOperation = lastOperationRepository.findByGuid(serviceInstanceGuid)
        lastOperation.getOperation() == LastOperation.Operation.DEPROVISION
        lastOperation.getStatus() == LastOperation.Status.SUCCESS

        and: "should have removed the binding"
        ServiceBinding serviceBinding = serviceBindingRepository.findByGuid(bindingGuid)
        serviceBinding == null

        where:
        serviceInstanceGuid << [UUID.randomUUID().toString(), UUID.randomUUID().toString()]
        bindingGuid << [UUID.randomUUID().toString(), UUID.randomUUID().toString()]
        planGuid << ["0ef19631-1212-47cc-9c77-22d78ddaae3a", "47273c6a-ff8b-40d6-9981-2b25663718a1"]
    }

    @Unroll
    def "should fail to mark a service instance for purge which does not exist for service instance guid: '#serviceInstanceGuid'"() {
        when:
        ServiceInstance result = sut.markServiceInstanceForPurge(serviceInstanceGuid)

        then: "should return the purged service instance"
        result == null
        def ex = thrown(IllegalArgumentException)
        ex.getMessage() == String.format(message, serviceInstanceGuid)

        where:
        serviceInstanceGuid          | message
        UUID.randomUUID().toString() | "Service Instance Guid '%s' does not exist"
        null                         | "Service Instance Guid cannot be empty"
        " "                          | "Service Instance Guid cannot be empty"

    }
}
