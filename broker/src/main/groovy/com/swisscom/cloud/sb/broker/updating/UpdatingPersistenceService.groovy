package com.swisscom.cloud.sb.broker.updating

import com.swisscom.cloud.sb.broker.model.Plan
import com.swisscom.cloud.sb.broker.model.ServiceDetail
import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.broker.model.UpdateRequest
import com.swisscom.cloud.sb.broker.model.repository.ServiceInstanceRepository
import com.swisscom.cloud.sb.broker.model.repository.UpdateRequestRepository
import com.swisscom.cloud.sb.broker.provisioning.ProvisioningPersistenceService
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Slf4j
@Service
@Transactional
class UpdatingPersistenceService {
    private UpdateRequestRepository updateRequestRepository
    private ServiceInstanceRepository serviceInstanceRepository
    private ProvisioningPersistenceService provisioningPersistenceService

    @Autowired
    UpdatingPersistenceService(
            UpdateRequestRepository updateRequestRepository,
            ServiceInstanceRepository serviceInstanceRepository,
            ProvisioningPersistenceService provisioningPersistenceService)
    {
        this.updateRequestRepository = updateRequestRepository
        this.serviceInstanceRepository = serviceInstanceRepository
        this.provisioningPersistenceService = provisioningPersistenceService
    }

    void saveUpdateRequest(UpdateRequest updateRequest) {
        updateRequestRepository.save(updateRequest)
    }

    void updatePlanAndServiceDetails(final ServiceInstance serviceInstance, final Collection<ServiceDetail> serviceDetails, Plan plan) {
        serviceInstance.plan = plan
        serviceInstanceRepository.saveAndFlush(serviceInstance)
        updateServiceDetails(serviceDetails, serviceInstance)
    }

    void updateServiceDetails(final Collection<ServiceDetail> serviceDetails, final ServiceInstance serviceInstance) {
        provisioningPersistenceService.updateServiceDetails(serviceDetails, serviceInstance)
    }
}
