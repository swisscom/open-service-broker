package com.swisscom.cloud.sb.broker.provisioning

import com.swisscom.cloud.sb.broker.model.LastOperation
import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.broker.model.repository.ServiceInstanceRepository
import com.swisscom.cloud.sb.broker.provisioning.lastoperation.LastOperationPersistenceService
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.joda.time.LocalDateTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@CompileStatic
@Slf4j
@Transactional
class ServiceInstanceCleanup {
    public static final int MONTHS_TO_KEEP_DELETED_INSTANCE_REFERENCES = 3

    @Autowired
    private ProvisioningPersistenceService provisioningPersistenceService

    @Autowired
    private ServiceInstanceRepository serviceInstanceRepository

    @Autowired
    LastOperationPersistenceService lastOperationPersistenceService

    def cleanOrphanedServiceInstances() {
        def deleteOlderThan = new LocalDateTime().minusMonths(MONTHS_TO_KEEP_DELETED_INSTANCE_REFERENCES).toDate()
        def oprhanedServiceInstances = serviceInstanceRepository.queryServiceInstanceForLastOperation(LastOperation.Operation.DEPROVISION, LastOperation.Status.SUCCESS, deleteOlderThan)
        def candidateCount = oprhanedServiceInstances.size()
        log.info("Found ${candidateCount} serviceInstance candidate(s) to clean up!")
        oprhanedServiceInstances.each { ServiceInstance si ->
            provisioningPersistenceService.deleteServiceInstanceAndCorrespondingDeprovisionRequestIfExists(si)
            lastOperationPersistenceService.deleteLastOperation(si.guid)
        }
        return candidateCount
    }
}
