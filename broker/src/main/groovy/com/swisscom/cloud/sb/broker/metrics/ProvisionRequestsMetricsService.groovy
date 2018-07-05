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
class ProvisionRequestsMetricsService extends ServiceBrokerMetricsService {

    private final String PROVISION_REQUEST = "provisionRequest"

    @Autowired
    ProvisionRequestsMetricsService(LastOperationRepository lastOperationRepository, MetricsCache metricsCache, MeterRegistry meterRegistry) {
        super(lastOperationRepository, metricsCache)
        addMetricsToMeterRegistry(meterRegistry)
    }

    void addMetricsToMeterRegistry(MeterRegistry meterRegistry) {
        super.addMetricsToMeterRegistry(meterRegistry, PROVISION_REQUEST)
    }


    @Override
    boolean considerServiceInstance(ServiceInstance serviceInstance) {
        // every provision request should be counted, whether the service instance has been deleted or not is irrelevant
        return true;
    }

    @Override
    String tag() {
        return ProvisionRequestsMetricsService.class.getSimpleName()
    }
}
