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
class ProvisionRequestsMetricsServiceService extends ServiceBrokerMetricsService {

    private final String PROVISION_REQUEST = "provisionRequest"

    @Autowired
    ProvisionRequestsMetricsServiceService(ServiceInstanceRepository serviceInstanceRepository, CFServiceRepository cfServiceRepository, LastOperationRepository lastOperationRepository, PlanRepository planRepository, MeterRegistry meterRegistry) {
        super(serviceInstanceRepository, cfServiceRepository, lastOperationRepository, planRepository)
        addMetricsToMeterRegistry(meterRegistry, serviceInstanceRepository)
    }

    void addMetricsToMeterRegistry(MeterRegistry meterRegistry, ServiceInstanceRepository serviceInstanceRepository) {
        super.addMetricsToMeterRegistry(meterRegistry, serviceInstanceRepository, PROVISION_REQUEST)
    }


    @Override
    boolean considerServiceInstance(ServiceInstance serviceInstance) {
        // every provision request should be counted, whether the service instance has been deleted or not is irrelevant
        return true;
    }

    @Override
    String tag() {
        return ProvisionRequestsMetricsServiceService.class.getSimpleName()
    }
}
