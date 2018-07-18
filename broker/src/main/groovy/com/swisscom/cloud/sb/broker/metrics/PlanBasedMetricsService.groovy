package com.swisscom.cloud.sb.broker.metrics

import com.swisscom.cloud.sb.broker.model.Plan
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry

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