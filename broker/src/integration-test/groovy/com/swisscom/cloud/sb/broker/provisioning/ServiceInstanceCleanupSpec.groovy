package com.swisscom.cloud.sb.broker.provisioning

import com.swisscom.cloud.sb.broker.model.LastOperation
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

@ContextConfiguration
@SpringBootTest(properties = "spring.autoconfigure.exclude=com.swisscom.cloud.sb.broker.util.httpserver.WebSecurityConfig")
@ComponentScan(excludeFilters = @ComponentScan.Filter(type = FilterType.ASPECTJ, pattern = "com.swisscom.cloud.sb.broker.util.httpserver.*"))
class ServiceInstanceCleanupSpec extends Specification {
    @Autowired
    private ServiceInstanceRepository serviceInstanceRepository

    @Autowired
    private ServiceBindingRepository serviceBindingRepository

    @Autowired
    private LastOperationRepository lastOperationRepository

    @Autowired
    private ProvisioningPersistenceService provisioningPersistenceService

    @Autowired
    private LastOperationPersistenceService lastOperationPersistenceService

    private ServiceInstanceCleanup sut

    def setup() {
        sut = new ServiceInstanceCleanup(provisioningPersistenceService,
                                         serviceInstanceRepository,
                                         lastOperationPersistenceService,
                                         lastOperationRepository)
    }

    def "should successfully mark service instance for purge"() {
        given:
        ServiceInstance serviceInstance = new ServiceInstance(guid: serviceInstanceGuid)
        serviceInstanceRepository.save(serviceInstance)

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

        where:
        serviceInstanceGuid = UUID.randomUUID().toString()
    }
}
