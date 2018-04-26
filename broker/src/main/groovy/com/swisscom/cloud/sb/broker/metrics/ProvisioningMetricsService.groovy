package com.swisscom.cloud.sb.broker.metrics

import com.swisscom.cloud.sb.broker.async.job.JobManager
import com.swisscom.cloud.sb.broker.model.repository.ServiceInstanceRepository
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.actuate.metrics.Metric
import org.springframework.stereotype.Service

@Service
@CompileStatic
@Slf4j
class ProvisioningMetricsService extends ServiceBrokerMetrics {

    @Autowired
    protected JobManager jobManager

    @Autowired
    private ServiceInstanceRepository serviceInstanceRepository

    private final String PROVISION_REQUEST = "provisionRequest"
    private final String PROVISIONED_INSTANCES = "provisionedInstances"

    private long totalNrOfProvisionRequests
    private HashMap<String, Long> totalNrOfProvisionRequestsPerService
    private HashMap<String, Long> totalNrOfProvisionRequestsPerPlan

    private long totalNrOfSuccessfulProvisionRequests
    private HashMap<String, Long> totalNrOfSuccessfulProvisionRequestsPerService
    private HashMap<String, Long> totalNrOfSuccessfulProvisionRequestsPerPlan

    private long totalNrOfFailedProvisionRequests
    private HashMap<String, Long> totalNrOfFailedProvisionRequestsPerService
    private HashMap<String, Long> totalNrOfFailedProvisionRequestsPerPlan

    private long totalNrOfProvisionedServiceInstances
    private HashMap<String, Long> totalNrOfProvisionedServiceInstancesPerService
    private HashMap<String, Long> totalNrOfProvisionedServiceInstancesPerPlan

    private long totalNrOfSuccessfullyProvisionedServiceInstances
    private HashMap<String, Long> totalNrOfSuccessfullyProvisionedServiceInstancesPerService
    private HashMap<String, Long> totalNrOfSuccessfullyProvisionedServiceInstancesPerPlan

    private long totalNrOfFailedProvisionedServiceInstances
    private HashMap<String, Long> totalNrOfFailedProvisionedServiceInstancesPerService
    private HashMap<String, Long> totalNrOfFailedProvisionedServiceInstancesPerPlan

    void retrieveMetricsForTotalNrOfProvisionRequests() {
        def totalCounter = 0
        def successCounter = 0
        def failCounter = 0

        def allServiceInstances = serviceInstanceRepository.findAll()
        def it = allServiceInstances.listIterator()
        while (it.hasNext()) {
            def serviceInstance = it.next()
            totalCounter++
            if (serviceInstance.completed) {
                successCounter++
            } else if (!checkIfNotCompletedProvisionIsInProgress(serviceInstance.guid)) {
                // only failed provisions are counted, the ones in progress are ignored
                failCounter++
            }
        }
        totalNrOfProvisionRequests = totalCounter
        log.info("Total nr of provision requests: ${totalNrOfProvisionRequests}")

        totalNrOfSuccessfulProvisionRequests = successCounter
        log.info("Total nr of successful provision requests: ${totalNrOfSuccessfulProvisionRequests}")

        totalNrOfFailedProvisionRequests = failCounter
        log.info("Total nr of failed provision requests: ${totalNrOfFailedProvisionRequests}")
    }

    void retrieveMetricsForTotalNrOfProvisionedServiceInstances() {
        def totalCounter = 0
        def successCounter = 0
        def failCounter = 0

        def allServiceInstances = serviceInstanceRepository.findAll()
        def it = allServiceInstances.listIterator()
        while (it.hasNext()) {
            def serviceInstance = it.next()
            if (!serviceInstance.deleted) {
                totalCounter++
                if (serviceInstance.completed) {
                    successCounter++
                } else if (!checkIfNotCompletedProvisionIsInProgress(serviceInstance.guid)) {
                    // only failed provisions are counted, the ones in progress are ignored
                    failCounter++
                }
            }
        }
        totalNrOfProvisionedServiceInstances = totalCounter
        log.info("Total nr of provisioned service instances: ${totalNrOfProvisionedServiceInstances}")

        totalNrOfSuccessfullyProvisionedServiceInstances = successCounter
        log.info("Total nr of successfully provisioned service instances: ${totalNrOfSuccessfullyProvisionedServiceInstances}")

        totalNrOfFailedProvisionedServiceInstances = failCounter
        log.info("Total nr of failed provisioned service instances: ${totalNrOfFailedProvisionedServiceInstances}")
    }


