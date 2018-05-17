package com.swisscom.cloud.sb.broker.metrics

import com.swisscom.cloud.sb.broker.model.LastOperation
import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.broker.model.repository.LastOperationRepository
import com.swisscom.cloud.sb.broker.model.repository.ServiceInstanceRepository
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.micrometer.core.instrument.Clock
import io.micrometer.core.instrument.Meter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.influx.InfluxConfig
import io.micrometer.influx.InfluxMeterRegistry
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.actuate.endpoint.PublicMetrics
import org.springframework.boot.actuate.metrics.Metric

import java.time.Duration

@CompileStatic
@Slf4j
abstract class ServiceBrokerMetrics implements PublicMetrics {

    protected final String SUCCESS = "success"
    protected final String FAIL = "fail"
    protected final String TOTAL = "total"
    protected final String SERVICE = "service"
    protected final String PLAN = "plan"
    protected final String RATIO = "ratio"

    protected ServiceInstanceRepository serviceInstanceRepository
    protected LastOperationRepository lastOperationRepository

    @Autowired
    ServiceBrokerMetrics(ServiceInstanceRepository serviceInstanceRepository, LastOperationRepository lastOperationRepository) {
        this.serviceInstanceRepository = serviceInstanceRepository
        this.lastOperationRepository = lastOperationRepository
    }

    abstract void addMetricsToMeterRegistry(MeterRegistry meterRegistry)

    abstract Collection<Metric<?>> metrics()

    protected InfluxMeterRegistry configureInfluxMeterRegistry() {
        new InfluxMeterRegistry(new InfluxConfig() {
            @Override
            public Duration step() {
                return Duration.ofSeconds(5);
            }

            @Override
            public String db() {
                return "mydb";
            }

            @Override
            public String get(String k) {
                return null; // accept the rest of the defaults
            }
        }, Clock.SYSTEM)
    }

    protected MetricsResult retrieveTotalMetrics(List<ServiceInstance> serviceInstanceList) {
        def totalCounter = 0
        def successCounter = 0
        def failCounter = 0

        serviceInstanceList.each { serviceInstance ->
            if (considerServiceInstance(serviceInstance)) {
                totalCounter++
                if (serviceInstance.completed) {
                    successCounter++
                } else if (!checkIfNotCompletedProvisionIsInProgress(serviceInstance.guid)) {
                    // only failed provisions are counted, the ones in progress are ignored
                    failCounter++
                }
            }
        }

        log.info("${tag()} total: ${totalCounter}")
        log.info("${tag()} total success: ${successCounter}")
        log.info("${tag()} total failure: ${failCounter}")

        return new MetricsResult(total: totalCounter, totalSuccess: successCounter, totalFailures: failCounter)
    }

    MetricsResultMap retrieveTotalMetricsPerService(List<ServiceInstance> serviceInstanceList) {
        HashMap<String, Long> totalHm = new HashMap<>()
        HashMap<String, Long> successHm = new HashMap<>()
        HashMap<String, Long> failHm = new HashMap<>()

        serviceInstanceList.each { serviceInstance ->
            def serviceName = getServiceName(serviceInstance)
            if (considerServiceInstance(serviceInstance)) {
                totalHm = addOrUpdateEntryOnHashMap(totalHm, serviceName)
                if (serviceInstance.completed) {
                    successHm = addOrUpdateEntryOnHashMap(successHm, serviceName)
                } else if (!checkIfNotCompletedProvisionIsInProgress(serviceInstance.guid)) {
                    // only failed provisions are counted, the ones in progress are ignored
                    failHm = addOrUpdateEntryOnHashMap(failHm, serviceName)
                }
            }
        }

        log.info("${tag()} total service: ${totalHm}")
        log.info("${tag()} total success service: ${successHm}")
        log.info("${tag()} total failure service: ${failHm}")

        return new MetricsResultMap(total: totalHm, totalSuccess: successHm, totalFailures: failHm)
    }

    MetricsResultMap retrieveTotalMetricsPerPlan(List<ServiceInstance> serviceInstanceList) {
        HashMap<String, Long> totalHm = new HashMap<>()
        HashMap<String, Long> successHm = new HashMap<>()
        HashMap<String, Long> failHm = new HashMap<>()

        serviceInstanceList.each { serviceInstance ->
            def planName = serviceInstance.plan.name
            if (considerServiceInstance(serviceInstance)) {
                totalHm = addOrUpdateEntryOnHashMap(totalHm, planName)
                if (serviceInstance.completed) {
                    successHm = addOrUpdateEntryOnHashMap(successHm, planName)
                } else if (!checkIfNotCompletedProvisionIsInProgress(serviceInstance.guid)) {
                    // only failed provisions are counted, the ones in progress are ignored
                    failHm = addOrUpdateEntryOnHashMap(failHm, planName)
                }
            }
        }

        log.info("${tag()} total: ${totalHm}")
        log.info("${tag()} total success plan: ${successHm}")
        log.info("${tag()} total failure plan: ${failHm}")

        return new MetricsResultMap(total: totalHm, totalSuccess: successHm, totalFailures: failHm)
    }

    abstract boolean considerServiceInstance(ServiceInstance serviceInstance)

    abstract String tag()

    HashMap<String, Long> addOrUpdateEntryOnHashMap(HashMap<String, Long> hm, String key) {
        if (hm.get(key) == null) {
            hm.put(key, 1L)
        } else {
            def currentCounter = hm.get(key)
            def newCounter = currentCounter + 1
            hm.put(key, newCounter)
        }
        return hm
    }

    String getServiceName(ServiceInstance serviceInstance) {
        def cfService = serviceInstance.plan.service
        def cfServiceName = "someService"
        if (cfService) {
            cfServiceName = cfService.name
        }
        return cfServiceName
    }

    /*
    * The reason for checking for IN_PROGRESS rather than for FAILED is that when a service instance has been
    * provisioned unsuccessfully and is then deprovisioned, the operation in the lastOperationRepository is changed
    * from PROVISION to DEPROVISION and if the deprovisioning is successful, the status is set to SUCCESS, therefore
    * failed provisionings cannot be detected once they have been followed by deprovisioning. By checking for IN_PROGRESS
    * the case that a service instance is still being successfully provisioned is excluded from failures
    */

    boolean checkIfNotCompletedProvisionIsInProgress(String serviceInstanceGuid) {
        def lastOperation = lastOperationRepository.findByGuid(serviceInstanceGuid)
        return lastOperation.status == LastOperation.Status.IN_PROGRESS
    }

    double calculateRatio(long total, long amount) {
        return 100 / total * amount
    }

    List<Metric<?>> addCountersFromHashMapToMetrics(HashMap<String, Long> totalhm, HashMap<String, Long> hm, List<Metric<?>> metrics, String kind, String level, String qualifier) {
        for (Map.Entry<String, Long> entry : hm.entrySet()) {
            metrics.add(new Metric<Long>("${kind}.${level}.${qualifier}.${entry.getKey()}", entry.getValue()))
            if (totalhm != hm) {
                def total = totalhm.get(entry.getKey())
                metrics.add(new Metric<Double>("${kind}.${level}.${entry.getKey()}.${qualifier}.${RATIO}", calculateRatio(total, entry.getValue())))
            }
        }
        return metrics
    }
}