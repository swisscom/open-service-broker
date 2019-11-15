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
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@CompileStatic
@Slf4j
class BindingMetricsService extends PlanBasedMetricsService implements BindingMetricService {

    static String BINDING_SERVICE_KEY = "ServiceBindings"
    static String NEW_SERVICE_BINDINGS_KEY = "NewServiceBindings"

    private static final Map<String, Counter> succeededCounterByPlanGuid = new HashMap<String, Counter>()
    private static final Map<String, Counter> failedCounterByPlanGuid = new HashMap<String, Counter>()

    @Autowired
    protected BindingMetricsService(MeterRegistry meterRegistry, MetricsCache metricsCache, ServiceBrokerMetricsConfig serviceBrokerMetricsConfig) {
        super(meterRegistry, metricsCache, serviceBrokerMetricsConfig)
    }

    @Override
    void bindMetricsPerPlan(Plan plan) {
        addMetricsGauge(plan, BINDING_SERVICE_KEY, { metricsCache.bindingCountByPlanGuid.get(plan.guid, 0.0D) })
        succeededCounterByPlanGuid.put(plan.guid, createMetricsCounter(plan, NEW_SERVICE_BINDINGS_KEY, ["status": "completed"]))
        failedCounterByPlanGuid.put(plan.guid, createMetricsCounter(plan, NEW_SERVICE_BINDINGS_KEY, ["status": "failed"]))
    }

    void notifyBinding(String planGuid, boolean succeeded) {
        def counterMap = succeeded ? succeededCounterByPlanGuid : failedCounterByPlanGuid

        if (counterMap.containsKey(planGuid)) {
            counterMap.get(planGuid).increment()
        }
    }
    }
