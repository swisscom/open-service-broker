/*
 * Copyright (c) 2018 Swisscom (Switzerland) Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.swisscom.cloud.sb.broker.metrics

import com.swisscom.cloud.sb.broker.model.Plan
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.stereotype.Component

class LastOperationMetricsService extends PlanBasedMetricsService implements LastOperationMetricService {
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
