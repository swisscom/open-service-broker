package com.swisscom.cloud.sb.broker.provisioning

import com.swisscom.cloud.sb.broker.binding.ServiceBindingPersistenceService
import com.swisscom.cloud.sb.broker.model.LastOperation
import com.swisscom.cloud.sb.broker.model.ServiceBinding
import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.broker.provisioning.lastoperation.LastOperationPersistenceService
import com.swisscom.cloud.sb.broker.repository.LastOperationRepository
import com.swisscom.cloud.sb.broker.repository.ServiceBindingRepository
import com.swisscom.cloud.sb.broker.repository.ServiceInstanceRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification
import spock.lang.Unroll

@ContextConfiguration
@SpringBootTest(properties = "spring.autoconfigure.exclude=com.swisscom.cloud.sb.broker.util.httpserver.WebSecurityConfig")
@ComponentScan(excludeFilters = @ComponentScan.Filter(type = FilterType.
        ASPECTJ, pattern = "com.swisscom.cloud.sb.broker.util.httpserver.*"))
class ServiceInstanceCleanupSpec extends Specification {
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

    private ServiceInstanceCleanup sut

    def setup() {
        sut = new ServiceInstanceCleanup(provisioningPersistenceService,
                                         serviceInstanceRepository,
                                         lastOperationPersistenceService,
                                         lastOperationRepository,
                                         serviceBindingPersistenceService)
    }

    @Unroll
    def "should successfully mark service instance '#serviceInstanceGuid' for purge also remove binding '#bindingGuid'"() {
        given: "the service instance to be cleaned up"
        ServiceInstance serviceInstance = new ServiceInstance(guid: serviceInstanceGuid)
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
        serviceInstanceGuid = UUID.randomUUID().toString()
        bindingGuid = UUID.randomUUID().toString()
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
