package com.swisscom.cf.broker.provisioning

import com.swisscom.cf.broker.model.LastOperation
import com.swisscom.cf.broker.model.ServiceInstance
import com.swisscom.cf.broker.model.repository.ServiceInstanceRepository
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

    //TODO
/*
    @Autowired
    LastOperationPersistenceService lastOperationPersistenceService
*/

    def cleanOrphanedServiceInstances() {
        def deleteOlderThan = new LocalDateTime().minusMonths(MONTHS_TO_KEEP_DELETED_INSTANCE_REFERENCES).toDate()
        def oprhanedServiceInstances = serviceInstanceRepository.queryServiceInstanceForLastOperation(LastOperation.Operation.DEPROVISION, LastOperation.Status.SUCCESS, deleteOlderThan)
        def candidateCount = oprhanedServiceInstances.size()
        log.info("Found ${candidateCount} serviceInstance candidate(s) to clean up!")
        oprhanedServiceInstances.each { ServiceInstance si ->
            provisioningPersistenceService.deleteServiceInstanceAndCorrespondingDeprovisionRequestIfExists(si)
            //lastOperationPersistenceService.deleteLastOpeation(si.guid)
        }
        return candidateCount
    }
}
