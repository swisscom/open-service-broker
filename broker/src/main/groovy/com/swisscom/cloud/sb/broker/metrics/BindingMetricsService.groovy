package com.swisscom.cloud.sb.broker.metrics

import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.broker.model.ServiceBinding
import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.broker.model.repository.ServiceBindingRepository
import com.swisscom.cloud.sb.broker.model.repository.ServiceInstanceRepository
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.actuate.metrics.Metric
import org.springframework.boot.autoconfigure.AutoConfigureOrder
import org.springframework.stereotype.Service

@Service
@CompileStatic
@Slf4j
class BindingMetricsService extends ServiceBrokerMetrics{

    @Autowired
    private ServiceBindingRepository serviceBindingRepository

    @AutoConfigureOrder
    private ServiceInstanceRepository serviceInstanceRepository

    private final String BINDING = "binding"

    private long totalSuccessfulNrOfBindings
    private HashMap<String, Long> totalSuccessfulBindingsPerService


    void retrieveMetricsForTotalNrOfBindings() {

        def it = getServiceBindingIterator()
        totalSuccessfulNrOfBindings = it.size()

        log.info("Total nr of provision requests: ${totalSuccessfulNrOfBindings}")

    }

    void retrieveTotalBindingsPerService(){
        HashMap<String, Long> totalHm = new HashMap<>()

        def it = getServiceBindingIterator()
        while(it.hasNext()) {
            def serviceBinding = it.next()
            def serviceInstance = serviceBinding.serviceInstance
            def service = serviceInstance.plan.service
            def serviceName = "someService"
            if(service) {
                serviceName = service.name
            }
            totalHm = addEntryToHm(totalHm, serviceName)
        }
        totalSuccessfulBindingsPerService = totalHm
        log.info("${tag()} total bindings per service: ${totalSuccessfulBindingsPerService}")
    }

    ListIterator<ServiceBinding> getServiceBindingIterator() {
        def allServiceBindings = serviceBindingRepository.findAll()
        return allServiceBindings.listIterator()
    }

    @Override
    Collection<Metric<?>> metrics() {
        List<Metric<?>> metrics = new ArrayList<>()

        retrieveMetricsForTotalNrOfBindings()
        metrics.add(new Metric<Long>("${BINDING}.${TOTAL}.${TOTAL}", totalSuccessfulNrOfBindings))

        retrieveTotalBindingsPerService()
        metrics = addCountersFromHashMapToMetrics(totalSuccessfulBindingsPerService, totalSuccessfulBindingsPerService, metrics, BINDING, SERVICE, SUCCESS)

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