    void retrieveMetricsForTotalNrOfProvisionRequestsPerService() {
        HashMap<String, Long> totalHm = new HashMap<>()
        HashMap<String, Long> successHm = new HashMap<>()
        HashMap<String, Long> failHm = new HashMap<>()

        def allServiceInstances = serviceInstanceRepository.findAll()
        def it = allServiceInstances.listIterator()
        while (it.hasNext()) {
            def serviceInstance = it.next()
            def serviceName ="someService"
            if(serviceInstance.plan.service) {
                serviceName = serviceInstance.plan.service.name
            }
            totalHm = addEntryToHm(totalHm, serviceName)
            if (serviceInstance.completed) {
                successHm = addEntryToHm(successHm, serviceName)
            } else if (!checkIfNotCompletedProvisionIsInProgress(serviceInstance.guid)) {
                // only failed provisions are counted, the ones in progress are ignored
                failHm = addEntryToHm(failHm, serviceName)
            }
        }
        totalNrOfProvisionRequestsPerService = totalHm
        totalNrOfSuccessfulProvisionRequestsPerService = successHm
        totalNrOfFailedProvisionRequestsPerService = failHm
    }

    void retrieveMetricsForTotalNrOfProvisionedServiceInstancesPerService() {
        HashMap<String, Long> totalHm = new HashMap<>()
        HashMap<String, Long> successHm = new HashMap<>()
        HashMap<String, Long> failHm = new HashMap<>()

        def allServiceInstances = serviceInstanceRepository.findAll()
        def it = allServiceInstances.listIterator()
        while (it.hasNext()) {
            def serviceInstance = it.next()
            def serviceName = "someService"
            if(serviceInstance.plan.service) {
                serviceName = serviceInstance.plan.service.name
            }
            if (!serviceInstance.deleted) {
                totalHm = addEntryToHm(totalHm, serviceName)
                if (serviceInstance.completed) {
                    successHm = addEntryToHm(successHm, serviceName)
                } else if (!checkIfNotCompletedProvisionIsInProgress(serviceInstance.guid)) {
                    // only failed provisions are counted, the ones in progress are ignored
                    failHm = addEntryToHm(failHm, serviceName)
                }
            }
        }
        totalNrOfProvisionedServiceInstancesPerService = totalHm
        totalNrOfSuccessfullyProvisionedServiceInstancesPerService = successHm
        totalNrOfFailedProvisionedServiceInstancesPerService = failHm
    }

    void retrieveMetricsForTotalNrOfProvisionRequestsPerPlan() {
        HashMap<String, Long> totalHm = new HashMap<>()
        HashMap<String, Long> successHm = new HashMap<>()
        HashMap<String, Long> failHm = new HashMap<>()

        def allServiceInstances = serviceInstanceRepository.findAll()
        def it = allServiceInstances.listIterator()
        while (it.hasNext()) {
            def serviceInstance = it.next()
            def plan = serviceInstance.plan.name
            totalHm = addEntryToHm(totalHm, plan)
            if (serviceInstance.completed) {
                successHm = addEntryToHm(successHm, plan)
            } else if (!checkIfNotCompletedProvisionIsInProgress(serviceInstance.guid)) {
                // only failed provisions are counted, the ones in progress are ignored
                failHm = addEntryToHm(failHm, plan)
            }
        }
        totalNrOfProvisionRequestsPerPlan = totalHm
        totalNrOfSuccessfulProvisionRequestsPerPlan = successHm
        totalNrOfFailedProvisionRequestsPerPlan = failHm
    }

    void retrieveMetricsForTotalNrOfProvisionedServiceInstancesPerPlan() {
        HashMap<String, Long> totalHm = new HashMap<>()
        HashMap<String, Long> successHm = new HashMap<>()
        HashMap<String, Long> failHm = new HashMap<>()

        def allServiceInstances = serviceInstanceRepository.findAll()
        def it = allServiceInstances.listIterator()
        while (it.hasNext()) {
            def serviceInstance = it.next()
            def plan = serviceInstance.plan.name
            if (!serviceInstance.deleted) {
                totalHm = addEntryToHm(totalHm, plan)
                if (serviceInstance.completed) {
                    successHm = addEntryToHm(successHm, plan)
                } else if (!checkIfNotCompletedProvisionIsInProgress(serviceInstance.guid)) {
                    // only failed provisions are counted, the ones in progress are ignored
                    failHm = addEntryToHm(failHm, plan)
                }
            }
        }
        totalNrOfProvisionedServiceInstancesPerPlan = totalHm
        totalNrOfSuccessfullyProvisionedServiceInstancesPerPlan = successHm
        totalNrOfFailedProvisionedServiceInstancesPerPlan = failHm
    }


