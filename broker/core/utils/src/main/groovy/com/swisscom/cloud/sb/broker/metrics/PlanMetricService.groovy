package com.swisscom.cloud.sb.broker.metrics

import com.swisscom.cloud.sb.broker.model.Plan

interface PlanMetricService {
    void bindMetricsPerPlan(Plan plan)
}