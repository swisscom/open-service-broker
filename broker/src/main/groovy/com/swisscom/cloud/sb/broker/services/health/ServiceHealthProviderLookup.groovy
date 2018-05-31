package com.swisscom.cloud.sb.broker.services.health

import com.google.common.base.Optional
import com.google.common.base.Preconditions
import com.swisscom.cloud.sb.broker.cfextensions.serviceusage.ServiceUsageProvider
import com.swisscom.cloud.sb.broker.error.ErrorCode
import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.broker.model.repository.ServiceInstanceRepository
import com.swisscom.cloud.sb.broker.services.common.ServiceProvider
import com.swisscom.cloud.sb.broker.services.common.ServiceProviderLookup
import com.swisscom.cloud.sb.model.health.ServiceHealth
import com.swisscom.cloud.sb.model.health.ServiceHealthStatus
import com.swisscom.cloud.sb.model.usage.ServiceUsage
import groovy.transform.CompileStatic
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
@CompileStatic
class ServiceHealthProviderLookup {
    private final ServiceProviderLookup serviceProviderLookup
    private final ServiceInstanceRepository serviceInstanceRepository

    @Autowired
    ServiceHealthProviderLookup(
            ServiceProviderLookup serviceProviderLookup,
            ServiceInstanceRepository serviceInstanceRepository) {
        this.serviceInstanceRepository = serviceInstanceRepository
        this.serviceProviderLookup = serviceProviderLookup
    }

    ServiceHealth getHealthForServiceInstance(String serviceInstanceGuid) {
        def serviceInstance = serviceInstanceRepository.findByGuid(serviceInstanceGuid)

        if (!serviceInstance) {
            ErrorCode.SERVICE_INSTANCE_NOT_FOUND.throwNew()
        }

        getHealthForServiceInstance(serviceInstance)
    }

    ServiceHealth getHealthForServiceInstance(ServiceInstance serviceInstance) {
        Preconditions.checkNotNull(serviceInstance, "A valid ServiceInstance is required.")

        ServiceProvider serviceProvider = serviceProviderLookup.findServiceProvider(serviceInstance.plan)
        if (!(serviceProvider instanceof ServiceHealthProvider)) {
            return new ServiceHealth(status: ServiceHealthStatus.UNDEFINED)
        }

        (serviceProvider as ServiceHealthProvider).getHealth(serviceInstance)
    }
}