    @Override
    Collection<Metric<?>> metrics() {
        List<Metric<?>> metrics = new ArrayList<>()

        retrieveMetricsForTotalNrOfProvisionRequests()
        metrics.add(new Metric<Long>("${PROVISION_REQUEST}.${TOTAL}.${TOTAL}", totalNrOfProvisionRequests))
        metrics.add(new Metric<Long>("${PROVISION_REQUEST}.${TOTAL}.${SUCCESS}", totalNrOfSuccessfulProvisionRequests))
        metrics.add(new Metric<Long>("${PROVISION_REQUEST}.${TOTAL}.${FAIL}", totalNrOfFailedProvisionRequests))
        metrics.add(new Metric<Double>("${PROVISION_REQUEST}.${SUCCESS}.${RATIO}", calculateRatio(totalNrOfProvisionRequests, totalNrOfSuccessfulProvisionRequests)))
        metrics.add(new Metric<Double>("${PROVISION_REQUEST}.${FAIL}.${RATIO}", calculateRatio(totalNrOfProvisionRequests, totalNrOfFailedProvisionRequests)))

        retrieveMetricsForTotalNrOfProvisionedServiceInstances()
        metrics.add(new Metric<Long>("${PROVISIONED_INSTANCES}.${TOTAL}.${TOTAL}", totalNrOfProvisionedServiceInstances))
        metrics.add(new Metric<Long>("${PROVISIONED_INSTANCES}.${TOTAL}.${SUCCESS}", totalNrOfSuccessfullyProvisionedServiceInstances))
        metrics.add(new Metric<Long>("${PROVISIONED_INSTANCES}.${TOTAL}.${FAIL}", totalNrOfFailedProvisionedServiceInstances))
        metrics.add(new Metric<Double>("${PROVISIONED_INSTANCES}.${SUCCESS}.${RATIO}", calculateRatio(totalNrOfProvisionedServiceInstances, totalNrOfSuccessfullyProvisionedServiceInstances)))
        metrics.add(new Metric<Double>("${PROVISIONED_INSTANCES}.${FAIL}.${RATIO}", calculateRatio(totalNrOfProvisionedServiceInstances, totalNrOfFailedProvisionedServiceInstances)))

        retrieveMetricsForTotalNrOfProvisionRequestsPerService()
        metrics = addCountersFromHashMapToMetrics(totalNrOfProvisionRequestsPerService, totalNrOfProvisionRequestsPerService, metrics, PROVISION_REQUEST, SERVICE, TOTAL)
        metrics = addCountersFromHashMapToMetrics(totalNrOfProvisionRequestsPerService, totalNrOfSuccessfulProvisionRequestsPerService, metrics, PROVISION_REQUEST, SERVICE, SUCCESS)
        metrics = addCountersFromHashMapToMetrics(totalNrOfProvisionRequestsPerService, totalNrOfFailedProvisionRequestsPerService, metrics, PROVISION_REQUEST, SERVICE, FAIL)

        retrieveMetricsForTotalNrOfProvisionedServiceInstancesPerService()
        metrics = addCountersFromHashMapToMetrics(totalNrOfProvisionedServiceInstancesPerService, totalNrOfProvisionedServiceInstancesPerService, metrics, PROVISIONED_INSTANCES, SERVICE, TOTAL)
        metrics = addCountersFromHashMapToMetrics(totalNrOfProvisionedServiceInstancesPerService, totalNrOfSuccessfullyProvisionedServiceInstancesPerService, metrics, PROVISIONED_INSTANCES, SERVICE, SUCCESS)
        metrics = addCountersFromHashMapToMetrics(totalNrOfProvisionedServiceInstancesPerService, totalNrOfFailedProvisionedServiceInstancesPerService, metrics, PROVISIONED_INSTANCES, SERVICE, FAIL)

        retrieveMetricsForTotalNrOfProvisionRequestsPerPlan()
        metrics = addCountersFromHashMapToMetrics(totalNrOfProvisionRequestsPerPlan, totalNrOfProvisionRequestsPerPlan, metrics, PROVISION_REQUEST, PLAN, TOTAL)
        metrics = addCountersFromHashMapToMetrics(totalNrOfProvisionRequestsPerPlan, totalNrOfSuccessfulProvisionRequestsPerPlan, metrics, PROVISION_REQUEST, PLAN, SUCCESS)
        metrics = addCountersFromHashMapToMetrics(totalNrOfProvisionRequestsPerPlan, totalNrOfFailedProvisionRequestsPerPlan, metrics, PROVISION_REQUEST, PLAN, FAIL)

        retrieveMetricsForTotalNrOfProvisionedServiceInstancesPerPlan()
        metrics = addCountersFromHashMapToMetrics(totalNrOfProvisionedServiceInstancesPerPlan, totalNrOfProvisionedServiceInstancesPerPlan, metrics, PROVISIONED_INSTANCES, PLAN, TOTAL)
        metrics = addCountersFromHashMapToMetrics(totalNrOfProvisionedServiceInstancesPerPlan, totalNrOfSuccessfullyProvisionedServiceInstancesPerPlan, metrics, PROVISIONED_INSTANCES, PLAN, SUCCESS)
        metrics = addCountersFromHashMapToMetrics(totalNrOfProvisionedServiceInstancesPerPlan, totalNrOfFailedProvisionedServiceInstancesPerPlan, metrics, PROVISIONED_INSTANCES, PLAN, FAIL)

        def influx = new InfluxDBConnector().influxMetricsWriter()
        for(Metric<?> m: metrics) {
            influx.set(m)
        }

        return metrics
    }
}