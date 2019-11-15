package com.swisscom.cloud.sb.broker.metrics

import com.swisscom.cloud.sb.broker.model.Plan
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class NoOpPlanMetricService implements PlanMetricService {
    private final static Logger LOGGER = LoggerFactory.getLogger(NoOpPlanMetricService.class)

    @Override
    void bindMetricsPerPlan(Plan plan) {
        LOGGER.debug("bindMetricsPerPlan({})", plan)
    }
}
