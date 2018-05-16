package com.swisscom.cloud.sb.broker.metrics

import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.broker.model.repository.LastOperationRepository
import com.swisscom.cloud.sb.broker.model.repository.ServiceInstanceRepository
import groovy.transform.CompileStatic
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.actuate.metrics.Metric
import org.springframework.stereotype.Service

@Service
@CompileStatic
class ProvisionRequestsMetricsService extends ServiceBrokerMetrics {

    @Autowired
    ProvisionRequestsMetricsService(ServiceInstanceRepository serviceInstanceRepository, LastOperationRepository lastOperationRepository) {
        super(serviceInstanceRepository, lastOperationRepository)
    }

    private final String PROVISION_REQUEST = "provisionRequest"

    @Override
    boolean considerServiceInstance(ServiceInstance serviceInstance) {
        // every provision request should be counted, whether the service instance has been deleted or not is irrelevant
        return true;
    }

    @Override
    String tag() {
        return ProvisionRequestsMetricsService.class.getSimpleName()
    }

    @Override
    Collection<Metric<?>> metrics() {
        List<Metric<?>> metrics = new ArrayList<>()

        retrieveTotalMetrics()
        metrics.add(new Metric<Long>("${PROVISION_REQUEST}.${TOTAL}.${TOTAL}", total))
        metrics.add(new Metric<Long>("${PROVISION_REQUEST}.${TOTAL}.${SUCCESS}", totalSuccess))
        metrics.add(new Metric<Long>("${PROVISION_REQUEST}.${TOTAL}.${FAIL}", totalFailure))
        metrics.add(new Metric<Double>("${PROVISION_REQUEST}.${SUCCESS}.${RATIO}", calculateRatio(total, totalSuccess)))
        metrics.add(new Metric<Double>("${PROVISION_REQUEST}.${FAIL}.${RATIO}", calculateRatio(total, totalFailure)))

        retrieveTotalMetricsPerService()
        metrics = addCountersFromHashMapToMetrics(totalPerService, totalPerService, metrics, PROVISION_REQUEST, SERVICE, TOTAL)
        metrics = addCountersFromHashMapToMetrics(totalPerService, totalSuccessPerService, metrics, PROVISION_REQUEST, SERVICE, SUCCESS)
        metrics = addCountersFromHashMapToMetrics(totalPerService, totalFailurePerService, metrics, PROVISION_REQUEST, SERVICE, FAIL)

        retrieveTotalMetricsPerPlan()
        metrics = addCountersFromHashMapToMetrics(totalPerPlan, totalPerPlan, metrics, PROVISION_REQUEST, PLAN, TOTAL)
        metrics = addCountersFromHashMapToMetrics(totalPerPlan, totalSuccessPerPlan, metrics, PROVISION_REQUEST, PLAN, SUCCESS)
        metrics = addCountersFromHashMapToMetrics(totalPerPlan, totalFailurePerPlan, metrics, PROVISION_REQUEST, PLAN, FAIL)

        return metrics
    }
}
