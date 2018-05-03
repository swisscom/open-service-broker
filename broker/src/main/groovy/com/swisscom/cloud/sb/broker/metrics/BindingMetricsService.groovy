package com.swisscom.cloud.sb.broker.metrics

import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.broker.model.repository.ServiceBindingRepository
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.actuate.metrics.Metric
import org.springframework.stereotype.Service

@Service
@CompileStatic
@Slf4j
class BindingMetricsService extends ServiceBrokerMetrics{

    @Autowired
    private ServiceBindingRepository serviceBindingRepository

    private final String BINDING = "binding"

    private long totalNrOfBindings

    void retrieveMetricsForTotalNrOfBindings() {

        def allServiceBindings = serviceBindingRepository.findAll()
        totalNrOfBindings = allServiceBindings.size()

        log.info("Total nr of provision requests: ${totalNrOfBindings}")

    }

    @Override
    Collection<Metric<?>> metrics() {
        List<Metric<?>> metrics = new ArrayList<>()

        retrieveMetricsForTotalNrOfBindings()
        metrics.add(new Metric<Long>("${BINDING}.${TOTAL}.${TOTAL}", totalNrOfBindings))

        return metrics
    }

    @Override
    boolean considerServiceInstance(ServiceInstance serviceInstance) {
        // TODO
        return false
    }

    @Override
    String tag() {
        return BindingMetricsService.class.getSimpleName()
    }
}
