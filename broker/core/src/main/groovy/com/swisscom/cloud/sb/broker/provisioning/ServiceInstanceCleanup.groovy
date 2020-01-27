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

import com.swisscom.cloud.sb.broker.backup.SystemBackupProvider
import com.swisscom.cloud.sb.broker.binding.ServiceBindingPersistenceService
import com.swisscom.cloud.sb.broker.cfextensions.ServiceInstancePurgeInformation
import com.swisscom.cloud.sb.broker.model.LastOperation
import com.swisscom.cloud.sb.broker.model.Plan
import com.swisscom.cloud.sb.broker.model.ServiceBinding
import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.broker.provisioning.lastoperation.LastOperationPersistenceService
import com.swisscom.cloud.sb.broker.repository.LastOperationRepository
import com.swisscom.cloud.sb.broker.repository.PlanRepository
import com.swisscom.cloud.sb.broker.repository.ServiceInstanceRepository
import com.swisscom.cloud.sb.broker.services.ServiceProviderLookup
import com.swisscom.cloud.sb.broker.services.common.ServiceProvider
import com.swisscom.cloud.sb.broker.util.Audit
import groovy.transform.CompileStatic
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.tuple.Pair
import org.joda.time.LocalDateTime
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

import static com.google.common.base.Preconditions.checkArgument
import static com.swisscom.cloud.sb.broker.cfextensions.ServiceInstancePurgeInformation.serviceInstancePurgeInformation

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
    private final ServiceBindingPersistenceService serviceBindingPersistenceService
    private final ServiceProviderLookup serviceProviderLookup
    private final PlanRepository planRepository

    ServiceInstanceCleanup(ProvisioningPersistenceService provisioningPersistenceService,
                           ServiceInstanceRepository serviceInstanceRepository,
                           LastOperationPersistenceService lastOperationPersistenceService,
                           LastOperationRepository lastOperationRepository,
                           ServiceBindingPersistenceService serviceBindingPersistenceService,
                           ServiceProviderLookup serviceProviderLookup,
                           PlanRepository planRepository) {
        this.provisioningPersistenceService = provisioningPersistenceService
        this.serviceInstanceRepository = serviceInstanceRepository
        this.lastOperationPersistenceService = lastOperationPersistenceService
        this.lastOperationRepository = lastOperationRepository
        this.serviceBindingPersistenceService = serviceBindingPersistenceService
        this.serviceProviderLookup = serviceProviderLookup
        this.planRepository = planRepository
    }

    def cleanOrphanedServiceInstances() {
        def deleteOlderThan = new LocalDateTime().minusMonths(MONTHS_TO_KEEP_DELETED_INSTANCE_REFERENCES).toDate()
        def oprhanedServiceInstances = serviceInstanceRepository.
                queryServiceInstanceForLastOperation(LastOperation.Operation.DEPROVISION,
                                                     LastOperation.Status.SUCCESS,
                                                     deleteOlderThan)
        def candidateCount = oprhanedServiceInstances.size()
        LOGGER.info("Found {} serviceInstance candidate(s) to clean up!", candidateCount)
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
     * Marks a service instance for cleanup, removes any CredHub credentials from bindings and deregisters the service
     * instance from the backup system (if it is a {@link com.swisscom.cloud.sb.broker.backup.SystemBackupProvider})
     * @param serviceInstanceGuid to be purged
     * @return the purged service instance
     */
    ServiceInstancePurgeInformation markServiceInstanceForPurge(String serviceInstanceGuid) {
        checkArgument(StringUtils.isNotBlank(serviceInstanceGuid), "Service Instance Guid cannot be empty")
        ServiceInstance serviceInstanceToPurge = provisioningPersistenceService.getServiceInstance(serviceInstanceGuid)
        checkArgument(serviceInstanceToPurge != null,
                      "Service Instance Guid '" + serviceInstanceGuid + "' does not exist")

        ServiceInstancePurgeInformation.Builder result = serviceInstancePurgeInformation()
        Set<String> errors = new HashSet<>()

        Audit.log("Purging service instance",
                  [
                          serviceInstanceGuid: serviceInstanceGuid,
                          action             : Audit.AuditAction.Delete
                  ]
        )

        provisioningPersistenceService.markServiceInstanceAsDeleted(serviceInstanceToPurge)
        setSuccessfulDeprovisionLastOperation(serviceInstanceGuid)
        int deletedBindings = 0
        for (ServiceBinding binding : serviceInstanceToPurge.getBindings()) {
            try {
                serviceBindingPersistenceService.delete(binding, serviceInstanceToPurge)
                deletedBindings += 1
            } catch (Exception e) {
                LOGGER.
                        error("Ignoring any unbinding problems while purging service instance {}, failed to delete binding {}.",
                              serviceInstanceGuid,
                              binding.getGuid(),
                              e)
                errors.add("Failed to delete binding " + binding.getGuid())
            }
        }
        Pair<Boolean, String> deregisterdFromBackup = deregisterFromBackup(serviceInstanceToPurge.getPlan(),
                                                                           serviceInstanceGuid)
        if (!deregisterdFromBackup.getRight().isEmpty()) {
            errors.add(deregisterdFromBackup.getRight())
        }

        return result.
                purgedServiceInstanceGuid(serviceInstanceGuid).
                deletedBindings(deletedBindings).
                systemBackupProvider(deregisterdFromBackup.getLeft()).
                errors(errors).
                build()
    }

    /**
     * For the cleanup process to actually cleanup a service instance there must be
     * a successful deprovision last operation for the given service instance.
     * @param serviceInstanceGuid to be set a successful last operation for
     * @return created LastOperation
     */
    private LastOperation setSuccessfulDeprovisionLastOperation(String serviceInstanceGuid) {
        LastOperation lastOperation = lastOperationPersistenceService.
                createOrUpdateLastOperation(serviceInstanceGuid, LastOperation.Operation.DEPROVISION)
        lastOperation.status = LastOperation.Status.SUCCESS
        lastOperation.description = "Set as successful deprovision by admin service instance purging"
        lastOperationRepository.save(lastOperation)
    }

    /**
     * Tries to deregister service instance from the backup system
     * @param plan
     * @param serviceInstanceGuid
     * @return ([BooleanspecifyingwhetherServiceProviderisSystemBackupProvider],
     *[Stringcontainingerrors,emptyStringifnoerrorsoccurred])
     */
    private Pair<Boolean, String> deregisterFromBackup(Plan plan, String serviceInstanceGuid) {
        Plan planWithService = planRepository.findByGuidAndFetchServiceEagerly(plan.getGuid())
        ServiceProvider serviceProvider = serviceProviderLookup.findServiceProvider(planWithService)

        if (serviceProvider instanceof SystemBackupProvider) {
            try {
                serviceProvider.unregisterSystemBackupOnShield(serviceInstanceGuid)
                return Pair.of(true, "");
            } catch (Exception e) {
                LOGGER.error("Failed to deregister service instance {} from backup system."
                                     + "Ignoring failure while purging a service instance.", serviceInstanceGuid, e)
                return Pair.of(true, "Failed to deregister from backup system")
            }
        }
        return Pair.of(false, "")
    }
}
