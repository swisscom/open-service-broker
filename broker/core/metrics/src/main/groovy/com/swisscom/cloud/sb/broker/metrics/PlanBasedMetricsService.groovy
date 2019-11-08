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

//FIXME For modularizing the metrics com.swisscom.cloud.sb.broker.servicedefinition.ServiceDefinitionProcessor is not depending in PlanBasedMetrics anymore
abstract class PlanBasedMetricsService extends ServiceBrokerMetricsService {
    protected PlanBasedMetricsService(
            MeterRegistry meterRegistry,
            MetricsCache metricsCache,
            ServiceBrokerMetricsConfig serviceBrokerMetricsConfig) {
        super(meterRegistry, metricsCache, serviceBrokerMetricsConfig)
    }

    void addMetricsGauge(Plan plan, String name, Closure<Double> function, Map<String, String> tags = new HashMap<String, String>()) {
        super.addMetricsGauge(name, function, withPlanTags(plan, tags))
    }

    Counter createMetricsCounter(Plan plan, String name, Map<String, String> tags = new HashMap<String, String>()) {
        super.createMetricsCounter(name, withPlanTags(plan, tags))
    }

    private HashMap<String, String> withPlanTags(Plan plan, HashMap<String, String> tags) {
        tags.put("serviceName", plan.service.name)
        tags.put("planName", plan.name)

        tags
    }

    abstract void bindMetricsPerPlan(Plan plan)

    void bindAll() {
        metricsCache.listOfPlans.each { p -> bindMetricsPerPlan(p) }
    }
}