package com.swisscom.cloud.sb.broker.metrics

import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.broker.model.repository.CFServiceRepository
import com.swisscom.cloud.sb.broker.model.repository.LastOperationRepository
import com.swisscom.cloud.sb.broker.model.repository.PlanRepository
import com.swisscom.cloud.sb.broker.model.repository.ServiceInstanceRepository
import groovy.transform.CompileStatic
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
@CompileStatic
class ProvisionedInstancesMetricsService extends ServiceBrokerMetricsService {

    private final String PROVISIONED_INSTANCES = "provisionedInstances"

    @Autowired
    ProvisionedInstancesMetricsService(LastOperationRepository lastOperationRepository, MetricsCache metricsCache, MeterRegistry meterRegistry) {
        super(lastOperationRepository, metricsCache)
        addMetricsToMeterRegistry(meterRegistry)
    }

    void addMetricsToMeterRegistry(MeterRegistry meterRegistry){
        super.addMetricsToMeterRegistry(meterRegistry, PROVISIONED_INSTANCES)
    }


    @Override
    boolean considerServiceInstance(ServiceInstance serviceInstance) {
        // service instance should only be considered if it hasn't been deleted, yet
        return !serviceInstance.deleted
    }

    @Override
    String tag() {
        return ProvisionedInstancesMetricsService.class.getSimpleName()
    }
}