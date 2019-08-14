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

package com.swisscom.cloud.sb.broker.provisioning

import com.swisscom.cloud.sb.broker.model.LastOperation
import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.broker.repository.ServiceInstanceRepository
import com.swisscom.cloud.sb.broker.provisioning.lastoperation.LastOperationPersistenceService
import com.swisscom.cloud.sb.broker.util.Audit
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

            Audit.log("Delete service instance",
                    [
                            serviceInstanceGuid: si.guid,
                            action: Audit.AuditAction.Delete
                    ]
            )
        }
        return candidateCount
    }
}
