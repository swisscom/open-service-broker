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

import com.google.common.base.Preconditions
import com.swisscom.cloud.sb.broker.model.LastOperation
import com.swisscom.cloud.sb.broker.model.ServiceBinding
import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.broker.provisioning.lastoperation.LastOperationPersistenceService
import com.swisscom.cloud.sb.broker.repository.LastOperationRepository
import com.swisscom.cloud.sb.broker.repository.ServiceInstanceRepository
import com.swisscom.cloud.sb.broker.services.credential.CredentialStore
import com.swisscom.cloud.sb.broker.util.Audit
import groovy.transform.CompileStatic
import org.apache.commons.lang3.StringUtils
import org.joda.time.LocalDateTime
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

import static com.google.common.base.Preconditions.checkArgument

@Component
@CompileStatic
@Transactional
class ServiceInstanceCleanup {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceInstanceCleanup.class)
    public static final int MONTHS_TO_KEEP_DELETED_INSTANCE_REFERENCES = 3

    private final ProvisioningPersistenceService provisioningPersistenceService
    private final ServiceInstanceRepository serviceInstanceRepository
    private final LastOperationPersistenceService lastOperationPersistenceService
    private final LastOperationRepository lastOperationRepository
    private final CredentialStore credentialStore

    ServiceInstanceCleanup(ProvisioningPersistenceService provisioningPersistenceService,
                           ServiceInstanceRepository serviceInstanceRepository,
                           LastOperationPersistenceService lastOperationPersistenceService,
                           LastOperationRepository lastOperationRepository) {
        this.provisioningPersistenceService = provisioningPersistenceService
        this.serviceInstanceRepository = serviceInstanceRepository
        this.lastOperationPersistenceService = lastOperationPersistenceService
        this.lastOperationRepository = lastOperationRepository
    }

    def cleanOrphanedServiceInstances() {
        def deleteOlderThan = new LocalDateTime().minusMonths(MONTHS_TO_KEEP_DELETED_INSTANCE_REFERENCES).toDate()
        def oprhanedServiceInstances = serviceInstanceRepository.
                queryServiceInstanceForLastOperation(LastOperation.Operation.DEPROVISION,
                                                     LastOperation.Status.SUCCESS,
                                                     deleteOlderThan)
        def candidateCount = oprhanedServiceInstances.size()
        LOGGER.info("Found ${candidateCount} serviceInstance candidate(s) to clean up!")
        oprhanedServiceInstances.each {ServiceInstance si ->
            provisioningPersistenceService.deleteServiceInstanceAndCorrespondingDeprovisionRequestIfExists(si)
            lastOperationPersistenceService.deleteLastOperation(si.guid)

            Audit.log("Delete service instance",
                      [
                              serviceInstanceGuid: si.guid,
                              action             : Audit.AuditAction.Delete
                      ]
            )
        }
        return candidateCount
    }

    /**
     * Marks a service instance for cleanup and removes any CredHub credentials from bindings
     * @param serviceInstanceGuid to be purged
     * @return the purged service instance
     */
    ServiceInstance markServiceInstanceForPurge(String serviceInstanceGuid) {
        checkArgument(StringUtils.isNotBlank(serviceInstanceGuid), "Service Instance Guid cannot be empty")
        ServiceInstance serviceInstanceToPurge = provisioningPersistenceService.getServiceInstance(serviceInstanceGuid)
        checkArgument(serviceInstanceToPurge != null, "Service Instance Guid does not exist")

        try {
            serviceInstanceToPurge.
                    getBindings().
                    forEach({binding -> deleteCredentialInCredHub(binding as ServiceBinding)})
        } catch (Exception e) {
            LOGGER.error("Ignoring any CredHub problems while purging a service instance. Got following exception:", e)
        }

        provisioningPersistenceService.markServiceInstanceAsDeleted(serviceInstanceToPurge)
        setSuccessfulDeprovisionLastOperation(serviceInstanceGuid)
        return serviceInstanceToPurge
    }

    /**
     * For the cleanup process to actually cleanup a service instance there must be
     * a successful deprovision last operation for the given service instance.
     * @param serviceInstanceGuid to be set a successful last operation for
     * @return crated LastOperation
     */
    private LastOperation setSuccessfulDeprovisionLastOperation(String serviceInstanceGuid) {
        LastOperation lastOperation = lastOperationPersistenceService.
                createOrUpdateLastOperation(serviceInstanceGuid, LastOperation.Operation.DEPROVISION)
        lastOperation.status = LastOperation.Status.SUCCESS
        lastOperationRepository.save(lastOperation)
    }

    private ServiceBinding deleteCredentialInCredHub(ServiceBinding binding) {
        checkArgument(binding != null, "Binding to delete should not be null")
        if (binding.credhubCredentialId != null && StringUtils.isNotBlank(binding.credhubCredentialId)) {
            return credentialStore.delete(binding)
        }
        return binding
    }
}
