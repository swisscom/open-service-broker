package com.swisscom.cloud.sb.broker.metrics

import com.swisscom.cloud.sb.broker.model.Plan
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.stereotype.Component

@Component
class LastOperationMetricsService extends PlanBasedMetricsService {
    static String LAST_OPERATIONS = "LastOperations"
    static String LAST_OPERATION_EVENTS = "LastOperationEvents"

    private static final Map<String, Counter> succeededCounterByPlanGuid = new HashMap<String, Counter>()
    private static final Map<String, Counter> failedWithTimeoutCounterByPlanGuid = new HashMap<String, Counter>()
    private static final Map<String, Counter> failedWithExceptionByPlanGuid = new HashMap<String, Counter>()
    private static final Map<String, Counter> failedByServiceProvider = new HashMap<String, Counter>()

    protected LastOperationMetricsService(
            MeterRegistry meterRegistry,
            MetricsCache metricsCache,
            ServiceBrokerMetricsConfig serviceBrokerMetricsConfig) {
        super(meterRegistry, metricsCache, serviceBrokerMetricsConfig)
    }

    void notifySucceeded(String planGuid) {
        incrementCounter(succeededCounterByPlanGuid, planGuid)
    }

    void notifyFailedWithTimeout(String planGuid) {
        incrementCounter(failedWithTimeoutCounterByPlanGuid, planGuid)
    }

    void notifyFailedWithException(String planGuid) {
        incrementCounter(failedWithExceptionByPlanGuid, planGuid)
    }

    void notifyFailedByServiceProvider(String planGuid) {
        incrementCounter(failedByServiceProvider, planGuid)
    }

    private void incrementCounter(Map<String, Counter> counterMap, String planGuid) {
        if (counterMap.containsKey(planGuid))
            counterMap.get(planGuid).increment()
    }

    @Override
    void bindMetricsPerPlan(Plan plan) {
        succeededCounterByPlanGuid.put(plan.guid, createMetricsCounter(plan, LAST_OPERATION_EVENTS, ["status": "completed"]))
        failedWithTimeoutCounterByPlanGuid.put(plan.guid, createMetricsCounter(plan, LAST_OPERATION_EVENTS, ["status": "failed", "failureReason": "timeout"]))
        failedWithExceptionByPlanGuid.put(plan.guid, createMetricsCounter(plan, LAST_OPERATION_EVENTS, ["status": "failed", "failureReason": "exception"]))
        failedByServiceProvider.put(plan.guid, createMetricsCounter(plan, LAST_OPERATION_EVENTS, ["status": "failed", "failureReason": "serviceprovider"]))
        addMetricsGauge(plan, LAST_OPERATIONS,{ metricsCache.getFailedLastOperationCount(plan.guid) }, ["status": "failed"])
    }
}
