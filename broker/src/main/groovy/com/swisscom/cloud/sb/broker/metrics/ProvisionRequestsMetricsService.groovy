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

    private final String PROVISION_REQUEST = "provisionRequest"

    @Autowired
    ProvisionRequestsMetricsService(ServiceInstanceRepository serviceInstanceRepository, LastOperationRepository lastOperationRepository) {
        super(serviceInstanceRepository, lastOperationRepository)
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

    @Override
    Collection<Metric<?>> metrics() {
        List<Metric<?>> metrics = new ArrayList<>()
        List<ServiceInstance> serviceInstanceList = serviceInstanceRepository.findAll()

        def totalMetrics = retrieveTotalMetrics(serviceInstanceList)
        metrics.add(new Metric<Long>("${PROVISION_REQUEST}.${TOTAL}.${TOTAL}", totalMetrics.total))
        metrics.add(new Metric<Long>("${PROVISION_REQUEST}.${TOTAL}.${SUCCESS}", totalMetrics.totalSuccess))
        metrics.add(new Metric<Long>("${PROVISION_REQUEST}.${TOTAL}.${FAIL}", totalMetrics.totalFailures))
        metrics.add(new Metric<Double>("${PROVISION_REQUEST}.${SUCCESS}.${RATIO}", calculateRatio(totalMetrics.total, totalMetrics.totalSuccess)))
        metrics.add(new Metric<Double>("${PROVISION_REQUEST}.${FAIL}.${RATIO}", calculateRatio(totalMetrics.total, totalMetrics.totalFailures)))

        def totalMetricsPerService = retrieveTotalMetricsPerService(serviceInstanceList)
        metrics = addCountersFromHashMapToMetrics(totalMetricsPerService.total, totalMetricsPerService.total, metrics, PROVISION_REQUEST, SERVICE, TOTAL)
        metrics = addCountersFromHashMapToMetrics(totalMetricsPerService.total, totalMetricsPerService.totalSuccess, metrics, PROVISION_REQUEST, SERVICE, SUCCESS)
        metrics = addCountersFromHashMapToMetrics(totalMetricsPerService.total, totalMetricsPerService.totalFailures, metrics, PROVISION_REQUEST, SERVICE, FAIL)

        def totalMetricsPerPlan = retrieveTotalMetricsPerPlan(serviceInstanceList)
        metrics = addCountersFromHashMapToMetrics(totalMetricsPerPlan.total, totalMetricsPerPlan.total, metrics, PROVISION_REQUEST, PLAN, TOTAL)
        metrics = addCountersFromHashMapToMetrics(totalMetricsPerPlan.total, totalMetricsPerPlan.totalSuccess, metrics, PROVISION_REQUEST, PLAN, SUCCESS)
        metrics = addCountersFromHashMapToMetrics(totalMetricsPerPlan.total, totalMetricsPerPlan.totalFailures, metrics, PROVISION_REQUEST, PLAN, FAIL)

        return metrics
    }
}