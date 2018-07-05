package com.swisscom.cloud.sb.broker.metrics

import com.swisscom.cloud.sb.broker.model.CFService
import com.swisscom.cloud.sb.broker.model.Plan
import com.swisscom.cloud.sb.broker.model.ServiceBinding
import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.broker.model.repository.CFServiceRepository
import com.swisscom.cloud.sb.broker.model.repository.PlanRepository
import com.swisscom.cloud.sb.broker.model.repository.ServiceBindingRepository
import com.swisscom.cloud.sb.broker.model.repository.ServiceInstanceRepository
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import java.time.LocalDateTime

@Component
@CompileStatic
@Slf4j
class MetricsCache {

    @Autowired
    ServiceInstanceRepository serviceInstanceRepository
    @Autowired
    CFServiceRepository cfServiceRepository
    @Autowired
    ServiceBindingRepository serviceBindingRepository
    @Autowired
    PlanRepository planRepository

    @Autowired
    ServiceBrokerMetricsConfig serviceBrokerMetricsConfig

    static final List<ServiceInstance> serviceInstanceList = new ArrayList<>()
    private static LocalDateTime serviceInstanceListLastModified = LocalDateTime.now()
    static final List<ServiceBinding> serviceBindingList = new ArrayList<>()
    private static LocalDateTime serviceBindingListLastModified = LocalDateTime.now()
    static final List<CFService> cfServiceList = new ArrayList<>()
    private static LocalDateTime cfServiceListLastModified = LocalDateTime.now()
    static final List<Plan> planList = new ArrayList<>()
    private static LocalDateTime planListLastModified = LocalDateTime.now()

    List<ServiceInstance> getServiceInstanceList() {
        synchronized (serviceInstanceList) {
            int timeoutInSeconds = Integer.parseInt(serviceBrokerMetricsConfig.step.substring(0, serviceBrokerMetricsConfig.step.length()-1))
            if (serviceInstanceListLastModified.isBefore(LocalDateTime.now().minusSeconds(timeoutInSeconds))) {
                serviceInstanceList.clear()
                log.info("Id of repo: ${java.lang.System.identityHashCode(serviceInstanceRepository)}")
                if(serviceInstanceRepository.findAll().size()> 0)
                log.info("Value: ${serviceInstanceRepository.findAll().get(serviceInstanceRepository.findAll().size()-1)}")
                serviceInstanceList.addAll(serviceInstanceRepository.findAll())
                serviceInstanceListLastModified = LocalDateTime.now()
                log.info("actualised serviceInstanceList")
            }

            log.info("Id of listInstance: ${java.lang.System.identityHashCode(serviceInstanceList)}")
            return serviceInstanceList
        }
    }

    List<ServiceBinding> getServiceBindingList() {
        synchronized (serviceBindingList) {
            int timeoutInSeconds = Integer.parseInt(serviceBrokerMetricsConfig.step.substring(0, serviceBrokerMetricsConfig.step.length()-1))
            if (serviceBindingListLastModified.isBefore(LocalDateTime.now().minusSeconds(timeoutInSeconds))) {
                serviceBindingList.clear()
                serviceBindingList.addAll(serviceBindingRepository.findAll())
                serviceBindingListLastModified = LocalDateTime.now()
                log.info("actualised serviceBindingList")
            }
            return serviceBindingList
        }
    }

    List<CFService> getCfServiceList() {
        synchronized (cfServiceList) {
            int timeoutInSeconds = Integer.parseInt(serviceBrokerMetricsConfig.step.substring(0, serviceBrokerMetricsConfig.step.length()-1))
            if (cfServiceListLastModified.isBefore(LocalDateTime.now().minusSeconds(timeoutInSeconds))) {
                cfServiceList.clear()
                cfServiceList.addAll(cfServiceRepository.findAll())
                cfServiceListLastModified = LocalDateTime.now()
                log.info("actualised cfServiceList")
            }
            return cfServiceList
        }
    }

    List<Plan> getPlanList() {
        synchronized (planList) {
            int timeoutInSeconds = Integer.parseInt(serviceBrokerMetricsConfig.step.substring(0, serviceBrokerMetricsConfig.step.length()-1))
            if (planListLastModified.isBefore(LocalDateTime.now().minusSeconds(timeoutInSeconds))) {
                planList.clear()
                planList.addAll(planRepository.findAll())
                planListLastModified = LocalDateTime.now()
                log.info("actualised planList")
            }
            return planList
        }
    }
}
