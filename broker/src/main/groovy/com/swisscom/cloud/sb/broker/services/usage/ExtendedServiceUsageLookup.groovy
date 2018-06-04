package com.swisscom.cloud.sb.broker.services.usage

import com.google.common.base.Preconditions
import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.broker.services.common.ServiceProvider
import com.swisscom.cloud.sb.broker.services.common.ServiceProviderLookup
import com.swisscom.cloud.sb.model.usage.extended.ServiceUsageItem
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
@CompileStatic
@Slf4j
class ExtendedServiceUsageLookup {
    private final ServiceProviderLookup serviceProviderLookup

    @Autowired
    ExtendedServiceUsageLookup(ServiceProviderLookup serviceProviderLookup) {
        this.serviceProviderLookup = serviceProviderLookup
    }

    Set<ServiceUsageItem> getUsage(ServiceInstance serviceInstance) {
        Preconditions.checkNotNull(serviceInstance, "A valid ServiceInstance is required.")

        ServiceProvider serviceProvider = serviceProviderLookup.findServiceProvider(serviceInstance.plan)
        if (!(serviceProvider instanceof ExtendedServiceUsageProvider)) {
            log.info("Usage requested for serviceinstance(guid:${serviceInstance.guid}) " +
                        "with plan(guid:${serviceInstance.plan.guid},name:${serviceInstance.plan.name}) " +
                        "not providing a ExtendedServiceUsageProvider implementation.")

            return new HashSet<ServiceUsageItem>()
        }

        (serviceProvider as ExtendedServiceUsageProvider).getUsages(serviceInstance)
    }
}
